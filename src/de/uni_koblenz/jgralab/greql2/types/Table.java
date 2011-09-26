package de.uni_koblenz.jgralab.greql2.types;

import java.util.Collection;
import java.util.Iterator;

import org.pcollections.PCollection;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;

public class Table<T> implements PCollection<T> {
	private PVector<String> titles;
	private PVector<T> entries;

	private static Table<?> empty = new Table<Object>();

	private Table() {
		titles = JGraLab.vector();
		entries = JGraLab.vector();
	}

	private Table(PVector<String> t, PVector<T> e) {
		titles = t;
		entries = e;
	}

	@SuppressWarnings("unchecked")
	public static <E> Table<E> empty() {
		return (Table<E>) empty;
	}

	public PVector<T> toPVector() {
		return entries;
	}

	public PVector<String> getTitles() {
		return titles;
	}

	public Table<T> withTitles(PVector<String> t) {
		return new Table<T>(t, entries);
	}

	public T get(int index) {
		return entries.get(index);
	}

	@Override
	public Table<T> plus(T e) {
		return new Table<T>(titles, entries.plus(e));
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
	public Iterator<T> iterator() {
		return entries.iterator();
	}

	@Override
	public Object[] toArray() {
		return entries.toArray();
	}

	@Override
	public <E> E[] toArray(E[] a) {
		return entries.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return entries.containsAll(c);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public boolean add(T arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public boolean addAll(Collection<? extends T> arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PCollection<T> minus(Object arg0) {
		return new Table<T>(titles, entries.minus(arg0));
	}

	@Override
	public PCollection<T> minusAll(Collection<?> arg0) {
		return new Table<T>(titles, entries.minusAll(arg0));
	}

	@Override
	public Table<T> plusAll(Collection<? extends T> arg0) {
		return new Table<T>(titles, entries.plusAll(arg0));
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
		if (obj == null || !(obj instanceof Table)) {
			return false;
		}
		Table<?> o = (Table<?>) obj;
		return entries.equals(o.entries) && titles.equals(o.titles);
	}

	@Override
	public int hashCode() {
		return entries.hashCode() + titles.hashCode();
	}
}
