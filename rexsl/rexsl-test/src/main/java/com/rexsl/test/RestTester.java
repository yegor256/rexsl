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
package com.rexsl.test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

/**
 * A static entry point for a test client.
 *
 * <p>For example (in your Groovy script):
 *
 * <pre>
 * import java.net.HttpURLConnection
 * import javax.ws.rs.core.HttpHeaders
 * import javax.ws.rs.core.MediaType
 * import org.xmlmatchers.XmlMatchers
 * import org.hamcrest.Matchers
 * new RestTester.start(rexsl.home)
 *   .header(HttpHeaders.USER_AGENT, 'Safari 4')
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
 *   .body('name=John Doe')
 *   .post(UriBuilder.fromUri('/{id}').build(number))
 *   .assertStatus(HttpURLConnection.HTTP_OK)
 *   .assertBody(XmlMatchers.hasXPath('/data/user[.="John Doe"]'))
 *   .assertBody(Matchers.containsString('xml'))
 * </pre>
 *
 * <p>This example will make a <tt>POST</tt> request to the URI pre-built
 * by <tt>UriBuilder</tt>, providing headers and request body. Response will
 * be validated with matchers.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class RestTester {

    /**
     * It's a utility class.
     */
    private RestTester() {
        // empty
    }

    /**
     * Start new session.
     * @param uri Home URI
     */
    public static TestClient start(final URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException(
                String.format(
                    "URI '%s' has to be absolute",
                    uri
                )
            );
        }
        final ClientConfig config = new DefaultClientConfig();
        config.getProperties()
            .put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);
        URI dest = uri;
        if ("".equals(dest.getPath())) {
            dest = UriBuilder.fromUri(uri).path("/").build();
        }
        return new JerseyTestClient(Client.create(config).resource(dest));
    }

}
