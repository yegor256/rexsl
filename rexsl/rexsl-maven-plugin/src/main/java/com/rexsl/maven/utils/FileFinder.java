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
package com.rexsl.maven.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;

/**
 * Convenient finder of files by extension in a given directory.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class FileFinder {

    /**
     * File extension.
     */
    @NotNull
    private final transient String extension;

    /**
     * Directory.
     */
    @NotNull
    private final transient File directory;

    /**
     * Public ctor.
     * @param dir Directory, containing files
     * @param ext File extension to search for
     */
    public FileFinder(@NotNull final File dir, @NotNull final String ext) {
        this.directory = dir;
        this.extension = ext;
    }

    /**
     * Returns {@link SortedSet} of files in an alphabetical order.
     * @return Set of files
     */
    public SortedSet<File> ordered() {
        return new TreeSet<File>(this.fetch());
    }

    /**
     * Returns {@link Set} of files in a random order.
     * @return Set of files
     */
    public Set<File> random() {
        final List<File> scripts = new ArrayList<File>(this.fetch());
        Collections.shuffle(scripts);
        return new LinkedHashSet<File>(scripts);
    }

    /**
     * Fetches files from the directory.
     * @return Collection of file found
     */
    private Collection<File> fetch() {
        return FileUtils.listFiles(
            this.directory,
            new String[] {this.extension},
            true
        );
    }

}
