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

package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * Represents an m1 graph. The classes jgralab.impl.array.GraphImpl and
 * jgralab.impl.list.GraphImpl implement this interface.
 * 
 * @author Steffen Kahle
 * 
 */
public interface Graph extends AttributedElement {

	/**
	 * Creates a instance of the given class and adds this vertex to the graph
	 */
	public <T extends Vertex> T createVertex(Class<T> cls);

	/**
	 * Creates a instance of the given class and adds this edge to the graph
	 */
	public <T extends Edge> T createEdge(Class<T> cls, Vertex alpha,
			Vertex omega);

	/**
	 * This method checks, if the graph is currently being loaded
	 * 
	 * @return true if the graph is being loaded
	 */
	public boolean isLoading();

	/**
	 * This method is called as soon as the loading of a graph is completed One
	 * may use it to perform own operations as soon as the loading is completed
	 */
	public void loadingCompleted();

	/**
	 * Checks if the graph has changed with respect to the given
	 * <code>aGraphVersion</code>. Every change in the graph, e.g. adding,
	 * creating and reordering of edges and vertices or changes of attributes of
	 * the graph, an edge or a vertex are treated as a change.
	 * 
	 * @param aGraphVersion
	 *            The graphVersion to check against
	 * @return <code>true</code> if the internal graph version of the graph is
	 *         different from the given version <code>aGraphVersion</code>.
	 */
	public boolean isGraphModified(long aGraphVersion);

	/**
	 * Changes the graph version, should be called whenever the graph is
	 * changed, all changes like adding, creating and reordering of edges and
	 * vertices or changes of attributes of the graph, an edge or a vertex are
	 * treated as a change.
	 */
	public void graphModified();

	/**
	 * @return the internal graph version
	 * @see #graphModified()
	 * @see #isGraphModified(long)
	 */
	public long getGraphVersion();

	/**
	 * Checks if the graph-structure has changed with respect to the given
	 * <code>graphStructureVersion</code>. Changes in the graph structure are
	 * creation and deletion as well as reordering of vertices and edges, but
	 * not changes of attribute values.
	 * 
	 * @return <code>true</code> if the internal graph structure version of
	 *         the graph is different from the given version
	 *         <code>graphStructureVersion</code>.
	 */
	public boolean isVertexListModified(long vertexListVersion);

	/**
	 * @return the internal graph structure version
	 * @see #vertexListModified()
	 * @see #isVertexListModified(long)
	 */
	public long getVertexListVersion();

	/**
	 * Checks if the graph-structure has changed with respect to the given
	 * <code>graphStructureVersion</code>. Changes in the graph structure are
	 * creation and deletion as well as reordering of vertices and edges, but
	 * not changes of attribute values.
	 * 
	 * @return <code>true</code> if the internal graph structure version of
	 *         the graph is different from the given version
	 *         <code>graphStructureVersion</code>.
	 */
	public boolean isEdgeListModified(long edgeListVersion);

	/**
	 * @return the internal edge list version
	 * @see #edgeListModified()
	 * @see #isEdgeListModified(long)
	 */
	public long getEdgeListVersion();

	/**
	 * @return true iff this graph contains the given vertex
	 */
	public boolean containsVertex(Vertex v);

	/**
	 * @return true iff this graph contains the given edge
	 */
	boolean containsEdge(Edge e);

	/**
	 * removes the specified vertex from vSeq and erases its attributes
	 * 
	 * @param v
	 *            the id of the vertex to be deleted
	 */
	public void deleteVertex(Vertex v);

	/**
	 * Callback function for triggered actions just before a vertex is actually
	 * deleted.
	 * 
	 * @param v
	 *            the deleted vertex
	 */
	public void vertexDeleted(Vertex v);

	/**
	 * Callback function for triggered actions just after a vertex was added.
	 * 
	 * @param v
	 *            the deleted vertex
	 */
	public void vertexAdded(Vertex v);

	/**
	 * removes this edge from eSeq and erases its attributes
	 * 
	 * @param e
	 *            the edge to be deleted
	 * 
	 */
	public void deleteEdge(Edge e);

	/**
	 * Callback function for triggered actions just before an edge is actually
	 * deleted.
	 * 
	 * @param e
	 *            the deleted edge
	 */
	public void edgeDeleted(Edge e);

	/**
	 * Callback function for triggered actions just after an edge was added.
	 * 
	 * @param e
	 *            the deleted vertex
	 */
	public void edgeAdded(Edge e);

	/**
	 * @return the first vertex object of vSeq
	 */
	public Vertex getFirstVertex();

	public Vertex getLastVertex();

