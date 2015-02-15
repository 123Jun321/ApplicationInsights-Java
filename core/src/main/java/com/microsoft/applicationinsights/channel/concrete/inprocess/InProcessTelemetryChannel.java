/*
 * AppInsights-Java
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

package com.microsoft.applicationinsights.channel.concrete.inprocess;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;
import com.microsoft.applicationinsights.internal.channel.TelemetriesTransmitter;
import com.microsoft.applicationinsights.internal.channel.OldTelemetriesTransmitter;
import com.microsoft.applicationinsights.internal.channel.TransmitterFactory;
import com.microsoft.applicationinsights.internal.channel.common.OldTelemetryBuffer;
import com.microsoft.applicationinsights.internal.channel.common.TelemetryBuffer;
import com.microsoft.applicationinsights.internal.logger.InternalLogger;
import com.microsoft.applicationinsights.internal.util.Sanitizer;
import com.microsoft.applicationinsights.telemetry.JsonTelemetryDataSerializer;
import com.microsoft.applicationinsights.telemetry.Telemetry;
import com.microsoft.applicationinsights.channel.TelemetryChannel;

import com.google.common.base.Preconditions;

/**
 * An implementation of {@link com.microsoft.applicationinsights.channel.TelemetryChannel}
 *
 * The channel holds two main entities:
 *
 * A buffer for incoming {@link com.microsoft.applicationinsights.telemetry.Telemetry} instances
 * A transmitter
 *
 * The buffer is stores incoming telemetry instances. Every new buffer starts a timer.
 * When the timer expires, or when the buffer is 'full' (whichever happens first), the
 * transmitter will pick up that buffer and will handle its sending to the server. For example,
 * a transmitter will be responsible for compressing, sending and activate a policy in case of failures.
 *
 * The model here is:
 *
 * Use application threads to populate the buffer
 * Use channel's threads to send buffers to the server
 *
 * Created by gupele on 12/17/2014.
 */
public final class InProcessTelemetryChannel implements TelemetryChannel {
    private final static int DEFAULT_NUMBER_OF_TELEMETRIES_PER_CONTAINER = 500;

    private final static int TRANSMIT_BUFFER_DEFAULT_TIMEOUT_IN_SECONDS = 10;

    private final static String DEVELOPER_MODE = "DeveloperMode";
    private final static String EndpointAddress = "EndpointAddress";

    private boolean developerMode = false;
    private static TransmitterFactory s_transmitterFactory;

    private boolean stopped = false;

    private TelemetriesTransmitter telemetriesTransmitter;
    private OldTelemetriesTransmitter oldTelemetriesTransmitter;

    private TelemetryBuffer telemetryBuffer;
    private OldTelemetryBuffer oldTelemetryBuffer;

    private boolean useOld;

    public InProcessTelemetryChannel() {
        this(null, false);
    }

    /**
     * Ctor
     * @param endpointAddress Must be empty string or a valid uri, else an exception will be thrown
     * @param developerMode True will behave in a 'non-production' mode to ease the debugging
     */
    public InProcessTelemetryChannel(String endpointAddress, boolean developerMode) {
        initialize(endpointAddress, developerMode);
    }

    /**
     * This Ctor will query the 'namesAndValues' map for data to initialize itself
     * It will ignore data that is not of its interest, this Ctor is useful for building an instance from configuration
     * @param nameAndValues - The data passed as name and value pairs
     */
    public InProcessTelemetryChannel(Map<String, String> nameAndValues) {
        boolean developerMode = false;
        String endpointAddress = null;

        if (nameAndValues != null) {
            developerMode = Boolean.valueOf(nameAndValues.get(DEVELOPER_MODE));
            endpointAddress = nameAndValues.get(EndpointAddress);
        }

        initialize(endpointAddress, developerMode);
    }

    /**
     *  Gets value indicating whether this channel is in developer mode.
     */
    @Override
    public boolean isDeveloperMode() {
        return developerMode;
    }

