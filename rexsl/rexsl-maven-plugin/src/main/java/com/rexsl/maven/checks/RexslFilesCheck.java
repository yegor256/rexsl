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
import java.util.Collection;
import java.util.Map;
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
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 */
public final class RexslFilesCheck implements Check {

    /**
     * Allowed extensions for files in sub folders.
     */
    @SuppressWarnings({
        "PMD.UseConcurrentHashMap",
        "PMD.AvoidDuplicateLiterals"
    })
    private static final Map<String, String> DIR_EXTENSIONS = ArrayUtils.toMap(
        new String[][]{
            {"xml", "xml|"},
            // @checkstyle MultipleStringLiterals (4 lines)
            {"xhtml", "groovy|"},
            {"scripts", "groovy|"},
            {"setup", "groovy|"},
            {"bootstrap", "groovy|"},
            {"xsd", "xsd|"},
        }
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final Environment env) {
        final File dir = new File(env.basedir(), "src/test/rexsl");
        boolean valid = true;
        for (File file : this.getFiles(dir)) {
            Logger.warn(this, file.getAbsolutePath());
            final File folder = file.getParentFile();
            if (!folder.getParentFile().equals(dir)) {
                Logger.warn(this, "Incorrect rexsl folder structure");
                valid = false;
                continue;
            }
            if (!this.DIR_EXTENSIONS.containsKey(folder.getName())) {
                Logger.warn(this, "Incorrect sub directory %s", folder);
                valid = false;
                continue;
            }
            final String path = file.getAbsolutePath()
                .substring(dir.getAbsolutePath().length() + 1);
            final String ext = FilenameUtils.getExtension(path);
            if (!ext.matches(this.DIR_EXTENSIONS.get(folder.getName()))) {
                Logger.warn(this, "File %s has incorrect type/extension", file);
                valid = false;
                continue;
            }
        }
        return valid;
    }

    /**
     * Get files to iterate over.
     * @param dir Folder to getFiles from
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
