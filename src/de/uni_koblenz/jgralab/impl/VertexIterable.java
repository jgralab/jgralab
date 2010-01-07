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

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class provides an Iterable to iterate over vertices in a graph. One may
 * use this class to use the advanced for-loop of Java 5. Instances of this
 * class should never, and this means <b>never</b> created manually but only
 * using the methods <code>vertices(params)</code> of th graph. Every special
 * graphclass contains generated methods similar to
 * <code>vertices(params)</code> for every VertexClass that is part of the
 * GraphClass.
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <V>
 *            The type of the vertices to iterate over. To mention it again,
 *            <b>don't</b> create instances of this class directly.
 */
public class VertexIterable<V extends Vertex> implements Iterable<V> {

	/**
	 * This Iterator iterates over all vertices in a graph
	 * 
	 * @author ist@uni-koblenz.de
	 * 
	 */
	class VertexIterator implements Iterator<V> {

		/**
		 * the vertex that hasNext() retrieved and that a call of next() will
		 * return
		 */
		protected V current = null;

		/**
		 * the graph this iterator works on
		 */
		protected Graph graph = null;

		/**
		 * the version of the vertex list of the graph at the beginning of the
		 * iteration. This information is used to check if the vertex list has
		 * changed, the failfast-iterator will then throw an exception the next
		 * time "next()" is called
		 */
		protected long vertexListVersion;

		/**
		 * creates a new VertexIterator for the given graph
		 * 
		 * @param g
		 *            the graph to work on
		 */
		VertexIterator(Graph g) {
			graph = g;
			vertexListVersion = g.getVertexListVersion();
			current = getFirst();
		}

		protected VertexIterator() {
		};

		/**
		 * @return the next vertex in the graph which mathes the conditions of
		 *         this iterator
		 */
		public V next() {
			if (graph.isVertexListModified(vertexListVersion)) {
				throw new ConcurrentModificationException(
						"The vertex list of the graph has been modified - the iterator is not longer valid");
			}
			V v = current;
			if (current != null) {
				current = getNext();
			}
			return v;
		}

		/**
		 * @return true iff there is at least one next vertex to retrieve
		 */
		public boolean hasNext() {
			return current != null;
		}

		/**
		 * only for internal use, returns the next vertex according to the
		 * conditions of this iterator, should be overwritten by superclasses so
		 * the basic algorithm of <code>next()</code> must not be re-implemented
		 */
		@SuppressWarnings("unchecked")
		protected V getNext() {
			return (V) current.getNextVertex();
		}

		/**
		 * only for internal use, returns the first vertex according to the
		 * conditions of this iterator, should be overwritten by superclasses so
		 * the basic algorithm of <code>next()</code> must not be re-implemented
		 */
		@SuppressWarnings("unchecked")
		protected V getFirst() {
			return (V) graph.getFirstVertex();
		}

		/**
		 * Using the VertexIterator, it is <b>not</b> possible to remove
		 * vertices from a graph neither the iterator will recognize such a
		 * removal.
		 * 
		 * @throw UnsupportedOperationException every time the method is called
		 */
		public void remove() {
			throw new UnsupportedOperationException(
					"It is not allowed to remove vertices during iteration.");
		}

	}

	class VertexIteratorClass extends VertexIterator {
		Class<? extends Vertex> ec;

		public VertexIteratorClass(Graph g, Class<? extends Vertex> c) {
			graph = g;
			vertexListVersion = g.getVertexListVersion();
			ec = c;
			current = getFirst();
		}

		@Override
		@SuppressWarnings("unchecked")
		protected V getNext() {
			return (V) current.getNextVertexOfClass(ec);
		}

		@Override
		@SuppressWarnings("unchecked")
		protected V getFirst() {
			return (V) graph.getFirstVertexOfClass(ec);
		}

	}

	private VertexIterator iter;

	public VertexIterable(Graph g) {
		iter = new VertexIterator(g);
	}

	public VertexIterable(Graph g, Class<? extends Vertex> ec) {
		iter = new VertexIteratorClass(g, ec);
	}

	public Iterator<V> iterator() {
		return iter;
	}

}
