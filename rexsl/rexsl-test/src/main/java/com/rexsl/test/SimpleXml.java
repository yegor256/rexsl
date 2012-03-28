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

import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of {@link XmlDocument}.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.7
 */
public final class SimpleXml implements XmlDocument {

    /**
     * Namespace context to use.
     */
    private final transient XPathContext context;

    /**
     * Cached document.
     */
    private final transient Element element;

    /**
     * Public ctor.
     * @param text Body
     */
    public SimpleXml(final String text) {
        this.element = new DomParser(text)
            .document()
            .getDocumentElement();
        this.context = new XPathContext();
    }

    /**
     * Private ctor.
     * @param elm The element
     * @param ctx Namespace context
     */
    private SimpleXml(final Element elm, final XPathContext ctx) {
        this.element = elm;
        this.context = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> xpath(final String query) {
        final NodeList nodes = this.nodelist(query);
        final List<String> items = new ArrayList<String>();
        for (int idx = 0; idx < nodes.getLength(); ++idx) {
            MatcherAssert.assertThat(
                "Only /text() nodes or attributes are retrievable with xpath()",
                nodes.item(idx).getNodeType(),
                Matchers.<Short>either(Matchers.equalTo(Node.TEXT_NODE))
                    .or(Matchers.equalTo(Node.ATTRIBUTE_NODE))
                    .or(Matchers.equalTo(Node.CDATA_SECTION_NODE))
            );
            items.add(nodes.item(idx).getNodeValue());
        }
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleXml registerNs(final String prefix, final Object uri) {
        return new SimpleXml(this.element, this.context.add(prefix, uri));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<XmlDocument> nodes(final String query) {
        final NodeList nodes = this.nodelist(query);
        final List<XmlDocument> items = new ArrayList<XmlDocument>();
        for (int idx = 0; idx < nodes.getLength(); ++idx) {
            MatcherAssert.assertThat(
                "Only elements are retrievable with nodes()",
                nodes.item(idx).getNodeType(),
                Matchers.equalTo(Node.ELEMENT_NODE)
            );
            items.add(new SimpleXml((Element) nodes.item(idx), this.context));
        }
        return items;
    }

    /**
     * Retrieve and return a nodelist for XPath query.
     * @param query XPath query
     * @return List of DOM nodes
     */
    private NodeList nodelist(final String query) {
        NodeList nodes;
        try {
            final XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(this.context);
            nodes = (NodeList) xpath.evaluate(
                query,
                this.element,
                XPathConstants.NODESET
            );
        } catch (javax.xml.xpath.XPathExpressionException ex) {
            throw new AssertionError(ex);
        }
        return nodes;
    }

}
