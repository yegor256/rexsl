/**
 * Copyright (c) 2011-2012, ReXSL.com
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
package com.rexsl.page;

import java.net.URI;
import javax.ws.rs.core.UriInfo;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link ForwardedUriInfo}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id: ForwardedUriInfoTest.java 2143 2012-10-28 15:44:26Z yegor@tpc2.com $
 */
public final class ForwardedUriInfoTest {

    /**
     * ForwardedUriInfo can forward request.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void forwardsUriCorrectlyWithXForwarder() throws Exception {
        final UriInfo info = new ForwardedUriInfo(
            new UriInfoMocker()
                .withRequestUri(new URI("http://localhost/abc/foo?test"))
                .mock(),
            new HttpHeadersMocker()
                .withHeader("X-forwarded-Host", "example.com")
                .withHeader("x-Forwarded-host", "proxy.example.com")
                .withHeader("X-Forwarded-Proto", "https")
                .mock()
        );
        MatcherAssert.assertThat(
            info.getRequestUri().toString(),
            Matchers.equalTo("https://example.com/abc/foo?test")
        );
    }

    /**
     * ForwardedUriInfo can forward request with official header.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void forwardsUriCorrectlyWithOfficialForwarder() throws Exception {
        final UriInfo info = new ForwardedUriInfo(
            new UriInfoMocker()
                .withRequestUri(new URI("http://localhost/a-a"))
                .mock(),
            new HttpHeadersMocker()
                .withHeader("Forwarded", "host=a.com;for=proxy;proto=https")
                .withHeader("forwarded", "host=b.com;for=proxy;proto=http")
                .mock()
        );
        MatcherAssert.assertThat(
            info.getRequestUri().toString(),
            Matchers.equalTo("https://a.com/a-a")
        );
    }

}
