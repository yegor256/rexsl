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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RestResponse}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class RestResponseTest {

    /**
     * RestResponse can assert HTTP status.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = AssertionError.class)
    public void assertsHttpStatusCode() throws Exception {
        new RestResponse(
            new ResponseMocker().withStatus(HttpURLConnection.HTTP_OK).mock()
        ).assertStatus(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * RestResponse can retrieve a cookie by name.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void retrievesCookieByName() throws Exception {
        final RestResponse response = new RestResponse(
            new ResponseMocker()
                .withBody("<hello/>")
                .withHeader(
                    HttpHeaders.SET_COOKIE,
                    "cookie1=foo1;Path=/;Comment=\"\", bar=1;"
                )
                .mock()
        );
        MatcherAssert.assertThat(
            response.cookie("cookie1"),
            Matchers.allOf(
                Matchers.hasProperty("value", Matchers.equalTo("foo1")),
                Matchers.hasProperty("path", Matchers.equalTo("/"))
            )
        );
    }

    /**
     * RestResponse can transfer cookies.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void transfersCookiesOnFollow() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(Request.GET))
            .expectHeader(
                HttpHeaders.COOKIE,
                Matchers.containsString("alpha=boom")
            )
            .mock();
        final RestResponse response = new RestResponse(
            new ResponseMocker()
                .withHeader(HttpHeaders.SET_COOKIE, "alpha=boom; path=/")
                .withHeader(HttpHeaders.LOCATION, container.home().toString())
                .with(new ApacheRequest(container.home()))
                .mock()
        );
        response.follow()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestResponse can avoid transferring of empty cookies.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void avoidsTransferringOfEmptyCookiesOnFollow() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(Request.GET))
            .expectHeader(
                HttpHeaders.COOKIE,
                Matchers.not(Matchers.containsString("second"))
            )
            .mock();
        final RestResponse response = new RestResponse(
            new ResponseMocker()
                .withHeader(HttpHeaders.SET_COOKIE, "first=A; path=/")
                .withHeader(HttpHeaders.SET_COOKIE, "second=; path=/")
                .withHeader(HttpHeaders.LOCATION, container.home().toString())
                .with(new ApacheRequest(container.home()))
                .mock()
        );
        response.follow()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RestResponse can jump to a relative URL.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void jumpsToRelativeUrls() throws Exception {
        MatcherAssert.assertThat(
            new RestResponse(
                new ResponseMocker()
                    .with(new JdkRequest("http://locahost:888/tt"))
                    .mock()
            ).jump(new URI("/foo/bar?hey")).uri().get(),
            Matchers.hasToString("http://locahost:888/foo/bar?hey")
        );
    }

}
