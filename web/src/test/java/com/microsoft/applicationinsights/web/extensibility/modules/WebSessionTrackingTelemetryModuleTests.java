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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.applicationinsights.internal.util.DateTimeUtils;
import com.microsoft.applicationinsights.telemetry.HttpRequestTelemetry;
import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.telemetry.SessionState;
import com.microsoft.applicationinsights.telemetry.SessionStateTelemetry;
import com.microsoft.applicationinsights.web.internal.cookies.SessionCookie;
import com.microsoft.applicationinsights.web.utils.MockTelemetryChannel;
import com.microsoft.applicationinsights.web.utils.CookiesContainer;
import com.microsoft.applicationinsights.web.utils.HttpHelper;
import com.microsoft.applicationinsights.web.utils.JettyTestServer;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * Created by yonisha on 2/5/2015.
 */
public class WebSessionTrackingTelemetryModuleTests {

    // region Members

    private static String sessionCookieFormatted;
    private static JettyTestServer server = new JettyTestServer();
    private static MockTelemetryChannel channel;

    // endregion Members

    // region Initialization

    @BeforeClass
    public static void classInitialize() throws Exception {
        server.start();

        // Set mock channel
        channel = MockTelemetryChannel.INSTANCE;
        TelemetryConfiguration.getActive().setChannel(channel);
        TelemetryConfiguration.getActive().setInstrumentationKey("SOME_INT_KEY");
    }

    @Before
    public void testInitialize() {
        sessionCookieFormatted = HttpHelper.getFormattedSessionCookieHeader(false);
        channel.reset();
    }

    @AfterClass
    public static void classCleanup() throws Exception {
        server.shutdown();
    }

    // endregion Initialization

    // region Tests

    @Test
    public void testNewSessionIsCreatedWhenCookieNotExist() throws Exception {
        CookiesContainer cookiesContainer = HttpHelper.sendRequestAndGetResponseCookie();

        Assert.assertNotNull("Session cookie shouldn't be null.", cookiesContainer.getSessionCookie());
    }

    @Test
    public void testIsFirstSessionIsPopulatedOnFirstSession() throws Exception {
        HttpHelper.sendRequestAndGetResponseCookie();

        HttpRequestTelemetry requestTelemetry = channel.getTelemetryItems(HttpRequestTelemetry.class).get(0);

        Assert.assertTrue(requestTelemetry.getContext().getSession().getIsFirst());
    }

    @Test
    public void testNoSessionCreatedWhenValidSessionExists() throws Exception {
        CookiesContainer cookiesContainer = HttpHelper.sendRequestAndGetResponseCookie(sessionCookieFormatted);

        Assert.assertNull(cookiesContainer.getSessionCookie());
    }

    @Test
    public void testNewSessionIsCreatedWhenCookieSessionExpired() throws Exception {
        sessionCookieFormatted = HttpHelper.getFormattedSessionCookieHeader(true);

        CookiesContainer cookiesContainer = HttpHelper.sendRequestAndGetResponseCookie(sessionCookieFormatted);
        SessionCookie sessionCookie = cookiesContainer.getSessionCookie();

        Assert.assertNotNull(sessionCookie);
        Assert.assertFalse(sessionCookieFormatted.contains(sessionCookie.getSessionId()));
    }

    @Test
    public void testNewSessionIsCreatedWhenCookieCorrupted() throws Exception {
        CookiesContainer cookiesContainer = HttpHelper.sendRequestAndGetResponseCookie("corrupted;session;cookie");

        Assert.assertNotNull("Session cookie shouldn't be null.", cookiesContainer.getSessionCookie());
    }

    @Test
    public void testWhenSessionExpiredSessionStateEndTracked() throws Exception {
        sessionCookieFormatted = HttpHelper.getFormattedSessionCookieHeader(true);

        HttpHelper.sendRequestAndGetResponseCookie(sessionCookieFormatted);

        verifySessionState(SessionState.End);
    }

    @Test
    public void testWhenNewSessionStartedSessionStateStartTracked() throws Exception {
        HttpHelper.sendRequestAndGetResponseCookie();

        verifySessionState(SessionState.Start);
    }

    @Test
    public void testOnFirstSessionStartedNoSessionStateEndTracked() throws Exception {
        HttpHelper.sendRequestAndGetResponseCookie();

        SessionStateTelemetry telemetry = getSessionStateTelemetryWithState(SessionState.End);
        Assert.assertNull("No telemetry with SessionEnd expected.", telemetry);
    }

    @Test
    public void testSessionStateTelemetryContainsSessionIdOnStartState() throws Exception {
        HttpHelper.sendRequestAndGetResponseCookie();

        SessionStateTelemetry telemetry = getSessionStateTelemetryWithState(SessionState.Start);

        Assert.assertNotNull("Session ID shouldn't be null", telemetry.getContext().getSession().getId());
    }

