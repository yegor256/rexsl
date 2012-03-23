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

import com.rexsl.maven.Environment;
import com.ymock.util.Logger;
import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * To be executed before all other code.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class RuntimeListener implements ServletContextListener {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.UseProperClassLoader")
    public void contextInitialized(final ServletContextEvent event) {
        final long start = System.nanoTime();
        final Environment env = new RuntimeEnvironment(
            event.getServletContext()
        );
        final File dir = new File(env.basedir(), "src/test/rexsl/bootstrap");
        if (dir.exists()) {
            int counter = 0;
            final GroovyExecutor exec = new GroovyExecutor(
                event.getServletContext().getClassLoader(),
                new BindingBuilder(env).build()
            );
            final FileFinder finder = new FileFinder(dir, "groovy");
            for (File script : finder.ordered()) {
                Logger.info(this, "Running '%s'...", script);
                try {
                    exec.execute(script);
                } catch (com.rexsl.maven.utils.GroovyException ex) {
                    throw new IllegalStateException(ex);
                }
                // @checkstyle IllegalTokenCheck (1 line)
                counter++;
            }
            Logger.debug(
                this,
                // @checkstyle LineLength (1 line)
                "#contextInitialized(%s): initialized with %d script(s) in %[nano]s",
                event.getClass().getName(),
                counter,
                System.nanoTime() - start
            );
        } else {
            Logger.info(
                this,
                "%s directory is absent, no bootstrap scripts to run",
                dir
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        Logger.debug(this, "#contextDestroyed(): destroyed");
    }

}
