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
import com.restfb.DefaultFacebookClient;
import com.restfb.types.User;
import com.rexsl.page.Link;
import com.rexsl.page.Resource;
import com.rexsl.test.RestTester;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Facebook authentication provider.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.7
 */
@ToString
@EqualsAndHashCode(of = { "appId", "appKey" })
public final class Facebook implements Provider {

    /**
     * Query param.
     */
    private static final String FLAG = "rexsl-facebook";

    /**
     * Resource.
     */
    private final transient Resource resource;

    /**
     * Facebook ID.
     */
    private final transient String appId;

    /**
     * Facebook secret key.
     */
    private final transient String appKey;

    /**
     * Public ctor.
     * @param res Resource
     * @param aid Application id
     * @param key Application secret key
     */
    public Facebook(@NotNull final Resource res,
        @NotNull final String aid, @NotNull final String key) {
        this.resource = res;
        this.appId = aid;
        this.appKey = key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public Identity identity() throws IOException {
        Identity identity = Identity.ANONYMOUS;
        if (this.resource.uriInfo().getQueryParameters()
            .containsKey(Facebook.FLAG)) {
            final List<String> code = this.resource.uriInfo()
                // @checkstyle MultipleStringLiterals (1 line)
                .getQueryParameters().get("code");
            if (code.isEmpty()) {
                throw new IllegalStateException(
                    "HTTP query parameter 'code' is mandatory"
                );
            }
            final User fbuser = this.fetch(this.token(code.get(0)));
            identity = new Identity() {
                @Override
                public URN urn() {
                    return URN.create(
                        String.format("urn:facebook:%s", fbuser.getId())
                    );
                }
                @Override
                public String name() {
                    return fbuser.getName();
                }
                @Override
                public URI photo() {
                    return UriBuilder.fromUri("https://graph.facebook.com/")
                        .path("/{id}/picture")
                        .build(fbuser.getId());
                }
            };
        }
        return identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public Link link() {
        return new Link(
            "auth-facebook",
            UriBuilder
                .fromUri("https://www.facebook.com/dialog/oauth")
                // @checkstyle MultipleStringLiterals (2 lines)
                .queryParam("client_id", "{client_id}")
                .queryParam("redirect_uri", "{redirect_uri}")
                .build(this.appKey, this.redirectUri())
        );
    }

    /**
     * Redirect URI.
     * @return The URI
     */
    private URI redirectUri() {
        return this.resource.uriInfo().getRequestUriBuilder()
            .clone()
            .queryParam(Facebook.FLAG)
            .build();
    }

    /**
     * Retrieve facebook access token.
     * @param code Facebook "authorization code"
     * @return The token
     * @throws IOException If failed
     */
    private String token(final String code) throws IOException {
        final URI uri = UriBuilder
            .fromUri("https://graph.facebook.com/oauth/access_token")
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
        final String response = RestTester.start(uri)
            .get("fetch Facebook access token")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .getBody();
        final String[] sectors = response.split("&");
        String token = null;
        for (String sector : sectors) {
            final String[] pair = sector.split("=");
            if (pair.length != 2) {
                throw new IllegalArgumentException(
                    String.format(
                        "Invalid response: '%s'",
                        response
                    )
                );
            }
            if ("access_token".equals(pair[0])) {
                token = pair[1];
                break;
            }
        }
        if (token == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Access token not found in response: '%s'",
                    response
                )
            );
        }
        return token;
    }

    /**
     * Get user name from Facebook, but the code provided.
     * @param token Facebook access token
     * @return The user found in FB
     */
    private User fetch(final String token) {
        try {
            return new DefaultFacebookClient(token)
                .fetchObject("me", User.class);
        } catch (com.restfb.exception.FacebookException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
