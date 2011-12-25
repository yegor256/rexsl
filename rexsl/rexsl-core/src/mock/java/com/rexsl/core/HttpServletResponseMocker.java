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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link HttpServletResponse}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class HttpServletResponseMocker {

    /**
     * The mock.
     */
    private final transient HttpServletResponse response =
        Mockito.mock(HttpServletResponse.class);

    /**
     * Mocked stream.
     */
    private final transient ServletOutputStream stream =
        new ServletOutputStreamMocker().mock();

    /**
     * Public ctor.
     */
    public HttpServletResponseMocker() {
        try {
            Mockito.doReturn(this.stream).when(this.response).getOutputStream();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        Mockito.doAnswer(
            new Answer() {
                public Object answer(final InvocationOnMock invocation) {
                    return HttpServletResponseMocker.this.stream.toString();
                }
            }
        ).when(this.response).toString();
    }

    /**
     * Expect only this status code.
     * @param status The HTTP status code
     * @return This object
     */
    public HttpServletResponseMocker expectStatus(final int status) {
        Mockito.doAnswer(
            new Answer() {
                public Object answer(final InvocationOnMock invocation) {
                    final int actual = (Integer) invocation.getArguments()[0];
                    MatcherAssert.assertThat(actual, Matchers.equalTo(status));
                    return null;
                }
            }
        ).when(this.response).setStatus(Mockito.anyInt());
        return this;
    }

    /**
     * Mock it.
     * @return Mocked request
     */
    public HttpServletResponse mock() {
        return this.response;
    }

}
