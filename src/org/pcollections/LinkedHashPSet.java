package org.pcollections;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Wraps a {@link LinkedHashSet} and converts to some other {@link POrderedSet}
 * on "modification".
 *
 * CAUTION: Don't ever modify a {@link LinkedHashSet} which backs a
 * LinkedHashPSet! This will change the LinkedHashPSet as well. (Such
 * modifications will be detected and you'll get an exception at the next access
 * to the LinkedHashPSet that's backed by the LinkedHashSet in question).
 *
 * @param <E>
 */
public class LinkedHashPSet<E> implements POrderedSet<E> {

	private final LinkedHashSet<E> lhs;
	private final int storedHashCode;

	private LinkedHashPSet(LinkedHashSet<E> l) {
		lhs = l;
		storedHashCode = lhs.hashCode();
	}

	/**
	 * @param lhs
	 * @return an immutable POrderedSet wrapping the given {@link LinkedHashSet}
	 */
	public static <T> LinkedHashPSet<T> immute(LinkedHashSet<T> lhs) {
		return new LinkedHashPSet<>(lhs);
	}

	private final void checkUnmodified() {
		if (storedHashCode != lhs.hashCode()) {
			throw new RuntimeException("Backing LinkedHashSet was modified!");
		}
	}

	@Override
	public int hashCode() {
		checkUnmodified();
		return storedHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		checkUnmodified();
		if ((obj == null) || !(obj instanceof Set)) {
			return false;
		} else if (obj == this) {
			return true;
		}
		@SuppressWarnings("rawtypes")
		Set other = (Set) obj;
		if (other.size() != lhs.size()) {
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
	public final POrderedSet<E> plus(E e) {
		if (contains(e)) {
			return this;
		} else {
			return immute(1).plus(e);
		}
	}

	@Override
	public final int size() {
		checkUnmodified();
		return lhs.size();
	}

	@Override
	public final boolean isEmpty() {
		checkUnmodified();
		return lhs.isEmpty();
	}

	@Override
	public final boolean contains(Object o) {
		checkUnmodified();
		return lhs.contains(o);
	}

	private static final class ImmutableIterator<E> implements Iterator<E> {
		private Iterator<E> it;

		private ImmutableIterator(Iterator<E> i) {
			it = i;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public E next() {
			return it.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public final Iterator<E> iterator() {
		checkUnmodified();
		return new ImmutableIterator<>(lhs.iterator());
	}

	@Override
	public final Object[] toArray() {
		checkUnmodified();
		return lhs.toArray();
	}

	@Override
	public final <T> T[] toArray(T[] a) {
		checkUnmodified();
		return lhs.toArray(a);
	}

	@Override
	public final boolean containsAll(Collection<?> c) {
		return lhs.containsAll(c);
	}

	@Override
	public final E get(int index) {
		checkUnmodified();
		if ((index < 0) || (index > (size() - 1))) {
			throw new IndexOutOfBoundsException();
		}
		for (E i : lhs) {
			if (index == 0) {
				return i;
			}
			index--;
		}
		// cannot happen
		throw new RuntimeException();
	}

	@Override
	public final int indexOf(Object o) {
		if (!contains(o)) {
			return -1;
		}
		int idx = 0;
		for (E i : this) {
			if (i == o) {
				return idx;
			}
			idx++;
		}
		// cannot happen
		throw new RuntimeException();
	}

	@Override
	public final POrderedSet<E> plusAll(Collection<? extends E> list) {
		checkUnmodified();
		return immute(list.size()).plusAll(list);
	}

	private final POrderedSet<E> immute(int maxAddCount) {
		POrderedSet<E> r = ((lhs.size() + maxAddCount) <= ArrayPSet.SIZELIMIT) ? ArrayPSet.<E> empty()
				: OrderedPSet.<E> empty();
		return r.plusAll(lhs);
	}

	@Override
	public final POrderedSet<E> minus(Object e) {
		if (!contains(e)) {
			return this;
		} else {
			return immute(-1).minus(e);
		}
	}

	@Override
	public final POrderedSet<E> minusAll(Collection<?> list) {
		checkUnmodified();
		return immute(-list.size()).minusAll(list);
	}

	@Override
	public final String toString() {
		if (isEmpty()) {
			return "{}";
		}
		StringBuilder sb = new StringBuilder();
		String delim = "{";
		for (E e : lhs) {
			sb.append(delim).append(e);
			delim = ", ";
		}
		return sb.append("}").toString();
	}

	@Override
	@Deprecated
	public boolean add(E o) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void clear() {
		throw new UnsupportedOperationException();
	}
}
