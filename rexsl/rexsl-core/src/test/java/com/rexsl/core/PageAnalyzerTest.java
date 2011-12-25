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
package com.rexsl.core;

import java.util.Arrays;
import java.util.Collection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test case for{@link PageAnalyzer}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class PageAnalyzerTest {

    /**
     * The page to test.
     */
    private final transient String page;

    /**
     * The value of "Accept" HTTP header.
     */
    private final transient String accept;

    /**
     * The value of "User-agent" HTTP header.
     */
    private final transient String agent;

    /**
     * The decision expected.
     */
    private final transient boolean decision;

    /**
     * Public ctor.
     * @param text Page content
     * @param agnt The agent
     * @param acpt Accept
     * @param dec Expected decision
     * @checkstyle ParameterNumber (3 lines)
     */
    public PageAnalyzerTest(final String text, final String agnt,
        final String acpt, final boolean dec) {
        this.page = text;
        this.agent = agnt;
        this.accept = acpt;
        this.decision = dec;
    }

    /**
     * Params for this parametrized test.
     * @return Array of arrays of params for ctor
     */
    @Parameters
    public static Collection<Object[]> params() {
        // @checkstyle LineLength (1 line)
        final String xml = "<?xml version='1.0'?><?xml-stylesheet href='/foo.xsl' type='text/xsl'?><page/>";
        return Arrays.asList(
            new Object[][] {
                {xml, null, null, true},
                {xml, "Firefox", MediaType.TEXT_HTML, true},
                {xml, null, "application/xml;q=0.9,*/*;q=0.7", true},
                {xml, "Firefox 8", "application/xml;q=0.9,*/*;q=0.4", true},
                {xml, null, MediaType.APPLICATION_XML, false},
                {xml, "Chrome 9", MediaType.TEXT_HTML, true},
                {xml, "Chrome 10", null, true},
                {xml, "Chrome", "application/xml;q=0.9,*/*;q=0.3", false},
                {"", null, null, false},
                {"", "Safari 5", MediaType.TEXT_HTML, false},
                {"some text doc", "Safari 4", MediaType.TEXT_HTML, false},
                // @checkstyle LineLength (1 line)
                {"<stylesheet xmlns='http://www.w3.org/1999/XSL/Transform'></stylesheet>", "Safari 5.1", MediaType.TEXT_HTML, false},
            }
        );
    }

    /**
     * PageAnalyzer can make decide to transform.
     * @throws Exception If something goes wrong
     */
    @Test
    public void decidesToTransform() throws Exception {
        final HttpServletRequestMocker mocker = new HttpServletRequestMocker();
        if (this.agent != null) {
            mocker.withHeader(HttpHeaders.USER_AGENT, this.agent);
        }
        if (this.accept != null) {
            mocker.withHeader(HttpHeaders.ACCEPT, this.accept);
        }
        MatcherAssert.assertThat(
            new PageAnalyzer(this.page, mocker.mock()).needsTransformation(),
            Matchers.describedAs(
                String.format(
                    "'%s' with '%s' by '%s'",
                    this.page,
                    this.accept,
                    this.agent
                ),
                Matchers.equalTo(this.decision)
            )
        );
    }

}
