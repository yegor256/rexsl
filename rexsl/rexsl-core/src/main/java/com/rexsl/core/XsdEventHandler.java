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
package com.rexsl.core;

import java.io.StringWriter;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;

/**
 * Handler of XSD events.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class XsdEventHandler implements ValidationEventHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleEvent(final ValidationEvent event) {
        throw new IllegalStateException(
            String.format(
                "JAXB error: \"%s\" at '%s' [%d:%d]: \"%s\"",
                event.getMessage(),
                event.getLocator().getURL(),
                event.getLocator().getLineNumber(),
                event.getLocator().getColumnNumber(),
                this.asText(event.getLocator().getNode())
            )
        );
    }

    /**
     * Convert XML node to text.
     * @param node The node to convert
     * @return The text
     */
    private String asText(final Node node) {
        String text = "XML is not available";
        if (node != null) {
            try {
                final StringWriter writer = new StringWriter();
                TransformerFactory.newInstance().newTransformer().transform(
                    new DOMSource(node),
                    new StreamResult(writer)
                );
                writer.flush();
                text = writer.toString();
            } catch (javax.xml.transform.TransformerConfigurationException ex) {
                text = ex.getMessage();
            } catch (javax.xml.transform.TransformerException ex) {
                text = ex.getMessage();
            }
        }
        return text;
    }

}
