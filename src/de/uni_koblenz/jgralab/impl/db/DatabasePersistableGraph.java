package de.uni_koblenz.jgralab.impl.db;

import de.uni_koblenz.jgralab.Graph;

/**
 * Defines interface of a graph which can be persistent in a graph database.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public interface DatabasePersistableGraph extends DatabasePersistable, Graph {

	/**
	 * Sets primary key of graph in database. When retrieving a graph from
	 * database, always set it's primary key as last.
	 * 
	 * @param primaryKey
	 *            Primary key of graph in database.
	 */
	void setGId(int gId);

	/**
	 * Adds minimal vertex data.
	 * 
	 * @param vId
	 *            Id of vertex in graph.
	 * @param sequenceNumber
	 *            Number mapping vertex's sequence in VSeq.
	 */
	void addVertex(int vId, long sequenceNumber);

	/**
	 * Adds minimal edge data.
	 * 
	 * @param eId
	 *            Id of edge in graph.
	 * @param sequenceNumber
	 *            Number mapping edge's sequence in ESeq.
	 */
	void addEdge(int eId, long sequenceNumber);

	/**
	 * Deletes graph from database.
	 * 
	 * @throws GraphDatabaseException
	 *             Deletion not successful.
	 */
	void delete() throws GraphDatabaseException;
}
