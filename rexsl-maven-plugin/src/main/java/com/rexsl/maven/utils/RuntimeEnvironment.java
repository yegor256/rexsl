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

import com.jcabi.aspects.Loggable;
import com.rexsl.maven.Environment;
import java.io.File;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.validation.constraints.NotNull;

/**
 * Runtime environment, for {@link RuntimeListener}.
 *
 * <p>This class is an adapter between servlet context and
 * {@link RuntimeListener}, which is interested in context parameters.
 *
 * <p>{@link ServletContext} is filled with values in {@link EmbeddedContainer}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
final class RuntimeEnvironment implements Environment {

    /**
     * Basedir of the project (name of {@link ServletContext} init parameter).
     */
    public static final String BASEDIR_PARAM = "com.rexsl.maven.utils.BASEDIR";

    /**
     * Webdir of the webapp inside the container (name of
     * {@link ServletContext} init parameter).
     */
    public static final String WEBDIR_PARAM = "com.rexsl.maven.utils.WEBDIR";

    /**
     * Port number of the webapp inside the container (name of
     * {@link ServletContext} init parameter).
     */
    public static final String PORT_PARAM = "com.rexsl.maven.utils.PORT";

    /**
     * Servlet context.
     */
    @NotNull
    private final transient ServletContext context;

    /**
     * Public ctor.
     * @param ctx Context
     */
    RuntimeEnvironment(@NotNull final ServletContext ctx) {
        this.context = ctx;
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public File basedir() {
        return new File(
            this.context.getInitParameter(RuntimeEnvironment.BASEDIR_PARAM)
        );
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public File webdir() {
        return new File(
            this.context.getInitParameter(RuntimeEnvironment.WEBDIR_PARAM)
        );
    }

    /**
     * {@inheritDoc}
     *
     * <p>The method always thows an exception because this method should
     * never be called.
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public Set<File> classpath(final boolean test) {
        throw new UnsupportedOperationException("#classpath()");
    }

    /**
     * {@inheritDoc}
     *
     * <p>The method always thows an exception because this method should
     * never be called.
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public boolean useRuntimeFiltering() {
        throw new UnsupportedOperationException("#useRuntimeFiltering()");
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public int port() {
        return Integer.parseInt(
            this.context.getInitParameter(RuntimeEnvironment.PORT_PARAM)
        );
    }

}
