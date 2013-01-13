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
import java.io.File;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Validates CSS files against style sheet rules.
 *
 * <p>The class is immutable and thread-safe.
 *
 * <p>Since this class is NOT public its documentation is not available online.
 * All details of this check should be explained in the JavaDoc of
 * {@link DefaultChecksProvider}.
 *
 * <p>{@code CssLint.class} file is created by Rhino JavaScript-to-Java
 * compiler during {@code process-sources} Maven phase (see {@code pom.xml}
 * file). Here we're just executing this class in a standalone process, in
 * order to capture its output into string.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @see <a href="http://www.mozilla.org/rhino/jsc.html">Rhino JavaScript to Java compiler</a>
 * @see <a href="https://github.com/stubbornella/csslint/wiki/Command-line-interface">CSSLint Command Line Interface</a>
 */
final class CssStaticCheck implements Check {

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
        final File csslint = new File(
            this.getClass().getResource("/CssLint.class").getFile()
        );
        final ProcessBuilder builder = new ProcessBuilder(
            "java",
            "-classpath",
            StringUtils.join(
                env.classpath(false),
                System.getProperty("path.separator")
            ),
            "CssLint",
            env.basedir().getPath(),
            "--format=compact"
        );
        builder.directory(csslint.getParentFile());
        String report;
        try {
            final Process process = builder.start();
            if (process.waitFor() != 0) {
                throw new IllegalStateException(
                    IOUtils.toString(process.getErrorStream())
                );
            }
            report = IOUtils.toString(process.getInputStream());
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        return this.isValid(report);
    }

    /**
     * Validate the report from CSSLint.
     *
     * <p>CSSLint report is a plain text document, where every line is a
     * message about one defect. In this method we split this plain text
     * document into lines and send them one by one to the log. If there are
     * no lines in the document we return {@code true}, which means that
     * there are no errors and the CSS document is valid.
     *
     * @param report The report (one problem per line)
     * @return True if it's valid (no errors)
     */
    private boolean isValid(final String report) {
        final String[] lines = report.split("\n");
        for (String line : lines) {
            Logger.warn(this, "%s", line.trim());
        }
        return lines.length == 0;
    }

}
