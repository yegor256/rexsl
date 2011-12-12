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
package com.rexsl.maven.utils;

import com.rexsl.maven.Environment;
import com.ymock.util.Logger;
import java.io.File;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.io.FileUtils;

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
    public void contextInitialized(final ServletContextEvent event) {
        final Environment env = new RuntimeListener.RuntimeEnvironment(
            event.getServletContext()
        );
        final File dir = new File(env.basedir(), "src/test/rexsl/bootstrap");
        if (!dir.exists()) {
            Logger.info(
                this,
                "%s directory is absent, no bootstrap scripts to run",
                dir
            );
            return;
        }
        int counter = 0;
        for (File script
            : FileUtils.listFiles(dir, new String[] {"groovy"}, true)) {
            Logger.info(this, "Running '%s'...", script);
            final GroovyExecutor exec = new GroovyExecutor(
                event.getServletContext().getClassLoader(),
                new BindingBuilder(env).build()
            );
            try {
                exec.execute(script);
            } catch (com.rexsl.maven.utils.GroovyException ex) {
                throw new IllegalStateException(ex);
            }
            counter += 1;
        }
        Logger.debug(
            this,
            "#contextInitialized(%s): initialized with %d script(s)",
            event.getClass().getName(),
            counter
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        Logger.debug(this, "#contextDestroyed(): destroyed");
    }

    /**
     * Runtime environment.
     */
    private static final class RuntimeEnvironment implements Environment {
        /**
         * Servlet context.
         */
        private final ServletContext context;
        /**
         * Public ctor.
         * @param ctx Context
         */
        public RuntimeEnvironment(final ServletContext ctx) {
            this.context = ctx;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public File basedir() {
            return new File(
                this.context.getInitParameter("com.rexsl.maven.utils.BASEDIR")
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public File webdir() {
            return new File(
                this.context.getInitParameter("com.rexsl.maven.utils.WEBDIR")
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public List<File> classpath(final boolean test) {
            throw new IllegalStateException("#classpath");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean useRuntimeFiltering() {
            throw new IllegalStateException("#useRuntimeFiltering");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Integer port() {
            return Integer.valueOf(
                this.context.getInitParameter("com.rexsl.maven.utils.PORT")
            );
        }
    }

}
