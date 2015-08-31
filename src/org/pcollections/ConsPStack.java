/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * 
 * A simple persistent stack of non-null values.
 * <p>
 * This implementation is thread-safe (assuming Java's AbstractSequentialList is
 * thread-safe), although its iterators may not be.
 * 
 * @author harold
 * 
 * @param <E>
 */
public final class ConsPStack<E> extends AbstractSequentialList<E> implements
		PStack<E> {
	// // STATIC FACTORY METHODS ////
	private static final ConsPStack<Object> EMPTY = new ConsPStack<>();

	/**
	 * @param <E>
	 * @return an empty stack
	 */
	@SuppressWarnings("unchecked")
	public static <E> ConsPStack<E> empty() {
		return (ConsPStack<E>) EMPTY;
	}

	/**
	 * @param <E>
	 * @param e
	 * @return empty().plus(e)
	 */
	public static <E> ConsPStack<E> singleton(final E e) {
		return ConsPStack.<E> empty().plus(e);
	}

	/**
	 * @param <E>
	 * @param list
	 * @return a stack consisting of the elements of list in the order of
	 *         list.iterator()
	 */
	@SuppressWarnings("unchecked")
	public static <E> ConsPStack<E> from(final Collection<? extends E> list) {
		if (list instanceof ConsPStack) {
			return (ConsPStack<E>) list; // (actually we only know it's
											// ConsPStack<? extends E>)
		}
		// but that's good enough for an immutable
		// (i.e. we can't mess someone else up by adding the wrong type to it)
		return from(list.iterator());
	}

	private static <E> ConsPStack<E> from(final Iterator<? extends E> i) {
		if (!i.hasNext()) {
			return empty();
		}

		E e = i.next();
		// Java 8 requires explicit typing: "ConsPStack.<E>". This is a change
		// to the original PCollection code.
		return ConsPStack.<E> from(i).plus(e);
	}

	// // PRIVATE CONSTRUCTORS ////
	private final E first;
	private final ConsPStack<E> rest;
	private final int size;

	// not externally instantiable (or subclassable):
	private ConsPStack() { // EMPTY constructor
		if (EMPTY != null) {
			throw new RuntimeException(
					"empty constructor should only be used once");
		}
		size = 0;
		first = null;
		rest = null;
	}

	private ConsPStack(final E first, final ConsPStack<E> rest) {
		this.first = first;
		this.rest = rest;

		size = 1 + rest.size;
	}

	// // REQUIRED METHODS FROM AbstractSequentialList ////
	@Override
	public int size() {
		return size;
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}

		return new ListIterator<E>() {
			int i = index;
			ConsPStack<E> next = subList(index);

			@Override
			public boolean hasNext() {
				return next.size > 0;
			}

			@Override
			public boolean hasPrevious() {
				return i > 0;
			}

			@Override
			public int nextIndex() {
				return index;
			}

			@Override
			public int previousIndex() {
				return index - 1;
			}

			@Override
			public E next() {
				E e = next.first;
				next = next.rest;
				return e;
			}

			@Override
			public E previous() {
				System.err
						.println("ConsPStack.listIterator().previous() is inefficient, don't use it!");
				next = subList(index - 1); // go from beginning...
				return next.first;
			}

			@Override
			public void add(final E o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void set(final E o) {
				throw new UnsupportedOperationException();
			}
		};
	}

	// // OVERRIDDEN METHODS FROM AbstractSequentialList ////
	@Override
	public ConsPStack<E> subList(final int start, final int end) {
		if (start < 0 || end > size || start > end) {
			throw new IndexOutOfBoundsException();
		}
		if (end == size) {
			return subList(start); // this is faster
		}
		if (start == end) {
			return empty();
		}
		if (start == 0) {
			return new ConsPStack<>(first, rest.subList(0, end - 1));
		}
		// otherwise, don't want the current element:
		return rest.subList(start - 1, end - 1);
	}

	// // IMPLEMENTED METHODS OF PStack ////
	@Override
	public ConsPStack<E> plus(final E e) {
		return new ConsPStack<>(e, this);
	}

	@Override
	public ConsPStack<E> plusAll(final Collection<? extends E> list) {
		ConsPStack<E> result = this;
		for (E e : list) {
			result = result.plus(e);
		}
		return result;
	}

	@Override
	public ConsPStack<E> plus(final int i, final E e) {
		if (i < 0 || i > size) {
			throw new IndexOutOfBoundsException();
		}
		if (i == 0) {
			return plus(e);
		}
		return new ConsPStack<>(first, rest.plus(i - 1, e));
	}

	@Override
	public ConsPStack<E> plusAll(final int i, final Collection<? extends E> list) {
		// TODO inefficient if list.isEmpty()
		if (i < 0 || i > size) {
			throw new IndexOutOfBoundsException();
		}
		if (i == 0) {
			return plusAll(list);
		}
		return new ConsPStack<>(first, rest.plusAll(i - 1, list));
	}

	@Override
	public ConsPStack<E> minus(final Object e) {
		if (size == 0) {
			return this;
		}
		if (first.equals(e)) {
			return rest; // don't recurse (only remove one)
		}
		// otherwise keep looking:
		ConsPStack<E> newRest = rest.minus(e);
		if (newRest == rest) {
			return this;
		}
		return new ConsPStack<>(first, newRest);
	}

	@Override
	public ConsPStack<E> minus(final int i) {
		return minus(get(i));
	}

	@Override
	public ConsPStack<E> minusAll(final Collection<?> list) {
		if (size == 0) {
			return this;
		}
		if (list.contains(first)) {
			return rest.minusAll(list); // recursively delete all
		}
		// either way keep looking:
		ConsPStack<E> newRest = rest.minusAll(list);
		if (newRest == rest) {
			return this;
		}
		return new ConsPStack<>(first, newRest);
	}

	@Override
	public ConsPStack<E> with(final int i, final E e) {
		if (i < 0 || i >= size) {
			throw new IndexOutOfBoundsException();
		}
		if (i == 0) {
			if (first.equals(e)) {
				return this;
			}
			return new ConsPStack<>(e, rest);
		}
		ConsPStack<E> newRest = rest.with(i - 1, e);
		if (newRest == rest) {
			return this;
		}
		return new ConsPStack<>(first, newRest);
	}

	@Override
	public ConsPStack<E> subList(final int start) {
		if (start < 0 || start > size) {
			throw new IndexOutOfBoundsException();
		}
		if (start == 0) {
			return this;
		}
		return rest.subList(start - 1);
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	};

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	};
}
