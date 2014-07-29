/**
 * Copyright (c) 2011-2014, ReXSL.com
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * LinkedIn authentication provider.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @see <a href="https://developer.linkedin.com/documents/authentication">OAuth in LinkedIn</a>
 * @todo #644 When issue 799 is complete add an integration test for LinkedIn
 *  provider.
 */
@ToString
@EqualsAndHashCode(of = { "app", "key" })
@Loggable(Loggable.DEBUG)
@Provider.Redirect
public final class LinkedIn implements Provider, Provider.Visible {

    /**
     * Query param.
     */
    private static final String FLAG = "rexsl-linkedin";

    /**
     * A long unique string value of your choice that is hard to guess.
     * Used to prevent CSRF.
     */
    private static final String STATE = RandomStringUtils.random(16);

    /**
     * Resource.
     */
    private final transient Resource resource;

    /**
     * LinkedIn ID.
     */
    private final transient String app;

    /**
     * LinkedIn secret key.
     */
    private final transient String key;

    /**
     * Public ctor.
     * @param res JAX-RS resource
     * @param aid Application id
     * @param secret Application secret key
     */
    public LinkedIn(@NotNull final Resource res,
        @NotNull final String aid, @NotNull final String secret) {
        this.resource = res;
        this.app = aid;
        this.key = secret;
    }

    @Override
    public Identity identity() throws IOException {
        Identity identity = Identity.ANONYMOUS;
        if (this.resource.uriInfo().getQueryParameters()
            .containsKey(LinkedIn.FLAG)) {
            final List<String> code = this.resource.uriInfo()
                // @checkstyle MultipleStringLiterals (1 line)
                .getQueryParameters().get("code");
            if (code == null || code.isEmpty()) {
                throw new WebApplicationException(
                    new IllegalArgumentException(
                        "HTTP query parameter 'code' is mandatory"
                    ),
                    HttpURLConnection.HTTP_BAD_REQUEST
                );
            }
            identity = this.fetch(this.token(code.get(0)));
        }
        return identity;
    }

    @Override
    public Link link() {
        return new Link(
            "rexsl:linkedin",
            UriBuilder
                .fromUri("https://www.linkedin.com/uas/oauth2/authorization")
                // @checkstyle MultipleStringLiterals (2 lines)
                .queryParam("client_id", "{client_id}")
                .queryParam("redirect_uri", "{redirect_uri}")
                .queryParam("state", "{state}")
                .build(this.app, this.redirectUri(), LinkedIn.STATE)
        );
    }

    /**
     * Redirect URI.
     * @return The URI
     */
    private URI redirectUri() {
        return this.resource.uriInfo().getRequestUriBuilder()
            .clone()
            .queryParam(LinkedIn.FLAG, "")
            .build();
    }

    /**
     * Retrieve LinkedIn access token.
     * @param code LinkedIn "authorization code"
     * @return The token
     * @throws java.io.IOException If failed
     */
    private String token(final String code) throws IOException {
        final URI uri = UriBuilder
            .fromUri("https://www.linkedin.com/uas/oauth2/accessToken")
            .queryParam("grant_type", "authorization_code")
            .queryParam("client_id", "{id}")
            .queryParam("redirect_uri", "{uri}")
            .queryParam("client_secret", "{secret}")
            .queryParam("code", "{code}")
            .build(
                this.app,
                this.redirectUri(),
                this.key,
                code
            );
        return new JdkRequest(uri)
            .method(Request.POST)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(JsonResponse.class)
            .json().readObject().getString("access_token");
    }

    /**
     * Get user name from LinkedIn, with the token provided.
     * @param token LinkedIn access token
     * @return The user found in LinkedIn
     * @throws java.io.IOException If fails
     */
    private Identity fetch(final String token) throws IOException {
        final URI uri = UriBuilder
            // @checkstyle LineLength (1 line)
            .fromUri("https://api.linkedin.com/v1/people/~:(id,first-name,last-name,picture-url)?format=json")
            .queryParam("oauth2_access_token", "{token}")
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
     * @param json JSON received from LinkedIn
     * @return Identity found
     */
    private Identity parse(final JsonObject json) {
        return new Identity.Simple(
            URN.create(String.format("urn:linkedin:%d", json.getInt("id"))),
            String.format(
                "%s %s",
                json.getString("first_name", Identity.ANONYMOUS.name()),
                json.getString("last_name", "")
            ).trim(),
            URI.create(
                json.getString(
                    "picture-url",
                    Identity.ANONYMOUS.photo().toString()
                )
            )
        );
    }

}
