package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public interface InternalEdge extends Edge,
		InternalGraphElement<EdgeClass, Edge> {

	/**
	 * @return next edge in eSeq
	 */
	public InternalEdge getNextEdgeInESeq();

	/**
	 * @return previous edge in eSeq
	 */
	public InternalEdge getPrevEdgeInESeq();

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
	public InternalEdge getNextIncidenceInISeq();

	/**
	 * @return the previous incidence object in iSeq of current vertex
	 */
	public InternalEdge getPrevIncidenceInISeq();

	public void setIncidentVertex(Vertex v);

	public InternalVertex getIncidentVertex();

	public void setNextIncidenceInternal(InternalEdge nextIncidence);

	public void setPrevIncidenceInternal(InternalEdge prevIncidence);

}