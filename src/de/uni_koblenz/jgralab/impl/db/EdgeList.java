package de.uni_koblenz.jgralab.impl.db;

import java.util.TreeMap;
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
	 * Sorted collection mapping sequence numbers of edges onto their primary
	 * key.
	 */
	private TreeMap<Long, Integer> edgeIdMap;

	/**
	 * Creates and initializes a new <code>EdgeList</code>.
	 * 
	 * @param graph
	 *            Graph this edge list belongs to.
	 */
	public EdgeList(GraphImpl graph) {
		super();
		this.owningGraph = graph;
		this.edgeIdMap = new TreeMap<Long, Integer>();
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
			if (graph.isEdgeCached(id))
				updateCachedEdge(id, sequenceNumber);
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
		this.edgeIdMap.put(sequenceNumber, eId);
	}

	/**
	 * Gets first edge in edge list.
	 * 
	 * @returns First edge in edge list or null if edge list is empty.
	 */
	@Override
	public DatabasePersistableEdge getFirst() {
		if (!this.isEmpty())
			return (DatabasePersistableEdge) this.owningGraph.getEdge(this
					.getFirstEdgeId());
		else
			return null;
	}

	private int getFirstEdgeId() {
		Entry<Long, Integer> firstEntry = this.edgeIdMap.firstEntry();
		return firstEntry.getValue();
	}

	/**
	 * Checks whether edge list is empty.
	 * 
	 * @return true if edge list is empty, false otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return this.edgeIdMap.isEmpty();
	}

	/**
	 * Gets last edge in edge list.
	 * 
	 * @returns Last edge in edge list or null if edge list is empty.
	 */
	@Override
	public DatabasePersistableEdge getLast() {
		if (!this.isEmpty())
			return (DatabasePersistableEdge) this.owningGraph.getEdge(this
					.getLastEdgeId());
		else
			return null;
	}

	private int getLastEdgeId() {
		Entry<Long, Integer> firstEntry = this.edgeIdMap.lastEntry();
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
		assert this.contains(edge);
		if (!this.isFirst(edge)) {
			int prevEId = this.getPrevEdgeId(edge);
			return (DatabasePersistableEdge) this.owningGraph.getEdge(prevEId);
		} else
			return null;
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
		Integer eId = this.edgeIdMap.get(edge.getSequenceNumberInESeq());
		if (eId != null)
			return eId == Math.abs(edge.getId());
		else
			return false;
	}

	private boolean isFirst(DatabasePersistableEdge edge) {
		if (!this.isEmpty())
			return this.equalsFirst(edge);
		else
			return false;
	}

	private boolean equalsFirst(DatabasePersistableEdge edge) {
		int firstEId = this.getFirstEdgeId();
		return firstEId == edge.getId();
	}

	private int getPrevEdgeId(DatabasePersistableEdge edge) {
		Entry<Long, Integer> previousEntry = this.edgeIdMap.lowerEntry(edge
				.getSequenceNumberInESeq());
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
		assert this.contains(edge);
		if (!this.isLast(edge)) {
			int nextEId = this.getNextEdgeId(edge);
			return (DatabasePersistableEdge) this.owningGraph.getEdge(nextEId);
		} else
			return null;
	}

	private boolean isLast(DatabasePersistableEdge edge) {
		if (!this.isEmpty())
			return this.equalsLast(edge);
		else
			return false;
	}

	private boolean equalsLast(DatabasePersistableEdge edge) {
		int lastEId = this.getLastEdgeId();
		return lastEId == edge.getId();
	}

	private int getNextEdgeId(DatabasePersistableEdge edge) {
		Entry<Long, Integer> nextEntry = this.edgeIdMap.higherEntry(edge
				.getSequenceNumberInESeq());
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
		if (!this.isFirst(edge))
			this.prependByMoveOrInsert(edge);
	}

	private void prependByMoveOrInsert(DatabasePersistableEdge edge) {
		this.assureThatElementCanBePrepended(this.edgeIdMap);
		long sequenceNumber = this
				.getRegularSequenceNumberBeforeFirstElementOf(this.edgeIdMap);
		this.moveOrInsert(edge, sequenceNumber);
	}

	private void moveOrInsert(DatabasePersistableEdge edge, long sequenceNumber) {
		if (this.contains(edge))
			this.moveTo(edge, sequenceNumber);
		else
			this.insertAt(edge, sequenceNumber);
	}

	private void moveTo(DatabasePersistableEdge edge, long sequenceNumber) {
		assert this.contains(edge);
		this.edgeIdMap.remove(edge.getSequenceNumberInESeq());
		edge.setSequenceNumberInESeq(sequenceNumber);
		this.edgeIdMap.put(sequenceNumber, edge.getId());
	}

	private void insertAt(DatabasePersistableEdge edge, long sequenceNumber) {
		assert !this.contains(edge);
		edge.setSequenceNumberInESeq(sequenceNumber);
		this.edgeIdMap.put(sequenceNumber, edge.getId());
	}

	/**
	 * Appends an edge to edge list.
	 * 
	 * @param edge
	 *            The edge to append to edge list.
	 */
	@Override
	public void append(DatabasePersistableEdge edge) {
		if (!edge.isNormal())
			edge = (DatabasePersistableEdge) edge.getNormalEdge();
		if (!this.isLast(edge))
			this.appendByMoveOrInsert(edge);
	}

	private void appendByMoveOrInsert(DatabasePersistableEdge edge) {
		this.assureThatElementCanBeAppended(this.edgeIdMap);
		long sequenceNumber = this
				.getRegularSequenceNumberBehindLastElementOf(this.edgeIdMap);
		this.moveOrInsert(edge, sequenceNumber);
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
		assert this.areBothInSameEdgeList(targetEdge, movedEdge);
		if (targetEdge != movedEdge
				&& !this.isPrevNeighbour(targetEdge, movedEdge))
			this.moveEdgeBefore(movedEdge, targetEdge);
	}

	private boolean areBothInSameEdgeList(DatabasePersistableEdge targetEdge,
			DatabasePersistableEdge movedEdge) {
		return this.contains(targetEdge) && this.contains(movedEdge);
	}

	private boolean isPrevNeighbour(DatabasePersistableEdge edge,
			DatabasePersistableEdge allegedNeighbourEdge) {
		if (!this.isFirst(edge))
			return this.getPrevEdgeId(edge) == allegedNeighbourEdge.getId();
		else
			return false;
	}

	private void moveEdgeBefore(DatabasePersistableEdge movedEdge,
			DatabasePersistableEdge targetEdge) {
		long newSequenceNumber = this.getPrevFreeSequenceNumber(targetEdge);
		this.moveTo(movedEdge, newSequenceNumber);
		this.owningGraph.edgeListModified();
	}

	private long getPrevFreeSequenceNumber(DatabasePersistableEdge edge) {
		assert !isFirst(edge);
		return this.getPrevFreeSequenceNumber(this.edgeIdMap, edge
				.getSequenceNumberInESeq());
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
		assert this.areBothInSameEdgeList(targetEdge, movedEdge);
		assert targetEdge != movedEdge;
		if (targetEdge != movedEdge
				&& !this.isNextNeighbour(targetEdge, movedEdge))
			this.moveEdgeBehind(movedEdge, targetEdge);
	}

	private boolean isNextNeighbour(DatabasePersistableEdge edge,
			DatabasePersistableEdge allegedNeighbourEdge) {
		if (!this.isLast(edge))
			return this.getNextEdgeId(edge) == allegedNeighbourEdge.getId();
		else
			return false;
	}

	private void moveEdgeBehind(DatabasePersistableEdge movedEdge,
			DatabasePersistableEdge targetEdge) {
		long newSequenceNumber = this.getNextFreeSequenceNumber(targetEdge);
		this.moveTo(movedEdge, newSequenceNumber);
		this.owningGraph.edgeListModified();
	}

	private long getNextFreeSequenceNumber(DatabasePersistableEdge edge) {
		assert !isLast(edge);
		return this.getNextFreeSequenceNumber(this.edgeIdMap, edge
				.getSequenceNumberInESeq());
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
		assert this.contains(edge);
		this.edgeIdMap.remove(edge.getSequenceNumberInESeq());
	}

	@Override
	public int size() {
		return this.edgeIdMap.size();
	}

	@Override
	protected void reorganize() {
		this.reorganizeInMemory();
		this.owningGraph.reorganizeEdgeListInGraphDatabase();
	}

	private void reorganizeInMemory() {
		try {
			EdgeListReorganizer reorganizer = new EdgeListReorganizer(this.owningGraph);
			this.edgeIdMap = reorganizer.getReorganisedMap(this.edgeIdMap);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Override
	protected void clear() {
		this.edgeIdMap.clear();
	}

	/**
	 * Checks if an edge with given id is part of ESeq.
	 * @param eId Id of edge
	 * @return true if an edge with given id is part of ESeq, otherwise false.
	 */
	protected boolean containsEdge(int eId) {
		return this.edgeIdMap.containsValue(Math.abs(eId));
	}
}
