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

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import org.hamcrest.Matcher;

/**
 * Response returned by {@link TestClient}.
 *
 * <p>It is used as an output of {@link TestClient}, which is an output of
 * {@link RestTester}, for example:
 *
 * <pre>
 * TestResponse resp = RestTester.start(new URI("http://www.google.com"))
 *   .get("load from page of Google");
 * if (resp.getStatus() == 200) {
 *   // everything is fine
 * } else if (resp.getStatus() == 404) {
 *   // google.com not found, hm...
 * }
 * </pre>
 *
 * <p>{@link TestResponse} extends {@link XmlDocument}, which is a abstract
 * of an XML document, which can be retrieved from itself. For example:
 *
 * <pre>
 * TestResponse resp = RestTester.start(new URI("http://example.com"))
 *   .get("load XML document");
 * Collection&lt;XmlDocument&gt; employees = resp.nodes("/Staff/Employee");
 * for (XmlDocument employee : employees) {
 *   String name = employee.xpath("name/text()").get(0);
 *   // ...
 * }
 * </pre>
 *
 * <p>Implementation of this interface shall be mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public interface TestResponse extends XmlDocument {

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
    TestClient rel(String query);

    /**
     * Get status of the response as a positive integer number.
     * @return The status code
     */
    Integer getStatus();

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
    Cookie cookie(String name);

    /**
     * Get body as a string, assuming it's {@code UTF-8}.
     * @return The body
     */
    String getBody();

    /**
     * Fail and report a problem.
     * @param reason Reason of failure
     */
    void fail(String reason);

    /**
     * Assert something.
     * @param assertion The assertion to use
     * @return This object
     * @since 0.3.4
     */
    TestResponse assertThat(AssertionPolicy assertion);

    /**
     * Verifies HTTP response status code against the provided absolute value,
     * and throws {@link AssertionError} in case of mismatch.
     * @param status Expected status code
     * @return This object
     */
    TestResponse assertStatus(int status);

    /**
     * Verifies HTTP response status code against the provided matcher.
     * @param matcher Matcher to validate status code
     * @return This object
     */
    TestResponse assertStatus(Matcher<Integer> matcher);

    /**
     * Verifies HTTP response body content against provided matcher.
     * @param matcher The matcher to use
     * @return This object
     */
    TestResponse assertBody(Matcher<String> matcher);

    /**
     * Verifies HTTP header against provided matcher.
     * @param name Name of the header to match
     * @param matcher The matcher to use
     * @return This object
     */
    TestResponse assertHeader(String name, Matcher matcher);

    /**
     * Verifies HTTP response body XHTML/XML content against XPath query.
     * @param xpath Query to use
     * @return This object
     */
    TestResponse assertXPath(String xpath);

}
