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
package com.rexsl.maven.utils;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.validation.constraints.NotNull;

/**
 * Runtime wrapper that redirects all the writes
 * to {@link ByteArrayOutputStream}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @see RuntimeFilter#doFilter(ServletRequest,ServletResponse,FilterChain)
 */
public final class RuntimeResponseWrapper extends HttpServletResponseWrapper {

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
     * HTTP status.
     */
    private transient int status;

    /**
     * Error message.
     */
    private transient String message;

    /**
     * Public ctor.
     * @param response Servlet response being wrapped.
     * @see RuntimeFilter#doFilter(ServletRequest,ServletResponse,FilterChain)
     */
    public RuntimeResponseWrapper(@NotNull final HttpServletResponse response) {
        super(response);
        this.writer = new PrintWriter(
            new OutputStreamWriter(this.stream)
        );
    }

    /**
     * Get the underlying byte stream.
     * @return Byte stream that contains the response.
     */
    @Loggable(Loggable.DEBUG)
    public ByteArrayOutputStream getByteStream() {
        return this.stream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public PrintWriter getWriter() {
        return this.writer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public void sendError(final int stc, @NotNull final String msg) {
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
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public void flushBuffer() throws IOException {
        // nothing to do
    }

    /**
     * Pass through all sendError() calls.
     * @throws IOException If there is some problem
     */
    @SuppressWarnings("PMD.ConfusingTernary")
    @Loggable(Loggable.DEBUG)
    public void passThrough() throws IOException {
        if (this.message != null) {
            Logger.debug(
                this,
                "#passThrough(): super.sendError(%d, %s) called",
                this.status,
                this.message
            );
            super.sendError(this.status, this.message);
        } else if (this.status != 0) {
            Logger.debug(
                this,
                "#passThrough(): super.setStatus(%d) called",
                this.status
            );
            super.setStatus(this.status);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
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
