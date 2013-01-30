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
		if(v==null)
			this.incidentVertexId = 0;
		else
			this.incidentVertexId = v.getId();
		 ((EdgeImpl)this.getReversedEdge()).getTracker().putVariable(60, this.incidentVertexId);
	}

	@Override
	public void setNextIncidenceInternal(InternalEdge nextIncidence) {
		if(nextIncidence == null)
			this.nextIncidenceId = 0;
		else 
			this.nextIncidenceId = nextIncidence.getId();
		 ((EdgeImpl)this.getReversedEdge()).getTracker().putVariable(44, this.nextIncidenceId);
	}

	@Override
	public void setPrevIncidenceInternal(InternalEdge prevIncidence) {
		if(prevIncidence ==null)
			this.prevIncidenceId = 0;
		else
			this.prevIncidenceId = prevIncidence.getId();
		((EdgeImpl)this.getReversedEdge()).getTracker().putVariable(52, this.prevIncidenceId);
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

	public void restoreIncidentVertexId(int id){
		this.incidentVertexId = id;
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
	
	@Override
	public int hashCode(){
		return - this.id;
	}
	
	
}
