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

package com.microsoft.applicationinsights.internal.channel.common;

import org.junit.Test;

import static org.junit.Assert.*;

public final class BackOffTimesContainerFactoryTest {
    @Test
    public void testEmptyString() {
        BackOffTimesContainer container = new BackOffTimesContainerFactory().create("");
        assertTrue(container instanceof ExponentialBackOffTimesContainer);
    }

    @Test
    public void testNullString() {
        BackOffTimesContainer container = new BackOffTimesContainerFactory().create(null);
        assertTrue(container instanceof ExponentialBackOffTimesContainer);
    }

    @Test
    public void testWrongString() {
        BackOffTimesContainer container = new BackOffTimesContainerFactory().create("exponentiall");
        assertTrue(container instanceof ExponentialBackOffTimesContainer);
    }

    @Test
    public void testExponentialLowerCase() {
        BackOffTimesContainer container = new BackOffTimesContainerFactory().create("exponential");
        assertTrue(container instanceof ExponentialBackOffTimesContainer);
    }

    @Test
    public void testExponentialUpperCase() {
        BackOffTimesContainer container = new BackOffTimesContainerFactory().create("EXPONENTIAL");
        assertTrue(container instanceof ExponentialBackOffTimesContainer);
    }

    @Test
    public void testExponentialDifferentCase() {
        BackOffTimesContainer container = new BackOffTimesContainerFactory().create("EXpoNENTIAL");
        assertTrue(container instanceof ExponentialBackOffTimesContainer);
    }

    @Test
    public void testStaticLowerCase() {
        BackOffTimesContainer container = new BackOffTimesContainerFactory().create("static");
        assertTrue(container instanceof StaticBackOffTimesContainer);
    }

    @Test
    public void testStaticUpperCase() {
        BackOffTimesContainer container = new BackOffTimesContainerFactory().create("STATIC");
        assertTrue(container instanceof StaticBackOffTimesContainer);
    }

    @Test
    public void testStaticDifferentCase() {
        BackOffTimesContainer container = new BackOffTimesContainerFactory().create("stAtic");
        assertTrue(container instanceof StaticBackOffTimesContainer);
    }
}
