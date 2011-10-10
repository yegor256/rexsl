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

import com.rexsl.core.ByteArrayResponseWrapper;
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
     * Character encoding of the page.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        Logger.debug(this, "#destroy(): runtime filter destroyed");
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
        Logger.debug(this, "#doFilter(..)");
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
        Logger.debug(this, "#init(..): runtime filter initialized");
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
        final ByteArrayResponseWrapper wrapper =
            new ByteArrayResponseWrapper(response);
        chain.doFilter(request, wrapper);
        String content = wrapper.getByteStream().toString(this.ENCODING);
        final List<File> dirs = new ArrayList<File>();
        dirs.add(new File("./src/test/rexsl"));
        dirs.add(new File("./src/main/webapp"));
        final String path = request.getRequestURI();
        boolean found = false;
        for (File dir : dirs) {
            final File file = new File(dir, path);
            if (file.isDirectory()) {
                continue;
            }
            if (file.exists()) {
                content = FileUtils.readFileToString(file, this.ENCODING);
                response.setStatus(HttpServletResponse.SC_OK);
                Logger.info(
                    this,
                    "#filter(%s): fetched from %s (%d bytes)",
                    path,
                    file,
                    file.length()
                );
                found = true;
            }
        }
        if (!found) {
            Logger.debug(this, "#filter(%s): no files found", path);
        }
        response.getOutputStream().write(content.getBytes(this.ENCODING));
    }

}
