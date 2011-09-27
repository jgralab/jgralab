package de.uni_koblenz.jgralab.greql2.types;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import org.pcollections.PSequence;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;

public class Tuple implements PSequence<Object> {
	private PVector<Object> entries;

	private static Tuple empty = new Tuple();

	private Tuple() {
		entries = JGraLab.vector();
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

	@Override
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

	@Override
	public int indexOf(Object o) {
		return entries.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return entries.lastIndexOf(o);
	}

	@Override
	public ListIterator<Object> listIterator() {
		return entries.listIterator();
	}

	@Override
	public ListIterator<Object> listIterator(int index) {
		return entries.listIterator(index);
	}

	@Override
	public PSequence<Object> with(int i, Object e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PSequence<Object> plus(int i, Object e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PSequence<Object> plusAll(int i, Collection<? extends Object> list) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PSequence<Object> minus(int i) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PSequence<Object> subList(int start, int end) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public boolean addAll(int index, Collection<? extends Object> c) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public void add(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}
}
