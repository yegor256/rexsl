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
package com.rexsl.core;

import com.rexsl.test.XhtmlMatchers;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link ContextResourceResolver}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @todo #336 The majority of tests here are disabled, because YMOCK doesn't
 *  work at the moment with this Socket mocking mechanism. As soon as this
 *  feature is fixed in YMOCK let's use it here
 */
public final class ContextResourceResolverTest {

    /**
     * ContextResourceResolver can resolve resource by HREF.
     * @throws Exception If something goes wrong
     */
    @Test
    public void resolvesResourceByHref() throws Exception {
        final String href = "/test.xml?123";
        final ServletContext ctx = new ServletContextMocker()
            .withResource("/test.xml", "<r>\u0443</r>")
            .mock();
        final URIResolver resolver = new ContextResourceResolver(ctx);
        final Source src = resolver.resolve(href, null);
        MatcherAssert.assertThat(src.getSystemId(), Matchers.equalTo(href));
        MatcherAssert.assertThat(src, XhtmlMatchers.hasXPath("/r[.='\u0443']"));
    }

    /**
     * ContextResourceResolver can resolve when a resource is an Absolute URI.
     * @throws Exception If something goes wrong
     */
    @Test
    @org.junit.Ignore
    public void resolvesWhenResourcesIsAnAbsoluteLink() throws Exception {
        final String href = "http://localhost/xsl/file.xsl";
        final ServletContext ctx = new ServletContextMocker()
            .withoutResource(href)
            .mock();
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
     * ContextResourceResolver throws exception if absolute URI fetched through
     * HTTP has invalid (non-OK) HTTP response status code.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.xml.transform.TransformerException.class)
    @org.junit.Ignore
    public void throwsExceptionWhenAbsoluteResourceHasInvalidStatusCode()
        throws Exception {
        final String href = "http://localhost/some-non-existing-file.xsl";
        final ServletContext ctx = new ServletContextMocker()
            .withoutResource(href)
            .mock();
        final URIResolver resolver = new ContextResourceResolver(ctx);
        final HttpURLConnection conn = this.mockConnection(href);
        Mockito.doReturn(HttpURLConnection.HTTP_NOT_FOUND)
            .when(conn).getResponseCode();
        resolver.resolve(href, null);
    }

    /**
     * ContextResourceResolver throws exception when absolute URI
     * fetched through HTTP throws IO exception.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.xml.transform.TransformerException.class)
    @org.junit.Ignore
    public void throwsExceptionWhenAbsoluteResourceThrowsIoException()
        throws Exception {
        final String href = "http://localhost/erroneous-file.xsl";
        final ServletContext ctx = new ServletContextMocker()
            .withoutResource(href)
            .mock();
        final URIResolver resolver = new ContextResourceResolver(ctx);
        final HttpURLConnection conn = this.mockConnection(href);
        Mockito.doThrow(new java.io.IOException("ouch")).when(conn).connect();
        resolver.resolve(href, null);
    }

    /**
     * ContextResourceResolver throws exception when resource is absent and
     * is not an absolute URI.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.xml.transform.TransformerException.class)
    public void throwsWhenResourceIsAbsent() throws Exception {
        final String href = "/xsl/file.xsl";
        final ServletContext ctx = new ServletContextMocker()
            .withoutResource(href)
            .mock();
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
        return (HttpURLConnection) new URL(href).openConnection();
    }

}
