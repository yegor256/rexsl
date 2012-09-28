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
package com.rexsl.misc;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;

/**
 * Cookie builder.
 *
 * <p>It is a convenient cookie builder for JAX-RS responses, for example:
 *
 * <pre>Response.ok().cookie(
 *   new CookieBuilder(this.uriInfo().getBaseUri())
 *     .name("my-cookie")
 *     .value("some value of the cookie")
 *     .temporary()
 *     .build()
 * );</pre>
 *
 * <p>When you want to instruct the client to delete the cookie:
 *
 * <pre>Response.ok().cookie(
 *   new CookieBuilder(this.uriInfo().getBaseUri())
 *     .name("my-cookie")
 *     .build()
 * );</pre>
 *
 * <p>It is much more convenient than {@code new NewCookie(..)} from JAX-RS.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @see <a href="http://tools.ietf.org/html/rfc6265">RFC6265</a>
 * @todo #381 Somehow we should specify PORT in the cookie. Without this param
 *  the site doesn't work in localhost:9099 in Chrome. Works fine in Safari,
 *  but not in Chrome. see http://stackoverflow.com/questions/1612177
 */
public final class CookieBuilder {

    /**
     * Domain.
     */
    private final transient String domain;

    /**
     * Name of cookie.
     */
    private transient String cookie = "unknown";

    /**
     * Value.
     *
     * <p>By default it has to contain some rubbish, in order to replace
     * the value cached/stored by a client.
     *
     * @see http://trac.fazend.com/rexsl/ticket/581
     */
    private transient String val = "deleted";

    /**
     * Path.
     */
    private transient String url;

    /**
     * When it should expire.
     *
     * <p>By default it is already expired.
     */
    private transient Date expires = new Date(0);

    /**
     * Public ctor.
     * @param uri The URI
     */
    public CookieBuilder(@NotNull final URI uri) {
        this.domain = uri.getHost();
        this.url = uri.getPath();
    }

    /**
     * Public ctor.
     * @param builder The URI builder
     */
    public CookieBuilder(@NotNull final UriBuilder builder) {
        this(builder.build());
    }

    /**
     * Named like this.
     * @param txt The name
     * @return This object
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-2.2">RFC2616</a>
     */
    public CookieBuilder name(@NotNull final String txt) {
        if (!txt.matches("[\\x20-\\x7E]+")) {
            throw new IllegalArgumentException(
                String.format("illegal cookie name: '%s'", txt)
            );
        }
        this.cookie = txt;
        return this;
    }

    /**
     * With value like this.
     * @param txt The value
     * @return This object
     */
    public CookieBuilder value(@NotNull final String txt) {
        // @checkstyle LineLength (1 line)
        if (!txt.matches("[\\x21\\x23-\\x2B\\x2D-\\x3A\\x3C-\\x5B\\x5D-\\x7E]*")) {
            throw new IllegalArgumentException(
                String.format("illegal cookie value: '%s'", txt)
            );
        }
        this.val = txt;
        return this;
    }

    /**
     * Set path.
     * @param txt The path
     * @return This object
     */
    public CookieBuilder path(@NotNull final String txt) {
        if (!txt.matches("/[\\x20-\\x3A\\x3C-\\x7E]*")) {
            throw new IllegalArgumentException(
                String.format("illegal cookie path: '%s'", txt)
            );
        }
        this.url = txt;
        return this;
    }

    /**
     * Make this cookie temporary, with 90 days age.
     * @return This object
     */
    public CookieBuilder temporary() {
        // @checkstyle MagicNumber (1 line)
        this.days(90);
        return this;
    }

    /**
     * Make this cookie temporary, with certain pre-defined age in days.
     * @param days How many days to live
     * @return This object
     */
    public CookieBuilder days(final int days) {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        this.expires = cal.getTime();
        return this;
    }

    /**
     * Build cookie.
     * @return The cookie string to be used in "Set-cookie" header.
     */
    public NewCookie build() {
        return new NewCookie(this.cookie, this.val) {
            @Override
            public String toString() {
                return String.format(
                    Locale.ENGLISH,
                    // @checkstyle LineLength (1 line)
                    "%s=\"%s\"; Domain=%s; Path=%s; Expires=%ta, %5$td-%5$tb-%5$tY %5$tT GMT",
                    this.getName(),
                    this.getValue(),
                    CookieBuilder.this.domain,
                    CookieBuilder.this.url,
                    CookieBuilder.this.expires
                );
            }
        };
    }

}
