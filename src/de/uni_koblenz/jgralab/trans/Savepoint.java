package de.uni_koblenz.jgralab.trans;

/**
 * This interface represents a save-point for a <code>Transaction</code>.
 * 
 * A save-point can be defined by calling the method
 * <code>{@link Transaction#defineSavepoint() defineSavepoint}</code> of a
 * <code>Transaction</code>.
 * 
 * A save-point can be restored within the <code>Transaction</code> ==
 * <code>{@link #getTransaction() getTransaction}</code> by calling the method
 * {@link Transaction#restoreSavepoint(Savepoint savepoint) restoreSavepoint}
 * </code> of the corresponding <code>Transaction</code>.
 * 
 * @author José Monte(monte@uni-koblenz.de)
 */
public interface Savepoint {
	/**
	 * 
	 * @return the <code>Transaction</code> the save-point belongs to
	 */
	public Transaction getTransaction();

	/**
	 * 
	 * @return the ID of the save-point
	 */
	public int getID();

	/**
	 * A save-point is true if
	 * <code>{@link Transaction#getSavepoints() Transaction.getSavepoints()}</code>
	 * .contains(this) == true.
	 * 
	 * @return if the save-point is still valid within the corresponding
	 *         <code>Transaction</code>.
	 */
	public boolean isValid();
}
