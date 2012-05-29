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
package com.rexsl.page;

import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Link}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class LinkTest {

    /**
     * Link can be converted to XML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsToXml() throws Exception {
        final Link link = new Link(
            "foo",
            UriBuilder.fromUri("http://bar").build(),
            MediaType.TEXT_XML
        );
        link.with(new JaxbBundle("label", "some label"));
        MatcherAssert.assertThat(
            JaxbConverter.the(link),
            XhtmlMatchers.hasXPaths(
                "/link[@rel='foo']",
                "/link[@href='http://bar']",
                "/link[@type='text/xml']",
                "/link[label='some label']"
            )
        );
    }

    /**
     * Link can be attached to resource.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void attachesToResource() throws Exception {
        final Link link = new Link("bar", "/boom/test?a=x");
        link.attachTo(new ResourceMocker().mock());
        MatcherAssert.assertThat(
            link.getHref(),
            Matchers.allOf(
                Matchers.endsWith("/test?a=x"),
                Matchers.not(Matchers.containsString("//tes"))
            )
        );
        final Link second = new Link("bar2", "./boom/test?a=y");
        second.attachTo(new ResourceMocker().mock());
        MatcherAssert.assertThat(
            second.getHref(),
            Matchers.allOf(
                Matchers.endsWith("/test?a=y"),
                Matchers.not(Matchers.containsString("//test"))
            )
        );
    }

}
