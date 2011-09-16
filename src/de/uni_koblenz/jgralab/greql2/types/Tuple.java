package de.uni_koblenz.jgralab.greql2.types;

import java.util.Collection;
import java.util.Iterator;

import org.pcollections.ArrayPVector;
import org.pcollections.PCollection;
import org.pcollections.PVector;

public class Tuple implements PCollection<Object> {
	private PVector<Object> entries;

	private static Tuple empty = new Tuple();

	private Tuple() {
		entries = ArrayPVector.empty();
	}

	private Tuple(PVector<Object> e) {
		entries = e;
	}

	public static Tuple empty() {
		return empty;
	}

	public PVector<Object> toPVector() {
		return entries;
	}

	public Object get(int index) {
		return entries.get(index);
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
	public boolean contains(Object o) {
		return entries.contains(o);
	}

	@Override
	public Iterator<Object> iterator() {
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

	@Override
	public boolean containsAll(Collection<?> c) {
		return entries.containsAll(c);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public boolean add(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public boolean addAll(Collection<? extends Object> arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Tuple minus(Object arg0) {
		return new Tuple(entries.minus(arg0));
	}

	@Override
	public Tuple minusAll(Collection<?> arg0) {
		return new Tuple(entries.minusAll(arg0));
	}

	@Override
	public Tuple plus(Object arg0) {
		return new Tuple(entries.plus(arg0));
	}

	@Override
	public Tuple plusAll(Collection<? extends Object> arg0) {
		return new Tuple(entries.plusAll(arg0));
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Tuple)) {
			return false;
		}
		Tuple o = (Tuple) obj;
		return entries.equals(o.entries);
	}

	@Override
	public int hashCode() {
		return entries.hashCode();
	}
}
