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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * A map-backed persistent set.
 * <p>
 * If the backing map is thread-safe, then this implementation is thread-safe
 * (assuming Java's AbstractSet is thread-safe), although its iterators may not
 * be.
 * 
 * @author harold
 * 
 * @param <E>
 */
@SuppressWarnings("deprecation")
public final class MapPSet<E> extends AbstractSet<E> implements PSet<E> {
	// // STATIC FACTORY METHODS ////
	/**
	 * @param <E>
	 * @param map
	 * @return a PSet with the elements of map.keySet(), backed by map
	 */
	@SuppressWarnings("unchecked")
	public static <E> MapPSet<E> from(final PMap<E, ?> map) {
		return new MapPSet<E>((PMap<E, Object>) map);
	}

	/**
	 * @param <E>
	 * @param map
	 * @param e
	 * @return from(map).plus(e)
	 */
	public static <E> MapPSet<E> from(final PMap<E, ?> map, E e) {
		return from(map).plus(e);
	}

	/**
	 * @param <E>
	 * @param map
	 * @param list
	 * @return from(map).plusAll(list)
	 */
	public static <E> MapPSet<E> from(final PMap<E, ?> map,
			final Collection<? extends E> list) {
		return from(map).plusAll(list);
	}

	// // PRIVATE CONSTRUCTORS ////
	private final PMap<E, Object> map;

	// not instantiable (or subclassable):
	private MapPSet(final PMap<E, Object> map) {
		this.map = map;
	}

	// // REQUIRED METHODS FROM AbstractSet ////
	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}

	// // OVERRIDDEN METHODS OF AbstractSet ////
	@Override
	public boolean contains(final Object e) {
		return map.containsKey(e);
	}

	// // IMPLEMENTED METHODS OF PSet ////
	private static enum In {
		IN
	}

	public MapPSet<E> plus(final E e) {
		if (contains(e)) {
			return this;
		}
		return new MapPSet<E>(map.plus(e, In.IN));
	}

	public MapPSet<E> minus(final Object e) {
		if (!contains(e)) {
			return this;
		}
		return new MapPSet<E>(map.minus(e));
	}

	public MapPSet<E> plusAll(final Collection<? extends E> list) {
		PMap<E, Object> map = this.map;
		for (E e : list) {
			map = map.plus(e, In.IN);
		}
		return from(map);
	}

	public MapPSet<E> minusAll(final Collection<?> list) {
		PMap<E, Object> map = this.map.minusAll(list);
		return from(map);
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
