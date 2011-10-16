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
package com.rexsl.maven.utils;

import com.rexsl.core.JaxbConfigurator;
import com.ymock.util.Logger;
import java.io.File;
import javax.servlet.ServletContext;
import javax.xml.XMLConstants;
import javax.xml.bind.Marshaller;
import javax.xml.validation.SchemaFactory;

/**
 * Configures JAXB marshaller in runtime in order to check XSD compliance
 * of all output XML documents.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class XsdConfigurator implements JaxbConfigurator {

    /**
     * Folder with XSD files.
     */
    private File folder;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ServletContext ctx) {
        final String name =
            ctx.getInitParameter("com.rexsl.maven.utils.XSD_FOLDER");
        if (name == null) {
            // this parameter should come from EmbeddedContainer#params()
            throw new IllegalStateException("XSD folder is not configured");
        }
        this.folder = new File(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Marshaller marshaller(final Marshaller mrsh,
        final Class<?> type) {
        final String name = type.getName();
        final File xsd = new File(this.folder, String.format("%s.xsd", name));
        if (xsd.exists()) {
            final SchemaFactory factory = SchemaFactory.newInstance(
                XMLConstants.W3C_XML_SCHEMA_NS_URI
            );
            try {
                mrsh.setSchema(factory.newSchema(xsd));
            } catch (org.xml.sax.SAXException ex) {
                throw new IllegalStateException(
                    String.format(
                        "Failed to use XSD schema from '%s' for class '%s'",
                        xsd,
                        name
                    ),
                    ex
                );
            }
            try {
                mrsh.setEventHandler(new XsdEventHandler());
            } catch (javax.xml.bind.JAXBException ex) {
                throw new IllegalStateException(ex);
            }
            Logger.info(
                this,
                "'%s' will be validated with '%s' schema",
                name,
                xsd
            );
        } else {
            Logger.info(
                this,
                "No XSD schema for '%s' in '%s' file",
                name,
                xsd
            );
        }
        return mrsh;
    }

}
