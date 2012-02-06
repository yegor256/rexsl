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

import com.sun.jersey.api.client.ClientResponse;
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link JerseyTestResponse}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class JerseyTestResponseTest {

    /**
     * TestResponse can find nodes with XPath.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void findsDocumentNodesWithXpath() throws Exception {
        final ClientResponse resp = new ClientResponseMocker()
            .withEntity("<r><a>\u0443\u0440\u0430!</a><a>B</a></r>")
            .mock();
        final TestResponse response =
            new JerseyTestResponse(this.fetcher(resp));
        MatcherAssert.assertThat(
            response.xpath("//a/text()"),
            Matchers.hasSize(2)
        );
        MatcherAssert.assertThat(
            response.xpath("/r/a/text()"),
            Matchers.hasItem("\u0443\u0440\u0430!")
        );
    }

    /**
     * TestResponse can assert with XPath.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void assertsWithXpath() throws Exception {
        final ClientResponse resp = new ClientResponseMocker()
            .withEntity("<x><y>\u0443\u0440\u0430!</y></x>")
            .mock();
        new JerseyTestResponse(this.fetcher(resp))
            .assertXPath("//y[.='\u0443\u0440\u0430!']")
            .assertXPath("/x/y[contains(.,'\u0430')]");
    }

    /**
     * TestResponse can assert with XPath and namespaces.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void assertsWithXpathAndNamespaces() throws Exception {
        final ClientResponse resp = new ClientResponseMocker()
            // @checkstyle LineLength (1 line)
            .withEntity("<html xmlns='http://www.w3.org/1999/xhtml'><div>\u0443\u0440\u0430!</div></html>")
            .mock();
        new JerseyTestResponse(this.fetcher(resp))
            .assertXPath("/xhtml:html/xhtml:div")
            .assertXPath("//xhtml:div[.='\u0443\u0440\u0430!']");
    }

    /**
     * TestResponse can assert with XPath with custom namespaces.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void assertsWithXpathWithCustomNamespace() throws Exception {
        final ClientResponse resp = new ClientResponseMocker()
            .withEntity("<a xmlns='urn:foo'><b>yes!</b></a>")
            .mock();
        final TestResponse response = new JerseyTestResponse(this.fetcher(resp))
            .registerNs("foo", "urn:foo")
            .assertXPath("/foo:a/foo:b[.='yes!']");
        MatcherAssert.assertThat(
            response.xpath("//foo:b/text()").get(0),
            Matchers.equalTo("yes!")
        );
    }

    /**
     * TestResponse can find and return nodes with XPath.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void findsDocumentNodesWithXpathAndReturnsThem() throws Exception {
        final ClientResponse resp = new ClientResponseMocker()
            .withEntity("<root><a><x>1</x></a><a><x>2</x></a></root>")
            .mock();
        final TestResponse response =
            new JerseyTestResponse(this.fetcher(resp));
        MatcherAssert.assertThat(
            response.nodes("//a"),
            Matchers.hasSize(2)
        );
        MatcherAssert.assertThat(
            response.nodes("/root/a").get(0).xpath("x/text()").get(0),
            Matchers.equalTo("1")
        );
    }

    /**
     * TestResponse can fail on demand.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = AssertionError.class)
    public void failsOnDemand() throws Exception {
        new JerseyTestResponse(this.fetcher(new ClientResponseMocker().mock()))
            .fail("some reason");
    }

    /**
     * TestResponse can assert HTTP status.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = AssertionError.class)
    public void assertsHttpStatusCode() throws Exception {
        new JerseyTestResponse(this.fetcher(new ClientResponseMocker().mock()))
            .assertStatus(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * TestResponse can retry a few times.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void retriesWhenRequiredByCustomAssertion() throws Exception {
        final JerseyFetcher fetcher = Mockito.mock(JerseyFetcher.class);
        Mockito.doReturn(new ClientResponseMocker().mock())
            .when(fetcher).fetch();
        new JerseyTestResponse(fetcher).assertThat(
            new AssertionPolicy() {
                @Override
                public void assertThat(final TestResponse resp) {
                    throw new AssertionError();
                }
                @Override
                public boolean again(final int attempt) {
                    return attempt < 2;
                }
            }
        );
        Mockito.verify(fetcher, Mockito.times(2)).fetch();
    }

    /**
     * Create fetcher with response on board.
     * @param resp The response to return
     * @return The fetcher
     */
    private JerseyFetcher fetcher(final ClientResponse resp) {
        return new JerseyFetcher() {
            @Override
            public ClientResponse fetch() {
                return resp;
            }
        };
    }

}
