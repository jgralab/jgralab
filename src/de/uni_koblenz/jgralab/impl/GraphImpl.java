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

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.GraphIO;
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
	protected long graphVersion;

	/**
	 * Indicates if this graph is currently loading.
	 */
	private boolean loading;

	// ------------- VERTEX LIST VARIABLES -------------
	/**
	 * maximum number of vertices
	 */
	protected int vMax;

	/**
	 * number of vertices in the graph
	 */
	abstract protected void setVCount(int count);

	/**
	 * indexed with vertex-id, holds the actual vertex-object itself
	 */
	abstract protected VertexImpl[] getVertex();

	abstract protected void setVertex(VertexImpl[] vertex);

	/**
	 * free index list for vertices
	 */
	protected FreeIndexList freeVertexList;

	abstract protected FreeIndexList getFreeVertexList();

	/**
	 * holds the id of the first vertex in Vseq
	 */
	abstract protected void setFirstVertex(VertexImpl firstVertex);

	/**
	 * holds the id of the last vertex in Vseq
	 */
	abstract protected void setLastVertex(VertexImpl lastVertex);

	abstract protected void setVertexListVersion(long vertexListVersion);

	/**
	 * List of vertices to be deleted by a cascading delete caused by deletion
	 * of a composition "parent".
	 */
	abstract protected List<VertexImpl> getDeleteVertexList();

	abstract protected void setDeleteVertexList(
			List<VertexImpl> deleteVertexList);

	// ------------- EDGE LIST VARIABLES -------------

	/**
	 * maximum number of edges
	 */
	protected int eMax;

	/**
	 * number of edges in the graph
	 */
	abstract protected void setECount(int count);

	/**
	 * indexed with edge-id, holds the actual edge-object itself
	 */
	abstract protected EdgeImpl[] getEdge();

	abstract protected void setEdge(EdgeImpl[] edge);

	abstract protected ReversedEdgeImpl[] getRevEdge();

	abstract protected void setRevEdge(ReversedEdgeImpl[] revEdge);

	/**
	 * free index list for edges
	 */
	protected FreeIndexList freeEdgeList;

	abstract protected FreeIndexList getFreeEdgeList();

	/**
	 * holds the id of the first edge in Eseq
	 */
	abstract protected void setFirstEdgeInGraph(EdgeImpl firstEdge);

	/**
	 * holds the id of the last edge in Eseq
	 */
	abstract protected void setLastEdgeInGraph(EdgeImpl lastEdge);

	abstract protected void setEdgeListVersion(long edgeListVersion);

	/**
	 * Creates a graph of the given GraphClass with the given id
	 * 
	 * @param id
	 *            this Graph's id
	 * @param cls
	 *            the GraphClass of this Graph
	 */
	public GraphImpl(String id, GraphClass cls) {
		this(id, cls, 1000, 1000);
	}

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
	protected GraphImpl(String id, GraphClass cls, int vMax, int eMax) {
		if (vMax < 0) {
			throw new GraphException("vMax must not be less than zero", null);
		}
		if (eMax < 0) {
			throw new GraphException("eMax must not be less than zero", null);
		}

		schema = cls.getSchema();
		graphFactory = schema.getGraphFactory();
		setId(id == null ? RandomIdGenerator.generateId() : id);
		// needed for initialization of graphVersion with transactions
		graphVersion = -1;
		setGraphVersion(0);

		expandVertexArray(vMax);
		setFirstVertex(null);
		setLastVertex(null);
		setVCount(0);
		setDeleteVertexList(new LinkedList<VertexImpl>());

		expandEdgeArray(eMax);
		setFirstEdgeInGraph(null);
		setLastEdgeInGraph(null);
		setECount(0);
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
		assert (newEdge.isNormal()) : "The edge to be added";
		if ((alpha == null) || (omega == null)) {
			throw new GraphException(
					"Alpha or Omega vertex of an edge may not be null");
		}
		assert ((alpha.getSchema() == omega.getSchema())
				&& (alpha.getSchema() == this.schema) && (newEdge.getSchema() == this.schema)) : "The schemas of alpha, omega, newEdge and this graph don't match!";
		assert ((alpha.getGraph() == omega.getGraph())
				&& (alpha.getGraph() == this) && (newEdge.getGraph() == this)) : "The graph of alpha, omega, newEdge and this graph don't match!";

		EdgeImpl e = (EdgeImpl) newEdge;

		VertexImpl a = (VertexImpl) alpha;
		if (!a.isValidAlpha(e)) {
			throw new GraphException("Edges of class "
					+ e.getAttributedElementClass().getQualifiedName()
					+ " may not start at vertices of class "
					+ a.getAttributedElementClass().getQualifiedName());
		}

		VertexImpl o = (VertexImpl) omega;
		if (!o.isValidOmega(e)) {
			throw new GraphException("Edges of class "
					+ e.getAttributedElementClass().getQualifiedName()
					+ " may not end at vertices of class "
					+ o.getAttributedElementClass().getQualifiedName());
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
			if (!canAddGraphElement(eId)) {
				throw new GraphException("can not add an edge with id != 0");
			}

			/*
			 * eId = allocateIndex(getFreeEdgeList(), eId); if (eId == 0) {
			 * expandEdgeArray(getExpandedEdgeCount()); eId =
			 * allocateIndex(getFreeEdgeList(), eId); }
			 */
			// changed for easier integration of transaction support; expanding
			// is done internally if needed; returned id is valid for sure
			eId = allocateEdgeIndex(eId);
			assert eId != 0;
			e.setId(eId);
			a.appendIncidenceToLambaSeq(e);
			o.appendIncidenceToLambaSeq(e.reversedEdge);
		}
		appendEdgeToESeq(e);

		if (!isLoading()) {
			a.incidenceListModified();
			o.incidenceListModified();
			edgeListModified();
			internalEdgeAdded(e);
		}
	}

	protected void internalEdgeAdded(EdgeImpl e) {
		edgeAdded(e);
	}

	/**
	 * Checks whether the <code>GraphElement</code> with the given Id can be
	 * added.
	 * 
	 * @param graphElementId
	 * 
	 * @return
	 */
	protected boolean canAddGraphElement(int graphElementId) {
		if (graphElementId != 0) {
			return false;
		}
		return true;
	}

	/*
	 * adds the given vertex object to this graph. if the vertex' id is 0, a
	 * valid id is set, otherwise the vertex' current id is used if possible.
	 * Should only be used by m1-Graphs derived from Graph. To create a new
	 * Vertex as user, use the appropriate methods from the derived Graphs like
	 * <code>createStreet(...)</code>
	 * 
	 * @param newVertex the Vertex to add
	 * 
	 * @throws GraphException if a vertex with the same id already exists
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
			if (!canAddGraphElement(vId)) {
				throw new GraphException("can not add a vertex with id != 0");
			}

			/*
			 * vId = allocateIndex(getFreeVertexList(), vId); if (vId == 0) {
			 * expandVertexArray(getExpandedVertexCount()); vId =
			 * allocateIndex(getFreeVertexList(), vId); }
			 */
			// changed for easier integration of transaction support; expanding
			// is done internally if needed; returned id is valid for sure
			vId = allocateVertexIndex(vId);
			assert vId != 0;
			v.setId(vId);
		}

		appendVertexToVSeq(v);

		if (!isLoading()) {
			vertexListModified();
			internalVertexAdded(v);
		}
	}

	protected void internalVertexAdded(VertexImpl v) {
		vertexAdded(v);
	}

	/**
	 * Appends the edge e to the global edge sequence of this graph.
	 * 
	 * @param e
	 *            an edge
	 */
	protected void appendEdgeToESeq(EdgeImpl e) {
		// moved from below to first place here - needed for working of
		// transaction support
		getEdge()[e.id] = e;
		getRevEdge()[e.id] = e.reversedEdge;
		setECount(getECount() + 1);
		if (getFirstEdge() == null) {
			setFirstEdgeInGraph(e);
		}
		if (getLastEdgeInGraph() != null) {
			((EdgeImpl) getLastEdgeInGraph()).setNextEdgeInGraph(e);
			e.setPrevEdgeInGraph(getLastEdgeInGraph());
		}
		setLastEdgeInGraph(e);
		// getEdge()[((EdgeImpl) e).id] = e;
		// getRevEdge()[((EdgeImpl) e).id] = e.reversedEdge;
		// setECount(getECount() + 1);
	}

	/**
	 * Appends the vertex v to the global vertex sequence of this graph.
	 * 
	 * @param v
	 *            a vertex
	 */
	protected void appendVertexToVSeq(VertexImpl v) {
		// moved from below to first place here - needed for working of
		// transaction support
		getVertex()[v.id] = v;
		setVCount(getVCount() + 1);
		if (getFirstVertex() == null) {
			setFirstVertex(v);
		}
		if (getLastVertex() != null) {
			((VertexImpl) getLastVertex()).setNextVertex(v);
			v.setPrevVertex(getLastVertex());
		}
		setLastVertex(v);
		// getVertex()[((VertexImpl) v).id] = v;
		// setVCount(getVCount() + 1);
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
	 * @see
	 * de.uni_koblenz.jgralab.Graph#containsEdge(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public final boolean containsEdge(Edge e) {
		return (e != null)
				&& (e.getGraph() == this)
				&& containsEdgeId(((EdgeImpl) e.getNormalEdge()).id)
				&& (getEdge(((EdgeImpl) e.getNormalEdge()).id) == e
						.getNormalEdge());
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
		return (eId > 0) && (eId <= eMax) && (getEdge()[eId] != null)
				&& (getRevEdge()[eId] != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Graph#containsVertex(de.uni_koblenz.jgralab.Vertex
	 */
	@Override
	public final boolean containsVertex(Vertex v) {
		VertexImpl[] vertex = getVertex();
		return (v != null) && (v.getGraph() == this)
				&& containsVertexId(((VertexImpl) v).id)
				&& (vertex[((VertexImpl) v).id] == v);
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
		return (vId > 0) && (vId <= vMax) && (getVertex()[vId] != null);
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
			Edge e = internalCreateEdge(cls);
			addEdge(e, alpha, omega);
			return (T) e;
		} catch (Exception ex) {
			throw new GraphException("Error creating edge of class "
					+ cls.getName(), ex);
		}
	}

	protected Edge internalCreateEdge(Class<? extends Edge> cls) {
		return graphFactory.createEdge(cls, 0, this);
	}

	/**
	 * Creates a vertex of the given class and adds this edge to the graph.
	 * <code>cls</code> has to be the "Impl" class.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Vertex> T createVertex(Class<T> cls) {
		try {
			Vertex v = internalCreateVertex(cls);
			addVertex(v);
			return (T) v;
		} catch (Exception ex) {
			throw new GraphException("Error creating vertex of class "
					+ cls.getName(), ex);
		}
	}

	protected Vertex internalCreateVertex(Class<? extends Vertex> cls) {
		return graphFactory.createVertex(cls, 0, this);
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
	 * @see
	 * de.uni_koblenz.jgralab.Graph#deleteVertex(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void deleteVertex(Vertex v) {
		assert v.isValid();
		getDeleteVertexList().add((VertexImpl) v);
		internalDeleteVertex();
		// moved call of vertexListModified to internalDeleteVertex to make sure
		// everytime!!! a vertex is deleted vertexListModified is called
		// vertexListModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Graph#edgeDeleted(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void edgeDeleted(Edge e) {
	}

	/**
	 * Callback function for triggered actions just after the edge
	 * <code>e</code> was deleted from this Graph. Override this method to
	 * implement user-defined behaviour upon deletion of edges. Note that any
	 * changes to this graph are forbidden.
	 * 
	 * Needed for transaction support.
	 * 
	 * @param e
	 *            the deleted Edge
	 * @param oldAlpha
	 *            the alpha-vertex before deletion
	 * @param oldOmega
	 *            the omega-vertex before deletion
	 */
	protected void edgeAfterDeleted(Edge e, Vertex oldAlpha, Vertex oldOmega) {
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
		setEdgeListVersion(getEdgeListVersion() + 1);
		setGraphVersion(getGraphVersion() + 1);
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
	 * @see
	 * de.uni_koblenz.jgralab.Graph#edges(de.uni_koblenz.jgralab.schema.EdgeClass
	 * )
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
		if (getEdge() != null) {
			System.arraycopy(getEdge(), 0, e, 0, getEdge().length);
		}
		setEdge(e);

		ReversedEdgeImpl[] r = new ReversedEdgeImpl[newSize + 1];

		if (getRevEdge() != null) {
			System.arraycopy(getRevEdge(), 0, r, 0, getRevEdge().length);
		}

		setRevEdge(r);
		if (getFreeEdgeList() == null) {
			setFreeEdgeList(new FreeIndexList(newSize));
		} else {
			getFreeEdgeList().expandBy(newSize - eMax);
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
		if (getVertex() != null) {
			System.arraycopy(getVertex(), 0, expandedArray, 0,
					getVertex().length);
		}

		if (getFreeVertexList() == null) {
			setFreeVertexList(new FreeIndexList(newSize));
		} else {
			getFreeVertexList().expandBy(newSize - vMax);
		}
		setVertex(expandedArray);
		vMax = newSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getECount()
	 */
	@Override
	abstract public int getECount();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getEdge(int)
	 */
	@Override
	public Edge getEdge(int eId) {
		assert (((eId < 0) && (-eId <= eMax)) || ((eId > 0) && (eId <= eMax)));
		return eId < 0 ? getRevEdge()[-eId] : getEdge()[eId];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getEdgeListVersion()
	 */
	@Override
	abstract public long getEdgeListVersion();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstEdgeInGraph()
	 */
	@Override
	abstract public Edge getFirstEdgeInGraph();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getLastEdgeInGraph()
	 */
	@Override
	abstract public Edge getLastEdgeInGraph();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Graph#getFirstEdgeOfClassInGraph(java.lang.Class)
	 */
	@Override
	public Edge getFirstEdgeOfClassInGraph(Class<? extends Edge> edgeClass) {
		return getFirstEdgeOfClassInGraph(edgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Graph#getFirstEdgeOfClassInGraph(java.lang.Class,
	 * boolean)
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
	 * @see
	 * de.uni_koblenz.jgralab.Graph#getFirstEdgeOfClassInGraph(de.uni_koblenz
	 * .jgralab.schema.EdgeClass)
	 */
	@Override
	public Edge getFirstEdgeOfClassInGraph(EdgeClass edgeClass) {
		return getFirstEdgeOfClassInGraph(edgeClass.getM1Class(), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Graph#getFirstEdgeOfClassInGraph(de.uni_koblenz
	 * .jgralab.schema.EdgeClass, boolean)
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
	abstract public Vertex getFirstVertex();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getLastVertex()
	 */
	@Override
	abstract public Vertex getLastVertex();

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
	 * boolean)
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
	 * @see
	 * de.uni_koblenz.jgralab.Graph#getFirstVertexOfClass(de.uni_koblenz.jgralab
	 * .schema.VertexClass)
	 */
	@Override
	public Vertex getFirstVertexOfClass(VertexClass vertexClass) {
		return getFirstVertexOfClass(vertexClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Graph#getFirstVertexOfClass(de.uni_koblenz.jgralab
	 * .schema.VertexClass, boolean)
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
	abstract public long getGraphVersion();

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
	abstract public int getVCount();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getVertex(int)
	 */
	@Override
	public Vertex getVertex(int vId) {
		assert ((vId > 0) && (vId <= vMax)) : "Called getVertex with ID "
				+ vId
				+ " which is "
				+ (vId <= 0 ? "too small." : "bigger than vMax (" + vMax + ").");
		return getVertex()[vId];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getVertexListVersion()
	 */
	@Override
	abstract public long getVertexListVersion();

	/**
	 * Changes this graph's version. graphModified() is called whenever the
	 * graph is changed, all changes like adding, creating and reordering of
	 * edges and vertices or changes of attributes of the graph, an edge or a
	 * vertex are treated as a change.
	 */
	public void graphModified() {
		setGraphVersion(getGraphVersion() + 1);
	}

	/**
	 * Deletes the edge from the internal structures of this graph.
	 * 
	 * @param edge
	 *            an edge
	 */
	private void internalDeleteEdge(Edge edge) {
		EdgeImpl e = (EdgeImpl) edge.getNormalEdge();
		internalEdgeDeleted(e);

		VertexImpl alpha = e.getIncidentVertex();
		alpha.removeIncidenceFromLambaSeq(e);
		alpha.incidenceListModified();

		VertexImpl omega = e.reversedEdge.getIncidentVertex();
		omega.removeIncidenceFromLambaSeq(e.reversedEdge);
		omega.incidenceListModified();

		removeEdgeFromESeq(e);
		edgeAfterDeleted(e, alpha, omega);
	}

	protected void internalEdgeDeleted(EdgeImpl e) {
		edgeDeleted(e);
	}

	/**
	 * Deletes all vertices in deleteVertexList from the internal structures of
	 * this graph. Possibly, cascading deletes of child vertices occur when
	 * parent vertices of Composition classes are deleted.
	 */
	private void internalDeleteVertex() {
		while (!getDeleteVertexList().isEmpty()) {
			VertexImpl v = getDeleteVertexList().remove(0);
			internalVertexDeleted(v);
			removeVertexFromVSeq(v);
			vertexListModified();
			vertexAfterDeleted(v);

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
								&& !getDeleteVertexList().contains(omega)) {
							// System.err.println("Delete omega vertex v" +
							// omegaId + "
							// of composition e" + eId);
							getDeleteVertexList().add(omega);
							// TODO check this!!!
							// omega.delete();
						}
					} else {
						VertexImpl alpha = (VertexImpl) e.getAlpha();
						if (containsVertex(alpha)
								&& !getDeleteVertexList().contains(alpha)) {
							// System.err.println("Delete alpha vertex v" +
							// alphaId + "
							// of composition e" + eId);
							getDeleteVertexList().add(alpha);
							// TODO check this!!!
							// alpha.delete();
						}
					}
				}

				// internalDeleteEdge(e);
				// calling deleteEdge instead of internalDeleteEdge, so that
				// deletion is notified within transaction support
				deleteEdge(e);
				e = v.getFirstEdge();
			}
		}
	}

	protected void internalVertexDeleted(VertexImpl v) {
		vertexDeleted(v);
	}

	/**
	 * Removes the vertex v from the global vertex sequence of this graph.
	 * 
	 * @param v
	 *            a vertex
	 */
	private final void removeVertexFromVSeq(VertexImpl v) {
		if (v == getFirstVertex()) {
			// delete at head of vertex list
			setFirstVertex((VertexImpl) v.getNextVertex());
			if (getFirstVertex() != null) {
				((VertexImpl) getFirstVertex()).setPrevVertex(null);
			}
			if (v == getLastVertex()) {
				// this vertex was the only one...
				setLastVertex(null);
			}
		} else if (v == getLastVertex()) {
			// delete at tail of vertex list
			setLastVertex((VertexImpl) v.getPrevVertex());
			if (getLastVertex() != null) {
				((VertexImpl) getLastVertex()).setNextVertex(null);
			}
		} else {
			// delete somewhere in the middle
			((VertexImpl) v.getPrevVertex()).setNextVertex(v.getNextVertex());
			((VertexImpl) v.getNextVertex()).setPrevVertex(v.getPrevVertex());
		}
		// freeIndex(getFreeVertexList(), v.getId());
		freeVertexIndex(v.getId());
		getVertex()[v.getId()] = null;
		(v).setPrevVertex(null);
		(v).setNextVertex(null);
		v.setId(0);
		setVCount(getVCount() - 1);
	}

	/**
	 * Removes the edge e from the global edge sequence of this graph.
	 * 
	 * @param e
	 *            an edge
	 */
	private final void removeEdgeFromESeq(EdgeImpl e) {
		if (e == getFirstEdge()) {
			// delete at head of edge list
			setFirstEdgeInGraph((EdgeImpl) e.getNextEdgeInGraph());
			if (getFirstEdge() != null) {
				getFirstEdge().setPrevEdgeInGraph(null);
			}
			if (e == getLastEdgeInGraph()) {
				// this edge was the only one...
				setLastEdgeInGraph(null);
			}
		} else if (e == getLastEdgeInGraph()) {
			// delete at tail of edge list
			setLastEdgeInGraph((EdgeImpl) e.getPrevEdgeInGraph());
			if (getLastEdgeInGraph() != null) {
				((EdgeImpl) getLastEdgeInGraph()).setNextEdgeInGraph(null);
			}
		} else {
			// delete somewhere in the middle
			((EdgeImpl) e.getPrevEdgeInGraph()).setNextEdgeInGraph(e
					.getNextEdgeInGraph());
			((EdgeImpl) e.getNextEdgeInGraph()).setPrevEdgeInGraph(e
					.getPrevEdgeInGraph());
		}

		// freeIndex(getFreeEdgeList(), e.getId());
		freeEdgeIndex(e.getId());
		getEdge()[e.getId()] = null;
		getRevEdge()[e.getId()] = null;
		e.setPrevEdgeInGraph(null);
		e.setNextEdgeInGraph(null);
		e.setId(0);
		setECount(getECount() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#isEdgeListModified(long)
	 */
	@Override
	public boolean isEdgeListModified(long edgeListVersion) {
		return (this.getEdgeListVersion() != edgeListVersion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#isGraphModified(long)
	 */
	@Override
	public boolean isGraphModified(long previousVersion) {
		return (getGraphVersion() != previousVersion);
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
		return (this.getVertexListVersion() != previousVersion);
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
		getFreeVertexList().reinitialize(getVertex());
		getFreeEdgeList().reinitialize(getEdge());
		for (int vId = 1; vId < getVertex().length; ++vId) {
			VertexImpl v = getVertex()[vId];
			if (v != null) {
				int eId = firstIncidence[vId];
				while (eId != 0) {
					v.appendIncidenceToLambaSeq(eId < 0 ? getRevEdge()[-eId]
							: getEdge()[eId]);
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
	protected void putEdgeAfterInGraph(EdgeImpl targetEdge, EdgeImpl movedEdge) {
		assert (targetEdge.isValid() && movedEdge.isValid());

		if (targetEdge == movedEdge) {
			throw new GraphException("an edge can't be put after itself");
		}

		if (targetEdge.getNextEdgeInGraph() == movedEdge) {
			return;
		}

		assert getFirstEdge() != getLastEdgeInGraph();

		// remove moved edge from eSeq
		if (movedEdge == getFirstEdge()) {
			setFirstEdgeInGraph((EdgeImpl) movedEdge.getNextEdgeInGraph());
			((EdgeImpl) movedEdge.getNextEdgeInGraph())
					.setPrevEdgeInGraph(null);
		} else if (movedEdge == getLastEdgeInGraph()) {
			setLastEdgeInGraph((EdgeImpl) movedEdge.getPrevEdgeInGraph());
			((EdgeImpl) movedEdge.getPrevEdgeInGraph())
					.setNextEdgeInGraph(null);
		} else {
			((EdgeImpl) movedEdge.getPrevEdgeInGraph())
					.setNextEdgeInGraph(movedEdge.getNextEdgeInGraph());
			((EdgeImpl) movedEdge.getNextEdgeInGraph())
					.setPrevEdgeInGraph(movedEdge.getPrevEdgeInGraph());
		}

		// insert moved edge in eSeq immediately after target
		if (targetEdge == getLastEdgeInGraph()) {
			setLastEdgeInGraph(movedEdge);
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
	protected void putVertexAfter(VertexImpl targetVertex,
			VertexImpl movedVertex) {
		assert (targetVertex.isValid() && movedVertex.isValid());

		if (targetVertex == movedVertex) {
			throw new GraphException("a vertex can't be put after itself");
		}

		Vertex nextVertex = targetVertex.getNextVertex();
		if (nextVertex == movedVertex) {
			return;
		}

		assert getFirstVertex() != getLastVertex();

		// remove moved vertex from vSeq
		if (movedVertex == getFirstVertex()) {
			VertexImpl newFirstVertex = (VertexImpl) movedVertex
					.getNextVertex();
			setFirstVertex(newFirstVertex);
			newFirstVertex.setPrevVertex(null);
			// ((VertexImpl) movedVertex.getNextVertex()).setPrevVertex(null);
		} else if (movedVertex == getLastVertex()) {
			setLastVertex((VertexImpl) movedVertex.getPrevVertex());
			((VertexImpl) movedVertex.getPrevVertex()).setNextVertex(null);
		} else {
			((VertexImpl) movedVertex.getPrevVertex())
					.setNextVertex(movedVertex.getNextVertex());
			((VertexImpl) movedVertex.getNextVertex())
					.setPrevVertex(movedVertex.getPrevVertex());
		}

		// insert moved vertex in vSeq immediately after target
		if (targetVertex == getLastVertex()) {
			setLastVertex(movedVertex);
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
	protected void putEdgeBeforeInGraph(EdgeImpl targetEdge, EdgeImpl movedEdge) {
		assert (targetEdge.isValid() && movedEdge.isValid());

		if (targetEdge == movedEdge) {
			throw new GraphException("an edge can't be put before itself");
		}

		if (targetEdge.getPrevEdgeInGraph() == movedEdge) {
			return;
		}

		assert getFirstEdge() != getLastEdgeInGraph();

		// remove moved edge from eSeq
		if (movedEdge == getFirstEdge()) {
			setFirstEdgeInGraph((EdgeImpl) movedEdge.getNextEdgeInGraph());
			((EdgeImpl) movedEdge.getNextEdgeInGraph())
					.setPrevEdgeInGraph(null);
		} else if (movedEdge == getLastEdgeInGraph()) {
			setLastEdgeInGraph((EdgeImpl) movedEdge.getPrevEdgeInGraph());
			((EdgeImpl) movedEdge.getPrevEdgeInGraph())
					.setNextEdgeInGraph(null);
		} else {
			((EdgeImpl) movedEdge.getPrevEdgeInGraph())
					.setNextEdgeInGraph(movedEdge.getNextEdgeInGraph());
			((EdgeImpl) movedEdge.getNextEdgeInGraph())
					.setPrevEdgeInGraph(movedEdge.getPrevEdgeInGraph());
		}

		// insert moved edge in eSeq immediately before target
		if (targetEdge == getFirstEdge()) {
			setFirstEdgeInGraph(movedEdge);
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
	protected void putVertexBefore(VertexImpl targetVertex,
			VertexImpl movedVertex) {
		assert (targetVertex.isValid() && movedVertex.isValid());

		if (targetVertex == movedVertex) {
			throw new GraphException("a vertex can't be put before itself");
		}

		Vertex prevVertex = targetVertex.getPrevVertex();
		if (prevVertex == movedVertex) {
			return;
		}

		assert getFirstVertex() != getLastVertex();

		// remove moved vertex from vSeq
		if (movedVertex == getFirstVertex()) {
			setFirstVertex((VertexImpl) movedVertex.getNextVertex());
			((VertexImpl) movedVertex.getNextVertex()).setPrevVertex(null);
		} else if (movedVertex == getLastVertex()) {
			setLastVertex((VertexImpl) movedVertex.getPrevVertex());
			((VertexImpl) movedVertex.getPrevVertex()).setNextVertex(null);
		} else {
			((VertexImpl) movedVertex.getPrevVertex())
					.setNextVertex(movedVertex.getNextVertex());
			((VertexImpl) movedVertex.getNextVertex())
					.setPrevVertex(movedVertex.getPrevVertex());
		}

		// insert moved vertex in vSeq immediately before target
		if (targetVertex == getFirstVertex()) {
			setFirstVertex(movedVertex);
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
	abstract public void setGraphVersion(long graphVersion);

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
	 * @see
	 * de.uni_koblenz.jgralab.Graph#vertexDeleted(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void vertexDeleted(Vertex v) {
	}

	/**
	 * Callback function for triggered actions just after the vertex
	 * <code>v</code> was deleted from this Graph. Override this method to
	 * implement user-defined behaviour upon deletion of vertices. Note that any
	 * changes to this graph are forbidden.
	 * 
	 * @param v
	 *            the deleted vertex
	 */
	abstract protected void vertexAfterDeleted(Vertex v);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Graph#vertexAdded(de.uni_koblenz.jgralab.Vertex)
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
		setVertexListVersion(getVertexListVersion() + 1);
		setGraphVersion(getGraphVersion() + 1);
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
	 * @seede.uni_koblenz.jgralab.Graph#vertices(de.uni_koblenz.jgralab.schema.
	 * VertexClass)
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
		if (getVCount() < vMax) {
			if (getVCount() > 0) {
				int vId = vMax;
				while (getFreeVertexList().isFragmented()) {
					while ((vId >= 1) && (getVertex()[vId] == null)) {
						--vId;
					}
					assert vId >= 1;
					VertexImpl v = getVertex()[vId];
					getVertex()[vId] = null;
					getFreeVertexList().freeIndex(vId);
					int newId = allocateVertexIndex(vId);
					assert newId < vId;
					v.setId(newId);
					getVertex()[newId] = v;
					--vId;
				}
			}
			int newVMax = (getVCount() == 0 ? 1 : getVCount());
			if (newVMax != vMax) {
				vMax = newVMax;
				VertexImpl[] newVertex = new VertexImpl[vMax + 1];
				System
						.arraycopy(getVertex(), 0, newVertex, 0,
								newVertex.length);
				setVertex(newVertex);
			}
			graphModified();
			System.gc();
		}
		// defragment edge array
		if (getECount() < eMax) {
			if (getECount() > 0) {
				int eId = eMax;
				while (getFreeEdgeList().isFragmented()) {
					while ((eId >= 1) && (getEdge()[eId] == null)) {
						--eId;
					}
					assert eId >= 1;
					EdgeImpl e = getEdge()[eId];
					getEdge()[eId] = null;
					// ReversedEdgeImpl r = getRevEdge()[eId];
					// getRevEdge()[eId] = null;
					getFreeEdgeList().freeIndex(eId);
					int newId = allocateEdgeIndex(eId);
					assert newId < eId;
					e.setId(newId);
					getEdge()[newId] = e;
					// getRevEdge()[newId] = r;
					--eId;
				}
			}
			int newEMax = (getECount() == 0 ? 1 : getECount());
			if (newEMax != eMax) {
				eMax = newEMax;
				EdgeImpl[] newEdge = new EdgeImpl[eMax + 1];
				System.arraycopy(getEdge(), 0, newEdge, 0, newEdge.length);
				setEdge(newEdge);
				System.gc();
			}
			graphModified();
			System.gc();
		}
	}

	private EdgeImpl getFirstEdge() {
		return (EdgeImpl) getFirstEdgeInGraph();
	}

	// access to <code>FreeIndexList</code>s with these functions
	// abstract protected void freeIndex(FreeIndexList freeIndexList, int
	// index);

	/**
	 * Use to free an <code>Edge</code>-index
	 * 
	 * @param index
	 */
	abstract protected void freeEdgeIndex(int index);

	/**
	 * Use to free a <code>Vertex</code>-index.
	 * 
	 * @param index
	 */
	abstract protected void freeVertexIndex(int index);

	/**
	 * Use to allocate a <code>Vertex</code>-index.
	 * 
	 * @param currentId
	 *            needed for transaction support
	 */
	abstract protected int allocateVertexIndex(int currentId);

	/**
	 * Use to allocate a <code>Edge</code>-index.
	 * 
	 * @param currentId
	 *            needed for transaction support
	 */
	abstract protected int allocateEdgeIndex(int currentId);

	/**
	 * 
	 * @param freeVertexList
	 */
	protected void setFreeVertexList(FreeIndexList freeVertexList) {
		this.freeVertexList = freeVertexList;
	}

	/**
	 * 
	 * @param freeEdgeList
	 */
	protected void setFreeEdgeList(FreeIndexList freeEdgeList) {
		this.freeEdgeList = freeEdgeList;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param recordClass
	 * @param io
	 * @return
	 */
	public <T> T createRecord(Class<T> recordClass, GraphIO io) {
		T record = null;
		try {
			Constructor<T> cons = recordClass.getDeclaredConstructor(Graph.class, GraphIO.class);
			cons.setAccessible(true);
			record = cons.newInstance(this, io);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return record;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param recordClass
	 * @param io
	 * @return
	 */
	public <T> T createRecord(Class<T> recordClass, Map<String, Object> fields) {
		T record = null;
		try {
			Constructor<T> cons = recordClass.getDeclaredConstructor(Graph.class, Map.class);
			cons.setAccessible(true);
			record = cons.newInstance(this, fields);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return record;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param recordClass
	 * @param io
	 * @return
	 */
	public <T> T createRecord(Class<T> recordClass, Object... components) {
		T record = null;
		try {
			Constructor<T> cons = recordClass.getDeclaredConstructor(Graph.class, Object[].class);
			cons.setAccessible(true);
			record = cons.newInstance(this, components);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return record;
	}
}
