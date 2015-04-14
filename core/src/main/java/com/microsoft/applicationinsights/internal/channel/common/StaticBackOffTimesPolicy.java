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

package com.microsoft.applicationinsights.internal.channel.common;

/**
 * Created by gupele on 2/10/2015.
 */
final class StaticBackOffTimesPolicy implements BackOffTimesPolicy {
    public static final int NUMBER_OF_BACK_OFFS = 20;

    private static final long TEN_SECONDS_IN_MS = 10000;
    @Override
    public long[] getBackOffTimeoutsInMillis() {
        long[] backOffInSeconds = new long[NUMBER_OF_BACK_OFFS];
        int couples = NUMBER_OF_BACK_OFFS / 2;
        for (int i = 0; i < couples; ++i) {
            int position = i * 2;
            backOffInSeconds[position] = BackOffTimesPolicy.MIN_TIME_TO_BACK_OFF_IN_MILLS;
            backOffInSeconds[position + 1] = TEN_SECONDS_IN_MS;
        }

        return backOffInSeconds;
    }
}
