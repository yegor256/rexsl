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
package com.rexsl.test;

import com.jcabi.aspects.Immutable;
import java.net.URI;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Implementation of {@link RequestURI}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Immutable
@ToString(of = "home")
@EqualsAndHashCode(of = "home")
final class DefaultURI implements RequestURI {

    /**
     * Original request.
     */
    private final transient Request req;

    /**
     * URI.
     */
    private final transient String home;

    /**
     * Public ctor.
     * @param request Original request
     * @param uri The resource to work with
     */
    DefaultURI(final Request request, final URI uri) {
        this.home = uri.toString();
        this.req = request;
    }

    @Override
    public Request back() {
        return this.req;
    }

    @Override
    @NotNull
    public URI get() {
        return URI.create(this.home);
    }

    @Override
    public RequestURI set(@NotNull(message = "URI can't be NULL")
        final URI uri) {
        return new DefaultURI(this.req, uri);
    }

    @Override
    public RequestURI queryParam(
        @NotNull(message = "query param name can't be NULL") final String name,
        @NotNull(message = "param value can't be NULL") final Object value) {
        return new DefaultURI(
            this.req,
            UriBuilder.fromUri(this.home)
                .queryParam(name, "{value}")
                .build(value)
        );
    }

    @Override
    public RequestURI path(
        @NotNull(message = "path can't be NULL") final String segment) {
        return new DefaultURI(
            this.req,
            UriBuilder.fromUri(this.home)
                .path(segment)
                .build()
        );
    }

}
