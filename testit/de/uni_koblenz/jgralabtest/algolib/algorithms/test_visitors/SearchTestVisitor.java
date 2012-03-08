package de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;

public class SearchTestVisitor extends SearchVisitorAdapter {

	protected Vertex currentRoot;

	protected List<Vertex> vertices;
	protected List<Edge> edges;
	protected List<Edge> treeEdges;
	protected List<Edge> fronds;

	public List<Vertex> getVertices() {
		return vertices;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public List<Edge> getTreeEdges() {
		return treeEdges;
	}

	public List<Edge> getFronds() {
		return fronds;
	}

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
		vertices.add(v);
	}

	@Override
	public void visitEdge(Edge e) throws AlgorithmTerminatedException {
		assertFalse("Edge " + e + " already visited.", getAlgorithm()
				.getVisitedEdges().get(e));
		edges.add(e);
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
		treeEdges.add(e);
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
		fronds.add(e);
	}

	@Override
	public void reset() {
		currentRoot = null;
		vertices = new LinkedList<Vertex>();
		edges = new LinkedList<Edge>();
		treeEdges = new LinkedList<Edge>();
		fronds = new LinkedList<Edge>();
	}

	public void performPostTests() throws Exception {
		// check if all vertices have been visited only once
		Set<Vertex> vertexSet = new HashSet<Vertex>();
		vertexSet.addAll(vertices);
		assertEquals("Some vertices have been visited multiple times."
				+ vertexSet.size(), vertexSet.size(), vertices.size());

		// check if all edges have been visited only once
		Set<Edge> edgeSet = new HashSet<Edge>();
		edgeSet.addAll(edges);
		assertEquals("Some edges have been visited multiple times.",
				edgeSet.size(), edges.size());

		// check if all tree edges have been visited only once
		Set<Edge> treeEdgeSet = new HashSet<Edge>();
		treeEdgeSet.addAll(treeEdges);
		assertEquals("Some tree edges have been visited multiple times.",
				treeEdgeSet.size(), treeEdges.size());

		// check if all fronds have been visited only once
		Set<Edge> frondSet = new HashSet<Edge>();
		frondSet.addAll(fronds);
		assertEquals("Some fronds have been visited multiple times.",
				frondSet.size(), fronds.size());

		// check if the intersection of the tree edge- and frond sets are empty
		// and their union is identical with the complete edge set
		Set<Edge> unionSet = new HashSet<Edge>();
		unionSet.addAll(treeEdgeSet);
		unionSet.addAll(frondSet);
		assertEquals(
				"The union of tree edges and fronds is not equal to the whole set of visited edges.",
				edgeSet, unionSet);
		assertEquals(
				"The intersection of the tree edge set and the frond set is not empty.",
				edgeSet.size(), treeEdgeSet.size() + frondSet.size());

		// check if the correct level was computed for all visited vertices
		for (Edge currentTreeEdge : treeEdges) {
			Vertex parent = currentTreeEdge.getThis();
			Vertex v = currentTreeEdge.getThat();
			IntFunction<Vertex> level = getAlgorithm().getLevel();
			assertTrue("Parent level is higher or equal to element level for "
					+ v, level.get(parent) < level.get(v));
			assertEquals(
					"The level difference of element level and parent level is bigger than 1 for "
							+ v, 1, level.get(v) - level.get(parent));
		}
	}
}
