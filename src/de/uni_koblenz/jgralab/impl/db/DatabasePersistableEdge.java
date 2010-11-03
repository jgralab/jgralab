package de.uni_koblenz.jgralab.impl.db;

import de.uni_koblenz.jgralab.Edge;

/**
 * An edge which can be persisted to database.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public interface DatabasePersistableEdge extends
		DatabasePersistableGraphElement, DatabasePersistableIncidence, Edge {

	/**
	 * Gets number mapping edge's sequence in ESeq of graph.
	 * 
	 * @return Number mapping edge's sequence in ESeq of graph.
	 */
	long getSequenceNumberInESeq();

	/**
	 * Sets number mapping edge's sequence in ESeq of graph.
	 * 
	 * @param sequenceNumberInEseq
	 *            Number mapping edge's sequence in ESeq of graph.
	 */
	void setSequenceNumberInESeq(long sequenceNumber);
}
