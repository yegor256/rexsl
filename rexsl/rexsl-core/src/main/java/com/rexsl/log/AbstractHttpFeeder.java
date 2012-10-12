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
package com.rexsl.log;

import com.jcabi.log.Logger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Abstract feeder through HTTP POST request.
 *
 * <p>The class has to be public in order to allow LOG4J to access it.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.2
 */
public abstract class AbstractHttpFeeder implements Feeder {

    /**
     * The URL to post to.
     */
    private transient URL url;

    /**
     * HTTP Request Properties.
     */
    private final transient ConcurrentMap<String, String> props =
        new ConcurrentHashMap<String, String>();

    /**
     * Set option {@code url}.
     * @param addr The URL
     * @throws java.net.MalformedURLException If format of the URL is
     *  not correct
     */
    public final void setUrl(@NotNull final String addr)
        throws java.net.MalformedURLException {
        this.url = new URL(addr);
        this.props.clear();
        this.props.put(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
        final String auth = this.getUrl().getUserInfo();
        if (auth != null) {
            final StringBuffer authval = new StringBuffer();
            final String encauth = Base64.encodeBase64String(auth.getBytes());
            authval.append("Basic ").append(encauth);
            this.props.put(HttpHeaders.AUTHORIZATION, authval.toString());
        }
    }

    /**
     * Get URL.
     * @return The URL
     */
    protected final URL getUrl() {
        if (this.url == null) {
            throw new IllegalStateException("URL is not configured");
        }
        return this.url;
    }

    /**
     * POST one line of text.
     * @param text The text to post
     * @throws IOException If failed to post
     * @todo #341 Would be much better to use RestTester here, but it
     *  will mean that rexsl-test module will become a transitive dependency
     *  of rexsl-core, which is not a good idea..
     */
    protected final void post(@NotNull final String text) throws IOException {
        final HttpURLConnection conn  = this.connect();
        try {
            conn.setConnectTimeout((int) TimeUnit.MINUTES.toMillis(1L));
            conn.setReadTimeout((int) TimeUnit.MINUTES.toMillis(1L));
            conn.setDoOutput(true);
            try {
                conn.setRequestMethod("POST");
            } catch (java.net.ProtocolException ex) {
                throw new IOException(ex);
            }
            IOUtils.write(
                text,
                conn.getOutputStream(),
                CharEncoding.UTF_8
            );
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(
                    Logger.format(
                        "Invalid response code #%d from %s",
                        conn.getResponseCode(),
                        this.url
                    )
                );
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Creates a new HttpURLConnection from the existing URL.
     * @throws IOException If a problem happens inside.
     * @return HttpURLConnection HTTP URL Connection.
     */
    private HttpURLConnection connect() throws IOException {
        final HttpURLConnection conn =
            (HttpURLConnection) this.getUrl().openConnection();
        for (String key : this.props.keySet()) {
            conn.setRequestProperty(key, this.props.get(key));
        }
        return conn;
    }
}
