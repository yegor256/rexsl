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
package com.rexsl.maven.checks;

import com.rexsl.test.Request;
import com.rexsl.test.XhtmlMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RuntimeResolver}.
 * @author Yegor Bugayenko (yegor@qulice.com)
 * @version $Id$
 */
public final class RuntimeResolverTest {

    /**
     * RuntimeResolver can resolve source by URL.
     * @throws Exception If something goes wrong
     */
    @Test
    public void resolvesSourceByUrl() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .returnBody("<doc><test/></doc>")
            .mock();
        final RuntimeResolver resolver = new RuntimeResolver(container.home());
        MatcherAssert.assertThat(
            resolver.resolve("/", ""),
            XhtmlMatchers.hasXPath("/doc/test")
        );
        container.stop();
    }

    /**
     * RuntimeResolver can resolve source by URL with base.
     * @throws Exception If something goes wrong
     */
    @Test
    public void resolvesSourceByUrlWithBase() throws Exception {
        final String css = "/css/test.css";
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(Request.GET))
            .expectRequestUri(Matchers.equalTo(css))
            .returnBody("<test/>")
            .mock();
        final ContainerMocker reserve = new ContainerMocker()
            .expectMethod(Matchers.equalTo(Request.GET))
            .expectRequestUri(Matchers.equalTo(css))
            .returnBody("<doc/>")
            .mock();
        final RuntimeResolver resolver = new RuntimeResolver(container.home());
        MatcherAssert.assertThat(
            resolver.resolve(css, reserve.home().toString()),
            XhtmlMatchers.hasXPath("/doc")
        );
        container.stop();
    }

}
