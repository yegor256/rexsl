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

import com.jcabi.aspects.LogExceptions;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * All uncaught exceptions will be caught here.
 *
 * <p>Configure it in your {@code WEB-INF/web.xml} like this:
 *
 * <pre> &lt;servlet>
 *  &lt;servlet-name&gt;ExceptionTrap&lt;/servlet-name&gt;
 *  &lt;servlet-class&gt;com.rexsl.trap.ExceptionTrap&lt;/servlet-class&gt;
 *  &lt;init-param&gt;
 *   &lt;param-name&gt;template&lt;/param-name&gt;
 *   &lt;param-value&gt;/com/example/Trap-Template.html&lt;/param-value&gt;
 *  &lt;/init-param&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *  &lt;servlet-name&gt;ExceptionTrap&lt;/servlet-name&gt;
 *  &lt;url-pattern&gt;/trap&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * &lt;error-page&gt;
 *  &lt;exception-type&gt;java.lang.Throwable&lt;/exception-type&gt;
 *  &lt;location&gt;/trap&lt;/location&gt;
 * &lt;/error-page&gt;</pre>
 *
 * <p>The template of "service not available" web page is configured with
 * the only one {@code init-params}. The template should be available in
 * classpath as a plain text file (preferably in HTML) and shall contain
 * {@code &#36;&#123;text&#125;} text, which will be replaced with
 * a full description of exception just thrown and caught.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.2
 */
@ToString
@EqualsAndHashCode(callSuper = false, of = "template")
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public final class ExceptionTrap extends HttpServlet {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x75298A7876D21470L;

    /**
     * The template.
     */
    private Template template;

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (3 lines)
     */
    @Override
    public void init() throws ServletException {
        final String param = this.getInitParameter("template");
        if (param == null) {
            this.template = new AlertTemplate("HTML template not configured");
        } else {
            this.template = new StaticTemplate(
                URI.create(
                    param.replaceAll("[\\p{Cntrl}\\p{Space}]+", "")
                )
            );
        }
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    @LogExceptions
    public void service(final HttpServletRequest request,
        final HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        final StringBuilder text = this.text(request);
        try {
            response.getWriter().print(this.template.render(text.toString()));
        } finally {
            response.getWriter().close();
        }
        Logger.error(this, "#service():\n%s", text);
    }

    /**
     * Prepare text.
     * @param request The HTTP request
     * @return Builder of text
     */
    private StringBuilder text(final HttpServletRequest request) {
        final StringBuilder text = new StringBuilder(Tv.THOUSAND)
            .append(Logger.format("date: %s\n", new Date()));
        this.append(text, request, "code");
        this.append(text, request, "message");
        this.append(text, request, "exception_type");
        this.append(text, request, "request_uri");
        final ServletContext ctx = this.getServletContext();
        text
            .append(
                Logger.format(
                    "servlet context path: \"%s\"\n",
                    request.getContextPath()
            )
        )
            .append(
                Logger.format(
                    "requested: %s (%s) at %s:%d\n",
                    request.getRequestURL().toString(),
                    request.getMethod(),
                    request.getServerName(),
                    request.getServerPort()
                )
            )
            .append(
                Logger.format(
                    "request: %s, %d bytes\n",
                    request.getContentType(),
                    request.getContentLength()
                )
            )
            .append(
                Logger.format(
                    "remote: %s:%d (%s)\n",
                    request.getRemoteAddr(),
                    request.getRemotePort(),
                    request.getRemoteHost()
                )
            )
            .append(
                Logger.format(
                    "servlet: \"%s\" (API %d.%d) at \"%s\"\n",
                    ctx.getServletContextName(),
                    ctx.getMajorVersion(),
                    ctx.getMinorVersion(),
                    ctx.getServerInfo()
                )
            )
            .append("headers:\n")
            .append(ExceptionTrap.headers(request))
            .append('\n')
            .append(
                Logger.format(
                    "exception: %[exception]s\n",
                    request.getAttribute("javax.servlet.error.exception")
                )
            );
        return text;
    }

    /**
     * Extend velocity context with a value from java servlet.
     * @param text The text to add to
     * @param request The request to get attributes from
     * @param suffix The suffix of java attribute
     */
    private void append(final StringBuilder text,
        final ServletRequest request, final String suffix) {
        Object attr = request.getAttribute(
            Logger.format("javax.servlet.error.%s", suffix)
        );
        if (attr == null) {
            attr = "NULL";
        }
        text.append(suffix)
            // @checkstyle MultipleStringLiterals (1 line)
            .append(": ")
            .append(attr.toString())
            .append('\n');
    }

    /**
     * Format headers.
     * @param request The HTTP request
     * @return Text with headers
     */
    private static String headers(final HttpServletRequest request) {
        final StringBuilder text = new StringBuilder(0);
        final Enumeration<?> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            final String header = names.nextElement().toString();
            text.append("  ")
                .append(header)
                .append(": ")
                .append(request.getHeader(header))
                .append('\n');
        }
        return text.toString();
    }
}
