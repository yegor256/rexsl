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
package com.rexsl.maven.checks;

import com.rexsl.maven.Environment;
import com.rexsl.maven.utils.PortReserver;
import java.io.File;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * StaticCssAnalysis test case.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @version $Id$
 * @todo #56 Implement CSS stylesheet validation and enable all test methods in
 * this class.
 */
public final class StaticCssAnalysisTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Forward SLF4J to Maven Log.
     * @throws Exception If something is wrong inside
     */
    @BeforeClass
    public static void startLogging() throws Exception {
        new com.rexsl.maven.LogStarter().start();
    }

    /**
     * Validates correct CSS files.
     * @throws Exception If something goes wrong
     */
    @org.junit.Ignore
    @Test
    public void testTruePositiveValidation() throws Exception {
        final File basedir = this.temp.newFolder("base-1");
        Utils.copy(basedir, "src/main/webapp/css/valid.css");
        final Environment env = Mockito.mock(Environment.class);
        Mockito.doReturn(basedir).when(env).basedir();
        Mockito.doReturn(new PortReserver().port()).when(env).port();
        Mockito.doReturn(this.webdir(basedir)).when(env).webdir();
        MatcherAssert.assertThat(
            new StaticCssCheck().validate(env),
            Matchers.is(true)
        );
    }

    /**
     * Validates wrong CSS files.
     * @throws Exception If something goes wrong
     */
    @org.junit.Ignore
    @Test
    public void testFalsePositiveValidation() throws Exception {
        final File basedir = this.temp.newFolder("base-2");
        Utils.copy(basedir, "src/main/webapp/css/invalid.css");
        final Environment env = Mockito.mock(Environment.class);
        Mockito.doReturn(basedir).when(env).basedir();
        Mockito.doReturn(this.webdir(basedir)).when(env).webdir();
        MatcherAssert.assertThat(
            new StaticCssCheck().validate(env),
            Matchers.is(false)
        );
    }

    /**
     * Build webdir out of basedir.
     * @param basedir The basedir
     * @return The webdir
     */
    private File webdir(final File basedir) {
        return new File(basedir, "src/main/webapp");
    }

}
