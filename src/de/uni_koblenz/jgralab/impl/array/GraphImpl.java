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
 
package de.uni_koblenz.jgralab.impl.array;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.CompositionClass;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphClass;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Schema;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.VertexClass;
import de.uni_koblenz.jgralab.impl.GraphBaseImpl;

/**
 * uses an incidence array to save the whole graph structure
 * 
 * @author Steffen Kahle et. al.
 */
@SuppressWarnings("unchecked")
public abstract class GraphImpl extends GraphBaseImpl {

	/**
	 * true = verbose output false = no output
	 */
	private final boolean DEBUG = false;

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
	 * @param id
	 *            the name of the graph's id
	 * @param vMax
	 *            the maximum number of vertices
	 * @param eMax
	 *            the maximum number of edges
	 */
	public GraphImpl(String id, GraphClass aGraphClass,
			Schema schema, int vMax, int eMax) {
		super(id, aGraphClass, schema, vMax, eMax);

		firstVertex = 0;
		lastVertex = 0;
		firstEdge = 0;
		lastEdge = 0;
		vCount = 0;
		eCount = 0;
		graphVersion = 0;
		deleteVertexList = new LinkedList<Integer>();
	}

	/**
	 * This method overwrites the internal arrays that store the first and last edges at a 
	 * vertex as well as the array that contains the next edge at a vertex.
	 * You should not use this method outside of GraphIO and it will
	 * check if it is called from outside.
	 */
	public void overwriteEdgeAtVertexArrays(int[] firstAtVertex, int[] nextAtVertex, int[] lastAtVertex) {
		String callingClassName = Thread.currentThread().getStackTrace()[2].getClassName();
		if (!callingClassName.equals("de.uni_koblenz.jgralab.GraphIO"))
			throw new RuntimeException("overwriteEdgeAtVertexArrays(...) must not be called from outside of GraphIO");

		if (
			(firstAtVertex.length != firstEdgeAtVertex.length) ||
			(nextAtVertex.length != nextEdgeAtVertex.length) ||
			(lastAtVertex.length != lastEdgeAtVertex.length))
			throw new RuntimeException("overwriteEdgeAtVertexArrays(...) must not be called with arrays that have" +
					"not the same length as the arrays used internally");
		firstEdgeAtVertex = firstAtVertex;
		nextEdgeAtVertex = nextAtVertex;
		lastEdgeAtVertex = lastAtVertex;
	}
	
	
	
