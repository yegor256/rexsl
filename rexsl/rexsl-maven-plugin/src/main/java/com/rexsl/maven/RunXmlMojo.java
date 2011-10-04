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

import com.rexsl.maven.utils.Grizzly;
import com.rexsl.maven.utils.PortReserver;
import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Run xml files in web.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @goal run-xml
 * @threadSafe
 */
public final class RunXmlMojo extends AbstractMojo {

    /**
     * Maven project, to be injected by Maven itself.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Webapp directory.
     * @parameter expression="${rexsl.webappDirectory}" default-value="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private String webapp;

    /**
     * Public ctor.
     */
    public RunXmlMojo() {
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
     * Set webapp directory.
     * @param dir The directory
     */
    public void setWebapp(final String dir) {
        this.webapp = dir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoFailureException {
        org.slf4j.impl.StaticLoggerBinder.getSingleton()
            .setMavenLog(this.getLog());
        if (!"war".equals(this.project.getPackaging())) {
            throw new IllegalStateException("project packaging is not WAR");
        }
        if (!new File(this.webapp).exists()) {
            throw new IllegalStateException(
                String.format(
                    "Directory '%s' doesn't exist, package the project first",
                    this.webapp
                )
            );
        }
        this.getLog().info("Running XML files over " + this.webapp);
        final Integer port = new PortReserver().port();
        final Grizzly grizzly = Grizzly.start(new File(this.webapp), port);
        this.getLog().info("Web front available at http://localhost:" + port);
        this.getLog().info("Press Ctrl-C to stop...");
        try {
            Thread.sleep(0);
        } catch (java.lang.InterruptedException ex) {
            grizzly.stop();
        }
    }

}
