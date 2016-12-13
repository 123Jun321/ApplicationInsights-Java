/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.internal.perfcounter;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.microsoft.applicationinsights.internal.util.LocalStringsUtils;
import org.apache.http.client.methods.HttpPost;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.internal.channel.common.ApacheSender;
import com.microsoft.applicationinsights.internal.channel.common.ApacheSenderFactory;
import com.microsoft.applicationinsights.internal.logger.InternalLogger;
import com.microsoft.applicationinsights.internal.shutdown.SDKShutdownActivity;
import com.microsoft.applicationinsights.internal.shutdown.Stoppable;
import com.microsoft.applicationinsights.internal.util.DeviceInfo;

/**
 * Created by gupele on 12/4/2016.
 */
public enum QuickPulse implements Stoppable {
    INSTANCE;

    private final static long DEFAULT_WAIT_BETWEEN_PING_IN_MS = 5000;
    private final static long DEFAULT_WAIT_BETWEEN_POSTS_IN_MS = 1000;
    private final static long WAIT_BETWEEN_PINGS_AFTER_ERROR_IN_MS = 60000;

    private class Coordinator implements Runnable {
        private volatile boolean stopped = false;
        private volatile boolean pingMode = true;
        private final QuickPulsePingSender pingSender;
        private final QuickPulseDataFetcher dataFetcher;
        private final QuickPulseDataSender dataSender;
        private final ArrayBlockingQueue<HttpPost> sendQueue;

        public Coordinator(
                final ApacheSender apacheSender,
                final String ikey,
                final String quickPulseId,
                final QuickPulseDataSender dataSender,
                final ArrayBlockingQueue<HttpPost> sendQueue) {
            this.dataSender = dataSender;
            this.sendQueue = sendQueue;
            String instanceName = DeviceInfo.getHostName();
            if (LocalStringsUtils.isNullOrEmpty(instanceName)) {
                instanceName = "Unknown host";
            }

            pingSender = new QuickPulsePingSender(apacheSender, instanceName, quickPulseId);
            dataFetcher = new QuickPulseDataFetcher(sendQueue, ikey, instanceName, quickPulseId);
        }

        @Override
        public void run() {
            try {
                while (!stopped) {
                    long sleepInMS;
                    if (pingMode) {
                        sleepInMS = ping();
                    } else {
                        sleepInMS = sendData();
                    }
                    try {
                        Thread.sleep(sleepInMS);
                    } catch (InterruptedException e) {
                    }
                }
            } catch (Throwable t) {
            }
        }

        private long sendData() {
            dataFetcher.prepareQuickPulseDataForSend();
            final QuickPulseNetworkHelper.QuickPulseStatus currentQPStatus = dataSender.getQuickPulseStatus();
            switch (currentQPStatus) {
                case ERROR:
                    pingMode = true;
                    return WAIT_BETWEEN_PINGS_AFTER_ERROR_IN_MS;

                case QP_IS_OFF:
                    pingMode = true;
                    return DEFAULT_WAIT_BETWEEN_PING_IN_MS;

                case QP_IS_ON:
                    return DEFAULT_WAIT_BETWEEN_POSTS_IN_MS;

                default:
                    InternalLogger.INSTANCE.logAlways(InternalLogger.LoggingLevel.ERROR, "Critical error while sending QP data: unknown status, aborting");
                    QuickPulseDataCollector.INSTANCE.disable();
                    stopped = true;
                    return 0;
            }
        }

        private long ping() {
            QuickPulseNetworkHelper.QuickPulseStatus pingResult = pingSender.ping();
            switch (pingResult) {
                case ERROR:
                    return WAIT_BETWEEN_PINGS_AFTER_ERROR_IN_MS;

                case QP_IS_ON:
                    pingMode = false;
                    dataSender.startSending();
                    return DEFAULT_WAIT_BETWEEN_POSTS_IN_MS;

                case QP_IS_OFF:
                    return DEFAULT_WAIT_BETWEEN_PING_IN_MS;

                default:
                    InternalLogger.INSTANCE.logAlways(InternalLogger.LoggingLevel.ERROR, "Critical error while ping QP: unknown status, aborting");
                    QuickPulseDataCollector.INSTANCE.disable();
                    stopped = true;
                    return 0;
            }
        }

        public void stop() {
            stopped = true;
        }
    }

    private volatile boolean initialized = false;
    private Thread thread;
    private Thread senderThread;
    private Coordinator coordinator;
    private ApacheSender apacheSender;
    private QuickPulseDataSender quickPulseDataSender;

    public void initialize() {
        if (!initialized) {
            synchronized (INSTANCE) {
                if (!initialized) {
                    initialized = true;
                    createWorkers();

                    QuickPulseDataCollector.INSTANCE.enable();
                }
            }
        }
    }

    /**
     * Stopping the collection of performance data.
     * @param timeout The timeout to wait for the stop to happen.
     * @param timeUnit The time unit to use when waiting for the stop to happen.
     */
    public synchronized void stop(long timeout, TimeUnit timeUnit) {
        if (!initialized) {
            return;
        }

        coordinator.stop();
        quickPulseDataSender.stop();

        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
        senderThread.interrupt();
        try {
            senderThread.join();
        } catch (InterruptedException e) {
        }

        initialized = false;
    }

    private void createWorkers() {
        final String quickPulseId = UUID.randomUUID().toString().replace("-", "");
        apacheSender = ApacheSenderFactory.INSTANCE.create();
        ArrayBlockingQueue<HttpPost> sendQueue = new ArrayBlockingQueue<HttpPost>(256, true);
        quickPulseDataSender = new QuickPulseDataSender(apacheSender, sendQueue);

        final String ikey = TelemetryConfiguration.getActive().getInstrumentationKey();
        coordinator = new Coordinator(apacheSender, ikey, quickPulseId, quickPulseDataSender, sendQueue);

        senderThread = new Thread(quickPulseDataSender);
        senderThread.setDaemon(true);
        senderThread.start();

        thread = new Thread(coordinator);
        thread.setDaemon(true);
        thread.start();

        SDKShutdownActivity.INSTANCE.register(this);
    }
}
