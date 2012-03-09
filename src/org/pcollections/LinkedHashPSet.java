package org.pcollections;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Wraps a {@link LinkedHashSet} and converts to some other {@link POrderedSet}
 * on "modification".
 *
 * CAUTION: Don't ever modify a {@link LinkedHashSet} which backs a
 * LinkedHashPSet! This will change the LinkedHashPSet as well.
 *
 * @param <E>
 */
public class LinkedHashPSet<E> implements POrderedSet<E> {

	private LinkedHashSet<E> lhs;

	private LinkedHashPSet(LinkedHashSet<E> l) {
		lhs = l;
	}

	/**
	 * @param lhs
	 * @return an immutable POrderedSet wrapping the given {@link LinkedHashSet}
	 */
	public static <T> LinkedHashPSet<T> immute(LinkedHashSet<T> lhs) {
		return new LinkedHashPSet<T>(lhs);
	}

	@Override
	public POrderedSet<E> plus(E e) {
		if (contains(e)) {
			return this;
		} else {
			return immute(1).plus(e);
		}
	}

	@Override
	public int size() {
		return lhs.size();
	}

	@Override
	public boolean isEmpty() {
		return lhs.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return lhs.contains(o);
	}

	private static class ImmutableIterator<E> implements Iterator<E> {
		private Iterator<E> it;

		ImmutableIterator(Iterator<E> i) {
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
	public Iterator<E> iterator() {
		return new ImmutableIterator<E>(lhs.iterator());
	}

	@Override
	public Object[] toArray() {
		return lhs.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return lhs.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return lhs.containsAll(c);
	}

	@Override
	public E get(int index) {
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
	public int indexOf(Object o) {
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
	public POrderedSet<E> plusAll(Collection<? extends E> list) {
		return immute(list.size()).plusAll(list);
	}

	private POrderedSet<E> immute(int maxAddCount) {
		POrderedSet<E> r = ((lhs.size() + maxAddCount) <= ArrayPSet.SIZELIMIT) ? ArrayPSet
				.<E> empty() : OrderedPSet.<E> empty();
		return r.plusAll(lhs);
	}

	@Override
	public POrderedSet<E> minus(Object e) {
		if (!contains(e)) {
			return this;
		} else {
			return immute(-1).minus(e);
		}
	}

	@Override
	public POrderedSet<E> minusAll(Collection<?> list) {
		return immute(-list.size()).minusAll(list);
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
}
