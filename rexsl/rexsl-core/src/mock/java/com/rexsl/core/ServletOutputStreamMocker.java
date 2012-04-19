/**
 * Copyright (c) 2011-2012, ReXSL.com
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
import org.apache.commons.lang.CharEncoding;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link ServletOutputStream}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class ServletOutputStreamMocker {

    /**
     * The mock.
     */
    private final transient ServletOutputStream stream =
        Mockito.mock(ServletOutputStream.class);

    /**
     * Buffer.
     */
    private final transient StringBuilder buffer = new StringBuilder();

    /**
     * Public ctor.
     */
    public ServletOutputStreamMocker() {
        try {
            Mockito.doAnswer(
                new Answer() {
                    public Object answer(final InvocationOnMock invocation)
                        throws java.io.IOException {
                        final int data = (Integer) invocation.getArguments()[0];
                        ServletOutputStreamMocker.this.buffer
                            .append((char) data);
                        return null;
                    }
                }
            ).when(this.stream).write(Mockito.anyInt());
            Mockito.doAnswer(
                new Answer() {
                    public Object answer(final InvocationOnMock invocation)
                        throws java.io.IOException {
                        final byte[] data =
                            (byte[]) invocation.getArguments()[0];
                        ServletOutputStreamMocker.this.buffer.append(
                            new String(data, CharEncoding.UTF_8).toCharArray()
                        );
                        return null;
                    }
                }
            ).when(this.stream).write((byte[]) Mockito.any());
            Mockito.doAnswer(
                new Answer() {
                    public Object answer(final InvocationOnMock invocation)
                        throws java.io.IOException {
                        final byte[] data =
                            (byte[]) invocation.getArguments()[0];
                        final int off = (Integer) invocation.getArguments()[1];
                        final int len = (Integer) invocation.getArguments()[2];
                        ServletOutputStreamMocker.this.buffer.append(
                            new String(data, CharEncoding.UTF_8).toCharArray(),
                            off,
                            len
                        );
                        return null;
                    }
                }
            ).when(this.stream)
                .write(
                    (byte[]) Mockito.any(), Mockito.anyInt(), Mockito.anyInt()
                );
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        Mockito.doAnswer(
            new Answer() {
                public Object answer(final InvocationOnMock invocation) {
                    return ServletOutputStreamMocker.this.buffer.toString();
                }
            }
        ).when(this.stream).toString();
    }

    /**
     * Mock it.
     * @return Mocked request
     */
    public ServletOutputStream mock() {
        return this.stream;
    }

}
