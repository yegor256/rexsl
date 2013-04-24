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

import com.jcabi.log.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.ServletContext;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

/**
 * Resolves resources using {@link ServletContext}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode(of = "context")
final class ContextResourceResolver implements URIResolver {

    /**
     * Servlet Context.
     */
    private final transient ServletContext context;

    /**
     * Constructor.
     * @param ctx Servlet Context.
     */
    public ContextResourceResolver(@NotNull final ServletContext ctx) {
        this.context = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Source resolve(@NotNull final String href, final String base)
        throws TransformerException {
        InputStream stream = null;
        if (href.charAt(0) == '/') {
            stream = this.local(href);
        }
        if (stream == null) {
            stream = this.absolute(href, base);
        }
        final Source source = this.source(stream);
        IOUtils.closeQuietly(stream);
        source.setSystemId(href);
        return source;
    }
    /**
     * Creates a source based on the provided stream.
     * @param stream The stream to take data from.
     * @return The source created from the stream.
     * @throws TransformerException If a problem happens inside.
     */
    private Source source(final InputStream stream)
        throws TransformerException {
        final Source source;
        try {
            source = new StreamSource(
                new BufferedReader(
                    new InputStreamReader(
                        IOUtils.toInputStream(
                            IOUtils.toString(stream, CharEncoding.UTF_8),
                            CharEncoding.UTF_8
                        ),
                        CharEncoding.UTF_8
                    )
                )
            );
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new TransformerException(ex);
        } catch (IOException ex) {
            throw new TransformerException(ex);
        }
        return source;
    }
    /**
     * Load stream from local address.
     * @param path The path to resource to load from
     * @return The stream opened or NULL if nothing found
     */
    private InputStream local(final String path) {
        final InputStream stream = this.context.getResourceAsStream(
            URI.create(path).getPath()
        );
        if (stream != null) {
            Logger.debug(
                this,
                "#local('%s'): found local resource",
                path
            );
        }
        return stream;
    }

    /**
     * Try to find and return a resource, which is absolute.
     * @param href HREF provided by the client
     * @param base Base
     * @return The stream found
     * @throws TransformerException If fails
     */
    private InputStream absolute(final String href, final String base)
        throws TransformerException {
        URI uri;
        if (base == null || base.isEmpty()) {
            uri = UriBuilder.fromUri(href).build();
        } else {
            try {
                uri = new URL(new URL(base), href).toURI();
            } catch (MalformedURLException ex) {
                throw new TransformerException(ex);
            } catch (URISyntaxException ex) {
                throw new TransformerException(ex);
            }
        }
        if (!uri.isAbsolute()) {
            throw new TransformerException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Non-absolute URI '%s' can't be resolved, href='%s', base='%s'",
                    uri,
                    href,
                    base
                )
            );
        }
        try {
            return this.fetch(uri);
        } catch (IOException ex) {
            throw new TransformerException(
                String.format("failed to fetch absolute URI '%s'", uri),
                ex
            );
        }
    }

    /**
     * Load HTTP stream from URI.
     * @param uri The URI to load from
     * @return The stream opened
     * @throws IOException If some problem happens
     */
    private InputStream fetch(final URI uri) throws IOException {
        final URLConnection conn = uri.toURL().openConnection();
        InputStream stream;
        if (conn instanceof HttpURLConnection) {
            stream = this.http(HttpURLConnection.class.cast(conn));
        } else {
            stream = conn.getInputStream();
        }
        return stream;
    }

    /**
     * Fetch stream from HTTP connection.
     * @param conn The connection to fetch from
     * @return The stream opened
     * @throws IOException If some problem happens
     */
    private InputStream http(final HttpURLConnection conn) throws IOException {
        final long start = System.currentTimeMillis();
        InputStream stream;
        try {
            conn.connect();
            final int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException(
                    Logger.format(
                        "Invalid HTTP response code %d at '%s'",
                        code,
                        conn.getURL()
                    )
                );
            }
            Logger.debug(
                this,
                "#fetch('%s'): retrieved %d bytes of type '%s' in %[ms]s",
                conn.getURL(),
                conn.getContentLength(),
                conn.getContentType(),
                System.currentTimeMillis() - start
            );
            stream = IOUtils.toInputStream(
                IOUtils.toString(conn.getInputStream())
            );
        } finally {
            conn.disconnect();
        }
        return stream;
    }

}
