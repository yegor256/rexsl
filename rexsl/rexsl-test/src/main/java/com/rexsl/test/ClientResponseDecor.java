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
package com.rexsl.test;

import com.sun.jersey.api.client.ClientResponse;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Decor for {@link ClientResponse}.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class ClientResponseDecor implements Formattable {

    /**
     * End of line.
     */
    private static final String EOL = "\n";

    /**
     * Indentation.
     */
    private static final String INDENT = "    ";

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
    public ClientResponseDecor(final ClientResponse resp, final String text) {
        this.response = resp;
        this.body = text;
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (4 lines)
     */
    @Override
    public void formatTo(final Formatter formatter, final int flags,
        final int width, final int precision) {
        final StringBuilder builder = new StringBuilder();
        for (MultivaluedMap.Entry<String, List<String>> header
            : this.response.getHeaders().entrySet()) {
            builder.append(
                Logger.format(
                    "%s%s: %s%s",
                    this.INDENT,
                    header.getKey(),
                    StringUtils.join(header.getValue(), ", "),
                    this.EOL
                )
            );
        }
        if (builder.length() > 0) {
            builder.append(this.EOL);
        }
        builder.append(this.INDENT)
            .append(
                StringUtils.join(
                    this.lines(this.body),
                    Logger.format("%s%s", this.EOL, this.INDENT)
                )
            )
            .append(this.EOL);
        formatter.format("%s", builder.toString());
    }

    /**
     * Get all lines from the text.
     * @param text The text
     * @return Lines
     */
    private List<String> lines(final String text) {
        final List<String> lines = new ArrayList<String>();
        for (String line : StringUtils.split(text, this.EOL)) {
            lines.add(StringEscapeUtils.escapeJava(line));
        }
        return lines;
    }

}
