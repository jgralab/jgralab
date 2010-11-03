/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */

package de.uni_koblenz.jgralab.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphStructureChangedListener;
import de.uni_koblenz.jgralab.GraphStructureChangedListenerWithAutoRemove;
import de.uni_koblenz.jgralab.RandomIdGenerator;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
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
public abstract class GraphBaseImpl implements Graph {

	// ------------- GRAPH VARIABLES -------------

	/**
	 * the unique id of the graph in the schema
	 */
	private String id;

	/**
	 * The schema this graph belongs to
	 */
	private final Schema schema;

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
	protected int vMax;

	/**
	 * number of vertices in the graph
	 */
	abstract protected void setVCount(int count);

	/**
	 * indexed with vertex-id, holds the actual vertex-object itself
	 */
	abstract protected VertexBaseImpl[] getVertex();

	abstract protected void setVertex(VertexBaseImpl[] vertex);

	/**
	 * free index list for vertices
	 */
	protected FreeIndexList freeVertexList;

	abstract protected FreeIndexList getFreeVertexList();

	/**
	 * holds the id of the first vertex in Vseq
	 */
	abstract protected void setFirstVertex(VertexBaseImpl firstVertex);

	/**
	 * holds the id of the last vertex in Vseq
	 */
	abstract protected void setLastVertex(VertexBaseImpl lastVertex);

	/**
	 * Sets version of VSeq if it is different than previous version.
	 * 
	 * @param vertexListVersion
	 *            Version of VSeq.
	 */
	abstract protected void setVertexListVersion(long vertexListVersion);

	/**
	 * List of vertices to be deleted by a cascading delete caused by deletion
	 * of a composition "parent".
	 */
	abstract protected List<VertexBaseImpl> getDeleteVertexList();

	abstract protected void setDeleteVertexList(
			List<VertexBaseImpl> deleteVertexList);

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
	abstract protected EdgeBaseImpl[] getEdge();

	abstract protected void setEdge(EdgeBaseImpl[] edge);

	abstract protected ReversedEdgeBaseImpl[] getRevEdge();

	abstract protected void setRevEdge(ReversedEdgeBaseImpl[] revEdge);

	/**
	 * free index list for edges
	 */
	protected FreeIndexList freeEdgeList;

	abstract protected FreeIndexList getFreeEdgeList();

	/**
	 * holds the id of the first edge in Eseq
	 */
	abstract protected void setFirstEdgeInGraph(EdgeBaseImpl firstEdge);

	/**
	 * holds the id of the last edge in Eseq
	 */
	abstract protected void setLastEdgeInGraph(EdgeBaseImpl lastEdge);

	/**
	 * Sets version of ESeq.
	 * 
	 * @param edgeListVersion
	 *            Version to set.
	 */
	abstract protected void setEdgeListVersion(long edgeListVersion);

	/**
	 * Creates a graph of the given GraphClass with the given id
	 * 
	 * @param id
	 *            this Graph's id
	 * @param cls
	 *            the GraphClass of this Graph
	 */
	protected GraphBaseImpl(String id, GraphClass cls) {
		this(id, cls, 1000, 1000);
	}

