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

import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.rexsl.maven.Reporter;
import groovy.lang.Binding;
import java.io.File;
import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

/**
 * Validate XHTML output.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class XhtmlOutputCheck implements Check {

    /**
     * Directory with XML files.
     */
    private static final String XML_DIR = "src/test/rexsl/xml";

    /**
     * Directory with Groovy files.
     */
    private static final String GROOVY_DIR = "src/test/rexsl/xhtml";

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean validate(final Environment env) {
        final File dir = new File(env.basedir(), this.XML_DIR);
        if (!dir.exists()) {
            env.reporter().report(
                "%s directory is absent, no XHTML tests",
                this.XML_DIR
            );
            return true;
        }
        boolean success = true;
        for (File xml : FileUtils.listFiles(dir, new String[] {"xml"}, true)) {
            try {
                env.reporter().report("Testing %s...", xml);
                this.one(env, xml);
            } catch (InternalCheckException ex) {
                env.reporter().report("Failed: %s", ex.getMessage());
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
    public final void one(final Environment env, final File file)
        throws InternalCheckException {
        final File root = new File(env.basedir(), this.GROOVY_DIR);
        if (!root.exists()) {
            throw new InternalCheckException(
                "%s directory is absent",
                this.GROOVY_DIR
            );
        }
        final String basename = FilenameUtils.getBaseName(file.getPath());
        final String script = String.format("%s.groovy", basename);
        final File groovy = new File(root, script);
        if (!groovy.exists()) {
            throw new InternalCheckException(
                "Groovy script '%s' is absent for '%s' XML page",
                groovy,
                file
            );
        }
        final String xhtml = this.xhtml(env, file);
        final Binding binding = new Binding();
        binding.setVariable("document", xhtml);
        final GroovyExecutor exec = new GroovyExecutor(env, binding);
        exec.execute(groovy);
    }

    /**
     * Turn XML into XHTML.
     * @param env Environment
     * @param file XML document
     * @throws InternalCheckException If some failure inside
     */
    public final String xhtml(final Environment env, final File file)
        throws InternalCheckException {
        final Source xml = new StreamSource(file);
        final TransformerFactory factory = TransformerFactory.newInstance();
        factory.setURIResolver(
            new XhtmlOutputCheck.InDirResolver(
                new File(env.basedir(), "src/main/webapp")
            )
        );
        Source xsl;
        try {
            xsl = factory.getAssociatedStylesheet(xml, null, null, null);
        } catch (javax.xml.transform.TransformerConfigurationException ex) {
            throw new InternalCheckException(ex);
        }
        if (xsl == null) {
            throw new InternalCheckException(
                "Associated XSL stylesheet not found in '%s'",
                file
            );
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
        return writer.toString();
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
