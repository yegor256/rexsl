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
package com.rexsl.test.wire;

import com.rexsl.test.mock.MkAnswer;
import com.rexsl.test.mock.MkContainer;
import com.rexsl.test.mock.MkGrizzlyContainer;
import com.rexsl.test.mock.MkQuery;
import com.rexsl.test.request.JdkRequest;
import com.rexsl.test.response.RestResponse;
import java.net.HttpURLConnection;
import javax.ws.rs.core.HttpHeaders;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link CookieOptimizingWire}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class CookieOptimizingWireTest {

    /**
     * CookieOptimizingWire can transfer cookies.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void transfersCookiesOnFollow() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
                .withHeader(HttpHeaders.SET_COOKIE, "alpha=boom1; path=/")
                .withHeader(HttpHeaders.LOCATION, "/")
        ).next(new MkAnswer.Simple("")).start();
        new JdkRequest(container.home())
            .through(CookieOptimizingWire.class)
            .header(HttpHeaders.COOKIE, "alpha=boom5")
            .fetch()
            .as(RestResponse.class)
            .follow()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        container.take();
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.headers().get(HttpHeaders.COOKIE),
            Matchers.hasSize(1)
        );
        MatcherAssert.assertThat(
            query.headers(),
            Matchers.hasEntry(
                Matchers.equalTo(HttpHeaders.COOKIE),
                Matchers.<String>everyItem(Matchers.equalTo("alpha=boom1"))
            )
        );
    }

    /**
     * CookieOptimizingWire can avoid transferring of empty cookies.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void avoidsTransferringOfEmptyCookiesOnFollow() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
                .withHeader(HttpHeaders.SET_COOKIE, "first=A; path=/")
                .withHeader(HttpHeaders.SET_COOKIE, "second=; path=/")
                .withHeader(HttpHeaders.LOCATION, "/a")
        ).next(new MkAnswer.Simple("")).start();
        new JdkRequest(container.home())
            .through(CookieOptimizingWire.class)
            .fetch()
            .as(RestResponse.class)
            .follow()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        container.take();
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.headers().get(HttpHeaders.COOKIE),
            Matchers.hasSize(1)
        );
        MatcherAssert.assertThat(
            query.headers(),
            Matchers.hasEntry(
                Matchers.equalTo(HttpHeaders.COOKIE),
                Matchers.not(
                    Matchers.hasItem(Matchers.containsString("second"))
                )
            )
        );
    }

}
