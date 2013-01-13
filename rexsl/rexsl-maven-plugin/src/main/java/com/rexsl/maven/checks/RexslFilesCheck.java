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
import java.util.Collection;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.lang.ArrayUtils;

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
    private static final Map<String, String> EXTS = ArrayUtils.toMap(
        new String[][]{
            {"xml", "xml"},
            {"xhtml", "groovy"},
            {"scripts", "groovy"},
            {"setup", "groovy"},
            {"bootstrap", "groovy"},
            {"xsd", "xsd"},
        }
    );

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
        final File dir = new File(env.basedir(), "src/test/rexsl");
        boolean valid = true;
        if (dir.exists()) {
            for (File folder : dir.listFiles()) {
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
        for (File file : this.getFiles(folder)) {
            final String ext = FilenameUtils.getExtension(file.getPath());
            final String regex = RexslFilesCheck.EXTS.get(folder.getName());
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

    /**
     * Get files, recursively.
     * @param dir The directory to read from
     * @return Collection of files
     */
    private Collection<File> getFiles(final File dir) {
        return FileUtils.listFiles(
            dir,
            HiddenFileFilter.VISIBLE,
            new AndFileFilter(
                HiddenFileFilter.VISIBLE,
                new NotFileFilter(new NameFileFilter(".svn"))
            )
        );
    }

}
