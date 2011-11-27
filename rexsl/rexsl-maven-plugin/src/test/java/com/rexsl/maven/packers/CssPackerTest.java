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
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * Test case for {@link CssPacker}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class CssPackerTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    /**
     * Forward SLF4J to Maven Log.
     * @throws Exception If something is wrong inside
     */
    @BeforeClass
    public static void startLogging() throws Exception {
        new com.rexsl.maven.LogStarter().start();
    }

    /**
     * Simple packaging.
     * @throws Exception If something goes wrong inside
     * @todo #6 This test doesn't work because the Packer is not implemented.
     *  We should package CSS files and compress them.
     */
    @Test
    public void testCssPacking() throws Exception {
        final Environment env = Mockito.mock(Environment.class);
        final File basedir = this.temp.newFolder("basedir");
        Mockito.doReturn(basedir).when(env).basedir();
        final File webdir = new File(basedir, "target/webdir");
        Mockito.doReturn(webdir).when(env).webdir();
        final File src = new File(basedir, "src/main/webapp/css/screen.css");
        final File dest = new File(webdir, "css/screen.css");
        src.getParentFile().mkdirs();
        FileUtils.writeStringToFile(
            src,
            "\u002F* test */ a: { color: red; }"
        );
        final Packer packer = new CssPacker();
        packer.pack(env);
        MatcherAssert.assertThat(dest.exists(), Matchers.equalTo(true));
        // MatcherAssert.assertThat(
        //     FileUtils.readFileToString(dest),
        //     Matchers.equalTo("a: {color: red;}")
        // );
    }

}
