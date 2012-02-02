package org.pcollections;

import java.util.Collection;
import java.util.Iterator;

public class OrderedPSet<E> implements POrderedSet<E> {
	private static final OrderedPSet<Object> EMPTY = new OrderedPSet<Object>(
			Empty.set(), Empty.vector());

	@SuppressWarnings("unchecked")
	public static <E> OrderedPSet<E> empty() {
		return (OrderedPSet<E>) EMPTY;
	}

	@SuppressWarnings("unchecked")
	public static <E> PSet<E> from(final Collection<? extends E> list) {
		if (list instanceof OrderedPSet) {
			return (OrderedPSet<E>) list;
		}
		return OrderedPSet.<E> empty().plusAll(list);
	}

	public static <E> PSet<E> singleton(final E e) {
		return OrderedPSet.<E> empty().plus(e);
	}

	private PSet<E> contents;
	private PVector<E> order;
	private int hashCode = 0;

	private OrderedPSet(PSet<E> c, PVector<E> o) {
		contents = c;
		order = o;
	}

	@Override
	public int hashCode() {
		if ((hashCode == 0) && (size() > 0)) {
			for (E item : order) {
				hashCode += item.hashCode();
			}
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		return contents.equals(o);
	}

	@Override
	public PSet<E> plus(E e) {
		if (contents.contains(e)) {
			return this;
		}
		return new OrderedPSet<E>(contents.plus(e), order.plus(e));
	}

	@Override
	public PSet<E> plusAll(Collection<? extends E> list) {
		PSet<E> s = this;
		for (E e : list) {
			s = s.plus(e);
		}
		return s;
	}

	@Override
	public PSet<E> minus(Object e) {
		if (!contents.contains(e)) {
			return this;
		}
		return new OrderedPSet<E>(contents.minus(e), order.minus(e));
	}

	@Override
	public PSet<E> minusAll(Collection<?> list) {
		PSet<E> s = this;
		for (Object e : list) {
			s = s.minus(e);
		}
		return s;
	}

	@Override
	public Iterator<E> iterator() {
		return order.iterator();
	}

	@Override
	public int size() {
		return contents.size();
	}

	@Override
	public E get(int index) {
		return order.get(index);
	}

	@Override
	public int indexOf(Object o) {
		if (!contents.contains(o)) {
			return -1;
		}
		return order.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		return contents.contains(o);
	}

	@Override
	public Object[] toArray() {
		return order.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return order.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return contents.containsAll(c);
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
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (E item : order) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(item);
		}
		sb.append("}");
		return sb.toString();
	}
}
