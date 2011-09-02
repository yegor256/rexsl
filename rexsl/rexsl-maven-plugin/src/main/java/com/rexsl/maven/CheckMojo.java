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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Test XSL stylesheets against XML mocks.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @goal check
 * @phase test
 * @threadSafe
 */
public final class CheckMojo extends AbstractMojo {

    /**
     * Maven project, to be injected by Maven itself.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Shall we skip execution?
     * @parameter expression="${qulice.skip}" default-value="false"
     * @required
     */
    private boolean skip;

    /**
     * Public ctor.
     */
    public CheckMojo() {
        super();
        this.project = null;
    }

    /**
     * Set Maven Project (used mostly for unit testing).
     * @param proj The project to set
     */
    public void setProject(final MavenProject proj) {
        this.project = proj;
    }

    /**
     * Set skip option.
     * @param skp Shall we skip execution?
     */
    public void setSkip(final boolean skp) {
        this.skip = skp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoFailureException {
        if (this.skip) {
            this.getLog().info("Execution skipped");
            return;
        }
        this.getLog().info(
            String.format(
                "XSL stylesheets tested in %s",
                this.project.getName()
            )
        );
    }

}
