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

import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link WebLinkingResponse}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.9
 * @checkstyle MultipleStringLiterals (500 lines)
 */
public final class WebLinkingResponseTest {

    /**
     * WebLinkingResponse can recognize Links in headers.
     * @throws Exception If something goes wrong inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void parsesLinksInHeaders() throws Exception {
        final String[] headers = {
            "</hey/foo>; title=\"Hi!\"; rel=foo",
            "</hey/foo>; title=\"\u20ac\"; rel=\"foo\"; media=\"text/xml\"",
        };
        for (final String header : headers) {
            final WebLinkingResponse response = new WebLinkingResponse(
                new ResponseMocker()
                    .withHeader("Link", header)
                    .mock()
            );
            final WebLinkingResponse.Link link = response.link("foo");
            MatcherAssert.assertThat(
                link.uri(),
                Matchers.hasToString("/hey/foo")
            );
            MatcherAssert.assertThat(
                link,
                Matchers.hasKey("title")
            );
            MatcherAssert.assertThat(
                response.hasLink("another one"),
                Matchers.is(false)
            );
        }
    }

    /**
     * WebLinkingResponse can follow a link.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void followsLinksInHeaders() throws Exception {
        final WebLinkingResponse response = new WebLinkingResponse(
            new ResponseMocker()
                .with(new JdkRequest("http://localhost/test"))
                .withHeader(
                    "Link",
                    "</a>; rel=\"first\", <http://localhost/o>; rel=\"second\""
                )
                .mock()
        );
        MatcherAssert.assertThat(
            response.follow("first").uri().get(),
            Matchers.equalTo(new URI("http://localhost/a"))
        );
        MatcherAssert.assertThat(
            response.follow("second").uri().get(),
            Matchers.equalTo(new URI("http://localhost/o"))
        );
    }

}
