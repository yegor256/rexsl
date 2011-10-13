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
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Start/stop grizzly container.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class EmbeddedContainer {

    /**
     * The server just started.
     */
    private Server server;

    /**
     * Private ctor.
     * @param srv The server just started
     */
    private EmbeddedContainer(final Server srv) {
        this.server = srv;
    }

    /**
     * Create and start Grizzly container.
     * @param port The port to mount to
     * @param env The environment
     * @return The container
     */
    public static EmbeddedContainer start(final Integer port,
        final Environment env) {
        final Server server = new Server(port);
        final WebAppContext ctx = new WebAppContext();
        ctx.addFilter(
            RuntimeFilter.class,
            "/*",
            EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR)
        );
        ctx.setWar(env.webdir().getPath());
        server.setHandler(ctx);
        try {
            server.start();
        // @checkstyle IllegalCatch (1 line)
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to start Jetty", ex);
        }
        return new EmbeddedContainer(server);
    }

    /**
     * Stop this container.
     */
    public void stop() {
        try {
            this.server.stop();
        // @checkstyle IllegalCatch (1 line)
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to stop Jetty", ex);
        }
    }

}
