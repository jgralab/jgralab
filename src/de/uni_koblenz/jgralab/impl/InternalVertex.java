package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.impl.DirectedM1EdgeClass;

public interface InternalVertex extends Vertex, InternalGraphElement {

	/**
	 * @return the internal vertex structure version
	 * @see #isIncidenceListModified(long)
	 */
	public long getIncidenceListVersion();

	/**
	 * Must be called by all methods which manipulate the incidence list of this
	 * Vertex.
	 */
	public abstract void incidenceListModified();

	/**
	 * Checks if the list of incident edges has changed with respect to the
	 * given <code>incidenceListVersion</code>.
	 */
	public boolean isIncidenceListModified(long incidenceListVersion);

	/**
	 * tests if the Edge <code>edge</code> may start at this vertex
	 * 
	 * @return <code>true</code> iff <code>edge</code> may start at this vertex
	 */
	public boolean isValidAlpha(Edge edge);

	/**
	 * tests if the Edge <code>edge</code> may end at this vertex
	 * 
	 * @return <code>true</code> iff <code>edge</code> may end at this vertex
	 */
	public boolean isValidOmega(Edge edge);

	public DirectedM1EdgeClass getEdgeForRolename(String rolename);

	/**
	 * @return the next vertex in vSeq
	 */
	public InternalVertex getNextVertexInVSeq();

	/**
	 * @return the previous vertex in vSeq
	 */
	public InternalVertex getPrevVertexInVSeq();

	public void setNextVertex(Vertex nextVertex);

	public void setPrevVertex(Vertex prevVertex);

	/**
	 * @return first incident edge of this vertex
	 */
	public InternalEdge getFirstIncidenceInISeq();

	/**
	 * @return last incident edge of this vertex
	 */
	public InternalEdge getLastIncidenceInISeq();

	public void setFirstIncidence(InternalEdge firstIncidence);

	public void setLastIncidence(InternalEdge lastIncidence);

	public void appendIncidenceToISeq(InternalEdge i);

	public void removeIncidenceFromISeq(InternalEdge i);

	public void putIncidenceBefore(InternalEdge target, InternalEdge moved);

	public void putIncidenceAfter(InternalEdge target, InternalEdge moved);

	public void setIncidenceListVersion(long incidenceListVersion);

}