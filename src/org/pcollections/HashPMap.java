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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * A persistent map from non-null keys to non-null values.
 * <p>
 * This map uses a given integer map to map hashcodes to lists of elements with
 * the same hashcode. Thus if all elements have the same hashcode, performance
 * is reduced to that of an association list.
 * <p>
 * This implementation is thread-safe (assuming Java's AbstractMap and
 * AbstractSet are thread-safe), although its iterators may not be.
 *
 * @author harold
 *
 * @param <K>
 * @param <V>
 */
public final class HashPMap<K, V> extends AbstractMap<K, V> implements PMap<K, V> {
	// // STATIC FACTORY METHODS ////
	/**
	 * @param <K>
	 * @param <V>
	 * @param intMap
	 * @return a map backed by an empty version of intMap, i.e. backed by
	 *         intMap.minusAll(intMap.keySet())
	 */
	public static <K, V> HashPMap<K, V> empty(final PMap<Integer, PSequence<Entry<K, V>>> intMap) {
		return new HashPMap<>(intMap.minusAll(intMap.keySet()), 0);
	}

	// // PRIVATE CONSTRUCTORS ////
	private final PMap<Integer, PSequence<Entry<K, V>>> intMap;
	private final int size;

	// not externally instantiable (or subclassable):
	private HashPMap(final PMap<Integer, PSequence<Entry<K, V>>> intMap, final int size) {
		this.intMap = intMap;
		this.size = size;
	}

	// // REQUIRED METHODS FROM AbstractMap ////
	// this cache variable is thread-safe since assignment in Java is atomic:
	private Set<Entry<K, V>> entrySet = null;

	@Override
	public Set<Entry<K, V>> entrySet() {
		if (entrySet == null) {
			entrySet = new AbstractSet<Entry<K, V>>() {
				// REQUIRED METHODS OF AbstractSet //
				@Override
				public int size() {
					return size;
				}

				@Override
				public Iterator<Entry<K, V>> iterator() {
					return new SequenceIterator<>(intMap.values().iterator());
				}

				// OVERRIDDEN METHODS OF AbstractSet //
				@Override
				public boolean contains(final Object e) {
					if (!(e instanceof Entry)) {
						return false;
					}
					V value = get(((Entry<?, ?>) e).getKey());
					return value != null && value.equals(((Entry<?, ?>) e).getValue());
				}
			};
		}
		return entrySet;
	}

	// // OVERRIDDEN METHODS FROM AbstractMap ////
	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean containsKey(final Object key) {
		return keyIndexIn(getEntries(key.hashCode()), key) != -1;
	}

	@Override
	public V get(final Object key) {
		PSequence<Entry<K, V>> entries = getEntries(key.hashCode());
		for (Entry<K, V> entry : entries) {
			if (entry.getKey().equals(key)) {
				return entry.getValue();
			}
		}
		return null;
	}

	// // IMPLEMENTED METHODS OF PMap////
	@Override
	public HashPMap<K, V> plusAll(final Map<? extends K, ? extends V> map) {
		HashPMap<K, V> result = this;
		for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
			result = result.plus(entry.getKey(), entry.getValue());
		}
		return result;
	}

	@Override
	public HashPMap<K, V> minusAll(final Collection<?> keys) {
		HashPMap<K, V> result = this;
		for (Object key : keys) {
			result = result.minus(key);
		}
		return result;
	}

	@Override
	public HashPMap<K, V> plus(final K key, final V value) {
		PSequence<Entry<K, V>> entries = getEntries(key.hashCode());
		int size0 = entries.size(), i = keyIndexIn(entries, key);
		if (i != -1) {
			entries = entries.minus(i);
		}
		entries = entries.plus(new org.pcollections.SimpleImmutableEntry<>(key, value));
		return new HashPMap<>(intMap.plus(key.hashCode(), entries), size - size0 + entries.size());
	}

	@Override
	public HashPMap<K, V> minus(final Object key) {
		PSequence<Entry<K, V>> entries = getEntries(key.hashCode());
		int i = keyIndexIn(entries, key);
		if (i == -1) {
			return this;
		}
		entries = entries.minus(i);
		if (entries.size() == 0) {
			return new HashPMap<>(intMap.minus(key.hashCode()), size - 1);
		}
		// otherwise replace hash entry with new smaller one:
		return new HashPMap<>(intMap.plus(key.hashCode(), entries), size - 1);
	}

	// // PRIVATE UTILITIES ////
	private PSequence<Entry<K, V>> getEntries(final int hash) {
		PSequence<Entry<K, V>> entries = intMap.get(hash);
		if (entries == null) {
			return ConsPStack.empty();
		}
		return entries;
	}

	// // PRIVATE STATIC UTILITIES ////
	private static <K, V> int keyIndexIn(final PSequence<Entry<K, V>> entries, final Object key) {
		int i = 0;
		for (Entry<K, V> entry : entries) {
			if (entry.getKey().equals(key)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	static class SequenceIterator<E> implements Iterator<E> {
		private final Iterator<PSequence<E>> i;
		private PSequence<E> seq = ConsPStack.empty();

		SequenceIterator(Iterator<PSequence<E>> i) {
			this.i = i;
		}

		@Override
		public boolean hasNext() {
			return seq.size() > 0 || i.hasNext();
		}

		@Override
		public E next() {
			if (seq.size() == 0) {
				seq = i.next();
			}
			final E result = seq.get(0);
			seq = seq.subList(1, seq.size());
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	@Deprecated
	public V put(K k, V v) {
		throw new UnsupportedOperationException();
	};

	@Override
	@Deprecated
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}
}
