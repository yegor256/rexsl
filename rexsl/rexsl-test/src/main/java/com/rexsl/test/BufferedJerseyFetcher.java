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
package com.rexsl.test;

import com.jcabi.log.Logger;
import com.sun.jersey.api.client.ClientResponse;
import javax.validation.constraints.NotNull;

/**
 * Buffer in front {@link JerseyFetcher}.
 *
 * <p>Objects of this class are mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id: JerseyTestResponse.java 2033 2012-08-30 14:22:53Z guard $
 */
final class BufferedJerseyFetcher implements JerseyFetcher {

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
     * The body of HTTP response, should be loaded on demand in
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
     * Get body of request.
     * @return The body as string
     */
    public String body() {
        synchronized (this.fetcher) {
            if (this.entity == null) {
                if (this.fetch().hasEntity()) {
                    this.entity = this.fetch().getEntity(String.class);
                } else {
                    this.entity = "";
                }
                Logger.debug(
                    this,
                    "#body(): HTTP response body:\n%s",
                    new ClientResponseDecor(this.fetch(), this.entity)
                );
            }
            return this.entity;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public ClientResponse fetch() {
        synchronized (this.fetcher) {
            if (this.rsp == null) {
                try {
                    this.rsp = this.fetcher.fetch();
                // @checkstyle IllegalCatch (1 line)
                } catch (RuntimeException ex) {
                    Logger.warn(this, "Failed to fetch: %[exception]s", ex);
                    throw new AssertionError(ex);
                }
            }
            return this.rsp;
        }
    }

}
