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
package com.rexsl.maven.packers;

import com.jcabi.log.Logger;
import com.rexsl.maven.Environment;
import com.rexsl.maven.Filter;
import com.rexsl.maven.Packer;
import com.rexsl.maven.utils.FileFinder;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import javax.validation.constraints.NotNull;

/**
 * Abstract packer.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
abstract class AbstractPacker implements Packer {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public final void pack(@NotNull final Environment env,
        @NotNull final Filter filter) {
        final File srcdir = new File(
            env.basedir(),
            Logger.format("src/main/webapp/%s", this.extension())
        );
        final File destdir = this.dir(env);
        if (srcdir.exists()) {
            for (File src : new FileFinder(srcdir, this.extension()).random()) {
                final File dest = new File(destdir, src.getName());
                try {
                    this.pack(filter.filter(src), dest);
                } catch (IOException ex) {
                    throw new IllegalArgumentException(ex);
                }
            }
        } else {
            Logger.info(this, "#pack(): %s directory is absent", srcdir);
        }
    }

    /**
     * Get extension of files (and directory name).
     * @return The suffix
     */
    protected abstract String extension();

    /**
     * Pack one file from source to destination.
     * @param src Source file
     * @param dest Destination file
     * @throws IOException If some IO problem inside
     */
    protected abstract void pack(Reader src, File dest) throws IOException;

    /**
     * Prepare and return destination dir (creates such a directory if it's
     * absent and report this operation to the log).
     * @param env The environment
     * @return The directory ready to save files
     */
    private File dir(final Environment env) {
        final File dir = new File(env.webdir(), this.extension());
        if (dir.mkdirs()) {
            Logger.info(this, "#dir(): %s directory created", dir);
        } else {
            Logger.info(this, "#dir(): %s directory already exists", dir);
        }
        return dir;
    }

}
