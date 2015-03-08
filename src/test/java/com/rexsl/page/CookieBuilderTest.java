/**
 * Copyright (c) 2011-2015, ReXSL.com
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

import java.net.HttpCookie;
import java.net.URI;
import org.hamcrest.CustomMatcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link CookieBuilder}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
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
        final String[] texts = {
            "",
            "text",
            "some-text-to-accept(!)",
        };
        for (final String text : texts) {
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
        final String[] texts = {
            " ",
            ";",
            "\\ backslash is not allowed",
        };
        for (final String text : texts) {
            try {
                new CookieBuilder(new URI("http://localhost/foo"))
                    .name("some-name")
                    .value(text)
                    .build();
                Assert.fail("Exception expected here");
            } catch (final IllegalArgumentException ex) {
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
        final String name = "some-cookie-name-6";
        final String value = "some-value-of-it-6";
        MatcherAssert.assertThat(
            new CookieBuilder(new URI("http://google.com/6"))
                .name(name)
                .value(value)
                .build(),
            Matchers.allOf(
                Matchers.hasToString(Matchers.containsString(name)),
                Matchers.hasProperty("maxAge", Matchers.lessThanOrEqualTo(0)),
                Matchers.hasProperty("domain", Matchers.equalTo("google.com")),
                Matchers.hasProperty("path", Matchers.equalTo("/6")),
                Matchers.hasProperty("name", Matchers.equalTo(name)),
                // @checkstyle MultipleStringLiterals (1 line)
                Matchers.hasProperty("value", Matchers.equalTo(value))
            )
        );
    }

    /**
     * CookieBuilder can build a cookie cleaning request.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsCookieDeletionRequest() throws Exception {
        final String cookie = new CookieBuilder(new URI("http://localhost/f"))
            .name("some-other-cookie-name")
            .build()
            .toString();
        MatcherAssert.assertThat(
            HttpCookie.parse(cookie).get(0),
            Matchers.allOf(
                Matchers.hasProperty("value", Matchers.equalTo("")),
                new CustomMatcher<HttpCookie>("expired cookie") {
                    @Override
                    public boolean matches(final Object obj) {
                        final HttpCookie cookie = HttpCookie.class.cast(obj);
                        return cookie.hasExpired();
                    }
                }
            )
        );
    }

    /**
     * CookieBuilder can build cookie with broken URI.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsCookieWithBrokenInputUri() throws Exception {
        new CookieBuilder(new URI("#"))
            .name("some-cookie-name-888")
            .value("the-value")
            .path("/999")
            .build();
    }

}
