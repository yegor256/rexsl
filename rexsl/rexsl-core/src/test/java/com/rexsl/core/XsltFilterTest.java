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
package com.rexsl.core;

import com.rexsl.test.XhtmlConverter;
import com.rexsl.test.XhtmlMatchers;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link XslFilter}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class XsltFilterTest {

    /**
     * XsltFilter can transform from XML to HTML.
     * @throws Exception If something goes wrong
     */
    @Test
    public void transformsFromXmlToHtml() throws Exception {
        final ServletContext context = new ServletContextMocker()
            .withResource(
                "/foo.xsl",
                // @checkstyle LineLength (1 line)
                "<stylesheet xmlns='http://www.w3.org/1999/XSL/Transform' xmlns:x='http://www.w3.org/1999/xhtml'><template match='/'><x:html><value-of select='/page/data'/></x:html></template></stylesheet>"
        ).mock();
        final FilterConfig config = new FilterConfigMocker()
            .withServletContext(context)
            .mock();
        final HttpServletRequest request = new HttpServletRequestMocker()
            .withHeader(HttpHeaders.USER_AGENT, "Firefox")
            .withHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
            .mock();
        final HttpServletResponse response = new HttpServletResponseMocker()
            .mock();
        final FilterChain chain = new FilterChainMocker()
            // @checkstyle LineLength (1 line)
            .withOutput("<?xml version='1.0'?><?xml-stylesheet href='/foo.xsl' type='text/xsl'?><page><data>123</data></page>")
            .mock();
        final Filter filter = new XsltFilter();
        filter.init(config);
        filter.doFilter(request, response, chain);
        filter.destroy();
        MatcherAssert.assertThat(
            XhtmlConverter.the(response.toString()),
            XhtmlMatchers.hasXPath("/xhtml:html[.='123']")
        );
    }

}
