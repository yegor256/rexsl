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
package com.rexsl.page.inset;

import com.rexsl.page.BasePage;
import com.rexsl.page.BasePageMocker;
import com.rexsl.page.HttpHeadersMocker;
import com.rexsl.page.Inset;
import com.rexsl.page.Resource;
import com.rexsl.page.ResourceMocker;
import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import java.net.URI;
import java.util.logging.Level;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link FlashInset}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class FlashInsetTest {

    /**
     * FlashInset can be quiet when cookie is absent.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doestRenderWhenCookieIsAbsent() throws Exception {
        final Resource resource = new ResourceMocker().mock();
        final Inset inset = new FlashInset(resource);
        final BasePage<?, ?> page = new BasePageMocker().init(resource);
        inset.render(page, Response.ok());
        MatcherAssert.assertThat(
            JaxbConverter.the(page),
            Matchers.not(XhtmlMatchers.hasXPath("/*/flash"))
        );
    }

    /**
     * FlashInset can render cookie and add a cleaning header.
     * @throws Exception If there is some problem inside
     * @todo #628 Doesn't work because HttpHeadersMocker doesn't implement
     *  method getCookies() correctly
     */
    @Test
    @org.junit.Ignore
    public void rendersCookieAndAddsHttpHeader() throws Exception {
        final Resource resource = new ResourceMocker().withHttpHeaders(
            new HttpHeadersMocker().withHeader(
                HttpHeaders.COOKIE,
                "Rexsl-Flash=JFHEMTZ2NBSWY3DPFQQHO33SNRSCC===;path=/"
            ).mock()
        ).mock();
        final Inset inset = new FlashInset(resource);
        final BasePage<?, ?> page = new BasePageMocker().init(resource);
        inset.render(page, Response.ok());
        MatcherAssert.assertThat(
            JaxbConverter.the(page),
            XhtmlMatchers.hasXPath(
                "/*/flash[message = 'hello, world!' and level = 'INFO']"
            )
        );
    }

    /**
     * FlashInset can build a web application exception.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsForwardingException() throws Exception {
        MatcherAssert.assertThat(
            FlashInset.forward(
                URI.create("#link"),
                "hello",
                Level.INFO
            ).getResponse().getMetadata(),
            Matchers.allOf(
                Matchers.hasEntry(
                    Matchers.equalTo(HttpHeaders.LOCATION),
                    Matchers.hasItem(
                        Matchers.hasToString(Matchers.startsWith("#li"))
                    )
                ),
                Matchers.hasEntry(
                    Matchers.equalTo(HttpHeaders.SET_COOKIE),
                    Matchers.hasItem(
                        Matchers.hasToString(
                            Matchers.containsString(
                                "Rexsl-Flash=JFHEMTZ2NBSWY3DP"
                            )
                        )
                    )
                )
            )
        );
    }

    /**
     * FlashInset can build a severe web application exception.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsSevereForwardingException() throws Exception {
        MatcherAssert.assertThat(
            FlashInset.forward(
                URI.create("#some-link"),
                new IllegalArgumentException("problem text")
            ).getResponse().getMetadata(),
            Matchers.allOf(
                Matchers.hasEntry(
                    Matchers.equalTo(HttpHeaders.LOCATION),
                    Matchers.hasItem(
                        Matchers.hasToString(Matchers.startsWith("#some-li"))
                    )
                ),
                Matchers.hasEntry(
                    Matchers.equalTo(HttpHeaders.SET_COOKIE),
                    Matchers.hasItem(
                        Matchers.hasToString(
                            Matchers.containsString(
                                "Rexsl-Flash=KNCVMRKSIU5HA4TPMJWGK3JAORSXQ5A="
                            )
                        )
                    )
                )
            )
        );
    }

}
