/**
 * Copyright (c) 2011-2013, ReXSL.com
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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rexsl.maven.utils.PortReserver;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.slf4j.impl.StaticLoggerBinder;

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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @see <a href="http://www.slf4j.org/manual.html">SLF4J manual</a>
 */
public abstract class AbstractRexslMojo extends AbstractMojo
    implements Contextualizable {

    /**
     * Maven project, to be injected by Maven itself.
     */
    @Component
    private transient MavenProject prj;

    /**
     * The current repository/network configuration of Maven.
     */
    @Component
    private transient MavenSession session;

    /**
     * Container.
     */
    private transient PlexusContainer container;

    /**
     * Shall we skip execution?
     */
    @Parameter(property = "rexsl.skip", defaultValue = "false")
    private transient boolean skip;

    /**
     * Webapp directory.
     */
    @Parameter(
        property = "rexsl.webappDirectory",
        defaultValue = "${project.build.directory}/${project.build.finalName}"
    )
    private transient String webappDirectory;

    /**
     * TPC port to bind to (by default a random port is used).
     */
    @Parameter(property = "rexsl.port")
    private transient Integer port;

    /**
     * System variables to be set before running tests.
     *
     * <p>You define them in a similar way as in
     * <a href="http://maven.apache.org/plugins/maven-surefire-plugin/"
     * >maven-surefire-plugin</a>,
     * for example you want to reconfigure LOG4J just for the tests:
     *
     * <pre>&lt;plugin>
     *   &lt;groupId&gt;com.rexsl&lt;/groupId&gt;
     *   &lt;artifactId&gt;rexsl-maven-plugin&lt;/artifactId&gt;
     *   &lt;configuration&gt;
     *     &lt;systemPropertyVariables&gt;
     *       &lt;log4j.configuration&gt;./x.props&lt;/log4j.configuration&gt;
     *     &lt;/systemPropertyVariables&gt;
     *   &lt;/configuration&gt;
     * &lt;/plugin&gt;</pre>
     *
     * @since 0.3
     */
    @SuppressWarnings("PMD.LongVariable")
    @Parameter
    private transient Map<String, String> systemPropertyVariables;

    /**
     * Environment.
     */
    private transient MavenEnvironment environment;

    /**
     * Set Maven Project (used mostly for unit testing).
     * @param proj The project to set
     */
    @Loggable(Loggable.DEBUG)
    public final void setProject(final MavenProject proj) {
        this.prj = proj;
    }

    /**
     * Set skip option.
     * @param skp Shall we skip execution?
     */
    @Loggable(Loggable.DEBUG)
    public final void setSkip(final boolean skp) {
        this.skip = skp;
    }

    /**
     * Set webapp directory.
     * @param dir The directory
     */
    @Loggable(Loggable.DEBUG)
    public final void setWebappDirectory(final String dir) {
        this.webappDirectory = dir;
    }

    /**
     * Set repository system session.
     * @param sess The session
     */
    @Loggable(Loggable.DEBUG)
    public final void setSession(final MavenSession sess) {
        this.session = sess;
    }

    /**
     * Set systemPropertyVariables.
     * @param props Properties
     */
    @Loggable(Loggable.DEBUG)
    public final void setSystemProperties(final Map<String, String> props) {
        this.systemPropertyVariables = props;
    }

    /**
     * Get access to project.
     * @return The project
     */
    protected final MavenProject project() {
        return this.prj;
    }

    /**
     * Get access to environment.
     * @return The environment
     */
    protected final MavenEnvironment env() {
        return this.environment;
    }

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
        this.environment = new MavenEnvironment(
            this.project(), this.session, this.container, properties
        );
        if (this.port == null) {
            this.environment.setPort(new PortReserver().port());
        } else {
            this.environment.setPort(this.port);
        }
        final Properties before = this.inject();
        try {
            this.run();
        } finally {
            this.revert(before);
        }
    }

    /**
     * Run this mojo.
     * @throws MojoFailureException If some problem inside
     */
    protected abstract void run() throws MojoFailureException;

    /**
     * Sets the system properties to the argument passed.
     * @param before The properties.
     */
    private void revert(final Properties before) {
        System.setProperties(before);
    }

    /**
     * Injects system property variables and returns the properties as
     * they are before being modified in the method.
     * @return The properties before being modified
     */
    private Properties inject() {
        final Properties before = new Properties();
        before.putAll(System.getProperties());
        if (this.systemPropertyVariables != null) {
            for (final Map.Entry<String, String> var
                : this.systemPropertyVariables.entrySet()) {
                if (var.getValue() == null) {
                    Logger.warn(
                        this,
                        "System variable '%s' can't be set to NULL",
                        var.getKey()
                    );
                } else {
                    System.setProperty(var.getKey(), var.getValue());
                    Logger.info(
                        this,
                        "System variable '%s' set to '%s'",
                        var.getKey(),
                        var.getValue()
                    );
                }
            }
        }
        return before;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void contextualize(final Context context)
        throws ContextException {
        this.container = (PlexusContainer) context
            .get(PlexusConstants.PLEXUS_KEY);
    }
}
