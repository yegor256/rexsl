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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;

/**
 * Contains methods to find and sort scripts.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @version $Id: ScriptsFinder.java 464 2011-12-12 09:30:18Z guard $
 */
public final class ScriptsFinder {

    /**
     * Scripts mask.
     */
    private static final String MASK = "groovy";

    /**
     * Directory, containing scripts.
     */
    private final transient File directory;

    /**
     * Creates new instance of <code>ScriptsFinder</code>.
     * @param dir Directory, containing scripts.
     */
    public ScriptsFinder(final File dir) {
        this.directory = dir;
    }

    /**
     * Fetches scripts from the input directory.
     * @return Collection of scripts.
     */
    private Collection<File> fetch() {
        final String[] extensions = new String[]{this.MASK};
        final Collection<File> scripts = FileUtils.listFiles(
            this.directory,
            extensions,
            true);
        return scripts;
    }

    /**
     * Returns <code>Set</code> of scripts in the alphabetical order.
     * @return Set of scripts in the alphabetical order.
     */
    public Set<File> ordered() {
        final Collection<File> scripts = this.fetch();
        return new TreeSet<File>(scripts);
    }

    /**
     * Returns <code>Set</code> of scripts in the random order.
     * @return Set of scripts in the random order.
     * @todo #46:1h Add test, checking random order.
     */
    public Set<File> random() {
        final List<File> scripts = (List<File>) this.fetch();
        Collections.shuffle(scripts);
        return new LinkedHashSet<File>(scripts);
    }
}
