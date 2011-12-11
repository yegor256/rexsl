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
package com.rexsl.test;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.ymock.util.Logger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

/**
 * Mocker of Java Servlet container.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class ContainerMocker {

    /**
     * Grizzly adapter.
     */
    private final transient ContainerMocker.Adapter adapter =
        new ContainerMocker.Adapter();

    /**
     * Grizzly container.
     */
    private transient GrizzlyWebServer gws;

    /**
     * Port where it works.
     */
    private transient Integer port;

    /**
     * Expect body.
     * @param matcher Matcher for body
     * @return This object
     */
    public ContainerMocker expectBody(final Matcher<String> matcher) {
        this.adapter.setBodyMatcher(matcher);
        return this;
    }

    /**
     * Expect request URI to match this matcher.
     * @param matcher Matcher for request path
     * @return This object
     */
    public ContainerMocker expectRequestUri(final Matcher<String> matcher) {
        this.adapter.setRequestUriMatcher(matcher);
        return this;
    }

    /**
     * Expect this header with this matcher.
     * @param name Name of the header.
     * @param matcher Matcher for body
     * @return This object
     */
    public ContainerMocker expectHeader(final String name,
        final Matcher<String> matcher) {
        this.adapter.addHeaderMatcher(name, matcher);
        return this;
    }

    /**
     * Return this body.
     * @param body The body
     * @return This object
     */
    public ContainerMocker returnBody(final String body) {
        this.adapter.setBody(body);
        return this;
    }

    /**
     * Return this header.
     * @param name Header's name
     * @param value Header's value
     * @return This object
     */
    public ContainerMocker returnHeader(final String name, final String value) {
        this.adapter.addHeader(name, value);
        return this;
    }

    /**
     * Return this status.
     * @param code The code
     * @return This object
     */
    public ContainerMocker returnStatus(final int code) {
        this.adapter.setStatus(code);
        return this;
    }

    /**
     * Mock it, and return this object.
     * @throws Exception If something goes wrong inside
     */
    public ContainerMocker mock() throws Exception {
        this.port = this.reservePort();
        this.gws = new GrizzlyWebServer(this.port);
        this.gws.addGrizzlyAdapter(this.adapter, new String[] {"/"});
        this.start();
        return this;
    }

    /**
     * Mock it, and return this object.
     * @throws Exception If something goes wrong inside
     */
    public void start() throws Exception {
        this.gws.start();
        Logger.debug(
            this,
            "#start(): Grizzly started at port #%s",
            this.port
        );
    }

    /**
     * Stop Servlet container.
     * @throws Exception If something goes wrong inside
     */
    public void stop() throws Exception {
        this.gws.stop();
        Logger.debug(
            this,
            "#stop(): Grizzly stopped at port #%s",
            this.port
        );
    }

    /**
     * Get its home.
     * @throws Exception If something goes wrong inside
     */
    public URI home() throws Exception {
        return new URI("http", "", "localhost", this.port, "", "", "");
    }

    /**
     * Reserve port.
     * @throws Exception If something goes wrong inside
     */
    private Integer reservePort() throws Exception {
        final ServerSocket socket = new ServerSocket(0);
        final Integer reserved = socket.getLocalPort();
        socket.close();
        return reserved;
    }

    /**
     * Adapter for Grizzly container.
     */
    private static final class Adapter extends GrizzlyAdapter {
        /**
         * Body matcher.
         */
        private Matcher<String> bodyMatcher;
        /**
         * Request URI matcher.
         */
        private Matcher<String> requestUriMatcher;
        /**
         * Header matchers.
         */
        private ConcurrentMap<String, Matcher<String>> headerMatchers =
            new ConcurrentHashMap<String, Matcher<String>>();
        /**
         * Content to return.
         */
        private String body = "";
        /**
         * Status to return.
         */
        private int status = HttpURLConnection.HTTP_OK;
        /**
         * Headers to return.
         */
        private final ConcurrentMap<String, String> headers =
            new ConcurrentHashMap<String, String>();
        /**
         * {@inheritDoc}
         */
        @Override
        public void service(final GrizzlyRequest request,
            final GrizzlyResponse response) {
            this.assertRequestUri(request);
            this.assertBody(request);
            this.assertHeaders(request);
            for (ConcurrentMap.Entry<String, String> entry
                : this.headers.entrySet()) {
                response.addHeader(entry.getKey(), entry.getValue());
            }
            response.setStatus(this.status);
            try {
                response.getWriter().println(this.body);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            response.setContentLength(this.body.length());
        }
        /**
         * Set request URI matcher.
         * @param matcher The matcher to set
         */
        public void setRequestUriMatcher(final Matcher<String> matcher) {
            this.requestUriMatcher = matcher;
        }
        /**
         * Set body matcher.
         * @param matcher The matcher to set
         */
        public void setBodyMatcher(final Matcher<String> matcher) {
            this.bodyMatcher = matcher;
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
         * @param content The body to return
         */
        public void setBody(final String content) {
            this.body = content;
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
         * Make assertions about body.
         * @param request The HTTP grizzly request
         */
        private void assertBody(final GrizzlyRequest request) {
            if (this.bodyMatcher != null) {
                try {
                    MatcherAssert.assertThat(
                        String.format(
                            "Body matches provided matcher in:%n%s",
                            this.asText(request)
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
         */
        private void assertHeaders(final GrizzlyRequest request) {
            for (ConcurrentMap.Entry<String, Matcher<String>> entry
                : this.headerMatchers.entrySet()) {
                MatcherAssert.assertThat(
                    String.format(
                        "Header '%s' matches specified matcher in:%n%s",
                        entry.getKey(),
                        this.asText(request)
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
         */
        private String asText(final GrizzlyRequest request) {
            final StringBuilder builder = new StringBuilder();
            for (Object name : Collections.list(request.getHeaderNames())) {
                builder.append(
                    String.format(
                        "%s: [%s]\n",
                        (String) name,
                        StringUtils.join(
                            Collections.list(request.getHeaders((String) name)),
                            "], ["
                        )
                    )
                );
            }
            return builder.toString();
        }
    }

}
