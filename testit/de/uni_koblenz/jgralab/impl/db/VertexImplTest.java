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
