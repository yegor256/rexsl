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

import java.util.Properties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystemSession;

/**
 * Test entire project against RESTful principles.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @goal check
 * @phase verify
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
     * The current repository/network configuration of Maven.
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession session;

    /**
     * Shall we skip execution?
     * @parameter expression="${rexsl.skip}" default-value="false"
     * @required
     */
    private boolean skip;

    /**
     * Webapp directory.
     * @parameter expression="${rexsl.webappDirectory}" default-value="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private String webappDirectory;

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
     * Set webapp directory.
     * @param dir The directory
     */
    public void setWebappDirectory(final String dir) {
        this.webappDirectory = dir;
    }

    /**
     * Set repository system session.
     * @param sess The session
     */
    public void setSession(final RepositorySystemSession sess) {
        this.session = sess;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoFailureException {
        org.slf4j.impl.StaticLoggerBinder.getSingleton()
            .setMavenLog(this.getLog());
        if (this.skip) {
            this.getLog().info("execution skipped because of 'skip' option");
            return;
        }
        if (!"war".equals(this.project.getPackaging())) {
            throw new IllegalStateException("project packaging is not WAR");
        }
        final Properties properties = new Properties();
        properties.setProperty("webappDirectory", this.webappDirectory);
        final Environment env = new MavenEnvironment(
            this.project,
            new MavenReporter(this.getLog()),
            properties
        );
        env.setLocalRepository(
            this.session.getLocalRepository().getBasedir().getPath()
        );
        for (Check check : new ChecksProvider().all()) {
            if (!check.validate(env)) {
                throw new MojoFailureException(
                    String.format(
                        "%s check failed",
                        check.getClass().getName()
                    )
                );
            }
        }
        this.getLog().info(
            String.format(
                "All ReXSL checks passed in '%s'",
                this.project.getName()
            )
        );
    }

}