	@Override
	public void initializeAttributesWithDefaultValues() {
		for (Attribute attr : getAttributedElementClass().getAttributeList()) {
			try {
				if ((attr.getDefaultValueAsString() != null)
						&& !attr.getDefaultValueAsString().isEmpty()) {
					internalSetDefaultValue(attr);
				}
			} catch (GraphIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param attr
	 * @throws GraphIOException
	 */
	protected void internalSetDefaultValue(Attribute attr)
			throws GraphIOException {
		attr.setDefaultValue(this);
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
	protected GraphBaseImpl(String id, GraphClass cls, int vMax, int eMax) {
		if (vMax < 1) {
			throw new GraphException("vMax must not be less than 1", null);
		}
		if (eMax < 1) {
			throw new GraphException("eMax must not be less than 1", null);
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
		setDeleteVertexList(new LinkedList<VertexBaseImpl>());

		expandEdgeArray(eMax);
		setFirstEdgeInGraph(null);
		setLastEdgeInGraph(null);
		setECount(0);
	}

	/**
	 * Adds an edge to this graph. If the edges id is 0, a valid id is set,
	 * otherwise the edges current id is used if possible. Should only be used
	 * by m1-Graphs derived from Graph. To create a new Edge as user, use the
	 * appropriate methods from the derived Graphs like
	 * <code>createStreet(...)</code>
	 * 
	 * @param newEdge
	 *            Edge to add
	 * @param alpha
	 *            Vertex new edge should start at.
	 * @param omega
	 *            Vertex new edge should end at.
	 * @throws GraphException
	 *             vertices do not suit the edge, an edge with same id already
	 *             exists in graph, id of edge greater than possible count of
	 *             edges in graph
	 */
	protected void addEdge(Edge newEdge, Vertex alpha, Vertex omega) {
		assert newEdge != null;
		assert (alpha != null) && alpha.isValid() && containsVertex(alpha) : "Alpha vertex is invalid";
		assert (omega != null) && omega.isValid() && containsVertex(omega) : "Omega vertex is invalid";
		assert newEdge.isNormal() : "Can't add reversed edge";
		assert (alpha.getSchema() == omega.getSchema())
				&& (alpha.getSchema() == schema)
				&& (newEdge.getSchema() == schema) : "The schemas of alpha, omega, newEdge and this graph don't match!";
		assert (alpha.getGraph() == omega.getGraph())
				&& (alpha.getGraph() == this) && (newEdge.getGraph() == this) : "The graph of alpha, omega, newEdge and this graph don't match!";

		EdgeBaseImpl e = (EdgeBaseImpl) newEdge;
		VertexBaseImpl a = (VertexBaseImpl) alpha;
		if (!a.isValidAlpha(e)) {
			throw new GraphException("Edges of class "
					+ e.getAttributedElementClass().getQualifiedName()
					+ " may not start at vertices of class "
					+ a.getAttributedElementClass().getQualifiedName());
		}

		VertexBaseImpl o = (VertexBaseImpl) omega;
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
			eId = allocateEdgeIndex(eId);
			assert eId != 0;
			e.setId(eId);
			a.appendIncidenceToLambdaSeq(e);
			o.appendIncidenceToLambdaSeq(e.reversedEdge);
		}
		appendEdgeToESeq(e);
		if (!isLoading()) {
			a.incidenceListModified();
			o.incidenceListModified();
			edgeListModified();
			internalEdgeAdded(e);
		}
	}

	protected void internalEdgeAdded(EdgeBaseImpl e) {
		notifyEdgeAdded(e);
	}

	/*
	 * Adds a vertex to this graph. If the vertex' id is 0, a valid id is set,
	 * otherwise the vertex' current id is used if possible. Should only be used
	 * by m1-Graphs derived from Graph. To create a new Vertex as user, use the
	 * appropriate methods from the derived Graphs like
	 * <code>createStreet(...)</code>
	 * 
	 * @param newVertex the Vertex to add
	 * 
	 * @throws GraphException if a vertex with the same id already exists
	 */
	protected void addVertex(Vertex newVertex) {
		VertexBaseImpl v = (VertexBaseImpl) newVertex;

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
				throw new GraphException("can not add a vertex with vId " + vId);
			}
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

	protected void internalVertexAdded(VertexBaseImpl v) {
		notifyVertexAdded(v);
	}

	/**
	 * Appends the edge e to the global edge sequence of this graph.
	 * 
	 * @param e
	 *            an edge
	 */
	protected void appendEdgeToESeq(EdgeBaseImpl e) {
		getEdge()[e.id] = e;
		getRevEdge()[e.id] = e.reversedEdge;
		setECount(getECount() + 1);
		if (getFirstEdgeInGraph() == null) {
			setFirstEdgeInGraph(e);
		}
		if (getLastEdgeInGraph() != null) {
			((EdgeBaseImpl) getLastEdgeInGraph()).setNextEdgeInGraph(e);
			e.setPrevEdgeInGraph(getLastEdgeInGraph());
		}
		setLastEdgeInGraph(e);
	}

	/**
	 * Appends the vertex v to the global vertex sequence of this graph.
	 * 
	 * @param v
	 *            a vertex
	 */
	protected void appendVertexToVSeq(VertexBaseImpl v) {
		getVertex()[v.id] = v;
		setVCount(getVCount() + 1);
		if (getFirstVertex() == null) {
			setFirstVertex(v);
		}
		if (getLastVertex() != null) {
			((VertexBaseImpl) getLastVertex()).setNextVertex(v);
			v.setPrevVertex(getLastVertex());
		}
		setLastVertex(v);
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
		return n >= 1048576 ? n + 131072 : n >= 262144 ? n + 262144 : n + n;
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
			return hashCode() - g.hashCode();
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
	public boolean containsEdge(Edge e) {
		return (e != null)
				&& (e.getGraph() == this)
				&& containsEdgeId(((EdgeBaseImpl) e.getNormalEdge()).id)
				&& (getEdge(((EdgeBaseImpl) e.getNormalEdge()).id) == e
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
	public boolean containsVertex(Vertex v) {
		VertexBaseImpl[] vertex = getVertex();
		return (v != null) && (v.getGraph() == this)
				&& containsVertexId(((VertexBaseImpl) v).id)
				&& (vertex[((VertexBaseImpl) v).id] == v);
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
			return (T) internalCreateEdge(cls, alpha, omega);
		} catch (Exception exception) {
			if (exception instanceof GraphException) {
				throw (GraphException) exception;
			} else {
				throw new GraphException("Error creating edge of class "
						+ cls.getName(), exception);
			}
		}
	}

	protected Edge internalCreateEdge(Class<? extends Edge> cls, Vertex alpha,
			Vertex omega) {
		return graphFactory.createEdge(cls, 0, this, alpha, omega);
	}

	/**
	 * Creates a vertex of the given class and adds this edge to the graph.
	 * <code>cls</code> has to be the "Impl" class.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Vertex> T createVertex(Class<T> cls) {
		try {
			return (T) internalCreateVertex(cls);
		} catch (Exception ex) {
			if (ex instanceof GraphException) {
				throw (GraphException) ex;
			}
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
		assert (e != null) && e.isValid() && containsEdge(e);
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
		assert (v != null) && v.isValid() && containsVertex(v);

		getDeleteVertexList().add((VertexBaseImpl) v);
		internalDeleteVertex();
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

		EdgeBaseImpl[] e = new EdgeBaseImpl[newSize + 1];
		if (getEdge() != null) {
			System.arraycopy(getEdge(), 0, e, 0, getEdge().length);
		}
		setEdge(e);

		ReversedEdgeBaseImpl[] r = new ReversedEdgeBaseImpl[newSize + 1];

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
		notifyMaxEdgeCountIncreased(newSize);
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
		VertexBaseImpl[] expandedArray = new VertexBaseImpl[newSize + 1];
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
		notifyMaxVertexCountIncreased(newSize);
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
		assert eId != 0 : "The edge id must be != 0, given was " + eId;
		try {
			return eId < 0 ? getRevEdge()[-eId] : getEdge()[eId];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
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
		assert edgeClass != null;
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
		assert edgeClass != null;
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
		assert edgeClass != null;
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
	public Edge getFirstEdgeOfClassInGraph(EdgeClass edgeClass,
			boolean noSubclasses) {
		assert edgeClass != null;
		return getFirstEdgeOfClassInGraph(edgeClass.getM1Class(), noSubclasses);
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
		assert vertexClass != null;
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
		assert vertexClass != null;
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
		assert vertexClass != null;
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
		assert vertexClass != null;
		return getFirstVertexOfClass(vertexClass.getM1Class(), noSubclasses);
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
	abstract public int getVCount();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getVertex(int)
	 */
	@Override
	public Vertex getVertex(int vId) {
		assert (vId > 0) : "The vertex id must be > 0, given was " + vId;
		try {
			return getVertex()[vId];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
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
		assert (edge != null) && edge.isValid() && containsEdge(edge);

		EdgeBaseImpl e = (EdgeBaseImpl) edge.getNormalEdge();
		internalEdgeDeleted(e);

		VertexBaseImpl alpha = e.getIncidentVertex();
		alpha.removeIncidenceFromLambdaSeq(e);
		alpha.incidenceListModified();

		VertexBaseImpl omega = e.reversedEdge.getIncidentVertex();
		omega.removeIncidenceFromLambdaSeq(e.reversedEdge);
		omega.incidenceListModified();

		removeEdgeFromESeq(e);
		edgeAfterDeleted(e, alpha, omega);
	}

	protected void internalEdgeDeleted(EdgeBaseImpl e) {
		assert e != null;
		notifyEdgeDeleted(e);
	}

	/**
	 * Deletes all vertices in deleteVertexList from the internal structures of
	 * this graph. Possibly, cascading deletes of child vertices occur when
	 * parent vertices of Composition classes are deleted.
	 */
	private void internalDeleteVertex() {
		while (!getDeleteVertexList().isEmpty()) {
			VertexBaseImpl v = getDeleteVertexList().remove(0);
			assert (v != null) && v.isValid() && containsVertex(v);
			internalVertexDeleted(v);
			// delete all incident edges including incidence objects
			Edge e = v.getFirstEdge();
			while (e != null) {
				assert e.isValid() && containsEdge(e);
				if (e.getThatSemantics() == AggregationKind.COMPOSITE) {
					// check for cascading delete of vertices in incident
					// composition edges
					VertexBaseImpl other = (VertexBaseImpl) e.getThat();
					if ((other != v) && containsVertex(other)
							&& !getDeleteVertexList().contains(other)) {
						getDeleteVertexList().add(other);
					}
				}
				deleteEdge(e);
				e = v.getFirstEdge();
			}
			removeVertexFromVSeq(v);
			vertexListModified();
			vertexAfterDeleted(v);
		}
	}

	protected void internalVertexDeleted(VertexBaseImpl v) {
		assert v != null;
		notifyVertexDeleted(v);
	}

	/**
	 * Removes the vertex v from the global vertex sequence of this graph.
	 * 
	 * @param v
	 *            a vertex
	 */
	protected void removeVertexFromVSeq(VertexBaseImpl v) {
		assert v != null;
		if (v == getFirstVertex()) {
			// delete at head of vertex list
			setFirstVertex((VertexBaseImpl) v.getNextVertex());
			if (getFirstVertex() != null) {
				((VertexBaseImpl) getFirstVertex()).setPrevVertex(null);
			}
			if (v == getLastVertex()) {
				// this vertex was the only one...
				setLastVertex(null);
			}
		} else if (v == getLastVertex()) {
			// delete at tail of vertex list
			setLastVertex((VertexBaseImpl) v.getPrevVertex());
			if (getLastVertex() != null) {
				((VertexBaseImpl) getLastVertex()).setNextVertex(null);
			}
		} else {
			// delete somewhere in the middle
			((VertexBaseImpl) v.getPrevVertex()).setNextVertex(v
					.getNextVertex());
			((VertexBaseImpl) v.getNextVertex()).setPrevVertex(v
					.getPrevVertex());
		}
		// freeIndex(getFreeVertexList(), v.getId());
		freeVertexIndex(v.getId());
		getVertex()[v.getId()] = null;
		v.setPrevVertex(null);
		v.setNextVertex(null);
		v.setId(0);
		setVCount(getVCount() - 1);
	}

	/**
	 * Removes the edge e from the global edge sequence of this graph.
	 * 
	 * @param e
	 *            an edge
	 */
	protected void removeEdgeFromESeq(EdgeBaseImpl e) {
		assert e != null;
		removeEdgeFromESeqWithoutDeletingIt(e);

		// freeIndex(getFreeEdgeList(), e.getId());
		freeEdgeIndex(e.getId());
		getEdge()[e.getId()] = null;
		getRevEdge()[e.getId()] = null;
		e.setPrevEdgeInGraph(null);
		e.setNextEdgeInGraph(null);
		e.setId(0);
		setECount(getECount() - 1);
	}

	private void removeEdgeFromESeqWithoutDeletingIt(EdgeBaseImpl e) {
		if (e == getFirstEdgeInGraph()) {
			// delete at head of edge list
			setFirstEdgeInGraph((EdgeBaseImpl) e.getNextEdgeInGraph());
			if (getFirstEdgeInGraph() != null) {
				((EdgeBaseImpl) getFirstEdgeInGraph()).setPrevEdgeInGraph(null);
			}
			if (e == getLastEdgeInGraph()) {
				// this edge was the only one...
				setLastEdgeInGraph(null);
			}
		} else if (e == getLastEdgeInGraph()) {
			// delete at tail of edge list
			setLastEdgeInGraph((EdgeBaseImpl) e.getPrevEdgeInGraph());
			if (getLastEdgeInGraph() != null) {
				((EdgeBaseImpl) getLastEdgeInGraph()).setNextEdgeInGraph(null);
			}
		} else {
			// delete somewhere in the middle
			((EdgeBaseImpl) e.getPrevEdgeInGraph()).setNextEdgeInGraph(e
					.getNextEdgeInGraph());
			((EdgeBaseImpl) e.getNextEdgeInGraph()).setPrevEdgeInGraph(e
					.getPrevEdgeInGraph());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#isEdgeListModified(long)
	 */
	@Override
	public boolean isEdgeListModified(long edgeListVersion) {
		return getEdgeListVersion() != edgeListVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#isGraphModified(long)
	 */
	@Override
	public boolean isGraphModified(long previousVersion) {
		return getGraphVersion() != previousVersion;
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
		return getVertexListVersion() != previousVersion;
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
			VertexBaseImpl v = getVertex()[vId];
			if (v != null) {
				int eId = firstIncidence[vId];
				while (eId != 0) {
					v.appendIncidenceToLambdaSeq(eId < 0 ? getRevEdge()[-eId]
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
	protected void putEdgeAfterInGraph(EdgeBaseImpl targetEdge,
			EdgeBaseImpl movedEdge) {
		assert (targetEdge != null) && targetEdge.isValid()
				&& containsEdge(targetEdge);
		assert (movedEdge != null) && movedEdge.isValid()
				&& containsEdge(movedEdge);
		assert targetEdge != movedEdge;

		if ((targetEdge == movedEdge)
				|| (targetEdge.getNextEdgeInGraph() == movedEdge)) {
			return;
		}

		assert getFirstEdgeInGraph() != getLastEdgeInGraph();

		// remove moved edge from eSeq
		if (movedEdge == getFirstEdgeInGraph()) {
			setFirstEdgeInGraph((EdgeBaseImpl) movedEdge.getNextEdgeInGraph());
			((EdgeBaseImpl) movedEdge.getNextEdgeInGraph())
					.setPrevEdgeInGraph(null);
		} else if (movedEdge == getLastEdgeInGraph()) {
			setLastEdgeInGraph((EdgeBaseImpl) movedEdge.getPrevEdgeInGraph());
			((EdgeBaseImpl) movedEdge.getPrevEdgeInGraph())
					.setNextEdgeInGraph(null);
		} else {
			((EdgeBaseImpl) movedEdge.getPrevEdgeInGraph())
					.setNextEdgeInGraph(movedEdge.getNextEdgeInGraph());
			((EdgeBaseImpl) movedEdge.getNextEdgeInGraph())
					.setPrevEdgeInGraph(movedEdge.getPrevEdgeInGraph());
		}

		// insert moved edge in eSeq immediately after target
		if (targetEdge == getLastEdgeInGraph()) {
			setLastEdgeInGraph(movedEdge);
			movedEdge.setNextEdgeInGraph(null);
		} else {
			((EdgeBaseImpl) targetEdge.getNextEdgeInGraph())
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
	protected void putVertexAfter(VertexBaseImpl targetVertex,
			VertexBaseImpl movedVertex) {
		assert (targetVertex != null) && targetVertex.isValid()
				&& containsVertex(targetVertex);
		assert (movedVertex != null) && movedVertex.isValid()
				&& containsVertex(movedVertex);
		assert targetVertex != movedVertex;

		Vertex nextVertex = targetVertex.getNextVertex();
		if ((targetVertex == movedVertex) || (nextVertex == movedVertex)) {
			return;
		}

		assert getFirstVertex() != getLastVertex();

		// remove moved vertex from vSeq
		if (movedVertex == getFirstVertex()) {
			VertexBaseImpl newFirstVertex = (VertexBaseImpl) movedVertex
					.getNextVertex();
			setFirstVertex(newFirstVertex);
			newFirstVertex.setPrevVertex(null);
			// ((VertexImpl) movedVertex.getNextVertex()).setPrevVertex(null);
		} else if (movedVertex == getLastVertex()) {
			setLastVertex((VertexBaseImpl) movedVertex.getPrevVertex());
			((VertexBaseImpl) movedVertex.getPrevVertex()).setNextVertex(null);
		} else {
			((VertexBaseImpl) movedVertex.getPrevVertex())
					.setNextVertex(movedVertex.getNextVertex());
			((VertexBaseImpl) movedVertex.getNextVertex())
					.setPrevVertex(movedVertex.getPrevVertex());
		}

		// insert moved vertex in vSeq immediately after target
		if (targetVertex == getLastVertex()) {
			setLastVertex(movedVertex);
			movedVertex.setNextVertex(null);
		} else {
			((VertexBaseImpl) targetVertex.getNextVertex())
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
	protected void putEdgeBeforeInGraph(EdgeBaseImpl targetEdge,
			EdgeBaseImpl movedEdge) {
		assert (targetEdge != null) && targetEdge.isValid()
				&& containsEdge(targetEdge);
		assert (movedEdge != null) && movedEdge.isValid()
				&& containsEdge(movedEdge);
		assert targetEdge != movedEdge;

		if ((targetEdge == movedEdge)
				|| (targetEdge.getPrevEdgeInGraph() == movedEdge)) {
			return;
		}

		assert getFirstEdgeInGraph() != getLastEdgeInGraph();

		removeEdgeFromESeqWithoutDeletingIt(movedEdge);

		// insert moved edge in eSeq immediately before target
		if (targetEdge == getFirstEdgeInGraph()) {
			setFirstEdgeInGraph(movedEdge);
			movedEdge.setPrevEdgeInGraph(null);
		} else {
			EdgeBaseImpl previousEdge = ((EdgeBaseImpl) targetEdge
					.getPrevEdgeInGraph());
			previousEdge.setNextEdgeInGraph(movedEdge);
			movedEdge.setPrevEdgeInGraph(previousEdge);
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
	protected void putVertexBefore(VertexBaseImpl targetVertex,
			VertexBaseImpl movedVertex) {
		assert (targetVertex != null) && targetVertex.isValid()
				&& containsVertex(targetVertex);
		assert (movedVertex != null) && movedVertex.isValid()
				&& containsVertex(movedVertex);
		assert targetVertex != movedVertex;

		Vertex prevVertex = targetVertex.getPrevVertex();
		if ((targetVertex == movedVertex) || (prevVertex == movedVertex)) {
			return;
		}

		assert getFirstVertex() != getLastVertex();

		// remove moved vertex from vSeq
		if (movedVertex == getFirstVertex()) {
			setFirstVertex((VertexBaseImpl) movedVertex.getNextVertex());
			((VertexBaseImpl) movedVertex.getNextVertex()).setPrevVertex(null);
		} else if (movedVertex == getLastVertex()) {
			setLastVertex((VertexBaseImpl) movedVertex.getPrevVertex());
			((VertexBaseImpl) movedVertex.getPrevVertex()).setNextVertex(null);
		} else {
			((VertexBaseImpl) movedVertex.getPrevVertex())
					.setNextVertex(movedVertex.getNextVertex());
			((VertexBaseImpl) movedVertex.getNextVertex())
					.setPrevVertex(movedVertex.getPrevVertex());
		}

		// insert moved vertex in vSeq immediately before target
		if (targetVertex == getFirstVertex()) {
			setFirstVertex(movedVertex);
			movedVertex.setPrevVertex(null);
		} else {
			VertexBaseImpl previousVertex = (VertexBaseImpl) targetVertex
					.getPrevVertex();
			previousVertex.setNextVertex(movedVertex);
			movedVertex.setPrevVertex(previousVertex);
		}
		movedVertex.setNextVertex(targetVertex);
		targetVertex.setPrevVertex(movedVertex);
		vertexListModified();
	}

	/**
	 * Sets the version counter of this graph. Should only be called immediately
	 * after loading.
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
					VertexBaseImpl v = getVertex()[vId];
					getVertex()[vId] = null;
					getFreeVertexList().freeIndex(vId);
					int newId = allocateVertexIndex(vId);
					assert newId < vId;
					v.setId(newId);
					getVertex()[newId] = v;
					--vId;
				}
			}
			int newVMax = getVCount() == 0 ? 1 : getVCount();
			if (newVMax != vMax) {
				vMax = newVMax;
				VertexBaseImpl[] newVertex = new VertexBaseImpl[vMax + 1];
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
					EdgeBaseImpl e = getEdge()[eId];
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
			int newEMax = getECount() == 0 ? 1 : getECount();
			if (newEMax != eMax) {
				eMax = newEMax;
				EdgeBaseImpl[] newEdge = new EdgeBaseImpl[eMax + 1];
				System.arraycopy(getEdge(), 0, newEdge, 0, newEdge.length);
				setEdge(newEdge);
				System.gc();
			}
			graphModified();
			System.gc();
		}
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
	 * A list of all registered <code>GraphStructureChangedListener</code> as
	 * <i>WeakReference</i>s.
	 */
	protected List<WeakReference<GraphStructureChangedListener>> graphStructureChangedListenersWithAutoRemoval;
	protected List<GraphStructureChangedListener> graphStructureChangedListeners;
	{
		graphStructureChangedListenersWithAutoRemoval = null;
		graphStructureChangedListeners = new ArrayList<GraphStructureChangedListener>();
	}

	private void lazyCreateGraphStructureChangedListenersWithAutoRemoval() {
		if (graphStructureChangedListenersWithAutoRemoval == null) {
			graphStructureChangedListenersWithAutoRemoval = new LinkedList<WeakReference<GraphStructureChangedListener>>();
		}
	}

	@Override
	public void addGraphStructureChangedListener(
			GraphStructureChangedListener newListener) {
		assert newListener != null;
		if (newListener instanceof GraphStructureChangedListenerWithAutoRemove) {
			lazyCreateGraphStructureChangedListenersWithAutoRemoval();
			graphStructureChangedListenersWithAutoRemoval
					.add(new WeakReference<GraphStructureChangedListener>(
							newListener));
		} else {
			graphStructureChangedListeners.add(newListener);
		}
	}

	@Override
	public void removeGraphStructureChangedListener(
			GraphStructureChangedListener listener) {
		assert listener != null;
		if (listener instanceof GraphStructureChangedListenerWithAutoRemove) {
			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
			while (iterator.hasNext()) {
				GraphStructureChangedListener currentListener = iterator.next()
						.get();
				if ((currentListener == null) || (currentListener == listener)) {
					iterator.remove();
				}
			}
		} else {
			Iterator<GraphStructureChangedListener> iterator = getListenerListIterator();
			while (iterator.hasNext()) {
				GraphStructureChangedListener currentListener = iterator.next();
				if (currentListener == listener) {
					iterator.remove();
				}
			}
		}
	}

	private void setAutoListenerListToNullIfEmpty() {
		if (graphStructureChangedListenersWithAutoRemoval.isEmpty()) {
			graphStructureChangedListenersWithAutoRemoval = null;
		}
	}

	@Override
	public void removeAllGraphStructureChangedListeners() {
		graphStructureChangedListenersWithAutoRemoval = null;
		graphStructureChangedListeners.clear();
	}

	@Override
	public int getGraphStructureChangedListenerCount() {
		return graphStructureChangedListenersWithAutoRemoval == null ? graphStructureChangedListeners
				.size()
				: graphStructureChangedListenersWithAutoRemoval.size()
						+ graphStructureChangedListeners.size();
	}

	private Iterator<WeakReference<GraphStructureChangedListener>> getListenerListIteratorForAutoRemove() {
		return graphStructureChangedListenersWithAutoRemoval.iterator();
	}

	private Iterator<GraphStructureChangedListener> getListenerListIterator() {
		return graphStructureChangedListeners.iterator();
	}

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the given vertex <code>v</code> is about to be deleted. All invalid
	 * <code>WeakReference</code>s are deleted automatically from the internal
	 * listener list.
	 * 
	 * @param v
	 *            the vertex that is about to be deleted.
	 */
	protected void notifyVertexDeleted(Vertex v) {
		assert (v != null) && v.isValid() && containsVertex(v);
		if (graphStructureChangedListenersWithAutoRemoval != null) {
			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
			while (iterator.hasNext()) {
				GraphStructureChangedListener currentListener = iterator.next()
						.get();
				if (currentListener == null) {
					iterator.remove();
				} else {
					currentListener.vertexDeleted(v);
				}
			}
			setAutoListenerListToNullIfEmpty();
		}
		int n = graphStructureChangedListeners.size();
		for (int i = 0; i < n; i++) {
			graphStructureChangedListeners.get(i).vertexDeleted(v);
		}
	}

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the given vertex <code>v</code> has been created. All invalid
	 * <code>WeakReference</code>s are deleted automatically from the internal
	 * listener list.
	 * 
	 * @param v
	 *            the vertex that has been created.
	 */
	protected void notifyVertexAdded(Vertex v) {
		assert (v != null) && v.isValid() && containsVertex(v);
		if (graphStructureChangedListenersWithAutoRemoval != null) {
			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
			while (iterator.hasNext()) {
				GraphStructureChangedListener currentListener = iterator.next()
						.get();
				if (currentListener == null) {
					iterator.remove();
				} else {
					currentListener.vertexAdded(v);
				}
			}
			setAutoListenerListToNullIfEmpty();
		}
		int n = graphStructureChangedListeners.size();
		for (int i = 0; i < n; i++) {
			graphStructureChangedListeners.get(i).vertexAdded(v);
		}
	}

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the given edge <code>e</code> is about to be deleted. All invalid
	 * <code>WeakReference</code>s are deleted automatically from the internal
	 * listener list.
	 * 
	 * @param e
	 *            the edge that is about to be deleted.
	 */
	protected void notifyEdgeDeleted(Edge e) {
		assert (e != null) && e.isValid() && e.isNormal() && containsEdge(e);
		if (graphStructureChangedListenersWithAutoRemoval != null) {
			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
			while (iterator.hasNext()) {
				GraphStructureChangedListener currentListener = iterator.next()
						.get();
				if (currentListener == null) {
					iterator.remove();
				} else {
					currentListener.edgeDeleted(e);
				}
			}
			setAutoListenerListToNullIfEmpty();
		}
		int n = graphStructureChangedListeners.size();
		for (int i = 0; i < n; i++) {
			graphStructureChangedListeners.get(i).edgeDeleted(e);
		}
	}

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the given edge <code>e</code> has been created. All invalid
	 * <code>WeakReference</code>s are deleted automatically from the internal
	 * listener list.
	 * 
	 * @param e
	 *            the edge that has been created.
	 */
	protected void notifyEdgeAdded(Edge e) {
		assert (e != null) && e.isValid() && e.isNormal() && containsEdge(e);
		if (graphStructureChangedListenersWithAutoRemoval != null) {
			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
			while (iterator.hasNext()) {
				GraphStructureChangedListener currentListener = iterator.next()
						.get();
				if (currentListener == null) {
					iterator.remove();
				} else {
					currentListener.edgeAdded(e);
				}
			}
			setAutoListenerListToNullIfEmpty();
		}
		int n = graphStructureChangedListeners.size();
		for (int i = 0; i < n; i++) {
			graphStructureChangedListeners.get(i).edgeAdded(e);
		}
	}

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the maximum vertex count has been increased to the given
	 * <code>newValue</code>. All invalid <code>WeakReference</code>s are
	 * deleted automatically from the internal listener list.
	 * 
	 * @param newValue
	 *            the new maximum vertex count.
	 */
	protected void notifyMaxVertexCountIncreased(int newValue) {
		if (graphStructureChangedListenersWithAutoRemoval != null) {
			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
			while (iterator.hasNext()) {
				GraphStructureChangedListener currentListener = iterator.next()
						.get();
				if (currentListener == null) {
					iterator.remove();
				} else {
					currentListener.maxVertexCountIncreased(newValue);
				}
			}
			setAutoListenerListToNullIfEmpty();
		}
		int n = graphStructureChangedListeners.size();
		for (int i = 0; i < n; i++) {
			graphStructureChangedListeners.get(i).maxVertexCountIncreased(
					newValue);
		}
	}

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the maximum edge count has been increased to the given
	 * <code>newValue</code>. All invalid <code>WeakReference</code>s are
	 * deleted automatically from the internal listener list.
	 * 
	 * @param newValue
	 *            the new maximum edge count.
	 */
	protected void notifyMaxEdgeCountIncreased(int newValue) {
		if (graphStructureChangedListenersWithAutoRemoval != null) {
			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
			while (iterator.hasNext()) {
				GraphStructureChangedListener currentListener = iterator.next()
						.get();
				if (currentListener == null) {
					iterator.remove();
				} else {
					currentListener.maxEdgeCountIncreased(newValue);
				}
			}
			setAutoListenerListToNullIfEmpty();
		}
		int n = graphStructureChangedListeners.size();
		for (int i = 0; i < n; i++) {
			graphStructureChangedListeners.get(i).maxEdgeCountIncreased(
					newValue);
		}
	}

	protected boolean canAddGraphElement(int graphElementId) {
		return graphElementId == 0;
	}

}
