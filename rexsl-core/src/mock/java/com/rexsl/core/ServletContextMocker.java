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

import java.io.InputStream;
import javax.servlet.ServletContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.mockito.Mockito;

/**
 * Mocker of {@link ServletContext}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class ServletContextMocker {

    /**
     * The mock.
     */
    private final transient ServletContext context =
        Mockito.mock(ServletContext.class);

    /**
     * With this resource on board.
     * @param name Name of it
     * @param content The content of it
     * @return This object
     */
    public ServletContextMocker withResource(
        final String name, final String content
    ) {
        InputStream stream;
        try {
            stream = IOUtils.toInputStream(content, CharEncoding.UTF_8);
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        Mockito.doReturn(stream).when(this.context).getResourceAsStream(name);
        return this;
    }

    /**
     * This resource is absent, and returns NULL.
     * @param name Name of it
     * @return This object
     */
    public ServletContextMocker withoutResource(final String name) {
        Mockito.doReturn(null).when(this.context).getResourceAsStream(name);
        return this;
    }

    /**
     * Mock it.
     * @return Mocked servlet context
     */
    public ServletContext mock() {
        return this.context;
    }

}
