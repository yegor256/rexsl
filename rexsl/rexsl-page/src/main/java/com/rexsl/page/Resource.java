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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * JAX-RS resource has to implement this interface, in order to be
 * injectable into {@link BasePage}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3.7
 * @see BasePage
 */
public interface Resource {

    /**
     * This resource should understand {@code X-Forwarded-For}
     * HTTP header and change its properties (mostly inside
     * {@link UriInfo}) accordingly.
     *
     * @see <a href="http://en.wikipedia.org/wiki/X-Forwarded-For">X-Forwarded-For HTTP header</a>
     * @see <a href="http://tools.ietf.org/html/draft-ietf-appsawg-http-forwarded-10">IETF Forwarded HTTP Extension</a>
     * @since 0.4
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Forwarded {
    }

    /**
     * When this resource creation was started by JAX-RS implementation.
     * @return Time in milliseconds
     */
    long started();

    /**
     * Get URI Info.
     * @return URI info
     */
    UriInfo uriInfo();

    /**
     * All registered JAX-RS providers.
     * @return Providers
     */
    Providers providers();

    /**
     * All Http Headers.
     * @return Headers
     */
    HttpHeaders httpHeaders();

    /**
     * Request just received.
     * @return The request
     */
    HttpServletRequest httpServletRequest();

}
