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
package com.rexsl.maven.checks;

import com.jcabi.log.Logger;
import com.rexsl.maven.Environment;
import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.constraints.NotNull;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Transform XML to XHTML through XSL.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class XhtmlTransformer {

    /**
     * Turn XML into XHTML.
     * @param env Environment
     * @param file XML document
     * @return XHTML as text
     * @throws InternalCheckException If some failure inside
     */
    public String transform(@NotNull final Environment env,
        @NotNull final File file) throws InternalCheckException {
        final TransformerFactory factory = this.factory(env);
        final Source xsl = this.xsl(file, factory);
        final StringWriter writer = this.transformer(file, factory, xsl);
        return writer.toString();
    }
    /**
     * Constructs writer that contains XHTML code from XML document.
     * @param file XML document.
     * @param factory Transformer factory
     * @param xsl XSL Source
     * @return Writer that contains XHTML code from XML document
     * @throws InternalCheckException If some failure inside
     */
    private StringWriter transformer(final File file,
        final TransformerFactory factory, final Source xsl)
        throws InternalCheckException {
        final Source xml = new StreamSource(file);
        final StringWriter writer = new StringWriter();
        try {
            final Transformer transformer = factory.newTransformer(xsl);
            transformer.transform(xml, new StreamResult(writer));
        } catch (TransformerConfigurationException ex) {
            throw new InternalCheckException(ex);
        } catch (TransformerException ex) {
            throw new InternalCheckException(ex);
        }
        return writer;
    }
    /**
     * Creates XSL {@link Source} based on XML doc and transformer factory.
     * @param file XML document
     * @param factory Transformer factory
     * @return XSL source
     * @throws InternalCheckException If some failure inside
     */
    private Source xsl(final File file, final TransformerFactory factory)
        throws InternalCheckException {
        final Source xml = new StreamSource(file);
        Source xsl;
        try {
            xsl = factory.getAssociatedStylesheet(xml, null, null, null);
        } catch (TransformerConfigurationException ex) {
            throw new InternalCheckException(ex);
        }
        if (xsl == null) {
            throw new InternalCheckException(
                "Associated XSL stylesheet not found in '%s'",
                file
            );
        }
        return xsl;
    }
    /**
     * Creates a {@link TransformerFactory} based on the provided environment.
     * @param env Environment
     * @return Transformer factory
     */
    private TransformerFactory factory(final Environment env) {
        final TransformerFactory factory = TransformerFactory.newInstance();
        try {
            final URI home = new URI(
                Logger.format("http://localhost:%d", env.port())
            );
            factory.setURIResolver(new RuntimeResolver(home));
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        return factory;
    }
}
