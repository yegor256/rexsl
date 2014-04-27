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
import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;
import com.jcabi.urn.URN;
import com.rexsl.page.Link;
import com.rexsl.page.Resource;
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
 * Google authentication provider.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.8
 * @see <a href="http://www.rexsl.com/rexsl-page/inset-oauth.html">OAuth in RESTful Interfaces</a>
 * @see <a href="https://developers.google.com/accounts/docs/OAuth2">Google OAuth</a>
 */
@ToString
@EqualsAndHashCode(of = { "app", "secret" })
@Loggable(Loggable.DEBUG)
@Provider.Redirect
public final class Google implements Provider, Provider.Visible {

    /**
     * Query param.
     */
    private static final String FLAG = "rexsl-google";

    /**
     * Google state query param.
     */
    private static final String STATE = "state";

    /**
     * Resource.
     */
    private final transient Resource resource;

    /**
     * Facebook ID.
     */
    private final transient String app;

    /**
     * Facebook secret key.
     */
    private final transient String secret;

    /**
     * Public ctor.
     * @param res Resource
     * @param aid Application id
     * @param key Application secret key
     */
    public Google(@NotNull final Resource res,
        @NotNull final String aid, @NotNull final String key) {
        this.resource = res;
        this.app = aid;
        this.secret = key;
    }

    @Override
    public Identity identity() throws IOException {
        Identity identity = Identity.ANONYMOUS;
        final boolean authed = this.resource.uriInfo()
            .getQueryParameters().containsKey(Google.STATE)
            && this.resource.uriInfo().getQueryParameters()
            .getFirst(Google.STATE).equals(Google.FLAG);
        if (authed) {
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
            "rexsl:google",
            UriBuilder
                .fromUri("https://accounts.google.com/o/oauth2/auth")
                // @checkstyle MultipleStringLiterals (2 lines)
                .queryParam("client_id", "{id}")
                .queryParam("redirect_uri", "{uri}")
                .queryParam("response_type", "code")
                .queryParam(Google.STATE, Google.FLAG)
                .queryParam(
                    "scope",
                    "https://www.googleapis.com/auth/userinfo.profile"
                )
                .build(this.app, this.resource.uriInfo().getBaseUri())
        );
    }

    /**
     * Retrieve Google access token.
     * @param code Google "authorization code"
     * @return The token
     * @throws IOException If fails
     */
    private String token(final String code) throws IOException {
        return new JdkRequest("https://accounts.google.com/o/oauth2/token")
            .body()
            .formParam("client_id", this.app)
            .formParam("redirect_uri", this.resource.uriInfo().getBaseUri())
            .formParam("client_secret", this.secret)
            .formParam("grant_type", "authorization_code")
            .formParam("code", code)
            .back()
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .method(Request.POST)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(JsonResponse.class).json()
            .readObject()
            // @checkstyle MultipleStringLiterals (1 line)
            .getString("access_token");
    }

    /**
     * Get user name from Google, by the code provided.
     * @param token Google access token
     * @return The user found in Google
     * @throws IOException If fails
     */
    private Identity fetch(final String token) throws IOException {
        final URI uri = UriBuilder
            .fromPath("https://www.googleapis.com/oauth2/v1/userinfo")
            .queryParam("alt", "json")
            .queryParam("access_token", "{token}")
            .build(token);
        return this.parse(
            new JdkRequest(uri).fetch()
                .as(JsonResponse.class).json()
                .readObject()
        );
    }

    /**
     * Make identity from JSON object.
     * @param json JSON received from Google
     * @return Identity found
     */
    private Identity parse(final JsonObject json) {
        return new Identity.Simple(
            URN.create(String.format("urn:google:%s", json.getString("id"))),
            json.getString("name", Identity.ANONYMOUS.name()),
            URI.create(
                json.getString(
                    "picture",
                    Identity.ANONYMOUS.photo().toString()
                )
            )
        );
    }

}
