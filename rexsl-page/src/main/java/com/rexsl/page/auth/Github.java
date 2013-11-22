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
package com.rexsl.page.auth;

import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.rexsl.page.Link;
import com.rexsl.page.Resource;
import com.rexsl.test.request.JdkRequest;
import com.rexsl.test.response.JsonResponse;
import com.rexsl.test.Request;
import com.rexsl.test.response.RestResponse;
import com.rexsl.test.response.XmlResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import javax.json.JsonObject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Github authentication provider.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.5
 * @see <a href="http://developer.github.com/v3/oauth/">OAuth in Github</a>
 */
@ToString
@EqualsAndHashCode(of = { "appId", "appKey" })
@Loggable(Loggable.DEBUG)
@Provider.Redirect
public final class Github implements Provider, Provider.Visible {

    /**
     * Query param.
     */
    private static final String FLAG = "rexsl-github";

    /**
     * Resource.
     */
    private final transient Resource resource;

    /**
     * Github ID.
     */
    private final transient String appId;

    /**
     * Github secret key.
     */
    private final transient String appKey;

    /**
     * Public ctor.
     * @param res JAX-RS resource
     * @param aid Application id
     * @param key Application secret key
     */
    public Github(@NotNull final Resource res,
        @NotNull final String aid, @NotNull final String key) {
        this.resource = res;
        this.appId = aid;
        this.appKey = key;
    }

    @Override
    public Identity identity() throws IOException {
        Identity identity = Identity.ANONYMOUS;
        if (this.resource.uriInfo().getQueryParameters()
            .containsKey(Github.FLAG)) {
            final List<String> code = this.resource.uriInfo()
                // @checkstyle MultipleStringLiterals (1 line)
                .getQueryParameters().get("code");
            if (code == null || code.isEmpty()) {
                throw new IllegalStateException(
                    "HTTP query parameter 'code' is mandatory"
                );
            }
            identity = this.fetch(this.token(code.get(0)));
        }
        return identity;
    }

    @Override
    public Link link() {
        return new Link(
            "auth-github",
            UriBuilder
                .fromUri("https://github.com/login/oauth/authorize")
                // @checkstyle MultipleStringLiterals (2 lines)
                .queryParam("client_id", "{client_id}")
                .queryParam("redirect_uri", "{redirect_uri}")
                .build(this.appId, this.redirectUri())
        );
    }

    /**
     * Redirect URI.
     * @return The URI
     */
    private URI redirectUri() {
        return this.resource.uriInfo().getRequestUriBuilder()
            .clone()
            .queryParam(Github.FLAG, "")
            .build();
    }

    /**
     * Retrieve Github access token.
     * @param code Github "authorization code"
     * @return The token
     * @throws IOException If failed
     */
    private String token(final String code) throws IOException {
        final URI uri = UriBuilder
            .fromUri("https://github.com/login/oauth/access_token")
            .queryParam("client_id", "{id}")
            .queryParam("redirect_uri", "{uri}")
            .queryParam("client_secret", "{secret}")
            .queryParam("code", "{code}")
            .build(
                this.appId,
                this.redirectUri(),
                this.appKey,
                code
            );
        return new JdkRequest(uri)
            .method(Request.POST)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .xml().xpath("/OAuth/access_token/text()").get(0);
    }

    /**
     * Get user name from Github, with the token provided.
     * @param token Github access token
     * @return The user found in Github
     * @throws IOException If fails
     */
    private Identity fetch(final String token) throws IOException {
        final URI uri = UriBuilder
            .fromUri("https://api.github.com/user")
            .queryParam("access_token", "{token}")
            .build(token);
        return this.parse(
            new JdkRequest(uri)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(JsonResponse.class).json().readObject()
        );
    }

    /**
     * Make identity from JSON object.
     * @param json JSON received from Github
     * @return Identity found
     */
    private Identity parse(final JsonObject json) {
        return new Identity.Simple(
            URN.create(String.format("urn:github:%d", json.getInt("id"))),
            json.getString("name", Identity.ANONYMOUS.name()),
            URI.create(
                json.getString(
                    "avatar_url",
                    Identity.ANONYMOUS.photo().toString()
                )
            )
        );
    }

}
