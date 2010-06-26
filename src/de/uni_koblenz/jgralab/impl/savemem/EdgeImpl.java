package de.uni_koblenz.jgralab.impl.savemem;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;

/**
 * The implementation of an <code>Edge</code> accessing attributes without
 * versioning. This implementation uses singly-linked lists only, in contrast to
 * the original version which uses doubly-linked lists.
 * 
 * @author Jose Monte {monte@uni-koblenz.de} (original implementation)
 * @author Mahdi Derakhshanmanesh {manesh@uni-koblenz.de} (adjusted version)
 */
public abstract class EdgeImpl extends de.uni_koblenz.jgralab.impl.EdgeBaseImpl {
	/**
	 * The reference to the next {@link Edge}.
	 */
	private EdgeImpl nextEdge;

	/**
	 * The 'this' {@link Vertex}.
	 */
	private VertexBaseImpl incidentVertex;

	/**
	 * The next incident {@link Edge}.
	 */
	private IncidenceImpl nextIncidence;

	@Override
	public Edge getNextEdgeInGraph() {
		assert isValid();
		return this.nextEdge;
	}

	@Override
	public Edge getPrevEdgeInGraph() {
		assert isValid();

		Edge prevEdge = null;

		for (Edge currEdge = getGraph().getFirstEdgeInGraph(); currEdge != null; currEdge = currEdge
				.getNextEdgeInGraph()) {
			if (currEdge == this) {
				return prevEdge;
			}
			prevEdge = currEdge;
		}

		return null;
	}

	/**
	 * The constructor.
	 * 
	 * @param anId
	 *            The id of this {@link Edge}.
	 * @param graph
	 *            A reference to the {@link Graph}, this {@link Edge} shall be
	 *            added to.
	 * @param alpha
	 *            The starting {@link Vertex} node.
	 * @param omega
	 *            The ending {@link Vertex} node.
	 */
	protected EdgeImpl(int anId, Graph graph, Vertex alpha, Vertex omega) {
		super(anId, graph);
		((GraphImpl) graph).addEdge(this, alpha, omega);
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
	protected void setNextEdgeInGraph(Edge nextEdge) {
		this.nextEdge = (EdgeImpl) nextEdge;
	}

	@Override
	protected void setPrevEdgeInGraph(Edge prevEdge) {
		throw new UnsupportedOperationException(
				"Unsupported in savemem implementation.");
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
		throw new UnsupportedOperationException(
				"Unsupported in savemem implementation.");
	}

	@Override
	protected void setId(int id) {
		assert id >= 0;
		this.id = id;
	}
}
