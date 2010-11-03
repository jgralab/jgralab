package de.uni_koblenz.jgralab.impl.db;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralabtest.schemas.vertextest.A;
import de.uni_koblenz.jgralabtest.schemas.vertextest.B;
import de.uni_koblenz.jgralabtest.schemas.vertextest.E;

public class EdgeImplTest extends ImplTest {

	@Before
	public void setUp() {
		this.createTestGraphWithOneEdgeOfTypeE();
	}

	private void createTestGraphWithOneEdgeOfTypeE() {
		this.vertexTestGraph = this.createVertexTestGraphWithDatabaseSupport(
				"EdgeImplTest", 1000, 1000);
		A aVertex = this.vertexTestGraph.createA();
		B bVertex = this.vertexTestGraph.createB();
		this.vertexTestGraph.createE(aVertex, bVertex);
	}

	@After
	public void tearDown() {
		this.cleanDatabaseOfTestGraph(vertexTestGraph);
	}

	@Test
	public void deletingOneEdgeDecrementsIncidenceCountOfAlphaByOne() {
		E edge = this.vertexTestGraph.getFirstEInGraph();
		A alpha = (A) edge.getAlpha();
		int degreeBefore = alpha.getDegree();
		edge.delete();
		int degreeAfter = alpha.getDegree();
		assertEquals(degreeBefore - 1, degreeAfter);
	}

	@Test
	public void deletingOneEdgeDecrementsIncidenceCountOfOmegaByOne() {
		E edge = this.vertexTestGraph.getFirstEInGraph();
		B omega = (B) edge.getOmega();
		int degreeBefore = omega.getDegree();
		edge.delete();
		int degreeAfter = omega.getDegree();
		assertEquals(degreeBefore - 1, degreeAfter);
	}

	@Test
	public void deletingOneEdgeDecrementsEdgeCountByOne() {
		E edge = this.vertexTestGraph.getFirstEInGraph();
		int eCountBefore = this.vertexTestGraph.getECount();
		edge.delete();
		int eCountAfter = this.vertexTestGraph.getECount();
		assertEquals(eCountBefore - 1, eCountAfter);
	}

	private final int N = 3;

	@Test
	public void deletingSeveralEdges() {
		A aVertex = this.vertexTestGraph.getFirstA();
		B bVertex = this.vertexTestGraph.getFirstB();

		int startDegree = aVertex.getDegree();

		for (int i = 0; i < N; i++)
			this.vertexTestGraph.createE(aVertex, bVertex);

		int currentDegree = aVertex.getDegree();
		assertEquals(startDegree + N, currentDegree);

		for (int i = 0; i < N; i++) {
			int degreeBefore = aVertex.getDegree();
			E edge = (E) aVertex.getFirstEdge();
			edge.delete();
			int degreeAfter = aVertex.getDegree();
			assertEquals(degreeBefore - 1, degreeAfter);
		}
		int endDegree = aVertex.getDegree();
		assertEquals(startDegree, endDegree);
	}

}
