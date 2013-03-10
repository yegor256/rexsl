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
package com.rexsl.page.inset;

import com.jcabi.aspects.Loggable;
import com.rexsl.page.BasePage;
import com.rexsl.page.CookieBuilder;
import com.rexsl.page.Inset;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Resource;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.CharEncoding;

/**
 * Page with a flash message (through cookie).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.7
 * @see BasePage
 */
@ToString
@EqualsAndHashCode(of = "resource")
public class FlashInset implements Inset {

    /**
     * Header name.
     */
    private static final String HEADER = "X-Rexsl-Flash";

    /**
     * Cookie name.
     */
    private static final String COOKIE = "Rexsl-Flash";

    /**
     * The resource.
     */
    private final transient Resource resource;

    /**
     * Public ctor.
     * @param res The resource
     */
    public FlashInset(@NotNull final Resource res) {
        this.resource = res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public final void render(@NotNull final BasePage<?, ?> page,
        @NotNull final Response.ResponseBuilder builder) {
        if (this.resource.httpHeaders().getCookies()
            .containsKey(FlashInset.COOKIE)) {
            final FlashInset.Flash cookie = new FlashInset.Flash(
                this.resource.httpHeaders().getCookies().get(FlashInset.COOKIE)
            );
            page.append(
                new JaxbBundle("flash")
                    .add("message", cookie.message())
                    .up()
                    .add("level", cookie.level())
                    .up()
            );
            builder.cookie(
                new CookieBuilder(this.resource.uriInfo().getBaseUri())
                    .name(FlashInset.COOKIE)
                    .build()
            );
        }
    }

    /**
     * Create an exception that will forward to the page with an error message.
     * @param uri The URI to forward to
     * @param message The message to show as error
     * @param level Message level
     * @return The exception to throw
     */
    @Loggable(Loggable.DEBUG)
    public static WebApplicationException forward(@NotNull final URI uri,
        @NotNull final String message, @NotNull final Level level) {
        return new WebApplicationException(
            Response.status(HttpURLConnection.HTTP_SEE_OTHER)
                .location(uri)
                .cookie(new FlashInset.Flash(uri, message, Level.SEVERE))
                .header(FlashInset.HEADER, message)
                .entity(message)
                .build()
        );
    }

    /**
     * Throw an exception that will forward to the page with an error message.
     * @param uri The URI to forward to
     * @param cause The cause of this problem
     * @return The exception to throw
     */
    public static WebApplicationException forward(@NotNull final URI uri,
        @NotNull final Exception cause) {
        return FlashInset.forward(uri, cause.getMessage(), Level.SEVERE);
    }

    /**
     * The cookie.
     */
    public static final class Flash extends NewCookie {
        /**
         * The message.
         */
        private final transient String msg;
        /**
         * Level of message.
         */
        private final transient Level lvl;
        /**
         * Public ctor, from a cookie encoded text.
         * @param cookie The cookie
         */
        public Flash(@NotNull final Cookie cookie) {
            super(FlashInset.COOKIE, cookie.getValue());
            String[] parts;
            try {
                parts = new String(
                    new Base32().decode(this.getValue()), CharEncoding.UTF_8
                ).split(":", 2);
            } catch (java.io.UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                    String.format("can't decode cookie '%s'", this.getValue())
                );
            }
            if (!parts[0].matches("INFO|WARNING|SEVERE")) {
                throw new IllegalArgumentException(
                    String.format("invalid cookie '%s'", this.getValue())
                );
            }
            this.lvl = Level.parse(parts[0]);
            this.msg = parts[1];
        }
        /**
         * Public ctor, from exact values.
         * @param base Base URI where we're using it
         * @param message The message
         * @param level The level
         */
        public Flash(@NotNull final URI base,
            @NotNull final String message, @NotNull final Level level) {
            super(
                new CookieBuilder(base)
                    .name(FlashInset.COOKIE)
                    .value(FlashInset.Flash.encode(message, level))
                    .temporary()
                    .build()
            );
            this.msg = message;
            this.lvl = level;
        }
        /**
         * Get message.
         * @return The message
         */
        public String message() {
            return this.msg;
        }
        /**
         * Get color of it.
         * @return The color
         */
        public Level level() {
            return this.lvl;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String getPath() {
            return "/";
        }
        /**
         * Encode message and color.
         * @param message The message
         * @param level The level
         * @return Encoded cookie value
         */
        private static String encode(final String message, final Level level) {
            try {
                return new Base32().encodeToString(
                    String.format("%s:%s", level, message)
                        .getBytes(CharEncoding.UTF_8)
                );
            } catch (java.io.UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }

    }

}
