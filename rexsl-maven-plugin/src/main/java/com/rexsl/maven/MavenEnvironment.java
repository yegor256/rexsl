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

import com.jcabi.aether.MavenClasspath;
import com.jcabi.aspects.Loggable;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyGraphBuilder;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * Environment proxy, between Maven plugin and checks.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class MavenEnvironment implements Environment {

    /**
     * Property name, with webappDirectory inside.
     * @since 0.3.8
     */
    public static final String WEBAPP_DIR = "webappDirectory";

    /**
     * Maven test scope name.
     */
    public static final String TEST_SCOPE = "test";

    /**
     * The project, from Maven plugin.
     */
    private final transient MavenProject project;

    /**
     * The list of properties from Maven plugin.
     */
    private final transient Properties properties;

    /**
     * Shall we use runtime filtering?
     */
    private transient boolean runtimeFiltering;

    /**
     * Port number.
     */
    private transient int iport;

    /**
     * Container.
     */
    private final transient PlexusContainer container;

    /**
     * The current repository/network configuration of Maven.
     */
    private final transient MavenSession session;

    /**
     * Ctor.
     * @param prj Maven project
     * @param sess Maven session
     * @param cntnr Plexus container
     * @param props Properties
     * @checkstyle ParameterNumberCheck (4 lines)
     */
    public MavenEnvironment(
        final MavenProject prj, final MavenSession sess,
        final PlexusContainer cntnr, final Properties props
    ) {
        this.project = prj;
        this.session = sess;
        this.container = cntnr;
        this.properties = props;
    }

    /**
     * Set port number or set default.
     * @param prt The port number
     */
    @Loggable(Loggable.DEBUG)
    public void setPort(final int prt) {
        this.iport = prt;
    }

    /**
     * Shall we do runtime filtering?
     * @param filtering Shall we?
     */
    @Loggable(Loggable.DEBUG)
    public void setRuntimeFiltering(final boolean filtering) {
        this.runtimeFiltering = filtering;
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public File basedir() {
        return this.project.getBasedir();
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public int port() {
        return this.iport;
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public File webdir() {
        final String dir = this.properties
            .getProperty(MavenEnvironment.WEBAPP_DIR);
        if (dir == null) {
            throw new IllegalStateException("webappDirectory property not set");
        }
        return new File(dir);
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Loggable(Loggable.DEBUG)
    public Set<File> classpath(final boolean tonly) {
        final Collection<String> scopes;
        if (tonly) {
            scopes = Collections.singletonList(MavenEnvironment.TEST_SCOPE);
        } else {
            scopes = Arrays.asList(
                MavenEnvironment.TEST_SCOPE,
                "compile",
                "provided",
                "runtime",
                "system"
            );
        }
        try {
            return new MavenClasspath(
                (DefaultDependencyGraphBuilder) this.container.lookup(
                    DependencyGraphBuilder.class.getCanonicalName()
                ),
                this.session, scopes);
        } catch (ComponentLookupException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public boolean useRuntimeFiltering() {
        return this.runtimeFiltering;
    }
}
