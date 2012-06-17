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
import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.rexsl.maven.utils.BindingBuilder;
import com.rexsl.maven.utils.EmbeddedContainer;
import com.rexsl.maven.utils.FileFinder;
import com.rexsl.maven.utils.GroovyExecutor;
import com.rexsl.maven.utils.LoggingManager;
import com.rexsl.w3c.Defect;
import com.rexsl.w3c.ValidationResponse;
import com.rexsl.w3c.Validator;
import com.rexsl.w3c.ValidatorBuilder;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Validate XHTML output.
 *
 * <p>Since this class is NOT public its documentation is not available online.
 * All details of this check should be explained in the JavaDoc of
 * {@link DefaultChecksProvider}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (2 lines)
 */
final class XhtmlOutputCheck implements Check {

    /**
     * Directory with XML files.
     */
    private static final String XML_DIR = "src/test/rexsl/xml";

    /**
     * Directory with Groovy files.
     */
    private static final String GROOVY_DIR = "src/test/rexsl/xhtml";

    /**
     * Validator.
     */
    @NotNull
    private final transient Validator validator;

    /**
     * Scope of tests to execute.
     */
    private transient String test = ".*";

    /**
     * Mutex for synchronization.
     */
    @SuppressWarnings("PMD.FinalFieldCouldBeStatic")
    private final transient Object mutex = "_mutex_";

    /**
     * Default public ctor.
     */
    public XhtmlOutputCheck() {
        this(new ValidatorBuilder().html());
    }

    /**
     * Full ctor, for tests mostly.
     * @param val HTML validator
     */
    public XhtmlOutputCheck(@NotNull final Validator val) {
        this.validator = val;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setScope(@NotNull final String scope) {
        synchronized (this.mutex) {
            this.test = scope;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(@NotNull final Environment env) {
        final File dir = new File(env.basedir(), XhtmlOutputCheck.XML_DIR);
        boolean success = true;
        if (dir.exists()) {
            Logger.info(
                this,
                "Starting embedded servlet container in '%s'...",
                env.webdir()
            );
            final EmbeddedContainer container = EmbeddedContainer.start(env);
            final Collection<File> files = new FileFinder(dir, "xml").random();
            try {
                for (File xml : files) {
                    final String name =
                        FilenameUtils.removeExtension(xml.getName());
                    if (!name.matches(this.test)) {
                        continue;
                    }
                    LoggingManager.enter(name);
                    success &= this.one(env, xml);
                    LoggingManager.leave();
                    if (!success) {
                        Logger.warn(
                            this,
                            "Validation Failed!"
                        );
                    }
                }
            } finally {
                container.stop();
                Logger.info(this, "Embedded servlet container stopped");
            }
        } else {
            Logger.info(
                this,
                "%s directory is absent, no XHTML tests",
                XhtmlOutputCheck.XML_DIR
            );
        }
        return success;
    }

    /**
     * Check one XML document.
     * @param env Environment to work with
     * @param file Check this particular XML document
     * @return Is the XML document valid?
     */
    private boolean one(final Environment env, final File file) {
        boolean valid = true;
        final File root = new File(env.basedir(), XhtmlOutputCheck.GROOVY_DIR);
        if (!root.exists()) {
            Logger.error(
                "%s directory is absent",
                XhtmlOutputCheck.GROOVY_DIR
            );
            valid = false;
        }
        if (valid) {
            final String basename = FilenameUtils.getBaseName(file.getPath());
            final String script = Logger.format("%s.groovy", basename);
            final File groovy = new File(root, script);
            if (!groovy.exists()) {
                Logger.error(
                    "Groovy script '%s' is absent for '%s' XML page",
                    groovy.toString(),
                    file.toString()
                );
                valid = false;
            }
            try {
                final String xhtml =
                    new XhtmlTransformer().transform(env, file);
                this.validate(file, xhtml);
                final GroovyExecutor exec = new GroovyExecutor(
                    env,
                    new BindingBuilder(env).add("document", xhtml).build()
                );
                exec.execute(groovy);
            } catch (com.rexsl.maven.utils.GroovyException ex) {
                this.logException(ex);
                valid = false;
            } catch (InternalCheckException ex) {
                this.logException(ex);
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Validates XHTML file.
     * @param xml The file being validated
     * @param xhtml Contains XHTML file to validate.
     * @throws InternalCheckException If file is invalid.
     */
    private void validate(final File xml, final String xhtml)
        throws InternalCheckException {
        final ValidationResponse response = this.validator.validate(xhtml);
        if (!response.valid()) {
            Logger.error(
                this,
                "%s produced invalid XHTML:\n%s",
                xml,
                StringEscapeUtils.escapeJava(xhtml).replace("\\n", "\n")
            );
            final Set<Defect> defects = new HashSet<Defect>();
            defects.addAll(response.errors());
            defects.addAll(response.warnings());
            for (Defect defect : defects) {
                Logger.error(
                    this,
                    "[%d] %s: %s",
                    defect.line(),
                    defect.message(),
                    defect.source()
                );
            }
            throw new InternalCheckException(
                "XHTML validation failed with %d errors and %d warnings",
                response.errors().size(),
                response.warnings().size()
            );
        }
    }

    /**
     * Logs exception to the logger.
     * @param exc Exception to log.
     */
    private void logException(final Exception exc) {
        Logger.error(
            this,
            "Failed:\n%[exception]s",
            exc
        );
    }
}
