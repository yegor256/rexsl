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

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.mockito.Mockito;

/**
 * Mocker of {@link Response}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ResponseMocker {

    /**
     * Response mocked.
     */
    private final transient Response response = Mockito.mock(Response.class);

    /**
     * Headers.
     */
    private final transient ConcurrentMap<String, List<String>> headers =
        new ConcurrentHashMap<String, List<String>>();

    /**
     * Public ctor.
     */
    public ResponseMocker() {
        Mockito.doReturn(this.headers).when(this.response).headers();
        Mockito.doReturn(new RequestMocker().mock()).when(this.response).back();
        this.withStatus(HttpURLConnection.HTTP_OK);
        this.withBody("");
    }

    /**
     * Return this status code.
     * @param code The code
     * @return This object
     */
    public ResponseMocker withStatus(final int code) {
        Mockito.doReturn(code).when(this.response).status();
        return this;
    }

    /**
     * Return this header.
     * @param name The name of ti
     * @param value The value
     * @return This object
     */
    public ResponseMocker withHeader(final String name,
        final String value) {
        this.headers.putIfAbsent(name, new ArrayList<String>(0));
        this.headers.get(name).add(value);
        return this;
    }

    /**
     * Return this entity.
     * @param body The entity
     * @return This object
     */
    public ResponseMocker withBody(final String body) {
        Mockito.doReturn(body).when(this.response).body();
        return this;
    }

    /**
     * Return this request.
     * @param request The request
     * @return This object
     */
    public ResponseMocker with(final Request request) {
        Mockito.doReturn(request).when(this.response).back();
        return this;
    }

    /**
     * Mock it.
     * @return Mocked class
     */
    public Response mock() {
        return this.response;
    }

}
