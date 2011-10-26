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
package com.rexsl.maven.aether;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.filter.DependencyFilterUtils;

/**
 * Resolver of dependencies for one artifact.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class DepsResolver {

    /**
     * The project.
     */
    private final MavenProject project;

    /**
     * Location of local repo.
     */
    private final String localRepo;

    /**
     * Ctor.
     * @param prj The project
     * @param repo Local repository location
     */
    public DepsResolver(final MavenProject prj, final String repo) {
        this.project = prj;
        this.localRepo = repo;
    }

    /**
     * List of transitive deps of the artifact.
     * @param root The artifact to work with
     * @return The list of dependencies
     */
    public List<Artifact> deps(final Artifact root) {
        final CollectRequest crq = new CollectRequest();
        crq.setRoot(new Dependency(root, JavaScopes.RUNTIME));
        for (RemoteRepository repo
            : this.project.getRemoteProjectRepositories()) {
            crq.addRepository(repo);
        }
        final RepositorySystem system = new RepositorySystemBuilder().build();
        final DependencyFilter filter =
            DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME);
        final MavenRepositorySystemSession session =
            new MavenRepositorySystemSession();
        final LocalRepository local = new LocalRepository(this.localRepo);
        session.setLocalRepositoryManager(
            system.newLocalRepositoryManager(local)
        );
        Collection<ArtifactResult> results;
        try {
            results = system.resolveDependencies(
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

}
