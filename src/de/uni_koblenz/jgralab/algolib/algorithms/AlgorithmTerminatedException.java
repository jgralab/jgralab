package de.uni_koblenz.jgralab.algolib.algorithms;

public class AlgorithmTerminatedException extends RuntimeException {

	/**
	 * This exception signals the early termination of an algorithm. It is
	 * useful for terminating algorithms that operate on huge graphs before they
	 * are completely done, if the required result is already computed. In order
	 * to exploit it, it has to be caught.
	 */
	private static final long serialVersionUID = 1L;

	public AlgorithmTerminatedException(String message) {
		super(message);
	}

	public AlgorithmTerminatedException() {
		super();
	}

}
