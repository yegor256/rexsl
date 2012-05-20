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
package com.rexsl.test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.w3c.dom.Node;

/**
 * Wrapper of {@link List}.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.7
 */
@SuppressWarnings("PMD.TooManyMethods")
final class ListWrapper<T> implements List<T> {

    /**
     * The original list.
     */
    private final transient List<T> original;

    /**
     * The XML where this list came from.
     */
    private final transient Node dom;

    /**
     * XPath.
     */
    private final transient String xpath;

    /**
     * Public ctor.
     * @param list Original list
     * @param node The XML
     * @param addr Address
     */
    public ListWrapper(final List<T> list, final Node node, final String addr) {
        this.original = list;
        this.dom = node;
        this.xpath = addr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(final T element) {
        throw new UnsupportedOperationException("#add(T)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final int index, final T element) {
        throw new UnsupportedOperationException("#add(int, T)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(final Collection<? extends T> elements) {
        throw new UnsupportedOperationException("#addAll(Collection)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(final int index,
        final Collection<? extends T> elements) {
        throw new UnsupportedOperationException("#add(int, Collection)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("#clear()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Object element) {
        return this.original.contains(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(final Collection<?> elements) {
        return this.original.containsAll(elements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object element) {
        return this.original.equals(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(final int index) {
        if (index >= this.size()) {
            throw new NodeNotFoundException(
                String.format(
                    "Index (%d) is out of bounds (size=%d)",
                    index,
                    this.size()
                ),
                this.dom,
                this.xpath
            );
        }
        return this.original.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.original.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(final Object element) {
        return this.original.indexOf(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.original.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        return this.original.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(final Object element) {
        return this.original.lastIndexOf(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<T> listIterator() {
        return this.original.listIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<T> listIterator(final int index) {
        return this.original.listIterator(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T remove(final int index) {
        throw new UnsupportedOperationException("#remove(int)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(final Object element) {
        throw new UnsupportedOperationException("#remove(Object)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(final Collection<?> elements) {
        throw new UnsupportedOperationException("#removeAll(Collection)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(final Collection<?> elements) {
        throw new UnsupportedOperationException("#retainAll(Collection)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T set(final int index, final T element) {
        throw new UnsupportedOperationException("#set(int, T)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.original.size();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The method throws {@link NodeNotFoundException} when either
     * {@code start} or {@code end} is bigger than the size of the list. In all
     * other cases of illegal method call (start is less than zero, end is
     * less than zero, or start is bigger than end) a standard
     * {@link IndexOutOfBoundException} is thrown (by the incapsulated
     * implementation of {@Link List}).
     */
    @Override
    public List<T> subList(final int start, final int end) {
        if (start >= this.size()) {
            throw new NodeNotFoundException(
                String.format(
                    "Start of subList (%d) is out of bounds (size=%d)",
                    start,
                    this.size()
                ),
                this.dom,
                this.xpath
            );
        }
        if (end >= this.size()) {
            throw new NodeNotFoundException(
                String.format(
                    "End of subList (%d) is out of bounds (size=%d)",
                    end,
                    this.size()
                ),
                this.dom,
                this.xpath
            );
        }
        return this.original.subList(start, end);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return this.original.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> E[] toArray(final E[] array) {
        return this.original.toArray(array);
    }

}
