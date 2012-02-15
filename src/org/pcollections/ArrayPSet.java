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
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * ArrayPSet is a PSet implementation based on an ArrayPVector. ArrayPSet
 * preserves the insertion order upon iteration. When the size gets above
 * SIZELIMIT elements, a plus() operation automatically promotes to an
 * OrderedPSet.
 * 
 * This implementation is not thread safe.
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <E>
 */
public final class ArrayPSet<E> implements POrderedSet<E>, Serializable {
	private static final long serialVersionUID = 5643294766821496614L;
	// When to promote to OrderedPSet
	private static final int SIZELIMIT = 16;
	private PVector<E> entries;
	private int hashCode;

	private ArrayPSet(PVector<E> entries) {
		this.entries = entries;
	}

	private static ArrayPSet<?> empty = new ArrayPSet<Object>(
			ArrayPVector.empty());

	@SuppressWarnings("unchecked")
	public static <T> ArrayPSet<T> empty() {
		return (ArrayPSet<T>) empty;
	}

	@Override
	public int hashCode() {
		if ((hashCode == 0) && (size() > 0)) {
			for (E item : entries) {
				hashCode += item.hashCode();
			}
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof Set)) {
			return false;
		} else if (obj == this) {
			return true;
		}
		@SuppressWarnings("rawtypes")
		Set other = (Set) obj;
		if (other.size() != entries.size()) {
			return false;
		}
		for (E item : this) {
			if (!other.contains(item)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean contains(Object o) {
		return entries.contains(o);
	}

	@Override
	public PSet<E> plus(E e) {
		if (contains(e)) {
			return this;
		}
		if (entries.size() >= SIZELIMIT) {
			POrderedSet<E> s = OrderedPSet.empty();
			return s.plusAll(entries).plus(e);
		}
		return new ArrayPSet<E>(entries.plus(e));
	}

	PSet<E> plusWithoutCheck(E e) {
		return new ArrayPSet<E>(entries.plus(e));
	}

	@Override
	public PSet<E> plusAll(Collection<? extends E> list) {
		if (list.isEmpty()) {
			return this;
		}
		PSet<E> r = this;
		for (E e : list) {
			r = r.plus(e);
		}
		return r;
	}

	@Override
	public PSet<E> minus(Object e) {
		return contains(e) ? new ArrayPSet<E>(entries.minus(e)) : this;
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean add(E o) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return entries.size();
	}

	@Override
	public boolean isEmpty() {
		return entries.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return entries.iterator();
	}

	@Override
	public Object[] toArray() {
		return entries.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return entries.toArray(a);
	}

	public PVector<E> toPVector() {
		return entries;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public PSet<E> minusAll(Collection<?> list) {
		if (list.isEmpty()) {
			return this;
		}
		PSet<E> r = this;
		for (Object e : list) {
			r = r.minus(e);
		}
		return r;
	}

	@Override
	public E get(int index) {
		return entries.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return entries.indexOf(o);
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "{}";
		}
		StringBuilder sb = new StringBuilder();
		String delim = "{";
		for (E e : entries) {
			sb.append(delim).append(e);
			delim = ", ";
		}
		return sb.append("}").toString();

	}
}
