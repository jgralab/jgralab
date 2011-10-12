package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public interface InternalEdge extends Edge, InternalGraphElement {

	/**
	 * @return next edge in eSeq
	 */
	public InternalEdge getNextBaseEdge();

	/**
	 * @return previous edge in eSeq
	 */
	public InternalEdge getPrevBaseEdge();

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
	public InternalEdge getNextBaseIncidence();

	/**
	 * @return the previous incidence object in iSeq of current vertex
	 */
	public InternalEdge getPrevBaseIncidence();

	public void setIncidentVertex(Vertex v);

	public InternalVertex getIncidentVertex();

	public void setNextIncidenceInternal(InternalEdge nextIncidence);

	public void setPrevIncidenceInternal(InternalEdge prevIncidence);

}