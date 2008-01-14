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
	public <T extends Vertex> T createVertex(Class<T> cls)  ;
	
	/**
	 * Creates a instance of the given class and adds this edge to the graph
	 */
	public <T extends Edge> T createEdge(Class<T> cls, Vertex alpha, Vertex omega)   ;
	
	/**
	 * adds the given vertex object to this graph. if the vertex' id is 0, a
	 * valid id is set, otherwise the vertex' current id is used if possible.
	 * Should only be used by m1-Graphs derived from Graph. To create a new
	 * Vertex as user, use the appropriate methods from the derived Graphs like
	 * <code>createStreet(...)</code>
	 * 
	 * @param aVertex
	 *            the Vertex to add
	 * @throws GraphException if a vertex with the same id already exists
	 */
	void addVertex(Vertex aVertex) ;

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
	 * @throws GraphException if a edge with the same id already exists
	 */
	void addEdge(Edge newEdge, Vertex alpha, Vertex omega);

	/**
	 * @return true iff this graph contains the given edge
	 */
	boolean containsEdge(Edge e);

	/**
	 * removes the vertex with the given id from vSeq and erases its attributes
	 * 
	 * @param id
	 *            the id of the vertex to be deleted
	 */
	public void deleteVertex(int id) ;

	/**
	 * removes the specified vertex from vSeq and erases its attributes
	 * 
	 * @param v
	 *            the id of the vertex to be deleted
	 */
	public void deleteVertex(Vertex v) ;

	/**
	 * removes this edge from eSeq and erases its attributes
	 * 
	 * @param id
	 *            the id of the edge to be deleted
	 *
	 */
	public void deleteEdge(int id) ;

	/**
	 * internally used method to delete incidences, do not use!
	 * 
	 * @param vNo
	 *            vertex number from which the incidence originates
	 * @param iNo
	 *            incidence number of the vertex
	 */
	void deleteEdgeTo(int vNo, int iNo) ;

	/**
	 * prints the incidence array on the console, only works for package
	 * 'incarray'
	 */
	public void printArray();

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
	 * @param v
	 * @return the next vertex object in vSeq of v's id
	 */
	public Vertex getNextVertex(int v);

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
	public Vertex getNextVertexOfClass(Vertex aVertex, Class<? extends Vertex> aM1VertexClass);

	
	/**
	 * @param aVertexNo
	 *            the number of the current vertex
	 * @param aVertexClass
	 *            the class of the next vertex
	 * @return the next vertex in vSeq of class aVertexClass or its superclasses
	 */
	public Vertex getNextVertexOfClass(int aVertexNo, VertexClass aVertexClass);

	/**
	 * @param aVertexNo
	 *            the number of the current vertex
	 * @param aM1VertexClass
	 *            the class of the next vertex
	 * @return the next vertex in vSeq of class aVertexClass or its superclasses
	 */
	public Vertex getNextVertexOfClass(int aVertexNo, Class<? extends Vertex> aM1VertexClass);

	
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
	 * @param aVertexNo
	 *            the number of the current vertex
	 * @param aVertexClass
	 *            the class of the next vertex
	 * @param explicitType
	 *            if set to true, only vertices which are explicitly of the
	 *            given edge class will be retrieved, otherwise also vertices of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next vertex in vSeq of explicit class aVertexClass
	 */
	public Vertex getNextVertexOfClass(int aVertexNo, VertexClass aVertexClass,
			boolean explicitType);
	
	/**
	 * @param aVertexNo
	 *            the number of the current vertex
	 * @param aM1VertexClass
	 *            the class of the next vertex
	 * @param explicitType
	 *            if set to true, only vertices which are explicitly of the
	 *            given edge class will be retrieved, otherwise also vertices of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next vertex in vSeq of explicit class aVertexClass
	 */
	public Vertex getNextVertexOfClass(int aVertexNo, Class<? extends Vertex> aM1VertexClass,
			boolean explicitType);

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
	 * @param e
	 * @return the next edge object in eSeq of e's id
	 */
	public Edge getNextEdgeInGraph(int e);

	/**
	 * @param anEdgeNo
	 *            the number of the current edge
	 * @param anEdgeClass
	 * @return the next object of anEdgeClass or its superclasses in eSeq
	 */
	public Edge getNextEdgeOfClassInGraph(int anEdgeNo, EdgeClass anEdgeClass);

	/**
	 * @param anEdgeNo
	 *            the number of the current edge
	 * @param anEdgeClass
	 * @return the next object of anEdgeClass or its superclasses in eSeq
	 */
	public Edge getNextEdgeOfClassInGraph(int anEdgeNo, Class<? extends Edge> anEdgeClass);
	
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
	public Edge getNextEdgeOfClassInGraph(Edge anEdge, Class<? extends Edge> anEdgeClass);
	
	/**
	 * @param anEdgeNo
	 *            the number of the current edge
	 * @param anEdgeClass
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next object of explicit anEdgeClass in eSeq
	 */
	public Edge getNextEdgeOfClassInGraph(int anEdgeNo, EdgeClass anEdgeClass,
			boolean explicitType);
	
	/**
	 * @param anEdgeNo
	 *            the number of the current edge
	 * @param anEdgeClass
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next object of explicit anEdgeClass in eSeq
	 */
	public Edge getNextEdgeOfClassInGraph(int anEdgeNo, Class<? extends Edge> anEdgeClass,
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
	public Edge getNextEdgeOfClassInGraph(Edge anEdge, Class<? extends Edge> anEdgeClass,
			boolean explicitType);

	/**
	 * @param v
	 *            the vertex which is connected to the incidence
	 * @return first incidence object in iSeq of vertex v
	 */
	public Edge getFirstEdge(Vertex v);

	/**
	 * @param v
	 *            the vertex which is connect to the incidence
	 * @return first incidence object in iSeq of vertex-id v
	 */
	public Edge getFirstEdge(int v);

	/**
	 * @param v
	 *            the vertex which is connected to the incidence
	 * @return first incidence object in iSeq of vertex v which has the
	 *         orientation IN or OUT
	 * @param orientation
	 *            the orientation the next incidence should have
	 */
	public Edge getFirstEdge(Vertex v, EdgeDirection orientation);

	/**
	 * @param i
	 *            the vertex-id which is connected to the incidence
	 * @return first incidence object in iSeq of vertex e which has the
	 *         orientation IN or OUT
	 * @param orientation
	 *            the orientation the next incidence should have
	 */
	public Edge getFirstEdge(int i, EdgeDirection orientation);

	/**
	 * @param i
	 *            an incidence object
	 * @return the next incidence object in iSeq after e
	 */
	public Edge getNextEdge(Edge i);

	/**
	 * @param i
	 *            an incidence-id
	 * @return the next incidence object in iSeq after e
	 */
	public Edge getNextEdge(int i);

	/**
	 * @param i
	 *            an incidence object
	 * @return the next incidence object in iSeq after e which has the
	 *         orientation IN or OUT
	 * @param orientation
	 *            the orientation the next incidence should have
	 */
	public Edge getNextEdge(Edge i, EdgeDirection orientation);

	/**
	 * @param i
	 *            an incidence-id
	 * @return the next incidence object in iSeq after e which has the
	 *         orientation IN or OUT
	 * @param orientation
	 *            the orientation the next incidence should have
	 */
	public Edge getNextEdge(int i, EdgeDirection orientation);

	/**
	 * @param v
	 *            the vertex object which degree is to be determined
	 * @return the degree of vertex v
	 */
	public int getDegree(Vertex v);

	/**
	 * @param v
	 *            the vertex-id which degree is to be determined
	 * @return the degree of vertex v
	 */
	public int getDegree(int v);

	/**
	 * @param v
	 *            the vertex object which degree is to be determined
	 * @param orientation
	 *            the orientation the next incidence should have are counted
	 */
	public int getDegree(Vertex v, EdgeDirection orientation);

	/**
	 * @param v
	 *            the vertex-id which degree is to be determined
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @return the degree of vertex v, only IN or OUT incidences are counted
	 */
	public int getDegree(int v, EdgeDirection orientation);

	/**
	 * @param id
	 *            the id of the vertex
	 * @return vertex with id-number id
	 */
	public Vertex getVertex(int id);

	/**
	 * @param vc
	 *            the vertexclass of the vertex
	 * @param id
	 *            the id of the vertex
	 * @return vertex with id-number id only if vertex' class is vc or one of
	 *         its subclasses
	 */
	@Deprecated
	public Vertex getVertexOfClass(VertexClass vc, int id);

	/**
	 * @param vc
	 *            the vertexclass of the vertex
	 * @param id
	 *            the id of the vertex
	 * @param explicitType
	 *            if set to true, only vertices which are explicitly of the
	 *            given vertex class will be retrieved, otherwise also vertices of
	 *            subclasses of the given VertexClass will be retrieved
	 * @return vertex with id-number id only if vertex' class is vc
	 */
	@Deprecated
	public Vertex getVertexOfClass(VertexClass vc, int id, boolean explicitType);

	/**
	 * @param id
	 *            the id of the edge
	 * @return edge with id-number id
	 */
	public Edge getEdge(int id);

	/**
	 * @param ec
	 *            the vertexclass of the vertex
	 * @param id
	 *            the id of the vertex
	 * @return vertex with id-number id only if vertex' class is ec or one of
	 *         its subclasses
	 */
	@Deprecated
	public Edge getEdgeOfClassInGraph(EdgeClass ec, int id);

	/**
	 * @param ec
	 *            the vertexclass of the vertex
	 * @param id
	 *            the id of the vertex
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return edge with id-number id only if edges class is ec
	 */
	@Deprecated
	public Edge getEdgeOfClassInGraph(EdgeClass ec, int id, boolean explicitType);

	/**
	 * @param e
	 *            the edge object which alpha vertex has to be determined
	 * @return the alpha vertex of edge e
	 */
	public Vertex getAlpha(Edge e);

	/**
	 * @param e
	 *            the edge-id which alpha vertex has to be determined
	 * @return the alpha vertex of edge e
	 */
	public Vertex getAlpha(int e);

	/**
	 * @param e
	 *            the edge object which omega vertex has to be determined
	 * @return the omega vertex of edge e
	 */
	public Vertex getOmega(Edge e);

	/**
	 * @param e
	 *            the edge-id which omega vertex has to be determined
	 * @return the omega vertex of edge e
	 */
	public Vertex getOmega(int e);

	/**
	 * warning: slow in package 'incarray'
	 * 
	 * @param v
	 *            the vertex-id which previous vertex in vSeq has to be
	 *            determined
	 * @return the previous vertex object in vSeq
	 */
	public Vertex getPrevVertex(int v);

	/**
	 * warning: slow in package 'incarray'
	 * 
	 * @param e
	 *            the edge-id which previous edge in eSeq has to be determined
	 * @return the previous edge object in eSeq
	 */
	public Edge getPrevEdgeInGraph(int e);

	/**
	 * @param source
	 *            a vertex id
	 * @param target
	 *            a vertex id
	 * @return true, if target is before source in vSeq
	 */
	public boolean isBeforeVertex(int source, int target);

	/**
	 * @param source
	 *            an edge id
	 * @param target
	 *            an edge id
	 * @return true, if target is before source in eSeq
	 */
	public boolean isBeforeEdgeInGraph(int source, int target);

	/**
	 * @param source
	 *            a vertex id
	 * @param target
	 *            a vertex id
	 * @return true, if target is after source in vSeq
	 */
	public boolean isAfterVertex(int source, int target);

	/**
	 * @param source
	 *            an edge id
	 * @param target
	 *            an edge id
	 * @return true, if target is after source in eSeq
	 */
	public boolean isAfterEdgeInGraph(int source, int target);

	/**
	 * puts source somewhere before target in vSeq WARNING: implementation is
	 * different for package 'incarray' to 'oo'
	 * 
	 * @param source
	 *            a vertex id
	 * @param target
	 *            a vertex id
	 */
	public void putAfterVertex(int target, int source);

	/**
	 * puts source somewhere after target in eSeq WARNING: implementation is
	 * different for package 'incarray' to 'oo'
	 * 
	 * @param source
	 *            an edge id
	 * @param target
	 *            an edge id
	 */
	public void putAfterEdgeInGraph(int target, int source);

	/**
	 * puts source somewhere before target in vSeq WARNING: implementation is
	 * different for package 'incarray' to 'oo'
	 * 
	 * @param source
	 *            a vertex id
	 * @param target
	 *            a vertex id
	 */
	public void putBeforeVertex(int target, int source);

	/**
	 * puts source somewhere before target in eSeq WARNING: implementation is
	 * different for package 'incarray' to 'oo'
	 * 
	 * @param source
	 *            an edge id
	 * @param target
	 *            an edge id
	 */
	public void putBeforeEdgeInGraph(int target, int source);

	/**
	 * locates vertex v in vSeq and moves it to position pos
	 * 
	 * @param v
	 *            the vertex to move in vSeq
	 * @param pos
	 *            the position to where the vertex has to go
	 */
	public void insertVertexAtPos(int v, int pos);

	/**
	 * locates edge e in eSeq and moves it to position pos
	 * 
	 * @param e
	 *            the edge to move in eSeq
	 * @param pos
	 *            the position to where the edge has to go
	 */
	public void insertEdgeInGraphAtPos(int e, int pos);

	/**
	 * puts the given edge <code>edge</code> before the given edge
	 * <code>nextEdge</code> in the incidence list. This does neither affect
	 * the global edge sequence eSeq nor the alpha or omega vertices, only the
	 * order of the edges at the <code>this-vertex</code> of e is changed
	 */
	public void putEdgeBefore(Edge edge, Edge nextEdge);
	
	/**
	 * puts the edge given by <code>edgeId</code> before the edge given by
	 * <code>nextEdgeId</code> in the incidence list. This does neither affect
	 * the global edge sequence eSeq nor the alpha or omega vertices, only the
	 * order of the edges at the <code>this-vertex</code> of e is changed
	 */
	public void putEdgeBefore(int edgeId, int nextEdgeId);

	/**
	 * puts the given edge <code>edge</code> after the given edge
	 * <code>previousEdge</code> in the incidence list. This does neither
	 * affect the global edge sequence eSeq nor the alpha or omega vertices,
	 * only the order of the edges at the <code>this-vertex</code> of e is
	 * changed
	 */
	public void putEdgeAfter(Edge edge, Edge previousEdge);
	
	/**
	 * puts the edge by <code>edgeId</code> after the edge given by
	 * <code>previousEdgeId</code> in the incidence list. This does neither
	 * affect the global edge sequence eSeq nor the alpha or omega vertices,
	 * only the order of the edges at the <code>this-vertex</code> of e is
	 * changed
	 */
	public void putEdgeAfter(int edgeId, int previousEdgeId);

	/**
	 * inserts the given edge <code>edge</code> at the given position
	 * <code>pos</code> in the incidence list. This does neither affect the
	 * global edge sequence eSeq nor the alpha or omega vertices, only the order
	 * of the edges at the <code>this-vertex</code> of e is changed
	 */
	public void insertEdgeAt(Vertex vertex, Edge edge, int pos);
	
	/**
	 * inserts the edge given by <code>eId</code> at the given position
	 * <code>pos</code> in the incidence list. This does neither affect the
	 * global edge sequence eSeq nor the alpha or omega vertices, only the order
	 * of the edges at the <code>this-vertex</code> of e is changed
	 */
	public void insertEdgeAt(int vId, int eId, int pos);

	/**
	 * @param id
	 *            the id of the incidence to start
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getNextEdgeOfClass(int id, EdgeClass anEdgeClass);

	/**
	 * @param id
	 *            the id of the incidence to start
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getNextEdgeOfClass(int id, Class<? extends Edge> anEdgeClass);
	
	/**
	 * @param id
	 *            the id of the incidence to start
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getNextEdgeOfClass(int id, EdgeClass anEdgeClass,
			EdgeDirection orientation);

	/**
	 * @param id
	 *            the id of the incidence to start
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getNextEdgeOfClass(int id, Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation);

	
	/**
	 * @param id
	 *            the id of the vertex to which the incidence belongs
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(int id, EdgeClass anEdgeClass);
	
	/**
	 * @param id
	 *            the id of the vertex to which the incidence belongs
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(int id, Class<? extends Edge> anEdgeClass);

	/**
	 * @param id
	 *            the id of the vertex to which the incidence belongs
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(int id, EdgeClass anEdgeClass,
			EdgeDirection orientation);
	
	/**
	 * @param id
	 *            the id of the vertex to which the incidence belongs
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(int id, Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation);

	/**
	 * @param id
	 *            the id of the incidence to start
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getNextEdgeOfClass(int id, EdgeClass anEdgeClass,
			boolean explicitType);

	/**
	 * @param id
	 *            the id of the incidence to start
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getNextEdgeOfClass(int id, Class<? extends Edge> anEdgeClass,
			boolean explicitType);
	
	/**
	 * @param id
	 *            the id of the incidence to start
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getNextEdgeOfClass(int id, EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType);
	
	/**
	 * @param id
	 *            the id of the incidence to start
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getNextEdgeOfClass(int id, Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean explicitType);

	/**
	 * @param id
	 *            the id of the vertex to which the incidence belongs
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved *
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(int id, EdgeClass anEdgeClass,
			boolean explicitType);
	
	/**
	 * @param id
	 *            the id of the vertex to which the incidence belongs
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved *
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(int id, Class<? extends Edge> anEdgeClass,
			boolean explicitType);

	/**
	 * @param id
	 *            the id of the vertex to which the incidence belongs
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved *
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(int id, EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType);
	
	/**
	 * @param id
	 *            the id of the vertex to which the incidence belongs
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @param explicitType
	 *            if set to true, only edges which are explicitly of the given
	 *            edge class will be retrieved, otherwise also edges of
	 *            subclasses of the given EdgeClass will be retrieved *
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getFirstEdgeOfClass(int id, Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean explicitType);

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
	 * sets the first edge in eSeq of the graph to e
	 * 
	 * @param e
	 *            an edge
	 */
	public void setFirstEdgeInGraph(Edge e);

	/**
	 * Sets the start vertex of the given edge <code>e</code> to
	 * <code>alpha</code>. Also removes the edge from the incidence sequence
	 * of its old alpha vertex
	 */
	public void setAlpha(Edge e, Vertex alpha) ;

	/**
	 * Sets the end vertex of the given edge <code>e</code> to
	 * <code>omega</code> Also removes the edge from the incidence sequence of
	 * its old omega vertex
	 */
	public void setOmega(Edge e, Vertex omega) ;

	/**
	 * Sets the end vertex of the edge with id <code>eId</code> to
	 * the vertex with id <code>vId</code> Also removes the edge from the incidence sequence of
	 * its old omega vertex
	 */
	public void setOmega(int eId, int vId) ;

	/**
	 * Sets the start vertex of the edge with id <code>eId</code> to
	 * the vertex with id <code>vId</code> Also removes the edge from the incidence sequence of
	 * its old alpha vertex
	 */
	public void setAlpha(int eId, int vId) ;
	
	/**
	 * @return the current version of the graph. This version is increased everytime the graph is changed
	 */	
	public long getGraphVersion();
	
	/**
	 * Sets the graph's version.
	 * 
	 * @param graphVersion the new version of the graph
	 */
	void setGraphVersion(long graphVersion);
	
	/**
	 * @param aGraphVersion a version number as <code>long</code> to check the current graph version against 
	 * @return true iff the given version and the current version are not identical.
	 */
	public boolean isModified(long aGraphVersion);
	
	/**
	 * Using this method, one can simply iterate over all edges of this graph using the
	 * advanced for-loop
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Edge> edges();
	
	/**
	 * Using this method, one can simply iterate over all edges of this graph using the
	 * advanced for-loop
	 * @param eclass the EdgeClass of the edges which should be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Edge> edges(EdgeClass eclass);
	
	/**
	 * Using this method, one can simply iterate over all edges of this graph using the
	 * advanced for-loop
	 * @param eclass the M1-Class of the edges which should be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Edge> edges(Class<? extends Edge> eclass);
	
	/**
	 * Using this method, one can simply iterate over all edges of this graph using the
	 * advanced for-loop
	 * @param eclass the EdgeClass of the edges which should be iterated
	 * @param explicitType if set to true, no subclasses of the given EdgeClass will be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Edge> edges(EdgeClass eclass,  boolean explicitType);
	
	/**
	 * Using this method, one can simply iterate over all edges of this graph using the
	 * advanced for-loop
	 * @param eclass the M1-Class of the edges which should be iterated
	 * @param explicitType if set to true, no subclasses of the given EdgeClass will be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Edge> edges(Class<? extends Edge> eclass, boolean explicitType);
	
	/**
	 * Using this method, one can simply iterate over all aggregations of this graph using the
	 * advanced for-loop
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Aggregation> aggregations();
	
	
	/**
	 * Using this method, one can simply iterate over all compositions of this graph using the
	 * advanced for-loop
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Composition> compositions();
	
	
	/**
	 * Using this method, one can simply iterate over all vertices of this graph using the
	 * advanced for-loop
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Vertex> vertices();
	
	/**
	 * Using this method, one can simply iterate over all vertices of this graph using the
	 * advanced for-loop
	 * @param vclass the VertexClass of the vertices which should be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Vertex> vertices(VertexClass vclass);
	
	/**
	 * Using this method, one can simply iterate over all vertices of this graph using the
	 * advanced for-loop
	 * @param vclass the M1-Class of the vertices which should be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Vertex> vertices(Class<? extends Vertex> eclass);
	
	/**
	 * Using this method, one can simply iterate over all vertices of this graph using the
	 * advanced for-loop
	 * @param vclass the VertexClass of the vertices which should be iterated
	 * @param explicitType if set to true, no subclasses of the given VertexClass will be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Vertex> vertices(VertexClass vclass,  boolean explicitType);
	
	/**
	 * Using this method, one can simply iterate over all vertices of this graph using the
	 * advanced for-loop
	 * @param vclass the M1-Class of the vertices which should be iterated
	 * @param explicitType if set to true, no subclasses of the given VertexClass will be iterated
	 * @return a iterable object which can be iterated through using the advanced for-loop 
	 */
	public Iterable<Vertex> vertices(Class<? extends Vertex> vclass, boolean explicitType);

}