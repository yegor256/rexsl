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

import java.io.StringWriter;
import java.util.Locale;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;

/**
 * Private class for DOM to String converting.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class StringSource extends DOMSource {

    /**
     * Transformer factory.
     */
    private static final TransformerFactory TFACTORY =
        TransformerFactory.newInstance();

    /**
     * The XML itself.
     */
    private final transient String xml;

    /**
     * Public ctor.
     * @param text The content of the document
     */
    public StringSource(final String text) {
        super();
        this.xml = text;
        super.setNode(new DomParser(text).document());
    }

    /**
     * Public ctor.
     * @param node The node
     */
    public StringSource(final Node node) {
        super();
        final StringWriter writer = new StringWriter();
        try {
            final Transformer transformer = this.getTransformer();
            transformer.setOutputProperty(
                OutputKeys.OMIT_XML_DECLARATION,
                "yes"
            );
            transformer.transform(
                new DOMSource(node),
                new StreamResult(writer)
            );
        } catch (TransformerException ex) {
            throw new IllegalArgumentException(ex);
        }
        this.xml = writer.toString();
        super.setNode(node);
    }

    /**
     * Thread safe construct Transfomer.
     * @return Transformer, never null
     * @throws TransformerConfigurationException Transfomer problem
     */
    private Transformer getTransformer()
        throws TransformerConfigurationException {
        synchronized (this.TFACTORY) {
            return this.TFACTORY.newTransformer();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        final int length = this.xml.length();
        for (int pos = 0; pos < length; ++pos) {
            final char chr = this.xml.charAt(pos);
            // @checkstyle MagicNumber (1 line)
            if (chr > 0x7f) {
                buf.append("&#");
                buf.append(
                    Integer.toHexString(chr).toUpperCase(Locale.ENGLISH)
                );
                buf.append(";");
            } else {
                buf.append(chr);
            }
        }
        return buf.toString();
    }

}
