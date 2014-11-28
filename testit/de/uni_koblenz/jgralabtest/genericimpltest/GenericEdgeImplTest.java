/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphException;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;
import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.RecordImpl;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;

public class GenericEdgeImplTest {

	@Test
	public void testAccessAttributes() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
				"TestVertex"));
		Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass(
				"TestVertex"));
		Edge e1 = g.createEdge(g.getGraphClass().getEdgeClass("TestEdge"), v1,
				v2);

		// accessing default values
		assertEquals(true, e1.getAttribute("boolEdge"));
		assertEquals(
				JGraLab.vector().plus(JGraLab.vector().plus(true))
						.plus(JGraLab.vector().plus(false))
						.plus(JGraLab.vector().plus(true)),
				e1.getAttribute("complexListEdge"));
		assertEquals(
				JGraLab.map()
						.plus(JGraLab.vector().plus(true),
								JGraLab.set().plus(true))
						.plus(JGraLab.vector().plus(false),
								JGraLab.set().plus(false)),
				e1.getAttribute("complexMapEdge"));
		assertEquals(
				JGraLab.set().plus(JGraLab.set().plus(true))
						.plus(JGraLab.set().plus(false)),
				e1.getAttribute("complexSetEdge"));
		assertEquals((Double) 1.1d, e1.getAttribute("doubleEdge"));
		assertEquals("FIRST", e1.getAttribute("enumEdge"));
		assertEquals((Integer) 1, e1.getAttribute("intEdge"));
		assertEquals(JGraLab.vector().plus(true).plus(false).plus(true),
				e1.getAttribute("listEdge"));
		assertEquals((Long) 1l, e1.getAttribute("longEdge"));
		assertEquals(JGraLab.map().plus(1, true).plus(2, false).plus(3, true),
				e1.getAttribute("mapEdge"));
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
						.plus("setRecord", JGraLab.set().plus(true).plus(false))
						.plus("stringRecord", "test"),
				e1.getAttribute("recordEdge"));
		assertEquals(JGraLab.set().plus(true).plus(false),
				e1.getAttribute("setEdge"));
		assertEquals("test", e1.getAttribute("stringEdge"));

		// changing values
		e1.setAttribute("boolEdge", false);
		assertEquals(false, e1.getAttribute("boolEdge"));
		e1.setAttribute(
				"complexListEdge",
				JGraLab.vector().plus(JGraLab.vector().plus(false))
						.plus(JGraLab.vector().plus(false)));
		assertEquals(
				JGraLab.vector().plus(JGraLab.vector().plus(false))
						.plus(JGraLab.vector().plus(false)),
				e1.getAttribute("complexListEdge"));
		e1.setAttribute(
				"complexMapEdge",
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
				e1.getAttribute("complexMapEdge"));
		e1.setAttribute("complexSetEdge",
				JGraLab.set().plus(JGraLab.set().plus(false)));
		assertEquals(JGraLab.set().plus(JGraLab.set().plus(false)),
				e1.getAttribute("complexSetEdge"));
		e1.setAttribute("doubleEdge", 2.2d);
		assertEquals((Double) 2.2d, e1.getAttribute("doubleEdge"));
		e1.setAttribute("enumEdge", "SECOND");
		assertEquals("SECOND", e1.getAttribute("enumEdge"));
		e1.setAttribute("intEdge", 42);
		assertEquals((Integer) 42, e1.getAttribute("intEdge"));
		e1.setAttribute("listEdge", JGraLab.vector().plus(false).plus(false)
				.plus(true));
		assertEquals(JGraLab.vector().plus(false).plus(false).plus(true),
				e1.getAttribute("listEdge"));
		e1.setAttribute("longEdge", 987654321l);
		assertEquals((Long) 987654321l, e1.getAttribute("longEdge"));
		e1.setAttribute("mapEdge", JGraLab.map().plus(42, true).plus(24, false));
		assertEquals(JGraLab.map().plus(42, true).plus(24, false),
				e1.getAttribute("mapEdge"));
		e1.setAttribute(
				"recordEdge",
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
								JGraLab.map().plus(42, true).plus(24, false))
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
								JGraLab.map().plus(42, true).plus(24, false))
						.plus("setRecord", JGraLab.set().plus(false))
						.plus("stringRecord", "more test"),
				e1.getAttribute("recordEdge"));
		e1.setAttribute("setEdge", JGraLab.set().plus(true));
		assertEquals(JGraLab.set().plus(true), e1.getAttribute("setEdge"));
		e1.setAttribute("stringEdge", "some String");
		assertEquals("some String", e1.getAttribute("stringEdge"));
	}

	// Test setting an attribute that doesn't exist
	@Test(expected = GraphException.class)
	public void testAccessAttributesFailure1() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
				"TestVertex"));
		Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass(
				"TestVertex"));
		Edge e1 = g.createEdge(g.getGraphClass().getEdgeClass("TestEdge"), v1,
				v2);
		e1.setAttribute("MapEdge", JGraLab.map());
	}

	@Test(expected = GraphException.class)
	public void testAccessAttributesFailure2() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
				"TestVertex"));
		Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass(
				"TestVertex"));
		Edge e1 = g.createEdge(g.getGraphClass().getEdgeClass("TestEdge"), v1,
				v2);
		e1.setAttribute("SapEdge", JGraLab.set().plus(false));
	}

	// Test setting attributes with values that don't conform to their domain
	@Test(expected = ClassCastException.class)
	public void testAccessAttributesFailure3() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
				"TestVertex"));
		Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass(
				"TestVertex"));
		Edge e1 = g.createEdge(g.getGraphClass().getEdgeClass("TestEdge"), v1,
				v2);
		e1.setAttribute("mapEdge", JGraLab.set());
	}

	@Test
	public void testGetNextEdge() throws GraphIOException {
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
	}

	@Test
	public void testGetNextIncidence() throws GraphIOException {
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

		assertEquals(edges[4].getReversedEdge(),
				edges[0].getNextIncidence(edgeClasses[4], EdgeDirection.IN));
		assertEquals(edges[4].getReversedEdge(),
				edges[3].getNextIncidence(edgeClasses[4], EdgeDirection.IN));
		assertNull(edges[1].getNextIncidence(edgeClasses[1], EdgeDirection.IN));
		assertNull(edges[0].getNextIncidence(edgeClasses[0], true));
	}

	@Test
	public void testGetAggregationKind() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "VertexTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);

		Vertex superNode = g.createVertex(g.getGraphClass().getVertexClass(
				"SuperNode"));
		Vertex subNode = g.createVertex(g.getGraphClass().getVertexClass(
				"SubNode"));
		Vertex doubleSubNode = g.createVertex(g.getGraphClass().getVertexClass(
				"DoubleSubNode"));

		Edge link = g.createEdge(g.getGraphClass().getEdgeClass("Link"),
				subNode, superNode);
		Edge linkBack = g.createEdge(
				g.getGraphClass().getEdgeClass("LinkBack"), superNode, subNode);
		Edge subLink = g.createEdge(g.getGraphClass().getEdgeClass("SubLink"),
				doubleSubNode, superNode);

		assertEquals(AggregationKind.COMPOSITE, subLink.getAggregationKind());
		assertEquals(AggregationKind.NONE, link.getAggregationKind());
		assertEquals(AggregationKind.SHARED, linkBack.getAggregationKind());
	}

	@Test
	public void testGetAlphaOmegaAggregationKind() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "VertexTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);

		Vertex superNode = g.createVertex(g.getGraphClass().getVertexClass(
				"SuperNode"));
		Vertex subNode = g.createVertex(g.getGraphClass().getVertexClass(
				"SubNode"));
		Vertex doubleSubNode = g.createVertex(g.getGraphClass().getVertexClass(
				"DoubleSubNode"));

		Edge link = g.createEdge(g.getGraphClass().getEdgeClass("Link"),
				subNode, superNode);
		Edge linkBack = g.createEdge(
				g.getGraphClass().getEdgeClass("LinkBack"), superNode, subNode);
		Edge subLink = g.createEdge(g.getGraphClass().getEdgeClass("SubLink"),
				doubleSubNode, superNode);

		assertEquals(AggregationKind.NONE, subLink.getAlphaAggregationKind());
		assertEquals(AggregationKind.COMPOSITE,
				subLink.getOmegaAggregationKind());
		assertEquals(AggregationKind.NONE, link.getAlphaAggregationKind());
		assertEquals(AggregationKind.NONE, link.getOmegaAggregationKind());
		assertEquals(AggregationKind.NONE, linkBack.getAlphaAggregationKind());
		assertEquals(AggregationKind.SHARED, linkBack.getOmegaAggregationKind());

	}

	// Tests parsing of attribute values (DefaultValueTestSchema.tg)
	@Test
	public void testReadAttributeValueFromString() throws GraphIOException {

		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		InternalEdge e = g.createEdge(g.getGraphClass()
				.getEdgeClass("TestEdge"), g.createVertex(g.getGraphClass()
				.getVertexClass("TestSubVertex")), g.createVertex(g
				.getGraphClass().getVertexClass("TestSubVertex")));
		for (Attribute a : e.getAttributedElementClass().getAttributeList()) {
			GenericGraphImplTest.testDefaultValue(e.getAttribute(a.getName()),
					a);
		}

		// parse values different from the default ones
		e.readAttributeValueFromString("boolEdge", "f");
		assertEquals(false, e.getAttribute("boolEdge"));
		e.readAttributeValueFromString("complexListEdge", "[[f]]");
		assertEquals(JGraLab.vector().plus(JGraLab.vector().plus(false)),
				e.getAttribute("complexListEdge"));
		e.readAttributeValueFromString("complexMapEdge",
				"{[t t] - {f} [f f] - {t f}}");
		assertEquals(
				JGraLab.map()
						.plus(JGraLab.vector().plus(true).plus(true),
								JGraLab.set().plus(false))
						.plus(JGraLab.vector().plus(false).plus(false),
								JGraLab.set().plus(true).plus(false)),
				e.getAttribute("complexMapEdge"));
		e.readAttributeValueFromString("complexSetEdge", "{{f}}");
		assertEquals(JGraLab.set().plus(JGraLab.set().plus(false)),
				e.getAttribute("complexSetEdge"));
		e.readAttributeValueFromString("doubleEdge", "12.34");
		assertEquals((Double) 12.34d, e.getAttribute("doubleEdge"));
		e.readAttributeValueFromString("enumEdge", "SECOND");
		assertEquals("SECOND", e.getAttribute("enumEdge"));
		e.readAttributeValueFromString("intEdge", "42");
		assertEquals((Integer) 42, e.getAttribute("intEdge"));
		e.readAttributeValueFromString("listEdge", "[t t]");
		assertEquals(JGraLab.vector().plus(true).plus(true),
				e.getAttribute("listEdge"));
		e.readAttributeValueFromString("longEdge", "987654321");
		assertEquals((Long) 987654321l, e.getAttribute("longEdge"));
		e.readAttributeValueFromString("mapEdge", "{1 - f 2 - t}");
		assertEquals(JGraLab.map().plus(1, false).plus(2, true),
				e.getAttribute("mapEdge"));
		e.readAttributeValueFromString("recordEdge",
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
				e.getAttribute("recordEdge"));
		e.readAttributeValueFromString("setEdge", "{f}");
		assertEquals(JGraLab.set().plus(false), e.getAttribute("setEdge"));
		e.readAttributeValueFromString("stringEdge", "\"some String\"");
		assertEquals("some String", e.getAttribute("stringEdge"));

	}

	@Test
	public void testReadAttributeValues() throws GraphIOException {

		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		InternalEdge e = g.createEdge(g.getGraphClass()
				.getEdgeClass("TestEdge"), g.createVertex(g.getGraphClass()
				.getVertexClass("TestSubVertex")), g.createVertex(g
				.getGraphClass().getVertexClass("TestSubVertex")));
		for (Attribute a : e.getAttributedElementClass().getAttributeList()) {
			GenericGraphImplTest.testDefaultValue(e.getAttribute(a.getName()),
					a);
		}

		e.readAttributeValues(GraphIO
				.createStringReader(
						"f "
								+ "[[f]] "
								+ "{[t t] - {f} [f f] - {t f}} "
								+ "{{f}} "
								+ "12.34 "
								+ "SECOND "
								+ "42"
								+ "[t t] "
								+ "987654321 "
								+ "{1 - f 2 - t} "
								+ "(f 2.2 THIRD 42 [f t] 987654321 {1 - f 2 - t} {t} \"some String\") "
								+ "{f} " + "\"some String\"", e.getSchema()));

		// parse values different from the default ones
		assertEquals(false, e.getAttribute("boolEdge"));
		assertEquals(JGraLab.vector().plus(JGraLab.vector().plus(false)),
				e.getAttribute("complexListEdge"));
		assertEquals(
				JGraLab.map()
						.plus(JGraLab.vector().plus(true).plus(true),
								JGraLab.set().plus(false))
						.plus(JGraLab.vector().plus(false).plus(false),
								JGraLab.set().plus(true).plus(false)),
				e.getAttribute("complexMapEdge"));
		assertEquals(JGraLab.set().plus(JGraLab.set().plus(false)),
				e.getAttribute("complexSetEdge"));
		assertEquals((Double) 12.34d, e.getAttribute("doubleEdge"));
		assertEquals("SECOND", e.getAttribute("enumEdge"));
		assertEquals((Integer) 42, e.getAttribute("intEdge"));
		assertEquals(JGraLab.vector().plus(true).plus(true),
				e.getAttribute("listEdge"));
		assertEquals((Long) 987654321l, e.getAttribute("longEdge"));
		assertEquals(JGraLab.map().plus(1, false).plus(2, true),
				e.getAttribute("mapEdge"));
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
				e.getAttribute("recordEdge"));
		assertEquals(JGraLab.set().plus(false), e.getAttribute("setEdge"));
		assertEquals("some String", e.getAttribute("stringEdge"));

	}

	@Test
	public void testWriteAttributeValueToString() throws GraphIOException,
			NoSuchAttributeException, IOException {

		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		InternalEdge e = g.createEdge(g.getGraphClass()
				.getEdgeClass("TestEdge"), g.createVertex(g.getGraphClass()
				.getVertexClass("TestSubVertex")), g.createVertex(g
				.getGraphClass().getVertexClass("TestSubVertex")));
		for (Attribute a : e.getAttributedElementClass().getAttributeList()) {
			GenericGraphImplTest.testDefaultValue(e.getAttribute(a.getName()),
					a);
		}
		// Default values count as unset, so everything's u
		assertEquals("u", e.writeAttributeValueToString("boolEdge"));
		assertEquals("u", e.writeAttributeValueToString("complexListEdge"));
		assertEquals("u", e.writeAttributeValueToString("complexMapEdge"));
		assertEquals("u", e.writeAttributeValueToString("complexSetEdge"));
		assertEquals("u", e.writeAttributeValueToString("doubleEdge"));
		assertEquals("u", e.writeAttributeValueToString("enumEdge"));
		assertEquals("u", e.writeAttributeValueToString("intEdge"));
		assertEquals("u", e.writeAttributeValueToString("listEdge"));
		assertEquals("u", e.writeAttributeValueToString("longEdge"));
		assertEquals("u", e.writeAttributeValueToString("mapEdge"));
		assertEquals("u", e.writeAttributeValueToString("recordEdge"));
		assertEquals("u", e.writeAttributeValueToString("setEdge"));
		assertEquals("u", e.writeAttributeValueToString("stringEdge"));

		// Now explicity set every attribute to its current default value, so
		// that it becomes set.
		for (Attribute a : e.getAttributedElementClass().getAttributeList()) {
			e.setAttribute(a.getName(), e.getAttribute(a.getName()));
		}
		assertEquals("t", e.writeAttributeValueToString("boolEdge"));
		assertEquals("[[t][f][t]]",
				e.writeAttributeValueToString("complexListEdge"));
		assertEquals("{[t]-{t}[f]-{f}}",
				e.writeAttributeValueToString("complexMapEdge"));
		assertEquals("{{t}{f}}",
				e.writeAttributeValueToString("complexSetEdge"));
		assertEquals("1.1", e.writeAttributeValueToString("doubleEdge"));
		assertEquals("FIRST", e.writeAttributeValueToString("enumEdge"));
		assertEquals("1", e.writeAttributeValueToString("intEdge"));
		assertEquals("[t f t]", e.writeAttributeValueToString("listEdge"));
		assertEquals("1", e.writeAttributeValueToString("longEdge"));
		assertEquals("{1 - t 2 - f 3 - t}",
				e.writeAttributeValueToString("mapEdge"));
		assertEquals("(t 1.1 FIRST 1[t f t]1{1 - t 2 - f 3 - t}{t f}\"test\")",
				e.writeAttributeValueToString("recordEdge"));
		assertEquals("{t f}", e.writeAttributeValueToString("setEdge"));
		assertEquals("\"test\"", e.writeAttributeValueToString("stringEdge"));

	}

	@Test
	public void testWriteAttributeValues() throws GraphIOException, IOException {

		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		InternalEdge e = g.createEdge(g.getGraphClass()
				.getEdgeClass("TestEdge"), g.createVertex(g.getGraphClass()
				.getVertexClass("TestSubVertex")), g.createVertex(g
				.getGraphClass().getVertexClass("TestSubVertex")));
		for (Attribute a : e.getAttributedElementClass().getAttributeList()) {
			GenericGraphImplTest.testDefaultValue(e.getAttribute(a.getName()),
					a);
		}

		GraphIO io = GraphIO.createStringWriter(e.getSchema());
		e.writeAttributeValues(io);
		assertEquals("u u u u u u u u u u u u u", io.getStringWriterResult());

		// Now explicity set every attribute to its current default value,
		// so
		// that it becomes set.
		for (Attribute a : e.getAttributedElementClass().getAttributeList()) {
			e.setAttribute(a.getName(), e.getAttribute(a.getName()));
		}
		io = GraphIO.createStringWriter(e.getSchema());
		e.writeAttributeValues(io);
		assertEquals("t" + "[[t][f][t]]" + "{[t]-{t}[f]-{f}}" + "{{t}{f}}"
				+ "1.1 " + "FIRST " + "1" + "[t f t]" + "1"
				+ "{1 - t 2 - f 3 - t}"
				+ "(t 1.1 FIRST 1[t f t]1{1 - t 2 - f 3 - t}{t f}\"test\")"
				+ "{t f}\"test\"", io.getStringWriterResult());

	}

	@Test
	public void testAccessAttributesAfterSaveAndLoad() throws GraphIOException,
			IOException {
		Schema schema = GraphIO
				.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
						+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC);
		Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
				"TestVertex"));
		Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass(
				"TestVertex"));
		Edge e = g.createEdge(g.getGraphClass().getEdgeClass("TestEdge"), v1,
				v2);
		File tmp = File.createTempFile("graph", "tg");
		g.save(tmp.getPath());
		g = GraphIO.loadGraphFromFile(tmp.getPath(),
				ImplementationType.GENERIC, null);
		e = g.getFirstEdge();

		for (Attribute a : e.getAttributedElementClass().getAttributeList()) {
			GraphIO io = GraphIO.createStringWriter(g.getSchema());
			a.getDomain().serializeGenericAttribute(io,
					e.getAttribute(a.getName()));
			assertEquals(a.getDefaultValueAsString(),
					io.getStringWriterResult());
		}
	}
}
