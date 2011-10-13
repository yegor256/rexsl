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
import java.io.File;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * XhtmlOutputCheck test case.
 * @author Yegor Bugayenko (yegor@qulice.com)
 * @version $Id$
 */
public final class XhtmlOutputCheckTest {

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
     * Validate correct XML+XSL transformation.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testTruePositiveValidation() throws Exception {
        final File basedir = this.temp.newFolder("base-1");
        // @checkstyle MultipleStringLiterals (4 lines)
        Utils.copy(basedir, "src/main/webapp/xsl/layout.xsl");
        Utils.copy(basedir, "src/main/webapp/xsl/Home.xsl");
        Utils.copy(basedir, "src/test/rexsl/xml/index.xml");
        Utils.copy(basedir, "src/test/rexsl/xhtml/index.groovy");
        final Environment env = Mockito.mock(Environment.class);
        Mockito.doReturn(basedir).when(env).basedir();
        Mockito.doReturn(this.getClass().getClassLoader())
            .when(env).classloader();
        MatcherAssert.assertThat(
            new XhtmlOutputCheck().validate(env),
            Matchers.is(true)
        );
    }

    /**
     * Validate incorrect XML+XSL transformation (layout file is missed).
     * @throws Exception If something goes wrong
     */
    @Test
    public void testFalsePositiveValidation() throws Exception {
        final File basedir = this.temp.newFolder("base-2");
        // @checkstyle MultipleStringLiterals (2 lines)
        Utils.copy(basedir, "src/main/webapp/xsl/Home.xsl");
        Utils.copy(basedir, "src/test/rexsl/xml/index.xml");
        final Environment env = Mockito.mock(Environment.class);
        Mockito.doReturn(basedir).when(env).basedir();
        Mockito.doReturn(this.getClass().getClassLoader())
            .when(env).classloader();
        MatcherAssert.assertThat(
            new XhtmlOutputCheck().validate(env),
            Matchers.is(false)
        );
    }

}
