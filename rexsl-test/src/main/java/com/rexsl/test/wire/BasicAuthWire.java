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
package com.rexsl.test.wire;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.rexsl.test.ImmutableHeader;
import com.rexsl.test.Request;
import com.rexsl.test.Response;
import com.rexsl.test.Wire;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.CharEncoding;

/**
 * Wire with HTTP basic authentication based on user info of URI.
 *
 * <p>This wire converts user info from URI into
 * {@code "Authorization"} HTTP header, for example:
 *
 * <pre> String html = new JdkRequest("http://jeff:12345@example.com")
 *   .through(BasicAuthWire.class)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>In this example, an additional HTTP header {@code Authorization}
 * will be added with a value {@code Basic amVmZjoxMjM0NQ==}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.10
 * @see <a href="http://tools.ietf.org/html/rfc2617">RFC 2617 "HTTP Authentication: Basic and Digest Access Authentication"</a>
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class BasicAuthWire implements Wire {

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public BasicAuthWire(@NotNull(message = "wire can't be NULL")
        final Wire wire) {
        this.origin = wire;
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (7 lines)
     */
    @Override
    public Response send(final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final byte[] content) throws IOException {
        final Collection<Map.Entry<String, String>> hdrs =
            new LinkedList<Map.Entry<String, String>>();
        hdrs.addAll(headers);
        final String info = URI.create(home).getUserInfo();
        if (info != null) {
            final String[] parts = info.split(":", 2);
            try {
                hdrs.add(
                    new ImmutableHeader(
                        HttpHeaders.AUTHORIZATION,
                        Logger.format(
                            "Basic %s",
                            Base64.encodeBase64String(
                                Logger.format(
                                    "%s:%s",
                                    URLEncoder.encode(
                                        parts[0], CharEncoding.UTF_8
                                    ),
                                    URLEncoder.encode(
                                        parts[1], CharEncoding.UTF_8
                                    )
                                ).getBytes(Charsets.UTF_8)
                            )
                        )
                    )
                );
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return this.origin.send(req, home, method, hdrs, content);
    }
}
