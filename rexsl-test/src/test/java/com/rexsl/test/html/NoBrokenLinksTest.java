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
package com.rexsl.test.html;

import com.rexsl.test.ResponseMocker;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link NoBrokenLinks}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class NoBrokenLinksTest {

    /**
     * NoBrokenLinks can find broken links in HTML.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void findsBrokenLinksInHtml() throws Exception {
        MatcherAssert.assertThat(
            new ResponseMocker().withBody(
                StringUtils.join(
                    "<html><head>",
                    "<link rel='stylesheet' href='http://broken.rexsl.com'/>",
                    "</head></html>"
                )
            ).mock(),
            Matchers.not(
                new NoBrokenLinks(new URI("http://www.google.com/"))
            )
        );
    }

    /**
     * NoBrokenLinks can find empty links in HTML.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void findsEmptyLinksInHtml() throws Exception {
        MatcherAssert.assertThat(
            new ResponseMocker().withBody(
                "<html><head><link rel='stylesheet' href=''/></head></html>"
            ).mock(),
            Matchers.not(
                new NoBrokenLinks(new URI("http://www.facebook.com/"))
            )
        );
    }

    /**
     * NoBrokenLinks can find empty links in HTML.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void findsBrLinksInHtmlWithNamespace() throws Exception {
        MatcherAssert.assertThat(
            new ResponseMocker().withBody(
                StringUtils.join(
                    "<html xmlns='http://www.w3.org/1999/xhtml'>",
                    "<head><link rel='stylesheet' href='/broken-link'/>",
                    "</head></html>"
                )
            ).mock(),
            Matchers.not(
                new NoBrokenLinks(new URI("http://www.rexsl.com/rexsl-test"))
            )
        );
    }

    /**
     * NoBrokenLinks can pass withou broken links in HTML.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void passesWithoutBrokenLinks() throws Exception {
        MatcherAssert.assertThat(
            new ResponseMocker().withBody(
                StringUtils.join(
                    "<html xmlns='http://www.w3.org/1999/xhtml'>",
                    "<body><a href='/index.html'/>",
                    "<a href='http://img.rexsl.com/logo.png'/>",
                    "</body></html>"
                )
            ).mock(),
            new NoBrokenLinks(new URI("http://www.rexsl.com/"))
        );
    }

    /**
     * NoBrokenLinks can throw for a broken HTML.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenHtmlIsBroken() throws Exception {
        MatcherAssert.assertThat(
            new ResponseMocker().withBody("not HTML at all").mock(),
            new NoBrokenLinks(new URI("#"))
        );
    }

}
