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
package com.rexsl.page;

import com.jcabi.log.Logger;
import java.net.URI;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Custom implementation of {@link UriInfo} that is aware of
 * {@code X-Forwarded-For} HTTP header.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id: BaseResource.java 2145 2012-10-28 16:07:02Z yegor@tpc2.com $
 * @see <a href="http://tools.ietf.org/html/draft-ietf-appsawg-http-forwarded-10">IETF Forwarded HTTP Extension</a>
 */
final class ForwardedUriInfo implements UriInfo {

    /**
     * Original {@link UriInfo}.
     */
    private final transient UriInfo info;

    /**
     * Http headers, injected by JAX-RS implementation.
     */
    private final transient HttpHeaders headers;

    /**
     * New host, or empty string if not required, or NULL if not yet sure.
     */
    private transient String host;

    /**
     * Scheme to set, or NULL if not necessary
     */
    private transient String scheme;

    /**
     * Public ctor.
     * @param inf The original UriInfo
     * @param hdrs HTTP headers
     */
    public ForwardedUriInfo(final UriInfo inf, final HttpHeaders hdrs) {
        this.info = inf;
        this.headers = hdrs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getAbsolutePath() {
        return this.getAbsolutePathBuilder().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return this.forward(this.info.getAbsolutePathBuilder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getBaseUri() {
        return this.getBaseUriBuilder().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UriBuilder getBaseUriBuilder() {
        return this.forward(this.info.getBaseUriBuilder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getRequestUri() {
        return this.getRequestUriBuilder().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UriBuilder getRequestUriBuilder() {
        return this.forward(this.info.getRequestUriBuilder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getMatchedResources() {
        return this.info.getMatchedResources();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getMatchedURIs() {
        return this.info.getMatchedURIs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getMatchedURIs(final boolean decode) {
        return this.info.getMatchedURIs(decode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return this.info.getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath(final boolean decode) {
        return this.info.getPath(decode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return this.info.getPathParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultivaluedMap<String, String> getPathParameters(
        final boolean decode) {
        return this.info.getPathParameters(decode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PathSegment> getPathSegments() {
        return this.info.getPathSegments();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PathSegment> getPathSegments(final boolean decode) {
        return this.info.getPathSegments(decode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return this.info.getQueryParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        if (this.host == null) {
            this.interpret();
        }
        if (!this.host.isEmpty()) {
            builder.host(this.host).scheme(this.scheme);
        }
        return builder;
    }

    /**
     * Interpret HTTP headers and save host/scheme pair into this object.
     * @see <a href="http://tools.ietf.org/html/draft-ietf-appsawg-http-forwarded-10">IETF Forwarded HTTP Extension</a>
     */
    private void interpret() {
        final MultivaluedMap<String, String> map =
            this.headers.getRequestHeaders();
        com.jcabi.log.Logger.info(this, "map: %[list]s", map.keySet());
        final List<String> hosts = map.get("X-Forwarded-Host");
        if (hosts != null && !hosts.isEmpty()) {
            this.host = hosts.get(hosts.size() - 1);
        }
        final List<String> protos = map.get("X-Forwarded-Proto");
        if (protos != null && !protos.isEmpty()) {
            this.scheme = protos.get(protos.size() - 1);
        }
        final List<String> fwds = map.get("Forwarded");
        if (fwds != null) {
            for (String fwd : fwds) {
                for (String sector : fwd.split("\\s*,\\s*")) {
                    for (String pair : sector.split("\\s*;\\s*")) {
                        final String[] parts = pair.split("=", 2);
                        if (parts[0].equals("host")) {
                            this.host = parts[1];
                        }
                        if (parts[0].equals("proto")) {
                            this.scheme = parts[1];
                        }
                    }
                }
            }
        }
        if (this.host == null) {
            this.host = "";
        }
    }

}
