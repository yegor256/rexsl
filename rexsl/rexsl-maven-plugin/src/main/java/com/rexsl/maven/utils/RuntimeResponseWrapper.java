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
package com.rexsl.maven.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Runtime wrapper that redirects all the writes
 * to {@link ByteArrayOutputStream}.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @see RuntimeFilter#filter(HttpServletRequest,HttpServletResponse)
 */
public final class RuntimeResponseWrapper extends HttpServletResponseWrapper {

    /**
     * Stream for keeping the servlet response.
     */
    private final ByteArrayOutputStream stream =
        new ByteArrayOutputStream();

    /**
     * Wraps ByteArrayOutputStream into a PrintWriter.
     */
    private final PrintWriter writer;

    /**
     * HTTP status.
     */
    private int status;

    /**
     * Error message.
     */
    private String message;

    /**
     * Public ctor.
     * @param response Servlet response being wrapped.
     * @see XslBrowserFilter#filter(HttpServletRequest,HttpServletResponse)
     */
    public RuntimeResponseWrapper(final HttpServletResponse response) {
        super(response);
        this.writer = new PrintWriter(
            new OutputStreamWriter(this.stream)
        );
    }

    /**
     * Get the underlying byte stream.
     * @return Byte stream that contains the response.
     */
    public ByteArrayOutputStream getByteStream() {
        return this.stream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrintWriter getWriter() {
        return this.writer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendError(final int stc, final String msg) {
        this.status = stc;
        this.message = msg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(final int stc) {
        this.status = stc;
    }

    /**
     * Pass through all sendError() calls.
     * @throws IOException If there is some problem
     */
    public void passThrough() throws IOException {
        if (this.message != null) {
            super.sendError(this.status, this.message);
        }
        if (this.status != 0) {
            super.setStatus(this.status);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void write(final int part) throws IOException {
                RuntimeResponseWrapper.this.stream.write(part);
            }
        };
    }

}
