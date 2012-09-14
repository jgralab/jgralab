/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralab.impl.std;

import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.TemporaryEdge;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.FreeIndexList;
import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * The implementation of a <code>Graph</code> accessing attributes without
 * versioning.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class GraphImpl extends
		de.uni_koblenz.jgralab.impl.GraphBaseImpl {
	private InternalVertex[] vertex;
	private int vCount;
	private InternalEdge[] edge;
	private InternalEdge[] revEdge;
	private int eCount;
	private InternalVertex firstVertex;
	private InternalVertex lastVertex;
	private InternalEdge firstEdge;
	private InternalEdge lastEdge;
	private ThreadLocal<TraversalContext> tc = new ThreadLocal<TraversalContext>();

	/**
	 * Holds the version of the vertex sequence. For every modification (e.g.
	 * adding/deleting a vertex or changing the vertex sequence) this version
	 * number is increased by 1. It is set to 0 when the graph is loaded.
	 */
	private long vertexListVersion;

	/**
	 * Holds the version of the edge sequence. For every modification (e.g.
	 * adding/deleting an edge or changing the edge sequence) this version
	 * number is increased by 1. It is set to 0 when the graph is loaded.
	 */
	private long edgeListVersion;

	/**
	 * List of vertices to be deleted by a cascading delete caused by deletion
	 * of a composition "parent".
	 */
	private List<InternalVertex> deleteVertexList;

	@Override
	public InternalVertex[] getVertex() {
		return vertex;
	}

	@Override
	public int getVCountInVSeq() {
		return vCount;
	}

	@Override
	public InternalEdge[] getEdge() {
		return edge;
	}

	@Override
	public InternalEdge[] getRevEdge() {
		return revEdge;
	}

	@Override
	public int getECountInESeq() {
		return eCount;
	}

	@Override
	public InternalVertex getFirstVertexInVSeq() {
		return firstVertex;
	}

	@Override
	public InternalVertex getLastVertexInVSeq() {
		return lastVertex;
	}

	@Override
	public InternalEdge getFirstEdgeInESeq() {
		return firstEdge;
	}

	@Override
	public InternalEdge getLastEdgeInESeq() {
		return lastEdge;
	}

	@Override
	public FreeIndexList getFreeVertexList() {
		return freeVertexList;
	}

	@Override
	public FreeIndexList getFreeEdgeList() {
		return freeEdgeList;
	}

	@Override
	public void setVertex(InternalVertex[] vertex) {
		this.vertex = vertex;
	}

	@Override
	public void setVCount(int count) {
		vCount = count;
	}

	@Override
	public void setEdge(InternalEdge[] edge) {
		this.edge = edge;
	}

	@Override
	public void setRevEdge(InternalEdge[] revEdge) {
		this.revEdge = revEdge;
	}

	@Override
	public void setECount(int count) {
		eCount = count;
	}

	@Override
	public void setFirstVertex(InternalVertex firstVertex) {
		this.firstVertex = firstVertex;
	}

	@Override
	public void setLastVertex(InternalVertex lastVertex) {
		this.lastVertex = lastVertex;
	}

	@Override
	public void setFirstEdgeInGraph(InternalEdge firstEdge) {
		this.firstEdge = firstEdge;
	}

	@Override
	public void setLastEdgeInGraph(InternalEdge lastEdge) {
		this.lastEdge = lastEdge;
	}

	@Override
	public List<InternalVertex> getDeleteVertexList() {
		return deleteVertexList;
	}

	@Override
	public void setDeleteVertexList(List<InternalVertex> deleteVertexList) {
		this.deleteVertexList = deleteVertexList;
	}

	@Override
	public void setVertexListVersion(long vertexListVersion) {
		this.vertexListVersion = vertexListVersion;
	}

	@Override
	public long getVertexListVersion() {
		return vertexListVersion;
	}

	@Override
	public void setEdgeListVersion(long edgeListVersion) {
		this.edgeListVersion = edgeListVersion;
	}

	@Override
	public long getEdgeListVersion() {
		return edgeListVersion;
	}

	/**
	 * 
	 * @param id
	 * @param cls
	 * @param max
	 * @param max2
	 */
	protected GraphImpl(String id, GraphClass cls, int max, int max2) {
		super(id, cls, max, max2);
	}

	protected GraphImpl(String id, GraphClass cls) {
		super(id, cls);
	}

	@Override
	public int allocateVertexIndex(int currentId) {
		int vId = freeVertexList.allocateIndex();
		if (vId == 0) {
			expandVertexArray(getExpandedVertexCount());
			vId = freeVertexList.allocateIndex();
		}
		return vId;
	}

	@Override
	public int allocateEdgeIndex(int currentId) {
		int eId = freeEdgeList.allocateIndex();
		if (eId == 0) {
			expandEdgeArray(getExpandedEdgeCount());
			eId = freeEdgeList.allocateIndex();
		}
		return eId;
	}

	/*
	 * @Override protected void freeIndex(FreeIndexList freeIndexList, int
	 * index) { freeIndexList.freeIndex(index); }
	 */

	@Override
	public void freeEdgeIndex(int index) {
		freeEdgeList.freeIndex(index);
	}

	@Override
	public void freeVertexIndex(int index) {
		freeVertexList.freeIndex(index);
	}

	@Override
	public void vertexAfterDeleted(Vertex vertexToBeDeleted) {

	}

	@Override
	public void edgeAfterDeleted(Edge edgeToBeDeleted, Vertex oldAlpha,
			Vertex oldOmega) {

	}

	@Override
	public TraversalContext getTraversalContext() {
		return tc.get();
	}

	@Override
	public TraversalContext setTraversalContext(TraversalContext tc) {
		TraversalContext oldTc = this.tc.get();
		this.tc.set(tc);
		return oldTc;
	}

	@Override
	public TemporaryVertex createTemporaryVertex() {
		return new TemporaryVertexImpl(0, this);
	}

	@Override
	public TemporaryVertex createTemporaryVertex(VertexClass preliminaryType) {
		return new TemporaryVertexImpl(0, this, preliminaryType);
	}

	@Override
	public TemporaryEdge createTemporaryEdge(Vertex alpha, Vertex omega) {
		return new TemporaryEdgeImpl(0, this, alpha, omega);
	}

	@Override
	public TemporaryEdge createTemporaryEdge(EdgeClass preliminaryType,
			Vertex alpha, Vertex omega) {
		return new TemporaryEdgeImpl(0, this, preliminaryType, alpha, omega);
	}

	@Override
	public boolean hasTemporaryElements() {
		return this.getFirstVertex(this.getGraphClass()
				.getTemporaryVertexClass()) != null
				|| this.getFirstEdge(this.getGraphClass()
						.getTemporaryEdgeClass()) != null;
	}

}
