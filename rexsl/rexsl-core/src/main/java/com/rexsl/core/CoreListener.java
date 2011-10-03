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
package com.rexsl.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.ymock.util.Logger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Core listener to be used in web.xml.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class CoreListener extends GuiceServletContextListener {

    /**
     * Init parameters.
     */
    private final Map<String, String> params = new HashMap<String, String>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextInitialized(final ServletContextEvent event) {
        final ServletContext ctx = event.getServletContext();
        final List<String> names =
            Collections.list(ctx.getInitParameterNames());
        for (String name : names) {
            this.params.put(name, ctx.getInitParameter(name));
            Logger.info(
                this,
                "#contextInitialized(): '%s' init-param set to '%s' (web.xml)",
                name,
                ctx.getInitParameter(name)
            );
        }
        if (names.size() == 0) {
            Logger.info(
                this,
                "#contextInitialized(): no init-params provided in web.xml"
            );
        }
        Logger.info(
            this,
            "#contextInitialized(%s): done",
            event.getClass().getName()
        );
        super.contextInitialized(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Injector getInjector() {
        Logger.info(
            this,
            "#getInjector(): returning JerseyModule (%d params)",
            this.params.size()
        );
        return Guice.createInjector(new JerseyModule(this.params));
    }

}
