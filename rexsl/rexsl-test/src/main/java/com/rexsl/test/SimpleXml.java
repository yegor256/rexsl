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
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
    private final transient Node node;

    /**
     * Public ctor.
     *
     * <p>The object is created with a default implementation of
     * {@link javax.xml.namespace.NamespaceContext}, which already defines a
     * number of namespaces, for convenience, including:
     *
     * <pre>
     * xhtml: http://www.w3.org/1999/xhtml
     * xs: http://www.w3.org/2001/XMLSchema
     * xsi: http://www.w3.org/2001/XMLSchema-instance
     * xsl: http://www.w3.org/1999/XSL/Transform
     * svg: http://www.w3.org/2000/svg
     * </pre>
     *
     * <p>In future versions we will add more namespaces (submit a ticket if
     * you need more of them defined here).
     *
     * @param text Body
     */
    public SimpleXml(final String text) {
        this(
            new DomParser(text).document().getDocumentElement(),
            new XPathContext()
        );
    }

    /**
     * Public ctor.
     *
     * <p>The object is created with a default implementation of
     * {@link javax.xml.namespace.NamespaceContext}, which already defines a
     * number of namespaces, see {@link SimpleXml#SimpleXml(String)}.
     *
     * @param source DOM source
     */
    public SimpleXml(final Source source) {
        this(SimpleXml.transform(source), new XPathContext());
    }

    /**
     * Private ctor.
     * @param dom The source
     * @param ctx Namespace context
     */
    private SimpleXml(final Node dom, final XPathContext ctx) {
        this.node = dom;
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
        return new SimpleXml(this.node, this.context.add(prefix, uri));
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
            items.add(new SimpleXml(nodes.item(idx), this.context));
        }
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlDocument merge(final NamespaceContext ctx) {
        return new SimpleXml(this.node, this.context.merge(ctx));
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
                this.node,
                XPathConstants.NODESET
            );
        } catch (javax.xml.xpath.XPathExpressionException ex) {
            throw new AssertionError(ex);
        }
        return nodes;
    }

    /**
     * Transform source to DOM node.
     * @param source The source
     * @return The node
     */
    private static Node transform(final Source source) {
        final DOMResult result = new DOMResult();
        try {
            TransformerFactory.newInstance()
                .newTransformer()
                .transform(source, result);
        } catch (javax.xml.transform.TransformerConfigurationException ex) {
            throw new IllegalStateException(ex);
        } catch (javax.xml.transform.TransformerException ex) {
            throw new IllegalStateException(ex);
        }
        return result.getNode();
    }

}
