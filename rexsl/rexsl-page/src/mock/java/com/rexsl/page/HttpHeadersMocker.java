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

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Builds an instance of {@link HttpHeaders}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id: HttpHeadersMocker.java 2028 2012-08-22 11:59:49Z guard $
 */
public final class HttpHeadersMocker {

    /**
     * The mock.
     */
    private final transient HttpHeaders subj =
        Mockito.mock(HttpHeaders.class);

    /**
     * Multivalued map of headers.
     */
    private final transient ConcurrentMap<String, List<String>> headers =
        new ConcurrentHashMap<String, List<String>>();

    /**
     * With this header on board.
     * @param name The name of it
     * @param value The value of it
     * @return This object
     */
    public HttpHeadersMocker withHeader(final String name, final String value) {
        this.headers.putIfAbsent(name, new LinkedList<String>());
        this.headers.get(name).add(value);
        return this;
    }

    /**
     * Build an instance of provided class.
     * @return The resource just created
     */
    @SuppressWarnings("unchecked")
    public HttpHeaders mock() {
        final MultivaluedMap<String, String> map =
            Mockito.mock(MultivaluedMap.class);
        Mockito.doReturn(map).when(this.subj).getRequestHeaders();
        Mockito.doAnswer(
            new Answer<List<String>>() {
                public List<String> answer(final InvocationOnMock inv) {
                    final String name = inv.getArguments()[0].toString();
                    return HttpHeadersMocker.this.headers.get(name);
                }
            }
        ).when(this.subj).getRequestHeader(Mockito.anyString());
        Mockito.doAnswer(
            new Answer<String>() {
                public String answer(final InvocationOnMock inv) {
                    final String name = inv.getArguments()[0].toString();
                    String first = null;
                    if (HttpHeadersMocker.this.headers.containsKey(name)) {
                        final Iterator<String> values =
                            HttpHeadersMocker.this.headers.get(name).iterator();
                        if (values.hasNext()) {
                            first = values.next();
                        }
                    }
                    return first;
                }
            }
        ).when(map).getFirst(Mockito.anyString());
        Mockito.doAnswer(
            new Answer<Boolean>() {
                public Boolean answer(final InvocationOnMock inv) {
                    return HttpHeadersMocker.this.headers.containsKey(
                        inv.getArguments()[0].toString()
                    );
                }
            }
        ).when(map).containsKey(Mockito.anyString());
        Mockito.doAnswer(
            new Answer<Set<Map.Entry<String, String>>>() {
                public Set<Map.Entry<String, String>> answer(
                    final InvocationOnMock inv) {
                    final Set<Map.Entry<String, String>> entries =
                        new HashSet<Map.Entry<String, String>>();
                    for (String key : HttpHeadersMocker.this.headers.keySet()) {
                        for (String value
                            : HttpHeadersMocker.this.headers.get(key)) {
                            entries.add(
                                new AbstractMap.SimpleEntry<String, String>(
                                    key, value
                                )
                            );
                        }
                    }
                    return entries;
                }
            }
        ).when(map).entrySet();
        Mockito.doAnswer(
            new Answer<Set<String>>() {
                public Set<String> answer(final InvocationOnMock inv) {
                    return HttpHeadersMocker.this.headers.keySet();
                }
            }
        ).when(map).keySet();
        Mockito.doAnswer(
            new Answer<List<String>>() {
                public List<String> answer(final InvocationOnMock inv) {
                    return HttpHeadersMocker.this.headers.get(
                        inv.getArguments()[0].toString()
                    );
                }
            }
        ).when(map).get(Mockito.anyString());
        return this.subj;
    }

}
