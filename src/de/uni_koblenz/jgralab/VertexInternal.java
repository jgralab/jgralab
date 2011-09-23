package de.uni_koblenz.jgralab;

public interface VertexInternal {

	/**
	 * Must be called by all methods which manipulate the incidence list of this
	 * Vertex.
	 */
	public abstract void incidenceListModified();

}