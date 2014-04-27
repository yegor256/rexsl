/**
 * Copyright (c) 2011-2013, ReXSL.com
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
package com.rexsl.mock;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.CharEncoding;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link FilterChain}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class FilterChainMocker {

    /**
     * The mock.
     */
    private final transient FilterChain chain =
        Mockito.mock(FilterChain.class);

    /**
     * Public ctor.
     */
    public FilterChainMocker() {
        this.withOutput("");
    }

    /**
     * With this output.
     * @param text The output
     * @return This object
     */
    public FilterChainMocker withOutput(final String text) {
        try {
            Mockito.doAnswer(
                new Answer<Object>() {
                    public Object answer(final InvocationOnMock invocation)
                        throws java.io.IOException {
                        final HttpServletResponse response =
                            (HttpServletResponse) invocation.getArguments()[1];
                        response.getOutputStream()
                            .write(text.getBytes(CharEncoding.UTF_8));
                        return null;
                    }
                }
            ).when(this.chain)
                .doFilter(
                    Mockito.any(HttpServletRequest.class),
                    Mockito.any(HttpServletResponse.class)
                );
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        } catch (javax.servlet.ServletException ex) {
            throw new IllegalStateException(ex);
        }
        return this;
    }

    /**
     * With this output.
     * @param data The output
     * @return This object
     */
    public FilterChainMocker withOutput(final byte[] data) {
        try {
            Mockito.doAnswer(
                new Answer<Object>() {
                    public Object answer(final InvocationOnMock invocation)
                        throws java.io.IOException {
                        final HttpServletResponse response =
                            (HttpServletResponse) invocation.getArguments()[1];
                        response.getOutputStream().write(data);
                        return null;
                    }
                }
            ).when(this.chain)
                .doFilter(
                    Mockito.any(HttpServletRequest.class),
                    Mockito.any(HttpServletResponse.class)
                );
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        } catch (javax.servlet.ServletException ex) {
            throw new IllegalStateException(ex);
        }
        return this;
    }

    /**
     * Mock it.
     * @return Mocked chain
     */
    public FilterChain mock() {
        return this.chain;
    }

}
