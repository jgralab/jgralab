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

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.RandomIdGenerator;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.CompositionClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * Implementation of interface Graph with doubly linked lists realizing eSeq,
 * vSeq and lambdaSeq, while ensuring efficient direct access to vertices and
 * edges by id via vertex and edge arrays.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class GraphImpl implements Graph {

	// ------------- GRAPH VARIABLES -------------

	/**
	 * the unique id of the graph in the schema
	 */
	private String id;

	/**
	 * The schema this graph belongs to
	 */
	private Schema schema;

	/**
	 * The GraphFactory that was used to create this graph. This factory wil lbe
	 * used to create vertices and edges in this graph.
	 */
	protected GraphFactory graphFactory;

	/**
	 * Holds the version of the graph, for every modification (e.g. adding a
	 * vertex or edge or changing the vertex or edge sequence or changing of an
	 * attribute value), this version number is increased by 1, It is saved in
	 * the tg-file.
	 */
	private long graphVersion;

	/**
	 * Indicates if this graph is currently loading.
	 */
	private boolean loading;

	// ------------- VERTEX LIST VARIABLES -------------
	/**
	 * maximum number of vertices
	 */
	private int vMax;

	/**
	 * number of vertices in the graph
	 */
	private int vCount = 0;

	/**
	 * indexed with vertex-id, holds the actual vertex-object itself
	 */
	private VertexImpl vertex[];

	/**
	 * free index list for vertices
	 */
	private FreeIndexList freeVertexList;

	/**
	 * holds the id of the first vertex in Vseq
	 */
	private VertexImpl firstVertex;

	/**
	 * holds the id of the last vertex in Vseq
	 */
	private VertexImpl lastVertex;

	/**
	 * Holds the version of the vertex sequence. For every modification (e.g.
	 * adding/deleting a vertex or changing the vertex sequence) this version
	 * number is increased by 1. It is set to 0 when the graph is loaded.
	 */
	private long vertexListVersion;

	/**
	 * List of vertices to be deleted by a cascading delete caused by deletion
	 * of a composition "parent".
	 */
	private List<VertexImpl> deleteVertexList;

	// ------------- EDGE LIST VARIABLES -------------

	/**
	 * maximum number of edges
	 */
	private int eMax;

	/**
	 * number of edges in the graph
	 */
	private int eCount = 0;

	/**
	 * indexed with edge-id, holds the actual edge-object itself
	 */
	private EdgeImpl edge[];
	private ReversedEdgeImpl revEdge[];

	/**
	 * free index list for edges
	 */
	private FreeIndexList freeEdgeList;

	/**
	 * holds the id of the first edge in Eseq
	 */
	private EdgeImpl firstEdge;

	/**
	 * holds the id of the last edge in Eseq
	 */
	private EdgeImpl lastEdge;

	/**
	 * Holds the version of the edge sequence. For every modification (e.g.
	 * adding/deleting an edge or changing the edge sequence) this version
	 * number is increased by 1. It is set to 0 when the graph is loaded.
	 */
	private long edgeListVersion;

	/**
	 * @param id
	 *            this Graph's id
	 * @param cls
	 *            the GraphClass of this Graph
	 * @param vMax
	 *            initial maximum number of vertices
	 * @param eMax
	 *            initial maximum number of edges
	 */
	public GraphImpl(String id, GraphClass cls, int vMax, int eMax) {
		if (vMax < 0) {
			throw new GraphException("vMax must not be less than zero", null);
		}
		if (eMax < 0) {
			throw new GraphException("eMax must not be less than zero", null);
		}

		schema = cls.getSchema();
		graphFactory = schema.getGraphFactory();
		setId(id == null ? RandomIdGenerator.generateId() : id);
		graphVersion = 0;

		expandVertexArray(vMax);
		firstVertex = null;
		lastVertex = null;
		vCount = 0;
		deleteVertexList = new LinkedList<VertexImpl>();

		expandEdgeArray(eMax);
		firstEdge = null;
		lastEdge = null;
		eCount = 0;
	}

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
	protected void addEdge(Edge newEdge, Vertex alpha, Vertex omega) {
		assert (newEdge.isNormal());
		EdgeImpl e = (EdgeImpl) newEdge;

		VertexImpl a = (VertexImpl) alpha;
		if (!a.isValidAlpha(e)) {
			throw new GraphException("Edges of class "
					+ e.getAttributedElementClass().getUniqueName()
					+ " may not start at vertices of class "
					+ a.getAttributedElementClass().getUniqueName());
		}

		VertexImpl o = (VertexImpl) omega;
		if (!o.isValidOmega(e)) {
			throw new GraphException("Edges of class "
					+ e.getAttributedElementClass().getUniqueName()
					+ " may not end at at vertices of class "
					+ o.getAttributedElementClass().getUniqueName());
		}

		int eId = e.getId();
		if (isLoading()) {
			if (eId > 0) {
				// the given edge already has an id, try to use it
				if (containsEdgeId(eId)) {
					throw new GraphException("edge with id " + e.getId()
							+ " already exists");
				}

				if (eId > eMax) {
					throw new GraphException("edge id " + e.getId()
							+ " is bigger than eSize");
				}
			} else {
				throw new GraphException("can not load an edge with id <= 0");
			}
		} else {
			if (eId != 0) {
				throw new GraphException("can not add an edge with id != 0");
			} else {
				eId = freeEdgeList.allocateIndex();
				if (eId == 0) {
					expandEdgeArray(getExpandedEdgeCount());
					eId = freeEdgeList.allocateIndex();
				}
				assert eId != 0;
				e.setId(eId);
			}
			a.appendIncidenceToLambaSeq(e);
			o.appendIncidenceToLambaSeq(e.reversedEdge);
		}
		appendEdgeToESeq(e);

		if (!isLoading()) {
			a.incidenceListModified();
			o.incidenceListModified();
			edgeListModified();
			edgeAdded(e);
		}
	}

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
	protected void addVertex(Vertex newVertex) {
		VertexImpl v = (VertexImpl) newVertex;

		int vId = v.getId();
		if (isLoading()) {
			if (vId > 0) {
				// the given vertex already has an id, try to use it
				if (containsVertexId(vId)) {
					throw new GraphException("vertex with id " + vId
							+ " already exists");
				}

				if (vId > vMax) {
					throw new GraphException("vertex id " + vId
							+ " is bigger than vSize");
				}
			} else {
				throw new GraphException("can not load a vertex with id <= 0");
			}
		} else {
			if (vId != 0) {
				throw new GraphException("can not add a vertex with id != 0");
			} else {
				vId = freeVertexList.allocateIndex();
				if (vId == 0) {
					expandVertexArray(getExpandedVertexCount());
					vId = freeVertexList.allocateIndex();
				}
				assert vId != 0;
				v.setId(vId);
			}
		}

		appendVertexToVSeq(v);

		if (!isLoading()) {
			vertexListModified();
			vertexAdded(v);
		}
	}

	/**
	 * Appends the edge e to the global edge sequence of this graph.
	 * 
	 * @param e
	 *            an edge
	 */
	private final void appendEdgeToESeq(EdgeImpl e) {
		if (firstEdge == null) {
			firstEdge = e;
		}
		if (lastEdge != null) {
			lastEdge.setNextEdgeInGraph(e);
			e.setPrevEdgeInGraph(lastEdge);
		}
		lastEdge = e;
		edge[e.getId()] = e;
		revEdge[e.getId()] = e.reversedEdge;
		++eCount;
	}

	/**
	 * Appends the vertex v to the global vertex sequence of this graph.
	 * 
	 * @param v
	 *            a vertex
	 */
	private final void appendVertexToVSeq(VertexImpl v) {
		if (firstVertex == null) {
			firstVertex = v;
		}
		if (lastVertex != null) {
			lastVertex.setNextVertex(v);
			v.setPrevVertex(lastVertex);
		}
		lastVertex = v;
		vertex[v.getId()] = v;
		++vCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getExpandedVertexCount()
	 */
	@Override
	public int getExpandedVertexCount() {
		return computeNewSize(vMax);
	}

	/**
	 * Computes new size of vertex and edge array depending on the current size.
	 * Up to 256k elements, the size is doubled. Between 256k and 1M elements,
	 * 256k elements are added. Beyond 1M, increase is 128k elements.
	 * 
	 * @param n
	 *            current size
	 * @return new size
	 */
	private int computeNewSize(int n) {
		return (n >= 1048576) ? n + 131072 : (n >= 262144) ? n + 262144 : n + n;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getExpandedEdgeCount()
	 */
	@Override
	public int getExpandedEdgeCount() {
		return computeNewSize(eMax);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AttributedElement a) {
		if (a instanceof Graph) {
			Graph g = (Graph) a;
			return this.hashCode() - g.hashCode();
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#containsEdge(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public final boolean containsEdge(Edge e) {
		return e != null && e.getGraph() == this && containsEdgeId(e.getId())
				&& getEdge(e.getId()) == e;
	}

	/**
	 * Checks if the edge id eId is valid and if there is an such an edge in
	 * this graph.
	 * 
	 * @param eId
	 *            an edge id
	 * @return true if this graph contains an edge with id eId
	 */
	private final boolean containsEdgeId(int eId) {
		if (eId < 0) {
			eId = -eId;
		}
		return eId > 0 && eId <= eMax && edge[eId] != null
				&& revEdge[eId] != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#containsVertex(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public final boolean containsVertex(Vertex v) {
		return v != null && v.getGraph() == this && containsVertexId(v.getId())
				&& vertex[v.getId()] == v;
	}

	/**
	 * Checks if the vertex id evd is valid and if there is an such a vertex in
	 * this graph.
	 * 
	 * @param vId
	 *            a vertex id
	 * @return true if this graph contains a vertex with id vId
	 */
	private final boolean containsVertexId(int vId) {
		return vId > 0 && vId <= vMax && vertex[vId] != null;
	}

	/**
	 * Creates an edge of the given class and adds this edge to the graph.
	 * <code>cls</code> has to be the "Impl" class.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Edge> T createEdge(Class<T> cls, Vertex alpha,
			Vertex omega) {
		try {
			Edge e = graphFactory.createEdge(cls, 0, this);
			addEdge(e, alpha, omega);
			return (T) e;
		} catch (Exception ex) {
			throw new GraphException("Error creating edge of class "
					+ cls.getName(), ex);
		}
	}

	/**
	 * Creates a vertex of the given class and adds this edge to the graph.
	 * <code>cls</code> has to be the "Impl" class.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Vertex> T createVertex(Class<T> cls) {
		try {
			Vertex v = graphFactory.createVertex(cls, 0, this);
			addVertex(v);
			return (T) v;
		} catch (Exception ex) {
			throw new GraphException("Error creating vertex of class "
					+ cls.getName(), ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#deleteEdge(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void deleteEdge(Edge e) {
		assert e.isValid();
		internalDeleteEdge(e);
		edgeListModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#deleteVertex(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void deleteVertex(Vertex v) {
		assert v.isValid();
		deleteVertexList.add((VertexImpl) v);
		internalDeleteVertex();
		vertexListModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#edgeDeleted(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void edgeDeleted(Edge e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#edgeAdded(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void edgeAdded(Edge e) {
	}

	/**
	 * Changes the graph structure version, should be called whenever the
	 * structure of the graph is changed, for instance by creation and deletion
	 * or reordering of vertices and edges
	 */
	protected void edgeListModified() {
		++edgeListVersion;
		++graphVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#edges()
	 */
	@Override
	public Iterable<Edge> edges() {
		return new EdgeIterable<Edge>(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#edges(java.lang.Class)
	 */
	@Override
	public Iterable<Edge> edges(Class<? extends Edge> edgeClass) {
		return new EdgeIterable<Edge>(this, edgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#edges(de.uni_koblenz.jgralab.schema.EdgeClass)
	 */
	@Override
	public Iterable<Edge> edges(EdgeClass edgeClass) {
		return new EdgeIterable<Edge>(this, edgeClass.getM1Class());
	}

	/**
	 * Changes the size of the edge array of this graph to newSize.
	 * 
	 * @param newSize
	 *            the new size of the edge array
	 */
	protected void expandEdgeArray(int newSize) {
		if (newSize <= eMax) {
			throw new GraphException("newSize must be > eSize: eSize=" + eMax
					+ ", newSize=" + newSize);
		}

		EdgeImpl[] e = new EdgeImpl[newSize + 1];
		if (edge != null) {
			System.arraycopy(edge, 0, e, 0, edge.length);
		}
		edge = e;

		ReversedEdgeImpl[] r = new ReversedEdgeImpl[newSize + 1];
		if (revEdge != null) {
			System.arraycopy(revEdge, 0, r, 0, revEdge.length);
		}
		revEdge = r;

		if (freeEdgeList == null) {
			freeEdgeList = new FreeIndexList(newSize);
		} else {
			freeEdgeList.expandBy(newSize - eMax);
		}
		eMax = newSize;
	}

	/**
	 * Changes the size of the vertex array of this graph to newSize.
	 * 
	 * @param newSize
	 *            the new size of the vertex array
	 */
	protected void expandVertexArray(int newSize) {
		if (newSize <= vMax) {
			throw new GraphException("newSize must > vSize: vSize=" + vMax
					+ ", newSize=" + newSize);
		}
		VertexImpl[] expandedArray = new VertexImpl[newSize + 1];
		if (vertex != null) {
			System.arraycopy(vertex, 0, expandedArray, 0, vertex.length);
		}

		if (freeVertexList == null) {
			freeVertexList = new FreeIndexList(newSize);
		} else {
			freeVertexList.expandBy(newSize - vMax);
		}
		vertex = expandedArray;
		vMax = newSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getECount()
	 */
	@Override
	public int getECount() {
		return eCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getEdge(int)
	 */
	@Override
	public Edge getEdge(int eId) {
		assert (eId < 0 && -eId <= eMax || eId > 0 && eId <= eMax);
		return eId < 0 ? revEdge[-eId] : edge[eId];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getEdgeListVersion()
	 */
	@Override
	public long getEdgeListVersion() {
		return edgeListVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstEdgeInGraph()
	 */
	@Override
	public Edge getFirstEdgeInGraph() {
		return firstEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getLastEdgeInGraph()
	 */
	@Override
	public Edge getLastEdgeInGraph() {
		return lastEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstEdgeOfClassInGraph(java.lang.Class)
	 */
	@Override
	public Edge getFirstEdgeOfClassInGraph(Class<? extends Edge> edgeClass) {
		return getFirstEdgeOfClassInGraph(edgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstEdgeOfClassInGraph(java.lang.Class,
	 *      boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClassInGraph(Class<? extends Edge> edgeClass,
			boolean noSubclasses) {
		Edge currentEdge = getFirstEdgeInGraph();
		while (currentEdge != null) {
			if (noSubclasses) {
				if (edgeClass == currentEdge.getM1Class()) {
					return currentEdge;
				}
			} else {
				if (edgeClass.isInstance(currentEdge)) {
					return currentEdge;
				}
			}
			currentEdge = currentEdge.getNextEdgeInGraph();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstEdgeOfClassInGraph(de.uni_koblenz.jgralab.schema.EdgeClass)
	 */
	@Override
	public Edge getFirstEdgeOfClassInGraph(EdgeClass edgeClass) {
		return getFirstEdgeOfClassInGraph(edgeClass.getM1Class(), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstEdgeOfClassInGraph(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClassInGraph(EdgeClass eEdgeClass,
			boolean noSubclasses) {
		return getFirstEdgeOfClassInGraph(eEdgeClass.getM1Class(), noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstVertex()
	 */
	@Override
	public Vertex getFirstVertex() {
		return firstVertex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getLastVertex()
	 */
	@Override
	public Vertex getLastVertex() {
		return lastVertex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstVertexOfClass(java.lang.Class)
	 */
	@Override
	public Vertex getFirstVertexOfClass(Class<? extends Vertex> vertexClass) {
		return getFirstVertexOfClass(vertexClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstVertexOfClass(java.lang.Class,
	 *      boolean)
	 */
	@Override
	public Vertex getFirstVertexOfClass(Class<? extends Vertex> vertexClass,
			boolean noSubclasses) {
		Vertex firstVertex = getFirstVertex();
		if (firstVertex == null) {
			return null;
		}
		if (noSubclasses) {
			if (vertexClass == firstVertex.getM1Class()) {
				return firstVertex;
			}
		} else {
			if (vertexClass.isInstance(firstVertex)) {
				return firstVertex;
			}
		}
		return firstVertex.getNextVertexOfClass(vertexClass, noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstVertexOfClass(de.uni_koblenz.jgralab.schema.VertexClass)
	 */
	@Override
	public Vertex getFirstVertexOfClass(VertexClass vertexClass) {
		return getFirstVertexOfClass(vertexClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstVertexOfClass(de.uni_koblenz.jgralab.schema.VertexClass,
	 *      boolean)
	 */
	@Override
	public Vertex getFirstVertexOfClass(VertexClass vertexClass,
			boolean noSubclasses) {
		return getFirstVertexOfClass((vertexClass.getM1Class()), noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.AttributedElement#getGraphClass()
	 */
	@Override
	public GraphClass getGraphClass() {
		return (GraphClass) getAttributedElementClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getGraphVersion()
	 */
	@Override
	public long getGraphVersion() {
		return graphVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getMaxECount()
	 */
	@Override
	public int getMaxECount() {
		return eMax;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getMaxVCount()
	 */
	@Override
	public int getMaxVCount() {
		return vMax;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.AttributedElement#getSchema()
	 */
	@Override
	public Schema getSchema() {
		return schema;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getVCount()
	 */
	@Override
	public int getVCount() {
		return vCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getVertex(int)
	 */
	@Override
	public Vertex getVertex(int vId) {
		assert (vId > 0 && vId <= vMax);
		return vertex[vId];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getVertexListVersion()
	 */
	@Override
	public long getVertexListVersion() {
		return vertexListVersion;
	}

	/**
	 * Changes this graph's version. graphModified() is called whenever the
	 * graph is changed, all changes like adding, creating and reordering of
	 * edges and vertices or changes of attributes of the graph, an edge or a
	 * vertex are treated as a change.
	 */
	public void graphModified() {
		++graphVersion;
	}

	/**
	 * Deletes the edge from the internal structures of this graph.
	 * 
	 * @param edge
	 *            an edge
	 */
	private void internalDeleteEdge(Edge edge) {
		EdgeImpl e = (EdgeImpl) edge.getNormalEdge();
		edgeDeleted(e);

		VertexImpl alpha = e.getIncidentVertex();
		alpha.removeIncidenceFromLambaSeq(e);
		alpha.incidenceListModified();

		VertexImpl omega = e.reversedEdge.getIncidentVertex();
		omega.removeIncidenceFromLambaSeq(e.reversedEdge);
		omega.incidenceListModified();

		removeEdgeFromESeq(e);
	}

	/**
	 * Deletes all vertices in deleteVertexList from the internal structures of
	 * this graph. Possibly, cascading deletes of child vertices occur when
	 * parent vertices of Composition classes are deleted.
	 */
	private void internalDeleteVertex() {
		while (!deleteVertexList.isEmpty()) {
			VertexImpl v = deleteVertexList.remove(0);
			vertexDeleted(v);
			removeVertexFromVSeq(v);

			// delete all incident edges including incidence objects
			Edge e = v.getFirstEdge();
			while (e != null) {
				// check for cascading delete of vertices in incident
				// composition edges
				AttributedElementClass aec = e.getAttributedElementClass();
				if (aec instanceof CompositionClass) {
					CompositionClass comp = (CompositionClass) aec;
					if (comp.isAggregateFrom()) {
						// omega vertex is to be deleted
						VertexImpl omega = (VertexImpl) e.getOmega();
						if (containsVertex(omega)
								&& !deleteVertexList.contains(omega)) {
							// System.err.println("Delete omega vertex v" +
							// omegaId + "
							// of composition e" + eId);
							deleteVertexList.add(omega);
						}
					} else {
						VertexImpl alpha = (VertexImpl) e.getAlpha();
						if (containsVertex(alpha)
								&& !deleteVertexList.contains(alpha)) {
							// System.err.println("Delete alpha vertex v" +
							// alphaId + "
							// of composition e" + eId);
							deleteVertexList.add(alpha);
						}
					}
				}

				internalDeleteEdge(e);
				e = v.getFirstEdge();
			}
		}
	}

	/**
	 * Removes the vertex v from the global vertex sequence of this graph.
	 * 
	 * @param v
	 *            a vertex
	 */
	private final void removeVertexFromVSeq(VertexImpl v) {
		if (v == firstVertex) {
			// delete at head of vertex list
			firstVertex = (VertexImpl) v.getNextVertex();
			if (firstVertex != null) {
				firstVertex.setPrevVertex(null);
			}
			if (v == lastVertex) {
				// this vertex was the only one...
				lastVertex = null;
			}
		} else if (v == lastVertex) {
			// delete at tail of vertex list
			lastVertex = (VertexImpl) v.getPrevVertex();
			if (lastVertex != null) {
				lastVertex.setNextVertex(null);
			}
		} else {
			// delete somewhere in the middle
			((VertexImpl) v.getPrevVertex()).setNextVertex(v.getNextVertex());
			((VertexImpl) v.getNextVertex()).setPrevVertex(v.getPrevVertex());
		}
		freeVertexList.freeIndex(v.getId());
		vertex[v.getId()] = null;
		(v).setPrevVertex(null);
		(v).setNextVertex(null);
		v.setId(0);
		--vCount;
	}

	/**
	 * Removes the edge e from the global edge sequence of this graph.
	 * 
	 * @param e
	 *            an edge
	 */
	private final void removeEdgeFromESeq(EdgeImpl e) {
		if (e == firstEdge) {
			// delete at head of edge list
			firstEdge = (EdgeImpl) e.getNextEdgeInGraph();
			if (firstEdge != null) {
				firstEdge.setPrevEdgeInGraph(null);
			}
			if (e == lastEdge) {
				// this edge was the only one...
				lastEdge = null;
			}
		} else if (e == lastEdge) {
			// delete at tail of edge list
			lastEdge = (EdgeImpl) e.getPrevEdgeInGraph();
			if (lastEdge != null) {
				lastEdge.setNextEdgeInGraph(null);
			}
		} else {
			// delete somewhere in the middle
			((EdgeImpl) e.getPrevEdgeInGraph()).setNextEdgeInGraph(e
					.getNextEdgeInGraph());
			((EdgeImpl) e.getNextEdgeInGraph()).setPrevEdgeInGraph(e
					.getPrevEdgeInGraph());
		}

		freeEdgeList.freeIndex(e.getId());
		edge[e.getId()] = null;
		revEdge[e.getId()] = null;
		e.setPrevEdgeInGraph(null);
		e.setNextEdgeInGraph(null);
		e.setId(0);
		--eCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#isEdgeListModified(long)
	 */
	@Override
	public boolean isEdgeListModified(long edgeListVersion) {
		return (this.edgeListVersion != edgeListVersion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#isGraphModified(long)
	 */
	@Override
	public boolean isGraphModified(long previousVersion) {
		return (graphVersion != previousVersion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#isLoading()
	 */
	@Override
	public boolean isLoading() {
		return loading;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#isVertexListModified(long)
	 */
	@Override
	public boolean isVertexListModified(long previousVersion) {
		return (this.vertexListVersion != previousVersion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#loadingCompleted()
	 */
	@Override
	public void loadingCompleted() {
	}

	/**
	 * Constructs incidence lists for all vertices after loading this graph.
	 * 
	 * @param firstIncidence
	 *            array of edge ids of the first incidence
	 * @param nextIncidence
	 *            array of edge ids of subsequent edges
	 */
	public void internalLoadingCompleted(int[] firstIncidence,
			int[] nextIncidence) {
		freeVertexList.reinitialize(vertex);
		freeEdgeList.reinitialize(edge);
		for (int vId = 1; vId < vertex.length; ++vId) {
			VertexImpl v = vertex[vId];
			if (v != null) {
				int eId = firstIncidence[vId];
				while (eId != 0) {
					v.appendIncidenceToLambaSeq(eId < 0 ? revEdge[-eId]
							: edge[eId]);
					eId = nextIncidence[eMax + eId];
				}
			}
		}
	}

	/**
	 * Modifies eSeq such that the movedEdge is immediately after the
	 * targetEdge.
	 * 
	 * @param targetEdge
	 *            an edge
	 * @param movedEdge
	 *            the edge to be moved
	 */
	public void putEdgeAfterInGraph(EdgeImpl targetEdge, EdgeImpl movedEdge) {
		assert (targetEdge.isValid() && movedEdge.isValid());

		if (targetEdge == movedEdge) {
			throw new GraphException("an edge can't be put after itself");
		}

		if (targetEdge.getNextEdgeInGraph() == movedEdge) {
			return;
		}

		assert firstEdge != lastEdge;

		// remove moved edge from eSeq
		if (movedEdge == firstEdge) {
			firstEdge = (EdgeImpl) movedEdge.getNextEdgeInGraph();
			((EdgeImpl) movedEdge.getNextEdgeInGraph())
					.setPrevEdgeInGraph(null);
		} else if (movedEdge == lastEdge) {
			lastEdge = (EdgeImpl) movedEdge.getPrevEdgeInGraph();
			((EdgeImpl) movedEdge.getPrevEdgeInGraph())
					.setNextEdgeInGraph(null);
		} else {
			((EdgeImpl) movedEdge.getPrevEdgeInGraph())
					.setNextEdgeInGraph(movedEdge.getNextEdgeInGraph());
			((EdgeImpl) movedEdge.getNextEdgeInGraph())
					.setPrevEdgeInGraph(movedEdge.getPrevEdgeInGraph());
		}

		// insert moved edge in eSeq immediately after target
		if (targetEdge == lastEdge) {
			lastEdge = movedEdge;
			movedEdge.setNextEdgeInGraph(null);
		} else {
			((EdgeImpl) targetEdge.getNextEdgeInGraph())
					.setPrevEdgeInGraph(movedEdge);
			movedEdge.setNextEdgeInGraph(targetEdge.getNextEdgeInGraph());
		}
		movedEdge.setPrevEdgeInGraph(targetEdge);
		targetEdge.setNextEdgeInGraph(movedEdge);
		edgeListModified();
	}

	/**
	 * Modifies vSeq such that the movedVertex is immediately after the
	 * targetVertex.
	 * 
	 * @param targetVertex
	 *            a vertex
	 * @param movedVertex
	 *            the vertex to be moved
	 */
	public void putVertexAfter(VertexImpl targetVertex, VertexImpl movedVertex) {
		assert (targetVertex.isValid() && movedVertex.isValid());

		if (targetVertex == movedVertex) {
			throw new GraphException("a vertex can't be put after itself");
		}

		if (targetVertex.getNextVertex() == movedVertex) {
			return;
		}

		assert firstVertex != lastVertex;

		// remove moved vertex from vSeq
		if (movedVertex == firstVertex) {
			firstVertex = (VertexImpl) movedVertex.getNextVertex();
			((VertexImpl) movedVertex.getNextVertex()).setPrevVertex(null);
		} else if (movedVertex == lastVertex) {
			lastVertex = (VertexImpl) movedVertex.getPrevVertex();
			((VertexImpl) movedVertex.getPrevVertex()).setNextVertex(null);
		} else {
			((VertexImpl) movedVertex.getPrevVertex())
					.setNextVertex(movedVertex.getNextVertex());
			((VertexImpl) movedVertex.getNextVertex())
					.setPrevVertex(movedVertex.getPrevVertex());
		}

		// insert moved vertex in vSeq immediately after target
		if (targetVertex == lastVertex) {
			lastVertex = movedVertex;
			movedVertex.setNextVertex(null);
		} else {
			((VertexImpl) targetVertex.getNextVertex())
					.setPrevVertex(movedVertex);
			movedVertex.setNextVertex(targetVertex.getNextVertex());
		}
		movedVertex.setPrevVertex(targetVertex);
		targetVertex.setNextVertex(movedVertex);
		vertexListModified();
	}

	/**
	 * Modifies eSeq such that the movedEdge is immediately before the
	 * targetEdge.
	 * 
	 * @param targetEdge
	 *            an edge
	 * @param movedEdge
	 *            the edge to be moved
	 */
	public void putEdgeBeforeInGraph(EdgeImpl targetEdge, EdgeImpl movedEdge) {
		assert (targetEdge.isValid() && movedEdge.isValid());

		if (targetEdge == movedEdge) {
			throw new GraphException("an edge can't be put before itself");
		}

		if (targetEdge.getPrevEdgeInGraph() == movedEdge) {
			return;
		}

		assert firstEdge != lastEdge;

		// remove moved edge from eSeq
		if (movedEdge == firstEdge) {
			firstEdge = (EdgeImpl) movedEdge.getNextEdgeInGraph();
			((EdgeImpl) movedEdge.getNextEdgeInGraph())
					.setPrevEdgeInGraph(null);
		} else if (movedEdge == lastEdge) {
			lastEdge = (EdgeImpl) movedEdge.getPrevEdgeInGraph();
			((EdgeImpl) movedEdge.getPrevEdgeInGraph())
					.setNextEdgeInGraph(null);
		} else {
			((EdgeImpl) movedEdge.getPrevEdgeInGraph())
					.setNextEdgeInGraph(movedEdge.getNextEdgeInGraph());
			((EdgeImpl) movedEdge.getNextEdgeInGraph())
					.setPrevEdgeInGraph(movedEdge.getPrevEdgeInGraph());
		}

		// insert moved edge in eSeq immediately before target
		if (targetEdge == firstEdge) {
			firstEdge = movedEdge;
			movedEdge.setPrevEdgeInGraph(null);
		} else {
			((EdgeImpl) targetEdge.getPrevEdgeInGraph())
					.setNextEdgeInGraph(movedEdge);
			movedEdge.setPrevEdgeInGraph(targetEdge.getPrevEdgeInGraph());
		}
		movedEdge.setNextEdgeInGraph(targetEdge);
		targetEdge.setPrevEdgeInGraph(movedEdge);
		edgeListModified();
	}

	/**
	 * Modifies vSeq such that the movedVertex is immediately before the
	 * targetVertex.
	 * 
	 * @param targetVertex
	 *            a vertex
	 * @param movedVertex
	 *            the vertex to be moved
	 */
	public void putVertexBefore(VertexImpl targetVertex, VertexImpl movedVertex) {
		assert (targetVertex.isValid() && movedVertex.isValid());

		if (targetVertex == movedVertex) {
			throw new GraphException("a vertex can't be put before itself");
		}

		if (targetVertex.getPrevVertex() == movedVertex) {
			return;
		}

		assert firstVertex != lastVertex;

		// remove moved vertex from vSeq
		if (movedVertex == firstVertex) {
			firstVertex = (VertexImpl) movedVertex.getNextVertex();
			((VertexImpl) movedVertex.getNextVertex()).setPrevVertex(null);
		} else if (movedVertex == lastVertex) {
			lastVertex = (VertexImpl) movedVertex.getPrevVertex();
			((VertexImpl) movedVertex.getPrevVertex()).setNextVertex(null);
		} else {
			((VertexImpl) movedVertex.getPrevVertex())
					.setNextVertex(movedVertex.getNextVertex());
			((VertexImpl) movedVertex.getNextVertex())
					.setPrevVertex(movedVertex.getPrevVertex());
		}

		// insert moved vertex in vSeq immediately before target
		if (targetVertex == firstVertex) {
			firstVertex = movedVertex;
			movedVertex.setPrevVertex(null);
		} else {
			((VertexImpl) targetVertex.getPrevVertex())
					.setNextVertex(movedVertex);
			movedVertex.setPrevVertex(targetVertex.getPrevVertex());
		}
		movedVertex.setNextVertex(targetVertex);
		targetVertex.setPrevVertex(movedVertex);
		vertexListModified();
	}

	/**
	 * Sets the version counter of this graph. Should only be called by GraphIO
	 * immediately after loading.
	 * 
	 * @param graphVersion
	 *            new version value
	 */
	public void setGraphVersion(long graphVersion) {
		this.graphVersion = graphVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#setId(java.lang.String)
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets the loading flag.
	 * 
	 * @param isLoading
	 */
	public void setLoading(boolean isLoading) {
		loading = isLoading;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#vertexDeleted(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void vertexDeleted(Vertex v) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#vertexAdded(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void vertexAdded(Vertex v) {
	}

	/**
	 * Changes the vertex sequence version of this graph. Should be called
	 * whenever the vertex list of this graph is changed, for instance by
	 * creation and deletion or reordering of vertices.
	 */
	protected void vertexListModified() {
		++vertexListVersion;
		++graphVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#vertices()
	 */
	@Override
	public Iterable<Vertex> vertices() {
		return new VertexIterable<Vertex>(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#vertices(java.lang.Class)
	 */
	@Override
	public Iterable<Vertex> vertices(Class<? extends Vertex> vertexClass) {
		return new VertexIterable<Vertex>(this, vertexClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#vertices(de.uni_koblenz.jgralab.schema.VertexClass)
	 */
	@Override
	public Iterable<Vertex> vertices(VertexClass vertexClass) {
		return new VertexIterable<Vertex>(this, vertexClass.getM1Class());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#defragment()
	 */
	@Override
	public void defragment() {
		// defragment vertex array
		if (vCount < vMax) {
			if (vCount > 0) {
				int vId = vMax;
				while (freeVertexList.isFragmented()) {
					while (vId >= 1 && vertex[vId] == null) {
						--vId;
					}
					assert vId >= 1;
					VertexImpl v = vertex[vId];
					vertex[vId] = null;
					freeVertexList.freeIndex(vId);
					int newId = freeVertexList.allocateIndex();
					assert newId < vId;
					v.setId(newId);
					vertex[newId] = v;
					--vId;
				}
			}
			int newVMax = (vCount == 0 ? 1 : vCount);
			if (newVMax != vMax) {
				vMax = newVMax;
				VertexImpl[] newVertex = new VertexImpl[vMax + 1];
				System.arraycopy(vertex, 0, newVertex, 0, newVertex.length);
				vertex = newVertex;
			}
			graphModified();
			System.gc();
		}
		// defragment edge array
		if (eCount < eMax) {
			if (eCount > 0) {
				int eId = eMax;
				while (freeEdgeList.isFragmented()) {
					while (eId >= 1 && edge[eId] == null) {
						--eId;
					}
					assert eId >= 1;
					EdgeImpl e = edge[eId];
					edge[eId] = null;
					ReversedEdgeImpl r = revEdge[eId];
					revEdge[eId] = null;
					freeEdgeList.freeIndex(eId);
					int newId = freeEdgeList.allocateIndex();
					assert newId < eId;
					e.setId(newId);
					edge[newId] = e;
					revEdge[newId] = r;
					--eId;
				}
			}
			int newEMax = (eCount == 0 ? 1 : eCount);
			if (newEMax != eMax) {
				eMax = newEMax;
				EdgeImpl[] newEdge = new EdgeImpl[eMax + 1];
				System.arraycopy(edge, 0, newEdge, 0, newEdge.length);
				edge = newEdge;
				System.gc();
			}
			graphModified();
			System.gc();
		}
	}
}
