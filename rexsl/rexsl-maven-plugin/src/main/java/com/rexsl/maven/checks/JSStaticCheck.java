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

import com.googlecode.jslint4java.Issue;
import com.googlecode.jslint4java.JSLint;
import com.googlecode.jslint4java.JSLintBuilder;
import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.ymock.util.Logger;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * Validates Java script files against style sheet rules.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id: JSStaticCheck.java 204 2011-10-26 21:15:28Z guard $
 */
public final class JSStaticCheck implements Check {

    /**
     * Directory with JS files.
     */
    private static final String JS_DIR = "src/main/webapp/js";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final Environment env) {
        final File dir = new File(env.basedir(), this.JS_DIR);
        boolean success = true;
        if (dir.exists()) {
            final String[] exts = new String[] {"js"};
            for (File css : FileUtils.listFiles(dir, exts, true)) {
                try {
                    success &= this.one(css);
                } catch (InternalCheckException ex) {
                    Logger.warn(
                        this,
                        "Failed:\n%[exception]s",
                        ex
                    );
                    success = false;
                }
            }
        } else {
            Logger.info(
                this,
                "%s directory is absent, no JS tests",
                this.JS_DIR
            );
        }

        return success;
    }

    /**
     * Check one script.
     * @param file Script file to check
     * @throws InternalCheckException If some failure inside
     * @return Is script valid?
     */
    private boolean one(File file) throws InternalCheckException {
        final String jScript;
        try {
            jScript = FileUtils.readFileToString(file);
        } catch (java.io.IOException ex) {
            throw new InternalCheckException(ex);
        }
        final JSLint lint = new JSLintBuilder().fromDefault();
        final List<Issue> issues = lint.lint("systemId", jScript).getIssues();
        for (Issue issue : issues) {
            Logger.warn(
                this,
                "%s\n",
                issue.toString()
            );
        }
        return issues.size()==0;
    }
}
