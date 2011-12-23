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

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link RestTester}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class RestTesterTest {

    /**
     * RestTester can send HTTP request and process HTTP response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsHttpRequestAndProcessesHttpResponse() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectRequestUri(CoreMatchers.containsString("foo"))
            .expectMethod(CoreMatchers.equalTo(RestTester.GET))
            .returnBody("hello!!")
            .mock();
        RestTester
            .start(UriBuilder.fromUri(container.home()).path("/foo"))
            .get()
            // .assertBody(CoreMatchers.containsString("\u0443\u0440\u0430"))
            .assertBody(CoreMatchers.containsString("!!"))
            .assertBody(CoreMatchers.containsString("hello!"))
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can send HTTP headers.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsHttpRequestWithHeaders() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectHeader(HttpHeaders.ACCEPT, CoreMatchers.containsString("*"))
            .expectMethod(CoreMatchers.equalTo(RestTester.GET))
            .mock();
        RestTester
            .start(container.home())
            .header(HttpHeaders.ACCEPT, "*/*")
            .get()
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can send GET request with query params.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsTextWithGetParameters() throws Exception {
        final String name = "qparam";
        final String value = "some value of this param &^%*;'\"";
        final ContainerMocker container = new ContainerMocker()
            .expectParam(name, CoreMatchers.equalTo(value))
            .expectMethod(CoreMatchers.equalTo(RestTester.GET))
            .mock();
        RestTester
            .start(UriBuilder.fromUri(container.home()).queryParam(name, value))
            .get()
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can send body with HTTP POST request.
     * @throws Exception If something goes wrong inside
     * @todo #85 This test doesn't work for some reason, and I can't understand
     *  what exactly is the problem. Grizzly container doesn't understand
     *  POST params passed in HTTP request body. While Jersey understands them
     *  perfectly in rexsl-maven-plugin invoker tests. Maybe getParameters()
     *  method in GrizzlyHttpRequest doesn't work properly with POST params?
     */
    @Test
    @org.junit.Ignore
    public void sendsTextWithPostRequest() throws Exception {
        final String name = "postparam";
        final String value = "some random value of this param \"&^%*;'\"";
        final ContainerMocker container = new ContainerMocker()
            .expectBody(CoreMatchers.containsString(name))
            .expectBody(CoreMatchers.containsString(value))
            .expectParam(name, CoreMatchers.equalTo(value))
            .expectMethod(CoreMatchers.equalTo(RestTester.POST))
            .mock();
        RestTester
            .start(container.home())
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .post(String.format("%s=%s", name, URLEncoder.encode(value)))
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can assert HTTP status code value.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpStatus() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(CoreMatchers.equalTo(RestTester.GET))
            .returnStatus(HttpURLConnection.HTTP_NOT_FOUND)
            .mock();
        RestTester
            .start(container.home())
            .get()
            .assertStatus(HttpURLConnection.HTTP_NOT_FOUND)
            .assertStatus(
                CoreMatchers.equalTo(HttpURLConnection.HTTP_NOT_FOUND)
            );
    }

    /**
     * RestTester can assert response body.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpResponseBody() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(CoreMatchers.equalTo(RestTester.GET))
            .returnBody("some text")
            .mock();
        RestTester
            .start(container.home())
            .get()
            .assertBody(CoreMatchers.containsString("some"))
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestTester can assert HTTP headers in response.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpHeaders() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(CoreMatchers.equalTo(RestTester.GET))
            .returnHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
            .mock();
        RestTester
            .start(container.home())
            .get()
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertHeader(
                HttpHeaders.CONTENT_TYPE,
                CoreMatchers.containsString(MediaType.TEXT_PLAIN)
            );
    }

    /**
     * RestTester can assert response body content with XPath query.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsResponseBodyWithXpathQuery() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(CoreMatchers.equalTo(RestTester.GET))
            .returnBody("<root><a>\u0443\u0440\u0430!</a></root>")
            .mock();
        RestTester
            .start(container.home())
            .get()
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
            CoreMatchers.describedAs(uri.toString(), CoreMatchers.is(true))
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
            .get()
            .assertBody(CoreMatchers.containsString("\u0443\u0440\u0430"))
            .assertBody(CoreMatchers.containsString("!"));
    }

    /**
     * RestTester can handle unicode in XML response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    @org.junit.Ignore
    public void acceptsUnicodeInXml() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .returnHeader(HttpHeaders.CONTENT_TYPE, "text/xml;charset=utf-8")
            .returnBody("<text>\u0443\u0440\u0430!</text>")
            .mock();
        RestTester
            .start(UriBuilder.fromUri(container.home()).path("/barbar"))
            .get()
            .assertXPath("/text[contains(.,'\u0443\u0440\u0430')]");
    }

}
