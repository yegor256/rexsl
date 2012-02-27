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
package com.rexsl.trap;

import com.ymock.util.Logger;
import java.net.URI;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Template from static URL, loaded just one on setup.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.6
 */
public final class StaticTemplate implements Template {

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
     * @param props The properties
     * @todo #167 We support only local resources now. This implementation
     *  has to be extended in order to support different formats of URI, incl.
     *  "file:...", "http:...", etc.
     */
    public StaticTemplate(final Properties props) {
        this.text = StaticTemplate.load(URI.create(props.getProperty("uri")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(final String defect) {
        return this.text.replace(
            this.MARKER,
            StringEscapeUtils.escapeHtml(defect)
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
        try {
            txt = IOUtils.toString(
                StaticTemplate.class.getResourceAsStream(uri.toString())
            );
        } catch (java.io.IOException ex) {
            txt = Logger.format(
                "%s\nfailed to load '%s': %[exception]s",
                StaticTemplate.MARKER,
                uri,
                ex
            );
        }
        if (txt.contains(StaticTemplate.MARKER)) {
            txt = String.format("%s\n%s", StaticTemplate.MARKER, txt);
        }
        return txt;
    }

}
