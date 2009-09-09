package de.uni_koblenz.jgralab.impl.std;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.impl.EdgeImpl;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.VertexImpl;

/**
 * The implementation of an ReversedEdge accessing attributes without
 * versioning.
 * 
 * @author José Monte(monte@uni-koblenz.de)
 */
public abstract class ReversedEdgeImpl extends
		de.uni_koblenz.jgralab.impl.ReversedEdgeImpl {
	private VertexImpl incidentVertex;
	private IncidenceImpl nextIncidence;
	private IncidenceImpl prevIncidence;

	@Override
	protected VertexImpl getIncidentVertex() {
		return incidentVertex;
	}

	@Override
	protected IncidenceImpl getNextIncidence() {
		return nextIncidence;
	}

	@Override
	protected IncidenceImpl getPrevIncidence() {
		return prevIncidence;
	}

	@Override
	protected void setIncidentVertex(VertexImpl v) {
		this.incidentVertex = v;
	}

	@Override
	protected void setNextIncidence(IncidenceImpl nextIncidence) {
		this.nextIncidence = nextIncidence;
	}

	@Override
	protected void setPrevIncidence(IncidenceImpl prevIncidence) {
		this.prevIncidence = prevIncidence;
	}

	/**
	 * 
	 * @param normalEdge
	 * @param graph
	 */
	protected ReversedEdgeImpl(EdgeImpl normalEdge, Graph graph) {
		super(normalEdge, graph);
	}
}
