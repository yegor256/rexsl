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
package com.rexsl.test;

import javax.xml.namespace.NamespaceContext;
import org.hamcrest.Matcher;

/**
 * Convenient set of matchers for XHTML/XML content.
 *
 * <p>For example:
 *
 * <pre>
 * MatcherAssert.assertThat(
 *   "&lt;root&gt;&lt;a/&gt;&lt;/root&gt;",
 *   XhtmlMatchers.withXPath("/root/a[.='']")
 * );
 * </pre>
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.2.6
 */
public final class XhtmlMatchers {

    /**
     * Private ctor, it's a utility class.
     */
    private XhtmlMatchers() {
        // intentionally empty
    }

    /**
     * Matches content agains XPath query.
     * @param query The query
     * @return Matcher suitable for JUnit/Hamcrest matching
     * @param <T> Type of XML content provided
     */
    public static <T> Matcher<T> hasXPath(final String query) {
        return XhtmlMatchers.hasXPath(query, new XPathContext());
    }

    /**
     * Matches content agains XPath query, with custom namespaces.
     *
     * <p>Every namespace from the {@code namespaces} list will be assigned to
     * its own prefix, in order of appearance. Start with {@code 1}.
     * For example:
     *
     * <pre>
     * MatcherAssert.assert(
     *   XhtmlConverter.the("&lt;foo xmlns='my-namespace'&gt;&lt;/foo&gt;"),
     *   XhtmlMatchers.hasXPath("/ns1:foo", "my-namespace")
     * );
     * </pre>
     *
     * @param query The query
     * @param namespaces List of namespaces
     * @return Matcher suitable for JUnit/Hamcrest matching
     * @param <T> Type of XML content provided
     */
    public static <T> Matcher<T> hasXPath(final String query,
        final Object... namespaces) {
        return XhtmlMatchers.hasXPath(query, new XPathContext(namespaces));
    }

    /**
     * Matches content agains XPath query, with custom context.
     * @param query The query
     * @param ctx The context
     * @return Matcher suitable for JUnit/Hamcrest matching
     * @param <T> Type of XML content provided
     */
    public static <T> Matcher<T> hasXPath(final String query,
        final NamespaceContext ctx) {
        return new XPathMatcher(query, ctx);
    }

}
