package com.microsoft.applicationinsights.channel;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.microsoft.applicationinsights.util.ThreadPoolUtils;

/**
 * The class is responsible for de-coupling the file persist activity.
 *
 * When this class is called it will use a thread pool's thread to do the persistence
 *
 * Created by gupele on 12/22/2014.
 */
public class ActiveTransmissionFileSystemOutput implements TransmissionOutput {
    private final ThreadPoolExecutor threadPool;

    private final TransmissionOutput actualOutput;

    public ActiveTransmissionFileSystemOutput(TransmissionOutput actualOutput) {
        this.actualOutput = actualOutput;
        threadPool = ThreadPoolUtils.newLimitedThreadPool(1, 3, 20L, 1024);
    }

    @Override
    public boolean send(final Transmission transmission) {
        try {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        actualOutput.send(transmission);
                    } catch (Exception e) {
                        // Do nothing
                        // The expectation is that the 'actual output' will consume all exceptions
                    }
                }
            });
            return true;
        } catch (RejectedExecutionException e) {
            // Note that currently if we cannot put the job to work we drop
            // the transmission, we need to add internal logging for that case
            // TODO: log
        } catch (Exception e) {
        }

        return false;
    }

    @Override
    public void stop(long timeout, TimeUnit timeUnit) {
        actualOutput.stop(timeout, timeUnit);
        ThreadPoolUtils.stop(threadPool, timeout, timeUnit);
    }
}
