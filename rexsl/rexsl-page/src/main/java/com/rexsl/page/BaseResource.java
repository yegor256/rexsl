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
package com.rexsl.page;

import com.jcabi.log.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * Base implementation of {@link Resource}.
 *
 * <p>It is recommended to use this class as a base of all your JAX-RS resource
 * classes and construct pages with {@link PageBuilder},
 * on top of {@link BasePage}, for example:
 *
 * <pre>
 * &#64;Path("/")
 * public class MainRs extends BaseResource {
 *   &#64;GET
 *   &#64;Produces(MediaTypes.APPLICATION_XML)
 *   public BasePage front() {
 *     return new PageBuilder()
 *       .stylesheet("/xsl/front.xsl")
 *       .build(BasePage.class)
 *       .init(this)
 *       .append(new JaxbBundle("text", "Hello!"));
 *   }
 * }
 * </pre>
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.7
 * @see BasePage
 * @see PageBuilder
 */
public class BaseResource implements Resource {

    /**
     * Start time of page building.
     */
    private final transient long start = System.currentTimeMillis();

    /**
     * List of known JAX-RS providers, injected by JAX-RS implementation.
     */
    private transient Providers iproviders;

    /**
     * URI info, injected by JAX-RS implementation.
     */
    private transient UriInfo iuriInfo;

    /**
     * Http headers, injected by JAX-RS implementation.
     */
    private transient HttpHeaders ihttpHeaders;

    /**
     * HTTP servlet request, injected by JAX-RS implementation.
     */
    private transient HttpServletRequest ihttpRequest;

    /**
     * {@inheritDoc}
     */
    @Override
    public final long started() {
        return this.start;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Providers providers() {
        if (this.iproviders == null) {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s#providers was never injected by JAX-RS",
                    this
                )
            );
        }
        return this.iproviders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final HttpHeaders httpHeaders() {
        if (this.ihttpHeaders == null) {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s#httpHeaders was never injected by JAX-RS",
                    this
                )
            );
        }
        return this.ihttpHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final UriInfo uriInfo() {
        if (this.iuriInfo == null) {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s#uriInfo was never injected by JAX-RS",
                    this
                )
            );
        }
        return this.iuriInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final HttpServletRequest httpServletRequest() {
        if (this.ihttpRequest == null) {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s#httpRequest was never injected by JAX-RS",
                    this
                )
            );
        }
        return this.ihttpRequest;
    }

    /**
     * Set URI Info. Should be called by JAX-RS implemenation
     * because of {@code @Context} annotation.
     * @param info The info to inject
     */
    @Context
    public final void setUriInfo(final UriInfo info) {
        this.iuriInfo = info;
        Logger.debug(
            this,
            "#setUriInfo(%[type]s): injected",
            info
        );
    }

    /**
     * Set Providers. Should be called by JAX-RS implemenation
     * because of {@code @Context} annotation.
     * @param prov List of providers
     */
    @Context
    public final void setProviders(final Providers prov) {
        this.iproviders = prov;
        Logger.debug(
            this,
            "#setProviders(%[type]s): injected",
            prov
        );
    }

    /**
     * Set HttpHeaders. Should be called by JAX-RS implemenation
     * because of {@code @Context} annotation.
     * @param hdrs List of headers
     */
    @Context
    public final void setHttpHeaders(final HttpHeaders hdrs) {
        this.ihttpHeaders = hdrs;
        Logger.debug(
            this,
            "#setHttpHeaders(%[type]s): injected",
            hdrs
        );
    }

    /**
     * Set HttpServletRequest. Should be called by JAX-RS implemenation
     * because of {@code @Context} annotation.
     * @param request The request
     */
    @Context
    public final void setHttpServletRequest(final HttpServletRequest request) {
        this.ihttpRequest = request;
        Logger.debug(
            this,
            "#setHttpServletRequest(%[type]s): injected",
            request
        );
    }

}
