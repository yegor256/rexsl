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

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * HATEOAS link.
 *
 * <p>This is how it is supposed to be used, for example:
 *
 * <pre>
 * &#64;Path("/alpha")
 * public class MainRs {
 *   &#64;GET
 *   &#64;Produces(MediaTypes.APPLICATION_XML)
 *   public BasePage front() {
 *     return new PageBuilder()
 *       .stylesheet("/xsl/front.xsl")
 *       .build(BasePage.class)
 *       .link(new Link("search", "./s"))
 *       .link(new Link("start", "/start"));
 *   }
 * }
 * </pre>
 *
 * <p>That is how an XML will look like (if the site is deployed to
 * {@code http://example.com/foo/}):
 *
 * <pre>
 * &lt;?xml version="1.0" ?&gt;
 * &lt;?xml-stylesheet type='text/xsl' href='/xsl/front.xsl'?&gt;
 * &lt;page&gt;
 *   &lt;links&gt;
 *     &lt;link rel="search" href="http://example.com/foo/alpha/s"
 *       type="application/xml"/&gt;
 *     &lt;link rel="start" href="http://example.com/foo/start"
 *       type="application/xml"/&gt;
 *   &lt;/links&gt;
 * &lt;/page&gt;
 * </pre>
 *
 * <p>Sometimes it's necessary to add more information to the link, besides the
 * the mandatory {@code rel}, {@code href}, and {@code type} attributes. That's
 * how you can specify more:
 *
 * <pre>
 * return new PageBuilder()
 *   .build(BasePage.class)
 *   .link(new Link("search", "./s").with(new JaxbBundle("name", "John Doe")))
 * </pre>
 *
 * <p>The result XML will look like:
 *
 * <pre>
 * &lt;?xml version="1.0" ?&gt;
 * &lt;?xml-stylesheet type='text/xsl' href='/xsl/front.xsl'?&gt;
 * &lt;page&gt;
 *   &lt;links&gt;
 *     &lt;link rel="search" href="http://example.com/foo/alpha/s"
 *       type="application/xml"/&gt;
 *       &lt;name&gt;John Doe&lt;/name&gt;
 *     &lt;/link&gt;
 *   &lt;/links&gt;
 * &lt;/page&gt;
 * </pre>
 *
 * <p>URI provided as a second parameter of any constructor of this class
 * may be absolute or relative. It is relative if it starts with a slash
 * ({@code "/"}) or a dot ({@link "."}). URIs started with a slash are related
 * to the absolute path of the deployed application. URIs started with a dot
 * are related to the path of the currently rendered page. To get the
 * information about current context {@link #attachTo(Resource)} method
 * is used. It is being called by {@link BasePage#init(Resource)}.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
@XmlRootElement(name = "link")
@XmlAccessorType(XmlAccessType.NONE)
public final class Link {

    /**
     * Name of {@code rel} attribute.
     */
    @NotNull
    private final transient String rel;

    /**
     * The type of resource there.
     */
    @NotNull
    private final transient String type;

    /**
     * Optional sub-elements.
     */
    private final transient List<Object> elements =
        new CopyOnWriteArrayList<Object>();

    /**
     * Content of {@code href} attribute, with URI.
     */
    @NotNull
    private transient String href;

    /**
     * Public ctor for JAXB (always throws a runtime exception).
     *
     * <p>You're not supposed to use this ctor (it is here just for compliance
     * with JAXB implementations). Use other ctors instead.
     */
    public Link() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Public ctor.
     * @param rname The "rel" of it
     * @param link The href
     */
    public Link(@NotNull final String rname, @NotNull final String link) {
        this(rname, UriBuilder.fromPath(link).build());
    }

    /**
     * Public ctor.
     * @param rname The "rel" of it
     * @param uri The href
     */
    public Link(@NotNull final String rname, @NotNull final URI uri) {
        this(rname, uri, MediaType.TEXT_XML);
    }

    /**
     * Public ctor.
     * @param rname The "rel" of it
     * @param builder URI builder
     */
    public Link(@NotNull final String rname,
        @NotNull final UriBuilder builder) {
        this(rname, builder.build(), MediaType.TEXT_XML);
    }

    /**
     * Public ctor.
     * @param rname The "rel" of it
     * @param uri The href
     * @param tpe Media type of destination
     */
    public Link(@NotNull @Pattern(regexp = ".+") final String rname,
        @NotNull final URI uri,
        @NotNull @Pattern(regexp = ".*") final String tpe) {
        this.rel = rname;
        this.href = uri.toString();
        this.type = tpe;
    }

    /**
     * REL attribute of the link.
     * @return The name
     */
    @XmlAttribute
    public String getRel() {
        return this.rel;
    }

    /**
     * HREF attribute of the link.
     * @return The url
     */
    @XmlAttribute
    public String getHref() {
        return this.href;
    }

    /**
     * Type of destination resource.
     * @return The type
     */
    @XmlAttribute
    public String getType() {
        return this.type;
    }

    /**
     * Get all elements.
     * @return Full list of injected elements
     */
    @XmlAnyElement(lax = true)
    @XmlMixed
    public List<Object> getElements() {
        return this.elements;
    }

    /**
     * Add new sub-element.
     * @param element The sub-element to add
     * @return This object
     */
    public Link with(@NotNull final Object element) {
        this.elements.add(element);
        return this;
    }

    /**
     * Add new JAXB bundle.
     * @param bundle The bundle to add
     * @return This object
     */
    public Link with(@NotNull final JaxbBundle bundle) {
        this.with(bundle.element());
        return this;
    }

    /**
     * Set HREF attribute of the link.
     * @param uri The value of it
     */
    public void setHref(@NotNull final String uri) {
        synchronized (this.elements) {
            this.href = uri;
        }
    }

    /**
     * Attach to this resource and make {@code HREF} attribute
     * absolute, using the URI information of the resource.
     * @param res The resource to attach to
     */
    public void attachTo(@NotNull @Valid final Resource res) {
        synchronized (this.elements) {
            if (this.href.charAt(0) == '.') {
                this.href = res.uriInfo().getRequestUriBuilder()
                    .clone()
                    .path(this.href.substring(1))
                    .build()
                    .toString();
            } else if (this.href.charAt(0) == '/') {
                this.href = res.uriInfo().getBaseUriBuilder()
                    .clone()
                    .path(this.href)
                    .build()
                    .toString();
            }
        }
    }

}
