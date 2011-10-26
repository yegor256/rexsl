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

import com.rexsl.maven.aether.DepsResolver;
import com.ymock.util.Logger;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * Environment proxy, between Maven plugin and checks.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class MavenEnvironment implements Environment {

    /**
     * The project, from Maven plugin.
     */
    private final MavenProject project;

    /**
     * The list of properties from Maven plugin.
     */
    private final Properties properties;

    /**
     * Location of local repo.
     */
    private String localRepo;

    /**
     * Shall we use runtime filtering?
     */
    private boolean runtimeFiltering;

    /**
     * Port number.
     */
    private Integer port;

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
    public void setPort(final Integer prt) {
        this.port = prt;
    }

    /**
     * Shall we do runtime filtering?
     * @param filtering Shall we?
     */
    public void setRuntimeFiltering(final boolean filtering) {
        this.runtimeFiltering = filtering;
    }

    /**
     * Set location of local repository.
     * @param dir The directory of the repository
     */
    public void setLocalRepository(final String dir) {
        this.localRepo = dir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File basedir() {
        return this.project.getBasedir();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer port() {
        return this.port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File webdir() {
        final String dir = this.properties.getProperty("webappDirectory");
        if (dir == null) {
            throw new IllegalStateException("webappDirectory property not set");
        }
        return new File(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader classloader() {
        final List<String> paths = new ArrayList<String>();
        for (Artifact artifact : this.artifacts()) {
            paths.add(artifact.getFile().getPath());
        }
        final List<URL> urls = new ArrayList<URL>();
        for (String path : paths) {
            try {
                urls.add(new File(path).toURI().toURL());
            } catch (java.net.MalformedURLException ex) {
                throw new IllegalStateException("Failed to build URL", ex);
            }
        }
        final URLClassLoader loader = new URLClassLoader(
            urls.toArray(new URL[] {}),
            this.getClass().getClassLoader()
        );
        final List<String> names = new ArrayList<String>();
        for (URL url : loader.getURLs()) {
            names.add(url.toString());
        }
        Logger.debug(
            this,
            "ReXSL classpath: %s",
            StringUtils.join(names, "\n")
        );
        return loader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean useRuntimeFiltering() {
        return this.runtimeFiltering;
    }

    /**
     * List of artifacts, which should be available in classpath.
     * @return The list of artifacts
     * @see #classloader()
     */
    private List<Artifact> artifacts() {
        final List<Artifact> artifacts = new ArrayList<Artifact>();
        final DepsResolver resolver =
            new DepsResolver(this.project, this.localRepo);
        Logger.debug(this, "Full tree of artifacts in classpath:");
        for (Artifact root : this.roots()) {
            Logger.debug(
                this,
                "  %s:%s:%s",
                root.getGroupId(),
                root.getArtifactId(),
                root.getVersion()
            );
            for (Artifact dep : resolver.deps(root)) {
                boolean found = false;
                for (Artifact exists : artifacts) {
                    if (dep.getArtifactId().equals(exists.getArtifactId())
                        && dep.getGroupId().equals(exists.getGroupId())) {
                        found = true;
                    }
                }
                if (!found) {
                    artifacts.add(dep);
                    Logger.debug(
                        this,
                        "    %s:%s:%s",
                        dep.getGroupId(),
                        dep.getArtifactId(),
                        dep.getVersion()
                    );
                }
            }
        }
        return artifacts;
    }

    /**
     * List of root artifacts.
     * @return The list of root artifacts
     * @see #artifacts()
     * @todo #7 The implementation is very rough now. We should not specify
     *  coordinates of REXSL-MAVEN-PLUGIN explicitly here. Somehow we should
     *  get this information in runtime.
     */
    private List<Artifact> roots() {
        final List<Artifact> roots = new ArrayList<Artifact>();
        for (org.apache.maven.artifact.Artifact artf
            : this.project.getDependencyArtifacts()) {
            roots.add(
                new DefaultArtifact(
                    artf.getGroupId(),
                    artf.getArtifactId(),
                    artf.getClassifier(),
                    artf.getType(),
                    artf.getVersion()
                )
            );
        }
        return roots;
    }

}
