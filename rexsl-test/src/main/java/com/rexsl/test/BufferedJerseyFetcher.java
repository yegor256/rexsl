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
package com.rexsl.test;

import com.jcabi.aspects.Loggable;
import com.sun.jersey.api.client.ClientResponse;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Buffer in front of {@link JerseyFetcher}.
 *
 * <p>Objects of this class are mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@ToString(of = "fetcher")
@EqualsAndHashCode(of = { "fetcher", "rsp", "entity" })
@Loggable(Loggable.DEBUG)
final class BufferedJerseyFetcher implements JerseyFetcher {

    /**
     * UTF-8 error marker.
     */
    private static final String ERR = "\uFFFD";

    /**
     * Fetcher of response.
     */
    private final transient JerseyFetcher fetcher;

    /**
     * The response, should be initialized on demand in {@link #fetch()}.
     * @see #response()
     */
    private transient ClientResponse rsp;

    /**
     * The UTF-8 body of HTTP response, should be loaded on demand in
     * {@link #body()}.
     * @see #body()
     */
    private transient String entity;

    /**
     * Public ctor.
     * @param ftch Response fetcher
     */
    public BufferedJerseyFetcher(@NotNull final JerseyFetcher ftch) {
        this.fetcher = ftch;
    }

    /**
     * Reset it back to initial state.
     */
    @SuppressWarnings("PMD.NullAssignment")
    public void reset() {
        synchronized (this.fetcher) {
            this.rsp = null;
            this.entity = null;
        }
    }

    /**
     * Get body of request, as a UTF-8 string.
     * @return The body as UTF-8 string
     * @throws IOException If can't read it from response
     */
    @NotNull
    public String body() throws IOException {
        synchronized (this.fetcher) {
            if (this.entity == null) {
                this.entity = this.load();
            }
            return this.entity;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    @NotNull
    public ClientResponse fetch() {
        synchronized (this.fetcher) {
            if (this.rsp == null) {
                try {
                    this.rsp = this.fetcher.fetch();
                // @checkstyle IllegalCatch (1 line)
                } catch (RuntimeException ex) {
                    throw new AssertionError(ex);
                }
            }
            return this.rsp;
        }
    }

    /**
     * Load body and return it as a UTF-8 string (method is not thread safe!).
     * @return The body as UTF-8 string
     * @throws IOException If can't read it from response
     */
    private String load() throws IOException {
        final ClientResponse response = this.fetch();
        String body;
        if (response.hasEntity()) {
            final byte[] bytes = IOUtils.toByteArray(
                response.getEntityInputStream()
            );
            body = new String(bytes, CharEncoding.UTF_8);
            if (body.contains(BufferedJerseyFetcher.ERR)) {
                throw new IOException(
                    String.format(
                        "broken Unicode text at line #%d in '%s' (%d bytes)",
                        StringUtils.countMatches(
                            "\n",
                            body.substring(
                                0, body.indexOf(BufferedJerseyFetcher.ERR)
                            )
                        ) + 2,
                        body,
                        bytes.length
                    )
                );
            }
        } else {
            body = "";
        }
        return body;
    }

}
