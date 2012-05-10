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

package de.uni_koblenz.jgralab.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.VertexFilter;

/**
 * This class provides an Iterable for the Vertices adjacent to a given vertex.
 * 
 * @author ist@uni-koblenz.de
 */
public class NeighbourIterable<E extends Edge, V extends Vertex> implements
		Iterable<V> {
	private IncidenceIterable<E> it;
	private VertexFilter<V> filter;

	/**
	 * Creates an Iterable for all neighbours adjacent to <code>v</code> via
	 * edges of the specified edgeclass <code>ec</code> and
	 * <code>orientation</code>.
	 * 
	 * @param v
	 *            a Vertex
	 * @param ec
	 *            restricts edges to that class or subclasses
	 * @param dir
	 *            desired orientation
	 */
	public NeighbourIterable(Vertex v, Class<? extends Edge> ec,
			EdgeDirection dir, VertexFilter<V> filter) {
		assert v != null && v.isValid();
		this.filter = filter;
		it = new IncidenceIterable<E>(v, ec, dir);
	}

	class NeigbourIterator implements Iterator<V> {
		Iterator<E> incidenceIterator;
		V current;

		public NeigbourIterator(Iterator<E> i) {
			incidenceIterator = i;
			getNext();
		}

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public V next() {
			if (current == null) {
				throw new NoSuchElementException();
			}
			V result = current;
			getNext();
			return result;
		}

		@SuppressWarnings("unchecked")
		private void getNext() {
			while (incidenceIterator.hasNext()) {
				current = (V) incidenceIterator.next().getThat();
				if (filter == null || filter.accepts(current)) {
					return;
				}
			}
			current = null;
		}

		@Override
		public void remove() {
			incidenceIterator.remove();
		}
	}

	@Override
	public Iterator<V> iterator() {
		return new NeigbourIterator(it.iterator());
	}
}
