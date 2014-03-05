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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.rexsl.maven.utils.Sources;
import java.io.File;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;

/**
 * Checks that all files are text ones.
 *
 * <p>This check validates all files found in {@code src/main/webapp} and
 * fails your build if any binary files are found there.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode
final class BinaryFilesCheck implements Check {

    @Override
    @Loggable(Loggable.DEBUG)
    public void setScope(@NotNull final String scope) {
        // nothing to scope here
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public boolean validate(@NotNull final Environment env) {
        boolean valid = true;
        final File dir = new File(env.basedir(), "src/main/webapp");
        final Collection<File> files = new Sources(dir).files();
        for (final File file : files) {
            final String path = file.getAbsolutePath()
                .substring(dir.getAbsolutePath().length() + 1);
            final String ext = FilenameUtils.getExtension(path);
            if (!ext.matches("html|xml|xhtml|txt|xsl|css|js")) {
                Logger.warn(
                    this,
                    "File %s has incorrect type/extension '%s'",
                    file, ext
                );
                valid = false;
            }
        }
        return valid;
    }
}
