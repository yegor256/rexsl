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
import java.util.Set;
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
public final class Environment {

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
    public Environment(final MavenProject prj, final Reporter rep,
        final Properties props) {
        this.project = prj;
        this.reporter = rep;
        this.properties = props;
    }

    /**
     * Set location of local repository.
     * @param url The URL of the repo (file://...)
     */
    public void setLocalRepository(final String url) {
        this.localRepo = url;
    }

    /**
     * Get basedir of the project.
     * @return The basedir
     */
    public File basedir() {
        return this.project.getBasedir();
    }

    /**
     * Get web root.
     * @return The web dir
     */
    public File webdir() {
        return new File(this.properties.getProperty("webappDirectory"));
    }

    /**
     * Get reporter.
     * @return The reporter
     */
    public Reporter reporter() {
        return this.reporter;
    }

    /**
     * Create classloader, from all artifacts available for this
     * plugin in runtime (incl. "test").
     * @return The classloader
     */
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
            this.getClass().getClassLoader()
        );
        for (URL url : loader.getURLs()) {
            this.reporter.log("ReXSL runtime classpath: %s", url);
        }
        return loader;
    }

    /**
     * List of artifacts, which should be available in classpath.
     * @return The list of artifacts
     * @see #classloader()
     */
    private List<Artifact> artifacts() {
        final Set<org.apache.maven.artifact.Artifact> roots =
            this.project.getDependencyArtifacts();
        final List<Artifact> artifacts = new ArrayList<Artifact>();
        for (org.apache.maven.artifact.Artifact root : roots) {
            final CollectRequest crq = new CollectRequest();
            crq.setRoot(
                new Dependency(
                    new DefaultArtifact(
                        root.getGroupId(),
                        root.getArtifactId(),
                        root.getClassifier(),
                        root.getType(),
                        root.getVersion()
                    ),
                    JavaScopes.RUNTIME
                )
            );
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
            for (ArtifactResult res : results) {
                artifacts.add(res.getArtifact());
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

    final class ManualWagonProvider implements WagonProvider {
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
