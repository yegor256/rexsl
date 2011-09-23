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
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.mockito.Mockito.*;

/**
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ XslBrowserFilter.class, ByteArrayResponseWrapper.class,
    StringWriter.class, TransformerFactory.class })
public final class XslBrowserFilterTest {

    private static final String ACCEPT_BROWSER_WITHOUT_XML =
        "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8";

    private static final String ACCEPT_BROWSER_WITH_XML =
        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    private static final String ACCEPT_XMLONLY =
        "application/xml;q=0.9,*/*;q=0.8";

    private Transformer transformer;

    @Before
    public void mockTransformer() throws Exception {
        PowerMockito.mockStatic(TransformerFactory.class);
        final TransformerFactory factory = mock(TransformerFactory.class);
        PowerMockito.when(TransformerFactory.newInstance())
            .thenReturn(factory);
        this.transformer = mock(Transformer.class);
        when(factory.newTransformer(any(Source.class)))
            .thenReturn(this.transformer);
        final StringWriter writer = PowerMockito.mock(StringWriter.class);
        PowerMockito.whenNew(StringWriter.class).withNoArguments()
            .thenReturn(writer);
        PowerMockito.when(writer.toString())
            .thenReturn("<?xml version=\"1.0\"?>");
    }

    @Test
    public void testMissingUserAgentAcceptingXml1() throws Exception {
        this.filter(
            "<?xml version='1.0'?><document/>",
            this.ACCEPT_BROWSER_WITH_XML,
            null
        );
        verify(this.transformer, times(0))
            .transform(any(Source.class), any(Result.class));
    }

    @Test
    public void testMissingUserAgentAcceptingXml2() throws Exception {
        this.filter(
            "<?xml version='1.0'?><page><data>1</data></page>",
            this.ACCEPT_XMLONLY,
            null
        );
        verify(this.transformer, times(0))
            .transform(any(Source.class), any(Result.class));
    }

    @Test
    public void testMissingUserAgentNotAcceptingXml() throws Exception {
        this.filter(
            "<?xml version='1.0'?><files><file nam='test.txt'/></files>",
            this.ACCEPT_BROWSER_WITHOUT_XML,
            null
        );
        verify(this.transformer)
            .transform(any(Source.class), any(Result.class));
    }

    @Test
    public void testMissingUserAgentAndMissingAcceptHeader() throws Exception {
        this.filter(
            "<?xml version='1.0'?><data/>",
            null,
            null
        );
        verify(this.transformer)
            .transform(any(Source.class), any(Result.class));
    }

    @Test
    public void testDoTransform() throws Exception {
        this.filter(
            "<?xml version='1.0'?><doc/>",
            this.ACCEPT_BROWSER_WITH_XML,
            "Firefox"
        );
        verify(this.transformer)
            .transform(any(Source.class), any(Result.class));
        reset(this.transformer);
        this.filter(
            "<?xml version='1.0'?><documents/>",
            this.ACCEPT_XMLONLY,
            "Firefox"
        );
        verify(this.transformer)
            .transform(any(Source.class), any(Result.class));
        reset(this.transformer);
        this.filter(
            "<?xml version='1.0'?><abc/>",
            this.ACCEPT_BROWSER_WITHOUT_XML,
            "Chrome"
        );
        verify(this.transformer).transform(any(Source.class),
            any(Result.class));
        reset(this.transformer);
        this.filter(
            "<?xml version='1.0'?><test/>",
            null,
            "Chrome"
        );
        verify(this.transformer)
            .transform(any(Source.class), any(Result.class));
    }

    @Test
    public void testDontTransform() throws Exception {
        this.filter(
            "<?xml version='1.0'?><data-module/>",
            this.ACCEPT_BROWSER_WITH_XML,
            "Chrome"
        );
        verify(this.transformer, times(0))
            .transform(any(Source.class), any(Result.class));
        reset(this.transformer);
        this.filter(
            "<?xml version='1.0'?><some-data/>",
            this.ACCEPT_XMLONLY,
            null
        );
        verify(this.transformer, times(0))
            .transform(any(Source.class), any(Result.class));
    }

    @Test
    public void testDontTransformEmptyDoc() throws Exception {
        this.filter(
            "",
            this.ACCEPT_BROWSER_WITH_XML,
            "Firefox"
        );
        verify(this.transformer, times(0))
            .transform(any(Source.class), any(Result.class));
    }

    @Test
    public void testDontTransformNonXmlDoc() throws Exception {
        this.filter(
            "some text",
            this.ACCEPT_BROWSER_WITH_XML,
            "Firefox"
        );
        verify(this.transformer, times(0))
            .transform(any(Source.class), any(Result.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testTransformingWithTransformerException() throws Exception {
        doThrow(new javax.xml.transform.TransformerException("some message"))
            .when(this.transformer)
            .transform(any(Source.class), any(Result.class));
        this.filter(
            "<?xml version='1.0'?><error/>",
            this.ACCEPT_BROWSER_WITH_XML,
            "Firefox"
        );
    }

    private void filter(final String xml, final String accept,
        final String agent) throws Exception {
        final XslBrowserFilter filter = new XslBrowserFilter();
        final FilterConfig config = mock(FilterConfig.class);
        filter.init(config);
        final FilterChain chain = mock(FilterChain.class);
        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("User-Agent")).thenReturn(agent);
        when(req.getHeader("Accept")).thenReturn(accept);
        final HttpServletResponse res = mock(HttpServletResponse.class);
        doReturn(mock(ServletOutputStream.class)).when(res).getOutputStream();
        final ByteArrayResponseWrapper wrapper =
            PowerMockito.mock(ByteArrayResponseWrapper.class);
        PowerMockito.whenNew(ByteArrayResponseWrapper.class)
            .withArguments(any(HttpServletResponse.class)).thenReturn(wrapper);
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(xml.getBytes());
        when(wrapper.getByteStream()).thenReturn(stream);
        filter.doFilter(req, res, chain);
        filter.destroy();
    }

}
