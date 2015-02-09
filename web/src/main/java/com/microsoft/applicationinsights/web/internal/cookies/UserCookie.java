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

package com.microsoft.applicationinsights.web.internal.cookies;

import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import com.microsoft.applicationinsights.internal.util.DateTimeUtils;

/**
 * Created by yonisha on 2/7/2015.
 */
public class UserCookie extends com.microsoft.applicationinsights.web.internal.cookies.Cookie {

    // region Consts

    public static final String COOKIE_NAME = "ai_user";
    private static final int RAW_COOKIE_USER_ID_INDEX = 0;
    private static final int RAW_COOKIE_ACQUISITION_DATE_INDEX = 1;
    private static final int RAW_COOKIE_EXPECTED_VALUES_COUNT = 2;

    // endregion Consts

    // region Members

    private boolean isNewUser;
    private String userId;
    private Date acquisitionDate;

    // endregion Members

    // region Ctor

    public UserCookie(Cookie cookie) throws Exception {
        parseCookie(cookie);
        isNewUser = false;
    }

    public UserCookie() {
        userId = UUID.randomUUID().toString();
        acquisitionDate = DateTimeUtils.getDateTimeNow();
        isNewUser = true;
    }

    // endregion Ctor

    // region Public

    public String getUserId() {
        return userId;
    }

    public Date getAcquisitionDate() {
        return acquisitionDate;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    /**
     * Formats the given values to a user cookie value.
     * @param values The values to format.
     * @return Formatted user cookie.
     */
    public static String formatCookie(String[] values) {
        return StringUtils.join(values, RAW_COOKIE_DELIMITER);
    }

    // endregion Public

    // region Private

    private void parseCookie(Cookie cookie) throws Exception {
        String[] split = cookie.getValue().split(RAW_COOKIE_SPLIT_DELIMITER);

        if (split.length < RAW_COOKIE_EXPECTED_VALUES_COUNT) {

            // TODO: dedicated exception
            String errorMessage = String.format("Session cookie is not in the correct format: %s", cookie.getValue());

            throw new Exception(errorMessage);
        }

        try {
            userId = split[RAW_COOKIE_USER_ID_INDEX];
            validateUUID(userId);

            acquisitionDate = new Date(Long.parseLong(split[RAW_COOKIE_ACQUISITION_DATE_INDEX]));
        } catch (Exception e) {
            String errorMessage = String.format("Failed to parse session cookie with exception: %s", e.getMessage());

            // TODO: dedicated exception
            throw new Exception(errorMessage);
        }
    }

    // endregion Private
}