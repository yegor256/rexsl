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

import com.jcabi.aether.Classpath;
import com.jcabi.aspects.Loggable;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.util.artifact.JavaScopes;

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
     * The project, from Maven plugin.
     */
    private final transient MavenProject project;

    /**
     * The list of properties from Maven plugin.
     */
    private final transient Properties properties;

    /**
     * Location of local repo.
     */
    private transient String localRepo;

    /**
     * Shall we use runtime filtering?
     */
    private transient boolean runtimeFiltering;

    /**
     * Port number.
     */
    private transient int iport;

    /**
     * Ctor.
     * @param prj Maven project
     * @param props Properties
     */
    public MavenEnvironment(final MavenProject prj,
        final Properties props) {
        this.project = prj;
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

    /**
     * Set location of local repository.
     * @param dir The directory of the repository
     */
    @Loggable(Loggable.DEBUG)
    public void setLocalRepository(final String dir) {
        this.localRepo = dir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public File basedir() {
        return this.project.getBasedir();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public int port() {
        return this.iport;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Loggable(Loggable.DEBUG)
    public Set<File> classpath(final boolean tonly) {
        Collection<String> scopes;
        if (tonly) {
            scopes = Arrays.asList(JavaScopes.TEST);
        } else {
            scopes = Arrays.asList(JavaScopes.COMPILE, JavaScopes.PROVIDED);
        }
        return new Classpath(this.project, new File(this.localRepo), scopes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean useRuntimeFiltering() {
        return this.runtimeFiltering;
    }


}