    @Test
    public void testSessionStateTelemetryContainsSessionIdOnEndState() throws Exception {
        sessionCookieFormatted = HttpHelper.getFormattedSessionCookieHeader(true);

        HttpHelper.sendRequestAndGetResponseCookie(sessionCookieFormatted);

        SessionStateTelemetry telemetry = getSessionStateTelemetryWithState(SessionState.End);

        Assert.assertNotNull("Session ID shouldn't be null", telemetry.getContext().getSession().getId());
    }

    @Test
    public void testSessionStateTelemetryEndStateContainsExpiredSessionId() throws Exception {
        sessionCookieFormatted = HttpHelper.getFormattedSessionCookieHeader(true);

        HttpHelper.sendRequestAndGetResponseCookie(sessionCookieFormatted);

        SessionStateTelemetry telemetry = getSessionStateTelemetryWithState(SessionState.End);

        String expectedSessionId = HttpHelper.getSessionIdFromCookie(sessionCookieFormatted);
        Assert.assertEquals(
                "Expected session ID of the expired session cookie",
                expectedSessionId,
                telemetry.getContext().getSession().getId());
    }

    @Test
    public void testModulesInitializedCorrectlyWithGenerateNewSessionParam() {
        final String value = "false";

        WebSessionTrackingTelemetryModule module = createModuleWithParam(
                WebSessionTrackingTelemetryModule.GENERATE_NEW_SESSIONS_PARAM_KEY, value);

        Assert.assertEquals(Boolean.parseBoolean(value), module.getGenerateNewSessions());
    }

    @Test
    public void testModulesInitializedCorrectlyWithSessionTimeoutParam() {
        final String value = "13";

        WebSessionTrackingTelemetryModule module = createModuleWithParam(
                WebSessionTrackingTelemetryModule.SESSION_TIMEOUT_PARAM_KEY, value);

        Assert.assertEquals(Integer.parseInt(value),(int)module.getSessionTimeoutInMinutes());
    }

    @Test
    public void testWhenGenerateNewSessionIsFalseSessionsAreNotGenerated() {
        WebSessionTrackingTelemetryModule module = createModuleWithParam(
                WebSessionTrackingTelemetryModule.GENERATE_NEW_SESSIONS_PARAM_KEY, "false");

        Cookie cookie = callOnBeginRequestAndGetCookieResult(module);

        Assert.assertNull("No cookie should be generated." , cookie);
    }

    @Test
    public void testWhenSessionTimeoutParameterUsedThenCookieCreatedWithCorrectAge() {
        int sessionTimeoutInMinutes = 12;
        WebSessionTrackingTelemetryModule module = createModuleWithParam(
                WebSessionTrackingTelemetryModule.SESSION_TIMEOUT_PARAM_KEY, String.valueOf(sessionTimeoutInMinutes));

        Cookie cookie = callOnBeginRequestAndGetCookieResult(module);

        Assert.assertEquals(sessionTimeoutInMinutes * 60, cookie.getMaxAge());
    }

    // endregion Tests

    // region Private

    private Cookie callOnBeginRequestAndGetCookieResult(WebSessionTrackingTelemetryModule module) {
        ThreadContext.setRequestTelemetryContext(new RequestTelemetryContext(DateTimeUtils.getDateTimeNow().getTime()));
        module.initialize(TelemetryConfiguration.getActive());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        final Cookie[] cookies = new Cookie[1];
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                cookies[0] = ((Cookie) invocation.getArguments()[0]);

                return null;
            }
        }).when(response).addCookie(any(Cookie.class));

        module.onBeginRequest(request, response);

        return cookies[0];
    }

    private WebSessionTrackingTelemetryModule createModuleWithParam(String paramName, String paramValue) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(paramName, paramValue);

        return new WebSessionTrackingTelemetryModule(map);
    }

    private void verifySessionState(SessionState expectedSessionState) {
        SessionStateTelemetry telemetry = getSessionStateTelemetryWithState(expectedSessionState);

        Assert.assertNotNull("Session state telemetry expected.", telemetry);
        Assert.assertEquals(expectedSessionState + " state expected.", expectedSessionState, telemetry.getSessionState());
    }

    private SessionStateTelemetry getSessionStateTelemetryWithState(SessionState state) {
        List<SessionStateTelemetry> items = channel.getTelemetryItems(SessionStateTelemetry.class);

        for (SessionStateTelemetry telemetry : items) {
            if (telemetry.getSessionState().compareTo(state) == 0) {
                return telemetry;
            }
        }

        return null;
    }

    // endregion Private
}
