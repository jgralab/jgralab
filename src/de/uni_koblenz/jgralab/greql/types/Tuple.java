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
package de.uni_koblenz.jgralab.greql.types;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import org.pcollections.PSequence;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;

@SuppressWarnings("deprecation")
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
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("(");
		for (Object o : entries) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(o);
		}
		sb.append(")");
		return sb.toString();
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

	@Override
	public boolean add(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Object> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
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

	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof Tuple)) {
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

	@Override
	public boolean addAll(int index, Collection<? extends Object> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}
}
