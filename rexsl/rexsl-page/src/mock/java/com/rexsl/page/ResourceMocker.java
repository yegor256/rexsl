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

import com.rexsl.core.XslResolver;
import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.Marshaller;
import org.mockito.Mockito;

/**
 * Builds an instance of {@link Resource}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ResourceMocker {

    /**
     * Mock of resource.
     */
    private final transient Resource resource = Mockito.mock(Resource.class);

    /**
     * Public ctor.
     */
    public ResourceMocker() {
        final URI home = URI.create("http://localhost:9999/local");
        this.withUriInfo(new UriInfoMocker().withRequestUri(home).mock());
        final HttpServletRequest request =
            Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn(home.getHost()).when(request).getRemoteAddr();
        Mockito.doReturn(home.getPath()).when(request).getRequestURI();
        Mockito.doReturn(home.getPath()).when(request).getContextPath();
        this.withServletRequest(request);
        this.withHttpHeaders(new HttpHeadersMocker().mock());
        final Providers providers = Mockito.mock(Providers.class);
        Mockito.doReturn(new XslResolver())
            .when(providers)
            .getContextResolver(
                Marshaller.class, MediaType.APPLICATION_XML_TYPE
            );
        this.withProviders(providers);
    }

    /**
     * With this {@link UriInfo}.
     * @param info The {@link UriInfo} object
     * @return This object
     */
    public ResourceMocker withUriInfo(final UriInfo info) {
        Mockito.doReturn(info).when(this.resource).uriInfo();
        return this;
    }

    /**
     * With this instance of {@link Providers}.
     * @param providers The instance
     * @return This object
     */
    public ResourceMocker withProviders(final Providers providers) {
        Mockito.doReturn(providers).when(this.resource).providers();
        return this;
    }

    /**
     * With this instance of {@link HttpHeaders}.
     * @param headers The instance
     * @return This object
     */
    public ResourceMocker withHttpHeaders(final HttpHeaders headers) {
        Mockito.doReturn(headers).when(this.resource).httpHeaders();
        return this;
    }

    /**
     * With this instance of {@link HttpServletRequests}.
     * @param req The instance
     * @return This object
     */
    public ResourceMocker withServletRequest(final HttpServletRequest req) {
        Mockito.doReturn(req).when(this.resource).httpServletRequest();
        return this;
    }

    /**
     * Build an instance of provided class.
     * @return The resource
     */
    public Resource mock() {
        return this.resource;
    }

}
