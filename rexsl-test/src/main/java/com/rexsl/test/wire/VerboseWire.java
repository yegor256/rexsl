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
import com.rexsl.test.Request;
import com.rexsl.test.RequestBody;
import com.rexsl.test.Response;
import com.rexsl.test.Wire;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Verbose wire.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.10
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class VerboseWire implements Wire {

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public VerboseWire(@NotNull(message = "wire can't be NULL")
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
        final Response response = this.origin.send(
            req, home, method, headers, content
        );
        final StringBuilder text = new StringBuilder(0);
        for (final Map.Entry<String, String> header : headers) {
            text.append(header.getKey())
                .append(": ")
                .append(header.getValue())
                .append('\n');
        }
        text.append('\n').append(RequestBody.Printable.toString(content));
        Logger.info(
            this,
            "#send(%s %s):\nHTTP Request (%s):\n%s\nHTTP Response (%s):\n%s",
            method, home,
            req.getClass().getName(),
            VerboseWire.indent(text.toString()),
            response.getClass().getName(),
            VerboseWire.indent(response.toString())
        );
        return response;
    }

    /**
     * Indent provided text.
     * @param text Text to indent
     * @return Indented text
     */
    private static String indent(final String text) {
        return new StringBuilder("  ")
            .append(text.replaceAll("(\n|\n\r)", "$1  "))
            .toString();
    }

}