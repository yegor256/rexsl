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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Validates CSS files against style sheet rules.
 *
 * <p>{@code CssLint.class} file is created by Rhino JavaScript-to-Java
 * compiler during {@code process-sources} Maven phase (see {@code pom.xml}
 * file). Here we're just executing this class in a standalone process, in
 * order to capture its output into string.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id: CssStaticCheck.java 204 2011-10-26 21:15:28Z guard $
 * @see <a href="http://www.mozilla.org/rhino/jsc.html">Rhino JavaScript to Java compiler</a>
 * @see <a href="https://github.com/stubbornella/csslint/wiki/Command-line-interface">CSSLint Command Line Interface</a>
 */
final class CssStaticCheck implements Check {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final Environment env) {
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
     * @param report The report (one problem per line)
     * @return True if it's valid (no errors)
     */
    private boolean isValid(final String report) {
        final String[] lines = report.split("\n|\r\n");
        for (String line : lines) {
            Logger.warn(this, "%s", line);
        }
        return lines.length == 0;
    }

}
