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

import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Testing {@link RestfulServlet} class.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class RestfulServletTest {

    /**
     * Let's test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testJerseyInteractions() throws Exception {
        final ServletContext ctx = new ServletContextMocker().mock();
        final ServletConfig config = new ServletConfigMocker()
            .withParam("com.rexsl.PACKAGES", "com.rexsl.core")
            .withServletContext(ctx)
            .mock();
        // Mockito.doReturn(this.getClass().getResourceAsStream("main.xsl"))
        //     .when(ctx).getResourceAsStream("/xsl/main.xsl");
        final HttpServlet servlet = new RestfulServlet();
        servlet.init(config);
        final HttpServletRequest request = new HttpServletRequestMocker()
            .mock();
        final HttpServletResponse response = new HttpServletResponseMocker()
            .expectStatus(HttpServletResponse.SC_OK)
            .mock();
        servlet.service(request, response);
        MatcherAssert.assertThat(
            response.toString(),
            Matchers.containsString("\u0443\u0440\u0430")
        );
    }

    @Path("/")
    public static final class FrontEnd {
        /**
         * Front page.
         * @return The content of it
         */
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String front() {
            return "some text, \u0443\u0440\u0430!";
        }
    }

}
