/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

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
import de.uni_koblenz.jgralab.impl.RecordImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class GenericVertexImplTest {
	@Test
	public void testAccessAttributes() throws GraphIOException {

		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v = g.createVertex(g.getGraphClass()
				.getVertexClass("TestVertex"));

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
		assertEquals(JGraLab.map().plus(1, true).plus(2, false).plus(3, true),
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
						.plus("setRecord", JGraLab.set().plus(true).plus(false))
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
		assertEquals(
				JGraLab.vector().plus(JGraLab.vector().plus(false))
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
		v.setAttribute("listVertex", JGraLab.vector().plus(false).plus(false)
				.plus(true));
		assertEquals(JGraLab.vector().plus(false).plus(false).plus(true),
				v.getAttribute("listVertex"));
		v.setAttribute("longVertex", 987654321l);
		assertEquals(987654321l, v.getAttribute("longVertex"));
		v.setAttribute("mapVertex", JGraLab.map().plus(42, true)
				.plus(24, false));
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
				v.getAttribute("recordVertex"));
		v.setAttribute("setVertex", JGraLab.set().plus(true));
		assertEquals(JGraLab.set().plus(true), v.getAttribute("setVertex"));
		v.setAttribute("stringVertex", "some String");
		assertEquals("some String", v.getAttribute("stringVertex"));
	}

	// Test setting an attribute that doesn't exist
	@Test(expected = GraphException.class)
	public void testAccessAttributesFailure1() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v = g.createVertex(g.getGraphClass()
				.getVertexClass("TestVertex"));
		v.getAttribute("StringVertex"); // "stringVertex"!
	}

	@Test(expected = GraphException.class)
	public void testAccessAttributesFailure2() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v = g.createVertex(g.getGraphClass()
				.getVertexClass("TestVertex"));
		v.setAttribute("abcd", 123);
	}

	// Test setting attributes with values that don't conform to their domain
	@Test(expected = ClassCastException.class)
	public void testAccessAttributesFailure3() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v = g.createVertex(g.getGraphClass()
				.getVertexClass("TestVertex"));
		v.setAttribute("stringVertex", 42);
	}

	// Test type specific getDegree Methods (VertexTestSchema.tg)
	@Test
	public void testGetDegree() throws GraphIOException {
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

		assertEquals(0, vertices[0].getDegree(edgeClasses[0]));
		assertEquals(0,
				vertices[0].getDegree(edgeClasses[0], EdgeDirection.OUT));
		assertEquals(0, vertices[1].getDegree(edgeClasses[0]));
		assertEquals(0, vertices[1].getDegree(edgeClasses[0], EdgeDirection.IN));

		assertEquals(0, vertices[2].getDegree(edgeClasses[1]));
		assertEquals(0,
				vertices[2].getDegree(edgeClasses[1], EdgeDirection.OUT));
		assertEquals(0, vertices[3].getDegree(edgeClasses[1]));
		assertEquals(0, vertices[3].getDegree(edgeClasses[1], EdgeDirection.IN));

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
		assertEquals(3, vertices[1].getDegree(edgeClasses[0], EdgeDirection.IN));

		assertEquals(1, vertices[2].getDegree(edgeClasses[1]));
		assertEquals(1,
				vertices[2].getDegree(edgeClasses[1], EdgeDirection.OUT));
		assertEquals(1, vertices[3].getDegree(edgeClasses[1]));
		assertEquals(1, vertices[3].getDegree(edgeClasses[1], EdgeDirection.IN));

		assertEquals(1, vertices[2].getDegree(edgeClasses[2]));
		assertEquals(1,
				vertices[2].getDegree(edgeClasses[2], EdgeDirection.OUT));
		assertEquals(1, vertices[3].getDegree(edgeClasses[2]));
		assertEquals(1, vertices[3].getDegree(edgeClasses[2], EdgeDirection.IN));

		assertEquals(2, vertices[0].getDegree(edgeClasses[3]));
		assertEquals(2,
				vertices[0].getDegree(edgeClasses[3], EdgeDirection.OUT));
		assertEquals(2, vertices[1].getDegree(edgeClasses[3]));
		assertEquals(2, vertices[1].getDegree(edgeClasses[3], EdgeDirection.IN));

		assertEquals(2, vertices[0].getDegree(edgeClasses[4]));
		assertEquals(1,
				vertices[0].getDegree(edgeClasses[4], EdgeDirection.OUT));
		assertEquals(1, vertices[0].getDegree(edgeClasses[4], EdgeDirection.IN));

		assertEquals(1, vertices[4].getDegree(edgeClasses[5]));
		assertEquals(1,
				vertices[4].getDegree(edgeClasses[5], EdgeDirection.OUT));
		assertEquals(1, vertices[5].getDegree(edgeClasses[5]));
		assertEquals(1, vertices[5].getDegree(edgeClasses[5], EdgeDirection.IN));

		assertEquals(1, vertices[0].getDegree(edgeClasses[6]));
		assertEquals(1,
				vertices[0].getDegree(edgeClasses[6], EdgeDirection.OUT));
		assertEquals(1, vertices[1].getDegree(edgeClasses[6]));
		assertEquals(1, vertices[1].getDegree(edgeClasses[6], EdgeDirection.IN));
	}

	// Test type specific getNextVertex method
	@Test
	public void testGetNextVertex() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
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
	}

	// Test type specific getFirstIncidence Method
	@Test
	public void testGetFirstIncidence() throws GraphIOException {
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

		for (Vertex v : vertices) {
			for (EdgeClass ec : edgeClasses) {
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
		assertEquals(vertices[1].getFirstIncidence(edgeClasses[0]),
				edges[0].getReversedEdge());
		assertEquals(vertices[1].getFirstIncidence(edgeClasses[6]),
				edges[6].getReversedEdge());
		assertEquals(vertices[2].getFirstIncidence(edgeClasses[1]), edges[1]);
		assertEquals(vertices[3].getFirstIncidence(edgeClasses[1]),
				edges[1].getReversedEdge());
		assertEquals(vertices[4].getFirstIncidence(edgeClasses[5]), edges[5]);
		assertEquals(vertices[5].getFirstIncidence(edgeClasses[5]),
				edges[5].getReversedEdge());

		assertEquals(
				vertices[0].getFirstIncidence(edgeClasses[4], EdgeDirection.IN),
				edges[4].getReversedEdge());
		assertEquals(vertices[2].getFirstIncidence(edgeClasses[0],
				EdgeDirection.OUT), edges[1]);
	}

	@Test
	public void testAccessAttributesAfterSaveAndLoad() throws GraphIOException,
			IOException {
		Schema schema = GraphIO
				.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
						+ "DefaultValueTestSchema.tg");
		Graph g = schema.createGraph(ImplementationType.GENERIC);
		Vertex v = g.createVertex(g.getGraphClass()
				.getVertexClass("TestVertex"));
		File tmp = File.createTempFile("graph", "tg");
		g.save(tmp.getPath());
		g = GraphIO.loadGraphFromFile(tmp.getPath(),
				ImplementationType.GENERIC, null);
		v = g.getFirstVertex();

		for (Attribute a : v.getAttributedElementClass().getAttributeList()) {
			GraphIO io = GraphIO.createStringWriter(g.getSchema());
			a.getDomain().serializeGenericAttribute(io,
					v.getAttribute(a.getName()));
			assertEquals(a.getDefaultValueAsString(),
					io.getStringWriterResult());
		}
	}

	// Test type specific incidences() method
	@Test
	public void testIncidences() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "VertexTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);

		Vertex a = g.createVertex(g.getGraphClass().getVertexClass("A"));
		Vertex b = g.createVertex(g.getGraphClass().getVertexClass("B"));
		Vertex c = g.createVertex(g.getGraphClass().getVertexClass("C"));
		Vertex d = g.createVertex(g.getGraphClass().getVertexClass("D"));
		Vertex c2 = g.createVertex(g.getGraphClass().getVertexClass("C2"));
		Vertex d2 = g.createVertex(g.getGraphClass().getVertexClass("D2"));

		HashMap<String, EdgeClass> ecs = new HashMap<String, EdgeClass>();
		ecs.put("E", g.getGraphClass().getEdgeClass("E"));
		ecs.put("F", g.getGraphClass().getEdgeClass("F"));
		ecs.put("G", g.getGraphClass().getEdgeClass("G"));
		ecs.put("H", g.getGraphClass().getEdgeClass("H"));
		ecs.put("I", g.getGraphClass().getEdgeClass("I"));
		ecs.put("J", g.getGraphClass().getEdgeClass("J"));
		ecs.put("K", g.getGraphClass().getEdgeClass("K"));
		Iterator<Edge> iterator = a.incidences(ecs.get("E")).iterator();
		assertFalse(iterator.hasNext());
		iterator = a.incidences(ecs.get("E"), EdgeDirection.OUT).iterator();
		assertFalse(iterator.hasNext());
		iterator = c.incidences(ecs.get("F"), EdgeDirection.OUT).iterator();
		assertFalse(iterator.hasNext());

		Edge[] edges = new Edge[7];
		edges[0] = g.createEdge(ecs.get("E"), a, b);
		edges[1] = g.createEdge(ecs.get("F"), c, d);
		edges[2] = g.createEdge(ecs.get("G"), c, d);
		edges[3] = g.createEdge(ecs.get("H"), a, b);
		edges[4] = g.createEdge(ecs.get("I"), a, a);
		edges[5] = g.createEdge(ecs.get("J"), c2, d2);
		edges[6] = g.createEdge(ecs.get("K"), a, b);

		Edge[] incidentEdges = new Edge[] { edges[0], edges[3], edges[6] };
		Iterator<Edge> ii1 = a.incidences(ecs.get("E")).iterator();
		Iterator<Edge> ii2 = a.incidences(ecs.get("E"), EdgeDirection.OUT)
				.iterator();
		for (int i = 0; i < incidentEdges.length; i++) {
			Edge e1 = ii1.next();
			Edge e2 = ii2.next();
			assertEquals(incidentEdges[i], e1);
			assertEquals(incidentEdges[i], e2);
		}
		assertFalse(ii1.hasNext());
		assertFalse(ii2.hasNext());

		incidentEdges = new Edge[] { edges[3], edges[6] };
		ii1 = a.incidences(ecs.get("H")).iterator();
		ii2 = a.incidences(ecs.get("H"), EdgeDirection.OUT).iterator();
		for (int i = 0; i < incidentEdges.length; i++) {
			Edge e1 = ii1.next();
			Edge e2 = ii2.next();
			assertEquals(incidentEdges[i], e1);
			assertEquals(incidentEdges[i], e2);
		}
		assertFalse(ii1.hasNext());
		assertFalse(ii2.hasNext());

		incidentEdges = new Edge[] { edges[0].getReversedEdge(),
				edges[3].getReversedEdge(), edges[6].getReversedEdge() };
		ii1 = b.incidences(ecs.get("E")).iterator();
		ii2 = b.incidences(ecs.get("E"), EdgeDirection.IN).iterator();
		for (int i = 0; i < incidentEdges.length; i++) {
			Edge e1 = ii1.next();
			Edge e2 = ii2.next();
			assertEquals(incidentEdges[i], e1);
			assertEquals(incidentEdges[i], e2);
		}
		assertFalse(ii1.hasNext());
		assertFalse(ii2.hasNext());

		incidentEdges = new Edge[] { edges[1] };
		ii1 = c.incidences(ecs.get("F")).iterator();
		ii2 = c.incidences(ecs.get("F"), EdgeDirection.OUT).iterator();
		for (int i = 0; i < incidentEdges.length; i++) {
			Edge e1 = ii1.next();
			Edge e2 = ii2.next();
			assertEquals(incidentEdges[i], e1);
			assertEquals(incidentEdges[i], e2);
		}
		assertFalse(ii1.hasNext());
		assertFalse(ii2.hasNext());

		incidentEdges = new Edge[] { edges[2] };
		ii1 = c.incidences(ecs.get("G")).iterator();
		ii2 = c.incidences(ecs.get("G"), EdgeDirection.OUT).iterator();
		for (int i = 0; i < incidentEdges.length; i++) {
			Edge e1 = ii1.next();
			Edge e2 = ii2.next();
			assertEquals(incidentEdges[i], e1);
			assertEquals(incidentEdges[i], e2);
		}
		assertFalse(ii1.hasNext());
		assertFalse(ii2.hasNext());

		incidentEdges = new Edge[] { edges[4], edges[4].getReversedEdge() };
		ii1 = a.incidences(ecs.get("I")).iterator();
		for (int i = 0; i < incidentEdges.length; i++) {
			Edge e1 = ii1.next();
			assertEquals(incidentEdges[i], e1);
		}
		assertFalse(ii1.hasNext());

		incidentEdges = new Edge[] { edges[5] };
		ii1 = c2.incidences(ecs.get("J")).iterator();
		ii2 = c2.incidences(ecs.get("J"), EdgeDirection.OUT).iterator();
		for (int i = 0; i < incidentEdges.length; i++) {
			Edge e1 = ii1.next();
			Edge e2 = ii2.next();
			assertEquals(incidentEdges[i], e1);
			assertEquals(incidentEdges[i], e2);
		}
		assertFalse(ii1.hasNext());
		assertFalse(ii2.hasNext());
	}

	// Tests parsing of attribute values (DefaultValueTestSchema.tg)
	@Test
	public void testReadAttributeValueFromString() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v = g.createVertex(g.getGraphClass()
				.getVertexClass("TestVertex"));
		for (Attribute a : v.getAttributedElementClass().getAttributeList()) {
			GenericGraphImplTest.testDefaultValue(v.getAttribute(a.getName()),
					a);
		}

		// parse values different from the default ones
		v.readAttributeValueFromString("boolVertex", "f");
		assertEquals(false, v.getAttribute("boolVertex"));
		v.readAttributeValueFromString("complexListVertex", "[[f]]");
		assertEquals(JGraLab.vector().plus(JGraLab.vector().plus(false)),
				v.getAttribute("complexListVertex"));
		v.readAttributeValueFromString("complexMapVertex",
				"{[t t] - {f} [f f] - {t f}}");
		assertEquals(
				JGraLab.map()
						.plus(JGraLab.vector().plus(true).plus(true),
								JGraLab.set().plus(false))
						.plus(JGraLab.vector().plus(false).plus(false),
								JGraLab.set().plus(true).plus(false)),
				v.getAttribute("complexMapVertex"));
		v.readAttributeValueFromString("complexSetVertex", "{{f}}");
		assertEquals(JGraLab.set().plus(JGraLab.set().plus(false)),
				v.getAttribute("complexSetVertex"));
		v.readAttributeValueFromString("doubleVertex", "12.34");
		assertEquals(12.34d, v.getAttribute("doubleVertex"));
		v.readAttributeValueFromString("enumVertex", "SECOND");
		assertEquals("SECOND", v.getAttribute("enumVertex"));
		v.readAttributeValueFromString("intVertex", "42");
		assertEquals(42, v.getAttribute("intVertex"));
		v.readAttributeValueFromString("listVertex", "[t t]");
		assertEquals(JGraLab.vector().plus(true).plus(true),
				v.getAttribute("listVertex"));
		v.readAttributeValueFromString("longVertex", "987654321");
		assertEquals(987654321l, v.getAttribute("longVertex"));
		v.readAttributeValueFromString("mapVertex", "{1 - f 2 - t}");
		assertEquals(JGraLab.map().plus(1, false).plus(2, true),
				v.getAttribute("mapVertex"));
		v.readAttributeValueFromString("recordVertex",
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
				v.getAttribute("recordVertex"));
		v.readAttributeValueFromString("setVertex", "{f}");
		assertEquals(JGraLab.set().plus(false), v.getAttribute("setVertex"));
		v.readAttributeValueFromString("stringVertex", "\"some String\"");
		assertEquals("some String", v.getAttribute("stringVertex"));
	}

	@Test
	public void testReadAttributeValues() throws GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v = g.createVertex(g.getGraphClass()
				.getVertexClass("TestVertex"));
		for (Attribute a : v.getAttributedElementClass().getAttributeList()) {
			GenericGraphImplTest.testDefaultValue(v.getAttribute(a.getName()),
					a);
		}

		v.readAttributeValues(GraphIO
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
								+ "{f} " + "\"some String\"", v.getSchema()));

		// parse values different from the default ones
		assertEquals(false, v.getAttribute("boolVertex"));
		assertEquals(JGraLab.vector().plus(JGraLab.vector().plus(false)),
				v.getAttribute("complexListVertex"));
		assertEquals(
				JGraLab.map()
						.plus(JGraLab.vector().plus(true).plus(true),
								JGraLab.set().plus(false))
						.plus(JGraLab.vector().plus(false).plus(false),
								JGraLab.set().plus(true).plus(false)),
				v.getAttribute("complexMapVertex"));
		assertEquals(JGraLab.set().plus(JGraLab.set().plus(false)),
				v.getAttribute("complexSetVertex"));
		assertEquals(12.34d, v.getAttribute("doubleVertex"));
		assertEquals("SECOND", v.getAttribute("enumVertex"));
		assertEquals(42, v.getAttribute("intVertex"));
		assertEquals(JGraLab.vector().plus(true).plus(true),
				v.getAttribute("listVertex"));
		assertEquals(987654321l, v.getAttribute("longVertex"));
		assertEquals(JGraLab.map().plus(1, false).plus(2, true),
				v.getAttribute("mapVertex"));
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
				v.getAttribute("recordVertex"));
		assertEquals(JGraLab.set().plus(false), v.getAttribute("setVertex"));
		assertEquals("some String", v.getAttribute("stringVertex"));
	}

	@Test
	public void testWriteAttributeValueToString()
			throws NoSuchAttributeException, IOException, GraphIOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v = g.createVertex(g.getGraphClass().getVertexClass(
				"TestSubVertex"));
		for (Attribute a : v.getAttributedElementClass().getAttributeList()) {
			GenericGraphImplTest.testDefaultValue(v.getAttribute(a.getName()),
					a);
		}
		// Default values count as unset, so everything's u
		assertEquals("u", v.writeAttributeValueToString("boolVertex"));
		assertEquals("u", v.writeAttributeValueToString("complexListVertex"));
		assertEquals("u", v.writeAttributeValueToString("complexMapVertex"));
		assertEquals("u", v.writeAttributeValueToString("complexSetVertex"));
		assertEquals("u", v.writeAttributeValueToString("doubleVertex"));
		assertEquals("u", v.writeAttributeValueToString("enumVertex"));
		assertEquals("u", v.writeAttributeValueToString("intVertex"));
		assertEquals("u", v.writeAttributeValueToString("listVertex"));
		assertEquals("u", v.writeAttributeValueToString("longVertex"));
		assertEquals("u", v.writeAttributeValueToString("mapVertex"));
		assertEquals("u", v.writeAttributeValueToString("recordVertex"));
		assertEquals("u", v.writeAttributeValueToString("setVertex"));
		assertEquals("u", v.writeAttributeValueToString("stringVertex"));

		// Now explicity set every attribute to its current default value, so
		// that it becomes set.
		for (Attribute a : v.getAttributedElementClass().getAttributeList()) {
			v.setAttribute(a.getName(), v.getAttribute(a.getName()));
		}
		assertEquals("t", v.writeAttributeValueToString("boolVertex"));
		assertEquals("[[t][f][t]]",
				v.writeAttributeValueToString("complexListVertex"));
		assertEquals("{[t]-{t}[f]-{f}}",
				v.writeAttributeValueToString("complexMapVertex"));
		assertEquals("{{t}{f}}",
				v.writeAttributeValueToString("complexSetVertex"));
		assertEquals("1.1", v.writeAttributeValueToString("doubleVertex"));
		assertEquals("FIRST", v.writeAttributeValueToString("enumVertex"));
		assertEquals("1", v.writeAttributeValueToString("intVertex"));
		assertEquals("[t f t]", v.writeAttributeValueToString("listVertex"));
		assertEquals("1", v.writeAttributeValueToString("longVertex"));
		assertEquals("{1 - t 2 - f 3 - t}",
				v.writeAttributeValueToString("mapVertex"));
		assertEquals("(t 1.1 FIRST 1[t f t]1{1 - t 2 - f 3 - t}{t f}\"test\")",
				v.writeAttributeValueToString("recordVertex"));
		assertEquals("{t f}", v.writeAttributeValueToString("setVertex"));
		assertEquals("\"test\"", v.writeAttributeValueToString("stringVertex"));
	}

	@Test
	public void testWriteAttributeValues() throws GraphIOException, IOException {
		Schema s = GraphIO.loadSchemaFromFile(GenericGraphImplTest.SCHEMAFOLDER
				+ "DefaultValueTestSchema.tg");
		Graph g = s.createGraph(ImplementationType.GENERIC);
		Vertex v = g.createVertex(g.getGraphClass()
				.getVertexClass("TestVertex"));
		for (Attribute a : v.getAttributedElementClass().getAttributeList()) {
			GenericGraphImplTest.testDefaultValue(v.getAttribute(a.getName()),
					a);
		}

		GraphIO io = GraphIO.createStringWriter(v.getSchema());
		v.writeAttributeValues(io);

		// Default values count as unset, so everything's u
		assertEquals("u u u u u u u u u u u u u", io.getStringWriterResult());

		// Now explicity set every attribute to its current default value,
		// so that it becomes set.
		for (Attribute a : v.getAttributedElementClass().getAttributeList()) {
			v.setAttribute(a.getName(), v.getAttribute(a.getName()));
		}
		io = GraphIO.createStringWriter(v.getSchema());
		v.writeAttributeValues(io);
		assertEquals("t" + "[[t][f][t]]" + "{[t]-{t}[f]-{f}}" + "{{t}{f}}"
				+ "1.1 " + "FIRST " + "1" + "[t f t]" + "1"
				+ "{1 - t 2 - f 3 - t}"
				+ "(t 1.1 FIRST 1[t f t]1{1 - t 2 - f 3 - t}{t f}\"test\")"
				+ "{t f}\"test\"", io.getStringWriterResult());
	}
}
