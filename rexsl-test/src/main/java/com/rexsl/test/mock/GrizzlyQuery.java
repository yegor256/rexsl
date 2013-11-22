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
package com.rexsl.test.mock;

import com.jcabi.aspects.Immutable;
import com.jcabi.immutable.ArrayMap;
import com.rexsl.test.ImmutableHeader;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

/**
 * Mock HTTP query/request.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.10
 */
@Immutable
final class GrizzlyQuery implements MkQuery {

    private final transient String mtd;
    private final transient byte[] content;
    private final transient String home;
    private final transient ArrayMap<String, List<String>> hdrs;

    /**
     * Ctor.
     * @param request Grizzly request
     */
    GrizzlyQuery(final GrizzlyRequest request) throws IOException {
        request.setCharacterEncoding(CharEncoding.UTF_8);
        this.home = request.getDecodedRequestURI();
        this.mtd = request.getMethod();
        final ConcurrentMap<String, List<String>> headers =
            new ConcurrentHashMap<String, List<String>>(0);
        final Enumeration<?> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            final String name = ImmutableHeader.normalize(
                names.nextElement().toString()
            );
            final List<String> list = new LinkedList<String>();
            final Enumeration<?> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                list.add(values.nextElement().toString());
            }
            headers.put(name, list);
        }
        this.hdrs = new ArrayMap<String, List<String>>(headers);
        this.content = IOUtils.toByteArray(request.getInputStream());
    }

    @Override
    public URI uri() {
        return URI.create(this.home);
    }

    @Override
    public String method() {
        return this.mtd;
    }

    @Override
    public Map<String, List<String>> headers() {
        return this.hdrs;
    }

    @Override
    public String body() {
        return new String(this.content, Charsets.UTF_8);
    }
}
