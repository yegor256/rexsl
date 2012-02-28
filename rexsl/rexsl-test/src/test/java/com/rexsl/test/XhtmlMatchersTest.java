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
package com.rexsl.test;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link XhtmlMatchers}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class XhtmlMatchersTest {

    /**
     * XhtmlMatchers can match with custom namespace.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void matchesWithCustomNamespace() throws Exception {
        final String text = "<a xmlns='foo'><file>abc.txt</file></a>";
        MatcherAssert.assertThat(
            XhtmlConverter.the(text),
            XhtmlMatchers.hasXPath("/ns1:a/ns1:file[.='abc.txt']", "foo")
        );
    }

    /**
     * XhtmlMatchers can match against string.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void matchesPlainString() throws Exception {
        MatcherAssert.assertThat(
            "<b xmlns='bar'><file>abc.txt</file></b>",
            XhtmlMatchers.withXPath("/ns1:b/ns1:file[.='abc.txt']", "bar")
        );
        MatcherAssert.assertThat("<a><b/></a>", XhtmlMatchers.withXPath("//b"));
    }

    /**
     * XhtmlMatchers can match against type.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void matchesType() throws Exception {
        final Foo foo = new Foo();
        MatcherAssert.assertThat(
            foo,
            Matchers.allOf(
                Matchers.hasProperty("abc", Matchers.containsString("some")),
                XhtmlMatchers.<Foo>withXPath("//c")
            )
        );
    }

    public static final class Foo {

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "<a><c/></a>";
        }

        /**
         * Property abc.
         * @return Value of abc
         */
        public String getAbc() {
            return "some value";
        }
    }
}
