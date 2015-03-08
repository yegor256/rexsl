/**
 * Copyright (c) 2011-2015, ReXSL.com
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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

/**
 * Resolves resources using {@link ServletContext}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode(of = "context")
@Loggable(Loggable.DEBUG)
final class ContextResourceResolver implements URIResolver {

    /**
     * Servlet Context.
     */
    private final transient ServletContext context;

    /**
     * Opens connections.
     */
    private final transient ConnectionProvider provider;

    /**
     * Constructor.
     * @param ctx Servlet Context.
     */
    ContextResourceResolver(@NotNull final ServletContext ctx) {
        this(
            ctx,
            new ConnectionProvider() {
                @Override
                public URLConnection open(final URL url) throws IOException {
                    return url.openConnection();
                }
            }
        );
    }

    /**
     * Constructor.
     * @param ctx Servlet Context.
     * @param prov Connection provider.
     */
    ContextResourceResolver(@NotNull final ServletContext ctx,
        final ConnectionProvider prov) {
        this.context = ctx;
        this.provider = prov;
    }

    @Override
    @NotNull
    @SuppressWarnings({
        "PMD.ExceptionAsFlowControl", "PMD.PreserveStackTrace"
    })
    public Source resolve(@NotNull final String href, final String base)
        throws TransformerException {
        Source source;
        try {
            source = this.local(href, base);
        } catch (final TransformerException ex) {
            try {
                source = ContextResourceResolver.source(
                    ContextResourceResolver.absolute(href, base, this.provider)
                );
                source.setSystemId(href);
            } catch (final TransformerException exp) {
                throw new TransformerException(ex.getMessage(), exp);
            }
        }
        return source;
    }
    /**
     * Creates a source based on the provided stream.
     * @param stream The stream to take data from.
     * @return The source created from the stream.
     * @throws TransformerException If a problem happens inside.
     */
    private static Source source(final InputStream stream)
        throws TransformerException {
        final Source source;
        try {
            source = new StreamSource(
                IOUtils.toBufferedReader(
                    new InputStreamReader(
                        IOUtils.toInputStream(
                            IOUtils.toString(stream, CharEncoding.UTF_8),
                            CharEncoding.UTF_8
                        ),
                        CharEncoding.UTF_8
                    )
                )
            );
        } catch (final UnsupportedEncodingException ex) {
            throw new TransformerException(ex);
        } catch (final IOException ex) {
            throw new TransformerException(ex);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return source;
    }
    /**
     * Load stream from local address.
     * @param path The path to resource to load from
     * @param base Base of request
     * @return The stream opened or NULL if nothing found
     * @throws TransformerException If not found
     */
    private Source local(final String path, final String base)
        throws TransformerException {
        if (path.charAt(0) != '/' && path.charAt(0) != '.'
            && path.charAt(0) != '\\') {
            throw new TransformerException(
                String.format("'%s' is not a local path", path)
            );
        }
        final String abs = ContextResourceResolver.compose(path, base);
        final InputStream stream = this.context.getResourceAsStream(abs);
        if (stream == null) {
            throw new TransformerException(
                Logger.format(
                    // @checkstyle LineLength (1 line)
                    "local resource '%s' not found by %[type]s, abs='%s', realPath='%s'",
                    path, this.context, abs, this.context.getRealPath(abs)
                )
            );
        }
        final Source source = ContextResourceResolver.source(stream);
        source.setSystemId(abs);
        return source;
    }

    /**
     * Try to find and return a resource, which is absolute.
     * @param href HREF provided by the client
     * @param base Base
     * @param prov Connection provider.
     * @return The stream found
     * @throws TransformerException If not found
     */
    private static InputStream absolute(final String href, final String base,
        final ConnectionProvider prov)
        throws TransformerException {
        final URI uri;
        if (base == null || base.isEmpty()) {
            uri = UriBuilder.fromUri(href).build();
        } else {
            try {
                uri = new URL(new URL(base), href).toURI();
            } catch (final MalformedURLException ex) {
                throw new TransformerException(ex);
            } catch (final URISyntaxException ex) {
                throw new TransformerException(ex);
            }
        }
        if (!uri.isAbsolute()) {
            throw new TransformerException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Non-absolute URI '%s' can't be resolved, href='%s', base='%s'",
                    uri, href, base
                )
            );
        }
        try {
            return ContextResourceResolver.fetch(uri, prov);
        } catch (final IOException ex) {
            throw new TransformerException(
                String.format(
                    "failed to fetch absolute URI '%s', href='%s', base='%s'",
                    href, base, uri
                ),
                ex
            );
        }
    }

    /**
     * Load HTTP stream from URI.
     * @param uri The URI to load from
     * @param prov Connection provider.
     * @return The stream opened
     * @throws IOException If some problem happens
     */
    private static InputStream fetch(final URI uri,
        final ConnectionProvider prov)
        throws IOException {
        final URLConnection conn = prov.open(uri.toURL());
        final InputStream stream;
        if (conn instanceof HttpURLConnection) {
            stream = ContextResourceResolver.http(
                HttpURLConnection.class.cast(conn)
            );
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
    private static InputStream http(final HttpURLConnection conn)
        throws IOException {
        InputStream stream;
        try {
            conn.connect();
            final int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException(
                    Logger.format(
                        "Invalid HTTP response code %d at '%s'",
                        code, conn.getURL()
                    )
                );
            }
            stream = IOUtils.toInputStream(
                IOUtils.toString(conn.getInputStream(), CharEncoding.UTF_8),
                CharEncoding.UTF_8
            );
        } finally {
            conn.disconnect();
        }
        return stream;
    }

    /**
     * Compose a local path from path component and base.
     * @param path Path
     * @param base Base
     * @return Absolute local path
     */
    private static String compose(final String path, final String base) {
        final StringBuilder full = new StringBuilder(0);
        if (!StringUtils.isEmpty(base) && path.charAt(0) != '/'
            && path.charAt(0) != '\\') {
            full.append(
                FilenameUtils.getFullPath(URI.create(base).getPath())
            ).append('/');
        }
        full.append(URI.create(path).getPath());
        return URI.create(
            FilenameUtils.normalizeNoEndSeparator(full.toString())
                .replace(System.getProperty("file.separator"), "/")
        ).getPath();
    }

    /**
     * Provides URL connection for this class.
     */
    public interface ConnectionProvider {
        /**
         * Open connection to given URL resource.
         * @param url URL to open connection to.
         * @return Opened connection.
         * @throws IOException In case of problems when opening connection.
         */
        URLConnection open(URL url) throws IOException;
    }
}