    /**
     * Sets value indicating whether this channel is in developer mode.
     * @param developerMode True or false
     */
    @Override
    public void setDeveloperMode(boolean developerMode) {
        if (developerMode != this.developerMode) {
            this.developerMode = developerMode;
            int maxTelemetriesInBatch = this.developerMode ? 1 : DEFAULT_NUMBER_OF_TELEMETRIES_PER_CONTAINER;

            if (useOld) {
                oldTelemetryBuffer.setMaxTelemetriesInBatch(maxTelemetriesInBatch);
            } else {
                telemetryBuffer.setMaxTelemetriesInBatch(maxTelemetriesInBatch);
            }
        }
    }

    /**
     *  Sends a Telemetry instance through the channel.
     */
    @Override
    public void send(Telemetry telemetry) {
        Preconditions.checkNotNull(telemetry, "Telemetry item must be non null");

        if (isDeveloperMode()) {
            telemetry.getContext().getProperties().put("DeveloperMode", "true");
        }

        if (useOld) {
            oldTelemetryBuffer.add(telemetry);
        } else {
            StringWriter writer = new StringWriter();
            JsonTelemetryDataSerializer jsonWriter = null;
            try {
                jsonWriter = new JsonTelemetryDataSerializer(writer);
                telemetry.serialize(jsonWriter);
                jsonWriter.close();
                String asJson = writer.toString();
                telemetryBuffer.add(asJson);
            } catch (IOException e) {
                InternalLogger.INSTANCE.error("Failed to serialize Telemetry");
                return;
            }
        }

        if (isDeveloperMode()) {
            writeTelemetryToDebugOutput(telemetry);
        }
    }

    /**
     * Stops on going work
     */
    @Override
    public synchronized void stop(long timeout, TimeUnit timeUnit) {
        try {
            if (stopped) {
                return;
            }

            telemetriesTransmitter.stop(timeout, timeUnit);
            stopped = true;
        } catch (Throwable t) {
        }
    }

    private void writeTelemetryToDebugOutput(Telemetry telemetry) {
        InternalLogger.INSTANCE.trace("InProcessTelemetryChannel sending telemetry");
    }

    private synchronized void initialize(String endpointAddress, boolean developerMode) {
        useOld = Boolean.valueOf(System.getenv("JAVA_SDK_USE_OLD_T_POLICY"));

        if (!useOld) {
            InternalLogger.INSTANCE.trace("Using new transmission policy");
        }

        makeSureEndpointAddressIsValid(endpointAddress);

        if (s_transmitterFactory == null) {
            s_transmitterFactory = new InProcessTelemetryChannelFactory();
        }

        if (useOld) {
            oldTelemetriesTransmitter = s_transmitterFactory.createOld(endpointAddress);
            oldTelemetryBuffer = new OldTelemetryBuffer(oldTelemetriesTransmitter, DEFAULT_NUMBER_OF_TELEMETRIES_PER_CONTAINER, TRANSMIT_BUFFER_DEFAULT_TIMEOUT_IN_SECONDS);
        } else {
            telemetriesTransmitter = s_transmitterFactory.create(endpointAddress);
            telemetryBuffer = new TelemetryBuffer(telemetriesTransmitter, DEFAULT_NUMBER_OF_TELEMETRIES_PER_CONTAINER, TRANSMIT_BUFFER_DEFAULT_TIMEOUT_IN_SECONDS);
        }
        setDeveloperMode(developerMode);
    }

    /**
     * The method will throw IllegalArgumentException if the endpointAddress is not a valid uri
     * Please note that a null or empty string is valid as far as the class is concerned and thus considered valid
     * @param endpointAddress
     */
    private void makeSureEndpointAddressIsValid(String endpointAddress) {
        if (Strings.isNullOrEmpty(endpointAddress)) {
            return;
        }

        URI uri = Sanitizer.sanitizeUri(endpointAddress);
        if (uri == null) {
            String errorMessage = String.format("Endpoint address %s is not a valid uri", endpointAddress);
            InternalLogger.INSTANCE.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

}
