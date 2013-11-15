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
package com.rexsl.page;

import com.jcabi.log.Logger;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import lombok.ToString;

/**
 * Custom implementation of {@link UriInfo} that is aware of
 * {@code X-Forwarded-For} HTTP header.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @see <a href="http://tools.ietf.org/html/draft-ietf-appsawg-http-forwarded-10">IETF Forwarded HTTP Extension</a>
 */
@ToString
final class ForwardedUriInfo implements UriInfo {

    /**
     * Original {@link UriInfo}.
     */
    private final transient UriInfo info;

    /**
     * Http headers, injected by JAX-RS implementation.
     */
    private final transient AtomicReference<HttpHeaders> headers;

    /**
     * TRUE if HTTP headers already where analyzed.
     */
    private transient boolean analyzed;

    /**
     * New host, or empty string if not required, or NULL if not yet sure.
     */
    private transient String host;

    /**
     * Scheme to set, or NULL if not necessary.
     */
    private transient String scheme;

    /**
     * Public ctor.
     * @param inf The original UriInfo
     * @param hdrs HTTP headers
     */
    ForwardedUriInfo(final UriInfo inf,
        final AtomicReference<HttpHeaders> hdrs) {
        if (inf == null) {
            throw new IllegalStateException(
                "UriInfo is incorrectly injected into BaseResource"
            );
        }
        this.info = inf;
        this.headers = hdrs;
    }

    @Override
    @NotNull
    public URI getAbsolutePath() {
        return this.getAbsolutePathBuilder().build();
    }

    @Override
    @NotNull
    public UriBuilder getAbsolutePathBuilder() {
        return this.forward(this.info.getAbsolutePathBuilder());
    }

    @Override
    @NotNull
    public URI getBaseUri() {
        return this.getBaseUriBuilder().build();
    }

    @Override
    @NotNull
    public UriBuilder getBaseUriBuilder() {
        return this.forward(this.info.getBaseUriBuilder());
    }

    @Override
    @NotNull
    public URI getRequestUri() {
        return this.getRequestUriBuilder().build();
    }

    @Override
    @NotNull
    public UriBuilder getRequestUriBuilder() {
        return this.forward(this.info.getRequestUriBuilder());
    }

    @Override
    @NotNull
    public List<Object> getMatchedResources() {
        return this.info.getMatchedResources();
    }

    @Override
    @NotNull
    public List<String> getMatchedURIs() {
        return this.info.getMatchedURIs();
    }

    @Override
    @NotNull
    public List<String> getMatchedURIs(final boolean decode) {
        return this.info.getMatchedURIs(decode);
    }

    @Override
    @NotNull
    public String getPath() {
        return this.info.getPath();
    }

    @Override
    @NotNull
    public String getPath(final boolean decode) {
        return this.info.getPath(decode);
    }

    @Override
    @NotNull
    public MultivaluedMap<String, String> getPathParameters() {
        return this.info.getPathParameters();
    }

    @Override
    @NotNull
    public MultivaluedMap<String, String> getPathParameters(
        final boolean decode) {
        return this.info.getPathParameters(decode);
    }

    @Override
    @NotNull
    public List<PathSegment> getPathSegments() {
        return this.info.getPathSegments();
    }

    @Override
    @NotNull
    public List<PathSegment> getPathSegments(final boolean decode) {
        return this.info.getPathSegments(decode);
    }

    @Override
    @NotNull
    public MultivaluedMap<String, String> getQueryParameters() {
        return this.info.getQueryParameters();
    }

    @Override
    @NotNull
    public MultivaluedMap<String, String> getQueryParameters(
        final boolean decode) {
        return this.info.getQueryParameters(decode);
    }

    /**
     * Forward this builder to the right host/port (if necessary).
     * @param builder The builder to forward
     * @return The same builder
     */
    private UriBuilder forward(final UriBuilder builder) {
        if (!this.analyzed) {
            if (this.headers.get() == null) {
                throw new IllegalStateException(
                    "HttpHeaders is not injected into BaseResource"
                );
            }
            for (final Map.Entry<String, List<String>> header
                : this.headers.get().getRequestHeaders().entrySet()) {
                for (final String value : header.getValue()) {
                    this.consume(header.getKey(), value);
                }
            }
            Logger.debug(
                this,
                "#forward(..): analyzed, host=%s, scheme=%s",
                this.host,
                this.scheme
            );
            this.analyzed = true;
        }
        if (this.host != null) {
            builder.host(this.host);
        }
        if (this.scheme != null) {
            builder.scheme(this.scheme);
        }
        return builder;
    }

    /**
     * Interpret HTTP header and save host/scheme pair into this object.
     * @param name HTTP header name
     * @param value HTTP header value
     * @see <a href="http://tools.ietf.org/html/draft-ietf-appsawg-http-forwarded-10">IETF Forwarded HTTP Extension</a>
     */
    private void consume(final String name, final String value) {
        if (this.host == null
            && "x-forwarded-host".equals(name.toLowerCase(Locale.ENGLISH))) {
            this.host = value;
        } else if (this.scheme == null
            && "x-forwarded-proto".equals(name.toLowerCase(Locale.ENGLISH))) {
            this.scheme = value;
        } else if ("forwarded".equals(name.toLowerCase(Locale.ENGLISH))) {
            this.forwarded(value);
        }
    }

    /**
     * Consume specifically "Forwarded" header.
     * @param value HTTP header value
     */
    private void forwarded(final String value) {
        for (final String sector : value.split("\\s*,\\s*")) {
            for (final String opt : sector.split("\\s*;\\s*")) {
                final String[] parts = opt.split("=", 2);
                if (this.host == null && "host".equals(parts[0])) {
                    this.host = parts[1];
                }
                if (this.scheme == null && "proto".equals(parts[0])) {
                    this.scheme = parts[1];
                }
            }
        }
    }

}
