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
package com.rexsl.maven.packers;

import com.rexsl.maven.Packer;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provider of a collection of all packers.
 *
 * <p>Packers are executed in the order they are listed here.
 *
 * <h3>CssPacker</h3>
 *
 * <p>All files from {@code src/main/webapp/css} (CSS stylesheets) are
 * compressed using YUI compressor. Then, they are
 * saved to {@code target/../css}.
 *
 * <h3>JsPacker</h3>
 *
 * <p>All files from {@code src/main/webapp/js} (JavaScript files) are
 * compressed using YUI compressor. Then, they are
 * saved to {@code target/../js}.
 *
 * <h3>XslPacker</h3>
 *
 * <p>All files from {@code src/main/webapp/xsl} (XSL stylesheets) are
 * compressed as XML documents and all comments are removed. Then, they are
 * saved to {@code target/../xsl}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class PackersProvider {

    /**
     * Get full collection of packers.
     * @return List of packers
     */
    public Set<Packer> all() {
        final Set<Packer> packers = new LinkedHashSet<Packer>();
        packers.add(new CssPacker());
        packers.add(new JsPacker());
        packers.add(new XslPacker());
        return packers;
    }

}
