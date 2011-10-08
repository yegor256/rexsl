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
@PrepareForTest({ XslBrowserFilter.class, ByteArrayResponseWrapper.class,
    StringWriter.class, TransformerFactory.class })
public final class XslBrowserFilterTest {

    /**
     * HTTP "Accept" header.
     */
    private static final String ACCEPT_BROWSER_WITHOUT_XML =
        "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8";

    /**
     * HTTP "Accept" header.
     */
    private static final String ACCEPT_BROWSER_WITH_XML =
        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    /**
     * HTTP "Accept" header.
     */
    private static final String ACCEPT_XMLONLY =
        "application/xml;q=0.9,*/*;q=0.8";

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
    public void testMissingUserAgentAcceptingXml1() throws Exception {
        this.filter(
            "<?xml version='1.0'?><document/>",
            this.ACCEPT_BROWSER_WITH_XML,
            null
        );
        Mockito.verify(this.transformer, Mockito.times(0))
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testMissingUserAgentAcceptingXml2() throws Exception {
        this.filter(
            "<?xml version='1.0'?><page><data>1</data></page>",
            this.ACCEPT_XMLONLY,
            null
        );
        Mockito.verify(this.transformer, Mockito.times(0))
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testMissingUserAgentNotAcceptingXml() throws Exception {
        this.filter(
            "<?xml version='1.0'?><files><file nam='test.txt'/></files>",
            this.ACCEPT_BROWSER_WITHOUT_XML,
            null
        );
        Mockito.verify(this.transformer)
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testMissingUserAgentAndMissingAcceptHeader() throws Exception {
        this.filter(
            "<?xml version='1.0'?><data/>",
            null,
            null
        );
        Mockito.verify(this.transformer)
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testDoTransform() throws Exception {
        this.filter(
            "<?xml version='1.0'?><doc/>",
            this.ACCEPT_BROWSER_WITH_XML,
            "Firefox 3.0"
        );
        Mockito.verify(this.transformer)
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
        Mockito.reset(this.transformer);
        this.filter(
            "<?xml version='1.0'?><documents/>",
            this.ACCEPT_XMLONLY,
            "Firefox 2.0"
        );
        Mockito.verify(this.transformer)
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
        Mockito.reset(this.transformer);
        this.filter(
            "<?xml version='1.0'?><abc/>",
            this.ACCEPT_BROWSER_WITHOUT_XML,
            "Chrome 2.0"
        );
        Mockito.verify(this.transformer)
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
        Mockito.reset(this.transformer);
        this.filter(
            "<?xml version='1.0'?><test/>",
            null,
            "Chrome 9.0"
        );
        Mockito.verify(this.transformer)
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testDontTransform() throws Exception {
        this.filter(
            "<?xml version='1.0'?><data-module/>",
            this.ACCEPT_BROWSER_WITH_XML,
            "Chrome beta"
        );
        Mockito.verify(this.transformer, Mockito.times(0))
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
        Mockito.reset(this.transformer);
        this.filter(
            "<?xml version='1.0'?><some-data/>",
            this.ACCEPT_XMLONLY,
            null
        );
        Mockito.verify(this.transformer, Mockito.times(0))
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testDontTransformEmptyDoc() throws Exception {
        this.filter(
            "",
            this.ACCEPT_BROWSER_WITH_XML,
            "Firefox 1.0"
        );
        Mockito.verify(this.transformer, Mockito.times(0))
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testDontTransformNonXmlDoc() throws Exception {
        this.filter(
            "some text",
            this.ACCEPT_BROWSER_WITH_XML,
            "Firefox 1a"
        );
        Mockito.verify(this.transformer, Mockito.times(0))
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test(expected = IllegalStateException.class)
    public void testTransformingWithTransformerException() throws Exception {
        Mockito.doThrow(new TransformerException("some message"))
            .when(this.transformer)
            .transform(Mockito.any(Source.class), Mockito.any(Result.class));
        this.filter(
            "<?xml version='1.0'?><error/>",
            this.ACCEPT_BROWSER_WITH_XML,
            "Firefox 5.0"
        );
    }

    /**
     * Filter one resource.
     * @param xml The XML
     * @param accept Value of "Accept" HTTP header
     * @param agent Value of "User-agent" HTTP header
     * @throws Exception If something goes wrong
     */
    private void filter(final String xml, final String accept,
        final String agent) throws Exception {
        final ServletContext context = Mockito.mock(ServletContext.class);
        final XslBrowserFilter filter = new XslBrowserFilter(context);
        final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getHeader("User-Agent")).thenReturn(agent);
        Mockito.when(req.getHeader("Accept")).thenReturn(accept);
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
        Mockito.when(wrapper.getByteStream()).thenReturn(stream);
        filter.filter(req, res);
    }

}
