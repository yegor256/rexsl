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

import javax.servlet.ServletContext;
import javax.xml.bind.Marshaller;

/**
 * Configures {@link Marshaller} according to custom login/rules.
 *
 * An instance of this class is used by {@link XslResolver}, if configured.
 * {@link Marshaller} will be configured before returning to JAX-RS. Normally
 * you should not use this class. It is used by <tt>rexsl-maven-plugin</tt> in
 * order to validate compliance of your XML responses with XSD schemas.
 *
 * <p>In order to inform {@link XslResolver} which implementation of this
 * interface to use you should configure <tt>com.rexsl.core.CONFIGURATOR</tt>
 * servlet <tt>init-param</tt> with a name of the implementaiton class.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @see XslResolver#getContext(Class)
 */
public interface JaxbConfigurator {

    /**
     * Initialize it, to be called by
     * {@link XslResolver#setServletContext(ServletContext)}.
     * @param ctx The servlet context
     * @see XslResolver#setServletContext(ServletContext)
     */
    void init(final ServletContext ctx);

    /**
     * Configure marhaller and return a new one (or the same), to be called by
     * {@link XslResolver#getContext(Class)}.
     * @param mrsh The marshaller, already created and ready to marshal
     * @param type The class to be marshalled
     * @return New marshalled to be used instead
     * @see XslResolver#getContext(Class)
     */
    Marshaller marshaller(final Marshaller mrsh, final Class<?> type);

}