	protected void expandVertexArray(int newSize) {
		int oldSize = vSize;
		super.expandVertexArray(newSize);

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

	protected void expandEdgeArray(int newSize) {
		int oldSize = eSize;
		super.expandEdgeArray(newSize);

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

	public void addVertex(Vertex aVertex) {
		int vId = aVertex.getId();

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
				if (DEBUG) {
					System.out.println("VertexCount is: " + vCount);
					System.out.println("Vertex Size is: " + vSize);
					System.out.println("Expanding vertices");
				}
				expandVertices(EXPANSIONFACTOR);
			}
			vId = nextVertex[0];
			nextVertex[0] = nextVertex[vId];
			aVertex.setId(vId);
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

		vertex[vId] = aVertex;

		if (DEBUG) {
			System.out.println("Vertex " + vId + "added.");
			printArray();
		}
		modified();
	}

	public void addEdge(Edge newEdge, Vertex alpha, Vertex omega) {
		assert (newEdge.isNormal());
		if (!alpha.isValidAlpha(newEdge))
			throw new GraphException("Edges of class " + newEdge.getAttributedElementClass().getName() + " may not start at vertices of class " + alpha.getAttributedElementClass().getName());
		if (!omega.isValidOmega(newEdge))
			throw new GraphException("Edges of class " + newEdge.getAttributedElementClass().getName() + " may not end at at vertices of class" + omega.getAttributedElementClass().getName());

		
		int eId = newEdge.getId();

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
				if (DEBUG) {
					System.out.println("EdgeCount is: " + eCount);
					System.out.println("Edge Size is: " + eSize);
					System.out.println("Expanding edges");
				}
				expandEdges(EXPANSIONFACTOR);
			}
			eId = nextEdgeInGraph[0];
			nextEdgeInGraph[0] = nextEdgeInGraph[eId];
			newEdge.setId(eId);
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

		if (DEBUG) {
			System.out.println("Edge " + eId + " added.");
			printArray();
		}

		modified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#printArray()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstVertex()
	 */
	public Vertex getFirstVertex() {
		return vertex[firstVertex];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextVertex(jgralab.Vertex)
	 */
	public Vertex getNextVertex(Vertex aVertex) {
		return getNextVertex(aVertex.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextVertex(int)
	 */
	public Vertex getNextVertex(int vId) {
		assert (containsVertexId(vId));
		return vertex[nextVertex[vId]];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextVertexOfClass(jgralab.Vertex,
	 *      jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(Vertex aVertex, VertexClass aVertexClass) {
		return getNextVertexOfClass(aVertex.getId(), aVertexClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextVertexOfClass(jgralab.Vertex,
	 *      jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(Vertex aVertex, Class aM1VertexClass) {
		return getNextVertexOfClass(aVertex.getId(), aM1VertexClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextVertexOfClass(int, jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(int vId, VertexClass aVertexClass) {
		return getNextVertexOfClass(vId, aVertexClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextVertexOfClass(int, jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(int vId, Class aM1VertexClass) {
		return getNextVertexOfClass(vId, aM1VertexClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextVertexOfExplicitClass(jgralab.Vertex,
	 *      jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(Vertex aVertex,
			VertexClass aVertexClass, boolean explicitType) {
		Vertex nextVertex = getNextVertex(aVertex);
		while (nextVertex != null) {
			if (aVertexClass.equals(nextVertex.getAttributedElementClass()))
				return nextVertex;
			if (!explicitType
					&& aVertexClass.isSuperClassOf(nextVertex
							.getAttributedElementClass()))
				return nextVertex;
			nextVertex = getNextVertex(nextVertex);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextVertexOfExplicitClass(jgralab.Vertex,
	 *      jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(Vertex aVertex, Class aM1VertexClass,
			boolean explicitType) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextVertexOfExplicitClass(int, jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(int vId, VertexClass aVertexClass,
			boolean explicitType) {
		return getNextVertexOfClass(getVertex(vId), aVertexClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextVertexOfExplicitClass(int, jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(int vId, Class aM1VertexClass,
			boolean explicitType) {
		return getNextVertexOfClass(getVertex(vId), aM1VertexClass,
				explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstEdge()
	 */
	public Edge getFirstEdgeInGraph() {
		return edge[edgeOffset(firstEdge)];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstEdgeOfClass(jgralab.EdgeClass)
	 */
	public Edge getFirstEdgeOfClassInGraph(EdgeClass anEdgeClass) {
		return getFirstEdgeOfClassInGraph(anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstEdgeOfClass(jgralab.EdgeClass)
	 */
	public Edge getFirstEdgeOfClassInGraph(Class anEdgeClass) {
		return getFirstEdgeOfClassInGraph(anEdgeClass, false);
	}

	public Edge getFirstEdgeOfClassInGraph(EdgeClass anEdgeClass,
			boolean explicitType) {
		Edge currentEdge = getFirstEdgeInGraph();
		while (currentEdge != null) {
			if (currentEdge.getAttributedElementClass().equals(anEdgeClass))
				return currentEdge;
			if ((!explicitType)
					&& anEdgeClass.isSuperClassOf(currentEdge
							.getAttributedElementClass()))
				return currentEdge;
			currentEdge = currentEdge.getNextEdgeInGraph();
		}
		return null;
	}

	public Edge getFirstEdgeOfClassInGraph(Class anEdgeClass,
			boolean explicitType) {
		Edge currentEdge = getFirstEdgeInGraph();
		Edge firstEdge = currentEdge;
		if (currentEdge != null)
			while (currentEdge != firstEdge) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextEdge(jgralab.Edge)
	 */
	public Edge getNextEdgeInGraph(Edge anEdge) {
		return getNextEdgeInGraph(anEdge.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextEdge(int)
	 */
	public Edge getNextEdgeInGraph(int eId) {
		assert (containsEdgeId(eId));
		return edge[edgeOffset(nextEdgeInGraph[eId])];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextEdgeOfClass(jgralab.Edge, jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(Edge anEdge, EdgeClass anEdgeClass) {
		return getNextEdgeOfClassInGraph(anEdge.getId(), anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextEdgeOfClass(jgralab.Edge, jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(Edge anEdge, Class anEdgeClass) {
		return getNextEdgeOfClassInGraph(anEdge.getId(), anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextEdgeOfClass(int, jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(int eId, EdgeClass anEdgeClass) {
		return getNextEdgeOfClassInGraph(eId, anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextEdgeOfClass(int, jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(int eId, Class anEdgeClass) {
		return getNextEdgeOfClassInGraph(eId, anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextEdgeOfExplicitClass(jgralab.Edge,
	 *      jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(Edge anEdge, EdgeClass anEdgeClass,
			boolean explicitType) {
		return getNextEdgeOfClassInGraph(anEdge.getId(), anEdgeClass,
				explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextEdgeOfExplicitClass(jgralab.Edge,
	 *      jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(Edge anEdge, Class anEdgeClass,
			boolean explicitType) {
		return getNextEdgeOfClassInGraph(anEdge.getId(), anEdgeClass,
				explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextEdgeOfExplicitClass(int, jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(int eId, EdgeClass anEdgeClass,
			boolean explicitType) {
		Edge currentEdge = getNextEdgeInGraph(eId);
		while (currentEdge != null) {
			if (currentEdge.getAttributedElementClass().equals(anEdgeClass))
				return currentEdge;
			if ((!explicitType)
					&& anEdgeClass.isSuperClassOf(currentEdge
							.getAttributedElementClass()))
				return currentEdge;
			currentEdge = currentEdge.getNextEdgeInGraph();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextEdgeOfExplicitClass(int, jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(int eId, Class anEdgeClass,
			boolean explicitType) {
		Edge currentEdge = getNextEdgeInGraph(eId);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidence(jgralab.Vertex)
	 */
	public Edge getFirstEdge(Vertex v) {
		return getFirstEdge(v.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidence(int)
	 */
	public Edge getFirstEdge(int vId) {
		assert (containsVertexId(vId));
		if (firstEdgeAtVertex[vId] == 0)
			return null;
		return edge[edgeOffset(firstEdgeAtVertex[vId])];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidenceOfClass(int, jgralab.EdgeClass)
	 */
	public Edge getFirstEdgeOfClass(int vId, EdgeClass anEdgeClass) {
		return getFirstEdgeOfClass(vId, anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidenceOfClass(int, jgralab.EdgeClass)
	 */
	public Edge getFirstEdgeOfClass(int vId, Class anEdgeClass) {
		return getFirstEdgeOfClass(vId, anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidenceOfClass(int, jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getFirstEdgeOfClass(int vId, EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return getFirstEdgeOfClass(vId, anEdgeClass, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidenceOfClass(int, jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getFirstEdgeOfClass(int vId, Class anEdgeClass,
			EdgeDirection orientation) {
		return getFirstEdgeOfClass(vId, anEdgeClass, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidenceOfExplicitClass(int,
	 *      jgralab.EdgeClass)
	 */
	public Edge getFirstEdgeOfClass(int vId, EdgeClass anEdgeClass,
			boolean explicitType) {
		Edge currentEdge = getFirstEdge(vId);
		while (currentEdge != null) {
			if (currentEdge.getAttributedElementClass().equals(anEdgeClass))
				return currentEdge;
			if ((!explicitType)
					&& (anEdgeClass.isSuperClassOf(currentEdge
							.getAttributedElementClass())))
				return currentEdge;
			currentEdge = currentEdge.getNextEdge();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidenceOfExplicitClass(int,
	 *      jgralab.EdgeClass)
	 */
	public Edge getFirstEdgeOfClass(int vId, Class anEdgeClass,
			boolean explicitType) {
		Edge currentEdge = getFirstEdge(vId);
		while (currentEdge != null) {
			if (explicitType) {
				if (anEdgeClass == currentEdge.getM1Class())
					return currentEdge;
			} else {
				if (anEdgeClass.isInstance(currentEdge))
					return currentEdge;
			}
			currentEdge = currentEdge.getNextEdge();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidenceOfExplicitClass(int,
	 *      jgralab.EdgeClass, boolean)
	 */
	public Edge getFirstEdgeOfClass(int vId, EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		Edge currentEdge = getFirstEdge(vId, orientation);
		if (anEdgeClass == null)
			throw new GraphException("Cannot get first edge of class null");
		while (currentEdge != null) {
			if (currentEdge.getAttributedElementClass().equals(anEdgeClass))
				return currentEdge;
			if ((!explicitType)
					&& (anEdgeClass.isSuperClassOf(currentEdge
							.getAttributedElementClass())))
				return currentEdge;
			currentEdge = currentEdge.getNextEdge(orientation);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidenceOfExplicitClass(int,
	 *      jgralab.EdgeClass, boolean)
	 */
	public Edge getFirstEdgeOfClass(int vId, Class anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		Edge currentEdge = getFirstEdge(vId, orientation);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidence(jgralab.Vertex, boolean)
	 */
	public Edge getFirstEdge(Vertex v, EdgeDirection orientation) {
		return getFirstEdge(v.getId(), orientation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstIncidence(int, boolean)
	 */
	public final Edge getFirstEdge(int vId, EdgeDirection orientation) {
		assert (containsVertexId(vId));
		int nextI = firstEdgeAtVertex[vId];
		if (orientation == EdgeDirection.IN) {
			while (nextI != 0) {
				if (nextI < 0) {
					return edge[edgeOffset(nextI)];
				}
				nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			}
		} else {
			while (nextI != 0) {
				if (nextI > 0) {
					return edge[edgeOffset(nextI)];
				}
				nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidence(jgralab.Incidence)
	 */
	public Edge getNextEdge(Edge e) {
		return getNextEdge(e.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidence(int)
	 */
	public final Edge getNextEdge(int eId) {
		assert (containsEdgeId(eId));
		int nextEdgeId = nextEdgeAtVertex[edgeOffset(eId)];
		return edge[edgeOffset(nextEdgeId)];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidence(jgralab.Incidence, boolean)
	 */
	public Edge getNextEdge(Edge e, EdgeDirection orientation) {
		return getNextEdge(e.getId(), orientation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidence(int, boolean)
	 */
	public Edge getNextEdge(int eId, EdgeDirection orientation) {
		assert (containsEdgeId(eId));
		int nextI = nextEdgeAtVertex[edgeOffset(eId)];
		if (orientation == EdgeDirection.IN) {
			while (nextI != 0) {
				if (nextI < 0)
					return edge[edgeOffset(nextI)];
				nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			}
		} else {
			while (nextI != 0) {
				if (nextI > 0)
					return edge[edgeOffset(nextI)];
				nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidenceOfClass(int, jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(int eId, EdgeClass anEdgeClass) {
		return getNextEdgeOfClass(eId, anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidenceOfClass(int, jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(int eId, Class anEdgeClass) {
		return getNextEdgeOfClass(eId, anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidenceOfExplicitClass(int,
	 *      jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(int eId, EdgeClass anEdgeClass,
			boolean explicitType) {
		Edge nextI = getNextEdge(eId);
		while (nextI != null) {
			if (anEdgeClass.equals(nextI.getAttributedElementClass()))
				return nextI;
			if ((!explicitType)
					&& (nextI.getAttributedElementClass()
							.isSuperClassOf(anEdgeClass)))
				return nextI;
			nextI = nextI.getNextEdge();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidenceOfExplicitClass(int,
	 *      jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(int eId, Class anEdgeClass,
			boolean explicitType) {
		Edge currentEdge = getNextEdge(eId);
		while (currentEdge != null) {
			if (explicitType) {
				if (anEdgeClass == currentEdge.getM1Class())
					return currentEdge;
			} else {
				if (anEdgeClass.isInstance(currentEdge))
					return currentEdge;
			}
			currentEdge = currentEdge.getNextEdge();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidenceOfClass(int, jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getNextEdgeOfClass(int eId, EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return getNextEdgeOfClass(eId, anEdgeClass, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidenceOfClass(int, jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getNextEdgeOfClass(int eId, Class anEdgeClass,
			EdgeDirection orientation) {
		return getNextEdgeOfClass(eId, anEdgeClass, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidenceOfExplicitClass(int,
	 *      jgralab.EdgeClass, boolean)
	 */
	public Edge getNextEdgeOfClass(int eId, EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		Edge nextI = getNextEdge(eId, orientation);
		while (nextI != null) {
			if (anEdgeClass.equals(nextI.getAttributedElementClass()))
				return nextI;
			if ((!explicitType)
					&& (nextI.getAttributedElementClass()
							.isSuperClassOf(anEdgeClass)))
				return nextI;
			nextI = nextI.getNextEdge(orientation);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getNextIncidenceOfExplicitClass(int,
	 *      jgralab.EdgeClass, boolean)
	 */
	public Edge getNextEdgeOfClass(int eId, Class anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		Edge currentEdge = getNextEdge(eId, orientation);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getDegree(jgralab.Vertex)
	 */
	public int getDegree(Vertex v) {
		return getDegree(v.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getDegree(int)
	 */
	public int getDegree(int vId) {
		assert (containsVertexId(vId));
		int nextI = firstEdgeAtVertex[vId];
		int count = 0;
		while (nextI != 0) {
			count++;
			nextI = nextEdgeAtVertex[edgeOffset(nextI)];
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getDegree(jgralab.Vertex, boolean)
	 */
	public int getDegree(Vertex v, EdgeDirection orientation) {
		return getDegree(v.getId(), orientation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getDegree(int, boolean)
	 */
	public int getDegree(int vId, EdgeDirection orientation) {
		assert (containsVertexId(vId));
		int count = 0;
		int nextI = firstEdgeAtVertex[vId];
		if (orientation == EdgeDirection.IN) { // TRUE = IN = negative id
			while (nextI != 0) {
				if (nextI < 0) {
					count++;
				}
				nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			}
		} else { // FALSE = OUT = positive id
			while (nextI != 0) {
				if (nextI > 0) {
					count++;
				}
				nextI = nextEdgeAtVertex[edgeOffset(nextI)];
			}
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getVertex(int)
	 */
	public Vertex getVertex(int vId) {
		assert (vId >= 0 && vId < vSize);
		return vertex[vId];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getVertexOfClass(jgralab.VertexClass, int)
	 */
	@Deprecated
	public Vertex getVertexOfClass(VertexClass vc, int vId) {
		Vertex v = getVertex(vId);
		if (v == null)
			return null;
		if (v.getAttributedElementClass().getName().equals(vc.getName()))
			return v;
		if (vc.isSuperClassOf(v.getAttributedElementClass()))
			return v;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getVertexOfExplicitClass(jgralab.VertexClass, int)
	 */
	@Deprecated
	public Vertex getVertexOfClass(VertexClass vc, int vId, boolean explicitType) {
		if (explicitType) {
			Vertex v = getVertex(vId);
			if (v == null)
				return null;
			if (vc.getName().equals(v.getAttributedElementClass().getName()))
				return getVertex(vId);
			return null;
		} else {
			return getVertexOfClass(vc, vId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getEdge(int)
	 */
	public Edge getEdge(int eId) {
		assert (containsEdgeId(eId));
		return edge[edgeOffset(eId < 0 ? -eId : eId)];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getEdgeOfClass(jgralab.EdgeClass, int)
	 */
	@Deprecated
	public Edge getEdgeOfClassInGraph(EdgeClass ec, int eId) {
		Edge e = getEdge(eId);
		if (e == null)
			return null;
		if (e.getAttributedElementClass().getName().equals(ec.getName()))
			return e;
		if (ec.isSuperClassOf(e.getAttributedElementClass()))
			return e;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getEdgeOfExplicitClass(jgralab.EdgeClass, int)
	 */
	@Deprecated
	public Edge getEdgeOfClassInGraph(EdgeClass ec, int eId,
			boolean explicitType) {
		if (explicitType) {
			Edge e = getEdge(eId);
			if (e == null)
				return null;
			if (ec.getName().equals(e.getAttributedElementClass().getName()))
				return e;
			return null;
		} else {
			return getEdgeOfClassInGraph(ec, eId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getAlpha(jgralab.Edge)
	 */
	public Vertex getAlpha(Edge e) {
		return getAlpha(e.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getAlpha(int)
	 */
	public final Vertex getAlpha(int eId) {
		assert (containsEdgeId(eId));
		return vertex[targetVertex[edgeOffset(eId > 0 ? -eId : eId)]];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getOmega(jgralab.Edge)
	 */
	public Vertex getOmega(Edge e) {
		return getOmega(e.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getOmega(int)
	 */
	public final Vertex getOmega(int eId) {
		assert (containsEdgeId(eId));
		return vertex[targetVertex[edgeOffset(eId > 0 ? eId : -eId)]];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getPrevVertex(int)
	 */
	public Vertex getPrevVertex(int vId) {
		assert (containsVertexId(vId));
		if (firstVertex == 0)
			return null;
		int i = firstVertex;
		while (nextVertex[i] != 0 && nextVertex[i] != vId)
			++i;
		return vertex[i];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getPrevEdge(int)
	 */
	public Edge getPrevEdgeInGraph(int eId) {
		assert (containsEdgeId(eId));
		if (firstEdge == 0)
			return null;
		int i = firstEdge;
		while (nextEdgeInGraph[i] != 0 && nextEdgeInGraph[i] != eId)
			++i;
		return edge[edgeOffset(i)];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#isBeforeVertex(int, int)
	 */
	public boolean isBeforeVertex(int source, int target) {
		assert (containsVertexId(source) && containsVertexId(target));
		if (source == target)
			return false;
		int nextId = nextVertex[source];
		while ((nextId != 0) && (nextId != target)) {
			nextId = nextVertex[nextId];
		}
		return nextId == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#isBeforeEdge(int, int)
	 */
	public boolean isBeforeEdgeInGraph(int source, int target) {
		assert (containsEdgeId(source) && containsEdgeId(target));
		if (source == target)
			return false;
		int nextId = nextEdgeInGraph[source];
		while ((nextId != 0) && (nextId != target)) {
			nextId = nextEdgeInGraph[nextId];
		}
		return nextId == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#isAfterVertex(int, int)
	 */
	public boolean isAfterVertex(int source, int target) {
		assert (containsVertexId(source) && containsVertexId(target));
		if (source == target)
			return false;
		int nextId = nextVertex[source];
		while ((nextId != 0) && (nextId != target)) {
			nextId = nextVertex[nextId];
		}
		return nextId != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#isAfterEdge(int, int)
	 */
	public boolean isAfterEdgeInGraph(int source, int target) {
		assert (containsEdgeId(source) && containsEdgeId(target));
		if (source == target)
			return false;
		int nextId = nextEdgeInGraph[source];
		while ((nextId != 0) && (nextId != target)) {
			nextId = nextEdgeInGraph[nextId];
		}
		return nextId != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#putAfterVertex(int, int)
	 */
	public void putAfterVertex(int target, int source) {
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
			modified();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#putBeforeVertex(int, int)
	 */
	public void putBeforeVertex(int target, int source) {
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
			modified();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#insertVertexAtPos(int, int)
	 */
	public void insertVertexAtPos(int vId, int pos) {
		if (getPositionOfVertex(vId) < pos)
			putAfterVertex(getVertexAtPosition(pos), vId);
		else
			putBeforeVertex(getVertexAtPosition(pos), vId);
		modified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#insertEdgeAtPos(int, int)
	 */
	public void insertEdgeInGraphAtPos(int eId, int pos) {
		if (getPositionOfEdge(eId) < pos)
			putAfterEdgeInGraph(getEdgeAtPosition(pos), eId);
		else
			putBeforeEdgeInGraph(getEdgeAtPosition(pos), eId);
		modified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#deleteVertex(jgralab.Vertex)
	 */
	public void deleteVertex(Vertex v) {
		deleteVertex(v.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#deleteVertex(int)
	 */
	public void deleteVertex(int vId) {
		if (!(containsVertexId(vId) && firstVertex > 0 && vCount > 0)) {
			throw new GraphException("Vertex " + vId + " doesn't exist");
		}
		deleteVertexList.add(vId);
	//	System.err.println("Delete vertex" + getVertex(vId));
		internalDeleteVertex();
		modified();
	}

	private void internalDeleteVertex()  {
		while (!deleteVertexList.isEmpty()) {
			int vId = deleteVertexList.remove(0);
			if (DEBUG) {
				System.out.println("Deleting vertex " + vId);
			}
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

			int eId = firstEdgeAtVertex[vId];
			while (eId != 0) {
				internalDeleteEdge(eId);
				eId = firstEdgeAtVertex[vId];
			}

			firstEdgeAtVertex[vId] = 0;
			lastEdgeAtVertex[vId] = 0;

			if (DEBUG) {
				System.out.println("Vertex " + vId + " deleted.");
				printArray();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#deleteEdge(int)
	 */
	public void deleteEdge(int eId)  {
		if (DEBUG)
			System.out.println("deleting edge " + eId);

		if (!(containsEdgeId(eId) && firstEdge > 0 && eCount > 0)) {
			throw new GraphException("Edge " + eId + " doesn't exist");
		}

		internalDeleteEdge(eId);

		if (!deleteVertexList.isEmpty()) {
			internalDeleteVertex();
		}

		modified();
	}

	private void internalDeleteEdge(int eId) {
		// remove edge from eSeq
		if (eId < 0)
			eId = -eId;
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

		Edge deletedEdge = edge[edgeOffset(eId)];

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

		// check for cascading delete of vertices in composition edges
		AttributedElementClass aec = deletedEdge.getAttributedElementClass();
		if (aec instanceof CompositionClass) {
			CompositionClass comp = (CompositionClass) aec;
			if (comp.isAggregateFrom()) {
				// omega vertex is to be deleted
				if (containsVertexId(omegaId)) {
				//	 System.err.println("Delete omega vertex v" + omegaId + " of composition e" + eId);
					deleteVertexList.add(omegaId);
				}
			} else {
				if (containsVertexId(alphaId)) {
				//	 System.err.println("Delete alpha vertex v" + alphaId + " of composition e" + eId);
					deleteVertexList.add(alphaId);
				}
			}
		}

		if (DEBUG) {
			System.out.println("Edge " + eId + " deleted.");
			printArray();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#deleteIncidenceTo(int, int)
	 */
	public void deleteEdgeTo(int vId, int iNo) {
		if (DEBUG) {
			System.out.println("deleting incidence " + iNo + " of vertex "
					+ vId);
		}

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
		modified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getVCount()
	 */
	public int getVCount() {
		return vCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getECount()
	 */
	public int getECount() {
		return eCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#setFirstEdge(jgralab.Edge)
	 */
	public void setFirstEdgeInGraph(Edge e) {
		firstEdge = e.getId();
		modified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#putAfterEdge(int, int)
	 */
	public void putAfterEdgeInGraph(int target, int source) {
		source = Math.abs(source);
		target = Math.abs(target);
		if (target != source) {
			// delete references to target
			nextEdgeInGraph[getPrevEdgeInGraph(source).getId()] = nextEdgeInGraph[source];
			if (source == firstEdge)
				firstEdge = nextEdgeInGraph[source];
			nextEdgeInGraph[source] = 0;

			// insert source in eSeq after target
			nextEdgeInGraph[source] = nextEdgeInGraph[target];
			nextEdgeInGraph[target] = source;
		}
		modified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#putBeforeEdge(int, int)
	 */
	public void putBeforeEdgeInGraph(int target, int source) {
		source = Math.abs(source);
		target = Math.abs(target);
		if (target != source) {
			// delete references to source
			nextEdgeInGraph[getPrevEdgeInGraph(source).getId()] = nextEdgeInGraph[source];
			if (source == firstEdge)
				firstEdge = nextEdgeInGraph[source];
			nextEdgeInGraph[source] = 0;

			// insert source in eSeq before target
			if (target == firstEdge)
				firstEdge = source;
			nextEdgeInGraph[getPrevEdgeInGraph(target).getId()] = source;
			nextEdgeInGraph[source] = target;
		}
		modified();
	}

	// internally used methods

	/**
	 * @param pos
	 * @return the vertex at position pos of vSeq
	 */
	private int getVertexAtPosition(int pos) {
		assert (1 <= pos && pos <= vCount);
		int result = firstVertex;
		for (int i = 1; i < pos; ++i) {
			result = nextVertex[result];
		}
		return result;
	}

	/**
	 * @param pos
	 * @return the edge at position pos of eSeq
	 */
	private int getEdgeAtPosition(int pos) {
		assert (1 <= pos && pos <= eCount);
		int result = firstEdge;
		for (int i = 1; i < pos; ++i) {
			result = nextEdgeInGraph[result];
		}
		return result;
	}

	/**
	 * @param vId
	 * @return the position of vertex in vSeq
	 */
	private int getPositionOfVertex(int vId) {
		assert (containsVertexId(vId));
		int cnt = 1;
		int v = firstVertex;
		while (v != vId) {
			v = nextVertex[v];
			++cnt;
		}
		return cnt;
	}

	/**
	 * @param eId
	 * @return the position of edge in eSeq
	 */
	private int getPositionOfEdge(int eId) {
		assert (containsEdgeId(eId));
		
		eId = edgeOffset(eId);
		int cnt = 1;
		int e = firstEdge;
		while (e != eId) {
			e = nextEdgeInGraph[e];
			++cnt;
		}
		return cnt;
	}

	/**
	 * expand vertex arrays with the factor factor
	 * 
	 * @param factor
	 * @
	 */
	private void expandVertices(double factor)  {
		int newSize = (int) (vSize * factor);
		if (DEBUG) {
			System.out.println("expanding vertices from " + vSize + " to "
					+ newSize);
			System.out.println("old incidence array:");
			printArray();
		}
		expandVertexArray(newSize);

		if (DEBUG) {
			System.out.println("new incidence array:");
			printArray();
		}
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

	/**
	 * expand the edge array with the factor factor
	 * 
	 * @param factor
	 * @
	 */
	private void expandEdges(double factor)  {
		int newSize = (int) (eSize * factor);
		if (DEBUG) {
			System.out
					.print("expanding edges from " + eSize + " to " + newSize);
			System.out.println(", old offset is " + eSize + ", new offset is "
					+ (int) (eSize * factor - 1));
			System.out.println("old incidence array:");
			printArray();
		}
		expandEdgeArray(newSize);
		if (DEBUG) {
			System.out.println("new incidence array:");
			printArray();
		}
	}

	private boolean containsVertexId(int vId) {
		return vId > 0 && vId < vSize && vertex[vId] != null;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Graph#containsVertex(jgralab.Vertex)
	 */
	public boolean containsVertex(Vertex v) {
		return v != null && v.getGraph() == this && containsVertexId(v.getId());
	}

	private boolean containsEdgeId(int eId) {
		if (eId < 0)
			eId = -eId;
		return eId > 0 && eId < eSize && edge[edgeOffset(eId)] != null;
	}

	/* (non-Javadoc)
	 * @see jgralab.Graph#containsEdge(jgralab.Edge)
	 */
	public boolean containsEdge(Edge e) {
		return e != null && e.getGraph() == this && containsEdgeId(e.getId());
	}
	
	public void setAlpha(Edge edge, Vertex alpha)  {
		setAlpha(edge.getId(), alpha.getId());
	}

	public void setAlpha(int edgeId, int alphaId)  {
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
		modified();
	}

	public void setOmega(Edge e, Vertex omega)  {
		setOmega(e.getId(), omega.getId());
	}

	public void setOmega(int edgeId, int omegaId)  {
		int posId = edgeId;
		int negId = -edgeId;
		if (posId < 0) {
			posId = -edgeId;
			negId = edgeId;
		}
		// System.err.println("Old alpha was: " +
		// targetVertex[edgeOffset(negId)]);
		// System.err.println("Old omega was: " +
		// targetVertex[edgeOffset(posId)]);
		if (targetVertex[edgeOffset(posId)] == omegaId)
			return;
		deleteEdgeTo(targetVertex[edgeOffset(posId)], negId);
		appendEdgeAtVertex(omegaId, negId);
		targetVertex[edgeOffset(posId)] = omegaId;
		// System.out.println("New omega is " +
		// targetVertex[edgeOffset(posId)]);
		modified();
	}

	public void putEdgeBefore(Edge edge, Edge nextEdge) {
		putEdgeBefore(edge.getId(), nextEdge.getId());
	}

	public void putEdgeBefore(int edgeId, int nextEdgeId) {
		if (DEBUG)
			System.out.println("Putting edge: " + edgeId + " before Edge " + nextEdgeId);
		if (targetVertex[edgeOffset(-edgeId)] != targetVertex[edgeOffset(-nextEdgeId)])
			throw new GraphException("Cannot put edge " + edgeId + " before edge " + nextEdgeId + ", edges have different this-vertices");
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
		modified();
	}

	public void putEdgeAfter(Edge edge, Edge previousEdge) {
		putEdgeAfter(edge.getId(), previousEdge.getId());
	}

	public void putEdgeAfter(int edgeId, int previousEdgeId) {
		if (DEBUG)
			System.out.println("Putting edge: " + edgeId + " after Edge " + previousEdgeId);
		int thisVertexId = targetVertex[edgeOffset(-edgeId)];
		if (thisVertexId != targetVertex[edgeOffset(-previousEdgeId)])
			throw new GraphException("Cannot put edge " + edgeId + " after edge " + previousEdgeId + ", edges have different this-vertices");
		if (edgeId == previousEdgeId) 
			return;
		if (edgeId == nextEdgeAtVertex[edgeOffset(previousEdgeId)])
			return;

		//if edgeId is the first edge, make the next one the first one
		if (edgeId == firstEdgeAtVertex[thisVertexId]) {
			firstEdgeAtVertex[thisVertexId] = nextEdgeAtVertex[edgeOffset(edgeId)];
		} else {
			//otherwise remove the edge from the old position
			int oldPreviousEdgeId = 0;
			int currentEdgeId = firstEdgeAtVertex[targetVertex[edgeOffset(-edgeId)]];
			while (currentEdgeId != edgeId) {
				oldPreviousEdgeId = currentEdgeId;
				currentEdgeId = nextEdgeAtVertex[edgeOffset(currentEdgeId)];
			}
			nextEdgeAtVertex[edgeOffset(oldPreviousEdgeId)] = nextEdgeAtVertex[edgeOffset(edgeId)]; 
			//if the edgeId was the last edge at the vertex, set the last edge to the previous one
			if (lastEdgeAtVertex[thisVertexId] == edgeId)
				lastEdgeAtVertex[thisVertexId] = oldPreviousEdgeId;
		}
		//put edge after previous one
		nextEdgeAtVertex[edgeOffset(edgeId)] = nextEdgeAtVertex[edgeOffset(previousEdgeId)];
		nextEdgeAtVertex[edgeOffset(previousEdgeId)] = edgeId;
		//if the new previous edge was the last edge at the vertex, set the last edge to edgeId
		if (lastEdgeAtVertex[thisVertexId] == previousEdgeId)
			lastEdgeAtVertex[thisVertexId] = edgeId;
		modified();
	}

	public void insertEdgeAt(Vertex vertex, Edge edge, int pos) {
		insertEdgeAt(vertex.getId(), edge.getId(), pos);
	}

	public void insertEdgeAt(int vertexId, int edgeId, int pos) {
		// System.out.println("Inserting edge at: " + pos);
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
		modified();
	}

	public void appendEdgeAtVertex(int vertexId, int edgeId) {
		// System.out.println("append edge at vertex");
		int lastEdgeId = lastEdgeAtVertex[vertexId];
		if (lastEdgeId == 0) {
			firstEdgeAtVertex[vertexId] = edgeId;
		} else {
			nextEdgeAtVertex[edgeOffset(lastEdgeAtVertex[vertexId])] = edgeId;
		}
		lastEdgeAtVertex[vertexId] = edgeId;
		nextEdgeAtVertex[edgeOffset(edgeId)] = 0;
		modified();
	}

	public void appendEdgeAtVertex(Vertex vertex, Edge edge) {
		appendEdgeAtVertex(vertex.getId(), edge.getId());
	}
}
