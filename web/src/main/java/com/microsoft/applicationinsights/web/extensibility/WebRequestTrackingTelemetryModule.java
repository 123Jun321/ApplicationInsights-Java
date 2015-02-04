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

package com.microsoft.applicationinsights.web.extensibility;

import java.util.Date;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.common.base.Strings;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.extensibility.TelemetryModule;
import com.microsoft.applicationinsights.internal.logger.InternalLogger;
import com.microsoft.applicationinsights.telemetry.HttpRequestTelemetry;
import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import org.apache.http.HttpStatus;

/**
 * Created by yonisha on 2/2/2015.
 */
public class WebRequestTrackingTelemetryModule implements WebTelemetryModule, TelemetryModule {
    private TelemetryClient telemetryClient;
    private boolean isInitialized = false;

    /**
     * Begin request processing.
     * @param req The request to process
     * @param res The response to modify
     */
    @Override
    public void onBeginRequest(ServletRequest req, ServletResponse res) {
        if (!isInitialized) {
            // Avoid logging to not spam the log. It is sufficient that the module initialization failure
            // has been logged.
            return;
        }

        try {
            RequestTelemetryContext context = getTelemetryContextFromServletRequest(req);
            HttpRequestTelemetry telemetry = context.getHttpRequestTelemetry();

            HttpServletRequest request = (HttpServletRequest) req;
            String method = request.getMethod();
            String rURI = request.getRequestURI();
            String scheme = request.getScheme();
            String host = request.getHeader("Host");
            String query = request.getQueryString();

            telemetry.setHttpMethod(method);
            if (!Strings.isNullOrEmpty(query)) {
                telemetry.setUrl(String.format("%s://%s%s?%s", scheme, host, rURI, query));
            }
            else {
                telemetry.setUrl(String.format("%s://%s%s", scheme, host, rURI));
            }

            telemetry.setName(String.format("%s %s", method, rURI));
            telemetry.setTimestamp(new Date(context.getRequestStartTimeTicks()));
        } catch (Exception e) {
            String moduleClassName = this.getClass().getSimpleName();
            InternalLogger.INSTANCE.error("Telemetry module " + moduleClassName + " onBeginRequest failed with exception: %s", e.getMessage());
        }
    }

    /**
     * End request processing.
     * @param req The request to process
     * @param res The response to modify
     */
    @Override
    public void onEndRequest(ServletRequest req, ServletResponse res) {
        if (!isInitialized) {
            // Avoid logging to not spam the log. It is sufficient that the module initialization failure
            // has been logged.
            return;
        }

        try {
            RequestTelemetryContext context = getTelemetryContextFromServletRequest(req);
            HttpRequestTelemetry telemetry = context.getHttpRequestTelemetry();

            long endTime = new Date().getTime();

            HttpServletResponse response = (HttpServletResponse)res;
            telemetry.setSuccess(HttpStatus.SC_OK == response.getStatus());
            telemetry.setResponseCode(Integer.toString(response.getStatus()));
            telemetry.setDuration(endTime - context.getRequestStartTimeTicks());

            telemetryClient.track(telemetry);
        } catch (Exception e) {
            String moduleClassName = this.getClass().getSimpleName();
            InternalLogger.INSTANCE.error("Telemetry module " + moduleClassName + " onEndRequest failed with exception: %s", e.getMessage());
        }
    }

    /**
     * Initializes the telemetry module with the given telemetry configuration.
     * @param configuration The telemetry configuration.
     */
    @Override
    public void initialize(TelemetryConfiguration configuration) {
        try {
            telemetryClient = new TelemetryClient(configuration);
            isInitialized = true;
        } catch (Exception e) {
            InternalLogger.INSTANCE.error(
                    "Failed to initialize telemetry module " + this.getClass().getSimpleName() + ". Exception: %s.", e.getMessage());
        }
    }

    // region Private methods

    private RequestTelemetryContext getTelemetryContextFromServletRequest(ServletRequest request) {
        return (RequestTelemetryContext)request.getAttribute(RequestTelemetryContext.CONTEXT_ATTR_KEY);
    }

    // endregion Private methods
}
