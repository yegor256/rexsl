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
package com.rexsl.core;

import java.util.Arrays;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test case for {@link UserAgent}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class UserAgentTest {

    /**
     * The decision.
     */
    private final transient boolean decision;

    /**
     * The text to test.
     */
    private final transient String text;

    /**
     * Public ctor.
     * @param supports Does it support XHTML?
     * @param txt The text
     */
    public UserAgentTest(final boolean supports, final String txt) {
        this.decision = supports;
        this.text = txt;
    }

    /**
     * Params for this parametrized test.
     * @return Array of arrays of params for ctor
     */
    @Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(
            new Object[][] {
                // @checkstyle LineLength (2 lines)
                {true, "Mozilla/5.0 AppleWebKit/534.51.22 (KHTML, like Gecko) Version/5.1.1 Safari/534.51.22"},
                {true, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22"},
                {false, "Firefox/3.0.2 (Mac; Intel) Version/398.0-beta"},
                {true, "Chrome/10.5 (HTML like Geko) Version/10"},
                {false, "Chrome/7 Version/7.89.96655-beta"},
                {false, ""},
                {false, "some incorrect User-Agent header"},
                {false, null},
            }
        );
    }

    /**
     * UserAgent can make correct decision.
     * @throws Exception If something goes wrong
     */
    @Test
    public void decidesToTransform() throws Exception {
        final UserAgent agent = new UserAgent(this.text);
        MatcherAssert.assertThat(
            agent.isXsltCapable(),
            Matchers.describedAs(
                agent.toString(),
                Matchers.equalTo(this.decision)
            )
        );
    }

}
