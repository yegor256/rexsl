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

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link JerseyTestClient}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class JerseyTestClientTest {

    /**
     * TestClient can send GET request twice.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsIdenticalHttpRequestTwice() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML)
            .mock();
        final TestClient client = RestTester
            .start(UriBuilder.fromUri(container.home()).path("/foo"))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML);
        client.get("first request")
            .assertStatus(HttpURLConnection.HTTP_OK);
        client.post("second try, should work exactly like the first one", "")
            .assertStatus(HttpURLConnection.HTTP_OK);
        client.get("third request")
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * TestClient can recover after first assertion error.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void recoversAfterFirstAssertionError() throws Exception {
        final ContainerMocker container = new ContainerMocker().returnBody(
            new Callable<byte[]>() {
                private transient int retry;
                @Override
                public byte[] call() {
                    String output;
                    if (this.retry <= 2) {
                        output = "<failure />";
                    } else {
                        output = "<success />";
                    }
                    ++this.retry;
                    return output.getBytes();
                }
            }
        ).mock();
        RestTester.start(container.home())
            .get("download content in two attempts")
            .assertThat(
                new AssertionPolicy() {
                    @Override
                    public void assertThat(final TestResponse response) {
                        MatcherAssert.assertThat(
                            response.getBody(),
                            XhtmlMatchers.hasXPath("//success")
                        );
                    }
                    @Override
                    public boolean isRetryNeeded(final int attempt) {
                        return true;
                    }
                }
            )
            .assertXPath("/success");
    }

}
