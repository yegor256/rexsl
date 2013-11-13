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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Implementation of {@link Request}, based on Apache HTTP client.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(of = { "home", "mtd", "headers", "content" })
@Loggable(Loggable.DEBUG)
public final class ApacheRequest implements Request {

    /**
     * UTF-8 error marker.
     */
    private static final String ERR = "\uFFFD";

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
     * Request URI.
     */
    private final transient RequestURI home;

    /**
     * Method to use.
     */
    private final transient String mtd;

    /**
     * Headers.
     */
    private final transient Array<Header> headers;

    /**
     * Body to use.
     */
    private final transient RequestBody content;

    /**
     * Public ctor.
     * @param uri The resource to work with
     */
    ApacheRequest(@NotNull(message = "URI can't be NULL")
        final URI uri) {
        this(uri.toString());
    }

    /**
     * Public ctor.
     * @param uri The resource to work with
     */
    ApacheRequest(@NotNull(message = "URI can't be NULL")
        final String uri) {
        this.home = new DefaultURI(this, URI.create(uri));
        this.headers = new Array<Header>();
        this.mtd = Request.GET;
        this.content = new DefaultBody(this, "");
    }

    /**
     * Public ctor.
     * @param uri The resource to work with
     * @param hdrs Headers
     * @param method HTTP method
     * @param body HTTP request body
     */
    private ApacheRequest(final RequestURI uri, final Iterable<Header> hdrs,
        final String method, final RequestBody body) {
        this.home = uri;
        this.headers = new Array<Header>(hdrs);
        this.mtd = method;
        this.content = body;
    }

    @Override
    @NotNull
    public RequestURI uri() {
        return this.home;
    }

    @Override
    public Request header(
        @NotNull(message = "header name can't be NULL") final String name,
        @NotNull(message = "header value can't be NULL") final Object value) {
        return new ApacheRequest(
            this.home,
            this.headers.with(new Header(name, value.toString())),
            this.mtd,
            this.content
        );
    }

    @Override
    public RequestBody body() {
        return this.content;
    }

    @Override
    public Request method(
        @NotNull(message = "method can't be NULL") final String method) {
        return new ApacheRequest(
            this.home,
            this.headers,
            method,
            this.content
        );
    }

    @Override
    public Response fetch() throws IOException {
        final CloseableHttpClient client = HttpClients.createDefault();
        final HttpEntityEnclosingRequestBase req =
            new HttpEntityEnclosingRequestBase() {
                @Override
                public String getMethod() {
                    return ApacheRequest.this.mtd;
                }
            };
        req.setURI(this.home.get());
        req.setEntity(new StringEntity(this.content.get(), Charsets.UTF_8));
        boolean agent = false;
        for (final Header header : this.headers) {
            req.addHeader(header.getKey(), header.getValue());
            if (header.sameAs(HttpHeaders.USER_AGENT)) {
                agent = true;
            }
        }
        if (!agent) {
            req.addHeader(
                HttpHeaders.USER_AGENT,
                ApacheRequest.AGENT
            );
        }
        final String info = this.home.get().getUserInfo();
        if (info != null) {
            final String[] parts = info.split(":", 2);
            try {
                req.addHeader(
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
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }
        final long start = System.currentTimeMillis();
        final CloseableHttpResponse response = client.execute(req);
        Logger.info(
            this,
            "#fetch(%s %s): completed in %[ms]s [%d %s]: %s",
            this.mtd,
            this.home.get().getPath(),
            System.currentTimeMillis() - start,
            response.getStatusLine().getStatusCode(),
            response.getStatusLine().getReasonPhrase(),
            this.home.get()
        );
        try {
            return new DefaultResponse(
                this,
                response.getStatusLine().getStatusCode(),
                response.getStatusLine().getReasonPhrase(),
                ApacheRequest.headers(response.getAllHeaders()),
                ApacheRequest.consume(response.getEntity())
            );
        } finally {
            response.close();
        }
    }

    /**
     * Fetch body from http entity.
     * @param entity HTTP entity
     * @return Body in UTF-8
     * @throws IOException If fails
     */
    private static String consume(final HttpEntity entity) throws IOException {
        String body = "";
        if (entity != null) {
            body = EntityUtils.toString(entity, Charsets.UTF_8);
        }
        if (body.contains(ApacheRequest.ERR)) {
            throw new IOException(
                String.format(
                    "broken Unicode text at line #%d in '%s' (%d bytes)",
                    StringUtils.countMatches(
                        "\n",
                        body.substring(0, body.indexOf(ApacheRequest.ERR))
                    ) + 2,
                    body,
                    body.getBytes().length
                )
            );
        }
        return body;
    }

    /**
     * Make a list of all headers.
     * @param list Apache HTTP headers
     * @return Body in UTF-8
     */
    private static Array<Map.Entry<String, String>> headers(
        final org.apache.http.Header... list) {
        final Collection<Map.Entry<String, String>> headers =
            new LinkedList<Map.Entry<String, String>>();
        for (final org.apache.http.Header header : list) {
            headers.add(new Header(header.getName(), header.getValue()));
        }
        return new Array<Map.Entry<String, String>>(headers);
    }

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder("HTTP/1.1 ")
            .append(this.mtd).append(' ')
            .append(this.home.get().getPath())
            .append('\n');
        for (final Map.Entry<String, String> header : this.headers) {
            text.append(
                Logger.format(
                    "%s: %s\n",
                    header.getKey(),
                    header.getValue()
                )
            );
        }
        text.append('\n');
        if (this.content.get().isEmpty()) {
            text.append("<<empty request body>>");
        } else {
            text.append(this.content.get());
        }
        return text.toString();
    }

}
