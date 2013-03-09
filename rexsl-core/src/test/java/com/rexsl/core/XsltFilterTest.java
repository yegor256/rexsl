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
package com.rexsl.core;

import com.jcabi.manifests.Manifests;
import com.rexsl.test.XhtmlMatchers;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link XslFilter}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class XsltFilterTest {

    /**
     * XsltFilter can transform from XML to HTML.
     * @throws Exception If something goes wrong
     */
    @Test
    public void transformsFromXmlToHtml() throws Exception {
        Manifests.inject("ReXSL-Version", "1.0-SNAPSHOT");
        final ServletContext context = new ServletContextMocker()
            .withResource(
                "/foo.xsl",
                // @checkstyle LineLength (1 line)
                "<stylesheet xmlns='http://www.w3.org/1999/XSL/Transform' xmlns:x='http://www.w3.org/1999/xhtml'><template match='/'><x:html><x:div><value-of select='/page/data'/></x:div><x:p>\u0443</x:p></x:html></template></stylesheet>"
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
            .withOutput("<?xml version='1.0'?><?xml-stylesheet href='/foo.xsl' type='text/xsl'?><page><data>\u0443\u0440\u0430</data></page>")
            .mock();
        final Filter filter = new XsltFilter();
        filter.init(config);
        filter.doFilter(request, response, chain);
        filter.destroy();
        MatcherAssert.assertThat(
            response,
            XhtmlMatchers.hasXPath("//xhtml:div[.='\u0443\u0440\u0430']")
        );
        MatcherAssert.assertThat(
            response,
            XhtmlMatchers.hasXPath("/xhtml:html/xhtml:p[.='\u0443']")
        );
    }

    /**
     * XsltFilter can pass binary content through.
     * @throws Exception If something goes wrong
     */
    @Test
    public void doesntTouchBinaryContent() throws Exception {
        final byte[] binary = new byte[] {(byte) 0x00, (byte) 0xff};
        final FilterChain chain = new FilterChainMocker()
            .withOutput(binary)
            .mock();
        final Filter filter = new XsltFilter();
        filter.init(new FilterConfigMocker().mock());
        final ServletOutputStream stream =
            Mockito.mock(ServletOutputStream.class);
        final HttpServletResponse response =
            Mockito.mock(HttpServletResponse.class);
        Mockito.doReturn(stream).when(response).getOutputStream();
        filter.doFilter(
            new HttpServletRequestMocker().mock(),
            response,
            chain
        );
        filter.destroy();
        Mockito.verify(stream).write(binary);
    }

}
