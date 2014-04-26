/**
 * Copyright (c) 2011-2014, ReXSL.com
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
package com.rexsl.maven.utils;

import java.io.File;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;

/**
 * Convenience class for working with file filters.
 *
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @author Simon Njenga
 * @version $Id$
 */
public final class Sources {

    /**
     * The Version Control System currently in use. It is recommended to change
     * this to either ".git", ".svn", "CVS" e.t.c to the one in use.
     * Changing this value also requires an additional change to be be done at
     * {@link BinaryFilesCheckTest#validatesTextFile()} so as to prevent Unit
     * tests from failing. Either of the following values is to be substituted
     * accordingly ".git/index", ".svn/entries", "CVS/Entries" e.t.c
     */
    private static final String VERSION_CONTROL = ".git";

    /**
     * File directories.
     */
    @NotNull
    private final transient File directories;

    /**
     * Public ctor.
     * @param dir The file directory to read from
     */
    public Sources(@NotNull final File dir) {
        this.directories = dir;
    }

    /**
     * Get files, recursively while ignoring/excluding GIT directories.
     * @return Collection of files
     * @see #VERSION_CONTROL
     */
    public Collection<File> files() {
        return FileUtils.listFiles(
            this.directories,
            HiddenFileFilter.VISIBLE,
            new AndFileFilter(
                HiddenFileFilter.VISIBLE,
                new NotFileFilter(new NameFileFilter(VERSION_CONTROL))
            )
        );
    }
}
