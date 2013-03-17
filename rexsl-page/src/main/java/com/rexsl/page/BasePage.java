/**
 * Copyright (c) 2011-2013, ReXSL.com
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
package com.rexsl.page;

import com.jcabi.log.Logger;
import com.rexsl.core.XslResolver;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Base page.
 *
 * <p>Use it as a base class for your own page, for example:
 *
 * <pre> public class MyBasePage extends BasePage&lt;MyPage&gt; {
 *   &#64;XmlAnyElement(lax = true)
 *   &#64;XmlElement
 *   public String getUser() {
 *     return "John Doe";
 *   }
 * }</pre>
 *
 * <p>However, it is recommended to use {@link #append(Object)} and
 * {@link JaxbGroup}/{@link JaxbBundle} instead of defining own methods
 * annotated with {@code @XmlElement}.
 *
 * <p>Don't forget to call {@link #init(Resource)} right after the page is
 * built by {@link PageBuilder}, for example:
 *
 * <pre> &#64;Path("/")
 * public class MainRs extends BaseResource {
 *   &#64;GET
 *   &#64;Produces(MediaTypes.APPLICATION_XML)
 *   public BasePage front() {
 *     return new PageBuilder()
 *       .stylesheet("/xsl/front.xsl")
 *       .build(BasePage.class)
 *       .init(this);
 *   }
 * }</pre>
 *
 * <p>JAX-RS resource classes should implement {@link Resource} or even
 * extend {@link BaseResource}, which is preferred.
 *
 * <p>This class adds {@code date} and {@code ip} attributes to the page, and
 * {@code links} and {@code millis} element. Thus, an empty page (if you don't
 * {@link #append(Object)} anything to it) will look like:
 *
 * <pre> &lt;?xml version="1.0" ?&gt;
 * &lt;page date="2012-04-15T07:07Z" ip="127.0.0.1"&gt;
 *   &lt;links /&gt;
 *   &lt;millis&gt;234&lt;/millis&gt;
 * &lt;/page&gt;</pre>
 *
 * <p>This functionality is not changeable. If this is not what you need in
 * your page - just don't use this class and create your own. However, we
 * believe that the majority of web applications need this information in
 * their XML pages.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3.7
 * @see PageBuilder
 * @see Resource
 * @see BaseResource
 */
@XmlType(name = "com.rexsl.page.BasePage")
@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
@SuppressWarnings("unchecked")
@ToString
@EqualsAndHashCode(callSuper = false, of = "resource")
public class BasePage<T extends BasePage<?, ?>, R extends Resource> {

    /**
     * The resource.
     */
    private transient R resource;

    /**
     * Collection of links.
     */
    private final transient Collection<Link> links =
        new CopyOnWriteArrayList<Link>();

    /**
     * Collection of elements.
     */
    private final transient Collection<Object> elements =
        new CopyOnWriteArrayList<Object>();

    /**
     * Init it with Resource.
     * @param res The resource
     * @return This object
     */
    @NotNull
    public final T init(@NotNull final R res) {
        synchronized (this.links) {
            this.resource = res;
        }
        return (T) this;
    }

    /**
     * Render it.
     * @return JAX-RS response
     */
    @NotNull
    public final Response.ResponseBuilder render() {
        final Response.ResponseBuilder builder = Response.ok();
        for (Class<? extends Inset> type
            : BasePage.defaults(this.resource.getClass())) {
            this.inset(type).render(this, builder);
        }
        for (Method method : this.resource.getClass().getMethods()) {
            if (method.isAnnotationPresent(Inset.Runtime.class)) {
                this.inset(method).render(this, builder);
            }
        }
        builder.entity(this);
        return builder;
    }

    /**
     * Append new element.
     * @param element The element to append
     * @return This object
     */
    @NotNull
    public final T append(@NotNull final Object element) {
        this.elements.add(element);
        if (!(element instanceof org.w3c.dom.Element)) {
            final XslResolver resolver = (XslResolver) this.home()
                .providers()
                .getContextResolver(
                    Marshaller.class,
                    MediaType.APPLICATION_XML_TYPE
                );
            resolver.add(element.getClass());
        }
        return (T) this;
    }

    /**
     * Add new element.
     * @param bundle The element
     * @return This object
     */
    @NotNull
    public final T append(@NotNull final JaxbBundle bundle) {
        this.append(bundle.element());
        return (T) this;
    }

    /**
     * Get home.
     * @return The home resource
     */
    @NotNull
    public final R home() {
        if (this.resource == null) {
            throw new IllegalStateException("call BasePage#init() first");
        }
        return this.resource;
    }

    /**
     * Add new HATEOAS link.
     * @param link The link to add
     * @return This object
     */
    @NotNull
    public final T link(@NotNull final Link link) {
        link.attachTo(this.home());
        this.links.add(link);
        return (T) this;
    }

    /**
     * Get all elements.
     * @return Full list of injected elements
     */
    @XmlAnyElement(lax = true)
    @XmlMixed
    @NotNull
    public final Collection<Object> getElements() {
        return Collections.unmodifiableCollection(this.elements);
    }

    /**
     * List of links.
     * @return List of links.
     */
    @XmlElement(name = "link")
    @XmlElementWrapper(name = "links")
    @NotNull
    public final Collection<Link> getLinks() {
        return Collections.unmodifiableCollection(this.links);
    }

    /**
     * Get IP address of the server.
     * @return The IP address
     */
    @XmlAttribute
    @NotNull
    public final String getIp() {
        String addr;
        try {
            addr = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (java.net.UnknownHostException ex) {
            Logger.error(this, "#getIp(): %[exception]s", ex);
            addr = "";
        }
        return addr;
    }

    /**
     * Get date and time when this page is generated.
     * @return The date
     */
    @XmlAttribute
    @NotNull
    public final Date getDate() {
        return new Date();
    }

    /**
     * Get SSL or non-SSL flag.
     * @return The flag
     */
    @XmlAttribute
    public final boolean isSsl() {
        return this.home().securityContext().isSecure();
    }

    /**
     * Get page generation time, in milliseconds.
     * @return Page generation time
     */
    @XmlElement
    public final long getMillis() {
        return System.currentTimeMillis() - this.home().started();
    }

    /**
     * Get all specified (by annotations) default insets.
     * @param type The type to fetch them from or NULL
     * @return List of them
     */
    private static Set<Class<? extends Inset>> defaults(
        final Class<?> type) {
        Set<Class<? extends Inset>> insets;
        if (type != null && type.isAnnotationPresent(Inset.Default.class)) {
            insets = new HashSet<Class<? extends Inset>>(
                Arrays.asList(
                    type.getAnnotation(Inset.Default.class).value()
                )
            );
            insets.addAll(BasePage.defaults(type.getSuperclass()));
            for (Class<?> iface : type.getInterfaces()) {
                insets.addAll(BasePage.defaults(iface));
            }
        } else {
            insets = new HashSet<Class<? extends Inset>>();
        }
        return insets;
    }

    /**
     * Instantiate inset.
     * @param type Type of inset
     * @return Instance of it
     */
    private Inset inset(final Class<? extends Inset> type) {
        try {
            return type.getConstructor(Resource.class)
                .newInstance(this.resource);
        } catch (InstantiationException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        } catch (SecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Instantiate inset from a method.
     * @param method The method
     * @return Instance of it
     */
    private Inset inset(final Method method) {
        try {
            return Inset.class.cast(
                method.invoke(this.resource, new Object[] {})
            );
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
