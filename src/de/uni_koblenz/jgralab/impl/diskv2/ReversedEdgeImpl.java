package de.uni_koblenz.jgralab.impl.diskv2;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.EdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.InternalVertex;

/**
 * The implementation of an ReversedEdge accessing attributes without
 * versioning.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class ReversedEdgeImpl extends
		de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl {
	private int incidentVertexId;
	private int nextIncidenceId;
	private int prevIncidenceId;

	@Override
	public InternalVertex getIncidentVertex() {
		return (InternalVertex) this.getGraph().getVertex(incidentVertexId);
	}

	@Override
	public InternalEdge getNextIncidenceInISeq() {
		if(nextIncidenceId < 0)
			return (InternalEdge) this.getGraph().getEdge(- this.nextIncidenceId);
		else
			return (InternalEdge) this.getGraph().getEdge(this.nextIncidenceId);
	}

	@Override
	public InternalEdge getPrevIncidenceInISeq() {
		if(prevIncidenceId < 0){
			return (InternalEdge) this.getGraph().getEdge(- this.prevIncidenceId);
		}else{
			return (InternalEdge) this.getGraph().getEdge(this.prevIncidenceId);
		}
	}

	@Override
	public void setIncidentVertex(Vertex v) {
		 this.incidentVertexId = v.getId();
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

	/**
	 * 
	 * @param normalEdge
	 * @param graph
	 */
	protected ReversedEdgeImpl(EdgeBaseImpl normalEdge, Graph graph) {
		super(normalEdge, graph);
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
