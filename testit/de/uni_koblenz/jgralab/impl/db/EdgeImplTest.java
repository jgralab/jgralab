/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
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
		E edge = this.vertexTestGraph.getFirstE();
		A alpha = (A) edge.getAlpha();
		int degreeBefore = alpha.getDegree();
		edge.delete();
		int degreeAfter = alpha.getDegree();
		assertEquals(degreeBefore - 1, degreeAfter);
	}

	@Test
	public void deletingOneEdgeDecrementsIncidenceCountOfOmegaByOne() {
		E edge = this.vertexTestGraph.getFirstE();
		B omega = (B) edge.getOmega();
		int degreeBefore = omega.getDegree();
		edge.delete();
		int degreeAfter = omega.getDegree();
		assertEquals(degreeBefore - 1, degreeAfter);
	}

	@Test
	public void deletingOneEdgeDecrementsEdgeCountByOne() {
		E edge = this.vertexTestGraph.getFirstE();
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
			E edge = (E) aVertex.getFirstIncidence();
			edge.delete();
			int degreeAfter = aVertex.getDegree();
			assertEquals(degreeBefore - 1, degreeAfter);
		}
		int endDegree = aVertex.getDegree();
		assertEquals(startDegree, endDegree);
	}

}
