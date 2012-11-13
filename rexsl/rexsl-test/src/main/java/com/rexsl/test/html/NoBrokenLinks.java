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
package com.rexsl.test.html;

import com.jcabi.log.Logger;
import com.rexsl.test.AssertionPolicy;
import com.rexsl.test.RestTester;
import com.rexsl.test.TestResponse;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * Matches HTTP header against required value.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3.4
 */
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

    /**
     * {@inheritDoc}
     */
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
            if (link.charAt(0) == '/') {
                uri = this.home.resolve(link);
            } else {
                uri = URI.create(link);
            }
            if (!NoBrokenLinks.isValid(uri)) {
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

    /**
     * {@inheritDoc}
     */
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
        boolean valid;
        try {
            RestTester.start(uri)
                .get("checking URI existence")
                .assertStatus(
                    Matchers.lessThan(HttpURLConnection.HTTP_BAD_REQUEST)
                );
            valid = true;
        } catch (AssertionError ex) {
            valid = false;
            Logger.warn(
                NoBrokenLinks.class,
                "#isValid('%s'): not valid: %s",
                uri,
                ex.getMessage()
            );
        }
        return valid;
    }

}
