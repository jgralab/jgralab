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
	 * Get the Tracker for this GraphElement
	 * 
	 * @return The Tracker that tracks this GraphElement
	 */
	public VertexTracker getTracker() {
		return ((GraphImpl) this.graph).getStorage().getVertexTracker(this.id);
	}

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
		if (this.nextVertexId == 0) {
			return null;
		}
		return (InternalVertex) this.getGraph().getVertex(nextVertexId);
	}

	@Override
	public InternalVertex getPrevVertexInVSeq() {
		assert isValid() : this + " is not valid.";
		if (this.prevVertexId == 0) {
			return null;
		}
		return (InternalVertex) this.getGraph().getVertex(prevVertexId);
	}

	@Override
	public InternalEdge getFirstIncidenceInISeq() {
		if (firstIncidenceId < 0) {
			return (InternalEdge) this.getGraph()
					.getEdge(-1 * firstIncidenceId).getReversedEdge();
		} else if (firstIncidenceId == 0) {
			return null;
		} else {
			return (InternalEdge) this.getGraph().getEdge(firstIncidenceId);
		}

	}

	@Override
	public InternalEdge getLastIncidenceInISeq() {
		if (lastIncidenceId < 0) {
			return (InternalEdge) this.getGraph().getEdge(-lastIncidenceId)
					.getReversedEdge();
		} else if (lastIncidenceId == 0) {
			return null;
		} else {
			return (InternalEdge) this.getGraph().getEdge(lastIncidenceId);
		}
	}

	@Override
	public void setNextVertex(Vertex nextVertex) {
		if (nextVertex == null) {
			this.nextVertexId = 0;
		} else {
			this.nextVertexId = nextVertex.getId();
		}
		getTracker().putVariable(4, this.nextVertexId);
	}

	@Override
	public void setPrevVertex(Vertex prevVertex) {
		if (prevVertex == null) {
			this.prevVertexId = 0;
		} else {
			this.prevVertexId = prevVertex.getId();
		}
		getTracker().putVariable(12, this.prevVertexId);
	}

	@Override
	public void setFirstIncidence(InternalEdge firstIncidence) {
		if (firstIncidence == null) {
			this.firstIncidenceId = 0;
		} else {
			this.firstIncidenceId = firstIncidence.getId();
			// System.err.println("VertexImpl.setFirstIncidence: looking "
			// + this
			// + " up in the graph: "
			// + ((GraphImpl) this.graph).getStorage().getVertexFromArray(
			// this.id));
		}

		getTracker().putVariable(20, this.firstIncidenceId);
	}

	@Override
	public void setLastIncidence(InternalEdge lastIncidence) {
		if (lastIncidence == null) {
			this.lastIncidenceId = 0;
		} else {
			this.lastIncidenceId = lastIncidence.getId();
		}
		// System.err.println("VertexImpl.setLastIncidence: looking "
		// + this
		// + " up in the graph: "
		// + ((GraphImpl) this.graph).getStorage().getVertexFromArray(
		// this.id));
		getTracker().putVariable(28, this.lastIncidenceId);
	}

	@Override
	public void setIncidenceListVersion(long incidenceListVersion) {
		this.incidenceListVersion = incidenceListVersion;
		// System.err.println("VertexImpl.setIncidenceListVersion: looking "
		// + this
		// + " up in the graph: "
		// + ((GraphImpl) this.graph).getStorage().getVertexFromArray(
		// this.id));
		getTracker().putVariable(36, this.incidenceListVersion);
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
		// ((GraphImpl) graph).addVertex(this);
	}

	@Override
	public void setId(int id) {
		assert id >= 0;
		this.id = id;
	}

	public int getNextVertexId() {
		return nextVertexId;
	}

	public void restoreNextVertexId(int id) {
		this.nextVertexId = id;
	}

	public int getPrevVertexId() {
		return prevVertexId;
	}

	public void restorePrevVertexId(int id) {
		this.prevVertexId = id;
	}

	public int getFirstIncidenceId() {
		return firstIncidenceId;
	}

	public void restoreFirstIncidenceId(int id) {
		this.firstIncidenceId = id;
	}

	public int getLastIncidenceId() {
		return lastIncidenceId;
	}

	public void restoreLastIncidenceId(int id) {
		this.lastIncidenceId = id;
	}

	public void restoreIncidenceListVersion(long version) {
		this.incidenceListVersion = version;
	}

	/**
	 * Called whenever a primitive attribute of this GraphElement changed so the
	 * new attribute value is stored in the Tracker.
	 */
	public void attributeChanged() {
		VertexTracker tracker = getTracker();
		if (tracker != null) {
			tracker.storeAttributes(this);
		}
	}

	/**
	 * Called whenever a String of this GraphElement changed so the new String
	 * is stored in the Tracker.
	 */
	public void stringChanged() {
		VertexTracker tracker = getTracker();
		if (tracker != null) {
			tracker.storeStrings(this);
		}
	}

	/**
	 * Called whenever a List of this GraphElement changed so the new list value
	 * is stored in the Tracker.
	 */
	public void listChanged() {
		VertexTracker tracker = getTracker();
		if (tracker != null) {
			tracker.storeLists(this);
		}
	}

	@Override
	public int hashCode() {
		return this.id;
	}

}
