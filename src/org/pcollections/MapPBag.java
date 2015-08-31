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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 
 * A map-backed persistent bag.
 * <p>
 * If the backing map is thread-safe, then this implementation is thread-safe
 * (assuming Java's AbstractCollection is thread-safe), although its iterators
 * may not be.
 * 
 * @author harold
 * 
 * @param <E>
 */
public final class MapPBag<E> extends AbstractCollection<E> implements PBag<E> {
	// // STATIC FACTORY METHODS ////
	/**
	 * @param <E>
	 * @param map
	 * @return a PBag backed by an empty version of map, i.e. by
	 *         map.minusAll(map.keySet())
	 */
	public static <E> MapPBag<E> empty(final PMap<E, Integer> map) {
		return new MapPBag<>(map.minusAll(map.keySet()), 0);
	}

	// // PRIVATE CONSTRUCTORS ////
	private final PMap<E, Integer> map;
	private final int size;

	// not instantiable (or subclassable):
	private MapPBag(final PMap<E, Integer> map, final int size) {
		this.map = map;
		this.size = size;
	}

	// // REQUIRED METHODS FROM AbstractCollection ////
	@Override
	public int size() {
		return size;
	}

	@Override
	public Iterator<E> iterator() {
		final Iterator<Entry<E, Integer>> i = map.entrySet().iterator();
		return new Iterator<E>() {
			private E e;
			private int n = 0;

			@Override
			public boolean hasNext() {
				return n > 0 || i.hasNext();
			}

			@Override
			public E next() {
				if (n == 0) { // finished with current element
					Entry<E, Integer> entry = i.next();
					e = entry.getKey();
					n = entry.getValue();
				}
				n--;
				return e;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	// // OVERRIDDEN METHODS OF AbstractCollection ////
	@Override
	public boolean contains(final Object e) {
		return map.containsKey(e);
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		for (E e : this) {
			hashCode += e.hashCode();
		}
		return hashCode;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object that) {
		if (!(that instanceof PBag)) {
			return false;
		}
		if (!(that instanceof MapPBag)) {
			// make that into a MapPBag:
			// TODO this is INEFFICIENT
			MapPBag<Object> empty = (MapPBag<Object>) this.minusAll(this);
			that = empty.plusAll((PBag<?>) that);
		}
		return this.map.equals(((MapPBag<?>) that).map);
	}

	// // IMPLEMENTED METHODS OF PSet ////
	@Override
	public MapPBag<E> plus(final E e) {
		return new MapPBag<>(map.plus(e, count(e) + 1), size + 1);
	}

	@Override
	@SuppressWarnings("unchecked")
	public MapPBag<E> minus(final Object e) {
		int n = count(e);
		if (n == 0) {
			return this;
		}
		if (n == 1) {
			return new MapPBag<>(map.minus(e), size - 1);
		}
		// otherwise just decrement count:
		return new MapPBag<>(map.plus((E) e, n - 1), size - 1);
	}

	@Override
	public MapPBag<E> plusAll(final Collection<? extends E> list) {
		MapPBag<E> bag = this;
		for (E e : list) {
			bag = bag.plus(e);
		}
		return bag;
	}

	@Override
	public MapPBag<E> minusAll(final Collection<?> list) {
		// removes _all_ elements found in list, i.e. counts are irrelevant:
		PMap<E, Integer> map = this.map.minusAll(list);
		return new MapPBag<>(map, size(map)); // (completely recomputes size)
	}

	// // PRIVATE UTILITIES ////
	// TODO should this be part of PBag?
	private int count(final Object o) {
		if (!contains(o)) {
			return 0;
		}
		// otherwise o must be an E:
		return map.get(o);
	}

	// // PRIVATE STATIC UTILITIES ////
	private static int size(final PMap<?, Integer> map) {
		int size = 0;
		for (Integer n : map.values()) {
			size += n;
		}
		return size;
	}

	@Override
	public boolean add(E o) {
		throw new UnsupportedOperationException();
	};

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}
}
