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
package com.rexsl.core;

import com.jcabi.log.Logger;
import java.util.regex.Pattern;

/**
 * Product version.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @see <a href="http://tools.ietf.org/html/rfc2616#section-3.8">RFC-2616</a>
 */
final class ProductVersion implements Comparable<ProductVersion> {

    /**
     * Text presentation of it.
     */
    private final transient String normalized;

    /**
     * Public ctor.
     * @param text The text of it
     * @see <a href="http://stackoverflow.com/questions/198431">prototype</a>
     */
    public ProductVersion(final String text) {
        final String[] parts = Pattern
            .compile(".", Pattern.LITERAL)
            .split(text);
        final StringBuilder bldr = new StringBuilder();
        for (String part : parts) {
            bldr.append(Logger.format("%4s.", part));
        }
        this.normalized = bldr.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final ProductVersion ver) {
        return this.normalized.compareTo(ver.normalized);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.normalized.replace(" ", "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.normalized.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        boolean equals;
        if (this == obj) {
            equals = true;
        } else if (obj instanceof ProductVersion) {
            final ProductVersion ver = ProductVersion.class.cast(obj);
            equals = ver.normalized().equals(this.normalized);
        } else {
            equals = false;
        }
        return equals;
    }

}
