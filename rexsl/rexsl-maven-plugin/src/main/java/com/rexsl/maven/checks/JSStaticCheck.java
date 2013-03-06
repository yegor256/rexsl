/**
 * Copyright (c) 2011-2013, ReXSL.com
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
import java.io.File;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Validates Java script files against style sheet rules.
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
@ToString
@EqualsAndHashCode
final class JSStaticCheck implements Check {

    /**
     * Directory with JS files.
     */
    private static final String JS_DIR = "src/main/webapp/js";

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
        final File dir = new File(env.basedir(), JSStaticCheck.JS_DIR);
        boolean success = true;
        if (dir.exists()) {
            final Collection<File> files = new FileFinder(dir, "js").random();
            for (File file : files) {
                final String name =
                    FilenameUtils.removeExtension(file.getName());
                LoggingManager.enter(name);
                success &= this.isValid(file);
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
                "%s directory is absent, no JS tests",
                JSStaticCheck.JS_DIR
            );
        }
        return success;
    }

    /**
     * Check one JavaScrip file for validity.
     *
     * <pre>
     * final JSLint lint = new JSLintBuilder().fromDefault();
     * final List<Issue> issues = lint.lint("systemId", jScript)
     *     .getIssues();
     * for (Issue issue : issues) {
     *    Logger.warn(
     *        this,
     *        "%s\n",
     *        issue.toString()
     *    );
     * }
     * return issues.size()==0;
     * </pre>
     *
     * @param file Script file to check
     * @return Is script valid?
     * @todo #112! Move the code above to this method when yui finish migration
     *  to Rhino 1.7R3. At the momemnt the implementaiton is just a stub - it
     *  validates that the file exists and that's it.
     */
    private boolean isValid(final File file) {
        boolean valid = true;
        try {
            FileUtils.readFileToString(file);
        } catch (java.io.IOException ex) {
            Logger.error(
                this,
                "Failed:\n%[exception]s",
                ex
            );
            valid = false;
        }
        return valid;
    }
}
