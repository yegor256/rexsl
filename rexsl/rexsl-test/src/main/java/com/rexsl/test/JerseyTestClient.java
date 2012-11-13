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
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.CharEncoding;

/**
 * Implementation of {@link TestClient}.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
final class JerseyTestClient implements TestClient {

    /**
     * Jersey web resource.
     */
    private final transient WebResource resource;

    /**
     * Headers.
     */
    private final transient List<Header> headers = new ArrayList<Header>();

    /**
     * Entry point.
     */
    private final transient URI home;

    /**
     * Public ctor.
     * @param res The resource to work with
     */
    public JerseyTestClient(@NotNull final WebResource res) {
        this.resource = res;
        this.home = res.getURI();
        final String info = this.home.getUserInfo();
        if (info != null) {
            final String[] parts = info.split(":", 2);
            try {
                this.header(
                    HttpHeaders.AUTHORIZATION,
                    Logger.format(
                        "Basic %s",
                        Base64.encodeBase64String(
                            Logger.format(
                                "%s:%s",
                                URLDecoder.decode(parts[0], CharEncoding.UTF_8),
                                URLDecoder.decode(parts[1], CharEncoding.UTF_8)
                            ).getBytes()
                        )
                    )
                );
            } catch (java.io.UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI uri() {
        return this.home;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestClient header(@NotNull final String name,
        @NotNull final Object value) {
        synchronized (this.headers) {
            boolean exists = false;
            for (Header header : this.headers) {
                if (header.getKey().equals(name)
                    && header.getValue().equals(value.toString())) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                Logger.debug(this, "#header('%s', '%s'): dupe", name, value);
            } else {
                this.headers.add(new Header(name, value.toString()));
                Logger.debug(this, "#header('%s', '%s'): set", name, value);
            }
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse get(@NotNull final String desc) {
        return new JerseyTestResponse(
            new JerseyFetcher() {
                @Override
                public ClientResponse fetch() {
                    return JerseyTestClient.this
                        .method(RestTester.GET, "", desc);
                }
            },
            new RequestDecor(this.headers, "")
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse post(@NotNull final String desc,
        @NotNull final Object body) {
        final String content = body.toString();
        return new JerseyTestResponse(
            new JerseyFetcher() {
                @Override
                public ClientResponse fetch() {
                    return JerseyTestClient.this
                        .method(RestTester.POST, content, desc);
                }
            },
            new RequestDecor(this.headers, content)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse put(@NotNull final String desc,
        @NotNull final Object body) {
        final String content = body.toString();
        return new JerseyTestResponse(
            new JerseyFetcher() {
                @Override
                public ClientResponse fetch() {
                    return JerseyTestClient.this
                        .method(RestTester.PUT, content, desc);
                }
            },
            new RequestDecor(this.headers, content)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse delete(@NotNull final String desc) {
        return new JerseyTestResponse(
            new JerseyFetcher() {
                @Override
                public ClientResponse fetch() {
                    return JerseyTestClient.this
                        .method(RestTester.DELETE, "", desc);
                }
            },
            new RequestDecor(this.headers, "")
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse head(@NotNull final String desc) {
        return new JerseyTestResponse(
            new JerseyFetcher() {
                @Override
                public ClientResponse fetch() {
                    return JerseyTestClient.this
                        .method(RestTester.HEAD, "", desc);
                }
            },
            new RequestDecor(this.headers, "")
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse options(@NotNull final String desc,
        @NotNull final Object body) {
        final String content = body.toString();
        return new JerseyTestResponse(
            new JerseyFetcher() {
                @Override
                public ClientResponse fetch() {
                    return JerseyTestClient.this
                        .method(RestTester.OPTIONS, content, desc);
                }
            },
            new RequestDecor(this.headers, content)
        );
    }

    /**
     * Run this method.
     * @param name The name of HTTP method
     * @param body Body of HTTP request
     * @param desc Description of the operation, for logging
     * @return The response
     */
    private ClientResponse method(final String name, final String body,
        final String desc) {
        final WebResource.Builder builder = this.resource.getRequestBuilder();
        for (Header header : this.headers) {
            builder.header(header.getKey(), header.getValue());
        }
        final long start = System.currentTimeMillis();
        ClientResponse resp;
        if (RestTester.GET.equals(name)) {
            resp = builder.get(ClientResponse.class);
        } else if (RestTester.HEAD.equals(name)) {
            resp = builder.head();
        } else if (RestTester.DELETE.equals(name)) {
            resp = builder.delete(ClientResponse.class);
        } else {
            resp = builder.method(name, ClientResponse.class, body);
        }
        Logger.debug(
            this,
            "#%s('%s'): HTTP request body:\n%s",
            name,
            this.home.getPath(),
            new RequestDecor(this.headers, body)
        );
        Logger.info(
            this,
            "#%s('%s'): \"%s\" completed in %[ms]s [%d %s]: %s",
            name,
            this.home.getPath(),
            desc,
            System.currentTimeMillis() - start,
            resp.getStatus(),
            resp.getClientResponseStatus(),
            this.home
        );
        return resp;
    }

}
