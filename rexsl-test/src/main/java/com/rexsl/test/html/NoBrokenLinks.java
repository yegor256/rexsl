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
package com.rexsl.test.html;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * Matches HTTP header against required value.
 *
 * <p>Use it in combination with {@link com.rexsl.test.RestTester},
 * in order to detect possibly
 * broken links in the HTML output, for example:
 *
 * <pre> RestTester.start(new URI("http://www.rexsl.com/"))
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
 *   .get("front page of ReXSL.com")
 *   .assertThat(new NoBrokenLinks())</pre>
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3.4
 */
@ToString
@EqualsAndHashCode(of = "home")
@Loggable(Loggable.DEBUG)
public final class NoBrokenLinks implements AssertionPolicy {

    /**
     * Home page.
     */
    private final transient URI home;

    /**
     * Public ctor.
     * @param uri Home page URI, for relative links
     */
    public NoBrokenLinks(final URI uri) {
        this.home = uri;
    }

    @Override
    public void assertThat(final TestResponse response) {
        final Collection<String> links = response.xpath(
            // @checkstyle LineLength (1 line)
            "//head/link/@href | //body//a/@href | //body//img/@src | //xhtml:img/@src | //xhtml:a/@href | //xhtml:link/@href"
        );
        Logger.debug(
            this,
            "#assertThat(): %d links found: %[list]s",
            links.size(),
            links
        );
        final Collection<URI> broken = new LinkedList<URI>();
        for (String link : links) {
            URI uri;
            if (link.isEmpty() || link.charAt(0) != '/') {
                uri = URI.create(link);
            } else {
                uri = this.home.resolve(link);
            }
            if (!uri.isAbsolute() || !NoBrokenLinks.isValid(uri)) {
                broken.add(uri);
            }
        }
        MatcherAssert.assertThat(
            Logger.format(
                "%d broken link(s) found: %[list]s",
                broken.size(),
                broken
            ),
            broken,
            Matchers.empty()
        );
    }

    @Override
    public boolean isRetryNeeded(final int attempt) {
        return false;
    }

    /**
     * Check whether the URI is valid and returns code 200.
     * @param uri The URI to check
     * @return TRUE if it's valid
     */
    private static boolean isValid(final URI uri) {
        boolean valid = false;
        try {
            final int code = NoBrokenLinks.http(uri.toURL());
            if (code < HttpURLConnection.HTTP_BAD_REQUEST) {
                valid = true;
            } else {
                Logger.warn(
                    NoBrokenLinks.class,
                    "#isValid('%s'): not valid since responde code=%d",
                    uri,
                    code
                );
            }
        } catch (java.net.MalformedURLException ex) {
            Logger.warn(
                NoBrokenLinks.class,
                "#isValid('%s'): invalid URL: %s",
                uri,
                ex.getMessage()
            );
        }
        return valid;
    }

    /**
     * Get HTTP response code from this URL.
     * @param url The URL to get
     * @return HTTP response code
     */
    private static int http(final URL url) {
        int code = HttpURLConnection.HTTP_BAD_REQUEST;
        HttpURLConnection conn;
        try {
            conn = HttpURLConnection.class.cast(url.openConnection());
            try {
                code = conn.getResponseCode();
            } catch (java.io.IOException ex) {
                Logger.warn(
                    NoBrokenLinks.class,
                    "#http('%s'): can't get response code: %s",
                    url,
                    ex.getMessage()
                );
            } finally {
                conn.disconnect();
            }
        } catch (java.io.IOException ex) {
            Logger.warn(
                NoBrokenLinks.class,
                "#http('%s'): can't open connection: %s",
                url,
                ex.getMessage()
            );
        }
        return code;
    }

}
