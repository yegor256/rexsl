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

import com.jcabi.log.Logger;
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
 * Test case for {@link RestTester}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class RestTesterTest {

    /**
     * RestTester can fetch HTTP request and process HTTP response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsHttpRequestAndProcessesHttpResponse() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectRequestUri(Matchers.containsString("foo"))
            .expectMethod(Matchers.equalTo(RestTester.GET))
            .returnBody("hello!!".getBytes(CharEncoding.UTF_8))
            .mock();
        RestTester
            .start(UriBuilder.fromUri(container.home()).path("/foo"))
            .get("test of HTTP request and response")
            .assertBody(Matchers.containsString("!!"))
            .assertBody(Matchers.containsString("hello!"))
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can fetch HTTP headers.
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
            .expectMethod(Matchers.equalTo(RestTester.GET))
            .mock();
        RestTester
            .start(container.home())
            .header(HttpHeaders.ACCEPT, "*/*")
            .get("test of HTTP simple request")
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can fetch GET request with query params.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsTextWithGetParameters() throws Exception {
        final String name = "qparam";
        final String value = "some value of this param &^%*;'\"";
        final ContainerMocker container = new ContainerMocker()
            .expectParam(name, Matchers.equalTo(value))
            .expectMethod(Matchers.equalTo(RestTester.GET))
            .expectHeader(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
            .mock();
        RestTester
            .start(UriBuilder.fromUri(container.home()).queryParam(name, value))
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
            .get("test of GET params")
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can fetch body with HTTP POST request.
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
            .expectMethod(Matchers.equalTo(RestTester.POST))
            .mock();
        RestTester
            .start(container.home())
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .post(
                "testing of POST request",
                Logger.format(
                    "%s=%s",
                    name,
                    URLEncoder.encode(value, CharEncoding.UTF_8)
                )
            )
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can fetch body with HTTP POST request.
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
            .expectMethod(Matchers.equalTo(RestTester.POST))
            .mock();
        RestTester
            .start(container.home())
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .post(
                "testing of POST request body",
                URLEncoder.encode(value, CharEncoding.UTF_8)
            )
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can assert HTTP status code value.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpStatus() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(RestTester.GET))
            .returnStatus(HttpURLConnection.HTTP_NOT_FOUND)
            .mock();
        RestTester
            .start(container.home())
            .get("asserts HTTP status")
            .assertStatus(HttpURLConnection.HTTP_NOT_FOUND)
            .assertStatus(
                Matchers.equalTo(HttpURLConnection.HTTP_NOT_FOUND)
            );
    }

    /**
     * RestTester can assert response body.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpResponseBody() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(RestTester.GET))
            .returnBody("some text")
            .mock();
        RestTester
            .start(container.home())
            .get("asserts HTTP response body")
            .assertBody(Matchers.containsString("some"))
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can assert HTTP headers in response.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpHeaders() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(RestTester.GET))
            .returnHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
            .mock();
        RestTester
            .start(container.home())
            .get("asserts HTTP headers")
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
     * RestTester can assert response body content with XPath query.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsResponseBodyWithXpathQuery() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(RestTester.GET))
            .returnBody("<root><a>\u0443\u0440\u0430!</a></root>")
            .mock();
        RestTester
            .start(container.home())
            .get("asserts response body with XPath")
            .assertXPath("/root/a[contains(.,'!')]")
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can work with URL returned by ContainerMocker.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void mockedUrlIsInCorrectFormat() throws Exception {
        final URI uri = new ContainerMocker().mock().home();
        final String regex = "^http://localhost:\\d+/$";
        MatcherAssert.assertThat(
            uri.toString().matches(regex),
            Matchers.describedAs(uri.toString(), Matchers.is(true))
        );
    }

    /**
     * RestTester can handle unicode in plain text response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void acceptsUnicodeInPlainText() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .returnHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8")
            .returnBody("\u0443\u0440\u0430!")
            .mock();
        RestTester
            .start(UriBuilder.fromUri(container.home()).path("/abcde"))
            .get("unicode in plain text")
            .assertBody(Matchers.containsString("\u0443\u0440\u0430"))
            .assertBody(Matchers.containsString("!"));
    }

    /**
     * RestTester can handle unicode in XML response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void acceptsUnicodeInXml() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .returnHeader(HttpHeaders.CONTENT_TYPE, "text/xml;charset=utf-8")
            .returnBody("<text>\u0443\u0440\u0430!</text>")
            .mock();
        RestTester
            .start(UriBuilder.fromUri(container.home()).path("/barbar"))
            .get("unicode conversion")
            .assertXPath("/text[contains(.,'\u0443\u0440\u0430')]");
    }

    /**
     * RestTester can use basic authentication scheme.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsBasicAuthenticationHeader() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectHeader(
                HttpHeaders.AUTHORIZATION,
                Matchers.equalTo("Basic dXNlcjpwd2Q=")
        ).mock();
        RestTester
            .start(UriBuilder.fromUri(container.home()).userInfo("user:pwd"))
            .get("test with Basic authorization")
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can silently proceed on connection error.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = AssertionError.class)
    public void continuesOnConnectionError() throws Exception {
        RestTester
            .start(UriBuilder.fromUri("http://absent-1.rexsl.com/"))
            .get("GET from non-existing host")
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can retry on connection error.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void retriesOnConnectionError() throws Exception {
        RestTester
            .start(UriBuilder.fromUri("http://absent-2.rexsl.com/"))
            .get("GET from non-existing host, with attempt to retry")
            .assertThat(
                new AssertionPolicy() {
                    @Override
                    public void assertThat(final TestResponse response) {
                        try {
                            MatcherAssert.assertThat(
                                response.getStatus(),
                                Matchers.equalTo(HttpURLConnection.HTTP_OK)
                            );
                            throw new IllegalStateException();
                        } catch (AssertionError ex) {
                            assert ex != null;
                        }
                    }
                    @Override
                    public boolean isRetryNeeded(final int attempt) {
                        return false;
                    }
                }
            );
    }

}
