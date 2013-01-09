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
		//if(nextIncidenceId < 0)
		//	return (InternalEdge) this.getGraph().getEdge(- nextIncidenceId);
		//else
			return (InternalEdge) this.getGraph().getEdge(nextIncidenceId);
	}

	@Override
	public InternalEdge getPrevIncidenceInISeq() {
		//if(prevIncidenceId < 0)
		//	return (InternalEdge) this.getGraph().getEdge(- prevIncidenceId);
	//	else
			return (InternalEdge) this.getGraph().getEdge(prevIncidenceId);
	}

	@Override
	public void setNextEdgeInGraph(Edge nextEdge) {
		if(nextEdge == null)
			this.nextEdgeId = 0;
		else
			this.nextEdgeId = nextEdge.getId();
		getTracker().putVariable(4, this.nextEdgeId);
	}

	@Override
	public void setPrevEdgeInGraph(Edge prevEdge) {
		if(prevEdge == null)
			this.prevEdgeId = 0;
		else				
			this.prevEdgeId = prevEdge.getId();
		getTracker().putVariable(12, this.prevEdgeId);
	}

	@Override
	public void setIncidentVertex(Vertex v) {
		if(v == null)
			this.incidentVertexId = 0;
		else
			incidentVertexId = v.getId();
		getTracker().putVariable(36, this.incidentVertexId);
	}

	@Override
	public void setNextIncidenceInternal(InternalEdge nextIncidence) {
		if(nextIncidence == null)
			this.nextIncidenceId = 0;
		else
			this.nextIncidenceId = nextIncidence.getId();
		getTracker().putVariable(20, this.nextIncidenceId);
	}

	@Override
	public void setPrevIncidenceInternal(InternalEdge prevIncidence) {
		if(prevIncidence == null)
			this.prevIncidenceId = 0;
		else
			this.prevIncidenceId = prevIncidence.getId();
		getTracker().putVariable(28, this.prevIncidenceId);
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
	
	
	/**
	 * Get the Tracker for this GraphElement
	 * 
	 * @return The Tracker that tracks this GraphElement
	 */
	public EdgeTracker getTracker(){
		return ((GraphImpl)graph).getStorage().getEdgeTracker(this.id);
	}
	
	/**
	 * Called whenever a primitive attribute of this GraphElement changed so the
	 * new attribute value is stored in the Tracker.
	 */
	public void attributeChanged() {
		EdgeTracker tracker = getTracker();
		if (tracker != null)
			tracker.storeAttributes(this);
	}
	
	/**
	 * Called whenever a String of this GraphElement changed so the
	 * new String is stored in the Tracker.
	 */
	public void stringChanged() {
		EdgeTracker tracker = getTracker();
		if (tracker != null)
			tracker.storeStrings(this);
	}
	
	/**
	 * Called whenever a List of this GraphElement changed so the
	 * new list value is stored in the Tracker.
	 */
	public void listChanged() {
		EdgeTracker tracker = getTracker();
		if (tracker != null)
			tracker.storeLists(this);
	}
	
	@Override
	public int hashCode(){
		return this.id;
	}
}
