/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
package de.uni_koblenz.jgralab.impl.trans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralab.trans.InvalidSavepointException;
import de.uni_koblenz.jgralab.trans.ListPosition;
import de.uni_koblenz.jgralab.trans.Savepoint;
import de.uni_koblenz.jgralab.trans.Transaction;
import de.uni_koblenz.jgralab.trans.TransactionState;
import de.uni_koblenz.jgralab.trans.VersionedDataObject;
import de.uni_koblenz.jgralab.trans.VertexPosition;

/**
 * The implementation of a <code>Transaction</code>.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public class TransactionImpl implements Transaction {
	protected long temporaryVersionCounter;
	protected long persistentVersionAtBot;
	protected long persistentVersionAtCommit;

	private TransactionManagerImpl transactionManager;
	private GraphImpl graph;
	private Thread thread;
	private int id;
	private TransactionState state;
	private boolean readOnly;

	private short savepointIdCounter;

	// change sets and maps needed for validation- and writing-phase
	// important to have lists here to be able to reproduce the order of adding
	// and deleting in writing phase
	protected List<VertexImpl> addedVertices;
	protected List<EdgeImpl> addedEdges;
	protected List<VertexImpl> deletedVertices;
	protected List<EdgeImpl> deletedEdges;
	protected Map<VertexImpl, Map<ListPosition, Boolean>> changedVseqVertices;
	protected Map<EdgeImpl, Map<ListPosition, Boolean>> changedEseqEdges;
	protected Map<VertexImpl, Map<IncidenceImpl, Map<ListPosition, Boolean>>> changedIncidences;
	protected Map<EdgeImpl, VertexPosition> changedEdges;
	protected Map<AttributedElement, Set<VersionedDataObject<?>>> changedAttributes;

	protected List<VertexImpl> deletedVerticesWhileWriting;
	protected List<de.uni_koblenz.jgralab.impl.VertexBaseImpl> deleteVertexList;

	protected SavepointImpl latestDefinedSavepoint;
	protected SavepointImpl latestRestoredSavepoint;
	private List<Savepoint> savepointList;

	private ValidationComponent validationComponent;
	private WritingComponent writingComponent;

	// needed to undo creation of new persistent versions if during writing
	// phase something goes wrong unexpectedly.
	protected Set<VersionedDataObjectImpl<?>> changedDuringCommit;

	/**
	 * A map which assigns a single temporary value to a versioned data object
	 * (if needed). This map is used for storing temporary values of versioned
	 * data objects id this transaction has no save-points yet and therefore
	 * don't need to have a version number assigned to the temporary version
	 * values. Note that as soon as a save-point has been created for a
	 * save-point the temporary values of the versioned data objects need to be
	 * stored in <code>temporaryVersionMap</code>.
	 * 
	 * Introduced for "better" and less memory usage.
	 */
	protected Map<VersionedDataObject<?>, Object> temporaryValueMap;

	/**
	 * This map assigns a versioned data object a sorted map (by version number)
	 * of temporary values belonging to this transaction. This map is only
	 * needed if this transactions has defined save-points and therefore needs a
	 * mapping between temporary version numbers and temporary values of the
	 * versioned data objects.
	 * 
	 * Defining save-point means important increase of memory usage.
	 */
	protected Map<VersionedDataObject<?>, SortedMap<Long, Object>> temporaryVersionMap;

	/**
	 * 
	 * @return increases the value of the temporary version counter and returns
	 *         it
	 */
	protected long incrTemporaryVersionCounter() {
		++temporaryVersionCounter;
		return temporaryVersionCounter;
	}

	/**
	 * 
	 * 
	 * @param transactionManager
	 * @param graph
	 * @param ID
	 * @param readOnly
	 */
	public TransactionImpl(TransactionManagerImpl transactionManager,
			GraphImpl graph, int ID, boolean readOnly) {
		this.transactionManager = transactionManager;
		this.graph = graph;
		id = ID;
		this.readOnly = readOnly;
		temporaryVersionCounter = 0;
		latestDefinedSavepoint = null;
		validationComponent = null;
		writingComponent = null;
		thread = Thread.currentThread();
		state = TransactionState.NOTRUNNING;
		// implicit BOT - do it here not in TransactionManagerImpl to avoid
		// deadlocks!!!
		bot();
		savepointIdCounter = 0;
	}

	@Override
	public void abort() {
		if (thread != Thread.currentThread()) {
			throw new GraphException(
					"Transaction is not active in current thread.");
		}
		if (!readOnly) {
			// remove all temporary values created...
			removeAllTemporaryValues();
			state = TransactionState.ABORTING;
			// free Ids of added vertices and edges...
			if (addedVertices != null && addedVertices.size() > 0) {
				synchronized (graph.getFreeVertexList()) {
					for (VertexImpl vertex : addedVertices) {
						graph.freeVertexIndex(vertex.getId());
					}
					addedVertices = null;
				}
			}
			if (addedEdges != null && addedEdges.size() > 0) {
				synchronized (graph.getFreeEdgeList()) {
					for (EdgeImpl edge : addedEdges) {
						graph.freeEdgeIndex(edge.getId());
					}
					addedEdges = null;
				}
			}
			graph.freeStoredIndexes();
		}
		state = TransactionState.ABORTED;
		transactionManager.removeTransactionForThread(Thread.currentThread());

		// free memory
		deletedVertices = null;
		deletedEdges = null;
		deleteVertexList = null;
		changedVseqVertices = null;
		changedEseqEdges = null;
		changedIncidences = null;
		changedEdges = null;
		changedAttributes = null;
		savepointList = null;
		changedDuringCommit = null;
		temporaryValueMap = null;
	}

	@Override
	public void bot() {
		if (thread != Thread.currentThread()) {
			throw new GraphException(
					"Transaction is not active in current thread.");
		}
		if (state == TransactionState.NOTRUNNING) {
			transactionManager.botWritingSync.readLock().lock();
			persistentVersionAtBot = graph.getPersistentVersionCounter();
			transactionManager.botWritingSync.readLock().unlock();
			state = TransactionState.RUNNING;
		}
	}

	@Override
	public void commit() throws CommitFailedException {
		if (thread != Thread.currentThread()) {
			throw new GraphException(
					"Transaction is not active in current thread.");
		}
		assert state == TransactionState.RUNNING : "Expected TransactionState RUNNING, but it's "
				+ state + ".";
		state = TransactionState.COMMITTING;
		// case Read-Write-Transaction
		if (!readOnly) {
			// only one transaction at a time should be able to execute COMMIT
			transactionManager.commitSync.writeLock().lock();
			if (internalIsInConflict()) {
				state = TransactionState.RUNNING;
				transactionManager.commitSync.writeLock().unlock();
				throw new CommitFailedException(this, validationComponent
						.getConflictReason());
			}
			// make sure no other transaction is executing isInConflict()-method
			transactionManager.commitValidatingSync.writeLock().lock();
			// make sure no other transaction is doing its BOT
			transactionManager.botWritingSync.writeLock().lock();

			persistentVersionAtCommit = graph.getPersistentVersionCounter();
			if (writingComponent == null) {
				writingComponent = new WritingComponent(this);
			}
			try {
				state = TransactionState.WRITING;
				writingComponent.write();
			} catch (Exception e) {
				// this should not happen!!!
				e.printStackTrace();
				state = TransactionState.RUNNING;
				transactionManager.botWritingSync.writeLock().unlock();
				transactionManager.commitValidatingSync.writeLock().unlock();
				transactionManager.commitSync.writeLock().unlock();
				throw new CommitFailedException(this, e.getMessage());
			}
			state = TransactionState.COMMITTING;
			// free Ids of deleted vertices and edges
			if (deletedVertices != null && deletedVertices.size() > 0) {
				synchronized (graph.getFreeVertexList()) {
					for (VertexImpl vertex : deletedVertices) {
						int id = vertex.getId();
						assert id > 0;
						graph.freeVertexIndex(vertex.getId());
					}
					deletedVertices = null;
				}
			}
			if (deletedEdges != null && deletedEdges.size() > 0) {
				synchronized (graph.getFreeEdgeList()) {
					for (EdgeImpl edge : deletedEdges) {
						int id = edge.getId();
						assert id > 0;
						graph.freeEdgeIndex(edge.getId());
					}
					deletedEdges = null;
				}
			}
			graph.freeStoredIndexes();
			removeAllTemporaryValues();

			state = TransactionState.COMMITTED;
			transactionManager.removeTransactionForThread(Thread
					.currentThread());
			transactionManager.removeTransaction(this);

			transactionManager.botWritingSync.writeLock().unlock();
			transactionManager.commitValidatingSync.writeLock().unlock();
			transactionManager.commitSync.writeLock().unlock();
		// case Read-Only-Transaction
		} else {
			state = TransactionState.COMMITTED;
			transactionManager.removeTransactionForThread(Thread
					.currentThread());
			transactionManager.removeTransaction(this);
		}

		// free memory
		addedVertices = null;
		addedEdges = null;
		deletedVertices = null;
		deletedEdges = null;
		deleteVertexList = null;
		changedVseqVertices = null;
		changedEseqEdges = null;
		changedIncidences = null;
		changedEdges = null;
		changedAttributes = null;
		savepointList = null;
		changedDuringCommit = null;
		temporaryValueMap = null;
	}

	/**
	 * "Garbage collection" for persistent values.
	 */
	protected void removeNonReferencedPersistentValues() {
		// garbage collection - delete all persistent versions no longer
		// needed and all temporary versions for transaction
		long maxVersionNumber = 0;
		TransactionImpl oldestTransaction = (TransactionImpl) transactionManager
				.getOldestTransaction();
		// if current transaction is oldest transaction, take the next
		// transaction is oldest transaction (if any exists)
		if (oldestTransaction == this
				&& transactionManager.getTransactions().size() > 1) {
			oldestTransaction = (TransactionImpl) transactionManager
					.getTransactions().get(1);
		}
		maxVersionNumber = oldestTransaction.persistentVersionAtBot;
		// for all versioned data-objects which have been changed during commit
		// of this transaction...
		if (changedDuringCommit != null) {
			for (VersionedDataObjectImpl<?> versionedDataObject : changedDuringCommit) {
				// if current transaction is oldest and the only transaction
				// currently active, then set maxVersionNumber as the current
				// highest persistent version of versionedDataObject
				if (oldestTransaction == this
						&& transactionManager.getTransactions().size() == 1) {
					maxVersionNumber = versionedDataObject
							.getLatestPersistentVersion();
				}
				versionedDataObject.removePersistentValues(maxVersionNumber);
				versionedDataObject.removeAllTemporaryValues(this);
				long minRange = 0;
				TransactionImpl realOldestTransaction = (TransactionImpl) transactionManager
						.getOldestTransaction();
				long maxRange = realOldestTransaction.persistentVersionAtBot;
				versionedDataObject.removePersistentValues(minRange, maxRange);
				// Delete all unreferenced persistent values of versioned
				// data-object in between two versions...
				synchronized (transactionManager.transactionList) {
					if (transactionManager.transactionList.size() > 1) {
						for (int i = 0; i < transactionManager.transactionList
								.size() - 1; i++) {
							TransactionImpl t1 = (TransactionImpl) transactionManager
									.getTransactions().get(i);
							TransactionImpl t2 = (TransactionImpl) transactionManager
									.getTransactions().get(i + 1);
							minRange = t1.persistentVersionAtBot;
							maxRange = t2.persistentVersionAtBot;
							if (minRange > maxRange) {
								throw new GraphException(
										"This should not happen. The transactions in transaction list should be sorted asc by persistentVersionAtBot.");
							}
							versionedDataObject.removePersistentValues(
									minRange, maxRange);
						}
					}
				}
			}
		}
	}

	/**
	 * Removes all temporary values created within this transaction.
	 */
	private void removeAllTemporaryValues() {
		// remove all temporary values created...
		if (temporaryValueMap != null) {
			// synchronized (temporaryValueMap) {
			temporaryValueMap.clear();
			temporaryValueMap = null;
			// }
		}
		if (temporaryVersionMap != null) {
			temporaryVersionMap.clear();
			temporaryVersionMap = null;
		}
	}

	@Override
	public Savepoint defineSavepoint() {
		if (thread != Thread.currentThread()) {
			throw new GraphException(
					"Transaction is not active in current thread.");
		}
		if (readOnly) {
			throw new GraphException(
					"Read-only transactions are not allowed to define save-points.");
		}
		// initialize save-point list if needed
		if (savepointList == null) {
			savepointList = new ArrayList<Savepoint>(1);
		}
		// synchronized (savepointList) {
		SavepointImpl savepoint = new SavepointImpl(this, ++savepointIdCounter);
		latestDefinedSavepoint = savepoint;
		savepointList.add(savepoint);
		// if save-point is defined and temporary values don't have mappings
		// with version numbers yet, then it has to be done now...
		if (temporaryValueMap != null) {
			synchronized (temporaryValueMap) {
				Set<VersionedDataObject<?>> versionedDataObjects = new HashSet<VersionedDataObject<?>>(
						temporaryValueMap.keySet());
				for (VersionedDataObject<?> dataObject : versionedDataObjects) {
					// dataObject.createNewTemporaryValue(this);
					((VersionedDataObjectImpl<?>) dataObject)
							.moveTemporaryVersion(this);
				}
			}
		}
		return savepoint;
		// }
	}

	@Override
	public Graph getGraph() {
		return graph;
	}

	@Override
	public int getID() {
		return id;
	}

	@Override
	public List<Savepoint> getSavepoints() {
		if (thread != Thread.currentThread()) {
			throw new GraphException(
					"Transaction is not active in current thread.");
		}
		if (savepointList == null) {
			return new ArrayList<Savepoint>(1);
		}
		synchronized (savepointList) {
			// return a copy
			List<Savepoint> savepointList = new ArrayList<Savepoint>(
					this.savepointList);
			return savepointList;
		}
	}

	@Override
	public TransactionState getState() {
		return state;
	}

	protected void setThread(Thread thread) {
		this.thread = thread;
	}

	@Override
	public Thread getThread() {
		return thread;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public void restoreSavepoint(Savepoint savepoint)
			throws InvalidSavepointException {
		if (thread != Thread.currentThread()) {
			throw new GraphException(
					"Transaction is not active in current thread.");
		}
		if (savepointList == null || !savepointList.contains(savepoint)
				|| savepoint.getTransaction() != this) {
			throw new InvalidSavepointException(this, savepoint);
		}
		SavepointImpl sp = (SavepointImpl) savepoint;
		// mark restored save-point in this transaction...
		latestDefinedSavepoint = sp;
		latestRestoredSavepoint = sp;
		// restore change sets from save-point...
		addedEdges = sp.addedEdges;
		addedVertices = sp.addedVertices;
		deletedEdges = sp.deletedEdges;
		deletedVertices = sp.deletedVertices;
		changedEseqEdges = sp.changedEseqEdges;
		changedVseqVertices = sp.changedVseqVertices;
		changedIncidences = sp.changedIncidences;
		changedAttributes = sp.changedAttributes;
	}

	/**
	 * This method removes all invalid save-points after restoring a save-point
	 * and executing the first write-operation.
	 */
	protected void removeInvalidSavepoints() {
		assert latestDefinedSavepoint != null
				&& latestRestoredSavepoint != null;
		// remove all invalid save-points which have been defined after the
		// latest restored save-point
		for (int i = savepointList.indexOf(latestRestoredSavepoint) + 1; i < savepointList
				.size(); i++) {
			savepointList.remove(i);
		}
		// remove all invalid temporary values...
		if (temporaryVersionMap != null) {
			// avoids CurrentModificationException!!!
			Set<VersionedDataObject<?>> versionedDataObjects = new HashSet<VersionedDataObject<?>>(
					temporaryVersionMap.keySet());
			for (VersionedDataObject<?> versionedDataObject : versionedDataObjects) {
				// the stored version number at latest restored save-point
				// should be given as <code>minVersion</code>...
				versionedDataObject.removeTemporaryValues(
						latestRestoredSavepoint.versionAtSavepoint, this);
				// if there are no temporary values left for current versioned
				// data-object then remove mapping...
				if (!versionedDataObject.hasTemporaryValue(this)) {
					temporaryVersionMap.remove(versionedDataObject);
				}
			}
		}
		temporaryVersionCounter = latestRestoredSavepoint.versionAtSavepoint;
		// now this can be set to <code>null<code> again, because invalid
		// temporary values have been removed...
		latestRestoredSavepoint = null;
	}

	@Override
	public boolean isInConflict() {
		if (thread != Thread.currentThread()) {
			throw new GraphException(
					"Transaction is not active in current thread.");
		}
		state = TransactionState.VALIDATING;
		boolean result = false;
		// validation and COMMIT synchronized
		transactionManager.commitValidatingSync.readLock().lock();
		result = internalIsInConflict();
		transactionManager.commitValidatingSync.readLock().unlock();
		state = TransactionState.RUNNING;
		return result;
	}

	/**
	 * 
	 * @return if a conflict could be detected
	 */
	private boolean internalIsInConflict() {
		assert thread == Thread.currentThread();
		state = TransactionState.VALIDATING;
		if (readOnly) {
			return false;
		}
		if (validationComponent == null) {
			validationComponent = new ValidationComponent(this);
		}
		return validationComponent.isInConflict();
	}

	@Override
	public void removeSavepoint(Savepoint savepoint) {
		if (thread != Thread.currentThread()) {
			throw new GraphException(
					"Transaction is not active in current thread.");
		}
		if (savepoint.getTransaction() != this) {
			throw new GraphException(
					"This savepoint doesn't belong to this transaction.");
		}
		if (savepointList != null && !savepointList.isEmpty()) {
			synchronized (savepointList) {
				int position = savepointList.indexOf(savepoint);
				savepointList.remove(savepoint);
				// savepoint is the first and only savepoint
				if (savepointList.isEmpty()
						&& latestRestoredSavepoint != savepoint) {
					// move every versioned data-object from
					// <code>temporaryVersionMap</code> to
					// <code>temporaryValueMap</code>
					for (Entry<VersionedDataObject<?>, SortedMap<Long, Object>> entries : temporaryVersionMap
							.entrySet()) {
						VersionedDataObject<?> versionedDataObject = entries
								.getKey();
						SortedMap<Long, Object> versionsMap = entries
								.getValue();
						temporaryValueMap.put(versionedDataObject, versionsMap
								.get(versionsMap.lastKey()));
						temporaryVersionMap.remove(versionedDataObject);
					}
					temporaryVersionMap = null;
				} else {
					// savepoint is the latest defined
					if (savepointList.size() == position) {
						;// TODO implement
					} else {
						;// TODO implement
					}
				}
			}
		}
	}

	@Override
	public boolean isValid() {
		return getState() != TransactionState.ABORTED
				&& getState() != TransactionState.COMMITTED;
	}

	@Override
	public String toString() {
		return "TA-" + id + "_Graph-" + graph.getId() + "-" + state;
	}

	/**
	 * 
	 * @return a set containing all versioned data-objects which are not part of
	 *         the change sets
	 */
	public Set<VersionedDataObject<?>> getRemainingVersionedDataObjects() {
		Set<VersionedDataObject<?>> versionedDataObjects = new HashSet<VersionedDataObject<?>>();
		if (temporaryValueMap != null && temporaryValueMap.size() > 0) {
			versionedDataObjects = new HashSet<VersionedDataObject<?>>(
					temporaryValueMap.keySet());
		}
		if (temporaryVersionMap != null && temporaryVersionMap.size() > 0) {
			versionedDataObjects = new HashSet<VersionedDataObject<?>>(
					temporaryVersionMap.keySet());
		}
		if (versionedDataObjects.size() > 0) {
			Set<EdgeImpl> edges = new HashSet<EdgeImpl>();
			Set<ReversedEdgeImpl> reversedEdges = new HashSet<ReversedEdgeImpl>();
			if (addedEdges != null) {
				edges.addAll(addedEdges);
			}
			if (changedEdges != null) {
				edges.addAll(changedEdges.keySet());
			}
			if (changedEseqEdges != null) {
				edges.addAll(changedEseqEdges.keySet());
			}
			if (deletedEdges != null) {
				edges.addAll(deletedEdges);
			}
			if (changedIncidences != null) {
				for (Map<IncidenceImpl, Map<ListPosition, Boolean>> map : changedIncidences
						.values()) {
					for (IncidenceImpl incidence : map.keySet()) {
						if (incidence instanceof EdgeImpl) {
							edges.add((EdgeImpl) incidence);
						}
						if (incidence instanceof ReversedEdgeImpl) {
							reversedEdges.add((ReversedEdgeImpl) incidence);
						}
					}
				}
			}
			for (EdgeImpl edge : edges) {
				versionedDataObjects.remove(edge.incidentVertex);
				versionedDataObjects.remove(edge.nextEdge);
				versionedDataObjects.remove(edge.nextIncidence);
				versionedDataObjects.remove(edge.prevEdge);
				versionedDataObjects.remove(edge.prevIncidence);
			}
			for (ReversedEdgeImpl reversedEdge : reversedEdges) {
				versionedDataObjects.remove(reversedEdge.incidentVertex);
				versionedDataObjects.remove(reversedEdge.nextIncidence);
				versionedDataObjects.remove(reversedEdge.prevIncidence);
			}
			Set<VertexImpl> vertices = new HashSet<VertexImpl>();
			if (addedVertices != null) {
				vertices.addAll(addedVertices);
			}
			if (changedVseqVertices != null) {
				vertices.addAll(changedVseqVertices.keySet());
			}
			if (deletedVertices != null) {
				vertices.addAll(deletedVertices);
			}
			if (changedIncidences != null) {
				vertices.addAll(changedIncidences.keySet());
			}
			for (VertexImpl vertex : vertices) {
				versionedDataObjects.remove(vertex.firstIncidence);
				versionedDataObjects.remove(vertex.incidenceListVersion);
				versionedDataObjects.remove(vertex.lastIncidence);
				versionedDataObjects.remove(vertex.nextVertex);
				versionedDataObjects.remove(vertex.prevVertex);
			}
			if (changedAttributes != null) {
				for (Entry<AttributedElement, Set<VersionedDataObject<?>>> entry : changedAttributes
						.entrySet()) {
					Set<VersionedDataObject<?>> attributes = entry.getValue();
					for (VersionedDataObject<?> attribute : attributes) {
						versionedDataObjects.remove(attribute);
					}
				}
			}
		}
		return versionedDataObjects;
	}
}
