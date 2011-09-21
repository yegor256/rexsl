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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.filter.DependencyFilterUtils;
import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.sonatype.aether.connector.wagon.WagonProvider;

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
     * The reporter.
     */
    private final Reporter reporter;

    /**
     * The list of properties from Maven plugin.
     */
    private final Properties properties;

    /**
     * Location of local repo.
     */
    private String localRepo;

    /**
     * Ctor.
     * @param prj Maven project
     * @param rep The reporter
     * @param props Properties
     */
    public MavenEnvironment(final MavenProject prj, final Reporter rep,
        final Properties props) {
        this.project = prj;
        this.reporter = rep;
        this.properties = props;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    public File webdir() {
        return new File(this.properties.getProperty("webappDirectory"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reporter reporter() {
        return this.reporter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader classloader() {
        final List<String> paths = new ArrayList<String>();
        try {
            paths.addAll(this.project.getRuntimeClasspathElements());
        } catch (DependencyResolutionRequiredException ex) {
            throw new IllegalStateException("Failed to read classpath", ex);
        }
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
            null //this.getClass().getClassLoader()
        );
        for (URL url : loader.getURLs()) {
            this.reporter.report("ReXSL classpath: %s", url);
        }
        return loader;
    }

    /**
     * List of artifacts, which should be available in classpath.
     * @return The list of artifacts
     * @see #classloader()
     */
    private List<Artifact> artifacts() {
        final List<Artifact> artifacts = new ArrayList<Artifact>();
        for (Artifact root : this.roots()) {
            this.reporter.report(
                "%s:%s:%s",
                root.getGroupId(),
                root.getArtifactId(),
                root.getVersion()
            );
            for (Artifact dep : this.deps(root)) {
                boolean found = false;
                for (Artifact exists : artifacts) {
                    if (dep.getArtifactId().equals(exists.getArtifactId())
                        && dep.getGroupId().equals(exists.getGroupId())) {
                        found = true;
                    }
                }
                if (!found) {
                    artifacts.add(dep);
                    this.reporter.report(
                        "\t%s:%s:%s",
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
     * Create Aether repository system.
     * @return The repo system
     */
    private RepositorySystem system() {
        final DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.addService(
            RepositoryConnectorFactory.class,
            FileRepositoryConnectorFactory.class
        );
        locator.addService(
            RepositoryConnectorFactory.class,
            WagonRepositoryConnectorFactory.class
        );
        locator.setServices(
            WagonProvider.class,
            new ManualWagonProvider()
        );
        return locator.getService(RepositorySystem.class);
    }

    /**
     * List of root artifacts.
     * @return The list of root artifacts
     * @see #artifacts()
     */
    private List<Artifact> roots() {
        final List<Artifact> roots = new ArrayList<Artifact>();
        roots.add(
            new DefaultArtifact(
                "com.rexsl",
                "rexsl-maven-plugin",
                "",
                "jar",
                "1.0-SNAPSHOT"
            )
        );
        for (org.apache.maven.artifact.Artifact artf :
            this.project.getDependencyArtifacts()) {
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

    /**
     * List of transitive deps of the artifact.
     * @return The list of dependencies
     * @see #artifacts()
     */
    private List<Artifact> deps(final Artifact root) {
        final CollectRequest crq = new CollectRequest();
        crq.setRoot(new Dependency(root, JavaScopes.RUNTIME));
        for (RemoteRepository repo : this.project.getRemoteProjectRepositories()) {
            crq.addRepository(repo);
        }
        final DependencyFilter filter =
            DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME);
        final MavenRepositorySystemSession session =
            new MavenRepositorySystemSession();
        final LocalRepository local = new LocalRepository(this.localRepo);
        session.setLocalRepositoryManager(
            this.system().newLocalRepositoryManager(local)
        );
        Collection<ArtifactResult> results;
        try {
            results = this.system().resolveDependencies(
                session,
                new DependencyRequest(crq, filter)
            ).getArtifactResults();
        } catch (DependencyResolutionException ex) {
            throw new IllegalStateException(ex);
        }
        final List<Artifact> deps = new ArrayList<Artifact>();
        for (ArtifactResult res : results) {
            deps.add(res.getArtifact());
        }
        return deps;
    }

    /**
     * Private wagon provider.
     */
    private static final class ManualWagonProvider implements WagonProvider {
        /**
         * {@inheritDoc}
         */
        @Override
        public Wagon lookup(final String roleHint) throws Exception {
            if ("http".equals(roleHint)) {
                return new LightweightHttpWagon();
            }
            return null;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void release(final Wagon wagon) {
            // intentionally empty
        }

    }

}
