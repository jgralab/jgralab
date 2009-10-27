package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.std.GraphImpl;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.vertextest.DoubleSubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.Link;
import de.uni_koblenz.jgralabtest.schemas.vertextest.LinkBack;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubLink;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SuperNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

@RunWith(Parameterized.class)
public class GraphTest extends InstanceTest {

	public GraphTest(boolean transactionsEnabled) {
		super(transactionsEnabled);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private VertexTestGraph g1;
	private VertexTestGraph g2;
	private Vertex v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12;
	private VertexClass subN = null, superN = null, doubleSubN = null;
	private EdgeClass link = null, subL = null, lBack = null;

	@Before
	public void setUp() throws CommitFailedException {

		g1 = createNewGraph();
		g2 = createNewGraph();
		createTransaction(g1);
		// System.out.println("Graph2 is instance of class " + g2.getClass());
		v1 = g1.createVertex(SubNode.class);
		// System.out.println("V1 is instance of class " + v1.getClass());
		v2 = g1.createVertex(SubNode.class);
		v3 = g1.createVertex(SubNode.class);
		v4 = g1.createVertex(SubNode.class);
		v5 = g1.createVertex(SuperNode.class);
		v6 = g1.createVertex(SuperNode.class);
		v7 = g1.createVertex(SuperNode.class);
		v8 = g1.createVertex(SuperNode.class);
		v9 = g1.createVertex(DoubleSubNode.class);
		// System.out.println("v9= " + v9);
		v10 = g1.createVertex(DoubleSubNode.class);
		v11 = g1.createVertex(DoubleSubNode.class);
		v12 = g1.createVertex(DoubleSubNode.class);
		commit(g1);
	}

	private VertexTestGraph createNewGraph() {
		return transactionsEnabled ? VertexTestSchema.instance()
				.createVertexTestGraphWithTransactionSupport()
				: VertexTestSchema.instance().createVertexTestGraph();
	}

	@After
	public void tearDown() {
		// try {
		// graph.commit();
		// graph2.commit();
		// } catch (CommitFailedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public void getVertexClasses() {
		// get vertex- and edge-classes
		// VertexClass abstractSuperN;
		List<VertexClass> vclasses = g1.getSchema()
				.getVertexClassesInTopologicalOrder();
		for (VertexClass vc : vclasses) {
			// if (vc.getSimpleName().equals("AbstractSuperNode")) {
			// abstractSuperN = vc;
			// } else
			if (vc.getSimpleName().equals("SubNode")) {
				subN = vc;
			} else if (vc.getSimpleName().equals("SuperNode")) {
				superN = vc;
			} else if (vc.getSimpleName().equals("DoubleSubNode")) {
				doubleSubN = vc;
			}
		}
	}

	public void getEdgeClasses() {
		// preparations...
		List<EdgeClass> eclasses = g1.getSchema()
				.getEdgeClassesInTopologicalOrder();
		for (EdgeClass ec : eclasses) {
			if (ec.getSimpleName().equals("Link")) {
				link = ec;
			} else if (ec.getSimpleName().equals("SubLink")) {
				subL = ec;
			} else if (ec.getSimpleName().equals("LinkBack")) {
				lBack = ec;
			}
		}
	}

	@Test
	public void testCreateVertex() throws CommitFailedException {
		createTransaction(g1);
		createTransaction(g2);
		Vertex v13 = g1.createVertex(SubNode.class);
		Vertex v14 = g2.createVertex(SubNode.class);
		Vertex v15 = g1.createVertex(SuperNode.class);
		Vertex v16 = g2.createVertex(SuperNode.class);
		Vertex v17 = g1.createVertex(DoubleSubNode.class);
		Vertex v18 = g2.createVertex(DoubleSubNode.class);
		commit(g1);
		commit(g2);

		Vertex[] graphVertices = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12, v13, v15, v17 };
		Vertex[] graph2Vertices = { v14, v16, v18 };

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		// tests whether the vertex is an instance of the expected class
		assertTrue(v1 instanceof SubNode);
		assertTrue(v2 instanceof SubNode);
		assertTrue(v3 instanceof SubNode);
		assertTrue(v4 instanceof SubNode);
		assertTrue(v5 instanceof SuperNode);
		assertTrue(v6 instanceof SuperNode);
		assertTrue(v7 instanceof SuperNode);
		assertTrue(v8 instanceof SuperNode);
		assertTrue(v9 instanceof DoubleSubNode);
		assertTrue(v10 instanceof DoubleSubNode);
		assertTrue(v11 instanceof DoubleSubNode);
		assertTrue(v12 instanceof DoubleSubNode);
		assertTrue(v13 instanceof SubNode);
		assertTrue(v14 instanceof SubNode);
		assertTrue(v15 instanceof SuperNode);
		assertTrue(v16 instanceof SuperNode);
		assertTrue(v17 instanceof DoubleSubNode);
		assertTrue(v18 instanceof DoubleSubNode);

		// tests whether the graphs contain the right vertices in the right
		// order
		int i = 0;// the position of the vertex corresponding to the one
		// currently returned by the iterator
		for (Vertex v : g1.vertices()) {
			assertEquals(graphVertices[i], v);
			i++;
		}
		i = 0;
		for (Vertex v : g2.vertices()) {
			assertEquals(graph2Vertices[i], v);
			i++;
		}
		commit(g1);
		commit(g2);

	}

	@Test
	public void testCreateEdge() throws CommitFailedException {
		createTransaction(g1);
		Edge e1 = g1.createEdge(SubLink.class, v9, v5);
		Edge e2 = g1.createEdge(SubLink.class, v10, v6);
		Edge e3 = g1.createEdge(SubLink.class, v12, v8);
		Edge e4 = g1.createEdge(Link.class, v1, v5);
		Edge e5 = g1.createEdge(Link.class, v2, v6);
		Edge e6 = g1.createEdge(Link.class, v9, v6);
		Edge e7 = g1.createEdge(Link.class, v10, v5);
		Edge e8 = g1.createEdge(Link.class, v11, v6);
		Edge e9 = g1.createEdge(LinkBack.class, v5, v1);
		Edge e10 = g1.createEdge(LinkBack.class, v6, v2);
		Edge e11 = g1.createEdge(LinkBack.class, v5, v9);
		Edge e12 = g1.createEdge(LinkBack.class, v6, v10);
		Edge e13 = g1.createEdge(LinkBack.class, v5, v12);
		Edge e14 = g1.createEdge(LinkBack.class, v6, v10); // the same as e12
		commit(g1);

		// tests whether the edge is an instance of the expected class
		createReadOnlyTransaction(g1);
		assertTrue(e1 instanceof SubLink);
		assertTrue(e2 instanceof SubLink);
		assertTrue(e3 instanceof SubLink);
		assertTrue(e4 instanceof Link);
		assertTrue(e5 instanceof Link);
		assertTrue(e6 instanceof Link);
		assertTrue(e7 instanceof Link);
		assertTrue(e8 instanceof Link);
		assertTrue(e9 instanceof LinkBack);
		assertTrue(e10 instanceof LinkBack);
		assertTrue(e11 instanceof LinkBack);
		assertTrue(e12 instanceof LinkBack);
		assertTrue(e13 instanceof LinkBack);
		assertTrue(e14 instanceof LinkBack);
		/*
		 * tests whether the edges are part of the right graph and have been
		 * inserted in the right order
		 */
		Edge[] graphEdges = { e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11,
				e12, e13, e14 };
		int i = 0;// refers to the position of the edge which the iterator
		// currently returns
		for (Edge e : g1.edges()) {
			assertEquals(graphEdges[i], e);
			i++;
		}

		// tests whether the alpha-/ omega-vertex of the edge have been set
		// correctly
		assertEquals(v9, e1.getAlpha());
		assertEquals(v5, e1.getOmega());
		assertEquals(v10, e2.getAlpha());
		assertEquals(v6, e2.getOmega());
		assertEquals(v12, e3.getAlpha());
		assertEquals(v8, e3.getOmega());
		assertEquals(v1, e4.getAlpha());
		assertEquals(v5, e4.getOmega());
		assertEquals(v2, e5.getAlpha());
		assertEquals(v6, e5.getOmega());
		assertEquals(v9, e6.getAlpha());
		assertEquals(v6, e6.getOmega());
		assertEquals(v10, e7.getAlpha());
		assertEquals(v5, e7.getOmega());
		assertEquals(v11, e8.getAlpha());
		assertEquals(v6, e8.getOmega());
		assertEquals(v5, e9.getAlpha());
		assertEquals(v1, e9.getOmega());
		assertEquals(v6, e10.getAlpha());
		assertEquals(v2, e10.getOmega());
		assertEquals(v5, e11.getAlpha());
		assertEquals(v9, e11.getOmega());
		assertEquals(v6, e12.getAlpha());
		assertEquals(v10, e12.getOmega());
		assertEquals(v5, e13.getAlpha());
		assertEquals(v12, e13.getOmega());
		assertEquals(v6, e14.getAlpha());
		assertEquals(v10, e14.getOmega());
		commit(g1);
	}

	/**
	 * Tests if an exception is thrown if an edge between already deleted
	 * vertices is created. This test covers the case if the alpha vertex has
	 * been deleted.
	 * 
	 * @throws Exception
	 */
	@Test(expected = GraphException.class)
	public void testCreateEdgeAlphaDeleted() throws Exception {
		// use g1, delete v1 and try to create a link between v1 and v5
		createTransaction(g1);
		g1.deleteVertex(v1);
		commit(g1);

		createTransaction(g1);
		g1.createLink((SubNode) v1, (SuperNode) v5);
		commit(g1);
	}

	/**
	 * Tests if an exception is thrown if an edge between already deleted
	 * vertices is created. This test covers the case if the omega vertex has
	 * been deleted.
	 * 
	 * @throws Exception
	 */
	@Test(expected = GraphException.class)
	public void testCreateEdgeOmegaDeleted() throws Exception {
		// use g1, delete v5 and try to create a link between v1 and v5
		createTransaction(g1);
		g1.deleteVertex(v5);
		commit(g1);

		createTransaction(g1);
		g1.createLink((SubNode) v1, (SuperNode) v5);
		commit(g1);
	}

	/**
	 * Tests if an exception is thrown if an edge between already deleted
	 * vertices is created. This test covers the case if alpha and omega
	 * vertices have been deleted.
	 * 
	 * @throws Exception
	 */
	@Test(expected = GraphException.class)
	public void testCreateEdgeAlphaAndOmegaDeleted() throws Exception {
		// use g1, delete v5 and v5 and try to create a link between v1 and v5
		createTransaction(g1);
		g1.deleteVertex(v1);
		g1.deleteVertex(v5);
		commit(g1);

		createTransaction(g1);
		g1.createLink((SubNode) v1, (SuperNode) v5);
		commit(g1);
	}

	@Test
	public void testIsLoading() throws CommitFailedException {
		// TODO how do I get isLoading to return true
		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertEquals(false, g1.isLoading());
		assertEquals(false, g2.isLoading());
		commit(g1);
		commit(g2);
		/*
		 * try{ // graph =VertexTestSchema.instance().loadVertexTestGraph(
		 * "de.uni_koblenz.VertexTestSchema.tg");
		 * 
		 * VertexTestGraph graph3 =
		 * VertexTestSchema.instance().loadVertexTestGraph
		 * ("VertexTestSchema.tg"); }catch (GraphIOException e){
		 * e.printStackTrace(); }
		 */
	}

	@Test
	public void testLoadingCompleted() throws CommitFailedException {
		// TODO
		createReadOnlyTransaction(g1);
		GraphTestKlasse gTest = new GraphTestKlasse(g1.getGraphClass());
		commit(g1);

		assertEquals("nothing", gTest.getDone());

		/*
		 * try { graph =VertexTestSchema.instance().loadVertexTestGraph(
		 * "../../../testschemas/VertexTestSchema.tg"); } catch
		 * (GraphIOException e) { e.printStackTrace(); }
		 */

		// assertEquals("loadingCompleted", gTest.getDone());
	}

