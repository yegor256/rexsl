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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;

/**
 * A {@link Request} that retries to fetch in case of an {@link IOException}.
 *
 * <p>It is recommended to use {@link RetryRequest}
 * wrapper to avoid accidental {@link IOException} when connection is weak
 * or unstable, for example:
 *
 * <pre> String name = new RetryRequest(
 *   new JdkRequest("https://www.google.com")
 * ).fetch().body();</pre>
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.9
 * @see JdkRequest
 */
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class RetryRequest implements Request {

    /**
     * Original request.
     */
    private final transient Request origin;

    /**
     * Public ctor.
     * @param request Original request
     */
    public RetryRequest(final Request request) {
        this.origin = request;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public RequestURI uri() {
        return new RetryRequest.RetryURI(this.origin);
    }

    @Override
    public RequestBody body() {
        return new RetryRequest.RetryBody(this.origin);
    }

    @Override
    public Request header(final String name, final Object value) {
        return new RetryRequest(this.origin.header(name, value));
    }

    @Override
    public Request method(final String method) {
        return new RetryRequest(this.origin.method(method));
    }

    @Override
    @RetryOnFailure(
        attempts = Tv.THREE,
        delay = Tv.HUNDRED,
        unit = TimeUnit.MILLISECONDS,
        verbose = false,
        randomize = true,
        types = IOException.class
    )
    public Response fetch() throws IOException {
        return this.origin.fetch();
    }

    /**
     * Retry URI.
     */
    @Immutable
    @EqualsAndHashCode(of = "request")
    private static final class RetryURI implements RequestURI {
        /**
         * Origin request encapsulated.
         */
        private final transient Request request;
        /**
         * Public ctor.
         * @param req Request
         */
        public RetryURI(final Request req) {
            this.request = req;
        }
        @Override
        public String toString() {
            return this.request.uri().toString();
        }
        @Override
        public Request back() {
            return new RetryRequest(this.request);
        }
        @Override
        public URI get() {
            return this.request.uri().get();
        }
        @Override
        public RequestURI set(final URI uri) {
            return new RetryRequest.RetryURI(
                this.request.uri().set(uri).back()
            );
        }
        @Override
        public RequestURI queryParam(final String name, final Object value) {
            return new RetryRequest.RetryURI(
                this.request.uri().queryParam(name, value).back()
            );
        }
        @Override
        public RequestURI queryParams(final Map<String, String> map) {
            return new RetryRequest.RetryURI(
                this.request.uri().queryParams(map).back()
            );
        }
        @Override
        public RequestURI path(final String segment) {
            return new RetryRequest.RetryURI(
                this.request.uri().path(segment).back()
            );
        }
        @Override
        public RequestURI userInfo(final String info) {
            return new RetryRequest.RetryURI(
                this.request.uri().userInfo(info).back()
            );
        }
    }

    /**
     * Retry Body.
     */
    @Immutable
    @EqualsAndHashCode(of = "request")
    private static final class RetryBody implements RequestBody {
        /**
         * Original request encapsulated.
         */
        private final transient Request request;
        /**
         * Public ctor.
         * @param req Request
         */
        public RetryBody(final Request req) {
            this.request = req;
        }
        @Override
        public String toString() {
            return this.request.body().toString();
        }
        @Override
        public Request back() {
            return new RetryRequest(this.request);
        }
        @Override
        public String get() {
            return this.request.body().get();
        }
        @Override
        public RequestBody set(final String txt) {
            return new RetryRequest.RetryBody(
                this.request.body().set(txt).back()
            );
        }
        @Override
        public RequestBody set(final byte[] txt) {
            return new RetryRequest.RetryBody(
                this.request.body().set(txt).back()
            );
        }
        @Override
        public RequestBody formParam(final String name, final Object value) {
            return new RetryRequest.RetryBody(
                this.request.body().formParam(name, value).back()
            );
        }
    }
}
