package de.uni_koblenz.jgralabtest.genericimpltest;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Test;

import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.impl.generic.*;
import de.uni_koblenz.jgralab.schema.*;

public class GenericGraphImplTest {

	private static final String SCHEMAFOLDER = "testit" + File.separator
			+ "testschemas" + File.separator;
	private static final String GRAPHFOLDER = "testit" + File.separator
			+ "testgraphs" + File.separator;
	private static final String DATAFOLDER = "testit" + File.separator
			+ "testdata" + File.separator;

	/**
	 * Tests, if an graph/vertex/edge of a generic <code>Graph</code> contains
	 * all its attributes, as defined by the corresponding
	 * {@link AttributedElementClass} in the <code>Graph</code>'s
	 * <code>Schema</code>.
	 * 
	 * @param testObject
	 *            A {@link GenericGraphImpl}, {@link GenericVertexImpl} or
	 *            {@link GenericEdgeImpl} <code>Object</code>.
	 * @param aec
	 *            The element of the <code>Schema</code>, representing the
	 *            tested.
	 */
	private void testElementAttributes(Object testObject,
			AttributedElementClass aec) {
		try {
			Field f = testObject.getClass().getDeclaredField("attributes");
			f.setAccessible(true);
			Map<?, ?> attributes = (Map<?, ?>) f.get(testObject);
			if (attributes != null) {
				assertEquals(aec.getAttributeCount(), attributes.size());
				for (Attribute a : aec.getAttributeList()) {
					assertTrue(attributes.containsKey(a.getName()));
				}
			} else {
				assertEquals(0, aec.getAttributeCount());
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			fail();
		} catch (SecurityException e) {
			e.printStackTrace();
			fail();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Tests, if the value of an attribute in the generic TGraph implementation
	 * has the default value defined in its definition in the schema. If no
	 * explicit default value was defined, it tests, if the attribute's value
	 * corresponds to the general default value of its Domain.
	 * 
	 * @param value
	 * @param attribute
	 */
	private void testDefaultValue(Object value, Attribute attribute) {
		try {
			if (attribute.getDefaultValueAsString() != null) {
				Object expected = attribute.getDomain().parseGenericAttribute(
						GraphIO.createStringReader(attribute
								.getDefaultValueAsString(), attribute
								.getAttributedElementClass().getSchema()));
				assertEquals(expected, value);
			} else {
				assertEquals(GenericGraphImpl.genericAttributeDefaultValue(attribute
						.getDomain()), value);
			}
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test creating a small graph without attributes (MinimalSchema.tg).
	@Test
	public void testCreateGraph1() {
		try {
			Schema schema;
			schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "MinimalSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			assertTrue(g instanceof GenericGraphImpl);
			assertEquals(schema.getGraphClass(), g.getAttributedElementClass());
			testElementAttributes(g, schema.getGraphClass());
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test creating a graph with attributes that have explicitly defined
	// default values in the schema (DefaultValueSchema.tg)
	@Test
	public void testCreateGraph2() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			assertTrue(g instanceof GenericGraphImpl);
			assertEquals(schema.getGraphClass(), g.getAttributedElementClass());
			testElementAttributes(g, schema.getGraphClass());

			// Test if default values were set correctly
			assertEquals(true, g.getAttribute("boolGraph"));
			assertEquals(
					JGraLab.vector().plus(JGraLab.vector().plus(true))
							.plus(JGraLab.vector().plus(false))
							.plus(JGraLab.vector().plus(true)),
					g.getAttribute("complexListGraph"));
			assertEquals(
					JGraLab.map()
							.plus(JGraLab.vector().plus(true),
									JGraLab.set().plus(true))
							.plus(JGraLab.vector().plus(false),
									JGraLab.set().plus(false)),
					g.getAttribute("complexMapGraph"));
			assertEquals(
					JGraLab.set().plus(JGraLab.set().plus(true))
							.plus(JGraLab.set().plus(false)),
					g.getAttribute("complexSetGraph"));
			assertEquals(new Double(1.1), g.getAttribute("doubleGraph"));
			assertEquals("FIRST", g.getAttribute("enumGraph"));
			assertEquals(new Integer(1), g.getAttribute("intGraph"));
			assertEquals(JGraLab.vector().plus(true).plus(false).plus(true),
					g.getAttribute("listGraph"));
			assertEquals(new Long(1), g.getAttribute("longGraph"));
			assertEquals(
					JGraLab.map().plus(1, true).plus(2, false).plus(3, true),
					g.getAttribute("mapGraph"));
			assertEquals(JGraLab.set().plus(true).plus(false),
					g.getAttribute("setGraph"));
			assertEquals("test", g.getAttribute("stringGraph"));
			assertEquals(
					de.uni_koblenz.jgralab.impl.RecordImpl
							.empty()
							.plus("boolRecord", true)
							.plus("doubleRecord", new Double(1.1))
							.plus("enumRecord", "FIRST")
							.plus("intRecord", new Integer(1))
							.plus("listRecord",
									JGraLab.vector().plus(true).plus(false)
											.plus(true))
							.plus("longRecord", new Long(1))
							.plus("mapRecord",
									JGraLab.map().plus(1, true).plus(2, false)
											.plus(3, true))
							.plus("setRecord",
									JGraLab.set().plus(true).plus(false))
							.plus("stringRecord", "test"),
					g.getAttribute("recordGraph"));
			// Dynamic test to see if default values were set correctly
			for (Attribute a : schema.getGraphClass().getAttributeList()) {
				testDefaultValue(g.getAttribute(a.getName()), a);
			}
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Creating generic vertices without attributes (MinimalSchema.tg - Node)
	@Test
	public void testCreateVertex1() {
		try {
			Schema schema;
			schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "MinimalSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);

			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
					"Node"));
			assertTrue(v1 instanceof GenericVertexImpl);
			testElementAttributes(v1,
					schema.getGraphClass().getVertexClass("Node"));

			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
					"Node"));
			assertTrue(v2 instanceof GenericVertexImpl);
			testElementAttributes(v2,
					schema.getGraphClass().getVertexClass("Node"));

			assertTrue(g.containsVertex(v1));
			assertTrue(g.containsVertex(v2));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Create generic vertices with attributes (inherited ones and own ones)
	// (DefaultValueTestSchema.tg)
	@Test
	public void testCreateVertex2() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
					"TestVertex"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
					"TestSubVertex")); // Vertex with inherited attributes

			assertEquals(schema.getGraphClass().getVertexClass("TestVertex"),
					v1.getAttributedElementClass());
			assertEquals(
					schema.getGraphClass().getVertexClass("TestSubVertex"),
					v2.getAttributedElementClass());
			testElementAttributes(v1, v1.getAttributedElementClass());
			testElementAttributes(v2, v2.getAttributedElementClass());
			for (Attribute a : schema.getGraphClass()
					.getVertexClass("TestVertex").getAttributeList()) {
				testDefaultValue(v1.getAttribute(a.getName()), a);
			}
			for (Attribute a : schema.getGraphClass()
					.getVertexClass("TestSubVertex").getAttributeList()) {
				testDefaultValue(v2.getAttribute(a.getName()), a);
			}
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Create generic edges (1 attribute) (MinialSchema.tg)
	@Test
	public void testCreateEdge1() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "MinimalSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
					"Node"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
					"Node"));
			Edge e1 = g.createEdge(schema.getGraphClass().getEdgeClass("Link"),
					v1, v2);
			Edge e2 = g.createEdge(schema.getGraphClass().getEdgeClass("Link"),
					v2, v1);

			assertEquals(g.getSchema().getGraphClass().getEdgeClass("Link"),
					e1.getAttributedElementClass());
			assertEquals(g.getSchema().getGraphClass().getEdgeClass("Link"),
					e2.getAttributedElementClass());
			assertTrue(g.containsEdge(e1));
			assertTrue(g.containsEdge(e2));
			testElementAttributes(e1,
					schema.getGraphClass().getEdgeClass("Link"));
			testElementAttributes(e2,
					schema.getGraphClass().getEdgeClass("Link"));
			for (Attribute a : e1.getAttributedElementClass()
					.getAttributeList()) {
				testDefaultValue(e1.getAttribute(a.getName()), a);
			}
			for (Attribute a : e2.getAttributedElementClass()
					.getAttributeList()) {
				testDefaultValue(e1.getAttribute(a.getName()), a);
			}

			assertEquals(e1, v1.getFirstIncidence());
			assertEquals(e2, e1.getNextEdge());
			assertEquals(e2.getReversedEdge(), e1.getNextIncidence());
			assertEquals(e1.getReversedEdge(), v2.getFirstIncidence());
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Create generic edges with inherited attributes (VertexTestSchema.tg)
	@Test
	public void testCreateEdge2() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "VertexTestSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
					"SubNode"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
					"SuperNode"));
			Vertex v3 = g.createVertex(schema.getGraphClass().getVertexClass(
					"DoubleSubNode"));

			Edge e1 = g.createEdge(schema.getGraphClass().getEdgeClass("Link"),
					v1, v2);
			assertEquals(schema.getGraphClass().getEdgeClass("Link"),
					e1.getAttributedElementClass());
			testElementAttributes(e1, e1.getAttributedElementClass());
			testDefaultValue(e1.getAttribute("aString"), e1
					.getAttributedElementClass().getAttribute("aString"));

			// Edge with inherited attributes
			Edge e2 = g.createEdge(
					schema.getGraphClass().getEdgeClass("SubLink"), v3, v2);
			assertEquals(schema.getGraphClass().getEdgeClass("SubLink"),
					e2.getAttributedElementClass());
			testElementAttributes(e2, e2.getAttributedElementClass());
			testDefaultValue(e2.getAttribute("anInt"), e2
					.getAttributedElementClass().getAttribute("anInt"));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// EdgeClass is from a different schema
	@Test(expected = GraphException.class)
	public void testCreateEdgeFailure1() {
		Graph g1 = null;
		try {
			// both schemas contain an EdgeClass named "Link"
			Schema schema1 = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "MinimalSchema.tg");
			Schema schema2 = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "jnitestschema.tg");

			g1 = schema1.createGraph(ImplementationType.GENERIC, 100, 100);
			Vertex v1 = g1.createVertex(schema1.getGraphClass().getVertexClass(
					"Node"));
			Vertex v2 = g1.createVertex(schema1.getGraphClass().getVertexClass(
					"Node"));
			// Error: EdgeClass is from schema2
			g1.createEdge(schema2.getGraphClass().getEdgeClass("Link"), v1, v2);
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		} catch (GraphException e) {
			// Test if there has no edge been added to the graph
			if (0 == g1.getECount()) {
				throw e;
			}
		}
	}

	// EdgeClass is not defined between the connected nodes' VertexClasses
	@Test(expected = GraphException.class)
	public void testCreateEdgeFailure2() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "greqltestschema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
					"localities.Village"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
					"localities.Village"));

			g.createEdge(
					schema.getGraphClass().getEdgeClass("connections.Street"),
					v1, v2);
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// The IncidenceClass is redefined and therefore not allowed
	// (VertexTestSChema.tg)
	@Test(expected = GraphException.class)
	public void testCreateEdgeFailure3() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "VertexTestSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
					"C2"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
					"D2"));

			// this Edge should not be allowed => GraphException
			g.createEdge(schema.getGraphClass().getEdgeClass("E"), v1, v2);
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// VertexClass is from a different Schema
	@Test(expected = GraphException.class)
	public void testCreateVertexFailure1() {
		Graph g1 = null;
		try {
			Schema citimapschema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "citymapschema.tg");
			Schema greqltestschema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "greqltestschema.tg");

			g1 = citimapschema
					.createGraph(ImplementationType.GENERIC, 100, 100);

			g1.createVertex(greqltestschema.getGraphClass().getVertexClass(
					"junctions.Crossroad"));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		} catch (GraphException e) {
			if (0 == g1.getVCount()) {
				throw e;
			}
		}
	}

	// Test setting a TraversalContext
	public void testSetTraversalContext() {
		// TODO! ?
	}

	// Test setting and accessing a graph's attributes
	@Test
	public void testAccessAttributes1() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);

		testDefaultValue(g.getAttribute("boolGraph"), g.getGraphClass()
				.getAttribute("boolGraph"));
		g.setAttribute("boolGraph", false);
		assertEquals(false, g.getAttribute("boolGraph"));

		testDefaultValue(g.getAttribute("listGraph"), g.getGraphClass()
				.getAttribute("listGraph"));
		g.setAttribute("listGraph", JGraLab.vector().plus(true).plus(true)
				.plus(false));
		assertEquals(JGraLab.vector().plus(true).plus(true).plus(false),
				g.getAttribute("listGraph"));

		g.setAttribute("listGraph", null);
		assertEquals(null, g.getAttribute("listGraph"));

		g.setAttribute(
				"complexListGraph",
				JGraLab.vector().plus(JGraLab.vector().plus(true))
						.plus(JGraLab.vector().plus(false))
						.plus(JGraLab.vector().plus(false)));
		assertEquals(
				JGraLab.vector().plus(JGraLab.vector().plus(true))
						.plus(JGraLab.vector().plus(false))
						.plus(JGraLab.vector().plus(false)),
				g.getAttribute("complexListGraph"));
	}

	// Test setting attributes that don't exist. NoSuchAttributeException is
	// expected.
	@Test(expected = NoSuchAttributeException.class)
	public void testAccessAttributesFailure1() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);

		g.setAttribute("doesNotExist", true);
	}

	@Test(expected = NoSuchAttributeException.class)
	public void testAccessAttributesFailure2() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);

		g.setAttribute("boolgraph", true); // the actual attribute's name is
											// written in CamelCase
	}

	// Test setting attribute values of a wrong domain. A ClassCastException is
	// expected.
	@Test(expected = ClassCastException.class)
	public void testAccessAttributesFailure3() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);

		g.setAttribute("boolGraph", JGraLab.set().plus(1));
	}

	// Test setting attribute values of a wrong domain. A ClassCastException is
	// expected.
	@Test(expected = ClassCastException.class)
	public void testAccessAttributesFailure4() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);

		g.setAttribute("boolGraph", null);
	}

	// Test type-specific iteration over vertices (VertexTestSchema.tg)
	// This test requires the type-specific getNextVertex()/getNextEdge()
	// methods of the GenericVertexImpl/EdgeImpl classes, as the iterators
	// use them.
	@Test
	public void testVertexIteration() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "VertexTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC, 100, 100);

			Vertex v1 = g.createVertex(s.getGraphClass().getVertexClass("A"));
			Vertex v2 = g.createVertex(s.getGraphClass().getVertexClass("A"));
			Vertex v3 = g.createVertex(s.getGraphClass().getVertexClass("B"));
			Vertex v4 = g.createVertex(s.getGraphClass().getVertexClass("B"));
			Vertex v5 = g.createVertex(s.getGraphClass().getVertexClass("C"));
			Vertex v6 = g.createVertex(s.getGraphClass().getVertexClass("C"));
			Vertex v7 = g.createVertex(s.getGraphClass().getVertexClass("D"));
			Vertex v8 = g.createVertex(s.getGraphClass().getVertexClass("D"));
			Vertex v9 = g.createVertex(s.getGraphClass().getVertexClass("C2"));
			Vertex v10 = g.createVertex(s.getGraphClass().getVertexClass("C2"));

			assertNull(g.getFirstVertex(s.getGraphClass().getVertexClass("D2")));
			assertEquals(v1,
					g.getFirstVertex(s.getGraphClass().getVertexClass("A")));
			assertEquals(v3,
					g.getFirstVertex(s.getGraphClass().getVertexClass("B")));
			assertEquals(v5,
					g.getFirstVertex(s.getGraphClass().getVertexClass("C")));
			assertEquals(v7,
					g.getFirstVertex(s.getGraphClass().getVertexClass("D")));
			assertEquals(v9,
					g.getFirstVertex(s.getGraphClass().getVertexClass("C2")));

			Vertex v11 = g.createVertex(s.getGraphClass().getVertexClass("D2"));
			Vertex v12 = g.createVertex(s.getGraphClass().getVertexClass("D2"));

			Vertex[] aTest = new Vertex[] { v1, v2, v5, v6, v9, v10 };
			Vertex[] bTest = new Vertex[] { v3, v4, v7, v8, v11, v12 };
			Vertex[] cTest = new Vertex[] { v5, v6, v9, v10 };
			Vertex[] dTest = new Vertex[] { v7, v8, v11, v12 };
			Vertex[] c2Test = new Vertex[] { v9, v10 };
			Vertex[] d2Test = new Vertex[] { v11, v12 };

			int i = 0;
			for (Vertex v : g.vertices(s.getGraphClass().getVertexClass("A"))) {
				assertEquals(aTest[i], v);
				i++;
			}

			i = 0;
			for (Vertex v : g.vertices(s.getGraphClass().getVertexClass("B"))) {
				assertEquals(bTest[i], v);
				i++;
			}

			i = 0;
			for (Vertex v : g.vertices(s.getGraphClass().getVertexClass("C"))) {
				assertEquals(cTest[i], v);
				i++;
			}

			i = 0;
			for (Vertex v : g.vertices(s.getGraphClass().getVertexClass("D"))) {
				assertEquals(dTest[i], v);
				i++;
			}

			i = 0;
			for (Vertex v : g.vertices(s.getGraphClass().getVertexClass("C2"))) {
				assertEquals(c2Test[i], v);
				i++;
			}

			i = 0;
			for (Vertex v : g.vertices(s.getGraphClass().getVertexClass("D2"))) {
				assertEquals(d2Test[i], v);
				i++;
			}
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test type-specific iteration over edges (VertexTestSchema.tg)
	@Test
	public void testEdgeIteration() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "VertexTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC, 100, 100);

			Vertex v1 = g.createVertex(s.getGraphClass().getVertexClass("A"));
			Vertex v2 = g.createVertex(s.getGraphClass().getVertexClass("A"));
			Vertex v3 = g.createVertex(s.getGraphClass().getVertexClass("B"));
			Vertex v4 = g.createVertex(s.getGraphClass().getVertexClass("B"));
			Vertex v5 = g.createVertex(s.getGraphClass().getVertexClass("C"));
			Vertex v6 = g.createVertex(s.getGraphClass().getVertexClass("C"));
			Vertex v7 = g.createVertex(s.getGraphClass().getVertexClass("D"));
			Vertex v8 = g.createVertex(s.getGraphClass().getVertexClass("D"));

			Edge e1 = g.createEdge(s.getGraphClass().getEdgeClass("E"), v1, v3);
			Edge e2 = g.createEdge(s.getGraphClass().getEdgeClass("E"), v2, v4);
			Edge e3 = g.createEdge(s.getGraphClass().getEdgeClass("F"), v5, v7);
			Edge e4 = g.createEdge(s.getGraphClass().getEdgeClass("F"), v6, v8);

			assertNull(g.getFirstEdge(s.getGraphClass().getEdgeClass("G")));
			assertNull(g.getFirstEdge(s.getGraphClass().getEdgeClass("H")));
			assertNull(g.getFirstEdge(s.getGraphClass().getEdgeClass("I")));
			assertNull(g.getFirstEdge(s.getGraphClass().getEdgeClass("J")));
			assertNull(g.getFirstEdge(s.getGraphClass().getEdgeClass("K")));

			Edge e5 = g.createEdge(s.getGraphClass().getEdgeClass("G"), v5, v8);
			Edge e6 = g.createEdge(s.getGraphClass().getEdgeClass("G"), v6, v7);
			Edge e7 = g.createEdge(s.getGraphClass().getEdgeClass("H"), v1, v7);
			Edge e8 = g.createEdge(s.getGraphClass().getEdgeClass("H"), v2, v8);

			Edge[] eTest = new Edge[] { e1, e2, e3, e4, e5, e6, e7, e8 };
			Edge[] fTest = new Edge[] { e3, e4 };
			Edge[] gTest = new Edge[] { e5, e6 };
			Edge[] hTest = new Edge[] { e7, e8 };

			int i = 0;
			for (Edge e : g.edges(s.getGraphClass().getEdgeClass("E"))) {
				assertEquals(eTest[i], e);
				i++;
			}
			i = 0;
			for (Edge e : g.edges(s.getGraphClass().getEdgeClass("F"))) {
				assertEquals(fTest[i], e);
				i++;
			}
			i = 0;
			for (Edge e : g.edges(s.getGraphClass().getEdgeClass("G"))) {
				assertEquals(gTest[i], e);
				i++;
			}
			i = 0;
			for (Edge e : g.edges(s.getGraphClass().getEdgeClass("H"))) {
				assertEquals(hTest[i], e);
				i++;
			}
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSave1() {

		try {
			Schema schema;
			schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "MinimalSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
					"Node"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
					"Node"));

			g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v1, v2);
			g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v2, v1);
			g.save("testit" + File.separator + "testdata" + File.separator
					+ "GenericTestGraph1.tg");
			// TODO How to test saved file?
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test loading a graph (tested statically against a saved graph)
	@Test
	public void testLoadGraph1() {
		// Static test relies on an unchanged graph in the file!
		try {
			Graph g = GraphIO.loadGraphFromFile(
					GRAPHFOLDER + "citymapgraph.tg", null, null,
					ImplementationType.GENERIC);
			assertEquals(8, g.getVCount());
			assertEquals(11, g.getECount());

			// Check types
			VertexClass intersection = g.getSchema().getGraphClass()
					.getVertexClass("Intersection");
			VertexClass carPark = g.getSchema().getGraphClass()
					.getVertexClass("CarPark");
			EdgeClass street = g.getSchema().getGraphClass()
					.getEdgeClass("Street");
			EdgeClass bridge = g.getSchema().getGraphClass()
					.getEdgeClass("Bridge");
			Vertex[] vertices = new Vertex[8];
			for (int i = 0; i < 8; i++) {
				vertices[i] = g.getVertex(i + 1);
			}
			Edge[] edges = new Edge[11];
			for (int i = 0; i < 11; i++) {
				edges[i] = g.getEdge(i + 1);
			}

			assertEquals(vertices[0].getAttributedElementClass(), intersection);
			assertEquals(vertices[1].getAttributedElementClass(), intersection);
			assertEquals(vertices[2].getAttributedElementClass(), carPark);
			assertEquals(vertices[3].getAttributedElementClass(), intersection);
			assertEquals(vertices[4].getAttributedElementClass(), intersection);
			assertEquals(vertices[5].getAttributedElementClass(), intersection);
			assertEquals(vertices[6].getAttributedElementClass(), carPark);
			assertEquals(vertices[7].getAttributedElementClass(), carPark);

			assertEquals(edges[0].getAttributedElementClass(), street);
			assertEquals(edges[1].getAttributedElementClass(), street);
			assertEquals(edges[2].getAttributedElementClass(), street);
			assertEquals(edges[3].getAttributedElementClass(), street);
			assertEquals(edges[4].getAttributedElementClass(), bridge);
			assertEquals(edges[5].getAttributedElementClass(), street);
			assertEquals(edges[6].getAttributedElementClass(), street);
			assertEquals(edges[7].getAttributedElementClass(), street);
			assertEquals(edges[8].getAttributedElementClass(), bridge);
			assertEquals(edges[9].getAttributedElementClass(), bridge);
			assertEquals(edges[10].getAttributedElementClass(), street);

			// Check attribute values
			assertFalse((Boolean) vertices[0].getAttribute("roundabout"));
			assertFalse((Boolean) vertices[1].getAttribute("roundabout"));
			assertFalse((Boolean) vertices[3].getAttribute("roundabout"));
			assertFalse((Boolean) vertices[4].getAttribute("roundabout"));
			assertFalse((Boolean) vertices[5].getAttribute("roundabout"));
			for (Vertex v : vertices) {
				assertEquals(null, v.getAttribute("name"));
			}
			assertEquals(new Integer(2500),
					vertices[2].getAttribute("capacity"));
			assertEquals(new Integer(500), vertices[6].getAttribute("capacity"));
			assertEquals(new Integer(500), vertices[7].getAttribute("capacity"));

			for (int i = 0; i < edges.length; i++) {
				assertFalse((Boolean) edges[i].getAttribute("oneway"));
				assertEquals("e" + (i + 1), edges[i].getAttribute("name"));
				assertEquals(0, edges[i].getAttribute("length"));
			}
			assertEquals(0, edges[4].getAttribute("height"));
			assertEquals(0, edges[8].getAttribute("height"));
			assertEquals(0, edges[9].getAttribute("height"));

			// Check incidences
			assertEquals(2, vertices[0].getDegree());
			assertEquals(edges[0], vertices[0].getFirstIncidence());
			assertEquals(edges[2], vertices[0].getLastIncidence());
			assertEquals(3, vertices[1].getDegree());
			assertEquals(edges[0].getReversedEdge(),
					vertices[1].getFirstIncidence());
			assertEquals(edges[1], vertices[1].getFirstIncidence()
					.getNextIncidence());
			assertEquals(edges[3].getReversedEdge(),
					vertices[1].getLastIncidence());
			assertEquals(2, vertices[2].getDegree());
			assertEquals(edges[1].getReversedEdge(),
					vertices[2].getFirstIncidence());
			assertEquals(edges[4], vertices[2].getLastIncidence());
			assertEquals(3, vertices[3].getDegree());
			assertEquals(edges[2].getReversedEdge(),
					vertices[3].getFirstIncidence());
			assertEquals(edges[5], vertices[3].getFirstIncidence()
					.getNextIncidence());
			assertEquals(edges[7], vertices[3].getLastIncidence());
			assertEquals(4, vertices[4].getDegree());
			assertEquals(edges[3], vertices[4].getFirstIncidence());
			assertEquals(edges[5].getReversedEdge(), vertices[4]
					.getFirstIncidence().getNextIncidence());
			assertEquals(edges[6], vertices[4].getLastIncidence()
					.getPrevIncidence());
			assertEquals(edges[8], vertices[4].getLastIncidence());
			assertEquals(3, vertices[5].getDegree());
			assertEquals(edges[4].getReversedEdge(),
					vertices[5].getFirstIncidence());
			assertEquals(edges[6].getReversedEdge(), vertices[5]
					.getFirstIncidence().getNextIncidence());
			assertEquals(edges[9], vertices[5].getLastIncidence());
			assertEquals(2, vertices[6].getDegree());
			assertEquals(edges[7].getReversedEdge(),
					vertices[6].getFirstIncidence());
			assertEquals(edges[10], vertices[6].getLastIncidence());
			assertEquals(3, vertices[7].getDegree());
			assertEquals(edges[8].getReversedEdge(),
					vertices[7].getFirstIncidence());
			assertEquals(edges[9].getReversedEdge(), vertices[7]
					.getFirstIncidence().getNextIncidence());
			assertEquals(edges[10].getReversedEdge(),
					vertices[7].getLastIncidence());
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test loading the greqltestgraph.tg-file
	@Test
	public void testLoadGraph2() {
		try {
			Graph g = GraphIO.loadGraphFromFile(GRAPHFOLDER
					+ "greqltestgraph.tg", null, null,
					ImplementationType.GENERIC);
			Schema s = g.getSchema();
			assertEquals(s.getGraphClass(), g.getAttributedElementClass());
			// TODO
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test creating, saving and loading a random graph
	@Test
	public void testSaveLoadRandomGraph() {
		// TODO
	}

}
