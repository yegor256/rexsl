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

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.servlet.ServletContext;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Builds an instance of {@link ServletContext}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.11
 */
public final class ServletContextMocker {

    /**
     * The mock.
     */
    private final transient ServletContext subj =
        Mockito.mock(ServletContext.class);

    /**
     * Map of attributes.
     */
    private final transient ConcurrentMap<String, Object> attributes =
        new ConcurrentSkipListMap<String, Object>();

    /**
     * Public ctor.
     */
    public ServletContextMocker() {
        Mockito.doAnswer(
            new Answer<Void>() {
                @Override
                public Void answer(final InvocationOnMock invocation) {
                    ServletContextMocker.this.attributes.put(
                        invocation.getArguments()[0].toString(),
                        invocation.getArguments()[1]
                    );
                    return null;
                }
            }
        )
            .when(this.subj)
            .setAttribute(Mockito.anyString(), Mockito.anyObject());
        Mockito.doAnswer(
            new Answer<Object>() {
                @Override
                public Object answer(final InvocationOnMock invocation) {
                    return ServletContextMocker.this.attributes.get(
                        invocation.getArguments()[0].toString()
                    );
                }
            }
        ).when(this.subj).getAttribute(Mockito.anyString());
    }

    /**
     * With this attribute.
     * @param name The name of it
     * @param value The value of it
     * @return This object
     */
    public ServletContextMocker withAttribute(final String name,
        final Object value) {
        this.subj.setAttribute(name, value);
        return this;
    }

    /**
     * Build an instance.
     * @return The context
     */
    public ServletContext mock() {
        return this.subj;
    }

}
