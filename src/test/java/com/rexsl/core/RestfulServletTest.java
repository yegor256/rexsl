/**
 * Copyright (c) 2011-2014, ReXSL.com
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

import com.rexsl.mock.HttpServletRequestMocker;
import com.rexsl.mock.HttpServletResponseMocker;
import com.rexsl.mock.ServletConfigMocker;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RestfulServlet}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class RestfulServletTest {

    /**
     * RestfulServlet can pass HTTP requests to Jersey for further processing.
     * @throws Exception If something goes wrong
     */
    @Test
    public void passesHttpRequestsToJersey() throws Exception {
        final ServletConfig config = new ServletConfigMocker()
            .withParam(RestfulServlet.PACKAGES, "\t\ncom.rexsl.core  \r")
            .mock();
        final HttpServlet servlet = new RestfulServlet();
        servlet.init(config);
        final HttpServletResponse response = new HttpServletResponseMocker()
            .expectStatus(HttpServletResponse.SC_OK)
            .mock();
        servlet.service(new HttpServletRequestMocker().mock(), response);
        MatcherAssert.assertThat(
            response.toString(),
            Matchers.containsString("\u0443\u0440\u0430")
        );
    }

    /**
     * Attemps to create a new restful servlet with a configuration argument
     * that contains a non-valid package.
     * The package name contains hyphens.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.servlet.ServletException.class)
    public void initWithNonValidPackageHyphens() throws Exception {
        final ServletConfig config = new ServletConfigMocker()
            .withParam(RestfulServlet.PACKAGES, "package-with-hyphens")
            .mock();
        final HttpServlet servlet = new RestfulServlet();
        servlet.init(config);
    }

    /**
     * Attemps to create a new restful servlet with a configuration argument
     * that contains a non-valid package.
     * One of the packages starts with a number.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.servlet.ServletException.class)
    public void initWithNonValidPackageStartsWithNumber() throws Exception {
        final ServletConfig config = new ServletConfigMocker()
            .withParam(RestfulServlet.PACKAGES, "pa.1ck.age")
            .mock();
        final HttpServlet servlet = new RestfulServlet();
        servlet.init(config);
    }

    /**
     * RestfulServlet can be serialized and de-serialized.
     * @throws Exception If something goes wrong
     * @todo #204 This test is disabled because of a defect in Jersey:
     *  http://java.net/jira/browse/JERSEY-1128. Once it is resolved we
     *  can try to enable the test and see how it works.
     */
    @Test
    @org.junit.Ignore
    public void serializesItselfToStreamAndBack() throws Exception {
        final ServletConfig config = new ServletConfigMocker().withParam(
            RestfulServlet.PACKAGES,
            this.getClass().getPackage().getName()
        ).mock();
        final HttpServlet servlet = new RestfulServlet();
        servlet.init(config);
        final HttpServletResponse response = new HttpServletResponseMocker()
            .expectStatus(HttpServletResponse.SC_OK)
            .mock();
        HttpServlet.class.cast(
            SerializationUtils.deserialize(
                SerializationUtils.serialize(servlet)
            )
        ).service(new HttpServletRequestMocker().mock(), response);
        MatcherAssert.assertThat(
            response.toString(),
            Matchers.startsWith("\u0443\u0440")
        );
    }

    /**
     * It's a test JAX-RS resource.
     */
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
