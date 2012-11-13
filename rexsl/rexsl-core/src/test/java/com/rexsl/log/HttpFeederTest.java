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

import com.rexsl.test.ContainerMocker;
import com.rexsl.test.RestTester;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link HttpFeeder}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class HttpFeederTest {

    /**
     * HttpFeeder can send messages to HTTP via POST.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void sendsMessagesToCloud() throws Exception {
        final String message = "hi there!";
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(RestTester.POST))
            .expectHeader(
                HttpHeaders.CONTENT_TYPE,
                Matchers.equalTo(MediaType.TEXT_PLAIN)
            )
            .returnBody("posted")
            .mock();
        final HttpFeeder feeder = new HttpFeeder();
        feeder.setUrl(container.home().toString());
        feeder.activateOptions();
        feeder.feed(message);
    }

    /**
     * HttpFeeder can handle Basic HTTP Authentication.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void basicHttpAuthTest() throws Exception {
        final String message = "hello!";
        final String auth = "joe:secret";
        final StringBuilder authval = new StringBuilder();
        final String encauth = Base64.encodeBase64String(auth.getBytes());
        authval.append("Basic ").append(encauth);
        final URI home = new ContainerMocker()
            .expectMethod(Matchers.equalTo(RestTester.POST))
            .expectHeader(
                HttpHeaders.AUTHORIZATION,
                Matchers.equalTo(authval.toString())
            )
            .returnBody("success")
            .mock()
            .home();
        final URI uri = UriBuilder.fromUri(home)
            .userInfo(auth)
            .build();
        final HttpFeeder feeder = new HttpFeeder();
        feeder.setUrl(uri.toString());
        feeder.activateOptions();
        feeder.feed(message);
    }

    /**
     * HttpFeeder excessive test.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void feedExcessiveTest() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(RestTester.POST))
            .expectHeader(
                HttpHeaders.CONTENT_TYPE,
                Matchers.equalTo(MediaType.TEXT_PLAIN)
            )
            .returnBody("done")
            .mock();
        final HttpFeeder feeder = new HttpFeeder();
        feeder.setUrl(container.home().toString());
        feeder.activateOptions();
        try {
            // @checkstyle MagicNumber (1 line)
            for (int count = 0; count < 1500; ++count) {
                feeder.feed("some text\nmultiline");
            }
        } catch (IOException ex) {
            throw new Exception(ex);
        }
    }
}
