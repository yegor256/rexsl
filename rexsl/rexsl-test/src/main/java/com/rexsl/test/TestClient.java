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

import com.rexsl.test.client.BodyExtender;
import com.rexsl.test.client.Extender;
import com.rexsl.test.client.HeaderExtender;
import com.rexsl.test.client.Headers;
import com.ymock.util.Logger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Test HTTP client.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (200 lines)
 */
public final class TestClient {

    /**
     * Document root.
     */
    private final URI home;

    /**
     * List of HTTP request/response extenders.
     */
    private final List<Extender> extenders = new ArrayList<Extender>();

    /**
     * HTTP response.
     */
    private HttpResponse response;

    /**
     * Returned body.
     */
    private String body;

    /**
     * Public ctor.
     * @param uri Home of the server
     */
    public TestClient(final URI uri) {
        this.home = uri;
        this.response = null;
    }

    /**
     * Set request header.
     * @param name Header name
     * @param value Value of the header to set
     * @return This object
     */
    public TestClient header(final String name, final String value) {
        Logger.debug(this, "#header(%s, %s)", name, value);
        this.extenders.add(new HeaderExtender(name, value));
        return this;
    }

    /**
     * Set body as a string.
     * @param text The body to use for requests
     * @return This object
     */
    public TestClient body(final String text) {
        Logger.debug(this, "#body(%s)", text);
        this.extenders.add(new BodyExtender(text));
        return this;
    }

    /**
     * Execute GET request.
     * @param path The URL
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient get(final String path) throws Exception {
        final long start = System.nanoTime();
        this.response = this.execute(new HttpGet(this.uri(path)));
        Logger.info(
            this,
            "#get(%s): completed in %.3fms",
            path,
            // @checkstyle MagicNumber (1 line)
            (double) (System.nanoTime() - start) / (1000L * 1000)
        );
        return this;
    }

    /**
     * Execute POST request.
     * @param path The URL
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient post(final String path) throws Exception {
        final long start = System.nanoTime();
        this.response = this.execute(new HttpPost(this.uri(path)));
        Logger.info(
            this,
            "#post(%s): completed in %.3fms",
            path,
            // @checkstyle MagicNumber (1 line)
            (double) (System.nanoTime() - start) / (1000L * 1000)
        );
        return this;
    }

    /**
     * Execute PUT request.
     * @param path The URL
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient put(final String path) throws Exception {
        final long start = System.nanoTime();
        this.response = this.execute(new HttpPut(this.uri(path)));
        Logger.info(
            this,
            "#put(%s): completed in %.3fms",
            path,
            // @checkstyle MagicNumber (1 line)
            (double) (System.nanoTime() - start) / (1000L * 1000)
        );
        return this;
    }

    /**
     * Get body as a string.
     * @return The body
     * @throws java.io.IOException If some IO problem inside
     */
    public String getBody() throws java.io.IOException {
        if (this.body == null) {
            final HttpEntity entity = this.response.getEntity();
            if (entity != null) {
                this.body = IOUtils.toString(entity.getContent());
            }
        }
        return this.body;
    }

    /**
     * Get status of the response as a number.
     * @return The status
     */
    public Integer getStatus() {
        return this.response.getStatusLine().getStatusCode();
    }

    /**
     * Get status line of the response.
     * @return The status line
     */
    public String getStatusLine() {
        return String.format(
            "%s %s %s",
            this.response.getStatusLine().getProtocolVersion(),
            this.response.getStatusLine().getReasonPhrase(),
            this.response.getStatusLine().getStatusCode()
        );
    }

    /**
     * Get a collection of all headers.
     * @return The headers
     */
    public Headers getHeaders() {
        return new Headers(this.response.getAllHeaders());
    }

    /**
     * Build URI to request.
     * @param path The path to request
     * @return The URI
     * @throws java.net.URISyntaxException If there is some problem
     */
    private URI uri(final String path) throws java.net.URISyntaxException {
        return new URI(
            String.format(
                "%s://%s:%d%s",
                this.home.getScheme(),
                this.home.getHost(),
                this.home.getPort(),
                path
            )
        );
    }

    /**
     * Execute request and return response.
     * @param req The request
     * @return The response
     * @throws java.io.IOException If some IO problem
     */
    private HttpResponse execute(final HttpRequest req)
        throws java.io.IOException {
        this.body = null;
        for (Extender extender : this.extenders) {
            extender.extend(req);
        }
        final HttpClient client = new DefaultHttpClient();
        return client.execute((HttpUriRequest) req);
    }

}
