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
import com.rexsl.mock.ServletConfigMocker;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link ExceptionTrap}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ExceptionTrapTest {

    /**
     * Code start.
     */
    private static final String CODE_START = "code: ";

    /**
     * ExceptionTrap can render page with exception.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersExceptionIntoHtmlPage() throws Exception {
        final ServletConfig config = new ServletConfigMocker().mock();
        final HttpServlet servlet = new ExceptionTrap();
        servlet.init(config);
        MatcherAssert.assertThat(
            this.request(servlet),
            Matchers.containsString(ExceptionTrapTest.CODE_START)
        );
    }

    /**
     * ExceptionTrap can show all headers of the request.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void showsHeaders() throws Exception {
        final ServletConfig config = new ServletConfigMocker().mock();
        final HttpServlet servlet = new ExceptionTrap();
        servlet.init(config);
        final HttpServletRequest request = new HttpServletRequestMocker()
            .withHeader("header1", "A")
            .withHeader("header2", "B")
            .mock();
        final HttpServletResponse response =
            Mockito.mock(HttpServletResponse.class);
        final StringWriter writer = new StringWriter();
        Mockito.doReturn(new PrintWriter(writer)).when(response).getWriter();
        servlet.service(request, response);
        MatcherAssert.assertThat(
            writer.toString(),
            Matchers.allOf(
                Matchers.containsString("header1: A"),
                Matchers.containsString("header2: B")
            )
        );
    }

    /**
     * ExceptionTrap should serialize itself correctly.
     * @throws Exception in case of error.
     */
    @Test
    public void serializesItself() throws Exception {
        final ExceptionTrap before = new ExceptionTrap();
        final ServletConfig config = new ServletConfigMocker().mock();
        before.init(config);
        MatcherAssert.assertThat(
            this.request(before),
            Matchers.containsString(ExceptionTrapTest.CODE_START)
        );
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectOutputStream ostream = new ObjectOutputStream(out);
        ostream.writeObject(before);
        ostream.close();
        final ExceptionTrap after = (ExceptionTrap) new ObjectInputStream(
            new ByteArrayInputStream(out.toByteArray())
        ).readObject();
        after.init(config);
        MatcherAssert.assertThat(
            this.request(after),
            Matchers.containsString(ExceptionTrapTest.CODE_START)
        );
    }

    /**
     * Make a request to servlet and return the response.
     * @param servlet Servlet to make the request.
     * @return Response text.
     * @throws Exception In case of error.
     */
    private String request(final HttpServlet servlet) throws Exception {
        final HttpServletRequest request =
            new HttpServletRequestMocker().mock();
        final HttpServletResponse response =
            Mockito.mock(HttpServletResponse.class);
        final StringWriter writer = new StringWriter();
        Mockito.doReturn(new PrintWriter(writer)).when(response).getWriter();
        servlet.service(request, response);
        return writer.toString();
    }
}
