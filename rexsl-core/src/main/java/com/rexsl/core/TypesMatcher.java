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
package com.rexsl.core;

import com.jcabi.aspects.Immutable;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Media types matcher.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">RFC2616 Sec.14</a>
 */
@ToString
@EqualsAndHashCode(of = "types")
@Immutable
final class TypesMatcher {

    /**
     * Separator between type and subtype.
     */
    private static final String SEPARATOR = "/";

    /**
     * Asterix sign.
     */
    private static final String ASTERISK = "*";

    /**
     * Media types.
     */
    private final transient String[] types;

    /**
     * Public ctor.
     * @param header The text of HTTP "Accept" header
     */
    TypesMatcher(final String header) {
        final Set<String> set = new TreeSet<String>();
        if (header != null) {
            for (final String range : header.trim().split(",")) {
                final String[] parts = range.trim().split(";", 2);
                set.add(parts[0]);
            }
        }
        this.types = set.toArray(new String[set.size()]);
    }

    /**
     * Check if this MIME type is the only one there.
     * @param type The type to check
     * @return If this MIME type is the only one in the header
     */
    public boolean explicit(final String type) {
        return Arrays.binarySearch(this.types, type) >= 0
            && this.types.length == 1;
    }

    /**
     * Check if this MIME type is accepted.
     * @param match The type to check
     * @return If the MIME type is accepted
     */
    public boolean accepts(final String match) {
        boolean accepts = Arrays.binarySearch(this.types, match) >= 0;
        if (!accepts) {
            final String[] reqs = match.split(TypesMatcher.SEPARATOR, 2);
            for (final String type : this.types) {
                final String[] parts = type.split(TypesMatcher.SEPARATOR, 2);
                if (!TypesMatcher.ASTERISK.equals(parts[0])
                    && !parts[0].equals(reqs[0])) {
                    continue;
                }
                if (!TypesMatcher.ASTERISK.equals(parts[1])
                    && !parts[1].equals(reqs[1])) {
                    continue;
                }
                accepts = true;
                break;
            }
        }
        return accepts;
    }

}
