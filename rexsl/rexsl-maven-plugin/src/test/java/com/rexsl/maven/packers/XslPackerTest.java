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
import com.rexsl.maven.EnvironmentMocker;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link XslPacker}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class XslPackerTest {

    /**
     * XslPacker can pack XSL files.
     * @throws Exception If something goes wrong inside
     * @todo #37:1h Remove comments from the output file.
     *  XSL files should be compressed (all comments and spaces removed).
     */
    @Test
    public void packsXslFile() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withTextFile(
                "src/main/webapp/xsl/layout.xsl",
                "<stylesheet><!-- some text --></stylesheet>"
        ).mock();
        final File dest = new File(env.webdir(), "xsl/layout.xsl");
        new XslPacker().pack(env);
        MatcherAssert.assertThat("file created", dest.exists());
        MatcherAssert.assertThat(
            FileUtils.readFileToString(dest),
            Matchers.equalTo("<stylesheet></stylesheet>")
        );
    }

}
