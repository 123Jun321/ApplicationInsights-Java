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

package com.microsoft.applicationinsights.web.extensibility.modules;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.internal.util.DateTimeUtils;
import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import com.microsoft.applicationinsights.web.utils.CookiesContainer;
import com.microsoft.applicationinsights.web.utils.HttpHelper;
import com.microsoft.applicationinsights.web.utils.JettyTestServer;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * Created by yonisha on 2/9/2015.
 */
public class WebUserTrackingTelemetryModuleTests {
    // region Members

    private static String userCookieFormatted;
    private static JettyTestServer server = new JettyTestServer();

    // endregion Members

    // region Initialization

    @BeforeClass
    public static void classInitialize() throws Exception {
        server.start();
    }

    @Before
    public void testInitialize() {
        userCookieFormatted = HttpHelper.getFormattedUserCookieHeader();
    }

    @AfterClass
    public static void classCleanup() throws Exception {
        server.shutdown();
    }

    // endregion Initialization

    // region Tests

    @Test
    public void testNewUserCookieIsCreatedWhenCookieNotExist() throws Exception {
        CookiesContainer cookiesContainer = HttpHelper.sendRequestAndGetResponseCookie();

        Assert.assertNotNull("User cookie shouldn't be null.", cookiesContainer.getUserCookie());
    }

    @Test
    public void testNoUserCookieCreatedWhenValidCookieExists() throws Exception {
        CookiesContainer cookiesContainer = HttpHelper.sendRequestAndGetResponseCookie(userCookieFormatted);

        Assert.assertNull(cookiesContainer.getUserCookie());
    }

    @Test
    public void testNewUserCookieIsCreatedWhenCookieCorrupted() throws Exception {
        CookiesContainer cookiesContainer = HttpHelper.sendRequestAndGetResponseCookie("corrupted;user;cookie");

        Assert.assertNotNull("User cookie shouldn't be null.", cookiesContainer.getUserCookie());
    }

    @Test
    public void testModulesInitializedCorrectlyWithGenerateNewUserParam() {
        final String value = "false";

        Map<String, String> map = new HashMap<String, String>();
        map.put(WebUserTrackingTelemetryModule.GENERATE_NEW_USERS_PARAM_KEY, value);

        WebUserTrackingTelemetryModule module = new WebUserTrackingTelemetryModule(map);

        Assert.assertEquals(Boolean.parseBoolean(value), module.getGenerateNewUsers());
    }


    @Test
    public void testWhenGenerateNewUsersIsFalseUsersAreNotGenerated() {
        WebUserTrackingTelemetryModule module =
                createModuleWithParam(WebUserTrackingTelemetryModule.GENERATE_NEW_USERS_PARAM_KEY, "false");

        ThreadContext.setRequestTelemetryContext(new RequestTelemetryContext(DateTimeUtils.getDateTimeNow().getTime()));
        module.initialize(TelemetryConfiguration.getActive());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        final Cookie[] cookie = new Cookie[1];
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                cookie[0] = ((Cookie) invocation.getArguments()[0]);

                return null;
            }
        }).when(response).addCookie(any(Cookie.class));

        module.onBeginRequest(request, response);

        Assert.assertNull("No cookie should be generated." ,cookie[0]);
    }

    // endregion Tests

    // region Private

    private WebUserTrackingTelemetryModule createModuleWithParam(String paramName, String paramValue) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(paramName, paramValue);

        return new WebUserTrackingTelemetryModule(map);
    }

    // endregion Private
}
