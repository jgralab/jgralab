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
package de.uni_koblenz.jgralab.impl.db;

import java.util.Set;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.GraphException;

/**
 * Incidence list LamdaSeq of a vertex.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public class IncidenceList extends GraphElementList<DatabasePersistableEdge> {

	private VertexImpl owningVertex;

	/**
	 * Creates and initializes a new <code>IncidenceList</code>.
	 * 
	 * @param vertex
	 *            Vertex the incidence list belongs to.
	 */
	public IncidenceList(VertexImpl vertex) {
		super();
		this.owningVertex = vertex;
	}

	/**
	 * Internal reorganizer for incidence list.
	 */
	private class IncidenceListReorganizer extends Reorganizer<Integer> {

		public IncidenceListReorganizer(VertexImpl vertex) {
			super((GraphImpl) vertex.getGraph());
		}

		@Override
		protected void updateCachedElement(Integer eId, long sequenceNumber) {
			if (graph.isEdgeCached(eId)) {
				updateCachedIncidentEdge(eId, sequenceNumber);
			}
		}

		private void updateCachedIncidentEdge(int eId, long sequenceNumber) {
			DatabasePersistableEdge cachedEdge = (DatabasePersistableEdge) graph
					.getEdge(eId);
			updateWithoutUpdatingInDatabase(cachedEdge, sequenceNumber);
		}

		private void updateWithoutUpdatingInDatabase(
				DatabasePersistableEdge edge, long sequenceNumber) {
			edge.setInitialized(false);
			edge.setSequenceNumberInLambdaSeq(sequenceNumber);
			edge.setInitialized(true);
		}
	}

	/**
	 * Adds an incidence to list without incrementing incidence list version.
	 * Use it only to fill an incidence list at startup.
	 * 
	 * @param eId
	 *            Id of incident edge.
	 * @param sequenceNumber
	 *            Number mapping incidence's sequence in incidence list.
	 */
	public void add(int eId, long sequenceNumber) {
		this.sequenceNumberToIdMap.put(sequenceNumber, eId);
		usedIDs.set(Math.abs(eId));
	}

	@Override
	protected void updateVersion(long version) {
		this.writeBackVersion();
	}

	private void writeBackVersion() {
		this.owningVertex.writeBackIncidenceListVersion();
	}

	/**
	 * Gets first incidence in incidence list.
	 * 
	 * @return First incidence in incidence list or null if list is empty.
	 */
	@Override
	public DatabasePersistableEdge getFirst() {
		if (!this.isEmpty()) {
			return this.getFirstIncidence();
		} else {
			return null;
		}
	}

	/**
	 * Checks whether incidence list is empty.
	 * 
	 * @return true if incidence list is empty, false otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return this.sequenceNumberToIdMap.isEmpty();
	}

	private DatabasePersistableEdge getFirstIncidence() {
		Entry<Long, Integer> firstEntry = this.sequenceNumberToIdMap.firstEntry();
		return (DatabasePersistableEdge) this.owningVertex.getGraph().getEdge(
				firstEntry.getValue());
	}

	/**
	 * Gets last incidence in incidence list.
	 * 
	 * @return Last incidence in incidence list or null if list is empty.
	 */
	@Override
	public DatabasePersistableEdge getLast() {
		if (!this.isEmpty()) {
			return this.getLastIncidence();
		} else {
			return null;
		}
	}

	private DatabasePersistableEdge getLastIncidence() {
		Entry<Long, Integer> lastEntry = this.sequenceNumberToIdMap.lastEntry();
		return (DatabasePersistableEdge) this.owningVertex.getGraph().getEdge(
				lastEntry.getValue());
	}

	/**
	 * Gets previous incident edge.
	 * 
	 * @param edge
	 *            An incident edge.
	 * @return Previous incident edge or null, if given one is first incident
	 *         edge.
	 * 
	 *         Precondition: Given edge must be part of this incidence list.
	 */
	@Override
	public DatabasePersistableEdge getPrev(DatabasePersistableEdge edge) {
		assert this.contains(edge);
		if (!this.isFirst(edge)) {
			return getPrevOrientedEdge(edge);
		} else {
			return null;
		}
	}

	/**
	 * Checks if an edge is contained in incidence list.
	 * 
	 * @param edge
	 *            The edge to check for.
	 * @return true if given edge is contained in incidence list, false
	 *         otherwise.
	 */
	@Override
	public boolean contains(DatabasePersistableEdge edge) {
		Integer eId = this.sequenceNumberToIdMap
				.get(edge.getSequenceNumberInLambdaSeq());
		if (eId != null) {
			return eId == edge.getId()
					&& // Math.abs(eId) == edge.getId() &&
					this.owningVertex.getId() == edge.getIncidentVId()
					&& this.owningVertex.getGraph() == edge.getGraph();
		} else {
			return false;
		}
	}

	private boolean isFirst(DatabasePersistableEdge edge) {
		assert this.contains(edge);
		if (!this.isEmpty()) {
			return this.equalsFirstIncidence(edge);
		} else {
			return false;
		}
	}

	private boolean equalsFirstIncidence(DatabasePersistableEdge edge) {
		return this.sequenceNumberToIdMap.firstEntry().getValue() == edge.getId();
	}

	private DatabasePersistableEdge getPrevOrientedEdge(
			DatabasePersistableEdge edge) {
		int eId = this.getPrevIncidenceEId(edge);
		return (DatabasePersistableEdge) this.owningVertex.getGraph().getEdge(
				eId);
	}

	private int getPrevIncidenceEId(DatabasePersistableEdge edge) {
		return this.sequenceNumberToIdMap
				.lowerEntry(edge.getSequenceNumberInLambdaSeq()).getValue();
	}

	/**
	 * Gets next incident edge.
	 * 
	 * @param edge
	 *            An incident edge.
	 * @return Next incident edge or null, if given one is last incident edge.
	 * 
	 *         Precondition: Given edge must be part of this incidence list.
	 */
	@Override
	public DatabasePersistableEdge getNext(DatabasePersistableEdge edge) {
		assert this.contains(edge);
		if (!this.isLast(edge)) {
			return getNextOriented(edge);
		} else {
			return null;
		}
	}

	private boolean isLast(DatabasePersistableEdge edge) {
		assert this.contains(edge);
		if (!this.isEmpty()) {
			return this.equalsLastIncidence(edge);
		} else {
			return false;
		}
	}

	private boolean equalsLastIncidence(DatabasePersistableEdge edge) {
		return this.sequenceNumberToIdMap.lastEntry().getValue() == edge.getId();
	}

	private DatabasePersistableEdge getNextOriented(DatabasePersistableEdge edge) {
		int eId = this.getNextIncidenceEId(edge);
		return (DatabasePersistableEdge) this.owningVertex.getGraph().getEdge(
				eId);
	}

	private int getNextIncidenceEId(DatabasePersistableEdge edge) {
		return this.sequenceNumberToIdMap.higherEntry(
				edge.getSequenceNumberInLambdaSeq()).getValue();
	}

	/**
	 * Prepends an edge to incidence list.
	 * 
	 * @param edge
	 *            The edge to prepend.
	 */
	@Override
	public void prepend(DatabasePersistableEdge edge) {
		if (!this.isFirst(edge)) {
			this.prependByMoveOrInsert(edge);
		}
	}

	private void prependByMoveOrInsert(DatabasePersistableEdge edge) {
		long sequenceNumber = this
				.getRegularSequenceNumberBeforeFirstElementOf(this.sequenceNumberToIdMap);
		this.moveOrInsert(edge, sequenceNumber);
	}

	private void moveOrInsert(DatabasePersistableEdge edge, long sequenceNumber) {
		if (this.contains(edge)) {
			this.moveTo(edge, sequenceNumber);
		} else {
			this.insertAt(edge, sequenceNumber);
		}
	}

	private void moveTo(DatabasePersistableEdge edge, long sequenceNumber) {
		assert this.contains(edge);
		int eId = this.sequenceNumberToIdMap.get(edge.getSequenceNumberInLambdaSeq());
		this.sequenceNumberToIdMap.remove(edge.getSequenceNumberInLambdaSeq());
		edge.setSequenceNumberInLambdaSeq(sequenceNumber);
		this.sequenceNumberToIdMap.put(sequenceNumber, eId);
	}

	private void insertAt(DatabasePersistableEdge edge, long sequenceNumber) {
		edge.setSequenceNumberInLambdaSeq(sequenceNumber);
		this.sequenceNumberToIdMap.put(sequenceNumber, edge.getId());
		usedIDs.set(Math.abs(edge.getId()));
	}

	/**
	 * Appends an edge to incidence list.
	 * 
	 * @param edge
	 *            The edge to append.
	 */
	@Override
	public void append(DatabasePersistableEdge edge) {
		if (!this.isLast(edge)) {
			this.appendByMoveOrInsert(edge);
		}
	}

	private void appendByMoveOrInsert(DatabasePersistableEdge edge) {
		long sequenceNumber = this
				.getRegularSequenceNumberBehindLastElementOf(this.sequenceNumberToIdMap);
		this.moveOrInsert(edge, sequenceNumber);
	}

	/**
	 * Puts an edge before another one in incidence list.
	 * 
	 * @param targetEdge
	 *            The target edge.
	 * @param movedEdge
	 *            The edge to move.
	 * 
	 *            Precondition: Given edges must be part of this incidence list
	 *            and not the same.
	 * 
	 *            Postcondition: Moved edge is predecessor of target edge.
	 */
	@Override
	public void putBefore(DatabasePersistableEdge targetEdge,
			DatabasePersistableEdge movedEdge) {
		assert haveSameIncidentVertex(targetEdge, movedEdge);
		assert targetEdge != movedEdge;
		if (targetEdge != movedEdge
				&& !this.isPrevNeighbour(targetEdge, movedEdge)) {
			this.moveIncidenceBefore(movedEdge, targetEdge);
		}
	}

	/**
	 * Checks if two edges are incident to the same vertex. This does NOT mean
	 * that they are both in the same incidence list
	 * 
	 * @param edge
	 *            An edge.
	 * @param anotherEdge
	 *            Another edge.
	 * @return true if two edges are incident to the same vertex, false
	 *         otherwise.
	 */
	private boolean haveSameIncidentVertex(DatabasePersistableEdge edge,
			DatabasePersistableEdge anotherEdge) {
		return edge.getIncidentVId() == anotherEdge.getIncidentVId();
	}

	private boolean isPrevNeighbour(DatabasePersistableEdge edge,
			DatabasePersistableEdge allegedNeighbourEdge) {
		return this.getPrevEdgeId(edge) == allegedNeighbourEdge.getId();
	}

	private int getPrevEdgeId(DatabasePersistableEdge edge) {
		if (!this.isFirst(edge) && this.contains(edge)) {
			return this.sequenceNumberToIdMap.lowerEntry(
					edge.getSequenceNumberInLambdaSeq()).getValue();
		} else {
			return 0;
		}
	}

	/**
	 * Moves an edge before another edge in this incidence list.
	 * 
	 * @param movedEdge
	 *            Edge to move before another edge in this incidence list.
	 * @param targetEdge
	 *            Another edge part of this incidence list
	 */
	private void moveIncidenceBefore(DatabasePersistableEdge movedEdge,
			DatabasePersistableEdge targetEdge) {
		long newSequenceNumber = this.getPrevFreeSequenceNumber(
				this.sequenceNumberToIdMap, targetEdge.getSequenceNumberInLambdaSeq());
		this.moveTo(movedEdge, newSequenceNumber);
	}

	/**
	 * Puts an edge after another one in incidence list.
	 * 
	 * @param targetEdge
	 *            The target edge.
	 * @param movedEdge
	 *            The edge to move.
	 * 
	 *            Precondition: Given edges must be part of this incidence list
	 *            and not the same.
	 * 
	 *            Postcondition: Moved edge is successor of target edge.
	 * @throws Exception
	 */
	@Override
	public void putAfter(DatabasePersistableEdge targetEdge,
			DatabasePersistableEdge movedEdge) {
		assert haveSameIncidentVertex(targetEdge, movedEdge);
		assert targetEdge != movedEdge;

		if (targetEdge != movedEdge
				&& !this.isNextNeighbour(targetEdge, movedEdge)) {
			// System.out.println("putAfter calls moveIncidenceBehind");
			this.moveIncidenceBehind(movedEdge, targetEdge);
		}
	}

	private boolean isNextNeighbour(DatabasePersistableEdge edge,
			DatabasePersistableEdge allegedNeighbourEdge) {
		return this.getNextEdgeId(edge) == allegedNeighbourEdge.getId();
	}

	private int getNextEdgeId(DatabasePersistableEdge edge)
			throws GraphException {
		if (!this.isLast(edge) && this.contains(edge)) {
			return this.sequenceNumberToIdMap.higherEntry(
					edge.getSequenceNumberInLambdaSeq()).getValue();
		} else {
			return 0;
		}
	}

	private void moveIncidenceBehind(DatabasePersistableEdge movedEdge,
			DatabasePersistableEdge targetEdge) {
		long newSequenceNumber = this.getNextFreeSequenceNumber(
				this.sequenceNumberToIdMap, targetEdge.getSequenceNumberInLambdaSeq());
		this.moveTo(movedEdge, newSequenceNumber);
	}

	/**
	 * Removes an edge from incidence list.
	 * 
	 * @param The
	 *            edge to remove.
	 * 
	 *            Precondition: Edge must be contained in incidence list.
	 * 
	 *            Postcondition: Edge no longer contained in list and size
	 *            decremented by 1.
	 */
	@Override
	public void remove(DatabasePersistableEdge edge) {
		assert this.contains(edge);
		if (this.sequenceNumberToIdMap.containsKey(edge.getSequenceNumberInLambdaSeq())) {
			this.sequenceNumberToIdMap.remove(edge.getSequenceNumberInLambdaSeq());
			usedIDs.clear(Math.abs(edge.getId()));
			/*
			 * As it is not known here if incidence will be completely deleted
			 * or just updated as edge points to new alpha or omega, sequence
			 * number is not changed and thus not updated to database.
			 */
		}
	}

	/**
	 * @return Count of edges contained in incidence list.
	 */
	@Override
	public int size() {
		return this.sequenceNumberToIdMap.size();
	}

	/**
	 * Reorganizes incidence list at server holding database and locally at the
	 * client.
	 */
	@Override
	public void reorganize() {
		GraphImpl graph = (GraphImpl) this.owningVertex.getGraph();
		graph.reorganizeIncidenceListInDatabaseOf(this.owningVertex);
		this.reorganizeAtClient();
	}

	/**
	 * Reorganizes incidence list.
	 */
	private void reorganizeAtClient() {
		try {
			IncidenceListReorganizer reorganizer = new IncidenceListReorganizer(
					this.owningVertex);
			this.sequenceNumberToIdMap = reorganizer
					.getReorganisedMap(this.sequenceNumberToIdMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void print() {
		Set<Long> sequenceNumbers = this.sequenceNumberToIdMap.keySet();
		Long[] seqNumArray = {};
		seqNumArray = sequenceNumbers.toArray(seqNumArray);
		for (int i = 0; i < seqNumArray.length; i++) {
			System.out.println("[" + seqNumArray[i] + ", "
					+ this.sequenceNumberToIdMap.get(seqNumArray[i]) + "]");
		}
	}

	@Override
	protected void clear() {
		this.sequenceNumberToIdMap.clear();
		usedIDs.clear();
	}

	/**
	 * Gets iterable ids of incident edges.
	 * 
	 * @return
	 */
	protected Iterable<Integer> eIds() {
		return this.sequenceNumberToIdMap.values();
	}

	/**
	 * Counts outgoing incidences.
	 * 
	 * @return Amount of outgoing incidences.
	 */
	protected int countOutgoing() {
		int count = 0;
		for (int eId : this.sequenceNumberToIdMap.values()) {
			if (eId > 0) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Counts incoming incidences.
	 * 
	 * @return Amount of incoming incidences.
	 */
	protected int countIncoming() {
		int count = 0;
		for (int eId : this.sequenceNumberToIdMap.values()) {
			if (eId < 0) {
				count++;
			}
		}
		return count;
	}
}
