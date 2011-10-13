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

import com.rexsl.maven.Environment;
import com.ymock.util.Logger;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.io.FilenameUtils;

/**
 * Groovy scripts executor.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class GroovyExecutor {

    /**
     * Environment.
     */
    private final Environment environment;

    /**
     * Binding.
     */
    private final Binding binding;

    /**
     * Public ctor.
     * @param env The environment
     * @param bnd The binding
     */
    public GroovyExecutor(final Environment env, final Binding bnd) {
        this.environment = env;
        this.binding = bnd;
    }

    /**
     * Run one script.
     * @param file The file
     * @throws InternalCheckException If some failure inside
     */
    public void execute(final File file) throws InternalCheckException {
        final String basename = FilenameUtils.getBaseName(file.getPath());
        if (!basename.matches("[a-zA-Z]\\w*")) {
            throw new InternalCheckException(
                "Illegal script name: %s (only letters allowed)",
                basename
            );
        }
        GroovyScriptEngine gse;
        try {
            gse = new GroovyScriptEngine(
                new String[] {file.getParent()},
                this.environment.classloader()
            );
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(this.log(ex));
        }
        try {
            gse.run(file.getName(), this.binding);
        } catch (AssertionError ex) {
            throw new InternalCheckException(this.log(ex));
        } catch (groovy.util.ResourceException ex) {
            throw new IllegalArgumentException(this.log(ex));
        } catch (groovy.util.ScriptException ex) {
            throw new IllegalArgumentException(this.log(ex));
        }
    }

    /**
     * Protocol one exception just happened, and return it.
     * @param exception The exception to protocol
     * @return The same exception object
     */
    private Throwable log(final Throwable exception) {
        final StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        Logger.warn(this, "%s", writer.toString());
        return exception;
    }

}
