/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralabtest.genericimpltest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.RecordImpl;
import de.uni_koblenz.jgralab.impl.generic.GenericEdgeImpl;
import de.uni_koblenz.jgralab.impl.generic.GenericGraphImpl;
import de.uni_koblenz.jgralab.impl.generic.GenericVertexImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class GenericGraphImplTest {

	public static final String SCHEMAFOLDER = "testit" + File.separator
			+ "testschemas" + File.separator;
	public static final String GRAPHFOLDER = "testit" + File.separator
			+ "testgraphs" + File.separator;
	public static final String DATAFOLDER = "testit" + File.separator
			+ "testdata" + File.separator;

	/**
	 * Tests, if an graph/vertex/edge of a generic <code>Graph</code> contains
	 * all its attributes, as defined by the corresponding
	 * {@link AttributedElementClass} in the <code>Graph</code>'s
	 * <code>Schema</code>. However, this does not guarantee that there are no
	 * other, additional attributes.
	 *
	 * @param testObject
	 *            A {@link GenericGraphImpl}, {@link GenericVertexImpl} or
	 *            {@link GenericEdgeImpl} <code>Object</code>.
	 * @param aec
	 *            The element of the <code>Schema</code>, representing the
	 *            tested.
	 */
	public static void testElementAttributes(AttributedElement<?, ?> ae,
			AttributedElementClass<?, ?> aec) {
		for (Attribute a : aec.getAttributeList()) {
			Object value = ae.getAttribute(a.getName());
			assertTrue(a.getDomain().isConformGenericValue(value));
		}
	}

	/**
	 * Tests, if the value of an attribute in the generic TGraph implementation
	 * has the default value as defined in the schema. If no explicit default
	 * value was defined, it tests, if the attribute's value corresponds to the
	 * general default value of its Domain.
	 *
	 * @param value
	 * @param attribute
	 */
	public static void testDefaultValue(Object value, Attribute attribute) {
		try {
			if (attribute.getDefaultValueAsString() != null) {
				Object expected = attribute.getDomain().parseGenericAttribute(
						GraphIO.createStringReader(attribute
								.getDefaultValueAsString(), attribute
								.getAttributedElementClass().getSchema()));
				assertEquals(expected, value);
			} else {
				assertEquals(
						GenericGraphImpl.genericAttributeDefaultValue(attribute
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
			Graph g = schema.createGraph(ImplementationType.GENERIC);
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
			Graph g = schema.createGraph(ImplementationType.GENERIC);
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
			Graph g = schema.createGraph(ImplementationType.GENERIC);

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
			Graph g = schema.createGraph(ImplementationType.GENERIC);
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

	// try to instantiate a Vertex of an abstract class (VertexTestSchema.tg)
	// A GraphException is expected
	@Test
	public void testCreateVertexFailure1() {
		Schema s;
		Graph g = null;
		int vCountBefore = 0;
		try {
			s = GraphIO
					.loadSchemaFromFile(SCHEMAFOLDER + "VertexTestSchema.tg");
			g = s.createGraph(ImplementationType.GENERIC);
			vCountBefore = g.getVCount();
			g.createVertex(g.getGraphClass()
					.getVertexClass("AbstractSuperNode"));
		} catch (GraphException e) {
			assertEquals(vCountBefore, g.getVCount());
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Try to create a Vertex of a VertexClass from a different schema
	@Test(expected=GraphException.class)
	public void testCreateVertexFailure2() {
		try {
			Schema s1 = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "citymapschema.tg");
			Schema s2 = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "greqltestschema.tg");
			Graph g = s1.createGraph(ImplementationType.GENERIC);
			g.createVertex(s2.getGraphClass().getVertexClass("junctions.Crossroad"));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// try to instantiate an Edge of an abstract class (citymapschema.tg)
	// A GraphException is expected
	@Test
	public void testCreateEdgeFailure4() {
		Schema s;
		Graph g = null;
		int eCountBefore = 0;
		try {
			s = GraphIO.loadSchemaFromFile(SCHEMAFOLDER + "citymapschema.tg");
			g = s.createGraph(ImplementationType.GENERIC);
			Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
					"Intersection"));
			Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass(
					"Intersection"));
			eCountBefore = g.getECount();
			g.createEdge(g.getGraphClass().getEdgeClass("Way"), v1, v2);
		} catch (GraphException e) {
			assertEquals(eCountBefore, g.getECount());
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
			Graph g = schema.createGraph(ImplementationType.GENERIC);
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
			Graph g = schema.createGraph(ImplementationType.GENERIC);
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

	// Test if deletion works with the generic implementation
	@Test
	public void testDeleteEdge() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "VertexTestSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC);
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
					"DoubleSubNode"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
					"SuperNode"));

			Edge e1 = g.createEdge(
					schema.getGraphClass().getEdgeClass("SubLink"), v1, v2);
			Edge e2 = g.createEdge(
					schema.getGraphClass().getEdgeClass("LinkBack"), v2, v1);

			g.deleteEdge(e1);
			assertFalse(g.containsEdge(e1));
			assertTrue(g.containsVertex(v2));
			assertTrue(g.containsEdge(e2));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test if deletion works with the generic implementation
	@Test
	public void testDeleteVertex() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "VertexTestSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC);
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
					"DoubleSubNode"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
					"SuperNode"));

			Edge e1 = g.createEdge(
					schema.getGraphClass().getEdgeClass("SubLink"), v1, v2);
			Edge e2 = g.createEdge(
					schema.getGraphClass().getEdgeClass("LinkBack"), v2, v1);

			g.deleteVertex(v1);
			assertFalse(g.containsEdge(e1));
			assertFalse(g.containsVertex(v2));
			assertFalse(g.containsEdge(e2));
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

			g1 = schema1.createGraph(ImplementationType.GENERIC);
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
			Graph g = schema.createGraph(ImplementationType.GENERIC);
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
			Graph g = schema.createGraph(ImplementationType.GENERIC);
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

	// Test setting a TraversalContext (greqltestgraph.tg)
	public void testSetTraversalContext() {
		try {
			final Graph g = GraphIO.loadGraphFromFile(GRAPHFOLDER
					+ "greqltestgraph.tg", ImplementationType.GENERIC, null);
			g.setTraversalContext(new TraversalContext() {

				@Override
				public boolean containsVertex(Vertex v) {
					return v.getAttributedElementClass().equals(
							g.getGraphClass().getVertexClass("Crossroad"));
				}

				@Override
				public boolean containsEdge(Edge e) {
					return e.getAttributedElementClass().equals(
							g.getGraphClass().getEdgeClass("Street"));
				}

			});
			for (Vertex v : g.vertices()) {
				assertEquals(g.getGraphClass().getVertexClass("Crossroad"),
						v.getAttributedElementClass());
			}
			for (Edge e : g.edges()) {
				assertEquals(g.getGraphClass().getEdgeClass("Street"),
						e.getAttributedElementClass());
			}

			g.setTraversalContext(new TraversalContext() {

				@Override
				public boolean containsVertex(Vertex v) {
					return g.getGraphClass().getVertexClass("Plaza")
							.equals(v.getAttributedElementClass());
				}

				@Override
				public boolean containsEdge(Edge e) {
					return false;
				}
			});

			for (Vertex v : g.vertices()) {
				assertEquals(g.getGraphClass().getVertexClass("Plaza"),
						v.getAttributedElementClass());
			}

			assertNull(g.getFirstEdge());
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// Test setting and accessing a graph's attributes
	@Test
	public void testAccessAttributes1() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC);

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

		RecordImpl r = (RecordImpl) g.getAttribute("recordGraph");
		r = r.plus("stringRecord", null);
		r = r.plus("listRecord", null);
		r = r.plus("setRecord", null);
		r = r.plus("mapRecord", null);
		g.setAttribute("recordGraph", r);
		assertNull(r.getComponent("stringRecord"));
		assertNull(r.getComponent("listRecord"));
		assertNull(r.getComponent("setRecord"));
		assertNull(r.getComponent("mapRecord"));
	}

	// Test setting attributes that don't exist. NoSuchAttributeException is
	// expected.
	@Test(expected = NoSuchAttributeException.class)
	public void testAccessAttributesFailure1() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC);

		g.setAttribute("doesNotExist", true);
	}

	@Test(expected = NoSuchAttributeException.class)
	public void testAccessAttributesFailure2() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC);

		g.setAttribute("boolgraph", true); // the actual attribute's name is
											// written in CamelCase
	}

	// Test setting attribute values of a wrong domain. A ClassCastException is
	// expected.
	@Test(expected = ClassCastException.class)
	public void testAccessAttributesFailure3() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC);

		g.setAttribute("boolGraph", JGraLab.set().plus(1));
	}

	// Test setting attribute values of a wrong domain. A ClassCastException is
	// expected.
	@Test(expected = ClassCastException.class)
	public void testAccessAttributesFailure4() throws GraphIOException {
		Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC);

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
			Graph g = s.createGraph(ImplementationType.GENERIC);

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
			Graph g = s.createGraph(ImplementationType.GENERIC);

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

	// Tests parsing of attribute values (DefaultValueTestSchema.tg)
	@Test
	public void testReadAttributeValueFromString() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			for (Attribute a : g.getAttributedElementClass().getAttributeList()) {
				testDefaultValue(g.getAttribute(a.getName()), a);
			}

			// parse values different from the default ones
			g.readAttributeValueFromString("boolGraph", "f");
			assertEquals(false, g.getAttribute("boolGraph"));
			g.readAttributeValueFromString("complexListGraph", "[[f]]");
			assertEquals(JGraLab.vector().plus(JGraLab.vector().plus(false)),
					g.getAttribute("complexListGraph"));
			g.readAttributeValueFromString("complexMapGraph",
					"{[t t] - {f} [f f] - {t f}}");
			assertEquals(
					JGraLab.map()
							.plus(JGraLab.vector().plus(true).plus(true),
									JGraLab.set().plus(false))
							.plus(JGraLab.vector().plus(false).plus(false),
									JGraLab.set().plus(true).plus(false)),
					g.getAttribute("complexMapGraph"));
			g.readAttributeValueFromString("complexSetGraph", "{{f}}");
			assertEquals(JGraLab.set().plus(JGraLab.set().plus(false)),
					g.getAttribute("complexSetGraph"));
			g.readAttributeValueFromString("doubleGraph", "12.34");
			assertEquals(12.34d, g.getAttribute("doubleGraph"));
			g.readAttributeValueFromString("enumGraph", "SECOND");
			assertEquals("SECOND", g.getAttribute("enumGraph"));
			g.readAttributeValueFromString("intGraph", "42");
			assertEquals(42, g.getAttribute("intGraph"));
			g.readAttributeValueFromString("listGraph", "[t t]");
			assertEquals(JGraLab.vector().plus(true).plus(true),
					g.getAttribute("listGraph"));
			g.readAttributeValueFromString("longGraph", "987654321");
			assertEquals(987654321l, g.getAttribute("longGraph"));
			g.readAttributeValueFromString("mapGraph", "{1 - f 2 - t}");
			assertEquals(JGraLab.map().plus(1, false).plus(2, true),
					g.getAttribute("mapGraph"));
			g.readAttributeValueFromString("recordGraph",
					"(f 2.2 THIRD 42 [f t] 987654321 {1 - f 2 - t} {t} \"some String\")");
			assertEquals(
					RecordImpl
							.empty()
							.plus("boolRecord", false)
							.plus("doubleRecord", 2.2d)
							.plus("enumRecord", "THIRD")
							.plus("intRecord", 42)
							.plus("listRecord",
									JGraLab.vector().plus(false).plus(true))
							.plus("longRecord", 987654321l)
							.plus("mapRecord",
									JGraLab.map().plus(1, false).plus(2, true))
							.plus("setRecord", JGraLab.set().plus(true))
							.plus("stringRecord", "some String"),
					g.getAttribute("recordGraph"));
			g.readAttributeValueFromString("setGraph", "{f}");
			assertEquals(JGraLab.set().plus(false), g.getAttribute("setGraph"));
			g.readAttributeValueFromString("stringGraph", "\"some String\"");
			assertEquals("some String", g.getAttribute("stringGraph"));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testReadAttributeValues() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			for (Attribute a : g.getAttributedElementClass().getAttributeList()) {
				testDefaultValue(g.getAttribute(a.getName()), a);
			}
			g.readAttributeValues(GraphIO.createStringReader(
					"f " +
					"[[f]] " +
					"{[t t] - {f} [f f] - {t f}} " +
					"{{f}} " +
					"12.34 " +
					"SECOND " +
					"42" +
					"[t t] " +
					"987654321 " +
					"{1 - f 2 - t} " +
					"(f 2.2 THIRD 42 [f t] 987654321 {1 - f 2 - t} {t} \"some String\") " +
					"{f} " +
					"\"some String\"", g.getSchema()));

			// parse values different from the default ones
			assertEquals(false, g.getAttribute("boolGraph"));
			assertEquals(JGraLab.vector().plus(JGraLab.vector().plus(false)),
					g.getAttribute("complexListGraph"));
			assertEquals(
					JGraLab.map()
							.plus(JGraLab.vector().plus(true).plus(true),
									JGraLab.set().plus(false))
							.plus(JGraLab.vector().plus(false).plus(false),
									JGraLab.set().plus(true).plus(false)),
					g.getAttribute("complexMapGraph"));
			assertEquals(JGraLab.set().plus(JGraLab.set().plus(false)),
					g.getAttribute("complexSetGraph"));
			assertEquals(12.34d, g.getAttribute("doubleGraph"));
			assertEquals("SECOND", g.getAttribute("enumGraph"));
			assertEquals(42, g.getAttribute("intGraph"));
			assertEquals(JGraLab.vector().plus(true).plus(true),
					g.getAttribute("listGraph"));
			assertEquals(987654321l, g.getAttribute("longGraph"));
			assertEquals(JGraLab.map().plus(1, false).plus(2, true),
					g.getAttribute("mapGraph"));
			assertEquals(
					RecordImpl
							.empty()
							.plus("boolRecord", false)
							.plus("doubleRecord", 2.2d)
							.plus("enumRecord", "THIRD")
							.plus("intRecord", 42)
							.plus("listRecord",
									JGraLab.vector().plus(false).plus(true))
							.plus("longRecord", 987654321l)
							.plus("mapRecord",
									JGraLab.map().plus(1, false).plus(2, true))
							.plus("setRecord", JGraLab.set().plus(true))
							.plus("stringRecord", "some String"),
					g.getAttribute("recordGraph"));
			assertEquals(JGraLab.set().plus(false), g.getAttribute("setGraph"));
			assertEquals("some String", g.getAttribute("stringGraph"));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testWriteAttributeValueToString() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			for (Attribute a : g.getAttributedElementClass().getAttributeList()) {
				testDefaultValue(g.getAttribute(a.getName()), a);
			}

			assertEquals("t", g.writeAttributeValueToString("boolGraph"));
			assertEquals("[[t] [f] [t]]", g.writeAttributeValueToString("complexListGraph"));
			assertEquals("{[t] - {t} [f] - {f}}", g.writeAttributeValueToString("complexMapGraph"));
			assertEquals("{{t} {f}}", g.writeAttributeValueToString("complexSetGraph"));
			assertEquals("1.1", g.writeAttributeValueToString("doubleGraph"));
			assertEquals("FIRST", g.writeAttributeValueToString("enumGraph"));
			assertEquals("1", g.writeAttributeValueToString("intGraph"));
			assertEquals("[t f t]", g.writeAttributeValueToString("listGraph"));
			assertEquals("1", g.writeAttributeValueToString("longGraph"));
			assertEquals("{1 - t 2 - f 3 - t}", g.writeAttributeValueToString("mapGraph"));
			assertEquals("(t 1.1 FIRST 1 [t f t] 1 {1 - t 2 - f 3 - t} {t f} \"test\")", g.writeAttributeValueToString("recordGraph"));
			assertEquals("{t f}", g.writeAttributeValueToString("setGraph"));
			assertEquals("\"test\"", g.writeAttributeValueToString("stringGraph"));

			g.setAttribute("mapGraph", null);
			assertEquals(GraphIO.NULL_LITERAL, g.writeAttributeValueToString("mapGraph"));
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		} catch (NoSuchAttributeException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testWriteAttributeValues() {
		try {
			Schema s = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = s.createGraph(ImplementationType.GENERIC);
			for (Attribute a : g.getAttributedElementClass().getAttributeList()) {
				testDefaultValue(g.getAttribute(a.getName()), a);
			}

			GraphIO io = GraphIO.createStringWriter(g.getSchema());
			g.writeAttributeValues(io);
			assertEquals(
						"t " +
						"[[t] [f] [t]] " +
						"{[t] - {t} [f] - {f}} " +
						"{{t} {f}} " +
						"1.1 " +
						"FIRST " +
						"1 " +
						"[t f t] " +
						"1 " +
						"{1 - t 2 - f 3 - t} " +
						"(t 1.1 FIRST 1 [t f t] 1 {1 - t 2 - f 3 - t} {t f} \"test\") " +
						"{t f} " +
						"\"test\"",
					io.getStringWriterResult());
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		} catch (NoSuchAttributeException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSave() {
		try {
			Schema schema;
			schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "MinimalSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC);
			Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
					"Node"));
			Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
					"Node"));

			g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v1, v2);
			g.createEdge(schema.getGraphClass().getEdgeClass("Link"), v2, v1);
			g.save(DATAFOLDER + "GenericTestGraph1.tg");

			Graph g2 = GraphIO.loadGraphFromFile(DATAFOLDER
					+ "GenericTestGraph1.tg", schema,
					ImplementationType.GENERIC, null);
			Iterator<Vertex> vertexIterator1 = g.vertices().iterator();
			Iterator<Vertex> vertexIterator2 = g2.vertices().iterator();
			Iterator<Edge> edgeIterator1 = g.edges().iterator();
			Iterator<Edge> edgeIterator2 = g2.edges().iterator();

			while (vertexIterator1.hasNext()) {
				Vertex vg1 = vertexIterator1.next();
				Vertex vg2 = vertexIterator2.next();
				assertEquals(vg1.getId(), vg2.getId());
				assertEquals(vg1.getAttributedElementClass(),
						vg2.getAttributedElementClass());
			}
			assertFalse(vertexIterator2.hasNext());
			while (edgeIterator1.hasNext()) {
				Edge eg1 = edgeIterator1.next();
				Edge eg2 = edgeIterator2.next();
				assertEquals(eg1.getId(), eg2.getId());
				assertEquals(eg2.getAttributedElementClass(),
						eg2.getAttributedElementClass());
			}
			assertFalse(edgeIterator2.hasNext());
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
			Schema s = GraphIO.loadSchemaFromFile(GRAPHFOLDER
					+ "citymapgraph.tg");
			Graph g = GraphIO.loadGraphFromFile(
					GRAPHFOLDER + "citymapgraph.tg", s,
					ImplementationType.GENERIC, null);
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
			Schema s = GraphIO.loadSchemaFromFile(GRAPHFOLDER
					+ "greqltestgraph.tg");
			Graph g1 = GraphIO.loadGraphFromFile(GRAPHFOLDER
					+ "greqltestgraph.tg", s, ImplementationType.GENERIC, null);

			assertEquals(s.getGraphClass(), g1.getAttributedElementClass());
			assertEquals(157, g1.getVCount());
			assertEquals(357, g1.getECount());

			// compare against the same Graph loaded with the standard
			// implementation
			Graph g2 = GraphIO.loadGraphFromFile(GRAPHFOLDER
					+ "greqltestgraph.tg", ImplementationType.STANDARD, null);
			for (Vertex v : g2.vertices()) {
				assertEquals(v.getAttributedElementClass(),
						g1.getVertex(v.getId()).getAttributedElementClass());

				// assert equality of attributes
				for (Attribute a : v.getAttributedElementClass()
						.getAttributeList()) {
					try {
						assertEquals(
								v.writeAttributeValueToString(a.getName()),
								g2.getVertex(v.getId())
										.writeAttributeValueToString(
												a.getName()));
					} catch (IOException e) {
						e.printStackTrace();
						fail();
					}
				}
			}
			for (Edge edge : g2.edges()) {
				assertEquals(edge.getAttributedElementClass(),
						g1.getEdge(edge.getId()).getAttributedElementClass());

				// assert equality of attributes
				for (Attribute a : edge.getAttributedElementClass()
						.getAttributeList()) {
					try {
						assertEquals(edge.writeAttributeValueToString(a
								.getName()), g2.getEdge(edge.getId())
								.writeAttributeValueToString(a.getName()));
					} catch (IOException e) {
						e.printStackTrace();
						fail();
					}
				}
			}

		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testCreateRecord() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC);
			RecordDomain testRecordDomain = (RecordDomain) schema
					.getDomain("TestRecordDomain");
			Map<String, Object> values = new HashMap<String, Object>();
			Boolean boolRecord = Boolean.FALSE;
			Double doubleRecord = Double.valueOf(0.123d);
			String enumRecord = "SECOND";
			Integer intRecord = Integer.valueOf(42);
			List<?> listRecord = JGraLab.vector().plus(true).plus(false);
			Long longRecord = Long.valueOf(9876543210l);
			Map<?, ?> mapRecord = JGraLab.map().plus(1, true).plus(2, false);
			Set<?> setRecord = JGraLab.set().plus(true).plus(false);
			String stringRecord = "some string";
			values.put("boolRecord", boolRecord);
			values.put("doubleRecord", doubleRecord);
			values.put("enumRecord", enumRecord);
			values.put("intRecord", intRecord);
			values.put("listRecord", listRecord);
			values.put("longRecord", longRecord);
			values.put("mapRecord", mapRecord);
			values.put("setRecord", setRecord);
			values.put("stringRecord", stringRecord);
			Record r = g.createRecord(testRecordDomain, values);
			for (String componentName : values.keySet()) {
				assertEquals(values.get(componentName),
						r.getComponent(componentName));
			}
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetEnumConstant() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC);
			EnumDomain testEnumDomain = (EnumDomain) schema
					.getDomain("TestEnumDomain");
			for (String c : testEnumDomain.getConsts()) {
				assertEquals(c, g.getEnumConstant(testEnumDomain, c));
			}
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test(expected = GraphException.class)
	public void testGetEnumConstantFailure() {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(SCHEMAFOLDER
					+ "DefaultValueTestSchema.tg");
			Graph g = schema.createGraph(ImplementationType.GENERIC);
			EnumDomain testEnumDomain = (EnumDomain) schema
					.getDomain("TestEnumDomain");
			g.getEnumConstant(testEnumDomain, "FOURTH");
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@AfterClass
	public static void cleanup() {
		File f = new File(DATAFOLDER + "GenericTestGraph1.tg");
		f.delete();
	}

}
