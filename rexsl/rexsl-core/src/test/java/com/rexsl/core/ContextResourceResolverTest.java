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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test case for {@link ContextResourceResolver}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ContextResourceResolver.class, UriBuilder.class })
public final class ContextResourceResolverTest {

    /**
     * Let's test simple resolving.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testSimpleResolving() throws Exception {
        final ServletContext ctx = Mockito.mock(ServletContext.class);
        final String href = "/xsl/layout.xsl";
        final InputStream stream = IOUtils.toInputStream("");
        Mockito.doReturn(stream).when(ctx).getResourceAsStream(href);
        final URIResolver resolver = new ContextResourceResolver(ctx);
        final Source src = resolver.resolve(href, null);
        MatcherAssert.assertThat(src.getSystemId(), Matchers.equalTo(href));
    }

    /**
     * Absolute URI should be fetched through HTTP.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testWithAbsoluteResource() throws Exception {
        final ServletContext ctx = Mockito.mock(ServletContext.class);
        final String href = "http://localhost/xsl/file.xsl";
        Mockito.doReturn(null).when(ctx).getResourceAsStream(href);
        final URIResolver resolver = new ContextResourceResolver(ctx);
        final HttpURLConnection conn = this.mockConnection(href);
        Mockito.doReturn(HttpURLConnection.HTTP_OK)
            .when(conn).getResponseCode();
        Mockito.doReturn(
            IOUtils.toInputStream(
                "<stylesheet xmlns='http://www.w3.org/1999/XSL/Transform'/>"
            )
        )
            .when(conn).getInputStream();
        final Source src = resolver.resolve(href, null);
        MatcherAssert.assertThat(src, Matchers.notNullValue());
        TransformerFactory.newInstance().newTransformer(src);
    }

    /**
     * Absolute URI fetched through HTTP with invalid (non-OK) HTTP response.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.xml.transform.TransformerException.class)
    public void testWithAbsoluteResourceAndInvalidCode() throws Exception {
        final ServletContext ctx = Mockito.mock(ServletContext.class);
        final String href = "http://localhost/some-non-existing-file.xsl";
        Mockito.doReturn(null).when(ctx).getResourceAsStream(href);
        final URIResolver resolver = new ContextResourceResolver(ctx);
        final HttpURLConnection conn = this.mockConnection(href);
        Mockito.doReturn(HttpURLConnection.HTTP_NOT_FOUND)
            .when(conn).getResponseCode();
        resolver.resolve(href, null);
    }

    /**
     * Absolute URI fetched through HTTP with IO exception.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.xml.transform.TransformerException.class)
    public void testWithAbsoluteResourceAndIOException() throws Exception {
        final ServletContext ctx = Mockito.mock(ServletContext.class);
        final String href = "http://localhost/erroneous-file.xsl";
        Mockito.doReturn(null).when(ctx).getResourceAsStream(href);
        final URIResolver resolver = new ContextResourceResolver(ctx);
        final HttpURLConnection conn = this.mockConnection(href);
        Mockito.doThrow(new java.io.IOException("ouch")).when(conn).connect();
        resolver.resolve(href, null);
    }

    /**
     * Let's test with absent resource.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.xml.transform.TransformerException.class)
    public void testWithAbsentResource() throws Exception {
        final ServletContext ctx = Mockito.mock(ServletContext.class);
        final String href = "/xsl/file.xsl";
        Mockito.doReturn(null).when(ctx).getResourceAsStream(href);
        final URIResolver resolver = new ContextResourceResolver(ctx);
        resolver.resolve(href, null);
    }

    /**
     * Mock {@link HttpURLConnection} for the specific HREF.
     * @param href The URI
     * @return The connection mock
     * @throws Exception If something goes wrong
     */
    private HttpURLConnection mockConnection(final String href)
        throws Exception {
        PowerMockito.mockStatic(UriBuilder.class);
        final UriBuilder builder = Mockito.mock(UriBuilder.class);
        Mockito.when(UriBuilder.fromUri(href)).thenReturn(builder);
        final URI uri = PowerMockito.mock(URI.class);
        Mockito.doReturn(uri).when(builder).build();
        PowerMockito.doReturn(true).when(uri).isAbsolute();
        final URL url = PowerMockito.mock(URL.class);
        PowerMockito.doReturn(url).when(uri).toURL();
        final HttpURLConnection conn = Mockito.mock(HttpURLConnection.class);
        PowerMockito.doReturn(conn).when(url).openConnection();
        return conn;
    }

}
