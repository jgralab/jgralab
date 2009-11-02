package de.uni_koblenz.jgralab.impl.trans;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.trans.ListPosition;
import de.uni_koblenz.jgralab.trans.TransactionState;
import de.uni_koblenz.jgralab.trans.VersionedDataObject;
import de.uni_koblenz.jgralab.trans.VertexPosition;

/**
 * Executes writing phase for a transaction. At most one transaction should be
 * executing write() at the same time. No other transaction should be doing BOT
 * or validation.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public class WritingComponent {
	private TransactionImpl transaction;
	private GraphImpl graph;

	/**
	 * @param transaction
	 */
	protected WritingComponent(TransactionImpl transaction) {
		assert (transaction != null);
		this.transaction = transaction;
		graph = (GraphImpl) transaction.getGraph();
	}

	/**
	 * Commented out the "garbage collection", if exception occurs during
	 * writing-process. Exception should not happen anyway. Exception handling
	 * with "garbage collection" would include changes to
	 * VersionedDataObjectImpl with higher memory usage (use of persistentValue
	 * wouldn't be possible).
	 * 
	 * @throws Exception
	 */
	protected void write() throws Exception {
		assert (transaction.getState() == TransactionState.WRITING);
		// try {
		// if at least one vertex is to be added or deleted...
		if (((transaction.addedVertices != null && transaction.addedVertices
				.size() > 0) || (transaction.deletedVertices != null && transaction.deletedVertices
				.size() > 0))
				&& graph.vertex.isLatestPersistentValueReferenced()) {
			// important to synchronize here!!!
			graph.vertexSync.readLock().lock();
			// create new persistent value for vertex...
			VertexImpl[] lastVertex = graph.vertex.getLatestPersistentValue();
			graph.vertex.setValidValue(lastVertex.clone(), transaction);
			graph.vertexSync.readLock().unlock();
		}
		// if at least one edge is to be added or deleted...
		if (((transaction.addedEdges != null && transaction.addedEdges.size() > 0) || (transaction.deletedEdges != null && transaction.deletedEdges
				.size() > 0))
				// looking for graph.edge is sufficient, because edge and
				// revEdge are one unit..
				&& graph.edge.isLatestPersistentValueReferenced()) {
			// important to synchronize here!!!
			graph.edgeSync.readLock().lock();
			// create new persistent value for edge and revEdge...
			EdgeImpl[] lastEdge = graph.edge.getLatestPersistentValue();
			graph.edge.setValidValue(lastEdge.clone(), transaction);
			ReversedEdgeImpl[] lastRevEdge = graph.revEdge
					.getLatestPersistentValue();
			graph.revEdge.setValidValue(lastRevEdge.clone(), transaction);
			graph.edgeSync.readLock().unlock();
		}
		deleteVertices();
		deleteEdges();
		addVertices();
		addEdges();
		changeVseq();
		changeEseq();
		changeEdges();
		changeIseq();
		changeAttributes();
		transaction.removeNonReferencedPersistentValues();
		/*
		 * } catch (Exception e) { // if for any reason an exception occurs
		 * during writing-phase, then // all created new persistent versions
		 * created until the occurrence // of the exception should be removed.
		 * This should normally not // happen!!! e.printStackTrace(); if
		 * (transaction.changedDuringCommit != null) { for
		 * (VersionedDataObjectImpl<?> versionedDataObject :
		 * transaction.changedDuringCommit) {
		 * versionedDataObject.removeLastCreatedPersistentValue(); }
		 * transaction.changedDuringCommit.clear(); } throw e; }
		 */
	}

	/**
	 * Delete vertices.
	 * 
	 * @throws Exception
	 */
	private void deleteVertices() throws Exception {
		// graph.vertexSync.writeLock().lock();
		// delete vertices
		if (transaction.deletedVertices != null) {
			for (VertexImpl vertex : transaction.deletedVertices) {
				if (transaction.deletedVerticesWhileWriting == null
						|| !transaction.deletedVerticesWhileWriting
								.contains(vertex)) {
					assert (transaction.addedVertices == null || !transaction.addedVertices
							.contains(vertex));
					// delete current vertex
					vertex.delete();
				}
			}
			transaction.deletedVerticesWhileWriting = null;
			if (transaction.deletedVertices.size() > 0)
				transaction.changedDuringCommit.add(graph.vertex);
		}
		// graph.vertexSync.writeLock().unlock();
	}

	/**
	 * Delete edges.
	 * 
	 * @throws Exception
	 */
	private void deleteEdges() throws Exception {
		// graph.edgeSync.writeLock().lock();
		// delete edges
		if (transaction.deletedEdges != null) {
			for (EdgeImpl edge : transaction.deletedEdges) {
				assert (edge.isNormal() && (transaction.addedEdges == null || !transaction.addedEdges
						.contains(edge)));
				// only delete if still present in graph; could be that
				// <code>edge</code> has already been deleted within
				// deleteVertices()
				if (edge.isValid())
					edge.delete();
			}
			if (transaction.deletedEdges.size() > 0)
				transaction.changedDuringCommit.add(graph.edge);
		}
		// graph.edgeSync.writeLock().unlock();
	}

	/**
	 * Add new vertices.
	 * 
	 * @throws Exception
	 */
	private void addVertices() throws Exception {
		// graph.vertexSync.writeLock().lock();
		// add vertices
		if (transaction.addedVertices != null) {
			for (VertexImpl vertex : transaction.addedVertices) {
				assert (transaction.deletedVertices == null || !transaction.deletedVertices
						.contains(vertex));
				// add current vertex to graph
				graph.addVertex(vertex);
			}
			if (transaction.addedVertices.size() > 0)
				transaction.changedDuringCommit.add(graph.vertex);
		}
		// graph.vertexSync.writeLock().unlock();
	}

	/**
	 * Add new edges.
	 * 
	 * @throws Exception
	 */
	private void addEdges() throws Exception {
		// graph.edgeSync.writeLock().lock();
		// add edges
		if (transaction.addedEdges != null) {
			for (EdgeImpl edge : transaction.addedEdges) {
				assert (edge.isNormal() && (transaction.deletedEdges == null || !transaction.deletedEdges
						.contains(edge)));
				// alpha and omega should be the last temporary value within
				// the current transaction
				Vertex tempAlpha = edge.incidentVertex
						.getTemporaryValue(transaction);
				Vertex tempOmega = ((ReversedEdgeImpl) edge.getReversedEdge()).incidentVertex
						.getTemporaryValue(transaction);
				graph.addEdge(edge, tempAlpha, tempOmega);
			}
			if (transaction.addedEdges.size() > 0)
				transaction.changedDuringCommit.add(graph.edge);
		}
		// graph.edgeSync.writeLock().unlock();
	}

	/**
	 * Commit changes made to Vseq.
	 * 
	 * @throws Exception
	 */
	private void changeVseq() throws Exception {
		// changes for Vseq
		if (transaction.changedVseqVertices != null) {
			Set<Entry<VertexImpl, Map<ListPosition, Boolean>>> vertices = transaction.changedVseqVertices
					.entrySet();
			if (vertices != null) {
				for (Entry<VertexImpl, Map<ListPosition, Boolean>> entry : vertices) {
					VertexImpl vertex = entry.getKey();
					// get positions changed for current vertex
					Map<ListPosition, Boolean> positionsMap = entry.getValue();
					Set<Entry<ListPosition, Boolean>> positions = positionsMap
							.entrySet();
					for (Entry<ListPosition, Boolean> listPositionEntry : positions) {
						// has the vertex been moved?
						boolean movedVertex = listPositionEntry.getValue();
						switch (listPositionEntry.getKey()) {
						case PREV: {
							// temporary previous vertex of vertex in Vseq for
							// current transaction
							VertexImpl tempPrevVertex = vertex.prevVertex
									.getTemporaryValue(transaction);
							if (movedVertex)
								vertex.putAfter(tempPrevVertex);
							else
								tempPrevVertex.putBefore(vertex);
							break;
						}
						case NEXT: {
							// temporary next vertex of vertex in Vseq for
							// current transaction
							VertexImpl tempNextVertex = vertex.nextVertex
									.getTemporaryValue(transaction);
							if (movedVertex)
								vertex.putBefore(tempNextVertex);
							else
								tempNextVertex.putAfter(vertex);
							break;
						}
						default: {
							throw new GraphException(
									"Literal of ListPosition expected. This should not happen!");
						}
						}

					}
				}
			}
		}
	}

	/**
	 * Commit changes made to Eseq.
	 * 
	 * @throws Exception
	 */
	private void changeEseq() throws Exception {
		// changes for Eseq
		if (transaction.changedEseqEdges != null) {
			Set<Entry<EdgeImpl, Map<ListPosition, Boolean>>> edges = transaction.changedEseqEdges
					.entrySet();
			for (Entry<EdgeImpl, Map<ListPosition, Boolean>> entry : edges) {
				EdgeImpl edge = entry.getKey();
				// get positions changed for current edge
				Map<ListPosition, Boolean> positionsMap = entry.getValue();
				Set<Entry<ListPosition, Boolean>> positions = positionsMap
						.entrySet();
				for (Entry<ListPosition, Boolean> listPositionEntry : positions) {
					// has the edge been moved?
					boolean movedEdge = listPositionEntry.getValue();
					switch (listPositionEntry.getKey()) {
					case PREV: {
						// temporary previous edge of edge in Eseq for current
						// transaction
						EdgeImpl tempPrevEdge = edge.prevEdge
								.getTemporaryValue(transaction);
						if (movedEdge)
							edge.putAfterInGraph(tempPrevEdge);
						else
							tempPrevEdge.putBeforeInGraph(edge);
						break;
					}
					case NEXT: {
						// temporary previous edge of edge in Eseq for current
						// transaction
						EdgeImpl tempNextEdge = edge.nextEdge
								.getTemporaryValue(transaction);
						if (movedEdge)
							edge.putBeforeInGraph(tempNextEdge);
						else
							tempNextEdge.putAfterInGraph(edge);
						break;
					}
					default: {
						throw new GraphException(
								"Literal of ListPosition expected. This should not happen!");
					}
					}
				}
			}
		}
	}

	/**
	 * Commit alpha- and omega-changes made to edges.
	 * 
	 * @throws Exception
	 */
	private void changeEdges() throws Exception {
		// changes for Eseq
		if (transaction.changedEdges != null) {
			Set<Entry<EdgeImpl, VertexPosition>> edges = transaction.changedEdges
					.entrySet();
			for (Entry<EdgeImpl, VertexPosition> entry : edges) {
				EdgeImpl edge = entry.getKey();
				assert (edge.isNormal());
				boolean pass = false;
				switch (entry.getValue()) {
				case ALPHAOMEGA: {
					pass = true;
				}
				case ALPHA: {
					// temporary alpha of edge for current transaction
					Vertex tempAlpha = edge.incidentVertex
							.getTemporaryValue(transaction);
					edge.setAlpha(tempAlpha);
					if (!pass)
						break;
				}
				case OMEGA: {
					// temporary omega of edge for current transaction
					Vertex tempOmega = ((ReversedEdgeImpl) edge
							.getReversedEdge()).incidentVertex
							.getTemporaryValue(transaction);
					edge.setOmega(tempOmega);
					break;
				}
				default: {
					throw new GraphException(
							"Literal of VertexPosition expected. This should not happen!");
				}
				}
			}
		}
	}

	/**
	 * Commit changes made to Iseq.
	 * 
	 * @throws Exception
	 */
	private void changeIseq() throws Exception {
		// changes for Iseq
		if (transaction.changedIncidences != null) {
			Set<Entry<VertexImpl, Map<IncidenceImpl, Map<ListPosition, Boolean>>>> vertices = transaction.changedIncidences
					.entrySet();
			for (Entry<VertexImpl, Map<IncidenceImpl, Map<ListPosition, Boolean>>> entry : vertices) {
				// VertexImpl vertex = entry.getKey();
				Map<IncidenceImpl, Map<ListPosition, Boolean>> incidences = entry
						.getValue();
				for (Entry<IncidenceImpl, Map<ListPosition, Boolean>> incidenceEntry : incidences
						.entrySet()) {
					IncidenceImpl incidence = incidenceEntry.getKey();
					// need references to versioned previous and next
					// incidence...
					VersionedReferenceImpl<IncidenceImpl> prevIncidence = null;
					VersionedReferenceImpl<IncidenceImpl> nextIncidence = null;
					// but concrete type of incidence (EdgeImpl or
					// ReversedEdgeImpl) must be determined first
					if (incidence instanceof EdgeImpl) {
						EdgeImpl edge = (EdgeImpl) incidence;
						prevIncidence = edge.prevIncidence;
						nextIncidence = edge.nextIncidence;
					}
					if (incidence instanceof ReversedEdgeImpl) {
						ReversedEdgeImpl revEdge = (ReversedEdgeImpl) incidence;
						prevIncidence = revEdge.prevIncidence;
						nextIncidence = revEdge.nextIncidence;
					}
					// get positions changed for current incidence
					Map<ListPosition, Boolean> positionsMap = incidenceEntry
							.getValue();
					// boolean pass = false;
					Set<Entry<ListPosition, Boolean>> positions = positionsMap
							.entrySet();
					for (Entry<ListPosition, Boolean> listPositionEntry : positions) {
						// has the incidence been moved?
						boolean movedIncidence = listPositionEntry.getValue();
						switch (listPositionEntry.getKey()) {
						case PREV: {
							IncidenceImpl tempPrevIncidence = prevIncidence
									.getTemporaryValue(transaction);
							if (movedIncidence)
								incidence.putEdgeAfter(tempPrevIncidence);
							else
								tempPrevIncidence.putEdgeBefore(incidence);
							break;
						}
						case NEXT: {
							IncidenceImpl tempNextIncidence = nextIncidence
									.getTemporaryValue(transaction);
							if (movedIncidence)
								incidence.putEdgeBefore(tempNextIncidence);
							else
								tempNextIncidence.putEdgeAfter(incidence);
							break;
						}
						default: {
							throw new GraphException(
									"Literal of ListPosition expected. This should not happen!");
						}
						}
					}
				}
			}
		}
	}

	/**
	 * Commit changes made to attributes.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void changeAttributes() throws Exception {
		if (transaction.changedAttributes != null) {
			Set<Entry<AttributedElement, Set<VersionedDataObject<?>>>> elements = transaction.changedAttributes
					.entrySet();
			for (Entry<AttributedElement, Set<VersionedDataObject<?>>> entry : elements) {
				// AttributedElement attributedElement = entry.getKey();
				// TODO how can the unchecked usage of VersionedDataObject can
				// be avoided (using <?> doesn't work)
				Set<VersionedDataObject<?>> attributes = entry.getValue();
				for (VersionedDataObject<?> attribute : attributes) {
					// the temporary value of attribute for current transaction
					Object tempValue = attribute.getTemporaryValue(transaction);
					// create new persistent value
					// if (((VersionedDataObjectImpl) attribute)
					// .isLatestPersistentValueReferenced())
					// attribute.setNewPersistentValue(tempValue, true);
					// else
					// attribute.setPersistentValue(tempValue);
					((VersionedDataObjectImpl) attribute).setValidValue(
							tempValue, graph.getCurrentTransaction(), true);
				}
			}
		}
		// also write persistent values for remaining versioned dataobjects
		// TODO this doesn't seem to work correctly
		Set<VersionedDataObject<?>> versionedDataObjects = transaction
				.getRemainingVersionedDataObjects();
		for (VersionedDataObject<?> vdo : versionedDataObjects) {
			if (((transaction.changedDuringCommit == null || (transaction.changedDuringCommit != null && !transaction.changedDuringCommit
					.contains(vdo))) && (vdo.isCloneable() || vdo
					.isPartOfRecord()))) {
				Object tempValue = vdo.getTemporaryValue(transaction);
				((VersionedDataObjectImpl) vdo).setValidValue(tempValue, graph
						.getCurrentTransaction(), true);
			}
		}
	}
}
