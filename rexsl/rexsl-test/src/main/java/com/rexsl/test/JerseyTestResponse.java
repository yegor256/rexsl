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
import java.util.List;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.xml.namespace.NamespaceContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * Implementation of {@link TestResponse}.
 *
 * <p>Objects of this class are mutable and thread-safe.
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
     * Decorated request.
     */
    private final transient RequestDecor request;

    /**
     * The response, should be initialized on demand in {@link #response()}.
     * @see #response()
     */
    private transient ClientResponse iresponse;

    /**
     * The body of HTTP response, should be loaded on demand in
     * {@link #getBody()}.
     * @see #getBody()
     */
    private transient String body;

    /**
     * The XML document in the body, should be loaded on demand in
     * {@link #getXml()}.
     * @see #getXml()
     */
    private transient XmlDocument xml;

    /**
     * The Json document in the body, should be loaded on demand in
     * {@link #getJson()}.
     * @see #getJson()
     */
    private transient JsonDocument jsonDocument;

    /**
     * Public ctor.
     * @param ftch Response fetcher
     * @param rqst Decorated request, for logging purposes
     */
    public JerseyTestResponse(final JerseyFetcher ftch,
        final RequestDecor rqst) {
        this.fetcher = ftch;
        this.request = rqst;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestClient rel(final String query) {
        final List<String> links = this.xpath(query);
        MatcherAssert.assertThat(
            Logger.format(
                "XPath '%s' not found in:\n%s\nHTTP request:\n%s",
                StringEscapeUtils.escapeJava(query),
                new ClientResponseDecor(this.response(), this.getBody()),
                this.request
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
        synchronized (this.fetcher) {
            if (this.body == null) {
                if (this.response().hasEntity()) {
                    this.body = this.response().getEntity(String.class);
                } else {
                    this.body = "";
                }
                Logger.debug(
                    this,
                    "#getBody(): HTTP response body:\n%s",
                    new ClientResponseDecor(this.response(), this.body)
                );
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
        return this.getXml().xpath(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatusLine() {
        return Logger.format(
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
            Logger.format(
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
        synchronized (this.fetcher) {
            this.xml = this.getXml().registerNs(prefix, uri);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse merge(final NamespaceContext ctx) {
        synchronized (this.fetcher) {
            this.xml = this.getXml().merge(ctx);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<XmlDocument> nodes(final String query) {
        return this.getXml().nodes(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.NullAssignment")
    public TestResponse assertThat(final AssertionPolicy assertion) {
        synchronized (this.fetcher) {
            int attempt = 0;
            while (true) {
                try {
                    assertion.assertThat(this);
                    break;
                } catch (AssertionError ex) {
                    ++attempt;
                    if (!assertion.again(attempt)) {
                        throw ex;
                    }
                    if (attempt >= this.MAX_ATTEMPTS) {
                        this.fail(
                            Logger.format("failed after %d attempt(s)", attempt)
                        );
                    }
                    Logger.warn(
                        this,
                        // @checkstyle LineLength (1 line)
                        "#assertThat(%[type]s): attempt #%d failed, re-trying: %[exception]s",
                        assertion,
                        attempt,
                        ex
                    );
                    this.iresponse = null;
                    this.body = null;
                    this.xml = null;
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
                    "HTTP code has to be equal to #%d in:\n%s\nRequest:\n%s",
                    status,
                    new ClientResponseDecor(this.response(), this.getBody()),
                    this.request
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
                    "HTTP status code has to match:\n%s\nRequest:\n%s",
                    new ClientResponseDecor(this.response(), this.getBody()),
                    this.request
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
                    "HTTP header '%s' has to match:\n%s\nRequest:\n%s",
                    name,
                    new ClientResponseDecor(this.response(), this.getBody()),
                    this.request
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
                    "HTTP response content has to match:\n%s\nRequest:\n%s",
                    new ClientResponseDecor(this.response(), this.getBody()),
                    this.request
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
                    "XPath '%s' has to exist in:\n%s\nRequest:\n%s",
                    StringEscapeUtils.escapeJava(xpath),
                    new ClientResponseDecor(this.response(), this.getBody()),
                    this.request
                ),
                this.getXml(),
                xpath
            )
        );
        return this;
    }

    /**
     * Fetch and return {@link ClientResponse}.
     *
     * <p>Throws {@link AssertionError} if anything goes wrong inside. Never
     * throws anything else.
     *
     * @return The response
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private ClientResponse response() {
        synchronized (this.fetcher) {
            if (this.iresponse == null) {
                try {
                    this.iresponse = this.fetcher.fetch();
                // @checkstyle IllegalCatch (1 line)
                } catch (RuntimeException ex) {
                    Logger.warn(this, "Failed to fetch: %[exception]s", ex);
                    throw new AssertionError(ex);
                }
            }
            return this.iresponse;
        }
    }

    /**
     * Get XmlDocument of the body.
     *
     * <p>It uses {@code this.fetcher} as a synchronization lock because at
     * the time of method execution {@code this.xml} may be {@code NULL}.
     *
     * @return The XML document
     */
    public XmlDocument getXml() {
        synchronized (this.fetcher) {
            if (this.xml == null) {
                this.xml = new LazyXml(this, new XPathContext());
            }
            return this.xml;
        }
    }

    /**
     * Get JsonDocument of the body.
     * @return The Json document
     */
    public JsonDocument getJson() {
        synchronized (this.fetcher) {
            if (this.jsonDocument == null) {
                this.jsonDocument = new SimpleJson(this.getBody());
            }
            return this.jsonDocument;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse assertJson(final String element) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> json(final String query) {
        return this.getJson().json(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JsonDocument> nodesJson(final String query) {
        return this.getJson().nodesJson(query);
    }

}
