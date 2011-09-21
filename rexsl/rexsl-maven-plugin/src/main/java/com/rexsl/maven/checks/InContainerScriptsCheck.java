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
package com.rexsl.maven.checks;

import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.rexsl.maven.Reporter;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import java.io.File;
import java.net.ServerSocket;
import java.net.URI;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

/**
 * Validate the product in container, with Groovy scripts.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class InContainerScriptsCheck implements Check {

    /**
     * Directory with Groovy files.
     */
    private static final String GROOVY_DIR = "src/test/rexsl/scripts";

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean validate(final Environment env) {
        final File dir = new File(env.basedir(), this.GROOVY_DIR);
        if (!dir.exists()) {
            env.reporter().report(
                "%s directory is absent, no scripts to run",
                this.GROOVY_DIR
            );
            return true;
        }
        env.reporter().report(
            "Starting embedded Grizzly web server in '%s'...",
            env.webdir()
        );
        final Integer port = this.port();
        final GrizzlyWebServer gws = this.gws(env.webdir(), port);
        URI home;
        try {
            home = new URI(String.format("http://localhost:%d/", port));
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
        env.reporter().report("Web front available at %s", home);
        boolean success = true;
        for (File script :
            FileUtils.listFiles(dir, new String[] {"groovy"}, true)) {
            try {
                env.reporter().report("Testing '%s'...", script);
                this.one(env, home, script);
            } catch (InternalCheckException ex) {
                env.reporter().report("Test failed: %s", ex.getMessage());
                success = false;
            }
        }
        gws.stop();
        env.reporter().report("Embedded Grizzly web server stopped");
        return success;
    }

    /**
     * Check one script.
     * @param env The environment
     * @param home URI of running Grizzly container
     * @param script Check this particular Groovy script
     * @throws InternalCheckException If some failure inside
     */
    public final void one(final Environment env, final URI home,
        final File script) throws InternalCheckException {
        final Binding binding = new Binding();
        binding.setVariable("documentRoot", home);
        env.reporter().log("Running %s", script);
        final GroovyExecutor exec = new GroovyExecutor(env, binding);
        exec.execute(script);
    }

    /**
     * Create and start Grizzly container.
     * @param webapp Location of WEB home
     * @param port The port to mount to
     * @return The container
     */
    private GrizzlyWebServer gws(final File webdir, final Integer port) {
        final String context = "/";
        final GrizzlyWebServer gws = new GrizzlyWebServer(
            port,
            webdir.getPath()
        );
        try {
            gws.start();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        return gws;
    }

    /**
     * Find and return the first available port.
     * @return The port number
     */
    private Integer port() {
        Integer port;
        try {
            final ServerSocket socket = new ServerSocket(0);
            port = socket.getLocalPort();
            socket.close();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Failed to reserve port", ex);
        }
        return port;
    }

}
