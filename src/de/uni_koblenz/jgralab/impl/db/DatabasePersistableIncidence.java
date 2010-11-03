package de.uni_koblenz.jgralab.impl.db;

/**
 * An incidence that can be persisted to database.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public interface DatabasePersistableIncidence extends
		DatabasePersistableGraphElement {

	/**
	 * Gets id of incident vertex.
	 * 
	 * @return Id of incident vertex.
	 */
	int getIncidentVId();

	/**
	 * Sets id of incident vertex.
	 * 
	 * @param vId
	 *            Id of incident vertex.
	 */
	void setIncidentVId(int vId);

	/**
	 * Gets id of incident edge.
	 * 
	 * @return Id of incident edge.
	 */
	int getIncidentEId();

	/**
	 * Gets number mapping incidence's sequence in LambdaSeq of incident vertex.
	 * 
	 * @return Number mapping incidence's sequence in LambdaSeq of incident
	 *         vertex.
	 */
	long getSequenceNumberInLambdaSeq();

	/**
	 * Sets number mapping incidence's sequence in LambdaSeq of incident vertex.
	 * 
	 * @param sequenceNumber
	 *            Number mapping incidence's sequence in LambdaSeq of incident
	 *            vertex.
	 */
	void setSequenceNumberInLambdaSeq(long sequenceNumber);
}
