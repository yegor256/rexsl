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
package com.rexsl.maven;

import java.io.File;
import java.util.Properties;
import org.apache.maven.project.MavenProject;
import org.mockito.Mockito;

/**
 * Mocker of {@link MavenProject}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class MavenProjectMocker {

    /**
     * The mock.
     */
    private final transient MavenProject project =
        Mockito.mock(MavenProject.class);

    /**
     * Public ctor.
     */
    public MavenProjectMocker() {
        this.withPackaging("war");
    }

    /**
     * With this packaging name.
     * @param pkg The name
     * @return This object
     */
    public MavenProjectMocker withPackaging(final String pkg) {
        Mockito.doReturn(pkg).when(this.project).getPackaging();
        return this;
    }

    /**
     * With this basedir.
     * @param basedir The name
     * @return This object
     */
    public MavenProjectMocker withBasedir(final File basedir) {
        Mockito.doReturn(basedir).when(this.project).getBasedir();
        return this;
    }

    /**
     * With these properties.
     * @param props The properties
     * @return This object
     */
    public MavenProjectMocker withProperties(final Properties props) {
        Mockito.doReturn(props).when(this.project).getProperties();
        return this;
    }

    /**
     * Mock it.
     * @return Mocked project
     */
    public MavenProject mock() {
        return this.project;
    }

}
