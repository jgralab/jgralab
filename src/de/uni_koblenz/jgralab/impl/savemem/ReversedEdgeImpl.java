package de.uni_koblenz.jgralab.impl.savemem;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.EdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;

/**
 * The implementation of a reversed edge accessing attributes without
 * versioning. This implementation uses singly-linked lists only, in contrast to
 * the original version which uses doubly-linked lists.
 * 
 * @author Jose Monte {monte@uni-koblenz.de} (original implementation)
 * @author Mahdi Derakhshanmanesh {manesh@uni-koblenz.de} (adjusted version)
 */
public abstract class ReversedEdgeImpl extends
		de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl {
	/**
	 * The 'this' {@link Vertex}.
	 */
	private VertexBaseImpl incidentVertex;

	/**
	 * The next incident {@link Edge}.
	 */
	private IncidenceImpl nextIncidence;

	/**
	 * The constructor.
	 * 
	 * @param normalEdge
	 *            The {@link Edge} to be reversed.
	 * @param graph
	 *            A reference to the {@link Graph}, this {@link Edge} shall be
	 *            added to.
	 */
	protected ReversedEdgeImpl(EdgeBaseImpl normalEdge, Graph graph) {
		super(normalEdge, graph);
	}

	@Override
	protected VertexBaseImpl getIncidentVertex() {
		return incidentVertex;
	}

	@Override
	protected IncidenceImpl getNextIncidence() {
		return nextIncidence;
	}

	@Override
	protected IncidenceImpl getPrevIncidence() {
		Edge prevEdge = null;

		for (Edge currEdge = incidentVertex.getFirstEdge(); currEdge != null; currEdge = currEdge
				.getNextEdge()) {
			if (currEdge == this) {
				return (IncidenceImpl) prevEdge;
			}
			prevEdge = currEdge;
		}

		return null;
	}

	@Override
	protected void setIncidentVertex(VertexBaseImpl v) {
		this.incidentVertex = v;
	}

	@Override
	protected void setNextIncidence(IncidenceImpl nextIncidence) {
		this.nextIncidence = nextIncidence;
	}

	@Override
	protected void setPrevIncidence(IncidenceImpl prevIncidence) {
		// throw new UnsupportedOperationException(
		// "Unsupported in savemem implementation.");
	}
}
