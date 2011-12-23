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
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import java.io.ByteArrayInputStream;
import java.net.URI;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

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
    public TestClient rel(final String xpath) {
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(this.getBody().getBytes()));
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalArgumentException(ex);
        } catch (org.xml.sax.SAXException ex) {
            throw new IllegalArgumentException(ex);
        }
        NodeList nodes;
        try {
            nodes = (NodeList) XPathFactory.newInstance()
                .newXPath()
                .evaluate(xpath, document, XPathConstants.NODESET);
        } catch (javax.xml.xpath.XPathExpressionException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (nodes.getLength() != 1) {
            throw new AssertionError(
                Logger.format(
                    "XPath '%s' not found in:\n%s",
                    xpath,
                    new ClientResponseDecor(this.response)
                )
            );
        }
        final URI uri = UriBuilder
            .fromUri(nodes.item(0).getNodeValue())
            .build();
        return RestTester.start(uri);
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
    public GPathResult getGpath() {
        try {
            return new XmlSlurper().parseText(this.getBody());
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalArgumentException(ex);
        } catch (org.xml.sax.SAXException ex) {
            throw new IllegalArgumentException(ex);
        }
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
                new ClientResponseDecor(this.response)
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
                new ClientResponseDecor(this.response)
            ),
            status,
            Matchers.equalTo(this.getStatus())
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
                new ClientResponseDecor(this.response)
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
                new ClientResponseDecor(this.response)
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
                new ClientResponseDecor(this.response)
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
        final SimpleNamespaceContext context = new SimpleNamespaceContext()
            .withBinding("xhtml", "http://www.w3.org/1999/xhtml")
            .withBinding("xs", "http://www.w3.org/2001/XMLSchema")
            .withBinding("xsl", "http://www.w3.org/1999/XSL/Transform");
        MatcherAssert.assertThat(
            Logger.format(
                "XPath '%s' has to exist in:\n%s",
                xpath,
                new ClientResponseDecor(this.response)
            ),
            XhtmlConverter.the(this.getBody()),
            XmlMatchers.hasXPath(xpath, context)
        );
        return this;
    }

}
