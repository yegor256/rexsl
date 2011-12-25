/**
 * Copyright (c) 2011, ReXSL.com
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

import com.ymock.util.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.servlet.ServletContext;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * Resolves resources using {@link ServletContext}.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
final class ContextResourceResolver implements URIResolver {

    /**
     * Servlet Context.
     */
    private final transient ServletContext context;

    /**
     * Constructor.
     * @param ctx Servlet Context.
     */
    public ContextResourceResolver(final ServletContext ctx) {
        this.context = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source resolve(final String href, final String base)
        throws TransformerException {
        InputStream stream = null;
        if (href.charAt(0) == '/') {
            stream = this.local(href);
        }
        final URI uri = UriBuilder.fromUri(href).build();
        if (stream == null) {
            if (uri.isAbsolute()) {
                try {
                    stream = this.fetch(uri);
                } catch (IOException ex) {
                    throw new TransformerException(ex);
                }
            } else {
                throw new TransformerException(
                    String.format(
                        "URI '%s' is not absolute, can't be resolved",
                        uri
                    )
                );
            }
        }
        Source source;
        try {
            source = new StreamSource(
                new BufferedReader(new InputStreamReader(stream, "UTF-8"))
            );
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new TransformerException(ex);
        }
        source.setSystemId(href);
        return source;
    }

    /**
     * Load stream from local address.
     * @param path The path to resource to load from
     * @return The stream opened or NULL if nothing found
     */
    private InputStream local(final String path) {
        final InputStream stream = this.context.getResourceAsStream(path);
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
     * Load stream from URI.
     * @param uri The URI to load from
     * @return The stream opened
     * @throws IOException If some problem happens
     */
    private InputStream fetch(final URI uri) throws IOException {
        final long start = System.currentTimeMillis();
        final HttpURLConnection conn =
            (HttpURLConnection) uri.toURL().openConnection();
        conn.connect();
        final int code = conn.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            throw new IOException(
                String.format(
                    "Invalid HTTP response code %d at '%s'",
                    code,
                    uri
                )
            );
        }
        Logger.debug(
            this,
            "#fetch('%s'): retrieved %d bytes of type '%s' [%dms]",
            uri,
            conn.getContentLength(),
            conn.getContentType(),
            System.currentTimeMillis() - start
        );
        return conn.getInputStream();
    }

}
