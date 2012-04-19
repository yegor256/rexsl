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
package com.rexsl.misc;

import java.net.HttpCookie;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link CookieBuilder}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class CookieBuilderTest {

    /**
     * CookieBuilder can accept correct values.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void acceptsValidValues() throws Exception {
        final String[] texts = new String[] {
            "",
            "text",
            "some-text-to-accept(!)",
        };
        for (String text : texts) {
            new CookieBuilder(new URI("http://localhost/bar"))
                .name("some-name-of-cookie")
                .value(text)
                .build();
        }
    }

    /**
     * CookieBuilder can reject incorrect values.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void rejectsInvalidValues() throws Exception {
        final String[] texts = new String[] {
            " ",
            ";",
            "\\ backslash is not allowed",
        };
        for (String text : texts) {
            try {
                new CookieBuilder(new URI("http://localhost/foo"))
                    .name("some-name")
                    .value(text)
                    .build();
                Assert.fail("Exception expected here");
            } catch (IllegalArgumentException ex) {
                assert ex != null;
            }
        }
    }

    /**
     * CookieBuilder can build a valid cookie.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsCorrectCookie() throws Exception {
        final String name = "some-cookie-name";
        final String value = "some-value-of-it";
        final String cookie = new CookieBuilder(new URI("http://localhost/a"))
            .name(name)
            .value(value)
            .build()
            .toString();
        MatcherAssert.assertThat(
            HttpCookie.parse(cookie).get(0),
            Matchers.allOf(
                Matchers.hasToString(Matchers.containsString(name)),
                Matchers.hasProperty("name", Matchers.equalTo(name)),
                Matchers.hasProperty("value", Matchers.equalTo(value)),
                Matchers.hasProperty("domain", Matchers.equalTo("localhost")),
                Matchers.hasProperty("path", Matchers.equalTo("/a"))
            )
        );
    }

}
