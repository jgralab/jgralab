/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package org.pcollections;

import java.util.Collection;
import java.util.Iterator;

@SuppressWarnings("deprecation")
public class OrderedPSet<E> implements POrderedSet<E> {
	private static final OrderedPSet<Object> EMPTY = new OrderedPSet<Object>(
			Empty.set(), Empty.vector());

	@SuppressWarnings("unchecked")
	public static <E> OrderedPSet<E> empty() {
		return (OrderedPSet<E>) EMPTY;
	}

	@SuppressWarnings("unchecked")
	public static <E> PSet<E> from(final Collection<? extends E> list) {
		if (list instanceof OrderedPSet) {
			return (OrderedPSet<E>) list;
		}
		return OrderedPSet.<E> empty().plusAll(list);
	}

	public static <E> PSet<E> singleton(final E e) {
		return OrderedPSet.<E> empty().plus(e);
	}

	private PSet<E> contents;
	private PVector<E> order;
	private int hashCode = 0;

	private OrderedPSet(PSet<E> c, PVector<E> o) {
		contents = c;
		order = o;
	}

	@Override
	public int hashCode() {
		if ((hashCode == 0) && (size() > 0)) {
			for (E item : order) {
				hashCode += item.hashCode();
			}
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		return contents.equals(o);
	}

	@Override
	public POrderedSet<E> plus(E e) {
		if (contents.contains(e)) {
			return this;
		}
		return new OrderedPSet<E>(contents.plus(e), order.plus(e));
	}

	@Override
	public POrderedSet<E> plusAll(Collection<? extends E> list) {
		POrderedSet<E> s = this;
		for (E e : list) {
			s = s.plus(e);
		}
		return s;
	}

	@Override
	public POrderedSet<E> minus(Object e) {
		if (!contents.contains(e)) {
			return this;
		}
		return new OrderedPSet<E>(contents.minus(e), order.minus(e));
	}

	@Override
	public POrderedSet<E> minusAll(Collection<?> list) {
		POrderedSet<E> s = this;
		for (Object e : list) {
			s = s.minus(e);
		}
		return s;
	}

	@Override
	public Iterator<E> iterator() {
		return order.iterator();
	}

	@Override
	public int size() {
		return contents.size();
	}

	@Override
	public E get(int index) {
		return order.get(index);
	}

	@Override
	public int indexOf(Object o) {
		if (!contents.contains(o)) {
			return -1;
		}
		return order.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		return contents.contains(o);
	}

	@Override
	public Object[] toArray() {
		return order.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return order.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return contents.containsAll(c);
	}

	@Override
	public boolean add(E o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (E item : order) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(item);
		}
		sb.append("}");
		return sb.toString();
	}
}
