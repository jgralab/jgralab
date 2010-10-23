/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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

	// private Set<ConstraintViolation> constraintViolations;

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
	/*
	 * public CommitFailedException(Transaction transaction,
	 * Set<ConstraintViolation> constraintViolations) {
	 * super("Commit failed for the transaction " + transaction +
	 * " because the graph is inconsistent."); this.constraintViolations =
	 * constraintViolations; }
	 */

	/**
	 * 
	 * @return
	 */
	/*
	 * public Set<ConstraintViolation> getConstraintViolations() { return
	 * constraintViolations; }
	 */
}
