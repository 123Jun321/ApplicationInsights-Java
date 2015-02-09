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

package com.microsoft.applicationinsights.web.internal;

import java.util.Date;
import com.microsoft.applicationinsights.telemetry.HttpRequestTelemetry;

/**
 * Created by yonisha on 2/2/2015.
 */
public class RequestTelemetryContext {
    private HttpRequestTelemetry requestTelemetry;
    private long requestStartTimeTicks;
    private Date sessionAcquisitionDate;
    private Date sessionRenewalDate;

    public static final String CONTEXT_ATTR_KEY = "CONTEXT_ATTR";

    /**
     * Constructs new RequestTelemetryContext object.
     * @param ticks The time in ticks
     */
    public RequestTelemetryContext(long ticks) {
        requestTelemetry = new HttpRequestTelemetry();
        requestStartTimeTicks = ticks;
    }

    /**
     * Gets the http request telemetry associated with the context.
     * @return The http request telemetry.
     */
    public HttpRequestTelemetry getHttpRequestTelemetry() {
        return requestTelemetry;
    }

    /**
     * Gets the request start time in ticks
     * @return Request start time in ticks
     */
    public long getRequestStartTimeTicks() {
        return requestStartTimeTicks;
    }

    /**
     * Sets the session acquisition date.
     * @param sessionAcquisitionDate The session acquisition date.
     */
    public void setSessionAcquisitionDate(Date sessionAcquisitionDate) {
        this.sessionAcquisitionDate = sessionAcquisitionDate;
    }

    /**
     * Gets the session acquisition date.
     * @return Session acquisition date.
     */
    public Date getSessionAcquisitionDate() {
        return sessionAcquisitionDate;
    }

    /**
     * Sets the session renewal date.
     * @param sessionRenewalDate The session renewal date.
     */
    public void setSessionRenewalDate(Date sessionRenewalDate) {
        this.sessionRenewalDate = sessionRenewalDate;
    }

    /**
     * Gets the session renewal date.
     * @return Session renewal date.
     */
    public Date getSessionRenewalDate() {
        return sessionRenewalDate;
    }
}
