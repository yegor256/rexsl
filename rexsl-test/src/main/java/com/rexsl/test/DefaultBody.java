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

import com.jcabi.aspects.Immutable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;

/**
 * Implementation of {@link RequestBody}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 */
@Immutable
@ToString(of = "text")
@EqualsAndHashCode(of = { "req", "text" })
final class DefaultBody implements RequestBody {

    /**
     * Original request.
     */
    private final transient Request req;

    /**
     * Text.
     */
    private final transient String text;

    /**
     * Public ctor.
     * @param request Original request
     * @param txt Content to encapsulate
     */
    DefaultBody(final Request request, final String txt) {
        this.text = txt;
        this.req = request;
    }

    @Override
    public Request back() {
        return this.req;
    }

    @Override
    @NotNull
    public String get() {
        return this.text;
    }

    @Override
    public RequestBody set(@NotNull(message = "content can't be NULL")
        final String txt) {
        return new DefaultBody(this.req, txt);
    }

    @Override
    public RequestBody formParam(
        @NotNull(message = "form param name can't be NULL") final String name,
        @NotNull(message = "param value can't be NULL") final Object value) {
        try {
            return new DefaultBody(
                this.req,
                new StringBuilder(this.text).append(name).append('=').append(
                    URLEncoder.encode(value.toString(), CharEncoding.UTF_8)
                ).append('&').toString()
            );
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
