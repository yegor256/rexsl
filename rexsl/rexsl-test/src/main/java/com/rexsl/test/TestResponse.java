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
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

/**
 * Resonse returned by {@link TestClient}.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
interface TestResponse {

    /**
     * Follow the LOCATION header.
     * @return New client
     * @throws Exception If some problem inside
     */
    TestClient follow();

    /**
     * Find link in XML and return new client with this link as URI.
     * @param xpath The path of the link
     * @return New client
     * @throws Exception If some problem inside
     */
    TestClient rel(String xpath) throws Exception;

    /**
     * Get body as a string.
     * @return The body
     * @throws IOException If some IO problem inside
     */
    String getBody() throws IOException;

    /**
     * Get status of the response as a number.
     * @return The status
     */
    Integer getStatus();

    /**
     * Get body as {@link GPathResult}.
     * @return The GPath result
     * @throws Exception If some problem inside
     */
    GPathResult getGpath() throws Exception;

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
     * @throws IOException If some problem with body retrieval
     */
    TestResponse assertBody(Matcher<String> matcher) throws IOException;

    /**
     * Verifies HTTP response body XHTML/XML content against XPath query.
     * @param xpath Query to use
     * @return This object
     * @throws Exception If some problem with body retrieval or conversion
     */
    TestResponse assertXPath(String xpath) throws Exception;

}
