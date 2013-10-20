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
package com.rexsl.test;

import com.jcabi.xml.XML;
import javax.json.JsonReader;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import org.hamcrest.Matcher;

/**
 * Response returned by {@link TestClient}.
 *
 * <p>It is used as an output of {@link TestClient}, which is an output of
 * {@link RestTester}, for example:
 *
 * <pre> TestResponse resp = RestTester.start(new URI("http://www.google.com"))
 *   .get("load front page of Google");
 * if (resp.getStatus() == 200) {
 *   // everything is fine
 * } else if (resp.getStatus() == 404) {
 *   // google.com not found, hm...
 * }</pre>
 *
 * <p>{@link TestResponse} extends {@link XmlDocument}, which is a abstract
 * of an XML document, which can be retrieved from itself. For example:
 *
 * <pre> TestResponse resp = RestTester.start(new URI("http://example.com"))
 *   .get("load XML document");
 * Collection&lt;XmlDocument&gt; employees = resp.nodes("/Staff/Employee");
 * for (XmlDocument employee : employees) {
 *   String name = employee.xpath("name/text()").get(0);
 *   // ...
 * }</pre>
 *
 * <p>{@link TestResponse} also extends {@link JsonDocument}, which is an
 * abstract of a Json document, which can be retrieved from itself.
 *
 * <p>In case of any problems inside (connection, time-outs, assertions, etc.)
 * implementation has to throw {@link AssertionError}.
 *
 * <p>Implementation of this interface shall be mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface TestResponse extends XML {

    /**
     * How many attempts to make when {@link #assertThat(AssertionPolicy)}
     * reports a problem.
     */
    int MAX_ATTEMPTS = 4;

    /**
     * Follow the LOCATION header.
     * @return New client
     */
    TestClient follow();

    /**
     * Find link in XML and return new client with this link as URI.
     * @param query The path of the link, as XPath query
     * @return New client ready to fetch content from this new page
     */
    TestClient rel(@NotNull String query);

    /**
     * Get status of the response as a positive integer number.
     * @return The status code
     */
    int getStatus();

    /**
     * Get status line of the response.
     * @return The status line
     */
    String getStatusLine();

    /**
     * Get a collection of all headers.
     * @return The headers
     */
    MultivaluedMap<String, String> getHeaders();

    /**
     * Get cookie.
     * @param name Name of the cookie to get
     * @return The cookie
     */
    Cookie cookie(@NotNull String name);

    /**
     * Get body as a string, assuming it's {@code UTF-8} (if there is something
     * else that can't be translated into a UTF-8 string a runtime exception
     * will be thrown).
     *
     * <p><strong>DISCLAIMER</strong>:
     * The only encoding supported here is UTF-8. If the body of response
     * contains any chars that can't be used and should be replaced with
     * a "replacement character", a {@link AssertionError} will be thrown. If
     * you need to use some other encodings, use
     * {@link java.net.HttpURLConnection} directly instead of ReXSL.
     *
     * @return The body, as a UTF-8 string
     */
    String getBody();

    /**
     * Get JSON.
     * @return The body as a JSON document
     */
    JsonReader getJson();

    /**
     * {@inheritDoc}
     */
    @Override
    TestResponse registerNs(@NotNull String prefix, @NotNull Object uri);

    /**
     * Fail and report a problem (throws {@link AssertionError} with the
     * message provided).
     * @param reason Reason of failure
     */
    void fail(@NotNull String reason);

    /**
     * Assert something ({@link AssertionError} will be thrown if assertion
     * fails).
     * @param assertion The assertion to use
     * @return This object
     * @since 0.3.4
     */
    TestResponse assertThat(@NotNull AssertionPolicy assertion);

    /**
     * Verifies HTTP response status code against the provided absolute value,
     * and throws {@link AssertionError} in case of mismatch.
     * @param status Expected status code
     * @return This object
     */
    TestResponse assertStatus(int status);

    /**
     * Verifies HTTP response status code against the provided matcher,
     * and throws {@link AssertionError} in case of mismatch.
     * @param matcher Matcher to validate status code
     * @return This object
     */
    TestResponse assertStatus(@NotNull Matcher<Integer> matcher);

    /**
     * Verifies HTTP response body content against provided matcher,
     * and throws {@link AssertionError} in case of mismatch.
     * @param matcher The matcher to use
     * @return This object
     */
    TestResponse assertBody(@NotNull Matcher<String> matcher);

    /**
     * Verifies HTTP header against provided matcher, and throws
     * {@link AssertionError} in case of mismatch.
     *
     * <p>The iterator for the matcher will always be a real object an never
     * {@code NULL}, even if such a header is absent in the response. If the
     * header is absent the iterable will be empty.
     *
     * @param name Name of the header to match
     * @param matcher The matcher to use
     * @return This object
     */
    TestResponse assertHeader(@NotNull String name,
        @NotNull Matcher<Iterable<String>> matcher);

    /**
     * Verifies HTTP response body XHTML/XML content against XPath query,
     * and throws {@link AssertionError} in case of mismatch.
     * @param xpath Query to use
     * @return This object
     */
    TestResponse assertXPath(@NotNull String xpath);

    /**
     * Verifies the JSON data against the element identifier argument,
     * and throws {@link AssertionError} in case of mismatch.
     * @param element Element in the JSON data of this object
     * @return This object
     */
    TestResponse assertJson(@NotNull String element);

}
