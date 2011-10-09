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
import java.util.Vector;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmlmatchers.XmlMatchers;

/**
 * Testing {@link RestfulServlet} class.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class RestfulServletTest {

    /**
     * A message.
     */
    private static final String MESSAGE = "some text";

    /**
     * Initialize JUL-to-SLF4J bridge.
     */
    @BeforeClass
    public static void julToSlf4j() {
        final java.util.logging.Logger rootLogger =
            java.util.logging.LogManager.getLogManager().getLogger("");
        final java.util.logging.Handler[] handlers =
            rootLogger.getHandlers();
        for (int idx = 0; idx < handlers.length; idx += 1) {
            rootLogger.removeHandler(handlers[idx]);
        }
        org.slf4j.bridge.SLF4JBridgeHandler.install();
    }

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testJerseyInteractions() throws Exception {
        final HttpServlet servlet = this.servlet();
        final HttpServletRequest request =
            Mockito.mock(HttpServletRequest.class);
        final String root = "/";
        Mockito.doReturn(root).when(request).getRequestURI();
        Mockito.doReturn("").when(request).getContextPath();
        Mockito.doReturn(root).when(request).getPathInfo();
        Mockito.doReturn("").when(request).getServletPath();
        Mockito.doReturn(new StringBuffer("http://localhost/"))
            .when(request).getRequestURL();
        Mockito.doReturn(new Vector<String>().elements())
            .when(request).getHeaderNames();
        Mockito.doReturn("GET").when(request).getMethod();
        final HttpServletResponse response =
            Mockito.mock(HttpServletResponse.class);
        final Stream stream = new Stream();
        Mockito.doReturn(stream).when(response).getOutputStream();
        servlet.service(request, response);
        Mockito.verify(response).setStatus(HttpServletResponse.SC_OK);
        MatcherAssert.assertThat(
            XhtmlConverter.the(stream.toString()),
            XmlMatchers.hasXPath("/page[.='test']")
        );
    }

    /**
     * Create and initialize servlet.
     * @return The servlet for tests
     * @throws Exception If something goes wrong
     */
    private HttpServlet servlet() throws Exception {
        final ServletConfig config = Mockito.mock(ServletConfig.class);
        Mockito.doReturn(new Vector<String>().elements())
            .when(config).getInitParameterNames();
        final ServletContext ctx = Mockito.mock(ServletContext.class);
        Mockito.doReturn(ctx).when(config).getServletContext();
        Mockito.doReturn(this.getClass().getResourceAsStream("main.xsl"))
            .when(ctx).getResourceAsStream("/xsl/main.xsl");
        final HttpServlet servlet = new RestfulServlet();
        servlet.init(config);
        return servlet;
    }

    @Path("/")
    public static final class FrontEnd {
        /**
         * Simple name.
         * @return The name
         */
        @GET
        public String getName() {
            return "<?xml version='1.0'?><page>test</page>";
        }
    }

    private static final class Stream extends ServletOutputStream {
        /**
         * Buffer to hold all output.
         */
        private final StringBuffer buffer = new StringBuffer();
        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final int data) {
            this.buffer.append((char) data);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this.buffer.toString();
        }
    }

}
