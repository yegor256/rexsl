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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import org.mockito.Mockito;

/**
 * Mocker of {@link ServletConfig}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class ServletConfigMocker {

    /**
     * The mock.
     */
    private final transient ServletConfig config =
        Mockito.mock(ServletConfig.class);

    /**
     * Params.
     */
    private final transient List<String> params = new ArrayList<String>();

    /**
     * With this parameter.
     * @param name The name of it
     * @param val The value
     * @return This object
     */
    public ServletConfigMocker withParam(final String name, final String val) {
        this.params.add(name);
        Mockito.doReturn(val).when(this.config).getInitParameter(name);
        return this;
    }

    /**
     * With this servlet context.
     * @param ctx The context
     * @return This object
     */
    public ServletConfigMocker withServletContext(final ServletContext ctx) {
        Mockito.doReturn(ctx).when(this.config).getServletContext();
        return this;
    }

    /**
     * Mock it.
     * @return Mocked config
     */
    public ServletConfig mock() {
        Mockito.doReturn(Collections.enumeration(this.params))
            .when(this.config).getInitParameterNames();
        return this.config;
    }

}
