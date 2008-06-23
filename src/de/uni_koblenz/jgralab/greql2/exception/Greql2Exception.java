/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.exception;

/**
 * @author horn
 * 
 * Base class for all GReQL2 related exceptions.
 */
public class Greql2Exception extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2914800888476896758L;

	public Greql2Exception(String message) {
		super(message);
	}

	public Greql2Exception(String message, Exception cause) {
		super(message, cause);
	}

}
