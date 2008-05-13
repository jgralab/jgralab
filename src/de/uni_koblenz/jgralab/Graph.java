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
	 * @return true if the graph is being loaded
	 */
	public boolean isLoading();

	/**
	 * Sets the loading flag. 
	 * @param isLoading
	 */
	public void setLoading(boolean isLoading);

	/**
	 * This method is called as soon as the loading of a graph is completed.
	 * It is used internally and one should not touch it
	 */
	public void internalLoadingCompleted();
	
	/**
	 * This method is called as soon as the loading of a graph is completed
	 * One may use it to perform own operations as soon as the loading is completed
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
	 * sets the internal graph version of this graph to graphVersion. This
	 * method is needed to load a graph without changing its version back to
	 * zero
	 * 
	 * @param graphVersion
	 */
	public void setGraphVersion(long graphVersion);

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
	 * Changes the graph structure version, should be called whenever the
	 * structure of the graph is changed, for instance by creation and deletion
	 * or reordering of vertices and edges
	 */
	public void vertexListModified();

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
	 * Changes the graph structure version, should be called whenever the
	 * structure of the graph is changed, for instance by creation and deletion
	 * or reordering of vertices and edges
	 */
	public void edgeListModified();

	/**
	 * @return the internal edge list version
	 * @see #edgeListModified()
	 * @see #isEdgeListModified(long)
	 */
	public long getEdgeListVersion();

	/**
	 * adds the given vertex object to this graph. if the vertex' id is 0, a
	 * valid id is set, otherwise the vertex' current id is used if possible.
	 * Should only be used by m1-Graphs derived from Graph. To create a new
	 * Vertex as user, use the appropriate methods from the derived Graphs like
	 * <code>createStreet(...)</code>
	 * 
	 * @param newVertex
	 *            the Vertex to add
	 * @throws GraphException
	 *             if a vertex with the same id already exists
	 */
	void addVertex(Vertex newVertex);
	

	/**
	 * @return true iff this graph contains the given vertex
	 */
	boolean containsVertex(Vertex v);

	/**
	 * adds the given edge object to this graph. if the edges id is 0, a valid
	 * id is set, otherwise the edges current id is used if possible. Should
	 * only be used by m1-Graphs derived from Graph. To create a new Edge as
	 * user, use the appropriate methods from the derived Graphs like
	 * <code>createStreet(...)</code>
	 * 
	 * @param newEdge
	 *            the edge to add
	 * @param alpha
	 *            the vertex the new edge should start at
	 * @param omega
	 *            the vertex the new edge should end at
	 * @throws GraphException
	 *             if a edge with the same id already exists
	 */
	void addEdge(Edge newEdge, Vertex alpha, Vertex omega);

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

	/**
	 * @param aVertexClass
	 * @return the first vertex object of class aVertexClass in vSeq
	 */
	public Vertex getFirstVertexOfClass(VertexClass aVertexClass);

	/**
	 * @param aVertexClass
	 * @param explicitType
	 *            if set to true, only vertices which are explicitly of the
	 *            given edge class will be retrieved, otherwise also vertices of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the first vertex object of explicit class aVertexClass in vSeq
	 */
	public Vertex getFirstVertexOfClass(VertexClass aVertexClass,
			boolean explicitType);

	/**
	 * @param aVertexClass
	 * @return the first vertex object of class aVertexClass in vSeq
	 */
	public Vertex getFirstVertexOfClass(Class<? extends Vertex> aVertexClass);

	/**
	 * @param aVertexClass
	 * @param explicitType
	 *            if set to true, only vertices which are explicitly of the
	 *            given edge class will be retrieved, otherwise also vertices of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the first vertex object of explicit class aVertexClass in vSeq
	 */
	public Vertex getFirstVertexOfClass(Class<? extends Vertex> aVertexClass,
			boolean explicitType);

	/**
	 * @param aVertex
	 * @return the next vertex object in vSeq of aVertex
	 */
	public Vertex getNextVertex(Vertex aVertex);

	/**
	 * @param aVertex
	 *            the current vertex
	 * @param aVertexClass
	 *            the class of the next vertex
	 * @return the next vertex in vSeq of class aVertexClass or its superclasses
	 */
	public Vertex getNextVertexOfClass(Vertex aVertex, VertexClass aVertexClass);

	/**
	 * @param aVertex
	 *            the current vertex
	 * @param aM1VertexClass
	 *            the javaclass of the next vertex
	 * @return the next vertex in vSeq of class aVertexClass or its superclasses
	 */
	public Vertex getNextVertexOfClass(Vertex aVertex,
			Class<? extends Vertex> aM1VertexClass);

	/**
	 * @param aVertex
	 *            the current vertex
	 * @param aVertexClass
	 *            the class of the next vertex
	 * @param explicitType
	 *            if set to true, only vertices which are explicitly of the
	 *            given edge class will be retrieved, otherwise also vertices of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next vertex in vSeq of explicit class aVertexClass
	 */
	public Vertex getNextVertexOfClass(Vertex aVertex,
			VertexClass aVertexClass, boolean explicitType);

	/**
	 * @param aVertex
	 *            the current vertex
	 * @param aM1VertexClass
	 *            the class of the next vertex
	 * @param explicitType
	 *            if set to true, only vertices which are explicitly of the
	 *            given edge class will be retrieved, otherwise also vertices of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next vertex in vSeq of explicit class aVertexClass
	 */
	public Vertex getNextVertexOfClass(Vertex aVertex,
			Class<? extends Vertex> aM1VertexClass, boolean explicitType);


	/**
	 * @return first edge object of eSeq
	 */
	public Edge getFirstEdgeInGraph();

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
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the first edge object of explicit anEdgeClass in eSeq
	 */
	public Edge getFirstEdgeOfClassInGraph(EdgeClass anEdgeClass,
			boolean explicitType);

	/**
	 * @param anEdgeClass
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the first edge object of explicit anEdgeClass in eSeq
	 */
	public Edge getFirstEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass,
			boolean explicitType);

	/**
	 * @param anEdge
	 * @return the next edge object in eSeq of anEdge
	 */
	public Edge getNextEdgeInGraph(Edge anEdge);

	/**
	 * @param anEdge
	 *            the current edge
	 * @param anEdgeClass
	 * @return the next object of anEdgeClass or its superclasses in eSeq
	 */
	public Edge getNextEdgeOfClassInGraph(Edge anEdge, EdgeClass anEdgeClass);

	/**
	 * @param anEdge
	 *            the current edge
	 * @param anEdgeClass
	 * @return the next object of anEdgeClass or its superclasses in eSeq
	 */
	public Edge getNextEdgeOfClassInGraph(Edge anEdge,
			Class<? extends Edge> anEdgeClass);

	/**
	 * @param anEdge
	 *            the current edge
	 * @param anEdgeClass
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next object of explicit anEdgeClass in eSeq
	 */
	public Edge getNextEdgeOfClassInGraph(Edge anEdge, EdgeClass anEdgeClass,
			boolean explicitType);

	/**
	 * @param anEdge
	 *            the current edge
	 * @param anEdgeClass
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next object of explicit anEdgeClass in eSeq
	 */
	public Edge getNextEdgeOfClassInGraph(Edge anEdge,
			Class<? extends Edge> anEdgeClass, boolean explicitType);

	/**
	 * @param v
	 *            a vertex
	 * @return the first edge in the incidence list of v
	 */
	public Edge getFirstEdge(Vertex v);

	/**
	 * @param v
	 *            a vertex
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @return the first edge in the incidence list of v with the specified
	 *         orientation
	 */
	public Edge getFirstEdge(Vertex v, EdgeDirection orientation);

	public Edge getFirstEdgeOfClass(Vertex v, EdgeClass ec);

	public Edge getFirstEdgeOfClass(Vertex v, EdgeClass ec, boolean noSubclasses);

	public Edge getFirstEdgeOfClass(Vertex v, EdgeClass ec,
			EdgeDirection orientation, boolean noSubclasses);

	public Edge getFirstEdgeOfClass(Vertex v, Class<? extends Edge> ec);

	public Edge getFirstEdgeOfClass(Vertex v, Class<? extends Edge> ec,
			boolean noSubclasses);

	public Edge getFirstEdgeOfClass(Vertex v, Class<? extends Edge> ec,
			EdgeDirection orientation, boolean noSubclasses);

	/**
	 * @param e
	 *            an edge
	 * @return the next edge in the incidence list of this(e)
	 */
	public Edge getNextEdge(Edge e);

	/**
	 * @param e
	 *            an edge
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @return the next edge in the incidence list of this(e) with the specified
	 *         orientation
	 */
	public Edge getNextEdge(Edge e, EdgeDirection orientation);

	public Edge getNextEdgeOfClass(Edge e, EdgeClass ec);

	public Edge getNextEdgeOfClass(Edge e, EdgeClass ec, boolean noSubclasses);

	public Edge getNextEdgeOfClass(Edge e, EdgeClass ec,
			EdgeDirection orientation, boolean noSubclasses);

	public Edge getNextEdgeOfClass(Edge e, Class<? extends Edge> ec);

	public Edge getNextEdgeOfClass(Edge e, Class<? extends Edge> ec,
			boolean noSubclasses);

	public Edge getNextEdgeOfClass(Edge e, Class<? extends Edge> ec,
			EdgeDirection orientation, boolean noSubclasses);

	/**
	 * @param v
	 *            the vertex object which degree is to be determined
	 * @return the degree of vertex v
	 */
	public int getDegree(Vertex v);

	/**
	 * @param v
	 *            the vertex object which degree is to be determined
	 * @param orientation
	 *            the orientation the next incidence should have are counted
	 */
	public int getDegree(Vertex v, EdgeDirection orientation);

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
	 * @param e
	 *            the edge object which alpha vertex has to be determined
	 * @return the alpha vertex of edge e
	 */
	public Vertex getAlpha(Edge e);

	/**
	 * @param e
	 *            the edge object which omega vertex has to be determined
	 * @return the omega vertex of edge e
	 */
	public Vertex getOmega(Edge e);

	/**
	 * Puts source somewhere before target in vSeq
	 * 
	 * @param source
	 *            a vertex
	 * @param target
	 *            a vertex
	 */
	public void putAfterVertex(Vertex target, Vertex source);

	/**
	 * Checks whether <code>source</code> is after <code>target</code> in
	 * the global vertex sequence of this graph.
	 * 
	 * @param target
	 *            a Vertex
	 * @param source
	 *            another Vertex
	 * @return true if source is after target in the global vertex sequence of
	 *         this graph
	 */
	public boolean isAfterVertex(Vertex target, Vertex source);

	/**
	 * Checks whether <code>source</code> is before <code>target</code> in
	 * the global vertex sequence of this graph.
	 * 
	 * @param target
	 *            a Vertex
	 * @param source
	 *            another Vertex
	 * @return true if source is before target in the global vertex sequence of
	 *         this graph
	 */
	public boolean isBeforeVertex(Vertex target, Vertex source);

	/**
	 * puts source somewhere after target in eSeq
	 * 
	 * @param source
	 *            an edge
	 * @param target
	 *            an edge
	 */
	public void putAfterEdgeInGraph(Edge target, Edge source);

	/**
	 * puts source somewhere before target in vSeq
	 * 
	 * @param source
	 *            a vertex
	 * @param target
	 *            a vertex
	 */
	public void putBeforeVertex(Vertex target, Vertex source);

	/**
	 * puts source somewhere before target in eSeq
	 * 
	 * @param source
	 *            an edge
	 * @param target
	 *            an edge
	 */
	public void putBeforeEdgeInGraph(Edge target, Edge source);

	/**
	 * Checks whether <code>source</code> is after <code>target</code> in
	 * the global edge sequence of this graph.
	 * 
	 * @param target
	 *            an Edge
	 * @param source
	 *            another Edge
	 * @return true if source is after target in the global edge sequence of
	 *         this graph
	 */
	public boolean isAfterEdgeInGraph(Edge target, Edge source);

	/**
	 * Checks whether <code>source</code> is before <code>target</code> in
	 * the global edge sequence of this graph.
	 * 
	 * @param target
	 *            an Edge
	 * @param source
	 *            another Edge
	 * @return true if source is before target in the global edge sequence of
	 *         this graph
	 */
	public boolean isBeforeEdgeInGraph(Edge target, Edge source);

	/**
	 * puts the given edge <code>edge</code> before the given edge
	 * <code>nextEdge</code> in the incidence list. This does neither affect
	 * the global edge sequence eSeq nor the alpha or omega vertices, only the
	 * order of the edges at the <code>this-vertex</code> of e is changed
	 */
	public void putEdgeBefore(Edge edge, Edge nextEdge);

	/**
	 * puts the given edge <code>edge</code> after the given edge
	 * <code>previousEdge</code> in the incidence list. This does neither
	 * affect the global edge sequence eSeq nor the alpha or omega vertices,
	 * only the order of the edges at the <code>this-vertex</code> of e is
	 * changed
	 */
	public void putEdgeAfter(Edge edge, Edge previousEdge);

	/**
	 * inserts the given edge <code>edge</code> at the given position
	 * <code>pos</code> in the incidence list. This does neither affect the
	 * global edge sequence eSeq nor the alpha or omega vertices, only the order
	 * of the edges at the <code>this-vertex</code> of e is changed
	 * 
	 * @throws GraphException if the edges this-vertex and the given vertex are
	 *        not identical
	 */
	public void insertEdgeAt(Vertex vertex, Edge edge, int pos);

	/**
	 * @return the maximum number of vertices which can be stored in the graph
	 *         before the arrays are expanded
	 */
	public int getMaxVCount();

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
	 * Sets the start vertex of the given edge <code>e</code> to
	 * <code>alpha</code>. Also removes the edge from the incidence sequence
	 * of its old alpha vertex
	 */
	public void setAlpha(Edge e, Vertex alpha);

	/**
	 * Sets the end vertex of the given edge <code>e</code> to
	 * <code>omega</code> Also removes the edge from the incidence sequence of
	 * its old omega vertex
	 */
	public void setOmega(Edge e, Vertex omega);

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
	 * Using this method, one can simply iterate over all aggregations of this
	 * graph using the advanced for-loop
	 * 
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Aggregation> aggregations();

	/**
	 * Using this method, one can simply iterate over all compositions of this
	 * graph using the advanced for-loop
	 * 
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Composition> compositions();

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