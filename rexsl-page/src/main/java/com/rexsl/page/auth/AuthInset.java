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

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rexsl.page.BasePage;
import com.rexsl.page.CookieBuilder;
import com.rexsl.page.Inset;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.Resource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Authentication inset.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.8
 * @see <a href="http://www.rexsl.com/rexsl-page/inset-oauth.html">OAuth in RESTful Interfaces</a>
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@ToString
@EqualsAndHashCode(of = { "resource", "key" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class AuthInset implements Inset {

    /**
     * Name of identity authentication cookie.
     */
    private static final String AUTH_COOKIE = "Rexsl-Auth";

    /**
     * Logout Query param.
     */
    private static final String LOGOUT_FLAG = "rexsl-logout";

    /**
     * The resource.
     */
    private final transient Resource resource;

    /**
     * Security key.
     */
    private final transient String key;

    /**
     * Providers.
     */
    private final transient Set<Provider> providers = new HashSet<Provider>(0);

    /**
     * Public ctor.
     * @param res The resource
     * @param sec Security key
     */
    public AuthInset(@NotNull final Resource res, @NotNull final String sec) {
        this.resource = res;
        this.key = sec;
    }

    /**
     * Encrypt identity into text.
     * @param identity The identity to encrypt
     * @param key Security key
     * @return Encrypted text for cookie
     */
    public static String encrypt(@NotNull final Identity identity,
        @NotNull final String key) {
        return new Encrypted(identity, key).cookie();
    }

    /**
     * Encrypt identity into text.
     * @param identity The identity to encrypt
     * @return Encrypted text for cookie
     * @since 0.5
     */
    public String encrypt(@NotNull final Identity identity) {
        return AuthInset.encrypt(identity, this.key);
    }

    /**
     * With this authentication provider.
     * @param prov Additional authentication provider
     * @return This object
     */
    public AuthInset with(@NotNull final Provider prov) {
        this.providers.add(prov);
        return this;
    }

    /**
     * Get user's identity ({@link AuthException}
     * if not authenticated).
     * @return Identity, if authenticated
     */
    @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS)
    public Identity identity() {
        Identity identity = this.ofProviders();
        if (identity.equals(Identity.ANONYMOUS)) {
            identity = this.ofCookies();
        }
        return identity;
    }

    @Override
    public void render(@NotNull final BasePage<?, ?> page,
        @NotNull final Response.ResponseBuilder builder) {
        final Identity identity = this.identity();
        if (identity.equals(Identity.ANONYMOUS)) {
            for (final Provider prov : this.providers) {
                if (prov instanceof Provider.Visible) {
                    page.link(Provider.Visible.class.cast(prov).link());
                }
            }
        } else {
            page.append(
                new JaxbBundle("identity")
                    .add("urn", identity.urn().toString())
                    .up()
                    .add("name", identity.name())
                    .up()
                    .add("photo", identity.photo().toString())
                    .up()
                    .add("token", this.encrypt(identity))
                    .up()
            );
            page.link(
                new Link(
                    "auth-logout",
                    this.resource.uriInfo().getRequestUriBuilder()
                        .clone()
                        .queryParam(AuthInset.LOGOUT_FLAG, true)
                        .build()
                )
            );
            builder.cookie(this.cookie(identity));
            builder.header("X-Rexsl-Identity", identity.urn());
        }
        if (this.resource.uriInfo().getQueryParameters()
            .containsKey(AuthInset.LOGOUT_FLAG)) {
            throw new AuthException(
                Response.status(HttpURLConnection.HTTP_SEE_OTHER).location(
                    this.resource.uriInfo().getRequestUriBuilder()
                        .replaceQuery("")
                        .build()
                ).cookie(this.logout()).build(),
                String.format(
                    "redirecting because of '%s' flag in HTTP query",
                    AuthInset.LOGOUT_FLAG
                )
            );
        }
    }

    /**
     * Authentication cookie.
     * @param identity The identity to wrap into the cookie
     * @return The cookie
     */
    public NewCookie cookie(final Identity identity) {
        return new CookieBuilder(this.resource.uriInfo().getBaseUri())
            .name(AuthInset.AUTH_COOKIE)
            .value(new Encrypted(identity, this.key).cookie())
            .temporary()
            .build();
    }

    /**
     * Logout authentication cookie.
     *
     * <p>Use this cookie to log user out of the system, for example:
     *
     * <pre> if (you_are_not_allowed()) {
     *   throw new AuthException(
     *     Response.seeOther(this.uriInfo().getBaseUri())
     *       .cookie(this.auth().logout())
     *       .build()
     *   );
     * }
     * </pre>
     *
     * @return The cookie
     */
    @Cacheable.FlushAfter
    public NewCookie logout() {
        return new CookieBuilder(this.resource.uriInfo().getBaseUri())
            .name(AuthInset.AUTH_COOKIE)
            .build();
    }

    /**
     * Authenticate using providers.
     * @return Identity found or ANONYMOUS
     */
    private Identity ofProviders() {
        Identity identity = Identity.ANONYMOUS;
        for (final Provider prov : this.providers) {
            try {
                identity = prov.identity();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            if (identity.equals(Identity.ANONYMOUS)) {
                continue;
            }
            if (prov.getClass().isAnnotationPresent(Provider.Redirect.class)) {
                throw new AuthException(
                    Response.status(HttpURLConnection.HTTP_SEE_OTHER).location(
                        this.resource.uriInfo().getRequestUriBuilder()
                            .replaceQuery("")
                            .build()
                    ).cookie(this.cookie(identity)).build(),
                    Logger.format(
                        "redirecting because of @Provider.Redirect at %[type]s",
                        prov
                    )
                );
            }
            break;
        }
        return identity;
    }

    /**
     * Authenticate using cookies.
     * @return Identity found or ANONYMOUS
     */
    private Identity ofCookies() {
        Identity identity = Identity.ANONYMOUS;
        if (this.resource.httpHeaders().getCookies()
            .containsKey(AuthInset.AUTH_COOKIE)) {
            final String cookie = this.resource.httpHeaders().getCookies()
                .get(AuthInset.AUTH_COOKIE).getValue();
            try {
                identity = new Identity.Simple(
                    Encrypted.parse(cookie, this.key)
                );
            } catch (Encrypted.DecryptionException ex) {
                Logger.warn(
                    this,
                    "Failed to decrypt '%s' from '%s' to '%s': %[exception]s",
                    cookie,
                    this.resource.httpServletRequest().getRemoteAddr(),
                    this.resource.httpServletRequest().getRequestURI(),
                    ex
                );
            }
        } else {
            Logger.debug(
                this, "cookie '%s' not found among %[list]s",
                AuthInset.AUTH_COOKIE,
                this.resource.httpHeaders().getCookies().keySet()
            );
        }
        return identity;
    }

}
