/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql.schema.GreqlSchema;
import de.uni_koblenz.jgralab.impl.FreeIndexList;
import de.uni_koblenz.jgralab.impl.GraphBaseImpl;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralabtest.schemas.vertextest.A;
import de.uni_koblenz.jgralabtest.schemas.vertextest.B;
import de.uni_koblenz.jgralabtest.schemas.vertextest.C;
import de.uni_koblenz.jgralabtest.schemas.vertextest.C2;
import de.uni_koblenz.jgralabtest.schemas.vertextest.D;
import de.uni_koblenz.jgralabtest.schemas.vertextest.D2;
import de.uni_koblenz.jgralabtest.schemas.vertextest.F;
import de.uni_koblenz.jgralabtest.schemas.vertextest.G;
import de.uni_koblenz.jgralabtest.schemas.vertextest.H;
import de.uni_koblenz.jgralabtest.schemas.vertextest.J;
import de.uni_koblenz.jgralabtest.schemas.vertextest.K;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

@RunWith(Parameterized.class)
public class LoadTest extends InstanceTest {

	private static final String TESTGRAPH_FILENAME = "testgraph.tg";
	private static final String TESTGRAPH_PATH = "testit/testdata/";

	public LoadTest(ImplementationType implementationType, String dbURL) {
		super(implementationType, dbURL);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private Graph createTestGraph() throws Exception {
		if (implementationType == ImplementationType.STANDARD) {
			String query = "from i:c report i end where d:=\"drölfundfünfzig\", c:=b, b:=a, a:=\"Mensaessen\"";
			return GreqlParser.parse(query, null);
		}
		int vertexClasses = 6;
		int edgeClasses = 7;
		int vertexCountPerClass = 5;
		int edgeCountPerClass = 5;

		VertexTestGraph g = createVertexTestGraph(vertexClasses
				* vertexCountPerClass, edgeClasses * edgeCountPerClass);

		A[] as = new A[vertexCountPerClass];
		B[] bs = new B[vertexCountPerClass];
		C[] cs = new C[vertexCountPerClass];
		D[] ds = new D[vertexCountPerClass];
		C2[] c2s = new C2[vertexCountPerClass];
		D2[] d2s = new D2[vertexCountPerClass];

		// create some vertices
		for (int i = 0; i < vertexCountPerClass; i++) {
			as[i] = g.createA();
			bs[i] = g.createB();
			cs[i] = g.createC();
			ds[i] = g.createD();
			c2s[i] = g.createC2();
			d2s[i] = g.createD2();
		}

		Random r = new Random();
		// create edge arrays
		// E[] es = new E[edgeCountPerClass];
		// F[] fs = new F[edgeCountPerClass];
		// G[] gs = new G[edgeCountPerClass];
		// H[] hs = new H[edgeCountPerClass];
		// I[] is = new I[edgeCountPerClass];
		// J[] js = new J[edgeCountPerClass];
		// K[] ks = new K[edgeCountPerClass];

		// iterate and select alpha and omega randomly
		for (int i = 0; i < edgeCountPerClass; i++) {
			// E
			A alphaA = as[r.nextInt(vertexCountPerClass)];
			B omegaB = bs[r.nextInt(vertexCountPerClass)];
			g.createE(alphaA, omegaB);

			// F
			C alphaC = cs[r.nextInt(vertexCountPerClass)];
			D omegaD;
			omegaD = ds[r.nextInt(vertexCountPerClass)];
			if (omegaD.getDegree(F.EC, EdgeDirection.IN) > 3) {
				g.createF(alphaC, omegaD);
			}

			// G
			alphaC = cs[r.nextInt(vertexCountPerClass)];
			omegaD = ds[r.nextInt(vertexCountPerClass)];
			if (omegaD.getDegree(G.EC, EdgeDirection.IN) > 3) {
				g.createG(alphaC, omegaD);
			}

			// H
			alphaA = as[r.nextInt(vertexCountPerClass)];
			omegaB = bs[r.nextInt(vertexCountPerClass)];
			if (omegaD.getDegree(H.EC, EdgeDirection.IN) > 4) {
				g.createH(alphaA, omegaB);
			}

			// I
			alphaA = as[r.nextInt(vertexCountPerClass)];
			A omegaA = as[r.nextInt(vertexCountPerClass)];
			g.createI(alphaA, omegaA);

			// J
			C2 alphaC2 = c2s[r.nextInt(vertexCountPerClass)];
			D2 omegaD2 = d2s[r.nextInt(vertexCountPerClass)];
			if (omegaD2.getDegree(J.EC, EdgeDirection.IN) > 3) {
				g.createJ(alphaC2, omegaD2);
			}

			// K
			alphaA = as[r.nextInt(vertexCountPerClass)];
			omegaB = bs[r.nextInt(vertexCountPerClass)];
			if (omegaB.getDegree(K.EC, EdgeDirection.IN) > 3) {
				g.createK(alphaA, omegaB);
			}
		}

		for (int i = 0; i < vertexCountPerClass; i++) {
			B currentB = bs[i];
			while (currentB.getDegree(H.EC, EdgeDirection.IN) < 1) {
				A alphaA = as[r.nextInt(vertexCountPerClass)];
				g.createH(alphaA, currentB);
			}
			while (currentB.getDegree(K.EC, EdgeDirection.IN) < 2) {
				A alphaA = as[r.nextInt(vertexCountPerClass)];
				g.createK(alphaA, currentB);
			}

			D currentD = ds[i];
			while (currentD.getDegree(F.EC, EdgeDirection.IN) < 1) {
				C alphaC = cs[r.nextInt(vertexCountPerClass)];
				g.createF(alphaC, currentD);
			}
			while (currentD.getDegree(G.EC, EdgeDirection.IN) < 1) {
				C alphaC = cs[r.nextInt(vertexCountPerClass)];
				g.createG(alphaC, currentD);
			}

			D2 currentD2 = d2s[i];
			while (currentD2.getDegree(J.EC, EdgeDirection.IN) < 1) {
				C2 alphaC2 = c2s[r.nextInt(vertexCountPerClass)];
				g.createJ(alphaC2, currentD2);
			}
		}
		return g;
	}

	private VertexTestGraph createVertexTestGraph(int vMax, int eMax) {
		VertexTestGraph graph = null;
		switch (implementationType) {
		case STANDARD:
			graph = VertexTestSchema.instance().createVertexTestGraph(
					ImplementationType.STANDARD, null, vMax, eMax);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		return graph;
	}

	@Test
	public void testFreeElementList() {
		Graph g1 = null;
		Graph g2 = null;
		try {
			// g1 is always without transaction support
			g1 = createTestGraph();
			GraphIO.saveGraphToFile(g1, TESTGRAPH_PATH + TESTGRAPH_FILENAME,
					null);
			switch (implementationType) {
			case STANDARD:
				g2 = GreqlSchema.instance().loadGreqlGraph(
						TESTGRAPH_PATH + TESTGRAPH_FILENAME,
						ImplementationType.STANDARD);
				break;
			default:
				fail("Implementation " + implementationType
						+ " not yet supported by this test.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		checkEqualVertexList(g1, g2);
		checkEqualEdgeList(g1, g2);

		fillVertexList(g1, g2);

		checkEqualVertexList(g1, g2);
		checkEqualEdgeList(g1, g2);

		removeVertices(g1, g2);

		checkEqualVertexList(g1, g2);
		checkEqualEdgeList(g1, g2);

		fillVertexList(g1, g2);

		checkEqualVertexList(g1, g2);
		checkEqualEdgeList(g1, g2);

		removeVertices(g1, g2);

		checkEqualVertexList(g1, g2);
		checkEqualEdgeList(g1, g2);
	}

	private void checkEqualVertexList(Graph g1, Graph g2) {
		InternalGraph gb1 = (InternalGraph) g1;
		InternalGraph gb2 = (InternalGraph) g2;
		InternalVertex v1 = gb1.getFirstVertexInVSeq();
		InternalVertex v2 = gb2.getFirstVertexInVSeq();
		while (v1 != null) {
			if (v2 == null) {
				fail();
			}
			assertEquals(v1.getId(), v2.getId());
			assertEquals(v1.getAttributedElementClass().getQualifiedName(), v2
					.getAttributedElementClass().getQualifiedName());
			v1 = v1.getNextVertexInVSeq();
			v2 = v2.getNextVertexInVSeq();
		}
		if (v2 != null) {
			fail();
		}
	}

	private void checkEqualEdgeList(Graph g1, Graph g2) {
		Edge v1 = g1.getFirstEdge();
		Edge v2 = g2.getFirstEdge();
		while (v1 != null) {
			if (v2 == null) {
				fail();
			}
			assertEquals(v1.getId(), v2.getId());
			assertEquals(v1.getAttributedElementClass().getQualifiedName(), v2
					.getAttributedElementClass().getQualifiedName());
			v1 = v1.getNextEdge();
			v2 = v2.getNextEdge();
		}
		if (v2 != null) {
			fail();
		}
	}

	private void fillVertexList(Graph g1, Graph g2) {
		GraphClass gc = g1.getGraphClass();
		for (int i = 0; i < 100; i++) {
			VertexClass vertexClass = gc.getVertexClasses().get(
					i % gc.getVertexClasses().size());
			if (vertexClass.isAbstract()) {
				continue;
			}
			g1.createVertex(vertexClass);

			g2.createVertex(vertexClass);
		}
	}

	private void removeVertices(Graph g1, Graph g2) {
		for (int i = 1; i < g1.getVCount(); i += 7) {
			Vertex v1 = g1.getVertex(i);
			Vertex v2 = g2.getVertex(i);
			assertEquals(v1.getId(), v2.getId());
			assertEquals(v1.getAttributedElementClass().getQualifiedName(), v2
					.getAttributedElementClass().getQualifiedName());
			v1.delete();
			v2.delete();
		}
	}

	/**
	 * Returns the freeVertexList of <code>graph</code> if
	 * <code>getVertexList</code> is set to true else the freeEdgeList is
	 * returned.
	 * 
	 * @param graph
	 *            the graph
	 * @param getVertexList
	 * @return freeVertexList of g
	 */
	private FreeIndexList getFreeIndexListOfVertices(Graph graph,
			boolean getVertexList) {
		try {
			Field f = GraphBaseImpl.class
					.getDeclaredField(getVertexList ? "freeVertexList"
							: "freeEdgeList");
			f.setAccessible(true);
			return (FreeIndexList) f.get(graph);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the runs-Array of <code>fil</code>.
	 * 
	 * @param fil
	 *            a FreeIndexList
	 * @return runs
	 */
	private int[] getRunsOfFreeIndexList(FreeIndexList fil) {
		try {
			Field f = fil.getClass().getDeclaredField("runs");
			f.setAccessible(true);
			return (int[]) f.get(fil);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks if the FreeIndexList fullfills:<br>
	 * vList.getUsed()==<code>used</code><br>
	 * vList.getFree()==<code>free</code><br>
	 * vList.runs.length==<code>runsLength</code><br>
	 * vList.runs starts with the elements of <code>runsValues</code>. The other
	 * elements of runs must be 0.
	 * 
	 * @param vList
	 * @param used
	 * @param free
	 * @param runsLength
	 * @param runsValues
	 */
	private void checkFreeIndexList(FreeIndexList vList, int used, int free,
			int runsLength, int... runsValues) {
		assertNotNull("vList is null", vList);
		assertEquals("used isn't equal", used, vList.getUsed());
		assertEquals("free isn't equal", free, vList.getFree());
		assertEquals("size isn't equal", used + free, vList.getSize());
		int[] runs = getRunsOfFreeIndexList(vList);
		assertNotNull("runs is null", runs);
		assertEquals("runs has an unexpected length", runsLength, runs.length);
		assertTrue("runsValues.length<=runs.length",
				runsValues.length <= runs.length);
		for (int i = 0; i < runs.length; i++) {
			if (i < runsValues.length) {
				assertEquals("runs[" + i + "] isn't equal", runsValues[i],
						runs[i]);
			} else {
				assertEquals("runs[" + i + "] isn't equal", 0, runs[i]);
			}
		}
	}

	/**
	 * Test if graph has only one vertex.
	 * 
	 * @
	 */
	@Test
	public void allocateIndexTest0() {
		VertexTestGraph g = createVertexTestGraph(1, 1);
		A v1 = g.createA();
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		checkFreeIndexList(vList, 1, 0, 16, -1);
		v1.delete();
		checkFreeIndexList(vList, 0, 1, 16, 1);
	}

	/**
	 * Test if graph has a limit of one vertex and you want to create another
	 * one.
	 * 
	 * @
	 */
	@Test
	public void allocateIndexTest1() {
		VertexTestGraph g = createVertexTestGraph(1, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		g.createA();
		checkFreeIndexList(vList, 1, 0, 16, -1);
		g.createA();
		checkFreeIndexList(vList, 2, 0, 16, -2);
	}

	/**
	 * Test if you fill runs completely by the allocate method.
	 * 
	 * @
	 */
	@Test
	public void allocateIndexTest2() {
		VertexTestGraph g = createVertexTestGraph(1, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		for (int i = 0; i < 18; i++) {
			g.createA();
		}
		// delete every second vertex
		for (int i = 1; i < 15; i = i + 2) {
			g.getVertex(i).delete();
		}
		g.getVertex(2).delete();
		g.getVertex(15).delete();
		checkFreeIndexList(vList, 9, 23, 16, 3, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -3, 14);
		g.createA();
		checkFreeIndexList(vList, 10, 22, 16, -1, 2, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -3, 14);
	}

	/**
	 * Test if you reach an increase of runs by the allocate method.
	 * runs=[2,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1] and then create a new
	 * vertex.
	 * 
	 * @
	 */
	@Test
	public void allocateIndexTest3() {
		VertexTestGraph g = createVertexTestGraph(17, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		for (int i = 1; i <= 17; i++) {
			g.createA();
		}
		// delete the vertices
		g.getVertex(1).delete();
		g.getVertex(2).delete();
		for (int i = 4; i <= 17; i = i + 2) {
			g.getVertex(i).delete();
		}
		checkFreeIndexList(vList, 8, 9, 16, 2, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1);
		g.createA();
		checkFreeIndexList(vList, 9, 8, 32, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1,
				-1, 1, -1, 1, -1, 1, -1);
	}

	/**
	 * Test if you create 17 vertices and you delete every second(FreeIndexList
	 * must be increased).
	 * 
	 * @
	 */
	@Test
	public void freeRangeTest0() {
		VertexTestGraph g = createVertexTestGraph(1, 1);

		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		for (int i = 0; i < 17; i++) {
			g.createA();
		}
		checkFreeIndexList(vList, 17, 15, 16, -17, 15);
		// delete every second vertex
		for (int i = 2; i < 17; i = i + 2) {
			g.getVertex(i).delete();
		}
		checkFreeIndexList(vList, 9, 23, 32, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1,
				-1, 1, -1, 1, -1, 1, -1, 15);
	}

	/**
	 * A graph which has at most 2 vertices. One vertex is created and deleted.
	 * 
	 * @
	 */
	@Test
	public void freeRangeTest1() {
		VertexTestGraph g = createVertexTestGraph(2, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		A v1 = g.createA();
		checkFreeIndexList(vList, 1, 1, 16, -1, 1);
		v1.delete();
		checkFreeIndexList(vList, 0, 2, 16, 2);
	}

	/**
	 * Delete first node at a FreeIndexList.runs= [-2, 1, -1, 1, -1, 1, -1, 1,
	 * -1, 1, -1, 1, -1, 1, -1, 1]
	 * 
	 * @
	 */
	@Test
	public void freeRangeTest2() {
		VertexTestGraph g = createVertexTestGraph(17, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		// create vertices
		for (int i = 0; i < 17; i++) {
			g.createA();
		}
		checkFreeIndexList(vList, 17, 0, 16, -17);
		// delete vertices to fill runs
		// runs[0]==-2
		for (int i = 2; i < 17; i = i + 2) {
			g.getVertex(i + 1).delete();
		}
		checkFreeIndexList(vList, 9, 8, 16, -2, 1, -1, 1, -1, 1, -1, 1, -1, 1,
				-1, 1, -1, 1, -1, 1);
		// delete the first vertex. Runs must be enlarged.
		g.getVertex(1).delete();
		checkFreeIndexList(vList, 8, 9, 32, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1, 1);
	}

	/**
	 * FreeIndexList.runs= [1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -3, 0,
	 * 0] Delete the middle vertex of -3.
	 * 
	 * @
	 */
	@Test
	public void freeRangeTest3() {
		VertexTestGraph g = createVertexTestGraph(16, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		// create vertices
		for (int i = 0; i < 16; i++) {
			g.createA();
		}
		checkFreeIndexList(vList, 16, 0, 16, -16);
		// delete odd vertices to fill runs
		for (int i = 0; i < 14; i = i + 2) {
			g.getVertex(i + 1).delete();
		}
		checkFreeIndexList(vList, 9, 7, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -3);
		// delete the last vertex. runs[runs.length-1]==0
		g.getVertex(15).delete();
		checkFreeIndexList(vList, 8, 8, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1);
	}

	/**
	 * FreeIndexList.runs= [1, -1, 1, -3, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 0,
	 * 0] Delete the middle vertex of -3.
	 * 
	 * @
	 */
	@Test
	public void freeRangeTest4() {
		VertexTestGraph g = createVertexTestGraph(16, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		// create vertices
		for (int i = 0; i < 16; i++) {
			g.createA();
		}
		checkFreeIndexList(vList, 16, 0, 16, -16);
		// delete odd vertices to fill runs
		for (int i = 0; i < 16; i = i + 2) {
			if (i != 4) {
				g.getVertex(i + 1).delete();
			}
		}
		checkFreeIndexList(vList, 9, 7, 16, 1, -1, 1, -3, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1);
		// delete the last vertex. runs[runs.length-1]==0
		g.getVertex(5).delete();
		checkFreeIndexList(vList, 8, 8, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1);
	}

	/**
	 * FreeIndexList.runs= [-1, 1, -2, 0,...] Delete the first vertex of -2.
	 * 
	 * @
	 */
	@Test
	public void freeRangeTest5() {
		VertexTestGraph g = createVertexTestGraph(4, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		g.createA();
		g.createA();
		g.createA();
		g.createA();
		g.getVertex(2).delete();
		checkFreeIndexList(vList, 3, 1, 16, -1, 1, -2);
		g.getVertex(3).delete();
		checkFreeIndexList(vList, 2, 2, 16, -1, 2, -1);
	}

	/**
	 * FreeIndexList.runs= [-1, 1, -1, 0,...] Delete the last used index.
	 * 
	 * @
	 */
	@Test
	public void freeRangeTest6() {
		VertexTestGraph g = createVertexTestGraph(3, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		g.createA();
		g.createA();
		g.createA();
		g.getVertex(2).delete();
		checkFreeIndexList(vList, 2, 1, 16, -1, 1, -1);
		g.getVertex(3).delete();
		checkFreeIndexList(vList, 1, 2, 16, -1, 2);
	}

	/**
	 * FreeIndexList.runs= [1,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-2] Delete the
	 * last used index.
	 * 
	 * @
	 */
	@Test
	public void freeRangeTest7() {
		VertexTestGraph g = createVertexTestGraph(17, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		for (int i = 1; i <= 17; i++) {
			g.createA();
		}
		for (int i = 15; i > 0; i = i - 2) {
			g.getVertex(i).delete();
		}
		checkFreeIndexList(vList, 9, 8, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -2);
		g.getVertex(17).delete();
		checkFreeIndexList(vList, 8, 9, 32, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1, 1);
	}
}
