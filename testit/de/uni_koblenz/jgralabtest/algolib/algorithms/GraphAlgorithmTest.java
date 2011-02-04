package de.uni_koblenz.jgralabtest.algolib.algorithms;

public abstract class GraphAlgorithmTest {

	/**
	 * Tests the algorithm with the whole graph.
	 */
	public abstract void testAlgorithm();

	/**
	 * Tests if the vertex count is computed correctly.
	 */
	public abstract void testGetVertexCount();

	/**
	 * Tests if the edge count is computed correctly.
	 */
	public abstract void testGetEdgeCount();

	/**
	 * Tests the states and the state transitions.
	 */
	public abstract void testStates();

	/**
	 * Tests if early termination works correctly.
	 */
	public abstract void testEarlyTermination();

	/**
	 * Tests if thread interruption works correctly.
	 */
	public abstract void testCancel();

	/*
	 * Tests if addVisitor works correctly and that visitors are not added multiply.
	 */
	// public abstract void testAddVisitor();

	/**
	 * Tests addVisitor for throwing an exception if the algorithm is not
	 * compatible with the visitor (it acctually tests setAlgorithm from
	 * visitor).
	 */
	public abstract void testAddVisitorForException();

	/**
	 * Tests if remove visitor actually removes the visitors from the list.
	 */
	public abstract void testRemoveVisitor();
	
	/**
	 * Tests if all parameters have been resetted to their default values.
	 */
	public abstract void testResetParameters();
	
	/**
	 * Tests if all runtime variables have been initialized correctly.
	 */
	public abstract void testReset();

}
