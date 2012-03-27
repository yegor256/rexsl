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
package com.rexsl.trap;

import com.ymock.util.Logger;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.CharEncoding;

/**
 * All uncaught exceptions will be catched here.
 *
 * <p>Configure it in your {@code WEB-INF/web.xml} like this:
 *
 * <pre>
 * &lt;servlet>
 *  &lt;servlet-name&gt;ExceptionTrap&lt;/servlet-name&gt;
 *  &lt;servlet-class&gt;com.rexsl.trap.ExceptionTrap&lt;/servlet-class&gt;
 *  &lt;init-param&gt;
 *   &lt;param-name&gt;com.rexsl.trap.Template&lt;/param-name&gt;
 *   &lt;param-value&gt;
 *    com.rexsl.trap.ResourceTemplate?resource=com/example/Trap-Template.html
 *   &lt;/param-value&gt;
 *  &lt;/init-param&gt;
 *  &lt;init-param&gt;
 *   &lt;param-name&gt;com.rexsl.trap.Notifier&lt;/param-name&gt;
 *   &lt;param-value&gt;
 *    com.rexsl.trap.SmtpNotifier?to=me&#64;example.com&amp;...
 *   &lt;/param-value&gt;
 *  &lt;/init-param&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *  &lt;servlet-name&gt;ExceptionTrap&lt;/servlet-name&gt;
 *  &lt;url-pattern&gt;/trap&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * &lt;error-page&gt;
 *  &lt;exception-type&gt;java.lang.Throwable&lt;/exception-type&gt;
 *  &lt;location&gt;/trap&lt;/location&gt;
 * &lt;/error-page&gt;
 * </pre>
 *
 * <p>Configuration of the trap behavior is done through {@code init-params},
 * which are named as interfaces with values as URIs. Every URI has a name
 * of implementation class and a list of query params, which will be send to
 * the class in {@link Properties}. See {@link SmtpNotifier} and
 * {@link SmtpBulkNotifier} for better examples.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.6
 */
public final class ExceptionTrap extends HttpServlet {

    /**
     * List of notifiers ready to notify.
     */
    private transient List<Notifier> notifiers;

    /**
     * The template.
     */
    private transient Template template;

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (3 lines)
     */
    @Override
    public void init() throws ServletException {
        this.notifiers = this.instantiate(Notifier.class);
        final List<Template> templates = this.instantiate(Template.class);
        if (templates.isEmpty()) {
            this.template = new AlertTemplate("HTML template not configured");
        } else if (templates.size() > 1) {
            this.template = new AlertTemplate("One HTML template expected");
        } else {
            this.template = templates.get(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void service(final HttpServletRequest request,
        final HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        final StringBuilder text = this.text(request);
        for (Notifier notifier : this.notifiers) {
            try {
                notifier.notify(text.toString());
            // @checkstyle IllegalCatch (1 line)
            } catch (Throwable ex) {
                text.append(Logger.format("%[exception]s\n", ex));
                Logger.error(this, "#service(): %[exception]s", ex);
            }
        }
        response.getWriter().print(this.template.render(text.toString()));
        response.getWriter().close();
        Logger.error(this, "#service():\n%s", text);
    }

    /**
     * Instantiate class.
     * @param type The interface to instantiate
     * @return The object
     * @throws ServletException If some defect inside
     * @param <T> Expected type of response
     * @checkstyle RedundantThrows (3 lines)
     */
    private <T> List<T> instantiate(final Class<T> type)
        throws ServletException {
        final String param = this.getInitParameter(type.getName());
        List<T> list;
        if (param == null) {
            list = new ArrayList<T>();
        } else {
            final String[] uris = param.trim().split("\\s+");
            list = new ArrayList<T>(uris.length);
            for (String uri : uris) {
                final Properties props = this.props(uri);
                try {
                    list.add(
                        (T) Class.forName(URI.create(uri).getPath())
                            .getConstructor(Properties.class)
                            .newInstance(props)
                    );
                } catch (ClassNotFoundException ex) {
                    throw new ServletException(ex);
                } catch (NoSuchMethodException ex) {
                    throw new ServletException(ex);
                } catch (InstantiationException ex) {
                    throw new ServletException(ex);
                } catch (IllegalAccessException ex) {
                    throw new ServletException(ex);
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    throw new ServletException(ex);
                }
            }
        }
        return list;
    }

    /**
     * Prepare text.
     * @param request The HTTP request
     * @return Builder of text
     */
    private StringBuilder text(final HttpServletRequest request) {
        final StringBuilder text = new StringBuilder();
        text.append(Logger.format("date: %s\n", new Date()));
        this.append(text, request, "code");
        this.append(text, request, "message");
        this.append(text, request, "exception_type");
        this.append(text, request, "request_uri");
        final ServletContext ctx = this.getServletContext();
        text.append(
            Logger.format(
                "servlet context path: \"%s\"\n",
                request.getContextPath()
            )
        );
        text.append(
            Logger.format(
                "requested: %s (%s) at %s:%d\n",
                request.getRequestURL().toString(),
                request.getMethod(),
                request.getServerName(),
                request.getServerPort()
            )
        );
        text.append(
            Logger.format(
                "remote: %s:%d (%s)\n",
                request.getRemoteAddr(),
                request.getRemotePort(),
                request.getRemoteHost()
            )
        );
        text.append(
            Logger.format(
                "servlet: \"%s\" (API %d.%d) at \"%s\"\n",
                ctx.getServletContextName(),
                ctx.getMajorVersion(),
                ctx.getMinorVersion(),
                ctx.getServerInfo()
            )
        );
        text.append("headers:\n")
            .append(ExceptionTrap.headers(request))
            .append(" \n");
        text.append(
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
        final HttpServletRequest request, final String suffix) {
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
            .append("\n");
    }

    /**
     * Retrieve parameters of URI.
     * @param uri The URI
     * @return Map of all query params
     * @throws ServletException If some defect inside
     * @checkstyle RedundantThrows (3 lines)
     */
    public static Properties props(final String uri) throws ServletException {
        final Properties props = new Properties();
        final String[] parts = uri.split("\\?");
        if (parts.length > 1) {
            final String query = parts[1];
            for (String param : query.split("&")) {
                try {
                    final String[] pair = param.split("=", 2);
                    final String key =
                        URLDecoder.decode(pair[0], CharEncoding.UTF_8);
                    String value;
                    if (pair.length == 1) {
                        value = Boolean.TRUE.toString();
                    } else {
                        value = URLDecoder.decode(pair[1], CharEncoding.UTF_8);
                    }
                    props.setProperty(key, value);
                } catch (java.io.UnsupportedEncodingException ex) {
                    throw new ServletException(ex);
                }
            }
        }
        return props;
    }

    /**
     * Format headers.
     * @param request The HTTP request
     * @return Text with headers
     */
    private static String headers(final HttpServletRequest request) {
        final StringBuilder text = new StringBuilder();
        final Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String header = (String) headerNames.nextElement();
            text.append("  ")
                .append(header)
                .append(": ")
                .append(request.getHeader(header));
            text.append("  \n");
        }
        return text.toString();
    }

}
