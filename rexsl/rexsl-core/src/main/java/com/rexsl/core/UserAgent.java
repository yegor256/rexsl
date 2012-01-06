/**
 * Copyright (c) 2011, ReXSL.com
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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User-agent HTTP header wrapper.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @see <a href="http://tools.ietf.org/html/rfc2616#section-14.43">RFC-2616</a>
 */
final class UserAgent {

    /**
     * List of tokens found.
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private final transient Map<String, ProductVersion> tokens =
        new HashMap<String, ProductVersion>();

    /**
     * Public ctor.
     * @param text The text of HTTP header
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public UserAgent(final String text) {
        if (text != null) {
            final Pattern ptrn = Pattern.compile("(\\w+/[^ ]+|\\(.*?\\))");
            final Matcher matcher = ptrn.matcher(text);
            while (matcher.find()) {
                final String group = matcher.group();
                if (group.charAt(0) != '(') {
                    final String[] parts = group.split("/", 2);
                    this.tokens.put(parts[0], new ProductVersion(parts[1]));
                }
            }
        }
    }

    /**
     * Check if this agent supports XSLT.
     * @return Does it support XSLT 2.0?
     */
    public boolean isXsltCapable() {
        return this.isSafari() || this.isChrome();
    }

    /**
     * Check if this is Safari.
     * @return Is it?
     */
    private boolean isSafari() {
        return this.tokens.containsKey("Safari")
            && this.isVersion("5");
    }

    /**
     * Check if this is Safari.
     * @return Is it?
     */
    private boolean isChrome() {
        return this.tokens.containsKey("Chrome")
            && this.isVersion("10");
    }

    /**
     * Check if the version is higher than this one.
     * @param ver The version
     * @return Is it?
     */
    private boolean isVersion(final String ver) {
        final ProductVersion found = this.tokens.get("Version");
        boolean result = false;
        if (found != null) {
            result = found.compareTo(new ProductVersion(ver)) >= 0;
        }
        return result;
    }

}
