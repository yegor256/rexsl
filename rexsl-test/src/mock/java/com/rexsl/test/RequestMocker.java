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
package com.rexsl.test;

import java.net.URI;
import org.mockito.Mockito;

/**
 * Mocker of {@link Request}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class RequestMocker {

    /**
     * Request mocked.
     */
    private final transient Request request = Mockito.mock(Request.class);

    /**
     * Public ctor.
     */
    public RequestMocker() {
        Mockito.doReturn(this.request).when(this.request)
            .header(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(this.request).when(this.request)
            .method(Mockito.anyString());
        Mockito.doReturn(
            new DefaultURI(
                this.request,
                URI.create("http://localhost:8080/test")
            )
        ).when(this.request).uri();
        Mockito.doReturn(
            new DefaultBody(this.request, "test body content")
        ).when(this.request).body();
    }

    /**
     * Mock it.
     * @return Mocked class
     */
    public Request mock() {
        return this.request;
    }

}
