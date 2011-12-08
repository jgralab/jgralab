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
	
	/**
	 * Tests, if an graph/vertex/edge of a generic <code>Graph</code> contains all its attributes,
	 * as defined by the corresponding {@link AttributedElementClass} in the graph's
	 * <code>Schema</code>.  
	 * @param testObject A {@link GenericGraphImpl}, {@link GenericVertexImpl} or {@link GenericEdgeImpl} Object.
	 * @param aec The element of the Schema, representing the tested. 
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

	// Test creating a small graph without attributes (MinimalSchema.tg). 
	@Test
	public void testCreateGraph1() {
		try {
			Schema schema;
			schema = GraphIO.loadSchemaFromFile("testit" + File.separator + "testschemas" + File.separator + "MinimalSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			assertTrue(g instanceof GenericGraphImpl);
			assertEquals(schema.getGraphClass(), g.getAttributedElementClass());
			
			testElementAttributes(g, schema.getGraphClass());
			
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass("Node"));
			assertTrue(v1 instanceof GenericVertexImpl);
			
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass("Node"));
			assertTrue(v2 instanceof GenericVertexImpl);
			
			Edge e1 = g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v1, v2);
			Edge e2 = g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v2, v1);
			
			assertEquals(v1, g.getFirstVertex());
			assertEquals(v2, v1.getNextVertex());
			assertEquals(null, v2.getNextVertex());
			assertEquals(e1, v1.getFirstIncidence());
			assertEquals(e2, e1.getNextEdge());
			assertEquals(e2.getReversedEdge(), e1.getNextIncidence());
			assertEquals(e1.getReversedEdge(), v2.getFirstIncidence());
	
			testElementAttributes(g, schema.getGraphClass());
			testElementAttributes(v1, schema.getGraphClass().getVertexClass("Node"));
			testElementAttributes(v2, schema.getGraphClass().getVertexClass("Node"));
			testElementAttributes(e1, schema.getGraphClass().getEdgeClass("Link"));
			testElementAttributes(e2, schema.getGraphClass().getEdgeClass("Link"));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// Test creating a graph with attributes that have explicitly defined default values in the schema
	@Test
	public void testCreateGraph2() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile("testit" + File.separator + "testschemas" + File.separator + "DefaultValueTestSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			assertTrue(g instanceof GenericGraphImpl);
			assertEquals(schema.getGraphClass(), g.getAttributedElementClass());
			
			testElementAttributes(g, schema.getGraphClass());
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
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test setting and accessing a graph's attributes
	@Test
	public void testAccessAttributes1() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile("testit" + File.separator + "testschemas" + File.separator + "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
		
		g.setAttribute("boolGraph", false);
		assertEquals(false, g.getAttribute("boolGraph"));
		
		g.setAttribute("listGraph", JGraLab.vector().plus(true).plus(true).plus(false));
		assertEquals(JGraLab.vector().plus(true).plus(true).plus(false), g.getAttribute("listGraph"));
		
		g.setAttribute("listGraph", null);
		assertEquals(null, g.getAttribute("listGraph"));
	}
	
	// Test setting attributes that don't exist. NoSuchAttributeException is expected. 
	@Test(expected=NoSuchAttributeException.class)
	public void testAccessAttributesFailure1() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile("testit" + File.separator + "testschemas" + File.separator + "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
		
		g.setAttribute("doesNotExist", true);
	}
	
	@Test(expected=NoSuchAttributeException.class)
	public void testAccessAttributesFailure2() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile("testit" + File.separator + "testschemas" + File.separator + "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
		
		g.setAttribute("boolgraph", true); // the actual attribute's name is written in CamelCase
	}
	
	
	// Test setting attribute values of a wrong domain. A ClassCastException is expected.
	@Test(expected=ClassCastException.class)
	public void testAccessAttributesFailure3() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile("testit" + File.separator + "testschemas" + File.separator + "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
		
		g.setAttribute("boolGraph", JGraLab.set().plus(1));
	}
	
	
	// Test setting attribute values of a wrong domain. A ClassCastException is expected.
	@Test(expected=ClassCastException.class)
	public void testAccessAttributesFailure4() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile("testit" + File.separator + "testschemas" + File.separator + "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
		
		g.setAttribute("boolGraph", null);
	}
	
	
	// TODO Test type-specific traversal
	@Test
	public void testTraversal1() {
		
	}
	
	// TODO Test saving a graph
	@Test
	public void testSave1() {

		try {
			Schema schema;
			schema = GraphIO.loadSchemaFromFile("testit" + File.separator + "testschemas" + File.separator + "MinimalSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC, 100, 100);
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass("Node"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass("Node"));
			
			g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v1, v2);
			g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v2, v1);
			g.save("testit" + File.separator + "testdata" + File.separator + "GenericTestGraph1.tg");
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// TODO Test Loading the graph
	@Test
	public void testLoadGraph1() {
		try {
			Graph g = GraphIO.loadGraphFromFile("testit" + File.separator + "testgraphs" + File.separator + "citymapgraph.tg", null, null, ImplementationType.GENERIC);
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
