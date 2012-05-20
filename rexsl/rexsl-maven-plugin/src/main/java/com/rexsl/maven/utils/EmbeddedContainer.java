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
package com.rexsl.maven.utils;

import com.jcabi.log.Logger;
import com.rexsl.core.XslResolver;
import com.rexsl.maven.Environment;
import java.io.File;
import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.DispatcherType;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Start/stop grizzly container.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (3 lines)
 */
public final class EmbeddedContainer {

    /**
     * The server just started.
     */
    private final transient Server server;

    /**
     * Private ctor.
     * @param srv The server just started
     */
    private EmbeddedContainer(final Server srv) {
        this.server = srv;
    }

    /**
     * Create and start Grizzly container.
     *
     * <p>{@code ctx.setParentLoaderPriority(false)} is called because of
     * a classloading conflict between Maven classloader and
     * Jetty WebApp classloader.
     *
     * @param env The environment
     * @return The container
     * @see <a href="http://docs.codehaus.org/display/JETTY/Classloading">Jetty Classloading</a>
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public static EmbeddedContainer start(final Environment env) {
        if (!env.webdir().exists()) {
            throw new IllegalArgumentException(
                Logger.format(
                    // @checkstyle LineLength (1 line)
                    "Directory %s is absent, maybe you forgot to 'package' the project?",
                    env.webdir()
                )
            );
        }
        final Server server = new Server(env.port());
        final WebAppContext ctx = new WebAppContext();
        ctx.setParentLoaderPriority(false);
        if (env.useRuntimeFiltering()) {
            ctx.addFilter(
                RuntimeFilter.class,
                "/*",
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR)
            );
        }
        for (ConcurrentMap.Entry<String, String> entry
            : EmbeddedContainer.params(env).entrySet()) {
            ctx.setInitParameter(entry.getKey(), entry.getValue());
        }
        ctx.setWar(env.webdir().getPath());
        ctx.addEventListener(new RuntimeListener());
        server.setHandler(ctx);
        Policy.setPolicy(new EmbeddedContainer.FreePolicy());
        ctx.setExtraClasspath(EmbeddedContainer.testClasspath(env));
        try {
            server.start();
        // @checkstyle IllegalCatch (1 line)
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to start Jetty", ex);
        }
        EmbeddedContainer.setup(env);
        return new EmbeddedContainer(server);
    }

    /**
     * Stop this container.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void stop() {
        try {
            this.server.stop();
        // @checkstyle IllegalCatch (1 line)
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to stop Jetty", ex);
        }
    }

    /**
     * Build set of params for embedded container.
     * @param env Environment
     * @return The params
     */
    private static ConcurrentMap<String, String> params(final Environment env) {
        final List<String> folders = new ArrayList<String>();
        folders.add(new File(env.basedir(), "src/main/webapp").getPath());
        folders.add(new File(env.basedir(), "src/test/rexsl").getPath());
        final ConcurrentMap<String, String> params =
            new ConcurrentHashMap<String, String>();
        params.put(
            RuntimeEnvironment.BASEDIR_PARAM,
            env.basedir().getAbsolutePath()
        );
        params.put(
            RuntimeEnvironment.WEBDIR_PARAM,
            env.webdir().getAbsolutePath()
        );
        params.put(
            RuntimeEnvironment.PORT_PARAM,
            String.valueOf(env.port())
        );
        params.put(
            RuntimeFilter.FOLDERS,
            StringUtils.join(folders, ";")
        );
        params.put(
            XslResolver.XSD_FOLDER,
            new File(env.basedir(), "src/test/rexsl/xsd").getPath()
        );
        return params;
    }

    /**
     * Run setup Groovy scripts.
     * @param env The environment
     */
    private static void setup(final Environment env) {
        final File dir = new File(env.basedir(), "src/test/rexsl/setup");
        if (dir.exists()) {
            final GroovyExecutor exec = new GroovyExecutor(
                env,
                new BindingBuilder(env).build()
            );
            final FileFinder finder = new FileFinder(dir, "groovy");
            for (File script : finder.ordered()) {
                Logger.info(EmbeddedContainer.class, "Running '%s'...", script);
                try {
                    exec.execute(script);
                } catch (com.rexsl.maven.utils.GroovyException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        } else {
            Logger.info(
                EmbeddedContainer.class,
                "%s directory is absent, no setup scripts to run",
                dir
            );
        }
    }

    /**
     * Build and return test classpath, for WebAppContext.
     * @param env The environment
     * @return Extra classpath to be used in tests
     * @see #start(Environment)
     */
    private static String testClasspath(final Environment env) {
        final List<String> urls = new ArrayList<String>();
        for (File path : env.classpath(true)) {
            if (path.isDirectory()) {
                urls.add(Logger.format("%s/", path.getAbsolutePath()));
            } else {
                urls.add(path.getAbsolutePath());
            }
        }
        Logger.debug(
            EmbeddedContainer.class,
            "#testClasspath(%s): %[list]s",
            env.getClass().getName(),
            urls
        );
        return StringUtils.join(urls, ",");
    }

    /**
     * Custom security policy that allows access to classloader.
     */
    private static final class FreePolicy extends Policy {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean implies(final ProtectionDomain domain,
            final Permission perm) {
            return "getClassLoader".equals(perm.getName());
        }
    }

}
