package de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;

public class DFSTestVisitor extends SearchTestVisitor implements DFSVisitor {

	protected List<Edge> forwardArcs;
	protected List<Edge> backwardArcs;
	protected List<Edge> crosslinks;
	protected List<Edge> leftTreeEdges;
	protected List<Vertex> leftVertices;

	public List<Edge> getForwardArcs() {
		return forwardArcs;
	}

	public List<Edge> getBackwardArcs() {
		return backwardArcs;
	}

	public List<Edge> getCrossLinks() {
		return crosslinks;
	}

	@Override
	public void leaveVertex(Vertex v) throws AlgorithmTerminatedException {
		// if (v != currentRoot) {
		// DepthFirstSearch alg = getAlgorithm();
		// Vertex parent = alg.getInternalParent().get(v).getThis();
		// IntFunction<Vertex> rnumber = alg.getInternalRnumber();
		// assertTrue(rnumber.get(parent) < rnumber.get(v));
		// }
		leftVertices.add(v);
	}

	@Override
	public void leaveTreeEdge(Edge e) throws AlgorithmTerminatedException {
		leftTreeEdges.add(e);
	}

	@Override
	public void visitForwardArc(Edge e) throws AlgorithmTerminatedException {
		forwardArcs.add(e);
	}

	@Override
	public void visitBackwardArc(Edge e) throws AlgorithmTerminatedException {
		backwardArcs.add(e);
	}

	@Override
	public void visitCrosslink(Edge e) throws AlgorithmTerminatedException {
		crosslinks.add(e);
	}

	@Override
	public DepthFirstSearch getAlgorithm() {
		return (DepthFirstSearch) super.getAlgorithm();
	}

	@Override
	public void reset() {
		super.reset();
		forwardArcs = new LinkedList<Edge>();
		backwardArcs = new LinkedList<Edge>();
		crosslinks = new LinkedList<Edge>();
		leftTreeEdges = new LinkedList<Edge>();
		leftVertices = new LinkedList<Vertex>();
	}

	@Override
	public void performPostTests() throws Exception {
		super.performPostTests();

		// check if all visited vertices have been left
		Set<Vertex> vertexSet = new HashSet<Vertex>();
		vertexSet.addAll(vertices);
		Set<Vertex> leftVertexSet = new HashSet<Vertex>();
		leftVertexSet.addAll(leftVertices);
		assertEquals("Not all visited vertices have been left.", vertexSet,
				leftVertexSet);

		// check if all visited edges have been left
		Set<Edge> treeEdgeSet = new HashSet<Edge>();
		treeEdgeSet.addAll(treeEdges);
		Set<Edge> leftTreeEdgeSet = new HashSet<Edge>();
		leftTreeEdgeSet.addAll(leftTreeEdges);
		assertEquals("Not all visited tree edges have been left.", treeEdgeSet,
				leftTreeEdgeSet);

		// check if forward arcs have only been visited once
		Set<Edge> forwardArcSet = new HashSet<Edge>();
		forwardArcSet.addAll(forwardArcs);
		assertEquals("Some forward arcs have been visited multiple times.",
				forwardArcSet.size(), forwardArcs.size());

		// check if backward arcs have only been visited once
		Set<Edge> backwardArcSet = new HashSet<Edge>();
		backwardArcSet.addAll(backwardArcs);
		assertEquals("Some backward arcs have been visited multiple times.",
				backwardArcSet.size(), backwardArcs.size());

		// check if crosslinks have only been visited once
		Set<Edge> crosslinkSet = new HashSet<Edge>();
		crosslinkSet.addAll(crosslinks);
		assertEquals("Some crosslinks have been visited multiple times.",
				crosslinkSet.size(), crosslinks.size());

		// check if the intersection of the different frond sets and the
		// complete frond set is empty and their union is identical to
		// the complete frond set
		Set<Edge> frondSet = new HashSet<Edge>();
		frondSet.addAll(fronds);
		Set<Edge> unionSet = new HashSet<Edge>();
		unionSet.addAll(forwardArcSet);
		unionSet.addAll(backwardArcSet);
		unionSet.addAll(crosslinkSet);
		assertEquals(
				"The union of the different frond sets is not equal to the whole set of fronds.",
				frondSet, unionSet);
		assertEquals(
				"The intersection of the different frond sets is not empty.",
				frondSet.size(), forwardArcSet.size() + backwardArcSet.size()
						+ crosslinkSet.size());

		IntFunction<Vertex> number = getAlgorithm().getNumber();
		IntFunction<Vertex> rnumber = getAlgorithm().getRnumber();
		// check if number and rnumber really match for tree edges
		for (Edge currentTreeEdge : treeEdges) {
			Vertex start = currentTreeEdge.getThis();
			Vertex target = currentTreeEdge.getThat();
			assertTrue("NUMBER constraint violated for tree edge "
					+ currentTreeEdge, number.get(start) < number.get(target));
			assertTrue("RNUMBER constraint violated for tree edge "
					+ currentTreeEdge, rnumber.get(start) > rnumber.get(target));
		}

		// check if number and rnumber really match for forward arcs
		for (Edge currentForwardArc : forwardArcs) {
			Vertex start = currentForwardArc.getThis();
			Vertex target = currentForwardArc.getThat();
			assertTrue("NUMBER constraint violated for forward arc "
					+ currentForwardArc, number.get(start) < number.get(target));
			assertTrue("RUMBER constraint violated for forward arc "
					+ currentForwardArc,
					rnumber.get(start) > rnumber.get(target));
		}

		// check if number and rnumber really match for backward arcs
		for (Edge currentBackwardArc : backwardArcs) {
			Vertex start = currentBackwardArc.getThis();
			Vertex target = currentBackwardArc.getThat();
			assertTrue("NUMBER constraint violated for backward arc "
					+ currentBackwardArc,
					number.get(start) > number.get(target));
			assertTrue("RNUMBER constraint violated for backward arc "
					+ currentBackwardArc,
					rnumber.get(start) < rnumber.get(target));
		}

		// check if number and rnumber really match for crosslinks
		for (Edge currentCrosslink : crosslinks) {
			Vertex start = currentCrosslink.getThis();
			Vertex target = currentCrosslink.getThat();
			assertTrue("NUMBER constraint violated for crosslink "
					+ currentCrosslink, number.get(start) > number.get(target));
			assertTrue("RNUMBER constraint violated for crosslink "
					+ currentCrosslink,
					rnumber.get(start) > rnumber.get(target));
		}
	}

}
