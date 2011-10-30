/**
 * Copyright (c) 2011, ReXSL.com
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

import com.rexsl.maven.Environment;
import com.rexsl.maven.Packer;
import com.ymock.util.Logger;
import java.io.File;
import org.apache.commons.io.FileUtils;

/**
 * Packager of JS files. All comments, spaces, and unnecessary language
 * constructs are removed.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class JsPacker implements Packer {

    /**
     * The folder where all JavaScript files should be stored.
     * @checkstyle MultipleStringLiterals (2 lines)
     */
    public static final String FOLDER = "js";

    /**
     * Extensions to process.
     * @checkstyle MultipleStringLiterals (2 lines)
     */
    public static final String[] EXTS = new String[] {"js"};

    /**
     * {@inheritDoc}
     */
    @Override
    public void pack(final Environment env) {
        final File dir = new File(env.webdir(), this.FOLDER);
        if (!dir.exists()) {
            Logger.info(this, "#pack(): %s directory is absent", dir);
            return;
        }
        for (File script : FileUtils.listFiles(dir, this.EXTS, true)) {
            // it's a temporary warning, until we implement packing
            Logger.warn(this, "#pack(): %s was NOT packed", script);
        }
    }

}
