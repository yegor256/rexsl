/**
 * Copyright (c) 2011-2015, ReXSL.com
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
package com.rexsl.mock;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.immutable.ArrayMap;
import com.jcabi.log.Logger;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;

/**
 * Mock of {@link ServletContext}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.3
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "attributes")
public final class MkServletContext implements ServletContext {

    /**
     * Attributes.
     */
    private final transient ArrayMap<String, Object> attributes;

    /**
     * Resources.
     */
    private final transient ArrayMap<String, String> resources;

    /**
     * Ctor.
     */
    public MkServletContext() {
        this(new ArrayMap<String, Object>(), new ArrayMap<String, String>());
    }

    /**
     * Ctor.
     * @param attrs Attributes
     * @param res Resources
     */
    public MkServletContext(final Map<String, Object> attrs,
        final Map<String, String> res) {
        this.attributes = new ArrayMap<String, Object>(attrs);
        this.resources = new ArrayMap<String, String>(res);
    }

    /**
     * With this attribute.
     * @param name Attribute name
     * @param obj Object
     * @return New servlet context
     */
    public MkServletContext withAttr(final String name, final Object obj) {
        return new MkServletContext(
            this.attributes.with(name, obj), this.resources
        );
    }

    /**
     * With this resource.
     * @param name Resource name
     * @param content Content
     * @return New servlet context
     */
    public MkServletContext withResource(final String name,
        final String content) {
        return new MkServletContext(
            this.attributes, this.resources.with(name, content)
        );
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public ServletContext getContext(final String str) {
        return this;
    }

    @Override
    public int getMajorVersion() {
        return Tv.THREE;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(final String str) {
        throw new UnsupportedOperationException("#getMimeType()");
    }

    @Override
    public Set<String> getResourcePaths(final String str) {
        return new HashSet<String>(0);
    }

    @Override
    public URL getResource(final String name) {
        final String content = this.resources.get(name);
        if (content != null) {
            throw new UnsupportedOperationException(
                "#getResource() is not supported, use #getResourceAsStream()"
            );
        }
        return null;
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        final String content = this.resources.get(name);
        InputStream stream = null;
        if (content != null) {
            try {
                stream = new ByteArrayInputStream(
                    this.resources.get(name).getBytes(CharEncoding.UTF_8)
                );
            } catch (final UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return stream;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String str) {
        throw new UnsupportedOperationException("#getRequestDispatcher()");
    }

    @Override
    public RequestDispatcher getNamedDispatcher(final String str) {
        throw new UnsupportedOperationException("#getNamedDispatcher()");
    }

    @Override
    public void log(final String str) {
        Logger.info(this, str);
    }

    @Override
    public void log(final String str, final Throwable throwable) {
        Logger.info(this, "%s %[exception]s", str, throwable);
    }

    @Override
    public String getRealPath(final String str) {
        return str;
    }

    @Override
    public String getServerInfo() {
        return "";
    }

    @Override
    public String getInitParameter(final String str) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(Collections.<String>emptyList());
    }

    @Override
    public Object getAttribute(final String str) {
        return this.attributes.get(str);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    @Override
    public void setAttribute(final String str, final Object obj) {
        Logger.warn(
            this, "#setAttribute('%s', ..) ignored by %s",
            str, this.getClass().getSimpleName()
        );
    }

    @Override
    public void removeAttribute(final String str) {
        Logger.warn(
            this, "#removeAttribute('%s') ignored by %s",
            str, this.getClass().getSimpleName()
        );
    }

    @Override
    public String getServletContextName() {
        return this.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     * @deprecated As of Servlet API
     */
    @Override
    @Deprecated
    public Servlet getServlet(final String str) {
        throw new UnsupportedOperationException("#getServlet()");
    }

    /**
     * {@inheritDoc}
     * @deprecated As of Servlet API
     */
    @Override
    @Deprecated
    public Enumeration<Object> getServlets() {
        throw new UnsupportedOperationException("#getServlets()");
    }

    /**
     * {@inheritDoc}
     * @deprecated As of Servlet API
     */
    @Override
    @Deprecated
    public Enumeration<String> getServletNames() {
        throw new UnsupportedOperationException("#getServletNames()");
    }

    /**
     * {@inheritDoc}
     * @deprecated As of Servlet API
     */
    @Override
    @Deprecated
    public void log(final Exception exc, final String str) {
        throw new UnsupportedOperationException("#log()");
    }

}
