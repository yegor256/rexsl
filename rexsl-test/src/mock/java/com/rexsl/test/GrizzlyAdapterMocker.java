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

import com.jcabi.log.Logger;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

/**
 * Mocker of Java Servlet container.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class GrizzlyAdapterMocker extends GrizzlyAdapter {

    /**
     * Body matcher.
     */
    private transient Matcher<String> bodyMatcher;

    /**
     * Method matcher.
     */
    private transient Matcher<String> methodMatcher;

    /**
     * Request URI matcher.
     */
    private transient Matcher<String> requestUriMatcher;

    /**
     * Param matchers.
     * @checkstyle LineLength (2 lines)
     */
    private final transient ConcurrentMap<String, Matcher<String>> paramMatchers =
        new ConcurrentHashMap<String, Matcher<String>>();

    /**
     * Header matchers.
     * @checkstyle LineLength (2 lines)
     */
    private final transient ConcurrentMap<String, Matcher<String>> headerMatchers =
        new ConcurrentHashMap<String, Matcher<String>>();

    /**
     * Content to return.
     */
    private transient Callable<byte[]> body = new Callable<byte[]>() {
        @Override
        public byte[] call() {
            return "".getBytes();
        }
    };

    /**
     * Status to return.
     */
    private transient int status = HttpURLConnection.HTTP_OK;

    /**
     * Headers to return.
     */
    private final transient ConcurrentMap<String, String> headers =
        new ConcurrentHashMap<String, String>();

    @Override
    @SuppressWarnings({ "PMD.AvoidCatchingThrowable", "rawtypes" })
    public void service(final GrizzlyRequest request,
        final GrizzlyResponse response) {
        try {
            final String input = IOUtils.toString(request.getInputStream());
            this.assertMethod(request, input);
            this.assertRequestUri(request);
            this.assertParams(request, input);
            this.assertBody(request, input);
            this.assertHeaders(request, input);
            for (ConcurrentMap.Entry<String, String> entry
                : this.headers.entrySet()) {
                response.addHeader(entry.getKey(), entry.getValue());
            }
            response.setStatus(this.status);
            final byte[] bytes = this.body.call();
            response.getStream().write(bytes);
            response.setContentLength(bytes.length);
        // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            GrizzlyAdapterMocker.fail(response, ex);
        }
    }

    /**
     * Set request URI matcher.
     * @param matcher The matcher to set
     */
    public void setRequestUriMatcher(final Matcher<String> matcher) {
        this.requestUriMatcher = matcher;
    }

    /**
     * Set method matcher.
     * @param matcher The matcher to set
     */
    public void setMethodMatcher(final Matcher<String> matcher) {
        this.methodMatcher = matcher;
    }

    /**
     * Set body matcher.
     * @param matcher The matcher to set
     */
    public void setBodyMatcher(final Matcher<String> matcher) {
        this.bodyMatcher = matcher;
    }

    /**
     * Add param matcher.
     * @param name Param's name
     * @param matcher The matcher to set
     */
    public void addParamMatcher(final String name,
        final Matcher<String> matcher) {
        this.paramMatchers.put(name, matcher);
    }

    /**
     * Add header matcher.
     * @param name Header's name
     * @param matcher The matcher to set
     */
    public void addHeaderMatcher(final String name,
        final Matcher<String> matcher) {
        this.headerMatchers.put(name, matcher);
    }

    /**
     * Set body.
     * @param callable The body to return
     */
    public void setBody(final Callable<byte[]> callable) {
        this.body = callable;
    }

    /**
     * Add header.
     * @param name Header's name
     * @param value Header's value
     */
    public void addHeader(final String name, final String value) {
        this.headers.put(name, value);
    }

    /**
     * Set status code.
     * @param code The status code
     */
    public void setStatus(final int code) {
        this.status = code;
    }

    /**
     * Check if adapter has body matcher.
     * @return True if adapter has body matcher
     */
    public boolean hasBodyMatcher() {
        return this.bodyMatcher != null;
    }

    /**
     * Check if adapter has at least one parameter matcher.
     * @return True if adapter has parameter matcher
     */
    public boolean hasParamMatcher() {
        return !this.paramMatchers.isEmpty();
    }

    /**
     * Make assertions about request URI.
     * @param request The HTTP grizzly request
     */
    private void assertRequestUri(final GrizzlyRequest request) {
        if (this.requestUriMatcher != null) {
            MatcherAssert.assertThat(
                "Request-URI matches provided matcher",
                request.getRequestURI(),
                this.requestUriMatcher
            );
        }
    }

    /**
     * Make assertions about method.
     * @param request The HTTP grizzly request
     * @param input Incoming stream of data
     */
    private void assertMethod(final GrizzlyRequest request,
        final String input) {
        if (this.methodMatcher != null) {
            MatcherAssert.assertThat(
                Logger.format(
                    "HTTP method matches provided matcher in:%s",
                    this.asText(request, input)
                ),
                request.getMethod(),
                this.methodMatcher
            );
        }
    }

    /**
     * Make assertions about HTTP parameters.
     * @param request The HTTP grizzly request
     * @param input Incoming stream of data
     */
    private void assertParams(final GrizzlyRequest request,
        final String input) {
        for (ConcurrentMap.Entry<String, Matcher<String>> entry
            : this.paramMatchers.entrySet()) {
            MatcherAssert.assertThat(
                Logger.format(
                    "Param '%s' matches specified matcher in:%s",
                    entry.getKey(),
                    this.asText(request, input)
                ),
                request.getParameter(entry.getKey()),
                entry.getValue()
            );
        }
    }

    /**
     * Make assertions about body.
     * @param request The HTTP grizzly request
     * @param input Incoming stream of data
     */
    private void assertBody(final GrizzlyRequest request,
        final String input) {
        if (this.bodyMatcher != null) {
            try {
                MatcherAssert.assertThat(
                    Logger.format(
                        "Body matches provided matcher in:%s",
                        this.asText(request, input)
                    ),
                    IOUtils.toString(request.getInputStream()),
                    this.bodyMatcher
                );
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * Make assertions about headers.
     * @param request The HTTP grizzly request
     * @param input Incoming stream of data
     */
    private void assertHeaders(final GrizzlyRequest request,
        final String input) {
        for (final Map.Entry<String, Matcher<String>> entry
            : this.headerMatchers.entrySet()) {
            MatcherAssert.assertThat(
                Logger.format(
                    "Header '%s' matches specified matcher in:%s",
                    entry.getKey(),
                    this.asText(request, input)
                ),
                request.getHeader(entry.getKey()),
                entry.getValue()
            );
        }
    }

    /**
     * Show request as text.
     * @param request The request
     * @return The text
     * @param input Incoming stream of data
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private String asText(final GrizzlyRequest request, final String input) {
        final StringBuilder builder = new StringBuilder();
        builder.append(request.getMethod())
            .append(" ")
            .append(request.getRequestURI())
            .append("  ")
            .append(request.getProtocol())
            .append("\n");
        final Enumeration<?> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement().toString();
            final Collection<String> values = new LinkedList<String>();
            final Enumeration<?> hdrs = request.getHeaders(name);
            while (hdrs.hasMoreElements()) {
                values.add(hdrs.nextElement().toString());
            }
            builder.append(
                Logger.format(
                    "%s: [%s]\n",
                    name,
                    StringUtils.join(values, "], [")
                )
            );
        }
        builder.append(input);
        return builder.toString();
    }

    /**
     * Notify this response about failure.
     * @param response The response to notify
     * @param failure The failure just happened
     */
    private static void fail(final GrizzlyResponse<?> response,
        final Throwable failure) {
        response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        final PrintStream stream = new PrintStream(response.getStream());
        stream.print(Logger.format("%[exception]s", failure));
        stream.close();
    }

}
