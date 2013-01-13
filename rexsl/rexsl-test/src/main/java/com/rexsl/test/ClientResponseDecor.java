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
package com.rexsl.test;

import com.jcabi.log.Logger;
import com.sun.jersey.api.client.ClientResponse;
import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;

/**
 * Decor for {@link ClientResponse}.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
final class ClientResponseDecor implements Formattable {

    /**
     * The response.
     */
    private final transient ClientResponse response;

    /**
     * Its body.
     */
    private final transient String body;

    /**
     * Public ctor.
     * @param resp The response
     * @param text Body text
     */
    public ClientResponseDecor(@NotNull final ClientResponse resp,
        @NotNull final String text) {
        this.response = resp;
        this.body = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final Formatter fmt = new Formatter();
        this.formatTo(fmt, 0, 0, 0);
        return fmt.toString();
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (4 lines)
     */
    @Override
    public void formatTo(@NotNull final Formatter formatter, final int flags,
        final int width, final int precision) {
        final StringBuilder builder = new StringBuilder();
        for (MultivaluedMap.Entry<String, List<String>> header
            : this.response.getHeaders().entrySet()) {
            builder.append(
                Logger.format(
                    "%s%s: %s%s",
                    RequestDecor.SPACES,
                    header.getKey(),
                    StringUtils.join(header.getValue(), ", "),
                    RequestDecor.EOL
                )
            );
        }
        if (builder.length() > 0) {
            builder.append(RequestDecor.EOL);
        }
        builder.append(RequestDecor.SPACES)
            .append(RequestDecor.indent(this.body))
            .append(RequestDecor.EOL);
        formatter.format("%s", builder.toString());
    }

}
