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
package com.rexsl.test;

import com.rexsl.test.assertions.BodyMatcher;
import com.rexsl.test.assertions.Failure;
import com.rexsl.test.assertions.HeaderMatcher;
import com.rexsl.test.assertions.StatusMatcher;
import com.rexsl.test.assertions.XpathMatcher;
import com.sun.jersey.api.client.ClientResponse;
import com.ymock.util.Logger;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

/**
 * Implementation of {@link TestResponse}.
 *
 * <p>Objects of this class are thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
final class JerseyTestResponse implements TestResponse {

    /**
     * How many attempts to make when {@link #assertThat(AssertionPolicy)}
     * reports problem.
     */
    private static final int MAX_ATTEMPTS = 8;

    /**
     * Fetcher of response.
     */
    private final transient JerseyFetcher fetcher;

    /**
     * The response, should be initialized on demand in {@link #response()}.
     * @see #response()
     */
    private transient ClientResponse iresponse;

    /**
     * The body of response, should be loaded on demand in {@link #body()}.
     * @see #body()
     */
    private transient String body;

    /**
     * Namespace context to use.
     */
    private final transient SimpleNamespaceContext context;

    /**
     * Cached document, in the body.
     * @see #element()
     */
    private transient Element elm;

    /**
     * Public ctor.
     * @param ftch Response fetcher
     */
    public JerseyTestResponse(final JerseyFetcher ftch) {
        this.fetcher = ftch;
        this.context = XhtmlMatchers.context();
    }

    /**
     * Private ctor, for cloning.
     * @param resp The response
     * @param text Body
     * @param element DOM element
     * @param ctx Namespace context
     * @checkstyle ParameterNumber (4 lines)
     */
    @SuppressWarnings("PMD.NullAssignment")
    private JerseyTestResponse(final ClientResponse resp, final String text,
        final Element element, final SimpleNamespaceContext ctx) {
        this.fetcher = null;
        this.iresponse = resp;
        this.body = text;
        this.elm = element;
        this.context = ctx;
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
                new ClientResponseDecor(this.response(), this.getBody())
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
        return RestTester.start(this.response().getLocation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBody() {
        synchronized (this) {
            if (this.body == null) {
                if (this.response().hasEntity()) {
                    this.body = this.response().getEntity(String.class);
                } else {
                    this.body = "";
                }
            }
            return this.body;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getStatus() {
        return this.response().getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> xpath(final String query) {
        final NodeList nodes = this.nodelist(query);
        final List<String> items = new ArrayList<String>();
        for (int idx = 0; idx < nodes.getLength(); idx += 1) {
            MatcherAssert.assertThat(
                "Only /text() nodes or attributes are retrievable with xpath()",
                nodes.item(idx).getNodeType(),
                Matchers.<Short>either(Matchers.equalTo(Node.TEXT_NODE))
                    .or(Matchers.equalTo(Node.ATTRIBUTE_NODE))
                    .or(Matchers.equalTo(Node.CDATA_SECTION_NODE))
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
            this.response().getStatus(),
            this.response().getClientResponseStatus()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return this.response().getHeaders();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Cookie cookie(final String name) {
        final MultivaluedMap<String, String> headers = this.getHeaders();
        MatcherAssert.assertThat(
            "cookies should be set in HTTP header",
            headers.containsKey(HttpHeaders.SET_COOKIE)
        );
        final String header = StringUtils.join(
            headers.get(HttpHeaders.SET_COOKIE), ", "
        );
        Cookie cookie = null;
        for (HttpCookie candidate : HttpCookie.parse(header)) {
            if (candidate.getName().equals(name)) {
                cookie = new Cookie(
                    candidate.getName(),
                    candidate.getValue(),
                    candidate.getPath(),
                    candidate.getDomain(),
                    candidate.getVersion()
                );
                break;
            }
        }
        MatcherAssert.assertThat(
            String.format(
                "cookie '%s' not found in Set-Cookie header: '%s'",
                name,
                header
            ),
            cookie,
            Matchers.notNullValue()
        );
        return cookie;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse registerNs(final String prefix, final Object uri) {
        this.context.withBinding(prefix, uri.toString());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<TestResponse> nodes(final String query) {
        final NodeList nodes = this.nodelist(query);
        final List<TestResponse> items = new ArrayList<TestResponse>();
        for (int idx = 0; idx < nodes.getLength(); idx += 1) {
            MatcherAssert.assertThat(
                "Only elements are retrievable with nodes()",
                nodes.item(idx).getNodeType(),
                Matchers.equalTo(Node.ELEMENT_NODE)
            );
            items.add(
                new JerseyTestResponse(
                    this.response(),
                    this.body,
                    (Element) nodes.item(idx),
                    this.context
                )
            );
        }
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.NullAssignment")
    public TestResponse assertThat(final AssertionPolicy assertion) {
        synchronized (this) {
            int attempt = 0;
            while (true) {
                try {
                    assertion.assertThat(this);
                    break;
                } catch (AssertionError ex) {
                    attempt += 1;
                    if (!assertion.again(attempt)) {
                        throw ex;
                    }
                    if (attempt >= this.MAX_ATTEMPTS) {
                        this.fail(
                            String.format("failed after %d attempt(s)", attempt)
                        );
                    }
                    Logger.warn(
                        this,
                        "#assertThat(%[type]s): attempt #%d failed, re-trying",
                        assertion,
                        attempt
                    );
                    this.iresponse = null;
                    this.body = null;
                    this.elm = null;
                }
            }
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fail(final String reason) {
        this.assertThat(
            new Failure(
                Logger.format(
                    "%s:\n%s",
                    reason,
                    new ClientResponseDecor(this.response(), this.getBody())
                )
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertStatus(final int status) {
        this.assertThat(
            new StatusMatcher(
                Logger.format(
                    "HTTP status code has to be equal to %d in:\n%s",
                    status,
                    new ClientResponseDecor(this.response(), this.getBody())
                ),
                Matchers.equalTo(status)
            )
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertStatus(final Matcher<Integer> matcher) {
        this.assertThat(
            new StatusMatcher(
                Logger.format(
                    "HTTP status code has to match:\n%s",
                    new ClientResponseDecor(this.response(), this.getBody())
                ),
                matcher
            )
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertHeader(final String name, final Matcher matcher) {
        this.assertThat(
            new HeaderMatcher(
                Logger.format(
                    "HTTP header '%s' has to match:\n%s",
                    name,
                    new ClientResponseDecor(this.response(), this.getBody())
                ),
                name,
                matcher
            )
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertBody(final Matcher<String> matcher) {
        this.assertThat(
            new BodyMatcher(
                Logger.format(
                    "HTTP response content has to match:\n%s",
                    new ClientResponseDecor(this.response(), this.getBody())
                ),
                matcher
            )
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertXPath(final String xpath) {
        this.assertThat(
            new XpathMatcher(
                Logger.format(
                    "XPath '%s' has to exist in:\n%s",
                    StringEscapeUtils.escapeJava(xpath),
                    new ClientResponseDecor(this.response(), this.getBody())
                ),
                XhtmlConverter.the(this.element()),
                XhtmlMatchers.hasXPath(xpath, this.context)
            )
        );
        return this;
    }

    /**
     * Fetch and return response.
     * @return The response
     */
    public ClientResponse response() {
        synchronized (this) {
            if (this.iresponse == null) {
                this.iresponse = this.fetcher.fetch();
            }
            return this.iresponse;
        }
    }

    /**
     * Get document of body.
     * @return The document
     */
    private Element element() {
        synchronized (this) {
            if (this.elm == null) {
                this.elm = new DomParser(this.getBody())
                    .document()
                    .getDocumentElement();
            }
            return this.elm;
        }
    }

    /**
     * Retrieve and return a nodelist for XPath query.
     * @param query XPath query
     * @return List of DOM nodes
     */
    private NodeList nodelist(final String query) {
        NodeList nodes;
        try {
            final XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(this.context);
            nodes = (NodeList) xpath.evaluate(
                query,
                this.element(),
                XPathConstants.NODESET
            );
        } catch (javax.xml.xpath.XPathExpressionException ex) {
            throw new IllegalArgumentException(ex);
        }
        return nodes;
    }

}
