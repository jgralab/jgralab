package de.uni_koblenz.jgralab.impl.db;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;

/**
 * Implements a database persistable reversed edge.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public abstract class ReversedEdgeImpl extends ReversedEdgeBaseImpl implements
		DatabasePersistableEdge, DatabasePersistableIncidence {

	/**
	 * Number mapping incidence's sequence in LambdaSeq of incident vertex.
	 */
	private long sequenceNumberInLambdaSeq;

	/**
	 * Primary key of incident vertex in database.
	 */
	private int omegaVId;

	/**
	 * Normal edge.
	 */
	protected DatabasePersistableEdge normalEdge;

	/**
	 * Creates a new <code>ReversedEdge</code>.
	 * 
	 * @param normalEdge
	 *            Normal edge.
	 * @param graph
	 *            Graph edge is part of.
	 */
	public ReversedEdgeImpl(EdgeImpl normalEdge, Graph graph) {
		super(normalEdge, graph);
		this.normalEdge = normalEdge;
	}

	@Override
	protected VertexBaseImpl getIncidentVertex() {
		if (this.omegaVId > 0)
			return (VertexBaseImpl) this.graph.getVertex(this.omegaVId);
		else
			return null;
	}

	private GraphImpl getGraphImpl() {
		return (GraphImpl) super.graph;
	}

	@Override
	protected IncidenceImpl getPrevIncidence() {
		VertexImpl vertex = (VertexImpl) this.getIncidentVertex();
		return (IncidenceImpl) vertex.getPrevIncidence(this);
	}

	@Override
	protected IncidenceImpl getNextIncidence() {
		VertexImpl vertex = (VertexImpl) this.getIncidentVertex();
		return (IncidenceImpl) vertex.getNextIncidence(this);
	}

	@Override
	protected void setIncidentVertex(VertexBaseImpl v) {
		this.setIncidentVId(v.getId());
	}

	@Override
	protected void setNextIncidence(IncidenceImpl nextIncidence) {
		nextIncidence.putEdgeAfter(this);
	}

	@Override
	protected void setPrevIncidence(IncidenceImpl prevIncidence) {
		prevIncidence.putEdgeBefore(this);
	}

	@Override
	public boolean isValid() {
		return this.normalEdge.isValid();
	}

	@Override
	public int getIncidentVId() {
		return this.omegaVId;
	}

	@Override
	public long getSequenceNumberInLambdaSeq() {
		return this.sequenceNumberInLambdaSeq;
	}

	@Override
	public int getGId() {
		return this.normalEdge.getGId();
	}

	@Override
	public long getSequenceNumberInESeq() {
		return this.normalEdge.getSequenceNumberInESeq();
	}

	@Override
	public void setIncidentVId(int incidentVId) {
		if (this.omegaVId != incidentVId)
			this.updateIncidentVId(incidentVId);
	}

	@Override
	public void setSequenceNumberInLambdaSeq(long sequenceNumber) {
		if (this.sequenceNumberInLambdaSeq != sequenceNumber)
			this.updateSequenceNumberInLambdaSeq(sequenceNumber);
	}

	@Override
	public void setSequenceNumberInESeq(long sequenceNumberInESeq) {
		this.normalEdge.setSequenceNumberInESeq(sequenceNumberInESeq);
	}

	/**
	 * Updates number mapping incidences's sequence in LambdaSeq of incident
	 * vertex.
	 * 
	 * @param sequenceNumber
	 */
	private void updateSequenceNumberInLambdaSeq(long sequenceNumber) {
		this.sequenceNumberInLambdaSeq = sequenceNumber;
		if (this.isPersistent() && this.isInitialized())
			this.getGraphImpl().writeSequenceNumberInLambdaSeqBack(this);
	}

	/**
	 * Updates primary key of incident vertex.
	 * 
	 * @param vId
	 *            Id of incident vertex.
	 */
	private void updateIncidentVId(int vId) {
		this.omegaVId = vId;
		// this.getGraph();
		if (this.isPersistent() && this.isInitialized() && vId > 0)
			;
		this.getGraphImpl().writeIncidentVIdBack(this);
	}

	@Override
	public boolean isPersistent() {
		return this.normalEdge.isPersistent();
	}

	@Override
	public boolean isInitialized() {
		return this.normalEdge.isInitialized();
	}
	
	@Override
	public void setInitialized(boolean initialized) {
		this.normalEdge.setInitialized(initialized);
	}
	
	@Override
	public void setPersistent(boolean persistent) {
		this.normalEdge.setPersistent(persistent);
	}

	@Override
	public void deleted() {
		this.normalEdge.deleted();
	}

	@Override
	public int getIncidentEId() {
		return this.getId();
	}
}
