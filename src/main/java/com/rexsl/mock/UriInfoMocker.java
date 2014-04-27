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

import java.net.URI;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.mockito.Mockito;

/**
 * Builds an instance of {@link UriInfo}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class UriInfoMocker {

    /**
     * The mock.
     */
    private final transient UriInfo info = Mockito.mock(UriInfo.class);

    /**
     * Public ctor.
     */
    public UriInfoMocker() {
        this.withBaseUri(
            UriBuilder.fromUri("http://localhost:99/").build()
        );
        this.withRequestUri(
            UriBuilder.fromUri("http://localhost:99/local/foo").build()
        );
        this.withQueryParameters(new MultivaluedMapMocker());
    }

    /**
     * With this request URI.
     * @param uri The URI
     * @return This object
     */
    public UriInfoMocker withRequestUri(final URI uri) {
        Mockito.doReturn(uri).when(this.info).getRequestUri();
        Mockito.doReturn(UriBuilder.fromUri(uri))
            .when(this.info).getRequestUriBuilder();
        Mockito.doReturn(UriBuilder.fromUri(uri))
            .when(this.info).getAbsolutePathBuilder();
        Mockito.doReturn(uri).when(this.info).getAbsolutePath();
        return this;
    }

    /**
     * With this query parameters.
     * @param params Parameters
     * @return This object
     */
    public UriInfoMocker withQueryParameters(
        final MultivaluedMap<String, String> params) {
        Mockito.doReturn(params).when(this.info).getQueryParameters();
        return this;
    }

    /**
     * With this base URI.
     * @param uri The URI
     * @return This object
     */
    public UriInfoMocker withBaseUri(final URI uri) {
        Mockito.doReturn(UriBuilder.fromUri(uri))
            .when(this.info).getBaseUriBuilder();
        Mockito.doReturn(uri).when(this.info).getBaseUri();
        return this;
    }

    /**
     * Build an instance of provided class.
     * @return The resource just created
     */
    public UriInfo mock() {
        return this.info;
    }

}
