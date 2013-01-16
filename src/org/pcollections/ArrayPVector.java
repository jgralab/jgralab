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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * ArrayPVector is a PVector implementation based on arrays. This implementation
 * is biased for low memory consumption in cases where really many PVectors are
 * used. Instances share the same storage array as much as possible.
 * 
 * Also, the plus() operation is approximately O(1). Array copying only occurs
 * upon dynamic growth when the storage array has no more unused elements, or
 * when the ArrayPVector uses a sub-array of a different instance.
 * 
 * This implementation is not thread safe.
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <E>
 *            the element type
 */
public final class ArrayPVector<E> implements PVector<E>, Serializable {
	private static final long serialVersionUID = -3381080251584514162L;

	private static final int INITIAL_SIZE = 10;
	private static final double GROW_FACTOR = 1.5;

	private E[] value;
	private int count;
	private int offset;
	private boolean sublist;
	transient private int hashCode;

	private ArrayPVector() {
		hashCode = 0;
	}

	private ArrayPVector(E[] value, int offset, int count) {
		this.value = value;
		this.offset = offset;
		this.count = count;
		this.sublist = false;
	}

	private static ArrayPVector<?> empty = new ArrayPVector<Object>();

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof List)) {
			return false;
		} else if (obj == this) {
			return true;
		}
		if (obj instanceof ArrayPVector) {
			// use efficient iteration
			@SuppressWarnings("unchecked")
			ArrayPVector<? extends E> other = (ArrayPVector<? extends E>) obj;
			if (count != other.count) {
				return false;
			}
			for (int i = 0; i < count; ++i) {
				if (!value[offset + i].equals(other.value[other.offset + i])) {
					return false;
				}
			}
			return true;
		} else {
			// use list iteration
			@SuppressWarnings("unchecked")
			List<? extends E> other = (List<? extends E>) obj;
			if (other.size() != count) {
				return false;
			}
			int i = 0;
			for (E item : other) {
				if (!value[offset + i++].equals(item)) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public int hashCode() {
		if (hashCode == 0) {
			hashCode = 1;
			for (int i = 0; i < count; ++i) {
				hashCode = 31 * hashCode + value[offset + i].hashCode();
			}
		}
		return hashCode;
	}

	@SuppressWarnings("unchecked")
	public static <T> ArrayPVector<T> empty() {
		return (ArrayPVector<T>) empty;
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public void add(int arg0, E arg1) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean addAll(int arg0, Collection<? extends E> arg1) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public E remove(int arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public E set(int arg0, E arg1) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean add(E arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean addAll(Collection<? extends E> arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return count;
	}

	@Override
	public boolean isEmpty() {
		return count == 0;
	}

	@Override
	public boolean contains(Object o) {
		if (o == null) {
			return false;
		}
		for (int i = 0; i < count; i++) {
			if (value[offset + i].equals(o)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		return new PIterator();
	}

	@Override
	public Object[] toArray() {
		if (count == 0) {
			return new Object[0];
		}
		return Arrays.copyOfRange(value, offset, offset + count);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length >= count) {
			if (count > 0) {
				System.arraycopy(value, offset, a, 0, count);
			}
			if (a.length > count) {
				a[count] = null;
			}
			return a;
		} else {
			return (T[]) Arrays.copyOfRange(value, offset, offset + count,
					a.getClass());
		}
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
	public E get(int index) {
		if (index < 0 || index >= count) {
			throw new IndexOutOfBoundsException();
		}
		return value[offset + index];
	}

	@Override
	public int indexOf(Object o) {
		if (o == null) {
			return -1;
		}
		for (int i = 0; i < count; ++i) {
			if (value[offset + i].equals(o)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o == null) {
			return -1;
		}
		for (int i = count - 1; i >= 0; --i) {
			if (value[offset + i].equals(o)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<E> listIterator() {
		return new PListIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new PListIterator(index);
	}

	@Override
	public PVector<E> minus(Object o) {
		if (o == null) {
			return this;
		}
		int i = indexOf(o);
		return i < 0 ? this : minus(i);
	}

	@Override
	public PVector<E> minus(int i) {
		if (i < 0 || i >= count) {
			throw new IndexOutOfBoundsException();
		}
		ArrayPVector<E> result;
		if (i == 0) {
			// remove first
			result = new ArrayPVector<E>(value, offset + 1, count - 1);
			result.sublist = true;
		} else if (i == count - 1) {
			// remove last
			result = new ArrayPVector<E>(value, offset, count - 1);
			result.sublist = true;
		} else {
			// remove in the middle
			@SuppressWarnings("unchecked")
			E[] val = (E[]) new Object[count - 1];
			System.arraycopy(value, offset, val, 0, i);
			System.arraycopy(value, offset + i + 1, val, i, count - i - 1);
			result = new ArrayPVector<E>(val, 0, count - 1);
		}
		return result;
	}

	@Override
	public PVector<E> minusAll(Collection<?> list) {
		if (list.isEmpty()) {
			return this;
		}
		PVector<E> r = this;
		for (Object e : list) {
			r = r.minus(e);
		}
		return r;
	}

	@Override
	public PVector<E> plus(E e) {
		if (e == null) {
			throw new IllegalArgumentException(
					"Can't add null to an ArrayPVector");
		}
		if (sublist || count == 0 || offset + count + 1 > value.length) {
			if (count == 0) {
				// this is empty
				@SuppressWarnings("unchecked")
				E[] val = (E[]) new Object[INITIAL_SIZE];
				val[0] = e;
				return new ArrayPVector<E>(val, 0, 1);
			} else {
				// we have to clone
				@SuppressWarnings("unchecked")
				E[] val = (E[]) new Object[Math.max(count + 1,
						(int) (count * GROW_FACTOR))];
				System.arraycopy(value, offset, val, 0, count);
				val[count] = e;
				return new ArrayPVector<E>(val, 0, count + 1);

			}
		} else {
			sublist = true;
			value[offset + count] = e;
			return new ArrayPVector<E>(value, offset, count + 1);
		}
	}

	@Override
	public ArrayPVector<E> plus(int i, E e) {
		// insert e at index i
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@SuppressWarnings("unchecked")
	@Override
	public PVector<E> plusAll(Collection<? extends E> l) {
		// append l to this ArrayPList
		int n = l.size();
		if (n == 0) {
			return this;
		} else if (count == 0 && l instanceof ArrayPVector) {
			return (ArrayPVector<E>) l;
		} else if (!sublist && value != null
				&& offset + count + n <= value.length) {
			// l fits into the remaining space
			int i = count;
			for (E e : l) {
				if (e == null) {
					throw new IllegalArgumentException(
							"Can't add null to an ArrayPVector");
				}
				value[i++] = e;
			}
			sublist = true;
			return new ArrayPVector<E>(value, offset, i);
		} else {
			// l longer than remaining space
			E[] val = (E[]) new Object[count + n];
			if (value != null) {
				System.arraycopy(value, offset, val, 0, count);
			}
			int i = count;
			for (E e : l) {
				if (e == null) {
					throw new IllegalArgumentException(
							"Can't add null to an ArrayPVector");
				}
				val[i++] = e;
			}
			return new ArrayPVector<E>(val, 0, i);
		}
	}

	@Override
	public ArrayPVector<E> plusAll(int idx, Collection<? extends E> l) {
		// insert l at (i.e. before) index i
		if (idx < 0 || idx > count) {
			throw new IndexOutOfBoundsException();
		}
		if (l.isEmpty()) {
			return this;
		}
		int n = count + l.size();
		@SuppressWarnings("unchecked")
		E[] val = (E[]) new Object[n];
		if (idx > 0) {
			System.arraycopy(value, 0, val, 0, idx);
		}
		int i = idx;
		for (E item : l) {
			val[i++] = item;
		}
		if (idx < count) {
			System.arraycopy(value, idx, val, n - count + idx, count - idx);
		}
		return new ArrayPVector<E>(val, 0, n);
	}

	@Override
	public ArrayPVector<E> subList(int start, int end) {
		if (start == 0 && end == count) {
			return this;
		} else {
			if (start < 0 || end > count || start > end) {
				throw new IndexOutOfBoundsException();
			}
			if (start == end) {
				return empty();
			}
			ArrayPVector<E> result = new ArrayPVector<E>(value, offset + start,
					end - start);
			result.sublist = true;
			return result;
		}
	}

	@Override
	public ArrayPVector<E> with(int i, E e) {
		if (e == null) {
			throw new IllegalArgumentException(
					"Can't set an element to null in an ArrayPVector");
		}
		if (get(i).equals(e)) {
			return this;
		}
		E[] val = Arrays.copyOfRange(value, offset, offset + count);
		val[i] = e;
		return new ArrayPVector<E>(val, 0, count);
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < count; ++i) {
			sb.append(value[offset + i].toString());
			if (i < count - 1) {
				sb.append(", ");
			}
		}
		return sb.append("]").toString();
	}

	public class PIterator implements Iterator<E> {
		protected int cursor;

		@Override
		public boolean hasNext() {
			return cursor < count;
		}

		@Override
		public E next() {
			if (cursor == count) {
				throw new NoSuchElementException();
			}
			return value[offset + cursor++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public class PListIterator extends PIterator implements ListIterator<E> {

		public PListIterator() {
		}

		public PListIterator(int index) {
			if (index < 0 || index > count) {
				throw new IndexOutOfBoundsException();
			}
			cursor = index;
		}

		@Override
		public boolean hasPrevious() {
			return cursor > 0;
		}

		@Override
		public E previous() {
			if (cursor == 0) {
				throw new NoSuchElementException();
			}
			return value[--cursor + offset];
		}

		@Override
		public int nextIndex() {
			return cursor + 1;
		}

		@Override
		public int previousIndex() {
			return cursor - 1;
		}

		@Override
		public void set(E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(E e) {
			throw new UnsupportedOperationException();
		}
	}
}
