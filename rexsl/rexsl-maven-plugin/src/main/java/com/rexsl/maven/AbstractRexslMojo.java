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

import com.jcabi.log.Logger;
import com.rexsl.maven.utils.PortReserver;
import java.util.Properties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.slf4j.impl.StaticLoggerBinder;
import org.sonatype.aether.RepositorySystemSession;

/**
 * Abstract mojo.
 *
 * <p>We import {@link StaticLoggerBinder} from SLF4J package, but this
 * class actually is not from slf4j artifact. Instead, we are using
 * {@code com.rempl.plugins:rempl-maven-plugin} artifact, where this class
 * is defined. Additional information about how exactly this reloading
 * works can be found at SLF4J documentation. This implementation of
 * {@link StaticLoggerBinder} forwards all SLF4J calls to Maven log.
 *
 * <p>This class is public because Maven needs it this way.
 *
 * <p>The class is NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @see <a href="http://www.slf4j.org/manual.html">SLF4J manual</a>
 */
public abstract class AbstractRexslMojo extends AbstractMojo {

    /**
     * Maven project, to be injected by Maven itself.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private transient MavenProject iproject;

    /**
     * The current repository/network configuration of Maven.
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private transient RepositorySystemSession session;

    /**
     * Shall we skip execution?
     * @parameter expression="${rexsl.skip}" default-value="false"
     * @required
     */
    private transient boolean skip;

    /**
     * Webapp directory.
     * @parameter expression="${rexsl.webappDirectory}" default-value="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private transient String webappDirectory;

    /**
     * TPC port to bind to (by default a random port is used).
     * @parameter expression="${rexsl.port}"
     */
    private transient Integer port;

    /**
     * Environment.
     */
    private transient MavenEnvironment environment;

    /**
     * Set Maven Project (used mostly for unit testing).
     * @param proj The project to set
     */
    public final void setProject(final MavenProject proj) {
        this.iproject = proj;
    }

    /**
     * Set skip option.
     * @param skp Shall we skip execution?
     */
    public final void setSkip(final boolean skp) {
        this.skip = skp;
    }

    /**
     * Set webapp directory.
     * @param dir The directory
     */
    public final void setWebappDirectory(final String dir) {
        this.webappDirectory = dir;
    }

    /**
     * Set repository system session.
     * @param sess The session
     */
    public final void setSession(final RepositorySystemSession sess) {
        this.session = sess;
    }

    /**
     * Get access to project.
     * @return The project
     */
    protected final MavenProject project() {
        return this.iproject;
    }

    /**
     * Get access to environment.
     * @return The environment
     */
    protected final MavenEnvironment env() {
        return this.environment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void execute() throws MojoFailureException {
        StaticLoggerBinder.getSingleton().setMavenLog(this.getLog());
        if (this.skip) {
            Logger.info(this, "execution skipped because of 'skip' option");
            return;
        }
        if (!"war".equals(this.project().getPackaging())) {
            throw new IllegalStateException("project packaging is not WAR");
        }
        final Properties properties = new Properties();
        properties.setProperty(
            MavenEnvironment.WEBAPP_DIR,
            this.webappDirectory
        );
        this.environment = new MavenEnvironment(this.project(), properties);
        if (this.session != null) {
            this.environment.setLocalRepository(
                this.session.getLocalRepository().getBasedir().getPath()
            );
        }
        if (this.port == null) {
            this.environment.setPort(new PortReserver().port());
        } else {
            this.environment.setPort(this.port);
        }
        this.run();
    }

    /**
     * Run this mojo.
     * @throws MojoFailureException If some problem inside
     */
    protected abstract void run() throws MojoFailureException;

}
