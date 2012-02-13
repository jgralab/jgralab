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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * This class provides an Iterable for the Edges incident to a given vertex.
 *
 * @author ist@uni-koblenz.de
 */
public class IncidenceIterable<E extends Edge> implements Iterable<E> {
	/**
	 * Creates an Iterable for all incident edges of Vertex <code>v</code>.
	 *
	 * @param v
	 *            a Vertex
	 */
	public IncidenceIterable(Vertex v) {
		this(v, (EdgeClass) null, EdgeDirection.INOUT);
	}

	/**
	 * Creates an Iterable for all incident edges of Vertex <code>v</code> with
	 * the specified <code>orientation</code>.
	 *
	 * @param v
	 *            a Vertex
	 * @param orientation
	 *            desired orientation
	 */
	public IncidenceIterable(Vertex v, EdgeDirection orientation) {
		this(v, (EdgeClass) null, orientation);
	}

	/**
	 * Creates an Iterable for all incident edges of Vertex <code>v</code> with
	 * the specified edgeclass <code>ec</code>.
	 *
	 * @param v
	 *            a Vertex
	 * @param ec
	 *            restricts edges to that class or subclasses
	 */
	public IncidenceIterable(Vertex v, Class<? extends Edge> ec) {
		this(v, ec, EdgeDirection.INOUT);
	}

	public IncidenceIterable(Vertex v, EdgeClass ec) {
		this(v, ec, EdgeDirection.INOUT);
	}

	/**
	 * Creates an Iterable for all incident edges of Vertex <code>v</code> with
	 * the specified edgeclass <code>ec</code> and <code>orientation</code>.
	 *
	 * @param v
	 *            a Vertex
	 * @param ec
	 *            restricts edges to that class or subclasses
	 * @param orientation
	 *            desired orientation
	 */
	public IncidenceIterable(Vertex v, Class<? extends Edge> ec,
			EdgeDirection orientation) {
		assert v != null && v.isValid();
		iter = new IncidenceIterator((InternalVertex) v, ec, orientation);
	}

	public IncidenceIterable(Vertex v, EdgeClass ec, EdgeDirection orientation) {
		assert v != null && v.isValid();
		iter = new IncidenceIterator((InternalVertex) v, ec, orientation);
	}

	class IncidenceIterator implements Iterator<E> {
		protected E current = null;

		protected InternalVertex vertex = null;

		protected Class<? extends Edge> ec;

		protected EdgeClass schemaEc;

		protected EdgeDirection dir;

		/**
		 * the version of the incidence list of the vertex at the beginning of
		 * the iteration. This information is used to check if the incidence
		 * list has changed, the failfast-iterator will then throw an exception
		 * the next time "next()" is called
		 */
		protected long incidenceListVersion;

		@SuppressWarnings("unchecked")
		public IncidenceIterator(InternalVertex vertex,
				Class<? extends Edge> ec, EdgeDirection dir) {
			this.vertex = vertex;
			this.ec = ec;
			this.dir = dir;
			incidenceListVersion = vertex.getIncidenceListVersion();
			current = (E) ((ec == null) ? vertex.getFirstIncidence(dir)
					: vertex.getFirstIncidence(ec, dir));
		}

		@SuppressWarnings("unchecked")
		public IncidenceIterator(InternalVertex vertex, EdgeClass ec,
				EdgeDirection dir) {
			this.vertex = vertex;
			this.schemaEc = ec;
			this.dir = dir;
			incidenceListVersion = vertex.getIncidenceListVersion();
			current = (E) ((ec == null) ? vertex.getFirstIncidence(dir)
					: vertex.getFirstIncidence(ec, dir));
		}

		@Override
		@SuppressWarnings("unchecked")
		public E next() {
			if (vertex.isIncidenceListModified(incidenceListVersion)) {
				throw new ConcurrentModificationException(
						"The incidence list of the vertex has been modified - the iterator is not longer valid");
			}
			if (current == null) {
				throw new NoSuchElementException();
			}
			E result = current;
			current = (E) (ec == null && schemaEc == null ? current
					.getNextIncidence(dir) : schemaEc == null ? current.getNextIncidence(ec, dir) : current.getNextIncidence(schemaEc, dir));
			return result;
		}

		@Override
		public boolean hasNext() {
			if (vertex.isIncidenceListModified(incidenceListVersion)) {
				throw new ConcurrentModificationException(
						"The incidence list of the vertex has been modified - the iterator is not longer valid");
			}
			return current != null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Cannot remove Edges using Iterator");
		}

	}

	private IncidenceIterator iter = null;

	@Override
	public Iterator<E> iterator() {
		return iter;
	}
}
