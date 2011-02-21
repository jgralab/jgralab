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
package de.uni_koblenz.jgralab.impl.db;

import java.util.Map.Entry;

/**
 * Global vertex list VSeq of graph.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public class VertexList extends GraphElementList<DatabasePersistableVertex> {

	private GraphImpl owningGraph;

	/**
	 * Creates and initializes a new <code>VertexList</code>.
	 * 
	 * @param graph
	 *            Graph the vertex list belongs to.
	 */
	public VertexList(GraphImpl owningGraph) {
		super();
		this.owningGraph = owningGraph;
	}

	/**
	 * Internal reorganizer for vertex list.
	 */
	private class VertexListReorganizer extends Reorganizer<Integer> {

		/**
		 * Creates and initializes a new <code>VertexListReoarganizer</code>.
		 * 
		 * @param graph
		 *            Graph which has a vertex list to reorganize.
		 */
		public VertexListReorganizer(GraphImpl graph) {
			super(graph);
		}

		@Override
		protected void updateCachedElement(Integer id, long sequenceNumber) {
			if (graph.isVertexCached(id)) {
				updateCachedVertex(id, sequenceNumber);
			}
		}

		private void updateCachedVertex(int vId, long sequenceNumber) {
			DatabasePersistableVertex cachedVertex = (DatabasePersistableVertex) graph
					.getVertex(vId);
			updateWithoutUpdatingInDatabase(cachedVertex, sequenceNumber);
		}

		private void updateWithoutUpdatingInDatabase(
				DatabasePersistableVertex vertex, long sequenceNumber) {
			vertex.setInitialized(false);
			vertex.setSequenceNumberInVSeq(sequenceNumber);
			vertex.setInitialized(true);
		}
	}

	/**
	 * Adds minimal information of a vertex to list without modifying it.
	 * 
	 * @param sequenceNumber
	 *            Vertex's sequence number
	 * @param primaryKeyOfVertex
	 *            Id of vertex.
	 */
	public void add(long sequenceNumber, int vId) {
		sequenceNumberToIdMap.put(sequenceNumber, vId);
		// usedIDs.set(vId);
	}

	@Override
	protected void updateVersion(long version) {
		// nothing to do here as version of vertex list is only written back on
		// close of db
	}

	/**
	 * Gets first vertex of vertex list.
	 * 
	 * @return First vertex of vertex list, or null if vertex list is empty.
	 */
	@Override
	public DatabasePersistableVertex getFirst() {
		if (!isEmpty()) {
			return (DatabasePersistableVertex) owningGraph
					.getVertex(getFirstVertexId());
		} else {
			return null;
		}
	}

	/**
	 * Checks whether vertex list is empty.
	 * 
	 * @return true if vertex list contains no vertices, false otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return sequenceNumberToIdMap.isEmpty();
	}

	private int getFirstVertexId() {
		Entry<Long, Integer> firstEntry = sequenceNumberToIdMap.firstEntry();
		return firstEntry.getValue();
	}

	/**
	 * Gets last vertex of vertex list.
	 * 
	 * @return Last vertex of vertex list, or null if vertex list is empty.
	 */
	@Override
	public DatabasePersistableVertex getLast() {
		if (!isEmpty()) {
			return (DatabasePersistableVertex) owningGraph
					.getVertex(getLastVertexId());
		} else {
			return null;
		}
	}

	private int getLastVertexId() {
		Entry<Long, Integer> firstEntry = sequenceNumberToIdMap.lastEntry();
		return firstEntry.getValue();
	}

	/**
	 * Gets previous vertex in vertex list of a given one.
	 * 
	 * @param vertex
	 *            Vertex to get it's predecessor in vertex list.
	 * @return Previous vertex in vertex list of given one or null if given
	 *         vertex is first one.
	 * 
	 *         Precondition: Given vertex must be part of vertex list.
	 */
	@Override
	public DatabasePersistableVertex getPrev(DatabasePersistableVertex vertex) {
		assert contains(vertex);
		if (!isFirst(vertex)) {
			int prevVId = getPrevVertexId(vertex);
			return (DatabasePersistableVertex) owningGraph.getVertex(prevVId);
		} else {
			return null;
		}
	}

	/**
	 * Checks whether a vertex is part of vertex list.
	 * 
	 * @param vertex
	 *            The vertex to check for.
	 * @return true if given vertex is part of vertex list, false otherwise.
	 */
	@Override
	public boolean contains(DatabasePersistableVertex vertex) {
		Integer vId = sequenceNumberToIdMap.get(vertex
				.getSequenceNumberInVSeq());
		if (vId != null) {
			return vId == vertex.getId();
		} else {
			return false;
		}
	}

	@Override
	protected boolean equalsFirst(DatabasePersistableVertex vertex) {
		int firstVId = getFirstVertexId();
		return firstVId == vertex.getId();
	}

	private int getPrevVertexId(DatabasePersistableVertex vertex) {
		Entry<Long, Integer> previousEntry = sequenceNumberToIdMap
				.lowerEntry(vertex.getSequenceNumberInVSeq());
		return previousEntry.getValue();
	}

	/**
	 * Gets next vertex in vertex list of a given one.
	 * 
	 * @param vertex
	 *            Vertex to get it's successor in vertex list.
	 * @return Next vertex in vertex list of given one or null if given vertex
	 *         is last one.
	 * 
	 *         Precondition: Given vertex must be part of vertex list.
	 */
	@Override
	public DatabasePersistableVertex getNext(DatabasePersistableVertex vertex) {
		assert contains(vertex);
		if (!isLast(vertex)) {
			int nextVId = getNextVertexId(vertex);
			return (DatabasePersistableVertex) owningGraph.getVertex(nextVId);
		} else {
			return null;
		}
	}

	@Override
	protected boolean equalsLast(DatabasePersistableVertex vertex) {
		int lastVId = getLastVertexId();
		return lastVId == vertex.getId();
	}

	private int getNextVertexId(DatabasePersistableVertex vertex) {
		Entry<Long, Integer> nextEntry = sequenceNumberToIdMap
				.higherEntry(vertex.getSequenceNumberInVSeq());
		return nextEntry.getValue();
	}

	/**
	 * Prepends a vertex to vertex list.
	 * 
	 * @param vertex
	 *            The vertex to prepend.
	 */
	@Override
	public void prepend(DatabasePersistableVertex vertex) {
		if (!isFirst(vertex)) {
			prependByMoveOrInsert(vertex);
		}
	}

	private void prependByMoveOrInsert(DatabasePersistableVertex vertex) {
		assureThatElementCanBeAppended();
		long sequenceNumber = getRegularSequenceNumberBeforeFirstElementOf();
		moveOrInsert(vertex, sequenceNumber);
	}

	/**
	 * Appends a vertex to vertex list.
	 * 
	 * @param vertex
	 *            The vertex to append.
	 */
	@Override
	public void append(DatabasePersistableVertex vertex) {
		if (!isLast(vertex)) {
			appendByMoveOrInsert(vertex);
		}
	}

	private void appendByMoveOrInsert(DatabasePersistableVertex vertex) {
		assureThatElementCanBeAppended();
		long sequenceNumber = getRegularSequenceNumberAfterLastElementOf();
		moveOrInsert(vertex, sequenceNumber);
	}

	/**
	 * Puts a vertex behind another one in vertex list.
	 * 
	 * @param targetVertex
	 *            The target vertex.
	 * @param movedVertex
	 *            The vertex to move.
	 * 
	 *            Precondition: Given vertices must be part of this vertex list
	 *            and not the same.
	 * 
	 *            Postcondition: Moved vertex is successor of target edge.
	 */
	@Override
	public void putAfter(DatabasePersistableVertex targetVertex,
			DatabasePersistableVertex movedVertex) {
		assert areBothInSameVertexList(targetVertex, movedVertex);
		assert targetVertex != movedVertex;
		if (!targetVertex.equals(movedVertex)
				&& !isNextNeighbor(targetVertex, movedVertex)) {
			moveVertexBehind(movedVertex, targetVertex);
		}
	}

	private boolean areBothInSameVertexList(
			DatabasePersistableVertex targetVertex,
			DatabasePersistableVertex movedVertex) {
		return contains(targetVertex) && contains(movedVertex);
	}

	/**
	 * Puts a vertex before another one in vertex list.
	 * 
	 * @param targetVertex
	 *            The target vertex.
	 * @param movedVertex
	 *            The vertex to move.
	 * 
	 *            Precondition: Given vertices must be part of this vertex list
	 *            and not the same.
	 * 
	 *            Postcondition: Moved vertex is predecessor of target vertex.
	 */
	@Override
	public void putBefore(DatabasePersistableVertex targetVertex,
			DatabasePersistableVertex movedVertex) {
		assert areBothInSameVertexList(targetVertex, movedVertex);
		assert targetVertex != movedVertex;
		if (!targetVertex.equals(movedVertex)
				&& !isPrevNeighbour(targetVertex, movedVertex)) {
			moveVertexBefore(movedVertex, targetVertex);
		}
	}

	private boolean isPrevNeighbour(DatabasePersistableVertex vertex,
			DatabasePersistableVertex allegedNeighbourVertex) {
		if (!isFirst(vertex)) {
			return getPrevVertexId(vertex) == allegedNeighbourVertex.getId();
		} else {
			return false;
		}
	}

	@Override
	protected boolean isNextNeighbor(DatabasePersistableVertex vertex,
			DatabasePersistableVertex allegedNeighborVertex) {
		if (!isLast(vertex)) {
			return getNextVertexId(vertex) == allegedNeighborVertex.getId();
		} else {
			return false;
		}
	}

	private void moveVertexBefore(DatabasePersistableVertex movedVertex,
			DatabasePersistableVertex targetVertex) {
		long newSequenceNumber = this.getPrevFreeSequenceNumber(targetVertex);
		moveTo(movedVertex, newSequenceNumber);
		owningGraph.vertexListModified();
	}

	private long getPrevFreeSequenceNumber(DatabasePersistableVertex vertex) {
		assert !isFirst(vertex);
		return this.getPrevFreeSequenceNumber(vertex.getSequenceNumberInVSeq());
	}

	private void moveVertexBehind(DatabasePersistableVertex movedVertex,
			DatabasePersistableVertex targetVertex) {
		long newSequenceNumber = this.getNextFreeSequenceNumber(targetVertex);
		moveTo(movedVertex, newSequenceNumber);
		owningGraph.vertexListModified();
	}

	private long getNextFreeSequenceNumber(DatabasePersistableVertex vertex) {
		assert !isLast(vertex);
		return this.getNextFreeSequenceNumber(vertex.getSequenceNumberInVSeq());
	}

	@Override
	protected void moveTo(DatabasePersistableVertex vertex, long sequenceNumber) {
		sequenceNumberToIdMap.remove(vertex.getSequenceNumberInVSeq());
		vertex.setSequenceNumberInVSeq(sequenceNumber);
		sequenceNumberToIdMap.put(sequenceNumber, vertex.getId());
	}

	@Override
	protected void insertAt(DatabasePersistableVertex vertex,
			long sequenceNumber) {
		vertex.setSequenceNumberInVSeq(sequenceNumber);
		sequenceNumberToIdMap.put(sequenceNumber, vertex.getId());
		// usedIDs.set(vertex.getId());
	}

	/**
	 * Removes a vertex from vertex list.
	 * 
	 * @param vertex
	 *            The vertex to remove.
	 * 
	 *            Precondition: Vertex is part of vertex list.
	 * 
	 *            Postcondition: Vertex is no longer part of edge list and size
	 *            has been decremented by 1.
	 */
	@Override
	public void remove(DatabasePersistableVertex vertex) {
		assert contains(vertex);
		sequenceNumberToIdMap.remove(vertex.getSequenceNumberInVSeq());
		// usedIDs.clear(vertex.getId());
		/*
		 * As it is not known here if the vertex will be completely deleted or
		 * just moved, his sequence number is not changed and thus not updated
		 * to database.
		 */
	}

	/**
	 * @return Count of vertices contained in vertex list.
	 */
	@Override
	public int size() {
		return sequenceNumberToIdMap.size();
	}

	@Override
	public void reorganize() {
		owningGraph.reorganizeVertexListInGraphDatabase();
		reorganizeAtClient();
	}

	/**
	 * Reorganizes vertex list.
	 */
	private void reorganizeAtClient() {
		try {
			VertexListReorganizer reorganizer = new VertexListReorganizer(
					owningGraph);
			sequenceNumberToIdMap = reorganizer
					.getReorganisedMap(sequenceNumberToIdMap);
			// does this change the vertex IDs? (Apparently it does not)If this
			// is the case, the
			// BitSet has to be altered after this operation.
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void clear() {
		sequenceNumberToIdMap.clear();
		// usedIDs.clear();
	}

	/**
	 * Checks if list contains vertex with given id.
	 * 
	 * @param vId
	 *            id of vertex to check for.
	 * @return true if list contains vertex with given id, otherwise false.
	 */
	protected boolean containsVertex(int vId) {
		return sequenceNumberToIdMap.containsValue(vId);
		// return usedIDs.get(vId);
	}
}