	/**
	 * @param aVertexClass
	 * @return the first vertex object of class aVertexClass in vSeq
	 */
	public Vertex getFirstVertexOfClass(VertexClass aVertexClass);

	/**
	 * @param aVertexClass
	 * @param noSubclasses
	 *            if set to true, only vertices which are explicitly of the
	 *            given edge class will be retrieved, otherwise also vertices of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the first vertex object of explicit class aVertexClass in vSeq
	 */
	public Vertex getFirstVertexOfClass(VertexClass aVertexClass,
			boolean noSubclasses);

	/**
	 * @param aVertexClass
	 * @return the first vertex object of class aVertexClass in vSeq
	 */
	public Vertex getFirstVertexOfClass(Class<? extends Vertex> aVertexClass);

	/**
	 * @param aVertexClass
	 * @param noSubclasses
	 *            if set to true, only vertices which are explicitly of the
	 *            given edge class will be retrieved, otherwise also vertices of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the first vertex object of explicit class aVertexClass in vSeq
	 */
	public Vertex getFirstVertexOfClass(Class<? extends Vertex> aVertexClass,
			boolean noSubclasses);

	/**
	 * @return first edge object of eSeq
	 */
	public Edge getFirstEdgeInGraph();

	public Edge getLastEdgeInGraph();

	/**
	 * @param anEdgeClass
	 * @return the first edge object of anEdgeClass in eSeq
	 */
	public Edge getFirstEdgeOfClassInGraph(EdgeClass anEdgeClass);

	/**
	 * @param anEdgeClass
	 * @return the first edge object of anEdgeClass in eSeq
	 */
	public Edge getFirstEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass);

	/**
	 * @param anEdgeClass
	 * @param noSubclasses
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the first edge object of explicit anEdgeClass in eSeq
	 */
	public Edge getFirstEdgeOfClassInGraph(EdgeClass anEdgeClass,
			boolean noSubclasses);

	/**
	 * @param anEdgeClass
	 * @param noSubclasses
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the first edge object of explicit anEdgeClass in eSeq
	 */
	public Edge getFirstEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass,
			boolean noSubclasses);

	/**
	 * @param id
	 *            the id of the vertex
	 * @return vertex with id-number id
	 */
	public Vertex getVertex(int id);

	/**
	 * @param id
	 *            the id of the edge
	 * @return edge with id-number id
	 */
	public Edge getEdge(int id);

	/**
	 * @return the maximum number of vertices which can be stored in the graph
	 *         before the arrays are expanded
	 */
	public int getMaxVCount();

	/**
	 * Computes the new maximum number of vertices when expansion is needed.
	 * 
	 * @return the new maximum number of vertices
	 */
	public int getExpandedVertexCount();

	/**
	 * Computes the new maximum number of edges when expansion is needed.
	 * 
	 * @return the new maximum number of edges
	 */
	public int getExpandedEdgeCount();

	/**
	 * @return the maximum number of edges which can be stored in the graph
	 *         before the arrays are expanded
	 */
	public int getMaxECount();

	/**
	 * @return the current number of vertices stored in the graph
	 */
	public int getVCount();

	/**
	 * @return the current number of edges stored in the graph
	 */
	public int getECount();

	/**
	 * @return the id of the graph
	 */
	public String getId();

	/**
	 * sets the id of the graph
	 * 
	 * @param id
	 */
	public void setId(String id);

	/**
	 * Using this method, one can simply iterate over all edges of this graph
	 * using the advanced for-loop
	 * 
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Edge> edges();

	/**
	 * Using this method, one can simply iterate over all edges of this graph
	 * using the advanced for-loop
	 * 
	 * @param eclass
	 *            the EdgeClass of the edges which should be iterated
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Edge> edges(EdgeClass eclass);

	/**
	 * Using this method, one can simply iterate over all edges of this graph
	 * using the advanced for-loop
	 * 
	 * @param eclass
	 *            the M1-Class of the edges which should be iterated
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Edge> edges(Class<? extends Edge> eclass);

	/**
	 * Using this method, one can simply iterate over all vertices of this graph
	 * using the advanced for-loop
	 * 
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Vertex> vertices();

	/**
	 * Using this method, one can simply iterate over all vertices of this graph
	 * using the advanced for-loop
	 * 
	 * @param vclass
	 *            the VertexClass of the vertices which should be iterated
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Vertex> vertices(VertexClass vclass);

	/**
	 * Using this method, one can simply iterate over all vertices of this graph
	 * using the advanced for-loop
	 * 
	 * @param vclass
	 *            the M1-Class of the vertices which should be iterated
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Vertex> vertices(Class<? extends Vertex> vclass);

}