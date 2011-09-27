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

import com.rexsl.core.XsdLocator;
import com.rexsl.core.XslResolver;
import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.rexsl.maven.Reporter;
import com.rexsl.maven.utils.Grizzly;
import com.rexsl.maven.utils.PortReserver;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import java.io.File;
import java.net.URI;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

/**
 * Validate the product in container, with Groovy scripts.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class InContainerScriptsCheck implements Check {

    /**
     * Directory with Groovy files.
     */
    private static final String GROOVY_DIR = "src/test/rexsl/scripts";

    /**
     * Directory with XSD files.
     */
    private static final String XSD_DIR = "src/test/rexsl/xsd";

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean validate(final Environment env) {
        final File dir = new File(env.basedir(), this.GROOVY_DIR);
        if (!dir.exists()) {
            env.reporter().report(
                "%s directory is absent, no scripts to run",
                this.GROOVY_DIR
            );
            return true;
        }
        if (!env.webdir().exists()) {
            env.reporter().report(
                "Webapp dir '%s' is absent, package the project first",
                env.webdir()
            );
            return false;
        }
        env.reporter().report(
            "Starting embedded Grizzly web server in '%s'...",
            env.webdir()
        );
        final Integer port = new PortReserver().port();
        final Grizzly grizzly = Grizzly.start(env.webdir(), port);
        URI home;
        try {
            home = new URI(String.format("http://localhost:%d/", port));
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
        env.reporter().report("Web front available at %s", home);
        this.validateXsd(env);
        boolean success = true;
        for (File script :
            FileUtils.listFiles(dir, new String[] {"groovy"}, true)) {
            try {
                env.reporter().report("Testing '%s'...", script);
                this.one(env, home, script);
            } catch (InternalCheckException ex) {
                env.reporter().report("Test failed: %s", ex.getMessage());
                success = false;
            }
        }
        grizzly.stop();
        env.reporter().report("Embedded Grizzly web server stopped");
        return success;
    }

    /**
     * Check one script.
     * @param env The environment
     * @param home URI of running Grizzly container
     * @param script Check this particular Groovy script
     * @throws InternalCheckException If some failure inside
     */
    public final void one(final Environment env, final URI home,
        final File script) throws InternalCheckException {
        final Binding binding = new Binding();
        binding.setVariable("documentRoot", home);
        env.reporter().log("Running %s", script);
        final GroovyExecutor exec = new GroovyExecutor(env, binding);
        exec.execute(script);
    }

    /**
     * Configure XSD validation.
     * @param env The environment
     */
    private void validateXsd(final Environment env) {
        final File folder = new File(env.basedir(), this.XSD_DIR);
        if (!folder.exists()) {
            env.reporter().report(
                "%s folder is absent, no XSD validation",
                folder
            );
            return;
        }
        XslResolver.setXsdLocator(
            new InContainerScriptsCheck.XsdSchemaLocator(env)
        );
        env.reporter().report("Using XSD schemas from %s", folder);
    }

    /**
     * The locator.
     */
    private static final class XsdSchemaLocator implements XsdLocator {
        /**
         * The environment.
         */
        private final Environment env;
        /**
         * Public ctor.
         * @param environ The environment
         */
        public XsdSchemaLocator(final Environment environ) {
            this.env = environ;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Schema locate(final Class<?> type) {
            final String name = type.getName();
            final File xsd = new File(
                String.format(
                    "%s/%s.xsd",
                    InContainerScriptsCheck.XSD_DIR,
                    name
                )
            );
            Schema schema = null;
            if (xsd.exists()) {
                final SchemaFactory factory = SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_NS_URI
                );
                try {
                    schema = factory.newSchema(xsd);
                } catch (org.xml.sax.SAXException ex) {
                    throw new IllegalStateException(ex);
                }
                this.env.reporter().report(
                    "'%s' to be validated with '%s'",
                    name,
                    xsd
                );
            } else {
                this.env.reporter().report("No XSD schema for '%s'", name);
            }
            return schema;
        }
    }

}
