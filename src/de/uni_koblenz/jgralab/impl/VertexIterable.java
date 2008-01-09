/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import java.util.Iterator;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.VertexClass;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;

/**
 * This class provides an Iterable to iterate over vertices in a graph. One may use this class
 * to use the advanced for-loop of Java 5. 
 * Instances of this class should never, and this means <b>never</b> created manually but only
 * using the methods <code>vertices(params)</code> of th graph.
 * Every special graphclass contains generated methods similar to <code>vertices(params)</code>
 * for every VertexClass that is part of the GraphClass.
 *   
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de>
 *
 * @param <V> The type of the vertices to iterate over. To mention it again, <b>don't</b> create
 * instances of this class directly.
 */
public class VertexIterable<V extends Vertex> implements Iterable<V> {
	
	/**
	 * This Iterator iterates over all vertices in a graph
	 * @author dbildh
	 *
	 */
	class VertexIterator implements Iterator<V> {

		/**
		 * toggles wether the first elemet has to be touched
		 */
		private boolean first = true;
		
		/**
		 * toggles if the next current next element was already returned, 
		 * so hasNext() will retrieve the next one and stores it in field
		 * current
		 */
		private boolean gotNext = true;
		
		/**
		 * the vertex that hasNext() retrieved and that a call of 
		 * next() will return
		 */
		protected V current = null;;
		
		/**
		 * the graph this iterator works on
		 */
		protected Graph graph = null;

		/**
		 * creates a new VertexIterator for the given graph
		 * @param g the graph to work on
		 */
		VertexIterator(Graph g) {
			graph = g;
		}
		
		/**
		 * @return the next vertex in the graph which mathes the conditions of this iterator
		 */
		public V next() {
			gotNext = true; 
			return current;
		}
		
		/**
		 * @return  true iff there is at least one next vertex to retrieve
		 */
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
		
		/**
		 * only for internal use, returns the next vertex according to the 
		 * conditions of this iterator, should be overwritten by superclasses
		 * so the basic algorithm of <code>next()</code> must not be re-implemented 
		 */
		@SuppressWarnings("unchecked")
		protected V getNext() {
			return (V) current.getNextVertex();
		}
		
		/**
		 * only for internal use, returns the first vertex according to the 
		 * conditions of this iterator, should be overwritten by superclasses
		 * so the basic algorithm of <code>next()</code> must not be re-implemented 
		 */
		@SuppressWarnings("unchecked")
		protected V getFirst() {
			return (V) graph.getFirstVertex();
		}
		
		/**
		 * Using the VertexIterator, it is <b>not</b> possible to remove vertices
		 * from a graph neither the iterator will recognize such a removal. So
		 * don't remove or re-order vertices (or edges) during the usage of an
		 * iterator (or a advanced for-loop). 
		 * @throw GraphException every time the method is called
		 */
		public void remove() {
			throw new GraphException("It is not allowed to remove vertices during iteration");
		}
		
	}
	
		
	class VertexIteratorVertexClassExplicit extends VertexIterator {
		
		boolean type;
		
		VertexClass ec;
			
		public VertexIteratorVertexClassExplicit(Graph g, VertexClass c, boolean type) {
			super(g);
			this.type = type;
			ec = c;
		}
		
		@SuppressWarnings("unchecked")
		protected V getNext() {
			return (V) current.getNextVertexOfClass(ec, type);
		}
		
		@SuppressWarnings("unchecked")
		protected V getFirst() {
			return (V) graph.getFirstVertexOfClass(ec, type);
		}
		
	}
	
	class VertexIteratorClassExplicit extends VertexIterator {
		
		boolean type;
		
		Class<? extends Vertex> ec;
			
		public VertexIteratorClassExplicit(Graph g, Class<? extends Vertex> c, boolean type) {
			super(g);
			this.type = type;
			ec = c;
		}
		
		@SuppressWarnings("unchecked")
		protected V getNext() {
			return (V) current.getNextVertexOfClass(ec, type);
		}
		
		@SuppressWarnings("unchecked")
		protected V getFirst() {
			return (V) graph.getFirstVertexOfClass(ec, type);
		}
		
	}
	
	

	private VertexIterator iter = null;
	
	public VertexIterable(Graph g) {
		iter = new VertexIterator(g);
	}
	
	public VertexIterable(Graph g, VertexClass ec) {
		iter = new VertexIteratorVertexClassExplicit(g, ec, false);
	}

	public VertexIterable(Graph g, Class<? extends Vertex> ec) {
		iter = new VertexIteratorClassExplicit(g, ec, false);
	}
	
	public VertexIterable(Graph g, VertexClass ec, boolean explicitType) {
		iter = new VertexIteratorVertexClassExplicit(g, ec, explicitType);
	}
	
	public VertexIterable(Graph g, Class<? extends Vertex> ec, boolean explicitType) {
		iter = new VertexIteratorClassExplicit(g, ec, explicitType);
	}

		
	public Iterator<V> iterator() {
		return iter;
	}
	
}
