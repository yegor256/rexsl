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

import com.jcabi.aether.Aether;
import com.jcabi.log.Logger;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

/**
 * Environment proxy, between Maven plugin and checks.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
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
    public void setPort(final int prt) {
        this.iport = prt;
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
    public int port() {
        return this.iport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    public Set<File> classpath(final boolean tonly) {
        final Set<String> paths = new LinkedHashSet<String>();
        try {
            paths.addAll(this.project.getTestClasspathElements());
        } catch (DependencyResolutionRequiredException ex) {
            throw new IllegalStateException("Failed to read classpath", ex);
        }
        for (Artifact artifact : this.artifacts(tonly)) {
            paths.add(artifact.getFile().getPath());
        }
        final Set<File> files = new LinkedHashSet<File>();
        for (String path : paths) {
            files.add(new File(path));
            Logger.debug(
                this,
                "ReXSL classpath: %s",
                path
            );
        }
        return files;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean useRuntimeFiltering() {
        return this.runtimeFiltering;
    }

    /**
     * Set of unique artifacts, which should be available in classpath.
     *
     * <p>This method gets a full list of artifacts of the project,
     * including their transitive dependencies. The parameter of the method
     * determines whether we need "test" only artifacts, or all of them without
     * exceptions.
     *
     * <p>To get the list of transitive dependencies we're using
     * <a href="http://www.jcabi.com/jcabi-aether">jcabi-aether</a> toolkit.
     *
     * @param tonly We need artifacts in "test" scope only?
     * @return The set of artifacts
     * @see #classloader()
     */
    private Set<Artifact> artifacts(final boolean tonly) {
        final Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
        final Aether aether = new Aether(this.project, this.localRepo);
        Logger.debug(this, "Full tree of artifacts in classpath:");
        for (RootArtifact root : this.roots(tonly)) {
            Logger.debug(this, "  %s", root);
            for (Artifact dep : this.deps(aether, root)) {
                boolean found = false;
                for (Artifact exists : artifacts) {
                    if (dep.getArtifactId().equals(exists.getArtifactId())
                        && dep.getGroupId().equals(exists.getGroupId())
                        && dep.getClassifier().equals(exists.getClassifier())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    Logger.debug(
                        this,
                        "    %s:%s:%s:%s (duplicate, ignored)",
                        dep.getGroupId(),
                        dep.getArtifactId(),
                        dep.getClassifier(),
                        dep.getVersion()
                    );
                    continue;
                }
                if (root.excluded(dep)) {
                    Logger.debug(
                        this,
                        "    %s:%s:%s:%s (excluded)",
                        dep.getGroupId(),
                        dep.getArtifactId(),
                        dep.getClassifier(),
                        dep.getVersion()
                    );
                    continue;
                }
                artifacts.add(dep);
                Logger.debug(
                    this,
                    "    %s:%s:%s:%s",
                    dep.getGroupId(),
                    dep.getArtifactId(),
                    dep.getClassifier(),
                    dep.getVersion()
                );
            }
        }
        return artifacts;
    }

    /**
     * Set of unique root artifacts.
     *
     * <p>The method is getting a list of artifacts from Maven Project, without
     * their transitive dependencies (that's why they are called "root"
     * artifacts). The argument of this method determines whether we're
     * interested in artifacts in "test" scope only ({@code TRUE}) or we
     * would like to get a full list of artifacts ({@code FALSE}).
     *
     * @param tonly Are interested in artifacts in "test" scope only?
     * @return The set of root artifacts
     * @see #artifacts(boolean)
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<RootArtifact> roots(final boolean tonly) {
        final Set<RootArtifact> roots = new LinkedHashSet<RootArtifact>();
        for (Dependency dep : this.project.getDependencies()) {
            if (!org.apache.maven.artifact.Artifact.SCOPE_TEST
                .equals(dep.getScope()) && tonly) {
                continue;
            }
            roots.add(
                new RootArtifact(
                    new DefaultArtifact(
                        dep.getGroupId(),
                        dep.getArtifactId(),
                        dep.getClassifier(),
                        dep.getType(),
                        dep.getVersion()
                    ),
                    dep.getExclusions()
                )
            );
        }
        return roots;
    }

    /**
     * Get all deps of a root artifact.
     * @param aether The aether to use
     * @param root The root
     * @return The list of artifacts
     * @see #artifacts(boolean)
     */
    private Collection<Artifact> deps(final Aether aether,
        final RootArtifact root) {
        Collection<Artifact> deps;
        try {
            deps = aether.resolve(root.artifact(), JavaScopes.RUNTIME);
        } catch (DependencyResolutionException ex) {
            throw new IllegalStateException(
                String.format("Failed to resolve '%s'", root),
                ex
            );
        }
        return deps;
    }

}
