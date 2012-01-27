package de.uni_koblenz.jgralabtest.genericimpltest;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.RecordImpl;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;

public class GenericEdgeImplTest {

	@Test
	public void testAccessAttributes() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass("TestVertex"));
			Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass("TestVertex"));
			Edge e1 = g.createEdge(g.getGraphClass().getEdgeClass("TestEdge"), v1, v2);

			// accessing default values
			assertEquals(true, e1.getAttribute("boolEdge"));
			assertEquals(
					JGraLab.vector().plus(JGraLab.vector().plus(true))
							.plus(JGraLab.vector().plus(false)).plus(JGraLab.vector().plus(true)),
					e1.getAttribute("complexListEdge"));
			assertEquals(JGraLab.map().plus(JGraLab.vector().plus(true), JGraLab.set().plus(true))
					.plus(JGraLab.vector().plus(false), JGraLab.set().plus(false)),
					e1.getAttribute("complexMapEdge"));
			assertEquals(
					JGraLab.set().plus(JGraLab.set().plus(true)).plus(JGraLab.set().plus(false)),
					e1.getAttribute("complexSetEdge"));
			assertEquals(1.1d, e1.getAttribute("doubleEdge"));
			assertEquals("FIRST", e1.getAttribute("enumEdge"));
			assertEquals(1, e1.getAttribute("intEdge"));
			assertEquals(JGraLab.vector().plus(true).plus(false).plus(true),
					e1.getAttribute("listEdge"));
			assertEquals(1l, e1.getAttribute("longEdge"));
			assertEquals(JGraLab.map().plus(1, true).plus(2, false).plus(3, true),
					e1.getAttribute("mapEdge"));
			assertEquals(
					RecordImpl
							.empty()
							.plus("boolRecord", true)
							.plus("doubleRecord", 1.1d)
							.plus("enumRecord", "FIRST")
							.plus("intRecord", 1)
							.plus("listRecord", JGraLab.vector().plus(true).plus(false).plus(true))
							.plus("longRecord", 1l)
							.plus("mapRecord",
									JGraLab.map().plus(1, true).plus(2, false).plus(3, true))
							.plus("setRecord", JGraLab.set().plus(true).plus(false))
							.plus("stringRecord", "test"), e1.getAttribute("recordEdge"));
			assertEquals(JGraLab.set().plus(true).plus(false), e1.getAttribute("setEdge"));
			assertEquals("test", e1.getAttribute("stringEdge"));

			// changing values
			e1.setAttribute("boolEdge", false);
			assertEquals(false, e1.getAttribute("boolEdge"));
			e1.setAttribute("complexListEdge", JGraLab.vector().plus(JGraLab.vector().plus(false))
					.plus(JGraLab.vector().plus(false)));
			assertEquals(
					JGraLab.vector().plus(JGraLab.vector().plus(false))
							.plus(JGraLab.vector().plus(false)), e1.getAttribute("complexListEdge"));
			e1.setAttribute(
					"complexMapEdge",
					JGraLab.map()
							.plus(JGraLab.vector().plus(true).plus(false),
									JGraLab.set().plus(false))
							.plus(JGraLab.vector().plus(false).plus(true), JGraLab.set().plus(true)));
			assertEquals(
					JGraLab.map()
							.plus(JGraLab.vector().plus(true).plus(false),
									JGraLab.set().plus(false))
							.plus(JGraLab.vector().plus(false).plus(true), JGraLab.set().plus(true)),
					e1.getAttribute("complexMapEdge"));
			e1.setAttribute("complexSetEdge", JGraLab.set().plus(JGraLab.set().plus(false)));
			assertEquals(JGraLab.set().plus(JGraLab.set().plus(false)),
					e1.getAttribute("complexSetEdge"));
			e1.setAttribute("doubleEdge", 2.2d);
			assertEquals(2.2d, e1.getAttribute("doubleEdge"));
			e1.setAttribute("enumEdge", "SECOND");
			assertEquals("SECOND", e1.getAttribute("enumEdge"));
			e1.setAttribute("intEdge", 42);
			assertEquals(42, e1.getAttribute("intEdge"));
			e1.setAttribute("listEdge", JGraLab.vector().plus(false).plus(false).plus(true));
			assertEquals(JGraLab.vector().plus(false).plus(false).plus(true),
					e1.getAttribute("listEdge"));
			e1.setAttribute("longEdge", 987654321l);
			assertEquals(987654321l, e1.getAttribute("longEdge"));
			e1.setAttribute("mapEdge", JGraLab.map().plus(42, true).plus(24, false));
			assertEquals(JGraLab.map().plus(42, true).plus(24, false), e1.getAttribute("mapEdge"));
			e1.setAttribute(
					"recordEdge",
					RecordImpl
							.empty()
							.plus("boolRecord", false)
							.plus("doubleRecord", 1.3d)
							.plus("enumRecord", "THIRD")
							.plus("intRecord", 42)
							.plus("listRecord", JGraLab.vector().plus(false).plus(true).plus(false))
							.plus("longRecord", 987654321l)
							.plus("mapRecord", JGraLab.map().plus(42, true).plus(24, false))
							.plus("setRecord", JGraLab.set().plus(false))
							.plus("stringRecord", "more test"));
			assertEquals(
					RecordImpl
							.empty()
							.plus("boolRecord", false)
							.plus("doubleRecord", 1.3d)
							.plus("enumRecord", "THIRD")
							.plus("intRecord", 42)
							.plus("listRecord", JGraLab.vector().plus(false).plus(true).plus(false))
							.plus("longRecord", 987654321l)
							.plus("mapRecord", JGraLab.map().plus(42, true).plus(24, false))
							.plus("setRecord", JGraLab.set().plus(false))
							.plus("stringRecord", "more test"), e1.getAttribute("recordEdge"));
			e1.setAttribute("setEdge", JGraLab.set().plus(true));
			assertEquals(JGraLab.set().plus(true), e1.getAttribute("setEdge"));
			e1.setAttribute("stringEdge", "some String");
			assertEquals("some String", e1.getAttribute("stringEdge"));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test setting an attribute that doesn't exist
	@Test(expected = GraphException.class)
	public void testAccessAttributesFailure1() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass("TestVertex"));
			Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass("TestVertex"));
			Edge e1 = g.createEdge(g.getGraphClass().getEdgeClass("TestEdge"), v1, v2);
			e1.setAttribute("MapEdge", JGraLab.map());
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test(expected = GraphException.class)
	public void testAccessAttributesFailure2() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass("TestVertex"));
			Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass("TestVertex"));
			Edge e1 = g.createEdge(g.getGraphClass().getEdgeClass("TestEdge"), v1, v2);
			e1.setAttribute("SapEdge", JGraLab.set().plus(false));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test setting attributes with values that don't conform to their domain
	@Test(expected = ClassCastException.class)
	public void testAccessAttributesFailure3() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass("TestVertex"));
			Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass("TestVertex"));
			Edge e1 = g.createEdge(g.getGraphClass().getEdgeClass("TestEdge"), v1, v2);
			e1.setAttribute("mapEdge", JGraLab.set());
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetNextEdge() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
					+ "VertexTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);

			Vertex[] vertices = new Vertex[6];
			vertices[0] = g.createVertex(g.getGraphClass().getVertexClass("A"));
			vertices[1] = g.createVertex(g.getGraphClass().getVertexClass("B"));
			vertices[2] = g.createVertex(g.getGraphClass().getVertexClass("C"));
			vertices[3] = g.createVertex(g.getGraphClass().getVertexClass("D"));
			vertices[4] = g.createVertex(g.getGraphClass().getVertexClass("C2"));
			vertices[5] = g.createVertex(g.getGraphClass().getVertexClass("D2"));

			EdgeClass[] edgeClasses = new EdgeClass[7];
			edgeClasses[0] = g.getGraphClass().getEdgeClass("E");
			edgeClasses[1] = g.getGraphClass().getEdgeClass("F");
			edgeClasses[2] = g.getGraphClass().getEdgeClass("G");
			edgeClasses[3] = g.getGraphClass().getEdgeClass("H");
			edgeClasses[4] = g.getGraphClass().getEdgeClass("I");
			edgeClasses[5] = g.getGraphClass().getEdgeClass("J");
			edgeClasses[6] = g.getGraphClass().getEdgeClass("K");

			Edge[] edges = new Edge[7];
			edges[0] = g.createEdge(edgeClasses[0], vertices[0], vertices[1]);
			for (EdgeClass ec : edgeClasses) {
				assertNull(edges[0].getNextEdge(ec));
			}
			edges[1] = g.createEdge(edgeClasses[1], vertices[2], vertices[3]);
			edges[2] = g.createEdge(edgeClasses[2], vertices[2], vertices[3]);
			edges[3] = g.createEdge(edgeClasses[3], vertices[0], vertices[1]);
			edges[4] = g.createEdge(edgeClasses[4], vertices[0], vertices[0]);
			edges[5] = g.createEdge(edgeClasses[5], vertices[4], vertices[5]);
			edges[6] = g.createEdge(edgeClasses[6], vertices[0], vertices[1]);

			for (int i = 1; i < edgeClasses.length; i++) {
				assertEquals(edges[i], edges[i - 1].getNextEdge(edgeClasses[i]));
			}
			assertEquals(edges[1], edges[0].getNextEdge(edgeClasses[0]));
			assertEquals(edges[2], edges[1].getNextEdge(edgeClasses[0]));
			assertEquals(edges[3], edges[2].getNextEdge(edgeClasses[0]));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetNextIncidence() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
					+ "VertexTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);

			Vertex[] vertices = new Vertex[6];
			vertices[0] = g.createVertex(g.getGraphClass().getVertexClass("A"));
			vertices[1] = g.createVertex(g.getGraphClass().getVertexClass("B"));
			vertices[2] = g.createVertex(g.getGraphClass().getVertexClass("C"));
			vertices[3] = g.createVertex(g.getGraphClass().getVertexClass("D"));
			vertices[4] = g.createVertex(g.getGraphClass().getVertexClass("C2"));
			vertices[5] = g.createVertex(g.getGraphClass().getVertexClass("D2"));

			EdgeClass[] edgeClasses = new EdgeClass[7];
			edgeClasses[0] = g.getGraphClass().getEdgeClass("E");
			edgeClasses[1] = g.getGraphClass().getEdgeClass("F");
			edgeClasses[2] = g.getGraphClass().getEdgeClass("G");
			edgeClasses[3] = g.getGraphClass().getEdgeClass("H");
			edgeClasses[4] = g.getGraphClass().getEdgeClass("I");
			edgeClasses[5] = g.getGraphClass().getEdgeClass("J");
			edgeClasses[6] = g.getGraphClass().getEdgeClass("K");

			Edge[] edges = new Edge[7];
			edges[0] = g.createEdge(edgeClasses[0], vertices[0], vertices[1]);
			for (int i = 0; i < edgeClasses.length; i++) {
				assertNull(edges[0].getNextIncidence(edgeClasses[i]));
			}
			edges[1] = g.createEdge(edgeClasses[1], vertices[2], vertices[3]);
			edges[2] = g.createEdge(edgeClasses[2], vertices[2], vertices[3]);
			edges[3] = g.createEdge(edgeClasses[3], vertices[0], vertices[1]);
			edges[4] = g.createEdge(edgeClasses[4], vertices[0], vertices[0]);
			edges[5] = g.createEdge(edgeClasses[5], vertices[4], vertices[5]);
			edges[6] = g.createEdge(edgeClasses[6], vertices[0], vertices[1]);

			assertEquals(edges[3], edges[0].getNextIncidence(edgeClasses[0]));
			assertEquals(edges[3], edges[0].getNextIncidence(edgeClasses[3]));
			assertEquals(edges[4], edges[0].getNextIncidence(edgeClasses[4]));
			assertEquals(edges[6], edges[0].getNextIncidence(edgeClasses[6]));
			assertEquals(edges[2], edges[1].getNextIncidence(edgeClasses[0]));
			assertEquals(edges[2], edges[1].getNextIncidence(edgeClasses[2]));
			
			assertEquals(edges[4].getReversedEdge(), edges[0].getNextIncidence(edgeClasses[4], EdgeDirection.IN));
			assertEquals(edges[4].getReversedEdge(), edges[3].getNextIncidence(edgeClasses[4], EdgeDirection.IN));
			assertNull(edges[1].getNextIncidence(edgeClasses[1], EdgeDirection.IN));
			assertNull(edges[0].getNextIncidence(edgeClasses[0], true));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetAggregationKind() {
		// TODO
	}

	@Test
	public void testGetAlphaAggregationKind() {
		// TODO
	}

	@Test
	public void testGetOmegaAggregationKind() {
		// TODO
	}
}
