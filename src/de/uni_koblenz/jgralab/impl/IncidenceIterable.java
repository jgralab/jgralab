/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * This class provides an Iterable for the Edges incident to a vertex. Using the
 * vertex' different methods which return an instance of IncidenceIterable, one
 * may use an iterator or the advanced for loop of Java 5 to iterate over all
 * classes. If the list of incidence edges is changed during iteration, an exception
 * if thrown
 * 
 * @author dbildh
 */
public class IncidenceIterable<E extends Edge> implements Iterable<E> {

	class IncidenceIterator implements Iterator<E> {

		private boolean first = true;

		private boolean gotNext = true;

		protected E current = null;

		protected Vertex vertex = null;
		
		/**
		 * the version of the incidence list of the vertex
		 * at the beginning of the iteration. This information
		 * is used to check if the incidence list has changed,
		 * the failfast-iterator will then throw an exception
		 * the next time "next()" is called
		 */
		protected long incidenceListVersion;

		IncidenceIterator(Vertex v) {
			vertex = v;
			incidenceListVersion = v.getIncidenceListVersion();
		}

		public E next() {
			if (vertex.isIncidenceListModified(incidenceListVersion))
				throw new ConcurrentModificationException("The incidence list of the vertex has been modified - the iterator is not longer valid");
			gotNext = true;
			return current;
		}

		public boolean hasNext() {
			if (gotNext) {
				if (first) {
					current = getFirst();
					first = false;
				} else {
					current = getNext();
				}
				gotNext = false;
				return current != null;
			} else
				return true;
		}

		@SuppressWarnings("unchecked")
		protected E getNext() {
			return (E) current.getNextEdge();
		}

		@SuppressWarnings("unchecked")
		protected E getFirst() {
			return (E) vertex.getFirstEdge();
		}

		public void remove() {
			throw new GraphException("Cannot remove Edges using Iterator");
		}

	}

	class IncidenceIteratorEdgeDirection extends IncidenceIterator {

		EdgeDirection direction;

		public IncidenceIteratorEdgeDirection(Vertex v, EdgeDirection dir) {
			super(v);
			direction = dir;
		}

		@SuppressWarnings("unchecked")
		protected E getNext() {
			return (E)current.getNextEdge(direction);
		}

		@SuppressWarnings("unchecked")
		protected E getFirst() {
			return (E)vertex.getFirstEdge(direction);
		}

	}

	class IncidenceIteratorClassExplicit extends IncidenceIterator {

		Class<? extends Edge> ec;

		public IncidenceIteratorClassExplicit(Vertex v,
				Class<? extends Edge> c) {
			super(v);
			ec = c;
		}

		@SuppressWarnings("unchecked")
		protected E getNext() {
			return (E) current.getNextEdgeOfClass(ec);
		}

		@SuppressWarnings("unchecked")
		protected E getFirst() {
			return (E) vertex.getFirstEdgeOfClass(ec);
		}

	}


	class IncidenceIteratorClassDirection extends
			IncidenceIteratorClassExplicit {

		EdgeDirection direction;

		public IncidenceIteratorClassDirection(Vertex v,
				Class<? extends Edge> ec, EdgeDirection dir) {
			super(v, ec);
			direction = dir;
		}

		@SuppressWarnings("unchecked")
		protected E getNext() {
			return (E) current.getNextEdgeOfClass(ec, direction);
		}

		@SuppressWarnings("unchecked")
		protected E getFirst() {
			return (E) vertex.getFirstEdgeOfClass(ec, direction);
		}

	}

	private IncidenceIterator iter = null;

	public IncidenceIterable(Vertex v) {
		iter = new IncidenceIterator(v);
	}

	public IncidenceIterable(Vertex v, EdgeDirection orientation) {
		iter = new IncidenceIteratorEdgeDirection(v, orientation);
	}

	public IncidenceIterable(Vertex v, EdgeClass ec) {
		iter = new IncidenceIteratorClassExplicit(v, ec.getM1Class());
	}

	public IncidenceIterable(Vertex v, Class<? extends Edge> ec) {
		iter = new IncidenceIteratorClassExplicit(v, ec);
	}
	
	public IncidenceIterable(Vertex v, EdgeClass ec, EdgeDirection orientation) {
		iter = new IncidenceIteratorClassDirection(v, ec.getM1Class(), orientation);
	}

	public IncidenceIterable(Vertex v, Class<? extends Edge> ec,
			EdgeDirection orientation) {
		iter = new IncidenceIteratorClassDirection(v, ec, orientation);
	}

	public Iterator<E> iterator() {
		return iter;
	}

}
