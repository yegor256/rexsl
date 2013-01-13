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
import java.util.Collection;
import java.util.Formattable;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Decor for HTTP request.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
final class RequestDecor implements Formattable {

    /**
     * End of line.
     */
    public static final String EOL = System.getProperty("line.separator");

    /**
     * Indentation.
     */
    public static final String SPACES = "    ";

    /**
     * The headers.
     */
    @NotNull
    private final transient Collection<Header> headers;

    /**
     * The body.
     */
    @NotNull
    private final transient String body;

    /**
     * Public ctor.
     * @param hdrs The headers
     * @param text Body text
     */
    public RequestDecor(@NotNull final Collection<Header> hdrs,
        @NotNull final String text) {
        this.headers = hdrs;
        this.body = text;
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (4 lines)
     */
    @Override
    public void formatTo(@NotNull final Formatter formatter, final int flags,
        final int width, final int precision) {
        final StringBuilder builder = new StringBuilder();
        for (Header header : this.headers) {
            builder.append(
                Logger.format(
                    "%s%s: %s%s",
                    RequestDecor.SPACES,
                    header.getKey(),
                    header.getValue(),
                    RequestDecor.EOL
                )
            );
        }
        builder.append(RequestDecor.EOL).append(RequestDecor.SPACES);
        if (this.body.isEmpty()) {
            builder.append("<<empty body>>");
        } else {
            builder.append(RequestDecor.indent(this.body))
                .append(RequestDecor.EOL);
        }
        formatter.format("%s", builder.toString());
    }

    /**
     * Indent this text.
     * @param text The text
     * @return Indented text
     */
    public static String indent(final String text) {
        return StringUtils.join(
            RequestDecor.lines(text),
            Logger.format("%s%s", RequestDecor.EOL, RequestDecor.SPACES)
        );
    }

    /**
     * Get all lines from the text.
     * @param text The text
     * @return Lines
     */
    private static List<String> lines(final String text) {
        final List<String> lines = new LinkedList<String>();
        for (String line : StringUtils.split(text, RequestDecor.EOL)) {
            lines.add(StringEscapeUtils.escapeJava(line));
        }
        return lines;
    }

}
