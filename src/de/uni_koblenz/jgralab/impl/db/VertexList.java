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

import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Global vertex list VSeq of graph.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public class VertexList extends GraphElementList<DatabasePersistableVertex> {

	private GraphImpl owningGraph;

	/**
	 * Sorted collection mapping sequence numbers of vertices onto their primary
	 * key.
	 */
	private TreeMap<Long, Integer> vertexIdMap;

	/**
	 * Creates and initializes a new <code>VertexList</code>.
	 * 
	 * @param graph
	 *            Graph the vertex list belongs to.
	 */
	public VertexList(GraphImpl owningGraph) {
		super();
		this.owningGraph = owningGraph;
		this.vertexIdMap = new TreeMap<Long, Integer>();
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
		this.vertexIdMap.put(sequenceNumber, vId);
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
		if (!this.isEmpty()) {
			return (DatabasePersistableVertex) this.owningGraph.getVertex(this
					.getFirstVertexId());
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
		return this.vertexIdMap.isEmpty();
	}

	private int getFirstVertexId() {
		Entry<Long, Integer> firstEntry = this.vertexIdMap.firstEntry();
		return firstEntry.getValue();
	}

	/**
	 * Gets last vertex of vertex list.
	 * 
	 * @return Last vertex of vertex list, or null if vertex list is empty.
	 */
	@Override
	public DatabasePersistableVertex getLast() {
		if (!this.isEmpty()) {
			return (DatabasePersistableVertex) this.owningGraph.getVertex(this
					.getLastVertexId());
		} else {
			return null;
		}
	}

	private int getLastVertexId() {
		Entry<Long, Integer> firstEntry = this.vertexIdMap.lastEntry();
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
		assert this.contains(vertex);
		if (!this.isFirst(vertex)) {
			int prevVId = this.getPrevVertexId(vertex);
			return (DatabasePersistableVertex) this.owningGraph
					.getVertex(prevVId);
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
		Integer vId = this.vertexIdMap.get(vertex.getSequenceNumberInVSeq());
		if (vId != null) {
			return vId == vertex.getId();
		} else {
			return false;
		}
	}

	private boolean isFirst(DatabasePersistableVertex vertex) {
		if (!this.isEmpty()) {
			return this.equalsFirst(vertex);
		} else {
			return false;
		}
	}

	private boolean equalsFirst(DatabasePersistableVertex vertex) {
		int firstVId = this.getFirstVertexId();
		return firstVId == vertex.getId();
	}

	private int getPrevVertexId(DatabasePersistableVertex vertex) {
		Entry<Long, Integer> previousEntry = this.vertexIdMap.lowerEntry(vertex
				.getSequenceNumberInVSeq());
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
		assert this.contains(vertex);
		if (!this.isLast(vertex)) {
			int nextVId = this.getNextVertexId(vertex);
			return (DatabasePersistableVertex) this.owningGraph
					.getVertex(nextVId);
		} else {
			return null;
		}
	}

	private boolean isLast(DatabasePersistableVertex vertex) {
		if (!this.isEmpty()) {
			return this.equalsLast(vertex);
		} else {
			return false;
		}
	}

	private boolean equalsLast(DatabasePersistableVertex vertex) {
		int lastVId = this.getLastVertexId();
		return lastVId == vertex.getId();
	}

	private int getNextVertexId(DatabasePersistableVertex vertex) {
		Entry<Long, Integer> nextEntry = this.vertexIdMap.higherEntry(vertex
				.getSequenceNumberInVSeq());
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
		if (!this.isFirst(vertex)) {
			this.prependByMoveOrInsert(vertex);
		}
	}

	private void prependByMoveOrInsert(DatabasePersistableVertex vertex) {
		this.assureThatElementCanBeAppended(this.vertexIdMap);
		long sequenceNumber = this
				.getRegularSequenceNumberBeforeFirstElementOf(this.vertexIdMap);
		this.moveOrInsert(vertex, sequenceNumber);
	}

	// TODO Candidate to move to List<T>
	private void moveOrInsert(DatabasePersistableVertex vertex,
			long sequenceNumber) {
		if (this.contains(vertex)) {
			this.moveTo(vertex, sequenceNumber);
		} else {
			this.insertAt(vertex, sequenceNumber);
		}
	}

	/**
	 * Appends a vertex to vertex list.
	 * 
	 * @param vertex
	 *            The vertex to append.
	 */
	@Override
	public void append(DatabasePersistableVertex vertex) {
		if (!this.isLast(vertex)) {
			this.appendByMoveOrInsert(vertex);
		}
	}

	private void appendByMoveOrInsert(DatabasePersistableVertex vertex) {
		this.assureThatElementCanBeAppended(this.vertexIdMap);
		long sequenceNumber = this
				.getRegularSequenceNumberBehindLastElementOf(this.vertexIdMap);
		this.moveOrInsert(vertex, sequenceNumber);
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
		assert this.areBothInSameVertexList(targetVertex, movedVertex);
		assert targetVertex != movedVertex;
		if (!targetVertex.equals(movedVertex)
				&& !this.isNextNeighbour(targetVertex, movedVertex)) {
			this.moveVertexBehind(movedVertex, targetVertex);
		}
	}

	private boolean areBothInSameVertexList(
			DatabasePersistableVertex targetVertex,
			DatabasePersistableVertex movedVertex) {
		return this.contains(targetVertex) && this.contains(movedVertex);
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
		assert this.areBothInSameVertexList(targetVertex, movedVertex);
		assert targetVertex != movedVertex;
		if (!targetVertex.equals(movedVertex)
				&& !this.isPrevNeighbour(targetVertex, movedVertex)) {
			this.moveVertexBefore(movedVertex, targetVertex);
		}
	}

	private boolean isPrevNeighbour(DatabasePersistableVertex vertex,
			DatabasePersistableVertex allegedNeighbourVertex) {
		if (!this.isFirst(vertex)) {
			return this.getPrevVertexId(vertex) == allegedNeighbourVertex
					.getId();
		} else {
			return false;
		}
	}

	private boolean isNextNeighbour(DatabasePersistableVertex vertex,
			DatabasePersistableVertex allegedNeighbourVertex) {
		if (!this.isLast(vertex)) {
			return this.getNextVertexId(vertex) == allegedNeighbourVertex
					.getId();
		} else {
			return false;
		}
	}

	private void moveVertexBefore(DatabasePersistableVertex movedVertex,
			DatabasePersistableVertex targetVertex) {
		long newSequenceNumber = this.getPrevFreeSequenceNumber(targetVertex);
		this.moveTo(movedVertex, newSequenceNumber);
		this.owningGraph.vertexListModified();
	}

	private long getPrevFreeSequenceNumber(DatabasePersistableVertex vertex) {
		assert !isFirst(vertex);
		return this.getPrevFreeSequenceNumber(this.vertexIdMap, vertex
				.getSequenceNumberInVSeq());
	}

	private void moveVertexBehind(DatabasePersistableVertex movedVertex,
			DatabasePersistableVertex targetVertex) {
		long newSequenceNumber = this.getNextFreeSequenceNumber(targetVertex);
		this.moveTo(movedVertex, newSequenceNumber);
		this.owningGraph.vertexListModified();
	}

	private long getNextFreeSequenceNumber(DatabasePersistableVertex vertex) {
		assert !isLast(vertex);
		return this.getNextFreeSequenceNumber(this.vertexIdMap, vertex
				.getSequenceNumberInVSeq());
	}

	private void moveTo(DatabasePersistableVertex vertex, long sequenceNumber) {
		this.vertexIdMap.remove(vertex.getSequenceNumberInVSeq());
		vertex.setSequenceNumberInVSeq(sequenceNumber);
		this.vertexIdMap.put(sequenceNumber, vertex.getId());
	}

	private void insertAt(DatabasePersistableVertex vertex, long sequenceNumber) {
		vertex.setSequenceNumberInVSeq(sequenceNumber);
		this.vertexIdMap.put(sequenceNumber, vertex.getId());
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
		assert this.contains(vertex);
		this.vertexIdMap.remove(vertex.getSequenceNumberInVSeq());
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
		return this.vertexIdMap.size();
	}

	@Override
	public void reorganize() {
		this.owningGraph.reorganizeVertexListInGraphDatabase();
		this.reorganizeAtClient();
	}

	/**
	 * Reorganizes vertex list.
	 */
	private void reorganizeAtClient() {
		try {
			VertexListReorganizer reorganizer = new VertexListReorganizer(
					this.owningGraph);
			this.vertexIdMap = reorganizer.getReorganisedMap(this.vertexIdMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void clear() {
		this.vertexIdMap.clear();
	}

	/**
	 * Checks if list contains vertex with given id.
	 * 
	 * @param vId
	 *            id of vertex to check for.
	 * @return true if list contains vertex with given id, otherwise false.
	 */
	protected boolean containsVertex(int vId) {
		return this.vertexIdMap.containsValue(vId);
	}
}
