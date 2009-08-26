package de.uni_koblenz.jgralabtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.parser.Greql2Lexer;
import de.uni_koblenz.jgralab.greql2.parser.Greql2Parser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Schema;
import de.uni_koblenz.jgralab.impl.FreeIndexList;
import de.uni_koblenz.jgralab.impl.GraphImpl;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralabtest.schemas.vertextest.A;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

public class LoadTest {

	public static void main(String[] args) {
		LoadTest t = new LoadTest();
		t.testFreeElementList();
	}

	protected Graph createTestGraph() throws Exception {
		String query = "from i:c report i end where d:=\"drölfundfünfzig\", c:=b, b:=a, a:=\"Mensaessen\"";
		Greql2Lexer lexer = new Greql2Lexer(new ANTLRStringStream(query));
		CommonTokenStream tokens = new CommonTokenStream();
		tokens.setTokenSource(lexer);
		Greql2Parser parser = new Greql2Parser(tokens);
		parser.greqlExpression();
		// parser.saveGraph("createdTestGraph.tg");
		return parser.getGraph();
	}

	@Test
	public void testFreeElementList() {
		Greql2 g1 = null;
		Greql2 g2 = null;
		try {
			g1 = (Greql2) createTestGraph();
			GraphIO.saveGraphToFile("testgraph.tg", g1, null);
			g2 = Greql2Schema.instance().loadGreql2("testgraph.tg");
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
		Edge v1 = g1.getFirstEdgeInGraph();
		Edge v2 = g2.getFirstEdgeInGraph();
		while (v1 != null) {
			if (v2 == null) {
				fail();
			}
			assertEquals(v1.getId(), v2.getId());
			assertEquals(v1.getAttributedElementClass().getQualifiedName(), v2
					.getAttributedElementClass().getQualifiedName());
			v1 = v1.getNextEdgeInGraph();
			v2 = v2.getNextEdgeInGraph();
		}
		if (v2 != null) {
			fail();
		}
	}

	private void fillVertexList(Greql2 g1, Greql2 g2) {
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

	private void removeVertices(Greql2 g1, Greql2 g2) {
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
			Field f = GraphImpl.class
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
	 */
	@Test
	public void allocateIndexTest0() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(1, 1);
		A v1 = graph.createA();
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		checkFreeIndexList(vList, 1, 0, 16, -1);
		v1.delete();
		checkFreeIndexList(vList, 0, 1, 16, 1);
	}

	/**
	 * Test if graph has a limit of one vertex and you want to create another
	 * one.
	 */
	@Test
	public void allocateIndexTest1() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(1, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		graph.createA();
		checkFreeIndexList(vList, 1, 0, 16, -1);
		graph.createA();
		checkFreeIndexList(vList, 2, 0, 16, -2);
	}

	/**
	 * Test if you fill runs completely by the allocate method.
	 */
	@Test
	public void allocateIndexTest2() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(1, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		for (int i = 0; i < 18; i++) {
			graph.createA();
		}
		// delete every second vertex
		for (int i = 1; i < 15; i = i + 2) {
			graph.getVertex(i).delete();
		}
		graph.getVertex(2).delete();
		graph.getVertex(15).delete();
		checkFreeIndexList(vList, 9, 23, 16, 3, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -3, 14);
		graph.createA();
		checkFreeIndexList(vList, 10, 22, 16, -1, 2, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -3, 14);
	}

	/**
	 * Test if you reach an increase of runs by the allocate method.
	 * runs=[2,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1] and then create a new
	 * vertex.
	 */
	@Test
	public void allocateIndexTest3() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(17, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		for (int i = 1; i <= 17; i++) {
			graph.createA();
		}
		// delete the vertices
		graph.getVertex(1).delete();
		graph.getVertex(2).delete();
		for (int i = 4; i <= 17; i = i + 2) {
			graph.getVertex(i).delete();
		}
		checkFreeIndexList(vList, 8, 9, 16, 2, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1);
		graph.createA();
		checkFreeIndexList(vList, 9, 8, 32, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1,
				-1, 1, -1, 1, -1, 1, -1);
	}

	/**
	 * Test if you create 17 vertices and you delete every second(FreeIndexList
	 * must be increased).
	 */
	@Test
	public void freeRangeTest0() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(1, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		for (int i = 0; i < 17; i++) {
			graph.createA();
		}
		checkFreeIndexList(vList, 17, 15, 16, -17, 15);
		// delete every second vertex
		for (int i = 2; i < 17; i = i + 2) {
			graph.getVertex(i).delete();
		}
		checkFreeIndexList(vList, 9, 23, 32, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1,
				-1, 1, -1, 1, -1, 1, -1, 15);
	}

	/**
	 * A graph which has at most 2 vertices. One vertex is created and deleted.
	 */
	@Test
	public void freeRangeTest1() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(2, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		A v1 = graph.createA();
		checkFreeIndexList(vList, 1, 1, 16, -1, 1);
		v1.delete();
		checkFreeIndexList(vList, 0, 2, 16, 2);
	}

	/**
	 * Delete first node at a FreeIndexList.runs= [-2, 1, -1, 1, -1, 1, -1, 1,
	 * -1, 1, -1, 1, -1, 1, -1, 1]
	 */
	@Test
	public void freeRangeTest2() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(17, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		// create vertices
		for (int i = 0; i < 17; i++) {
			graph.createA();
		}
		checkFreeIndexList(vList, 17, 0, 16, -17);
		// delete vertices to fill runs
		// runs[0]==-2
		for (int i = 2; i < 17; i = i + 2) {
			graph.getVertex(i + 1).delete();
		}
		checkFreeIndexList(vList, 9, 8, 16, -2, 1, -1, 1, -1, 1, -1, 1, -1, 1,
				-1, 1, -1, 1, -1, 1);
		// delete the first vertex. Runs must be enlarged.
		graph.getVertex(1).delete();
		checkFreeIndexList(vList, 8, 9, 32, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1, 1);
	}

	/**
	 * FreeIndexList.runs= [1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -3, 0,
	 * 0] Delete the middle vertex of -3.
	 */
	@Test
	public void freeRangeTest3() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(16, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		// create vertices
		for (int i = 0; i < 16; i++) {
			graph.createA();
		}
		checkFreeIndexList(vList, 16, 0, 16, -16);
		// delete odd vertices to fill runs
		for (int i = 0; i < 14; i = i + 2) {
			graph.getVertex(i + 1).delete();
		}
		checkFreeIndexList(vList, 9, 7, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -3);
		// delete the last vertex. runs[runs.length-1]==0
		graph.getVertex(15).delete();
		checkFreeIndexList(vList, 8, 8, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1);
	}

	/**
	 * FreeIndexList.runs= [1, -1, 1, -3, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 0,
	 * 0] Delete the middle vertex of -3.
	 */
	@Test
	public void freeRangeTest4() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(16, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		// create vertices
		for (int i = 0; i < 16; i++) {
			graph.createA();
		}
		checkFreeIndexList(vList, 16, 0, 16, -16);
		// delete odd vertices to fill runs
		for (int i = 0; i < 16; i = i + 2) {
			if (i != 4) {
				graph.getVertex(i + 1).delete();
			}
		}
		checkFreeIndexList(vList, 9, 7, 16, 1, -1, 1, -3, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1);
		// delete the last vertex. runs[runs.length-1]==0
		graph.getVertex(5).delete();
		checkFreeIndexList(vList, 8, 8, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1);
	}

	/**
	 * FreeIndexList.runs= [-1, 1, -2, 0,...] Delete the first vertex of -2.
	 */
	@Test
	public void freeRangeTest5() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(4, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		graph.createA();
		graph.createA();
		graph.createA();
		graph.createA();
		graph.getVertex(2).delete();
		checkFreeIndexList(vList, 3, 1, 16, -1, 1, -2);
		graph.getVertex(3).delete();
		checkFreeIndexList(vList, 2, 2, 16, -1, 2, -1);
	}

	/**
	 * FreeIndexList.runs= [-1, 1, -1, 0,...] Delete the last used index.
	 */
	@Test
	public void freeRangeTest6() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(3, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		graph.createA();
		graph.createA();
		graph.createA();
		graph.getVertex(2).delete();
		checkFreeIndexList(vList, 2, 1, 16, -1, 1, -1);
		graph.getVertex(3).delete();
		checkFreeIndexList(vList, 1, 2, 16, -1, 2);
	}

	/**
	 * FreeIndexList.runs= [1,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-2] Delete the
	 * last used index.
	 */
	@Test
	public void freeRangeTest7() {
		VertexTestGraph graph = VertexTestSchema.instance()
				.createVertexTestGraph(17, 1);
		FreeIndexList vList = getFreeIndexListOfVertices(graph, true);
		for (int i = 1; i <= 17; i++) {
			graph.createA();
		}
		for (int i = 15; i > 0; i = i - 2) {
			graph.getVertex(i).delete();
		}
		checkFreeIndexList(vList, 9, 8, 16, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -2);
		graph.getVertex(17).delete();
		checkFreeIndexList(vList, 8, 9, 32, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1,
				1, -1, 1, -1, 1, -1, 1);
	}
}
