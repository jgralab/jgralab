package de.uni_koblenz.jgralab.impl.db;

import de.uni_koblenz.jgralab.Vertex;

/**
 * A vertex which can be persisted to database.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public interface DatabasePersistableVertex extends
		DatabasePersistableGraphElement, Vertex {

	/**
	 * Gets number mapping vertex's sequence in VSeq of graph.
	 * 
	 * @return Number mapping vertex's sequence in VSeq of graph.
	 */
	long getSequenceNumberInVSeq();

	/**
	 * Sets number mapping vertex's sequence in VSeq of graph.
	 * 
	 * @param sequenceNumber
	 *            Number mapping vertex's sequence in VSeq of graph.
	 */
	void setSequenceNumberInVSeq(long sequenceNumber);

	/**
	 * Adds an incidence to incidence list of vertex without incrementing
	 * incidence list version.
	 * 
	 * @param eId
	 *            Id of incident edge.
	 * @param sequenceNumber
	 *            Number representing edge's sequence in incidence list.
	 */
	void addIncidence(int eId, long sequenceNumber);

	/**
	 * Sets incidence list version of vertex.
	 * 
	 * @param incidenceListVersion
	 *            Incidence list version.
	 */
	void setIncidenceListVersion(long incidenceListVersion);
}
