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
package de.uni_koblenz.jgralab.impl.trans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphStructureChangedListener;
import de.uni_koblenz.jgralab.TemporaryEdge;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.VertexFilter;
import de.uni_koblenz.jgralab.exception.GraphException;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.impl.FreeIndexList;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralab.trans.InvalidSavepointException;
import de.uni_koblenz.jgralab.trans.ListPosition;
import de.uni_koblenz.jgralab.trans.Savepoint;
import de.uni_koblenz.jgralab.trans.Transaction;
import de.uni_koblenz.jgralab.trans.TransactionManager;
import de.uni_koblenz.jgralab.trans.TransactionState;
import de.uni_koblenz.jgralab.trans.VersionedDataObject;

/**
 * The implementation of a <code>Graph</edge> with versioning.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class GraphImpl extends
		de.uni_koblenz.jgralab.impl.GraphBaseImpl {
	private static Logger logger = null; // JGraLab.getLogger(GraphImpl.class);

	// the transactions of this instance are managed by a transaction manager
	private TransactionManager transactionManager;

	// TODO think about representing edge, [revEdge] and vertex as
	// java.util.List!?
	// With this eCount and vCount maybe could be removed (for saving memory)!?
	// represents Eset
	protected VersionedArrayImpl<EdgeImpl[]> edge;
	// TODO maybe think about removing revEdge completely (for saving memory)?!
	protected VersionedArrayImpl<ReversedEdgeImpl[]> revEdge;
	private VersionedReferenceImpl<Integer> eCount;

	// represents Vset
	protected VersionedArrayImpl<VertexImpl[]> vertex;
	private VersionedReferenceImpl<Integer> vCount;

	// represents begin and end of Eseq
	private VersionedReferenceImpl<EdgeImpl> firstEdge;
	private VersionedReferenceImpl<EdgeImpl> lastEdge;
	protected VersionedReferenceImpl<Long> edgeListVersion;

	// represents begin and end of Vseq
	private VersionedReferenceImpl<VertexImpl> firstVertex;
	private VersionedReferenceImpl<VertexImpl> lastVertex;
	protected VersionedReferenceImpl<Long> vertexListVersion;

	// for synchronization when expanding graph...
	protected ReadWriteLock vertexSync;
	protected ReadWriteLock edgeSync;

	// holds indexes of <code>GraphElement</code>s which couldn't be freed after
	// a COMMIT or an ABORT...these indexes should be freed later.
	protected List<Integer> edgeIndexesToBeFreed;
	protected List<Integer> vertexIndexesToBeFreed;

	/**
	 * 
	 * @return increases value of persistentVersionCounter (graphVersion) if
	 *         allowed and returns it
	 */
	protected long incrPersistentVersionCounter() {
		Transaction transaction = getCurrentTransaction();
		if (transaction.getState() != TransactionState.WRITING) {
			throw new GraphException(
					"Increasing persistent version counter only allowed in writing-phase.");
		}
		// increase by value "1"
		setGraphVersion(getGraphVersion() + 1);
		return getGraphVersion();
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
		transactionManager = TransactionManagerImpl.getInstance(this);
	}

	/**
	 * @param id
	 * @param cls
	 */
	public GraphImpl(String id, GraphClass cls) {
		super(id, cls);
	}

	// --- getter ---//

	/**
	 * 
	 * @return the current value of persistentVersionCounter (graphVersion)
	 */
	protected long getPersistentVersionCounter() {
		return getGraphVersion();
	}

	@Override
	public int getECountInESeq() {
		if (eCount == null) {
			return 0;
		}
		Integer value = eCount.getValidValue(getCurrentTransaction());
		if (value == null) {
			return 0;
		}
		return value;
	}

	@Override
	public EdgeImpl[] getEdge() {
		// accessing edge-Array while expanding it should not be allowed
		edgeSync.readLock().lock();
		EdgeImpl[] value = null;
		// important for correct loading of graph
		if (isLoading()) {
			value = edge.getLatestPersistentValue();
		} else {
			Transaction transaction = getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			// important for correct execution of validation-phase
			if (transaction.getState() == TransactionState.VALIDATING) {
				value = edge.getLatestPersistentValue();
			} else {
				value = edge.getValidValue(getCurrentTransaction());
			}
		}
		edgeSync.readLock().unlock();
		return value;
	}

	@Override
	public ReversedEdgeImpl[] getRevEdge() {
		// accessing revEdge-Array while expanding it should not be allowed
		edgeSync.readLock().lock();
		ReversedEdgeImpl[] value = null;
		if (isLoading()) {
			value = revEdge.getLatestPersistentValue();
		} else {
			Transaction transaction = getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			// important for correct execution of validation-phase
			if (transaction.getState() == TransactionState.VALIDATING) {
				value = revEdge.getLatestPersistentValue();
			} else {
				value = revEdge.getValidValue(getCurrentTransaction());
			}
		}
		edgeSync.readLock().unlock();
		return value;
	}

	@Override
	public VertexImpl[] getVertex() {
		// accessing vertex-Array while expanding it should not be allowed
		vertexSync.readLock().lock();
		VertexImpl[] value = null;
		// important for correct loading of graph
		if (isLoading()) {
			value = vertex.getLatestPersistentValue();
		} else {
			Transaction transaction = getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			// important for correct execution of validation-phase
			if (transaction.getState() == TransactionState.VALIDATING) {
				value = vertex.getLatestPersistentValue();
			} else {
				value = vertex.getValidValue(getCurrentTransaction());
			}
		}
		vertexSync.readLock().unlock();
		return value;
	}

	@Override
	public InternalEdge getFirstEdgeInESeq() {
		if (firstEdge == null) {
			return null;
		}
		return firstEdge.getValidValue(getCurrentTransaction());
	}

	@Override
	public InternalVertex getFirstVertexInVSeq() {
		if (firstVertex == null) {
			return null;
		}
		return firstVertex.getValidValue(getCurrentTransaction());
	}

	@Override
	public InternalEdge getLastEdgeInESeq() {
		if (lastEdge == null) {
			return null;
		}
		return lastEdge.getValidValue(getCurrentTransaction());
	}

	@Override
	public InternalVertex getLastVertexInVSeq() {
		if (lastVertex == null) {
			return null;
		}
		return lastVertex.getValidValue(getCurrentTransaction());
	}

	@Override
	public int getVCountInVSeq() {
		if (vCount == null) {
			return 0;
		}
		Integer value = vCount.getValidValue(getCurrentTransaction());
		if (value == null) {
			return 0;
		}
		return value;
	}

	@Override
	public FreeIndexList getFreeVertexList() {
		synchronized (freeVertexList) {
			return freeVertexList;
		}
	}

	@Override
	public FreeIndexList getFreeEdgeList() {
		synchronized (freeEdgeList) {
			return freeEdgeList;
		}
	}

	@Override
	public long getVertexListVersion() {
		if (vertexListVersion == null) {
			return 0;
		}
		Long value = vertexListVersion.getValidValue(getCurrentTransaction());
		if (value == null) {
			return 0;
		}
		return value;
	}

	@Override
	public long getEdgeListVersion() {
		if (edgeListVersion == null) {
			return 0;
		}
		Long value = edgeListVersion.getValidValue(getCurrentTransaction());
		if (value == null) {
			return 0;
		}
		return value;
	}

	/**
	 * 
	 * @return the delete vertex list of the current transaction
	 */
	@Override
	public List<InternalVertex> getDeleteVertexList() {
		TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
		assert ((transaction != null) && ((transaction.getState() == TransactionState.RUNNING) || (transaction
				.getState() == TransactionState.WRITING)));
		if (transaction.deleteVertexList == null) {
			transaction.deleteVertexList = new LinkedList<InternalVertex>();
		}
		return transaction.deleteVertexList;
	}

	// --- setter ---//

	/**
	 * 
	 * @param graphVersion
	 *            update value of graphVersion which is used as
	 *            persistentVersionCounter
	 * 
	 *            TODO rethink with -1 at the beginning. Maybe there is a more
	 *            elegant way?!
	 */
	@Override
	public void setGraphVersion(long graphVersion) {
		if ((getGraphVersion() != -1) && !isLoading()) {
			Transaction transaction = getCurrentTransaction();
			assert (transaction != null);
			// only update graphVersion (persistentVersionCounter) when
			// transaction is in writing-phase
			if (transaction.getState() == TransactionState.WRITING) {
				super.setGraphVersion(graphVersion);
			}
		} else {
			// for initialization
			super.setGraphVersion(graphVersion);
		}
	}

	@Override
	public void setECount(int count) {
		if ((eCount == null) || isLoading()) {
			eCount = new VersionedReferenceImpl<Integer>(this, count, "$eCount");
		} else {
			eCount.setValidValue(count, getCurrentTransaction());
		}
	}

	@Override
	public void setEdge(InternalEdge[] edge) {
		edgeSync.readLock().lock();
		try {
			this.edge.setValidValue((EdgeImpl[]) edge, getCurrentTransaction());
		} finally {
			edgeSync.readLock().unlock();
		}
	}

	@Override
	public void setFirstEdgeInGraph(InternalEdge firstEdge) {
		if ((this.firstEdge == null) || isLoading()) {
			this.firstEdge = new VersionedReferenceImpl<EdgeImpl>(this,
					(EdgeImpl) firstEdge, "$firstEdge");
		} else {
			this.firstEdge.setValidValue((EdgeImpl) firstEdge,
					getCurrentTransaction());
		}
	}

	@Override
	public void setFirstVertex(InternalVertex firstVertex) {
		if ((this.firstVertex == null) || isLoading()) {
			this.firstVertex = new VersionedReferenceImpl<VertexImpl>(this,
					(VertexImpl) firstVertex, "$firstVertex");
		} else {
			this.firstVertex.setValidValue((VertexImpl) firstVertex,
					getCurrentTransaction());
		}
	}

	@Override
	public void setLastEdgeInGraph(InternalEdge lastEdge) {
		if ((this.lastEdge == null) || isLoading()) {
			this.lastEdge = new VersionedReferenceImpl<EdgeImpl>(this,
					(EdgeImpl) lastEdge, "$lastEdge");
		} else {
			this.lastEdge.setValidValue((EdgeImpl) lastEdge,
					getCurrentTransaction());
		}
	}

	@Override
	public void setLastVertex(InternalVertex lastVertex) {
		if ((this.lastVertex == null) || isLoading()) {
			this.lastVertex = new VersionedReferenceImpl<VertexImpl>(this,
					(VertexImpl) lastVertex, "$lastVertex");
		} else {
			this.lastVertex.setValidValue((VertexImpl) lastVertex,
					getCurrentTransaction());
		}
	}

	@Override
	public void setRevEdge(InternalEdge[] revEdge) {
		edgeSync.readLock().lock();
		try {
			this.revEdge.setValidValue((ReversedEdgeImpl[]) revEdge,
					getCurrentTransaction());
		} finally {
			edgeSync.readLock().unlock();
		}
	}

	@Override
	public void setVCount(int count) {
		if ((vCount == null) || isLoading()) {
			vCount = new VersionedReferenceImpl<Integer>(this, count, "$vCount");
		} else {
			vCount.setValidValue(count, getCurrentTransaction());
		}
	}

	@Override
	public void setVertex(InternalVertex[] vertex) {
		vertexSync.readLock().lock();
		try {
			this.vertex.setValidValue((VertexImpl[]) vertex,
					getCurrentTransaction());
		} finally {
			vertexSync.readLock().unlock();
		}
	}

	@Override
	public void setVertexListVersion(long vertexListVersion) {
		if (this.vertexListVersion == null) {
			this.vertexListVersion = new VersionedReferenceImpl<Long>(this,
					vertexListVersion, "$vertexListVersion");
		}
		this.vertexListVersion.setValidValue(vertexListVersion,
				getCurrentTransaction());
	}

	@Override
	public void setEdgeListVersion(long edgeListVersion) {
		if (this.edgeListVersion == null) {
			this.edgeListVersion = new VersionedReferenceImpl<Long>(this,
					edgeListVersion, "$edgeListVersion");
		}
		this.edgeListVersion.setValidValue(edgeListVersion,
				getCurrentTransaction());
	}

	/**
	 * nothing needed here
	 */
	@Override
	public void setDeleteVertexList(List<InternalVertex> deleteVertexList) {
		// do nothing here
	}

	@Override
	public void abort() {
		if (getCurrentTransaction() == null) {
			throw new GraphException("Current transaction is null.");
		}
		getCurrentTransaction().abort();
	}

	@Override
	public void commit() throws CommitFailedException {
		if (getCurrentTransaction() == null) {
			throw new GraphException("Current transaction is null.");
		}
		getCurrentTransaction().commit();
	}

	@Override
	public Transaction newReadOnlyTransaction() {
		if (logger != null) {
			logger.fine("");
		}
		return transactionManager.createReadOnlyTransaction();
	}

	@Override
	public Transaction newTransaction() {
		if (logger != null) {
			logger.fine("");
		}
		return transactionManager.createTransaction();
	}

	@Override
	public Savepoint defineSavepoint() {
		if (getCurrentTransaction() == null) {
			throw new GraphException("Current transaction is null.");
		}
		return getCurrentTransaction().defineSavepoint();
	}

	@Override
	public Transaction getCurrentTransaction() {
		if (transactionManager == null) {
			transactionManager = TransactionManagerImpl.getInstance(this);
		}
		return transactionManager.getTransactionForThread(Thread
				.currentThread());
	}

	@Override
	public void restoreSavepoint(Savepoint savepoint)
			throws InvalidSavepointException {
		if (getCurrentTransaction() == null) {
			throw new GraphException("Current transaction is null.");
		}
		getCurrentTransaction().restoreSavepoint(savepoint);
	}

	@Override
	public void setCurrentTransaction(Transaction transaction) {
		if (logger != null) {
			logger.fine("tx id=" + transaction.getID());
		}
		transactionManager.setTransactionForThread(transaction,
				Thread.currentThread());
	}

	@Override
	public boolean isInConflict() {
		if (getCurrentTransaction() == null) {
			throw new GraphException("Current transaction is null.");
		}
		return getCurrentTransaction().isInConflict();
	}

	/**
	 * Should be called from generated <code>Graph</code> implementation classes
	 * whenever a versioned attribute is changed.
	 * 
	 * @param versionedAttribute
	 *            the changed attribute
	 */
	protected void attributeChanged(VersionedDataObject<?> versionedAttribute) {
		if (!isLoading()) {
			if (logger != null) {
				logger.finest(versionedAttribute.toString());
			}
			TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
			assert ((transaction != null)
					&& (transaction.getState() != TransactionState.NOTRUNNING)
					&& transaction.isValid() && !transaction.isReadOnly());
			if (transaction.changedAttributes == null) {
				transaction.changedAttributes = new HashMap<AttributedElement<?, ?>, Set<VersionedDataObject<?>>>(
						1, TransactionManagerImpl.LOAD_FACTOR);
			}
			Set<VersionedDataObject<?>> attributes = transaction.changedAttributes
					.get(this);
			if (attributes == null) {
				attributes = new HashSet<VersionedDataObject<?>>(1, 0.2f);
				transaction.changedAttributes.put(this, attributes);
			}
			attributes.add(versionedAttribute);
		}
	}

	@Override
	// TODO maybe support this?!
	public void defragment() {
		throw new UnsupportedOperationException(
				"Defragmentation of graph is not supported within the transaction concept.");
	}

	@Override
	public int allocateVertexIndex(int currentId) {
		vertexSync.writeLock().lock();
		int vId = 0;
		try {
			Transaction transaction = getCurrentTransaction();
			assert (transaction.getState() != null);
			if (transaction.getState() == TransactionState.RUNNING) {
				synchronized (freeVertexList) {
					vId = freeVertexList.allocateIndex();
					if (vId == 0) {
						int newSize = getExpandedVertexCount();
						expandVertexArray(newSize);
						vId = freeVertexList.allocateIndex();
					}
				}
			} else {
				vId = currentId;
			}
		} finally {
			vertexSync.writeLock().unlock();
		}
		return vId;
	}

	@Override
	public int allocateEdgeIndex(int currentId) {
		edgeSync.writeLock().lock();
		int eId = 0;
		try {
			Transaction transaction = getCurrentTransaction();
			assert (transaction.getState() != null);
			if (transaction.getState() == TransactionState.RUNNING) {
				eId = freeEdgeList.allocateIndex();
				if (eId == 0) {
					int newSize = getExpandedEdgeCount();
					expandEdgeArray(newSize);
					eId = freeEdgeList.allocateIndex();
				}
			} else {
				eId = currentId;
			}
		} finally {
			edgeSync.writeLock().unlock();
		}
		return eId;
	}

	@Override
	public void freeEdgeIndex(int index) {
		Transaction transaction = getCurrentTransaction();
		assert ((transaction != null) && !transaction.isReadOnly() && (freeEdgeList != null));
		if ((transaction.getState() == TransactionState.COMMITTING)
				|| (transaction.getState() == TransactionState.ABORTING)) {
			synchronized (freeEdgeList) {
				if (isEdgeIndexReferenced(index)) {
					if (edgeIndexesToBeFreed == null) {
						edgeIndexesToBeFreed = new ArrayList<Integer>();
					}
					if (!edgeIndexesToBeFreed.contains(index)) {
						edgeIndexesToBeFreed.add(0, index);
					}
				} else {
					freeEdgeList.freeIndex(index);
					if (edgeIndexesToBeFreed != null) {
						edgeIndexesToBeFreed.remove((Object) index);
					}
				}
			}
		}
	}

	/**
	 * Checks whether an <code>Edge</code> with the given <code>index</code>
	 * exists for at least one other parallel running read-write
	 * <code>Transaction</code>. If so the given <code>index</code> may not be
	 * freed yet, but has to be marked as "to-be-freed" in the future by putting
	 * it into <code>edgeIndexesToBeFreed</code>.
	 * 
	 * @param index
	 * @return
	 */
	protected boolean isEdgeIndexReferenced(int index) {
		edgeSync.readLock().lock();
		boolean result = false;
		List<Transaction> transactionsList = transactionManager
				.getTransactions();
		Transaction currentTransaction = getCurrentTransaction();
		synchronized (transactionsList) {
			for (Transaction transaction : transactionsList) {
				if ((transaction != currentTransaction)
						&& (transaction.isValid()) && !transaction.isReadOnly()) {
					EdgeImpl[] tempEdge = edge
							.getPersistentValueAtBot(transaction);
					if (tempEdge != null) {
						if ((tempEdge.length > index)
								&& (tempEdge[index] != null)) {
							result = true;
							break;
						}
					}
				}
			}
		}
		edgeSync.readLock().unlock();
		return result;
	}

	@Override
	public void freeVertexIndex(int index) {
		Transaction transaction = getCurrentTransaction();
		assert ((transaction != null) && !transaction.isReadOnly() && (freeVertexList != null));
		if ((transaction.getState() == TransactionState.COMMITTING)
				|| (transaction.getState() == TransactionState.ABORTING)) {
			synchronized (freeVertexList) {
				if (isVertexIndexReferenced(index)) {
					if (vertexIndexesToBeFreed == null) {
						vertexIndexesToBeFreed = new ArrayList<Integer>();
					}
					if (!vertexIndexesToBeFreed.contains(index)) {
						vertexIndexesToBeFreed.add(0, index);
					}
				} else {
					freeVertexList.freeIndex(index);
					if (vertexIndexesToBeFreed != null) {
						vertexIndexesToBeFreed.remove((Object) index);
					}
				}
			}
		}
	}

	/**
	 * Checks whether an <code>Vertex</code> with the given <code>index</code>
	 * exists for at least one other parallel running read-write
	 * <code>Transaction</code>. If so the given <code>index</code> may not be
	 * freed yet, but has to be marked as "to-be-freed" in the future by putting
	 * it into <code>vertexIndexesToBeFreed</code>.
	 * 
	 * @param index
	 * @return
	 */
	protected boolean isVertexIndexReferenced(int index) {
		vertexSync.readLock().lock();
		boolean result = false;
		List<Transaction> transactionsList = transactionManager
				.getTransactions();
		Transaction currentTransaction = getCurrentTransaction();
		synchronized (transactionsList) {
			for (Transaction transaction : transactionsList) {
				if ((transaction != currentTransaction)
						&& (transaction.isValid()) && !transaction.isReadOnly()) {
					VertexImpl[] tempVertex = vertex
							.getPersistentValueAtBot(transaction);
					if (tempVertex != null) {
						if ((tempVertex.length > index)
								&& (tempVertex[index] != null)) {
							result = true;
							break;
						}
					}
				}
			}
		}
		vertexSync.readLock().unlock();
		return result;
	}

	@Override
	public boolean canAddGraphElement(int graphElementId) {
		Transaction transaction = getCurrentTransaction();
		assert (transaction != null);
		return ((transaction.getState() == TransactionState.WRITING) || (transaction
				.getState() == TransactionState.RUNNING));
	}

	@Override
	public synchronized void expandVertexArray(int newSize) {
		if (vertexSync == null) {
			vertexSync = new ReentrantReadWriteLock(true);
		}
		vertexSync.writeLock().lock();
		try {
			if (newSize <= vMax) {
				throw new GraphException("newSize must > vSize: vSize=" + vMax
						+ ", newSize=" + newSize);
			}
			// mark if freeVertexList has been initialized in this method
			// invocation...
			boolean firstInit = false;
			// should be done with initialization of graph...
			if (freeVertexList == null) {
				firstInit = true;
				freeVertexList = new FreeIndexList(newSize);
			}
			synchronized (freeVertexList) {
				// initialization of vertex should be done with initialization
				// of graph...
				if (vertex == null) {
					vertex = new VersionedArrayImpl<VertexImpl[]>(this,
							new VertexImpl[newSize + 1], "$vertex[]");
				} else {
					synchronized (vertex) {
						// expand all vertex-values for all active
						// transactions
						vertex.expandVertexArrays(newSize);
					}
				}
				// only expand, if freeVertexList hasn't been initialized in
				// this method invocation...
				if (!firstInit) {
					freeVertexList.expandBy(newSize - vMax);
				}
				vMax = newSize;
				notifyMaxVertexCountIncreased(newSize);
			}
		} finally {
			vertexSync.writeLock().unlock();
		}
	}

	@Override
	public synchronized void expandEdgeArray(int newSize) {
		if (edgeSync == null) {
			edgeSync = new ReentrantReadWriteLock(true);
		}
		edgeSync.writeLock().lock();
		try {
			if (newSize <= eMax) {
				throw new GraphException("newSize must be > eSize: eSize="
						+ eMax + ", newSize=" + newSize);
			}
			// mark if freeEdgeList has been initialized in this method
			// invocation...
			boolean firstInit = false;
			if (freeEdgeList == null) {
				firstInit = true;
				freeEdgeList = new FreeIndexList(newSize);
			}
			synchronized (freeEdgeList) {
				// initialization edge and revEdge
				if (edge == null) {
					edge = new VersionedArrayImpl<EdgeImpl[]>(this,
							new EdgeImpl[newSize + 1], "$edge[]");
					assert (revEdge == null);
					revEdge = new VersionedArrayImpl<ReversedEdgeImpl[]>(this,
							new ReversedEdgeImpl[newSize + 1], "$revEdge[]");
				} else {
					// lock Array edge
					synchronized (edge) {
						edge.expandEdgeArrays(newSize);
						// lock Array revEdge
						synchronized (revEdge) {
							// expand all edge- and revEdge-values for all
							// active
							// transactions
							revEdge.expandRevEdgeArrays(newSize);
						}
					}
				}
				if (!firstInit) {
					freeEdgeList.expandBy(newSize - eMax);
				}
				eMax = newSize;
				notifyMaxEdgeCountIncreased(newSize);
			}
		} finally {
			edgeSync.writeLock().unlock();
		}
	}

	@Override
	public void addEdge(Edge newEdge, Vertex alpha, Vertex omega) {
		if (isLoading()) {
			super.addEdge(newEdge, alpha, omega);
		} else {
			TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			if (transaction.isReadOnly()) {
				throw new GraphException(
						"Read-only transactions are not allowed to add edges.");
			}
			// It should not be possible to add newEdge, if alpha isn't
			// valid in the current transaction.
			if (!alpha.isValid()) {
				throw new GraphException("Alpha-vertex " + alpha
						+ " is not valid within the current transaction "
						+ transaction + ".");
			}
			// It should not be possible to add newEdge, if omega isn't
			// valid in the current transaction.
			if (!omega.isValid()) {
				throw new GraphException("Omega-vertex " + omega
						+ " is not valid within the current transaction "
						+ transaction + ".");
			}
			synchronized (transaction) {
				// create temporary versions of edge and revEdge if not already
				// existing
				if (transaction.getState() == TransactionState.RUNNING) {
					edgeSync.writeLock().lock();
					edge.prepareValueChangeAfterReference(transaction);
					revEdge.prepareValueChangeAfterReference(transaction);
					edgeSync.writeLock().unlock();
				}
				try {
					super.addEdge(newEdge, alpha, omega);
				} catch (GraphException e) {
					throw e;
				}
				assert ((transaction != null) && !transaction.isReadOnly()
						&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
				if (transaction.getState() == TransactionState.RUNNING) {
					if (transaction.addedEdges == null) {
						transaction.addedEdges = new ArrayList<EdgeImpl>(1);
					}
					transaction.addedEdges
							.add((de.uni_koblenz.jgralab.impl.trans.EdgeImpl) (newEdge));
					if (transaction.deletedEdges != null) {
						transaction.deletedEdges.remove(newEdge);
					}
				}
			}
		}
	}

	@Override
	public void addVertex(Vertex newVertex) {
		if (isLoading()) {
			super.addVertex(newVertex);
		} else {
			TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			if (transaction.isReadOnly()) {
				throw new GraphException(
						"Read-only transactions are not allowed to add vertices.");
			}
			synchronized (transaction) {
				if (transaction.getState() == TransactionState.RUNNING) {
					vertexSync.writeLock().lock();
					vertex.prepareValueChangeAfterReference(transaction);
					vertexSync.writeLock().unlock();
				}
				try {
					super.addVertex(newVertex);
				} catch (GraphException e) {
					throw e;
				}
				assert ((transaction != null) && !transaction.isReadOnly()
						&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
				if (transaction.getState() == TransactionState.RUNNING) {
					if (transaction.addedVertices == null) {
						transaction.addedVertices = new ArrayList<VertexImpl>(1);
					}
					transaction.addedVertices
							.add((de.uni_koblenz.jgralab.impl.trans.VertexImpl) (newVertex));
					if (transaction.deletedVertices != null) {
						transaction.deletedVertices.remove(newVertex);
					}
				}
			}
		}
	}

	@Override
	public void deleteEdge(Edge edgeToBeDeleted) {
		TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
		if (transaction == null) {
			throw new GraphException("Current transaction is null.");
		}
		if (transaction.isReadOnly()) {
			throw new GraphException(
					"Read-only transactions are not allowed to delete edges.");
		}
		// It should not be possible to delete edgeToBeDeleted, if it isn't
		// valid in the current transaction.
		if (!edgeToBeDeleted.isValid()) {
			throw new GraphException("Edge " + edgeToBeDeleted
					+ " isn't valid within current transaction.");
		}
		synchronized (transaction) {
			if (transaction.getState() == TransactionState.RUNNING) {
				edgeSync.writeLock().lock();
				edge.prepareValueChangeAfterReference(transaction);
				revEdge.prepareValueChangeAfterReference(transaction);
				edgeSync.writeLock().unlock();
			}
			try {
				super.deleteEdge(edgeToBeDeleted);
			} catch (GraphException e) {
				throw e;
			}

		}
	}

	@Override
	public void edgeAfterDeleted(Edge edgeToBeDeleted, Vertex oldAlpha,
			Vertex oldOmega) {
		InternalEdge deletedEdge = (InternalEdge) edgeToBeDeleted;
		TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
		assert ((transaction != null) && !transaction.isReadOnly()
				&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
		if (transaction.getState() == TransactionState.RUNNING) {
			if ((transaction.addedEdges != null)
					&& transaction.addedEdges.contains(deletedEdge)) {
				transaction.addedEdges.remove(deletedEdge);
			} else {
				if (transaction.deletedEdges == null) {
					transaction.deletedEdges = new ArrayList<EdgeImpl>(1);
				}
				transaction.deletedEdges
						.add((de.uni_koblenz.jgralab.impl.trans.EdgeImpl) (deletedEdge
								.getNormalEdge()));
			}
			// delete references to edgeToBeDeleted in other change sets
			if (transaction.changedAttributes != null) {
				transaction.changedAttributes.remove(deletedEdge);
			}
			if (transaction.changedEdges != null) {
				transaction.changedEdges.remove(deletedEdge);
			}
			if (transaction.changedEseqEdges != null) {
				transaction.changedEseqEdges.remove(deletedEdge);
				Edge prevEdge = deletedEdge.getPrevIncidenceInISeq();
				if (transaction.changedEseqEdges.containsKey(prevEdge)) {
					if (transaction.changedEseqEdges.get(prevEdge).containsKey(
							ListPosition.NEXT)) {
						transaction.changedEseqEdges.remove(prevEdge);
					}
				}
				Edge nextEdge = deletedEdge.getNextIncidenceInISeq();
				// check if current (temporary) nextEdge has been changed
				// explicitly
				if (transaction.changedEseqEdges.containsKey(nextEdge)) {
					if (transaction.changedEseqEdges.get(nextEdge).containsKey(
							ListPosition.PREV)) {
						transaction.changedEseqEdges.remove(nextEdge);
					}
				}
			}
			if (transaction.changedIncidences != null) {
				// remove edgeToBeDeleted from incidence lists
				Map<IncidenceImpl, Map<ListPosition, Boolean>> changedAlphaIncidences = transaction.changedIncidences
						.get(oldAlpha);
				if (changedAlphaIncidences != null) {
					changedAlphaIncidences.remove(deletedEdge);
				}
				Map<IncidenceImpl, Map<ListPosition, Boolean>> changedOmegaIncidences = transaction.changedIncidences
						.get(oldOmega);
				if (changedOmegaIncidences != null) {
					changedOmegaIncidences.remove(deletedEdge);
				}
			}
		}
	}

	@Override
	public void deleteVertex(Vertex vertexToBeDeleted) {
		TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
		if (transaction == null) {
			throw new GraphException("Current transaction is null.");
		}
		if (transaction.isReadOnly()) {
			throw new GraphException(
					"Read-only transactions are not allowed to delete vertices.");
		}
		// It should not be possible to delete vertexToBeDeleted, if it isn't
		// valid in
		// the current transaction.
		if (!vertexToBeDeleted.isValid()) {
			throw new GraphException("Vertex " + vertexToBeDeleted
					+ " isn't valid within current transaction.");
		}
		synchronized (transaction) {
			if (transaction.getState() == TransactionState.RUNNING) {
				vertexSync.writeLock().lock();
				vertex.prepareValueChangeAfterReference(transaction);
				vertexSync.writeLock().unlock();
			}
			try {
				super.deleteVertex(vertexToBeDeleted);
			} catch (GraphException e) {
				throw e;
			}
		}
	}

	@Override
	public void vertexAfterDeleted(Vertex vertexToBeDeleted) {
		InternalVertex deletedVertex = (InternalVertex) vertexToBeDeleted;
		TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
		assert ((transaction != null) && !transaction.isReadOnly()
				&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
		if (transaction.getState() == TransactionState.RUNNING) {
			if ((transaction.addedVertices != null)
					&& transaction.addedVertices.contains(deletedVertex)) {
				transaction.addedVertices.remove(deletedVertex);
			} else {
				if (transaction.deletedVertices == null) {
					// transaction.deletedVertices = new HashSet<VertexImpl>(1,
					// 0.2f);
					transaction.deletedVertices = new ArrayList<VertexImpl>(1);
				}
				transaction.deletedVertices
						.add((de.uni_koblenz.jgralab.impl.trans.VertexImpl) (deletedVertex));
			}
			if (transaction.changedAttributes != null) {
				// delete references to vertexToBeDeleted in other change sets
				transaction.changedAttributes.remove(deletedVertex);
			}
			if (transaction.changedVseqVertices != null) {
				transaction.changedVseqVertices.remove(deletedVertex);
				Vertex prevVertex = deletedVertex.getPrevVertexInVSeq();
				if (transaction.changedVseqVertices.containsKey(prevVertex)) {
					if (transaction.changedVseqVertices.get(prevVertex)
							.containsKey(ListPosition.NEXT)) {
						transaction.changedVseqVertices.remove(prevVertex);
					}
				}
				Vertex nextVertex = deletedVertex.getNextVertexInVSeq();
				// check if current (temporary) nextVertex has been changed
				// explicitly
				if (transaction.changedVseqVertices.containsKey(nextVertex)) {
					if (transaction.changedVseqVertices.get(nextVertex)
							.containsKey(ListPosition.PREV)) {
						transaction.changedVseqVertices.remove(nextVertex);
					}
				}
			}
			if (transaction.changedIncidences != null) {
				transaction.changedIncidences.remove(deletedVertex);
			}
		}
		if (transaction.getState() == TransactionState.WRITING) {
			if (transaction.deletedVerticesWhileWriting == null) {
				transaction.deletedVerticesWhileWriting = new ArrayList<VertexImpl>(
						1);
			}
			transaction.deletedVerticesWhileWriting
					.add((de.uni_koblenz.jgralab.impl.trans.VertexImpl) deletedVertex);
		}

	}

	@Override
	public void putEdgeBeforeInGraph(InternalEdge targetEdge,
			InternalEdge movedEdge) {
		TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
		if (transaction == null) {
			throw new GraphException("Current transaction is null.");
		}
		// It should not be possible to execute this method, if targetEdge
		// isn't valid
		// in the current transaction.
		if (!targetEdge.isValid()) {
			throw new GraphException("Edge " + targetEdge
					+ " is not valid within the current transaction "
					+ transaction + ".");
		}
		// It should not be possible to execute this method, if movedEdge
		// isn't valid
		// in the current transaction.
		if (!movedEdge.isValid()) {
			throw new GraphException("Edge " + movedEdge
					+ " is not valid within the current transaction "
					+ transaction + ".");
		}
		synchronized (transaction) {
			super.putEdgeBeforeInGraph(targetEdge, movedEdge);
			assert ((transaction != null) && !transaction.isReadOnly()
					&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
			if (transaction.getState() == TransactionState.RUNNING) {
				if (transaction.changedEseqEdges == null) {
					transaction.changedEseqEdges = new HashMap<EdgeImpl, Map<ListPosition, Boolean>>(
							1, TransactionManagerImpl.LOAD_FACTOR);
				}
				Map<ListPosition, Boolean> positionsMap = transaction.changedEseqEdges
						.get(movedEdge);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1,
							TransactionManagerImpl.LOAD_FACTOR);
				}
				positionsMap.put(ListPosition.NEXT, true);
				if (transaction.changedEseqEdges.get(movedEdge) == null) {
					transaction.changedEseqEdges
							.put((de.uni_koblenz.jgralab.impl.trans.EdgeImpl) movedEdge,
									positionsMap);
				}
				positionsMap = transaction.changedEseqEdges.get(targetEdge);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1,
							TransactionManagerImpl.LOAD_FACTOR);
				}
				positionsMap.put(ListPosition.PREV, false);
				if (transaction.changedEseqEdges.get(targetEdge) == null) {
					transaction.changedEseqEdges
							.put((de.uni_koblenz.jgralab.impl.trans.EdgeImpl) targetEdge,
									positionsMap);
				}
			}
		}
	}

	@Override
	public void putEdgeAfterInGraph(InternalEdge targetEdge,
			InternalEdge movedEdge) {
		TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
		if (transaction == null) {
			throw new GraphException("Current transaction is null.");
		}
		// It should not be possible to execute this method, if targetEdge
		// isn't valid
		// in the current transaction.
		if (!targetEdge.isValid()) {
			throw new GraphException("Edge " + targetEdge
					+ " is not valid within the current transaction "
					+ transaction + ".");
		}
		// It should not be possible to execute this method, if movedEdge
		// isn't valid
		// in the current transaction.
		if (!movedEdge.isValid()) {
			throw new GraphException("Edge " + movedEdge
					+ " is not valid within the current transaction "
					+ transaction + ".");
		}
		synchronized (transaction) {
			super.putEdgeAfterInGraph(targetEdge, movedEdge);
			assert ((transaction != null) && !transaction.isReadOnly()
					&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
			if (transaction.getState() == TransactionState.RUNNING) {
				if (transaction.changedEseqEdges == null) {
					transaction.changedEseqEdges = new HashMap<EdgeImpl, Map<ListPosition, Boolean>>(
							1, TransactionManagerImpl.LOAD_FACTOR);
				}
				Map<ListPosition, Boolean> positionsMap = transaction.changedEseqEdges
						.get(movedEdge);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1,
							TransactionManagerImpl.LOAD_FACTOR);
				}
				positionsMap.put(ListPosition.PREV, true);
				if (transaction.changedEseqEdges.get(movedEdge) == null) {
					transaction.changedEseqEdges
							.put((de.uni_koblenz.jgralab.impl.trans.EdgeImpl) movedEdge,
									positionsMap);
				}
				positionsMap = transaction.changedEseqEdges.get(targetEdge);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1,
							TransactionManagerImpl.LOAD_FACTOR);
				}
				positionsMap.put(ListPosition.NEXT, false);
				if (transaction.changedEseqEdges.get(targetEdge) == null) {
					transaction.changedEseqEdges
							.put((de.uni_koblenz.jgralab.impl.trans.EdgeImpl) targetEdge,
									positionsMap);
				}
			}
		}
	}

	@Override
	public void putVertexAfter(InternalVertex targetVertex,
			InternalVertex movedVertex) {
		TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
		if (transaction == null) {
			throw new GraphException("Current transaction is null.");
		}
		// It should not be possible to execute this method, if targetVertex
		// isn't valid
		// in the current transaction.
		if (!targetVertex.isValid()) {
			throw new GraphException("Edge " + targetVertex
					+ " is not valid within the current transaction "
					+ transaction + ".");
		}
		// It should not be possible to execute this method, if movedVertex
		// isn't valid
		// in the current transaction.
		if (!movedVertex.isValid()) {
			throw new GraphException("Edge " + movedVertex
					+ " is not valid within the current transaction "
					+ transaction + ".");
		}
		synchronized (transaction) {
			super.putVertexAfter(targetVertex, movedVertex);
			assert ((transaction != null) && !transaction.isReadOnly()
					&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
			if (transaction.getState() == TransactionState.RUNNING) {
				if (transaction.changedVseqVertices == null) {
					transaction.changedVseqVertices = new HashMap<VertexImpl, Map<ListPosition, Boolean>>(
							1, TransactionManagerImpl.LOAD_FACTOR);
				}
				Map<ListPosition, Boolean> positionsMap = transaction.changedVseqVertices
						.get(movedVertex);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1,
							TransactionManagerImpl.LOAD_FACTOR);
				}
				positionsMap.put(ListPosition.PREV, true);
				if (transaction.changedVseqVertices.get(movedVertex) == null) {
					transaction.changedVseqVertices
							.put((de.uni_koblenz.jgralab.impl.trans.VertexImpl) movedVertex,
									positionsMap);
				}
				positionsMap = transaction.changedVseqVertices
						.get(targetVertex);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1,
							TransactionManagerImpl.LOAD_FACTOR);
				}
				positionsMap.put(ListPosition.NEXT, false);
				if (transaction.changedVseqVertices.get(targetVertex) == null) {
					transaction.changedVseqVertices
							.put((de.uni_koblenz.jgralab.impl.trans.VertexImpl) targetVertex,
									positionsMap);
				}
			}
		}
	}

	@Override
	public void putVertexBefore(InternalVertex targetVertex,
			InternalVertex movedVertex) {
		TransactionImpl transaction = (TransactionImpl) getCurrentTransaction();
		if (transaction == null) {
			throw new GraphException("Current transaction is null.");
		}
		// It should not be possible to execute this method, if targetVertex
		// isn't valid
		// in the current transaction.
		if (!targetVertex.isValid()) {
			throw new GraphException("Edge " + targetVertex
					+ " is not valid within the current transaction "
					+ transaction + ".");
		}
		// It should not be possible to execute this method, if movedVertex
		// isn't valid
		// in the current transaction.
		if (!movedVertex.isValid()) {
			throw new GraphException("Edge " + movedVertex
					+ " is not valid within the current transaction "
					+ transaction + ".");
		}
		synchronized (transaction) {
			super.putVertexBefore(targetVertex, movedVertex);
			assert ((transaction != null) && !transaction.isReadOnly()
					&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
			if (transaction.getState() == TransactionState.RUNNING) {
				if (transaction.changedVseqVertices == null) {
					transaction.changedVseqVertices = new HashMap<VertexImpl, Map<ListPosition, Boolean>>(
							1, TransactionManagerImpl.LOAD_FACTOR);
				}
				Map<ListPosition, Boolean> positionsMap = transaction.changedVseqVertices
						.get(movedVertex);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1,
							TransactionManagerImpl.LOAD_FACTOR);
				}
				positionsMap.put(ListPosition.NEXT, true);
				if (transaction.changedVseqVertices.get(movedVertex) == null) {
					transaction.changedVseqVertices
							.put((de.uni_koblenz.jgralab.impl.trans.VertexImpl) movedVertex,
									positionsMap);
				}
				positionsMap = transaction.changedVseqVertices
						.get(targetVertex);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1,
							TransactionManagerImpl.LOAD_FACTOR);
				}
				positionsMap.put(ListPosition.PREV, false);
				if (transaction.changedVseqVertices.get(targetVertex) == null) {
					transaction.changedVseqVertices
							.put((de.uni_koblenz.jgralab.impl.trans.VertexImpl) targetVertex,
									positionsMap);
				}
			}
		}
	}

	@Override
	public Iterable<Vertex> vertices(VertexFilter<Vertex> filter) {
		return new AttributedElementIterable<Vertex>(super.vertices(filter),
				this);
	}

	@Override
	public Iterable<Vertex> vertices(VertexClass vertexClass,
			VertexFilter<Vertex> filter) {
		return new AttributedElementIterable<Vertex>(super.vertices(
				vertexClass, filter), this);
	}

	@Override
	public Iterable<Edge> edges() {
		return new AttributedElementIterable<Edge>(super.edges(), this);
	}

	@Override
	public Iterable<Edge> edges(EdgeClass edgeClass) {
		return new AttributedElementIterable<Edge>(super.edges(edgeClass), this);
	}

	@Override
	public void appendEdgeToESeq(InternalEdge e) {
		edgeSync.readLock().lock();
		try {
			super.appendEdgeToESeq(e);
		} finally {
			edgeSync.readLock().unlock();
		}
	}

	@Override
	public void appendVertexToVSeq(InternalVertex v) {
		vertexSync.readLock().lock();
		try {
			super.appendVertexToVSeq(v);
		} finally {
			vertexSync.readLock().unlock();
		}
	}

	/**
	 * Trying to free cached <code>Vertex</code>- and <code>Edge</code> indexes.
	 */
	protected void freeStoredIndexes() {
		synchronized (freeVertexList) {
			if (vertexIndexesToBeFreed != null) {
				for (Integer index : new ArrayList<Integer>(
						vertexIndexesToBeFreed)) {
					freeVertexIndex(index);
				}
			}
		}
		synchronized (freeEdgeList) {
			if (edgeIndexesToBeFreed != null) {
				for (Integer index : new ArrayList<Integer>(
						edgeIndexesToBeFreed)) {
					freeEdgeIndex(index);
				}
			}
		}
	}

	@Override
	public final boolean hasTransactionSupport() {
		return true;
	}

	private boolean isWriting() {
		return getCurrentTransaction().getState() == TransactionState.WRITING;
	}

	@Override
	public void internalVertexDeleted(InternalVertex v) {
		if (isWriting()) {
			super.internalVertexDeleted(v);
		}
	}

	@Override
	public void internalVertexAdded(InternalVertex v) {
		if (isWriting()) {
			super.internalVertexAdded(v);
		}
	}

	@Override
	public void internalEdgeDeleted(InternalEdge e) {
		if (isWriting()) {
			super.internalEdgeDeleted(e);
		}
	}

	@Override
	public void internalEdgeAdded(InternalEdge e) {
		if (isWriting()) {
			super.internalEdgeAdded(e);
		}
	}

	@Override
	public void addGraphStructureChangedListener(
			GraphStructureChangedListener newListener) {
		synchronized (graphStructureChangedListeners) {
			super.addGraphStructureChangedListener(newListener);
		}
	}

	@Override
	public void removeGraphStructureChangedListener(
			GraphStructureChangedListener listener) {
		synchronized (graphStructureChangedListeners) {
			super.removeGraphStructureChangedListener(listener);
		}
	}

	@Override
	public void removeAllGraphStructureChangedListeners() {
		synchronized (graphStructureChangedListeners) {
			super.removeAllGraphStructureChangedListeners();
		}
	}

	@Override
	public void initializeAttributesWithDefaultValues() {
		try {
			Transaction defaultValuesTransaction = newTransaction();
			super.initializeAttributesWithDefaultValues();
			defaultValuesTransaction.commit();
		} catch (Exception e) {
			throw new GraphException(
					"The initialization for the default values of the graph failed.\n"
							+ " Caused by: " + e.getClass() + " - "
							+ e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void internalSetDefaultValue(Attribute attr) throws GraphIOException {
		attr.setDefaultTransactionValue(this);
	}

	@Override
	public TraversalContext getTraversalContext() {
		return getCurrentTransaction().getTraversalContext();
	}

	@Override
	public synchronized TraversalContext setTraversalContext(TraversalContext tc) {
		return getCurrentTransaction().setTraversalContext(tc);
	}

	@Override
	public TemporaryVertex createTemporaryVertex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TemporaryVertex createTemporaryVertex(VertexClass preliminaryType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TemporaryEdge createTemporaryEdge(Vertex alpha, Vertex omega) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TemporaryEdge createTemporaryEdge(EdgeClass preliminaryType,
			Vertex alpha, Vertex omega) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasTemporaryElements() {
		return true;
	}

}
