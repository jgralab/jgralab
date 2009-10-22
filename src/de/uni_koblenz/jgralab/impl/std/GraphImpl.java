package de.uni_koblenz.jgralab.impl.std;

import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.EdgeImpl;
import de.uni_koblenz.jgralab.impl.FreeIndexList;
import de.uni_koblenz.jgralab.impl.ReversedEdgeImpl;
import de.uni_koblenz.jgralab.impl.VertexImpl;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.trans.Savepoint;
import de.uni_koblenz.jgralab.trans.Transaction;

/**
 * The implementation of a <code>Graph</code> accessing attributes without
 * versioning.
 * 
 * @author José Monte(monte@uni-koblenz.de)
 */
public abstract class GraphImpl extends de.uni_koblenz.jgralab.impl.GraphImpl {
	private VertexImpl[] vertex;
	private int vCount;
	private EdgeImpl[] edge;
	private ReversedEdgeImpl[] revEdge;
	private int eCount;
	private VertexImpl firstVertex;
	private VertexImpl lastVertex;
	private EdgeImpl firstEdge;
	private EdgeImpl lastEdge;

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
	private List<VertexImpl> deleteVertexList;

	@Override
	protected VertexImpl[] getVertex() {
		return vertex;
	}

	@Override
	public int getVCount() {
		return vCount;
	}

	@Override
	protected EdgeImpl[] getEdge() {
		return edge;
	}

	@Override
	protected ReversedEdgeImpl[] getRevEdge() {
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
	protected void setVertex(VertexImpl[] vertex) {
		this.vertex = vertex;
	}

	@Override
	protected void setVCount(int count) {
		vCount = count;
	}

	@Override
	protected void setEdge(EdgeImpl[] edge) {
		this.edge = edge;
	}

	@Override
	protected void setRevEdge(ReversedEdgeImpl[] revEdge) {
		this.revEdge = revEdge;
	}

	@Override
	protected void setECount(int count) {
		eCount = count;
	}

	@Override
	protected void setFirstVertex(VertexImpl firstVertex) {
		this.firstVertex = firstVertex;
	}

	@Override
	protected void setLastVertex(VertexImpl lastVertex) {
		this.lastVertex = lastVertex;
	}

	@Override
	protected void setFirstEdgeInGraph(EdgeImpl firstEdge) {
		this.firstEdge = firstEdge;
	}

	@Override
	protected void setLastEdgeInGraph(EdgeImpl lastEdge) {
		this.lastEdge = lastEdge;
	}

	@Override
	protected List<VertexImpl> getDeleteVertexList() {
		return deleteVertexList;
	}

	@Override
	protected void setDeleteVertexList(List<VertexImpl> deleteVertexList) {
		this.deleteVertexList = deleteVertexList;
	}

	@Override
	public long getGraphVersion() {
		return graphVersion;
	}

	@Override
	protected void setVertexListVersion(long vertexListVersion) {
		this.vertexListVersion = vertexListVersion;
	}

	@Override
	public long getVertexListVersion() {
		return vertexListVersion;
	}

	/**
	 * Sets the version counter of this graph. Should only be called by GraphIO
	 * immediately after loading.
	 * 
	 * @param graphVersion
	 *            new version value
	 */
	@Override
	public void setGraphVersion(long graphVersion) {
		this.graphVersion = graphVersion;
	}

	@Override
	protected void setEdgeListVersion(long edgeListVersion) {
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
	public GraphImpl(String id, GraphClass cls, int max, int max2) {
		super(id, cls, max, max2);
	}

	public GraphImpl(String id, GraphClass cls) {
		super(id, cls);
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
	public final boolean hasTransactionSupport() {
		return false;
	}
}
