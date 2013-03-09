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

import javax.ws.rs.core.UriBuilder;
import org.mockito.Mockito;

/**
 * Mocker of {@link TestClient}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TestClientMocker {

    /**
     * Mock.
     */
    private final transient TestClient client = Mockito.mock(TestClient.class);

    /**
     * Public ctor.
     */
    public TestClientMocker() {
        this.withUri("http://localhost");
        this.withResponse(new TestResponseMocker().mock());
    }

    /**
     * Return this uri.
     * @param uri The uri
     * @return This object
     */
    public TestClientMocker withUri(final String uri) {
        Mockito.doReturn(UriBuilder.fromUri(uri).build())
            .when(this.client).uri();
        return this;
    }

    /**
     * Return this response.
     * @param resp The response
     * @return This object
     */
    public TestClientMocker withResponse(final TestResponse resp) {
        Mockito.doReturn(resp).when(this.client).get(Mockito.anyString());
        Mockito.doReturn(resp).when(this.client)
            .post(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(resp).when(this.client)
            .put(Mockito.anyString(), Mockito.anyString());
        return this;
    }

    /**
     * Mock it.
     * @return Mocked class
     */
    public TestClient mock() {
        return this.client;
    }

}
