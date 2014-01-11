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
package com.rexsl.test.request;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import com.rexsl.test.Request;
import com.rexsl.test.RequestBody;
import com.rexsl.test.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Default implementation of {@link com.rexsl.test.Response}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Immutable
@EqualsAndHashCode(of = "req")
@Loggable(Loggable.DEBUG)
final class DefaultResponse implements Response {

    /**
     * UTF-8 error marker.
     */
    private static final String ERR = "\uFFFD";

    /**
     * Request.
     */
    private final transient Request req;

    /**
     * Status code.
     */
    private final transient int code;

    /**
     * Reason phrase.
     */
    private final transient String phrase;

    /**
     * Headers.
     */
    private final transient Array<Map.Entry<String, String>> hdrs;

    /**
     * Content received.
     */
    private final transient byte[] content;

    /**
     * Public ctor.
     * @param request The request
     * @param status HTTP status
     * @param reason HTTP reason phrase
     * @param headers HTTP headers
     * @param body Body of HTTP response
     * @checkstyle ParameterNumber (5 lines)
     */
    DefaultResponse(final Request request, final int status,
        final String reason, final Array<Map.Entry<String, String>> headers,
        final byte[] body) {
        this.req = request;
        this.code = status;
        this.phrase = reason;
        this.hdrs = headers;
        this.content = ArrayUtils.clone(body);
    }

    @Override
    @NotNull
    public Request back() {
        return this.req;
    }

    @Override
    public int status() {
        return this.code;
    }

    @Override
    public String reason() {
        return this.phrase;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Map<String, List<String>> headers() {
        final ConcurrentMap<String, List<String>> map =
            new ConcurrentHashMap<String, List<String>>();
        for (final Map.Entry<String, String> header : this.hdrs) {
            map.putIfAbsent(header.getKey(), new LinkedList<String>());
            map.get(header.getKey()).add(header.getValue());
        }
        return map;
    }

    @Override
    public String body() {
        final String body = new String(this.content, Charsets.UTF_8);
        if (body.contains(DefaultResponse.ERR)) {
            throw new IllegalStateException(
                Logger.format(
                    "broken Unicode text at line #%d in '%[text]s' (%d bytes)",
                    StringUtils.countMatches(
                        "\n",
                        body.substring(0, body.indexOf(DefaultResponse.ERR))
                    ) + 2,
                    body,
                    this.content.length
                )
            );
        }
        return body;
    }

    @Override
    public byte[] binary() {
        return ArrayUtils.clone(this.content);
    }
    /**
     * {@inheritDoc}
     * @checkstyle MethodName (4 lines)
     */
    @Override
    @SuppressWarnings("PMD.ShortMethodName")
    public <T> T as(final Class<T> type) {
        try {
            return type.getDeclaredConstructor(Response.class)
                .newInstance(this);
        } catch (InstantiationException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder(0)
            .append(this.code).append(' ')
            .append(this.phrase)
            .append(" [")
            .append(this.back().uri().get())
            .append("]\n");
        for (final Map.Entry<String, String> header : this.hdrs) {
            text.append(
                Logger.format(
                    "%s: %s\n",
                    header.getKey(),
                    header.getValue()
                )
            );
        }
        return text.append('\n')
            .append(RequestBody.Printable.toString(this.content))
            .toString();
    }

}
