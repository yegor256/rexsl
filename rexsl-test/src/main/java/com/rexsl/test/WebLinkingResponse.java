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
import com.jcabi.immutable.ArrayMap;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * Web Linking response.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.9
 * @see <a href="http://tools.ietf.org/html/rfc5988">RFC 5988</a>
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("PMD.TooManyMethods")
public final class WebLinkingResponse extends AbstractResponse {

    /**
     * ImmutableHeader name.
     */
    private static final String HEADER = "Link";

    /**
     * Param name.
     */
    private static final String REL = "rel";

    /**
     * Public ctor.
     * @param resp Response
     */
    public WebLinkingResponse(
        @NotNull(message = "response can't be NULL") final Response resp) {
        super(resp);
    }

    /**
     * Follow link by REL.
     * @param rel Relation name
     * @return The same object
     * @throws IOException If fails
     */
    @NotNull(message = "response is never NULL")
    public Request follow(@NotNull(message = "rel can't be NULL")
        final String rel) throws IOException {
        final WebLinkingResponse.Link link = this.links().get(rel);
        if (link == null) {
            throw new IOException(
                String.format(
                    "Link with rel=\"%s\" doesn't exist, use #hasLink()",
                    rel
                )
            );
        }
        return new RestResponse(this).jump(link.uri());
    }

    /**
     * Get all links provided.
     * @return List of all links found
     * @throws IOException If fails
     */
    @NotNull(message = "list of links is never NULL")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Map<String, WebLinkingResponse.Link> links() throws IOException {
        final ConcurrentMap<String, WebLinkingResponse.Link> links =
            new ConcurrentHashMap<String, WebLinkingResponse.Link>();
        final Collection<String> headers =
            this.headers().get(WebLinkingResponse.HEADER);
        if (headers != null) {
            for (final String header : headers) {
                for (final String part : header.split(",")) {
                    final WebLinkingResponse.Link link =
                        new WebLinkingResponse.SimpleLink(part.trim());
                    final String rel = link.get(WebLinkingResponse.REL);
                    if (rel != null) {
                        links.put(rel, link);
                    }
                }
            }
        }
        return links;
    }

    /**
     * Single link.
     */
    @Immutable
    public interface Link extends Map<String, String> {
        /**
         * Its URI.
         * @return URI
         */
        URI uri();
    }

    /**
     * Implementation of a link.
     */
    @Immutable
    @EqualsAndHashCode
    private static final class SimpleLink implements WebLinkingResponse.Link {
        /**
         * Pattern to match link value.
         */
        private static final Pattern PTN = Pattern.compile(
            "<([^>]+)>\\s*;(.*)"
        );
        /**
         * URI encapsulated.
         */
        private final transient String addr;
        /**
         * Map of link params.
         */
        private final transient ArrayMap<String, String> params;
        /**
         * Public ctor (parser).
         * @param text Text to parse
         * @throws IOException If fails
         */
        SimpleLink(final String text) throws IOException {
            final ConcurrentMap<String, String> args =
                new ConcurrentHashMap<String, String>();
            final Matcher matcher = WebLinkingResponse.SimpleLink.PTN
                .matcher(text);
            if (!matcher.matches()) {
                throw new IOException(
                    String.format(
                        "Link header value doesn't comply to RFC-5988: \"%s\"",
                        text
                    )
                );
            }
            this.addr = matcher.group(1);
            for (final String pair
                : matcher.group(2).trim().split("\\s*;\\s*")) {
                final String[] parts = pair.split("=");
                args.put(
                    parts[0].trim().toLowerCase(Locale.ENGLISH),
                    StringUtils.strip(parts[1].trim(), "\"")
                );
            }
            this.params = new ArrayMap<String, String>(args);
        }
        @Override
        public URI uri() {
            return URI.create(this.addr);
        }
        @Override
        public int size() {
            return this.params.size();
        }
        @Override
        public boolean isEmpty() {
            return this.params.isEmpty();
        }
        @Override
        public boolean containsKey(final Object key) {
            return this.params.containsKey(key);
        }
        @Override
        public boolean containsValue(final Object value) {
            return this.params.containsValue(value);
        }
        @Override
        public String get(final Object key) {
            return this.params.get(key);
        }
        @Override
        public String put(final String key, final String value) {
            throw new UnsupportedOperationException("#put()");
        }
        @Override
        public String remove(final Object key) {
            throw new UnsupportedOperationException("#remove()");
        }
        @Override
        public void putAll(final Map<? extends String, ? extends String> map) {
            throw new UnsupportedOperationException("#putAll()");
        }
        @Override
        public void clear() {
            throw new UnsupportedOperationException("#clear()");
        }
        @Override
        public Set<String> keySet() {
            return this.params.keySet();
        }
        @Override
        public Collection<String> values() {
            return this.params.values();
        }
        @Override
        public Set<Map.Entry<String, String>> entrySet() {
            return this.params.entrySet();
        }
    }

}
