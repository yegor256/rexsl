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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link BaseRequest} and its children.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class BaseRequestTest {

    /**
     * Make a request.
     * @param uri URI to start with
     * @return Request
     */
    private Request request(final URI uri) {
        return new JdkRequest(uri);
    }

    /**
     * ApacheRequest can fetch HTTP request and process HTTP response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsHttpRequestAndProcessesHttpResponse() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectRequestUri(Matchers.containsString("helloall"))
            .expectMethod(Matchers.equalTo(Request.GET))
            .returnBody("hello!!".getBytes(CharEncoding.UTF_8))
            .mock();
        this.request(container.home())
            .uri().path("/helloall").back()
            .method(Request.GET)
            .fetch().as(RestResponse.class)
            .assertBody(Matchers.containsString("!!"))
            .assertBody(Matchers.containsString("hello!"))
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * ApacheRequest can fetch HTTP headers.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsHttpRequestWithHeaders() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectHeader(HttpHeaders.ACCEPT, Matchers.containsString("*"))
            .expectHeader(
                HttpHeaders.USER_AGENT,
                Matchers.containsString("ReXSL")
            )
            .expectMethod(Matchers.equalTo(Request.GET))
            .mock();
        this.request(container.home())
            .uri().path("/foo1").back()
            .method(Request.GET)
            .header(HttpHeaders.ACCEPT, "*/*")
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * ApacheRequest can fetch GET request with query params.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsTextWithGetParameters() throws Exception {
        final String name = "qparam";
        final String value = "some value of this param &^%*;'\"";
        final ContainerMocker container = new ContainerMocker()
            .expectParam(name, Matchers.equalTo(value))
            .expectMethod(Matchers.equalTo(Request.GET))
            .expectHeader(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
            .mock();
        this.request(container.home())
            .uri().queryParam(name, value).back()
            .method(Request.GET)
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * ApacheRequest can fetch body with HTTP POST request.
     * @throws Exception If something goes wrong inside
     * @todo #151 Is it possible to initialize input variable with
     *  InputStream from request without side effects on request?
     *  request's parameters retrieve is done lazily and requires
     *  InputStream rewinded to it's start
     */
    @Test
    @org.junit.Ignore
    public void sendsTextWithPostRequestMatchParam() throws Exception {
        final String name = "postparam";
        final String value = "some random value of this param \"&^%*;'\"";
        final ContainerMocker container = new ContainerMocker()
            .expectParam(name, Matchers.equalTo(value))
            .expectMethod(Matchers.equalTo(Request.POST))
            .mock();
        this.request(container.home())
            .method(Request.POST)
            .body().formParam(name, value).back()
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * ApacheRequest can fetch body with HTTP POST request.
     * @throws Exception If something goes wrong inside
     * @todo #151 Is it possible to initialize input variable with
     *  InputStream from request without side effects on request?
     *  request's parameters retrieve is done lazily and requires
     *  InputStream rewinded to it's start
     */
    @Test
    @org.junit.Ignore
    public void sendsTextWithPostRequestMatchBody() throws Exception {
        final String value = "some body value with \"&^%*;'\"";
        final ContainerMocker container = new ContainerMocker()
            .expectBody(Matchers.containsString("with"))
            .expectMethod(Matchers.equalTo(Request.POST))
            .mock();
        this.request(container.home())
            .method(Request.POST)
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .body().set(URLEncoder.encode(value, CharEncoding.UTF_8)).back()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * ApacheRequest can assert HTTP status code value.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpStatus() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(Request.GET))
            .returnStatus(HttpURLConnection.HTTP_NOT_FOUND)
            .mock();
        this.request(container.home())
            .method(Request.GET)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_NOT_FOUND)
            .assertStatus(
                Matchers.equalTo(HttpURLConnection.HTTP_NOT_FOUND)
            );
    }

    /**
     * ApacheRequest can assert response body.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpResponseBody() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(Request.GET))
            .returnBody("some text")
            .mock();
        this.request(container.home())
            .method(Request.GET)
            .fetch().as(RestResponse.class)
            .assertBody(Matchers.containsString("some"))
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * ApacheRequest can assert HTTP headers in response.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpHeaders() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(Request.GET))
            .returnHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
            .mock();
        this.request(container.home())
            .method(Request.GET)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertHeader(
                "absent-for-sure",
                Matchers.emptyIterableOf(String.class)
            )
            .assertHeader(
                HttpHeaders.CONTENT_TYPE,
                Matchers.<String>everyItem(
                    Matchers.containsString(MediaType.TEXT_PLAIN)
                )
            );
    }

    /**
     * ApacheRequest can assert response body content with XPath query.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsResponseBodyWithXpathQuery() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(Request.GET))
            .returnBody("<root><a>\u0443\u0440\u0430!</a></root>")
            .mock();
        this.request(container.home())
            .method(Request.GET)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .assertXPath("/root/a[contains(.,'!')]");
    }

    /**
     * ApacheRequest can work with URL returned by ContainerMocker.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void mockedUrlIsInCorrectFormat() throws Exception {
        final URI uri = new ContainerMocker().mock().home();
        MatcherAssert.assertThat(
            uri.toString().matches("^http://localhost:\\d+/$"),
            Matchers.describedAs(uri.toString(), Matchers.is(true))
        );
    }

    /**
     * ApacheRequest can handle unicode in plain text response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void acceptsUnicodeInPlainText() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .returnHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8")
            .returnBody("\u0443\u0440\u0430!")
            .mock();
        this.request(container.home())
            .method(Request.GET)
            .uri().path("/abcdefff").back()
            .fetch().as(RestResponse.class)
            .assertBody(Matchers.containsString("\u0443\u0440\u0430"))
            .assertBody(Matchers.containsString("!"));
    }

    /**
     * ApacheRequest can handle unicode in XML response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void acceptsUnicodeInXml() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .returnHeader(HttpHeaders.CONTENT_TYPE, "text/xml;charset=utf-8")
            .returnBody("<text>\u0443\u0440\u0430!</text>")
            .mock();
        this.request(container.home())
            .method(Request.GET)
            .uri().path("/barbar").back()
            .fetch().as(XmlResponse.class)
            .assertXPath("/text[contains(.,'\u0443\u0440\u0430')]");
    }

    /**
     * ApacheRequest can use basic authentication scheme.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsBasicAuthenticationHeader() throws Exception {
        final ContainerMocker container = new ContainerMocker().expectHeader(
            HttpHeaders.AUTHORIZATION,
            Matchers.equalTo("Basic dXNlcjpwd2Q=")
        ).mock();
        final URI uri = UriBuilder.fromUri(container.home())
            .userInfo("user:pwd").build();
        this.request(uri)
            .method(Request.GET)
            .uri().path("/abcde").back()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * ApacheRequest can throw a correct exception on connection error.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = IOException.class)
    public void continuesOnConnectionError() throws Exception {
        this.request(new URI("http://absent-1.rexsl.com/"))
            .method(Request.GET)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * ApacheRequest can fetch GET request twice.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsIdenticalHttpRequestTwice() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML)
            .mock();
        final Request req = this.request(container.home())
            .uri().path("/foo").back()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML);
        req.method(Request.GET).fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        req.method(Request.POST).fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        req.method(Request.GET).fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

}
