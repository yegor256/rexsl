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
package com.rexsl.core;

import com.jcabi.log.Logger;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.validation.constraints.NotNull;

/**
 * Adapter between {@link ServletConfig} and {@link FilterConfig}.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @see RestfulServlet
 */
final class ServletConfigWrapper implements FilterConfig {

    /**
     * Wrapped config.
     */
    private final transient ServletConfig config;

    /**
     * Additional properties.
     */
    private final transient Properties properties;

    /**
     * Public ctor.
     * @param cfg Servlet config
     * @param props Properties to add to existing params
     */
    public ServletConfigWrapper(@NotNull final ServletConfig cfg,
        @NotNull final Properties props) {
        this.config = cfg;
        this.properties = props;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilterName() {
        return Logger.format("%s-filter", this.config.getServletName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInitParameter(@NotNull final String name) {
        String value = this.properties.getProperty(name);
        if (value == null) {
            value = this.config.getInitParameter(name);
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getInitParameterNames() {
        final Set<String> names = new HashSet<String>();
        for (Object name : this.properties.keySet()) {
            names.add(name.toString());
        }
        final Enumeration<?> encapsulated = this.config.getInitParameterNames();
        while (encapsulated.hasMoreElements()) {
            names.add(encapsulated.nextElement().toString());
        }
        return Collections.enumeration(names);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServletContext getServletContext() {
        return this.config.getServletContext();
    }

}

