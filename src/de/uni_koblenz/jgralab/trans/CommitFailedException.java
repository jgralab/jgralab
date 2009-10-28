package de.uni_koblenz.jgralab.trans;

/**
 * This exception indicates, that the commit of a <code>Transaction</code>
 * failed.
 * 
 * @author José Monte(monte@uni-koblenz.de)
 */
public class CommitFailedException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param transaction
	 *            the <code>Transaction</code> whose commit failed
	 * @param reason
	 *            the reason, why the commit failed
	 */
	public CommitFailedException(Transaction transaction, String reason) {
		super("Commit failed for the transaction " + transaction + ". "
				+ "\nReason: " + reason);
	}
}
