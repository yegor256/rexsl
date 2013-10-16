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
import com.rexsl.page.Resource;
import com.rexsl.page.inset.FlashInset;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

/**
 * HTTP Basic authentication provider.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.5
 * @see <a href="http://en.wikipedia.org/wiki/Basic_access_authentication">HTTP Basic Auth</a>
 */
@ToString
@EqualsAndHashCode(of = "vault")
@Loggable(Loggable.DEBUG)
public final class HttpBasic implements Provider {

    /**
     * Never authenticating vault.
     */
    public static final HttpBasic.Vault NEVER = new HttpBasic.Vault() {
        @Override
        public Identity authenticate(final String user, final String password) {
            return Identity.ANONYMOUS;
        }
    };

    /**
     * Resource.
     */
    private final transient Resource resource;

    /**
     * Vault that enables access.
     */
    private final transient HttpBasic.Vault vault;

    /**
     * Public ctor.
     * @param res JAX-RS resource
     * @param vlt Authentication vault
     */
    public HttpBasic(@NotNull final Resource res,
        @NotNull final HttpBasic.Vault vlt) {
        this.resource = res;
        this.vault = vlt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity identity() throws IOException {
        final List<String> headers = this.resource.httpHeaders()
            .getRequestHeader(HttpHeaders.AUTHORIZATION);
        Identity identity = Identity.ANONYMOUS;
        if (headers != null && !headers.isEmpty()) {
            identity = this.parse(headers.get(0));
        }
        return identity;
    }

    /**
     * Parse header and return identity.
     * @param header The HTTP header to parse
     * @return Identity found (or anonymous)
     * @throws IOException If fails
     */
    private Identity parse(final String header) throws IOException {
        final String[] parts = header.split("\\s+");
        if (!"Basic".equals(parts[0])) {
            throw FlashInset.forward(
                this.resource.uriInfo().getBaseUri(),
                String.format(
                    "Invalid authentication scheme '%s'",
                    parts[0]
                ),
                Level.SEVERE
            );
        }
        final String body = new String(
            Base64.decodeBase64(parts[1]), Charsets.UTF_8
        );
        final String[] tokens = StringUtils.splitPreserveAllTokens(body, ':');
        if (tokens.length != 2) {
            throw FlashInset.forward(
                this.resource.uriInfo().getBaseUri(),
                String.format(
                    "Authentication body '%s' has %d part(s), two expected",
                    body,
                    tokens.length
                ),
                Level.SEVERE
            );
        }
        return this.vault.authenticate(
            URLDecoder.decode(tokens[0], CharEncoding.UTF_8),
            URLDecoder.decode(tokens[1], CharEncoding.UTF_8)
        );
    }

    /**
     * Vault the authenticates.
     */
    public interface Vault {
        /**
         * Authenticate or not.
         * @param user User name
         * @param password Password
         * @return Authenticated identity or {@link Identity.ANONYMOUS}
         */
        Identity authenticate(String user, String password);
    }

}
