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
package com.rexsl.test.response;

import com.jcabi.aspects.Immutable;
import com.jcabi.immutable.ArrayMap;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XPathContext;
import com.rexsl.test.Request;
import com.rexsl.test.Response;
import com.rexsl.test.XhtmlMatchers;
import java.net.URI;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;
import lombok.EqualsAndHashCode;
import org.hamcrest.MatcherAssert;

/**
 * XML response.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class XmlResponse extends AbstractResponse {

    /**
     * Map of namespaces.
     */
    private final transient ArrayMap<String, String> namespaces;

    /**
     * Public ctor.
     * @param resp Response
     */
    public XmlResponse(
        @NotNull(message = "response can't be NULL") final Response resp) {
        this(resp, new ArrayMap<String, String>());
    }

    /**
     * Public ctor.
     * @param resp Response
     * @param map Map of namespaces
     */
    private XmlResponse(final Response resp,
        final ArrayMap<String, String> map) {
        super(resp);
        this.namespaces = map;
    }

    /**
     * Get XML body.
     * @return XML body
     */
    @NotNull(message = "XML is never NULL")
    public XML xml() {
        return new XMLDocument(this.body()).merge(this.context());
    }

    /**
     * Register this new namespace.
     * @param prefix Prefix to use
     * @param uri Namespace URI
     * @return This object
     */
    @NotNull(message = "response is never NULL")
    public XmlResponse registerNs(
        @NotNull(message = "prefix can't be NULL") final String prefix,
        @NotNull(message = "URI can't be NULL") final String uri) {
        return new XmlResponse(this, this.namespaces.with(prefix, uri));
    }

    /**
     * Verifies HTTP response body XHTML/XML content against XPath query,
     * and throws {@link AssertionError} in case of mismatch.
     * @param xpath Query to use
     * @return This object
     */
    @NotNull(message = "response is never NULL")
    public XmlResponse assertXPath(
        @NotNull(message = "xpath can't be NULL") final String xpath) {
        MatcherAssert.assertThat(
            String.format(
                "XML doesn't contain required XPath '%s':\n%s",
                xpath, this.body()
            ),
            this.body(),
            XhtmlMatchers.hasXPath(xpath, this.context())
        );
        return this;
    }

    /**
     * Follow XML link.
     * @param query XPath query to fetch new URI
     * @return New request
     */
    @NotNull(message = "request is never NULL")
    public Request rel(
        @NotNull(message = "query can't be NULL") final String query) {
        this.assertXPath(query);
        return new RestResponse(this).jump(
            URI.create(this.xml().xpath(query).get(0))
        );
    }

    /**
     * Create XPath context.
     * @return Context
     */
    private NamespaceContext context() {
        XPathContext context = new XPathContext();
        for (final Map.Entry<String, String> entry
            : this.namespaces.entrySet()) {
            context = context.add(entry.getKey(), entry.getValue());
        }
        return context;
    }

}
