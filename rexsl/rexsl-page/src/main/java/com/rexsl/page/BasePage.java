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
package com.rexsl.page;

import com.jcabi.log.Logger;
import com.rexsl.core.XslResolver;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;

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
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.7
 * @see PageBuilder
 * @see Resource
 * @see BaseResource
 */
@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
@SuppressWarnings("unchecked")
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
    public final T init(@NotNull final R res) {
        synchronized (this.links) {
            this.resource = res;
        }
        this.link(new Link("self", "./"));
        this.link(new Link("home", "/"));
        return (T) this;
    }

    /**
     * Append new element.
     * @param element The element to append
     * @return This object
     */
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
    public final T append(@NotNull final JaxbBundle bundle) {
        this.append(bundle.element());
        return (T) this;
    }

    /**
     * Get home.
     * @return The home resource
     */
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
    public final Collection<Object> getElements() {
        return this.elements;
    }

    /**
     * List of links.
     * @return List of links.
     */
    @XmlElement(name = "link")
    @XmlElementWrapper(name = "links")
    public final Collection<Link> getLinks() {
        return this.links;
    }

    /**
     * Get IP address of the server.
     * @return The IP address
     */
    @XmlAttribute
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
    public final Date getDate() {
        return new Date();
    }

    /**
     * Get SSL or non-SSL flag.
     * @return The flag
     */
    @XmlAttribute
    public final boolean isSsl() {
        return "https".equals(this.home().uriInfo().getBaseUri().getScheme());
    }

    /**
     * Get page generation time, in milliseconds.
     * @return Page generation time
     */
    @XmlElement
    public final long getMillis() {
        return System.currentTimeMillis() - this.home().started();
    }

}
