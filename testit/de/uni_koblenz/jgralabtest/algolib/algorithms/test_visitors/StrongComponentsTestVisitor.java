package de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.strong_components.StrongComponentsWithDFS;
import de.uni_koblenz.jgralab.algolib.algorithms.strong_components.visitors.ReducedGraphVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.Function;

public class StrongComponentsTestVisitor extends ReducedGraphVisitorAdapter {

	private List<Vertex> representativeVertexList;
	private List<Edge> reducedEdgeList;

	private Set<Vertex> representativeVertices;
	private Set<Edge> reducedEdges;

	private StrongComponentsWithDFS alg;

	@Override
	public void reset() {
		super.reset();
		representativeVertexList = new LinkedList<Vertex>();
		reducedEdgeList = new LinkedList<Edge>();

		representativeVertices = new HashSet<Vertex>();
		reducedEdges = new HashSet<Edge>();
	}

	@Override
	public void visitReducedEdge(Edge e) {
		super.visitReducedEdge(e);
		reducedEdgeList.add(e);
		reducedEdges.add(e);
	}

	@Override
	public void visitRepresentativeVertex(Vertex v) {
		super.visitRepresentativeVertex(v);
		representativeVertexList.add(v);
		representativeVertices.add(v);
	}

	@Override
	public void setAlgorithm(GraphAlgorithm alg) {
		super.setAlgorithm(alg);
		reset();
		this.alg = (StrongComponentsWithDFS) alg;
	}

	public void performPostTests() throws Exception {
		// check if all representative vertices have only been visited once
		assertTrue(representativeVertices.size() == representativeVertexList
				.size());

		// check if all reduced edges have been visited once
		assertTrue(reducedEdges.size() == reducedEdgeList.size());

		Function<Vertex, Vertex> strongComponents = alg.getStrongComponents();
		for (Vertex v : alg.getGraph().vertices()) {
			Vertex currentStrongComponent = strongComponents.get(v);
			// check if all vertices are part of a strong component
			assertNotNull(currentStrongComponent);

			// check if all representative vertices are part of the set of
			// representative vertices
			assertTrue(representativeVertices.contains(currentStrongComponent));

		}

		for (Edge e : alg.getGraph().edges()) {
			Vertex alphaRep = strongComponents.get(e.getAlpha());
			Vertex omegaRep = strongComponents.get(e.getOmega());
			if (reducedEdges.contains(e)) {
				// check if start and end point of reduced edges are in
				// different strong components
				assertNotSame(alphaRep, omegaRep);
			} else {
				// check if start and end point of non-reduced edges are in the
				// same strong component
				assertSame(alphaRep, omegaRep);
			}
		}
	}
}
