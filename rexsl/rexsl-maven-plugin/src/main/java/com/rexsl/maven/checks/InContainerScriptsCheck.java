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
import com.rexsl.maven.utils.BindingBuilder;
import com.rexsl.maven.utils.EmbeddedContainer;
import com.rexsl.maven.utils.GroovyExecutor;
import com.rexsl.maven.utils.XsdEventHandler;
import com.ymock.util.Logger;
import java.io.File;
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
        final EmbeddedContainer container = EmbeddedContainer.start(env);
        XsdEventHandler.reset();
        boolean success = this.run(dir, env);
        container.stop();
        Logger.info(this, "Embedded servlet container stopped");
        if (XsdEventHandler.hasEvents()) {
            Logger.warn(this, "XSD failures experienced");
            success = false;
        }
        return success;
    }

    /**
     * Run all checks.
     * @param dir Where script are located
     * @param env Environment
     * @return Was it successful?
     */
    private boolean run(final File dir, final Environment env) {
        boolean success = true;
        for (File script
            : FileUtils.listFiles(dir, new String[] {"groovy"}, true)) {
            try {
                Logger.info(this, "Testing '%s'...", script);
                this.one(env, script);
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
     * @param script Check this particular Groovy script
     * @throws InternalCheckException If some failure inside
     */
    private void one(final Environment env, final File script)
        throws InternalCheckException {
        Logger.debug(this, "Running %s", script);
        final GroovyExecutor exec = new GroovyExecutor(
            env,
            new BindingBuilder(env).build()
        );
        try {
            exec.execute(script);
        } catch (com.rexsl.maven.utils.GroovyException ex) {
            throw new InternalCheckException(ex);
        }
    }

}
