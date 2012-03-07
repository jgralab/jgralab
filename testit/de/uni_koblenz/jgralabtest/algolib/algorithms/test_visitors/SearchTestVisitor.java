package de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;

public class SearchTestVisitor extends SearchVisitorAdapter {

	protected Vertex currentRoot;

	@Override
	public void visitRoot(Vertex v) throws AlgorithmTerminatedException {
		currentRoot = v;
		SearchAlgorithm alg = getAlgorithm();
		assertFalse("The root has already been visited.", alg
				.getVisitedVertices().get(v));
		assertNull("The root " + v + " has a parent vertex.", alg
				.getInternalParent().get(v));
	}

	@Override
	public void visitVertex(Vertex v) throws AlgorithmTerminatedException {
		SearchAlgorithm alg = getAlgorithm();
		BooleanFunction<Vertex> visitedVertices = alg.getVisitedVertices();
		assertFalse("The vertex " + v + " has already been visited.",
				visitedVertices.get(v));
		if (v != currentRoot) {
			BooleanFunction<Edge> visitedEdges = alg.getVisitedEdges();
			Function<Vertex, Edge> parent = alg.getInternalParent();
			Edge currentParent = parent.get(v);
			assertTrue("The parent edge " + currentParent
					+ " has not been visited.", visitedEdges.get(currentParent));
			Vertex currentParentVertex = currentParent.getThis();
			assertTrue("The parent vertex " + v + " has not been visited.",
					visitedVertices.get(currentParentVertex));
		}
	}

	@Override
	public void visitEdge(Edge e) throws AlgorithmTerminatedException {
		assertFalse("Edge " + e + " already visited.", getAlgorithm()
				.getVisitedEdges().get(e));
	}

	@Override
	public void visitTreeEdge(Edge e) throws AlgorithmTerminatedException {
		SearchAlgorithm alg = getAlgorithm();
		BooleanFunction<Vertex> visitedVertices = alg.getVisitedVertices();
		Vertex start = e.getThis();
		assertTrue("Start vertex " + start + " not visited.",
				visitedVertices.get(start));
		Vertex target = e.getThat();
		assertFalse("Target vertex " + target + " already visited.",
				visitedVertices.get(target));
	}

	@Override
	public void visitFrond(Edge e) throws AlgorithmTerminatedException {
		SearchAlgorithm alg = getAlgorithm();
		BooleanFunction<Vertex> visitedVertices = alg.getVisitedVertices();
		Vertex start = e.getThis();
		assertTrue("Start vertex " + start + " not visited.",
				visitedVertices.get(start));
		Vertex target = e.getThat();
		assertTrue("Target vertex " + target + " not visited.",
				visitedVertices.get(target));
	}

	@Override
	public void reset() {
		currentRoot = null;
	}

}
