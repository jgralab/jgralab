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

	@Override
	public String toString() {
		return titles.toString() + " " + entries.toString();
	}
}
