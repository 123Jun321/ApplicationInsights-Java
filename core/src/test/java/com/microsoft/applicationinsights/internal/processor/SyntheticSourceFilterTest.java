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

package com.microsoft.applicationinsights.internal.processor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.microsoft.applicationinsights.telemetry.PageViewTelemetry;
import com.microsoft.applicationinsights.telemetry.Telemetry;
import org.junit.Test;

/** Created by gupele on 7/26/2016. */
public class SyntheticSourceFilterTest {

  @Test
  public void testNullSources() {
    SyntheticSourceFilter tested = new SyntheticSourceFilter();
    boolean result = tested.process(new PageViewTelemetry());

    assertTrue(result);
  }

  @Test
  public void testEmptySources() throws Throwable {
    SyntheticSourceFilter tested = new SyntheticSourceFilter();
    boolean result = tested.process(new PageViewTelemetry());

    assertTrue(result);
  }

  @Test
  public void testNullTelemetry() throws Throwable {
    SyntheticSourceFilter tested = new SyntheticSourceFilter();
    boolean result = tested.process(null);

    assertTrue(result);
  }

  @Test
  public void testOneSourceThatIsFound() throws Throwable {
    SyntheticSourceFilter tested = new SyntheticSourceFilter();
    Telemetry telemetry = new PageViewTelemetry();
    telemetry.getContext().getOperation().setSyntheticSource("A");
    boolean result = tested.process(telemetry);

    assertFalse(result);
  }

  @Test
  public void testSourcesThatIsDeclaredAndFound() throws Throwable {
    SyntheticSourceFilter tested = new SyntheticSourceFilter();
    tested.setNotNeededSources("A, B");
    Telemetry telemetry = new PageViewTelemetry();
    telemetry.getContext().getOperation().setSyntheticSource("A");
    boolean result = tested.process(telemetry);

    assertFalse(result);
  }

  @Test
  public void testSourcesThatIsDeclaredAndNOTFound() throws Throwable {
    SyntheticSourceFilter tested = new SyntheticSourceFilter();
    tested.setNotNeededSources("A, B");
    Telemetry telemetry = new PageViewTelemetry();
    telemetry.getContext().getOperation().setSyntheticSource("A1");
    boolean result = tested.process(telemetry);

    assertTrue(result);
  }
}
