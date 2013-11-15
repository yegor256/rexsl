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
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Implementation of {@link Request}, based on Apache HTTP client.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = "base")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class ApacheRequest implements Request {

    /**
     * The wire to use.
     * @checkstyle AnonInnerLength (200 lines)
     */
    private static final Wire WIRE = new Wire() {
        /**
         * {@inheritDoc}
         * @checkstyle ParameterNumber (6 lines)
         */
        @Override
        public Response send(final Request req, final String home,
            final String method,
            final Collection<Map.Entry<String, String>> headers,
            final String content) throws IOException {
            final CloseableHttpResponse response =
                HttpClients.createDefault().execute(
                    this.httpRequest(home, method, headers, content)
                );
            try {
                return new DefaultResponse(
                    req,
                    response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase(),
                    this.headers(response.getAllHeaders()),
                    this.consume(response.getEntity())
                );
            } finally {
                response.close();
            }
        }
        /**
         * Create request.
         * @return Request
         * @checkstyle ParameterNumber (6 lines)
         */
        public HttpEntityEnclosingRequestBase httpRequest(final String home,
            final String method,
            final Collection<Map.Entry<String, String>> headers,
            final String content) {
            final HttpEntityEnclosingRequestBase req =
                new HttpEntityEnclosingRequestBase() {
                    @Override
                    public String getMethod() {
                        return method;
                    }
                };
            final URI uri = URI.create(home);
            req.setURI(uri);
            req.setEntity(new StringEntity(content, Charsets.UTF_8));
            for (final Map.Entry<String, String> header : headers) {
                req.addHeader(header.getKey(), header.getValue());
            }
            return req;
        }
        /**
         * Fetch body from http entity.
         * @param entity HTTP entity
         * @return Body in UTF-8
         * @throws IOException If fails
         */
        private String consume(final HttpEntity entity) throws IOException {
            String body = "";
            if (entity != null) {
                body = EntityUtils.toString(entity, Charsets.UTF_8);
            }
            return body;
        }
        /**
         * Make a list of all hdrs.
         * @param list Apache HTTP hdrs
         * @return Body in UTF-8
         */
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        private Array<Map.Entry<String, String>> headers(
            final org.apache.http.Header... list) {
            final Collection<Map.Entry<String, String>> headers =
                new LinkedList<Map.Entry<String, String>>();
            for (final org.apache.http.Header header : list) {
                headers.add(new Header(header.getName(), header.getValue()));
            }
            return new Array<Map.Entry<String, String>>(headers);
        }
    };

    /**
     * Base request.
     */
    private final transient Request base;

    /**
     * Public ctor.
     * @param uri The resource to work with
     */
    public ApacheRequest(@NotNull(message = "URI can't be NULL")
    final URI uri) {
        this(uri.toString());
    }

    /**
     * Public ctor.
     * @param uri The resource to work with
     */
    public ApacheRequest(@NotNull(message = "URI can't be NULL")
    final String uri) {
        this.base = new BaseRequest(ApacheRequest.WIRE, uri);
    }

    @Override
    @NotNull
    public RequestURI uri() {
        return this.base.uri();
    }

    @Override
    public Request header(
        @NotNull(message = "header name can't be NULL") final String name,
        @NotNull(message = "header value can't be NULL") final Object value) {
        return this.base.header(name, value);
    }

    @Override
    public RequestBody body() {
        return this.base.body();
    }

    @Override
    public Request method(
        @NotNull(message = "method can't be NULL") final String method) {
        return this.base.method(method);
    }

    @Override
    public Response fetch() throws IOException {
        return this.base.fetch();
    }

}
