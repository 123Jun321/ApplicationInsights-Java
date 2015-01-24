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

public class TransmissionTest {
    private final static String MOCK_WEB_CONTENT_TYPE = "MockContent";
    private final static String MOCK_WEB_ENCODING_TYPE = "MockEncoding";

    @Test(expected = IllegalArgumentException.class)
    public void testNullContentType() throws Exception {
        byte[] mockContent = new byte[2];
        new Transmission(mockContent, null, MOCK_WEB_ENCODING_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyContentType() throws Exception {
        byte[] mockContent = new byte[2];
        new Transmission(mockContent, "", MOCK_WEB_ENCODING_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullContentEncodingType() throws Exception {
        byte[] mockContent = new byte[2];
        new Transmission(mockContent, MOCK_WEB_CONTENT_TYPE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyContentEncodingType() throws Exception {
        byte[] mockContent = new byte[2];
        new Transmission(mockContent, MOCK_WEB_CONTENT_TYPE, "");
    }

    @Test(expected = NullPointerException.class)
    public void testNullContent() throws Exception {
        new Transmission(null, MOCK_WEB_CONTENT_TYPE, MOCK_WEB_ENCODING_TYPE);
    }

    @Test
    public void testGetContent() throws Exception {
        byte[] mockContent = new byte[2];
        Transmission tested = new Transmission(mockContent, MOCK_WEB_CONTENT_TYPE, MOCK_WEB_ENCODING_TYPE);

        assertSame(mockContent, tested.getContent());
    }

    @Test
    public void testGetWebContentType() throws Exception {
        byte[] mockContent = new byte[2];
        Transmission tested = new Transmission(mockContent, MOCK_WEB_CONTENT_TYPE, MOCK_WEB_ENCODING_TYPE);

        assertEquals(MOCK_WEB_CONTENT_TYPE, tested.getWebContentType());
    }

    @Test
    public void testGetWebContentEncodingType() throws Exception {
        byte[] mockContent = new byte[2];
        Transmission tested = new Transmission(mockContent, MOCK_WEB_CONTENT_TYPE, MOCK_WEB_ENCODING_TYPE);

        assertEquals(MOCK_WEB_ENCODING_TYPE, tested.getWebContentEncodingType());
    }
}