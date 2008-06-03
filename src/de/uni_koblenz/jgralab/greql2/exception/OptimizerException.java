/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.exception;

import de.uni_koblenz.jgralab.greql2.optimizer.Optimizer;

/**
 * Should be thrown on errors in an {@link Optimizer}.
 * 
 * @author Tassilo Horn (heimdall), 2008, Diploma Thesis
 * 
 */
public class OptimizerException extends Exception {

	private static final long serialVersionUID = 8869420302056125072L;

	public OptimizerException(String message) {
		super(message);
	}

	public OptimizerException(String message, Exception cause) {
		super(message, cause);
	}
}