	@Test
	public void testIsGraphModified() throws Exception {
		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		long gVersion1 = g1.getGraphVersion();
		long gVersion2 = g2.getGraphVersion();
		assertEquals(false, g1.isGraphModified(gVersion1));
		assertEquals(false, g2.isGraphModified(gVersion2));
		commit(g1);
		commit(g2);

		createTransaction(g1);
		createTransaction(g2);
		g1.createEdge(SubLink.class, v9, v5);
		g2.createSubNode();
		commit(g1);
		commit(g2);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertEquals(true, g1.isGraphModified(gVersion1));
		assertEquals(true, g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();
		gVersion2 = g2.getGraphVersion();
		commit(g1);
		commit(g2);

		createTransaction(g1);
		createTransaction(g2);
		Edge e1 = g1.createEdge(Link.class, v1, v6);
		g2.createSuperNode();
		commit(g1);
		commit(g2);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertEquals(true, g1.isGraphModified(gVersion1));
		assertEquals(true, g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();
		gVersion2 = g2.getGraphVersion();
		commit(g1);
		commit(g2);

		createTransaction(g1);
		createTransaction(g2);
		g1.createEdge(LinkBack.class, v7, v10);
		g2.createDoubleSubNode();
		commit(g1);
		commit(g2);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertEquals(true, g1.isGraphModified(gVersion1));
		assertEquals(true, g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();
		gVersion2 = g2.getGraphVersion();
		commit(g1);
		commit(g2);

		createTransaction(g1);
		Edge e2 = g1.createEdge(SubLink.class, v9, v5);
		commit(g1);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertEquals(true, g1.isGraphModified(gVersion1));
		assertEquals(false, g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();
		commit(g1);
		commit(g2);

		createTransaction(g1);
		g1.deleteEdge(e1);
		commit(g1);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertEquals(true, g1.isGraphModified(gVersion1));
		assertEquals(false, g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();
		commit(g1);
		commit(g2);

		createTransaction(g1);
		g1.deleteVertex(v3);
		commit(g1);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertEquals(true, g1.isGraphModified(gVersion1));
		assertEquals(false, g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();
		commit(g1);
		commit(g2);

		createTransaction(g1);
		g1.deleteEdge(e2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.containsVertex(v9));
		assertTrue(g1.containsVertex(v5));
		commit(g1);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertEquals(true, g1.isGraphModified(gVersion1));
		assertEquals(false, g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();
		commit(g1);
		commit(g2);

		// TODO why does an exception occur here?
		createTransaction(g1);
		g1.deleteVertex(v9);
		commit(g1);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertEquals(true, g1.isGraphModified(gVersion1));
		assertEquals(false, g2.isGraphModified(gVersion2));
		commit(g1);
		commit(g2);
		System.out.println("Done testing isGraphModified.");
	}

	@Test
	public void testGetGraphVersion() throws CommitFailedException {
		createReadOnlyTransaction(g2);
		long graphVersion2 = g2.getGraphVersion();
		assertEquals(0, graphVersion2);
		commit(g2);

		createTransaction(g2);
		g2.createDoubleSubNode();
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(graphVersion2 < g2.getGraphVersion());
		graphVersion2 = g2.getGraphVersion();
		commit(g2);

		createTransaction(g2);
		g2.createDoubleSubNode();
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(graphVersion2 < g2.getGraphVersion());
		graphVersion2 = g2.getGraphVersion();
		commit(g2);

		createTransaction(g2);
		DoubleSubNode v1 = g2.createDoubleSubNode();
		g2.createDoubleSubNode();
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(graphVersion2 < g2.getGraphVersion());
		graphVersion2 = g2.getGraphVersion();
		commit(g2);

		for (int i = 0; i < 20; i++) {
			createTransaction(g2);
			g2.createSubNode();
			commit(g2);

			createReadOnlyTransaction(g2);
			assertTrue(graphVersion2 < g2.getGraphVersion());
			graphVersion2 = g2.getGraphVersion();
			commit(g2);
		}
		createReadOnlyTransaction(g2);
		assertEquals(graphVersion2, g2.getGraphVersion());
		commit(g2);

		createTransaction(g2);
		g2.deleteVertex(v1);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(graphVersion2 < g2.getGraphVersion());
		graphVersion2 = g2.getGraphVersion();
		commit(g2);
		System.out.println("Done testing getGraphVersion.");
	}

	@Test
	public void testIsVertexListModified() throws CommitFailedException {
		// border cases
		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		long vListVersion1 = g1.getVertexListVersion();
		long vListVersion2 = g2.getVertexListVersion();
		commit(g1);
		commit(g2);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertFalse(g1.isVertexListModified(vListVersion1));
		assertFalse(g2.isVertexListModified(vListVersion2));
		commit(g1);
		commit(g2);

		createTransaction(g1);
		Vertex v1 = g1.createVertex(DoubleSubNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		Vertex v2 = g1.createVertex(SuperNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		// makes sure that changing edges does not affect the vertexList
		createTransaction(g1);
		g1.createEdge(SubLink.class, v1, v2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		g1.createEdge(Link.class, v1, v2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		g1.deleteVertex(v2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		// normal cases
		for (int i = 0; i < 21; i++) {
			createTransaction(g1);
			g1.createVertex(SubNode.class);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertTrue(g1.isVertexListModified(vListVersion1));
			vListVersion1 = g1.getVertexListVersion();
			assertFalse(g1.isVertexListModified(vListVersion1));
			commit(g1);
		}

		createTransaction(g1);
		g1.deleteVertex(v1);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		for (int i = 0; i < 12; i++) {
			createTransaction(g1);
			g1.createVertex(SuperNode.class);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertTrue(g1.isVertexListModified(vListVersion1));
			vListVersion1 = g1.getVertexListVersion();
			assertFalse(g1.isVertexListModified(vListVersion1));
			commit(g1);
		}
		createReadOnlyTransaction(g1);
		vListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		Vertex v3 = g1.createVertex(SubNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		Vertex v4 = g1.createVertex(SuperNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		// if the order of the vertices is changed the vertexList is modified
		createTransaction(g1);
		v3.putAfter(v4);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		v3.putAfter(v4);// v3 is already after v4
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		v3.putBefore(v4);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		Vertex v5 = g1.createVertex(DoubleSubNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		v5.putBefore(v3);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		v4.putAfter(v5);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		// if attributes of vertices are changed this does not affect the
		// vertexList
		try {
			createTransaction(g1);
			v4.setAttribute("number", 5);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertFalse(g1.isVertexListModified(vListVersion1));
			commit(g1);

			createTransaction(g1);
			v4.setAttribute("number", 42);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertFalse(g1.isVertexListModified(vListVersion1));
			commit(g1);

		} catch (NoSuchFieldException e) {
			// :(
		}
		System.out.println("Done testing isVertexListModified.");
	}

	@Test
	public void testGetVertexListVersion() throws CommitFailedException {

		// border cases
		createReadOnlyTransaction(g2);
		long vertexListVersion2 = g2.getVertexListVersion();
		// assertEquals(0, vertexListVersion2);
		commit(g2);

		createTransaction(g2);
		Vertex v13 = g2.createVertex(SuperNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();
		commit(g2);

		// normal cases
		createReadOnlyTransaction(g1);
		long vertexListVersion1 = g1.getVertexListVersion();
		// assertEquals(12, vertexListVersion1); with transactions enabled it is
		// not 12
		commit(g1);

		createTransaction(g1);
		g1.createVertex(SubNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g2);
		g2.createVertex(SuperNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();
		commit(g2);

		createTransaction(g2);
		g2.createVertex(DoubleSubNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();
		commit(g2);

		for (int i = 4; i < 100; i++) {
			createTransaction(g2);
			g2.createVertex(SuperNode.class);
			commit(g2);

			createReadOnlyTransaction(g2);
			assertTrue(vertexListVersion2 < g2.getVertexListVersion());
			vertexListVersion2 = g2.getVertexListVersion();
			commit(g2);
		}

		createTransaction(g2);
		g2.createVertex(DoubleSubNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();
		commit(g2);

		// tests whether the version changes correctly if vertices are deleted
		createTransaction(g2);
		g2.deleteVertex(v13);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();
		commit(g2);

		for (int i = 14; i < 31; i += 3) {
			createTransaction(g1);
			g1.createVertex(DoubleSubNode.class);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertTrue(vertexListVersion1 < g1.getVertexListVersion());
			vertexListVersion1 = g1.getVertexListVersion();
			commit(g1);

			createTransaction(g1);
			g1.createVertex(SubNode.class);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertTrue(vertexListVersion1 < g1.getVertexListVersion());
			vertexListVersion1 = g1.getVertexListVersion();
			commit(g1);

			createTransaction(g1);
			g1.createVertex(SuperNode.class);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertTrue(vertexListVersion1 < g1.getVertexListVersion());
			vertexListVersion1 = g1.getVertexListVersion();
			commit(g1);
		}

		createTransaction(g1);
		Vertex v14 = g1.createVertex(SuperNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		Vertex v15 = g1.createVertex(DoubleSubNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		// TODO deleted vertices should not be used for new edges
		// createTransaction(g1);
		// g1.deleteVertex(v15);
		// commit(g1);
		//
		// createReadOnlyTransaction(g1);
		// assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		// vertexListVersion1 = g1.getVertexListVersion();
		// commit(g1);

		// createTransaction(g1);
		// g1.deleteVertex(v14);
		// commit(g1);
		//
		// createReadOnlyTransaction(g1);
		// assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		// vertexListVersion1 = g1.getVertexListVersion();
		// commit(g1);

		// makes sure that editing edges does not change the vertexList
		createTransaction(g1);
		g1.createEdge(SubLink.class, v15, v14);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(vertexListVersion1, g1.getVertexListVersion());
		commit(g1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v14, v15);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(vertexListVersion1, g1.getVertexListVersion());
		commit(g1);

		// reordering the vertices does change the vertexListVersion
		createTransaction(g1);
		v3.putAfter(v7);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		v5.putBefore(v2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		v5.putAfter(v3);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		v7.putBefore(v2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		v7.putBefore(v2);// v7 is already before v2
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(vertexListVersion1, g1.getVertexListVersion());
		commit(g1);

		// changing attributes of vertices does not change the vertexListVersion
		try {
			createTransaction(g1);
			v5.setAttribute("number", 17);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertEquals(vertexListVersion1, g1.getVertexListVersion());
			commit(g1);

			createTransaction(g1);
			v8.setAttribute("number", 42);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertEquals(vertexListVersion1, g1.getVertexListVersion());
			commit(g1);

			createTransaction(g1);
			v7.setAttribute("number", 2);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertEquals(vertexListVersion1, g1.getVertexListVersion());
			commit(g1);

			createTransaction(g1);
			v5.setAttribute("number", 15);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertEquals(vertexListVersion1, g1.getVertexListVersion());
			commit(g1);
		} catch (NoSuchFieldException e) {
			// :(
			e.printStackTrace();
		}

		System.out.println("Done testing getVertexListVersion.");
	}

	@Test
	public void testIsEdgeListModified() throws CommitFailedException {
		// preparations...
		createTransaction(g2);
		Vertex v13 = g2.createVertex(SubNode.class);
		Vertex v14 = g2.createVertex(SubNode.class);
		Vertex v15 = g2.createVertex(SubNode.class);
		Vertex v16 = g2.createVertex(SubNode.class);
		Vertex v17 = g2.createVertex(SuperNode.class);
		Vertex v18 = g2.createVertex(SuperNode.class);
		Vertex v19 = g2.createVertex(SuperNode.class);
		Vertex v20 = g2.createVertex(SuperNode.class);
		Vertex v21 = g2.createVertex(DoubleSubNode.class);
		Vertex v22 = g2.createVertex(DoubleSubNode.class);
		Vertex v23 = g2.createVertex(DoubleSubNode.class);
		Vertex v24 = g2.createVertex(DoubleSubNode.class);
		commit(g2);

		// border cases
		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		long edgeListVersion1 = g1.getEdgeListVersion();
		long edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g1.isEdgeListModified(edgeListVersion1));
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g1);
		commit(g2);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v11, v7);
		commit(g1);

		createTransaction(g2);
		Edge e1 = g2.createEdge(Link.class, v15, v19);
		commit(g2);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertTrue(g1.isEdgeListModified(edgeListVersion1));
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion1 = g1.getEdgeListVersion();
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g1.isEdgeListModified(edgeListVersion1));
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g1);
		commit(g2);

		createTransaction(g2);
		g2.deleteEdge(e1);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		// normal cases
		createReadOnlyTransaction(g2);
		int ecount = g2.getECount();
		commit(g2);

		createTransaction(g2);
		g2.createEdge(LinkBack.class, v19, v15);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		assertEquals(ecount + 1, g2.getECount());
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(Link.class, v15, v19);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e2 = g2.createEdge(SubLink.class, v23, v19);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(Link.class, v16, v20);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e3 = g2.createEdge(Link.class, v23, v20);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(Link.class, v24, v19);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(LinkBack.class, v20, v16);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e4 = g2.createEdge(SubLink.class, v24, v20);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.deleteEdge(e2);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(LinkBack.class, v19, v23);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(LinkBack.class, v20, v24);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.deleteEdge(e4);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.deleteEdge(e3);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e5 = g2.createEdge(SubLink.class, v21, v17);
		commit(g2);

		createTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e6 = g2.createEdge(Link.class, v13, v18);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e7 = g2.createEdge(LinkBack.class, v17, v14);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(Link.class, v22, v18);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		// adding vertices does not affect the edgeList
		createTransaction(g2);
		Vertex v25 = g2.createVertex(DoubleSubNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Vertex v26 = g2.createVertex(SuperNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.deleteVertex(v20);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		// reordering edges does change the edgeList
		createTransaction(g2);
		e6.putBeforeInGraph(e5);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		e5.putAfterInGraph(e6);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		e5.putAfterInGraph(e7);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		// changing the attributes of an edge does not change the edgeList
		createTransaction(g2);
		Edge e8 = g2.createEdge(SubLink.class, v25, v26);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);
		try {
			createTransaction(g2);
			e8.setAttribute("anInt", 2);
			commit(g2);

			createReadOnlyTransaction(g2);
			assertFalse(g2.isEdgeListModified(edgeListVersion2));
			commit(g2);

			createTransaction(g2);
			e8.setAttribute("anInt", -41);
			commit(g2);

			createReadOnlyTransaction(g2);
			assertFalse(g2.isEdgeListModified(edgeListVersion2));
			commit(g2);

			createTransaction(g2);
			e8.setAttribute("anInt", 1024);
			commit(g2);

			createReadOnlyTransaction(g2);
			assertFalse(g2.isEdgeListModified(edgeListVersion2));
			commit(g2);

			createTransaction(g2);
			e8.setAttribute("anInt", 15);
			commit(g2);

			createReadOnlyTransaction(g2);
			assertFalse(g2.isEdgeListModified(edgeListVersion2));
			commit(g2);
		} catch (NoSuchFieldException e) {
			// :(
			e.printStackTrace();
		}

		System.out.println("Done testing isEdgeListModified.");
	}

	// TODO continue here
	@Test
	public void testGetEdgeListVersion() throws Exception {
		createTransaction(g1);
		// preparations...
		Vertex v1 = g1.createVertex(SubNode.class);
		Vertex v2 = g1.createVertex(SubNode.class);
		Vertex v3 = g1.createVertex(SubNode.class);
		Vertex v4 = g1.createVertex(SubNode.class);
		Vertex v5 = g1.createVertex(SuperNode.class);
		Vertex v6 = g1.createVertex(SuperNode.class);
		Vertex v7 = g1.createVertex(SuperNode.class);
		Vertex v8 = g1.createVertex(SuperNode.class);
		Vertex v9 = g1.createVertex(DoubleSubNode.class);
		Vertex v10 = g1.createVertex(DoubleSubNode.class);
		Vertex v11 = g1.createVertex(DoubleSubNode.class);
		Vertex v12 = g1.createVertex(DoubleSubNode.class);
		commit(g1);

		long elv1;
		createReadOnlyTransaction(g1);
		// border cases
		elv1 = g1.getEdgeListVersion();
		assertEquals(0, elv1);
		assertEquals(0, g2.getEdgeListVersion());
		commit(g1);

		createTransaction(g1);
		Edge e1 = g1.createEdge(SubLink.class, v9, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteEdge(e1);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// normal cases
		createTransaction(g1);
		g1.createEdge(SubLink.class, v10, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v10, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v10, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v10, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v11, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v11, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v11, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v11, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v12, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v12, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v12, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e3 = g1.createEdge(SubLink.class, v12, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v9, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v9, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v9, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteEdge(e3);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// when deleting a vertex, incident edges are also deleted and the
		// edgeListVersion changes.
		createTransaction(g1);
		Vertex v13 = g1.createVertex(DoubleSubNode.class);
		Vertex v14 = g1.createVertex(DoubleSubNode.class);
		g1.createEdge(Link.class, v13, v14);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteVertex(v13);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// when deleting a vertex with degree=0, the edgeListVersion should
		// remain unchanged.
		createTransaction(g1);
		g1.deleteVertex(v14);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v1, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v2, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v3, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v4, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v10, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v11, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v12, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// v6 does not exist anymore
		createTransaction(g1);
		g1.createEdge(Link.class, v1, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v1, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v1, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e4 = g1.createEdge(Link.class, v3, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v11, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v5, v1);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v6, v2);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e5 = g1.createEdge(LinkBack.class, v7, v3);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v8, v4);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v8, v9);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v7, v10);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v6, v11);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e6 = g1.createEdge(LinkBack.class, v5, v12);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteEdge(e4);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteEdge(e5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteEdge(e6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// reordering edges does change the edgeListVersion
		createTransaction(g1);
		Edge e7 = g1.createEdge(SubLink.class, v9, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e8 = g1.createEdge(SubLink.class, v12, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e9 = g1.createEdge(SubLink.class, v11, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		e7.putBeforeInGraph(e9);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		e8.putBeforeInGraph(e7);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

		createTransaction(g1);
		e9.putAfterInGraph(e8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		e8.putAfterInGraph(e7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// changing attributes does not change the edgeListVersion
		createTransaction(g1);
		e7.setAttribute("anInt", 22);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

		createTransaction(g1);
		e8.setAttribute("anInt", 203);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

		createTransaction(g1);
		e9.setAttribute("anInt", 2209);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

		createTransaction(g1);
		e7.setAttribute("anInt", 15);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

		System.out.println("Done testing getEdgeListVersion.");
	}

	/**
	 * Asserts true if the edgeListVersion has changed. Returns the new
	 * edgeListVersion.
	 * 
	 * @param elv1
	 *            the edgeListVersion before the transaction.
	 * @return the edgeListVersion after the transaction.
	 * @throws CommitFailedException
	 *             should not happen.
	 */
	private long checkIfEdgeListVersionChanged(long elv1)
			throws CommitFailedException {
		long out;
		createReadOnlyTransaction(g1);
		assertTrue(elv1 < g1.getEdgeListVersion());
		out = g1.getEdgeListVersion();
		commit(g1);
		return out;
	}

	/**
	 * Asserts true if the edgeListVersion has not changed.
	 * 
	 * @param elv1
	 *            the edgeListVersion before the transaction.
	 * @throws CommitFailedException
	 *             should not happen.
	 */
	private void checkIfEdgeListVersionRemained(long elv1)
			throws CommitFailedException {
		createReadOnlyTransaction(g1);
		assertTrue(elv1 == g1.getEdgeListVersion());
		commit(g1);
	}

	@Test
	public void testContainsVertex() throws CommitFailedException {
		createTransaction(g1);
		createTransaction(g2);
		DoubleSubNode v13 = g1.createDoubleSubNode();
		DoubleSubNode v14 = g2.createDoubleSubNode();
		SubNode v15 = g1.createSubNode();
		SubNode v16 = g2.createSubNode();
		SuperNode v17 = g1.createSuperNode();
		SuperNode v18 = g2.createSuperNode();
		Vertex v19 = g2.createVertex(DoubleSubNode.class);
		Vertex v20 = g2.createVertex(SubNode.class);
		Vertex v21 = g2.createVertex(SuperNode.class);
		commit(g1);
		commit(g2);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertTrue(g1.containsVertex(v13));
		assertTrue(g1.containsVertex(v15));
		assertTrue(g1.containsVertex(v17));
		assertTrue(g2.containsVertex(v14));
		assertTrue(g2.containsVertex(v16));
		assertTrue(g2.containsVertex(v18));
		assertTrue(g2.containsVertex(v19));
		assertTrue(g2.containsVertex(v20));
		assertTrue(g2.containsVertex(v21));
		assertTrue(g1.containsVertex(v1));
		assertTrue(g1.containsVertex(v2));
		assertTrue(g1.containsVertex(v3));
		assertTrue(g1.containsVertex(v4));
		assertTrue(g1.containsVertex(v5));
		assertTrue(g1.containsVertex(v6));
		assertTrue(g1.containsVertex(v7));
		assertTrue(g1.containsVertex(v8));
		assertTrue(g1.containsVertex(v9));
		assertTrue(g1.containsVertex(v10));
		assertTrue(g1.containsVertex(v11));
		assertTrue(g1.containsVertex(v12));

		assertFalse(g1.containsVertex(v14));
		assertFalse(g1.containsVertex(v16));
		assertFalse(g1.containsVertex(v18));
		assertFalse(g2.containsVertex(v13));
		assertFalse(g2.containsVertex(v15));
		assertFalse(g2.containsVertex(v17));
		assertFalse(g2.containsVertex(v10));
		assertFalse(g2.containsVertex(v1));
		assertFalse(g2.containsVertex(v4));
		assertFalse(g1.containsVertex(v19));
		assertFalse(g1.containsVertex(v20));
		assertFalse(g1.containsVertex(v21));
		assertFalse(g2.containsVertex(v1));
		assertFalse(g2.containsVertex(v2));
		assertFalse(g2.containsVertex(v3));
		assertFalse(g2.containsVertex(v4));
		assertFalse(g2.containsVertex(v5));
		assertFalse(g2.containsVertex(v6));
		assertFalse(g2.containsVertex(v7));
		assertFalse(g2.containsVertex(v8));
		assertFalse(g2.containsVertex(v9));
		assertFalse(g2.containsVertex(v10));
		assertFalse(g2.containsVertex(v11));
		assertFalse(g2.containsVertex(v12));
		commit(g1);
		commit(g2);

		// deleting vertices changes the contains-information accordingly
		createTransaction(g1);
		g1.deleteVertex(v1);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsVertex(v1));
		commit(g1);

		createTransaction(g1);
		g1.deleteVertex(v9);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsVertex(v9));
		commit(g1);

		createTransaction(g2);
		g2.deleteVertex(v14);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertFalse(g2.containsVertex(v14));
		commit(g2);

		System.out.println("Done testing containsVertex.");
	}

	@Test
	public void testContainsEdge() throws CommitFailedException {
		createTransaction(g1);
		createTransaction(g2);
		DoubleSubNode v13 = g2.createDoubleSubNode();
		DoubleSubNode v14 = g2.createDoubleSubNode();
		SubNode v15 = g2.createSubNode();
		SubNode v16 = g2.createSubNode();
		SuperNode v17 = g2.createSuperNode();
		SuperNode v18 = g2.createSuperNode();

		Edge e1 = g1.createEdge(SubLink.class, v9, v7);
		SubLink e2 = g2.createSubLink(v13, v17);
		Edge e3 = g1.createEdge(Link.class, v10, v5);
		Link e4 = g2.createLink(v15, v17);
		Edge e5 = g1.createEdge(LinkBack.class, v7, v1);
		LinkBack e6 = g2.createLinkBack(v17, v13);
		Edge e7 = g1.createEdge(SubLink.class, v10, v5);
		Edge e8 = g1.createEdge(Link.class, v3, v7);
		Edge e9 = g1.createEdge(LinkBack.class, v5, v9);
		Edge e10 = g1.createEdge(SubLink.class, v9, v5);
		Edge e11 = g2.createEdge(SubLink.class, v14, v17);
		Edge e12 = g2.createEdge(Link.class, v16, v18);
		Edge e13 = g2.createEdge(LinkBack.class, v18, v13);
		commit(g1);
		commit(g2);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertTrue(g1.containsEdge(e1));
		assertTrue(g2.containsEdge(e2));
		assertTrue(g1.containsEdge(e3));
		assertTrue(g2.containsEdge(e4));
		assertTrue(g1.containsEdge(e5));
		assertTrue(g2.containsEdge(e6));
		assertTrue(g1.containsEdge(e7));
		assertTrue(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e9));
		assertTrue(g1.containsEdge(e10));
		assertTrue(g2.containsEdge(e11));
		assertTrue(g2.containsEdge(e12));
		assertTrue(g2.containsEdge(e13));

		assertFalse(g1.containsEdge(null));
		assertFalse(g2.containsEdge(null));
		assertFalse(g2.containsEdge(e1));
		assertFalse(g1.containsEdge(e2));
		assertFalse(g2.containsEdge(e3));
		assertFalse(g1.containsEdge(e4));
		assertFalse(g2.containsEdge(e5));
		assertFalse(g1.containsEdge(e6));
		assertFalse(g2.containsEdge(e7));
		assertFalse(g2.containsEdge(e8));
		assertFalse(g2.containsEdge(e9));
		assertFalse(g2.containsEdge(e10));
		assertFalse(g1.containsEdge(e11));
		assertFalse(g1.containsEdge(e12));
		assertFalse(g1.containsEdge(e13));
		commit(g1);
		commit(g2);

		// when a vertex is deleted, the edges to which it belonged are deleted
		// as well
		createTransaction(g1);
		e1 = g1.createEdge(SubLink.class, v10, v12);
		Edge e14 = g1.createEdge(SubLink.class, v9, v6);
		Edge e17 = g1.createEdge(LinkBack.class, v8, v10);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.containsEdge(e1));
		assertTrue(g1.containsEdge(e17));
		commit(g1);

		createTransaction(g1);
		g1.deleteVertex(v10);
		// create new instances of implicitly deleted vertices
		v12 = g1.createVertex(DoubleSubNode.class);
		commit(g1);

		// all edges from or to v10 do no longer exist
		createReadOnlyTransaction(g1);
		// check if implicitly deleted vertex v5 was really deleted and check if
		// its incident edges have been deleted.
		assertFalse(g1.containsVertex(v5));
		assertFalse(g1.containsEdge(e9));
		assertFalse(g1.containsEdge(e10));

		assertFalse(g1.containsEdge(e1));
		assertFalse(g1.containsEdge(e3));
		assertFalse(g1.containsEdge(e7));
		assertFalse(g1.containsEdge(e17));
		// all other edges do still exist
		assertTrue(g1.containsEdge(e5));
		assertTrue(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e14));
		commit(g1);

		createTransaction(g1);
		Edge e15 = g1.createEdge(LinkBack.class, v6, v11);
		Edge e16 = g1.createEdge(Link.class, v12, v8);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.containsEdge(e15));
		// assertTrue(g1.containsEdge(e16));
		commit(g1);

		createTransaction(g1);
		g1.deleteEdge(e5);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsEdge(e5));
		assertTrue(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e14));
		assertTrue(g1.containsEdge(e15));
		// assertTrue(g1.containsEdge(e16));
		commit(g1);

		createTransaction(g1);
		g1.deleteEdge(e16);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsEdge(e16));
		assertTrue(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e14));
		assertTrue(g1.containsEdge(e15));
		commit(g1);

		createTransaction(g1);
		g1.deleteEdge(e14);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsEdge(e14));
		assertTrue(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e15));
		commit(g1);

		createTransaction(g1);
		g1.deleteEdge(e8);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e15));
		commit(g1);

		createTransaction(g1);
		g1.deleteEdge(e15);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsEdge(e15));
		commit(g1);

		System.out.println("Done testing containsEdge.");
	}

	@Test
	public void testDeleteVertex() throws CommitFailedException {
		// TODO:
		// Removes the vertex from the vertex sequence of this graph.
		// any edges incident to the vertex are deleted
		// If the vertex is the parent of a composition, all child vertices are
		// deleted.
		// Pre: v.isValid()
		// Post: !v.isValid() && !containsVertex(v) && getVertex(v.getId()) ==
		// null
		createTransaction(g1);
		g1.deleteVertex(v1);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsVertex(v1));
		createTransaction(g1);
		g1.deleteVertex(v2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsVertex(v2));
		createTransaction(g1);
		g1.deleteVertex(v3);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsVertex(v3));
		commit(g1);

		createTransaction(g1);
		g1.deleteVertex(v7);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsVertex(v7));
		commit(g1);

		createTransaction(g1);
		g1.deleteVertex(v5);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsVertex(v5));
		commit(g1);

		createTransaction(g1);
		g1.deleteVertex(v6);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsVertex(v6));
		commit(g1);

		createTransaction(g1);
		g1.deleteVertex(v9);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsVertex(v9));
		commit(g1);

		createTransaction(g1);
		g1.deleteVertex(v10);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsVertex(v10));
		commit(g1);

		createTransaction(g1);
		g1.deleteVertex(v11);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.containsVertex(v11));
		commit(g1);

	}

	/**
	 * Tests if an exception is thrown if a vertex from another graph is subject
	 * to be removed.
	 * 
	 * @throws CommitFailedException
	 */
	@Test(expected = GraphException.class)
	public void testDeleteVertex2() throws CommitFailedException {
		createTransaction(g2);
		DoubleSubNode v15 = g2.createDoubleSubNode();
		commit(g2);

		createTransaction(g1);
		g1.deleteVertex(v15);
		commit(g1);
	}

	@Test
	public void testVertexDeleted() {
		onlyTestWithoutTransactionSupport();
		GraphTestKlasse gTest = new GraphTestKlasse(g1.getGraphClass());
		String name = "";

		Vertex v1 = gTest.createVertex(SuperNode.class);
		name = "vertexDeleted" + v1.toString();
		gTest.deleteVertex(v1);
		assertEquals(name, gTest.getDone());

		Vertex v2 = gTest.createVertex(SubNode.class);
		Vertex v3 = gTest.createVertex(DoubleSubNode.class);
		name = "vertexDeleted" + v2.toString();
		gTest.deleteVertex(v2);
		assertEquals(name, gTest.getDone());

		Vertex v4 = gTest.createVertex(SubNode.class);
		Vertex v5 = gTest.createVertex(SuperNode.class);
		Vertex v6 = gTest.createVertex(DoubleSubNode.class);
		Vertex v7 = gTest.createVertex(DoubleSubNode.class);
		Vertex v8 = gTest.createVertex(SuperNode.class);
		Vertex v9 = gTest.createVertex(SubNode.class);
		Vertex v10 = gTest.createVertex(SubNode.class);

		name = "vertexDeleted" + v10.toString();
		gTest.deleteVertex(v10);
		assertEquals(name, gTest.getDone());

		name = "vertexDeleted" + v5.toString();
		gTest.deleteVertex(v5);
		assertEquals(name, gTest.getDone());

		name = "vertexDeleted" + v7.toString();
		gTest.deleteVertex(v7);
		assertEquals(name, gTest.getDone());

		name = "vertexDeleted" + v3.toString();
		gTest.deleteVertex(v3);
		assertEquals(name, gTest.getDone());

		name = "vertexDeleted" + v9.toString();
		gTest.deleteVertex(v9);
		assertEquals(name, gTest.getDone());

		name = "vertexDeleted" + v6.toString();
		gTest.deleteVertex(v6);
		assertEquals(name, gTest.getDone());

		name = "vertexDeleted" + v4.toString();
		gTest.deleteVertex(v4);
		assertEquals(name, gTest.getDone());

		name = "vertexDeleted" + v8.toString();
		gTest.deleteVertex(v8);
		assertEquals(name, gTest.getDone());

		gTest = new GraphTestKlasse(g2.getGraphClass());
		Vertex v11 = gTest.createVertex(DoubleSubNode.class);
		name = "vertexDeleted" + v11.toString();
		gTest.deleteVertex(v11);
		assertEquals(name, gTest.getDone());

		Vertex v12 = gTest.createVertex(DoubleSubNode.class);
		Vertex v13 = gTest.createVertex(SuperNode.class);
		Vertex v14 = gTest.createVertex(SubNode.class);
		name = "vertexDeleted" + v14.toString();
		gTest.deleteVertex(v14);
		assertEquals(name, gTest.getDone());

		name = "vertexDeleted" + v13.toString();
		gTest.deleteVertex(v13);
		assertEquals(name, gTest.getDone());

		Vertex v15 = gTest.createVertex(SubNode.class);
		Vertex v16 = gTest.createVertex(SuperNode.class);
		name = "vertexDeleted" + v15.toString();
		gTest.deleteVertex(v15);
		assertEquals(name, gTest.getDone());

		name = "vertexDeleted" + v12.toString();
		gTest.deleteVertex(v12);
		assertEquals(name, gTest.getDone());

		name = "vertexDeleted" + v16.toString();
		gTest.deleteVertex(v16);
		assertEquals(name, gTest.getDone());

		System.out.println("Done testing vertexDeleted.");
	}

	@Test
	public void testVertexAdded() {
		onlyTestWithoutTransactionSupport();
		GraphTestKlasse gTest = new GraphTestKlasse(g1.getGraphClass());
		Vertex v = gTest.createVertex(SubNode.class);
		assertEquals("vertexAdded" + v.toString(), gTest.getDone());

		v = gTest.createVertex(SuperNode.class);
		assertEquals("vertexAdded" + v.toString(), gTest.getDone());

		v = gTest.createVertex(DoubleSubNode.class);
		assertEquals("vertexAdded" + v.toString(), gTest.getDone());

		v = gTest.createVertex(SuperNode.class);
		assertEquals("vertexAdded" + v.toString(), gTest.getDone());

		v = gTest.createVertex(DoubleSubNode.class);
		assertEquals("vertexAdded" + v.toString(), gTest.getDone());

		for (int i = 0; i < 100; i++) {
			v = gTest.createVertex(SubNode.class);
			assertEquals("vertexAdded" + v.toString(), gTest.getDone());
		}

		v = gTest.createVertex(SuperNode.class);
		assertEquals("vertexAdded" + v.toString(), gTest.getDone());

		gTest = new GraphTestKlasse(g2.getGraphClass());
		v = gTest.createVertex(SuperNode.class);
		assertEquals("vertexAdded" + v.toString(), gTest.getDone());

		v = gTest.createVertex(DoubleSubNode.class);
		assertEquals("vertexAdded" + v.toString(), gTest.getDone());

		v = gTest.createVertex(SubNode.class);
		assertEquals("vertexAdded" + v.toString(), gTest.getDone());

		System.out.println("Done testing vertexAdded.");
	}

	@Test
	public void testDeleteEdge() {
		onlyTestWithoutTransactionSupport();
		/*
		 * TODO apparently one cannot delete ALL edges of the same type (here:
		 * SubLink) after deleting a vertex?
		 */
		// TODO faults => assertions
		Link e1 = g1.createEdge(Link.class, v1, v6);
		Link e2 = g1.createEdge(Link.class, v11, v5);
		Link e3 = g1.createEdge(Link.class, v2, v8);
		SubLink e4 = g1.createEdge(SubLink.class, v11, v6);
		SubLink e5 = g1.createEdge(SubLink.class, v12, v5);
		LinkBack e6 = g1.createEdge(LinkBack.class, v6, v11);
		LinkBack e7 = g1.createEdge(LinkBack.class, v5, v2);
		LinkBack e8 = g1.createEdge(LinkBack.class, v8, v1);
		LinkBack e9 = g1.createEdge(LinkBack.class, v5, v10);
		Link e10 = g1.createEdge(Link.class, v12, v7);
		SubLink e11 = g1.createEdge(SubLink.class, v10, v6);
		SubLink e12 = g1.createEdge(SubLink.class, v9, v7);
		// SubLink e10 = graph2.createEdge(SubLink.class, v9, v6);

		int id = e12.getId();
		g1.deleteEdge(e12);
		assertFalse(e12.isValid());
		assertFalse(g1.containsEdge(e12));
		assertEquals(null, g1.getEdge(id));

		id = e1.getId();
		g1.deleteEdge(e1);
		assertFalse(e1.isValid());
		assertFalse(g1.containsEdge(e1));
		assertEquals(null, g1.getEdge(id));

		id = e2.getId();
		g1.deleteEdge(e2);
		assertFalse(e2.isValid());
		assertFalse(g1.containsEdge(e2));
		assertEquals(null, g1.getEdge(id));

		id = e7.getId();
		g1.deleteEdge(e7);
		assertFalse(e7.isValid());
		assertFalse(g1.containsEdge(e7));
		assertEquals(null, g1.getEdge(id));

		id = e3.getId();
		g1.deleteEdge(e3);
		assertFalse(e3.isValid());
		assertFalse(g1.containsEdge(e3));
		assertEquals(null, g1.getEdge(id));

		id = e9.getId();
		g1.deleteEdge(e9);
		assertFalse(e9.isValid());
		assertFalse(g1.containsEdge(e9));
		assertEquals(null, g1.getEdge(id));

		id = e4.getId();
		g1.deleteEdge(e4);
		assertFalse(e4.isValid());
		assertFalse(g1.containsEdge(e4));
		assertEquals(null, g1.getEdge(id));

		id = e10.getId();
		g1.deleteEdge(e10);
		assertFalse(e10.isValid());
		assertFalse(g1.containsEdge(e10));
		assertEquals(null, g1.getEdge(id));

		id = e5.getId();
		g1.deleteEdge(e5);
		assertFalse(e5.isValid());
		assertFalse(g1.containsEdge(e5));
		assertEquals(null, g1.getEdge(id));

		id = e8.getId();
		g1.deleteEdge(e8);
		assertFalse(e8.isValid());
		assertFalse(g1.containsEdge(e8));
		assertEquals(null, g1.getEdge(id));

		id = e11.getId();
		g1.deleteEdge(e11);
		assertFalse(e11.isValid());
		assertFalse(g1.containsEdge(e11));
		assertEquals(null, g1.getEdge(id));

		id = e6.getId();
		g1.deleteEdge(e6);
		assertFalse(e6.isValid());
		assertFalse(g1.containsEdge(e6));
		assertEquals(null, g1.getEdge(id));

		// border cases

		// faults
		// TODO
		// cannot try to delete an edge which has never been created?
		// graph.deleteEdge(e10);

		System.out.println("Done testing deleteEdge.");
	}

	@Test
	public void testEdgeDeleted() {
		onlyTestWithoutTransactionSupport();
		GraphTestKlasse gTest = new GraphTestKlasse(g1.getGraphClass());
		String name = "";

		Vertex v1 = gTest.createVertex(SubNode.class);
		Vertex v2 = gTest.createVertex(SubNode.class);
		Vertex v3 = gTest.createVertex(SuperNode.class);
		Vertex v4 = gTest.createVertex(SuperNode.class);
		Vertex v5 = gTest.createVertex(SuperNode.class);
		Vertex v6 = gTest.createVertex(SuperNode.class);
		Vertex v7 = gTest.createVertex(DoubleSubNode.class);
		Vertex v8 = gTest.createVertex(DoubleSubNode.class);
		Vertex v9 = gTest.createVertex(DoubleSubNode.class);

		Edge e1 = gTest.createEdge(SubLink.class, v8, v6);
		Edge e2 = gTest.createEdge(SubLink.class, v7, v3);
		Edge e3 = gTest.createEdge(Link.class, v1, v4);
		Edge e4 = gTest.createEdge(Link.class, v7, v5);
		Edge e5 = gTest.createEdge(LinkBack.class, v3, v2);
		Edge e6 = gTest.createEdge(LinkBack.class, v6, v9);

		name = "edgeDeleted" + e4.toString();
		gTest.deleteEdge(e4);
		assertEquals(name, gTest.getDone());

		name = "edgeDeleted" + e2.toString();
		gTest.deleteEdge(e2);
		assertEquals(name, gTest.getDone());

		name = "edgeDeleted" + e6.toString();
		gTest.deleteEdge(e6);
		assertEquals(name, gTest.getDone());

		name = "edgeDeleted" + e3.toString();
		gTest.deleteEdge(e3);
		assertEquals(name, gTest.getDone());

		name = "edgeDeleted" + e1.toString();
		gTest.deleteEdge(e1);
		assertEquals(name, gTest.getDone());

		name = "edgeDeleted" + e5.toString();
		gTest.deleteEdge(e5);
		assertEquals(name, gTest.getDone());

		Edge e7 = gTest.createEdge(Link.class, v2, v3);
		name = "edgeDeleted" + e7.toString();
		gTest.deleteEdge(e7);
		assertEquals(name, gTest.getDone());

		Edge e8 = gTest.createEdge(LinkBack.class, v5, v8);
		name = "edgeDeleted" + e8.toString();
		gTest.deleteEdge(e8);
		assertEquals(name, gTest.getDone());

		Edge e9 = gTest.createEdge(SubLink.class, v9, v4);
		name = "edgeDeleted" + e9.toString();
		gTest.deleteEdge(e9);
		assertEquals(name, gTest.getDone());

		gTest = new GraphTestKlasse(g2.getGraphClass());
		Vertex v10 = gTest.createVertex(SubNode.class);
		Vertex v11 = gTest.createVertex(SuperNode.class);
		Vertex v12 = gTest.createVertex(DoubleSubNode.class);
		Vertex v13 = gTest.createVertex(SuperNode.class);

		Edge e10 = gTest.createEdge(SubLink.class, v12, v11);
		Edge e11 = gTest.createEdge(Link.class, v12, v11);
		Edge e12 = gTest.createEdge(LinkBack.class, v11, v12);
		Edge e13 = gTest.createEdge(Link.class, v10, v13);

		name = "edgeDeleted" + e11.toString();
		gTest.deleteEdge(e11);
		assertEquals(name, gTest.getDone());

		name = "edgeDeleted" + e10.toString();
		gTest.deleteEdge(e10);
		assertEquals(name, gTest.getDone());

		name = "edgeDeleted" + e13.toString();
		gTest.deleteEdge(e13);
		assertEquals(name, gTest.getDone());

		name = "edgeDeleted" + e12.toString();
		gTest.deleteEdge(e12);
		assertEquals(name, gTest.getDone());

		System.out.println("Done testing edgeDeleted.");
	}

	@Test
	public void testEdgeAdded() {
		onlyTestWithoutTransactionSupport();
		GraphTestKlasse gTest = new GraphTestKlasse(g1.getGraphClass());

		Vertex v1 = gTest.createVertex(SubNode.class);
		Vertex v2 = gTest.createVertex(SubNode.class);
		Vertex v3 = gTest.createVertex(SuperNode.class);
		Vertex v4 = gTest.createVertex(SuperNode.class);
		Vertex v5 = gTest.createVertex(SuperNode.class);
		Vertex v6 = gTest.createVertex(SuperNode.class);
		Vertex v7 = gTest.createVertex(DoubleSubNode.class);
		Vertex v8 = gTest.createVertex(DoubleSubNode.class);
		Vertex v9 = gTest.createVertex(DoubleSubNode.class);

		Edge e = gTest.createEdge(SubLink.class, v8, v6);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		e = gTest.createEdge(SubLink.class, v7, v3);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		e = gTest.createEdge(Link.class, v1, v4);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		e = gTest.createEdge(Link.class, v7, v5);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		e = gTest.createEdge(LinkBack.class, v3, v2);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		e = gTest.createEdge(LinkBack.class, v6, v9);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		e = gTest.createEdge(Link.class, v2, v3);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		e = gTest.createEdge(LinkBack.class, v5, v8);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		e = gTest.createEdge(SubLink.class, v9, v4);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		gTest = new GraphTestKlasse(g2.getGraphClass());
		Vertex v10 = gTest.createVertex(SubNode.class);
		Vertex v11 = gTest.createVertex(SuperNode.class);
		Vertex v12 = gTest.createVertex(DoubleSubNode.class);
		Vertex v13 = gTest.createVertex(SuperNode.class);

		e = gTest.createEdge(SubLink.class, v12, v11);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		e = gTest.createEdge(Link.class, v12, v11);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		e = gTest.createEdge(LinkBack.class, v11, v12);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		e = gTest.createEdge(Link.class, v10, v13);
		assertEquals("edgeAdded" + e.toString(), gTest.getDone());

		System.out.println("Done testing edgeAdded.");
	}

	@Test
	public void testGetFirstVertex() {
		onlyTestWithoutTransactionSupport();
		assertEquals(v1, g1.getFirstVertex());
		assertEquals(null, g2.getFirstVertex());

		SubNode v13 = g2.createSubNode();
		g2.createSuperNode();

		assertEquals(v13, g2.getFirstVertex());

		g2.createDoubleSubNode();
		g2.createSubNode();
		assertEquals(v13, g2.getFirstVertex());

		g1.createDoubleSubNode();
		assertEquals(v1, g1.getFirstVertex());

		System.out.println("Done testing getFirstVertex.");
	}

	@Test
	public void testGetLastVertex() {
		onlyTestWithoutTransactionSupport();
		// border cases
		assertEquals(v12, g1.getLastVertex());
		assertEquals(null, g2.getLastVertex());

		Vertex v13 = g1.createVertex(SubNode.class);
		assertEquals(v13, g1.getLastVertex());

		// normal cases
		Vertex v14 = g1.createVertex(SubNode.class);
		assertEquals(v14, g1.getLastVertex());

		Vertex v15 = g1.createVertex(SubNode.class);
		assertEquals(v15, g1.getLastVertex());

		Vertex v16 = g1.createVertex(SubNode.class);
		assertEquals(v16, g1.getLastVertex());

		Vertex v17 = g1.createVertex(SuperNode.class);
		assertEquals(v17, g1.getLastVertex());

		Vertex v18 = g1.createVertex(SuperNode.class);
		assertEquals(v18, g1.getLastVertex());

		Vertex v19 = g1.createVertex(SuperNode.class);
		assertEquals(v19, g1.getLastVertex());

		Vertex v20 = g1.createVertex(SuperNode.class);
		assertEquals(v20, g1.getLastVertex());

		Vertex v21 = g1.createVertex(DoubleSubNode.class);
		assertEquals(v21, g1.getLastVertex());

		Vertex v22 = g1.createVertex(DoubleSubNode.class);
		assertEquals(v22, g1.getLastVertex());

		Vertex v23 = g1.createVertex(DoubleSubNode.class);
		assertEquals(v23, g1.getLastVertex());

		Vertex v24 = g1.createVertex(DoubleSubNode.class);
		assertEquals(v24, g1.getLastVertex());

		System.out.println("Done testing getLastVertex.");
	}

	@Test
	public void testGetFirstVertexOfClass() {
		onlyTestWithoutTransactionSupport();
		assertEquals(null, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(null, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v13 = g2.createVertex(SubNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(null, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v14 = g2.createVertex(SuperNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v15 = g2.createVertex(SubNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v16 = g2.createVertex(SubNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v17 = g2.createVertex(DoubleSubNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v18 = g2.createVertex(SuperNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v19 = g2.createVertex(SubNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v20 = g2.createVertex(DoubleSubNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(DoubleSubNode.class));

		g2.deleteVertex(v14);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v21 = g2.createVertex(DoubleSubNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(DoubleSubNode.class));

		g2.deleteVertex(v16);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(DoubleSubNode.class));

		g2.createVertex(SuperNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(DoubleSubNode.class));

		g2.createVertex(DoubleSubNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(DoubleSubNode.class));

		g2.deleteVertex(v13);
		assertEquals(v15, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, g2.getFirstVertexOfClass(DoubleSubNode.class));

		g2.deleteVertex(v17);
		assertEquals(v15, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v18, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v20, g2.getFirstVertexOfClass(DoubleSubNode.class));

		g2.deleteVertex(v20);
		assertEquals(v15, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v18, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v21, g2.getFirstVertexOfClass(DoubleSubNode.class));

		g2.createVertex(SuperNode.class);
		assertEquals(v15, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v18, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v21, g2.getFirstVertexOfClass(DoubleSubNode.class));

		g2.deleteVertex(v15);
		assertEquals(v19, g2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v18, g2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v21, g2.getFirstVertexOfClass(DoubleSubNode.class));

		System.out.println("Done testing getFirstVertexOfClass.");
	}

	@Test
	public void testGetFirstVertexOfClass2() {
		onlyTestWithoutTransactionSupport();
		assertEquals(null, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(null, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(null, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(null, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		Vertex v13 = g2.createVertex(SubNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(null, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(null, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		Vertex v14 = g2.createVertex(SuperNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		Vertex v15 = g2.createVertex(DoubleSubNode.class);
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v13, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		g2.deleteVertex(v13);
		assertEquals(null, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v15, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		Vertex v16 = g2.createVertex(SubNode.class);
		assertEquals(v16, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v15, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		Vertex v17 = g2.createVertex(SubNode.class);
		assertEquals(v16, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v15, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		g2.deleteVertex(v16);
		assertEquals(v17, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v15, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		Vertex v18 = g2.createVertex(DoubleSubNode.class);
		assertEquals(v17, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v15, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v15, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		g2.deleteVertex(v15);
		assertEquals(v17, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v17, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v18, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v18, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		Vertex v19 = g2.createVertex(SubNode.class);
		assertEquals(v17, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v17, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v18, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v18, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		g2.deleteVertex(v17);
		assertEquals(v19, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v18, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v18, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v18, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		Vertex v20 = g2.createVertex(DoubleSubNode.class);
		assertEquals(v19, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v18, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v18, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v18, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		g2.deleteVertex(v18);
		assertEquals(v19, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v19, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v20, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v20, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		Vertex v21 = g2.createVertex(SuperNode.class);
		assertEquals(v19, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v19, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v20, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v20, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		g2.deleteVertex(v14);
		assertEquals(v19, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v19, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v21, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v20, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v20, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v20, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		g2.deleteVertex(v19);
		assertEquals(null, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v20, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v21, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v20, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v20, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v20, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		Vertex v22 = g2.createVertex(SubNode.class);
		assertEquals(v22, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v20, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v21, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v20, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v20, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v20, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		g2.deleteVertex(v20);
		assertEquals(v22, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v22, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v21, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v21, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(null, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		Vertex v23 = g2.createVertex(DoubleSubNode.class);
		assertEquals(v22, g2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v22, g2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v21, g2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v21, g2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v23, g2.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v23, g2.getFirstVertexOfClass(DoubleSubNode.class, false));

		System.out.println("Done testing getFirstVertexOfClass2.");
	}

	@Test
	public void testGetFirstVertexOfClass3() {
		onlyTestWithoutTransactionSupport();
		// preparations
		getVertexClasses();

		assertEquals(null, g2.getFirstVertexOfClass(subN));
		assertEquals(null, g2.getFirstVertexOfClass(superN));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN));

		SuperNode v13 = g2.createSuperNode();
		assertEquals(null, g2.getFirstVertexOfClass(subN));
		assertEquals(v13, g2.getFirstVertexOfClass(superN));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN));

		SubNode v14 = g2.createSubNode();
		assertEquals(v14, g2.getFirstVertexOfClass(subN));
		assertEquals(v13, g2.getFirstVertexOfClass(superN));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN));

		SuperNode v15 = g2.createSuperNode();
		assertEquals(v14, g2.getFirstVertexOfClass(subN));
		assertEquals(v13, g2.getFirstVertexOfClass(superN));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN));

		DoubleSubNode v16 = g2.createDoubleSubNode();
		assertEquals(v14, g2.getFirstVertexOfClass(subN));
		assertEquals(v13, g2.getFirstVertexOfClass(superN));
		assertEquals(v16, g2.getFirstVertexOfClass(doubleSubN));

		g2.deleteVertex(v13);
		assertEquals(v14, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(v16, g2.getFirstVertexOfClass(doubleSubN));

		DoubleSubNode v17 = g2.createDoubleSubNode();
		assertEquals(v14, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(v16, g2.getFirstVertexOfClass(doubleSubN));

		SubNode v18 = g2.createSubNode();
		assertEquals(v14, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(v16, g2.getFirstVertexOfClass(doubleSubN));

		g2.deleteVertex(v14);
		assertEquals(v16, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(v16, g2.getFirstVertexOfClass(doubleSubN));

		g2.deleteVertex(v16);
		assertEquals(v17, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(v17, g2.getFirstVertexOfClass(doubleSubN));

		g2.deleteVertex(v17);
		assertEquals(v18, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN));

		DoubleSubNode v19 = g2.createDoubleSubNode();
		assertEquals(v18, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(v19, g2.getFirstVertexOfClass(doubleSubN));

		g2.deleteVertex(v18);
		assertEquals(v19, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(v19, g2.getFirstVertexOfClass(doubleSubN));

		SubNode v20 = g2.createSubNode();
		assertEquals(v19, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(v19, g2.getFirstVertexOfClass(doubleSubN));

		SuperNode v21 = g2.createSuperNode();
		assertEquals(v19, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(v19, g2.getFirstVertexOfClass(doubleSubN));

		g2.deleteVertex(v19);
		assertEquals(v20, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN));

		g2.deleteVertex(v20);
		assertEquals(null, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN));

		SubNode v22 = g2.createSubNode();
		assertEquals(v22, g2.getFirstVertexOfClass(subN));
		assertEquals(v15, g2.getFirstVertexOfClass(superN));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN));

		g2.deleteVertex(v15);
		assertEquals(v22, g2.getFirstVertexOfClass(subN));
		assertEquals(v21, g2.getFirstVertexOfClass(superN));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN));

		System.out.println("Done testing getFirstVertexOfClass3.");
	}

	@Test
	public void testGetFirstVertexOfClass4() {
		onlyTestWithoutTransactionSupport();
		// preparations
		getVertexClasses();

		// start testing
		assertEquals(null, g2.getFirstVertexOfClass(subN, true));
		assertEquals(null, g2.getFirstVertexOfClass(subN, false));
		assertEquals(null, g2.getFirstVertexOfClass(superN, true));
		assertEquals(null, g2.getFirstVertexOfClass(superN, false));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN, false));

		DoubleSubNode v13 = g2.createDoubleSubNode();
		assertEquals(null, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(subN, false));
		assertEquals(null, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v13, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(doubleSubN, false));

		SuperNode v14 = g2.createSuperNode();
		assertEquals(null, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v13, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(doubleSubN, false));

		DoubleSubNode v15 = g2.createDoubleSubNode();
		assertEquals(null, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v13, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(doubleSubN, false));

		SubNode v16 = g2.createSubNode();
		assertEquals(v16, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v13, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v13, g2.getFirstVertexOfClass(doubleSubN, false));

		g2.deleteVertex(v13);
		assertEquals(v16, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v15, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v14, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v15, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v15, g2.getFirstVertexOfClass(doubleSubN, false));

		g2.deleteVertex(v16);
		assertEquals(null, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v15, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v14, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v15, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v15, g2.getFirstVertexOfClass(doubleSubN, false));

		SubNode v17 = g2.createSubNode();
		assertEquals(v17, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v15, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v14, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v15, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v15, g2.getFirstVertexOfClass(doubleSubN, false));

		DoubleSubNode v18 = g2.createDoubleSubNode();
		assertEquals(v17, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v15, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v14, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v15, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v15, g2.getFirstVertexOfClass(doubleSubN, false));

		g2.deleteVertex(v14);
		assertEquals(v17, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v15, g2.getFirstVertexOfClass(subN, false));
		assertEquals(null, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v15, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v15, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v15, g2.getFirstVertexOfClass(doubleSubN, false));

		g2.deleteVertex(v15);
		assertEquals(v17, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v17, g2.getFirstVertexOfClass(subN, false));
		assertEquals(null, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v18, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(doubleSubN, false));

		g2.deleteVertex(v17);
		assertEquals(null, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(subN, false));
		assertEquals(null, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v18, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(doubleSubN, false));

		SubNode v19 = g2.createSubNode();
		assertEquals(v19, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(subN, false));
		assertEquals(null, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v18, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(doubleSubN, false));

		DoubleSubNode v20 = g2.createDoubleSubNode();
		assertEquals(v19, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(subN, false));
		assertEquals(null, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v18, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(doubleSubN, false));

		SuperNode v21 = g2.createSuperNode();
		assertEquals(v19, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v18, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v18, g2.getFirstVertexOfClass(doubleSubN, false));

		g2.deleteVertex(v18);
		assertEquals(v19, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v19, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v20, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v20, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v20, g2.getFirstVertexOfClass(doubleSubN, false));

		g2.deleteVertex(v19);
		assertEquals(null, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v20, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v20, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v20, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v20, g2.getFirstVertexOfClass(doubleSubN, false));

		SubNode v22 = g2.createSubNode();
		assertEquals(v22, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v20, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v20, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v20, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v20, g2.getFirstVertexOfClass(doubleSubN, false));

		g2.deleteVertex(v20);
		assertEquals(v22, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v22, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v21, g2.getFirstVertexOfClass(superN, false));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN, false));

		DoubleSubNode v23 = g2.createDoubleSubNode();
		assertEquals(v22, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v22, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v21, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v23, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v23, g2.getFirstVertexOfClass(doubleSubN, false));

		g2.deleteVertex(v22);
		assertEquals(null, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v23, g2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v21, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v23, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v23, g2.getFirstVertexOfClass(doubleSubN, false));

		g2.deleteVertex(v21);
		assertEquals(null, g2.getFirstVertexOfClass(subN, true));
		assertEquals(v23, g2.getFirstVertexOfClass(subN, false));
		assertEquals(null, g2.getFirstVertexOfClass(superN, true));
		assertEquals(v23, g2.getFirstVertexOfClass(superN, false));
		assertEquals(v23, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v23, g2.getFirstVertexOfClass(doubleSubN, false));

		g2.deleteVertex(v23);
		assertEquals(null, g2.getFirstVertexOfClass(subN, true));
		assertEquals(null, g2.getFirstVertexOfClass(subN, false));
		assertEquals(null, g2.getFirstVertexOfClass(superN, true));
		assertEquals(null, g2.getFirstVertexOfClass(superN, false));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(null, g2.getFirstVertexOfClass(doubleSubN, false));

		System.out.println("Done testing getFirstVertexOfClass4.");
	}

	@Test
	public void testGetFirstEdgeInGraph() {
		onlyTestWithoutTransactionSupport();
		Vertex v13 = g1.createVertex(SuperNode.class);

		assertEquals(null, g1.getFirstEdgeInGraph());

		Edge e1 = g1.createEdge(Link.class, v3, v6);
		assertEquals(e1, g1.getFirstEdgeInGraph());

		Edge e2 = g1.createEdge(Link.class, v3, v13);
		assertEquals(e1, g1.getFirstEdgeInGraph());

		Edge e3 = g1.createEdge(LinkBack.class, v7, v11);
		assertEquals(e1, g1.getFirstEdgeInGraph());

		g1.deleteEdge(e1);
		assertEquals(e2, g1.getFirstEdgeInGraph());

		g1.deleteEdge(e2);
		assertEquals(e3, g1.getFirstEdgeInGraph());

		Edge e4 = g1.createEdge(SubLink.class, v10, v8);
		assertEquals(e3, g1.getFirstEdgeInGraph());

		g1.deleteEdge(e3);
		assertEquals(e4, g1.getFirstEdgeInGraph());

		Edge e5 = g1.createEdge(LinkBack.class, v8, v3);
		assertEquals(e4, g1.getFirstEdgeInGraph());

		Edge e6 = g1.createEdge(Link.class, v2, v5);
		assertEquals(e4, g1.getFirstEdgeInGraph());

		g1.deleteEdge(e4);
		assertEquals(e5, g1.getFirstEdgeInGraph());

		Edge e7 = g1.createEdge(LinkBack.class, v13, v1);
		assertEquals(e5, g1.getFirstEdgeInGraph());

		g1.deleteEdge(e5);
		assertEquals(e6, g1.getFirstEdgeInGraph());

		Edge e8 = g1.createEdge(SubLink.class, v9, v6);
		assertEquals(e6, g1.getFirstEdgeInGraph());

		g1.deleteEdge(e7);
		assertEquals(e6, g1.getFirstEdgeInGraph());

		g1.deleteEdge(e6);
		assertEquals(e8, g1.getFirstEdgeInGraph());

		g1.deleteEdge(e8);
		assertEquals(null, g1.getFirstEdgeInGraph());

		System.out.println("Done testing getFirstEdgeInGraph.");
	}

	@Test
	public void testGetLastEdgeInGraph() {
		onlyTestWithoutTransactionSupport();
		Vertex v13 = g1.createVertex(SuperNode.class);

		assertEquals(null, g1.getLastEdgeInGraph());

		Edge e1 = g1.createEdge(Link.class, v3, v6);
		assertEquals(e1, g1.getLastEdgeInGraph());

		Edge e2 = g1.createEdge(Link.class, v3, v13);
		assertEquals(e2, g1.getLastEdgeInGraph());

		Edge e3 = g1.createEdge(LinkBack.class, v7, v11);
		assertEquals(e3, g1.getLastEdgeInGraph());

		g1.deleteEdge(e3);
		assertEquals(e2, g1.getLastEdgeInGraph());

		Edge e4 = g1.createEdge(SubLink.class, v10, v8);
		assertEquals(e4, g1.getLastEdgeInGraph());

		Edge e5 = g1.createEdge(LinkBack.class, v8, v3);
		assertEquals(e5, g1.getLastEdgeInGraph());

		Edge e6 = g1.createEdge(Link.class, v9, v5);
		assertEquals(e6, g1.getLastEdgeInGraph());

		Edge e7 = g1.createEdge(SubLink.class, v11, v7);
		assertEquals(e7, g1.getLastEdgeInGraph());

		g1.deleteEdge(e7);
		assertEquals(e6, g1.getLastEdgeInGraph());

		g1.deleteEdge(e6);
		assertEquals(e5, g1.getLastEdgeInGraph());

		Edge e8 = g1.createEdge(Link.class, v1, v13);
		assertEquals(e8, g1.getLastEdgeInGraph());

		g1.deleteEdge(e5);
		assertEquals(e8, g1.getLastEdgeInGraph());

		Edge e9 = g1.createEdge(LinkBack.class, v6, v2);
		assertEquals(e9, g1.getLastEdgeInGraph());

		g1.deleteEdge(e9);
		assertEquals(e8, g1.getLastEdgeInGraph());

		g1.deleteEdge(e8);
		assertEquals(e4, g1.getLastEdgeInGraph());

		g1.deleteEdge(e4);
		assertEquals(e2, g1.getLastEdgeInGraph());

		g1.deleteEdge(e2);
		assertEquals(e1, g1.getLastEdgeInGraph());

		g1.deleteEdge(e1);
		assertEquals(null, g1.getLastEdgeInGraph());

		System.out.println("Done testing getLastEdgeInGraph.");
	}

	@Test
	public void testGetFirstEdgeOfClassInGraph() {
		onlyTestWithoutTransactionSupport();
		Vertex v13 = g1.createVertex(SuperNode.class);

		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e1 = g1.createEdge(Link.class, v3, v6);
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e2 = g1.createEdge(Link.class, v3, v13);
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e3 = g1.createEdge(LinkBack.class, v7, v11);
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		g1.deleteEdge(e1);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e4 = g1.createEdge(SubLink.class, v10, v8);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e5 = g1.createEdge(LinkBack.class, v8, v3);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		g1.deleteEdge(e3);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e6 = g1.createEdge(Link.class, v9, v5);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		g1.deleteEdge(e2);
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e7 = g1.createEdge(SubLink.class, v11, v7);
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		g1.deleteEdge(e4);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e8 = g1.createEdge(SubLink.class, v10, v13);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e9 = g1.createEdge(LinkBack.class, v6, v1);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		g1.deleteEdge(e5);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		g1.deleteEdge(e7);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		g1.deleteEdge(e6);
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e10 = g1.createEdge(Link.class, v2, v7);
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		g1.deleteEdge(e8);
		assertEquals(e10, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		g1.deleteEdge(e10);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		g1.deleteEdge(e9);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class));

		System.out.println("Done testing getFirstEdgeOfClassInGraph.");
	}

	@Test
	public void testGetFirstEdgeOfClassInGraph2() {
		onlyTestWithoutTransactionSupport();
		Vertex v13 = g1.createVertex(SuperNode.class);

		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e1 = g1.createEdge(SubLink.class, v9, v6);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e2 = g1.createEdge(Link.class, v3, v8);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e3 = g1.createEdge(LinkBack.class, v7, v12);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		g1.deleteEdge(e1);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e4 = g1.createEdge(SubLink.class, v10, v13);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		g1.deleteEdge(e2);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		g1.deleteEdge(e3);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e5 = g1.createEdge(LinkBack.class, v13, v3);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e6 = g1.createEdge(Link.class, v9, v5);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e7 = g1.createEdge(SubLink.class, v12, v7);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		g1.deleteEdge(e4);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		g1.deleteEdge(e6);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e8 = g1.createEdge(SubLink.class, v10, v8);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		g1.deleteEdge(e5);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		g1.deleteEdge(e7);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e9 = g1.createEdge(LinkBack.class, v6, v1);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e10 = g1.createEdge(Link.class, v2, v7);
		assertEquals(e10, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		g1.deleteEdge(e9);
		assertEquals(e10, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		g1.deleteEdge(e8);
		assertEquals(e10, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e10, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		g1.deleteEdge(e10);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		System.out.println("Done testing getFirstEdgeOfClassInGraph2.");
	}

	@Test
	public void testGetFirstEdgeOfClassInGraph3() {
		onlyTestWithoutTransactionSupport();
		// preparations
		getEdgeClasses();
		Vertex v13 = g1.createVertex(SuperNode.class);

		// start tests
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack));

		Edge e1 = g1.createEdge(SubLink.class, v9, v6);
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack));

		Edge e2 = g1.createEdge(Link.class, v3, v13);
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack));

		Edge e3 = g1.createEdge(LinkBack.class, v7, v11);
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(lBack));

		g1.deleteEdge(e1);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(lBack));

		Edge e4 = g1.createEdge(SubLink.class, v10, v8);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(lBack));

		g1.deleteEdge(e3);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack));

		Edge e5 = g1.createEdge(LinkBack.class, v8, v3);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack));

		g1.deleteEdge(e2);
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack));

		Edge e6 = g1.createEdge(Link.class, v9, v5);
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack));

		g1.deleteEdge(e6);
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack));

		g1.deleteEdge(e4);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack));

		Edge e7 = g1.createEdge(SubLink.class, v11, v7);
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack));

		Edge e8 = g1.createEdge(SubLink.class, v10, v13);
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack));

		g1.deleteEdge(e5);
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack));

		Edge e9 = g1.createEdge(LinkBack.class, v6, v1);
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(lBack));

