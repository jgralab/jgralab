package de.uni_koblenz.jgralabtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.std.GraphImpl;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
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

public class GraphTest {
	private VertexTestGraph graph;
	private VertexTestGraph graph2;
	private Vertex v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12;
	private VertexClass subN = null, superN = null, doubleSubN = null;
	private EdgeClass link = null, subL = null, lBack = null;

	@Before
	public void setUp() {

		graph = VertexTestSchema.instance().createVertexTestGraph();
		graph2 = VertexTestSchema.instance().createVertexTestGraph();
		System.out.println("Graph2 is instance of class " + graph2.getClass());
		v1 = graph.createVertex(SubNode.class);
		System.out.println("V1 is instance of class " + v1.getClass());
		v2 = graph.createVertex(SubNode.class);
		v3 = graph.createVertex(SubNode.class);
		v4 = graph.createVertex(SubNode.class);
		v5 = graph.createVertex(SuperNode.class);
		v6 = graph.createVertex(SuperNode.class);
		v7 = graph.createVertex(SuperNode.class);
		v8 = graph.createVertex(SuperNode.class);
		v9 = graph.createVertex(DoubleSubNode.class);
		v10 = graph.createVertex(DoubleSubNode.class);
		v11 = graph.createVertex(DoubleSubNode.class);
		v12 = graph.createVertex(DoubleSubNode.class);
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
		List<VertexClass> vclasses = graph.getSchema()
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
		List<EdgeClass> eclasses = graph.getSchema()
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
	public void testCreateVertex() {
		Vertex v13 = graph.createVertex(SubNode.class);
		Vertex v14 = graph2.createVertex(SubNode.class);
		Vertex v15 = graph.createVertex(SuperNode.class);
		Vertex v16 = graph2.createVertex(SuperNode.class);
		Vertex v17 = graph.createVertex(DoubleSubNode.class);
		Vertex v18 = graph2.createVertex(DoubleSubNode.class);
		Vertex[] graphVertices = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12, v13, v15, v17 };
		Vertex[] graph2Vertices = { v14, v16, v18 };

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
		for (Vertex v : graph.vertices()) {
			assertEquals(graphVertices[i], v);
			i++;
		}
		i = 0;
		for (Vertex v : graph2.vertices()) {
			assertEquals(graph2Vertices[i], v);
			i++;
		}
	}

