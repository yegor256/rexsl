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
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Checks rexsl directory contents.
 *
 * <p>Since this class is NOT public its documentation is not available online.
 * All details of this check should be explained in the JavaDoc of
 * {@link DefaultChecksProvider}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode
final class RexslFilesCheck implements Check {

    /**
     * Allowed extensions for files in sub folders.
     * @checkstyle MultipleStringLiterals (15 lines)
     */
    @SuppressWarnings({
        "PMD.UseConcurrentHashMap",
        "PMD.AvoidDuplicateLiterals",
        "unchecked"
    })
    private static final Map<Object, Object> EXTS = ArrayUtils.toMap(
        new Object[][]{
            {"xml", "xml"},
            {"xhtml", "groovy"},
            {"scripts", "groovy"},
            {"setup", "groovy"},
            {"bootstrap", "groovy"},
            {"xsd", "xsd"},
        }
    );

    @Override
    @Loggable(Loggable.DEBUG)
    public void setScope(@NotNull final String scope) {
        // nothing to scope here
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public boolean validate(@NotNull final Environment env) {
        final File dir = new File(env.basedir(), "src/test/rexsl");
        boolean valid = true;
        if (dir.exists()) {
            for (final File folder : dir.listFiles()) {
                if (!RexslFilesCheck.EXTS.containsKey(folder.getName())) {
                    continue;
                }
                valid &= this.validate(folder);
            }
        } else {
            Logger.warn(this, "Directory '%s' is absent", dir);
        }
        return valid;
    }

    /**
     * Validate one folder.
     * @param folder The folder
     * @return TRUE if valid
     */
    private boolean validate(final File folder) {
        boolean valid = true;
        final Collection<File> files = new Sources(folder).files();
        for (final File file : files) {
            final String ext = FilenameUtils.getExtension(file.getPath());
            final String regex = RexslFilesCheck.EXTS
                .get(folder.getName()).toString();
            if (!ext.matches(regex)) {
                Logger.warn(
                    this,
                    "File '%s' has incorrect extension (should match '%s')",
                    file,
                    regex
                );
                valid = false;
            }
        }
        return valid;
    }
}
