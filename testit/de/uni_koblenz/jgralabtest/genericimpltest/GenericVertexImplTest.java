package de.uni_koblenz.jgralabtest.genericimpltest;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.RecordImpl;
import de.uni_koblenz.jgralab.schema.Schema;

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
			v.getAttribute("StringVertex");	// "stringVertex"!
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

	@Test
	public void testGetDegree() {
		try {
			Schema s = GraphIO
					.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
							+ "VertexTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			Vertex a = g.createVertex(g.getGraphClass().getVertexClass("A"));
			Vertex b = g.createVertex(g.getGraphClass().getVertexClass("B"));
			Vertex c = g.createVertex(g.getGraphClass().getVertexClass("C"));
			Vertex d = g.createVertex(g.getGraphClass().getVertexClass("D"));
			Vertex c2 = g.createVertex(g.getGraphClass().getVertexClass("C2"));
			Vertex d2 = g.createVertex(g.getGraphClass().getVertexClass("D2"));
			
			Edge e = g.createEdge(g.getGraphClass().getEdgeClass("E"), a, b);
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetNextVertex() {
		// TODO
	}

	@Test
	public void testGetFirstIncidence() {
		// TODO
	}

	@Test
	public void testIsValidAlpha() {
		// TODO
	}

	@Test
	public void testIsValidOmega() {

	}
}
