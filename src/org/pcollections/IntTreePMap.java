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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 
 * An efficient persistent map from integer keys to non-null values.
 * <p>
 * Iteration occurs in the integer order of the keys.
 * <p>
 * This implementation is thread-safe (assuming Java's AbstractMap and
 * AbstractSet are thread-safe), although its iterators may not be.
 * <p>
 * The balanced tree is based on the Glasgow Haskell Compiler's Data.Map
 * implementation, which in turn is based on "size balanced binary trees" as
 * described by:
 * <p>
 * Stephen Adams, "Efficient sets: a balancing act", Journal of Functional
 * Programming 3(4):553-562, October 1993,
 * http://www.swiss.ai.mit.edu/~adams/BB/.
 * <p>
 * J. Nievergelt and E.M. Reingold, "Binary search trees of bounded balance",
 * SIAM journal of computing 2(1), March 1973.
 * 
 * @author harold
 * 
 * @param <V>
 */
@SuppressWarnings("deprecation")
public final class IntTreePMap<V> extends AbstractMap<Integer, V> implements
		PMap<Integer, V> {
	// // STATIC FACTORY METHODS ////
	private static final IntTreePMap<Object> EMPTY = new IntTreePMap<Object>(
			IntTree.EMPTYNODE);

	/**
	 * @param <V>
	 * @return an empty map
	 */
	@SuppressWarnings("unchecked")
	public static <V> IntTreePMap<V> empty() {
		return (IntTreePMap<V>) EMPTY;
	}

	/**
	 * @param <V>
	 * @param key
	 * @param value
	 * @return empty().plus(key, value)
	 */
	public static <V> IntTreePMap<V> singleton(final Integer key, final V value) {
		return IntTreePMap.<V> empty().plus(key, value);
	}

	/**
	 * @param <V>
	 * @param map
	 * @return empty().plusAll(map)
	 */
	@SuppressWarnings("unchecked")
	public static <V> IntTreePMap<V> from(
			final Map<? extends Integer, ? extends V> map) {
		if (map instanceof IntTreePMap) {
			return (IntTreePMap<V>) map; // (actually we only know it's
											// IntTreePMap<? extends V>)
		}
		// but that's good enough for an immutable
		// (i.e. we can't mess someone else up by adding the wrong type to it)
		return IntTreePMap.<V> empty().plusAll(map);
	}

	// // PRIVATE CONSTRUCTORS ////
	private final IntTree<V> root;

	// not externally instantiable (or subclassable):
	private IntTreePMap(final IntTree<V> root) {
		this.root = root;
	}

	private IntTreePMap<V> withRoot(final IntTree<V> root) {
		if (root == this.root) {
			return this;
		}
		return new IntTreePMap<V>(root);
	}

	// // UNINHERITED METHODS OF IntTreePMap ////
	IntTreePMap<V> withKeysChangedAbove(final int key, final int delta) {
		// TODO check preconditions of changeKeysAbove()
		// TODO make public?
		return withRoot(root.changeKeysAbove(key, delta));
	}

	IntTreePMap<V> withKeysChangedBelow(final int key, final int delta) {
		// TODO check preconditions of changeKeysAbove()
		// TODO make public?
		return withRoot(root.changeKeysBelow(key, delta));
	}

	// // REQUIRED METHODS FROM AbstractMap ////
	// this cache variable is thread-safe, since assignment in Java is atomic:
	private Set<Entry<Integer, V>> entrySet = null;

	@Override
	public Set<Entry<Integer, V>> entrySet() {
		if (entrySet == null) {
			entrySet = new AbstractSet<Entry<Integer, V>>() {
				// REQUIRED METHODS OF AbstractSet //
				@Override
				public int size() { // same as Map
					return IntTreePMap.this.size();
				}

				@Override
				public Iterator<Entry<Integer, V>> iterator() {
					return root.iterator();
				}

				// OVERRIDDEN METHODS OF AbstractSet //
				@Override
				public boolean contains(final Object e) {
					if (!(e instanceof Entry)) {
						return false;
					}
					V value = get(((Entry<?, ?>) e).getKey());
					return value != null
							&& value.equals(((Entry<?, ?>) e).getValue());
				}
			};
		}
		return entrySet;
	}

	// // OVERRIDDEN METHODS FROM AbstractMap ////
	@Override
	public int size() {
		return root.size();
	}

	@Override
	public boolean containsKey(final Object key) {
		if (!(key instanceof Integer)) {
			return false;
		}
		return root.containsKey((Integer) key);
	}

	@Override
	public V get(final Object key) {
		if (!(key instanceof Integer)) {
			return null;
		}
		return root.get((Integer) key);
	}

	// // IMPLEMENTED METHODS OF PMap////
	public IntTreePMap<V> plus(final Integer key, final V value) {
		return withRoot(root.plus(key, value));
	}

	public IntTreePMap<V> minus(final Object key) {
		if (!(key instanceof Integer)) {
			return this;
		}
		return withRoot(root.minus((Integer) key));
	}

	public IntTreePMap<V> plusAll(final Map<? extends Integer, ? extends V> map) {
		IntTree<V> root = this.root;
		for (Entry<? extends Integer, ? extends V> entry : map.entrySet()) {
			root = root.plus(entry.getKey(), entry.getValue());
		}
		return withRoot(root);
	}

	public IntTreePMap<V> minusAll(final Collection<?> keys) {
		IntTree<V> root = this.root;
		for (Object key : keys) {
			if (key instanceof Integer) {
				root = root.minus((Integer) key);
			}
		}
		return withRoot(root);
	}

	@Override
	public V put(Integer k, V v) {
		throw new UnsupportedOperationException();
	};

	@Override
	public void putAll(Map<? extends Integer, ? extends V> m) {
		throw new UnsupportedOperationException();
	}
}
