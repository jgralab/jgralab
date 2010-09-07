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

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.trans.ListPosition;
import de.uni_koblenz.jgralab.trans.TransactionState;
import de.uni_koblenz.jgralab.trans.VersionedDataObject;
import de.uni_koblenz.jgralab.trans.VersionedIncidence;
import de.uni_koblenz.jgralab.trans.VertexPosition;

/**
 * Executes validation (check for conflicts) for a transaction.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public class ValidationComponent {
	private TransactionImpl transaction;
	private String conflictReason;

	/**
	 * 
	 * @return if validation fails (a conflict has been detected), the reason
	 *         for failing is returned
	 */
	protected String getConflictReason() {
		return conflictReason;
	}

	/**
	 * @param transaction
	 *            the transaction for which the validation should be executed
	 */
	protected ValidationComponent(TransactionImpl transaction) {
		assert (transaction != null);
		this.transaction = transaction;
		this.conflictReason = "";
	}

	/**
	 * 
	 * @return true if a conflict has been detected; false otherwise
	 */
	protected boolean isInConflict() {
		// if new persistent changes have occured since BOT of transaction...
		if (transaction.persistentVersionAtBot < ((GraphImpl) transaction
				.getGraph()).getPersistentVersionCounter()) {
			if (conflictWithinAddedEdges() || conflictWithinDeletedVertices()
					|| conflictWithinDeletedEdges() || conflictWithinVseq()
					|| conflictWithinEseq() || conflictWithinChangedEdges()
					|| conflictWithinChangedIncidences()
					|| conflictWithinChangedAttributes()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether alpha- and omega-vertices of added edges still exist.
	 * 
	 * @return if conflict has been detected
	 */
	private boolean conflictWithinAddedEdges() {
		assert (transaction.getState() == TransactionState.VALIDATING);
		if (transaction.addedEdges != null) {
			// for each added edge...
			for (EdgeImpl edge : transaction.addedEdges) {
				assert (edge.isNormal());
				Vertex alpha = edge.incidentVertex
						.getTemporaryValue(transaction);
				assert (alpha.isValidAlpha(edge));
				// check if alpha-vertex still exists (only relevant if
				// alpha-vertex hasn't been added within current transaction)
				if (!alpha.isValid()
						&& (transaction.addedVertices == null || !transaction.addedVertices
								.contains(alpha))) {
					// alpha-vertex is phantom
					conflictReason = "Cannot add edge " + edge
							+ " , because alpha-vertex " + alpha + "of edge "
							+ edge + " doesn't exist anymore.";
					return true;
				}
				Vertex omega = ((ReversedEdgeImpl) edge.getReversedEdge()).incidentVertex
						.getTemporaryValue(transaction);
				// check if omega-vertex still exists (only relevant if
				// omega-vertex hasn't been added within current transaction)
				assert (omega.isValidOmega(edge));
				if (!omega.isValid()
						&& (transaction.addedVertices == null || !transaction.addedVertices
								.contains(omega))) {
					// omega-vertex is phantom
					conflictReason = "Cannot add edge " + edge
							+ " , because omega-vertex " + omega + " of edge "
							+ edge + " doesn't exist anymore.";
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check whether the deletion of vertices causes lost updates.
	 * 
	 * @return if conflict has been detected
	 */
	private boolean conflictWithinDeletedVertices() {
		assert (transaction.getState() == TransactionState.VALIDATING);
		if (transaction.deletedVertices != null) {
			// for each deleted vertex
			for (VertexImpl vertex : transaction.deletedVertices) {
				assert (transaction.addedVertices == null || !transaction.addedVertices
						.contains(vertex));
				// vertex only relevant if it still exists
				if (vertex.isValid()) {
					// has there been changes to the incidence-list of vertex
					// since BOT?
					if (vertex.incidenceListVersion
							.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						conflictReason = "Vertex " + vertex
								+ " can't be deleted, because its Iseq("
								+ vertex + ") has changed since BOT.";
						return true;
					}
					// has the previous vertex of vertex in Vseq been explicitly
					// changed since BOT?
					if (vertex.prevVertex.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						conflictReason = "Vertex "
								+ vertex
								+ " can't be deleted, because its previous vertex in Vseq has changed since BOT.";
						return true;
					}
					// has the next vertex of vertex in Vseq been explicitly
					// changed since BOT?
					if (vertex.nextVertex.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						conflictReason = "Vertex "
								+ vertex
								+ " can't be deleted, because its next vertex in Vseq has changed since BOT.";
						return true;
					}
					// get references to all attributes for vertex...
					Set<VersionedDataObject<?>> attributes = vertex
							.attributes();
					if (attributes != null) {
						// check for every attribute, if its value has been
						// changed since BOT...
						for (VersionedDataObject<?> attribute : attributes) {
							if (attribute != null) {
								if (attribute.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
									conflictReason = "Vertex "
											+ vertex
											+ " can't be deleted, because its attribute '"
											+ attribute
											+ "' has changed since BOT.";
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check whether the deletion of edges causes lost updates.
	 * 
	 * @return if conflict has been detected
	 */
	private boolean conflictWithinDeletedEdges() {
		assert (transaction.getState() == TransactionState.VALIDATING);
		if (transaction.deletedEdges != null) {
			// for each deleted edge
			for (EdgeImpl edge : transaction.deletedEdges) {
				assert (transaction.addedEdges == null || !transaction.addedEdges
						.contains(edge));
				assert (edge.isNormal());
				// edge only relevant if it still exists
				if (edge.isValid()) {
					// has the alpha-vertex of edge been changed since BOT?
					if (edge.incidentVertex.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						conflictReason = "Edge "
								+ edge
								+ " can't be deleted, because Iseq("
								+ edge.incidentVertex
										.getLatestPersistentValue()
								+ ")  has changed since BOT.";
						return true;
					}
					// has the omega-vertex of edge been changed since BOT?
					if (((ReversedEdgeImpl) edge.getReversedEdge()).incidentVertex
							.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						conflictReason = "Edge "
								+ edge
								+ " can't be deleted, because Iseq( "
								+ ((ReversedEdgeImpl) edge.getReversedEdge()).incidentVertex
										.getLatestPersistentValue()
								+ ") has changed since BOT.";
						return true;
					}
					// has the previous incidence of edge been changed in Iseq
					// of alpha-vertex since BOT?
					if (edge.prevIncidence.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						conflictReason = "Edge "
								+ edge
								+ " can't be deleted, because its previous incidence in Iseq("
								+ edge.incidentVertex
										.getLatestPersistentValue() + ") "
								+ " has changed (explicitly) since BOT.";
						return true;
					}
					// has the next incidence of edge been changed in Iseq of
					// alpha-vertex since BOT?
					if (edge.nextIncidence.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						conflictReason = "Edge "
								+ edge
								+ " can't be deleted, because its next incidence in Iseq("
								+ edge.incidentVertex
										.getLatestPersistentValue() + ") "
								+ " has changed (explicitly) since BOT.";
						return true;
					}
					// has the previous incidence of edge been changed in Iseq
					// of omega-vertex since BOT?
					if (((ReversedEdgeImpl) edge.getReversedEdge()).prevIncidence
							.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						conflictReason = "Edge "
								+ edge
								+ " can't be deleted, because its previous incidence in Iseq("
								+ ((ReversedEdgeImpl) edge.getReversedEdge()).prevIncidence
										.getLatestPersistentValue() + ") "
								+ " has changed (explicitly) since BOT.";
						return true;
					}
					// has the next incidence of edge been changed in Iseq of
					// omega-vertex since BOT?
					if (((ReversedEdgeImpl) edge.getReversedEdge()).nextIncidence
							.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						conflictReason = "Edge "
								+ edge
								+ " can't be deleted, because its next incidence in Iseq("
								+ ((ReversedEdgeImpl) edge.getReversedEdge()).nextIncidence
										.getLatestPersistentValue() + ") "
								+ " has changed (explicitly) since BOT.";
						return true;
					}
					// has the previous edge of edge in Eseq been explicitly
					// changed since BOT?
					if (edge.prevEdge.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						conflictReason = "Edge "
								+ edge
								+ " can't be deleted, because previous edge in Eseq has changed (explicitly) since BOT.";
						return true;
					}
					// has the next edge of edge in Eseq been explicitly
					// changed since BOT?
					if (edge.nextEdge.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						conflictReason = "Edge "
								+ edge
								+ " can't be deleted, because next edge in Eseq has changed (explicitly) since BOT.";
						return true;
					}
					// get references to all attributes for edge...
					Set<VersionedDataObject<?>> attributes = edge.attributes();
					if (attributes != null) {
						// check for every attribute, if its value has been
						// changed since BOT...
						for (VersionedDataObject<?> attribute : attributes) {
							if (attribute != null) {
								if (attribute.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
									conflictReason = "Edge "
											+ edge
											+ " can't be deleted, because its attribute '"
											+ attribute
											+ "' has changed since BOT.";
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check whether explicit changes to Vseq cause conflicts.
	 * 
	 * @return if conflict has been detected.
	 */
	private boolean conflictWithinVseq() {
		GraphImpl graph = (GraphImpl) transaction.getGraph();
		assert (transaction.getState() == TransactionState.VALIDATING);
		// check only, if Vseq has changed since BOT of transaction
		if (graph.vertexListVersion != null) {
			if (graph.vertexListVersion.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
				if (transaction.changedVseqVertices != null) {
					// for every vertex whose previous and/or next vertex has
					// been
					// explicitly changed...
					for (VertexImpl vertex : transaction.changedVseqVertices
							.keySet()) {
						assert (transaction.deletedVertices == null || !transaction.deletedVertices
								.contains(vertex));
						// check if vertex still exists (only relevant if vertex
						// hasn't been added within current transaction)
						if (!vertex.isValid()
								&& (transaction.addedVertices == null || !transaction.addedVertices
										.contains(vertex))) {
							conflictReason = "Can't commit changes in Vseq for vertex "
									+ vertex
									+ ", because "
									+ vertex
									+ " doesn't exist anymore.";
							return true;
						}
						Map<ListPosition, Boolean> positionsMap = transaction.changedVseqVertices
								.get(vertex);
						Set<Entry<ListPosition, Boolean>> positions = positionsMap
								.entrySet();
						for (Entry<ListPosition, Boolean> entry : positions) {
							boolean movedVertex = entry.getValue();
							switch (entry.getKey()) {
							case PREV: {
								// does the previous vertex of vertex still
								// exists?
								if (!vertex.prevVertex.getTemporaryValue(
										transaction).isValid()
										&& (transaction.addedVertices == null || !transaction.addedVertices
												.contains(vertex.prevVertex
														.getTemporaryValue(transaction)))) {
									conflictReason = "Can't commit changes in Vseq for previous vertex of vertex "
											+ vertex
											+ ", because previous vertex "
											+ vertex.prevVertex
													.getTemporaryValue(transaction)
											+ " doesn't exist anymore.";
									return true;
								}
								// lost update for the previous vertex of
								// vertex?
								if (vertex.prevVertex
										.getLatestPersistentVersion() > transaction.persistentVersionAtBot
										&& vertex.prevVertex
												.getTemporaryValue(transaction) != vertex.prevVertex
												.getLatestPersistentValue()) {
									conflictReason = "Can't commit changes in Vseq for previous vertex of vertex "
											+ vertex
											+ ", because a lost update for previous vertex occured.";
									return true;
								}
								if (movedVertex) {
									// if vertex isn't last vertex in current
									// persistent
									// value of Vseq
									if (vertex.nextVertex
											.getLatestPersistentValue() != null) {
										// check whether vertex has been
										// (explicitly)
										// set as previous
										// vertex of another vertex in Vseq
										// since
										// BOT
										if (vertex.nextVertex
												.getLatestPersistentValue().prevVertex
												.getLatestPersistentVersion() > transaction.persistentVersionAtBot
												&& !(transaction.changedVseqVertices
														.containsKey(vertex.nextVertex
																.getLatestPersistentValue())
														&& transaction.changedVseqVertices
																.get(
																		vertex.nextVertex
																				.getLatestPersistentValue())
																.keySet()
																.contains(
																		ListPosition.PREV) && vertex.nextVertex
														.getLatestPersistentValue() == vertex.nextVertex
														.getTemporaryValue(transaction))) {
											conflictReason = "Can't commit change of position of vertex "
													+ vertex
													+ " in Vseq, because "
													+ vertex
													+ " has been explicitly set as previous vertex of "
													+ vertex.nextVertex
															.getLatestPersistentValue()
													+ ".";
											return true;
										}
									}
								}
								break;
							}
							case NEXT: {
								// does the next vertex of vertex still exists?
								if (!vertex.nextVertex.getTemporaryValue(
										transaction).isValid()
										&& (transaction.addedVertices == null || !transaction.addedVertices
												.contains(vertex.nextVertex
														.getTemporaryValue(transaction)))) {
									conflictReason = "Can't commit changes in Vseq for next vertex of vertex "
											+ vertex
											+ ", because next vertex "
											+ vertex.nextVertex
													.getTemporaryValue(transaction)
											+ " doesn't exist anymore.";
									return true;
								}
								// lost update for the next vertex of vertex?
								if (vertex.nextVertex
										.getLatestPersistentVersion() > transaction.persistentVersionAtBot
										&& vertex.nextVertex
												.getTemporaryValue(transaction) != vertex.nextVertex
												.getLatestPersistentValue()) {
									conflictReason = "Can't commit changes in Vseq for next vertex of vertex "
											+ vertex
											+ ", because a lost update for next vertex occured.";
									return true;
								}
								if (movedVertex) {
									// if vertex isn't first vertex in current
									// persistent value of Vseq
									if (vertex.prevVertex
											.getLatestPersistentValue() != null) {
										// check whether vertex has been
										// (explicitly)
										// set as next
										// vertex of another vertex in Vseq
										// since
										// BOT
										if (vertex.prevVertex
												.getLatestPersistentValue().nextVertex
												.getLatestPersistentVersion() > transaction.persistentVersionAtBot
												&& !(transaction.changedVseqVertices
														.containsKey(vertex.prevVertex
																.getLatestPersistentValue())
														&& transaction.changedVseqVertices
																.get(
																		vertex.prevVertex
																				.getLatestPersistentValue())
																.keySet()
																.contains(
																		ListPosition.NEXT) && vertex.prevVertex
														.getLatestPersistentValue() == vertex.prevVertex
														.getTemporaryValue(transaction))) {
											conflictReason = "Can't commit change of position of vertex "
													+ vertex
													+ " in Vseq, because "
													+ vertex
													+ " has been explicitly set as next vertex of "
													+ vertex.prevVertex
															.getLatestPersistentValue()
													+ ".";
											return true;
										}
									}
								}
								break;
							}
							default: {
								throw new GraphException(
										"Literal of ListPosition expected. This should not happen!!!");
							}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check whether explicit changes to Eseq cause conflicts.
	 * 
	 * @return if conflict has been detected.
	 */
	private boolean conflictWithinEseq() {
		GraphImpl graph = (GraphImpl) transaction.getGraph();
		assert (transaction.getState() == TransactionState.VALIDATING);
		// check only, if Eseq has changed since BOT of transaction
		if (graph.edgeListVersion != null) {
			if (graph.edgeListVersion.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
				if (transaction.changedEseqEdges != null)
					// for every edge whose previous and/or next vertex has been
					// explicitly changed...
					for (EdgeImpl edge : transaction.changedEseqEdges.keySet()) {
						assert (transaction.deletedEdges == null || !transaction.deletedEdges
								.contains(edge));
						assert (edge.isNormal());
						// check if edge still exists (only relevant if edge
						// hasn't been added within current transaction)
						if (!edge.isValid()
								&& (transaction.addedEdges == null || !transaction.addedEdges
										.contains(edge))) {
							conflictReason = "Can't commit changes in Eseq for edge "
									+ edge
									+ ", because "
									+ edge
									+ " it doesn't exist anymore.";
							return true;
						}
						Map<ListPosition, Boolean> positionsMap = transaction.changedEseqEdges
								.get(edge);
						Set<Entry<ListPosition, Boolean>> positions = positionsMap
								.entrySet();
						for (Entry<ListPosition, Boolean> entry : positions) {
							boolean movedEdge = entry.getValue();
							switch (entry.getKey()) {
							case PREV: {
								// does the previous edge of edge still exists?
								if (!edge.prevEdge.getTemporaryValue(
										transaction).isValid()
										&& (transaction.addedEdges == null || !transaction.addedEdges
												.contains(edge.prevEdge
														.getTemporaryValue(transaction)))) {
									conflictReason = "Can't commit changes in Eseq for previous edge of edge "
											+ edge
											+ ", because previous edge "
											+ edge.prevEdge
													.getTemporaryValue(transaction)
											+ " doesn't exist anymore.";
									return true;
								}
								// lost update for the previous edge of edge?
								if (edge.prevEdge.getLatestPersistentVersion() > transaction.persistentVersionAtBot
										&& edge.prevEdge
												.getTemporaryValue(transaction) != edge.prevEdge
												.getLatestPersistentValue()) {
									conflictReason = "Can't commit changes in Eseq for previous edge of edge "
											+ edge
											+ ", because a lost update for previous edge occured.";
									return true;
								}
								// if edge has been moved
								if (movedEdge) {
									// if edge isn't last edge in current
									// persistent
									// value of Eseq
									if (edge.nextEdge
											.getLatestPersistentValue() != null) {
										// check whether edge has been
										// (explicitly)
										// set
										// as previous
										// edge of another edge in Eseq since
										// BOT
										if (edge.nextEdge
												.getLatestPersistentValue() != null) {
											if (edge.nextEdge
													.getLatestPersistentValue().prevEdge
													.getLatestPersistentVersion() > transaction.persistentVersionAtBot
													&& !(transaction.changedEseqEdges
															.containsKey(edge.nextEdge
																	.getLatestPersistentValue())
															&& transaction.changedEseqEdges
																	.get(
																			edge.nextEdge
																					.getLatestPersistentValue())
																	.keySet()
																	.contains(
																			ListPosition.PREV) || edge.nextEdge
															.getLatestPersistentValue() == edge.nextEdge
															.getTemporaryValue(transaction))) {
												conflictReason = "Can't commit change of position of edge "
														+ edge
														+ " in Eseq, because "
														+ edge
														+ " has been explicitly set as previous edge of "
														+ edge.nextEdge
																.getLatestPersistentValue()
														+ ".";
												return true;
											}
										}
									}
								}
								break;
							}
							case NEXT: {
								// does the next edge of edge still exists?
								if (!edge.nextEdge.getTemporaryValue(
										transaction).isValid()
										&& (transaction.addedEdges == null || !transaction.addedEdges
												.contains(edge.nextEdge
														.getTemporaryValue(transaction)))) {
									conflictReason = "Can't commit changes in Eseq for next edge of edge "
											+ edge
											+ ", because next edge "
											+ edge.nextEdge
													.getTemporaryValue(transaction)
											+ " doesn't exist anymore.";
									return true;
								}
								// lost update for the next edge of edge?
								if (edge.nextEdge.getLatestPersistentVersion() > transaction.persistentVersionAtBot
										&& edge.nextEdge
												.getTemporaryValue(transaction) != edge.nextEdge
												.getLatestPersistentValue()) {
									conflictReason = "Can't commit changes in Eseq for next edge of edge "
											+ edge
											+ ", because a lost update for next edge occured.";
									return true;
								}
								// if edge has been moved
								if (movedEdge) {
									// if edge isn't first vertex in current
									// persistent value of Eseq
									if (edge.prevEdge
											.getLatestPersistentValue() != null) {
										// check whether edge has been
										// (explicitly)
										// set
										// as next
										// edge of another edge in Eseq since
										// BOT
										if (edge.prevEdge
												.getLatestPersistentValue().nextEdge
												.getLatestPersistentVersion() > transaction.persistentVersionAtBot
												&& !(transaction.changedEseqEdges
														.containsKey(edge.prevEdge
																.getLatestPersistentValue())
														&& transaction.changedEseqEdges
																.get(
																		edge.prevEdge
																				.getLatestPersistentValue())
																.keySet()
																.contains(
																		ListPosition.NEXT) && edge.prevEdge
														.getLatestPersistentValue() == edge.prevEdge
														.getTemporaryValue(transaction))) {
											conflictReason = "Can't commit change of position of edge "
													+ edge
													+ " in Eseq, because "
													+ edge
													+ " has been explicitly set as next vertex of "
													+ edge.nextEdge
															.getLatestPersistentValue()
													+ ".";
											return true;
										}
									}
								}
								break;
							}
							default: {
								throw new GraphException(
										"Literal of ListPosition expected. This should not happen!!!");
							}
							}
						}
					}
			}
		}
		return false;
	}

	/**
	 * Check whether explicit changes to Iseq cause conflicts.
	 * 
	 * @return if conflict has been detected.
	 */
	private boolean conflictWithinChangedIncidences() {
		assert (transaction.getState() == TransactionState.VALIDATING);
		// determine all vertices whose incidence list has been changed
		if (transaction.changedIncidences != null) {
			Set<Entry<VertexImpl, Map<IncidenceImpl, Map<ListPosition, Boolean>>>> vertices = transaction.changedIncidences
					.entrySet();
			for (Entry<VertexImpl, Map<IncidenceImpl, Map<ListPosition, Boolean>>> vertexMap : vertices) {
				VertexImpl vertex = vertexMap.getKey();
				assert (transaction.deletedVertices == null || !transaction.deletedVertices
						.contains(vertex));
				if (!vertex.isValid()
						&& (transaction.addedVertices == null || !transaction.addedVertices
								.contains(vertex))) {
					conflictReason = "Can't commit changes to Iseq(" + vertex
							+ "), because " + vertex
							+ " doesn't exist anymore.";
					return true;
				}
				if ((transaction.addedVertices == null || !transaction.addedVertices
						.contains(vertex))
						&& vertex.incidenceListVersion
								.getLatestPersistentVersion() <= transaction.persistentVersionAtBot)
					continue;
				Map<IncidenceImpl, Map<ListPosition, Boolean>> incidences = vertexMap
						.getValue();
				if (incidences != null) {
					Set<Entry<IncidenceImpl, Map<ListPosition, Boolean>>> incidencesEntrySet = incidences
							.entrySet();
					for (Entry<IncidenceImpl, Map<ListPosition, Boolean>> incidencesMap : incidencesEntrySet) {
						IncidenceImpl incidence = incidencesMap.getKey();
						assert (transaction.deletedEdges == null || !transaction.deletedEdges
								.contains(incidence));
						if ((transaction.addedEdges == null || !transaction.addedEdges
								.contains(incidence.getNormalEdge()))
								&& !incidence.getNormalEdge().isValid()) {
							conflictReason = "Can't commit changes for incidence "
									+ incidence
									+ " in Iseq("
									+ vertex
									+ "), because "
									+ incidence
									+ " doesn't exist anymore.";
							return true;
						}
						VersionedReferenceImpl<IncidenceImpl> prevIncidence = ((VersionedIncidence) incidence)
								.getVersionedPrevIncidence();
						VersionedReferenceImpl<IncidenceImpl> nextIncidence = ((VersionedIncidence) incidence)
								.getVersionedNextIncidence();
						/*
						 * if (incidence instanceof EdgeImpl) { EdgeImpl edge =
						 * (EdgeImpl) incidence; prevIncidence =
						 * edge.prevIncidence; nextIncidence =
						 * edge.nextIncidence; } if (incidence instanceof
						 * ReversedEdgeImpl) { ReversedEdgeImpl revEdge =
						 * (ReversedEdgeImpl) incidence; prevIncidence =
						 * revEdge.prevIncidence; nextIncidence =
						 * revEdge.nextIncidence; }
						 */
						Map<ListPosition, Boolean> positionsMap = incidencesMap
								.getValue();
						for (Entry<ListPosition, Boolean> entry : positionsMap
								.entrySet()) {
							boolean movedIncidence = entry.getValue();
							switch (entry.getKey()) {
							case PREV: {
								if (!prevIncidence.getTemporaryValue(
										transaction).getNormalEdge().isValid()
										&& (transaction.addedEdges == null || transaction.addedEdges
												.contains(prevIncidence
														.getTemporaryValue(
																transaction)
														.getNormalEdge()))) {
									conflictReason = "Can't commit previous incidence "
											+ prevIncidence.getTemporaryValue(
													transaction)
													.getNormalEdge()
											+ " for incidence "
											+ incidence
											+ " in Iseq("
											+ vertex
											+ "), because previous incidence doesn't exist anymore.";
									return true;
								}
								if (prevIncidence.getLatestPersistentVersion() > transaction.persistentVersionAtBot
										&& prevIncidence
												.getTemporaryValue(transaction) != prevIncidence
												.getLatestPersistentValue()) {
									conflictReason = "Can't commit previous incidence "
											+ prevIncidence.getTemporaryValue(
													transaction)
													.getNormalEdge()
											+ " for incidence "
											+ incidence
											+ " in Iseq("
											+ vertex
											+ "), because a lost update occured for previous incidence.";
									return true;
								}
								// if incidence has been moved
								if (movedIncidence) {
									// if incidence isn't last edge in current
									// persistent
									// value of Iseq(vertex)
									if (nextIncidence
											.getLatestPersistentValue() != null) {
										VersionedReferenceImpl<IncidenceImpl> niPrevIncidence = ((VersionedIncidence) nextIncidence
												.getLatestPersistentValue())
												.getVersionedPrevIncidence();
										/*
										 * if (nextIncidence
										 * .getLatestPersistentValue()
										 * instanceof EdgeImpl) { EdgeImpl edge =
										 * (EdgeImpl) nextIncidence
										 * .getLatestPersistentValue();
										 * niPrevIncidence = edge.prevIncidence; }
										 * if (nextIncidence
										 * .getLatestPersistentValue()
										 * instanceof ReversedEdgeImpl) {
										 * ReversedEdgeImpl revEdge =
										 * (ReversedEdgeImpl) nextIncidence
										 * .getLatestPersistentValue();
										 * niPrevIncidence =
										 * revEdge.prevIncidence; }
										 */
										// check whether edge has been
										// (explicitly)
										// set as previous
										// edge of another edge in Eseq since
										// BOT
										if (nextIncidence
												.getLatestPersistentValue() != null) {
											if (niPrevIncidence
													.getLatestPersistentVersion() > transaction.persistentVersionAtBot
													&& !(transaction.changedIncidences
															.containsKey(nextIncidence
																	.getLatestPersistentValue())
															&& transaction.changedIncidences
																	.get(
																			nextIncidence
																					.getLatestPersistentValue())
																	.keySet()
																	.contains(
																			ListPosition.PREV) && nextIncidence
															.getLatestPersistentValue() == nextIncidence
															.getTemporaryValue(transaction))) {
												conflictReason = "Can't commit change of position of incidence "
														+ incidence
														+ " in Iseq( "
														+ vertex
														+ "), because "
														+ incidence
														+ " has been explicitly set as previous incidence of "
														+ nextIncidence
																.getLatestPersistentValue()
														+ ".";
												return true;
											}
										}
									}
								}
								break;
							}
							case NEXT: {
								if (!nextIncidence.getTemporaryValue(
										transaction).getNormalEdge().isValid()
										&& (transaction.addedEdges == null || transaction.addedEdges
												.contains(nextIncidence
														.getTemporaryValue(
																transaction)
														.getNormalEdge()))) {
									conflictReason = "Can't commit next incidence "
											+ nextIncidence.getTemporaryValue(
													transaction)
													.getNormalEdge()
											+ " for incidence "
											+ incidence
											+ " in Iseq("
											+ vertex
											+ "), because next incidence doesn't exist anymore.";
									return true;
								}
								if (nextIncidence.getLatestPersistentVersion() > transaction.persistentVersionAtBot
										&& nextIncidence
												.getTemporaryValue(transaction) != nextIncidence
												.getLatestPersistentValue()) {
									conflictReason = "Can't commit next incidence "
											+ nextIncidence.getTemporaryValue(
													transaction)
													.getNormalEdge()
											+ " for incidence "
											+ incidence
											+ " in Iseq("
											+ vertex
											+ "), because a lost update occured for next incidence.";
									return true;
								}
								// if incidence has been moved
								if (movedIncidence) {
									// if incidence isn't first incidence in
									// current
									// persistent value of Iseq(vertex)
									if (prevIncidence
											.getLatestPersistentValue() != null) {
										VersionedReferenceImpl<IncidenceImpl> piNextIncidence = ((VersionedIncidence) prevIncidence
												.getLatestPersistentValue())
												.getVersionedNextIncidence();
										/*
										 * if (prevIncidence
										 * .getLatestPersistentValue()
										 * instanceof EdgeImpl) { EdgeImpl edge =
										 * (EdgeImpl) prevIncidence
										 * .getLatestPersistentValue();
										 * piNextIncidence = edge.nextIncidence; }
										 * if (prevIncidence
										 * .getLatestPersistentValue()
										 * instanceof ReversedEdgeImpl) {
										 * ReversedEdgeImpl revEdge =
										 * (ReversedEdgeImpl) prevIncidence
										 * .getLatestPersistentValue();
										 * piNextIncidence =
										 * revEdge.nextIncidence; }
										 */
										// check whether edge has been
										// (explicitly)
										// set as next
										// edge of another edge in Eseq since
										// BOT
										if (piNextIncidence
												.getLatestPersistentVersion() > transaction.persistentVersionAtBot
												&& !(transaction.changedIncidences
														.containsKey(prevIncidence
																.getLatestPersistentValue())
														&& transaction.changedIncidences
																.get(
																		prevIncidence
																				.getLatestPersistentValue())
																.keySet()
																.contains(
																		ListPosition.NEXT) && prevIncidence
														.getLatestPersistentValue() == prevIncidence
														.getTemporaryValue(transaction))) {
											conflictReason = "Can't commit change of position of edge "
													+ incidence
													+ " in Eseq, because "
													+ incidence
													+ " has been explicitly set as next incidence of "
													+ prevIncidence
															.getLatestPersistentValue()
													+ ".";
											return true;
										}
									}
								}
								break;
							}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check whether explicit changes to edges cause conflicts.
	 * 
	 * @return if conflict has been detected.
	 */
	private boolean conflictWithinChangedEdges() {
		assert (transaction.getState() == TransactionState.VALIDATING);
		if (transaction.changedEdges != null) {
			Set<Entry<EdgeImpl, VertexPosition>> edges = transaction.changedEdges
					.entrySet();
			// for each edge changed...
			for (Entry<EdgeImpl, VertexPosition> entry : edges) {
				EdgeImpl edge = entry.getKey();
				assert (transaction.deletedEdges == null || !transaction.deletedEdges
						.contains(edge));
				assert (edge.isNormal());
				// does edge still exists (only relevant if edge hasn't been
				// added within current transaction)
				if (!edge.isValid()
						&& (transaction.addedEdges == null || !transaction.addedEdges
								.contains(edge))) {
					conflictReason = "Changes made to edge " + edge
							+ " can't be committed, because " + edge
							+ " doesn't exist anymore";
					return true;
				}
				boolean pass = false;
				switch (entry.getValue()) {
				case ALPHAOMEGA: {
					// both cases following have to be processed
					// intentional fall-through; denounced by FindBugs
					pass = true;
				}
				case ALPHA: {
					// check if alpha-vertex still exists...
					if (!edge.incidentVertex.getTemporaryValue(transaction)
							.isValid()
							&& (transaction.addedVertices == null || !transaction.addedVertices
									.contains(edge.incidentVertex
											.getTemporaryValue(transaction)))) {
						conflictReason = "Can't commit alpha-vertex "
								+ edge.incidentVertex
										.getTemporaryValue(transaction)
								+ " of edge "
								+ edge
								+ ", because alpha-vertex doesn't exist anymore.";
						return true;
					}
					// lost update for alpha-vertex?
					if (edge.incidentVertex.getLatestPersistentVersion() > transaction.persistentVersionAtBot
							&& edge.incidentVertex
									.getTemporaryValue(transaction) != edge.incidentVertex
									.getLatestPersistentValue()) {
						conflictReason = "Can't commit alpha-vertex "
								+ edge.incidentVertex
										.getTemporaryValue(transaction)
								+ " of edge "
								+ edge
								+ ", because a lost update for alpha-vertex occured.";
						return true;
					}
					if (!pass) {
						break;
					}
				}
				case OMEGA: {
					// check if omega-vertex still exists...
					if (!((ReversedEdgeImpl) edge.getReversedEdge()).incidentVertex
							.getTemporaryValue(transaction).isValid()
							&& (transaction.addedVertices == null || !transaction.addedVertices
									.contains(((ReversedEdgeImpl) edge
											.getReversedEdge()).incidentVertex
											.getTemporaryValue(transaction)))) {
						conflictReason = "Can't commit omega-vertex "
								+ ((ReversedEdgeImpl) edge.getReversedEdge()).incidentVertex
										.getTemporaryValue(transaction)
								+ " of edge "
								+ edge
								+ ", because omega-vertex doesn't exist anymore.";
						return true;
					}
					// lost update for omega-vertex?
					if (((ReversedEdgeImpl) edge.getReversedEdge()).incidentVertex
							.getLatestPersistentVersion() > transaction.persistentVersionAtBot
							&& ((ReversedEdgeImpl) edge.getReversedEdge()).incidentVertex
									.getTemporaryValue(transaction) != ((ReversedEdgeImpl) edge
									.getReversedEdge()).incidentVertex
									.getLatestPersistentValue()) {
						conflictReason = "Can't commit omega-vertex "
								+ ((ReversedEdgeImpl) edge.getReversedEdge()).incidentVertex
										.getTemporaryValue(transaction)
								+ " of edge "
								+ edge
								+ ", because a lost update for omega-vertex occured.";
						return true;
					}
					break;
				}
				default: {
					throw new GraphException(
							"Literal of VertexPosition expected. This should not happen!!!");
				}
				}
			}
		}
		return false;
	}

	/**
	 * Check whether explicit changes to attributes cause conflicts.
	 * 
	 * @return if conflict has been detected.
	 */
	private boolean conflictWithinChangedAttributes() {
		if (transaction.changedAttributes != null) {
			Set<Entry<AttributedElement, Set<VersionedDataObject<?>>>> attributedElements = transaction.changedAttributes
					.entrySet();
			// for every AttributedElement for which at least one attribute has
			// been changed...
			for (Entry<AttributedElement, Set<VersionedDataObject<?>>> entry : attributedElements) {
				AttributedElement attributedElement = entry.getKey();
				String type = "";
				// attributedElement == vertex?
				if (attributedElement instanceof Vertex) {
					type = "vertex ";
					VertexImpl vertex = (VertexImpl) attributedElement;
					assert (transaction.deletedVertices == null || !transaction.deletedVertices
							.contains(attributedElement));
					// if vertex has been added within current transaction...
					if (transaction.addedVertices != null
							&& transaction.addedVertices
									.contains(attributedElement))
						continue;
					// does vertex still exists?
					if (!vertex.isValid()) {
						conflictReason = "Can't commit change for attributes of vertex "
								+ vertex
								+ ", because "
								+ vertex
								+ " doesn't exist anymore.";
						return true;
					}
				}
				// attributedElement == edge?
				if (attributedElement instanceof Edge) {
					type = "edge ";
					Edge edge = (Edge) attributedElement;
					assert (transaction.deletedEdges == null || !transaction.deletedEdges
							.contains(attributedElement));
					// if edge has been added within current transaction...
					if (transaction.addedEdges != null
							&& transaction.addedEdges
									.contains(attributedElement))
						continue;
					// does edge still exists?
					if (!edge.getNormalEdge().isValid()) {
						conflictReason = "Can't commit change for attributes of edge "
								+ edge
								+ ", because "
								+ edge
								+ " doesn't exist anymore.";
						return true;
					}
				}
				Set<VersionedDataObject<?>> attributes = entry.getValue();
				// for each changed attribute...
				for (VersionedDataObject<?> attribute : attributes) {
					// check if lost update occured...
					if (attribute.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
						boolean conflict = isAttributeInConflict(attribute);
						if (conflict) {
							conflictReason = "A lost update has been detected for the attribute "
									+ attribute
									+ " of "
									+ type
									+ attributedElement + ".";
							return true;
						}
					}
				}
			}
		}
		// also check all versioned dataobjects which were changed within
		// the transaction without the usage of setter
		Set<VersionedDataObject<?>> versionedDataObjects = transaction
				.getRemainingVersionedDataObjects();
		for (VersionedDataObject<?> vdo : versionedDataObjects) {
			if (vdo.isCloneable() || vdo.isPartOfRecord()) {
				boolean conflict = isAttributeInConflict(vdo);
				if (conflict) {
					conflictReason = "A lost update has been detected for the versioned dataobject "
							+ vdo + ".";
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param attribute
	 * @return
	 */
	private boolean isAttributeInConflict(VersionedDataObject<?> attribute) {
		if (attribute.getLatestPersistentVersion() > transaction.persistentVersionAtBot) {
			Object temporaryValue = attribute.getTemporaryValue(transaction);
			if (temporaryValue == null
					&& attribute.getLatestPersistentValue() == null)
				return false;
			if (temporaryValue == null
					&& attribute.getLatestPersistentValue() != null)
				return true;
			if (!attribute.getTemporaryValue(transaction).equals(
					attribute.getLatestPersistentValue()))
				return true;
		}
		return false;
	}
}
