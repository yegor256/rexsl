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
package com.rexsl.page;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import lombok.ToString;

/**
 * Base implementation of {@link Resource}.
 *
 * <p>It is recommended to use this class as a base of all your JAX-RS resource
 * classes and construct pages with {@link PageBuilder},
 * on top of {@link BasePage}, for example:
 *
 * <pre> &#64;Path("/")
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
 * }</pre>
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3.7
 * @see BasePage
 * @see PageBuilder
 */
@ToString
@Loggable(Loggable.DEBUG)
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
    private final transient AtomicReference<HttpHeaders> ihttpHeaders =
        new AtomicReference<HttpHeaders>();

    /**
     * HTTP servlet request, injected by JAX-RS implementation.
     */
    private transient HttpServletRequest ihttpRequest;

    /**
     * Security context.
     * @since 0.4.7
     */
    private transient SecurityContext security;

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
    @NotNull
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
    @NotNull
    public final HttpHeaders httpHeaders() {
        if (this.ihttpHeaders.get() == null) {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s#httpHeaders was never injected by JAX-RS",
                    this
                )
            );
        }
        return this.ihttpHeaders.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
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
    @NotNull
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
     * {@inheritDoc}
     * @since 0.4.7
     */
    @Override
    @NotNull
    public final SecurityContext securityContext() {
        if (this.security == null) {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s#securityContext was never injected by JAX-RS",
                    this
                )
            );
        }
        return this.security;
    }

    /**
     * Set URI Info. Should be called by JAX-RS implementation
     * because of {@code @Context} annotation.
     * @param info The info to inject
     */
    @Context
    public final void setUriInfo(@NotNull final UriInfo info) {
        if (this.needsForwarding()) {
            this.iuriInfo = new ForwardedUriInfo(info, this.ihttpHeaders);
        } else {
            this.iuriInfo = info;
        }
    }

    /**
     * Set Providers. Should be called by JAX-RS implementation
     * because of {@code @Context} annotation.
     * @param prov List of providers
     */
    @Context
    public final void setProviders(@NotNull final Providers prov) {
        this.iproviders = prov;
    }

    /**
     * Set HttpHeaders. Should be called by JAX-RS implementation
     * because of {@code @Context} annotation.
     * @param hdrs List of headers
     */
    @Context
    public final void setHttpHeaders(@NotNull final HttpHeaders hdrs) {
        this.ihttpHeaders.set(hdrs);
    }

    /**
     * Set HttpServletRequest. Should be called by JAX-RS implementation
     * because of {@code @Context} annotation.
     * @param request The request
     */
    @Context
    public final void setHttpServletRequest(
        @NotNull final HttpServletRequest request) {
        this.ihttpRequest = request;
    }

    /**
     * Set Security Context. Should be called by JAX-RS implementation
     * because of {@code @Context} annotation.
     * @param context The security context
     * @since 0.4.7
     */
    @Context
    public final void setSecurityContext(
        @NotNull final SecurityContext context) {
        this.security = context;
    }

    /**
     * This resource needs forwarding of {@link UriInfo}?
     * @return TRUE if yes, it needs to use {@link ForwardedUriInfo}
     */
    private boolean needsForwarding() {
        boolean needs = false;
        Class<?> type = this.getClass();
        while (!type.equals(Object.class)) {
            if (type.isAnnotationPresent(Resource.Forwarded.class)) {
                needs = true;
                break;
            }
            type = type.getSuperclass();
        }
        return needs;
    }

}
