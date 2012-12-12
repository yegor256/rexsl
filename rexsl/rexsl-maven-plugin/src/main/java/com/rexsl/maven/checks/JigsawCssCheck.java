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
import com.rexsl.maven.utils.FileFinder;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Validates CSS files.
 *
 * <p>Since this class is NOT public its documentation is not available online.
 * All details of this check should be explained in the JavaDoc of
 * {@link DefaultChecksProvider}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
final class JigsawCssCheck implements Check {

    /**
     * Directory with CSS files.
     */
    private static final String CSS_DIR = "src/main/webapp/css";

    /**
     * Validator.
     */
    @NotNull
    private final transient Validator validator;

    /**
     * Public ctor, default.
     */
    public JigsawCssCheck() {
        this(new ValidatorBuilder().css());
    }

    /**
     * Public ctor, with custom validator.
     * @param val The validator to use
     */
    public JigsawCssCheck(@NotNull final Validator val) {
        this.validator = val;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setScope(@NotNull final String scope) {
        // nothing to scope here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(@NotNull final Environment env) {
        final File dir = new File(env.basedir(), JigsawCssCheck.CSS_DIR);
        boolean success = true;
        if (dir.exists()) {
            final Collection<File> files = new FileFinder(dir, "css").random();
            for (File css : files) {
                final String name =
                    FilenameUtils.removeExtension(css.getName());
                LoggingManager.enter(name);
                success &= this.one(css);
                LoggingManager.leave();
                if (!success) {
                    Logger.warn(
                        this,
                        "Validation Failed!"
                    );
                }
            }
        } else {
            Logger.info(
                this,
                "%s directory is absent, no CSS tests",
                JigsawCssCheck.CSS_DIR
            );
        }
        return success;
    }

    /**
     * Check one CSS page.
     * @param file Check this particular CSS document
     * @return Is CSS valid?
     */
    private boolean one(final File file) {
        boolean valid = true;
        String page = null;
        try {
            page = FileUtils.readFileToString(file);
        } catch (java.io.IOException ex) {
            Logger.error(
                this,
                "Failed:\n%[exception]s",
                ex
            );
            valid = false;
        }
        if (valid) {
            final ValidationResponse response = this.validator.validate(page);
            final Set<Defect> defects = new HashSet<Defect>();
            defects.addAll(response.errors());
            defects.addAll(response.warnings());
            for (Defect defect : defects) {
                Logger.error(
                    this,
                    "[%d] %s: %s",
                    defect.line(),
                    defect.message(),
                    defect.explanation()
                );
            }
            if (!response.valid() && valid) {
                Logger.error(
                    this,
                    "%s contains invalid CSS (see errors above):\n%s",
                    file,
                    StringEscapeUtils.escapeJava(page).replace("\\n", "\n")
                );
                Logger.error(
                    this,
                    "CSS validation failed with %d errors and %d warnings",
                    response.errors().size(),
                    response.warnings().size()
                );
                valid = false;
            }
        }
        return valid;
    }

}
