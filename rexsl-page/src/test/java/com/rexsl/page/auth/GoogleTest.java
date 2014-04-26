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

import com.rexsl.page.Resource;
import com.rexsl.page.ResourceMocker;
import com.rexsl.page.UriInfoMocker;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Google}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class GoogleTest {

    /**
     * Google can be quiet when cookie is absent.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doestRenderWhenCookieIsAbsent() throws Exception {
        final Resource resource = new ResourceMocker().mock();
        final Provider provider = new Google(resource, "", "");
        MatcherAssert.assertThat(
            provider.identity(),
            Matchers.equalTo(Identity.ANONYMOUS)
        );
    }

    /**
     * Google can generate a HATEOAS link.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void generatesLink() throws Exception {
        final Resource resource = new ResourceMocker()
            .withUriInfo(new UriInfoMocker().withBaseUri(new URI("/A")).mock())
            .mock();
        final Provider.Visible provider = new Google(resource, "KEY", "SECRET");
        MatcherAssert.assertThat(
            provider.link().getHref().toString(),
            Matchers.allOf(
                Matchers.containsString("client_id=KEY"),
                Matchers.containsString("state=rexsl-google"),
                Matchers.containsString("redirect_uri=/A")
            )
        );
    }

}