		Edge e10 = g1.createEdge(Link.class, v2, v7);
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(lBack));

		g1.deleteEdge(e7);
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(lBack));

		g1.deleteEdge(e9);
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack));

		g1.deleteEdge(e8);
		assertEquals(e10, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack));

		g1.deleteEdge(e10);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(link));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack));

		System.out.println("Done testing getFirstEdgeOfClassInGraph3.");
	}

	@Test
	public void testGetFirstEdgeOfClassInGraph4() {
		onlyTestWithoutTransactionSupport();
		// preparations...
		getEdgeClasses();
		Vertex v13 = g1.createVertex(SuperNode.class);

		Edge e1 = g1.createEdge(SubLink.class, v9, v6);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e2 = g1.createEdge(Link.class, v3, v13);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e3 = g1.createEdge(LinkBack.class, v7, v11);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e1, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(lBack, false));

		g1.deleteEdge(e1);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e4 = g1.createEdge(SubLink.class, v10, v8);
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e2, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(lBack, false));

		g1.deleteEdge(e2);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e3, g1.getFirstEdgeOfClassInGraph(lBack, false));

		g1.deleteEdge(e3);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e5 = g1.createEdge(LinkBack.class, v8, v3);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e6 = g1.createEdge(Link.class, v9, v5);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e7 = g1.createEdge(SubLink.class, v11, v7);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, false));

		g1.deleteEdge(e4);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e7, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, false));

		g1.deleteEdge(e7);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e8 = g1.createEdge(SubLink.class, v10, v13);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e9 = g1.createEdge(LinkBack.class, v6, v1);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, g1.getFirstEdgeOfClassInGraph(lBack, false));

		g1.deleteEdge(e5);
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e6, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(lBack, false));

		g1.deleteEdge(e6);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e10 = g1.createEdge(Link.class, v2, v7);
		assertEquals(e10, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e9, g1.getFirstEdgeOfClassInGraph(lBack, false));

		g1.deleteEdge(e9);
		assertEquals(e10, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, false));

		g1.deleteEdge(e8);
		assertEquals(e10, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e10, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, false));

		g1.deleteEdge(e10);
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, g1.getFirstEdgeOfClassInGraph(lBack, false));

		System.out.println("Done testing getFirstEdgeOfClassInGraph4.");
	}

	@Test
	public void testGetVertex() {
		onlyTestWithoutTransactionSupport();
		Vertex v13 = g1.createVertex(SubNode.class);
		Vertex v14 = g1.createVertex(DoubleSubNode.class);
		Vertex v15 = g1.createVertex(SuperNode.class);
		Vertex v16 = g2.createVertex(SubNode.class);
		Vertex v17 = g2.createVertex(SuperNode.class);
		Vertex v18 = g2.createVertex(DoubleSubNode.class);

		// border cases
		assertEquals(v1, g1.getVertex(1));
		assertEquals(v16, g2.getVertex(1));
		assertEquals(null, g1.getVertex(42));
		assertEquals(null, g1.getVertex(33));
		assertEquals(null, g2.getVertex(4));
		// 1000 is the highest possible value
		assertEquals(null, g1.getVertex(1000));

		// normal cases
		assertEquals(v2, g1.getVertex(2));
		assertEquals(v3, g1.getVertex(3));
		assertEquals(v4, g1.getVertex(4));
		assertEquals(v5, g1.getVertex(5));
		assertEquals(v6, g1.getVertex(6));
		assertEquals(v7, g1.getVertex(7));
		assertEquals(v8, g1.getVertex(8));
		assertEquals(v9, g1.getVertex(9));
		assertEquals(v10, g1.getVertex(10));
		assertEquals(v11, g1.getVertex(11));
		assertEquals(v12, g1.getVertex(12));
		assertEquals(v13, g1.getVertex(13));
		assertEquals(v14, g1.getVertex(14));
		assertEquals(v15, g1.getVertex(15));
		assertEquals(v17, g2.getVertex(2));
		assertEquals(v18, g2.getVertex(3));

		System.out.println("Done testing getVertex.");
	}

	@Test
	public void testGetEdge() {
		onlyTestWithoutTransactionSupport();
		Edge e1 = g1.createEdge(LinkBack.class, v5, v1);
		Edge e2 = g1.createEdge(Link.class, v2, v7);
		Edge e3 = g1.createEdge(LinkBack.class, v8, v4);
		Edge e4 = g1.createEdge(SubLink.class, v11, v6);
		Edge e5 = g1.createEdge(Link.class, v2, v5);
		Edge e6 = g1.createEdge(LinkBack.class, v7, v12);
		Edge e7 = g1.createEdge(SubLink.class, v9, v8);
		Edge e8 = g1.createEdge(SubLink.class, v10, v6);
		Edge e9 = g1.createEdge(Link.class, v3, v7);
		Edge e10 = g1.createEdge(Link.class, v3, v7);

		// border cases
		assertEquals(null, g1.getEdge(42));
		assertEquals(null, g1.getEdge(-42));
		assertEquals(e1, g1.getEdge(1));
		assertEquals(null, g1.getEdge(1000));
		assertEquals(null, g1.getEdge(-1000));

		// normal cases
		assertEquals(e2, g1.getEdge(2));
		assertEquals(e2.getReversedEdge(), g1.getEdge(-2));
		assertEquals(e3, g1.getEdge(3));
		assertEquals(e3.getReversedEdge(), g1.getEdge(-3));
		assertEquals(e4, g1.getEdge(4));
		assertEquals(e4.getReversedEdge(), g1.getEdge(-4));
		assertEquals(e5, g1.getEdge(5));
		assertEquals(e5.getReversedEdge(), g1.getEdge(-5));
		assertEquals(e6, g1.getEdge(6));
		assertEquals(e6.getReversedEdge(), g1.getEdge(-6));
		assertEquals(e7, g1.getEdge(7));
		assertEquals(e7.getReversedEdge(), g1.getEdge(-7));
		assertEquals(e8, g1.getEdge(8));
		assertEquals(e8.getReversedEdge(), g1.getEdge(-8));
		assertEquals(e9, g1.getEdge(9));
		assertEquals(e9.getReversedEdge(), g1.getEdge(-9));
		assertEquals(e10, g1.getEdge(10));
		assertEquals(e10.getReversedEdge(), g1.getEdge(-10));
	}

	@Test
	public void testGetMaxVCount() {
		onlyTestWithoutTransactionSupport();
		assertEquals(1000, g1.getMaxVCount());
		assertEquals(1000, g2.getMaxVCount());
		MinimalGraph graph3 = MinimalSchema.instance().createMinimalGraph();
		assertEquals(1000, graph3.getMaxVCount());

		System.out.println("Done testing getMaxVCount.");
	}

	@Test
	public void testGetExpandedVertexCount() {
		onlyTestWithoutTransactionSupport();
		// border case
		assertEquals(2000, g1.getExpandedVertexCount());

		// normal cases
		for (int i = 12; i < 1000; i++) {
			g1.createVertex(SubNode.class);
		}
		assertEquals(2000, g1.getExpandedVertexCount());
		for (int i = 0; i < 1000; i++) {
			g1.createVertex(SuperNode.class);
		}
		assertEquals(4000, g1.getExpandedVertexCount());
		for (int i = 0; i < 1000; i++) {
			g1.createVertex(DoubleSubNode.class);
		}
		assertEquals(8000, g1.getExpandedVertexCount());
		System.out.println("Done testing getExpandedVertexCount.");
	}

	@Test
	public void testGetExpandedEdgeCount() {
		onlyTestWithoutTransactionSupport();
		// border case
		assertEquals(2000, g1.getExpandedEdgeCount());

		// normal cases
		for (int i = 0; i < 1000; i++) {
			g1.createEdge(SubLink.class, v9, v5);
		}
		assertEquals(2000, g1.getExpandedEdgeCount());

		for (int i = 0; i < 1000; i++) {
			g1.createEdge(Link.class, v1, v5);
		}
		assertEquals(4000, g1.getExpandedEdgeCount());

		for (int i = 0; i < 1000; i++) {
			g1.createEdge(LinkBack.class, v5, v9);
		}
		assertEquals(8000, g1.getExpandedEdgeCount());

		System.out.println("Done testing getExpandedEdgeCount.");
	}

	@Test
	public void testGetMaxECount() {
		onlyTestWithoutTransactionSupport();
		assertEquals(1000, g1.getMaxECount());
		assertEquals(1000, g2.getMaxECount());
		MinimalGraph graph3 = MinimalSchema.instance().createMinimalGraph();
		assertEquals(1000, graph3.getMaxECount());

		System.out.println("Done testing getMaxECount.");
	}

	@Test
	public void testGetVCount() {
		onlyTestWithoutTransactionSupport();
		// border cases
		assertEquals(0, g2.getVCount());

		Vertex v1 = g2.createVertex(SubNode.class);
		assertEquals(1, g2.getVCount());

		g2.deleteVertex(v1);
		assertEquals(0, g2.getVCount());

		g2.createVertex(SubNode.class);
		assertEquals(1, g2.getVCount());

		// normal cases
		assertEquals(12, g1.getVCount());
		Vertex v2 = g2.createVertex(SubNode.class);
		assertEquals(2, g2.getVCount());

		g2.createVertex(SubNode.class);
		assertEquals(3, g2.getVCount());

		g2.deleteVertex(v2);
		assertEquals(2, g2.getVCount());

		g2.createVertex(SuperNode.class);
		assertEquals(3, g2.getVCount());

		Vertex v3 = g2.createVertex(SuperNode.class);
		assertEquals(4, g2.getVCount());

		g2.deleteVertex(v3);
		assertEquals(3, g2.getVCount());

		Vertex v4 = g2.createVertex(SuperNode.class);
		assertEquals(4, g2.getVCount());

		g2.createVertex(SuperNode.class);
		assertEquals(5, g2.getVCount());

		g2.createVertex(DoubleSubNode.class);
		assertEquals(6, g2.getVCount());

		g2.createVertex(DoubleSubNode.class);
		assertEquals(7, g2.getVCount());

		g2.deleteVertex(v4);
		assertEquals(6, g2.getVCount());

		g2.createVertex(DoubleSubNode.class);
		assertEquals(7, g2.getVCount());

		g2.createVertex(DoubleSubNode.class);
		assertEquals(8, g2.getVCount());

		for (int i = 9; i < 20; i++) {
			g2.createVertex(SuperNode.class);
			assertEquals(i, g2.getVCount());
		}

		for (int i = 20; i < 32; i++) {
			g2.createVertex(DoubleSubNode.class);
			assertEquals(i, g2.getVCount());
		}

		for (int i = 32; i < 42; i++) {
			g2.createVertex(SubNode.class);
			assertEquals(i, g2.getVCount());
		}

		System.out.println("Done testing getVCount.");
	}

	@Test
	public void testGetECount() {
		onlyTestWithoutTransactionSupport();
		// border cases
		assertEquals(0, g1.getECount());
		Edge e1 = g1.createEdge(LinkBack.class, v5, v1);
		assertEquals(1, g1.getECount());

		// creating a vertex does not change the value
		g1.createVertex(DoubleSubNode.class);
		assertEquals(1, g1.getECount());

		// when an edge is deleted, the count is decreased by 1
		g1.deleteEdge(e1);
		assertEquals(0, g1.getECount());

		// normal cases
		// creating an edge increases the value by 1
		Edge e2 = g1.createEdge(Link.class, v2, v7);
		assertEquals(1, g1.getECount());
		Edge e3 = g1.createEdge(LinkBack.class, v8, v4);
		assertEquals(2, g1.getECount());
		Edge e4 = g1.createEdge(SubLink.class, v11, v6);
		assertEquals(3, g1.getECount());
		Edge e5 = g1.createEdge(Link.class, v2, v5);
		assertEquals(4, g1.getECount());
		Edge e6 = g1.createEdge(LinkBack.class, v7, v12);
		assertEquals(5, g1.getECount());
		Edge e7 = g1.createEdge(SubLink.class, v9, v8);
		assertEquals(6, g1.getECount());
		Edge e8 = g1.createEdge(SubLink.class, v10, v6);
		assertEquals(7, g1.getECount());
		Edge e9 = g1.createEdge(Link.class, v3, v7);
		assertEquals(8, g1.getECount());
		Edge e10 = g1.createEdge(Link.class, v3, v7);
		assertEquals(9, g1.getECount());

		// deleting edges...
		g1.deleteEdge(e2);
		assertEquals(8, g1.getECount());
		g1.deleteEdge(e3);
		assertEquals(7, g1.getECount());
		g1.deleteEdge(e4);
		assertEquals(6, g1.getECount());
		g1.deleteEdge(e5);
		assertEquals(5, g1.getECount());
		g1.deleteEdge(e6);
		assertEquals(4, g1.getECount());
		g1.deleteEdge(e7);
		assertEquals(3, g1.getECount());
		g1.deleteEdge(e8);
		assertEquals(2, g1.getECount());
		g1.deleteEdge(e9);
		assertEquals(1, g1.getECount());
		g1.deleteEdge(e10);
		assertEquals(0, g1.getECount());

		System.out.println("Done testing getECount.");
	}

	@Test
	public void testSetId() {
		onlyTestWithoutTransactionSupport();
		g1.setId("alpha");
		assertEquals("alpha", g1.getId());

		g1.setId("1265");
		assertEquals("1265", g1.getId());

		g1.setId("007");
		assertEquals("007", g1.getId());

		g1.setId("r2d2");
		assertEquals("r2d2", g1.getId());

		g1.setId("answer:42");
		assertEquals("answer:42", g1.getId());

		g1.setId("1506");
		assertEquals("1506", g1.getId());

		g1.setId("june15");
		assertEquals("june15", g1.getId());

		g1.setId("bang");
		assertEquals("bang", g1.getId());

		g1.setId("22now");
		assertEquals("22now", g1.getId());

		g1.setId("hjkutzbv");
		assertEquals("hjkutzbv", g1.getId());

		g1.setId("54rdcg9");
		assertEquals("54rdcg9", g1.getId());

		g1.setId(".k,oibt");
		assertEquals(".k,oibt", g1.getId());

		System.out.println("Done testing setId.");
	}

	@Test
	public void testEdges() {
		onlyTestWithoutTransactionSupport();
		assertEquals(null, g1.edges().iterator().next());
		assertEquals(false, g1.edges().iterator().hasNext());

		Edge e1 = g1.createEdge(Link.class, v3, v7);
		Edge e2 = g1.createEdge(Link.class, v4, v8);
		Edge e3 = g1.createEdge(Link.class, v1, v8);
		Edge e4 = g1.createEdge(SubLink.class, v12, v5);
		Edge e5 = g1.createEdge(SubLink.class, v10, v7);
		Edge e6 = g1.createEdge(SubLink.class, v11, v5);
		Edge e7 = g1.createEdge(LinkBack.class, v6, v12);
		Edge e8 = g1.createEdge(LinkBack.class, v6, v3);
		Edge e9 = g1.createEdge(LinkBack.class, v8, v9);

		Edge[] graphEdges = { e1, e2, e3, e4, e5, e6, e7, e8, e9 };
		int i = 0;
		for (Edge e : g1.edges()) {
			assertEquals(graphEdges[i], e);
			i++;
		}

		Edge e10 = g1.createEdge(SubLink.class, v11, v6);
		Edge e11 = g1.createEdge(LinkBack.class, v7, v12);
		Edge e12 = g1.createEdge(LinkBack.class, v5, v1);
		Edge e13 = g1.createEdge(Link.class, v12, v5);
		Edge e14 = g1.createEdge(SubLink.class, v9, v7);
		Edge e15 = g1.createEdge(SubLink.class, v11, v6);

		Edge[] graphEdges2 = { e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11,
				e12, e13, e14, e15 };
		i = 0;
		for (Edge e : g1.edges()) {
			assertEquals(graphEdges2[i], e);
			i++;
		}

		Edge e16 = g1.createEdge(LinkBack.class, v5, v2);
		Edge e17 = g1.createEdge(SubLink.class, v10, v6);
		Edge e18 = g1.createEdge(LinkBack.class, v8, v12);
		Edge e19 = g1.createEdge(Link.class, v1, v7);
		Edge e20 = g1.createEdge(SubLink.class, v10, v6);
		Edge e21 = g1.createEdge(Link.class, v3, v6);

		Edge[] graphEdges3 = { e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11,
				e12, e13, e14, e15, e16, e17, e18, e19, e20, e21 };
		i = 0;
		for (Edge e : g1.edges()) {
			assertEquals(graphEdges3[i], e);
			i++;
		}

		System.out.println("Done testing edges.");
	}

	@Test
	public void testEdges2() {
		onlyTestWithoutTransactionSupport();
		// preparations...
		getEdgeClasses();

		assertEquals(null, g1.edges(link).iterator().next());
		assertEquals(false, g1.edges(link).iterator().hasNext());
		assertEquals(null, g1.edges(subL).iterator().next());
		assertEquals(false, g1.edges(subL).iterator().hasNext());
		assertEquals(null, g1.edges(lBack).iterator().next());
		assertEquals(false, g1.edges(lBack).iterator().hasNext());

		Edge e1 = g1.createEdge(Link.class, v3, v7);
		Edge e2 = g1.createEdge(Link.class, v4, v8);
		Edge e3 = g1.createEdge(Link.class, v1, v8);
		Edge e4 = g1.createEdge(SubLink.class, v12, v5);
		Edge e5 = g1.createEdge(SubLink.class, v10, v7);
		Edge e6 = g1.createEdge(SubLink.class, v11, v5);
		Edge e7 = g1.createEdge(LinkBack.class, v6, v12);
		Edge e8 = g1.createEdge(LinkBack.class, v6, v2);
		Edge e9 = g1.createEdge(LinkBack.class, v8, v9);

		Edge[] graphLink = { e1, e2, e3, e4, e5, e6 };
		int i = 0;
		for (Edge e : g1.edges(link)) {
			assertEquals(graphLink[i], e);
			i++;
		}

		Edge[] graphSubLink = { e4, e5, e6 };
		i = 0;
		for (Edge e : g1.edges(subL)) {
			assertEquals(graphSubLink[i], e);
			i++;
		}

		Edge[] graphLinkBack = { e7, e8, e9 };
		i = 0;
		for (Edge e : g1.edges(lBack)) {
			assertEquals(graphLinkBack[i], e);
			i++;
		}

		Edge e10 = g1.createEdge(SubLink.class, v11, v6);
		Edge e11 = g1.createEdge(LinkBack.class, v7, v12);
		Edge e12 = g1.createEdge(LinkBack.class, v5, v1);
		Edge e13 = g1.createEdge(Link.class, v12, v5);
		Edge e14 = g1.createEdge(SubLink.class, v9, v7);
		Edge e15 = g1.createEdge(SubLink.class, v11, v6);

		Edge[] graphLink2 = { e1, e2, e3, e4, e5, e6, e10, e13, e14, e15 };
		i = 0;
		for (Edge e : g1.edges(link)) {
			assertEquals(graphLink2[i], e);
			i++;
		}

		Edge[] graphSubLink2 = { e4, e5, e6, e10, e14, e15 };
		i = 0;
		for (Edge e : g1.edges(subL)) {
			assertEquals(graphSubLink2[i], e);
			i++;
		}

		Edge[] graphLinkBack2 = { e7, e8, e9, e11, e12 };
		i = 0;
		for (Edge e : g1.edges(lBack)) {
			assertEquals(graphLinkBack2[i], e);
			i++;
		}

		System.out.println("Done testing edges2.");
	}

	@Test
	public void testEdges3() {
		onlyTestWithoutTransactionSupport();
		Edge e1 = g1.createEdge(Link.class, v3, v7);
		Edge e2 = g1.createEdge(Link.class, v4, v8);
		Edge e3 = g1.createEdge(Link.class, v1, v8);
		Edge e4 = g1.createEdge(SubLink.class, v12, v5);
		Edge e5 = g1.createEdge(SubLink.class, v10, v7);
		Edge e6 = g1.createEdge(SubLink.class, v11, v5);
		Edge e7 = g1.createEdge(LinkBack.class, v6, v12);
		Edge e8 = g1.createEdge(LinkBack.class, v6, v3);
		Edge e9 = g1.createEdge(LinkBack.class, v8, v9);

		Edge[] graphLink = { e1, e2, e3, e4, e5, e6 };
		int i = 0;
		for (Edge e : g1.edges(Link.class)) {
			assertEquals(graphLink[i], e);
			i++;
		}

		Edge[] graphSubLink = { e4, e5, e6 };
		i = 0;
		for (Edge e : g1.edges(SubLink.class)) {
			assertEquals(graphSubLink[i], e);
			i++;
		}

		Edge[] graphLinkBack = { e7, e8, e9 };
		i = 0;
		for (Edge e : g1.edges(LinkBack.class)) {
			assertEquals(graphLinkBack[i], e);
			i++;
		}

		Edge e10 = g1.createEdge(LinkBack.class, v5, v2);
		Edge e11 = g1.createEdge(SubLink.class, v10, v6);
		Edge e12 = g1.createEdge(LinkBack.class, v8, v12);
		Edge e13 = g1.createEdge(Link.class, v1, v7);
		Edge e14 = g1.createEdge(SubLink.class, v10, v6);
		Edge e15 = g1.createEdge(Link.class, v3, v6);

		Edge[] graphLink2 = { e1, e2, e3, e4, e5, e6, e11, e13, e14, e15 };
		i = 0;
		for (Edge e : g1.edges(Link.class)) {
			assertEquals(graphLink2[i], e);
			i++;
		}

		Edge[] graphSubLink2 = { e4, e5, e6, e11, e14 };
		i = 0;
		for (Edge e : g1.edges(SubLink.class)) {
			assertEquals(graphSubLink2[i], e);
			i++;
		}

		Edge[] graphLinkBack2 = { e7, e8, e9, e10, e12 };
		i = 0;
		for (Edge e : g1.edges(LinkBack.class)) {
			assertEquals(graphLinkBack2[i], e);
			i++;
		}

		System.out.println("Done testing edges3.");
	}

	@Test
	public void testVertices() {
		onlyTestWithoutTransactionSupport();
		assertEquals(false, g2.vertices().iterator().hasNext());
		assertEquals(true, g1.vertices().iterator().hasNext());

		Vertex[] graphVertices = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12 };

		int i = 0;
		for (Vertex v : g1.vertices()) {
			assertEquals(graphVertices[i], v);
			i++;
		}

		Vertex v13 = g1.createVertex(DoubleSubNode.class);
		Vertex v14 = g1.createVertex(SuperNode.class);
		Vertex v15 = g1.createVertex(SuperNode.class);
		Vertex v16 = g1.createVertex(DoubleSubNode.class);

		Vertex[] graphVertices2 = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12, v13, v14, v15, v16 };

		i = 0;
		for (Vertex v : g1.vertices()) {
			assertEquals(graphVertices2[i], v);
			i++;
		}

		Vertex v17 = g1.createVertex(SubNode.class);
		Vertex v18 = g1.createVertex(DoubleSubNode.class);
		Vertex v19 = g1.createVertex(SubNode.class);
		Vertex v20 = g1.createVertex(SuperNode.class);
		Vertex v21 = g1.createVertex(DoubleSubNode.class);
		Vertex v22 = g1.createVertex(SuperNode.class);

		Vertex[] graphVertices3 = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22 };

		i = 0;
		for (Vertex v : g1.vertices()) {
			assertEquals(graphVertices3[i], v);
			i++;
		}

		System.out.println("Done testing vertices.");
	}

	@Test
	public void testVertices2() {
		onlyTestWithoutTransactionSupport();
		// preparations...
		getVertexClasses();

		assertEquals(false, g2.vertices(subN).iterator().hasNext());
		assertEquals(false, g2.vertices(superN).iterator().hasNext());
		assertEquals(false, g2.vertices(doubleSubN).iterator().hasNext());
		assertEquals(true, g1.vertices(subN).iterator().hasNext());
		assertEquals(true, g1.vertices(superN).iterator().hasNext());
		assertEquals(true, g1.vertices(doubleSubN).iterator().hasNext());

		Vertex[] graphSubN = { v1, v2, v3, v4, v9, v10, v11, v12 };
		int i = 0;
		for (Vertex v : g1.vertices(subN)) {
			assertEquals(graphSubN[i], v);
			i++;
		}

		Vertex[] graphSuperN = { v5, v6, v7, v8, v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : g1.vertices(superN)) {
			assertEquals(graphSuperN[i], v);
			i++;
		}

		Vertex[] graphDSN = { v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : g1.vertices(doubleSubN)) {
			assertEquals(graphDSN[i], v);
			i++;
		}

		Vertex v13 = g1.createVertex(DoubleSubNode.class);
		Vertex v14 = g1.createVertex(SubNode.class);
		Vertex v15 = g1.createVertex(DoubleSubNode.class);
		Vertex v16 = g1.createVertex(SuperNode.class);
		Vertex v17 = g1.createVertex(SuperNode.class);
		Vertex v18 = g1.createVertex(SubNode.class);
		Vertex v19 = g1.createVertex(DoubleSubNode.class);
		Vertex v20 = g1.createVertex(SuperNode.class);

		Vertex[] graphSubN2 = { v1, v2, v3, v4, v9, v10, v11, v12, v13, v14,
				v15, v18, v19 };
		i = 0;
		for (Vertex v : g1.vertices(subN)) {
			assertEquals(graphSubN2[i], v);
			i++;
		}

		Vertex[] graphSuperN2 = { v5, v6, v7, v8, v9, v10, v11, v12, v13, v15,
				v16, v17, v19, v20 };
		i = 0;
		for (Vertex v : g1.vertices(superN)) {
			assertEquals(graphSuperN2[i], v);
			i++;
		}

		Vertex[] graphDSN2 = { v9, v10, v11, v12, v13, v15, v19 };
		i = 0;
		for (Vertex v : g1.vertices(doubleSubN)) {
			assertEquals(graphDSN2[i], v);
			i++;
		}

		System.out.println("Done testing vertices2.");

	}

	@Test
	public void testVertices3() {
		onlyTestWithoutTransactionSupport();
		assertEquals(false, g2.vertices(SubNode.class).iterator().hasNext());
		assertEquals(false, g2.vertices(SuperNode.class).iterator().hasNext());
		assertEquals(false, g2.vertices(DoubleSubNode.class).iterator()
				.hasNext());
		assertEquals(true, g1.vertices(SubNode.class).iterator().hasNext());
		assertEquals(true, g1.vertices(SuperNode.class).iterator().hasNext());
		assertEquals(true, g1.vertices(DoubleSubNode.class).iterator()
				.hasNext());

		Vertex[] graphSubN = { v1, v2, v3, v4, v9, v10, v11, v12 };
		int i = 0;
		for (Vertex v : g1.vertices(SubNode.class)) {
			assertEquals(graphSubN[i], v);
			i++;
		}

		Vertex[] graphSuperN = { v5, v6, v7, v8, v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : g1.vertices(SuperNode.class)) {
			assertEquals(graphSuperN[i], v);
			i++;
		}

		Vertex[] graphDoubleSubN = { v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : g1.vertices(DoubleSubNode.class)) {
			assertEquals(graphDoubleSubN[i], v);
			i++;
		}

		Vertex v13 = g1.createVertex(DoubleSubNode.class);
		Vertex v14 = g1.createVertex(DoubleSubNode.class);
		Vertex v15 = g1.createVertex(SuperNode.class);
		Vertex v16 = g1.createVertex(SubNode.class);
		Vertex v17 = g1.createVertex(SuperNode.class);
		Vertex v18 = g1.createVertex(DoubleSubNode.class);
		Vertex v19 = g1.createVertex(SubNode.class);
		Vertex v20 = g1.createVertex(SuperNode.class);

		Vertex[] graphSubN2 = { v1, v2, v3, v4, v9, v10, v11, v12, v13, v14,
				v16, v18, v19 };
		i = 0;
		for (Vertex v : g1.vertices(SubNode.class)) {
			assertEquals(graphSubN2[i], v);
			i++;
		}

		Vertex[] graphSuperN2 = { v5, v6, v7, v8, v9, v10, v11, v12, v13, v14,
				v15, v17, v18, v20 };
		i = 0;
		for (Vertex v : g1.vertices(SuperNode.class)) {
			assertEquals(graphSuperN2[i], v);
			i++;
		}

		Vertex[] graphDoubleSubN2 = { v9, v10, v11, v12, v13, v14, v18 };
		i = 0;
		for (Vertex v : g1.vertices(DoubleSubNode.class)) {
			assertEquals(graphDoubleSubN2[i], v);
			i++;
		}

		System.out.println("Done testing vertices3.");
	}

	@Test
	public void testDefragment() {
		onlyTestWithoutTransactionSupport();
		/*
		 * Testen der defragment()-Methode: Ein Vorher-Nachher Abbild von
		 * Vertex- Referenzen sammeln und vergleichen, genauso mit Kantenseq.
		 * Inzidenzen sind nicht betroffen (von defragment() zumindest das, was
		 * einfach zu testen ist); Dafür bedarf es einen Graph, indem gelöscht
		 * wurde und dadurch Lücken entstanden sind, sodass defragment() zum
		 * Einsatz kommen kann
		 */
	}

	public static class GraphTestKlasse extends GraphImpl {

		private String done;

		public GraphTestKlasse(GraphClass gC) {
			super("blubb", gC);
			done = "nothing";
		}

		@Override
		public Object getAttribute(String name) throws NoSuchFieldException {
			return null;
		}

		@Override
		public AttributedElementClass getAttributedElementClass() {
			return null;
		}

		@Override
		public Class<? extends AttributedElement> getM1Class() {
			return null;
		}

		@Override
		public void readAttributeValues(GraphIO io) throws GraphIOException {
		}

		@Override
		public void setAttribute(String name, Object data)
				throws NoSuchFieldException {
		}

		@Override
		public void writeAttributeValues(GraphIO io) throws IOException,
				GraphIOException {
		}

		public String getDone() {
			return done;
		}

		@Override
		public void loadingCompleted() {
			done = "loadingCompleted";
		}

		@Override
		public void vertexDeleted(Vertex v) {
			done = "vertexDeleted" + v.toString();
		}

		@Override
		public void vertexAdded(Vertex v) {
			done = "vertexAdded" + v.toString();
		}

		@Override
		public void edgeDeleted(Edge e) {
			done = "edgeDeleted" + e.toString();
		}

		@Override
		public void edgeAdded(Edge e) {
			done = "edgeAdded" + e.toString();
		}

		@Override
		public void readAttributeValueFromString(String attributeName,
				String value) throws GraphIOException, NoSuchFieldException {
			// TODO Auto-generated method stub

		}

		@Override
		public String writeAttributeValueToString(String attributeName)
				throws IOException, GraphIOException, NoSuchFieldException {
			// TODO Auto-generated method stub
			return null;
		}

	}

}