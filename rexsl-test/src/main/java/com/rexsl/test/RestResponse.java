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
import com.jcabi.log.Logger;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * REST response.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class RestResponse extends AbstractResponse {

    /**
     * Public ctor.
     * @param resp Response
     */
    public RestResponse(
        @NotNull(message = "response can't be NULL") final Response resp) {
        super(resp);
    }

    /**
     * Assert using custom matcher.
     * @param matcher The matcher to use
     * @return The same object
     */
    @NotNull(message = "response is never NULL")
    public RestResponse assertThat(
        @NotNull(message = "matcher can't be NULL")
        final Matcher<Response> matcher) {
        MatcherAssert.assertThat(
            String.format("HTTP response is not valid: %s", this),
            this,
            matcher
        );
        return this;
    }

    /**
     * Verifies HTTP response status code against the provided absolute value,
     * and throws {@link AssertionError} in case of mismatch.
     * @param status Expected status code
     * @return The same object
     */
    @NotNull(message = "response is never NULL")
    public RestResponse assertStatus(final int status) {
        final String message = String.format(
            "HTTP response with status %d", status
        );
        MatcherAssert.assertThat(
            String.format(
                "HTTP response status is not equal to %d:\n%s",
                status, this
            ),
            this,
            new CustomMatcher<Response>(message) {
                @Override
                public boolean matches(final Object resp) {
                    return Response.class.cast(resp).status() == status;
                }
            }
        );
        return this;
    }

    /**
     * Verifies HTTP response status code against the provided matcher,
     * and throws {@link AssertionError} in case of mismatch.
     * @param matcher Matcher to validate status code
     * @return This object
     */
    @NotNull(message = "response is never NULL")
    public RestResponse assertStatus(
        @NotNull(message = "matcher can't be NULL")
        final Matcher<Integer> matcher) {
        MatcherAssert.assertThat(
            String.format(
                "HTTP response status is not the one expected:\n%s",
                this
            ),
            this.status(), matcher
        );
        return this;
    }

    /**
     * Verifies HTTP response body content against provided matcher,
     * and throws {@link AssertionError} in case of mismatch.
     * @param matcher The matcher to use
     * @return This object
     */
    @NotNull(message = "response is never NULL")
    public RestResponse assertBody(
        @NotNull(message = "matcher can't be NULL")
        final Matcher<String> matcher) {
        MatcherAssert.assertThat(
            String.format(
                "HTTP response body content is not valid:\n%s",
                this
            ),
            this.body(), matcher
        );
        return this;
    }

    /**
     * Verifies HTTP header against provided matcher, and throws
     * {@link AssertionError} in case of mismatch.
     *
     * <p>The iterator for the matcher will always be a real object an never
     * {@code NULL}, even if such a header is absent in the response. If the
     * header is absent the iterable will be empty.
     *
     * @param name Name of the header to match
     * @param matcher The matcher to use
     * @return This object
     */
    @NotNull(message = "response is never NULL")
    public RestResponse assertHeader(
        @NotNull(message = "header name can't be NULL") final String name,
        @NotNull(message = "matcher can't be NULL")
        final Matcher<Iterable<String>> matcher) {
        Iterable<String> values = this.headers().get(name);
        if (values == null) {
            values = Collections.emptyList();
        }
        MatcherAssert.assertThat(
            String.format(
                "HTTP header %s is not valid:\n%s",
                name, this
            ),
            values, matcher
        );
        return this;
    }

    /**
     * Jump to a new location.
     * @param uri Destination to jump to
     * @return New request
     */
    @NotNull(message = "request is never NULL")
    public Request jump(@NotNull(message = "URI can't be NULL") final URI uri) {
        Request req = this.back().uri().set(uri).back();
        final Map<String, List<String>> headers = this.headers();
        if (headers.containsKey(HttpHeaders.SET_COOKIE)) {
            for (final String header : headers.get(HttpHeaders.SET_COOKIE)) {
                for (final HttpCookie cookie : HttpCookie.parse(header)) {
                    req = req.header(
                        HttpHeaders.COOKIE,
                        String.format(
                            "%s=%s",
                            cookie.getName(),
                            cookie.getValue()
                        )
                    );
                }
            }
        }
        return req;
    }

    /**
     * Follow LOCATION header.
     * @return New request
     */
    @NotNull(message = "request is never NULL")
    public Request follow() {
        this.assertHeader(
            HttpHeaders.LOCATION,
            Matchers.not(Matchers.emptyIterableOf(String.class))
        );
        return this.jump(
            URI.create(this.headers().get(HttpHeaders.LOCATION).get(0))
        );
    }

    /**
     * Get one cookie by name.
     * @param name Cookie name
     * @return Cookie found
     */
    @NotNull(message = "cookie is never NULL")
    public Cookie cookie(@NotNull final String name) {
        final Map<String, List<String>> headers = this.headers();
        MatcherAssert.assertThat(
            "cookies should be set in HTTP header",
            headers.containsKey(HttpHeaders.SET_COOKIE)
        );
        final String header = StringUtils.join(
            headers.get(HttpHeaders.SET_COOKIE), ", "
        );
        Cookie cookie = null;
        for (final HttpCookie candidate : HttpCookie.parse(header)) {
            if (candidate.getName().equals(name)) {
                cookie = new Cookie(
                    candidate.getName(),
                    candidate.getValue(),
                    candidate.getPath(),
                    candidate.getDomain(),
                    candidate.getVersion()
                );
                break;
            }
        }
        MatcherAssert.assertThat(
            Logger.format(
                "cookie '%s' not found in Set-Cookie header: '%s'",
                name, header
            ),
            cookie,
            Matchers.notNullValue()
        );
        assert cookie != null;
        return cookie;
    }

}