	@Test
	public void testCreateEdge() {
		Edge e1 = graph.createEdge(SubLink.class, v9, v5);
		Edge e2 = graph.createEdge(SubLink.class, v10, v6);
		Edge e3 = graph.createEdge(SubLink.class, v12, v8);
		Edge e4 = graph.createEdge(Link.class, v1, v5);
		Edge e5 = graph.createEdge(Link.class, v2, v6);
		Edge e6 = graph.createEdge(Link.class, v9, v6);
		Edge e7 = graph.createEdge(Link.class, v10, v5);
		Edge e8 = graph.createEdge(Link.class, v11, v6);
		Edge e9 = graph.createEdge(LinkBack.class, v5, v1);
		Edge e10 = graph.createEdge(LinkBack.class, v6, v2);
		Edge e11 = graph.createEdge(LinkBack.class, v5, v9);
		Edge e12 = graph.createEdge(LinkBack.class, v6, v10);
		Edge e13 = graph.createEdge(LinkBack.class, v5, v12);
		Edge e14 = graph.createEdge(LinkBack.class, v6, v10); // the same as e12

		// tests whether the edge is an instance of the expected class
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
		for (Edge e : graph.edges()) {
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
	}

	@Test
	public void testIsLoading() {
		// TODO how do I get isLoading to return true
		assertEquals(false, graph.isLoading());
		assertEquals(false, graph2.isLoading());

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
	public void testLoadingCompleted() {
		// TODO
		GraphTestKlasse gTest = new GraphTestKlasse(graph.getGraphClass());
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
		long l1 = graph.getGraphVersion();
		long l2 = graph2.getGraphVersion();

		assertEquals(false, graph.isGraphModified(l1));
		assertEquals(false, graph2.isGraphModified(l2));

		graph.createEdge(SubLink.class, v9, v5);
		graph2.createSubNode();

		assertEquals(true, graph.isGraphModified(l1));
		assertEquals(true, graph2.isGraphModified(l2));
		l1 = graph.getGraphVersion();
		l2 = graph2.getGraphVersion();

		Edge e1 = graph.createEdge(Link.class, v1, v6);
		graph2.createSuperNode();
		assertEquals(true, graph.isGraphModified(l1));
		assertEquals(true, graph2.isGraphModified(l2));
		l1 = graph.getGraphVersion();
		l2 = graph2.getGraphVersion();

		graph.createEdge(LinkBack.class, v7, v10);
		graph2.createDoubleSubNode();
		assertEquals(true, graph.isGraphModified(l1));
		assertEquals(true, graph2.isGraphModified(l2));
		l1 = graph.getGraphVersion();
		l2 = graph2.getGraphVersion();

		Edge e2 = graph.createEdge(SubLink.class, v9, v5);
		assertEquals(true, graph.isGraphModified(l1));
		assertEquals(false, graph2.isGraphModified(l2));
		l1 = graph.getGraphVersion();

		graph.deleteEdge(e1);
		assertEquals(true, graph.isGraphModified(l1));
		assertEquals(false, graph2.isGraphModified(l2));
		l1 = graph.getGraphVersion();

		graph.deleteVertex(v3);
		assertEquals(true, graph.isGraphModified(l1));
		assertEquals(false, graph2.isGraphModified(l2));
		l1 = graph.getGraphVersion();

		graph.deleteEdge(e2);
		assertEquals(true, graph.isGraphModified(l1));
		assertEquals(false, graph2.isGraphModified(l2));
		l1 = graph.getGraphVersion();

		graph.deleteVertex(v9);
		assertEquals(true, graph.isGraphModified(l1));
		assertEquals(false, graph2.isGraphModified(l2));

		System.out.println("Done testing isGraphModified.");
	}

	@Test
	public void testGetGraphVersion() {
		assertEquals(0, graph2.getGraphVersion());

		graph2.createDoubleSubNode();
		assertEquals(1, graph2.getGraphVersion());

		graph2.createDoubleSubNode();
		assertEquals(2, graph2.getGraphVersion());

		DoubleSubNode v1 = graph2.createDoubleSubNode();
		graph2.createDoubleSubNode();
		assertEquals(4, graph2.getGraphVersion());

		for (int i = 0; i < 20; i++) {
			graph2.createSubNode();
			assertEquals(i + 5, graph2.getGraphVersion());
		}
		assertEquals(24, graph2.getGraphVersion());

		graph2.deleteVertex(v1);
		assertEquals(25, graph2.getGraphVersion());

		System.out.println("Done testing getGraphVersion.");
	}

	@Test
	public void testIsVertexListModified() {
		// border cases
		long l1 = graph.getVertexListVersion();
		long l2 = graph2.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));
		assertFalse(graph2.isVertexListModified(l2));

		Vertex v1 = graph.createVertex(DoubleSubNode.class);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));

		Vertex v2 = graph.createVertex(SuperNode.class);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));

		// makes sure that changing edges does not affect the vertexList
		graph.createEdge(SubLink.class, v1, v2);
		assertFalse(graph.isVertexListModified(l1));
		graph.createEdge(Link.class, v1, v2);
		assertFalse(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));

		graph.deleteVertex(v2);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));

		// normal cases
		for (int i = 0; i < 21; i++) {
			graph.createVertex(SubNode.class);
			assertTrue(graph.isVertexListModified(l1));
			l1 = graph.getVertexListVersion();
			assertFalse(graph.isVertexListModified(l1));
		}

		graph.deleteVertex(v1);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));

		for (int i = 0; i < 12; i++) {
			graph.createVertex(SuperNode.class);
			assertTrue(graph.isVertexListModified(l1));
			l1 = graph.getVertexListVersion();
			assertFalse(graph.isVertexListModified(l1));
		}
		l1 = graph.getVertexListVersion();
		Vertex v3 = graph.createVertex(SubNode.class);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));

		Vertex v4 = graph.createVertex(SuperNode.class);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));

		// if the order of the vertices is changed the vertexList is modified
		v3.putAfter(v4);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();

		v3.putAfter(v4);// v3 is already after v4
		assertFalse(graph.isVertexListModified(l1));

		v3.putBefore(v4);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));

		Vertex v5 = graph.createVertex(DoubleSubNode.class);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));

		v5.putBefore(v3);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));

		v4.putAfter(v5);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));

		// if attributes of vertices are changed this does not affect the
		// vertexList
		try {
			v4.setAttribute("number", 5);
			assertFalse(graph.isVertexListModified(l1));

			v4.setAttribute("number", 42);
			assertFalse(graph.isVertexListModified(l1));
		} catch (NoSuchFieldException e) {
			// :(
		}
		System.out.println("Done testing isVertexListModified.");
	}

	@Test
	public void testGetVertexListVersion() {
		// border cases
		assertEquals(0, graph2.getVertexListVersion());
		Vertex v13 = graph2.createVertex(SuperNode.class);
		assertEquals(1, graph2.getVertexListVersion());

		// normal cases
		assertEquals(12, graph.getVertexListVersion());
		graph.createVertex(SubNode.class);
		assertEquals(13, graph.getVertexListVersion());
		graph2.createVertex(SuperNode.class);
		assertEquals(2, graph2.getVertexListVersion());
		graph2.createVertex(DoubleSubNode.class);
		assertEquals(3, graph2.getVertexListVersion());
		for (int i = 4; i < 100; i++) {
			graph2.createVertex(SuperNode.class);
			assertEquals(i, graph2.getVertexListVersion());
		}
		graph2.createVertex(DoubleSubNode.class);
		assertEquals(100, graph2.getVertexListVersion());

		// tests whether the version changes correctly if vertices are deleted
		graph2.deleteVertex(v13);
		assertEquals(101, graph2.getVertexListVersion());
		for (int i = 14; i < 31; i += 3) {
			graph.createVertex(DoubleSubNode.class);
			assertEquals(i, graph.getVertexListVersion());
			graph.createVertex(SubNode.class);
			assertEquals(i + 1, graph.getVertexListVersion());
			graph.createVertex(SuperNode.class);
			assertEquals(i + 2, graph.getVertexListVersion());
		}
		Vertex v14 = graph.createVertex(SuperNode.class);
		assertEquals(32, graph.getVertexListVersion());
		Vertex v15 = graph.createVertex(DoubleSubNode.class);
		assertEquals(33, graph.getVertexListVersion());
		graph.deleteVertex(v15);
		assertEquals(34, graph.getVertexListVersion());
		graph.deleteVertex(v14);
		assertEquals(35, graph.getVertexListVersion());

		// makes sure that editing edges does not change the vertexList
		graph.createEdge(SubLink.class, v15, v14);
		assertEquals(35, graph.getVertexListVersion());
		graph.createEdge(LinkBack.class, v14, v15);
		assertEquals(35, graph.getVertexListVersion());

		// reordering the vertices does change the vertexListVersion
		v3.putAfter(v7);
		assertEquals(36, graph.getVertexListVersion());

		v5.putBefore(v2);
		assertEquals(37, graph.getVertexListVersion());

		v5.putAfter(v3);
		assertEquals(38, graph.getVertexListVersion());

		v7.putBefore(v2);
		assertEquals(39, graph.getVertexListVersion());

		v7.putBefore(v2);// v7 is already before v2
		assertEquals(39, graph.getVertexListVersion());

		// changing attributes of vertices does not change the vertexListVersion
		try {
			v5.setAttribute("number", 17);
			assertEquals(39, graph.getVertexListVersion());

			v8.setAttribute("number", 42);
			assertEquals(39, graph.getVertexListVersion());

			v7.setAttribute("number", 2);
			assertEquals(39, graph.getVertexListVersion());

			v5.setAttribute("number", 15);
			assertEquals(39, graph.getVertexListVersion());
		} catch (NoSuchFieldException e) {
			// :(
			e.printStackTrace();
		}

		System.out.println("Done testing getVertexListVersion.");
	}

	@Test
	public void testIsEdgeListModified() {
		// preparations...
		Vertex v13 = graph2.createVertex(SubNode.class);
		Vertex v14 = graph2.createVertex(SubNode.class);
		Vertex v15 = graph2.createVertex(SubNode.class);
		Vertex v16 = graph2.createVertex(SubNode.class);
		Vertex v17 = graph2.createVertex(SuperNode.class);
		Vertex v18 = graph2.createVertex(SuperNode.class);
		Vertex v19 = graph2.createVertex(SuperNode.class);
		Vertex v20 = graph2.createVertex(SuperNode.class);
		Vertex v21 = graph2.createVertex(DoubleSubNode.class);
		Vertex v22 = graph2.createVertex(DoubleSubNode.class);
		Vertex v23 = graph2.createVertex(DoubleSubNode.class);
		Vertex v24 = graph2.createVertex(DoubleSubNode.class);

		// border cases
		long l1 = graph.getEdgeListVersion();
		long l2 = graph2.getEdgeListVersion();
		assertFalse(graph.isEdgeListModified(l1));
		assertFalse(graph2.isEdgeListModified(l2));

		graph.createEdge(SubLink.class, v11, v7);
		Edge e1 = graph2.createEdge(Link.class, v15, v19);
		assertTrue(graph.isEdgeListModified(l1));
		assertTrue(graph2.isEdgeListModified(l2));
		l1 = graph.getEdgeListVersion();
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph.isEdgeListModified(l1));
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.deleteEdge(e1);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		// normal cases
		int ecount = graph2.getECount();
		graph2.createEdge(LinkBack.class, v19, v15);
		assertTrue(graph2.isEdgeListModified(l2));
		assertEquals(ecount + 1, graph2.getECount());
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.createEdge(Link.class, v15, v19);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		Edge e2 = graph2.createEdge(SubLink.class, v23, v19);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.createEdge(Link.class, v16, v20);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		Edge e3 = graph2.createEdge(Link.class, v23, v20);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.createEdge(Link.class, v24, v19);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.createEdge(LinkBack.class, v20, v16);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		Edge e4 = graph2.createEdge(SubLink.class, v24, v20);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.deleteEdge(e2);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.createEdge(LinkBack.class, v19, v23);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.createEdge(LinkBack.class, v20, v24);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.deleteEdge(e4);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.deleteEdge(e3);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		Edge e5 = graph2.createEdge(SubLink.class, v21, v17);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		Edge e6 = graph2.createEdge(Link.class, v13, v18);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		Edge e7 = graph2.createEdge(LinkBack.class, v17, v14);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.createEdge(Link.class, v22, v18);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		// adding vertices does not affect the edgeList
		Vertex v25 = graph2.createVertex(DoubleSubNode.class);
		assertFalse(graph2.isEdgeListModified(l2));

		Vertex v26 = graph2.createVertex(SuperNode.class);
		assertFalse(graph2.isEdgeListModified(l2));

		graph2.deleteVertex(v20);
		assertTrue(graph2.isEdgeListModified(l2));

		// reordering edges does change the edgeList
		e6.putBeforeInGraph(e5);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		e5.putAfterInGraph(e6);
		assertFalse(graph2.isEdgeListModified(l2));

		e5.putAfterInGraph(e7);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		// changing the attributes of an edge does not change the edgeList
		Edge e8 = graph2.createEdge(SubLink.class, v25, v26);
		assertTrue(graph2.isEdgeListModified(l2));
		l2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(l2));

		try {
			e8.setAttribute("anInt", 2);
			assertFalse(graph2.isEdgeListModified(l2));

			e8.setAttribute("anInt", -41);
			assertFalse(graph2.isEdgeListModified(l2));

			e8.setAttribute("anInt", 1024);
			assertFalse(graph2.isEdgeListModified(l2));

			e8.setAttribute("anInt", 15);
			assertFalse(graph2.isEdgeListModified(l2));
		} catch (NoSuchFieldException e) {
			// :(
			e.printStackTrace();
		}

		System.out.println("Done testing isEdgeListModified.");
	}

	@Test
	public void testGetEdgeListVersion() {
		// preparations...
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SubNode.class);
		Vertex v3 = graph.createVertex(SubNode.class);
		Vertex v4 = graph.createVertex(SubNode.class);
		Vertex v5 = graph.createVertex(SuperNode.class);
		Vertex v6 = graph.createVertex(SuperNode.class);
		Vertex v7 = graph.createVertex(SuperNode.class);
		Vertex v8 = graph.createVertex(SuperNode.class);
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		Vertex v10 = graph.createVertex(DoubleSubNode.class);
		Vertex v11 = graph.createVertex(DoubleSubNode.class);
		Vertex v12 = graph.createVertex(DoubleSubNode.class);

		// border cases
		assertEquals(0, graph.getEdgeListVersion());
		assertEquals(0, graph2.getEdgeListVersion());

		Edge e1 = graph.createEdge(SubLink.class, v9, v7);
		assertEquals(1, graph.getEdgeListVersion());

		graph.deleteEdge(e1);
		assertEquals(2, graph.getEdgeListVersion());

		// normal cases
		graph.createEdge(SubLink.class, v10, v5);
		assertEquals(3, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v10, v6);
		assertEquals(4, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v10, v7);
		assertEquals(5, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v10, v8);
		assertEquals(6, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v11, v5);
		assertEquals(7, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v11, v6);
		assertEquals(8, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v11, v7);
		assertEquals(9, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v11, v8);
		assertEquals(10, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v12, v5);
		assertEquals(11, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v12, v6);
		assertEquals(12, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v12, v7);
		assertEquals(13, graph.getEdgeListVersion());

		Edge e3 = graph.createEdge(SubLink.class, v12, v8);
		assertEquals(14, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v9, v6);
		assertEquals(15, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v9, v7);
		assertEquals(16, graph.getEdgeListVersion());

		graph.createEdge(SubLink.class, v9, v8);
		assertEquals(17, graph.getEdgeListVersion());

		graph.deleteEdge(e3);
		assertEquals(18, graph.getEdgeListVersion());

		// making sure that changing a vertex does not affect the edges
		graph.deleteVertex(v9);
		// TODO: Update this, the vertex has edges and thus the edge list
		// version
		// changes if the vertex is deleted
		assertEquals(18, graph.getEdgeListVersion());

		graph.createEdge(Link.class, v1, v5);
		assertEquals(19, graph.getEdgeListVersion());

		graph.createEdge(Link.class, v2, v5);
		assertEquals(20, graph.getEdgeListVersion());

		graph.createEdge(Link.class, v3, v5);
		assertEquals(21, graph.getEdgeListVersion());

		graph.createEdge(Link.class, v4, v5);
		assertEquals(22, graph.getEdgeListVersion());

		// TODO how can this work if I have already deleted v9?
		// isValis() even returns true
		graph.createEdge(Link.class, v9, v5);
		assertEquals(23, graph.getEdgeListVersion());

		graph.createEdge(Link.class, v10, v5);
		assertEquals(24, graph.getEdgeListVersion());

		graph.createEdge(Link.class, v11, v5);
		assertEquals(25, graph.getEdgeListVersion());

		graph.createEdge(Link.class, v12, v5);
		assertEquals(26, graph.getEdgeListVersion());

		graph.createEdge(Link.class, v1, v6);
		assertEquals(27, graph.getEdgeListVersion());

		graph.createEdge(Link.class, v1, v7);
		assertEquals(28, graph.getEdgeListVersion());

		graph.createEdge(Link.class, v1, v8);
		assertEquals(29, graph.getEdgeListVersion());

		Edge e4 = graph.createEdge(Link.class, v3, v7);
		assertEquals(30, graph.getEdgeListVersion());

		graph.createEdge(Link.class, v11, v8);
		assertEquals(31, graph.getEdgeListVersion());

		graph.createEdge(LinkBack.class, v5, v1);
		assertEquals(32, graph.getEdgeListVersion());

		graph.createEdge(LinkBack.class, v6, v2);
		assertEquals(33, graph.getEdgeListVersion());

		Edge e5 = graph.createEdge(LinkBack.class, v7, v3);
		assertEquals(34, graph.getEdgeListVersion());

		graph.createEdge(LinkBack.class, v8, v4);
		assertEquals(35, graph.getEdgeListVersion());

		graph.createEdge(LinkBack.class, v8, v9);
		assertEquals(36, graph.getEdgeListVersion());

		graph.createEdge(LinkBack.class, v7, v10);
		assertEquals(37, graph.getEdgeListVersion());

		graph.createEdge(LinkBack.class, v6, v11);
		assertEquals(38, graph.getEdgeListVersion());

		Edge e6 = graph.createEdge(LinkBack.class, v5, v12);
		assertEquals(39, graph.getEdgeListVersion());

		graph.deleteEdge(e4);
		assertEquals(40, graph.getEdgeListVersion());

		graph.deleteEdge(e5);
		assertEquals(41, graph.getEdgeListVersion());

		graph.deleteEdge(e6);
		assertEquals(42, graph.getEdgeListVersion());

		// reordering edges does change the edgeListVersion
		Edge e7 = graph.createEdge(SubLink.class, v9, v5);
		assertEquals(43, graph.getEdgeListVersion());

		Edge e8 = graph.createEdge(SubLink.class, v12, v7);
		assertEquals(44, graph.getEdgeListVersion());

		Edge e9 = graph.createEdge(SubLink.class, v11, v6);
		assertEquals(45, graph.getEdgeListVersion());

		e7.putBeforeInGraph(e9);
		assertEquals(46, graph.getEdgeListVersion());

		e8.putBeforeInGraph(e7);
		assertEquals(46, graph.getEdgeListVersion());

		e9.putAfterInGraph(e8);
		assertEquals(47, graph.getEdgeListVersion());

		e8.putAfterInGraph(e7);
		assertEquals(48, graph.getEdgeListVersion());

		// changing attributes does not change the edgeListVersion
		try {
			e7.setAttribute("anInt", 22);
			assertEquals(48, graph.getEdgeListVersion());

			e8.setAttribute("anInt", 203);
			assertEquals(48, graph.getEdgeListVersion());

			e9.setAttribute("anInt", 2209);
			assertEquals(48, graph.getEdgeListVersion());

			e7.setAttribute("anInt", 15);
			assertEquals(48, graph.getEdgeListVersion());
		} catch (NoSuchFieldException e) {
			// :(
			e.printStackTrace();
		}

		System.out.println("Done testing getEdgeListVersion.");
	}

	@Test
	public void testContainsVertex() {
		DoubleSubNode v13 = graph.createDoubleSubNode();
		DoubleSubNode v14 = graph2.createDoubleSubNode();
		SubNode v15 = graph.createSubNode();
		SubNode v16 = graph2.createSubNode();
		SuperNode v17 = graph.createSuperNode();
		SuperNode v18 = graph2.createSuperNode();
		Vertex v19 = graph2.createVertex(DoubleSubNode.class);
		Vertex v20 = graph2.createVertex(SubNode.class);
		Vertex v21 = graph2.createVertex(SuperNode.class);

		assertTrue(graph.containsVertex(v13));
		assertTrue(graph.containsVertex(v15));
		assertTrue(graph.containsVertex(v17));
		assertTrue(graph2.containsVertex(v14));
		assertTrue(graph2.containsVertex(v16));
		assertTrue(graph2.containsVertex(v18));
		assertTrue(graph2.containsVertex(v19));
		assertTrue(graph2.containsVertex(v20));
		assertTrue(graph2.containsVertex(v21));
		assertTrue(graph.containsVertex(v1));
		assertTrue(graph.containsVertex(v2));
		assertTrue(graph.containsVertex(v3));
		assertTrue(graph.containsVertex(v4));
		assertTrue(graph.containsVertex(v5));
		assertTrue(graph.containsVertex(v6));
		assertTrue(graph.containsVertex(v7));
		assertTrue(graph.containsVertex(v8));
		assertTrue(graph.containsVertex(v9));
		assertTrue(graph.containsVertex(v10));
		assertTrue(graph.containsVertex(v11));
		assertTrue(graph.containsVertex(v12));

		assertFalse(graph.containsVertex(v14));
		assertFalse(graph.containsVertex(v16));
		assertFalse(graph.containsVertex(v18));
		assertFalse(graph2.containsVertex(v13));
		assertFalse(graph2.containsVertex(v15));
		assertFalse(graph2.containsVertex(v17));
		assertFalse(graph2.containsVertex(v10));
		assertFalse(graph2.containsVertex(v1));
		assertFalse(graph2.containsVertex(v4));
		assertFalse(graph.containsVertex(v19));
		assertFalse(graph.containsVertex(v20));
		assertFalse(graph.containsVertex(v21));
		assertFalse(graph2.containsVertex(v1));
		assertFalse(graph2.containsVertex(v2));
		assertFalse(graph2.containsVertex(v3));
		assertFalse(graph2.containsVertex(v4));
		assertFalse(graph2.containsVertex(v5));
		assertFalse(graph2.containsVertex(v6));
		assertFalse(graph2.containsVertex(v7));
		assertFalse(graph2.containsVertex(v8));
		assertFalse(graph2.containsVertex(v9));
		assertFalse(graph2.containsVertex(v10));
		assertFalse(graph2.containsVertex(v11));
		assertFalse(graph2.containsVertex(v12));

		// deleting vertices changes the contains-information accordingly
		graph.deleteVertex(v1);
		assertFalse(graph.containsVertex(v1));

		graph.deleteVertex(v9);
		assertFalse(graph.containsVertex(v9));

		graph2.deleteVertex(v14);
		assertFalse(graph2.containsVertex(v14));

		System.out.println("Done testing containsVertex.");
	}

	@Test
	public void testContainsEdge() {
		DoubleSubNode v13 = graph2.createDoubleSubNode();
		DoubleSubNode v14 = graph2.createDoubleSubNode();
		SubNode v15 = graph2.createSubNode();
		SubNode v16 = graph2.createSubNode();
		SuperNode v17 = graph2.createSuperNode();
		SuperNode v18 = graph2.createSuperNode();

		Edge e1 = graph.createEdge(SubLink.class, v9, v7);
		SubLink e2 = graph2.createSubLink(v13, v17);
		Edge e3 = graph.createEdge(Link.class, v10, v5);
		Link e4 = graph2.createLink(v15, v17);
		Edge e5 = graph.createEdge(LinkBack.class, v7, v1);
		LinkBack e6 = graph2.createLinkBack(v17, v13);
		Edge e7 = graph.createEdge(SubLink.class, v10, v5);
		Edge e8 = graph.createEdge(Link.class, v3, v7);
		Edge e9 = graph.createEdge(LinkBack.class, v5, v9);
		Edge e10 = graph.createEdge(SubLink.class, v9, v5);
		Edge e11 = graph2.createEdge(SubLink.class, v14, v17);
		Edge e12 = graph2.createEdge(Link.class, v16, v18);
		Edge e13 = graph2.createEdge(LinkBack.class, v18, v13);

		assertTrue(graph.containsEdge(e1));
		assertTrue(graph2.containsEdge(e2));
		assertTrue(graph.containsEdge(e3));
		assertTrue(graph2.containsEdge(e4));
		assertTrue(graph.containsEdge(e5));
		assertTrue(graph2.containsEdge(e6));
		assertTrue(graph.containsEdge(e7));
		assertTrue(graph.containsEdge(e8));
		assertTrue(graph.containsEdge(e9));
		assertTrue(graph.containsEdge(e10));
		assertTrue(graph2.containsEdge(e11));
		assertTrue(graph2.containsEdge(e12));
		assertTrue(graph2.containsEdge(e13));

		assertFalse(graph.containsEdge(null));
		assertFalse(graph2.containsEdge(null));
		assertFalse(graph2.containsEdge(e1));
		assertFalse(graph.containsEdge(e2));
		assertFalse(graph2.containsEdge(e3));
		assertFalse(graph.containsEdge(e4));
		assertFalse(graph2.containsEdge(e5));
		assertFalse(graph.containsEdge(e6));
		assertFalse(graph2.containsEdge(e7));
		assertFalse(graph2.containsEdge(e8));
		assertFalse(graph2.containsEdge(e9));
		assertFalse(graph2.containsEdge(e10));
		assertFalse(graph.containsEdge(e11));
		assertFalse(graph.containsEdge(e12));
		assertFalse(graph.containsEdge(e13));

		// when a vertex is deleted, the edges to which it belonged are deleted
		// as well
		e1 = graph.createEdge(SubLink.class, v10, v12);
		Edge e14 = graph.createEdge(SubLink.class, v9, v6);
		Edge e17 = graph.createEdge(LinkBack.class, v8, v10);
		assertTrue(graph.containsEdge(e1));
		assertTrue(graph.containsEdge(e17));

		graph.deleteVertex(v10);
		// all edges from or to v10 do no longer exist
		assertFalse(graph.containsEdge(e1));
		assertFalse(graph.containsEdge(e3));
		assertFalse(graph.containsEdge(e7));
		assertFalse(graph.containsEdge(e17));
		// all other edges do still exist
		assertTrue(graph.containsEdge(e5));
		assertTrue(graph.containsEdge(e8));
		assertTrue(graph.containsEdge(e14));

		Edge e15 = graph.createEdge(LinkBack.class, v6, v11);
		Edge e16 = graph.createEdge(Link.class, v12, v8);
		assertTrue(graph.containsEdge(e15));
		assertTrue(graph.containsEdge(e16));

		graph.deleteEdge(e5);
		assertFalse(graph.containsEdge(e5));
		assertTrue(graph.containsEdge(e8));
		assertTrue(graph.containsEdge(e14));
		assertTrue(graph.containsEdge(e15));
		assertTrue(graph.containsEdge(e16));

		graph.deleteEdge(e16);
		assertFalse(graph.containsEdge(e16));
		assertTrue(graph.containsEdge(e8));
		assertTrue(graph.containsEdge(e14));
		assertTrue(graph.containsEdge(e15));

		graph.deleteEdge(e14);
		assertFalse(graph.containsEdge(e14));
		assertTrue(graph.containsEdge(e8));
		assertTrue(graph.containsEdge(e15));

		graph.deleteEdge(e8);
		assertFalse(graph.containsEdge(e8));
		assertTrue(graph.containsEdge(e15));

		graph.deleteEdge(e15);
		assertFalse(graph.containsEdge(e15));

		System.out.println("Done testing containsEdge.");
	}

	@Test
	public void testDeleteVertex() {
		// TODO:
		// Removes the vertex from the vertex sequence of this graph.
		// any edges incident to the vertex are deleted
		// If the vertex is the parent of a composition, all child vertices are
		// deleted.
		// Pre: v.isValid()
		// Post: !v.isValid() && !containsVertex(v) && getVertex(v.getId()) ==
		// null

		graph2.createSubNode();
		DoubleSubNode v15 = graph2.createDoubleSubNode();

		graph.deleteVertex(v1);
		assertFalse(graph.containsVertex(v1));
		graph.deleteVertex(v2);
		assertFalse(graph.containsVertex(v2));
		graph.deleteVertex(v3);
		assertFalse(graph.containsVertex(v3));
		graph.deleteVertex(v7);
		assertFalse(graph.containsVertex(v7));
		graph.deleteVertex(v5);
		assertFalse(graph.containsVertex(v5));
		graph.deleteVertex(v6);
		assertFalse(graph.containsVertex(v6));
		graph.deleteVertex(v9);
		assertFalse(graph.containsVertex(v9));
		graph.deleteVertex(v10);
		assertFalse(graph.containsVertex(v10));
		graph.deleteVertex(v11);
		assertFalse(graph.containsVertex(v11));

		// v13 only returns an AssertionError if a SubNode is created in graph2
		// before the creation of v13; v15 always returns a
		// NullPointerException;
		// if v14 is the first vertex of graph2 it returns a
		// NullPointerException as
		// well otherwise it returns an AssertionError
		// TODO what is going on and why?
		try {
			graph.deleteVertex(v15);
		} catch (NullPointerException e) {
			// Exception ok
		}
	}

	@Test
	public void testVertexDeleted() {
		GraphTestKlasse gTest = new GraphTestKlasse(graph.getGraphClass());
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

		gTest = new GraphTestKlasse(graph2.getGraphClass());
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
		GraphTestKlasse gTest = new GraphTestKlasse(graph.getGraphClass());
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

		gTest = new GraphTestKlasse(graph2.getGraphClass());
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
		/*
		 * TODO apparently one cannot delete ALL edges of the same type (here:
		 * SubLink) after deleting a vertex?
		 */
		// TODO faults => assertions
		Link e1 = graph.createEdge(Link.class, v1, v6);
		Link e2 = graph.createEdge(Link.class, v11, v5);
		Link e3 = graph.createEdge(Link.class, v2, v8);
		SubLink e4 = graph.createEdge(SubLink.class, v11, v6);
		SubLink e5 = graph.createEdge(SubLink.class, v12, v5);
		LinkBack e6 = graph.createEdge(LinkBack.class, v6, v11);
		LinkBack e7 = graph.createEdge(LinkBack.class, v5, v2);
		LinkBack e8 = graph.createEdge(LinkBack.class, v8, v1);
		LinkBack e9 = graph.createEdge(LinkBack.class, v5, v10);
		Link e10 = graph.createEdge(Link.class, v12, v7);
		SubLink e11 = graph.createEdge(SubLink.class, v10, v6);
		SubLink e12 = graph.createEdge(SubLink.class, v9, v7);
		// SubLink e10 = graph2.createEdge(SubLink.class, v9, v6);

		int id = e12.getId();
		graph.deleteEdge(e12);
		assertFalse(e12.isValid());
		assertFalse(graph.containsEdge(e12));
		assertEquals(null, graph.getEdge(id));

		id = e1.getId();
		graph.deleteEdge(e1);
		assertFalse(e1.isValid());
		assertFalse(graph.containsEdge(e1));
		assertEquals(null, graph.getEdge(id));

		id = e2.getId();
		graph.deleteEdge(e2);
		assertFalse(e2.isValid());
		assertFalse(graph.containsEdge(e2));
		assertEquals(null, graph.getEdge(id));

		id = e7.getId();
		graph.deleteEdge(e7);
		assertFalse(e7.isValid());
		assertFalse(graph.containsEdge(e7));
		assertEquals(null, graph.getEdge(id));

		id = e3.getId();
		graph.deleteEdge(e3);
		assertFalse(e3.isValid());
		assertFalse(graph.containsEdge(e3));
		assertEquals(null, graph.getEdge(id));

		id = e9.getId();
		graph.deleteEdge(e9);
		assertFalse(e9.isValid());
		assertFalse(graph.containsEdge(e9));
		assertEquals(null, graph.getEdge(id));

		id = e4.getId();
		graph.deleteEdge(e4);
		assertFalse(e4.isValid());
		assertFalse(graph.containsEdge(e4));
		assertEquals(null, graph.getEdge(id));

		id = e10.getId();
		graph.deleteEdge(e10);
		assertFalse(e10.isValid());
		assertFalse(graph.containsEdge(e10));
		assertEquals(null, graph.getEdge(id));

		id = e5.getId();
		graph.deleteEdge(e5);
		assertFalse(e5.isValid());
		assertFalse(graph.containsEdge(e5));
		assertEquals(null, graph.getEdge(id));

		id = e8.getId();
		graph.deleteEdge(e8);
		assertFalse(e8.isValid());
		assertFalse(graph.containsEdge(e8));
		assertEquals(null, graph.getEdge(id));

		id = e11.getId();
		graph.deleteEdge(e11);
		assertFalse(e11.isValid());
		assertFalse(graph.containsEdge(e11));
		assertEquals(null, graph.getEdge(id));

		id = e6.getId();
		graph.deleteEdge(e6);
		assertFalse(e6.isValid());
		assertFalse(graph.containsEdge(e6));
		assertEquals(null, graph.getEdge(id));

		// border cases

		// faults
		// TODO
		// cannot try to delete an edge which has never been created?
		// graph.deleteEdge(e10);

		System.out.println("Done testing deleteEdge.");
	}

	@Test
	public void testEdgeDeleted() {
		GraphTestKlasse gTest = new GraphTestKlasse(graph.getGraphClass());
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

		gTest = new GraphTestKlasse(graph2.getGraphClass());
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
		GraphTestKlasse gTest = new GraphTestKlasse(graph.getGraphClass());

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

		gTest = new GraphTestKlasse(graph2.getGraphClass());
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
		assertEquals(v1, graph.getFirstVertex());
		assertEquals(null, graph2.getFirstVertex());

		SubNode v13 = graph2.createSubNode();
		graph2.createSuperNode();

		assertEquals(v13, graph2.getFirstVertex());

		graph2.createDoubleSubNode();
		graph2.createSubNode();
		assertEquals(v13, graph2.getFirstVertex());

		graph.createDoubleSubNode();
		assertEquals(v1, graph.getFirstVertex());

		System.out.println("Done testing getFirstVertex.");
	}

	@Test
	public void testGetLastVertex() {
		// border cases
		assertEquals(v12, graph.getLastVertex());
		assertEquals(null, graph2.getLastVertex());

		Vertex v13 = graph.createVertex(SubNode.class);
		assertEquals(v13, graph.getLastVertex());

		// normal cases
		Vertex v14 = graph.createVertex(SubNode.class);
		assertEquals(v14, graph.getLastVertex());

		Vertex v15 = graph.createVertex(SubNode.class);
		assertEquals(v15, graph.getLastVertex());

		Vertex v16 = graph.createVertex(SubNode.class);
		assertEquals(v16, graph.getLastVertex());

		Vertex v17 = graph.createVertex(SuperNode.class);
		assertEquals(v17, graph.getLastVertex());

		Vertex v18 = graph.createVertex(SuperNode.class);
		assertEquals(v18, graph.getLastVertex());

		Vertex v19 = graph.createVertex(SuperNode.class);
		assertEquals(v19, graph.getLastVertex());

		Vertex v20 = graph.createVertex(SuperNode.class);
		assertEquals(v20, graph.getLastVertex());

		Vertex v21 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v21, graph.getLastVertex());

		Vertex v22 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v22, graph.getLastVertex());

		Vertex v23 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v23, graph.getLastVertex());

		Vertex v24 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v24, graph.getLastVertex());

		System.out.println("Done testing getLastVertex.");
	}

	@Test
	public void testGetFirstVertexOfClass() {
		assertEquals(null, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(null, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v13 = graph2.createVertex(SubNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(null, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v14 = graph2.createVertex(SuperNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v15 = graph2.createVertex(SubNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v16 = graph2.createVertex(SubNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v17 = graph2.createVertex(DoubleSubNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v18 = graph2.createVertex(SuperNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v19 = graph2.createVertex(SubNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v20 = graph2.createVertex(DoubleSubNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		graph2.deleteVertex(v14);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v21 = graph2.createVertex(DoubleSubNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		graph2.deleteVertex(v16);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		graph2.createVertex(SuperNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		graph2.createVertex(DoubleSubNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		graph2.deleteVertex(v13);
		assertEquals(v15, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v17, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		graph2.deleteVertex(v17);
		assertEquals(v15, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v18, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v20, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		graph2.deleteVertex(v20);
		assertEquals(v15, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v18, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v21, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		graph2.createVertex(SuperNode.class);
		assertEquals(v15, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v18, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v21, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		graph2.deleteVertex(v15);
		assertEquals(v19, graph2.getFirstVertexOfClass(SubNode.class));
		assertEquals(v18, graph2.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v21, graph2.getFirstVertexOfClass(DoubleSubNode.class));

		System.out.println("Done testing getFirstVertexOfClass.");
	}

	@Test
	public void testGetFirstVertexOfClass2() {
		assertEquals(null, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(null, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(null, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(null, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		Vertex v13 = graph2.createVertex(SubNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(null, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(null, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		Vertex v14 = graph2.createVertex(SuperNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		Vertex v15 = graph2.createVertex(DoubleSubNode.class);
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		graph2.deleteVertex(v13);
		assertEquals(null, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		Vertex v16 = graph2.createVertex(SubNode.class);
		assertEquals(v16, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		Vertex v17 = graph2.createVertex(SubNode.class);
		assertEquals(v16, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		graph2.deleteVertex(v16);
		assertEquals(v17, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		Vertex v18 = graph2.createVertex(DoubleSubNode.class);
		assertEquals(v17, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v15, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		graph2.deleteVertex(v15);
		assertEquals(v17, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v17, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v18, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v18, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		Vertex v19 = graph2.createVertex(SubNode.class);
		assertEquals(v17, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v17, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v18, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v18, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		graph2.deleteVertex(v17);
		assertEquals(v19, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v18, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v18, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		Vertex v20 = graph2.createVertex(DoubleSubNode.class);
		assertEquals(v19, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v18, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v18, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		graph2.deleteVertex(v18);
		assertEquals(v19, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v19, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v20, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v20, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		Vertex v21 = graph2.createVertex(SuperNode.class);
		assertEquals(v19, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v19, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v20, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v20, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		graph2.deleteVertex(v14);
		assertEquals(v19, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v19, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v20, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v20, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		graph2.deleteVertex(v19);
		assertEquals(null, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v20, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v20, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		Vertex v22 = graph2.createVertex(SubNode.class);
		assertEquals(v22, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v20, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v20, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		graph2.deleteVertex(v20);
		assertEquals(v22, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v22, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v21, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(null, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		Vertex v23 = graph2.createVertex(DoubleSubNode.class);
		assertEquals(v22, graph2.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v22, graph2.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v21, graph2.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v23, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				true));
		assertEquals(v23, graph2.getFirstVertexOfClass(DoubleSubNode.class,
				false));

		System.out.println("Done testing getFirstVertexOfClass2.");
	}

	@Test
	public void testGetFirstVertexOfClass3() {
		// preparations
		getVertexClasses();

		assertEquals(null, graph2.getFirstVertexOfClass(subN));
		assertEquals(null, graph2.getFirstVertexOfClass(superN));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN));

		SuperNode v13 = graph2.createSuperNode();
		assertEquals(null, graph2.getFirstVertexOfClass(subN));
		assertEquals(v13, graph2.getFirstVertexOfClass(superN));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN));

		SubNode v14 = graph2.createSubNode();
		assertEquals(v14, graph2.getFirstVertexOfClass(subN));
		assertEquals(v13, graph2.getFirstVertexOfClass(superN));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN));

		SuperNode v15 = graph2.createSuperNode();
		assertEquals(v14, graph2.getFirstVertexOfClass(subN));
		assertEquals(v13, graph2.getFirstVertexOfClass(superN));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN));

		DoubleSubNode v16 = graph2.createDoubleSubNode();
		assertEquals(v14, graph2.getFirstVertexOfClass(subN));
		assertEquals(v13, graph2.getFirstVertexOfClass(superN));
		assertEquals(v16, graph2.getFirstVertexOfClass(doubleSubN));

		graph2.deleteVertex(v13);
		assertEquals(v14, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(v16, graph2.getFirstVertexOfClass(doubleSubN));

		DoubleSubNode v17 = graph2.createDoubleSubNode();
		assertEquals(v14, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(v16, graph2.getFirstVertexOfClass(doubleSubN));

		SubNode v18 = graph2.createSubNode();
		assertEquals(v14, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(v16, graph2.getFirstVertexOfClass(doubleSubN));

		graph2.deleteVertex(v14);
		assertEquals(v16, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(v16, graph2.getFirstVertexOfClass(doubleSubN));

		graph2.deleteVertex(v16);
		assertEquals(v17, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(v17, graph2.getFirstVertexOfClass(doubleSubN));

		graph2.deleteVertex(v17);
		assertEquals(v18, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN));

		DoubleSubNode v19 = graph2.createDoubleSubNode();
		assertEquals(v18, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(v19, graph2.getFirstVertexOfClass(doubleSubN));

		graph2.deleteVertex(v18);
		assertEquals(v19, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(v19, graph2.getFirstVertexOfClass(doubleSubN));

		SubNode v20 = graph2.createSubNode();
		assertEquals(v19, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(v19, graph2.getFirstVertexOfClass(doubleSubN));

		SuperNode v21 = graph2.createSuperNode();
		assertEquals(v19, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(v19, graph2.getFirstVertexOfClass(doubleSubN));

		graph2.deleteVertex(v19);
		assertEquals(v20, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN));

		graph2.deleteVertex(v20);
		assertEquals(null, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN));

		SubNode v22 = graph2.createSubNode();
		assertEquals(v22, graph2.getFirstVertexOfClass(subN));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN));

		graph2.deleteVertex(v15);
		assertEquals(v22, graph2.getFirstVertexOfClass(subN));
		assertEquals(v21, graph2.getFirstVertexOfClass(superN));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN));

		System.out.println("Done testing getFirstVertexOfClass3.");
	}

	@Test
	public void testGetFirstVertexOfClass4() {
		// preparations
		getVertexClasses();

		// start testing
		assertEquals(null, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(null, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(null, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN, false));

		DoubleSubNode v13 = graph2.createDoubleSubNode();
		assertEquals(null, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v13, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(doubleSubN, false));

		SuperNode v14 = graph2.createSuperNode();
		assertEquals(null, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v13, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(doubleSubN, false));

		DoubleSubNode v15 = graph2.createDoubleSubNode();
		assertEquals(null, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v13, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(doubleSubN, false));

		SubNode v16 = graph2.createSubNode();
		assertEquals(v16, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v13, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v13, graph2.getFirstVertexOfClass(doubleSubN, false));

		graph2.deleteVertex(v13);
		assertEquals(v16, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v15, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(doubleSubN, false));

		graph2.deleteVertex(v16);
		assertEquals(null, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v15, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(doubleSubN, false));

		SubNode v17 = graph2.createSubNode();
		assertEquals(v17, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v15, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(doubleSubN, false));

		DoubleSubNode v18 = graph2.createDoubleSubNode();
		assertEquals(v17, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v14, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v14, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v15, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(doubleSubN, false));

		graph2.deleteVertex(v14);
		assertEquals(v17, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v15, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v15, graph2.getFirstVertexOfClass(doubleSubN, false));

		graph2.deleteVertex(v15);
		assertEquals(v17, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v17, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v18, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(doubleSubN, false));

		graph2.deleteVertex(v17);
		assertEquals(null, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v18, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(doubleSubN, false));

		SubNode v19 = graph2.createSubNode();
		assertEquals(v19, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v18, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(doubleSubN, false));

		DoubleSubNode v20 = graph2.createDoubleSubNode();
		assertEquals(v19, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v18, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(doubleSubN, false));

		SuperNode v21 = graph2.createSuperNode();
		assertEquals(v19, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v18, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v18, graph2.getFirstVertexOfClass(doubleSubN, false));

		graph2.deleteVertex(v18);
		assertEquals(v19, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v19, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v20, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(doubleSubN, false));

		graph2.deleteVertex(v19);
		assertEquals(null, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v20, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(doubleSubN, false));

		SubNode v22 = graph2.createSubNode();
		assertEquals(v22, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v20, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v20, graph2.getFirstVertexOfClass(doubleSubN, false));

		graph2.deleteVertex(v20);
		assertEquals(v22, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v22, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v21, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN, false));

		DoubleSubNode v23 = graph2.createDoubleSubNode();
		assertEquals(v22, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v22, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v21, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v23, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v23, graph2.getFirstVertexOfClass(doubleSubN, false));

		graph2.deleteVertex(v22);
		assertEquals(null, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v23, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(v21, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v21, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v23, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v23, graph2.getFirstVertexOfClass(doubleSubN, false));

		graph2.deleteVertex(v21);
		assertEquals(null, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(v23, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(v23, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(v23, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(v23, graph2.getFirstVertexOfClass(doubleSubN, false));

		graph2.deleteVertex(v23);
		assertEquals(null, graph2.getFirstVertexOfClass(subN, true));
		assertEquals(null, graph2.getFirstVertexOfClass(subN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(superN, true));
		assertEquals(null, graph2.getFirstVertexOfClass(superN, false));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN, true));
		assertEquals(null, graph2.getFirstVertexOfClass(doubleSubN, false));

		System.out.println("Done testing getFirstVertexOfClass4.");
	}

	@Test
	public void testGetFirstEdgeInGraph() {
		Vertex v13 = graph.createVertex(SuperNode.class);

		assertEquals(null, graph.getFirstEdgeInGraph());

		Edge e1 = graph.createEdge(Link.class, v3, v6);
		assertEquals(e1, graph.getFirstEdgeInGraph());

		Edge e2 = graph.createEdge(Link.class, v3, v13);
		assertEquals(e1, graph.getFirstEdgeInGraph());

		Edge e3 = graph.createEdge(LinkBack.class, v7, v11);
		assertEquals(e1, graph.getFirstEdgeInGraph());

		graph.deleteEdge(e1);
		assertEquals(e2, graph.getFirstEdgeInGraph());

		graph.deleteEdge(e2);
		assertEquals(e3, graph.getFirstEdgeInGraph());

		Edge e4 = graph.createEdge(SubLink.class, v10, v8);
		assertEquals(e3, graph.getFirstEdgeInGraph());

		graph.deleteEdge(e3);
		assertEquals(e4, graph.getFirstEdgeInGraph());

		Edge e5 = graph.createEdge(LinkBack.class, v8, v3);
		assertEquals(e4, graph.getFirstEdgeInGraph());

		Edge e6 = graph.createEdge(Link.class, v2, v5);
		assertEquals(e4, graph.getFirstEdgeInGraph());

		graph.deleteEdge(e4);
		assertEquals(e5, graph.getFirstEdgeInGraph());

		Edge e7 = graph.createEdge(LinkBack.class, v13, v1);
		assertEquals(e5, graph.getFirstEdgeInGraph());

		graph.deleteEdge(e5);
		assertEquals(e6, graph.getFirstEdgeInGraph());

		Edge e8 = graph.createEdge(SubLink.class, v9, v6);
		assertEquals(e6, graph.getFirstEdgeInGraph());

		graph.deleteEdge(e7);
		assertEquals(e6, graph.getFirstEdgeInGraph());

		graph.deleteEdge(e6);
		assertEquals(e8, graph.getFirstEdgeInGraph());

		graph.deleteEdge(e8);
		assertEquals(null, graph.getFirstEdgeInGraph());

		System.out.println("Done testing getFirstEdgeInGraph.");
	}

	@Test
	public void testGetLastEdgeInGraph() {
		Vertex v13 = graph.createVertex(SuperNode.class);

		assertEquals(null, graph.getLastEdgeInGraph());

		Edge e1 = graph.createEdge(Link.class, v3, v6);
		assertEquals(e1, graph.getLastEdgeInGraph());

		Edge e2 = graph.createEdge(Link.class, v3, v13);
		assertEquals(e2, graph.getLastEdgeInGraph());

		Edge e3 = graph.createEdge(LinkBack.class, v7, v11);
		assertEquals(e3, graph.getLastEdgeInGraph());

		graph.deleteEdge(e3);
		assertEquals(e2, graph.getLastEdgeInGraph());

		Edge e4 = graph.createEdge(SubLink.class, v10, v8);
		assertEquals(e4, graph.getLastEdgeInGraph());

		Edge e5 = graph.createEdge(LinkBack.class, v8, v3);
		assertEquals(e5, graph.getLastEdgeInGraph());

		Edge e6 = graph.createEdge(Link.class, v9, v5);
		assertEquals(e6, graph.getLastEdgeInGraph());

		Edge e7 = graph.createEdge(SubLink.class, v11, v7);
		assertEquals(e7, graph.getLastEdgeInGraph());

		graph.deleteEdge(e7);
		assertEquals(e6, graph.getLastEdgeInGraph());

		graph.deleteEdge(e6);
		assertEquals(e5, graph.getLastEdgeInGraph());

		Edge e8 = graph.createEdge(Link.class, v1, v13);
		assertEquals(e8, graph.getLastEdgeInGraph());

		graph.deleteEdge(e5);
		assertEquals(e8, graph.getLastEdgeInGraph());

		Edge e9 = graph.createEdge(LinkBack.class, v6, v2);
		assertEquals(e9, graph.getLastEdgeInGraph());

		graph.deleteEdge(e9);
		assertEquals(e8, graph.getLastEdgeInGraph());

		graph.deleteEdge(e8);
		assertEquals(e4, graph.getLastEdgeInGraph());

		graph.deleteEdge(e4);
		assertEquals(e2, graph.getLastEdgeInGraph());

		graph.deleteEdge(e2);
		assertEquals(e1, graph.getLastEdgeInGraph());

		graph.deleteEdge(e1);
		assertEquals(null, graph.getLastEdgeInGraph());

		System.out.println("Done testing getLastEdgeInGraph.");
	}

	@Test
	public void testGetFirstEdgeOfClassInGraph() {
		Vertex v13 = graph.createVertex(SuperNode.class);

		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e1 = graph.createEdge(Link.class, v3, v6);
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e2 = graph.createEdge(Link.class, v3, v13);
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e3 = graph.createEdge(LinkBack.class, v7, v11);
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		graph.deleteEdge(e1);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e4 = graph.createEdge(SubLink.class, v10, v8);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e5 = graph.createEdge(LinkBack.class, v8, v3);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		graph.deleteEdge(e3);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e6 = graph.createEdge(Link.class, v9, v5);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		graph.deleteEdge(e2);
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e7 = graph.createEdge(SubLink.class, v11, v7);
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		graph.deleteEdge(e4);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e8 = graph.createEdge(SubLink.class, v10, v13);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e9 = graph.createEdge(LinkBack.class, v6, v1);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		graph.deleteEdge(e5);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		graph.deleteEdge(e7);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		graph.deleteEdge(e6);
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		Edge e10 = graph.createEdge(Link.class, v2, v7);
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		graph.deleteEdge(e8);
		assertEquals(e10, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		graph.deleteEdge(e10);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		graph.deleteEdge(e9);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class));

		System.out.println("Done testing getFirstEdgeOfClassInGraph.");
	}

	@Test
	public void testGetFirstEdgeOfClassInGraph2() {
		Vertex v13 = graph.createVertex(SuperNode.class);

		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(null, graph
				.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class,
				false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				false));

		Edge e1 = graph.createEdge(SubLink.class, v9, v6);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				false));

		Edge e2 = graph.createEdge(Link.class, v3, v8);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				false));

		Edge e3 = graph.createEdge(LinkBack.class, v7, v12);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e3, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		graph.deleteEdge(e1);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(null, graph
				.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class,
				false));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e3, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e4 = graph.createEdge(SubLink.class, v10, v13);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e3, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		graph.deleteEdge(e2);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e3, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		graph.deleteEdge(e3);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				false));

		Edge e5 = graph.createEdge(LinkBack.class, v13, v3);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e6 = graph.createEdge(Link.class, v9, v5);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e7 = graph.createEdge(SubLink.class, v12, v7);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		graph.deleteEdge(e4);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		graph.deleteEdge(e6);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e8 = graph.createEdge(SubLink.class, v10, v8);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e5, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		graph.deleteEdge(e5);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				false));

		graph.deleteEdge(e7);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				false));

		Edge e9 = graph.createEdge(LinkBack.class, v6, v1);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e9, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		Edge e10 = graph.createEdge(Link.class, v2, v7);
		assertEquals(e10, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(LinkBack.class, true));
		assertEquals(e9, graph
				.getFirstEdgeOfClassInGraph(LinkBack.class, false));

		graph.deleteEdge(e9);
		assertEquals(e10, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				false));

		graph.deleteEdge(e8);
		assertEquals(e10, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(e10, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(null, graph
				.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class,
				false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				false));

		graph.deleteEdge(e10);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class, false));
		assertEquals(null, graph
				.getFirstEdgeOfClassInGraph(SubLink.class, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class,
				false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class,
				false));

		System.out.println("Done testing getFirstEdgeOfClassInGraph2.");
	}

	@Test
	public void testGetFirstEdgeOfClassInGraph3() {
		// preparations
		getEdgeClasses();
		Vertex v13 = graph.createVertex(SuperNode.class);

		// start tests
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack));

		Edge e1 = graph.createEdge(SubLink.class, v9, v6);
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack));

		Edge e2 = graph.createEdge(Link.class, v3, v13);
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack));

		Edge e3 = graph.createEdge(LinkBack.class, v7, v11);
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(lBack));

		graph.deleteEdge(e1);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(lBack));

		Edge e4 = graph.createEdge(SubLink.class, v10, v8);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(lBack));

		graph.deleteEdge(e3);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack));

		Edge e5 = graph.createEdge(LinkBack.class, v8, v3);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack));

		graph.deleteEdge(e2);
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack));

		Edge e6 = graph.createEdge(Link.class, v9, v5);
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack));

		graph.deleteEdge(e6);
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack));

		graph.deleteEdge(e4);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack));

		Edge e7 = graph.createEdge(SubLink.class, v11, v7);
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack));

		Edge e8 = graph.createEdge(SubLink.class, v10, v13);
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack));

		graph.deleteEdge(e5);
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack));

		Edge e9 = graph.createEdge(LinkBack.class, v6, v1);
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(lBack));

		Edge e10 = graph.createEdge(Link.class, v2, v7);
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(lBack));

		graph.deleteEdge(e7);
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(lBack));

		graph.deleteEdge(e9);
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack));

		graph.deleteEdge(e8);
		assertEquals(e10, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack));

		graph.deleteEdge(e10);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(link));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack));

		System.out.println("Done testing getFirstEdgeOfClassInGraph3.");
	}

	@Test
	public void testGetFirstEdgeOfClassInGraph4() {
		// preparations...
		getEdgeClasses();
		Vertex v13 = graph.createVertex(SuperNode.class);

		Edge e1 = graph.createEdge(SubLink.class, v9, v6);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e2 = graph.createEdge(Link.class, v3, v13);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e3 = graph.createEdge(LinkBack.class, v7, v11);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(lBack, false));

		graph.deleteEdge(e1);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e4 = graph.createEdge(SubLink.class, v10, v8);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(lBack, false));

		graph.deleteEdge(e2);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(lBack, false));

		graph.deleteEdge(e3);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e5 = graph.createEdge(LinkBack.class, v8, v3);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e6 = graph.createEdge(Link.class, v9, v5);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e7 = graph.createEdge(SubLink.class, v11, v7);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, false));

		graph.deleteEdge(e4);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, false));

		graph.deleteEdge(e7);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e8 = graph.createEdge(SubLink.class, v10, v13);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e9 = graph.createEdge(LinkBack.class, v6, v1);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(lBack, false));

		graph.deleteEdge(e5);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(lBack, false));

		graph.deleteEdge(e6);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(lBack, false));

		Edge e10 = graph.createEdge(Link.class, v2, v7);
		assertEquals(e10, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(lBack, false));

		graph.deleteEdge(e9);
		assertEquals(e10, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, false));

		graph.deleteEdge(e8);
		assertEquals(e10, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(e10, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, false));

		graph.deleteEdge(e10);
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(link, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(link, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(subL, false));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, true));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(lBack, false));

		System.out.println("Done testing getFirstEdgeOfClassInGraph4.");
	}

	@Test
	public void testGetVertex() {
		Vertex v13 = graph.createVertex(SubNode.class);
		Vertex v14 = graph.createVertex(DoubleSubNode.class);
		Vertex v15 = graph.createVertex(SuperNode.class);
		Vertex v16 = graph2.createVertex(SubNode.class);
		Vertex v17 = graph2.createVertex(SuperNode.class);
		Vertex v18 = graph2.createVertex(DoubleSubNode.class);

		// border cases
		assertEquals(v1, graph.getVertex(1));
		assertEquals(v16, graph2.getVertex(1));
		assertEquals(null, graph.getVertex(42));
		assertEquals(null, graph.getVertex(33));
		assertEquals(null, graph2.getVertex(4));
		// 1000 is the highest possible value
		assertEquals(null, graph.getVertex(1000));

		// normal cases
		assertEquals(v2, graph.getVertex(2));
		assertEquals(v3, graph.getVertex(3));
		assertEquals(v4, graph.getVertex(4));
		assertEquals(v5, graph.getVertex(5));
		assertEquals(v6, graph.getVertex(6));
		assertEquals(v7, graph.getVertex(7));
		assertEquals(v8, graph.getVertex(8));
		assertEquals(v9, graph.getVertex(9));
		assertEquals(v10, graph.getVertex(10));
		assertEquals(v11, graph.getVertex(11));
		assertEquals(v12, graph.getVertex(12));
		assertEquals(v13, graph.getVertex(13));
		assertEquals(v14, graph.getVertex(14));
		assertEquals(v15, graph.getVertex(15));
		assertEquals(v17, graph2.getVertex(2));
		assertEquals(v18, graph2.getVertex(3));

		System.out.println("Done testing getVertex.");
	}

	@Test
	public void testGetEdge() {
		Edge e1 = graph.createEdge(LinkBack.class, v5, v1);
		Edge e2 = graph.createEdge(Link.class, v2, v7);
		Edge e3 = graph.createEdge(LinkBack.class, v8, v4);
		Edge e4 = graph.createEdge(SubLink.class, v11, v6);
		Edge e5 = graph.createEdge(Link.class, v2, v5);
		Edge e6 = graph.createEdge(LinkBack.class, v7, v12);
		Edge e7 = graph.createEdge(SubLink.class, v9, v8);
		Edge e8 = graph.createEdge(SubLink.class, v10, v6);
		Edge e9 = graph.createEdge(Link.class, v3, v7);
		Edge e10 = graph.createEdge(Link.class, v3, v7);

		// border cases
		assertEquals(null, graph.getEdge(42));
		assertEquals(null, graph.getEdge(-42));
		assertEquals(e1, graph.getEdge(1));
		assertEquals(null, graph.getEdge(1000));
		assertEquals(null, graph.getEdge(-1000));

		// normal cases
		assertEquals(e2, graph.getEdge(2));
		assertEquals(e2.getReversedEdge(), graph.getEdge(-2));
		assertEquals(e3, graph.getEdge(3));
		assertEquals(e3.getReversedEdge(), graph.getEdge(-3));
		assertEquals(e4, graph.getEdge(4));
		assertEquals(e4.getReversedEdge(), graph.getEdge(-4));
		assertEquals(e5, graph.getEdge(5));
		assertEquals(e5.getReversedEdge(), graph.getEdge(-5));
		assertEquals(e6, graph.getEdge(6));
		assertEquals(e6.getReversedEdge(), graph.getEdge(-6));
		assertEquals(e7, graph.getEdge(7));
		assertEquals(e7.getReversedEdge(), graph.getEdge(-7));
		assertEquals(e8, graph.getEdge(8));
		assertEquals(e8.getReversedEdge(), graph.getEdge(-8));
		assertEquals(e9, graph.getEdge(9));
		assertEquals(e9.getReversedEdge(), graph.getEdge(-9));
		assertEquals(e10, graph.getEdge(10));
		assertEquals(e10.getReversedEdge(), graph.getEdge(-10));
	}

	@Test
	public void testGetMaxVCount() {
		assertEquals(1000, graph.getMaxVCount());
		assertEquals(1000, graph2.getMaxVCount());
		MinimalGraph graph3 = MinimalSchema.instance().createMinimalGraph();
		assertEquals(1000, graph3.getMaxVCount());

		System.out.println("Done testing getMaxVCount.");
	}

	@Test
	public void testGetExpandedVertexCount() {
		// border case
		assertEquals(2000, graph.getExpandedVertexCount());

		// normal cases
		for (int i = 12; i < 1000; i++) {
			graph.createVertex(SubNode.class);
		}
		assertEquals(2000, graph.getExpandedVertexCount());
		for (int i = 0; i < 1000; i++) {
			graph.createVertex(SuperNode.class);
		}
		assertEquals(4000, graph.getExpandedVertexCount());
		for (int i = 0; i < 1000; i++) {
			graph.createVertex(DoubleSubNode.class);
		}
		assertEquals(8000, graph.getExpandedVertexCount());
		System.out.println("Done testing getExpandedVertexCount.");
	}

	@Test
	public void testGetExpandedEdgeCount() {
		// border case
		assertEquals(2000, graph.getExpandedEdgeCount());

		// normal cases
		for (int i = 0; i < 1000; i++) {
			graph.createEdge(SubLink.class, v9, v5);
		}
		assertEquals(2000, graph.getExpandedEdgeCount());

		for (int i = 0; i < 1000; i++) {
			graph.createEdge(Link.class, v1, v5);
		}
		assertEquals(4000, graph.getExpandedEdgeCount());

		for (int i = 0; i < 1000; i++) {
			graph.createEdge(LinkBack.class, v5, v9);
		}
		assertEquals(8000, graph.getExpandedEdgeCount());

		System.out.println("Done testing getExpandedEdgeCount.");
	}

	@Test
	public void testGetMaxECount() {
		assertEquals(1000, graph.getMaxECount());
		assertEquals(1000, graph2.getMaxECount());
		MinimalGraph graph3 = MinimalSchema.instance().createMinimalGraph();
		assertEquals(1000, graph3.getMaxECount());

		System.out.println("Done testing getMaxECount.");
	}

	@Test
	public void testGetVCount() {
		// border cases
		assertEquals(0, graph2.getVCount());

		Vertex v1 = graph2.createVertex(SubNode.class);
		assertEquals(1, graph2.getVCount());

		graph2.deleteVertex(v1);
		assertEquals(0, graph2.getVCount());

		graph2.createVertex(SubNode.class);
		assertEquals(1, graph2.getVCount());

		// normal cases
		assertEquals(12, graph.getVCount());
		Vertex v2 = graph2.createVertex(SubNode.class);
		assertEquals(2, graph2.getVCount());

		graph2.createVertex(SubNode.class);
		assertEquals(3, graph2.getVCount());

		graph2.deleteVertex(v2);
		assertEquals(2, graph2.getVCount());

		graph2.createVertex(SuperNode.class);
		assertEquals(3, graph2.getVCount());

		Vertex v3 = graph2.createVertex(SuperNode.class);
		assertEquals(4, graph2.getVCount());

		graph2.deleteVertex(v3);
		assertEquals(3, graph2.getVCount());

		Vertex v4 = graph2.createVertex(SuperNode.class);
		assertEquals(4, graph2.getVCount());

		graph2.createVertex(SuperNode.class);
		assertEquals(5, graph2.getVCount());

		graph2.createVertex(DoubleSubNode.class);
		assertEquals(6, graph2.getVCount());

		graph2.createVertex(DoubleSubNode.class);
		assertEquals(7, graph2.getVCount());

		graph2.deleteVertex(v4);
		assertEquals(6, graph2.getVCount());

		graph2.createVertex(DoubleSubNode.class);
		assertEquals(7, graph2.getVCount());

		graph2.createVertex(DoubleSubNode.class);
		assertEquals(8, graph2.getVCount());

		for (int i = 9; i < 20; i++) {
			graph2.createVertex(SuperNode.class);
			assertEquals(i, graph2.getVCount());
		}

		for (int i = 20; i < 32; i++) {
			graph2.createVertex(DoubleSubNode.class);
			assertEquals(i, graph2.getVCount());
		}

		for (int i = 32; i < 42; i++) {
			graph2.createVertex(SubNode.class);
			assertEquals(i, graph2.getVCount());
		}

		System.out.println("Done testing getVCount.");
	}

	@Test
	public void testGetECount() {
		// border cases
		assertEquals(0, graph.getECount());
		Edge e1 = graph.createEdge(LinkBack.class, v5, v1);
		assertEquals(1, graph.getECount());

		// creating a vertex does not change the value
		graph.createVertex(DoubleSubNode.class);
		assertEquals(1, graph.getECount());

		// when an edge is deleted, the count is decreased by 1
		graph.deleteEdge(e1);
		assertEquals(0, graph.getECount());

		// normal cases
		// creating an edge increases the value by 1
		Edge e2 = graph.createEdge(Link.class, v2, v7);
		assertEquals(1, graph.getECount());
		Edge e3 = graph.createEdge(LinkBack.class, v8, v4);
		assertEquals(2, graph.getECount());
		Edge e4 = graph.createEdge(SubLink.class, v11, v6);
		assertEquals(3, graph.getECount());
		Edge e5 = graph.createEdge(Link.class, v2, v5);
		assertEquals(4, graph.getECount());
		Edge e6 = graph.createEdge(LinkBack.class, v7, v12);
		assertEquals(5, graph.getECount());
		Edge e7 = graph.createEdge(SubLink.class, v9, v8);
		assertEquals(6, graph.getECount());
		Edge e8 = graph.createEdge(SubLink.class, v10, v6);
		assertEquals(7, graph.getECount());
		Edge e9 = graph.createEdge(Link.class, v3, v7);
		assertEquals(8, graph.getECount());
		Edge e10 = graph.createEdge(Link.class, v3, v7);
		assertEquals(9, graph.getECount());

		// deleting edges...
		graph.deleteEdge(e2);
		assertEquals(8, graph.getECount());
		graph.deleteEdge(e3);
		assertEquals(7, graph.getECount());
		graph.deleteEdge(e4);
		assertEquals(6, graph.getECount());
		graph.deleteEdge(e5);
		assertEquals(5, graph.getECount());
		graph.deleteEdge(e6);
		assertEquals(4, graph.getECount());
		graph.deleteEdge(e7);
		assertEquals(3, graph.getECount());
		graph.deleteEdge(e8);
		assertEquals(2, graph.getECount());
		graph.deleteEdge(e9);
		assertEquals(1, graph.getECount());
		graph.deleteEdge(e10);
		assertEquals(0, graph.getECount());

		System.out.println("Done testing getECount.");
	}

	@Test
	public void testSetId() {
		graph.setId("alpha");
		assertEquals("alpha", graph.getId());

		graph.setId("1265");
		assertEquals("1265", graph.getId());

		graph.setId("007");
		assertEquals("007", graph.getId());

		graph.setId("r2d2");
		assertEquals("r2d2", graph.getId());

		graph.setId("answer:42");
		assertEquals("answer:42", graph.getId());

		graph.setId("1506");
		assertEquals("1506", graph.getId());

		graph.setId("june15");
		assertEquals("june15", graph.getId());

		graph.setId("bang");
		assertEquals("bang", graph.getId());

		graph.setId("22now");
		assertEquals("22now", graph.getId());

		graph.setId("hjkutzbv");
		assertEquals("hjkutzbv", graph.getId());

		graph.setId("54rdcg9");
		assertEquals("54rdcg9", graph.getId());

		graph.setId(".k,oibt");
		assertEquals(".k,oibt", graph.getId());

		System.out.println("Done testing setId.");
	}

	@Test
	public void testEdges() {
		assertEquals(null, graph.edges().iterator().next());
		assertEquals(false, graph.edges().iterator().hasNext());

		Edge e1 = graph.createEdge(Link.class, v3, v7);
		Edge e2 = graph.createEdge(Link.class, v4, v8);
		Edge e3 = graph.createEdge(Link.class, v1, v8);
		Edge e4 = graph.createEdge(SubLink.class, v12, v5);
		Edge e5 = graph.createEdge(SubLink.class, v10, v7);
		Edge e6 = graph.createEdge(SubLink.class, v11, v5);
		Edge e7 = graph.createEdge(LinkBack.class, v6, v12);
		Edge e8 = graph.createEdge(LinkBack.class, v6, v3);
		Edge e9 = graph.createEdge(LinkBack.class, v8, v9);

		Edge[] graphEdges = { e1, e2, e3, e4, e5, e6, e7, e8, e9 };
		int i = 0;
		for (Edge e : graph.edges()) {
			assertEquals(graphEdges[i], e);
			i++;
		}

		Edge e10 = graph.createEdge(SubLink.class, v11, v6);
		Edge e11 = graph.createEdge(LinkBack.class, v7, v12);
		Edge e12 = graph.createEdge(LinkBack.class, v5, v1);
		Edge e13 = graph.createEdge(Link.class, v12, v5);
		Edge e14 = graph.createEdge(SubLink.class, v9, v7);
		Edge e15 = graph.createEdge(SubLink.class, v11, v6);

		Edge[] graphEdges2 = { e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11,
				e12, e13, e14, e15 };
		i = 0;
		for (Edge e : graph.edges()) {
			assertEquals(graphEdges2[i], e);
			i++;
		}

		Edge e16 = graph.createEdge(LinkBack.class, v5, v2);
		Edge e17 = graph.createEdge(SubLink.class, v10, v6);
		Edge e18 = graph.createEdge(LinkBack.class, v8, v12);
		Edge e19 = graph.createEdge(Link.class, v1, v7);
		Edge e20 = graph.createEdge(SubLink.class, v10, v6);
		Edge e21 = graph.createEdge(Link.class, v3, v6);

		Edge[] graphEdges3 = { e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11,
				e12, e13, e14, e15, e16, e17, e18, e19, e20, e21 };
		i = 0;
		for (Edge e : graph.edges()) {
			assertEquals(graphEdges3[i], e);
			i++;
		}

		System.out.println("Done testing edges.");
	}

	@Test
	public void testEdges2() {
		// preparations...
		getEdgeClasses();

		assertEquals(null, graph.edges(link).iterator().next());
		assertEquals(false, graph.edges(link).iterator().hasNext());
		assertEquals(null, graph.edges(subL).iterator().next());
		assertEquals(false, graph.edges(subL).iterator().hasNext());
		assertEquals(null, graph.edges(lBack).iterator().next());
		assertEquals(false, graph.edges(lBack).iterator().hasNext());

		Edge e1 = graph.createEdge(Link.class, v3, v7);
		Edge e2 = graph.createEdge(Link.class, v4, v8);
		Edge e3 = graph.createEdge(Link.class, v1, v8);
		Edge e4 = graph.createEdge(SubLink.class, v12, v5);
		Edge e5 = graph.createEdge(SubLink.class, v10, v7);
		Edge e6 = graph.createEdge(SubLink.class, v11, v5);
		Edge e7 = graph.createEdge(LinkBack.class, v6, v12);
		Edge e8 = graph.createEdge(LinkBack.class, v6, v2);
		Edge e9 = graph.createEdge(LinkBack.class, v8, v9);

		Edge[] graphLink = { e1, e2, e3, e4, e5, e6 };
		int i = 0;
		for (Edge e : graph.edges(link)) {
			assertEquals(graphLink[i], e);
			i++;
		}

		Edge[] graphSubLink = { e4, e5, e6 };
		i = 0;
		for (Edge e : graph.edges(subL)) {
			assertEquals(graphSubLink[i], e);
			i++;
		}

		Edge[] graphLinkBack = { e7, e8, e9 };
		i = 0;
		for (Edge e : graph.edges(lBack)) {
			assertEquals(graphLinkBack[i], e);
			i++;
		}

		Edge e10 = graph.createEdge(SubLink.class, v11, v6);
		Edge e11 = graph.createEdge(LinkBack.class, v7, v12);
		Edge e12 = graph.createEdge(LinkBack.class, v5, v1);
		Edge e13 = graph.createEdge(Link.class, v12, v5);
		Edge e14 = graph.createEdge(SubLink.class, v9, v7);
		Edge e15 = graph.createEdge(SubLink.class, v11, v6);

		Edge[] graphLink2 = { e1, e2, e3, e4, e5, e6, e10, e13, e14, e15 };
		i = 0;
		for (Edge e : graph.edges(link)) {
			assertEquals(graphLink2[i], e);
			i++;
		}

		Edge[] graphSubLink2 = { e4, e5, e6, e10, e14, e15 };
		i = 0;
		for (Edge e : graph.edges(subL)) {
			assertEquals(graphSubLink2[i], e);
			i++;
		}

		Edge[] graphLinkBack2 = { e7, e8, e9, e11, e12 };
		i = 0;
		for (Edge e : graph.edges(lBack)) {
			assertEquals(graphLinkBack2[i], e);
			i++;
		}

		System.out.println("Done testing edges2.");
	}

	@Test
	public void testEdges3() {
		Edge e1 = graph.createEdge(Link.class, v3, v7);
		Edge e2 = graph.createEdge(Link.class, v4, v8);
		Edge e3 = graph.createEdge(Link.class, v1, v8);
		Edge e4 = graph.createEdge(SubLink.class, v12, v5);
		Edge e5 = graph.createEdge(SubLink.class, v10, v7);
		Edge e6 = graph.createEdge(SubLink.class, v11, v5);
		Edge e7 = graph.createEdge(LinkBack.class, v6, v12);
		Edge e8 = graph.createEdge(LinkBack.class, v6, v3);
		Edge e9 = graph.createEdge(LinkBack.class, v8, v9);

		Edge[] graphLink = { e1, e2, e3, e4, e5, e6 };
		int i = 0;
		for (Edge e : graph.edges(Link.class)) {
			assertEquals(graphLink[i], e);
			i++;
		}

		Edge[] graphSubLink = { e4, e5, e6 };
		i = 0;
		for (Edge e : graph.edges(SubLink.class)) {
			assertEquals(graphSubLink[i], e);
			i++;
		}

		Edge[] graphLinkBack = { e7, e8, e9 };
		i = 0;
		for (Edge e : graph.edges(LinkBack.class)) {
			assertEquals(graphLinkBack[i], e);
			i++;
		}

		Edge e10 = graph.createEdge(LinkBack.class, v5, v2);
		Edge e11 = graph.createEdge(SubLink.class, v10, v6);
		Edge e12 = graph.createEdge(LinkBack.class, v8, v12);
		Edge e13 = graph.createEdge(Link.class, v1, v7);
		Edge e14 = graph.createEdge(SubLink.class, v10, v6);
		Edge e15 = graph.createEdge(Link.class, v3, v6);

		Edge[] graphLink2 = { e1, e2, e3, e4, e5, e6, e11, e13, e14, e15 };
		i = 0;
		for (Edge e : graph.edges(Link.class)) {
			assertEquals(graphLink2[i], e);
			i++;
		}

		Edge[] graphSubLink2 = { e4, e5, e6, e11, e14 };
		i = 0;
		for (Edge e : graph.edges(SubLink.class)) {
			assertEquals(graphSubLink2[i], e);
			i++;
		}

		Edge[] graphLinkBack2 = { e7, e8, e9, e10, e12 };
		i = 0;
		for (Edge e : graph.edges(LinkBack.class)) {
			assertEquals(graphLinkBack2[i], e);
			i++;
		}

		System.out.println("Done testing edges3.");
	}

	@Test
	public void testVertices() {
		assertEquals(false, graph2.vertices().iterator().hasNext());
		assertEquals(true, graph.vertices().iterator().hasNext());

		Vertex[] graphVertices = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12 };

		int i = 0;
		for (Vertex v : graph.vertices()) {
			assertEquals(graphVertices[i], v);
			i++;
		}

		Vertex v13 = graph.createVertex(DoubleSubNode.class);
		Vertex v14 = graph.createVertex(SuperNode.class);
		Vertex v15 = graph.createVertex(SuperNode.class);
		Vertex v16 = graph.createVertex(DoubleSubNode.class);

		Vertex[] graphVertices2 = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12, v13, v14, v15, v16 };

		i = 0;
		for (Vertex v : graph.vertices()) {
			assertEquals(graphVertices2[i], v);
			i++;
		}

		Vertex v17 = graph.createVertex(SubNode.class);
		Vertex v18 = graph.createVertex(DoubleSubNode.class);
		Vertex v19 = graph.createVertex(SubNode.class);
		Vertex v20 = graph.createVertex(SuperNode.class);
		Vertex v21 = graph.createVertex(DoubleSubNode.class);
		Vertex v22 = graph.createVertex(SuperNode.class);

		Vertex[] graphVertices3 = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22 };

		i = 0;
		for (Vertex v : graph.vertices()) {
			assertEquals(graphVertices3[i], v);
			i++;
		}

		System.out.println("Done testing vertices.");
	}

	@Test
	public void testVertices2() {
		// preparations...
		getVertexClasses();

		assertEquals(false, graph2.vertices(subN).iterator().hasNext());
		assertEquals(false, graph2.vertices(superN).iterator().hasNext());
		assertEquals(false, graph2.vertices(doubleSubN).iterator().hasNext());
		assertEquals(true, graph.vertices(subN).iterator().hasNext());
		assertEquals(true, graph.vertices(superN).iterator().hasNext());
		assertEquals(true, graph.vertices(doubleSubN).iterator().hasNext());

		Vertex[] graphSubN = { v1, v2, v3, v4, v9, v10, v11, v12 };
		int i = 0;
		for (Vertex v : graph.vertices(subN)) {
			assertEquals(graphSubN[i], v);
			i++;
		}

		Vertex[] graphSuperN = { v5, v6, v7, v8, v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : graph.vertices(superN)) {
			assertEquals(graphSuperN[i], v);
			i++;
		}

		Vertex[] graphDSN = { v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : graph.vertices(doubleSubN)) {
			assertEquals(graphDSN[i], v);
			i++;
		}

		Vertex v13 = graph.createVertex(DoubleSubNode.class);
		Vertex v14 = graph.createVertex(SubNode.class);
		Vertex v15 = graph.createVertex(DoubleSubNode.class);
		Vertex v16 = graph.createVertex(SuperNode.class);
		Vertex v17 = graph.createVertex(SuperNode.class);
		Vertex v18 = graph.createVertex(SubNode.class);
		Vertex v19 = graph.createVertex(DoubleSubNode.class);
		Vertex v20 = graph.createVertex(SuperNode.class);

		Vertex[] graphSubN2 = { v1, v2, v3, v4, v9, v10, v11, v12, v13, v14,
				v15, v18, v19 };
		i = 0;
		for (Vertex v : graph.vertices(subN)) {
			assertEquals(graphSubN2[i], v);
			i++;
		}

		Vertex[] graphSuperN2 = { v5, v6, v7, v8, v9, v10, v11, v12, v13, v15,
				v16, v17, v19, v20 };
		i = 0;
		for (Vertex v : graph.vertices(superN)) {
			assertEquals(graphSuperN2[i], v);
			i++;
		}

		Vertex[] graphDSN2 = { v9, v10, v11, v12, v13, v15, v19 };
		i = 0;
		for (Vertex v : graph.vertices(doubleSubN)) {
			assertEquals(graphDSN2[i], v);
			i++;
		}

		System.out.println("Done testing vertices2.");

	}

	@Test
	public void testVertices3() {
		assertEquals(false, graph2.vertices(SubNode.class).iterator().hasNext());
		assertEquals(false, graph2.vertices(SuperNode.class).iterator()
				.hasNext());
		assertEquals(false, graph2.vertices(DoubleSubNode.class).iterator()
				.hasNext());
		assertEquals(true, graph.vertices(SubNode.class).iterator().hasNext());
		assertEquals(true, graph.vertices(SuperNode.class).iterator().hasNext());
		assertEquals(true, graph.vertices(DoubleSubNode.class).iterator()
				.hasNext());

		Vertex[] graphSubN = { v1, v2, v3, v4, v9, v10, v11, v12 };
		int i = 0;
		for (Vertex v : graph.vertices(SubNode.class)) {
			assertEquals(graphSubN[i], v);
			i++;
		}

		Vertex[] graphSuperN = { v5, v6, v7, v8, v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : graph.vertices(SuperNode.class)) {
			assertEquals(graphSuperN[i], v);
			i++;
		}

		Vertex[] graphDoubleSubN = { v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : graph.vertices(DoubleSubNode.class)) {
			assertEquals(graphDoubleSubN[i], v);
			i++;
		}

		Vertex v13 = graph.createVertex(DoubleSubNode.class);
		Vertex v14 = graph.createVertex(DoubleSubNode.class);
		Vertex v15 = graph.createVertex(SuperNode.class);
		Vertex v16 = graph.createVertex(SubNode.class);
		Vertex v17 = graph.createVertex(SuperNode.class);
		Vertex v18 = graph.createVertex(DoubleSubNode.class);
		Vertex v19 = graph.createVertex(SubNode.class);
		Vertex v20 = graph.createVertex(SuperNode.class);

		Vertex[] graphSubN2 = { v1, v2, v3, v4, v9, v10, v11, v12, v13, v14,
				v16, v18, v19 };
		i = 0;
		for (Vertex v : graph.vertices(SubNode.class)) {
			assertEquals(graphSubN2[i], v);
			i++;
		}

		Vertex[] graphSuperN2 = { v5, v6, v7, v8, v9, v10, v11, v12, v13, v14,
				v15, v17, v18, v20 };
		i = 0;
		for (Vertex v : graph.vertices(SuperNode.class)) {
			assertEquals(graphSuperN2[i], v);
			i++;
		}

		Vertex[] graphDoubleSubN2 = { v9, v10, v11, v12, v13, v14, v18 };
		i = 0;
		for (Vertex v : graph.vertices(DoubleSubNode.class)) {
			assertEquals(graphDoubleSubN2[i], v);
			i++;
		}

		System.out.println("Done testing vertices3.");
	}

	@Test
	public void testDefragment() {
		/*
		 * Testen der defragment()-Methode: Ein Vorher-Nachher Abbild von
		 * Vertex- Referenzen sammeln und vergleichen, genauso mit Kantenseq.
		 * Inzidenzen sind nicht betroffen (von defragment() zumindest das, was
		 * einfach zu testen ist); Dafür bedarf es einen Graph, indem gelöscht
		 * wurde und dadurch Lücken entstanden sind, sodass defragment() zum
		 * Einsatz kommen kann
		 */
	}

	public class GraphTestKlasse extends GraphImpl {

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