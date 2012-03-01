/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 
 * A persistent vector of non-null elements.
 * <p>
 * This implementation is backed by an IntTreePMap and supports logarithmic-time
 * querying, setting, insertion, and removal.
 * <p>
 * This implementation is thread-safe (assuming Java's AbstractList is
 * thread-safe) although its iterators may not be.
 * 
 * @author harold
 * 
 * @param <E>
 */
@SuppressWarnings("deprecation")
public class TreePVector<E> extends AbstractList<E> implements PVector<E> {
	// // STATIC FACTORY METHODS ////
	private static final TreePVector<Object> EMPTY = new TreePVector<Object>(
			IntTreePMap.empty());

	/**
	 * @param <E>
	 * @return an empty vector
	 */
	@SuppressWarnings("unchecked")
	public static <E> TreePVector<E> empty() {
		return (TreePVector<E>) EMPTY;
	}

	/**
	 * @param <E>
	 * @param e
	 * @return empty().plus(e)
	 */
	public static <E> TreePVector<E> singleton(final E e) {
		return TreePVector.<E> empty().plus(e);
	}

	/**
	 * @param <E>
	 * @param list
	 * @return empty().plusAll(list)
	 */
	@SuppressWarnings("unchecked")
	public static <E> TreePVector<E> from(final Collection<? extends E> list) {
		if (list instanceof TreePVector) {
			return (TreePVector<E>) list; // (actually we only know it's
											// TreePVector<? extends E>)
		}
		// but that's good enough for an immutable
		// (i.e. we can't mess someone else up by adding the wrong type to it)
		return TreePVector.<E> empty().plusAll(list);
	}

	// // PRIVATE CONSTRUCTORS ////
	private final IntTreePMap<E> map;

	private TreePVector(final IntTreePMap<E> map) {
		this.map = map;
	}

	// // REQUIRED METHODS FROM AbstractList ////
	@Override
	public int size() {
		return map.size();
	}

	@Override
	public E get(final int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException();
		}
		return map.get(index);
	}

	// // OVERRIDDEN METHODS FROM AbstractList ////
	@Override
	public Iterator<E> iterator() {
		return map.values().iterator();
	}

	@Override
	public TreePVector<E> subList(final int start, final int end) {
		final int size = size();
		if (start < 0 || end > size || start > end) {
			throw new IndexOutOfBoundsException();
		}
		if (start == end) {
			return empty();
		}
		if (start == 0) {
			if (end == size) {
				return this;
			}
			// remove from end:
			return this.minus(size - 1).subList(start, end);
		}
		// remove from start:
		return this.minus(0).subList(start - 1, end - 1);
	}

	// //IMPLEMENTED METHODS OF PVector ////
	public TreePVector<E> plus(final E e) {
		return new TreePVector<E>(map.plus(size(), e));
	}

	public TreePVector<E> plus(final int i, final E e) {
		if (i < 0 || i > size()) {
			throw new IndexOutOfBoundsException();
		}
		return new TreePVector<E>(map.withKeysChangedAbove(i, 1).plus(i, e));
	}

	public TreePVector<E> minus(final Object e) {
		for (Entry<Integer, E> entry : map.entrySet()) {
			if (entry.getValue().equals(e)) {
				return minus((int) entry.getKey());
			}
		}
		return this;
	}

	public TreePVector<E> minus(final int i) {
		if (i < 0 || i >= size()) {
			throw new IndexOutOfBoundsException();
		}
		return new TreePVector<E>(map.minus(i).withKeysChangedAbove(i, -1));
	}

	public TreePVector<E> plusAll(final Collection<? extends E> list) {
		TreePVector<E> result = this;
		for (E e : list) {
			result = result.plus(e);
		}
		return result;
	}

	public TreePVector<E> minusAll(final Collection<?> list) {
		TreePVector<E> result = this;
		for (Object e : list) {
			result = result.minus(e);
		}
		return result;
	}

	public TreePVector<E> plusAll(int i, final Collection<? extends E> list) {
		if (i < 0 || i > size()) {
			throw new IndexOutOfBoundsException();
		}
		if (list.size() == 0) {
			return this;
		}
		IntTreePMap<E> map = this.map.withKeysChangedAbove(i, list.size());
		for (E e : list) {
			map = map.plus(i++, e);
		}
		return new TreePVector<E>(map);
	}

	public PVector<E> with(final int i, final E e) {
		if (i < 0 || i >= size()) {
			throw new IndexOutOfBoundsException();
		}
		IntTreePMap<E> map = this.map.plus(i, e);
		if (map == this.map) {
			return this;
		}
		return new TreePVector<E>(map);
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	};

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	};

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}
}
