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
package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
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
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Schema;
import de.uni_koblenz.jgralab.impl.FreeIndexList;
import de.uni_koblenz.jgralab.impl.GraphBaseImpl;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
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
	private static final String TESTGRAPH_PATH = "testit/testgraphs/";

	public LoadTest(ImplementationType implementationType) {
		super(implementationType);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	@Before
	public void setUp() {
		if (implementationType == ImplementationType.DATABASE) {
			dbHandler.connectToDatabase();
			dbHandler.loadVertexTestSchemaIntoGraphDatabase();
		}
	}

	@After
	public void tearDown() {
		if (implementationType == ImplementationType.DATABASE) {
			// dbHandler.cleanDatabaseOfTestGraph("VertexTest");
			// dbHandler.cleanDatabaseOfTestGraph("LoadTest");
			// super.cleanDatabaseOfTestSchema(VertexTestSchema.instance());
			dbHandler.clearAllTables();
			dbHandler.closeGraphdatabase();
		}
	}

	// public static void main(String[] args) {
	// LoadTest t = new LoadTest();
	// t.testFreeElementList();
	// }

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
		createTransaction(g);

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
			if (omegaD.getDegree(F.class, EdgeDirection.IN) > 3) {
				g.createF(alphaC, omegaD);
			}

			// G
			alphaC = cs[r.nextInt(vertexCountPerClass)];
			omegaD = ds[r.nextInt(vertexCountPerClass)];
			if (omegaD.getDegree(G.class, EdgeDirection.IN) > 3) {
				g.createG(alphaC, omegaD);
			}

			// H
			alphaA = as[r.nextInt(vertexCountPerClass)];
			omegaB = bs[r.nextInt(vertexCountPerClass)];
			if (omegaD.getDegree(H.class, EdgeDirection.IN) > 4) {
				g.createH(alphaA, omegaB);
			}

			// I
			alphaA = as[r.nextInt(vertexCountPerClass)];
			A omegaA = as[r.nextInt(vertexCountPerClass)];
			g.createI(alphaA, omegaA);

			// J
			C2 alphaC2 = c2s[r.nextInt(vertexCountPerClass)];
			D2 omegaD2 = d2s[r.nextInt(vertexCountPerClass)];
			if (omegaD2.getDegree(J.class, EdgeDirection.IN) > 3) {
				g.createJ(alphaC2, omegaD2);
			}

			// K
			alphaA = as[r.nextInt(vertexCountPerClass)];
			omegaB = bs[r.nextInt(vertexCountPerClass)];
			if (omegaB.getDegree(K.class, EdgeDirection.IN) > 3) {
				g.createK(alphaA, omegaB);
			}
		}

		for (int i = 0; i < vertexCountPerClass; i++) {
			B currentB = bs[i];
			while (currentB.getDegree(H.class, EdgeDirection.IN) < 1) {
				A alphaA = as[r.nextInt(vertexCountPerClass)];
				g.createH(alphaA, currentB);
			}
			while (currentB.getDegree(K.class, EdgeDirection.IN) < 2) {
				A alphaA = as[r.nextInt(vertexCountPerClass)];
				g.createK(alphaA, currentB);
			}

			D currentD = ds[i];
			while (currentD.getDegree(F.class, EdgeDirection.IN) < 1) {
				C alphaC = cs[r.nextInt(vertexCountPerClass)];
				g.createF(alphaC, currentD);
			}
			while (currentD.getDegree(G.class, EdgeDirection.IN) < 1) {
				C alphaC = cs[r.nextInt(vertexCountPerClass)];
				g.createG(alphaC, currentD);
			}

			D2 currentD2 = d2s[i];
			while (currentD2.getDegree(J.class, EdgeDirection.IN) < 1) {
				C2 alphaC2 = c2s[r.nextInt(vertexCountPerClass)];
				g.createJ(alphaC2, currentD2);
			}
		}
		commit(g);
		return g;
	}

	private VertexTestGraph createVertexTestGraph(int vMax, int eMax) {
		VertexTestGraph graph = null;
		switch (implementationType) {
		case STANDARD:
			graph = VertexTestSchema.instance().createVertexTestGraph(vMax,
					eMax);
			break;
		case TRANSACTION:
			graph = VertexTestSchema.instance()
					.createVertexTestGraphWithTransactionSupport(vMax, eMax);
			break;
		case DATABASE:
			graph = dbHandler.createVertexTestGraphWithDatabaseSupport(
					"LoadTest", vMax, eMax);
			break;
		case SAVEMEM:
			graph = VertexTestSchema.instance()
					.createVertexTestGraphWithSavememSupport(vMax, eMax);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		return graph;
	}

	// @Test
	// public void testFreeElementList() throws CommitFailedException {
	// onlyTestWithoutTransactionSupport();
	// Greql2 g1 = null;
	// Greql2 g2 = null;
	// try {
	// // g1 is always without transaction support
	// g1 = createTestGraph();
	// GraphIO.saveGraphToFile(TESTGRAPH_PATH + TESTGRAPH_FILENAME, g1,
	// null);
	// g2 = transactionsEnabled ? Greql2Schema.instance()
	// .loadGreql2WithTransactionSupport(
	// TESTGRAPH_PATH + TESTGRAPH_FILENAME) : Greql2Schema
	// .instance().loadGreql2(TESTGRAPH_PATH + TESTGRAPH_FILENAME);
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// createReadOnlyTransaction(g2);
	// checkEqualVertexList(g1, g2);
	// checkEqualEdgeList(g1, g2);
	// commit(g2);
	//
	// createTransaction(g2);
	// fillVertexList(g1, g2);
	// commit(g2);
	//
	// createReadOnlyTransaction(g2);
	// checkEqualVertexList(g1, g2);
	// checkEqualEdgeList(g1, g2);
	// commit(g2);
	//
	// createTransaction(g2);
	// removeVertices(g1, g2);
	// commit(g2);
	//
	// createReadOnlyTransaction(g2);
	// checkEqualVertexList(g1, g2);
	// checkEqualEdgeList(g1, g2);
	// commit(g2);
	//
	// createTransaction(g2);
	// fillVertexList(g1, g2);
	// commit(g2);
	//
	// createReadOnlyTransaction(g2);
	// checkEqualVertexList(g1, g2);
	// checkEqualEdgeList(g1, g2);
	// commit(g2);
	//
	// createTransaction(g2);
	// removeVertices(g1, g2);
	// commit(g2);
	//
	// createReadOnlyTransaction(g2);
	// checkEqualVertexList(g1, g2);
	// checkEqualEdgeList(g1, g2);
	// commit(g2);
	// }
	@SuppressWarnings("deprecation")
	@Test
	public void testFreeElementList() throws CommitFailedException {
		Graph g1 = null;
		Graph g2 = null;
		try {
			// g1 is always without transaction support
			g1 = createTestGraph();
			createReadOnlyTransaction(g1);
			GraphIO.saveGraphToFile(TESTGRAPH_PATH + TESTGRAPH_FILENAME, g1,
					null);
			commit(g1);
			switch (implementationType) {
			case STANDARD:
				g2 = Greql2Schema.instance().loadGreql2(
						TESTGRAPH_PATH + TESTGRAPH_FILENAME);
				break;
			case TRANSACTION:
				g2 = VertexTestSchema.instance()
						.loadVertexTestGraphWithTransactionSupport(
								TESTGRAPH_PATH + TESTGRAPH_FILENAME);
				break;
			case DATABASE:
				g2 = GraphIO.loadGraphFromFile(TESTGRAPH_PATH
						+ TESTGRAPH_FILENAME, null);
				break;
			case SAVEMEM:
				g2 = VertexTestSchema.instance()
						.loadVertexTestGraphWithSavememSupport(
								TESTGRAPH_PATH + TESTGRAPH_FILENAME);
				break;
			default:
				fail("Implementation " + implementationType
						+ " not yet supported by this test.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		createReadOnlyTransaction(g2);
		createReadOnlyTransaction(g1);
		checkEqualVertexList(g1, g2);
		checkEqualEdgeList(g1, g2);
		commit(g2);
		commit(g1);

		createTransaction(g2);
		createTransaction(g1);
		fillVertexList(g1, g2);
		commit(g2);
		commit(g1);

		createReadOnlyTransaction(g2);
		createReadOnlyTransaction(g1);
		checkEqualVertexList(g1, g2);
		checkEqualEdgeList(g1, g2);
		commit(g2);
		commit(g1);

		createTransaction(g2);
		createTransaction(g1);
		removeVertices(g1, g2);
		commit(g2);
		commit(g1);

		createReadOnlyTransaction(g2);
		createReadOnlyTransaction(g1);
		checkEqualVertexList(g1, g2);
		checkEqualEdgeList(g1, g2);
		commit(g2);
		commit(g1);

		createTransaction(g2);
		createTransaction(g1);
		fillVertexList(g1, g2);
		commit(g2);
		commit(g1);

		createReadOnlyTransaction(g2);
		createReadOnlyTransaction(g1);
		checkEqualVertexList(g1, g2);
		checkEqualEdgeList(g1, g2);
		commit(g2);
		commit(g1);

		createTransaction(g2);
		createTransaction(g1);
		removeVertices(g1, g2);
		commit(g2);
		commit(g1);

		createReadOnlyTransaction(g2);
		createReadOnlyTransaction(g1);
		checkEqualVertexList(g1, g2);
		checkEqualEdgeList(g1, g2);
		commit(g2);
		commit(g1);
	}

	private void checkEqualVertexList(Graph g1, Graph g2) {
		Vertex v1 = g1.getFirstVertex();
		Vertex v2 = g2.getFirstVertex();
		while (v1 != null) {
			if (v2 == null) {
				fail();
			}
			assertEquals(v1.getId(), v2.getId());
			assertEquals(v1.getAttributedElementClass().getQualifiedName(), v2
					.getAttributedElementClass().getQualifiedName());
			v1 = v1.getNextVertex();
			v2 = v2.getNextVertex();
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
			if (vertexClass.isInternal() || vertexClass.isAbstract()) {
				continue;
			}
			Class<? extends Vertex> vc = vertexClass.getM1Class();
			g1.createVertex(vc);
			// VertexClass vertexClass2 = gc.getVertexClasses().get(i %
			// gc.getVertexClasses().size());
			// if (vertexClass.isInternal())
			// continue;
			// Class<? extends Vertex> vc2 = vertexClass2.getM1Class();
			g2.createVertex(vc);
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
	 * @throws CommitFailedException
	 */
	@Test
	public void allocateIndexTest0() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(1, 1);
		createTransaction(g);
		A v1 = g.createA();
		commit(g);
		createReadOnlyTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		checkFreeIndexList(vList, 1, 0, 16, -1);
		commit(g);
		createTransaction(g);
		v1.delete();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 0, 1, 16, 1);
		commit(g);
	}

	/**
	 * Test if graph has a limit of one vertex and you want to create another
	 * one.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void allocateIndexTest1() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(1, 1);
		createReadOnlyTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		commit(g);
		createTransaction(g);
		g.createA();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 1, 0, 16, -1);
		commit(g);
		createTransaction(g);
		g.createA();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 2, 0, 16, -2);
		commit(g);
	}

	/**
	 * Test if you fill runs completely by the allocate method.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void allocateIndexTest2() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(1, 1);
		createTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		commit(g);
		for (int i = 0; i < 18; i++) {
			createTransaction(g);
			g.createA();
			commit(g);
		}
		// delete every second vertex
		for (int i = 1; i < 15; i = i + 2) {
			createTransaction(g);
			g.getVertex(i).delete();
			commit(g);
		}
		createTransaction(g);
		g.getVertex(2).delete();
		commit(g);
		createTransaction(g);
		g.getVertex(15).delete();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 9, 23, 16, 3, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -3, 14);
		commit(g);
		createTransaction(g);
		g.createA();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 10, 22, 16, -1, 2, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -3, 14);
		commit(g);
	}

	/**
	 * Test if you reach an increase of runs by the allocate method.
	 * runs=[2,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1] and then create a new
	 * vertex.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void allocateIndexTest3() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(17, 1);
		createReadOnlyTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		commit(g);
		for (int i = 1; i <= 17; i++) {
			createTransaction(g);
			g.createA();
			commit(g);
		}
		// delete the vertices
		createTransaction(g);
		g.getVertex(1).delete();
		commit(g);
		createTransaction(g);
		g.getVertex(2).delete();
		commit(g);
		for (int i = 4; i <= 17; i = i + 2) {
			createTransaction(g);
			g.getVertex(i).delete();
			commit(g);
		}
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 8, 9, 16, 2, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1);
		commit(g);
		createTransaction(g);
		g.createA();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 9, 8, 32, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1,
				-1, 1, -1, 1, -1, 1, -1);
		commit(g);
	}

	/**
	 * Test if you create 17 vertices and you delete every second(FreeIndexList
	 * must be increased).
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void freeRangeTest0() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(1, 1);

		createReadOnlyTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		commit(g);
		createTransaction(g);
		for (int i = 0; i < 17; i++) {
			g.createA();
		}
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 17, 15, 16, -17, 15);
		commit(g);
		// delete every second vertex
		createTransaction(g);
		for (int i = 2; i < 17; i = i + 2) {
			g.getVertex(i).delete();
		}
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 9, 23, 32, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1,
				-1, 1, -1, 1, -1, 1, -1, 15);
		commit(g);
	}

	/**
	 * A graph which has at most 2 vertices. One vertex is created and deleted.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void freeRangeTest1() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(2, 1);
		createTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		commit(g);
		createTransaction(g);
		A v1 = g.createA();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 1, 1, 16, -1, 1);
		commit(g);
		createTransaction(g);
		v1.delete();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 0, 2, 16, 2);
		commit(g);
	}

	/**
	 * Delete first node at a FreeIndexList.runs= [-2, 1, -1, 1, -1, 1, -1, 1,
	 * -1, 1, -1, 1, -1, 1, -1, 1]
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void freeRangeTest2() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(17, 1);
		createReadOnlyTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		commit(g);
		// create vertices
		createTransaction(g);
		for (int i = 0; i < 17; i++) {
			g.createA();
		}
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 17, 0, 16, -17);
		commit(g);
		// delete vertices to fill runs
		// runs[0]==-2
		createTransaction(g);
		for (int i = 2; i < 17; i = i + 2) {
			g.getVertex(i + 1).delete();
		}
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 9, 8, 16, -2, 1, -1, 1, -1, 1, -1, 1, -1, 1,
				-1, 1, -1, 1, -1, 1);
		commit(g);
		// delete the first vertex. Runs must be enlarged.
		createTransaction(g);
		g.getVertex(1).delete();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 8, 9, 32, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1, 1);
		commit(g);
	}

	/**
	 * FreeIndexList.runs= [1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -3, 0,
	 * 0] Delete the middle vertex of -3.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void freeRangeTest3() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(16, 1);
		createReadOnlyTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		commit(g);
		// create vertices
		createTransaction(g);
		for (int i = 0; i < 16; i++) {
			g.createA();
		}
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 16, 0, 16, -16);
		commit(g);
		// delete odd vertices to fill runs
		createTransaction(g);
		for (int i = 0; i < 14; i = i + 2) {
			g.getVertex(i + 1).delete();
		}
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 9, 7, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -3);
		commit(g);
		// delete the last vertex. runs[runs.length-1]==0
		createTransaction(g);
		g.getVertex(15).delete();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 8, 8, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1);
		commit(g);
	}

	/**
	 * FreeIndexList.runs= [1, -1, 1, -3, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 0,
	 * 0] Delete the middle vertex of -3.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void freeRangeTest4() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(16, 1);
		createReadOnlyTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		commit(g);
		// create vertices
		createTransaction(g);
		for (int i = 0; i < 16; i++) {
			g.createA();
		}
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 16, 0, 16, -16);
		commit(g);
		// delete odd vertices to fill runs
		createTransaction(g);
		for (int i = 0; i < 16; i = i + 2) {
			if (i != 4) {
				g.getVertex(i + 1).delete();
			}
		}
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 9, 7, 16, 1, -1, 1, -3, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1);
		commit(g);
		// delete the last vertex. runs[runs.length-1]==0
		createTransaction(g);
		g.getVertex(5).delete();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 8, 8, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1);
		commit(g);
	}

	/**
	 * FreeIndexList.runs= [-1, 1, -2, 0,...] Delete the first vertex of -2.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void freeRangeTest5() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(4, 1);
		createReadOnlyTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		commit(g);
		createTransaction(g);
		g.createA();
		commit(g);
		createTransaction(g);
		g.createA();
		commit(g);
		createTransaction(g);
		g.createA();
		commit(g);
		createTransaction(g);
		g.createA();
		commit(g);
		createTransaction(g);
		g.getVertex(2).delete();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 3, 1, 16, -1, 1, -2);
		commit(g);
		createTransaction(g);
		g.getVertex(3).delete();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 2, 2, 16, -1, 2, -1);
		commit(g);
	}

	/**
	 * FreeIndexList.runs= [-1, 1, -1, 0,...] Delete the last used index.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void freeRangeTest6() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(3, 1);
		createReadOnlyTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		commit(g);
		createTransaction(g);
		g.createA();
		commit(g);
		createTransaction(g);
		g.createA();
		commit(g);
		createTransaction(g);
		g.createA();
		commit(g);
		createTransaction(g);
		g.getVertex(2).delete();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 2, 1, 16, -1, 1, -1);
		commit(g);
		createTransaction(g);
		g.getVertex(3).delete();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 1, 2, 16, -1, 2);
		commit(g);
	}

	/**
	 * FreeIndexList.runs= [1,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-2] Delete the
	 * last used index.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void freeRangeTest7() throws CommitFailedException {
		VertexTestGraph g = createVertexTestGraph(17, 1);
		createReadOnlyTransaction(g);
		FreeIndexList vList = getFreeIndexListOfVertices(g, true);
		commit(g);
		for (int i = 1; i <= 17; i++) {
			createTransaction(g);
			g.createA();
			commit(g);
		}
		for (int i = 15; i > 0; i = i - 2) {
			createTransaction(g);
			g.getVertex(i).delete();
			commit(g);
		}
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 9, 8, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -2);
		commit(g);
		createTransaction(g);
		g.getVertex(17).delete();
		commit(g);
		createReadOnlyTransaction(g);
		checkFreeIndexList(vList, 8, 9, 32, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1, 1);
		commit(g);
	}
}
