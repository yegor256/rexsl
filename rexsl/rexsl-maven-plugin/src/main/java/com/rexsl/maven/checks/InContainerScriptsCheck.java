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

import com.rexsl.core.JaxbConfigurator;
import com.rexsl.core.XslResolver;
import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.rexsl.maven.utils.Grizzly;
import com.rexsl.maven.utils.PortReserver;
import groovy.lang.Binding;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.SchemaFactory;
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
    public boolean validate(final Environment env) {
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
        final InContainerScriptsCheck.EventHandler handler =
            new InContainerScriptsCheck.EventHandler();
        XslResolver.setJaxbConfigurator(
            new InContainerScriptsCheck.Configurator(env, handler)
        );
        boolean success = true;
        for (File script
            : FileUtils.listFiles(dir, new String[] {"groovy"}, true)) {
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
        if (handler.events().size() > 0) {
            for (ValidationEvent event : handler.events()) {
                env.reporter().report("JAXB error: %s", event.getMessage());
            }
            success = false;
        }
        return success;
    }

    /**
     * Check one script.
     * @param env The environment
     * @param home URI of running Grizzly container
     * @param script Check this particular Groovy script
     * @throws InternalCheckException If some failure inside
     */
    private void one(final Environment env, final URI home,
        final File script) throws InternalCheckException {
        final Binding binding = new Binding();
        binding.setVariable("documentRoot", home);
        env.reporter().log("Running %s", script);
        final GroovyExecutor exec = new GroovyExecutor(env, binding);
        exec.execute(script);
    }

    /**
     * The locator.
     */
    private static final class Configurator implements JaxbConfigurator {
        /**
         * The environment.
         */
        private final Environment env;
        /**
         * Event handler.
         */
        private final ValidationEventHandler handler;
        /**
         * Public ctor.
         * @param environ The environment
         * @param hdlr Handler of validation events
         */
        public Configurator(final Environment environ,
            final ValidationEventHandler hdlr) {
            this.env = environ;
            this.handler = hdlr;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Marshaller marshaller(final Marshaller mrsh,
            final Class<?> type) {
            final String name = type.getName();
            final File xsd = new File(
                String.format(
                    "%s/%s.xsd",
                    InContainerScriptsCheck.XSD_DIR,
                    name
                )
            );
            if (xsd.exists()) {
                final SchemaFactory factory = SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_NS_URI
                );
                try {
                    mrsh.setSchema(factory.newSchema(xsd));
                } catch (org.xml.sax.SAXException ex) {
                    throw new IllegalStateException(ex);
                }
                try {
                    mrsh.setEventHandler(this.handler);
                } catch (javax.xml.bind.JAXBException ex) {
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
            return mrsh;
        }
    }

    /**
     * Handler of events.
     */
    private static final class EventHandler implements ValidationEventHandler {
        /**
         * Errors.
         */
        private final List<ValidationEvent> events =
            new ArrayList<ValidationEvent>();
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean handleEvent(final ValidationEvent event) {
            this.events.add(event);
            return true;
        }
        /**
         * Get list of events.
         * @return The list of events
         */
        public List<ValidationEvent> events() {
            return this.events;
        }
    }

}
