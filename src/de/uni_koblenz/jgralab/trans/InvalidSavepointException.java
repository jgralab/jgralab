package de.uni_koblenz.jgralab.trans;

/**
 * This exception indicates, that a <code>Savepoint</code> to be restored is
 * invalid for a <code>Transaction</code>.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public class InvalidSavepointException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param transaction
	 *            the <code>Transaction</code> for which <code>savepoint</code>
	 *            should be restored
	 * @param savepoint
	 *            to <code>Savepoint</code> to be restored
	 */
	public InvalidSavepointException(Transaction transaction,
			Savepoint savepoint) {
		super("Invalid save-point " + savepoint + " for the transaction "
				+ transaction + ".");
	}
}
