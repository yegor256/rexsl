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

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link TestClient}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class TestClientTest {

    /**
     * TestClient can send HTTP request and process HTTP response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsHttpRequestAndProcessesHttpResponse() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectRequestUri(Matchers.containsString("path"))
            .returnBody("works fine")
            .mock();
        final TestClient client = new TestClient(container.home())
            .get("/the-path")
            .assertBody(Matchers.containsString("fine"))
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    // /**
    //  * TestClient can send HTTP request and process HTTP response.
    //  * @throws Exception If something goes wrong inside
    //  */
    // @Test
    // public void sendsHttpRequestAndProcessesHttpResponse() throws Exception {
    //     final ContainerMocker container = new ContainerMocker()
    //         .
    //         .mock();
    //     final TestClient client = new TestClient(container.home())
    //         .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
    //         .header(HttpHeaders.USER_AGENT, "Some Text")
    //         .get("/the-path")
    //         .assertBody(Matchers.containsString("fine"))
    //         .assertStatus(HttpURLConnection.HTTP_OK);
    //     MatcherAssert.assertThat(
    //         client.getHeaders().get(HttpHeaders.CONTENT_TYPE),
    //         Matchers.containsString(MediaType.TEXT_PLAIN)
    //     );
    //     MatcherAssert.assertThat(
    //         client.getHeaders().has(HttpHeaders.CONTENT_LENGTH),
    //         Matchers.equalTo(true)
    //     );
    //     MatcherAssert.assertThat(
    //         client.getHeaders().all(HttpHeaders.CONTENT_TYPE).size(),
    //         Matchers.equalTo(1)
    //     );
    // }
    //
    // /**
    //  * Test POST HTTP request with body.
    //  * @throws Exception If something goes wrong inside
    //  */
    // @Test
    // public void testPostRequestWithBody() throws Exception {
    //     new TestClient(TestClientTest.home)
    //         .body("hello")
    //         .post("/test")
    //         .assertBody(Matchers.containsString("works"));
    // }
    //
    // /**
    //  * Tests setting new cookie.
    //  * @throws Exception If something goes wrong inside.
    //  */
    // @Test
    // public void testCookie() throws Exception {
    //     new TestClient(TestClientTest.home)
    //         .cookie(new NewCookie("a", "c"));
    // }
    //
    // /**
    //  * Test <tt>assertStatus()</tt> methods.
    //  * @throws Exception If something goes wrong inside.
    //  */
    // @Test
    // public void testStatusAssertions() throws Exception {
    //     new TestClient(TestClientTest.home)
    //         .get("/some-path")
    //         .assertStatus(HttpURLConnection.HTTP_OK)
    //         .assertStatus(Matchers.equalTo(HttpURLConnection.HTTP_OK));
    // }
    //
    // /**
    //  * Test <tt>assertBody()</tt> method.
    //  * @throws Exception If something goes wrong inside.
    //  */
    // @Test
    // public void testBodyAssertions() throws Exception {
    //     new TestClient(TestClientTest.home)
    //         .get("/some-other-path")
    //         .assertBody(Matchers.containsString("<a>"));
    // }
    //
    // /**
    //  * Test <tt>assertXPath()</tt> method.
    //  * @throws Exception If something goes wrong inside.
    //  */
    // @Test
    // public void testXPathAssertions() throws Exception {
    //     new TestClient(TestClientTest.home)
    //         .get("/some-xml-path")
    //         .assertXPath("/a[contains(.,'works')]");
    // }
    //
    // /**
    //  * Test how URI is constructed.
    //  * @throws Exception If something goes wrong inside
    //  */
    // @Test
    // public void testURIConstructingMechanism() throws Exception {
    //     final URI base = UriBuilder.fromUri(TestClientTest.home)
    //         .queryParam("some-param", "some-value")
    //         .path("/some/extra/path")
    //         .build();
    //     final TestClient client = new TestClient(base).get(
    //         UriBuilder.fromPath("suffix").queryParam("alpha", "554").build()
    //     );
    // }

}
