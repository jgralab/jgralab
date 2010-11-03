package de.uni_koblenz.jgralab.impl.db;

/**
 * A database persistable Element.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public interface DatabasePersistable {

	/**
	 * Checks if persistable has already been persisted to database.
	 * 
	 * @return true if persistable is persistent in database, false otherwise.
	 */
	boolean isPersistent();

	/**
	 * Sets whether a persistable has been persisted.
	 * 
	 * @param persistent
	 *            Persistance status.
	 */
	void setPersistent(boolean persistent);

	/**
	 * Checks if graph element has been initialized and ready to be used.
	 * 
	 * @return true if graph element is initialized and ready to be used, false
	 *         otherwise.
	 */
	boolean isInitialized();

	/**
	 * Sets whether a graph element has been initialized and ready to be used.
	 * 
	 * @param initialized
	 *            Initialization status.
	 */
	void setInitialized(boolean initialized);

	/**
	 * Notifies persistable that it has been deleted.
	 */
	void deleted();

	/**
	 * Gets primary key of graph in database.
	 * 
	 * @return Primary key of graph in database or -1 if graph has not been
	 *         persisted yet.
	 */
	int getGId();
}
