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
package de.uni_koblenz.jgralab.impl.trans;

import java.util.Map;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.EdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;
import de.uni_koblenz.jgralab.trans.ListPosition;
import de.uni_koblenz.jgralab.trans.TransactionState;
import de.uni_koblenz.jgralab.trans.VersionedIncidence;

/**
 * Implementation of <code>ReversedEdgeImpl</code> with versioning.
 * 
 * Incident vertex and next and previous incidence in Iseq(incident vertex) are
 * versioned.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class ReversedEdgeImpl extends
		de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl implements
		VersionedIncidence {
	// attributes inherited from <code>IncidenceImpl</code>
	protected VersionedReferenceImpl<VertexBaseImpl> incidentVertex;
	protected VersionedReferenceImpl<IncidenceImpl> nextIncidence;
	protected VersionedReferenceImpl<IncidenceImpl> prevIncidence;

	/**
	 * Initialization of versioned attributes is avoided here, to not have
	 * persistent and temporary values for new instances within the transaction
	 * this instance is created in.
	 * 
	 * @param normalEdge
	 * @param graph
	 */
	protected ReversedEdgeImpl(EdgeBaseImpl normalEdge, Graph graph) {
		super(normalEdge, graph);
	}

	// --- getter ---//

	@Override
	public InternalVertex getIncidentVertex() {
		if (incidentVertex == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return incidentVertex.getValidValue(graph.getCurrentTransaction());
	}

	@Override
	public IncidenceImpl getNextBaseIncidence() {
		if (nextIncidence == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return nextIncidence.getValidValue(graph.getCurrentTransaction());
	}

	@Override
	public IncidenceImpl getPrevBaseIncidence() {
		if (prevIncidence == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return prevIncidence.getValidValue(graph.getCurrentTransaction());
	}

	// --- setter --- //
	@Override
	public void setIncidentVertex(Vertex v) {
		if (graph.isLoading()) {
			incidentVertex = new VersionedReferenceImpl<VertexBaseImpl>(
					normalEdge, (VertexBaseImpl) v);
		} else {
			// initialize here
			if (incidentVertex == null) {
				incidentVertex = new VersionedReferenceImpl<VertexBaseImpl>(
						normalEdge);
			}
			incidentVertex.setValidValue((VertexBaseImpl) v, graph
					.getCurrentTransaction());
		}
	}

	@Override
	public void setNextIncidenceInternal(InternalEdge nextIncidence) {
		if (graph.isLoading()) {
			this.nextIncidence = new VersionedReferenceImpl<IncidenceImpl>(
					normalEdge, (IncidenceImpl) nextIncidence);
		} else {
			TransactionImpl transaction = (TransactionImpl) graph
					.getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			boolean explicitChange = false;
			// check if next incidence has been changed explicitly or not - only
			// relevant in writing-phase
			if (transaction.getState() == TransactionState.WRITING) {
				if (transaction.changedIncidences != null) {
					InternalVertex currentIncidentVertex = incidentVertex
							.getTemporaryValue(transaction);
					Map<IncidenceImpl, Map<ListPosition, Boolean>> incidenceList = transaction.changedIncidences
							.get(currentIncidentVertex);
					explicitChange = false;
					if (incidenceList != null) {
						explicitChange = incidenceList.containsKey(this)
								&& (incidenceList.get(this).keySet()
										.contains(ListPosition.NEXT));
					}
				}
			}
			// initialize here
			if (this.nextIncidence == null) {
				this.nextIncidence = new VersionedReferenceImpl<IncidenceImpl>(
						normalEdge);
			}
			this.nextIncidence.setValidValue((IncidenceImpl) nextIncidence,
					transaction, explicitChange);
		}
	}

	@Override
	public void setPrevIncidenceInternal(InternalEdge prevIncidence) {
		if (graph.isLoading()) {
			this.prevIncidence = new VersionedReferenceImpl<IncidenceImpl>(
					normalEdge, (IncidenceImpl) prevIncidence);
		} else {
			TransactionImpl transaction = (TransactionImpl) graph
					.getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			boolean explicitChange = false;
			// check if previous incidence has been changed explicitly or not -
			// only relevant in writing-phase
			if (transaction.getState() == TransactionState.WRITING) {
				if (transaction.changedIncidences != null) {
					InternalVertex currentIncidentVertex = incidentVertex
							.getTemporaryValue(transaction);
					Map<IncidenceImpl, Map<ListPosition, Boolean>> incidenceList = transaction.changedIncidences
							.get(currentIncidentVertex);
					explicitChange = false;
					if (incidenceList != null) {
						explicitChange = incidenceList.containsKey(this)
								&& (incidenceList.get(this).keySet()
										.contains(ListPosition.PREV));
					}
				}
			}
			// initialize here
			if (this.prevIncidence == null) {
				this.prevIncidence = new VersionedReferenceImpl<IncidenceImpl>(
						normalEdge);
			}
			this.prevIncidence.setValidValue((IncidenceImpl) prevIncidence,
					transaction, explicitChange);
		}
	}

	@Override
	public VersionedReferenceImpl<IncidenceImpl> getVersionedNextIncidence() {
		return nextIncidence;
	}

	@Override
	public VersionedReferenceImpl<IncidenceImpl> getVersionedPrevIncidence() {
		return prevIncidence;
	}

	// @Override
	// public String toString() {
	// return "-e" + normalEdge.getId() + ": "
	// + getAttributedElementClass().getQualifiedName();
	// }
}
