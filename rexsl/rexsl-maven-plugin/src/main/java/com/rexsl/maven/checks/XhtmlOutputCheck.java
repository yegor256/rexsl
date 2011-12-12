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
import com.rexsl.maven.utils.BindingBuilder;
import com.rexsl.maven.utils.EmbeddedContainer;
import com.rexsl.maven.utils.GroovyExecutor;
import com.ymock.util.Logger;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Validate XHTML output.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (2 lines)
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
    public boolean validate(final Environment env) {
        final File dir = new File(env.basedir(), this.XML_DIR);
        boolean success = true;
        if (dir.exists()) {
            Logger.info(
                this,
                "Starting embedded servlet container in '%s'...",
                env.webdir()
            );
            final EmbeddedContainer container = EmbeddedContainer.start(env);
            final String[] exts = new String[] {"xml"};
            for (File xml : FileUtils.listFiles(dir, exts, true)) {
                try {
                    Logger.info(this, "Testing %s through...", xml);
                    this.one(env, xml);
                } catch (InternalCheckException ex) {
                    Logger.warn(
                        this,
                        "Failed: %s\n%s",
                        ex.getMessage(),
                        this.stacktrace(ex)
                    );
                    success = false;
                }
            }
            container.stop();
            Logger.info(this, "Embedded servlet container stopped");
        } else {
            Logger.info(
                this,
                "%s directory is absent, no XHTML tests",
                this.XML_DIR
            );
        }
        return success;
    }

    /**
     * Check one XML document.
     * @param env Environment to work with
     * @param file Check this particular XML document
     * @throws InternalCheckException If some failure inside
     */
    private void one(final Environment env, final File file)
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
        final String xhtml = new XhtmlTransformer().transform(env, file);
        this.validate(xhtml);
        final GroovyExecutor exec = new GroovyExecutor(
            env,
            new BindingBuilder(env).add("document", xhtml).build()
        );
        try {
            exec.execute(groovy);
        } catch (com.rexsl.maven.utils.GroovyException ex) {
            throw new InternalCheckException(ex);
        }
    }

    /**
     * Validates XHTML file.
     * @param xhtml Contains XHTML file to validate.
     * @throws InternalCheckException If file is invalid.
     */
    private void validate(final String xhtml) throws InternalCheckException {
        assert xhtml != null;
    }

    /**
     * Exception to string conversion.
     * @param exp The exception
     * @return Stacktrace
     */
    private String stacktrace(final Exception exp) {
        final StringWriter writer = new StringWriter();
        exp.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

}
