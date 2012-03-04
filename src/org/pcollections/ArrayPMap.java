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

import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * ArrayPMap is a PMap implementation based on ArrayPSet (for keys) and
 * ArrayPVector (for values). ArrayPMap preserves insertion order on iteration.
 * Also, memory consumption is low compared to the other PMap implementations,
 * but at the price of O(n^2) effort for the equals method (when comparing to
 * another ArrayPMap).
 * 
 * This implementation is not thread safe.
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <K>
 * @param <V>
 */
public final class ArrayPMap<K, V> implements POrderedMap<K, V>,
		Iterable<SimpleImmutableEntry<K, V>>, Serializable {
	private static final long serialVersionUID = -7101801297307300984L;

	private ArrayPSet<K> keys;
	private ArrayPVector<V> values;
	private int hashCode = 0;

	private ArrayPMap(PSet<K> keys, PVector<V> values) {
		this.keys = (ArrayPSet<K>) keys;
		this.values = (ArrayPVector<V>) values;
	}

	@Override
	public int hashCode() {
		if (hashCode == 0 && size() > 0) {
			for (Map.Entry<K, V> entry : entrySet()) {
				hashCode += entry.hashCode();
			}
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Map)) {
			return false;
		} else if (obj == this) {
			return true;
		}
		@SuppressWarnings("unchecked")
		Map<? extends K, ? extends V> o = (Map<? extends K, ? extends V>) obj;
		if (o.size() != keys.size()) {
			return false;
		}
		Iterator<V> vi = values.iterator();
		for (K key : keys) {
			V val = vi.next();
			// This test is sufficient since this maps values are guaranteed
			// to be not null. We don't have to additionally check whether the
			// other map contains the key.
			if (!val.equals(o.get(key))) {
				return false;
			}
		}
		return true;
	}

	private static ArrayPMap<?, ?> empty = new ArrayPMap<Object, Object>(
			ArrayPSet.empty(), ArrayPVector.empty());

	@SuppressWarnings("unchecked")
	public static <T, U> ArrayPMap<T, U> empty() {
		return (ArrayPMap<T, U>) empty;
	}

	@Override
	public int size() {
		return keys.size();
	}

	@Override
	public boolean isEmpty() {
		return keys.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return keys.contains(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return values.contains(value);
	}

	@Override
	public V get(Object key) {
		int i = keys.toPVector().indexOf(key);
		return (i >= 0) ? values.get(i) : null;
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<K> keySet() {
		return keys;
	}

	@Override
	public Collection<V> values() {
		return values;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		PSet<Map.Entry<K, V>> entries = ArrayPSet.empty();
		Iterator<V> val = values.iterator();
		for (K key : keys) {
			entries = entries.plus(new SimpleImmutableEntry<K, V>(key, val
					.next()));
		}
		return entries;
	}

	@Override
	public ArrayPMap<K, V> minus(Object key) {
		int i = keys.toPVector().indexOf(key);
		return i < 0 ? this : new ArrayPMap<K, V>(keys.minus(key),
				values.minus(i));
	}

	@Override
	public ArrayPMap<K, V> minusAll(Collection<?> l) {
		if (l.isEmpty()) {
			return this;
		}
		ArrayPMap<K, V> result = this;
		for (Object k : l) {
			result = result.minus(k);
		}
		return result;
	}

	@Override
	public ArrayPMap<K, V> plus(K key, V value) {
		int i = keys.toPVector().indexOf(key);
		if (i >= 0) {
			return new ArrayPMap<K, V>(keys, values.with(i, value));
		} else {
			return new ArrayPMap<K, V>(keys.plusWithoutCheck(key),
					values.plus(value));
		}
	}

	@Override
	public ArrayPMap<K, V> plusAll(Map<? extends K, ? extends V> m) {
		if (m.isEmpty()) {
			return this;
		}
		ArrayPMap<K, V> result = this;
		if (m instanceof ArrayPMap) {
			@SuppressWarnings("unchecked")
			ArrayPMap<? extends K, ? extends V> o = (ArrayPMap<K, V>) m;
			Iterator<? extends V> vals = o.values.iterator();
			for (K key : o.keys) {
				result = result.plus(key, vals.next());
			}
		} else {
			for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
				result = result.plus(e.getKey(), e.getValue());
			}
		}
		return result;
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "<>";
		}
		StringBuilder sb = new StringBuilder();
		Iterator<V> val = values.iterator();
		String delim = "<";
		for (K key : keys) {
			sb.append(delim).append(key).append("=").append(val.next());
			delim = ", ";
		}
		return sb.append(">").toString();
	}

	@Override
	public Iterator<SimpleImmutableEntry<K, V>> iterator() {
		return new ArrayPMapIterator();
	}

	private final class ArrayPMapIterator implements
			Iterator<SimpleImmutableEntry<K, V>> {
		Iterator<K> ki = keys.iterator();
		Iterator<V> vi = values.iterator();

		@Override
		public boolean hasNext() {
			return ki.hasNext();
		}

		@Override
		public SimpleImmutableEntry<K, V> next() {
			return new SimpleImmutableEntry<K, V>(ki.next(), vi.next());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public K keyAt(int idx) {
		return keys.get(idx);
	}

	@Override
	public V valueAt(int idx) {
		return values.get(idx);
	}

	@Override
	public Entry<K, V> entryAt(final int idx) {
		return new SimpleImmutableEntry<K, V>(keyAt(idx), valueAt(idx));
	}

}
