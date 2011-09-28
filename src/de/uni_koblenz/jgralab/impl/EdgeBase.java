package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public interface EdgeBase extends Edge {

	/**
	 * @return next edge in eSeq
	 */
	public EdgeBase getNextBaseEdge();

	/**
	 * @return previous edge in eSeq
	 */
	public EdgeBase getPrevBaseEdge();

	/**
	 * @param nextEdge
	 */
	public void setNextEdgeInGraph(Edge nextEdge);

	/**
	 * @param prevEdge
	 */
	public void setPrevEdgeInGraph(Edge prevEdge);

	/**
	 * @return the next incidence object in iSeq of current vertex
	 */
	public EdgeBase getNextBaseIncidence();

	/**
	 * @return the previous incidence object in iSeq of current vertex
	 */
	public EdgeBase getPrevBaseIncidence();

	public void setIncidentVertex(Vertex v);

	public VertexBase getIncidentVertex();

	public void setNextIncidenceInternal(EdgeBase nextIncidence);

	public void setPrevIncidenceInternal(EdgeBase prevIncidence);

}