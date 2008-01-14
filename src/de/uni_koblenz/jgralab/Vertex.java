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
 
package de.uni_koblenz.jgralab;



/**
 * represents a vertex, m1 classes inherit from this class
 * 
 * @author Steffen Kahle
 * 
 */
public interface Vertex extends GraphElement {

	/**
	 * @return the id of the vertex
	 */
	public int getId();

	/**
	 * @return the number of connected incidences to the vertex
	 */
	public int getDegree();

	/**
	 * @param orientation
	 *            of connected incidences,
	 * @return number of IN or OUT incidences connected to the vertex
	 */
	public int getDegree(EdgeDirection orientation);

	/**
	 * @param ec
	 *            an EdgeClass
	 * @return number of IN or OUT incidences of the specified EdgeClass
	 */
	public int getDegree(EdgeClass ec);

	
	/**
	 * @param ec
	 *            an EdgeClass
	 * @return number of IN or OUT incidences of the specified EdgeClass
	 */
	public int getDegree(Class<? extends Edge> ec);
	
	
	/**
	 * @param ec
	 *            an EdgeClass
	 * @param noSubClasses
	 *            if set to <code>true</code>, subclasses of <code>ec</code>
	 *            are not counted
	 * @return number of IN or OUT incidences of the specified EdgeClass
	 */
	public int getDegree(EdgeClass ec, boolean noSubClasses);
	
	/**
	 * @param ec
	 *            an EdgeClass
	 * @param noSubClasses
	 *            if set to <code>true</code>, subclasses of <code>ec</code>
	 *            are not counted
	 * @return number of IN or OUT incidences of the specified EdgeClass
	 */
	public int getDegree(Class<? extends Edge> ec, boolean noSubClasses);

	/**
	 * @param ec
	 *            an EdgeClass
	 * @param orientation
	 *            of connected incidences,
	 * @return number of IN or OUT incidences connected to the vertex
	 */
	public int getDegree(EdgeClass ec, EdgeDirection orientation);
	
	
	/**
	 * @param ec
	 *            an EdgeClass
	 * @param orientation
	 *            of connected incidences,
	 * @return number of IN or OUT incidences connected to the vertex
	 */
	public int getDegree(Class<? extends Edge> ec, EdgeDirection orientation);

	/**
	 * @param ec 
	 *            an EdgeClass
	 * @param orientation
	 *            of connected incidences,
	 * @param noSubClasses
	 *            if set to <code>true</code>, subclasses of <code>ec</code>
	 *            are not counted
	 * @return number of IN or OUT incidences connected to the vertex
	 */
	public int getDegree(EdgeClass ec, EdgeDirection orientation,
			boolean noSubClasses);
	
	/**
	 * @param ec
	 *            an EdgeClass
	 * @param orientation
	 *            of connected incidences,
	 * @param noSubClasses
	 *            if set to <code>true</code>, subclasses of <code>ec</code>
	 *            are not counted
	 * @return number of IN or OUT incidences connected to the vertex
	 */
	public int getDegree(Class<? extends Edge> ec, EdgeDirection orientation,
			boolean noSubClasses);

	/**
	 * @return the next vertex in vSeq after this vertex
	 */
	public Vertex getNextVertex();

	/**
	 * @param aVertexClass
	 *            the class of the next vertex
	 * @return the next vertex in vSeq of class aVertexClass or its superclasses
	 */
	public Vertex getNextVertexOfClass(VertexClass aVertexClass);

	/**
	 * @param aM1VertexClass
	 *            the class of the next vertex
	 * @return the next vertex in vSeq of class aVertexClass or its superclasses
	 */
	public Vertex getNextVertexOfClass(Class<? extends Vertex> aM1VertexClass);

	/**
	 * @param aVertexClass
	 *            the class of the next vertex
	 * @param explicitType
	 *            if true, no subclasses are returned
	 * @return the next vertex in vSeq of explicit class aVertexClass
	 */
	public Vertex getNextVertexOfClass(VertexClass aVertexClass,
			boolean explicitType);

