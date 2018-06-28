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

package com.microsoft.applicationinsights.internal.config;

import java.io.InputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public final class JaxbAppInsightsConfigurationBuilderTest {
  private static final String EXISTING_CONF_TEST_FILE = "ApplicationInsights2.xml";

  @Before
  public void clearProp() {
    System.clearProperty(ConfigurationFileLocator.CONFIG_DIR_PROPERTY);
  }

  @Test
  public void testNullInputShouldReturnNull() {
    Assert.assertNull(new JaxbAppInsightsConfigurationBuilder().build(null));
  }

  @Test
  public void testBuilderProducesCorrectConfig() {
    System.setProperty(ConfigurationFileLocator.CONFIG_DIR_PROPERTY, "src/test/resources");
    InputStream resourceFile =
        new ConfigurationFileLocator(EXISTING_CONF_TEST_FILE).getConfigurationFile();
    JaxbAppInsightsConfigurationBuilder builder = new JaxbAppInsightsConfigurationBuilder();
    ApplicationInsightsXmlConfiguration config = builder.build(resourceFile);

    // asserting a few config items only since the point of the test is to validate deserialization
    // occurs
    // with no errors.
    Assert.assertEquals("myikey", config.getInstrumentationKey());
    Assert.assertFalse(config.getChannel().getDeveloperMode());
    Assert.assertEquals(
        "mypackage.MyCustomContextInitializer",
        config.getContextInitializers().getAdds().get(0).getType());
  }
}