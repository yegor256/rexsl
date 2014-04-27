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
package com.rexsl.page.auth;

import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rexsl.mock.HttpHeadersMocker;
import com.rexsl.page.BasePage;
import com.rexsl.page.Inset;
import com.rexsl.page.Resource;
import com.rexsl.page.auth.mock.IdentityMocker;
import com.rexsl.page.mock.BasePageMocker;
import com.rexsl.page.mock.ResourceMocker;
import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import java.io.IOException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link AuthInset}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class AuthInsetTest {

    /**
     * AuthInset can be quiet when cookie is absent.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doestRenderWhenCookieIsAbsent() throws Exception {
        final Resource resource = new ResourceMocker().mock();
        final Inset inset = new AuthInset(resource, "");
        final BasePage<?, ?> page = new BasePageMocker().init(resource);
        inset.render(page, Response.ok());
        MatcherAssert.assertThat(
            JaxbConverter.the(page),
            Matchers.not(XhtmlMatchers.hasXPath("/*/identity"))
        );
    }

    /**
     * AuthInset can read cookie and add a cleaning header.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void readsCookieAndAddsHttpHeader() throws Exception {
        final String key = "74^54\u20ac";
        final URN urn = new URN("urn:test:7362423");
        final String name = "John \u20ac Smith";
        final String cookie = AuthInset.encrypt(
            new IdentityMocker()
                .withURN(urn)
                .withName(name)
                .mock(),
            key
        );
        final Resource resource = this.resource(cookie);
        final Inset inset = new AuthInset(resource, key);
        final BasePage<?, ?> page = new BasePageMocker().init(resource);
        inset.render(page, Response.ok());
        MatcherAssert.assertThat(
            JaxbConverter.the(page),
            XhtmlMatchers.hasXPaths(
                String.format("/*/identity[name='%s']", name),
                String.format("/*/identity[urn='%s']", urn),
                "/*/identity/token"
            )
        );
    }

    /**
     * AuthInset can authenticate through provider.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = javax.ws.rs.WebApplicationException.class)
    public void authenticatesWithRedirectingProvider() throws Exception {
        final Resource resource = new ResourceMocker().mock();
        final Inset inset = new AuthInset(resource, "")
            .with(new AuthInsetTest.Redirector());
        final BasePage<?, ?> page = new BasePageMocker().init(resource);
        inset.render(page, Response.ok());
    }

    /**
     * AuthInset can encrypt identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void encryptsIdentityToText() throws Exception {
        MatcherAssert.assertThat(
            AuthInset.encrypt(new IdentityMocker().mock(), "").length(),
            Matchers.greaterThan(Tv.FIFTY)
        );
        MatcherAssert.assertThat(
            new AuthInset(new ResourceMocker().mock(), "").encrypt(
                new IdentityMocker().mock()
            ).length(),
            Matchers.greaterThan(Tv.FIFTY)
        );
    }

    /**
     * AuthInset can return a unique identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsUniqueIdentitiesAlways() throws Exception {
        final String key = "74^54\u20ac0*43hsi";
        final String cookie = AuthInset.encrypt(
            new IdentityMocker().mock(), key
        );
        MatcherAssert.assertThat(
            new AuthInset(this.resource(cookie), key).identity(),
            Matchers.allOf(
                Matchers.not(Matchers.equalTo(Identity.ANONYMOUS)),
                Matchers.equalTo(
                    new AuthInset(this.resource(cookie), key).identity()
                )
            )
        );
    }

    /**
     * Make resource with the given auth cookie.
     * @param cookie The cookie
     * @return The resource made
     */
    private Resource resource(final String cookie) {
        return new ResourceMocker().withHttpHeaders(
            new HttpHeadersMocker().withHeader(
                HttpHeaders.COOKIE,
                String.format("Rexsl-Auth=%s;path=/", cookie)
            ).mock()
        ).mock();
    }

    /**
     * Test provider that redirects.
     */
    @Provider.Redirect
    final class Redirector implements Provider {
        @Override
        public Identity identity() throws IOException {
            return new IdentityMocker().mock();
        }
    }

}
