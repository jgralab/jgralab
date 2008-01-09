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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.EdgeVertexPair;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class provides an Iterable for the Edges incident to a vertex. 
 * Using the vertex' different methods which return an instance of
 * IncidenceIterable, one may use an iterator or the advanced for
 * loop of Java 5 to iterate over all classes.
 * In contrast to most other iterators, this iterators provides NO
 * functionality to remove edges during iteration, the iterator
 * neither supports the removel nor recognizes it. So DON'T change
 * anything at the edge sequence at a vertex during using an iterator. 
 * @author dbildh
 *
 * @param <E>
 * @param <V>
 */
public class IncidenceIterable<E extends Edge, V extends Vertex> implements Iterable<EdgeVertexPair<? extends E, ? extends V>> {
	
	class IncidenceIterator implements Iterator<EdgeVertexPair<? extends E, ? extends V>> {

		private boolean first = true;
		
		private boolean gotNext = true;
		
		protected EdgeVertexPair<? extends E, V> current = null;;
		
		protected Vertex vertex = null;

		IncidenceIterator(Vertex v) {
			vertex = v;
		}
			
		public EdgeVertexPair<? extends E, V> next() {
			gotNext = true; 
			return current;
		}
		
		@SuppressWarnings("unchecked")
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
		protected EdgeVertexPair<? extends E, V> getNext() {
			Edge e = current.getEdge().getNextEdge();
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		protected EdgeVertexPair<? extends E, V> getFirst() {
			Edge e = vertex.getFirstEdge();
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
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
		protected EdgeVertexPair<? extends E, V> getNext() {
			Edge e = current.getEdge().getNextEdge(direction);
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		protected EdgeVertexPair<? extends E, V> getFirst() {
			Edge e = vertex.getFirstEdge(direction);
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
		}
		
	}
	
		
	class IncidenceIteratorEdgeClassExplicit extends IncidenceIterator {
		
		boolean type;
		
		EdgeClass ec;
			
		public IncidenceIteratorEdgeClassExplicit(Vertex v, EdgeClass c, boolean type) {
			super(v);
			this.type = type;
			ec = c;
		}
		
		@SuppressWarnings("unchecked")
		protected EdgeVertexPair<? extends E, V> getNext() {
			Edge e = current.getEdge().getNextEdgeOfClass(ec, type);
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		protected EdgeVertexPair<? extends E, V> getFirst() {
			Edge e = vertex.getFirstEdgeOfClass(ec, type);
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
		}
		
	}
	
	class IncidenceIteratorClassExplicit extends IncidenceIterator {
		
		boolean type;
		
		Class<? extends Edge> ec;
		
		public IncidenceIteratorClassExplicit(Vertex v, Class<? extends Edge> c, boolean type) {
			super(v);
			this.type = type;
			ec = c;
		}
		
		@SuppressWarnings("unchecked")
		protected EdgeVertexPair<? extends E, V> getNext() {
			Edge e = current.getEdge().getNextEdgeOfClass(ec, type);
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		protected EdgeVertexPair<? extends E, V> getFirst() {
			Edge e = vertex.getFirstEdgeOfClass(ec, type);
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
		}
		
	}
	
	
	class IncidenceIteratorEdgeClassDirection extends IncidenceIteratorEdgeClassExplicit {
		
		EdgeDirection direction;
		
		public IncidenceIteratorEdgeClassDirection(Vertex v, EdgeClass ec, EdgeDirection dir, boolean explicit) {
			super(v, ec, explicit);
			direction = dir;
		}
			
		@SuppressWarnings("unchecked")
		protected EdgeVertexPair<? extends E, V> getNext() {
			Edge e = current.getEdge().getNextEdgeOfClass(ec, direction, type);
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		protected EdgeVertexPair<? extends E, V> getFirst() {
			Edge e = vertex.getFirstEdgeOfClass(ec, direction, type);
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
		}
		
		
		
	}
	
	class IncidenceIteratorClassDirection extends IncidenceIteratorClassExplicit {
		
		EdgeDirection direction;
		
		public IncidenceIteratorClassDirection(Vertex v, Class<? extends Edge> ec, EdgeDirection dir, boolean explicit) {
			super(v, ec, explicit);
			direction = dir;
		}
		
		@SuppressWarnings("unchecked")
		protected EdgeVertexPair<? extends E, V> getNext() {
			Edge e = current.getEdge().getNextEdgeOfClass(ec, direction, type);
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		protected EdgeVertexPair<? extends E, V> getFirst() {
			Edge e = vertex.getFirstEdgeOfClass(ec, direction, type);
			if (e != null) {
				return new EdgeVertexPair<E, V>((E) e, (V) e.getThat());
			}
			return null;
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
		iter = new IncidenceIteratorEdgeClassExplicit(v, ec, false);
	}

	public IncidenceIterable(Vertex v, Class<? extends Edge> ec) {
		iter = new IncidenceIteratorClassExplicit(v, ec, false);
	}
	
	public IncidenceIterable(Vertex v, EdgeClass ec, boolean explicitType) {
		iter = new IncidenceIteratorEdgeClassExplicit(v, ec, explicitType);
	}
	
	public IncidenceIterable(Vertex v, Class<? extends Edge> ec, boolean explicitType) {
		iter = new IncidenceIteratorClassExplicit(v, ec, explicitType);
	}

	public IncidenceIterable(Vertex v, EdgeClass ec, EdgeDirection orientation) {
		iter = new IncidenceIteratorEdgeClassDirection(v, ec, orientation, false);
	}
	
	public IncidenceIterable(Vertex v, Class<? extends Edge> ec, EdgeDirection orientation) {
		iter = new IncidenceIteratorClassDirection(v, ec, orientation, false);
	}
	
	public IncidenceIterable(Vertex v, EdgeClass ec, EdgeDirection orientation, boolean explicitType) {
		iter = new IncidenceIteratorEdgeClassDirection(v, ec, orientation, explicitType);
	}
	
	public IncidenceIterable(Vertex v, Class<? extends Edge> ec, EdgeDirection orientation, boolean explicitType) {
		iter = new IncidenceIteratorClassDirection(v, ec, orientation, explicitType);
	}

	
	public Iterator<EdgeVertexPair<? extends E, ? extends V>> iterator() {
		return iter;
	}
	
}
