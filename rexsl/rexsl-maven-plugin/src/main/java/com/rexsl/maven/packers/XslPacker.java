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
package com.rexsl.maven.packers;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Packager of XSL files.
 *
 * <p>All XML comments and unnecessary spaces are removed.
 *
 * <p>Since this class is NOT public its documentation is not available online.
 * All details of this check should be explained in the JavaDoc of
 * {@link PackersProvider}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class XslPacker extends AbstractPacker {

    /**
     * Document builder factory.
     */
    private static final DocumentBuilderFactory DFACTORY =
        DocumentBuilderFactory.newInstance();

    /**
     * Transformer factory.
     */
    private static final TransformerFactory TFACTORY =
        TransformerFactory.newInstance();

    /**
     * Xpath factory.
     */
    private static final XPathFactory XPATHFACTORY =
        XPathFactory.newInstance();

    /**
     * {@inheritDoc}
     */
    @Override
    protected String extension() {
        return "xsl";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void pack(final File src, final File dest) throws IOException {
        this.DFACTORY.setNamespaceAware(true);
        Document document;
        try {
            document = this.DFACTORY.newDocumentBuilder().parse(src);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        } catch (org.xml.sax.SAXException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            this.clear(document);
        } catch (XPathExpressionException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            final Transformer transformer = this.TFACTORY.newTransformer();
            transformer.setOutputProperty(
                OutputKeys.OMIT_XML_DECLARATION,
                "yes"
            );
            transformer.transform(
                new DOMSource(document),
                new StreamResult(dest)
            );
        } catch (javax.xml.transform.TransformerConfigurationException ex) {
            throw new IllegalStateException(ex);
        } catch (javax.xml.transform.TransformerException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Removes comments from the specified <code>Document</code>.
     * @param document Document to clear.
     * @throws XPathExpressionException If XPATH expression to retrieve comment
     *  <code>Node</code>s is wrong.
     */
    private void clear(final Document document)
        throws XPathExpressionException {
        final XPath xpath = this.XPATHFACTORY.newXPath();
        final XPathExpression expr = xpath.compile("//comment()");
        final Object result = expr.evaluate(document, XPathConstants.NODESET);
        final NodeList nodes = (NodeList) result;
        for (int index = 0; index < nodes.getLength(); index = index + 1) {
            final Node node = nodes.item(index);
            final Node parent = node.getParentNode();
            if (null == parent) {
                throw new IllegalStateException(
                    "Root element cannot be a comment"
                );
            }
            parent.removeChild(node);
        }
    }

}
