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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * The only and the main servlet from ReXSL framework.
 *
 * <p>You don't need to instantiate this class directly. It is instantiated
 * by servlet container according to configuration from {@code web.xml}.
 * Should be used in {@code web.xml} (together with {@link XsltFilter})
 * like that:
 *
 * <pre> &lt;servlet>
 *  &lt;servlet-name>RestfulServlet&lt;/servlet-name>
 *  &lt;servlet-class>com.rexsl.core.RestfulServlet&lt;/servlet-class>
 *  &lt;init-param>
 *   &lt;param-name>com.rexsl.PACKAGES&lt;/param-name>
 *   &lt;param-value>com.rexsl.foo&lt;/param-value>
 *  &lt;/init-param>
 * &lt;/servlet>
 * &lt;servlet-mapping>
 *  &lt;servlet-name>RestfulServlet&lt;/servlet-name>
 *  &lt;url-pattern>/*&lt;/url-pattern>
 * &lt;/servlet-mapping></pre>
 *
 * <p>{@code com.rexsl.PACKAGES} init parameter should contain comma-separated
 * list of packages where JAX-RS annotated resources are located and should be
 * discovered. If this parameter is not set a runtime exception will be thrown
 * and the servlet won't be initialized. The same will happen if the parameter
 * contains incorrect data. We will consider a package is valid if and only if
 * it abides to the Java package naming conventions.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @see <a href="http://www.rexsl.com">Introduction to ReXSL</a>
 * @see <a href="http://www.oracle.com/technetwork/java/javaee/servlet/index.html">Java Servlet Technology</a>
 * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-6.html#jls-6.1">jls-6.1</a>
 * @see <a href="http://www.oracle.com/technetwork/java/codeconventions-135099.html">Java naming conventions</a>
 * @see <a href="http://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html">Naming a package</a>
 * @since 0.2
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@Loggable(Loggable.DEBUG)
public final class RestfulServlet extends ServletContainer {

    /**
     * Name of servlet init param.
     *
     * <p>This parameter should be used in servlet initialization section of
     * {@code web.xml} in order to tell the servlet in which packages your
     * JAX-RS annotated resources are located. The value of the parameter
     * should contain a comma-separated list of Java package names.
     *
     * @see RestfulServlet
     * @since 0.3.7
     */
    public static final String PACKAGES = "com.rexsl.PACKAGES";

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x7526FA78EED21470L;

    /**
     * Comma, a separator between package names.
     */
    private static final String COMMA = ",";

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public void init(@NotNull final ServletConfig config)
        throws ServletException {
        final Collection<String> packages = new HashSet<String>(0);
        packages.add(this.getClass().getPackage().getName());
        final String param = config.getInitParameter(RestfulServlet.PACKAGES);
        if (param == null) {
            throw new ServletException(
                Logger.format(
                    "'%s' servlet parameter is mandatory",
                    RestfulServlet.PACKAGES
                )
            );
        }
        for (final String name
            : StringUtils.split(param, RestfulServlet.COMMA)) {
            final String pkg = name.trim();
            final Pattern ptrn = Pattern.compile(
                "^([a-z_][a-z0-9_]*(\\.[a-z_][a-z0-9_]*)*)$"
            );
            final Matcher match = ptrn.matcher(pkg);
            if (!match.matches()) {
                throw new ServletException(
                    Logger.format(
                        // @checkstyle LineLength (1 line)
                        "'%s' servlet parameter contains non-valid data: %s",
                        RestfulServlet.PACKAGES,
                        pkg
                    )
                );
            }
            packages.add(pkg);
            Logger.info(
                this,
                "#init(): '%s' package added (%d total)",
                pkg,
                packages.size()
            );
        }
        final Properties props = new Properties();
        props.setProperty(
            PackagesResourceConfig.PROPERTY_PACKAGES,
            StringUtils.join(packages, RestfulServlet.COMMA)
        );
        this.reconfigureJUL();
        this.init(new ServletConfigWrapper(config, props));
    }

    // @checkstyle ThrowsCount (8 lines)
    // @checkstyle RedundantThrows (8 lines)
    @Override
    @Loggable(value = Loggable.DEBUG, ignore = WebApplicationException.class)
    public void service(final HttpServletRequest request,
        final HttpServletResponse response)
        throws ServletException, IOException {
        final long start = System.currentTimeMillis();
        super.service(request, response);
        final long duration = System.currentTimeMillis() - start;
        if (duration > TimeUnit.SECONDS.toMillis(1L)) {
            Logger.warn(
                this,
                "#service(%s %s): %[ms]s is too slow (IP=%s)",
                request.getMethod(),
                request.getRequestURI(),
                duration,
                request.getRemoteAddr()
            );
        }
        response.addHeader("X-Rexsl-Millis", Long.toString(duration));
        response.addHeader(
            "X-Rexsl-Version",
            String.format(
                "%s/r%s",
                Manifests.read("ReXSL-Version"),
                Manifests.read("ReXSL-Build")
            )
        );
    }

    /**
     * Initialize JUL-to-SLF4J bridge.
     * @see #init(ServletConfig)
     */
    private void reconfigureJUL() {
        final LevelChangePropagator listener = new LevelChangePropagator();
        final LoggerContext ctx = LoggerContext.class.cast(
            LoggerFactory.getILoggerFactory()
        );
        ctx.addListener(listener);
        listener.setContext(ctx);
        ctx.start();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.debug(this, "#julToSlf4j(): JUL forwarded to SLF4j");
    }

}
