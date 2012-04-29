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

import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.ymock.util.Logger;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
 * <p>The class is immutable and thread-safe.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id: WebXmlCheck.java 204 2011-10-26 21:15:28Z guard $
 */
final class WebXmlCheck implements Check {

    /**
     * Total numbers of errors.
     */
    private transient int errors;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final Environment env) {
        final File file = new File(
            env.basedir(),
            "src/main/webapp/WEB-INF/web.xml"
        );
        boolean valid = false;
        if (file.exists()) {
            valid = this.validate(file);
        } else {
            Logger.warn(this, "File '%s' is absent, but should be there", file);
        }
        return valid;
    }

    /**
     * Performs validation of the specified XML file against it's XSD schema.
     * @param file File to be validated.
     * @return True if file is valid, <code>false</code> if file is invalid.
     */
    private boolean validate(final File file) {
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
            builder = factory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        this.errors = 0;
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
        } catch (SAXException exception) {
            throw new IllegalStateException(exception);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
        return this.errors == 0;
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
        ++this.errors;
    }

}
