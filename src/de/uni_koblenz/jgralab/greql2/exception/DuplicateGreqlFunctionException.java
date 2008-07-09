package de.uni_koblenz.jgralab.greql2.exception;

/**
 * This function is thrown if a class should be registered as
 * a GReQL function but there is already a GReQL function with 
 * the same name available in the function library.
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de>
 *
 */
public class DuplicateGreqlFunctionException extends RuntimeException {
	

	private static final long serialVersionUID = -5682985318690802997L;

	public DuplicateGreqlFunctionException(String message) {
		super(message);
	}

}
