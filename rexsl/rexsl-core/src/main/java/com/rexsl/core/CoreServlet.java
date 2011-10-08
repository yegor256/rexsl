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

import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.ymock.util.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

/**
 * Core servlet.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class CoreServlet extends HttpServlet {

    /**
     * Jersey servlet.
     */
    private final HttpServlet jersey = new ServletContainer();

    /**
     * XSL filter.
     */
    private XslBrowserFilter filter;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ServletConfig config) {
        final List<String> packages = new ArrayList<String>();
        packages.add(this.getClass().getPackage().getName());
        final String param = config.getInitParameter("com.rexsl.PACKAGES");
        if (param != null) {
            for (String pkg : StringUtils.split(param, ",")) {
                if (packages.contains(pkg)) {
                    continue;
                }
                packages.add(pkg);
                Logger.info(
                    this,
                    "#init(): '%s' package added (%d total)",
                    pkg,
                    packages.size()
                );
            }
        }
        // this.jersey.init(config);
        this.filter = new XslBrowserFilter(config.getServletContext());
    }

    /**
     * {@inheritDoc}
     * @checkstyle ThrowsCount (6 lines)
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    protected void service(final HttpServletRequest request,
        final HttpServletResponse response)
        throws ServletException, IOException {
        this.jersey.service(request, response);
        this.filter.filter(request, response);
    }

}
