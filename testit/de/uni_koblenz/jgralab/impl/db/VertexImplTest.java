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

public class VertexImplTest extends ImplTest {

	@Before
	public void setUp() {
		this.createTestGraphWithOneVertexOfTypeA();
	}

	private void createTestGraphWithOneVertexOfTypeA() {
		this.vertexTestGraph = this.createVertexTestGraphWithDatabaseSupport(
				"VertexImplTest", 1000, 1000);
		this.vertexTestGraph.createA();
	}

	@After
	public void tearDown() {
		this.cleanDatabaseOfTestGraph(vertexTestGraph);
	}

	@Test
	public void createVertexIncrementsVertexCount() {
		int vertexCountBefore = this.vertexTestGraph.getVCount();
		this.vertexTestGraph.createA();
		int vertexCountAfter = this.vertexTestGraph.getVCount();
		assertEquals(vertexCountBefore + 1, vertexCountAfter);
	}

	@Test
	public void deleteVertexDecrementsVertexCount() {
		int vertexCountBefore = this.vertexTestGraph.getVCount();
		A aVertex = this.vertexTestGraph.getFirstA();
		aVertex.delete();
		int vertexCountAfter = this.vertexTestGraph.getVCount();
		assertEquals(vertexCountBefore - 1, vertexCountAfter);
	}

	@Test
	public void deleteVertex() {
		A aVertex = this.vertexTestGraph.getFirstA();
		int deletedVertexId = aVertex.getId();
		aVertex.delete();

		assertNull(this.vertexTestGraph.getVertex(deletedVertexId));
		assertNull(this.vertexTestGraph.getFirstA());
		assertNull(this.vertexTestGraph.getFirstVertex());
		assertNull(this.vertexTestGraph.getLastVertex());
	}

	@Test
	public void deleted() {
		A aVertex = this.vertexTestGraph.getFirstA();
		aVertex.delete();

		assertNull(aVertex.getGraph());
		assertEquals(aVertex.getId(), 0);
	}

	private final int N = 3;

	@Test
	public void deletingVertexDeletesIncidentEdgesToo() {
		A aVertex = this.vertexTestGraph.getFirstA();
		for (int i = 0; i < N; i++) {
			this.createOutgoingEdgeFrom(aVertex);
		}
		int eCountBefore = this.vertexTestGraph.getECount();
		aVertex.delete();
		int eCountAfter = this.vertexTestGraph.getECount();
		assertEquals(eCountBefore - N, eCountAfter);
	}

	private void createOutgoingEdgeFrom(A aVertex) {
		B bVertex = this.vertexTestGraph.createB();
		this.vertexTestGraph.createE(aVertex, bVertex);
	}

}
