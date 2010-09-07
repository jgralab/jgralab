/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.impl;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;

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
		this(v, null, EdgeDirection.INOUT);
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
		this(v, null, orientation);
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
		iter = new IncidenceIterator(v, ec, orientation);
	}

	class IncidenceIterator implements Iterator<E> {
		protected E current = null;

		protected Vertex vertex = null;

		protected Class<? extends Edge> ec;

		protected EdgeDirection dir;

		/**
		 * the version of the incidence list of the vertex at the beginning of
		 * the iteration. This information is used to check if the incidence
		 * list has changed, the failfast-iterator will then throw an exception
		 * the next time "next()" is called
		 */
		protected long incidenceListVersion;

		@SuppressWarnings("unchecked")
		public IncidenceIterator(Vertex vertex, Class<? extends Edge> ec,
				EdgeDirection dir) {
			this.vertex = vertex;
			this.ec = ec;
			this.dir = dir;
			incidenceListVersion = vertex.getIncidenceListVersion();
			current = (E) ((ec == null) ? vertex.getFirstEdge(dir) : vertex
					.getFirstEdgeOfClass(ec, dir));
		}

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
			current = (E) ((ec == null) ? current.getNextEdge(dir) : current
					.getNextEdgeOfClass(ec, dir));
			return result;
		}

		public boolean hasNext() {
			if (vertex.isIncidenceListModified(incidenceListVersion)) {
				throw new ConcurrentModificationException(
						"The incidence list of the vertex has been modified - the iterator is not longer valid");
			}
			return current != null;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"Cannot remove Edges using Iterator");
		}

	}

	private IncidenceIterator iter = null;

	public Iterator<E> iterator() {
		return iter;
	}
}
