/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * This class provides an Iterable to iterate over edges in a graph. One may use
 * this class to use the advanced for-loop of Java 5. Instances of this class
 * should never, and this means <b>never</b> created manually but only using the
 * methods <code>edges(params)</code> of th graph. Every special graphclass
 * contains generated methods similar to <code>edges(params)</code> for every
 * EdgeClass that is part of the GraphClass.
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <E>
 *            The type of the Edges to iterate over. To mention it again,
 *            <b>don't</b> create instances of this class directly.
 */
public class EdgeIterable<E extends Edge> implements Iterable<E> {

	class EdgeIterator implements Iterator<E> {

		private boolean first = true;

		private boolean gotNext = true;

		protected E current = null;

		protected Graph graph = null;

		/**
		 * the version of the edge list of the graph at the beginning of the
		 * iteration. This information is used to check if the edge list has
		 * changed, the failfast-iterator will then throw an exception the next
		 * time "next()" is called
		 */
		protected long edgeListVersion;

		EdgeIterator(Graph g) {
			graph = g;
			edgeListVersion = g.getEdgeListVersion();
		}

		public E next() {
			if (graph.isEdgeListModified(edgeListVersion))
				throw new ConcurrentModificationException(
						"The edge list of the graph has been modified - the iterator is not longer valid");
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
			return (E) current.getNextEdgeInGraph();
		}

		@SuppressWarnings("unchecked")
		protected E getFirst() {
			return (E) graph.getFirstEdgeInGraph();
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"It is not allowed to remove edges during iteration.");
		}

	}

	class EdgeIteratorEdgeClassExplicit extends EdgeIterator {

		boolean type;

		EdgeClass ec;

		public EdgeIteratorEdgeClassExplicit(Graph g, EdgeClass c, boolean type) {
			super(g);
			this.type = type;
			ec = c;
		}

		@SuppressWarnings("unchecked")
		protected E getNext() {
			return (E) current.getNextEdgeOfClassInGraph(ec, type);
		}

		@SuppressWarnings("unchecked")
		protected E getFirst() {
			return (E) graph.getFirstEdgeOfClassInGraph(ec, type);
		}

	}

	class EdgeIteratorClassExplicit extends EdgeIterator {

		boolean type;

		Class<? extends Edge> ec;

		public EdgeIteratorClassExplicit(Graph g, Class<? extends Edge> c,
				boolean type) {
			super(g);
			this.type = type;
			ec = c;
		}

		@SuppressWarnings("unchecked")
		protected E getNext() {
			return (E) current.getNextEdgeOfClassInGraph(ec, type);
		}

		@SuppressWarnings("unchecked")
		protected E getFirst() {
			return (E) graph.getFirstEdgeOfClassInGraph(ec, type);
		}

	}

	private EdgeIterator iter;

	public EdgeIterable(Graph g) {
		iter = new EdgeIterator(g);
	}

	public EdgeIterable(Graph g, Class<? extends Edge> ec) {
		iter = new EdgeIteratorClassExplicit(g, ec, false);
	}

	public EdgeIterable(Graph g, Class<? extends Edge> ec, boolean noSubclasses) {
		iter = new EdgeIteratorClassExplicit(g, ec, noSubclasses);
	}

	public Iterator<E> iterator() {
		return iter;
	}
}
