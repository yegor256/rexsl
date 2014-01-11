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
import javax.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Immutable HTTP header.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Immutable
@ToString
@EqualsAndHashCode
final class Header implements Map.Entry<String, String> {

    /**
     * Key.
     */
    private final transient String left;

    /**
     * Value.
     */
    private final transient String right;

    /**
     * Public ctor.
     * @param key The name of it
     * @param value The value
     */
    Header(final String key, final String value) {
        this.left = Header.normalized(key);
        this.right = value;
    }

    @Override
    public String getKey() {
        return this.left;
    }

    @Override
    public String getValue() {
        return this.right;
    }

    @Override
    public String setValue(final String value) {
        throw new UnsupportedOperationException("#setValue()");
    }

    /**
     * Normalize key.
     * @param key The key to normalize
     * @return Normalized key
     */
    @NotNull
    private static String normalized(
        @NotNull(message = "key can't be NULL")
        @Pattern(regexp = "[a-zA-Z0-9\\-]+") final String key) {
        final char[] chars = key.toCharArray();
        chars[0] = Header.upper(chars[0]);
        for (int pos = 1; pos < chars.length; ++pos) {
            if (chars[pos - 1] == '-') {
                chars[pos] = Header.upper(chars[pos]);
            }
        }
        return new String(chars);
    }

    /**
     * Convert char to upper case, if required.
     * @param chr The char to convert
     * @return Upper-case char
     */
    private static char upper(final char chr) {
        final char upper;
        if (chr >= 'a' && chr <= 'z') {
            upper = (char) (chr - ('a' - 'A'));
        } else {
            upper = chr;
        }
        return upper;
    }

}
