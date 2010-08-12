package de.uni_koblenz.jgralab.algolib.algorithms;

public enum AlgorithmStates {
	/**
	 * In this state parameters may be changed and the algorithm can be started.
	 */
	INITIALIZED,

	/**
	 * In this state, no changes may be done to the algorithm. If it is
	 * supported, it can be interrupted using the method
	 * <code>Thread.interrupt()</code>.
	 */
	RUNNING,

	/**
	 * In this state the results can be obtained from the algorithm. For reusing
	 * the algorithm object, the method <code>reset</code> has to be called for
	 * reentering the state <code>INITIALIZED</code>.
	 */
	FINISHED,

	/**
	 * In this state the results can be obtained. Also a resuming of the
	 * algorithm with other parameters is possible. The results from the
	 * previous runs are used for the next run. It depends on the algorithm if
	 * this feature is possible, feasible and how it is exploited.
	 */
	STOPPED,

	/**
	 * In this state, the algorithm has been interrupted. No changes to
	 * parameters and no retrieval of results is possible. For reusing the
	 * algorithm object, the method <code>reset</code> has to be called for
	 * reentering the state <code>INITIALIZED</code>.
	 */
	CANCELED;
}
