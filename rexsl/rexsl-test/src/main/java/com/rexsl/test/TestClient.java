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
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

/**
 * A universal class for in-container testing of your web application.
 *
 * <p>For example (in your Groovy script):
 *
 * <pre>
 * import java.net.HttpURLConnection
 * import javax.ws.rs.core.HttpHeaders
 * import javax.ws.rs.core.MediaType
 * import org.xmlmatchers.XmlMatchers
 * import org.hamcrest.Matchers
 * new TestClient(rexsl.home)
 *   .header(HttpHeaders.USER_AGENT, 'Safari 4')
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
 *   .body('name=John Doe')
 *   .post(UriBuilder.fromUri('/{id}').build(number))
 *   .assertStatus(HttpURLConnection.HTTP_OK)
 *   .assertBody(XmlMatchers.hasXPath('/data/user[.="John Doe"]'))
 *   .assertBody(Matchers.containsString('xml'))
 * </pre>
 *
 * <p>This example will make a <tt>POST</tt> request to the URI pre-built
 * by <tt>UriBuilder</tt>, providing headers and request body. Response will
 * be validated with matchers. You got it.
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
     * HTTP request params.
     */
    private HttpParams params = new BasicHttpParams();

    /**
     * HTTP response.
     */
    private HttpResponse response;

    /**
     * Returned body.
     */
    private String rbody;

    /**
     * Public ctor.
     * @param uri Home of the server
     */
    public TestClient(final URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException(
                String.format(
                    "URI '%s' is not absolute, can't be used with %s",
                    uri,
                    this.getClass().getName()
                )
            );
        }
        this.home = uri;
        this.response = null;
        this.followRedirects(false);
    }

    /**
     * Public ctor.
     * @param uri Home of the server
     */
    public TestClient(final String uri) {
        this(UriBuilder.fromUri(uri).build());
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
     * Follow redirects.
     * @param follow Should we follow redirects?
     * @return This object
     */
    public TestClient followRedirects(final boolean follow) {
        this.params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, follow);
        return this;
    }

    /**
     * Execute GET request.
     * @param path The relative path on the server
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient get(final URI path) throws Exception {
        return this.method(path, HttpGet.class);
    }

    /**
     * Execute GET request.
     * @param path The relative path on the server
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient get(final String path) throws Exception {
        return this.get(UriBuilder.fromPath(path).build());
    }

    /**
     * Execute GET request.
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient get() throws Exception {
        return this.get("");
    }

    /**
     * Execute POST request.
     * @param path The path on the server
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient post(final URI path) throws Exception {
        return this.method(path, HttpPost.class);
    }

    /**
     * Execute POST request.
     * @param path The path on the server
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient post(final String path) throws Exception {
        return this.post(UriBuilder.fromPath(path).build());
    }

    /**
     * Execute POST request.
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient post() throws Exception {
        return this.post("");
    }

    /**
     * Execute PUT request.
     * @param path The path on the server
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient put(final URI path) throws Exception {
        return this.method(path, HttpPut.class);
    }

    /**
     * Execute PUT request.
     * @param path The path on the server
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient put(final String path) throws Exception {
        return this.put(UriBuilder.fromPath(path).build());
    }

    /**
     * Execute PUT request.
     * @return This object
     * @throws Exception If something goes wrong
     */
    public TestClient put() throws Exception {
        return this.put("");
    }

    /**
     * Get body as a string.
     * @return The body
     * @throws IOException If some IO problem inside
     */
    public String getBody() throws IOException {
        if (this.rbody == null) {
            final HttpEntity entity = this.response.getEntity();
            if (entity != null) {
                this.rbody = IOUtils.toString(entity.getContent());
            }
        }
        return this.rbody;
    }

    /**
     * Get status of the response as a number.
     * @return The status
     */
    public Integer getStatus() {
        return this.response.getStatusLine().getStatusCode();
    }

    /**
     * Get body as {@link GPathResult}.
     * @return The GPath result
     * @throws Exception If some problem inside
     */
    public GPathResult getGpath() throws Exception {
        return new XmlSlurper().parseText(this.getBody());
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
     * Sets new cookie.
     * @param cookie New cookie to be set.
     * @return This object.
     */
    public TestClient cookie(final NewCookie cookie) {
        this.header(HttpHeaders.SET_COOKIE, cookie.toString());
        return this;
    }

    /**
     * Verifies HTTP response status code against the provided absolute value,
     * and throws {@link AssertionError} in case of mismatch.
     * @param status Expected status code
     * @return This object
     */
    public TestClient assertStatus(final int status) {
        Assert.assertEquals(new Long(status), new Long(this.getStatus()));
        return this;
    }

    /**
     * Verifies HTTP response status code against the provided matcher.
     * @param matcher Matcher to validate status code
     * @return This object
     */
    public TestClient assertStatus(final Matcher<Integer> matcher) {
        Assert.assertThat(this.getStatus(), matcher);
        return this;
    }

    /**
     * Verifies HTTP response body content against provided matcher.
     * @param matcher The matcher to use
     * @return This object
     * @throws IOException If some problem with body retrieval
     */
    public TestClient assertBody(final Matcher<String> matcher)
        throws IOException {
        Assert.assertThat(this.getBody(), matcher);
        return this;
    }

    /**
     * Find link in XML and return new client with this link as URI.
     * @param xpath The path of the link
     * @return New client
     * @throws Exception If some problem inside
     */
    public TestClient rel(final String xpath) throws Exception {
        final Document document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(new ByteArrayInputStream(this.getBody().getBytes()));
        final NodeList nodes = (NodeList) XPathFactory.newInstance()
            .newXPath()
            .evaluate(xpath, document, XPathConstants.NODESET);
        if (nodes.getLength() != 1) {
            throw new AssertionError(
                String.format(
                    "XPath '%s' not found in document at '%s'",
                    xpath,
                    this.home
                )
            );
        }
        final URI uri = UriBuilder
            .fromUri(nodes.item(0).getNodeValue())
            .build();
        return new TestClient(uri);
    }

    /**
     * Verifies HTTP response body XHTML/XML content against XPath query.
     * @param xpath Query to use
     * @return This object
     * @throws Exception If some problem with body retrieval or conversion
     */
    public TestClient assertXPath(final String xpath) throws Exception {
        final SimpleNamespaceContext context = new SimpleNamespaceContext()
            .withBinding("xhtml", "http://www.w3.org/1999/xhtml")
            .withBinding("xs", "http://www.w3.org/2001/XMLSchema")
            .withBinding("xsl", "http://www.w3.org/1999/XSL/Transform");
        Assert.assertThat(
            XhtmlConverter.the(this.getBody()),
            XmlMatchers.hasXPath(xpath, context)
        );
        return this;
    }

    /**
     * Execute one request.
     * @param path The path
     * @param type Type of request
     * @return This object
     * @throws Exception If something goes wrong
     */
    private TestClient method(final URI path,
        final Class<? extends HttpUriRequest> type) throws Exception {
        final long start = System.currentTimeMillis();
        final URI absolute = this.absolute(path);
        final HttpUriRequest request = type
            .getConstructor(URI.class)
            .newInstance(absolute);
        this.response = this.execute(request);
        Logger.info(
            this,
            "#%s(%s): completed in %dms [%s]: %s",
            request.getMethod(),
            path,
            System.currentTimeMillis() - start,
            this.response.getStatusLine(),
            absolute
        );
        return this;
    }

    /**
     * Execute request and return response.
     * @param req The request
     * @return The response
     * @throws IOException If some IO problem
     */
    private HttpResponse execute(final HttpRequest req) throws IOException {
        synchronized (this) {
            this.rbody = null;
            for (Extender extender : this.extenders) {
                extender.extend(req);
            }
            final HttpClient client = new DefaultHttpClient(this.params);
            return client.execute((HttpUriRequest) req);
        }
    }

    /**
     * Build absolute URI from <tt>this.home</tt> and provided <tt>path</tt>.
     * @param path Relative path
     * @return Absolute URI
     * @throws Exception If some problem inside
     */
    private URI absolute(final URI path) throws Exception {
        return new URL(this.home.toURL(), path.toString()).toURI();
    }

}
