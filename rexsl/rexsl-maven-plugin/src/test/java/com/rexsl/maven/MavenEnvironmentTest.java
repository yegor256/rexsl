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
package com.rexsl.maven;

import java.io.File;
import java.util.Properties;
import org.apache.maven.project.MavenProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link MavenEnvironment}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class MavenEnvironmentTest {

    /**
     * Forward SLF4J to Maven Log.
     * @throws Exception If something is wrong inside
     */
    @BeforeClass
    public static void startLogging() throws Exception {
        new com.rexsl.maven.LogMocker().mock();
    }

    /**
     * MavenEnvironment can return pre-set props.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void returnsStaticProperties() throws Exception {
        final MavenProject project = Mockito.mock(MavenProject.class);
        final File basedir = Mockito.mock(File.class);
        Mockito.doReturn(basedir).when(project).getBasedir();
        final Properties props = new Properties();
        final File webdir = Mockito.mock(File.class);
        Mockito.doReturn("some-dir").when(webdir).getPath();
        props.setProperty("webappDirectory", webdir.getPath());
        final Environment env = new MavenEnvironment(project, props);
        MatcherAssert.assertThat(env.basedir(), Matchers.equalTo(basedir));
        MatcherAssert.assertThat(
            env.webdir().getPath(),
            Matchers.equalTo(webdir.getPath())
        );
    }

    /**
     * MavenEnvironment can build a classloader.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void classloaderIsBuilt() throws Exception {
        final MavenProject project = Mockito.mock(MavenProject.class);
        final Properties props = new Properties();
        new MavenEnvironment(project, props);
        // todo...
    }

}
