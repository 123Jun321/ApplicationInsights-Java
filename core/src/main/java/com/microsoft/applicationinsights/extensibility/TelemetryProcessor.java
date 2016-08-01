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

package com.microsoft.applicationinsights.extensibility;

import com.microsoft.applicationinsights.telemetry.Telemetry;

/**
 * The method gets a {@link Telemetry} instance that is ready to be sent. This is your chance to approve
 * or deny it. Returning 'false' means that the Telemetry will not be sent while 'true' means you approve it.
 *
 * The Telemetry might go through other filters though, that might deny its sending.
 *
 * To enable this processor you need to add it in the ApplicationInsights.xml like this:
 *
 * <pre>
 * {@code
 *  <TelemetryProcessors>
 *      <CustomProcessors>
 *          <Processor type="full.path.to.Filter">
 *              <Add name="Property" value="stringValue"/>
 *          </Processor>
 *      </CustomProcessors>
 *  </TelemetryProcessors>
 *  }
 *</pre>
 *
 * Note that the class defines a property named 'Property' which is configured too.
 * Every property that you wish to configure needs to have a 'setX' public method like 'setProperty' in this example
 * <b>Exceptions thrown from the 'setX' methods will be caught by the framework that will ignore the filter</b>
 *
 * Created by gupele on 7/26/2016.
 */
public interface TelemetryProcessor {
    boolean process(Telemetry telemetry);
}
