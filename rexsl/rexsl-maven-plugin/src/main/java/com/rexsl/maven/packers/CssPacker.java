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

import com.yahoo.platform.yui.compressor.CssCompressor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Packager of CSS files.
 *
 * <p>All comments and unnecessary spaces are removed.
 *
 * <p>Since this class is NOT public its documentation is not available online.
 * All details of this check should be explained in the JavaDoc of
 * {@link PackersProvider}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class CssPacker extends AbstractPacker {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String extension() {
        return "css";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void pack(final File src, final File dest) throws IOException {
        final Reader input = new InputStreamReader(
            new FileInputStream(src),
            CharEncoding.UTF_8
        );
        try {
            final Writer output = new OutputStreamWriter(
                new FileOutputStream(dest),
                CharEncoding.UTF_8
            );
            try {
                new CssCompressor(input).compress(output, -1);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
