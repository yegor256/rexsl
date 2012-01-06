/**
 * Copyright (c) 2011, ReXSL.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the ReXSL.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rexsl.core;

import com.rexsl.test.XhtmlConverter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xmlmatchers.XmlMatchers;

/**
 * Mocker of {@link HttpServletRequest}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class HttpServletRequestMocker {

    /**
     * The mock.
     */
    private final transient HttpServletRequest request =
        Mockito.mock(HttpServletRequest.class);

    /**
     * All headers.
     */
    private final transient ConcurrentMap<String, String> headers =
        new ConcurrentHashMap<String, String>();

    /**
     * Public ctor.
     */
    public HttpServletRequestMocker() {
        Mockito.doReturn(Collections.enumeration(new ArrayList<String>()))
            .when(this.request).getHeaderNames();
        Mockito.doReturn("").when(this.request).getContextPath();
        Mockito.doReturn("").when(this.request).getServletPath();
        Mockito.doReturn(new StringBuffer("http://localhost/"))
            .when(this.request).getRequestURL();
        Mockito.doAnswer(
            new Answer() {
                public Object answer(final InvocationOnMock invocation) {
                    final String name = (String) invocation.getArguments()[0];
                    return HttpServletRequestMocker.this.headers.get(name);
                }
            }
        ).when(this.request).getHeader(Mockito.anyString());
        Mockito.doAnswer(
            new Answer() {
                public Object answer(final InvocationOnMock invocation) {
                    return Collections.enumeration(
                        HttpServletRequestMocker.this.headers.keySet()
                    );
                }
            }
        ).when(this.request).getHeaderNames();
        this.withMethod("GET");
        this.withRequestUri("/");
    }

    /**
     * With this request URI.
     * @param uri The URI
     * @return This object
     */
    public HttpServletRequestMocker withRequestUri(final String uri) {
        Mockito.doReturn(uri).when(this.request).getRequestURI();
        Mockito.doReturn(uri).when(this.request).getPathInfo();
        return this;
    }

    /**
     * With this header.
     * @param name The name of it
     * @param value The value
     * @return This object
     */
    public HttpServletRequestMocker withHeader(final String name,
        final String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * With this HTTP method.
     * @param name Name of method
     * @return This object
     */
    public HttpServletRequestMocker withMethod(final String name) {
        Mockito.doReturn(name).when(this.request).getMethod();
        return this;
    }

    /**
     * Mock it.
     * @return Mocked request
     */
    public HttpServletRequest mock() {
        return this.request;
    }

}
