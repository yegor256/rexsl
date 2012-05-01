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
package com.rexsl.maven.checks;

import com.jcabi.log.Logger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * Resolver of resources.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
class RuntimeResolver implements URIResolver {

    /**
     * Home page of the site.
     */
    private final transient URI home;

    /**
     * Public ctor.
     * @param uri The home page of the site
     */
    public RuntimeResolver(final URI uri) {
        this.home = uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source resolve(final String href, final String base)
        throws TransformerException {
        URL url;
        try {
            url = new URL(this.home.toString() + href);
        } catch (java.net.MalformedURLException ex) {
            throw new TransformerException(ex);
        }
        HttpURLConnection conn;
        int code;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            code = conn.getResponseCode();
        } catch (java.io.IOException ex) {
            throw new TransformerException(ex);
        }
        if (code != HttpURLConnection.HTTP_OK) {
            throw new TransformerException(
                Logger.format(
                    "URL %s returned %d code (instead of %d)",
                    url,
                    code,
                    HttpURLConnection.HTTP_OK
                )
            );
        }
        Source src;
        try {
            src = new StreamSource(conn.getInputStream());
        } catch (java.io.IOException ex) {
            throw new TransformerException(ex);
        }
        src.setSystemId(href);
        Logger.debug(
            this,
            "#resolve(%s, %s): resolved from %s",
            href,
            base,
            url
        );
        return src;
    }

}
