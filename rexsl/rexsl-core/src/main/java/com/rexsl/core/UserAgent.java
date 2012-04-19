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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User-agent HTTP header wrapper.
 *
 * <p>This class is instantiated in {@link PageAnalyzer#needsTransformation()}
 * method, using the value of {@code User-Agent} HTTP header. If such a header
 * doesn't exist in the request - {@code NULL} value will be used instead.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @see <a href="http://tools.ietf.org/html/rfc2616#section-14.43">RFC-2616</a>
 */
final class UserAgent {

    /**
     * List of tokens found in {@code User-Agent} HTTP header.
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private final transient Map<String, ProductVersion> tokens =
        new HashMap<String, ProductVersion>();

    /**
     * Public ctor.
     *
     * <p>The class can be instantiated with a text value of {@code User-Agent}
     * HTTP header or {@code NULL}. If {@code NULL} is provided we just ignore
     * it and assume that the header is empty (user agent is not specified).
     * Such a mechanism is required for a unification of user agent
     * manipulations in {@link PageAnalyzer}. That class should have an instance
     * of {@link UserAgent} no matter what. That's why we accept
     * {@code NULL} here.
     *
     * @param text The text of HTTP header or {@code NULL} if such
     *  a header is absent in the HTTP request
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        for (Map.Entry<String, ProductVersion> token : this.tokens.entrySet()) {
            if (text.length() > 0) {
                text.append("; ");
            }
            text.append(token.getKey()).append("-").append(token.getValue());
        }
        return text.toString();
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
            && this.isVersionHigherOrEqual("5");
    }

    /**
     * Check if this is Google Chrome.
     * @return Is it?
     */
    private boolean isChrome() {
        return this.tokens.containsKey("Chrome")
            && this.isVersionHigherOrEqual("10");
    }

    /**
     * Check if the version is higher or equal than this one.
     * @param ver The version
     * @return Returns true if version 'ver' is higher or equal than this object's version
     */
    private boolean isVersionHigherOrEqual(final String ver) {
        final ProductVersion found = this.tokens.get("Version");
        boolean result = false;
        if (found != null) {
            result = found.compareTo(new ProductVersion(ver)) >= 0;
        }
        return result;
    }

}
