package de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;

public class DFSTestVisitor extends SearchTestVisitor implements DFSVisitor {

	@Override
	public void leaveVertex(Vertex v) throws AlgorithmTerminatedException {
		if (v != currentRoot) {
			DepthFirstSearch alg = getAlgorithm();
			Vertex parent = alg.getInternalParent().get(v).getThis();
			IntFunction<Vertex> rnumber = alg.getInternalRnumber();
			assertTrue(rnumber.get(parent) < rnumber.get(v));
		}
	}

	@Override
	public void leaveTreeEdge(Edge e) throws AlgorithmTerminatedException {
		DepthFirstSearch alg = getAlgorithm();
		Vertex start = e.getThis();
		Vertex target = e.getThat();
		IntFunction<Vertex> level = alg.getInternalLevel();
		IntFunction<Vertex> number = alg.getInternalNumber();
		assertTrue(level.get(start) < level.get(target));
		assertTrue(number.get(start) < number.get(target));
	}

	@Override
	public void visitForwardArc(Edge e) throws AlgorithmTerminatedException {
		DepthFirstSearch alg = getAlgorithm();
		Vertex start = e.getThis();
		Vertex target = e.getThat();
		IntFunction<Vertex> number = alg.getInternalNumber();
		// IntFunction<Vertex> rnumber = alg.getInternalRnumber();
		assertTrue(number.get(start) < number.get(target));
		// assertTrue(rnumber.get(start) > rnumber.get(target));
	}

	@Override
	public void visitBackwardArc(Edge e) throws AlgorithmTerminatedException {
		DepthFirstSearch alg = getAlgorithm();
		Vertex start = e.getThis();
		Vertex target = e.getThat();
		IntFunction<Vertex> number = alg.getInternalNumber();
		// IntFunction<Vertex> rnumber = alg.getInternalRnumber();
		assertTrue(number.get(start) > number.get(target));
		// assertTrue(rnumber.get(start) < rnumber.get(target));
	}

	@Override
	public void visitCrosslink(Edge e) throws AlgorithmTerminatedException {
		DepthFirstSearch alg = getAlgorithm();
		Vertex start = e.getThis();
		Vertex target = e.getThat();
		IntFunction<Vertex> number = alg.getInternalNumber();
		// IntFunction<Vertex> rnumber = alg.getInternalRnumber();
		assertTrue(number.get(start) > number.get(target));
		// assertTrue(rnumber.get(start) > rnumber.get(target));
	}

	@Override
	public DepthFirstSearch getAlgorithm() {
		return (DepthFirstSearch) super.getAlgorithm();
	}

}
