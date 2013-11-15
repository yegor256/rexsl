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
package com.rexsl.core;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Template from static URL, loaded just once on setup.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.2
 */
@ToString
@EqualsAndHashCode(of = "text")
@Immutable
final class StaticTemplate implements Template {

    /**
     * Marker to replace with text.
     */
    private static final String MARKER = "${text}";

    /**
     * Text.
     */
    private final transient String text;

    /**
     * Public ctor.
     * @param uri URI of template to use
     */
    StaticTemplate(@NotNull final URI uri) {
        this.text = StaticTemplate.load(uri);
    }

    @Override
    @NotNull
    public String render(@NotNull final String defect) {
        return this.text.replace(
            StaticTemplate.MARKER,
            StringEscapeUtils.escapeHtml4(defect)
        );
    }

    /**
     * Load template from URI.
     * @param uri The URI to load from
     * @return The text just loaded
     * @todo #167 We support only local resources now. This implementation
     *  has to be extended in order to support different formats of URI, incl.
     *  "file:...", "http:...", etc.
     */
    private static String load(final URI uri) {
        String txt;
        final InputStream stream = StaticTemplate.class
            .getResourceAsStream(uri.toString());
        try {
            if (stream == null) {
                txt = Logger.format(
                    "%s\nresource '%s' not found",
                    StaticTemplate.MARKER,
                    uri
                );
            } else {
                txt = IOUtils.toString(stream);
            }
        } catch (IOException ex) {
            txt = Logger.format(
                "%s\nfailed to load '%s': %[exception]s",
                StaticTemplate.MARKER,
                uri,
                ex
            );
        } finally {
            IOUtils.closeQuietly(stream);
        }
        if (!txt.contains(StaticTemplate.MARKER)) {
            txt = Logger.format("%s\n%s", StaticTemplate.MARKER, txt);
        }
        return txt;
    }

}
