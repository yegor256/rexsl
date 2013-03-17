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
package com.rexsl.page;

import com.rexsl.page.inset.FlashInset;
import com.rexsl.page.inset.LinksInset;
import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.annotation.XmlRootElement;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link BasePage}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class BasePageTest {

    /**
     * BasePage can be converted to XML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsToXml() throws Exception {
        final BasePageTest.ConcreteRs res = new BasePageTest.ConcreteRs();
        res.setUriInfo(new UriInfoMocker().mock());
        res.setHttpHeaders(new HttpHeadersMocker().mock());
        res.setSecurityContext(Mockito.mock(SecurityContext.class));
        final BasePageTest.FooPage page = new BasePageTest.FooPage()
            .init(res)
            .append(new JaxbBundle("title", "hello, world!"))
            .link(new Link("test", "/foo"));
        MatcherAssert.assertThat(
            JaxbConverter.the(page.render().build().getEntity()),
            XhtmlMatchers.hasXPaths(
                "/page/@date",
                "/page/@ip",
                "/page/@ssl",
                "/page/millis",
                "/page/title[. = 'hello, world!']",
                "/page/links/link[@rel = 'home']",
                "/page/links/link[@rel = 'self']",
                "/page/links/link[@rel = 'test']"
            )
        );
    }

    /**
     * Base resource for tests.
     */
    @Inset.Default({ LinksInset.class, FlashInset.class })
    private static class FooBaseRs extends BaseResource {
    }

    /**
     * Concrete resource for tests.
     */
    @Inset.Default({ LinksInset.class, FlashInset.class })
    private static final class ConcreteRs extends BasePageTest.FooBaseRs {
    }

    /**
     * Base page for tests.
     */
    @XmlRootElement(name = "page")
    private static final class FooPage
        extends BasePage<BasePageTest.FooPage, Resource> {
    }

}
