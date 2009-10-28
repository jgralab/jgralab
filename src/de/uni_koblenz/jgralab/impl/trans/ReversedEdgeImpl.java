package de.uni_koblenz.jgralab.impl.trans;

import java.util.Map;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.impl.EdgeImpl;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.VertexImpl;
import de.uni_koblenz.jgralab.trans.ListPosition;
import de.uni_koblenz.jgralab.trans.TransactionState;
import de.uni_koblenz.jgralab.trans.VersionedIncidence;

/**
 * Implementation of <code>ReversedEdgeImpl</code> with versioning.
 * 
 * Incident vertex and next and previous incidence in Iseq(incident vertex) are
 * versioned.
 * 
 * @author José Monte(monte@uni-koblenz.de)
 */
public abstract class ReversedEdgeImpl extends
		de.uni_koblenz.jgralab.impl.ReversedEdgeImpl implements
		VersionedIncidence {
	// attributes inherited from <code>IncidenceImpl</code>
	protected VersionedReferenceImpl<VertexImpl> incidentVertex;
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
	protected ReversedEdgeImpl(EdgeImpl normalEdge, Graph graph) {
		super(normalEdge, graph);
	}

	// --- getter ---//

	@Override
	protected VertexImpl getIncidentVertex() {
		if (incidentVertex == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return incidentVertex.getValidValue(graph.getCurrentTransaction());
	}

	@Override
	protected IncidenceImpl getNextIncidence() {
		if (nextIncidence == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return nextIncidence.getValidValue(graph.getCurrentTransaction());
	}

	@Override
	protected IncidenceImpl getPrevIncidence() {
		if (prevIncidence == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return prevIncidence.getValidValue(graph.getCurrentTransaction());
	}

	// --- setter --- //
	@Override
	protected void setIncidentVertex(VertexImpl v) {
		if (graph.isLoading()) {
			incidentVertex = new VersionedReferenceImpl<VertexImpl>(normalEdge,
					v);
		} else {
			// initialize here
			if (incidentVertex == null) {
				incidentVertex = new VersionedReferenceImpl<VertexImpl>(
						normalEdge);
			}
			incidentVertex.setValidValue(v, graph.getCurrentTransaction());
		}
	}

	@Override
	protected void setNextIncidence(IncidenceImpl nextIncidence) {
		if (graph.isLoading()) {
			this.nextIncidence = new VersionedReferenceImpl<IncidenceImpl>(
					normalEdge, nextIncidence);
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
					VertexImpl currentIncidentVertex = this.incidentVertex
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
			this.nextIncidence.setValidValue(nextIncidence, transaction,
					explicitChange);
		}
	}

	@Override
	protected void setPrevIncidence(IncidenceImpl prevIncidence) {
		if (graph.isLoading()) {
			this.prevIncidence = new VersionedReferenceImpl<IncidenceImpl>(
					normalEdge, prevIncidence);
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
					VertexImpl currentIncidentVertex = this.incidentVertex
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
			this.prevIncidence.setValidValue(prevIncidence, transaction,
					explicitChange);
		}
	}

	@Override
	public VersionedReferenceImpl<IncidenceImpl> getVersionedNextIncidence() {
		return this.nextIncidence;
	}

	@Override
	public VersionedReferenceImpl<IncidenceImpl> getVersionedPrevIncidence() {
		return this.prevIncidence;
	}
}
