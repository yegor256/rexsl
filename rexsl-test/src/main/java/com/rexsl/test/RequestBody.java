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
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.CharUtils;

/**
 * Request body.
 *
 * <p>Instances of this interface are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 */
@Immutable
public interface RequestBody {

    /**
     * Get back to the request it's related to.
     * @return The request we're in
     */
    @NotNull(message = "request is never NULL")
    Request back();

    /**
     * Get text content.
     * @return Content in UTF-8
     */
    @NotNull(message = "body can't be NULL")
    String get();

    /**
     * Set text content.
     * @param body Body content
     * @return New alternated body
     */
    @NotNull(message = "body is never NULL")
    RequestBody set(@NotNull(message = "body can't be NULL") String body);

    /**
     * Set byte array content.
     * @param body Body content
     * @return New alternated body
     */
    @NotNull(message = "body is never NULL")
    RequestBody set(@NotNull(message = "body can't be NULL") byte[] body);

    /**
     * Add form param.
     * @param name Query param name
     * @param value Value of the query param to set
     * @return New alternated body
     */
    @NotNull(message = "body is never NULL")
    RequestBody formParam(
        @NotNull(message = "form param name can't be NULL") String name,
        @NotNull(message = "form param value can't be NULL") Object value);

    /**
     * Add form params.
     * @param params Map of params
     * @return New alternated body
     * @since 0.10
     */
    @NotNull(message = "alternated body is never NULL")
    RequestBody formParams(
        @NotNull(message = "map of params can't be NULL")
        Map<String, String> params);

    /**
     * Printer of byte array.
     */
    @Immutable
    final class Printable {
        /**
         * Utility class.
         */
        private Printable() {
            // intentionally empty
        }
        /**
         * Safely print byte array.
         * @param bytes Bytes to print
         * @return Text, with ASCII symbols only
         */
        public static String toString(final byte[] bytes) {
            final StringBuilder text = new StringBuilder(0);
            if (bytes.length > 0) {
                for (final byte chr : bytes) {
                    if (CharUtils.isAscii((char) chr)) {
                        text.append((char) chr);
                    } else {
                        text.append(String.format("\\u%04x", chr));
                    }
                }
            } else {
                text.append("<<empty>>");
            }
            return text.toString();
        }
    }

}
