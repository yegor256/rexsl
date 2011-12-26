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
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlmatchers.transform.XmlConverters;

/**
 * Implementation of {@link TestResponse}.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class JerseyTestResponse implements TestResponse {

    /**
     * The response.
     */
    private final transient ClientResponse response;

    /**
     * The body of response.
     */
    private final transient String body;

    /**
     * Cached document, in the body.
     * @see #document()
     */
    private transient Document doc;

    /**
     * Public ctor.
     * @param resp The response
     */
    public JerseyTestResponse(final ClientResponse resp) {
        this.response = resp;
        if (this.response.hasEntity()) {
            this.body = this.response.getEntity(String.class);
        } else {
            this.body = "";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestClient rel(final String query) {
        final List<String> links = this.xpath(query);
        MatcherAssert.assertThat(
            Logger.format(
                "XPath '%s' not found in:\n%s",
                StringEscapeUtils.escapeJava(query),
                new ClientResponseDecor(this.response, this.getBody())
            ),
            links,
            Matchers.hasSize(1)
        );
        return RestTester.start(UriBuilder.fromUri(links.get(0)).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestClient follow() {
        return RestTester.start(this.response.getLocation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBody() {
        return this.body;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getStatus() {
        return this.response.getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> xpath(final String query) {
        NodeList nodes;
        try {
            nodes = (NodeList) XPathFactory.newInstance()
                .newXPath()
                .evaluate(query, this.document(), XPathConstants.NODESET);
        } catch (javax.xml.xpath.XPathExpressionException ex) {
            throw new IllegalArgumentException(ex);
        }
        final List<String> items = new ArrayList<String>();
        for (int idx = 0; idx < nodes.getLength(); idx += 1) {
            MatcherAssert.assertThat(
                "Only /text() nodes are retrievable with xpath()",
                nodes.item(idx).getNodeType(),
                Matchers.equalTo(org.w3c.dom.Node.TEXT_NODE)
            );
            items.add(nodes.item(idx).getNodeValue());
        }
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatusLine() {
        return String.format(
            "%d %s",
            this.response.getStatus(),
            this.response.getClientResponseStatus()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return this.response.getHeaders();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fail(final String reason) {
        throw new AssertionError(
            Logger.format(
                "%s:\n%s",
                reason,
                new ClientResponseDecor(this.response, this.getBody())
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertStatus(final int status) {
        MatcherAssert.assertThat(
            Logger.format(
                "HTTP status code has to be equal to %d in:\n%s",
                status,
                new ClientResponseDecor(this.response, this.getBody())
            ),
            this.getStatus(),
            Matchers.equalTo(status)
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertStatus(final Matcher<Integer> matcher) {
        MatcherAssert.assertThat(
            Logger.format(
                "HTTP status code has to match in:\n%s",
                new ClientResponseDecor(this.response, this.getBody())
            ),
            this.getStatus(),
            matcher
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertHeader(final String name, final Matcher matcher) {
        MatcherAssert.assertThat(
            Logger.format(
                "HTTP header '%s' has to match in:\n%s",
                name,
                new ClientResponseDecor(this.response, this.getBody())
            ),
            this.response.getHeaders().getFirst((String) name),
            matcher
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertBody(final Matcher<String> matcher) {
        MatcherAssert.assertThat(
            Logger.format(
                "HTTP response content has to match in:\n%s",
                new ClientResponseDecor(this.response, this.getBody())
            ),
            this.getBody(),
            matcher
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertXPath(final String xpath) {
        MatcherAssert.assertThat(
            Logger.format(
                "XPath '%s' has to exist in:\n%s",
                StringEscapeUtils.escapeJava(xpath),
                new ClientResponseDecor(this.response, this.getBody())
            ),
            XmlConverters.the(this.document()),
            XhtmlMatchers.hasXPath(xpath)
        );
        return this;
    }

    /**
     * Get document of body.
     * @return The document
     */
    private Document document() {
        synchronized (this) {
            if (this.doc == null) {
                this.doc = new DomParser(this.getBody()).document();
            }
            return this.doc;
        }
    }

}
