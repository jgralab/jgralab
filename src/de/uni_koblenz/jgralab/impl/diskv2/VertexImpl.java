package de.uni_koblenz.jgralab.impl.diskv2;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.InternalVertex;

/**
 * The implementation of a <code>Vertex</code> accessing attributes without
 * versioning.
 *
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class VertexImpl extends
		de.uni_koblenz.jgralab.impl.VertexBaseImpl {
	private int nextVertexId;
	private int prevVertexId;
	private int firstIncidenceId;
	private int lastIncidenceId;

	/**
	 * holds the version of the vertex structure, for every modification of the
	 * structure (e.g. adding or deleting an incident edge or changing the
	 * incidence sequence) this version number is increased by one. It is set to
	 * 0 when the vertex is created or the graph is loaded.
	 */
	private long incidenceListVersion = 0;

	@Override
	public InternalVertex getNextVertexInVSeq() {
		assert isValid() : this + " is not valid.";
		return (InternalVertex) this.getGraph().getVertex(nextVertexId);
	}

	@Override
	public InternalVertex getPrevVertexInVSeq() {
		assert isValid() : this + " is not valid.";
		return (InternalVertex) this.getGraph().getVertex(prevVertexId);
	}

	@Override
	public InternalEdge getFirstIncidenceInISeq() {
		if(firstIncidenceId < 0){
			return (InternalEdge) this.getGraph().getEdge(-1*firstIncidenceId).getReversedEdge();
		}else{
			return (InternalEdge) this.getGraph().getEdge(firstIncidenceId);
		}
		
	}

	@Override
	public InternalEdge getLastIncidenceInISeq() {
		if(lastIncidenceId < 0){
			return (InternalEdge) this.getGraph().getEdge(- lastIncidenceId).getReversedEdge();
		}else{
			return (InternalEdge) this.getGraph().getEdge(lastIncidenceId);
		}
	}

	@Override
	public void setNextVertex(Vertex nextVertex) {
		this.nextVertexId = nextVertex.getId();
	}

	@Override
	public void setPrevVertex(Vertex prevVertex) {
		this.prevVertexId = prevVertex.getId();
	}

	@Override
	public void setFirstIncidence(InternalEdge firstIncidence) {
		if(firstIncidence instanceof ReversedEdgeImpl){
			this.firstIncidenceId = - firstIncidence.getId();
		}else{
			this.firstIncidenceId = firstIncidence.getId();
		}
	}

	@Override
	public void setLastIncidence(InternalEdge lastIncidence) {
		if(lastIncidence instanceof ReversedEdgeImpl){
			this.lastIncidenceId = - lastIncidence.getId();
		}else{
			this.lastIncidenceId = lastIncidence.getId();
		}
	}

	@Override
	public void setIncidenceListVersion(long incidenceListVersion) {
		this.incidenceListVersion = incidenceListVersion;
	}

	@Override
	public long getIncidenceListVersion() {
		assert isValid();
		return incidenceListVersion;
	}

	/**
	 *
	 * @param id
	 * @param graph
	 */
	protected VertexImpl(int id, Graph graph) {
		super(id, graph);
		((GraphImpl) graph).addVertex(this);
	}

	@Override
	public void setId(int id) {
		assert id >= 0;
		this.id = id;
	}

	public int getNextVertexId() {
		return nextVertexId;
	}
	
	public void restoreNextVertexId(int id){
		this.nextVertexId = id;
	}

	public int getPrevVertexId() {
		return prevVertexId;
	}
	
	public void restorePrevVertexId(int id){
		this.prevVertexId = id;
	}

	public int getFirstIncidenceId() {
		return firstIncidenceId;
	}
	
	public void restoreFirstIncidenceId(int id){
		this.firstIncidenceId = id;
	}

	public int getLastIncidenceId() {
		return lastIncidenceId;
	}
	
	public void restoreLastIncidenceId(int id){
		this.lastIncidenceId = id;
	}
	
	public void restoreIncidenceListVersion(long version){
		this.incidenceListVersion = version;
	}
}

