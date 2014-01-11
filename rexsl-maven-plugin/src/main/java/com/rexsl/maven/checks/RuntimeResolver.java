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
package com.rexsl.maven.checks;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

/**
 * Resolver of resources.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode(of = "home")
final class RuntimeResolver implements URIResolver {

    /**
     * Home page of the site.
     */
    private final transient URI home;

    /**
     * Public ctor.
     * @param uri The home page of the site
     */
    RuntimeResolver(@NotNull final URI uri) {
        this.home = UriBuilder.fromUri(uri).path("/").build();
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public Source resolve(@NotNull final String href, final String base)
        throws TransformerException {
        final URL url = this.url(href, base);
        final Source src;
        try {
            src = RuntimeResolver.fetch(url);
        } catch (IOException ex) {
            throw new TransformerException(
                String.format("failed to fetch '%s' at base '%s'", href, base),
                ex
            );
        }
        src.setSystemId(url.toString());
        Logger.debug(
            this, "#resolve(%s, %s): resolved from %s",
            href, base, url
        );
        return src;
    }

    /**
     * Make URL to fetch.
     * @param href Href requested
     * @param base Base
     * @return URL
     * @throws TransformerException If fails
     */
    private URL url(final String href, final String base)
        throws TransformerException {
        final URL url;
        final String abs = String.format(
            "%s%s", this.home, StringUtils.stripStart(href, "/ ")
        );
        try {
            if (base == null || base.isEmpty()) {
                url = new URL(abs);
            } else if (base.matches("https?://.*")) {
                url = new URL(new URL(base), href);
            } else if (href.charAt(0) == '.') {
                url = new URL(
                    FilenameUtils.normalize(
                        String.format(
                            "%s/%s", FilenameUtils.getFullPath(base), href
                        )
                    )
                );
            } else {
                url = new URL(new URL(base), abs);
            }
        } catch (MalformedURLException ex) {
            throw new TransformerException(
                String.format("URL failure of '%s' at base '%s'", href, base),
                ex
            );
        }
        return url;
    }

    /**
     * Fetch source from URL.
     * @param url The URL to load from
     * @return The source
     * @throws IOException If some IO problem inside
     */
    private static Source fetch(final URL url) throws IOException {
        final URLConnection conn = url.openConnection();
        final Source src;
        try {
            conn.connect();
            if (conn instanceof HttpURLConnection) {
                final int code = HttpURLConnection.class.cast(conn)
                    .getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    throw new IOException(
                        Logger.format(
                            "URL %s returned %d code (instead of %d)",
                            url, code, HttpURLConnection.HTTP_OK
                        )
                    );
                }
            }
            src = new StreamSource(
                IOUtils.toInputStream(
                    IOUtils.toString(conn.getInputStream(), CharEncoding.UTF_8),
                    CharEncoding.UTF_8
                )
            );
        } finally {
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection.class.cast(conn).disconnect();
            }
        }
        return src;
    }

}
