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
package com.rexsl.maven.checks;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.rexsl.maven.utils.BindingBuilder;
import com.rexsl.maven.utils.EmbeddedContainer;
import com.rexsl.maven.utils.FileFinder;
import com.rexsl.maven.utils.GroovyException;
import com.rexsl.maven.utils.GroovyExecutor;
import com.rexsl.maven.utils.LoggingManager;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Validate the product in container, with Groovy scripts.
 *
 * <p>Since this class is NOT public its documentation is not available online.
 * All details of this check should be explained in the JavaDoc of
 * {@link DefaultChecksProvider}.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode(of = "test")
final class InContainerScriptsCheck implements Check {

    /**
     * Scope of tests to execute.
     */
    @NotNull
    private transient String test = ".*";

    /**
     * Mutex for synchronization.
     */
    @SuppressWarnings("PMD.FinalFieldCouldBeStatic")
    private final transient Object mutex = "_mutex_";
    @Override
    @Loggable(Loggable.DEBUG)
    public void setScope(@NotNull final String scope) {
        synchronized (this.mutex) {
            this.test = scope;
        }
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public boolean validate(@NotNull final Environment env) {
        final File dir = new File(env.basedir(), "src/test/rexsl/scripts");
        boolean success = true;
        if (dir.exists()) {
            if (!env.webdir().exists()) {
                throw new IllegalStateException(
                    Logger.format(
                        "Webapp dir '%s' is absent, package the project first",
                        env.webdir()
                    )
                );
            }
            Logger.info(
                this,
                "Testing in embedded servlet container at '%s' (test=%s)...",
                env.webdir(),
                this.test
            );
            final EmbeddedContainer container = EmbeddedContainer.start(env);
            success = this.run(dir, env);
            container.stop();
            Logger.info(this, "Embedded servlet container stopped");
        } else {
            Logger.info(
                this,
                "%s directory is absent, no scripts to run",
                dir
            );
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
        final FileFinder finder = new FileFinder(dir, "groovy");
        final Set<String> failed = new LinkedHashSet<String>(0);
        for (final File script : finder.random()) {
            final String name = FilenameUtils.removeExtension(script.getName());
            if (!name.matches(this.test) && !name.contains(this.test)) {
                Logger.info(this, "Ignored '%s'", script);
                continue;
            }
            LoggingManager.enter(name);
            try {
                Logger.info(this, "Testing '%s'...", script);
                if (!this.isValid(env, script)) {
                    success = false;
                    failed.add(script.getName());
                }
            } finally {
                LoggingManager.leave();
            }
        }
        if (!failed.isEmpty()) {
            Logger.warn(
                this,
                "In-container check failed because of:\n  %s",
                StringUtils.join(failed, "\n  ")
            );
        }
        return success;
    }

    /**
     * Runs and validates a single Groovy script.
     * @param env The environment
     * @param script Check this particular Groovy script
     * @return TRUE if this script is valid (no errors)
     * @see #run(File,Environment)
     */
    private boolean isValid(final Environment env, final File script) {
        Logger.debug(this, "Running %s", script);
        final GroovyExecutor exec = new GroovyExecutor(
            env,
            new BindingBuilder(env).build()
        );
        boolean valid;
        try {
            exec.execute(script);
            valid = true;
        } catch (GroovyException ex) {
            Logger.warn(
                this,
                "Test failed (%s): %s",
                script,
                ex.getMessage()
            );
            valid = false;
        }
        return valid;
    }

}
