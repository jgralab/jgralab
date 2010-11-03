/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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

		for (int i = 0; i < N; i++) {
			this.vertexTestGraph.createE(aVertex, bVertex);
		}

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
