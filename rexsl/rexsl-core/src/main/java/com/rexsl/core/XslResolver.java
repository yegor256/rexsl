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

import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * Replace standard marshaller.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @link <a href="http://markmail.org/search/?q=list%3Anet.java.dev.jersey.users+ContextResolver%3CMarshaller%3E#query:list%3Anet.java.dev.jersey.users%20ContextResolver%3CMarshaller%3E+page:1+mid:q4fkq6eqlgkzdodc+state:results">discussion</a>
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public final class XslResolver implements ContextResolver<Marshaller> {

    /**
     * Marshaller configurator.
     * @see #setServletContext(ServletContext)
     */
    private JaxbConfigurator configurator;

    /**
     * Classes to process.
     */
    private final List<Class> classes = new ArrayList<Class>();

    /**
     * JAXB context.
     */
    private JAXBContext context;

    /**
     * Set servlet context from container.
     * @param ctx The context
     */
    @Context
    public void setServletContext(final ServletContext ctx) {
        final String name = ctx.getInitParameter("com.rexsl.core.CONFIGURATOR");
        if (name != null) {
            Object cfg;
            try {
                cfg = Class.forName(name).newInstance();
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException(
                    String.format("Not found '%s'", name),
                    ex
                );
            } catch (InstantiationException ex) {
                throw new IllegalArgumentException(
                    String.format("Can't instantiate '%s'", name),
                    ex
                );
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(
                    String.format("Can't access '%s'", name),
                    ex
                );
            }
            if (!(cfg instanceof JaxbConfigurator)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Class '%s' is not a child of JaxbConfigurator",
                        name
                    )
                );
            }
            this.configurator = (JaxbConfigurator) cfg;
            this.configurator.init(ctx);
            Logger.debug(
                this,
                "#setServletContext(%s): configurator %s set and initialized",
                ctx.getClass().getName(),
                cfg.getClass().getName()
            );
        }
        Logger.debug(
            this,
            "#setServletContext(%s): context injected by JAX-RS",
            ctx.getClass().getName()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Marshaller getContext(final Class<?> type) {
        Marshaller mrsh;
        try {
            mrsh = this.context(type).createMarshaller();
            mrsh.setProperty(Marshaller.JAXB_FRAGMENT, true);
            final String header = String.format(
                // @checkstyle LineLength (1 line)
                "<?xml version='1.0'?><?xml-stylesheet type='text/xml' href='/xsl/%s.xsl'?>",
                type.getSimpleName()
            );
            mrsh.setProperty("com.sun.xml.bind.xmlHeaders", header);
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalStateException(ex);
        }
        if (this.configurator != null) {
            mrsh = this.configurator.marshaller(mrsh, type);
            Logger.debug(
                this,
                // @checkstyle LineLength (1 line)
                "#getContext(%s): marshaller createa and configured through %s: %s",
                type.getName(),
                this.configurator.getClass().getName(),
                mrsh.getClass().getName()
            );
        } else {
            Logger.debug(
                this,
                "#getContext(%s): marshaller created (no configurator)",
                type.getName()
            );
        }
        return mrsh;
    }

    /**
     * Create and return a context.
     * @param cls The class we should process
     * @return The context
     */
    private JAXBContext context(final Class cls) {
        synchronized (this) {
            if (!this.classes.contains(cls)) {
                try {
                    this.classes.add(cls);
                    this.context = JAXBContext.newInstance(
                        this.classes.toArray(new Class[] {})
                    );
                    Logger.info(
                        this,
                        "#context(%s): added to JAXBContext (%d total)",
                        cls.getName(),
                        this.classes.size()
                    );
                } catch (javax.xml.bind.JAXBException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return this.context;
        }
    }

}
