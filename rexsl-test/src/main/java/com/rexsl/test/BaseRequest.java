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
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.CharEncoding;

/**
 * Implementation of {@link Request}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "home", "mtd", "hdrs", "content" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
final class BaseRequest implements Request {

    /**
     * Default user agent.
     */
    private static final String AGENT = String.format(
        "ReXSL-%s/%s Java/%s",
        Manifests.read("ReXSL-Version"),
        Manifests.read("ReXSL-Build"),
        System.getProperty("java.version")
    );

    /**
     * Wire to use.
     */
    private final transient Wire wire;

    /**
     * Request URI.
     */
    private final transient String home;

    /**
     * Method to use.
     */
    private final transient String mtd;

    /**
     * Headers.
     */
    private final transient Array<Header> hdrs;

    /**
     * Body to use.
     */
    private final transient String content;

    /**
     * Public ctor.
     * @param wre Wire
     */
    BaseRequest(final Wire wre) {
        this(wre, "#", new Array<Header>(), Request.GET, "");
    }

    /**
     * Public ctor.
     * @param wre Wire
     * @param uri The resource to work with
     */
    BaseRequest(final Wire wre, final String uri) {
        this(wre, uri, new Array<Header>(), Request.GET, "");
    }

    /**
     * Public ctor.
     * @param wre Wire
     * @param uri The resource to work with
     * @param headers Headers
     * @param method HTTP method
     * @param body HTTP request body
     * @checkstyle ParameterNumber (5 lines)
     */
    BaseRequest(final Wire wre, final String uri,
        final Iterable<Header> headers,
        final String method, final String body) {
        this.wire = wre;
        this.home = uri;
        this.hdrs = new Array<Header>(headers);
        this.mtd = method;
        this.content = body;
    }

    @Override
    @NotNull
    public RequestURI uri() {
        return new BaseURI(this, this.home);
    }

    @Override
    public Request header(
        @NotNull(message = "header name can't be NULL") final String name,
        @NotNull(message = "header value can't be NULL") final Object value) {
        return new BaseRequest(
            this.wire,
            this.home,
            this.hdrs.with(new Header(name, value.toString())),
            this.mtd,
            this.content
        );
    }

    @Override
    public RequestBody body() {
        return new BaseBody(this, this.content);
    }

    @Override
    public Request method(
        @NotNull(message = "method can't be NULL") final String method) {
        return new BaseRequest(
            this.wire,
            this.home,
            this.hdrs,
            method,
            this.content
        );
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Response fetch() throws IOException {
        final long start = System.currentTimeMillis();
        final Collection<Map.Entry<String, String>> headers =
            new LinkedList<Map.Entry<String, String>>();
        boolean agent = false;
        for (final Map.Entry<String, String> header : this.hdrs) {
            headers.add(new Header(header.getKey(), header.getValue()));
            if (header.getKey().equals(HttpHeaders.USER_AGENT)) {
                agent = true;
            }
        }
        if (!agent) {
            headers.add(new Header(HttpHeaders.USER_AGENT, BaseRequest.AGENT));
        }
        final String info = URI.create(this.home).getUserInfo();
        if (info != null) {
            final String[] parts = info.split(":", 2);
            try {
                headers.add(
                    new Header(
                        HttpHeaders.AUTHORIZATION,
                        Logger.format(
                            "Basic %s",
                            Base64.encodeBase64String(
                                Logger.format(
                                    "%s:%s",
                                    URLDecoder.decode(
                                        parts[0], CharEncoding.UTF_8
                                    ),
                                    URLDecoder.decode(
                                        parts[1], CharEncoding.UTF_8
                                    )
                                ).getBytes()
                            )
                        )
                    )
                );
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }
        final Response response = this.wire.send(
            this, this.home, this.mtd, headers, this.content
        );
        Logger.info(
            this,
            "#fetch(%s %s): completed in %[ms]s [%d %s]: %s",
            this.mtd,
            URI.create(this.home).getPath(),
            System.currentTimeMillis() - start,
            response.status(),
            response.reason(),
            this.home
        );
        return response;
    }

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder("HTTP/1.1 ")
            .append(this.mtd).append(' ')
            .append(URI.create(this.home).getPath())
            .append('\n');
        for (final Map.Entry<String, String> header : this.hdrs) {
            text.append(
                Logger.format(
                    "%s: %s\n",
                    header.getKey(),
                    header.getValue()
                )
            );
        }
        text.append('\n');
        if (this.content.isEmpty()) {
            text.append("<<empty request body>>");
        } else {
            text.append(this.content);
        }
        return text.toString();
    }

    /**
     * Base URI.
     */
    @Immutable
    @EqualsAndHashCode(of = "address")
    private static final class BaseURI implements RequestURI {
        /**
         * URI encapsulated.
         */
        private final transient String address;
        /**
         * Base request encapsulated.
         */
        private final transient BaseRequest owner;
        /**
         * Public ctor.
         * @param req Request
         * @param uri The URI to start with
         */
        public BaseURI(final BaseRequest req, final String uri) {
            this.owner = req;
            this.address = uri;
        }
        @Override
        public String toString() {
            return this.address;
        }
        @Override
        public Request back() {
            return new BaseRequest(
                this.owner.wire,
                this.address,
                this.owner.hdrs,
                this.owner.mtd,
                this.owner.content
            );
        }
        @Override
        public URI get() {
            return URI.create(this.owner.home);
        }
        @Override
        public RequestURI set(@NotNull(message = "URI can't be NULL")
            final URI uri) {
            return new BaseURI(this.owner, uri.toString());
        }
        @Override
        public RequestURI queryParam(
            @NotNull(message = "param name can't be NULL") final String name,
            @NotNull(message = "value can't be NULL") final Object value) {
            return new BaseURI(
                this.owner,
                UriBuilder.fromUri(this.address)
                    .queryParam(name, "{value}")
                    .build(value).toString()
            );
        }
        @Override
        public RequestURI queryParams(@NotNull(message = "map can't be NULL")
            final Map<String, String> map) {
            final UriBuilder uri = UriBuilder.fromUri(this.address);
            final Object[] values = new Object[map.size()];
            int idx = 0;
            for (final Map.Entry<String, String> pair : map.entrySet()) {
                uri.queryParam(pair.getKey(), String.format("{x%d}", idx));
                values[idx] = pair.getValue();
                ++idx;
            }
            return new BaseURI(
                this.owner,
                uri.build(values).toString()
            );
        }
        @Override
        public RequestURI path(
            @NotNull(message = "path can't be NULL") final String segment) {
            return new BaseURI(
                this.owner,
                UriBuilder.fromUri(this.address)
                    .path(segment)
                    .build().toString()
            );
        }
        @Override
        public RequestURI userInfo(
            @NotNull(message = "info can't be NULL") final String info) {
            return new BaseURI(
                this.owner,
                UriBuilder.fromUri(this.address)
                    .userInfo(info)
                    .build().toString()
            );
        }
    }

    /**
     * Base URI.
     */
    @Immutable
    @EqualsAndHashCode(of = "text")
    private static final class BaseBody implements RequestBody {
        /**
         * Content encapsulated.
         */
        private final transient String text;
        /**
         * Base request encapsulated.
         */
        private final transient BaseRequest owner;
        /**
         * Public ctor.
         * @param req Request
         * @param txt Text to encapsulate
         */
        public BaseBody(final BaseRequest req, final String txt) {
            this.owner = req;
            this.text = txt;
        }
        @Override
        public String toString() {
            return this.text;
        }
        @Override
        public Request back() {
            return new BaseRequest(
                this.owner.wire,
                this.owner.home,
                this.owner.hdrs,
                this.owner.mtd,
                this.text
            );
        }
        @Override
        public String get() {
            return this.text;
        }
        @Override
        public RequestBody set(@NotNull(message = "content can't be NULL")
            final String txt) {
            return new BaseBody(this.owner, txt);
        }
        @Override
        public RequestBody formParam(
            @NotNull(message = "name can't be NULL") final String name,
            @NotNull(message = "value can't be NULL") final Object value) {
            try {
                return new BaseBody(
                    this.owner,
                    new StringBuilder(this.text)
                        .append(name)
                        .append('=')
                        .append(
                            URLEncoder.encode(
                                value.toString(), CharEncoding.UTF_8
                            )
                        )
                        .append('&')
                        .toString()
                );
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

}
