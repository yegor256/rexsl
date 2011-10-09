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

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * XslBrowserFilter test case.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ XsltFilter.class, ByteArrayResponseWrapper.class,
    StringWriter.class, TransformerFactory.class })
public final class XsltFilterTest {

    /**
     * XSLT transformer.
     */
    private Transformer transformer;

    /**
     * Let's prepare a transformer.
     * @throws Exception If something goes wrong
     */
    @Before
    public void mockTransformer() throws Exception {
        PowerMockito.mockStatic(TransformerFactory.class);
        final TransformerFactory factory =
            Mockito.mock(TransformerFactory.class);
        PowerMockito.when(TransformerFactory.newInstance())
            .thenReturn(factory);
        this.transformer = Mockito.mock(Transformer.class);
        Mockito.when(factory.newTransformer(Mockito.any(Source.class)))
            .thenReturn(this.transformer);
        final StringWriter writer = PowerMockito.mock(StringWriter.class);
        PowerMockito.whenNew(StringWriter.class).withNoArguments()
            .thenReturn(writer);
        PowerMockito.when(writer.toString())
            .thenReturn("<?xml version=\"1.0\"?>");
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testMissingUserAgent() throws Exception {
        // user agent is accepting XML, but we don't recognize it
        // as an agent we can trust - we should convert to XHTML
        this.filter("application/xml;q=0.9,*/*;q=0.7", null);
        this.verifyTransformation();
        Mockito.reset(this.transformer);
        // the agent is not provided, but it explicitly is asking
        // for clear XML - we provide it
        this.filter(XsltFilter.MIME_XML, null);
        this.verifyNoTransformation();
        Mockito.reset(this.transformer);
        // the agent didn't provide any information about itself,
        // and about the output it looking for - we should transform
        this.filter(null, null);
        this.verifyTransformation();
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testDoTransform() throws Exception {
        // the agent is asking for application/xml, but not explicitly
        // and this agent is not the one we can trust - let's transform
        this.filter("application/xml;q=0.9,*/*;q=0.8", "Firefox");
        this.verifyTransformation();
        Mockito.reset(this.transformer);
        // the agent is trustable, but it is not accepting "application/xml",
        // that's why we should transform
        this.filter(null, "Chrome 9.0");
        this.verifyTransformation();
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testDontTransform() throws Exception {
        // the agent is the one we trust and it is accepting XML, that's
        // why we shouldn't tranform
        this.filter("text/html,application/xml;q=0.9,*/*;q=0.8", "Chrome");
        this.verifyNoTransformation();
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testDontTransformEmptyDoc() throws Exception {
        this.filter("");
        this.verifyNoTransformation();
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testDontTransformNonXmlDoc() throws Exception {
        this.filter("this is text document");
        this.verifyNoTransformation();
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.servlet.ServletException.class)
    public void testTransformingWithTransformerException() throws Exception {
        Mockito.doThrow(new TransformerException("some message"))
            .when(this.transformer)
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
        this.filter("text/plain", "some agent");
    }

    /**
     * Verify that no transformation has been done.
     * @throws Exception If something goes wrong
     */
    private void verifyNoTransformation() throws Exception {
        Mockito.verify(this.transformer, Mockito.times(0))
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
    }

    /**
     * Verify that no transformation has been done.
     * @throws Exception If something goes wrong
     */
    private void verifyTransformation() throws Exception {
        Mockito.verify(this.transformer)
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
    }

    /**
     * Filter one resource.
     * @param accept Value of "Accept" HTTP header
     * @param agent Value of "User-agent" HTTP header
     * @throws Exception If something goes wrong
     */
    private void filter(final String accept, final String agent)
        throws Exception {
        this.filter("<?xml version='1.0'?><test/>", accept, agent);
    }

    /**
     * Filter one resource.
     * @param content The content
     * @throws Exception If something goes wrong
     */
    private void filter(final String content) throws Exception {
        this.filter(
            content,
            "application/xml,text/plain;q=0.9,*/*;q=0.8",
            // @checkstyle LineLength (1 line)
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1"
        );
    }

    /**
     * Filter one resource.
     * @param xml The XML
     * @param accept Value of "Accept" HTTP header
     * @param agent Value of "User-agent" HTTP header
     * @throws Exception If something goes wrong
     */
    private void filter(final String xml,
        final String accept, final String agent) throws Exception {
        final ServletContext context = Mockito.mock(ServletContext.class);
        final Filter filter = new XsltFilter();
        final FilterConfig config = Mockito.mock(FilterConfig.class);
        Mockito.doReturn(context).when(config).getServletContext();
        filter.init(config);
        final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn(agent).when(req).getHeader("User-Agent");
        Mockito.doReturn(accept).when(req).getHeader("Accept");
        Mockito.doReturn("/").when(req).getRequestURI();
        final HttpServletResponse res = Mockito.mock(HttpServletResponse.class);
        Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
            .when(res).getOutputStream();
        final ByteArrayResponseWrapper wrapper =
            PowerMockito.mock(ByteArrayResponseWrapper.class);
        PowerMockito.whenNew(ByteArrayResponseWrapper.class)
            .withArguments(Mockito.any(HttpServletResponse.class))
            .thenReturn(wrapper);
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(xml.getBytes());
        Mockito.doReturn(stream).when(wrapper).getByteStream();
        final FilterChain chain = Mockito.mock(FilterChain.class);
        filter.doFilter(req, res, chain);
        filter.destroy();
    }

}
