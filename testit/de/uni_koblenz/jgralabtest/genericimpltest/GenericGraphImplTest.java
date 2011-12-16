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
	
	private static final String SCHEMAFOLDER = "testit" + File.separator + "testschemas" + File.separator;
	private static final String GRAPHFOLDER = "testit" + File.separator + "testgraphs" + File.separator;
	private static final String DATAFOLDER = "testit" + File.separator + "testdata" + File.separator;
	
	/**
	 * Tests, if an graph/vertex/edge of a generic <code>Graph</code> contains all its attributes,
	 * as defined by the corresponding {@link AttributedElementClass} in the <code>Graph</code>'s
	 * <code>Schema</code>.
	 * @param testObject A {@link GenericGraphImpl}, {@link GenericVertexImpl} or {@link GenericEdgeImpl} <code>Object</code>.
	 * @param aec The element of the <code>Schema</code>, representing the tested. 
	 */
	private void testElementAttributes(Object testObject, AttributedElementClass aec) {
		try {
			Field f = testObject.getClass().getDeclaredField("attributes");
			f.setAccessible(true);
			Map<?, ?> attributes = (Map<?, ?>) f.get(testObject);
			if(attributes != null) {
				assertEquals(aec.getAttributeCount(), attributes.size());
				for(Attribute a : aec.getAttributeList()) {
					assertTrue(attributes.containsKey(a.getName()));
				}
			}
			else {
				assertEquals(0, aec.getAttributeCount());
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Tests, if the value of an attribute in the generic TGraph implementation has the
	 * default value corresponding to its definition in the schema.
	 * @param value
	 * @param attribute
	 */
	private void testDefaultValue(Object value, Attribute attribute) {
		try {
			Object expected = GenericUtil.parseGenericAttribute(attribute.getDomain(), GraphIO.createStringReader(attribute.getDefaultValueAsString(), attribute.getAttributedElementClass().getSchema()));
			assertEquals(expected, value);
		} catch (GraphIOException e) {
			fail();
			e.printStackTrace();
		}
	}

	// Test creating a small graph without attributes (MinimalSchema.tg). 
	@Test
	public void testCreateGraph1() {
		try {
			Schema schema;
			schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "MinimalSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			assertTrue(g instanceof GenericGraphImpl);
			assertEquals(schema.getGraphClass(), g.getAttributedElementClass());
			testElementAttributes(g, schema.getGraphClass());
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// Test creating a graph with attributes that have explicitly defined default values in the schema (DefaultValueSchema.tg)
	@Test
	public void testCreateGraph2() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "DefaultValueTestSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			assertTrue(g instanceof GenericGraphImpl);
			assertEquals(schema.getGraphClass(), g.getAttributedElementClass());
			testElementAttributes(g, schema.getGraphClass());
			
			// Test if default values were set correctly
			assertEquals(true, g.getAttribute("boolGraph"));
			assertEquals(JGraLab.vector().plus(JGraLab.vector().plus(true)).plus(JGraLab.vector().plus(false)).plus(JGraLab.vector().plus(true)), g.getAttribute("complexListGraph"));
			assertEquals(JGraLab.map().plus(JGraLab.vector().plus(true), JGraLab.set().plus(true)).plus(JGraLab.vector().plus(false), JGraLab.set().plus(false)), g.getAttribute("complexMapGraph"));
			assertEquals(JGraLab.set().plus(JGraLab.set().plus(true)).plus(JGraLab.set().plus(false)), g.getAttribute("complexSetGraph"));
			assertEquals(new Double(1.1), g.getAttribute("doubleGraph"));
			assertEquals("FIRST", g.getAttribute("enumGraph"));
			assertEquals(new Integer(1), g.getAttribute("intGraph"));
			assertEquals(JGraLab.vector().plus(true).plus(false).plus(true), g.getAttribute("listGraph"));
			assertEquals(new Long(1), g.getAttribute("longGraph"));
			assertEquals(JGraLab.map().plus(1, true).plus(2, false).plus(3, true), g.getAttribute("mapGraph"));
			assertEquals(JGraLab.set().plus(true).plus(false), g.getAttribute("setGraph"));
			assertEquals("test", g.getAttribute("stringGraph"));
			assertEquals(
				de.uni_koblenz.jgralab.impl.RecordImpl.empty().
					plus("boolRecord", true)
					.plus("doubleRecord", new Double(1.1))
					.plus("enumRecord", "FIRST")
					.plus("intRecord", new Integer(1))
					.plus("listRecord", JGraLab.vector().plus(true).plus(false).plus(true))
					.plus("longRecord", new Long(1))
					.plus("mapRecord", JGraLab.map().plus(1, true).plus(2, false).plus(3, true))
					.plus("setRecord", JGraLab.set().plus(true).plus(false))
					.plus("stringRecord", "test"),
				g.getAttribute("recordGraph"));
			// Dynamic test to see if default values were set correctly
			for(Attribute a : schema.getGraphClass().getAttributeList()) {
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
			schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "MinimalSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass("Node"));
			assertTrue(v1 instanceof GenericVertexImpl);
			testElementAttributes(v1, schema.getGraphClass().getVertexClass("Node"));
			
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass("Node"));
			assertTrue(v2 instanceof GenericVertexImpl);
			testElementAttributes(v2, schema.getGraphClass().getVertexClass("Node"));
			
			assertTrue(g.containsVertex(v1));
			assertTrue(g.containsVertex(v2));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// Create generic vertices with attributes (inherited ones and own ones) (DefaultValueTestSchema.tg)
	@Test
	public void testCreateVertex2() {
		// TODO
	}
	
	// Create generic edges (1 attribute) (MinialSchema.tg)
	@Test
	public void testCreateEdge1() {
		try {
			Schema schema;
			schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "MinimalSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
		
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass("Node"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass("Node"));
			
			Edge e1 = g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v1, v2);
			Edge e2 = g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v2, v1);
			assertEquals(g.getSchema().getGraphClass().getEdgeClass("Link"), e1.getAttributedElementClass());
			assertEquals(g.getSchema().getGraphClass().getEdgeClass("Link"), e2.getAttributedElementClass());
			testElementAttributes(e1, schema.getGraphClass().getEdgeClass("Link"));
			testElementAttributes(e2, schema.getGraphClass().getEdgeClass("Link"));
			assertTrue(g.containsEdge(e1));
			assertTrue(g.containsEdge(e2));
			
			assertEquals(e1, v1.getFirstIncidence());
			assertEquals(e2, e1.getNextEdge());
			assertEquals(e2.getReversedEdge(), e1.getNextIncidence());
			assertEquals(e1.getReversedEdge(), v2.getFirstIncidence());
			
			v1.setAttribute("nodeMap", JGraLab.map().plus(20, "twenty"));
			g.save(DATAFOLDER + "Test.tg");
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// Create generic edges with inherited attributes
	@Test
	public void testCreateEdge2() {
		// TODO
	}
	
	// EdgeClass is from a different schema 
	@Test
	public void testCreateEdgeFailure1() {
		// TODO
	}
	
	// EdgeClass is not defined as connecting the VertexClasses
	@Test
	public void testCreateEdgeFailure2() {
		// TODO
	}
	
	// The IncidenceClass is redefined and therefore not allowed
	@Test
	public void testCreateEdgeFailure3() {
		// TODO
	}
	
	// VertexClass is from a different Schema
	@Test
	public void testCreateVertexFailure1() {
		// TODO
	}

	// Test setting and accessing a graph's attributes
	@Test
	public void testAccessAttributes1() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
		
		g.setAttribute("boolGraph", false);
		assertEquals(false, g.getAttribute("boolGraph"));
		
		g.setAttribute("listGraph", JGraLab.vector().plus(true).plus(true).plus(false));
		assertEquals(JGraLab.vector().plus(true).plus(true).plus(false), g.getAttribute("listGraph"));
		
		g.setAttribute("listGraph", null);
		assertEquals(null, g.getAttribute("listGraph"));
		
		// TODO
	}
	
	// Test setting attributes that don't exist. NoSuchAttributeException is expected. 
	@Test(expected=NoSuchAttributeException.class)
	public void testAccessAttributesFailure1() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
		
		g.setAttribute("doesNotExist", true);
	}
	
	@Test(expected=NoSuchAttributeException.class)
	public void testAccessAttributesFailure2() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
		
		g.setAttribute("boolgraph", true); // the actual attribute's name is written in CamelCase
	}
	
	
	// Test setting attribute values of a wrong domain. A ClassCastException is expected.
	@Test(expected=ClassCastException.class)
	public void testAccessAttributesFailure3() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
		
		g.setAttribute("boolGraph", JGraLab.set().plus(1));
	}
	
	
	// Test setting attribute values of a wrong domain. A ClassCastException is expected.
	@Test(expected=ClassCastException.class)
	public void testAccessAttributesFailure4() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
		
		g.setAttribute("boolGraph", null);
	}
	
	
	// Test type-specific iteration over vertices (VertexTestSchema.tg)
	@Test
	public void testVertices() {
		// TODO
	}
	
	// Test type-specific iteration over edges (VertexTestSchema.tg)
	@Test
	public void testEdges() {
		// TODO
	}
	
	@Test
	public void testSave1() {

		try {
			Schema schema;
			schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "MinimalSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass("Node"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass("Node"));
			
			g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v1, v2);
			g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v2, v1);
			g.save("testit" + File.separator + "testdata" + File.separator + "GenericTestGraph1.tg");
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
			Graph g = GraphIO.loadGraphFromFile(GRAPHFOLDER + "citymapgraph.tg", null, null, ImplementationType.GENERIC);
			assertEquals(8, g.getVCount());
			assertEquals(11, g.getECount());
			
			// Check types
			VertexClass intersection = g.getSchema().getGraphClass().getVertexClass("Intersection");
			VertexClass carPark = g.getSchema().getGraphClass().getVertexClass("CarPark");
			EdgeClass street = g.getSchema().getGraphClass().getEdgeClass("Street");
			EdgeClass bridge = g.getSchema().getGraphClass().getEdgeClass("Bridge");
			Vertex[] vertices = new Vertex[8];
			for(int i = 0; i < 8; i++) {
				vertices[i] = g.getVertex(i + 1);
			}
			Edge[] edges = new Edge[11];
			for(int i = 0; i < 11; i++) {
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
			for(Vertex v : vertices) {
				assertEquals(null, v.getAttribute("name"));
			}
			assertEquals(new Integer(2500), vertices[2].getAttribute("capacity"));
			assertEquals(new Integer(500), vertices[6].getAttribute("capacity"));
			assertEquals(new Integer(500), vertices[7].getAttribute("capacity"));
			
			for(int i = 0; i < edges.length; i++) {
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
			assertEquals(edges[0].getReversedEdge(), vertices[1].getFirstIncidence());
			assertEquals(edges[1], vertices[1].getFirstIncidence().getNextIncidence());
			assertEquals(edges[3].getReversedEdge(), vertices[1].getLastIncidence());
			assertEquals(2, vertices[2].getDegree());
			assertEquals(edges[1].getReversedEdge(), vertices[2].getFirstIncidence());
			assertEquals(edges[4], vertices[2].getLastIncidence());
			assertEquals(3, vertices[3].getDegree());
			assertEquals(edges[2].getReversedEdge(), vertices[3].getFirstIncidence());
			assertEquals(edges[5], vertices[3].getFirstIncidence().getNextIncidence());
			assertEquals(edges[7], vertices[3].getLastIncidence());
			assertEquals(4, vertices[4].getDegree());
			assertEquals(edges[3], vertices[4].getFirstIncidence());
			assertEquals(edges[5].getReversedEdge(), vertices[4].getFirstIncidence().getNextIncidence());
			assertEquals(edges[6], vertices[4].getLastIncidence().getPrevIncidence());
			assertEquals(edges[8], vertices[4].getLastIncidence());
			assertEquals(3, vertices[5].getDegree());
			assertEquals(edges[4].getReversedEdge(), vertices[5].getFirstIncidence());
			assertEquals(edges[6].getReversedEdge(), vertices[5].getFirstIncidence().getNextIncidence());
			assertEquals(edges[9], vertices[5].getLastIncidence());
			assertEquals(2, vertices[6].getDegree());
			assertEquals(edges[7].getReversedEdge(), vertices[6].getFirstIncidence());
			assertEquals(edges[10], vertices[6].getLastIncidence());
			assertEquals(3, vertices[7].getDegree());
			assertEquals(edges[8].getReversedEdge(), vertices[7].getFirstIncidence());
			assertEquals(edges[9].getReversedEdge(), vertices[7].getFirstIncidence().getNextIncidence());
			assertEquals(edges[10].getReversedEdge(), vertices[7].getLastIncidence());
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// Test creating, saving and loading a random graph
	@Test
	public void testLoadGraph2() {
		// TODO
	}
	
}
