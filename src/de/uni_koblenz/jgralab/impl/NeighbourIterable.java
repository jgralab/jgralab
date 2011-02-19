/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class provides an Iterable for the Vertices adjacent to a given vertex.
 * 
 * @author ist@uni-koblenz.de
 */
public class NeighbourIterable<E extends Edge, V extends Vertex> implements
		Iterable<V> {
	/**
	 * Creates an Iterable for all neighbours adjacent to <code>v</code>.
	 * 
	 * @param v
	 *            a Vertex
	 */
	public NeighbourIterable(Vertex v) {
		this(v, null, EdgeDirection.INOUT);
	}

	/**
	 * Creates an Iterable for all neighbours adjacent to <code>v</code> via
	 * edges of the specified edgeclass <code>ec</code>.
	 * 
	 * @param v
	 *            a Vertex
	 * @param ec
	 *            restricts edges to that class or subclasses
	 */
	public NeighbourIterable(Vertex v, Class<? extends Edge> ec) {
		this(v, ec, EdgeDirection.INOUT);
	}

	/**
	 * Creates an Iterable for all neighbours adjacent to <code>v</code> via
	 * edges of the specified <code>orientation</code>.
	 * 
	 * @param v
	 *            a Vertex
	 * @param orientation
	 *            desired orientation
	 */
	public NeighbourIterable(Vertex v, EdgeDirection dir) {
		this(v, null, dir);
	}

	/**
	 * Creates an Iterable for all neighbours adjacent to <code>v</code> via
	 * edges of the specified edgeclass <code>ec</code> and
	 * <code>orientation</code>.
	 * 
	 * @param v
	 *            a Vertex
	 * @param ec
	 *            restricts edges to that class or subclasses
	 * @param orientation
	 *            desired orientation
	 */
	public NeighbourIterable(Vertex v, Class<? extends Edge> ec,
			EdgeDirection dir) {
		assert v != null && v.isValid();
		Iterable<E> it = new IncidenceIterable<E>(v, ec, dir);
		neighbourIterator = new NeigbourIterator(it.iterator());
	}

	class NeigbourIterator implements Iterator<V> {
		Iterator<E> incidenceIterator;

		public NeigbourIterator(Iterator<E> i) {
			incidenceIterator = i;
		}

		@Override
		public boolean hasNext() {
			return incidenceIterator.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		public V next() {
			return (V) incidenceIterator.next().getThat();
		}

		@Override
		public void remove() {
			incidenceIterator.remove();
		}
	}

	private Iterator<V> neighbourIterator;

	@Override
	public Iterator<V> iterator() {
		return neighbourIterator;
	}
}
