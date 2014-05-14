/**
 * Copyright (c) 2011-2014, ReXSL.com
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
package com.rexsl.page.auth;

import com.jcabi.urn.URN;
import com.rexsl.mock.HttpHeadersMocker;
import com.rexsl.page.Resource;
import com.rexsl.page.mock.ResourceMocker;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link HttpBasic}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class HttpBasicTest {

    /**
     * HttpBasic can be quiet when header is absent.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doestRenderWhenHeaderIsAbsent() throws Exception {
        final Resource resource = new ResourceMocker().mock();
        final Provider provider = new HttpBasic(resource, HttpBasic.NEVER);
        MatcherAssert.assertThat(
            provider.identity(),
            Matchers.equalTo(Identity.ANONYMOUS)
        );
    }

    /**
     * HttpBasic can authenticate.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void authenticatesWhenHeaderIsPresent() throws Exception {
        final Resource resource = new ResourceMocker().withHttpHeaders(
            new HttpHeadersMocker().withHeader(
                HttpHeaders.AUTHORIZATION,
                "Basic dXJuJTNBdGVzdCUzQTU1NTpzZWNyZXQ="
            ).mock()
        ).mock();
        final Provider provider = new HttpBasic(
            resource,
            new HttpBasic.Vault() {
                @Override
                public Identity authenticate(final String user,
                    final String password) {
                    return new Identity.Simple(
                        URN.create(user), password, URI.create("#")
                    );
                }
            }
        );
        MatcherAssert.assertThat(
            provider.identity().name(),
            Matchers.equalTo("secret")
        );
        MatcherAssert.assertThat(
            provider.identity().urn(),
            Matchers.equalTo(new URN("urn:test:555"))
        );
    }

    /**
     * HttpBasic can handle empty parts gracefully.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void gracefullyHandlesEmptyParts() throws Exception {
        final Resource resource = new ResourceMocker().withHttpHeaders(
            new HttpHeadersMocker().withHeader(
                HttpHeaders.AUTHORIZATION, "Basic Og=="
            ).mock()
        ).mock();
        new HttpBasic(
            resource,
            new HttpBasic.Vault() {
                @Override
                public Identity authenticate(final String user,
                    final String password) {
                    MatcherAssert.assertThat(user, Matchers.equalTo(""));
                    MatcherAssert.assertThat(password, Matchers.equalTo(""));
                    return Identity.ANONYMOUS;
                }
            }
        ).identity();
    }

}
