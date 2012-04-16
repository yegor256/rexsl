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
import javax.xml.transform.Source;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.w3c.dom.Node;

/**
 * Matcher of XPath against a plain string.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.7
 */
final class XPathMatcher<T> extends TypeSafeMatcher<T> {

    /**
     * The XPath to use.
     */
    private final transient String xpath;

    /**
     * The context to use.
     */
    private final transient NamespaceContext context;

    /**
     * Public ctor.
     * @param query The query
     * @param ctx The context
     */
    public XPathMatcher(final String query, final NamespaceContext ctx) {
        super();
        this.xpath = query;
        this.context = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesSafely(final T input) {
        Source source;
        if (input instanceof Source) {
            source = (Source) input;
        } else if (input instanceof Node) {
            source = new StringSource((Node) input);
        } else {
            source = new StringSource(input.toString());
        }
        return !new SimpleXml(source)
            .merge(this.context)
            .nodes(this.xpath)
            .isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void describeTo(final Description description) {
        description.appendText("an XML document with XPath ")
            .appendText(this.xpath);
    }

}
