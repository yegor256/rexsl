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
package com.rexsl.maven.utils;

import com.rexsl.maven.Environment;
import groovy.lang.Binding;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Builder of binding for {@link GroovyExecutor}.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class BindingBuilder {

    /**
     * Properties.
     */
    private final transient ConcurrentMap<String, Object> props =
        new ConcurrentHashMap<String, Object>();

    /**
     * Public ctor.
     * @param env The environment
     */
    public BindingBuilder(final Environment env) {
        URI home;
        try {
            home = new URI(String.format("http://localhost:%d/", env.port()));
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        this.props.put("home", home);
        this.props.put("basedir", env.basedir());
        this.props.put("webdir", env.webdir());
        this.props.put("port", env.port());
    }

    /**
     * Build a new {@link Binding}.
     * @return The binding
     */
    public Binding build() {
        final Binding binding = new Binding();
        binding.setVariable("rexsl", this.props);
        return binding;
    }

    /**
     * Add a new property there.
     * @param name Name of the property
     * @param value The value
     * @return This object
     */
    public BindingBuilder add(final String name, final Object value) {
        this.props.put(name, value);
        return this;
    }

}
