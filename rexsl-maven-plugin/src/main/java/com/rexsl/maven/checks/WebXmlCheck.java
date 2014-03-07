/**
 * Copyright (c) 2011-2014, ReXSL.com
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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates web.xml file against it's XSD schema.
 *
 * <p>Since this class is NOT public its documentation is not available online.
 * All details of this check should be explained in the JavaDoc of
 * {@link DefaultChecksProvider}.
 *
 * <p>The class is thread-safe.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode
final class WebXmlCheck implements Check {

    /**
     * Total numbers of errors.
     */
    private final transient AtomicInteger errors = new AtomicInteger();

    @Override
    @Loggable(Loggable.DEBUG)
    public void setScope(@NotNull final String scope) {
        // nothing to scope here
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public boolean validate(@NotNull final Environment env) throws IOException {
        final File file = new File(
            env.basedir(),
            "src/main/webapp/WEB-INF/web.xml"
        );
        final boolean exists = file.exists();
        if (!exists) {
            Logger.warn(this, "File '%s' is absent", file);
        }
        return exists && (WebXmlCheck.offline() || this.validate(file));
    }

    /**
     * Performs validation of the specified XML file against it's XSD schema.
     * @param file File to be validated.
     * @return If file is valid returns {@code TRUE}
     * @throws IOException If fails
     */
    private boolean validate(final File file) throws IOException {
        final DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setAttribute(
            "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
            "http://www.w3.org/2001/XMLSchema"
        );
        DocumentBuilder builder;
        try {
            factory.setFeature(
                "http://apache.org/xml/features/continue-after-fatal-error",
                true
            );
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        this.errors.set(0);
        builder.setErrorHandler(
            new ErrorHandler() {
                @Override
                public void warning(final SAXParseException excn) {
                    WebXmlCheck.this.error("WARNING", excn);
                }
                @Override
                public void error(final SAXParseException excn) {
                    WebXmlCheck.this.error("ERROR", excn);
                }
                @Override
                public void fatalError(final SAXParseException excn) {
                    WebXmlCheck.this.error("FATAL", excn);
                }
            }
        );
        try {
            builder.parse(file);
        } catch (SAXException ex) {
            throw new IllegalStateException(ex);
        }
        return this.errors.get() == 0;
    }

    /**
     * Registers validation error.
     * @param level Level of error
     * @param excn Exception to be added to the container.
     */
    private void error(final String level, final SAXParseException excn) {
        Logger.error(
            this,
            "web.xml[%d:%d] %s: %s",
            excn.getLineNumber(),
            excn.getColumnNumber(),
            level,
            excn.getMessage()
        );
        this.errors.incrementAndGet();
    }

    /**
     * Are we offline?
     * @return TRUE if we're offline
     */
    private static boolean offline() {
        boolean offline = false;
        try {
            new URL("http://www.google.com").getContent();
        } catch (IOException ex) {
            offline = true;
        }
        if (offline) {
            Logger.warn(
                WebXmlCheck.class,
                "We're offline, can't validate web.xml"
            );
        }
        return offline;
    }

}
