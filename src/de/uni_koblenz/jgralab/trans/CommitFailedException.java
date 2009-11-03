package de.uni_koblenz.jgralab.trans;

//import java.util.Set;

//import de.uni_koblenz.jgralab.graphvalidator.ConstraintViolation;

/**
 * This exception indicates, that the commit of a <code>Transaction</code>
 * failed.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public class CommitFailedException extends Exception {
	private static final long serialVersionUID = 1L;
	//private Set<ConstraintViolation> constraintViolations;

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

	/**
	 * 
	 * @param transaction
	 *            the <code>Transaction</code> whose commit failed
	 * @param reason
	 *            the reason, why the commit failed
	 */
	/*public CommitFailedException(Transaction transaction,
			Set<ConstraintViolation> constraintViolations) {
		super("Commit failed for the transaction " + transaction + " because the graph is inconsistent.");
		this.constraintViolations = constraintViolations;
	}*/
	
	/**
	 * 
	 * @return
	 */
	/*public Set<ConstraintViolation> getConstraintViolations() {
		return constraintViolations;
	}*/
}
