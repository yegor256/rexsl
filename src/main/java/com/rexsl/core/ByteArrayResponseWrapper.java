/**
 * Copyright (c) 2011-2014, ReXSL.com
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

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;

/**
 * Wrapper that redirects all the writes to {@link ByteArrayOutputStream}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode(callSuper = false, of = { "stream", "writer" })
final class ByteArrayResponseWrapper extends HttpServletResponseWrapper {

    /**
     * Stream for keeping the servlet response.
     */
    private final transient ByteArrayOutputStream stream =
        new ByteArrayOutputStream();

    /**
     * Wraps ByteArrayOutputStream into a PrintWriter.
     */
    private final transient PrintWriter writer;

    /**
     * Public ctor.
     * @param response Servlet response being wrapped.
     */
    ByteArrayResponseWrapper(
        @NotNull final HttpServletResponse response) {
        super(response);
        try {
            this.writer = new PrintWriter(
                new OutputStreamWriter(this.stream, CharEncoding.UTF_8)
            );
        } catch (final UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get the underlying byte array.
     * @return Byte array that contains the response.
     */
    @NotNull
    public byte[] getByteArray() {
        return this.stream.toByteArray();
    }

    @Override
    @NotNull
    public PrintWriter getWriter() {
        return this.writer;
    }

    @Override
    @NotNull
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {
            @Override
            public void write(final int part) {
                ByteArrayResponseWrapper.this.stream.write(part);
            }
        };
    }

}
