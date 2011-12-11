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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
    public TestClient rel(final String xpath) throws Exception {
        final Document document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(new ByteArrayInputStream(this.getBody().getBytes()));
        final NodeList nodes = (NodeList) XPathFactory.newInstance()
            .newXPath()
            .evaluate(xpath, document, XPathConstants.NODESET);
        if (nodes.getLength() != 1) {
            throw new AssertionError(
                String.format(
                    "XPath '%s' not found in:%n%s",
                    xpath,
                    this.asText()
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
    public String getBody() throws IOException {
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
    public GPathResult getGpath() throws Exception {
        return new XmlSlurper().parseText(this.getBody());
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
    public TestResponse assertStatus(final int status) throws IOException {
        MatcherAssert.assertThat(
            String.format("Invalid HTTP response code in:%n%s", this.asText()),
            status,
            Matchers.equalTo(this.getStatus())
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertStatus(final Matcher<Integer> matcher)
        throws IOException {
        MatcherAssert.assertThat(
            String.format("Invalid HTTP response code in:%n%s", this.asText()),
            this.getStatus(),
            matcher
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertBody(final Matcher<String> matcher)
        throws IOException {
        MatcherAssert.assertThat(
            String.format("Invalid content in:%n%s", this.asText()),
            this.getBody(),
            matcher
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertXPath(final String xpath) throws Exception {
        final SimpleNamespaceContext context = new SimpleNamespaceContext()
            .withBinding("xhtml", "http://www.w3.org/1999/xhtml")
            .withBinding("xs", "http://www.w3.org/2001/XMLSchema")
            .withBinding("xsl", "http://www.w3.org/1999/XSL/Transform");
        MatcherAssert.assertThat(
            String.format("XPath can't be evaluated in:%n%s", this.asText()),
            XhtmlConverter.the(this.getBody()),
            XmlMatchers.hasXPath(xpath, context)
        );
        return this;
    }

    /**
     * Show response as text.
     * @return The text
     * @throws IOException If some problem inside
     */
    private String asText() throws IOException {
        final StringBuilder builder = new StringBuilder();
        builder.append("\t---\n");
        for (MultivaluedMap.Entry<String, List<String>> header
            : this.response.getHeaders().entrySet()) {
            builder.append(
                String.format(
                    "\t%s: %s\n",
                    header.getKey(),
                    StringUtils.join(header.getValue(), ", ")
                )
            );
        }
        builder
            .append("\n\t")
            .append(this.getBody().replace("\n", "\n\t"))
            .append("\n\t---");
        return builder.toString();
    }

}
