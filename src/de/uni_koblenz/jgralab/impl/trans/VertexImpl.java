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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.trans.ListPosition;
import de.uni_koblenz.jgralab.trans.Transaction;
import de.uni_koblenz.jgralab.trans.TransactionState;
import de.uni_koblenz.jgralab.trans.VersionedDataObject;

/**
 * The implementation of a <code>Vertex</edge> with versioning.
 * 
 * Next and previous vertex in Vseq, the first and last incidence in Iseq(this) and the incidenceListVersion are versioned.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class VertexImpl extends
		de.uni_koblenz.jgralab.impl.VertexBaseImpl {
	// next and previous vertex in Vseq
	protected VersionedReferenceImpl<VertexImpl> nextVertex;
	protected VersionedReferenceImpl<VertexImpl> prevVertex;

	// represents begin and end of Iseq
	protected VersionedReferenceImpl<IncidenceImpl> firstIncidence;
	protected VersionedReferenceImpl<IncidenceImpl> lastIncidence;
	protected VersionedReferenceImpl<Long> incidenceListVersion;

	/**
	 * Initialization of versioned attributes is avoided here, to not have
	 * persistent and temporary values for new instances within the transaction
	 * this instance is created in.
	 * 
	 * @param id
	 *            the Id of the <code>Vertex</code>
	 * @param graph
	 *            the corresponding <code>Graph</code>
	 */
	protected VertexImpl(int anId, Graph graph) {
		super(anId, graph);
		((GraphImpl) graph).addVertex(this);
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
	protected IncidenceImpl getFirstIncidence() {
		if (firstIncidence == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return firstIncidence.getValidValue(graph.getCurrentTransaction());
	}

	@Override
	protected IncidenceImpl getLastIncidence() {
		if (lastIncidence == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return lastIncidence.getValidValue(graph.getCurrentTransaction());
	}

	@Override
	public Vertex getNextVertex() {
		if (nextVertex == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return nextVertex.getValidValue(graph.getCurrentTransaction());
	}

	@Override
	public Vertex getPrevVertex() {
		if (prevVertex == null) {
			return null;
		}
		// Note: if this instance isn't valid within current transaction, then
		// null is returned
		return prevVertex.getValidValue(graph.getCurrentTransaction());
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
			// avoid that this instance can be explicitly assigned the Id 0
			if ((state == TransactionState.RUNNING) && (id != 0)) {
				this.id = id;
			}
		}
	}

	@Override
	protected void setFirstIncidence(IncidenceImpl firstIncidence) {
		// graph loading -> new initialization...
		if (graph.isLoading()) {
			this.firstIncidence = new VersionedReferenceImpl<IncidenceImpl>(
					this, firstIncidence);
		} else {
			// initialize here
			if (this.firstIncidence == null) {
				this.firstIncidence = new VersionedReferenceImpl<IncidenceImpl>(
						this);
			}
			this.firstIncidence.setValidValue(firstIncidence, graph
					.getCurrentTransaction());
		}
	}

	@Override
	protected void setLastIncidence(IncidenceImpl lastIncidence) {
		// graph loading -> new initialization...
		if (graph.isLoading()) {
			this.lastIncidence = new VersionedReferenceImpl<IncidenceImpl>(
					this, lastIncidence);
		} else {
			// initialize here
			if (this.lastIncidence == null) {
				this.lastIncidence = new VersionedReferenceImpl<IncidenceImpl>(
						this);
			}
			this.lastIncidence.setValidValue(lastIncidence, graph
					.getCurrentTransaction());
		}
	}

	@Override
	protected void setNextVertex(Vertex nextVertex) {
		// graph loading -> new initialization...
		if (graph.isLoading()) {
			this.nextVertex = new VersionedReferenceImpl<VertexImpl>(this,
					(VertexImpl) nextVertex);
		} else {
			TransactionImpl transaction = (TransactionImpl) graph
					.getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			boolean explicitChange = false;
			// check if next vertex has been changed explicitly or not - only
			// relevant in writing-phase
			if (transaction.getState() == TransactionState.WRITING) {
				if (transaction.changedVseqVertices != null) {
					explicitChange = transaction.changedVseqVertices
							.containsKey(this)
							&& (transaction.changedVseqVertices.get(this)
									.containsKey(ListPosition.NEXT));
				}
			}
			if (this.nextVertex == null) {
				this.nextVertex = new VersionedReferenceImpl<VertexImpl>(this);
			}
			this.nextVertex.setValidValue((VertexImpl) nextVertex, transaction,
					explicitChange);
		}
	}

	@Override
	protected void setPrevVertex(Vertex prevVertex) {
		// graph loading -> new initialization...
		if (graph.isLoading()) {
			this.prevVertex = new VersionedReferenceImpl<VertexImpl>(this,
					(VertexImpl) prevVertex);
		} else {
			TransactionImpl transaction = (TransactionImpl) graph
					.getCurrentTransaction();
			if (transaction == null) {
				throw new GraphException("Current transaction is null.");
			}
			boolean explicitChange = false;
			// check if previous edge has been changed explicitly or not - only
			// relevant in writing-phase
			if (transaction.getState() == TransactionState.WRITING) {
				if (transaction.changedVseqVertices != null) {
					explicitChange = transaction.changedVseqVertices
							.containsKey(this)
							&& (transaction.changedVseqVertices.get(this)
									.containsKey(ListPosition.PREV));
				}
			}
			if (this.prevVertex == null) {
				this.prevVertex = new VersionedReferenceImpl<VertexImpl>(this);
			}
			this.prevVertex.setValidValue((VertexImpl) prevVertex, transaction,
					explicitChange);
		}
	}

	@Override
	protected void setIncidenceListVersion(long incidenceListVersion) {
		// initialize here
		if (this.incidenceListVersion == null) {
			this.incidenceListVersion = new VersionedReferenceImpl<Long>(this);
		}
		this.incidenceListVersion.setValidValue(incidenceListVersion, graph
				.getCurrentTransaction());
	}

	@Override
	public long getIncidenceListVersion() {
		if (incidenceListVersion == null) {
			return 0L;
		}
		Long value = incidenceListVersion.getValidValue(graph
				.getCurrentTransaction());
		if (value == null) {
			return 0;
		}
		return value;
	}

	@Override
	protected void putIncidenceBefore(IncidenceImpl targetIncidence,
			IncidenceImpl movedIncidence) {
		TransactionImpl transaction = (TransactionImpl) graph
				.getCurrentTransaction();
		if (transaction == null) {
			throw new GraphException("Current transaction is null.");
		}
		// It should not be possible to execute this method, if this instance
		// isn't valid in the current transaction.
		if (!isValid()) {
			throw new GraphException("Vertex " + this
					+ " is not valid within the current transaction.");
		}
		// It should not be possible to execute this method, if targetIncidence
		// isn't valid
		// in the current transaction.
		if (!targetIncidence.isValid()) {
			throw new GraphException("Incidence " + targetIncidence
					+ " is not valid within the current transaction.");
		}
		// It should not be possible to execute this method, if movedIncidence
		// isn't valid
		// in the current transaction.
		if (!movedIncidence.isValid()) {
			throw new GraphException("Incidence " + movedIncidence
					+ " is not valid within the current transaction.");
		}
		synchronized (transaction) {
			super.putIncidenceBefore(targetIncidence, movedIncidence);
			assert ((transaction != null) && !transaction.isReadOnly()
					&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
			if (transaction.getState() == TransactionState.RUNNING) {
				if (transaction.changedIncidences == null) {
					transaction.changedIncidences = new HashMap<VertexImpl, Map<IncidenceImpl, Map<ListPosition, Boolean>>>(
							1, TransactionManagerImpl.LOAD_FACTOR);
				}
				// changed incidences for this instance
				Map<IncidenceImpl, Map<ListPosition, Boolean>> changedIncidences = transaction.changedIncidences
						.get(this);
				if (changedIncidences == null) {
					changedIncidences = new HashMap<IncidenceImpl, Map<ListPosition, Boolean>>(
							1, TransactionManagerImpl.LOAD_FACTOR);
					transaction.changedIncidences.put(this, changedIncidences);
				}
				// movedIncidence
				Map<ListPosition, Boolean> positionsMap = changedIncidences
						.get(movedIncidence);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1, 0.2f);
					changedIncidences.put(movedIncidence, positionsMap);
				}
				positionsMap.put(ListPosition.NEXT, true);
				// targetIncidence
				positionsMap = changedIncidences.get(targetIncidence);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1, 0.2f);
					changedIncidences.put(targetIncidence, positionsMap);
				}
				positionsMap.put(ListPosition.PREV, false);
			}
		}
	}

	@Override
	protected void putIncidenceAfter(IncidenceImpl targetIncidence,
			IncidenceImpl movedIncidence) {
		TransactionImpl transaction = (TransactionImpl) graph
				.getCurrentTransaction();
		if (transaction == null) {
			throw new GraphException("Current transaction is null.");
		}
		// It should not be possible to execute this method, if this instance
		// isn't valid in the current transaction.
		if (!isValid()) {
			throw new GraphException("Vertex " + this
					+ " is not valid within the current transaction.");
		}
		// It should not be possible to execute this method, if targetIncidence
		// isn't valid
		// in the current transaction.
		if (!targetIncidence.isValid()) {
			throw new GraphException("Incidence " + targetIncidence
					+ " is not valid within the current transaction.");
		}
		// It should not be possible to execute this method, if movedIncidence
		// isn't valid
		// in the current transaction.
		if (!movedIncidence.isValid()) {
			throw new GraphException("Incidence " + movedIncidence
					+ " is not valid within the current transaction.");
		}
		synchronized (transaction) {
			super.putIncidenceAfter(targetIncidence, movedIncidence);
			assert ((transaction != null) && !transaction.isReadOnly()
					&& transaction.isValid() && (transaction.getState() != TransactionState.NOTRUNNING));
			if (transaction.getState() == TransactionState.RUNNING) {
				if (transaction.changedIncidences == null) {
					transaction.changedIncidences = new HashMap<VertexImpl, Map<IncidenceImpl, Map<ListPosition, Boolean>>>(
							1, TransactionManagerImpl.LOAD_FACTOR);
				}
				// changed incidences for this instance
				Map<IncidenceImpl, Map<ListPosition, Boolean>> changedIncidences = transaction.changedIncidences
						.get(this);
				if (changedIncidences == null) {
					changedIncidences = new HashMap<IncidenceImpl, Map<ListPosition, Boolean>>(
							1, TransactionManagerImpl.LOAD_FACTOR);
					transaction.changedIncidences.put(this, changedIncidences);
				}
				// movedIncidence
				Map<ListPosition, Boolean> positionsMap = changedIncidences
						.get(movedIncidence);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1, 0.2f);
					changedIncidences.put(movedIncidence, positionsMap);
				}
				positionsMap.put(ListPosition.PREV, true);
				// targetIncidence
				positionsMap = changedIncidences.get(targetIncidence);
				if (positionsMap == null) {
					positionsMap = new HashMap<ListPosition, Boolean>(1, 0.2f);
					changedIncidences.put(targetIncidence, positionsMap);
				}
				positionsMap.put(ListPosition.NEXT, false);
			}
		}
	}

	/**
	 * Should be called from generated <code>Vertex</code> implementation
	 * classes whenever a versioned attribute is changed.
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
				if (!isValid()) {
					throw new GraphException(
							"Trying to change the attribute '"
									+ versionedAttribute
									+ "' of Vertex "
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
	 * Implemented in concrete generated instances.
	 * 
	 * @return references to all versioned attributes of this instance. Needed
	 *         for validation!!!
	 */
	abstract public Set<VersionedDataObject<?>> attributes();

	@Override
	public Iterable<Edge> incidences() {
		return new AttributedElementIterable<Edge>(super.incidences(), graph);
	}

	@Override
	public Iterable<Edge> incidences(EdgeDirection dir) {
		return new AttributedElementIterable<Edge>(super.incidences(dir), graph);
	}

	@Override
	public Iterable<Edge> incidences(EdgeClass eclass, EdgeDirection dir) {
		return new AttributedElementIterable<Edge>(super
				.incidences(eclass, dir), graph);
	}

	@Override
	public Iterable<Edge> incidences(Class<? extends Edge> eclass,
			EdgeDirection dir) {
		return new AttributedElementIterable<Edge>(super
				.incidences(eclass, dir), graph);
	}

	@Override
	public Iterable<Edge> incidences(EdgeClass eclass) {
		return new AttributedElementIterable<Edge>(super.incidences(eclass),
				graph);
	}

	@Override
	public Iterable<Edge> incidences(Class<? extends Edge> eclass) {
		return new AttributedElementIterable<Edge>(super.incidences(eclass),
				graph);
	}

	@Override
	public boolean isValid() {
		((GraphImpl) graph).vertexSync.readLock().lock();
		boolean result = super.isValid();
		((GraphImpl) graph).vertexSync.readLock().unlock();
		return result;
	}

	@Override
	public void sortIncidences(Comparator<Edge> comp) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void internalSetDefaultValue(Attribute attr)
			throws GraphIOException {
		attr.setDefaultTransactionValue(this);
	}
	
	@Override
	public String toString() {
		return "v " + getId() + ": "
				+ getAttributedElementClass().getQualifiedName();
	}
}
