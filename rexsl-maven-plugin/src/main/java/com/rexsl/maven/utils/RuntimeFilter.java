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
import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.lang3.StringUtils;

/**
 * Filter used by embedded container in runtime.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class RuntimeFilter implements Filter {

    /**
     * Where static files are kept, list of folders
     * (name of {@link javax.servlet.ServletContext} init parameter).
     */
    public static final String FOLDERS = "com.rexsl.maven.utils.FOLDERS";

    /**
     * Folders where we read files in runtime.
     */
    private final transient List<File> folders = new ArrayList<File>(0);

    @Override
    @Loggable(Loggable.DEBUG)
    public void destroy() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     * @checkstyle ThrowsCount (8 lines)
     * @checkstyle RedundantThrows (7 lines)
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public void doFilter(final ServletRequest request,
        final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException {
        if (request instanceof HttpServletRequest
            && response instanceof HttpServletResponse) {
            Logger.debug(
                this,
                "#doFilter(%s)",
                ((HttpServletRequest) request).getRequestURI()
            );
            this.filter(
                (HttpServletRequest) request,
                (HttpServletResponse) response,
                chain
            );
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Loggable(Loggable.DEBUG)
    public void init(final FilterConfig config) {
        final String param = config.getServletContext()
            .getInitParameter(RuntimeFilter.FOLDERS);
        for (final String name : StringUtils.split(param, ";")) {
            this.folders.add(new File(name));
            Logger.debug(
                this,
                "#init(%s): runtime folder added: %s",
                config.getClass().getName(),
                name
            );
        }
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
        final RuntimeResponseWrapper wrapper =
            new RuntimeResponseWrapper(response);
        chain.doFilter(request, wrapper);
        final String path = request.getRequestURI();
        byte[] content = wrapper.getByteStream().toByteArray();
        Logger.debug(
            this,
            "#filter(%s): %d bytes of WEB-INF content retrieved",
            path,
            content.length
        );
        final byte[] replacement = this.fetch(path);
        if (replacement == null || Arrays.equals(content, replacement)) {
            wrapper.passThrough();
        } else {
            Logger.info(
                this,
                "#filter(%s): content replaced on-fly (%d bytes)",
                path,
                replacement.length
            );
            content = replacement;
            response.setStatus(HttpServletResponse.SC_OK);
            response.setIntHeader("Content-Length", content.length);
            response.addHeader(
                "Rexsl-Filtered",
                Logger.format("%d bytes", content.length)
            );
        }
        response.getOutputStream().write(content);
    }

    /**
     * Find file content by URL.
     * @param path The path
     * @return The content or NULL
     * @throws IOException If something goes wrong
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private byte[] fetch(final String path) throws IOException {
        byte[] content = null;
        for (final File dir : this.folders) {
            final File file = new File(dir, path);
            if (file.isDirectory()) {
                continue;
            }
            if (file.exists()) {
                content = FileUtils.readFileToByteArray(file);
                Logger.debug(
                    this,
                    "#fetch(%s): fetched from %s (%d bytes)",
                    path,
                    file,
                    file.length()
                );
            }
        }
        return content;
    }

}
