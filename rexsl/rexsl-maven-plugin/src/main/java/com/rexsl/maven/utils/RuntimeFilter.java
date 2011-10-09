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

import com.ymock.util.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;

/**
 * Start/stop grizzly container.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class RuntimeFilter implements Filter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // intentionally empty
    }

    /**
     * {@inheritDoc}
     * @checkstyle ThrowsCount (6 lines)
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public void doFilter(final ServletRequest request,
        final ServletResponse response, final FilterChain chain)
        throws java.io.IOException, javax.servlet.ServletException {
        if (request instanceof HttpServletRequest
            && response instanceof HttpServletResponse) {
            this.filter(
                (HttpServletRequest) request,
                (HttpServletResponse) response,
                chain
            );
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final FilterConfig config) {
        // intentionally empty
    }

    /**
     * Make filtering.
     * @param request The request
     * @param response The response
     * @param chain Filter chain
     * @throws IOException If something goes wrong
     * @throws ServletException If something goes wrong
     * @checkstyle ThrowsCount (6 lines)
     * @checkstyle RedundantThrows (5 lines)
     */
    private void filter(final HttpServletRequest request,
        final HttpServletResponse response, final FilterChain chain)
        throws IOException, ServletException {
        final List<File> dirs = new ArrayList<File>();
        dirs.add(new File("./src/test/rexsl"));
        dirs.add(new File("./src/webapp"));
        final String path = request.getRequestURI();
        for (File dir : dirs) {
            final File file = new File(dir, path);
            if (file.exists() && !file.isDirectory()) {
                response.getOutputStream().write(
                    FileUtils.readFileToString(file).getBytes()
                );
                Logger.info(
                    this,
                    "#filter(%s): fetched from %s (%d bytes)",
                    path,
                    file,
                    file.length()
                );
                return;
            }
        }
        chain.doFilter(request, response);
    }

}
