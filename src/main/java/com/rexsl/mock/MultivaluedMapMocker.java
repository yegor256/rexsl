/**
 * Copyright (c) 2011-2014, ReXSL.com
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
package com.rexsl.mock;

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

/**
 * Mocker of {@link MultivaluedMap}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class MultivaluedMapMocker
    implements MultivaluedMap<String, String> {

    /**
     * The map.
     */
    private final transient ConcurrentMap<String, Set<String>> map;

    /**
     * Public ctor.
     */
    public MultivaluedMapMocker() {
        this(new ConcurrentHashMap<String, Set<String>>());
    }

    /**
     * Public ctor.
     * @param origin Map
     */
    public MultivaluedMapMocker(
        final ConcurrentMap<String, Set<String>> origin) {
        this.map = origin;
    }

    /**
     * With this value.
     * @param key Key
     * @param value Value
     * @return This object
     */
    public MultivaluedMapMocker with(final String key, final String value) {
        this.map.putIfAbsent(key, new HashSet<String>());
        this.map.get(key).add(value);
        return this;
    }

    @Override
    public void putSingle(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFirst(final String key) {
        final Set<String> vals = this.map.get(key);
        String first = null;
        if (!vals.isEmpty()) {
            first = vals.iterator().next();
        }
        return first;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        boolean contains = false;
        for (final Set<String> list : this.map.values()) {
            if (list.contains(value.toString())) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    @Override
    public List<String> get(final Object key) {
        return new ArrayList<String>(this.map.get(key));
    }

    @Override
    public List<String> put(final String key, final List<String> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(
        final Map<? extends String, ? extends List<String>> all) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        return this.map.keySet();
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<List<String>> values() {
        final Collection<List<String>> values =
            new LinkedList<List<String>>();
        for (final Set<String> list : this.map.values()) {
            values.add(new ArrayList<String>(list));
        }
        return values;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Set<Entry<String, List<String>>> entrySet() {
        final Set<Entry<String, List<String>>> entries =
            new HashSet<Entry<String, List<String>>>();
        for (final Map.Entry<String, Set<String>> entry : this.map.entrySet()) {
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
