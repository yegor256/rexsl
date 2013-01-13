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
package com.rexsl.maven.utils;

import com.jcabi.log.Logger;
import com.rexsl.maven.Environment;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.FilenameUtils;

/**
 * Groovy scripts executor.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class GroovyExecutor {

    /**
     * The classloader to use.
     */
    @NotNull
    private final transient ClassLoader classloader;

    /**
     * Binding.
     */
    @NotNull
    private final transient Binding binding;

    /**
     * Public ctor.
     * @param ldr The classloader
     * @param bnd The binding
     */
    public GroovyExecutor(@NotNull final ClassLoader ldr,
        @NotNull final Binding bnd) {
        this.classloader = ldr;
        this.binding = bnd;
    }

    /**
     * Public ctor.
     *
     * <p>Classpath for Groovy execution is built from URLs defined in the
     * environment and Thread-default classloader (which is built by Maven)
     * for the plugin.
     *
     * @param env The environment
     * @param bnd The binding
     */
    public GroovyExecutor(@NotNull final Environment env,
        @NotNull final Binding bnd) {
        this.classloader = new URLClassLoader(
            GroovyExecutor.fetch(env),
            Thread.currentThread().getContextClassLoader()
        );
        this.binding = bnd;
    }

    /**
     * Run one script from the file provided.
     * @param file The groovy file with the script to run
     * @throws GroovyException If some failure inside
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void execute(@NotNull final File file) throws GroovyException {
        final String basename = FilenameUtils.getBaseName(file.getPath());
        if (!basename.matches("[a-zA-Z]\\w*")) {
            throw new GroovyException(
                "Illegal script name: '%s' (only letters allowed)",
                basename
            );
        }
        final GroovyScriptEngine gse = this.gse(file);
        try {
            gse.run(file.getName(), this.binding);
        // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            throw new GroovyException("%[exception]s", ex);
        }
    }

    /**
     * Creates a new instance of the {@link GroovyScriptEngine}.
     * @param file The groovy file with the script to run
     * @return New instance of {@link GroovyScriptEngine}
     */
    private GroovyScriptEngine gse(final File file) {
        GroovyScriptEngine gse;
        try {
            gse = new GroovyScriptEngine(
                new String[] {file.getParent()},
                this.classloader
            );
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(
                Logger.format("IOException: %[exception]s", ex),
                ex
            );
        }
        return gse;
    }

    /**
     * Retrieve URLs from env.
     * @param env The environment
     * @return Array of URLs
     */
    private static URL[] fetch(final Environment env) {
        final Collection<File> files = env.classpath(false);
        final URL[] urls = new URL[files.size()];
        int pos = 0;
        for (File file : files) {
            try {
                urls[pos] = file.toURI().toURL();
            } catch (java.net.MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
            ++pos;
        }
        return urls;
    }

}
