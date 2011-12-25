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

import java.util.HashSet;
import java.util.Set;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.LocalRepository;

/**
 * Test case for {@link CheckMojo}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CheckMojo.class, ChecksProvider.class })
public final class CheckMojoTest {

    /**
     * CheckMojo can cancel execution on "skip" option.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void skipOptionCancelsExecution() throws Exception {
        final CheckMojo mojo = new CheckMojo();
        mojo.setSkip(true);
        final Log log = Mockito.mock(Log.class);
        mojo.setLog(log);
        mojo.execute();
        Mockito.verify(log).info("execution skipped because of 'skip' option");
    }

    /**
     * CheckMojo rejects non-WAR projects.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = IllegalStateException.class)
    public void rejectsNonWarPackaging() throws Exception {
        final CheckMojo mojo = new CheckMojo();
        final MavenProject project = new MavenProjectMocker()
            .withPackaging("jar")
            .mock();
        mojo.setProject(project);
        mojo.execute();
    }

    /**
     * CheckMojo can validate using a collection of checks.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void onePositiveCheckIsExecuted() throws Exception {
        PowerMockito.mockStatic(ChecksProvider.class);
        final ChecksProvider provider =
            PowerMockito.mock(ChecksProvider.class);
        final Set<Check> checks = new HashSet<Check>();
        final Check check = new CheckMocker().withResult(true).mock();
        checks.add(check);
        Mockito.doReturn(checks).when(provider).all();
        PowerMockito.whenNew(ChecksProvider.class).withNoArguments()
            .thenReturn(provider);
        final CheckMojo mojo = this.mojo();
        final MavenProject project = new MavenProjectMocker().mock();
        mojo.setProject(project);
        mojo.execute();
        Mockito.verify(provider).all();
        Mockito.verify(check).validate(Mockito.any(Environment.class));
    }

    /**
     * Create and return a MOJO.
     * @return The mojo
     */
    private CheckMojo mojo() {
        final CheckMojo mojo = new CheckMojo();
        mojo.setWebappDirectory("");
        final RepositorySystemSession session =
            Mockito.mock(RepositorySystemSession.class);
        final LocalRepository repo = new LocalRepository(".");
        Mockito.doReturn(repo).when(session).getLocalRepository();
        mojo.setSession(session);
        mojo.setLog(new SystemStreamLog());
        return mojo;
    }

}