	/**
	 * @param aM1VertexClass
	 *            the class of the next vertex
	 * @param explicitType
	 *            if true, no subclasses are returned
	 * @return the next vertex in vSeq of explicit class aVertexClass
	 */
	public Vertex getNextVertexOfClass(Class<? extends Vertex> aM1VertexClass,
			boolean explicitType);

	/**
	 * warning: slow in package 'incarray'
	 * 
	 * @return the previous vertex in vSeq before this vertex
	 */
	public Vertex getPrevVertex();

	/**
	 * @return first incidence object of the graph
	 */
	public Edge getFirstEdge();

	/**
	 * @param orientation
	 *            of connected incidences,
	 * @return the first incidence of vertex with direction IN or OUT
	 */
	public Edge getFirstEdge(EdgeDirection orientation);
	


	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass);
	
	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            of the edge
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation);
	
	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            of the edge
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param explicitType
	 *            if true, no subclasses are returned
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass, boolean explicitType);
	
	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param explicitType
	 *            if true, no subclasses are returned
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass, boolean explicitType);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            set to TRUE, if edge has the 'in'-orientation, set to FALSE,
	 *            if edge has the 'out'-orientation
	 * @param explicitType
	 *            if true, no subclasses are returned
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType);
	
	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            set to TRUE, if edge has the 'in'-orientation, set to FALSE,
	 *            if edge has the 'out'-orientation
	 * @param explicitType
	 *            if true, no subclasses are returned
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean explicitType);

	/**
	 * @param v
	 * @return true, if this vertex is before v in vSeq
	 */
	public boolean isBefore(Vertex v);

	/**
	 * @param v
	 * @return true, if this vertex is before v's id in vSeq
	 */
	public boolean isBefore(int v);

	/**
	 * puts this vertex before v in vSeq
	 * 
	 * @param v
	 */
	public void putBefore(Vertex v);

	/**
	 * puts this vertex before v's id in vSeq
	 * 
	 * @param v
	 */
	public void putBefore(int v);

	/**
	 * @param v
	 * @return true, if this vertex is after v in vSeq
	 */
	public boolean isAfter(Vertex v);

	/**
	 * @param v
	 * @return true, if this vertex is after v's id in vSeq
	 */
	public boolean isAfter(int v);

	/**
	 * puts this vertex after v in vSeq
	 * 
	 * @param v
	 */
	public void putAfter(Vertex v);

	/**
	 * puts this vertex after v's id in vSeq
	 * 
	 * @param v
	 */
	public void putAfter(int v);

	/**
	 * inserts this vertex at position pos in vSeq
	 * 
	 * @param pos
	 */
	public void insertAt(int pos);

	/**
	 * deletes the incidence object incident to eNo
	 * 
	 * @param eNo
	 *            the edge number to which the incidence is connected to
	 */
	void deleteIncidenceTo(int eNo);

	/**
	 * sets the next vertex field of this vertex in package 'oo' to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setNextVertex(Vertex v);

	/**
	 * sets the previous vertex field of this vertex in package 'oo' to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setPrevVertex(Vertex v);

	/**
	 * sets the id field of this vertex in package 'oo' to id
	 * 
	 * @param id
	 *            an id
	 */
	void setId(int id);

	/**
	 * sets the first incidence field of this vertex in package 'oo' to i
	 * 
	 * @param i
	 *            an incidence
	 */
	void setFirstEdge(Edge i);

	/**
	 * removes this vertex from vSeq and erases its attributes
	 */
	public void delete();

	/**
	 * puts the given edge <code>edge</code> before the given edge
	 * <code>nextEdge</code> in the incidence list. This does neither affect
	 * the global edge sequence eSeq nor the alpha or omega vertices, only the
	 * order of the edges at this vertex is changed
	 */
	void putEdgeBefore(Edge e, Edge n);

	/**
	 * puts the given edge <code>edge</code> after the given edge
	 * <code>previousEdge</code> in the incidence list. This does neither
	 * affect the global edge sequence eSeq nor the alpha or omega vertices,
	 * only the order of the edges at this vertex is changed
	 */
	void putEdgeAfter(Edge edge, Edge previousEdge);

	/**
	 * inserts the given edge <code>edge</code> at the given position
	 * <code>pos</code> in the incidence list. This does neither affect the
	 * global edge sequence eSeq nor the alpha or omega vertices, only the order
	 * of the edges at this vertex is changed
	 */
	void insertEdgeAt(Edge edge, int pos);
	
	
	/**
	 * Using this method, one can simply iterate over all incident edges of this vertex using the
	 * advanced for-loop
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences();
	
	/**
	 * Using this method, one can simply iterate over all incident edges of this vertex using the
	 * advanced for-loop
	 * @param dir the direction of the edges which should be iterated, either EdgeDirection.IN or EdgeDirection.OUT
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(EdgeDirection dir);
	
	/**
	 * Using this method, one can simply iterate over all incident edges of this vertex using the
	 * advanced for-loop
	 * @param eclass the EdgeClass of the edges which should be iterated
	 * @param dir the direction of the edges which should be iterated, either EdgeDirection.IN or EdgeDirection.OUT
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(EdgeClass eclass, EdgeDirection dir);
	
	/**
	 * Using this method, one can simply iterate over all incident edges of this vertex using the
	 * advanced for-loop
	 * @param eclass the M1-Class of the edges which should be iterated
	 * @param dir the direction of the edges which should be iterated, either EdgeDirection.IN or EdgeDirection.OUT
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(Class<? extends Edge> eclass, EdgeDirection dir);
	
	/**
	 * Using this method, one can simply iterate over all incident edges of this vertex using the
	 * advanced for-loop
	 * @param eclass the EdgeClass of the edges which should be iterated
	 * @param dir the direction of the edges which should be iterated, either EdgeDirection.IN or EdgeDirection.OUT
	 * @param explicitType if set to true, no subclasses of the given EdgeClass will be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(EdgeClass eclass, EdgeDirection dir, boolean explicitType);
	
	/**
	 * Using this method, one can simply iterate over all incident edges of this vertex using the
	 * advanced for-loop
	 * @param eclass the M1-Class of the edges which should be iterated
	 * @param dir the direction of the edges which should be iterated, either EdgeDirection.IN or EdgeDirection.OUT
	 * @param explicitType if set to true, no subclasses of the given EdgeClass will be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(Class<? extends Edge> eclass, EdgeDirection dir, boolean explicitType);
	
	/**
	 * Using this method, one can simply iterate over all incident edges of this vertex using the
	 * advanced for-loop
	 * @param eclass the EdgeClass of the edges which should be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(EdgeClass eclass);
	
	/**
	 * Using this method, one can simply iterate over all incident edges of this vertex using the
	 * advanced for-loop
	 * @param eclass the M1-Class of the edges which should be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(Class<? extends Edge> eclass);
	
	/**
	 * Using this method, one can simply iterate over all incident edges of this vertex using the
	 * advanced for-loop
	 * @param eclass the EdgeClass of the edges which should be iterated
	 * @param explicitType if set to true, no subclasses of the given EdgeClass will be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(EdgeClass eclass,  boolean explicitType);
	
	/**
	 * Using this method, one can simply iterate over all incident edges of this vertex using the
	 * advanced for-loop
	 * @param eclass the M1-Class of the edges which should be iterated
	 * @param explicitType if set to true, no subclasses of the given EdgeClass will be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(Class<? extends Edge> eclass, boolean explicitType);
	
	
	/**
	 * tests if the Edge <code>edge</code> may start at this vertex
	 * @return <code>true</code> iff <code>edge</code> may start at this vertex 
	 */
	public boolean isValidAlpha(Edge edge);
	
	
	/**
	 * tests if the Edge <code>edge</code> may end at this vertex
	 * @return <code>true</code> iff <code>edge</code> may end at this vertex 
	 */
	public boolean isValidOmega(Edge edge);
	
	
	public Composition getFirstComposition();

	public Composition getFirstComposition(EdgeDirection orientation);

	public Composition getFirstComposition(boolean noSubClasses);

	public Composition getFirstComposition(EdgeDirection orientation, boolean noSubClasses);
	
	
	public Aggregation getFirstAggregation();

	public Aggregation getFirstAggregation(EdgeDirection orientation);

	public Aggregation getFirstAggregation(boolean noSubClasses);

	public Aggregation getFirstAggregation(EdgeDirection orientation, boolean noSubClasses);
}