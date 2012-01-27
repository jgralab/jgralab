/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         http://jgralab.uni-koblenz.de
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphStructureChangedListener;
import de.uni_koblenz.jgralab.GraphStructureChangedListenerWithAutoRemove;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.RandomIdGenerator;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.ECARuleManagerInterface;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * Implementation of interface Graph with doubly linked lists realizing eSeq,
 * vSeq and lambdaSeq, while ensuring efficient direct access to vertices and
 * edges by id via vertex and edge arrays.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class GraphBaseImpl implements Graph, InternalGraph {

	// ------------- GRAPH VARIABLES -------------

	@Override
	public GraphClass getAttributedElementClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends Graph> getSchemaClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException {
		// TODO Auto-generated method stub

	}

	@Override
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T getAttribute(String name) throws NoSuchAttributeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void setAttribute(String name, T data)
			throws NoSuchAttributeException {
		// TODO Auto-generated method stub

	}

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
	 * free index list for vertices
	 */
	protected FreeIndexList freeVertexList;

	// ------------- EDGE LIST VARIABLES -------------

	/**
	 * maximum number of edges
	 */
	protected int eMax;

	/**
	 * free index list for edges
	 */
	protected FreeIndexList freeEdgeList;

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
				e.printStackTrace();
			}
		}
	}

	@Override
	public void internalSetDefaultValue(Attribute attr) throws GraphIOException {
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
		// graphFactory = schema.getGraphFactory();
		setId(id == null ? RandomIdGenerator.generateId() : id);
		// needed for initialization of graphVersion with transactions
		graphVersion = -1;
		setGraphVersion(0);

		expandVertexArray(vMax);
		setFirstVertex(null);
		setLastVertex(null);
		setVCount(0);
		setDeleteVertexList(new LinkedList<InternalVertex>());

		expandEdgeArray(eMax);
		setFirstEdgeInGraph(null);
		setLastEdgeInGraph(null);
		setECount(0);
	}

	// protected TraversalContext traversalContext;

	// @Override
	// public TraversalContext setTraversalContext(TraversalContext tc) {
	// TraversalContext result = traversalContext;
	// traversalContext = tc;
	// return result;
	// }
	//
	// public TraversalContext getTraversalContext() {
	// return traversalContext;
	// }

	@Override
	public void addEdge(Edge newEdge, Vertex alpha, Vertex omega) {
		assert newEdge != null;
		assert (alpha != null) && alpha.isValid() && vSeqContainsVertex(alpha) : "Alpha vertex is invalid";
		assert (omega != null) && omega.isValid() && vSeqContainsVertex(omega) : "Omega vertex is invalid";
		assert newEdge.isNormal() : "Can't add reversed edge";
		assert (alpha.getSchema() == omega.getSchema())
				&& (alpha.getSchema() == schema)
				&& (newEdge.getSchema() == schema) : "The schemas of alpha, omega, newEdge and this graph don't match!";
		assert (alpha.getGraph() == omega.getGraph())
				&& (alpha.getGraph() == this) && (newEdge.getGraph() == this) : "The graph of alpha, omega, newEdge and this graph don't match!";

		EdgeBaseImpl e = (EdgeBaseImpl) newEdge;
		InternalVertex a = (InternalVertex) alpha;
		if (!a.isValidAlpha(e)) {
			throw new GraphException("Edges of class "
					+ e.getAttributedElementClass().getQualifiedName()
					+ " may not start at vertices of class "
					+ a.getAttributedElementClass().getQualifiedName());
		}

		InternalVertex o = (InternalVertex) omega;
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
			a.appendIncidenceToISeq(e);
			o.appendIncidenceToISeq(e.reversedEdge);
		}
		appendEdgeToESeq(e);
		if (!isLoading()) {
			a.incidenceListModified();
			o.incidenceListModified();
			edgeListModified();
			internalEdgeAdded(e);
		}
	}

	@Override
	public void internalEdgeAdded(InternalEdge e) {
		notifyEdgeAdded(e);
		if (getECARuleManagerIfThere() != null) {
			getECARuleManagerIfThere().fireAfterCreateEdgeEvents(e);
		}
	}

	@Override
	public void addVertex(Vertex newVertex) {
		InternalVertex v = (InternalVertex) newVertex;

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

	@Override
	public void internalVertexAdded(InternalVertex v) {
		notifyVertexAdded(v);
		if (getECARuleManagerIfThere() != null) {
			getECARuleManager().fireAfterCreateVertexEvents(v);
		}
	}

	@Override
	public void appendEdgeToESeq(InternalEdge e) {
		getEdge()[((EdgeBaseImpl) e).id] = e;
		getRevEdge()[((EdgeBaseImpl) e).id] = ((EdgeBaseImpl) e).reversedEdge;
		setECount(getECountInESeq() + 1);
		if (getFirstEdgeInESeq() == null) {
			setFirstEdgeInGraph(e);
		}
		if (getLastEdgeInESeq() != null) {
			(getLastEdgeInESeq()).setNextEdgeInGraph(e);

			e.setPrevEdgeInGraph(getLastEdgeInESeq());

		}
		setLastEdgeInGraph(e);
	}

	@Override
	public void appendVertexToVSeq(InternalVertex v) {
		getVertex()[((VertexBaseImpl) v).id] = v;
		setVCount(getVCountInVSeq() + 1);
		if (getFirstVertexInVSeq() == null) {
			setFirstVertex(v);
		}
		if (getLastVertexInVSeq() != null) {
			(getLastVertexInVSeq()).setNextVertex(v);
			v.setPrevVertex(getLastVertexInVSeq());
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
	public int compareTo(AttributedElement<GraphClass, Graph> a) {
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
		return getTraversalContext() == null ? eSeqContainsEdge(e)
				: eSeqContainsEdge(e) && getTraversalContext().containsEdge(e);
	}

	@Override
	public boolean eSeqContainsEdge(Edge e) {
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
		return getTraversalContext() == null ? vSeqContainsVertex(v)
				: vSeqContainsVertex(v)
						&& getTraversalContext().containsVertex(v);
	}

	@Override
	public boolean vSeqContainsVertex(Vertex v) {
		return (v != null) && (v.getGraph() == this)
				&& containsVertexId(((VertexBaseImpl) v).id)
				&& (getVertex()[((VertexBaseImpl) v).id] == v);
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

	/**
	 * Creates an edge of the given {@link EdgeClass} and adds it to the graph.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Edge> T createEdge(EdgeClass ec, Vertex alpha,
			Vertex omega) {
		return (T) createEdge(ec.getSchemaClass(), alpha, omega);
	}

	@Override
	public Edge internalCreateEdge(Class<? extends Edge> cls, Vertex alpha,
			Vertex omega) {
		// TODO factory
		// return graphFactory.createEdge(cls, 0, this, alpha, omega);
		return null;
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

	/**
	 * Creates a vertex of the given {@link VertexClass} and adds it to the
	 * graph.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Vertex> T createVertex(VertexClass vc) {
		return (T) createVertex(vc.getSchemaClass());
	}

	@Override
	public Vertex internalCreateVertex(Class<? extends Vertex> cls) {
		// TODO factory
		// return graphFactory.createVertex(cls, 0, this);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#deleteEdge(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void deleteEdge(Edge e) {
		assert (e != null) && e.isValid() && eSeqContainsEdge(e);
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
		assert (v != null) && v.isValid() && vSeqContainsVertex(v);

		getDeleteVertexList().add((InternalVertex) v);
		internalDeleteVertex();
	}

	@Override
	public void edgeAfterDeleted(Edge e, Vertex oldAlpha, Vertex oldOmega) {
	}

	@Override
	public void edgeListModified() {
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
		return new EdgeIterable<Edge>(this, edgeClass.getSchemaClass());
	}

	/**
	 * Changes the size of the edge array of this graph to newSize.
	 * 
	 * @param newSize
	 *            the new size of the edge array
	 */
	@Override
	public void expandEdgeArray(int newSize) {
		if (newSize <= eMax) {
			throw new GraphException("newSize must be > eSize: eSize=" + eMax
					+ ", newSize=" + newSize);
		}

		InternalEdge[] e = new InternalEdge[newSize + 1];
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
	@Override
	public void expandVertexArray(int newSize) {
		if (newSize <= vMax) {
			throw new GraphException("newSize must > vSize: vSize=" + vMax
					+ ", newSize=" + newSize);
		}
		InternalVertex[] expandedArray = new InternalVertex[newSize + 1];
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
	public int getECount() {
		TraversalContext tc = getTraversalContext();

		if (tc == null) {
			return getECountInESeq();
		}

		// if TC is present, count edges

		if (tc instanceof SubGraphMarker) {
			return ((SubGraphMarker) tc).getECount();
		}

		int count = 0;
		for (Edge e = getFirstEdge(); e != null; e = e.getNextEdge()) {
			count++;
		}
		return count;
	}

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
	 * @see de.uni_koblenz.jgralab.Graph#getFirstEdgeInGraph()
	 */
	@Override
	public Edge getFirstEdge() {
		TraversalContext tc = getTraversalContext();
		Edge firstEdge = getFirstEdgeInESeq();
		if (!((tc == null) || (firstEdge == null) || tc.containsEdge(firstEdge))) {
			firstEdge = firstEdge.getNextEdge();
		}
		return firstEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getLastEdgeInGraph()
	 */
	@Override
	public Edge getLastEdge() {
		TraversalContext tc = getTraversalContext();
		Edge lastEdge = getLastEdgeInESeq();
		if (!((tc == null) || (lastEdge == null) || tc.containsEdge(lastEdge))) {
			lastEdge = lastEdge.getPrevEdge();
		}
		return lastEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Graph#getFirstEdgeOfClassInGraph(java.lang.Class)
	 */
	@Override
	public Edge getFirstEdge(Class<? extends Edge> edgeClass) {
		assert edgeClass != null;
		Edge currentEdge = getFirstEdge();
		if (currentEdge == null) {
			return null;
		}
		if (edgeClass.isInstance(currentEdge)) {
			return currentEdge;
		}
		return currentEdge.getNextEdge(edgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Graph#getFirstEdgeOfClassInGraph(de.uni_koblenz
	 * .jgralab.schema.EdgeClass)
	 */
	@Override
	public Edge getFirstEdge(EdgeClass edgeClass) {
		assert edgeClass != null;
		return getFirstEdge(edgeClass.getSchemaClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstVertex()
	 */
	@Override
	public Vertex getFirstVertex() {
		TraversalContext tc = getTraversalContext();
		Vertex firstVertex = getFirstVertexInVSeq();
		if (!((tc == null) || (firstVertex == null) || tc
				.containsVertex(firstVertex))) {
			firstVertex = firstVertex.getNextVertex();
		}
		return firstVertex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getLastVertex()
	 */
	@Override
	public Vertex getLastVertex() {
		TraversalContext tc = getTraversalContext();
		Vertex lastVertex = getLastVertexInVSeq();
		if (!((tc == null) || (lastVertex == null) || tc
				.containsVertex(lastVertex))) {
			lastVertex = lastVertex.getPrevVertex();
		}
		return lastVertex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#getFirstVertexOfClass(java.lang.Class)
	 */
	@Override
	public Vertex getFirstVertex(Class<? extends Vertex> vertexClass) {
		assert vertexClass != null;
		Vertex firstVertex = getFirstVertex();
		if (firstVertex == null) {
			return null;
		}
		if (vertexClass.isInstance(firstVertex)) {
			return firstVertex;
		}
		return firstVertex.getNextVertex(vertexClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Graph#getFirstVertexOfClass(de.uni_koblenz.jgralab
	 * .schema.VertexClass)
	 */
	@Override
	public Vertex getFirstVertex(VertexClass vertexClass) {
		assert vertexClass != null;
		return getFirstVertex(vertexClass.getSchemaClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.AttributedElement#getGraphClass()
	 */
	@Override
	public GraphClass getGraphClass() {
		return getAttributedElementClass();
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
		TraversalContext tc = getTraversalContext();
		if (tc == null) {
			return getVCountInVSeq();
		}

		// if TC is present, count vertices

		if (tc instanceof SubGraphMarker) {
			return ((SubGraphMarker) tc).getVCount();
		}

		int count = 0;
		for (Vertex v = getFirstVertex(); v != null; v = v.getNextVertex()) {
			count++;
		}
		return count;
	}

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
	 * @see de.uni_koblenz.jgralab.impl.InternalGraph#graphModified()
	 */
	@Override
	public void graphModified() {
		setGraphVersion(getGraphVersion() + 1);
	}

	/**
	 * Triggers ECA-rules before an Attribute is changed
	 * 
	 * @param name
	 *            of the changing Attribute
	 */
	@Override
	public void ecaAttributeChanging(String name, Object oldValue,
			Object newValue) {
		if (!isLoading()) {
			getECARuleManager().fireBeforeChangeAttributeEvents(this, name,
					oldValue, newValue);
		}
	}

	/**
	 * Triggers ECA-rule after an Attribute is changed
	 * 
	 * @param name
	 *            of the changed Attribute
	 */
	@Override
	public void ecaAttributeChanged(String name, Object oldValue,
			Object newValue) {
		if (!isLoading()) {
			getECARuleManager().fireAfterChangeAttributeEvents(this, name,
					oldValue, newValue);
		}
	}

	/**
	 * Deletes the edge from the internal structures of this graph.
	 * 
	 * @param edge
	 *            an edge
	 */
	private void internalDeleteEdge(Edge edge) {
		assert (edge != null) && edge.isValid() && eSeqContainsEdge(edge);

		getECARuleManager().fireBeforeDeleteEdgeEvents(edge);

		InternalEdge e = (InternalEdge) edge.getNormalEdge();
		if (getECARuleManagerIfThere() != null) {
			getECARuleManagerIfThere().fireBeforeDeleteEdgeEvents(edge);
		}

		e = (InternalEdge) edge.getNormalEdge();
		internalEdgeDeleted(e);

		InternalVertex alpha = e.getIncidentVertex();
		alpha.removeIncidenceFromISeq(e);
		alpha.incidenceListModified();

		InternalVertex omega = ((EdgeBaseImpl) e).reversedEdge
				.getIncidentVertex();
		omega.removeIncidenceFromISeq(((EdgeBaseImpl) e).reversedEdge);
		omega.incidenceListModified();

		removeEdgeFromESeq(e);
		edgeListModified();

		if (getECARuleManagerIfThere() != null) {
			getECARuleManagerIfThere().fireAfterDeleteEdgeEvents(
					e.getAttributedElementClass());
		}
		edgeAfterDeleted(e, alpha, omega);
	}

	@Override
	public void internalEdgeDeleted(InternalEdge e) {
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
			InternalVertex v = getDeleteVertexList().remove(0);
			assert (v != null) && v.isValid() && vSeqContainsVertex(v);

			if (getECARuleManagerIfThere() != null) {
				getECARuleManagerIfThere().fireBeforeDeleteVertexEvents(v);
			}
			internalVertexDeleted(v);
			// delete all incident edges including incidence objects
			Edge e = v.getFirstIncidence();
			while (e != null) {
				assert e.isValid() && eSeqContainsEdge(e);
				if (e.getThatAggregationKind() == AggregationKind.COMPOSITE) {
					// check for cascading delete of vertices in incident
					// composition edges
					InternalVertex other = (InternalVertex) e.getThat();
					if ((other != v) && vSeqContainsVertex(other)
							&& !getDeleteVertexList().contains(other)) {
						getDeleteVertexList().add(other);
					}
				}
				deleteEdge(e);
				e = v.getFirstIncidence();
			}
			removeVertexFromVSeq(v);
			vertexListModified();

			if (getECARuleManagerIfThere() != null) {
				getECARuleManagerIfThere().fireAfterDeleteVertexEvents(
						v.getAttributedElementClass());
			}
			vertexAfterDeleted(v);
		}
	}

	@Override
	public void internalVertexDeleted(InternalVertex v) {
		assert v != null;
		notifyVertexDeleted(v);
	}

	@Override
	public void removeVertexFromVSeq(InternalVertex v) {
		assert v != null;
		if (v == getFirstVertexInVSeq()) {
			// delete at head of vertex list
			setFirstVertex(v.getNextVertexInVSeq());
			if (getFirstVertexInVSeq() != null) {
				(getFirstVertexInVSeq()).setPrevVertex(null);
			}
			if (v == getLastVertexInVSeq()) {
				// this vertex was the only one...
				setLastVertex(null);
			}
		} else if (v == getLastVertexInVSeq()) {
			// delete at tail of vertex list
			setLastVertex(v.getPrevVertexInVSeq());
			if (getLastVertexInVSeq() != null) {
				(getLastVertexInVSeq()).setNextVertex(null);
			}
		} else {
			// delete somewhere in the middle
			(v.getPrevVertexInVSeq()).setNextVertex(v.getNextVertexInVSeq());
			(v.getNextVertexInVSeq()).setPrevVertex(v.getPrevVertexInVSeq());
		}
		// freeIndex(getFreeVertexList(), v.getId());
		freeVertexIndex(v.getId());
		getVertex()[v.getId()] = null;
		v.setPrevVertex(null);
		v.setNextVertex(null);
		v.setId(0);
		setVCount(getVCountInVSeq() - 1);
	}

	@Override
	public void removeEdgeFromESeq(InternalEdge e) {
		assert e != null;
		removeEdgeFromESeqWithoutDeletingIt(e);

		// freeIndex(getFreeEdgeList(), e.getId());
		freeEdgeIndex(e.getId());
		getEdge()[e.getId()] = null;
		getRevEdge()[e.getId()] = null;
		e.setPrevEdgeInGraph(null);
		e.setNextEdgeInGraph(null);
		e.setId(0);
		setECount(getECountInESeq() - 1);
	}

	private void removeEdgeFromESeqWithoutDeletingIt(InternalEdge e) {
		if (e == getFirstEdgeInESeq()) {
			// delete at head of edge list
			setFirstEdgeInGraph(e.getNextEdgeInESeq());
			if (getFirstEdgeInESeq() != null) {
				(getFirstEdgeInESeq()).setPrevEdgeInGraph(null);
			}
			if (e == getLastEdgeInESeq()) {
				// this edge was the only one...
				setLastEdgeInGraph(null);
			}
		} else if (e == getLastEdgeInESeq()) {
			// delete at tail of edge list
			setLastEdgeInGraph(e.getPrevEdgeInESeq());
			if (getLastEdgeInESeq() != null) {
				(getLastEdgeInESeq()).setNextEdgeInGraph(null);
			}
		} else {
			// delete somewhere in the middle
			(e.getPrevEdgeInESeq()).setNextEdgeInGraph(e.getNextEdgeInESeq());
			(e.getNextEdgeInESeq()).setPrevEdgeInGraph(e.getPrevEdgeInESeq());

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.impl.InternalGraph#internalLoadingCompleted(int[],
	 * int[])
	 */
	@Override
	public void internalLoadingCompleted(int[] firstIncidence,
			int[] nextIncidence) {
		getFreeVertexList().reinitialize(getVertex());
		getFreeEdgeList().reinitialize(getEdge());
		for (int vId = 1; vId < getVertex().length; ++vId) {
			InternalVertex v = getVertex()[vId];
			if (v != null) {
				int eId = firstIncidence[vId];
				while (eId != 0) {
					v.appendIncidenceToISeq(eId < 0 ? getRevEdge()[-eId]
							: getEdge()[eId]);
					eId = nextIncidence[eMax + eId];
				}
			}
		}
	}

	@Override
	public void putEdgeAfterInGraph(InternalEdge targetEdge,
			InternalEdge movedEdge) {
		assert (targetEdge != null) && targetEdge.isValid()
				&& eSeqContainsEdge(targetEdge);
		assert (movedEdge != null) && movedEdge.isValid()
				&& eSeqContainsEdge(movedEdge);
		assert targetEdge != movedEdge;

		if ((targetEdge == movedEdge)
				|| (targetEdge.getNextEdgeInESeq() == movedEdge)) {
			return;
		}

		assert getFirstEdgeInESeq() != getLastEdgeInESeq();

		// remove moved edge from eSeq
		if (movedEdge == getFirstEdgeInESeq()) {
			setFirstEdgeInGraph(movedEdge.getNextEdgeInESeq());
			(movedEdge.getNextEdgeInESeq()).setPrevEdgeInGraph(null);
		} else if (movedEdge == getLastEdgeInESeq()) {
			setLastEdgeInGraph(movedEdge.getPrevEdgeInESeq());
			(movedEdge.getPrevEdgeInESeq()).setNextEdgeInGraph(null);
		} else {
			(movedEdge.getPrevEdgeInESeq()).setNextEdgeInGraph(movedEdge
					.getNextEdgeInESeq());
			(movedEdge.getNextEdgeInESeq()).setPrevEdgeInGraph(movedEdge
					.getPrevEdgeInESeq());

		}

		// insert moved edge in eSeq immediately after target
		if (targetEdge == getLastEdgeInESeq()) {
			setLastEdgeInGraph(movedEdge);
			movedEdge.setNextEdgeInGraph(null);
		} else {
			(targetEdge.getNextEdgeInESeq()).setPrevEdgeInGraph(movedEdge);
			movedEdge.setNextEdgeInGraph(targetEdge.getNextEdgeInESeq());
		}
		movedEdge.setPrevEdgeInGraph(targetEdge);

		targetEdge.setNextEdgeInGraph(movedEdge);
		edgeListModified();
	}

	@Override
	public void putVertexAfter(InternalVertex targetVertex,
			InternalVertex movedVertex) {
		assert (targetVertex != null) && targetVertex.isValid()
				&& vSeqContainsVertex(targetVertex);
		assert (movedVertex != null) && movedVertex.isValid()
				&& vSeqContainsVertex(movedVertex);
		assert targetVertex != movedVertex;

		Vertex nextVertex = targetVertex.getNextVertexInVSeq();
		if ((targetVertex == movedVertex) || (nextVertex == movedVertex)) {
			return;
		}

		assert getFirstVertexInVSeq() != getLastVertexInVSeq();

		// remove moved vertex from vSeq
		if (movedVertex == getFirstVertexInVSeq()) {
			InternalVertex newFirstVertex = movedVertex.getNextVertexInVSeq();
			setFirstVertex(newFirstVertex);
			newFirstVertex.setPrevVertex(null);
			// ((VertexImpl)
			// movedVertex.getNextVertex()).setPrevVertex(null);

		} else if (movedVertex == getLastVertexInVSeq()) {
			setLastVertex(movedVertex.getPrevVertexInVSeq());
			(movedVertex.getPrevVertexInVSeq()).setNextVertex(null);
		} else {
			(movedVertex.getPrevVertexInVSeq()).setNextVertex(movedVertex
					.getNextVertexInVSeq());
			(movedVertex.getNextVertexInVSeq()).setPrevVertex(movedVertex
					.getPrevVertexInVSeq());

		}

		// insert moved vertex in vSeq immediately after target
		if (targetVertex == getLastVertexInVSeq()) {
			setLastVertex(movedVertex);
			movedVertex.setNextVertex(null);
		} else {
			(targetVertex.getNextVertexInVSeq()).setPrevVertex(movedVertex);

			movedVertex.setNextVertex(targetVertex.getNextVertexInVSeq());
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
	@Override
	public void putEdgeBeforeInGraph(InternalEdge targetEdge,
			InternalEdge movedEdge) {
		assert (targetEdge != null) && targetEdge.isValid()
				&& eSeqContainsEdge(targetEdge);
		assert (movedEdge != null) && movedEdge.isValid()
				&& eSeqContainsEdge(movedEdge);
		assert targetEdge != movedEdge;

		if ((targetEdge == movedEdge)
				|| (targetEdge.getPrevEdgeInESeq() == movedEdge)) {
			return;
		}

		assert getFirstEdgeInESeq() != getLastEdgeInESeq();

		removeEdgeFromESeqWithoutDeletingIt(movedEdge);

		// insert moved edge in eSeq immediately before target
		if (targetEdge == getFirstEdgeInESeq()) {
			setFirstEdgeInGraph(movedEdge);
			movedEdge.setPrevEdgeInGraph(null);

		} else {
			InternalEdge previousEdge = (targetEdge.getPrevEdgeInESeq());
			previousEdge.setNextEdgeInGraph(movedEdge);
			movedEdge.setPrevEdgeInGraph(previousEdge);

		}
		movedEdge.setNextEdgeInGraph(targetEdge);
		targetEdge.setPrevEdgeInGraph(movedEdge);

		edgeListModified();
	}

	@Override
	public void putVertexBefore(InternalVertex targetVertex,
			InternalVertex movedVertex) {
		assert (targetVertex != null) && targetVertex.isValid()
				&& vSeqContainsVertex(targetVertex);
		assert (movedVertex != null) && movedVertex.isValid()
				&& vSeqContainsVertex(movedVertex);
		assert targetVertex != movedVertex;

		Vertex prevVertex = targetVertex.getPrevVertexInVSeq();
		if ((targetVertex == movedVertex) || (prevVertex == movedVertex)) {
			return;
		}

		assert getFirstVertexInVSeq() != getLastVertexInVSeq();

		// remove moved vertex from vSeq
		if (movedVertex == getFirstVertexInVSeq()) {
			setFirstVertex(movedVertex.getNextVertexInVSeq());
			(movedVertex.getNextVertexInVSeq()).setPrevVertex(null);

		} else if (movedVertex == getLastVertexInVSeq()) {
			setLastVertex(movedVertex.getPrevVertexInVSeq());
			(movedVertex.getPrevVertexInVSeq()).setNextVertex(null);
		} else {
			(movedVertex.getPrevVertexInVSeq()).setNextVertex(movedVertex
					.getNextVertexInVSeq());
			(movedVertex.getNextVertexInVSeq()).setPrevVertex(movedVertex
					.getPrevVertexInVSeq());

		}

		// insert moved vertex in vSeq immediately before target
		if (targetVertex == getFirstVertexInVSeq()) {
			setFirstVertex(movedVertex);
			movedVertex.setPrevVertex(null);
		} else {
			InternalVertex previousVertex = targetVertex.getPrevVertexInVSeq();
			previousVertex.setNextVertex(movedVertex);
			movedVertex.setPrevVertex(previousVertex);
		}
		movedVertex.setNextVertex(targetVertex);
		targetVertex.setPrevVertex(movedVertex);

		vertexListModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.impl.InternalGraph#setGraphVersion(long)
	 */
	@Override
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.impl.InternalGraph#setLoading(boolean)
	 */
	@Override
	public void setLoading(boolean isLoading) {
		loading = isLoading;
	}

	@Override
	public void vertexListModified() {
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
		return new VertexIterable<Vertex>(this, vertexClass.getSchemaClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Graph#defragment()
	 */
	@Override
	public void defragment() {
		// TODO is tc really required to be removed for defragmentation?
		TraversalContext tc = setTraversalContext(null);
		try {
			// defragment vertex array
			if (getVCountInVSeq() < vMax) {
				if (getVCountInVSeq() > 0) {
					int vId = vMax;
					while (getFreeVertexList().isFragmented()) {
						while ((vId >= 1) && (getVertex()[vId] == null)) {
							--vId;
						}
						assert vId >= 1;
						InternalVertex v = getVertex()[vId];
						getVertex()[vId] = null;
						getFreeVertexList().freeIndex(vId);
						int newId = allocateVertexIndex(vId);
						assert newId < vId;
						v.setId(newId);
						getVertex()[newId] = v;
						--vId;
					}
				}
				int newVMax = getVCountInVSeq() == 0 ? 1 : getVCountInVSeq();
				if (newVMax != vMax) {
					vMax = newVMax;
					InternalVertex[] newVertex = new InternalVertex[vMax + 1];
					System.arraycopy(getVertex(), 0, newVertex, 0,
							newVertex.length);
					setVertex(newVertex);
				}
				graphModified();
				System.gc();
			}
			// defragment edge array
			if (getECountInESeq() < eMax) {
				if (getECountInESeq() > 0) {
					int eId = eMax;
					while (getFreeEdgeList().isFragmented()) {
						while ((eId >= 1) && (getEdge()[eId] == null)) {
							--eId;
						}
						assert eId >= 1;
						InternalEdge e = getEdge()[eId];
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
				int newEMax = getECountInESeq() == 0 ? 1 : getECountInESeq();
				if (newEMax != eMax) {
					eMax = newEMax;
					InternalEdge[] newEdge = new InternalEdge[eMax + 1];
					System.arraycopy(getEdge(), 0, newEdge, 0, newEdge.length);
					setEdge(newEdge);
					System.gc();
				}
				graphModified();
				System.gc();
			}
		} finally {
			setTraversalContext(tc);
		}
	}

	// access to <code>FreeIndexList</code>s with these functions
	// abstract protected void freeIndex(FreeIndexList freeIndexList, int
	// index);

	@Override
	public void setFreeVertexList(FreeIndexList freeVertexList) {
		this.freeVertexList = freeVertexList;
	}

	@Override
	public void setFreeEdgeList(FreeIndexList freeEdgeList) {
		this.freeEdgeList = freeEdgeList;
	}

	// sort vertices
	@Override
	public void sortVertices(Comparator<Vertex> comp) {

		if (getFirstVertexInVSeq() == null) {
			// no sorting required for empty vertex lists
			return;
		}
		class VertexList {
			InternalVertex first;
			InternalVertex last;

			public void add(InternalVertex v) {
				if (first == null) {
					first = v;
					assert (last == null);
					last = v;
				} else {
					v.setPrevVertex(last);
					last.setNextVertex(v);
					last = v;
				}
				v.setNextVertex(null);
			}

			public InternalVertex remove() {
				if (first == null) {
					throw new NoSuchElementException();
				}
				InternalVertex out;
				if (first == last) {
					out = first;
					first = null;
					last = null;
					return out;
				}
				out = first;
				first = out.getNextVertexInVSeq();
				first.setPrevVertex(null);
				return out;
			}

			public boolean isEmpty() {
				assert ((first == null) == (last == null));
				return first == null;
			}

		}

		VertexList a = new VertexList();
		VertexList b = new VertexList();
		VertexList out = a;

		// split
		InternalVertex last;
		VertexList l = new VertexList();
		l.first = getFirstVertexInVSeq();
		l.last = getLastVertexInVSeq();

		out.add(last = l.remove());
		while (!l.isEmpty()) {
			InternalVertex current = l.remove();
			if (comp.compare(current, last) < 0) {
				out = (out == a) ? b : a;
			}
			out.add(current);
			last = current;
		}
		if (a.isEmpty() || b.isEmpty()) {
			out = a.isEmpty() ? b : a;
			setFirstVertex(out.first);
			setLastVertex(out.last);
			return;
		}

		while (true) {
			if (a.isEmpty() || b.isEmpty()) {
				out = a.isEmpty() ? b : a;
				setFirstVertex(out.first);
				setLastVertex(out.last);
				edgeListModified();
				return;
			}

			VertexList c = new VertexList();
			VertexList d = new VertexList();
			out = c;

			last = null;
			while (!a.isEmpty() && !b.isEmpty()) {
				int compareAToLast = last != null ? comp.compare(a.first, last)
						: 0;
				int compareBToLast = last != null ? comp.compare(b.first, last)
						: 0;

				if ((compareAToLast >= 0) && (compareBToLast >= 0)) {
					if (comp.compare(a.first, b.first) <= 0) {
						out.add(last = a.remove());
					} else {
						out.add(last = b.remove());
					}
				} else if ((compareAToLast < 0) && (compareBToLast < 0)) {
					out = (out == c) ? d : c;
					last = null;
				} else if ((compareAToLast < 0) && (compareBToLast >= 0)) {
					out.add(last = b.remove());
				} else {
					out.add(last = a.remove());
				}
			}

			// copy rest of A
			while (!a.isEmpty()) {
				InternalVertex current = a.remove();
				if (comp.compare(current, last) < 0) {
					out = (out == c) ? d : c;
				}
				out.add(current);
				last = current;
			}

			// copy rest of B
			while (!b.isEmpty()) {
				InternalVertex current = b.remove();
				if (comp.compare(current, last) < 0) {
					out = (out == c) ? d : c;
				}
				out.add(current);
				last = current;
			}

			a = c;
			b = d;
		}

	}

	@Override
	public Object getEnumConstant(EnumDomain enumDomain, String constantName) {
		Class<?> cls = enumDomain.getSchemaClass();
		Enum<?>[] consts = (Enum<?>[]) cls.getEnumConstants();
		for (int i = 0; i < consts.length; i++) {
			Enum<?> c = consts[i];
			if (c.name().equals(constantName)) {
				return c;
			}
		}
		throw new GraphException("No such enum constant '" + constantName
				+ "' in EnumDomain " + enumDomain);
	}

	@Override
	public Record createRecord(RecordDomain recordDomain,
			Map<String, Object> values) {
		Class<?> cls = recordDomain.getSchemaClass();
		try {
			Constructor<?> constr = cls.getConstructor(Map.class);
			return (Record) constr.newInstance(values);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GraphException(e);
		}
	}

	// sort edges

	@Override
	public void sortEdges(Comparator<Edge> comp) {

		if (getFirstEdgeInESeq() == null) {
			// no sorting required for empty edge lists
			return;
		}
		class EdgeList {
			InternalEdge first;
			InternalEdge last;

			public void add(InternalEdge e) {
				if (first == null) {
					first = e;
					assert (last == null);
					last = e;
				} else {
					e.setPrevEdgeInGraph(last);
					last.setNextEdgeInGraph(e);
					last = e;
				}
				e.setNextEdgeInGraph(null);
			}

			public InternalEdge remove() {
				if (first == null) {
					throw new NoSuchElementException();
				}
				InternalEdge out;
				if (first == last) {
					out = first;
					first = null;
					last = null;
					return out;
				}
				out = first;
				first = out.getNextEdgeInESeq();
				first.setPrevEdgeInGraph(null);

				return out;
			}

			public boolean isEmpty() {
				assert ((first == null) == (last == null));
				return first == null;
			}

		}

		EdgeList a = new EdgeList();
		EdgeList b = new EdgeList();
		EdgeList out = a;

		// split
		InternalEdge last;
		EdgeList l = new EdgeList();
		l.first = getFirstEdgeInESeq();
		l.last = getLastEdgeInESeq();

		out.add(last = l.remove());
		while (!l.isEmpty()) {
			InternalEdge current = l.remove();
			if (comp.compare(current, last) < 0) {
				out = (out == a) ? b : a;
			}
			out.add(current);
			last = current;
		}
		if (a.isEmpty() || b.isEmpty()) {
			out = a.isEmpty() ? b : a;
			setFirstEdgeInGraph(out.first);
			setLastEdgeInGraph(out.last);
			return;
		}

		while (true) {
			if (a.isEmpty() || b.isEmpty()) {
				out = a.isEmpty() ? b : a;
				setFirstEdgeInGraph(out.first);
				setLastEdgeInGraph(out.last);
				edgeListModified();
				return;
			}

			EdgeList c = new EdgeList();
			EdgeList d = new EdgeList();
			out = c;

			last = null;
			while (!a.isEmpty() && !b.isEmpty()) {
				int compareAToLast = last != null ? comp.compare(a.first, last)
						: 0;
				int compareBToLast = last != null ? comp.compare(b.first, last)
						: 0;

				if ((compareAToLast >= 0) && (compareBToLast >= 0)) {
					if (comp.compare(a.first, b.first) <= 0) {
						out.add(last = a.remove());
					} else {
						out.add(last = b.remove());
					}
				} else if ((compareAToLast < 0) && (compareBToLast < 0)) {
					out = (out == c) ? d : c;
					last = null;
				} else if ((compareAToLast < 0) && (compareBToLast >= 0)) {
					out.add(last = b.remove());
				} else {
					out.add(last = a.remove());
				}
			}

			// copy rest of A
			while (!a.isEmpty()) {
				InternalEdge current = a.remove();
				if (comp.compare(current, last) < 0) {
					out = (out == c) ? d : c;
				}
				out.add(current);
				last = current;
			}

			// copy rest of B
			while (!b.isEmpty()) {
				InternalEdge current = b.remove();
				if (comp.compare(current, last) < 0) {
					out = (out == c) ? d : c;
				}
				out.add(current);
				last = current;
			}

			a = c;
			b = d;
		}

	}

	// ECA Rules
	private ECARuleManagerInterface ecaRuleManager;

	@Override
	public ECARuleManagerInterface getECARuleManager() {
		if (ecaRuleManager == null) {
			Constructor<?> ruleManagerConstructor;
			try {
				ruleManagerConstructor = Class.forName(
						"de.uni_koblenz.jgralab.eca.ECARuleManager")
						.getConstructor(Graph.class);
				ecaRuleManager = (ECARuleManagerInterface) ruleManagerConstructor
						.newInstance(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			assert ecaRuleManager != null;
		}
		return ecaRuleManager;
	}

	@Override
	public ECARuleManagerInterface getECARuleManagerIfThere() {
		return ecaRuleManager;
	}

	// handle GraphStructureChangedListener

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
			while ((iterator != null) && iterator.hasNext()) {
				GraphStructureChangedListener currentListener = iterator.next()
						.get();
				if ((currentListener == null) || (currentListener == listener)) {
					iterator.remove();
				}
			}
		} else {
			Iterator<GraphStructureChangedListener> iterator = getListenerListIterator();
			while ((iterator != null) && iterator.hasNext()) {
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
				.size() : graphStructureChangedListenersWithAutoRemoval.size()
				+ graphStructureChangedListeners.size();
	}

	private Iterator<WeakReference<GraphStructureChangedListener>> getListenerListIteratorForAutoRemove() {
		return graphStructureChangedListenersWithAutoRemoval != null ? graphStructureChangedListenersWithAutoRemoval
				.iterator() : null;
	}

	private Iterator<GraphStructureChangedListener> getListenerListIterator() {
		return graphStructureChangedListeners != null ? graphStructureChangedListeners
				.iterator() : null;
	}

	@Override
	public void notifyVertexDeleted(Vertex v) {
		assert (v != null) && v.isValid() && vSeqContainsVertex(v);
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

	@Override
	public void notifyVertexAdded(Vertex v) {
		assert (v != null) && v.isValid() && vSeqContainsVertex(v);
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

	@Override
	public void notifyEdgeDeleted(Edge e) {
		assert (e != null) && e.isValid() && e.isNormal()
				&& eSeqContainsEdge(e);
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

	@Override
	public void notifyEdgeAdded(Edge e) {
		assert (e != null) && e.isValid() && e.isNormal()
				&& eSeqContainsEdge(e);
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

	@Override
	public void notifyMaxVertexCountIncreased(int newValue) {
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

	@Override
	public void notifyMaxEdgeCountIncreased(int newValue) {
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

	@Override
	public void save(String filename) throws GraphIOException {
		save(filename, null);
	}

	@Override
	public void save(String filename, ProgressFunction pf)
			throws GraphIOException {
		GraphIO.saveGraphToFile(this, filename, pf);
	}

	@Override
	public void save(DataOutputStream out) throws GraphIOException {
		save(out, null);
	}

	@Override
	public void save(DataOutputStream out, ProgressFunction pf)
			throws GraphIOException {
		GraphIO.saveGraphToStream(this, out, pf);
	}

	@Override
	public GraphFactory getGraphFactory() {
		return graphFactory;
	}

	@Override
	public void setGraphFactory(GraphFactory graphFactory) {
		this.graphFactory = graphFactory;
	}

	@Override
	public boolean isInstanceOf(AttributedElementClass cls) {
		// This is specific to all impl variants with code generation. Generic
		// needs to implement this with a schema lookup.
		return cls.getSchemaClass().isInstance(this);
	}
}
