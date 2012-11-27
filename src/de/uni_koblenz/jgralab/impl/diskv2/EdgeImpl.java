package de.uni_koblenz.jgralab.impl.diskv2;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.InternalVertex;

/**
 * The implementation of an <code>Edge</code> accessing attributes without
 * versioning.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class EdgeImpl extends de.uni_koblenz.jgralab.impl.EdgeBaseImpl {
	// global edge sequence
	private int nextEdgeId;
	private int prevEdgeId;

	// the this-vertex
	private int incidentVertexId;

	// incidence list
	private int nextIncidenceId;
	private int prevIncidenceId;

	@Override
	public InternalEdge getNextEdgeInESeq() {
		assert isValid();
		return (InternalEdge) this.getGraph().getEdge(nextEdgeId);
	}

	@Override
	public InternalEdge getPrevEdgeInESeq() {
		assert isValid();
		return (InternalEdge) this.getGraph().getEdge(prevEdgeId);
	}

	@Override
	public InternalVertex getIncidentVertex() {
		return (InternalVertex) this.getGraph().getVertex(this.incidentVertexId);
	}

	@Override
	public InternalEdge getNextIncidenceInISeq() {
		if(nextIncidenceId < 0)
			return (InternalEdge) this.getGraph().getEdge(- nextIncidenceId);
		else
			return (InternalEdge) this.getGraph().getEdge(nextIncidenceId);
	}

	@Override
	public InternalEdge getPrevIncidenceInISeq() {
		if(prevIncidenceId < 0)
			return (InternalEdge) this.getGraph().getEdge(- prevIncidenceId);
		else
			return (InternalEdge) this.getGraph().getEdge(prevIncidenceId);
	}

	@Override
	public void setNextEdgeInGraph(Edge nextEdge) {
		this.nextEdgeId = nextEdge.getId();
	}

	@Override
	public void setPrevEdgeInGraph(Edge prevEdge) {
		this.prevEdgeId = prevEdge.getId();
	}

	@Override
	public void setIncidentVertex(Vertex v) {
		incidentVertexId = v.getId();
	}

	@Override
	public void setNextIncidenceInternal(InternalEdge nextIncidence) {
		if(nextIncidence instanceof ReversedEdgeImpl)	
			this.nextIncidenceId = - nextIncidence.getId();
		else
			this.nextIncidenceId = nextIncidence.getId();
	}

	@Override
	public void setPrevIncidenceInternal(InternalEdge prevIncidence) {
		if(prevIncidence instanceof ReversedEdgeImpl)
			this.prevIncidenceId = - prevIncidence.getId();
		else
			this.prevIncidenceId = prevIncidence.getId();
	}

	protected EdgeImpl(int anId, Graph graph, Vertex alpha, Vertex omega) {
		super(anId, graph, alpha, omega);
	}

	@Override
	public void setId(int id) {
		assert id >= 0;
		this.id = id;
	}

	public int getNextEdgeId() {
		return nextEdgeId;
	}
	
	public void restoreNextEdgeId(int id){
		this.nextEdgeId = id;
	}

	public int getPrevEdgeId() {
		return prevEdgeId;
	}

	public void restorePrevEdgeId(int id){
		this.prevEdgeId = id;
	}
	
	public int getIncidentVertexId() {
		return incidentVertexId;
	}
	
	public int getNextIncidenceId() {
		return nextIncidenceId;
	}
	
	public void restoreNextIncidenceId(int id){
		this.nextIncidenceId = id;
	}

	public int getPrevIncidenceId() {
		return prevIncidenceId;
	}
	
	public void restorePrevIncidenceId(int id){
		this.prevIncidenceId = id;
	}
}
