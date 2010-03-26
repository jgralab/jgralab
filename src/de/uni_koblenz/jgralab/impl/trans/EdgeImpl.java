package de.uni_koblenz.jgralab.impl.trans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.trans.ListPosition;
import de.uni_koblenz.jgralab.trans.Transaction;
import de.uni_koblenz.jgralab.trans.TransactionState;
import de.uni_koblenz.jgralab.trans.VersionedDataObject;
import de.uni_koblenz.jgralab.trans.VersionedIncidence;
import de.uni_koblenz.jgralab.trans.VertexPosition;

/**
 * The implementation of an <code>Edge</code> with versioning.
 * 
 * Next and previous edge in Eseq, the incident vertex and the next and previous
 * incidence in Iseq(incidentVertex) are versioned.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class EdgeImpl extends de.uni_koblenz.jgralab.impl.EdgeBaseImpl
		implements VersionedIncidence {
	// next and previous edge in Eseq
	protected VersionedReferenceImpl<EdgeImpl> nextEdge;
	protected VersionedReferenceImpl<EdgeImpl> prevEdge;

	// attributes inherited from <code>IncidenceImpl</code>
	protected VersionedReferenceImpl<VertexBaseImpl> incidentVertex;
	protected VersionedReferenceImpl<IncidenceImpl> nextIncidence;
	protected VersionedReferenceImpl<IncidenceImpl> prevIncidence;

	/**
	 * Initialization of versioned attributes is avoided here, to not have
	 * persistent and temporary values for new instances within the transaction
	 * this instance is created in.
	 * 
	 * @param anId
	 *            the id of the <code>Edge</code>
	 * @param graph
	 *            the corresponding <code>Graph</code>
	 */
	protected EdgeImpl(int anId, Graph graph, Vertex alpha, Vertex omega) {
		super(anId, graph);
		createReversedEdge();
		((GraphImpl) graph).addEdge(this, alpha, omega);
	}

	// --- getter ---//
	@Override
	public int getId() {
		if (!graph.isLoading()) {
			Transaction transaction = graph.getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			// if this instance isn't valid for the current transaction, the id
			// is 0
			if ((transaction.getState() == TransactionState.RUNNING)
					&& !isValid()) {
				return 0;
			}
		}
		// otherwise return the "real" id
		return id;
	}

	@Override
	public Edge getNextEdgeInGraph() {
		if (nextEdge == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return nextEdge.getValidValue(graph.getCurrentTransaction());
	}

	@Override
	public Edge getPrevEdgeInGraph() {
		if (prevEdge == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return prevEdge.getValidValue(graph.getCurrentTransaction());
	}

	@Override
	protected VertexBaseImpl getIncidentVertex() {
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

	// --- setter ---//

	@Override
	protected void setId(int id) {
		// initialize id
		if (graph.isLoading()) {
			this.id = id;
		} else {
			Transaction transaction = graph.getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			TransactionState state = transaction.getState();
			// avoid that the Id 0 can be explicitly assigned to this instance
			if ((state == TransactionState.RUNNING) && (id != 0)) {
				this.id = id;
			}
		}
	}

	@Override
	protected void setNextEdgeInGraph(Edge nextEdge) {
		// graph loading -> new initialization...
		if (graph.isLoading()) {
			this.nextEdge = new VersionedReferenceImpl<EdgeImpl>(this,
					(EdgeImpl) nextEdge);
		} else {
			TransactionImpl transaction = (TransactionImpl) graph
					.getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			boolean explicitChange = false;
			// check if next edge has been changed explicitly or not - only
			// relevant in writing-phase
			if (transaction.getState() == TransactionState.WRITING) {
				if (transaction.changedEseqEdges != null) {
					explicitChange = transaction.changedEseqEdges
							.containsKey(this)
							&& (transaction.changedEseqEdges.get(this)
									.containsKey(ListPosition.NEXT));
				}
			}
			// initialization here
			if (this.nextEdge == null) {
				this.nextEdge = new VersionedReferenceImpl<EdgeImpl>(this);
			}
			this.nextEdge.setValidValue((EdgeImpl) nextEdge, transaction,
					explicitChange);
		}
	}

	@Override
	protected void setPrevEdgeInGraph(Edge prevEdge) {
		// graph loading -> new initialization...
		if (graph.isLoading()) {
			this.prevEdge = new VersionedReferenceImpl<EdgeImpl>(this,
					(EdgeImpl) prevEdge);
		} else {
			TransactionImpl transaction = (TransactionImpl) graph
					.getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			boolean explicitChange = false;
			// check if previous edge has been changed explicitly or not -
			// only relevant in writing-phase
			if (transaction.getState() == TransactionState.WRITING) {
				if (transaction.changedEseqEdges != null) {
					explicitChange = transaction.changedEseqEdges
							.containsKey(this)
							&& (transaction.changedEseqEdges.get(this)
									.containsKey(ListPosition.PREV));
				}
			}
			// initialization here
			if (this.prevEdge == null) {
				this.prevEdge = new VersionedReferenceImpl<EdgeImpl>(this);
			}
			this.prevEdge.setValidValue((EdgeImpl) prevEdge, transaction,
					explicitChange);
		}
	}

	@Override
	protected void setIncidentVertex(VertexBaseImpl v) {
		// graph loading -> new initialization...
		if (graph.isLoading()) {
			incidentVertex = new VersionedReferenceImpl<VertexBaseImpl>(this, v);
		} else {
			// initialization here
			if (incidentVertex == null) {
				incidentVertex = new VersionedReferenceImpl<VertexBaseImpl>(
						this);
			}
			incidentVertex.setValidValue(v, graph.getCurrentTransaction());
		}
	}

	@Override
	protected void setNextIncidence(IncidenceImpl nextIncidence) {
		// graph loading -> new initialization...
		if (graph.isLoading()) {
			this.nextIncidence = new VersionedReferenceImpl<IncidenceImpl>(
					this, nextIncidence);
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
					VertexBaseImpl temporaryIncidentVertex = incidentVertex
							.getTemporaryValue(transaction);
					Map<IncidenceImpl, Map<ListPosition, Boolean>> incidenceList = transaction.changedIncidences
							.get(temporaryIncidentVertex);
					if (incidenceList != null) {
						explicitChange = incidenceList.containsKey(this)
								&& (incidenceList.get(this).keySet()
										.contains(ListPosition.NEXT));
					}
				}
			}
			// initialization here
			if (this.nextIncidence == null) {
				this.nextIncidence = new VersionedReferenceImpl<IncidenceImpl>(
						this);
			}
			this.nextIncidence.setValidValue(nextIncidence, transaction,
					explicitChange);
		}
	}

	@Override
	protected void setPrevIncidence(IncidenceImpl prevIncidence) {
		// graph loading -> new initialization...
		if (graph.isLoading()) {
			this.prevIncidence = new VersionedReferenceImpl<IncidenceImpl>(
					this, prevIncidence);
		} else {
			TransactionImpl transaction = (TransactionImpl) graph
					.getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			boolean explicitChange = false;
			// check if previous incidence has been changed explicitly or
			// not - only relevant in writing-phase
			if (transaction.getState() == TransactionState.WRITING) {
				if (transaction.changedIncidences != null) {
					VertexBaseImpl temporaryIncidentVertex = incidentVertex
							.getTemporaryValue(transaction);
					Map<IncidenceImpl, Map<ListPosition, Boolean>> incidenceList = transaction.changedIncidences
							.get(temporaryIncidentVertex);
					if (incidenceList != null) {
						explicitChange = incidenceList.containsKey(this)
								&& (incidenceList.get(this).keySet()
										.contains(ListPosition.PREV));
					}
				}
			}
			// initialization here
			if (this.prevIncidence == null) {
				this.prevIncidence = new VersionedReferenceImpl<IncidenceImpl>(
						this);
			}
			this.prevIncidence.setValidValue(prevIncidence, transaction,
					explicitChange);
		}
	}

	@Override
	public void setAlpha(Vertex alpha) {
		TransactionImpl transaction = (TransactionImpl) graph
				.getCurrentTransaction();
		if (transaction == null) {
			throw new GraphException("Current transaction is null.");
		}
		// It should not be possible to set alpha, if this instance isn't valid
		// in the current transaction.
		if (!isValid()) {
			throw new GraphException("Edge " + this
					+ " is not valid within the current transaction.");
		}
		// important to temporary store old alpha!!!
		VertexBaseImpl oldAlpha = getIncidentVertex();
		// synchronize <code>transaction</code> to make sure that
		// <code>transaction</code> cannot be set active in another
		// <code>Thread</code> while <code>transaction</code> is executing this
		// method. This is applied to all other public write-operations.
		synchronized (transaction) {
			super.setAlpha(alpha);
			assert ((transaction != null) && !transaction.isReadOnly()
					&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
			// update changedEdges...
			if (transaction.getState() == TransactionState.RUNNING) {
				if (transaction.changedEdges == null) {
					transaction.changedEdges = new HashMap<EdgeImpl, VertexPosition>(
							1, TransactionManagerImpl.LOAD_FACTOR);
				}
				VertexPosition position = transaction.changedEdges.get(this);
				if (position == null) {
					position = VertexPosition.ALPHA;
				} else if (position == VertexPosition.OMEGA) {
					position = VertexPosition.ALPHAOMEGA;
				}
				transaction.changedEdges.put(this, position);
				// delete this as changed incidence from changedIncidences for
				// <code>oldAlpha</code>
				if (transaction.changedIncidences != null) {
					Map<IncidenceImpl, Map<ListPosition, Boolean>> oldAlphaIncidences = transaction.changedIncidences
							.get(oldAlpha);
					if (oldAlphaIncidences != null) {
						oldAlphaIncidences.remove(this);
					}
				}
			}
		}
	}

	@Override
	public void setOmega(Vertex omega) {
		TransactionImpl transaction = (TransactionImpl) graph
				.getCurrentTransaction();
		if (transaction == null) {
			throw new GraphException("Current transaction is null.");
		}
		// It should not be possible to set omega, if this instance isn't valid
		// in the current transaction.
		if (!isValid()) {
			throw new GraphException("Edge " + this
					+ " is not valid within the current transaction.");
		}
		// important to temporary store old omega!!!
		VertexBaseImpl oldOmega = ((ReversedEdgeImpl) reversedEdge)
				.getIncidentVertex();
		// synchronize <code>transaction</code> to make sure that
		// <code>transaction</code> cannot be set active in another
		// <code>Thread</code> while <code>transaction</code> is executing this
		// method. This is applied to all other public write-operations.
		synchronized (transaction) {
			super.setOmega(omega);
			assert ((transaction != null) && !transaction.isReadOnly()
					&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
			// update changedEdges...
			if (transaction.getState() == TransactionState.RUNNING) {
				if (transaction.changedEdges == null) {
					transaction.changedEdges = new HashMap<EdgeImpl, VertexPosition>(
							1, TransactionManagerImpl.LOAD_FACTOR);
				}
				VertexPosition position = transaction.changedEdges.get(this);
				if (position == null) {
					position = VertexPosition.OMEGA;
				} else if (position == VertexPosition.ALPHA) {
					position = VertexPosition.ALPHAOMEGA;
				}
				transaction.changedEdges.put(this, position);
				// delete reversedEdge as changed incidence from
				// changedIncidences for <code>oldOmega</code>
				if (transaction.changedIncidences != null) {
					Map<IncidenceImpl, Map<ListPosition, Boolean>> oldOmegaIncidences = transaction.changedIncidences
							.get(oldOmega);
					if (oldOmegaIncidences != null) {
						oldOmegaIncidences.remove(reversedEdge);
					}
				}
			}
		}
	}

	/**
	 * Should be called from generated <code>Edge</code> implementation classes
	 * whenever a versioned attribute is changed.
	 * 
	 * @param versionedAttribute
	 *            the changed attribute
	 */
	protected void attributeChanged(
			VersionedDataObjectImpl<?> versionedAttribute) {
		if (!graph.isLoading()) {
			TransactionImpl transaction = (TransactionImpl) graph
					.getCurrentTransaction();
			assert ((transaction != null)
					&& (transaction.getState() != TransactionState.NOTRUNNING)
					&& transaction.isValid() && !transaction.isReadOnly());
			if (transaction.getState() == TransactionState.RUNNING) {
				// This check is also made in concrete generated instances, so
				// that should not happen!!!
				if (!isValid()) {
					throw new GraphException(
							"Trying to change the attribute '"
									+ versionedAttribute
									+ "' of Edge "
									+ this
									+ ", that has been deleted within the current transaction.");
				}
				// get all changed attributes of this instance...
				if (transaction.changedAttributes == null) {
					transaction.changedAttributes = new HashMap<AttributedElement, Set<VersionedDataObject<?>>>(
							1, TransactionManagerImpl.LOAD_FACTOR);
				}
				Set<VersionedDataObject<?>> attributes = transaction.changedAttributes
						.get(this);
				if (attributes == null) {
					attributes = new HashSet<VersionedDataObject<?>>(1,
							TransactionManagerImpl.LOAD_FACTOR);
					transaction.changedAttributes.put(this, attributes);
				}
				attributes.add(versionedAttribute);
			}
		}
	}

	/**
	 * Implemented in generated subclasses.
	 * 
	 * @return references to all versioned attributes of this instance. Needed
	 *         for validation!!!
	 */
	abstract public Set<VersionedDataObject<?>> attributes();

	@Override
	public boolean isValid() {
		// avoid that validity of this instance is checked while edge-Arrays are
		// expanded
		((GraphImpl) graph).edgeSync.readLock().lock();
		boolean result = super.isValid();
		((GraphImpl) graph).edgeSync.readLock().unlock();
		return result;
	}

	@Override
	public VersionedReferenceImpl<IncidenceImpl> getVersionedNextIncidence() {
		return this.nextIncidence;
	}

	@Override
	public VersionedReferenceImpl<IncidenceImpl> getVersionedPrevIncidence() {
		return this.prevIncidence;
	}

	@Override
	protected void internalSetDefaultValue(Attribute attr)
			throws GraphIOException, NoSuchFieldException {
		attr.setDefaultTransactionValue(this);
	}
}
