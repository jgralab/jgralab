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

import de.uni_koblenz.jgralab.Aggregation;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Composition;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
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
 * uses an incidence array to save the whole graph structure
 * 
 * @author Steffen Kahle et. al.
 */
@SuppressWarnings("unchecked")
public abstract class GraphImpl extends AttributedElementImpl implements
Graph {

	/**
	 * indexed with incidence-id, holds the vertex-id the edge is pointing to
	 */
	private int targetVertex[];

	/**
	 * indexed with incidence-id, holds the next incidence of the current vertex
	 * to represent iSeq
	 */
	private int nextEdgeAtVertex[];

	/**
	 * indexed with vertex-id, points to the next used vertex number to
	 * represent vSeq
	 */
	private int nextVertex[];

	/**
	 * indexed with edge-id, points to the next used edge number to represent
	 * eSeq
	 */
	private int nextEdgeInGraph[];

	/**
	 * indexed with vertex-id, holds the first incidence-id of the vertex
	 */
	private int firstEdgeAtVertex[];

	/**
	 * indexed with vertex-id, holds the last incidence-id of the vertex
	 */
	private int lastEdgeAtVertex[];

	/**
	 * number of vertices in the graph
	 */
	private int vCount = 0;

	/**
	 * number of edges in the graph
	 */
	private int eCount = 0;

	/**
	 * holds the id of the first edge in Eseq
	 */
	protected int firstEdge;

	/**
	 * holds the id of the first vertex in Vseq
	 */
	protected int firstVertex;

	/**
	 * holds the id of the last edge in Eseq
	 */
	protected int lastEdge;

	/**
	 * holds the id of the last vertex in Vseq
	 */
	protected int lastVertex;

	private List<Integer> deleteVertexList;

	/**
	 * factor to expand the internal arrays if vertices or edges get too big
	 */
	protected final double EXPANSIONFACTOR = 2.0;

	
	/**
	 * The schema this graph belongs to
	 */
	private Schema schema;

	/**
	 * the unique id of the graph in the schema
	 */
	private String id;

	/**
	 * indexed with vertex-id, holds the actual vertex-object itself
	 */
	protected Vertex vertex[];

	/**
	 * indexed with edge-id, holds the actual edge-object itself
	 */
	protected Edge edge[];

	/**
	 * maximum number of edges + 1
	 */
	protected int eSize;

	/**
	 * maximum number of vertices + 1
	 */
	protected int vSize;

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
	 * Holds the version of the vertex sequence. For every modification (e.g.
	 * adding/deleting a vertex or changing the vertex sequence) this version
	 * number is increased by 1. It is set to 0 when the graph is loaded.
	 */
	protected long vertexListVersion;

	/**
	 * Holds the version of the edge sequence. For every modification (e.g.
	 * adding/deleting an edge or changing the edge sequence) this version
	 * number is increased by 1. It is set to 0 when the graph is loaded.
	 */
	protected long edgeListVersion;

	private boolean loading;

	/**
	 * @param id
	 *            the name of the graph's id
	 * @param vMax
	 *            the maximum number of vertices
	 * @param eMax
	 *            the maximum number of edges
	 */
	public GraphImpl(String id, GraphClass aGraphClass, int vMax, int eMax) {
		super(aGraphClass);
		if (vMax < 0)
			throw new GraphException("vMax must not be less than zero", null);
		if (eMax < 0)
			throw new GraphException("eMax must not be less than zero", null);

		this.schema = aGraphClass.getSchema();
		if (id == null) {
			this.id = RandomIdGenerator.generateId();
		} else {
			this.id = id;
		}

		expandVertexArray(vMax + 1);
		expandEdgeArray(eMax + 1);
		graphFactory = schema.getGraphFactory();
		
		firstVertex = 0;
		lastVertex = 0;
		firstEdge = 0;
		lastEdge = 0;
		vCount = 0;
		eCount = 0;
		graphVersion = 0;
		deleteVertexList = new LinkedList<Integer>();
	}

	@Override
	public void addEdge(Edge newEdge, Vertex alpha, Vertex omega) {
		assert (newEdge.isNormal());
		if (!alpha.isValidAlpha(newEdge))
			throw new GraphException("Edges of class "
					+ newEdge.getAttributedElementClass().getQualifiedName()
					+ " may not start at vertices of class "
					+ alpha.getAttributedElementClass().getQualifiedName());
		if (!omega.isValidOmega(newEdge))
			throw new GraphException("Edges of class "
					+ newEdge.getAttributedElementClass().getQualifiedName()
					+ " may not end at at vertices of class"
					+ omega.getAttributedElementClass().getQualifiedName());

		int eId = newEdge.getId();

		if (isLoading()) {
			if (eId != 0) {
				// the given vertex already has an id, try to use it
				if (containsEdgeId(eId))
					throw new GraphException("edge with id " + eId
							+ " already exists");

				if (eId >= eSize)
					throw new GraphException("edge id " + eId
							+ " is bigger than eSize");
			} else {
				throw new GraphException("a vertex that has not id may not be added while a graph is loading");
			}
		} else {
			if (eId != 0) {
				// the given vertex already has an id, try to use it
				if (containsEdgeId(eId))
					throw new GraphException("edge with id " + eId
							+ " already exists");
	
				if (eId >= eSize)
					throw new GraphException("edge id " + eId
							+ " is bigger than eSize");
	
				// remove edge from free edge list
				int i = 0;
				while (nextEdgeInGraph[i] != eId)
					++i;
				nextEdgeInGraph[i] = nextEdgeInGraph[eId];
			} else {
				if (nextEdgeInGraph[0] == 0) {
					expandEdges(EXPANSIONFACTOR);
				}
				eId = nextEdgeInGraph[0];
				nextEdgeInGraph[0] = nextEdgeInGraph[eId];
				newEdge.setId(eId);
			}
		}
		++eCount;

		if (firstEdge == 0) {
			firstEdge = eId;
		}
		if (lastEdge != 0) {
			nextEdgeInGraph[lastEdge] = eId;
		}
		lastEdge = eId;
		
		nextEdgeInGraph[eId] = 0;

		edge[edgeOffset(eId)] = newEdge;
		edge[edgeOffset(-eId)] = newEdge.getReversedEdge();

		int omegaId = omega.getId();
		int alphaId = alpha.getId();

		// put in alpha and omega
		targetVertex[edgeOffset(eId)] = omegaId;
		targetVertex[edgeOffset(-eId)] = alphaId;

		if (firstEdgeAtVertex[alphaId] == 0) {
			// alphaId has no incident edges yet
			firstEdgeAtVertex[alphaId] = eId;
			lastEdgeAtVertex[alphaId] = eId;
		} else {
			// incident edges in alphaId present
			// insert eNo in iSeq(alphaId)
			nextEdgeAtVertex[edgeOffset(lastEdgeAtVertex[alphaId])] = eId;
			lastEdgeAtVertex[alphaId] = eId;
		}
		if (firstEdgeAtVertex[omegaId] == 0) {
			// omegaId has no incident edges yet
			firstEdgeAtVertex[omegaId] = -eId;
			lastEdgeAtVertex[omegaId] = -eId;
		} else {
			// incident edges in omegaId present
			// insert -eNo in iSeq(omegaId)
			nextEdgeAtVertex[edgeOffset(lastEdgeAtVertex[omegaId])] = -eId;
			lastEdgeAtVertex[omegaId] = -eId;
		}

		if (!isLoading()) {
			alpha.incidenceListModified();
			omega.incidenceListModified();
			edgeListModified();
		}	
		edgeAdded(newEdge);
	}

	@Override
	public void addVertex(Vertex newVertex) {
		int vId = newVertex.getId();

		if (isLoading()) {
			if (vId != 0) {
				// the given vertex already has an id, try to use it
				if (containsVertexId(vId))
					throw new GraphException("vertex with id " + vId
							+ " already exists");

				if (vId >= vSize)
					throw new GraphException("vertex id " + vId
							+ " is bigger than vSize");
			} else {
				throw new GraphException("a vertex that has not id may not be added while a graph is loading");
			}
		} else {
			if (vId != 0) {
				// the given vertex already has an id, try to use it
				if (containsVertexId(vId))
					throw new GraphException("vertex with id " + vId
							+ " already exists");
	
				if (vId >= vSize)
					throw new GraphException("vertex id " + vId
							+ " is bigger than vSize");
	
				// remove vertex from free vertex list
				int i = 0;
				while (nextVertex[i] != vId)
					++i;
				nextVertex[i] = nextVertex[vId];
			} else {
				if (nextVertex[0] == 0) {
					expandVertices(EXPANSIONFACTOR);
				}
				vId = nextVertex[0];
				nextVertex[0] = nextVertex[vId];
				newVertex.setId(vId);
			}
		}

		++vCount;

		if (firstVertex == 0) {
			firstVertex = vId;
		}

		if (lastVertex != 0) {
			nextVertex[lastVertex] = vId;
		}

		lastVertex = vId;

		nextVertex[vId] = 0;

		vertex[vId] = newVertex;

		if (!isLoading())
			vertexListModified();
		vertexAdded(newVertex);
	}
	
	
	@Override
	public Iterable<Aggregation> aggregations() {
		return new EdgeIterable<Aggregation>(this, Aggregation.class);
	}

	private void appendEdgeAtVertex(int vertexId, int edgeId) {
		int lastEdgeId = lastEdgeAtVertex[vertexId];
		if (lastEdgeId == 0) {
			firstEdgeAtVertex[vertexId] = edgeId;
		} else {
			nextEdgeAtVertex[edgeOffset(lastEdgeAtVertex[vertexId])] = edgeId;
		}
		lastEdgeAtVertex[vertexId] = edgeId;
		nextEdgeAtVertex[edgeOffset(edgeId)] = 0;
		vertex[vertexId].incidenceListModified();
	}

	@Override
	public int compareTo(AttributedElement a) {
		if (a instanceof Graph) {
			Graph g = (Graph) a;
			return this.hashCode() - g.hashCode();
		}
		return -1;
	}

	@Override
	public Iterable<Composition> compositions() {
		return new EdgeIterable<Composition>(this, Composition.class);
	}

	@Override
	public boolean containsEdge(Edge e) {
		return e != null && e.getGraph() == this && containsEdgeId(e.getId()) && edge[edgeOffset(e.getId())] == e;
	}

	private boolean containsEdgeId(int eId) {
		if (eId < 0)
			eId = -eId;
		return eId > 0 && eId < eSize && edge[edgeOffset(eId)] != null;
	}

	@Override
	public boolean containsVertex(Vertex v) {
		return v != null && v.getGraph() == this && containsVertexId(v.getId()) && vertex[v.getId()] == v;
	}

	private boolean containsVertexId(int vId) {
		return vId > 0 && vId < vSize && vertex[vId] != null;
	}

	/**
	 * Creates an edge of the given class and adds this edge to the graph.
	 * <code>cls</code> has to be the "Impl" class.
	 */
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

	@Override
	public void deleteEdge(Edge e) {
		deleteEdge(e.getId());
	}

	private void deleteEdge(int eId) {
		if (!(containsEdgeId(eId) && firstEdge > 0 && eCount > 0)) {
			throw new GraphException("Edge " + eId + " doesn't exist");
		}

		internalDeleteEdge(eId);

		if (!deleteVertexList.isEmpty()) {
			internalDeleteVertex();
		}

		edgeListModified();
	}

	void deleteEdgeTo(int vId, int iNo) {
		int prevId = 0;
		if (iNo == firstEdgeAtVertex[vId]) {
			firstEdgeAtVertex[vId] = nextEdgeAtVertex[edgeOffset(iNo)];
		} else {
			prevId = firstEdgeAtVertex[vId];
			while (nextEdgeAtVertex[edgeOffset(prevId)] != iNo) {
				prevId = nextEdgeAtVertex[edgeOffset(prevId)];
			}
		}
		if (iNo == lastEdgeAtVertex[vId]) {
			lastEdgeAtVertex[vId] = prevId;
		}
		if (prevId != 0) {
			nextEdgeAtVertex[edgeOffset(prevId)] = nextEdgeAtVertex[edgeOffset(iNo)];
		}
		nextEdgeAtVertex[edgeOffset(iNo)] = 0;
		if (vertex[vId] != null)
			vertex[vId].incidenceListModified();
		edgeListModified();
	}

	private void deleteVertex(int vId) {
		if (!(containsVertexId(vId) && firstVertex > 0 && vCount > 0)) {
			throw new GraphException("Vertex " + vId + " doesn't exist");
		}
		deleteVertexList.add(vId);
		internalDeleteVertex();
		// vertexStructure of neighbours is changed in internalDeleteEdge,
		// called by internalDeleteVertex
		vertexListModified();
	}

	@Override
	public void deleteVertex(Vertex v) {
		deleteVertex(v.getId());
	}

	@Override
	public void edgeDeleted(Edge e) {
	}

	@Override
	public void edgeAdded(Edge e) {
	}

	@Override
	public final void edgeListModified() {
		edgeListVersion++;
		graphVersion++;
	}

	/**
	 * @param no
	 *            edge number (positive or negative)
	 * @return positive id to be used as index in incidence array
	 */
	protected final int edgeOffset(int no) {
		return no + eSize;
	}

	@Override
	public Iterable<Edge> edges() {
		return new EdgeIterable<Edge>(this);
	}

	@Override
	public Iterable<Edge> edges(Class<? extends Edge> eclass) {
		return new EdgeIterable<Edge>(this, eclass);
	}


	@Override
	public Iterable<Edge> edges(EdgeClass eclass) {
		return new EdgeIterable<Edge>(this, eclass);
	}

	private void expandEdgeArray(int newSize) {
		int oldSize = eSize;
		if (newSize <= eSize) {
			throw new GraphException("newSize be > eSize: eSize=" + eSize
					+ ", new size=" + newSize);
		}

		Edge[] expandedArray = new Edge[newSize * 2];
		if (edge != null) {
			System.arraycopy(edge, 0, expandedArray, newSize - eSize,
					edge.length);
		}
		edge = expandedArray;
		eSize = newSize;

		nextEdgeInGraph = expandIntArray(nextEdgeInGraph, newSize, 0);
		targetVertex = expandIntArray(targetVertex, newSize * 2, newSize
				- oldSize);
		nextEdgeAtVertex = expandIntArray(nextEdgeAtVertex, newSize * 2,
				newSize - oldSize);

		// initialize free edge list
		for (int i = oldSize; i < eSize - 1; ++i) {
			nextEdgeInGraph[i] = i + 1;
		}
		nextEdgeInGraph[eSize - 1] = 0;

		// append new free vertex list to end of old free vertex list
		int i = 0;
		while (nextEdgeInGraph[i] != 0)
			++i;
		nextEdgeInGraph[i] = oldSize;
	}

	/**
	 * expand the edge array with the factor factor
	 * 
	 * @param factor @
	 */
	private void expandEdges(double factor) {
		int newSize = (int) (eSize * factor);
		expandEdgeArray(newSize);
	}

	/**
	 * Expands an int array.
	 * 
	 * @param oldArray
	 *            the array which shall be expanded
	 * @param newSize
	 *            the the new array size
	 * @param destStart
	 *            Indicates the new array's starting index where to insert
	 *            array's elements
	 * @return the expanded array
	 */
	private int[] expandIntArray(int[] oldArray, int newSize, int destStart) {
		int[] expandedArray = new int[newSize];
		if (oldArray != null) {
			System.arraycopy(oldArray, 0, expandedArray, destStart,
					oldArray.length);
		}
		return expandedArray;
	}


	private void expandVertexArray(int newSize) {
		int oldSize = vSize;
		if (newSize <= vSize) {
			throw new GraphException("newSize be > vSize: vSize=" + vSize
					+ ", new size=" + newSize);
		}
		Vertex[] expandedArray = new Vertex[newSize];
		if (vertex != null) {
			System.arraycopy(vertex, 0, expandedArray, 0, vertex.length);
		}
		vertex = expandedArray;
		vSize = newSize;

		nextVertex = expandIntArray(nextVertex, newSize, 0);
		firstEdgeAtVertex = expandIntArray(firstEdgeAtVertex, newSize, 0);
		lastEdgeAtVertex = expandIntArray(lastEdgeAtVertex, newSize, 0);

		// initialize free vertex list
		for (int i = oldSize; i < vSize - 1; ++i) {
			nextVertex[i] = i + 1;
		}
		nextVertex[vSize - 1] = 0;

		// append new free vertex list to end of old free vertex list
		int i = 0;
		while (nextVertex[i] != 0)
			++i;
		nextVertex[i] = oldSize;
	}

	/**
	 * expand vertex arrays with the factor factor
	 * 
	 * @param factor @
	 */
	private void expandVertices(double factor) {
		int newSize = (int) (vSize * factor);
		expandVertexArray(newSize);
	}

	@Override
	public Vertex getAlpha(Edge e) {
		int eId = e.getId();
		assert (containsEdgeId(eId));
		return vertex[targetVertex[edgeOffset(eId > 0 ? -eId : eId)]];
	}

	@Override
	public int getDegree(Vertex v) {
		return getDegree(v, EdgeDirection.INOUT);
	}

	@Override
	public int getDegree(Vertex v, EdgeDirection orientation) {
		int vId = v.getId();
		assert (containsVertexId(vId));
		int count = 0;
		int nextI = firstEdgeAtVertex[vId];
		if (orientation == EdgeDirection.IN) {
			while (nextI != 0) {
				if (nextI < 0) {
					count++;
				}
				nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			}
		} else if (orientation == EdgeDirection.OUT) {
			while (nextI != 0) {
				if (nextI > 0) {
					count++;
				}
				nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			}
		} else {
			while (nextI != 0) {
				count++;
				nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			}
		}
		return count;
	}

	@Override
	public int getECount() {
		return eCount;
	}

	@Override
	public Edge getEdge(int eId) {
		assert (containsEdgeId(eId));
		return edge[edgeOffset(eId < 0 ? -eId : eId)];
	}

	@Override
	public long getEdgeListVersion() {
		return edgeListVersion;
	}

	@Override
	public Edge getFirstEdge(Vertex v) {
		return getFirstEdge(v, EdgeDirection.INOUT);
	}

	@Override
	public Edge getFirstEdge(Vertex v, EdgeDirection orientation) {
		int vId = v.getId();
		assert (containsVertexId(vId));
		if (orientation == EdgeDirection.INOUT) {
			return edge[edgeOffset(firstEdgeAtVertex[vId])];
		} else if (orientation == EdgeDirection.IN) {
			int eId = firstEdgeAtVertex[vId];
			while (eId != 0 && eId > 0) { 
				eId = nextEdgeAtVertex[edgeOffset(eId)];
			}
			return edge[edgeOffset(eId)];
		} else {
			int eId = firstEdgeAtVertex[vId];
			while (eId != 0 && eId < 0) {
				eId = nextEdgeAtVertex[edgeOffset(eId)];
			}
			return edge[edgeOffset(eId)];
		}
	}

	@Override
	public Edge getFirstEdgeInGraph() {
		return edge[edgeOffset(firstEdge)];
	}

	@Override
	public Edge getFirstEdgeOfClass(Vertex v, Class<? extends Edge> ec) {
		return getFirstEdgeOfClass(v, ec, EdgeDirection.INOUT, false);
	}

	@Override
	public Edge getFirstEdgeOfClass(Vertex v, Class<? extends Edge> ec,
			boolean noSubclasses) {
		return getFirstEdgeOfClass(v, ec, EdgeDirection.INOUT, noSubclasses);
	}

	@Override
	public Edge getFirstEdgeOfClass(Vertex v,
			Class<? extends Edge> anEdgeClass, EdgeDirection orientation,
			boolean explicitType) {
		Edge currentEdge = getFirstEdge(v, orientation);
		while (currentEdge != null) {
			if (explicitType) {
				if (anEdgeClass == currentEdge.getM1Class())
					return currentEdge;
			} else {
				if (anEdgeClass.isInstance(currentEdge.getNormalEdge()))
					return currentEdge;
			}
			currentEdge = currentEdge.getNextEdge(orientation);
		}
		return null;
	}

	@Override
	public Edge getFirstEdgeOfClass(Vertex v, EdgeClass ec) {
		return getFirstEdgeOfClass(v, (Class<? extends Edge>) ec.getM1Class(),
				EdgeDirection.INOUT, false);
	}

	@Override
	public Edge getFirstEdgeOfClass(Vertex v, EdgeClass ec, boolean noSubclasses) {
		return getFirstEdgeOfClass(v, (Class<? extends Edge>) ec.getM1Class(),
				EdgeDirection.INOUT, noSubclasses);
	}

	@Override
	public Edge getFirstEdgeOfClass(Vertex v, EdgeClass ec,
			EdgeDirection orientation, boolean noSubclasses) {
		return getFirstEdgeOfClass(v, (Class<? extends Edge>) ec.getM1Class(),
				orientation, noSubclasses);
	}

	@Override
	public Edge getFirstEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass) {
		return getFirstEdgeOfClassInGraph(anEdgeClass, false);
	}

	@Override
	public Edge getFirstEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass,
			boolean explicitType) {
		Edge currentEdge = getFirstEdgeInGraph();
		while (currentEdge != null) {
			if (explicitType) {
				if (anEdgeClass == currentEdge.getM1Class())
					return currentEdge;
			} else {
				if (anEdgeClass.isInstance(currentEdge))
					return currentEdge;
			}
			currentEdge = currentEdge.getNextEdgeInGraph();
		}
		return null;
	}

	@Override
	public Edge getFirstEdgeOfClassInGraph(EdgeClass anEdgeClass) {
		return getFirstEdgeOfClassInGraph((Class<? extends Edge>) anEdgeClass
				.getM1Class(), false);
	}

	@Override
	public Edge getFirstEdgeOfClassInGraph(EdgeClass anEdgeClass,
			boolean explicitType) {
		return getFirstEdgeOfClassInGraph((Class<? extends Edge>) anEdgeClass
				.getM1Class(), explicitType);
	}

	@Override
	public Vertex getFirstVertex() {
		return vertex[firstVertex];
	}

	@Override
	public Vertex getFirstVertexOfClass(Class<? extends Vertex> aVertexClass) {
		return getFirstVertexOfClass(aVertexClass, false);
	}

	@Override
	public Vertex getFirstVertexOfClass(Class<? extends Vertex> aVertexClass,
			boolean explicitType) {
		Vertex firstVertex = getFirstVertex();
		if (firstVertex == null)
			return null;
		if (explicitType) {
			if (aVertexClass == firstVertex.getM1Class())
				return firstVertex;
		} else {
			if (aVertexClass.isInstance(firstVertex))
				return firstVertex;
		}
		return getNextVertexOfClass(firstVertex, aVertexClass, explicitType);
	}

	@Override
	public Vertex getFirstVertexOfClass(VertexClass aVertexClass) {
		return getFirstVertexOfClass(aVertexClass, false);
	}

	@Override
	public Vertex getFirstVertexOfClass(VertexClass aVertexClass,
			boolean explicitType) {
		return getFirstVertexOfClass((Class<? extends Vertex>) (aVertexClass
				.getM1Class()), explicitType);
	}

	@Override
	public GraphClass getGraphClass() {
		return (GraphClass) theClass;
	}

	@Override
	public long getGraphVersion() {
		return graphVersion;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getMaxECount() {
		return eSize - 1;
	}

	@Override
	public int getMaxVCount() {
		return vSize - 1;
	}

	@Override
	public Edge getNextEdge(Edge e) {
		return getNextEdge(e, EdgeDirection.INOUT);
	}

	@Override
	public Edge getNextEdge(Edge e, EdgeDirection orientation) {
		int eId = e.getId();
		assert (containsEdgeId(eId));
		int nextI = nextEdgeAtVertex[edgeOffset(eId)];
		if (orientation == EdgeDirection.IN) {
			while (nextI != 0) {
				if (nextI < 0)
					return edge[edgeOffset(nextI)];
				nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			}
		} else if (orientation == EdgeDirection.OUT) {
			while (nextI != 0) {
				if (nextI > 0)
					return edge[edgeOffset(nextI)];
				nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			}
		} else {
			if (nextI != 0) {
				return edge[edgeOffset(nextI)];
			}
		}
		return null;
	}

	@Override
	public Edge getNextEdgeInGraph(Edge anEdge) {
		assert (containsEdgeId(anEdge.getId()));
		return edge[edgeOffset(nextEdgeInGraph[anEdge.getId()])];
	}

	@Override
	public Edge getNextEdgeOfClass(Edge e, Class<? extends Edge> ec) {
		return getNextEdgeOfClass(e, ec, EdgeDirection.INOUT, false);
	}

	@Override
	public Edge getNextEdgeOfClass(Edge e, Class<? extends Edge> ec,
			boolean noSubclasses) {
		return getNextEdgeOfClass(e, ec, EdgeDirection.INOUT, noSubclasses);
	}



	@Override
	public Edge getNextEdgeOfClass(Edge e, Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		Edge currentEdge = getNextEdge(e, orientation);
		while (currentEdge != null) {
			if (explicitType) {
				if (anEdgeClass == currentEdge.getM1Class())
					return currentEdge;
			} else {
				if (anEdgeClass.isInstance(currentEdge))
					return currentEdge;
			}
			currentEdge = currentEdge.getNextEdge(orientation);
		}
		return null;
	}

	@Override
	public Edge getNextEdgeOfClass(Edge e, EdgeClass ec) {
		return getNextEdgeOfClass(e, (Class<? extends Edge>) ec.getM1Class(),
				EdgeDirection.INOUT, false);
	}

	@Override
	public Edge getNextEdgeOfClass(Edge e, EdgeClass ec, boolean noSubclasses) {
		return getNextEdgeOfClass(e, (Class<? extends Edge>) ec.getM1Class(),
				EdgeDirection.INOUT, noSubclasses);
	}

	@Override
	public Edge getNextEdgeOfClass(Edge e, EdgeClass ec,
			EdgeDirection orientation, boolean noSubclasses) {
		return getNextEdgeOfClass(e, (Class<? extends Edge>) ec.getM1Class(),
				orientation, noSubclasses);
	}

	@Override
	public Edge getNextEdgeOfClassInGraph(Edge anEdge,
			Class<? extends Edge> anEdgeClass) {
		return getNextEdgeOfClassInGraph(anEdge, anEdgeClass, false);
	}

	@Override
	public Edge getNextEdgeOfClassInGraph(Edge e,
			Class<? extends Edge> anEdgeClass, boolean explicitType) {
		Edge currentEdge = getNextEdgeInGraph(e);
		while (currentEdge != null) {
			if (explicitType) {
				if (anEdgeClass == currentEdge.getM1Class())
					return currentEdge;
			} else {
				if (anEdgeClass.isInstance(currentEdge))
					return currentEdge;
			}
			currentEdge = currentEdge.getNextEdgeInGraph();
		}
		return null;
	}

	@Override
	public Edge getNextEdgeOfClassInGraph(Edge anEdge, EdgeClass anEdgeClass) {
		return getNextEdgeOfClassInGraph(anEdge,
				(Class<? extends Edge>) anEdgeClass.getM1Class(), false);
	}

	@Override
	public Edge getNextEdgeOfClassInGraph(Edge anEdge, EdgeClass anEdgeClass,
			boolean explicitType) {
		return getNextEdgeOfClassInGraph(anEdge,
				(Class<? extends Edge>) anEdgeClass.getM1Class(), explicitType);
	}

	public Vertex getNextVertex(int vId) {
		assert (containsVertexId(vId));
		return vertex[nextVertex[vId]];
	}

	@Override
	public Vertex getNextVertex(Vertex aVertex) {
		return getNextVertex(aVertex.getId());
	}

	public Vertex getNextVertexOfClass(int vId,
			Class<? extends Vertex> aM1VertexClass) {
		return getNextVertexOfClass(vId, aM1VertexClass, false);
	}

	public Vertex getNextVertexOfClass(int vId,
			Class<? extends Vertex> aM1VertexClass, boolean explicitType) {
		return getNextVertexOfClass(getVertex(vId), aM1VertexClass,
				explicitType);
	}

	public Vertex getNextVertexOfClass(int vId, VertexClass aVertexClass) {
		return getNextVertexOfClass(vId, aVertexClass, false);
	}

	public Vertex getNextVertexOfClass(int vId, VertexClass aVertexClass,
			boolean explicitType) {
		return getNextVertexOfClass(getVertex(vId), aVertexClass, explicitType);
	}

	@Override
	public Vertex getNextVertexOfClass(Vertex vertex,
			Class<? extends Vertex> vertexClass) {
		return getNextVertexOfClass(vertex, vertexClass, false);
	}

	@Override
	public Vertex getNextVertexOfClass(Vertex aVertex,
			Class<? extends Vertex> aM1VertexClass, boolean explicitType) {
		Vertex nextVertex = getNextVertex(aVertex);
		while (nextVertex != null) {
			if (explicitType) {
				if (aM1VertexClass == aVertex.getM1Class())
					return nextVertex;
			} else {
				if (aM1VertexClass.isInstance(nextVertex))
					return nextVertex;
			}
			nextVertex = getNextVertex(nextVertex);
		}
		return null;
	}

	@Override
	public Vertex getNextVertexOfClass(Vertex vertex, VertexClass vertexClass) {
		return getNextVertexOfClass(vertex,
				(Class<? extends Vertex>) vertexClass.getM1Class(), false);
	}


	@Override
	public Vertex getNextVertexOfClass(Vertex vertex, VertexClass vertexClass,
			boolean explicitType) {
		return getNextVertexOfClass(vertex,
				(Class<? extends Vertex>) vertexClass.getM1Class(),
				explicitType);
	}


	@Override
	public Vertex getOmega(Edge e) {
		int eId = e.getId();
		assert (containsEdgeId(eId));
		return vertex[targetVertex[edgeOffset(eId > 0 ? eId : -eId)]];
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public int getVCount() {
		return vCount;
	}

	@Override
	public Vertex getVertex(int vId) {
		assert (vId >= 0 && vId < vSize);
		return vertex[vId];
	}

	@Override
	public long getVertexListVersion() {
		return vertexListVersion;
	}


	@Override
	public final void graphModified() {
		graphVersion++;
	}

	@Override
	public void insertEdgeAt(Vertex v, Edge e, int pos) {
		int vertexId = v.getId();
		int edgeId = e.getId();
		if (targetVertex[edgeOffset(-edgeId)] != vertexId)
			throw new GraphException("Cannot put edge " + edgeId
					+ " at position " + pos + " at vertex " + vertexId
					+ ", this-vertex of edge " + edgeId + " is not " + vertexId);
		int oldPreviousEdgeId = 0;
		int currentEdgeId = firstEdgeAtVertex[targetVertex[edgeOffset(-edgeId)]];
		while (currentEdgeId != edgeId) {
			oldPreviousEdgeId = currentEdgeId;
			currentEdgeId = nextEdgeAtVertex[edgeOffset(currentEdgeId)];
		}
		if (oldPreviousEdgeId != 0)
			nextEdgeAtVertex[edgeOffset(oldPreviousEdgeId)] = nextEdgeAtVertex[edgeOffset(edgeId)];

		int nextI = firstEdgeAtVertex[vertexId];
		int currentEdge = nextI;
		int count = 0;
		while ((count < pos) && (nextI != 0)) {
			currentEdge = nextI;
			count++;
			nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			if (nextI == 0) {
				lastEdgeAtVertex[vertexId] = edgeId;
			}
		}
		nextEdgeAtVertex[edgeOffset(edgeId)] = nextEdgeAtVertex[edgeOffset(currentEdge)];
		nextEdgeAtVertex[edgeOffset(currentEdge)] = edgeId;
		vertex[vertexId].incidenceListModified();
	}

	private void internalDeleteEdge(int eId) {
		if (eId < 0)
			eId = -eId;

		Vertex v = vertex[targetVertex[edgeOffset(eId)]];
		if (v != null) {
			v.incidenceListModified();
		}
		v = vertex[targetVertex[edgeOffset(-eId)]];
		if (v != null) {
			v.incidenceListModified();
		}

		Edge deletedEdge = edge[edgeOffset(eId)];
		edgeDeleted(deletedEdge);

		// remove edge from eSeq
		int prevId = 0;
		if (eId == firstEdge) {
			firstEdge = nextEdgeInGraph[eId];
		} else {
			prevId = firstEdge;
			while (nextEdgeInGraph[prevId] != eId) {
				prevId = nextEdgeInGraph[prevId];
			}
		}
		if (eId == lastEdge) {
			lastEdge = prevId;
		}
		if (prevId != 0) {
			nextEdgeInGraph[prevId] = nextEdgeInGraph[eId];
		}

		// add edge to free edge list
		nextEdgeInGraph[eId] = nextEdgeInGraph[0];
		nextEdgeInGraph[0] = eId;

		// delete edge references
		edge[edgeOffset(eId)] = null;
		edge[edgeOffset(-eId)] = null;

		int omegaId = targetVertex[edgeOffset(eId)];
		targetVertex[edgeOffset(eId)] = 0;

		int alphaId = targetVertex[edgeOffset(-eId)];
		targetVertex[edgeOffset(-eId)] = 0;

		deleteEdgeTo(alphaId, eId);
		deleteEdgeTo(omegaId, -eId);

		--eCount;
	}

	private void internalDeleteVertex() {
		while (!deleteVertexList.isEmpty()) {
			int vId = deleteVertexList.remove(0);
			vertex[vId].incidenceListModified();
			vertexDeleted(vertex[vId]);
			// remove vertex from vSeq
			int prevId = 0;
			if (vId == firstVertex) {
				firstVertex = nextVertex[vId];
			} else {
				prevId = firstVertex;
				while (nextVertex[prevId] != vId) {
					prevId = nextVertex[prevId];
				}
			}
			if (vId == lastVertex) {
				lastVertex = prevId;
			}
			if (prevId != 0) {
				nextVertex[prevId] = nextVertex[vId];
			}

			// add vertex to free vertex list
			nextVertex[vId] = nextVertex[0];
			nextVertex[0] = vId;

			// delete vertex
			vertex[vId] = null;
			--vCount;

			// delete all incident edges including incidence objects

			int alphaId, omegaId;
			int eId = firstEdgeAtVertex[vId];
			while (eId != 0) {
				alphaId = targetVertex[edgeOffset(eId > 0 ? -eId : eId)];
				omegaId = targetVertex[edgeOffset(eId > 0 ? eId : -eId)];
				
				// check for cascading delete of vertices in incident composition edges
				AttributedElementClass aec = edge[edgeOffset(eId)].getAttributedElementClass();
				if (aec instanceof CompositionClass) {
					CompositionClass comp = (CompositionClass) aec;
					if (comp.isAggregateFrom()) {
						// omega vertex is to be deleted
						if (containsVertexId(omegaId) && !deleteVertexList.contains(omegaId)) {
							// System.err.println("Delete omega vertex v" + omegaId + "
							// of composition e" + eId);
							deleteVertexList.add(omegaId);
						}
					} else {
						if (containsVertexId(alphaId) && !deleteVertexList.contains(alphaId)) {
							// System.err.println("Delete alpha vertex v" + alphaId + "
							// of composition e" + eId);
							deleteVertexList.add(alphaId);
						}
					}
				}
				
				// delete edge
				internalDeleteEdge(eId);
				eId = firstEdgeAtVertex[vId];
			}

			firstEdgeAtVertex[vId] = 0;
			lastEdgeAtVertex[vId] = 0;

		}
	}

	@Override
	public boolean isAfterEdgeInGraph(Edge targetEdge, Edge sourceEdge) {
		int target = targetEdge.getId();
		int source = sourceEdge.getId();
		assert (containsEdgeId(source) && containsEdgeId(target));
		if (source == target)
			return false;
		int nextId = nextEdgeInGraph[source];
		while ((nextId != 0) && (nextId != target)) {
			nextId = nextEdgeInGraph[nextId];
		}
		return nextId != 0;
	}

	@Override
	public boolean isAfterVertex(Vertex targetVertex, Vertex sourceVertex) {
		int target = targetVertex.getId();
		int source = sourceVertex.getId();
		assert (containsVertexId(source) && containsVertexId(target));
		if (source == target)
			return false;
		int nextId = nextVertex[source];
		while ((nextId != 0) && (nextId != target)) {
			nextId = nextVertex[nextId];
		}
		return nextId == 0;
	}

	@Override
	public boolean isBeforeEdgeInGraph(Edge targetEdge, Edge sourceEdge) {
		int target = targetEdge.getId();
		int source = sourceEdge.getId();
		assert (containsEdgeId(source) && containsEdgeId(target));
		if (source == target)
			return false;
		int nextId = nextEdgeInGraph[source];
		while ((nextId != 0) && (nextId != target)) {
			nextId = nextEdgeInGraph[nextId];
		}
		return nextId == 0;
	}

	@Override
	public boolean isBeforeVertex(Vertex targetVertex, Vertex sourceVertex) {
		int target = targetVertex.getId();
		int source = sourceVertex.getId();
		assert (containsVertexId(source) && containsVertexId(target));
		if (source == target)
			return false;
		int nextId = nextVertex[source];
		while ((nextId != 0) && (nextId != target)) {
			nextId = nextVertex[nextId];
		}
		return nextId != 0;
	}

	@Override
	public final boolean isEdgeListModified(long edgeListVersion) {
		return (this.edgeListVersion != edgeListVersion);
	}

	@Override
	public final boolean isGraphModified(long aGraphVersion) {
		return (graphVersion != aGraphVersion);
	}

	@Override
	public boolean isLoading() {
		return loading;
	}

	@Override
	public final boolean isVertexListModified(long vertexListVersion) {
		return (this.vertexListVersion != vertexListVersion);
	}

	@Override
	public void loadingCompleted() {
	}
	

	@Override
	public final void internalLoadingCompleted() {
		//initialize list of free vertex ids
		int lastFreeVertex = 0;
		for (int i = 1; i < vSize; i++) {
			if (vertex[i] == null) {
				nextVertex[lastFreeVertex] = i;
				lastFreeVertex = i;
			}
		}
		nextVertex[lastFreeVertex] = 0;
		int lastFreeEdge = 0;
		for (int i = 1; i < eSize; i++) {
			if (edge[edgeOffset(i)] == null) {
				nextEdgeInGraph[lastFreeEdge] = i;
				lastFreeEdge = i;
			}
		}
		nextEdgeInGraph[lastFreeEdge] = 0;
	}


	/**
	 * This method overwrites the internal arrays that store the first and last
	 * edges at a vertex as well as the array that contains the next edge at a
	 * vertex. You should not use this method outside of GraphIO and it will
	 * check if it is called from outside.
	 */
	public void overwriteEdgeAtVertexArrays(int[] firstAtVertex,
			int[] nextAtVertex, int[] lastAtVertex) {
		String callingClassName = Thread.currentThread().getStackTrace()[2]
				.getClassName();
		if (!callingClassName.equals("de.uni_koblenz.jgralab.GraphIO"))
			throw new RuntimeException(
					"overwriteEdgeAtVertexArrays(...) must not be called from outside of GraphIO");

		if ((firstAtVertex.length != firstEdgeAtVertex.length)
				|| (nextAtVertex.length != nextEdgeAtVertex.length)
				|| (lastAtVertex.length != lastEdgeAtVertex.length))
			throw new RuntimeException(
					"overwriteEdgeAtVertexArrays(...) must not be called with arrays that have"
							+ "not the same length as the arrays used internally");
		firstEdgeAtVertex = firstAtVertex;
		nextEdgeAtVertex = nextAtVertex;
		lastEdgeAtVertex = lastAtVertex;
	}

	public void printArray() {
		System.out.println("vCount= " + vCount + ", firstVertex=" + firstVertex
				+ ", lastVertex=" + lastVertex);
		System.out.println("eCount=" + eCount + ", firstEdge=" + firstEdge
				+ ", lastEdge=" + lastEdge);

		System.out.print("                    ");
		for (int i = -eSize + 1; i < eSize; i++) {
			System.out.printf("%5d", i);
		}
		System.out.println();

		System.out.print("targetVertex:       ");
		for (int i = 1; i < eSize * 2; ++i) {
			System.out.printf("%5d", targetVertex[i]);
		}
		System.out.println();

		System.out.print("nextEdgeAtVertex:   ");
		for (int i = 1; i < eSize * 2; ++i) {
			System.out.printf("%5d", nextEdgeAtVertex[i]);
		}
		System.out.println();

		System.out.print("firstIncidence:     ");
		for (int i = 0; i < eSize; i++)
			System.out.print("     ");
		for (int i = 1; i < vSize; i++) {
			System.out.printf("%5d", firstEdgeAtVertex[i]);
		}
		System.out.println();

		System.out.print("lastIncidence:      ");
		for (int i = 0; i < eSize; i++)
			System.out.print("     ");
		for (int i = 1; i < vSize; i++) {
			System.out.printf("%5d", lastEdgeAtVertex[i]);
		}
		System.out.println();

		System.out.print("nextVertex:         ");
		for (int i = 1; i < eSize; i++)
			System.out.print("     ");
		for (int i = 0; i < vSize; i++) {
			System.out.printf("%5d", nextVertex[i]);
		}
		System.out.println();

		System.out.print("nextEdge:           ");
		for (int i = 1; i < eSize; i++)
			System.out.print("     ");
		for (int i = 0; i < eSize; i++) {
			System.out.printf("%5d", nextEdgeInGraph[i]);
		}
		System.out.println();
	}

	@Override
	public void putAfterEdgeInGraph(Edge targetEdge, Edge sourceEdge) {
		int source = Math.abs(sourceEdge.getId());
		int target = Math.abs(targetEdge.getId());
		if (target != source) {
			// delete references to source
			if (source == firstEdge) {
				firstEdge = nextEdgeInGraph[source];
			} else {
				int prevId = -1;
				int currId = firstEdge;
				while (currId != 0 && currId != source) {
					prevId = currId;
					currId = nextEdgeInGraph[currId];
				}
				assert prevId != 0;
				nextEdgeInGraph[prevId] = nextEdgeInGraph[source];
			}

			// insert source in eSeq after target
			nextEdgeInGraph[source] = nextEdgeInGraph[target];
			nextEdgeInGraph[target] = source;
		}
		edgeListModified();
	}

	@Override
	public void putAfterVertex(Vertex targetVertex, Vertex sourceVertex) {
		int target = targetVertex.getId();
		int source = sourceVertex.getId();
		assert (containsVertexId(target) && containsVertexId(source));
		if (target != source && nextVertex[target] != source) {
			// remove source from vSeq
			if (source == firstVertex) {
				firstVertex = nextVertex[source];
			} else {
				int prevId = firstVertex;
				while (nextVertex[prevId] != source) {
					prevId = nextVertex[prevId];
				}
				nextVertex[prevId] = nextVertex[source];
				if (source == lastVertex) {
					lastVertex = prevId;
				}
			}

			// insert source in vSeq immediately after target
			nextVertex[source] = nextVertex[target];
			nextVertex[target] = source;
			if (target == lastVertex) {
				lastVertex = source;
			}
			vertexListModified();
		}
	}

	@Override
	public void putBeforeEdgeInGraph(Edge targetEdge, Edge sourceEdge) {
		int source = Math.abs(sourceEdge.getId());
		int target = Math.abs(targetEdge.getId());
		if (target != source) {
			// delete references to source
			if (source == firstEdge) {
				firstEdge = nextEdgeInGraph[source];
			} else {
				int prevId = -1;
				int currId = firstEdge;
				while (currId != 0 && currId != source) {
					prevId = currId;
					currId = nextEdgeInGraph[currId];
				}
				assert prevId != 0;
				nextEdgeInGraph[prevId] = nextEdgeInGraph[source];
			}

			// insert source immediately before target
			if (target == firstEdge) {
				firstEdge = source;
			} else {
				int prevId = -1;
				int currId = firstEdge;
				while (currId != 0 && currId != target) {
					prevId = currId;
					currId = nextEdgeInGraph[currId];
				}
				assert prevId != 0;
				nextEdgeInGraph[prevId] = source;
			}
			nextEdgeInGraph[source] = target;
		}
		edgeListModified();
	}

	@Override
	public void putBeforeVertex(Vertex targetVertex, Vertex sourceVertex) {
		int target = targetVertex.getId();
		int source = sourceVertex.getId();
		assert (containsVertexId(target) && containsVertexId(source));
		if (target != source && nextVertex[source] != target) {
			// remove source from vSeq
			if (source == firstVertex) {
				firstVertex = nextVertex[source];
			} else {
				int prevId = firstVertex;
				while (nextVertex[prevId] != source) {
					prevId = nextVertex[prevId];
				}
				nextVertex[prevId] = nextVertex[source];
				if (source == lastVertex) {
					lastVertex = prevId;
				}
			}

			// insert source in vSeq before target
			if (target == firstVertex) {
				nextVertex[source] = firstVertex;
				firstVertex = source;
			} else {
				int prevId = firstVertex;
				while (nextVertex[prevId] != target) {
					prevId = nextVertex[prevId];
				}
				nextVertex[source] = target;
				nextVertex[prevId] = source;
			}
			vertexListModified();
		}
	}

	public void putEdgeAfter(Edge edge, Edge previousEdge) {
		int edgeId = edge.getId();
		int previousEdgeId = previousEdge.getId();
		int thisVertexId = targetVertex[edgeOffset(-edgeId)];
		if (thisVertexId != targetVertex[edgeOffset(-previousEdgeId)])
			throw new GraphException("Cannot put edge " + edgeId
					+ " after edge " + previousEdgeId
					+ ", edges have different this-vertices");
		if (edgeId == previousEdgeId)
			return;
		if (edgeId == nextEdgeAtVertex[edgeOffset(previousEdgeId)])
			return;

		// if edgeId is the first edge, make the next one the first one
		if (edgeId == firstEdgeAtVertex[thisVertexId]) {
			firstEdgeAtVertex[thisVertexId] = nextEdgeAtVertex[edgeOffset(edgeId)];
		} else {
			// otherwise remove the edge from the old position
			int oldPreviousEdgeId = 0;
			int currentEdgeId = firstEdgeAtVertex[targetVertex[edgeOffset(-edgeId)]];
			while (currentEdgeId != edgeId) {
				oldPreviousEdgeId = currentEdgeId;
				currentEdgeId = nextEdgeAtVertex[edgeOffset(currentEdgeId)];
			}
			nextEdgeAtVertex[edgeOffset(oldPreviousEdgeId)] = nextEdgeAtVertex[edgeOffset(edgeId)];
			// if the edgeId was the last edge at the vertex, set the last edge
			// to the previous one
			if (lastEdgeAtVertex[thisVertexId] == edgeId)
				lastEdgeAtVertex[thisVertexId] = oldPreviousEdgeId;
		}
		// put edge after previous one
		nextEdgeAtVertex[edgeOffset(edgeId)] = nextEdgeAtVertex[edgeOffset(previousEdgeId)];
		nextEdgeAtVertex[edgeOffset(previousEdgeId)] = edgeId;
		// if the new previous edge was the last edge at the vertex, set the
		// last edge to edgeId
		if (lastEdgeAtVertex[thisVertexId] == previousEdgeId)
			lastEdgeAtVertex[thisVertexId] = edgeId;
		vertex[thisVertexId].incidenceListModified();
	}

	public void putEdgeBefore(Edge edge, Edge nextEdge) {
		int edgeId = edge.getId();
		int nextEdgeId = nextEdge.getId();
		if (targetVertex[edgeOffset(-edgeId)] != targetVertex[edgeOffset(-nextEdgeId)])
			throw new GraphException("Cannot put edge " + edgeId
					+ " before edge " + nextEdgeId
					+ ", edges have different this-vertices");
		if (edgeId == nextEdgeId)
			return;
		int previousEdgeId = 0;
		int oldPreviousEdgeId = 0;
		int currentEdgeId = firstEdgeAtVertex[targetVertex[edgeOffset(-edgeId)]];
		while (currentEdgeId != edgeId) {
			oldPreviousEdgeId = currentEdgeId;
			currentEdgeId = nextEdgeAtVertex[edgeOffset(currentEdgeId)];
		}
		currentEdgeId = firstEdgeAtVertex[targetVertex[edgeOffset(-edgeId)]];
		while (currentEdgeId != nextEdgeId) {
			previousEdgeId = currentEdgeId;
			currentEdgeId = nextEdgeAtVertex[edgeOffset(currentEdgeId)];
		}
		int firstEdge = firstEdgeAtVertex[targetVertex[edgeOffset(-edgeId)]];
		int oldNextEdge = nextEdgeAtVertex[edgeOffset(edgeId)];
		int newNextEdge = nextEdgeAtVertex[edgeOffset(previousEdgeId)];

		if (oldPreviousEdgeId != 0)
			nextEdgeAtVertex[edgeOffset(oldPreviousEdgeId)] = oldNextEdge;
		if (previousEdgeId == 0) {
			nextEdgeAtVertex[edgeOffset(edgeId)] = firstEdge;
			firstEdgeAtVertex[targetVertex[edgeOffset(-edgeId)]] = edgeId;
		} else {
			nextEdgeAtVertex[edgeOffset(edgeId)] = newNextEdge;
			nextEdgeAtVertex[edgeOffset(previousEdgeId)] = edgeId;
		}
		if (lastEdgeAtVertex[targetVertex[edgeOffset(-edgeId)]] == edgeId)
			lastEdgeAtVertex[targetVertex[edgeOffset(-edgeId)]] = oldPreviousEdgeId;
		vertex[targetVertex[edgeOffset(-edgeId)]].incidenceListModified();
	}

	@Override
	public void setAlpha(Edge e, Vertex alpha) {
		int edgeId = e.getId();
		int alphaId = alpha.getId();

		int posId = edgeId;
		int negId = -edgeId;
		if (posId < 0) {
			posId = -edgeId;
			negId = edgeId;
		}
		if (targetVertex[edgeOffset(negId)] == alphaId) {
			return;
		}
		deleteEdgeTo(targetVertex[edgeOffset(negId)], posId);
		appendEdgeAtVertex(alphaId, posId);
		targetVertex[edgeOffset(negId)] = alphaId;
	}

	@Override
	public void setGraphVersion(long graphVersion) {
		this.graphVersion = graphVersion;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setLoading(boolean isLoading) {
		loading = isLoading;
	}

	@Override
	public void setOmega(Edge e, Vertex omega) {
		int edgeId = e.getId();
		int omegaId = omega.getId();

		int posId = edgeId;
		int negId = -edgeId;
		if (posId < 0) {
			posId = -edgeId;
			negId = edgeId;
		}
		if (targetVertex[edgeOffset(posId)] == omegaId)
			return;
		deleteEdgeTo(targetVertex[edgeOffset(posId)], negId);
		appendEdgeAtVertex(omegaId, negId);
		targetVertex[edgeOffset(posId)] = omegaId;
	}

	@Override
	public void vertexDeleted(Vertex v) {
	}

	@Override
	public void vertexAdded(Vertex v) {
	}

	@Override
	public final void vertexListModified() {
		vertexListVersion++;
		graphVersion++;
	}

	@Override
	public Iterable<Vertex> vertices() {
		return new VertexIterable<Vertex>(this);
	}

	@Override
	public Iterable<Vertex> vertices(Class<? extends Vertex> vclass) {
		return new VertexIterable<Vertex>(this, vclass);
	}

	@Override
	public Iterable<Vertex> vertices(VertexClass eclass) {
		return new VertexIterable<Vertex>(this, (Class<? extends Vertex>) eclass.getM1Class());
	}

}
