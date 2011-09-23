package de.uni_koblenz.jgralab;

public interface EdgeBase extends Edge {
	/**
	 * sets the alpha vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setAlpha(VertexBase v);

	/**
	 * sets the omega vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setOmega(VertexBase v);

	/**
	 * sets the this vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setThis(VertexBase v);

	/**
	 * sets the that vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setThat(VertexBase v);
}