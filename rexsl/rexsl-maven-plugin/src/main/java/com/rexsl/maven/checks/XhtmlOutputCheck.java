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
package com.rexsl.maven.checks;

import com.rexsl.maven.AbstractCheck;
import com.rexsl.maven.Reporter;
import java.io.File;
import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;

/**
 * Validate XHTML output.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class XhtmlOutputCheck extends AbstractCheck {

    /**
     * Public ctor.
     * @param basedir Base directory of maven project
     * @param reporter The reporter to use
     */
    public XhtmlOutputCheck(final File basedir, final Reporter reporter) {
        super(basedir, reporter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean validate() {
        final File dir = new File(this.basedir(), "src/test/rexsl/xml");
        boolean success = true;
        for (File xml : FileUtils.listFiles(dir, new String[] {"xml"}, true)) {
            try {
                this.one(xml);
            } catch (InternalCheckException ex) {
                final String msg = ex.getMessage();
                if (!msg.isEmpty()) {
                    this.reporter().report(msg);
                }
                success = false;
            }
        }
        return success;
    }

    /**
     * Check one XML document.
     * @param file Check this particular XML document
     * @throws InternalCheckException If some failure inside
     */
    public final void one(final File file) throws InternalCheckException {
        final Source xml = new StreamSource(file);
        final TransformerFactory factory = TransformerFactory.newInstance();
        factory.setURIResolver(
            new XhtmlOutputCheck.InDirResolver(
                new File(this.basedir(), "src/main/webapp")
            )
        );
        Source xsl;
        try {
            xsl = factory.getAssociatedStylesheet(xml, null, null, null);
        } catch (javax.xml.transform.TransformerConfigurationException ex) {
            throw new InternalCheckException(ex);
        }
        if (xsl == null) {
            this.reporter().report(
                "Associated XSL stylesheet not found in %s",
                file
            );
            throw new InternalCheckException();
        }
        Transformer transformer;
        try {
            transformer = factory.newTransformer(xsl);
        } catch (javax.xml.transform.TransformerConfigurationException ex) {
            throw new InternalCheckException(ex);
        }
        final StringWriter writer = new StringWriter();
        try {
            transformer.transform(xml, new StreamResult(writer));
        } catch (javax.xml.transform.TransformerException ex) {
            throw new InternalCheckException(ex);
        }
        final String xhtml = writer.toString();
        // run through Groovy
    }

    /**
     * Resolve URLs to point them to directory.
     */
    private static final class InDirResolver implements URIResolver {
        /**
         * The directory to work in.
         */
        private final File dir;
        /**
         * Public ctor.
         * @param path The directory
         */
        public InDirResolver(final File path) {
            this.dir = path;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Source resolve(final String href, final String base) {
            return new StreamSource(new File(this.dir, href));
        }
    }

}
