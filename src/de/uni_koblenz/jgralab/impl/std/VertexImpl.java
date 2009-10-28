package de.uni_koblenz.jgralab.impl.std;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;

/**
 * The implementation of a <code>Vertex</code> accessing attributes without
 * versioning.
 * 
 * @author José Monte(monte@uni-koblenz.de)
 */
public abstract class VertexImpl extends de.uni_koblenz.jgralab.impl.VertexImpl {
	private VertexImpl nextVertex;
	private VertexImpl prevVertex;
	private IncidenceImpl firstIncidence;
	private IncidenceImpl lastIncidence;

	/**
	 * holds the version of the vertex structure, for every modification of the
	 * structure (e.g. adding or deleting an incident edge or changing the
	 * incidence sequence) this version number is increased by one. It is set to
	 * 0 when the vertex is created or the graph is loaded.
	 */
	private long incidenceListVersion = 0;

	@Override
	public Vertex getNextVertex() {
		assert isValid();
		return nextVertex;
	}

	@Override
	protected IncidenceImpl getFirstIncidence() {
		return firstIncidence;
	}

	@Override
	protected IncidenceImpl getLastIncidence() {
		return lastIncidence;
	}

	@Override
	protected void setNextVertex(Vertex nextVertex) {
		this.nextVertex = (VertexImpl) nextVertex;
	}

	@Override
	protected void setPrevVertex(Vertex prevVertex) {
		this.prevVertex = (VertexImpl) prevVertex;
	}

	@Override
	public Vertex getPrevVertex() {
		return prevVertex;
	}

	@Override
	protected void setFirstIncidence(IncidenceImpl firstIncidence) {
		this.firstIncidence = firstIncidence;
	}

	@Override
	protected void setLastIncidence(IncidenceImpl lastIncidence) {
		this.lastIncidence = lastIncidence;
	}

	@Override
	protected void setIncidenceListVersion(long incidenceListVersion) {
		this.incidenceListVersion = incidenceListVersion;
	}

	@Override
	public long getIncidenceListVersion() {
		return incidenceListVersion;
	}

	/**
	 * 
	 * @param id
	 * @param graph
	 */
	protected VertexImpl(int id, Graph graph) {
		super(id, graph);
	}

	@Override
	protected void setId(int id) {
		assert id >= 0;
		this.id = id;
	}
}
