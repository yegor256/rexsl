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
import com.rexsl.maven.utils.EmbeddedContainer;
import com.rexsl.maven.utils.GroovyExecutor;
import com.rexsl.maven.utils.PortReserver;
import com.rexsl.maven.utils.XsdEventHandler;
import com.ymock.util.Logger;
import groovy.lang.Binding;
import java.io.File;
import java.net.URI;
import org.apache.commons.io.FileUtils;

/**
 * Validate the product in container, with Groovy scripts.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class InContainerScriptsCheck implements Check {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final Environment env) {
        final File dir = new File(env.basedir(), "src/test/rexsl/scripts");
        if (!dir.exists()) {
            Logger.info(
                this,
                "%s directory is absent, no scripts to run",
                dir
            );
            return true;
        }
        if (!env.webdir().exists()) {
            throw new IllegalStateException(
                String.format(
                    "Webapp dir '%s' is absent, package the project first",
                    env.webdir()
                )
            );
        }
        Logger.info(
            this,
            "Starting embedded servlet container in '%s'...",
            env.webdir()
        );
        final Integer port = new PortReserver().port();
        final EmbeddedContainer container = EmbeddedContainer.start(port, env);
        XsdEventHandler.reset();
        final URI home = this.home(port);
        Logger.info(this, "Web front available at %s", home);
        boolean success = this.run(dir, home, env);
        container.stop();
        Logger.info(this, "Embedded servlet container stopped");
        if (XsdEventHandler.hasEvents()) {
            Logger.warn(this, "XSD failures experienced");
            success = false;
        }
        return success;
    }

    /**
     * Calculate and return the address of home page.
     * @param port Port number
     * @return The URI
     */
    private URI home(final Integer port) {
        URI home;
        try {
            home = new URI(String.format("http://localhost:%d/", port));
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
        return home;
    }

    /**
     * Run all checks.
     * @param dir Where script are located
     * @param home Home page URI
     * @param env Environment
     * @return Was it successful?
     */
    private boolean run(final File dir, final URI home, final Environment env) {
        boolean success = true;
        for (File script
            : FileUtils.listFiles(dir, new String[] {"groovy"}, true)) {
            try {
                Logger.info(this, "Testing '%s'...", script);
                this.one(env, home, script);
            } catch (InternalCheckException ex) {
                Logger.warn(this, "Test failed: %s", ex.getMessage());
                success = false;
            }
        }
        return success;
    }

    /**
     * Check one script.
     * @param env The environment
     * @param home URI of running container
     * @param script Check this particular Groovy script
     * @throws InternalCheckException If some failure inside
     */
    private void one(final Environment env, final URI home,
        final File script) throws InternalCheckException {
        final Binding binding = new Binding();
        binding.setVariable("documentRoot", home);
        Logger.debug(this, "Running %s", script);
        final GroovyExecutor exec = new GroovyExecutor(env, binding);
        try {
            exec.execute(script);
        } catch (com.rexsl.maven.utils.GroovyException ex) {
            throw new InternalCheckException(ex);
        }
    }

}
