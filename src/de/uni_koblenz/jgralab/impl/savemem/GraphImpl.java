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
package de.uni_koblenz.jgralab.impl.savemem;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLabList;
import de.uni_koblenz.jgralab.JGraLabMap;
import de.uni_koblenz.jgralab.JGraLabSet;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.EdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.FreeIndexList;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.trans.Savepoint;
import de.uni_koblenz.jgralab.trans.Transaction;

/**
 * The implementation of a <code>Graph</code> accessing attributes without
 * versioning.
 * 
 * @author Jose Monte {monte@uni-koblenz.de} (original implementation)
 * @author Mahdi Derakhshanmanesh {manesh@uni-koblenz.de} (adjusted version)
 */
public abstract class GraphImpl extends
		de.uni_koblenz.jgralab.impl.GraphBaseImpl {
	/**
	 * A list of {@link Vertex} instances, owned by this {@link Graph}.
	 */
	private VertexBaseImpl[] vertex;

	/**
	 * The amount of {@link Vertex} instances, owned by this {@link Graph}.
	 */
	private int vCount;

	/**
	 * A list of {@link Edge} instances, owned by this {@link Graph}.
	 */
	private EdgeBaseImpl[] edge;

	/**
	 * A reversed list of {@link Edge} instances, owned by this {@link Graph}.
	 */
	private ReversedEdgeBaseImpl[] revEdge;

	/**
	 * The amount of {@link Edge} instances, owned by this {@link Graph}.
	 */
	private int eCount;

	/**
	 * The first {@link Vertex} in this {@link Graph}.
	 */
	private VertexBaseImpl firstVertex;

	/**
	 * The last {@link Vertex} in this {@link Graph}.
	 */
	private VertexBaseImpl lastVertex;

	/**
	 * The first {@link Edge} in this {@link Graph}.
	 */
	private EdgeBaseImpl firstEdge;

	/**
	 * The last {@link Edge} in this {@link Graph}.
	 */
	private EdgeBaseImpl lastEdge;

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
	private List<VertexBaseImpl> deleteVertexList;

	/**
	 * @param id
	 *            The id of the {@link Graph}.
	 * @param cls
	 *            The {@link GraphClass} of this {@link Graph}.
	 * @param vMax
	 *            The initial maximum number of {@link Vertex} instances.
	 * @param eMax
	 *            The initial maximum number of {@link Edge} instances.
	 */
	protected GraphImpl(String id, GraphClass cls, int max, int max2) {
		super(id, cls, max, max2);
	}

	/**
	 * @param id
	 *            The id of the {@link Graph}.
	 * @param cls
	 *            The {@link GraphClass} of this {@link Graph}.
	 */
	protected GraphImpl(String id, GraphClass cls) {
		super(id, cls);
	}

	@Override
	protected VertexBaseImpl[] getVertex() {
		return vertex;
	}

	@Override
	public int getVCount() {
		return vCount;
	}

	@Override
	protected EdgeBaseImpl[] getEdge() {
		return edge;
	}

	@Override
	protected ReversedEdgeBaseImpl[] getRevEdge() {
		return revEdge;
	}

	@Override
	public int getECount() {
		return eCount;
	}

	@Override
	public Vertex getFirstVertex() {
		return firstVertex;
	}

	@Override
	public Vertex getLastVertex() {
		return lastVertex;
	}

	@Override
	public Edge getFirstEdgeInGraph() {
		return firstEdge;
	}

	@Override
	public Edge getLastEdgeInGraph() {
		return lastEdge;
	}

	@Override
	protected FreeIndexList getFreeVertexList() {
		return freeVertexList;
	}

	@Override
	protected FreeIndexList getFreeEdgeList() {
		return freeEdgeList;
	}

	@Override
	protected void setVertex(VertexBaseImpl[] vertex) {
		this.vertex = vertex;
	}

	@Override
	protected void setVCount(int count) {
		vCount = count;
	}

	@Override
	protected void setEdge(EdgeBaseImpl[] edge) {
		this.edge = edge;
	}

	@Override
	protected void setRevEdge(ReversedEdgeBaseImpl[] revEdge) {
		this.revEdge = revEdge;
	}

	@Override
	protected void setECount(int count) {
		eCount = count;
	}

	@Override
	protected void setFirstVertex(VertexBaseImpl firstVertex) {
		this.firstVertex = firstVertex;
	}

	@Override
	protected void setLastVertex(VertexBaseImpl lastVertex) {
		this.lastVertex = lastVertex;
	}

	@Override
	protected void setFirstEdgeInGraph(EdgeBaseImpl firstEdge) {
		this.firstEdge = firstEdge;
	}

	@Override
	protected void setLastEdgeInGraph(EdgeBaseImpl lastEdge) {
		this.lastEdge = lastEdge;
	}

	@Override
	protected List<VertexBaseImpl> getDeleteVertexList() {
		return deleteVertexList;
	}

	@Override
	protected void setDeleteVertexList(List<VertexBaseImpl> deleteVertexList) {
		this.deleteVertexList = deleteVertexList;
	}

	@Override
	protected void setVertexListVersion(long vertexListVersion) {
		this.vertexListVersion = vertexListVersion;
	}

	@Override
	public long getVertexListVersion() {
		return vertexListVersion;
	}

	@Override
	protected void setEdgeListVersion(long edgeListVersion) {
		this.edgeListVersion = edgeListVersion;
	}

	@Override
	public long getEdgeListVersion() {
		return edgeListVersion;
	}

	@Override
	public void abort() {
		throw new UnsupportedOperationException(
				"Abort is not supported for this graph.");
	}

	@Override
	public void commit() {
		throw new UnsupportedOperationException(
				"Commit is not supported for this graph.");
	}

	@Override
	public Transaction newReadOnlyTransaction() {
		throw new UnsupportedOperationException(
				"Creation of read-only-transactions is not supported for this graph.");
	}

	@Override
	public Transaction newTransaction() {
		throw new UnsupportedOperationException(
				"Creation of read-write-transactions is not supported for this graph.");
	}

	@Override
	public Savepoint defineSavepoint() {
		throw new UnsupportedOperationException(
				"Definition of save-points is not supported for this graph.");
	}

	@Override
	public Transaction getCurrentTransaction() {
		throw new UnsupportedOperationException(
				"Transactions are not supported for this graph.");
	}

	@Override
	public void restoreSavepoint(Savepoint savepoint) {
		throw new UnsupportedOperationException(
				"Definition of save-points is not supported for this graph.");
	}

	@Override
	public void setCurrentTransaction(Transaction transaction) {
		throw new UnsupportedOperationException(
				"Transactions are not supported for this graph.");
	}

	@Override
	public boolean isInConflict() {
		throw new UnsupportedOperationException(
				"Transactions are not supported for this graph.");
	}

	@Override
	protected int allocateVertexIndex(int currentId) {
		int vId = freeVertexList.allocateIndex();
		if (vId == 0) {
			expandVertexArray(getExpandedVertexCount());
			vId = freeVertexList.allocateIndex();
		}
		return vId;
	}

	@Override
	protected int allocateEdgeIndex(int currentId) {
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
	protected void freeEdgeIndex(int index) {
		freeEdgeList.freeIndex(index);
	}

	@Override
	protected void freeVertexIndex(int index) {
		freeVertexList.freeIndex(index);
	}

	@Override
	protected void vertexAfterDeleted(Vertex vertexToBeDeleted) {

	}

	@Override
	protected void edgeAfterDeleted(Edge edgeToBeDeleted, Vertex oldAlpha,
			Vertex oldOmega) {

	}

	@Override
	public final boolean hasStandardSupport() {
		return false;
	}

	@Override
	public final boolean hasTransactionSupport() {
		return false;
	}

	@Override
	public final boolean hasSavememSupport() {
		return true;
	}

	@Override
	public <T extends Record> T createRecord(Class<T> recordClass, GraphIO io) {
		T record = graphFactory.createRecordWithSavememSupport(recordClass,
				this);
		try {
			record.readComponentValues(io);
		} catch (GraphIOException e) {
			e.printStackTrace();
		}
		return record;
	}

	@Override
	public <T extends Record> T createRecord(Class<T> recordClass,
			Map<String, Object> fields) {
		T record = graphFactory.createRecordWithSavememSupport(recordClass,
				this);
		record.setComponentValues(fields);
		return record;
	}

	@Override
	public <T extends Record> T createRecord(Class<T> recordClass,
			Object... components) {
		T record = graphFactory.createRecordWithSavememSupport(recordClass,
				this);
		record.setComponentValues(components);
		return record;
	}

	@Override
	public <T> JGraLabList<T> createList() {
		return new JGraLabListImpl<T>();
	}

	@Override
	public <T> JGraLabList<T> createList(Collection<? extends T> collection) {
		return new JGraLabListImpl<T>(collection);
	}

	@Override
	public <T> JGraLabList<T> createList(int initialCapacity) {
		return new JGraLabListImpl<T>(initialCapacity);
	}

	@Override
	public <T> JGraLabSet<T> createSet() {
		return new JGraLabSetImpl<T>();
	}

	@Override
	public <T> JGraLabSet<T> createSet(Collection<? extends T> collection) {
		return new JGraLabSetImpl<T>(collection);
	}

	@Override
	public <T> JGraLabSet<T> createSet(int initialCapacity) {
		return new JGraLabSetImpl<T>(initialCapacity);
	}

	@Override
	public <T> JGraLabSet<T> createSet(int initialCapacity, float loadFactor) {
		return new JGraLabSetImpl<T>(initialCapacity, loadFactor);
	}

	@Override
	public <K, V> JGraLabMap<K, V> createMap() {
		return new JGraLabMapImpl<K, V>();
	}

	@Override
	public <K, V> JGraLabMap<K, V> createMap(Map<? extends K, ? extends V> map) {
		return new JGraLabMapImpl<K, V>(map);
	}

	@Override
	public <K, V> JGraLabMap<K, V> createMap(int initialCapacity) {
		return new JGraLabMapImpl<K, V>(initialCapacity);
	}

	@Override
	public <K, V> JGraLabMap<K, V> createMap(int initialCapacity,
			float loadFactor) {
		return new JGraLabMapImpl<K, V>(initialCapacity, loadFactor);
	}

	@Override
	protected void addVertex(Vertex newVertex) {
		super.addVertex(newVertex);
	}

	@Override
	protected void addEdge(Edge newEdge, Vertex alpha, Vertex omega) {
		super.addEdge(newEdge, alpha, omega);
	}

	@Override
	protected Edge internalCreateEdge(Class<? extends Edge> cls, Vertex alpha,
			Vertex omega) {
		return graphFactory.createEdgeWithSavememSupport(cls, 0, this, alpha,
				omega);
	}

	@Override
	protected Vertex internalCreateVertex(Class<? extends Vertex> cls) {
		return graphFactory.createVertexWithSavememSupport(cls, 0, this);
	}

}
