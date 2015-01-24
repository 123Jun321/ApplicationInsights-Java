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

package com.microsoft.applicationinsights.telemetry;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.microsoft.applicationinsights.internal.schemav2.Data;
import com.microsoft.applicationinsights.internal.schemav2.Envelope;
import com.microsoft.applicationinsights.internal.schemav2.SendableData;
import com.microsoft.applicationinsights.internal.util.LocalStringsUtils;
import com.microsoft.applicationinsights.internal.util.Sanitizer;

/**
 * Superclass for all telemetry data classes.
 */
public abstract class BaseTelemetry<T extends SendableData> implements Telemetry
{
    private TelemetryContext context;
    private Date             timestamp;

    protected BaseTelemetry() {
    }

    /**
     * Initializes the instance with the context properties
     * @param properties The context properties
     */
    protected void initialize(ConcurrentMap<String, String> properties) {
        this.context = new TelemetryContext(properties, new ConcurrentHashMap<String, String>());
    }

    /**
     * Gets date and time when event was recorded.
     * @return The timestamp as Date
     */
    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets date and time when event was recorded.
     * @param date The timestamp as Date.
     */
    @Override
    public void setTimestamp(Date date) {
        timestamp = date;
    }

    /**
     * Gets the context associated with the current telemetry item.
     * @return The context
     */
    @Override
    public TelemetryContext getContext() {
        return context;
    }

    /**
     * Gets a dictionary of application-defined property names and values providing additional information about this event.
     * @return The properties
     */
    @Override
    public Map<String, String> getProperties() {
        return this.context.getProperties();
    }

    /**
     * Makes sure the data to send is sanitized from bad chars, proper length etc.
     */
    @Override
    public void sanitize() {
        Sanitizer.sanitizeProperties(this.getProperties());
        additionalSanitize();
    }

    /**
     * Serializes this object in JSON format.
     * @param writer The writer that helps with serializing into Json format
     * @throws IOException The exception that might be thrown during the serialization
     */
    @Override
    public void serialize(JsonTelemetryDataSerializer writer) throws IOException {

        Envelope envelope = new Envelope();

        envelope.setIKey(context.getInstrumentationKey());
        envelope.setData(new Data<T>(getData()));
        envelope.setTime(LocalStringsUtils.getDateFormatter().format(getTimestamp()));
        envelope.setTags(context.getTags());

        envelope.serialize(writer);
    }

    /**
     * Concrete classes should implement this method
     */
    protected abstract void additionalSanitize();

    /**
     * Concrete classes should implement this method which supplies the
     * data structure that this instance works with, which needs to implement {@link SendableData}
     * @return The inner data structure
     */
    protected abstract T getData();
}
