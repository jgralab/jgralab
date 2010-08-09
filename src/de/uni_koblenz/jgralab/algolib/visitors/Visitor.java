package de.uni_koblenz.jgralab.algolib.visitors;

import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;

public interface Visitor {
	
	/**
	 * If this visitor is used for computing results, this function resets this
	 * visitor. This includes reinitializing all variables needed for the
	 * computation of the result. If this visitor does not contain variables
	 * that need to be reinitialized, this method may do nothing.
	 */
	public void reset();

	/**
	 * Sets the graph algorithm this visitor is used by. This method is to be
	 * used for setting a field in the actual visitor object for allowing access
	 * to the intermediate results of the algorithm. This is required by many
	 * visitors for performing their own computations based on the computations
	 * made by the algorithm. The field of implementing visitor classes should
	 * use an explicit algorithm type and this method should perform a type
	 * check. This avoids unnecessary casts when accessing the visitor objects.
	 * 
	 * @param alg
	 *            the algorithm object that uses this visitor
	 * @throws IllegalArgumentException
	 *             if the given algorithm is incompatible with this visitor
	 */
	public void setAlgorithm(GraphAlgorithm alg);
}
