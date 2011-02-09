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

import java.util.Map.Entry;

/**
 * Global edge list ESeq of a graph.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public class EdgeList extends GraphElementList<DatabasePersistableEdge> {

	/**
	 * Graph this edge list belongs to.
	 */
	private GraphImpl owningGraph;

	/**
	 * Creates and initializes a new <code>EdgeList</code>.
	 * 
	 * @param owningGraph
	 *            Graph this edge list belongs to.
	 */
	public EdgeList(GraphImpl owningGraph) {
		super();
		this.owningGraph = owningGraph;
	}

	/**
	 * Internal reorganizer for edge list.
	 */
	private class EdgeListReorganizer extends Reorganizer<Integer> {

		EdgeListReorganizer(GraphImpl graph) {
			super(graph);
		}

		@Override
		protected void updateCachedElement(Integer id, long sequenceNumber) {
			if (graph.isEdgeCached(id)) {
				updateCachedEdge(id, sequenceNumber);
			}
		}

		private void updateCachedEdge(Integer eId, long sequenceNumber) {
			DatabasePersistableEdge cachedEdge = (DatabasePersistableEdge) graph
					.getEdge(eId);
			updateWithoutUpdatingInDatabase(cachedEdge, sequenceNumber);
		}

		private void updateWithoutUpdatingInDatabase(
				DatabasePersistableEdge edge, long sequenceNumber) {
			edge.setInitialized(false);
			edge.setSequenceNumberInESeq(sequenceNumber);
			edge.setInitialized(true);
		}
	}

	@Override
	protected void updateVersion(long version) {
		// nothing to do here as version of edge list is only written back on
		// close of db
	}

	/**
	 * Adds an edge without modifying version of list. Use method to load an
	 * existing list into memory before actively using it.
	 * 
	 * @param sequenceNumber
	 *            Number mapping edge's sequence in ESeq.
	 * @param eId
	 *            Edge id in graph.
	 */
	public void add(long sequenceNumber, int eId) {
		sequenceNumberToIdMap.put(sequenceNumber, Math.abs(eId));
		// usedIDs.set(Math.abs(eId));
	}

	/**
	 * Gets first edge in edge list.
	 * 
	 * @returns First edge in edge list or null if edge list is empty.
	 */
	@Override
	public DatabasePersistableEdge getFirst() {
		if (!isEmpty()) {
			return (DatabasePersistableEdge) owningGraph.getEdge(getFirstEdgeId());
		} else {
			return null;
		}
	}

	private int getFirstEdgeId() {
		Entry<Long, Integer> firstEntry = sequenceNumberToIdMap
				.firstEntry();
		return firstEntry.getValue();
	}

	/**
	 * Checks whether edge list is empty.
	 * 
	 * @return true if edge list is empty, false otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return sequenceNumberToIdMap.isEmpty();
	}

	/**
	 * Gets last edge in edge list.
	 * 
	 * @returns Last edge in edge list or null if edge list is empty.
	 */
	@Override
	public DatabasePersistableEdge getLast() {
		if (!isEmpty()) {
			return (DatabasePersistableEdge) owningGraph.getEdge(getLastEdgeId());
		} else {
			return null;
		}
	}

	private int getLastEdgeId() {
		Entry<Long, Integer> firstEntry = sequenceNumberToIdMap
				.lastEntry();
		return firstEntry.getValue();
	}

	/**
	 * Gets previous edge in edge list of a given one.
	 * 
	 * @param edge
	 *            Edge to get it's predecessor in vertex list.
	 * @return Previous edge in edge list of given one or null if given edge is
	 *         first one.
	 * 
	 *         Precondition: Given edge must be part of vertex list.
	 */
	@Override
	public DatabasePersistableEdge getPrev(DatabasePersistableEdge edge) {
		assert contains(edge);
		if (!isFirst(edge)) {
			int prevEId = getPrevEdgeId(edge);
			return (DatabasePersistableEdge) owningGraph.getEdge(prevEId);
		} else {
			return null;
		}
	}

	/**
	 * Checks whether edge list contains given edge.
	 * 
	 * @param edge
	 *            The edge to check for.
	 * @return true if edge list contains given edge, false otherwise.
	 */
	@Override
	public boolean contains(DatabasePersistableEdge edge) {
		Integer eId = sequenceNumberToIdMap.get(edge
				.getSequenceNumberInESeq());
		if (eId != null) {
			return eId == Math.abs(edge.getId());
		} else {
			return false;
		}
	}

	@Override
	protected boolean equalsFirst(DatabasePersistableEdge edge) {
		int firstEId = getFirstEdgeId();
		return firstEId == edge.getId();
	}

	private int getPrevEdgeId(DatabasePersistableEdge edge) {
		Entry<Long, Integer> previousEntry = sequenceNumberToIdMap
				.lowerEntry(edge.getSequenceNumberInESeq());
		return previousEntry.getValue();
	}

	/**
	 * Gets next edge in edge list of a given one.
	 * 
	 * @param edge
	 *            Edge to get it's successor in edge list.
	 * @return Next edge in edge list of given one or null if given edge is last
	 *         one.
	 * 
	 *         Precondition: Given edge must be part of edge list.
	 */
	@Override
	public DatabasePersistableEdge getNext(DatabasePersistableEdge edge) {
		assert contains(edge);
		if (!isLast(edge)) {
			int nextEId = getNextEdgeId(edge);
			return (DatabasePersistableEdge) owningGraph.getEdge(nextEId);
		} else {
			return null;
		}
	}

	@Override
	protected boolean equalsLast(DatabasePersistableEdge edge) {
		int lastEId = getLastEdgeId();
		return lastEId == edge.getId();
	}

	private int getNextEdgeId(DatabasePersistableEdge edge) {
		Entry<Long, Integer> nextEntry = sequenceNumberToIdMap
				.higherEntry(edge.getSequenceNumberInESeq());
		return nextEntry.getValue();
	}

	/**
	 * Prepends an edge to edge list.
	 * 
	 * @param edge
	 *            The edge to prepend to edge list.
	 */
	@Override
	public void prepend(DatabasePersistableEdge edge) {
		if (!isFirst(edge)) {
			prependByMoveOrInsert(edge);
		}
	}

	private void prependByMoveOrInsert(DatabasePersistableEdge edge) {
		assureThatElementCanBePrepended();
		long sequenceNumber = getRegularSequenceNumberBeforeFirstElementOf();
		moveOrInsert(edge, sequenceNumber);
	}

	@Override
	protected void moveTo(DatabasePersistableEdge edge, long sequenceNumber) {
		assert contains(edge);
		sequenceNumberToIdMap.remove(edge.getSequenceNumberInESeq());
		edge.setSequenceNumberInESeq(sequenceNumber);
		sequenceNumberToIdMap.put(sequenceNumber, Math.abs(edge.getId()));
	}

	@Override
	protected void insertAt(DatabasePersistableEdge edge, long sequenceNumber) {
		assert !contains(edge);
		edge.setSequenceNumberInESeq(sequenceNumber);
		sequenceNumberToIdMap.put(sequenceNumber, Math.abs(edge.getId()));
		// usedIDs.set(Math.abs(edge.getId()));
	}

	/**
	 * Appends an edge to edge list.
	 * 
	 * @param edge
	 *            The edge to append to edge list.
	 */
	@Override
	public void append(DatabasePersistableEdge edge) {
		if (!edge.isNormal()) {
			edge = (DatabasePersistableEdge) edge.getNormalEdge();
		}
		if (!isLast(edge)) {
			appendByMoveOrInsert(edge);
		}
	}

	private void appendByMoveOrInsert(DatabasePersistableEdge edge) {
		assureThatElementCanBeAppended();
		long sequenceNumber = getRegularSequenceNumberAfterLastElementOf();
		moveOrInsert(edge, sequenceNumber);
	}

	/**
	 * Puts an edge before another one in edge list.
	 * 
	 * @param targetEdge
	 *            The target edge.
	 * @param movedEdge
	 *            The edge to move.
	 * 
	 *            Precondition: Given edge must be part of this edge list and
	 *            not the same.
	 * 
	 *            Postcondition: Moved edge is predecessor of target edge.
	 */
	@Override
	public void putBefore(DatabasePersistableEdge targetEdge,
			DatabasePersistableEdge movedEdge) {
		assert areBothInSameEdgeList(targetEdge, movedEdge);
		if (targetEdge != movedEdge
				&& !isPrevNeighbour(targetEdge, movedEdge)) {
			moveEdgeBefore(movedEdge, targetEdge);
		}
	}

	private boolean areBothInSameEdgeList(DatabasePersistableEdge targetEdge,
			DatabasePersistableEdge movedEdge) {
		return contains(targetEdge) && contains(movedEdge);
	}

	private boolean isPrevNeighbour(DatabasePersistableEdge edge,
			DatabasePersistableEdge allegedNeighbourEdge) {
		if (!isFirst(edge)) {
			return getPrevEdgeId(edge) == allegedNeighbourEdge.getId();
		} else {
			return false;
		}
	}

	private void moveEdgeBefore(DatabasePersistableEdge movedEdge,
			DatabasePersistableEdge targetEdge) {
		long newSequenceNumber = this.getPrevFreeSequenceNumber(targetEdge);
		moveTo(movedEdge, newSequenceNumber);
		owningGraph.edgeListModified();
	}

	private long getPrevFreeSequenceNumber(DatabasePersistableEdge edge) {
		assert !isFirst(edge);
		return this.getPrevFreeSequenceNumber(edge.getSequenceNumberInESeq());
	}

	/**
	 * Puts an edge after another one in edge list.
	 * 
	 * @param targetEdge
	 *            The target edge.
	 * @param movedEdge
	 *            The edge to move.
	 * 
	 *            Precondition: Given edge must be part of this edge list and
	 *            not the same.
	 * 
	 *            Postcondition: Moved edge is successor of target edge.
	 */
	@Override
	public void putAfter(DatabasePersistableEdge targetEdge,
			DatabasePersistableEdge movedEdge) {
		assert areBothInSameEdgeList(targetEdge, movedEdge);
		assert targetEdge != movedEdge;
		if (targetEdge != movedEdge
				&& !isNextNeighbor(targetEdge, movedEdge)) {
			moveEdgeBehind(movedEdge, targetEdge);
		}
	}

	@Override
	protected boolean isNextNeighbor(DatabasePersistableEdge edge,
			DatabasePersistableEdge allegedNeighborEdge) {
		if (!isLast(edge)) {
			return getNextEdgeId(edge) == allegedNeighborEdge.getId();
		} else {
			return false;
		}
	}

	private void moveEdgeBehind(DatabasePersistableEdge movedEdge,
			DatabasePersistableEdge targetEdge) {
		long newSequenceNumber = this.getNextFreeSequenceNumber(targetEdge);
		moveTo(movedEdge, newSequenceNumber);
		owningGraph.edgeListModified();
	}

	private long getNextFreeSequenceNumber(DatabasePersistableEdge edge) {
		assert !isLast(edge);
		return this.getNextFreeSequenceNumber(edge.getSequenceNumberInESeq());
	}

	/**
	 * Removes given edge from edge list.
	 * 
	 * @param edge
	 *            The edge to remove.
	 * 
	 *            Precondition: Edge must be contained in edge list.
	 * 
	 *            Postcondition: Edge is no longer part of edge list and size
	 *            has been decremented by 1.
	 */
	@Override
	public void remove(DatabasePersistableEdge edge) {
		assert contains(edge);
		sequenceNumberToIdMap.remove(edge.getSequenceNumberInESeq());
		// usedIDs.clear(Math.abs(edge.getId()));
	}

	@Override
	public int size() {
		return sequenceNumberToIdMap.size();
	}

	@Override
	protected void reorganize() {
		reorganizeInMemory();
		owningGraph.reorganizeEdgeListInGraphDatabase();
	}

	private void reorganizeInMemory() {
		try {
			EdgeListReorganizer reorganizer = new EdgeListReorganizer(
					owningGraph);
			sequenceNumberToIdMap = reorganizer
					.getReorganisedMap(sequenceNumberToIdMap);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Override
	protected void clear() {
		sequenceNumberToIdMap.clear();
		// usedIDs.clear();
	}

	/**
	 * Checks if an edge with given id is part of ESeq.
	 * 
	 * @param eId
	 *            Id of edge
	 * @return true if an edge with given id is part of ESeq, otherwise false.
	 */
	protected boolean containsEdge(int eId) {
		return sequenceNumberToIdMap.containsValue(Math.abs(eId));
		// return usedIDs.get(Math.abs(eId));
	}
}
