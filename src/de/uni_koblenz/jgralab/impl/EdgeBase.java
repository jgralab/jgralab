package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public interface EdgeBase extends Edge {
	/**
	 * sets the alpha vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setAlpha(Vertex v);

	/**
	 * sets the omega vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setOmega(Vertex v);

	/**
	 * sets the this vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setThis(Vertex v);

	/**
	 * sets the that vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setThat(Vertex v);

	/**
	 * @return next edge in eSeq
	 */
	public EdgeBase getNextBaseEdge();

	/**
	 * @return previous edge in eSeq
	 */
	public EdgeBase getPrevBaseEdge();

	/**
	 * @return the next incidence object in iSeq of current vertex
	 */
	public EdgeBase getNextBaseIncidence();

	/**
	 * @return the previous incidence object in iSeq of current vertex
	 */
	public EdgeBase getPrevBaseIncidence();

}