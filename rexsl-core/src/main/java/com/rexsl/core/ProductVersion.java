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
import com.jcabi.log.Logger;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;

/**
 * Product version.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @see <a href="http://tools.ietf.org/html/rfc2616#section-3.8">RFC-2616</a>
 */
@EqualsAndHashCode(of = "origin")
@Immutable
final class ProductVersion implements Comparable<ProductVersion> {

    /**
     * Pattern to split numbers.
     */
    private static final Pattern SEPARATOR =
        Pattern.compile(".", Pattern.LITERAL);

    /**
     * Original version.
     */
    private final transient String origin;

    /**
     * Public ctor.
     * @param text The text of it
     */
    ProductVersion(final String text) {
        this.origin = text;
    }

    @Override
    public int compareTo(final ProductVersion ver) {
        return this.normalized().compareTo(ver.normalized());
    }

    @Override
    public String toString() {
        return this.origin;
    }

    /**
     * Get normalize version.
     * @return Normalized text
     * @see <a href="http://stackoverflow.com/questions/198431">prototype</a>
     */
    private String normalized() {
        final String[] parts = ProductVersion.SEPARATOR.split(this.origin);
        final StringBuilder bldr = new StringBuilder(0);
        for (final String part : parts) {
            bldr.append(Logger.format("%4s.", part));
        }
        return bldr.toString();
    }

}
