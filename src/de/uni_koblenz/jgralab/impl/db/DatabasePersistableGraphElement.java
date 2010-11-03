package de.uni_koblenz.jgralab.impl.db;

/**
 * A graph element that can be persisted to database.
 */
public interface DatabasePersistableGraphElement extends DatabasePersistable {

	/**
	 * Gets primary key of graph in database.
	 * 
	 * @return Primary key of graph in database.
	 */
	int getGId();
}
