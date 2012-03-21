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

import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.w3c.dom.Document;

/**
 * Implementation of {@link TestResponse}.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class DomParser {

    /**
     * The XML as a text.
     */
    private final transient String xml;

    /**
     * Public ctor.
     * @param txt The XML in text
     */
    public DomParser(final String txt) {
        if (txt == null) {
            throw new IllegalArgumentException("NULL instead of XML");
        }
        if (txt.charAt(0) != '<') {
            throw new IllegalArgumentException(
                String.format("Doesn't look like XML: '%s'", txt)
            );
        }
        this.xml = txt;
    }

    /**
     * Get document of body.
     * @return The document
     */
    public Document document() {
        Document doc;
        try {
            final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            // @checkstyle LineLength (1 line)
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setNamespaceAware(true);
            doc = factory
                .newDocumentBuilder()
                .parse(IOUtils.toInputStream(this.xml, CharEncoding.UTF_8));
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        } catch (org.xml.sax.SAXException ex) {
            throw new IllegalArgumentException(
                String.format("Invalid XML: \"%s\"", this.xml),
                ex
            );
        }
        return doc;
    }

}
