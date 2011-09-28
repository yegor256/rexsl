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
import java.net.ServerSocket;
import java.net.URI;
import org.apache.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
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
            .get("/");
        MatcherAssert.assertThat(
            client.getBody(),
            Matchers.containsString("works")
        );
        MatcherAssert.assertThat(
            client.getStatus(),
            Matchers.equalTo(HttpStatus.SC_OK)
        );
    }

}
