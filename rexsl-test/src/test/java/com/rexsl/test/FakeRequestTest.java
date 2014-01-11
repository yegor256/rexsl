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
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link FakeRequest}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class FakeRequestTest {

    /**
     * FakeRequest can fetch a fake response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    @org.junit.Ignore
    public void sendsHttpRequestAndProcessesHttpResponse() throws Exception {
        new FakeRequest()
            .withStatus(HttpURLConnection.HTTP_OK)
            .withReason("OK")
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
            .withBody("how are you?")
            .uri().path("/helloall").back()
            .method(Request.POST)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
            .assertBody(Matchers.containsString("are you?"));
    }

    /**
     * FakeRequest can change URI.
     * @throws Exception If something goes wrong inside
     */
    @Test
    @org.junit.Ignore
    public void changesUri() throws Exception {
        MatcherAssert.assertThat(
            new FakeRequest()
                .uri().set(new URI("http://facebook.com")).back()
                .uri().get().toString(),
            Matchers.endsWith("facebook.com")
        );
    }

    /**
     * FakeRequest can change URI in response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void changesUriInResponse() throws Exception {
        MatcherAssert.assertThat(
            new FakeRequest()
                .uri().set(new URI("http://google.com")).back()
                .fetch().back()
                .uri().get().toString(),
            Matchers.containsString("google.com")
        );
    }

}
