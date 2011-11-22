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

import com.rexsl.test.client.Headers;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import java.net.ServerSocket;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import org.apache.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test HTTP client.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class TestClientTest {

    /**
     * Root path.
     */
    private static final String ROOT = "/";

    /**
     * Test client body.
     */
    private static final String TEST_BODY =
        "<?xml version=\"1.0\"?><root>test</root>";

    /**
     * Error message.
     */
    private static final String ERROR_MESSAGE = "Error expected.";

    /**
     * Port to work with.
     */
    private static Integer port;

    /**
     * Grizzly container, used for tests.
     */
    private GrizzlyWebServer gws;

    /**
     * Where this Grizzly container is hosted.
     */
    private URI home;

    /**
     * Start Servlet container.
     * @throws Exception If something goes wrong inside
     */
    @BeforeClass
    public static void reservePort() throws Exception {
        final ServerSocket socket = new ServerSocket(0);
        TestClientTest.port = socket.getLocalPort();
        socket.close();
    }

    /**
     * Start Servlet container.
     * @throws Exception If something goes wrong inside
     */
    @Before
    public void startContainer() throws Exception {
        this.home = new URI(String.format("http://localhost:%d/", this.port));
        this.gws = new GrizzlyWebServer(this.port);
        this.gws.addGrizzlyAdapter(
            new GrizzlyAdapter() {
                public void service(final GrizzlyRequest request,
                    final GrizzlyResponse response) {
                    try {
                        response.addHeader("Content-type", "text/plain");
                        response.addHeader("Content-length", "12");
                        response.getWriter().println("works fine!");
                    } catch (java.io.IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        );
        this.gws.start();
    }

    /**
     * Stop Servlet container.
     * @throws Exception If something goes wrong inside
     */
    @After
    public void stopContainer() throws Exception {
        this.gws.stop();
    }

    /**
     * Test simple HTTP interaction.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testHTTPRequests() throws Exception {
        final TestClient client = new TestClient(this.home)
            .header("Accept", "application/json")
            .header("User-agent", "Some Text")
            .get(this.ROOT);
        MatcherAssert.assertThat(
            client.getBody(),
            Matchers.containsString("works")
        );
        MatcherAssert.assertThat(
            client.getBody(),
            Matchers.containsString("fine")
        );
        MatcherAssert.assertThat(
            client.getStatus(),
            Matchers.equalTo(HttpStatus.SC_OK)
        );
        MatcherAssert.assertThat(
            client.getHeaders().get("content-type"),
            Matchers.containsString("text")
        );
        MatcherAssert.assertThat(
            client.getHeaders().has("content-length"),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(
            client.getHeaders().all("Content-Type").size(),
            Matchers.equalTo(1)
        );
    }

    /**
     * Test POST HTTP request with body.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testPostRequestWithBody() throws Exception {
        final TestClient client = new TestClient(this.home)
            .body("hello")
            .post("/test");
        MatcherAssert.assertThat(
            client.getBody(),
            Matchers.containsString("works fine")
        );
    }

    /**
     * Tests setting new cookie.
     * @throws Exception If something goes wrong inside.
     * @todo #68 Implement TestClient.cookie();
     */
    @org.junit.Ignore
    @Test
    public void testCookie() throws Exception {
        final TestClient client = new TestClient(this.home);
        final NewCookie newCookie = new NewCookie("a", "c");
        client.cookie(newCookie);
        final Headers headers = client.getHeaders();
        Assert.assertTrue(headers.has(HttpHeaders.SET_COOKIE));
        final String value = headers.get(HttpHeaders.SET_COOKIE);
        Assert.assertEquals("a=c", value);
    }

    /**
     * Tests expectedStatus method.
     * @throws Exception If something goes wrong inside.
     * @todo #75 Implement TestClient.expectedStatus().
     */
    @org.junit.Ignore
    @Test
    public void testTrueExpectedStatus() throws Exception {
        TestClient client = new TestClient(this.home);
        client = client.get(this.ROOT);
        Assert.assertNotNull(client.expectedStatus(HttpStatus.SC_OK));
    }

    /**
     * Tests expectedStatus method.
     * @throws Exception If something goes wrong inside.
     * @todo #75 Implement TestClient.expectedStatus().
     */
    @org.junit.Ignore
    @Test(expected = AssertionError.class)
    public void testFalseExpectedStatus() throws Exception {
        TestClient client = new TestClient(this.home);
        client = client.get(this.ROOT);
    }

    /**
     * Tests expectedXPath method.
     * @todo #75 Implement TestClient.expectedXPath().
     * @throws Exception If something goes wrong inside.
     */
    @org.junit.Ignore
    @Test
    public void testTrueExpectedXPath() throws Exception {
        final TestClient client = new TestClient(this.home);
        client.body(this.TEST_BODY);
        Assert.assertNotNull(client.expectedXPath("/root[.='test']"));
    }

    /**
     * Tests expectedXPath method.
     * @todo #75 Implement TestClient.expectedXPath().
     * @throws Exception If something goes wrong inside.
     */
    @org.junit.Ignore
    @Test(expected = AssertionError.class)
    public void testFalseExpectedXPath() throws Exception {
        final TestClient client = new TestClient(this.home);
        client.body(this.TEST_BODY);
    }
}
