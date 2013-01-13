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

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Node;

/**
 * Lazy implementation of {@link XmlDocument}.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @see <a href="http://trac.fazend.com/rexsl/ticket/324">introduced in ticket #324</a>
 */
final class LazyXml implements XmlDocument {

    /**
     * Underlying source.
     */
    private final transient TestResponse response;

    /**
     * Namespace context to use.
     */
    private final transient XPathContext context;

    /**
     * Public ctor.
     * @param src Source of content
     * @param ctx Namespace context
     */
    public LazyXml(@NotNull @Valid final TestResponse src,
        @NotNull final XPathContext ctx) {
        this.response = src;
        this.context = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LazyXml registerNs(@NotNull final String prefix,
        @NotNull final Object uri) {
        return new LazyXml(this.response, this.context.add(prefix, uri));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> xpath(@NotNull final String query) {
        return new SimpleXml(this.response.getBody())
            .merge(this.context)
            .xpath(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node node() {
        return new SimpleXml(this.response.getBody()).node();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<XmlDocument> nodes(@NotNull final String query) {
        return new SimpleXml(this.response.getBody())
            .merge(this.context)
            .nodes(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlDocument merge(@NotNull final NamespaceContext ctx) {
        return new LazyXml(this.response, this.context.merge(ctx));
    }

}
