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
package com.rexsl.page;

import java.net.URI;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.mockito.Mockito;

/**
 * Builds an instance of {@link UriInfo}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class UriInfoMocker {

    /**
     * The mock.
     */
    private final transient UriInfo info = Mockito.mock(UriInfo.class);

    /**
     * Query parameters.
     */
    private final transient ConcurrentMap<String, Set<String>> params =
        new ConcurrentHashMap<String, Set<String>>();

    /**
     * Public ctor.
     */
    public UriInfoMocker() {
        this.withBaseUri(
            UriBuilder.fromUri("http://localhost:99/").build()
        );
        this.withRequestUri(
            UriBuilder.fromUri("http://localhost:99/local/foo").build()
        );
        Mockito.doReturn(new UriInfoMocker.SimpleMultivaluedMap(this.params))
            .when(this.info).getQueryParameters();
    }

    /**
     * With this request URI.
     * @param uri The URI
     * @return This object
     */
    public UriInfoMocker withRequestUri(final URI uri) {
        Mockito.doReturn(uri).when(this.info).getRequestUri();
        Mockito.doReturn(UriBuilder.fromUri(uri))
            .when(this.info).getRequestUriBuilder();
        Mockito.doReturn(UriBuilder.fromUri(uri))
            .when(this.info).getAbsolutePathBuilder();
        Mockito.doReturn(uri).when(this.info).getAbsolutePath();
        return this;
    }

    /**
     * With this base URI.
     * @param uri The URI
     * @return This object
     */
    public UriInfoMocker withBaseUri(final URI uri) {
        Mockito.doReturn(UriBuilder.fromUri(uri))
            .when(this.info).getBaseUriBuilder();
        Mockito.doReturn(uri).when(this.info).getBaseUri();
        return this;
    }

    /**
     * Build an instance of provided class.
     * @return The resource just created
     */
    public UriInfo mock() {
        return this.info;
    }

    private static final class SimpleMultivaluedMap
        implements MultivaluedMap<String, String> {
        /**
         * The map.
         */
        private final transient ConcurrentMap<String, Set<String>> map;
        /**
         * Public ctor.
         * @param origin Map
         */
        public SimpleMultivaluedMap(
            final ConcurrentMap<String, Set<String>> origin) {
            this.map = origin;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void putSingle(final String key, final String value) {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void add(final String key, final String value) {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String getFirst(final String key) {
            final Set<String> vals = this.map.get(key);
            String first = null;
            if (!vals.isEmpty()) {
                first = vals.iterator().next();
            }
            return first;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return this.map.size();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty() {
            return this.map.isEmpty();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean containsKey(final Object key) {
            return this.map.containsKey(key);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean containsValue(final Object value) {
            boolean contains = false;
            for (Set<String> list : this.map.values()) {
                if (list.contains(value.toString())) {
                    contains = true;
                    break;
                }
            }
            return contains;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public List<String> get(final Object key) {
            return new ArrayList<String>(this.map.get(key));
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public List<String> put(final String key, final List<String> value) {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public List<String> remove(final Object key) {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void putAll(
            final Map<? extends String, ? extends List<String>> all) {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Set<String> keySet() {
            return this.map.keySet();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        public Collection<List<String>> values() {
            final Collection<List<String>> values =
                new LinkedList<List<String>>();
            for (Set<String> list : this.map.values()) {
                values.add(new ArrayList<String>(list));
            }
            return values;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        public Set<Entry<String, List<String>>> entrySet() {
            final Set<Entry<String, List<String>>> entries =
                new HashSet<Entry<String, List<String>>>();
            for (Map.Entry<String, Set<String>> entry : this.map.entrySet()) {
                entries.add(
                    new AbstractMap.SimpleEntry<String, List<String>>(
                        entry.getKey(),
                        new ArrayList<String>(entry.getValue())
                    )
                );
            }
            return entries;
        }
    }

}
