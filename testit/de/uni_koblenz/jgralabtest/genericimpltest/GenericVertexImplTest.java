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
import de.uni_koblenz.jgralab.schema.VertexClass;

public class GenericVertexImplTest {
	@Test
	public void testAccessAttributes() {
		try {
			Schema s = GraphIO
					.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
							+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			Vertex v = g.createVertex(g.getGraphClass().getVertexClass(
					"TestVertex"));

			// accessing default values
			assertEquals(true, v.getAttribute("boolVertex"));
			assertEquals(
					JGraLab.vector().plus(JGraLab.vector().plus(true))
							.plus(JGraLab.vector().plus(false))
							.plus(JGraLab.vector().plus(true)),
					v.getAttribute("complexListVertex"));
			assertEquals(
					JGraLab.map()
							.plus(JGraLab.vector().plus(true),
									JGraLab.set().plus(true))
							.plus(JGraLab.vector().plus(false),
									JGraLab.set().plus(false)),
					v.getAttribute("complexMapVertex"));
			assertEquals(
					JGraLab.set().plus(JGraLab.set().plus(true))
							.plus(JGraLab.set().plus(false)),
					v.getAttribute("complexSetVertex"));
			assertEquals(1.1d, v.getAttribute("doubleVertex"));
			assertEquals("FIRST", v.getAttribute("enumVertex"));
			assertEquals(1, v.getAttribute("intVertex"));
			assertEquals(JGraLab.vector().plus(true).plus(false).plus(true),
					v.getAttribute("listVertex"));
			assertEquals(1l, v.getAttribute("longVertex"));
			assertEquals(
					JGraLab.map().plus(1, true).plus(2, false).plus(3, true),
					v.getAttribute("mapVertex"));
			assertEquals(
					RecordImpl
							.empty()
							.plus("boolRecord", true)
							.plus("doubleRecord", 1.1d)
							.plus("enumRecord", "FIRST")
							.plus("intRecord", 1)
							.plus("listRecord",
									JGraLab.vector().plus(true).plus(false)
											.plus(true))
							.plus("longRecord", 1l)
							.plus("mapRecord",
									JGraLab.map().plus(1, true).plus(2, false)
											.plus(3, true))
							.plus("setRecord",
									JGraLab.set().plus(true).plus(false))
							.plus("stringRecord", "test"),
					v.getAttribute("recordVertex"));
			assertEquals(JGraLab.set().plus(true).plus(false),
					v.getAttribute("setVertex"));
			assertEquals("test", v.getAttribute("stringVertex"));

			// changing values
			v.setAttribute("boolVertex", false);
			assertEquals(false, v.getAttribute("boolVertex"));
			v.setAttribute(
					"complexListVertex",
					JGraLab.vector().plus(JGraLab.vector().plus(false))
							.plus(JGraLab.vector().plus(false)));
			assertEquals(JGraLab.vector().plus(JGraLab.vector().plus(false))
					.plus(JGraLab.vector().plus(false)),
					v.getAttribute("complexListVertex"));
			v.setAttribute(
					"complexMapVertex",
					JGraLab.map()
							.plus(JGraLab.vector().plus(true).plus(false),
									JGraLab.set().plus(false))
							.plus(JGraLab.vector().plus(false).plus(true),
									JGraLab.set().plus(true)));
			assertEquals(
					JGraLab.map()
							.plus(JGraLab.vector().plus(true).plus(false),
									JGraLab.set().plus(false))
							.plus(JGraLab.vector().plus(false).plus(true),
									JGraLab.set().plus(true)),
					v.getAttribute("complexMapVertex"));
			v.setAttribute("complexSetVertex",
					JGraLab.set().plus(JGraLab.set().plus(false)));
			assertEquals(JGraLab.set().plus(JGraLab.set().plus(false)),
					v.getAttribute("complexSetVertex"));
			v.setAttribute("doubleVertex", 2.2d);
			assertEquals(2.2d, v.getAttribute("doubleVertex"));
			v.setAttribute("enumVertex", "SECOND");
			assertEquals("SECOND", v.getAttribute("enumVertex"));
			v.setAttribute("intVertex", 42);
			assertEquals(42, v.getAttribute("intVertex"));
			v.setAttribute("listVertex",
					JGraLab.vector().plus(false).plus(false).plus(true));
			assertEquals(JGraLab.vector().plus(false).plus(false).plus(true),
					v.getAttribute("listVertex"));
			v.setAttribute("longVertex", 987654321l);
			assertEquals(987654321l, v.getAttribute("longVertex"));
			v.setAttribute("mapVertex",
					JGraLab.map().plus(42, true).plus(24, false));
			assertEquals(JGraLab.map().plus(42, true).plus(24, false),
					v.getAttribute("mapVertex"));
			v.setAttribute(
					"recordVertex",
					RecordImpl
							.empty()
							.plus("boolRecord", false)
							.plus("doubleRecord", 1.3d)
							.plus("enumRecord", "THIRD")
							.plus("intRecord", 42)
							.plus("listRecord",
									JGraLab.vector().plus(false).plus(true)
											.plus(false))
							.plus("longRecord", 987654321l)
							.plus("mapRecord",
									JGraLab.map().plus(42, true)
											.plus(24, false))
							.plus("setRecord", JGraLab.set().plus(false))
							.plus("stringRecord", "more test"));
			assertEquals(
					RecordImpl
							.empty()
							.plus("boolRecord", false)
							.plus("doubleRecord", 1.3d)
							.plus("enumRecord", "THIRD")
							.plus("intRecord", 42)
							.plus("listRecord",
									JGraLab.vector().plus(false).plus(true)
											.plus(false))
							.plus("longRecord", 987654321l)
							.plus("mapRecord",
									JGraLab.map().plus(42, true)
											.plus(24, false))
							.plus("setRecord", JGraLab.set().plus(false))
							.plus("stringRecord", "more test"),
					v.getAttribute("recordVertex"));
			v.setAttribute("setVertex", JGraLab.set().plus(true));
			assertEquals(JGraLab.set().plus(true), v.getAttribute("setVertex"));
			v.setAttribute("stringVertex", "some String");
			assertEquals("some String", v.getAttribute("stringVertex"));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test setting an attribute that doesn't exist
	@Test(expected = GraphException.class)
	public void testAccessAttributesFailure1() {
		try {
			Schema s = GraphIO
					.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
							+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			Vertex v = g.createVertex(g.getGraphClass().getVertexClass(
					"TestVertex"));
			v.getAttribute("StringVertex"); // "stringVertex"!
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test(expected = GraphException.class)
	public void testAccessAttributesFailure2() {
		try {
			Schema s = GraphIO
					.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
							+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			Vertex v = g.createVertex(g.getGraphClass().getVertexClass(
					"TestVertex"));
			v.setAttribute("abcd", 123);
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test setting attributes with values that don't conform to their domain
	@Test(expected = ClassCastException.class)
	public void testAccessAttributesFailure3() {
		try {
			Schema s = GraphIO
					.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
							+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			Vertex v = g.createVertex(g.getGraphClass().getVertexClass(
					"TestVertex"));
			v.setAttribute("stringVertex", 42);
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test type specific getDegree Methods (VertexTestSchema.tg)
	@Test
	public void testGetDegree() {
		try {
			Schema s = GraphIO
					.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
							+ "VertexTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);

			Vertex[] vertices = new Vertex[6];
			vertices[0] = g.createVertex(g.getGraphClass().getVertexClass("A"));
			vertices[1] = g.createVertex(g.getGraphClass().getVertexClass("B"));
			vertices[2] = g.createVertex(g.getGraphClass().getVertexClass("C"));
			vertices[3] = g.createVertex(g.getGraphClass().getVertexClass("D"));
			vertices[4] = g
					.createVertex(g.getGraphClass().getVertexClass("C2"));
			vertices[5] = g
					.createVertex(g.getGraphClass().getVertexClass("D2"));

			EdgeClass[] edgeClasses = new EdgeClass[7];
			edgeClasses[0] = g.getGraphClass().getEdgeClass("E");
			edgeClasses[1] = g.getGraphClass().getEdgeClass("F");
			edgeClasses[2] = g.getGraphClass().getEdgeClass("G");
			edgeClasses[3] = g.getGraphClass().getEdgeClass("H");
			edgeClasses[4] = g.getGraphClass().getEdgeClass("I");
			edgeClasses[5] = g.getGraphClass().getEdgeClass("J");
			edgeClasses[6] = g.getGraphClass().getEdgeClass("K");

			assertEquals(0, vertices[0].getDegree(edgeClasses[0]));
			assertEquals(0,
					vertices[0].getDegree(edgeClasses[0], EdgeDirection.OUT));
			assertEquals(0, vertices[1].getDegree(edgeClasses[0]));
			assertEquals(0,
					vertices[1].getDegree(edgeClasses[0], EdgeDirection.IN));

			assertEquals(0, vertices[2].getDegree(edgeClasses[1]));
			assertEquals(0,
					vertices[2].getDegree(edgeClasses[1], EdgeDirection.OUT));
			assertEquals(0, vertices[3].getDegree(edgeClasses[1]));
			assertEquals(0,
					vertices[3].getDegree(edgeClasses[1], EdgeDirection.IN));

			g.createEdge(edgeClasses[0], vertices[0], vertices[1]);
			g.createEdge(edgeClasses[1], vertices[2], vertices[3]);
			g.createEdge(edgeClasses[2], vertices[2], vertices[3]);
			g.createEdge(edgeClasses[3], vertices[0], vertices[1]);
			g.createEdge(edgeClasses[4], vertices[0], vertices[0]);
			g.createEdge(edgeClasses[5], vertices[4], vertices[5]);
			g.createEdge(edgeClasses[6], vertices[0], vertices[1]);

			assertEquals(3, vertices[0].getDegree(edgeClasses[0]));
			assertEquals(3,
					vertices[0].getDegree(edgeClasses[0], EdgeDirection.OUT));
			assertEquals(3, vertices[1].getDegree(edgeClasses[0]));
			assertEquals(3,
					vertices[1].getDegree(edgeClasses[0], EdgeDirection.IN));

			assertEquals(1, vertices[2].getDegree(edgeClasses[1]));
			assertEquals(1,
					vertices[2].getDegree(edgeClasses[1], EdgeDirection.OUT));
			assertEquals(1, vertices[3].getDegree(edgeClasses[1]));
			assertEquals(1,
					vertices[3].getDegree(edgeClasses[1], EdgeDirection.IN));

			assertEquals(1, vertices[2].getDegree(edgeClasses[2]));
			assertEquals(1,
					vertices[2].getDegree(edgeClasses[2], EdgeDirection.OUT));
			assertEquals(1, vertices[3].getDegree(edgeClasses[2]));
			assertEquals(1,
					vertices[3].getDegree(edgeClasses[2], EdgeDirection.IN));

			assertEquals(2, vertices[0].getDegree(edgeClasses[3]));
			assertEquals(2,
					vertices[0].getDegree(edgeClasses[3], EdgeDirection.OUT));
			assertEquals(2, vertices[1].getDegree(edgeClasses[3]));
			assertEquals(2,
					vertices[1].getDegree(edgeClasses[3], EdgeDirection.IN));

			assertEquals(2, vertices[0].getDegree(edgeClasses[4]));
			assertEquals(1,
					vertices[0].getDegree(edgeClasses[4], EdgeDirection.OUT));
			assertEquals(1,
					vertices[0].getDegree(edgeClasses[4], EdgeDirection.IN));

			assertEquals(1, vertices[4].getDegree(edgeClasses[5]));
			assertEquals(1,
					vertices[4].getDegree(edgeClasses[5], EdgeDirection.OUT));
			assertEquals(1, vertices[5].getDegree(edgeClasses[5]));
			assertEquals(1,
					vertices[5].getDegree(edgeClasses[5], EdgeDirection.IN));

			assertEquals(1, vertices[0].getDegree(edgeClasses[6]));
			assertEquals(1,
					vertices[0].getDegree(edgeClasses[6], EdgeDirection.OUT));
			assertEquals(1, vertices[1].getDegree(edgeClasses[6]));
			assertEquals(1,
					vertices[1].getDegree(edgeClasses[6], EdgeDirection.IN));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test type specific getNextVertex method
	@Test
	public void testGetNextVertex() {
		try {
			Schema s = GraphIO
					.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
							+ "VertexTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);

			VertexClass[] vClasses = new VertexClass[6];
			vClasses[0] = g.getGraphClass().getVertexClass("A");
			vClasses[1] = g.getGraphClass().getVertexClass("B");
			vClasses[2] = g.getGraphClass().getVertexClass("C"); // C: A
			vClasses[3] = g.getGraphClass().getVertexClass("D"); // D: B
			vClasses[4] = g.getGraphClass().getVertexClass("C2"); // C2: C
			vClasses[5] = g.getGraphClass().getVertexClass("D2"); // D2: D

			Vertex[] vertices = new Vertex[6];
			// Order of created vertex types: A B C D C2 D2
			vertices[0] = g.createVertex(vClasses[0]);
			assertEquals(null, vertices[0].getNextVertex(vClasses[0]));
			vertices[1] = g.createVertex(vClasses[1]);
			vertices[2] = g.createVertex(vClasses[2]);
			vertices[3] = g.createVertex(vClasses[3]);
			vertices[4] = g.createVertex(vClasses[4]);
			vertices[5] = g.createVertex(vClasses[5]);

			assertEquals(vertices[2], vertices[0].getNextVertex(vClasses[0]));
			assertEquals(vertices[1], vertices[0].getNextVertex(vClasses[1]));
			assertEquals(vertices[2], vertices[0].getNextVertex(vClasses[2]));
			assertEquals(vertices[3], vertices[0].getNextVertex(vClasses[3]));
			assertEquals(vertices[4], vertices[0].getNextVertex(vClasses[4]));
			assertEquals(vertices[2], vertices[1].getNextVertex(vClasses[0]));
			assertEquals(vertices[3], vertices[1].getNextVertex(vClasses[1]));
			assertEquals(vertices[2], vertices[1].getNextVertex(vClasses[2]));
			assertEquals(vertices[3], vertices[1].getNextVertex(vClasses[3]));
			assertEquals(vertices[5], vertices[1].getNextVertex(vClasses[5]));
			assertEquals(vertices[4], vertices[2].getNextVertex(vClasses[0]));
			assertEquals(vertices[3], vertices[2].getNextVertex(vClasses[1]));
			assertEquals(vertices[4], vertices[2].getNextVertex(vClasses[2]));
			assertEquals(vertices[3], vertices[2].getNextVertex(vClasses[3]));
			assertEquals(vertices[4], vertices[2].getNextVertex(vClasses[4]));
			assertEquals(vertices[5], vertices[2].getNextVertex(vClasses[5]));
			assertEquals(vertices[4], vertices[3].getNextVertex(vClasses[0]));
			assertEquals(vertices[5], vertices[3].getNextVertex(vClasses[1]));
			assertEquals(vertices[4], vertices[3].getNextVertex(vClasses[2]));
			assertEquals(vertices[5], vertices[3].getNextVertex(vClasses[3]));
			assertEquals(vertices[4], vertices[3].getNextVertex(vClasses[4]));
			assertEquals(vertices[5], vertices[3].getNextVertex(vClasses[5]));
			assertEquals(null, vertices[4].getNextVertex(vClasses[0]));
			assertEquals(vertices[5], vertices[4].getNextVertex(vClasses[1]));
			assertEquals(null, vertices[4].getNextVertex(vClasses[2]));
			assertEquals(vertices[5], vertices[4].getNextVertex(vClasses[3]));
			assertEquals(null, vertices[4].getNextVertex(vClasses[4]));
			assertEquals(vertices[5], vertices[4].getNextVertex(vClasses[5]));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test type specific getFirstIncidence Method
	@Test
	public void testGetFirstIncidence() {
		try {
			Schema s = GraphIO
					.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
							+ "VertexTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
	
			Vertex[] vertices = new Vertex[6];
			vertices[0] = g.createVertex(g.getGraphClass().getVertexClass("A"));
			vertices[1] = g.createVertex(g.getGraphClass().getVertexClass("B"));
			vertices[2] = g.createVertex(g.getGraphClass().getVertexClass("C"));
			vertices[3] = g.createVertex(g.getGraphClass().getVertexClass("D"));
			vertices[4] = g
					.createVertex(g.getGraphClass().getVertexClass("C2"));
			vertices[5] = g
					.createVertex(g.getGraphClass().getVertexClass("D2"));
	
			EdgeClass[] edgeClasses = new EdgeClass[7];
			edgeClasses[0] = g.getGraphClass().getEdgeClass("E");
			edgeClasses[1] = g.getGraphClass().getEdgeClass("F");
			edgeClasses[2] = g.getGraphClass().getEdgeClass("G");
			edgeClasses[3] = g.getGraphClass().getEdgeClass("H");
			edgeClasses[4] = g.getGraphClass().getEdgeClass("I");
			edgeClasses[5] = g.getGraphClass().getEdgeClass("J");
			edgeClasses[6] = g.getGraphClass().getEdgeClass("K");
			
			for(Vertex v : vertices) {
				for(EdgeClass ec : edgeClasses) {
					assertNull(v.getFirstIncidence(ec));
					assertNull(v.getFirstIncidence(ec, EdgeDirection.IN));
					assertNull(v.getFirstIncidence(ec, EdgeDirection.OUT));
				}
			}
			
			Edge[] edges = new Edge[7];
			edges[0] = g.createEdge(edgeClasses[0], vertices[0], vertices[1]);
			edges[1] = g.createEdge(edgeClasses[1], vertices[2], vertices[3]);
			edges[2] = g.createEdge(edgeClasses[2], vertices[2], vertices[3]);
			edges[3] = g.createEdge(edgeClasses[3], vertices[0], vertices[1]);
			edges[4] = g.createEdge(edgeClasses[4], vertices[0], vertices[0]);
			edges[5] = g.createEdge(edgeClasses[5], vertices[4], vertices[5]);
			edges[6] = g.createEdge(edgeClasses[6], vertices[0], vertices[1]);
			
			assertEquals(vertices[0].getFirstIncidence(edgeClasses[0]), edges[0]);
			assertEquals(vertices[0].getFirstIncidence(edgeClasses[6]), edges[6]);
			assertEquals(vertices[1].getFirstIncidence(edgeClasses[0]), edges[0].getReversedEdge());
			assertEquals(vertices[1].getFirstIncidence(edgeClasses[6]), edges[6].getReversedEdge());
			assertEquals(vertices[2].getFirstIncidence(edgeClasses[1]), edges[1]);
			assertEquals(vertices[3].getFirstIncidence(edgeClasses[1]), edges[1].getReversedEdge());
			assertEquals(vertices[4].getFirstIncidence(edgeClasses[5]), edges[5]);
			assertEquals(vertices[5].getFirstIncidence(edgeClasses[5]), edges[5].getReversedEdge());
			
			assertEquals(vertices[0].getFirstIncidence(edgeClasses[4], EdgeDirection.IN), edges[4].getReversedEdge());
			assertEquals(vertices[2].getFirstIncidence(edgeClasses[0], EdgeDirection.OUT), edges[1]);
		}
		catch(GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testIsValidAlpha() {
		try {
			Schema s = GraphIO
					.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
							+ "VertexTestSchema.tg");
			Graph graph = s.createGraph(ImplementationType.GENERIC);
			Vertex a = graph.createVertex(graph.getGraphClass().getVertexClass("A"));
			Vertex b = graph.createVertex(graph.getGraphClass().getVertexClass("B"));
			Vertex c = graph.createVertex(graph.getGraphClass().getVertexClass("C"));
			Vertex d = graph.createVertex(graph.getGraphClass().getVertexClass("D"));
			Vertex c2 = graph.createVertex(graph.getGraphClass().getVertexClass("C2"));
			Vertex d2 = graph.createVertex(graph.getGraphClass().getVertexClass("D2"));
			
			// TODO instantiate by means of Reflection!
			Edge e = graph.createEdge(graph.getGraphClass().getEdgeClass("E"), a, b);
			Edge f = graph.createEdge(graph.getGraphClass().getEdgeClass("F"), c, d);
			Edge g = graph.createEdge(graph.getGraphClass().getEdgeClass("G"), c, d);
			Edge h = graph.createEdge(graph.getGraphClass().getEdgeClass("H"), a, b);
			Edge i = graph.createEdge(graph.getGraphClass().getEdgeClass("I"), a, a);
			Edge j = graph.createEdge(graph.getGraphClass().getEdgeClass("J"), c2, d2);
			Edge k = graph.createEdge(graph.getGraphClass().getEdgeClass("K"), a, b);
			
			assertTrue(a.isValidAlpha(e));
			assertTrue(c.isValidAlpha(f));
			assertTrue(c.isValidAlpha(g));
			assertTrue(a.isValidAlpha(h));
			assertTrue(a.isValidAlpha(i));
			assertTrue(c2.isValidAlpha(j));
			assertTrue(c2.isValidAlpha(k));
			assertTrue(c2.isValidAlpha(e));
		}
		catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testIsValidOmega() {
		try {
			Schema s = GraphIO
					.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
							+ "VertexTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			// TODO
		}
		catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
