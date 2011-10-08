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

import com.google.inject.servlet.GuiceFilter;
import com.ymock.util.Logger;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Core filter to be used in web.xml.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class CoreFilter implements Filter {

    /**
     * Filter to run.
     */
    private final Filter guice = new GuiceFilter();

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (3 lines)
     */
    @Override
    public void init(final FilterConfig config) throws ServletException {
        Settings.INSTANCE.reset(config.getServletContext());
        this.guice.init(config);
        Logger.info(this, "#init(%s): done", config.getClass().getName());
    }

    /**
     * {@inheritDoc}
     * @checkstyle ThrowsCount (6 lines)
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public void doFilter(final ServletRequest request,
        final ServletResponse response, final FilterChain chain)
        throws ServletException, IOException {
        this.guice.doFilter(request, response, chain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        this.guice.destroy();
        Logger.info(this, "#destroy(): done");
    }

}
