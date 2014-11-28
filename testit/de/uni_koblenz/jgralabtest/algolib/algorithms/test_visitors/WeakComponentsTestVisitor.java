package de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.weak_components.WeakComponentsWithBFS;
import de.uni_koblenz.jgralab.algolib.algorithms.weak_components.visitors.VertexPartitionVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.Function;

public class WeakComponentsTestVisitor extends VertexPartitionVisitorAdapter {

	private List<Vertex> representativeVertexList;

	private Set<Vertex> representativeVertices;

	private WeakComponentsWithBFS alg;

	@Override
	public void reset() {
		super.reset();
		representativeVertexList = new LinkedList<>();

		representativeVertices = new HashSet<>();
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
		this.alg = (WeakComponentsWithBFS) alg;
	}

	public void performPostTests() throws Exception {
		// check if all representative vertices have only been visited once
		assertTrue(representativeVertices.size() == representativeVertexList
				.size());

		Function<Vertex, Vertex> strongComponents = alg.getWeakComponents();
		for (Vertex v : alg.getGraph().vertices()) {
			Vertex currentStrongComponent = strongComponents.get(v);
			// check if all vertices are part of a strong component
			assertNotNull(currentStrongComponent);

			// check if all representative vertices are part of the set of
			// representative vertices
			assertTrue(representativeVertices.contains(currentStrongComponent));

		}
	}
}
