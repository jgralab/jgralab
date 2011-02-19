/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.schemas.vertextest.AbstractSuperNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.DoubleSubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.Link;
import de.uni_koblenz.jgralabtest.schemas.vertextest.LinkBack;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubLink;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SuperNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

@RunWith(Parameterized.class)
public class EdgeTest extends InstanceTest {

	private static final int RANDOM_EDGE_COUNT = 30;
	private static final int RANDOM_GRAPH_COUNT = 10;
	private static final int RANDOM_VERTEX_COUNT = 10;

	private final String ID = "EdgeTest";

	public EdgeTest(ImplementationType implementationType) {
		super(implementationType);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private VertexTestGraph g;
	private Random rand;

	/**
	 * Creates a new graph for each test.
	 */
	@Before
	public void setUp() {
		switch (implementationType) {
		case STANDARD:
			g = VertexTestSchema.instance().createVertexTestGraph();
			break;
		case TRANSACTION:
			g = VertexTestSchema.instance()
					.createVertexTestGraphWithTransactionSupport();
			break;
		case SAVEMEM:
			g = VertexTestSchema.instance()
					.createVertexTestGraphWithSavememSupport();
			break;
		case DATABASE:
			g = this.createVertexTestGraphWithDatabaseSupport();
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}

		rand = new Random(System.currentTimeMillis());
	}

	private VertexTestGraph createVertexTestGraphWithDatabaseSupport() {
		dbHandler.connectToDatabase();
		dbHandler.loadVertexTestSchemaIntoGraphDatabase();
		return dbHandler.createVertexTestGraphWithDatabaseSupport(ID);
	}

	@After
	public void tearDown() {
		if (implementationType == ImplementationType.DATABASE) {
			this.cleanAndCloseGraphDatabase();
		}
	}

	private void cleanAndCloseGraphDatabase() {
		// dbHandler.cleanDatabaseOfTestGraph(ID);
		// for (int i = 0; i < RANDOM_GRAPH_COUNT; i++) {
		// dbHandler.cleanDatabaseOfTestGraph(ID + i);
		// }
		// dbHandler.cleanDatabaseOfTestGraph("anotherGraph");
		// // this.cleanDatabaseOfTestSchema(VertexTestSchema.instance());
		// dbHandler.closeGraphdatabase();
		dbHandler.clearAllTables();
		dbHandler.closeGraphdatabase();
	}

	/*
	 * Test of the Interface Edge
	 */

	// tests for the method int getId();
	/**
	 * If you create several edges and you delete one, the next edge should get
	 * the id of the deleted edge. If you create a further edge it should get
	 * the next free id.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getIdTest0() throws Exception {
		createTransaction(g);
		DoubleSubNode v0 = g.createDoubleSubNode();
		DoubleSubNode v1 = g.createDoubleSubNode();
		Edge e0 = g.createLink(v0, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(1, e0.getId());
		commit(g);

		createTransaction(g);
		Edge e1 = g.createLink(v0, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(2, e1.getId());
		commit(g);

		createTransaction(g);
		g.deleteEdge(e0);
		commit(g);

		createTransaction(g);
		e0 = g.createLink(v1, v0);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(1, e0.getId());
		commit(g);

		createTransaction(g);
		e0 = g.createLink(v1, v0);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(3, e0.getId());
		commit(g);
	}

	// tests of the method Edge getNextEdge();
	// (tested in IncidenceListTest)

	// tests of the method Edge getPrevEdge();
	// (tested in IncidenceListTest)

	// tests of the method Edge getNextEdge(EdgeDirection orientation);
	/**
	 * There exists only one edge in the graph
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeTestEdgeDirection0() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v0 = g.createDoubleSubNode();
		DoubleSubNode v1 = g.createDoubleSubNode();
		Edge e0 = g.createLink(v0, v1);
		commit(g);

		createReadOnlyTransaction(g);
		// edges of vertex v0
		assertNull(e0.getNextIncidence(EdgeDirection.INOUT));
		assertNull(e0.getNextIncidence(EdgeDirection.OUT));
		assertNull(e0.getNextIncidence(EdgeDirection.IN));
		// edges of vertex v1
		assertNull(e0.getReversedEdge().getNextIncidence(EdgeDirection.INOUT));
		assertNull(e0.getReversedEdge().getNextIncidence(EdgeDirection.OUT));
		assertNull(e0.getReversedEdge().getNextIncidence(EdgeDirection.IN));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeTestEdgeDirection1() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLink(v1, v2);
		Edge e3 = g.createLink(v2, v1);
		Edge e4 = g.createSubLink(v1, v2);
		Edge e5 = g.createLinkBack(v2, v1);
		commit(g);

		createReadOnlyTransaction(g);
		// edges of vertex v0
		assertEquals(e2, e1.getNextIncidence(EdgeDirection.INOUT));
		assertEquals(e2, e1.getNextIncidence(EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e1
				.getNextIncidence(EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e2
				.getNextIncidence(EdgeDirection.INOUT));
		assertEquals(e4, e2.getNextIncidence(EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e2
				.getNextIncidence(EdgeDirection.IN));
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(
				EdgeDirection.INOUT));
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(
				EdgeDirection.OUT));
		assertEquals(e5.getReversedEdge(), e3.getReversedEdge()
				.getNextIncidence(EdgeDirection.IN));
		assertEquals(e5.getReversedEdge(), e4
				.getNextIncidence(EdgeDirection.INOUT));
		assertNull(e4.getNextIncidence(EdgeDirection.OUT));
		assertEquals(e5.getReversedEdge(), e4
				.getNextIncidence(EdgeDirection.IN));
		assertNull(e5.getNextIncidence(EdgeDirection.INOUT));
		assertNull(e5.getNextIncidence(EdgeDirection.OUT));
		assertNull(e5.getNextIncidence(EdgeDirection.IN));
		// edges of vertex v1
		assertEquals(e2.getReversedEdge(), e1.getReversedEdge()
				.getNextIncidence(EdgeDirection.INOUT));
		assertEquals(e3, e1.getReversedEdge().getNextIncidence(
				EdgeDirection.OUT));
		assertEquals(e2.getReversedEdge(), e1.getReversedEdge()
				.getNextIncidence(EdgeDirection.IN));
		assertEquals(e3, e2.getReversedEdge().getNextIncidence(
				EdgeDirection.INOUT));
		assertEquals(e3, e2.getReversedEdge().getNextIncidence(
				EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e2.getReversedEdge()
				.getNextIncidence(EdgeDirection.IN));
		assertEquals(e4.getReversedEdge(), e3
				.getNextIncidence(EdgeDirection.INOUT));
		assertEquals(e5, e3.getNextIncidence(EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e3
				.getNextIncidence(EdgeDirection.IN));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(
				EdgeDirection.INOUT));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(
				EdgeDirection.OUT));
		assertNull(e4.getReversedEdge().getNextIncidence(EdgeDirection.IN));
		assertNull(e5.getReversedEdge().getNextIncidence(EdgeDirection.INOUT));
		assertNull(e5.getReversedEdge().getNextIncidence(EdgeDirection.OUT));
		assertNull(e5.getReversedEdge().getNextIncidence(EdgeDirection.IN));
		commit(g);
	}

	/**
	 * Test in a randomly built graph
	 * 
	 * @throws Exception
	 */
	@Test
	public void getNextEdgeTestEdgeDirection2() throws Exception {
		for (int i = 0; i < RANDOM_GRAPH_COUNT; i++) {
			switch (implementationType) {
			case STANDARD:
				g = VertexTestSchema.instance().createVertexTestGraph();
				break;
			case TRANSACTION:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithTransactionSupport();
				break;
			case SAVEMEM:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithSavememSupport();
				break;
			case DATABASE:
				g = dbHandler.createVertexTestGraphWithDatabaseSupport(ID + i);
				break;
			default:
				fail("Implementation " + implementationType
						+ " not yet supported by this test.");
			}
			createTransaction(g);
			DoubleSubNode v0 = g.createDoubleSubNode();
			DoubleSubNode v1 = g.createDoubleSubNode();
			Edge[] edges = new Edge[RANDOM_EDGE_COUNT];
			Edge[] v0inout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] v0out = new Edge[RANDOM_EDGE_COUNT];
			Edge[] v0in = new Edge[RANDOM_EDGE_COUNT];
			Edge[] v1inout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] v1out = new Edge[RANDOM_EDGE_COUNT];
			Edge[] v1in = new Edge[RANDOM_EDGE_COUNT];
			int lastv0o = 0;
			int lastv0i = 0;
			int lastv1o = 0;
			int lastv1i = 0;
			int dir = 0;
			boolean first = true;
			for (int j = 0; j < RANDOM_EDGE_COUNT; j++) {
				dir = rand.nextInt(2);
				if (dir == 0) {
					Edge e = g.createLink(v0, v1);
					edges[j] = e;
					if (!first) {
						v0inout[j - 1] = e;
						v1inout[j - 1] = e.getReversedEdge();
						while (lastv0o < j) {
							v0out[lastv0o] = e;
							lastv0o++;
						}
						while (lastv1i < j) {
							v1in[lastv1i] = e.getReversedEdge();
							lastv1i++;
						}
					}
				} else {
					Edge e = g.createLink(v1, v0);
					edges[j] = e;
					if (!first) {
						v0inout[j - 1] = e.getReversedEdge();
						v1inout[j - 1] = e;
						while (lastv1o < j) {
							v1out[lastv1o] = e;
							lastv1o++;
						}
						while (lastv0i < j) {
							v0in[lastv0i] = e.getReversedEdge();
							lastv0i++;
						}
					}
				}
				first = false;
			}
			commit(g);

			createReadOnlyTransaction(g);
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				if (e.getAlpha() == v0) {
					assertEquals(v0inout[k], e
							.getNextIncidence(EdgeDirection.INOUT));
					assertEquals(v0out[k], e
							.getNextIncidence(EdgeDirection.OUT));
					assertEquals(v0in[k], e.getNextIncidence(EdgeDirection.IN));
					assertEquals(v1inout[k], e.getReversedEdge()
							.getNextIncidence(EdgeDirection.INOUT));
					assertEquals(v1out[k], e.getReversedEdge()
							.getNextIncidence(EdgeDirection.OUT));
					assertEquals(v1in[k], e.getReversedEdge().getNextIncidence(
							EdgeDirection.IN));
				} else {
					assertEquals(v0inout[k], e.getReversedEdge()
							.getNextIncidence(EdgeDirection.INOUT));
					assertEquals(v0out[k], e.getReversedEdge()
							.getNextIncidence(EdgeDirection.OUT));
					assertEquals(v0in[k], e.getReversedEdge().getNextIncidence(
							EdgeDirection.IN));
					assertEquals(v1inout[k], e
							.getNextIncidence(EdgeDirection.INOUT));
					assertEquals(v1out[k], e
							.getNextIncidence(EdgeDirection.OUT));
					assertEquals(v1in[k], e.getNextIncidence(EdgeDirection.IN));
				}
			}
			commit(g);
		}
	}

	// tests for the method Edge getNextEdgeOfClass(EdgeClass anEdgeClass);

	/**
	 * Creates an array of the EdgeClasses.
	 * 
	 * @return {Link, SubLink, LinkBack}
	 * @throws CommitFailedException
	 */
	private EdgeClass[] getEdgeClasses() throws CommitFailedException {
		EdgeClass[] ecs = new EdgeClass[3];
		createReadOnlyTransaction(g);
		List<EdgeClass> a = g.getSchema().getEdgeClassesInTopologicalOrder();
		commit(g);
		// TODO WTF?
		for (EdgeClass ec : a) {
			if (ec.getSimpleName().equals("Link")) {
				ecs[0] = ec;
			} else if (ec.getSimpleName().equals("SubLink")) {
				ecs[1] = ec;
			}
			if (ec.getSimpleName().equals("LinkBack")) {
				ecs[2] = ec;
			}
		}
		return ecs;
	}

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClass0() throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextIncidence(ecs[0]));
		assertNull(e1.getNextIncidence(ecs[1]));
		assertNull(e1.getNextIncidence(ecs[2]));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClass1() throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[0]));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[1]));
		assertEquals(e2, e1.getNextIncidence(ecs[2]));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[0]));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[1]));
		assertEquals(e5, e2.getNextIncidence(ecs[2]));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(ecs[0]));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1]));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(ecs[2]));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(ecs[0]));
		assertNull(e4.getNextIncidence(ecs[1]));
		assertEquals(e5, e4.getNextIncidence(ecs[2]));
		// test of edge e4.getReversedEdge
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0]));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1]));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(ecs[2]));
		// test of edge e5
		assertNull(e5.getNextIncidence(ecs[0]));
		assertNull(e5.getNextIncidence(ecs[1]));
		assertNull(e5.getNextIncidence(ecs[2]));
		commit(g);
	}

	/**
	 * Test in a randomly built graph
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClass2() throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();
		for (int i = 0; i < RANDOM_GRAPH_COUNT; i++) {
			switch (implementationType) {
			case STANDARD:
				g = VertexTestSchema.instance().createVertexTestGraph();
				break;
			case TRANSACTION:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithTransactionSupport();
				break;
			case SAVEMEM:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithSavememSupport();
				break;
			case DATABASE:
				g = dbHandler.createVertexTestGraphWithDatabaseSupport(ID + i);
				break;
			default:
				fail("Implementation " + implementationType
						+ " not yet supported by this test.");
			}
			createTransaction(g);
			DoubleSubNode v0 = g.createDoubleSubNode();
			DoubleSubNode v1 = g.createDoubleSubNode();
			Edge[] edges = new Edge[RANDOM_EDGE_COUNT];
			Edge[] link = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublink = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkback = new Edge[RANDOM_EDGE_COUNT];
			int lastlink = 0;
			int lastsublink = 0;
			int lastlinkback = 0;
			int edgetype = 0;
			boolean first = true;
			for (int j = 0; j < RANDOM_EDGE_COUNT; j++) {
				edgetype = rand.nextInt(2);
				if (edgetype == 0) {
					Edge e = g.createLink(v0, v1);
					edges[j] = e;
					if (!first) {
						while (lastlink < j) {
							link[lastlink] = e;
							lastlink++;
						}
					}
				}
				if (edgetype == 1) {
					Edge e = g.createSubLink(v0, v1);
					edges[j] = e;
					if (!first) {
						while (lastlink < j) {
							link[lastlink] = e;
							lastlink++;
						}
						while (lastsublink < j) {
							sublink[lastsublink] = e;
							lastsublink++;
						}
					}
				} else {
					Edge e = g.createLinkBack(v0, v1);
					edges[j] = e;
					if (!first) {
						while (lastlinkback < j) {
							linkback[lastlinkback] = e;
							lastlinkback++;
						}
					}
				}
				first = false;
			}
			commit(g);

			createReadOnlyTransaction(g);
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(link[k], e.getNextIncidence(ecs[0]));
				assertEquals(sublink[k], e.getNextIncidence(ecs[1]));
				assertEquals(linkback[k], e.getNextIncidence(ecs[2]));
			}
			commit(g);
		}
	}

	// tests for the method Edge getNextEdgeOfClass(Class<? extends Edge>
	// anEdgeClass);

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClass0() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextIncidence(Link.class));
		assertNull(e1.getNextIncidence(SubLink.class));
		assertNull(e1.getNextIncidence(LinkBack.class));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClass1() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(Link.class));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(SubLink.class));
		assertEquals(e2, e1.getNextIncidence(LinkBack.class));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(Link.class));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(SubLink.class));
		assertEquals(e5, e2.getNextIncidence(LinkBack.class));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(Link.class));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(LinkBack.class));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(Link.class));
		assertNull(e4.getNextIncidence(SubLink.class));
		assertEquals(e5, e4.getNextIncidence(LinkBack.class));
		// test of edge e4.getReversedEdge
		assertNull(e4.getReversedEdge().getNextIncidence(Link.class));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(LinkBack.class));
		// test of edge e5
		assertNull(e5.getNextIncidence(Link.class));
		assertNull(e5.getNextIncidence(SubLink.class));
		assertNull(e5.getNextIncidence(LinkBack.class));
		commit(g);
	}

	/**
	 * Test in a randomly built graph
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClass2() throws CommitFailedException {
		for (int i = 0; i < RANDOM_GRAPH_COUNT; i++) {
			switch (implementationType) {
			case STANDARD:
				g = VertexTestSchema.instance().createVertexTestGraph();
				break;
			case TRANSACTION:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithTransactionSupport();
				break;
			case SAVEMEM:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithSavememSupport();
				break;
			case DATABASE:
				g = dbHandler.createVertexTestGraphWithDatabaseSupport(ID + i);
				break;
			default:
				fail("Implementation " + implementationType
						+ " not yet supported by this test.");
			}
			createTransaction(g);
			DoubleSubNode v0 = g.createDoubleSubNode();
			DoubleSubNode v1 = g.createDoubleSubNode();
			Edge[] edges = new Edge[RANDOM_EDGE_COUNT];
			Edge[] link = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublink = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkback = new Edge[RANDOM_EDGE_COUNT];
			int lastlink = 0;
			int lastsublink = 0;
			int lastlinkback = 0;
			int edgetype = 0;
			boolean first = true;
			for (int j = 0; j < RANDOM_EDGE_COUNT; j++) {
				edgetype = rand.nextInt(2);
				if (edgetype == 0) {
					Edge e = g.createLink(v0, v1);
					edges[j] = e;
					if (!first) {
						while (lastlink < j) {
							link[lastlink] = e;
							lastlink++;
						}
					}
				}
				if (edgetype == 1) {
					Edge e = g.createSubLink(v0, v1);
					edges[j] = e;
					if (!first) {
						while (lastlink < j) {
							link[lastlink] = e;
							lastlink++;
						}
						while (lastsublink < j) {
							sublink[lastsublink] = e;
							lastsublink++;
						}
					}
				} else {
					Edge e = g.createLinkBack(v0, v1);
					edges[j] = e;
					if (!first) {
						while (lastlinkback < j) {
							linkback[lastlinkback] = e;
							lastlinkback++;
						}
					}
				}
				first = false;
			}
			commit(g);

			createReadOnlyTransaction(g);
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(link[k], e.getNextIncidence(Link.class));
				assertEquals(sublink[k], e.getNextIncidence(SubLink.class));
				assertEquals(linkback[k], e.getNextIncidence(LinkBack.class));
			}
			commit(g);
		}
	}

	// tests for the method Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
	// EdgeDirection orientation);

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirection0()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextIncidence(ecs[0], EdgeDirection.INOUT));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.INOUT));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.INOUT));
		assertNull(e1.getNextIncidence(ecs[0], EdgeDirection.OUT));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.OUT));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.OUT));
		assertNull(e1.getNextIncidence(ecs[0], EdgeDirection.IN));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.IN));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.IN));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirection1()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[0],
				EdgeDirection.INOUT));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[1],
				EdgeDirection.INOUT));
		assertEquals(e2, e1.getNextIncidence(ecs[2], EdgeDirection.INOUT));
		assertEquals(e4, e1.getNextIncidence(ecs[0], EdgeDirection.OUT));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.OUT));
		assertEquals(e2, e1.getNextIncidence(ecs[2], EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[0],
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[1],
				EdgeDirection.IN));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.IN));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[0],
				EdgeDirection.INOUT));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[1],
				EdgeDirection.INOUT));
		assertEquals(e5, e2.getNextIncidence(ecs[2], EdgeDirection.INOUT));
		assertEquals(e4, e2.getNextIncidence(ecs[0], EdgeDirection.OUT));
		assertNull(e2.getNextIncidence(ecs[1], EdgeDirection.OUT));
		assertEquals(e5, e2.getNextIncidence(ecs[2], EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[0],
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[1],
				EdgeDirection.IN));
		assertNull(e2.getNextIncidence(ecs[2], EdgeDirection.IN));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.INOUT));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.INOUT));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.INOUT));
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.OUT));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.OUT));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextIncidence(ecs[0], EdgeDirection.IN));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.IN));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.IN));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(ecs[0],
				EdgeDirection.INOUT));
		assertNull(e4.getNextIncidence(ecs[1], EdgeDirection.INOUT));
		assertEquals(e5, e4.getNextIncidence(ecs[2], EdgeDirection.INOUT));
		assertNull(e4.getNextIncidence(ecs[0], EdgeDirection.OUT));
		assertNull(e4.getNextIncidence(ecs[1], EdgeDirection.OUT));
		assertEquals(e5, e4.getNextIncidence(ecs[2], EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(ecs[0],
				EdgeDirection.IN));
		assertNull(e4.getNextIncidence(ecs[1], EdgeDirection.IN));
		assertNull(e4.getNextIncidence(ecs[2], EdgeDirection.IN));
		// test of edge e4.getReversedEdge
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.INOUT));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.INOUT));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.INOUT));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.OUT));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.OUT));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.OUT));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.IN));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.IN));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.IN));
		// test of edge e5
		assertNull(e5.getNextIncidence(ecs[0], EdgeDirection.INOUT));
		assertNull(e5.getNextIncidence(ecs[1], EdgeDirection.INOUT));
		assertNull(e5.getNextIncidence(ecs[2], EdgeDirection.INOUT));
		assertNull(e5.getNextIncidence(ecs[0], EdgeDirection.OUT));
		assertNull(e5.getNextIncidence(ecs[1], EdgeDirection.OUT));
		assertNull(e5.getNextIncidence(ecs[2], EdgeDirection.OUT));
		assertNull(e5.getNextIncidence(ecs[0], EdgeDirection.IN));
		assertNull(e5.getNextIncidence(ecs[1], EdgeDirection.IN));
		assertNull(e5.getNextIncidence(ecs[2], EdgeDirection.IN));
		commit(g);
	}

	/**
	 * Test in a randomly built graph
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirection2()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();
		for (int i = 0; i < RANDOM_GRAPH_COUNT; i++) {
			switch (implementationType) {
			case STANDARD:
				g = VertexTestSchema.instance().createVertexTestGraph();
				break;
			case TRANSACTION:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithTransactionSupport();
				break;
			case SAVEMEM:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithSavememSupport();
				break;
			case DATABASE:
				g = dbHandler.createVertexTestGraphWithDatabaseSupport(ID + i);
				break;
			default:
				fail("Implementation " + implementationType
						+ " not yet supported by this test.");
			}
			createTransaction(g);
			DoubleSubNode v0 = g.createDoubleSubNode();
			DoubleSubNode v1 = g.createDoubleSubNode();
			Edge[] edges = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkinout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkinout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackinout = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkinout = 0;
			int lastsublinkinout = 0;
			int lastlinkbackinout = 0;
			Edge[] linkout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackout = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkout = 0;
			int lastsublinkout = 0;
			int lastlinkbackout = 0;
			Edge[] linkin = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkin = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackin = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkin = 0;
			int lastsublinkin = 0;
			int lastlinkbackin = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < RANDOM_EDGE_COUNT; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? g.createLink(v0, v1) : g.createLink(
							v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkinout < j) {
								linkinout[lastlinkinout] = e;
								lastlinkinout++;
							}
							while (lastlinkout < j) {
								linkout[lastlinkout] = e;
								lastlinkout++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkinout < j) {
								linkinout[lastlinkinout] = e.getReversedEdge();
								lastlinkinout++;
							}
							while (lastlinkin < j) {
								linkin[lastlinkin] = e.getReversedEdge();
								lastlinkin++;
							}
						}
					}
				}
				if (edgetype == 1) {
					Edge e = direction ? g.createSubLink(v0, v1) : g
							.createSubLink(v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkinout < j) {
								linkinout[lastlinkinout] = e;
								lastlinkinout++;
							}
							while (lastlinkout < j) {
								linkout[lastlinkout] = e;
								lastlinkout++;
							}
							while (lastsublinkinout < j) {
								sublinkinout[lastsublinkinout] = e;
								lastsublinkinout++;
							}
							while (lastsublinkout < j) {
								sublinkout[lastsublinkout] = e;
								lastsublinkout++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkinout < j) {
								linkinout[lastlinkinout] = e.getReversedEdge();
								lastlinkinout++;
							}
							while (lastlinkin < j) {
								linkin[lastlinkin] = e.getReversedEdge();
								lastlinkin++;
							}
							while (lastsublinkinout < j) {
								sublinkinout[lastsublinkinout] = e
										.getReversedEdge();
								lastsublinkinout++;
							}
							while (lastsublinkin < j) {
								sublinkin[lastsublinkin] = e.getReversedEdge();
								lastsublinkin++;
							}
						}
					}
				} else {
					Edge e = direction ? g.createLinkBack(v0, v1) : g
							.createLinkBack(v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkbackinout < j) {
								linkbackinout[lastlinkbackinout] = e;
								lastlinkbackinout++;
							}
							while (lastlinkbackout < j) {
								linkbackout[lastlinkbackout] = e;
								lastlinkbackout++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkbackinout < j) {
								linkbackinout[lastlinkbackinout] = e
										.getReversedEdge();
								lastlinkbackinout++;
							}
							while (lastlinkbackin < j) {
								linkbackin[lastlinkbackin] = e
										.getReversedEdge();
								lastlinkbackin++;
							}
						}
					}
				}
				first = false;
			}
			commit(g);
			createReadOnlyTransaction(g);
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkinout[k], e.getNextIncidence(ecs[0],
						EdgeDirection.INOUT));
				assertEquals(sublinkinout[k], e.getNextIncidence(ecs[1],
						EdgeDirection.INOUT));
				assertEquals(linkbackinout[k], e.getNextIncidence(ecs[2],
						EdgeDirection.INOUT));
				assertEquals(linkout[k], e.getNextIncidence(ecs[0],
						EdgeDirection.OUT));
				assertEquals(sublinkout[k], e.getNextIncidence(ecs[1],
						EdgeDirection.OUT));
				assertEquals(linkbackout[k], e.getNextIncidence(ecs[2],
						EdgeDirection.OUT));
				assertEquals(linkin[k], e.getNextIncidence(ecs[0],
						EdgeDirection.IN));
				assertEquals(sublinkin[k], e.getNextIncidence(ecs[1],
						EdgeDirection.IN));
				assertEquals(linkbackin[k], e.getNextIncidence(ecs[2],
						EdgeDirection.IN));
			}
			commit(g);
		}
	}

	// tests for the method Edge getNextEdgeOfClass(Class<? extends Edge>
	// anEdgeClass, EdgeDirection orientation);

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirection0()
			throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextIncidence(Link.class, EdgeDirection.INOUT));
		assertNull(e1.getNextIncidence(SubLink.class, EdgeDirection.INOUT));
		assertNull(e1.getNextIncidence(LinkBack.class, EdgeDirection.INOUT));
		assertNull(e1.getNextIncidence(Link.class, EdgeDirection.OUT));
		assertNull(e1.getNextIncidence(SubLink.class, EdgeDirection.OUT));
		assertNull(e1.getNextIncidence(LinkBack.class, EdgeDirection.OUT));
		assertNull(e1.getNextIncidence(Link.class, EdgeDirection.IN));
		assertNull(e1.getNextIncidence(SubLink.class, EdgeDirection.IN));
		assertNull(e1.getNextIncidence(LinkBack.class, EdgeDirection.IN));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirection1()
			throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(Link.class,
				EdgeDirection.INOUT));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(e2, e1.getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT));
		assertEquals(e4, e1.getNextIncidence(Link.class, EdgeDirection.OUT));
		assertNull(e1.getNextIncidence(SubLink.class, EdgeDirection.OUT));
		assertEquals(e2, e1.getNextIncidence(LinkBack.class, EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(Link.class,
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(SubLink.class,
				EdgeDirection.IN));
		assertNull(e1.getNextIncidence(LinkBack.class, EdgeDirection.IN));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(Link.class,
				EdgeDirection.INOUT));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(e5, e2.getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT));
		assertEquals(e4, e2.getNextIncidence(Link.class, EdgeDirection.OUT));
		assertNull(e2.getNextIncidence(SubLink.class, EdgeDirection.OUT));
		assertEquals(e5, e2.getNextIncidence(LinkBack.class, EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(Link.class,
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(SubLink.class,
				EdgeDirection.IN));
		assertNull(e2.getNextIncidence(LinkBack.class, EdgeDirection.IN));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.INOUT));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT));
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.OUT));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextIncidence(Link.class, EdgeDirection.IN));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.IN));
		assertNull(e3.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.IN));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(Link.class,
				EdgeDirection.INOUT));
		assertNull(e4.getNextIncidence(SubLink.class, EdgeDirection.INOUT));
		assertEquals(e5, e4.getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT));
		assertNull(e4.getNextIncidence(Link.class, EdgeDirection.OUT));
		assertNull(e4.getNextIncidence(SubLink.class, EdgeDirection.OUT));
		assertEquals(e5, e4.getNextIncidence(LinkBack.class, EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(Link.class,
				EdgeDirection.IN));
		assertNull(e4.getNextIncidence(SubLink.class, EdgeDirection.IN));
		assertNull(e4.getNextIncidence(LinkBack.class, EdgeDirection.IN));
		// test of edge e4.getReversedEdge
		assertNull(e4.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.INOUT));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT));
		assertNull(e4.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.OUT));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.OUT));
		assertNull(e4.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.IN));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.IN));
		assertNull(e4.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.IN));
		// test of edge e5
		assertNull(e5.getNextIncidence(Link.class, EdgeDirection.INOUT));
		assertNull(e5.getNextIncidence(SubLink.class, EdgeDirection.INOUT));
		assertNull(e5.getNextIncidence(LinkBack.class, EdgeDirection.INOUT));
		assertNull(e5.getNextIncidence(Link.class, EdgeDirection.OUT));
		assertNull(e5.getNextIncidence(SubLink.class, EdgeDirection.OUT));
		assertNull(e5.getNextIncidence(LinkBack.class, EdgeDirection.OUT));
		assertNull(e5.getNextIncidence(Link.class, EdgeDirection.IN));
		assertNull(e5.getNextIncidence(SubLink.class, EdgeDirection.IN));
		assertNull(e5.getNextIncidence(LinkBack.class, EdgeDirection.IN));
		commit(g);
	}

	/**
	 * Test in a randomly built graph
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirection2()
			throws CommitFailedException {
		for (int i = 0; i < RANDOM_GRAPH_COUNT; i++) {
			switch (implementationType) {
			case STANDARD:
				g = VertexTestSchema.instance().createVertexTestGraph();
				break;
			case TRANSACTION:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithTransactionSupport();
				break;
			case SAVEMEM:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithSavememSupport();
				break;
			case DATABASE:
				g = dbHandler.createVertexTestGraphWithDatabaseSupport(ID + i);
				break;
			default:
				fail("Implementation " + implementationType
						+ " not yet supported by this test.");
			}
			createTransaction(g);
			DoubleSubNode v0 = g.createDoubleSubNode();
			DoubleSubNode v1 = g.createDoubleSubNode();
			Edge[] edges = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkinout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkinout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackinout = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkinout = 0;
			int lastsublinkinout = 0;
			int lastlinkbackinout = 0;
			Edge[] linkout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackout = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkout = 0;
			int lastsublinkout = 0;
			int lastlinkbackout = 0;
			Edge[] linkin = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkin = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackin = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkin = 0;
			int lastsublinkin = 0;
			int lastlinkbackin = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < RANDOM_EDGE_COUNT; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? g.createLink(v0, v1) : g.createLink(
							v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkinout < j) {
								linkinout[lastlinkinout] = e;
								lastlinkinout++;
							}
							while (lastlinkout < j) {
								linkout[lastlinkout] = e;
								lastlinkout++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkinout < j) {
								linkinout[lastlinkinout] = e.getReversedEdge();
								lastlinkinout++;
							}
							while (lastlinkin < j) {
								linkin[lastlinkin] = e.getReversedEdge();
								lastlinkin++;
							}
						}
					}
				}
				if (edgetype == 1) {
					Edge e = direction ? g.createSubLink(v0, v1) : g
							.createSubLink(v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkinout < j) {
								linkinout[lastlinkinout] = e;
								lastlinkinout++;
							}
							while (lastlinkout < j) {
								linkout[lastlinkout] = e;
								lastlinkout++;
							}
							while (lastsublinkinout < j) {
								sublinkinout[lastsublinkinout] = e;
								lastsublinkinout++;
							}
							while (lastsublinkout < j) {
								sublinkout[lastsublinkout] = e;
								lastsublinkout++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkinout < j) {
								linkinout[lastlinkinout] = e.getReversedEdge();
								lastlinkinout++;
							}
							while (lastlinkin < j) {
								linkin[lastlinkin] = e.getReversedEdge();
								lastlinkin++;
							}
							while (lastsublinkinout < j) {
								sublinkinout[lastsublinkinout] = e
										.getReversedEdge();
								lastsublinkinout++;
							}
							while (lastsublinkin < j) {
								sublinkin[lastsublinkin] = e.getReversedEdge();
								lastsublinkin++;
							}
						}
					}
				} else {
					Edge e = direction ? g.createLinkBack(v0, v1) : g
							.createLinkBack(v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkbackinout < j) {
								linkbackinout[lastlinkbackinout] = e;
								lastlinkbackinout++;
							}
							while (lastlinkbackout < j) {
								linkbackout[lastlinkbackout] = e;
								lastlinkbackout++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkbackinout < j) {
								linkbackinout[lastlinkbackinout] = e
										.getReversedEdge();
								lastlinkbackinout++;
							}
							while (lastlinkbackin < j) {
								linkbackin[lastlinkbackin] = e
										.getReversedEdge();
								lastlinkbackin++;
							}
						}
					}
				}
				first = false;
			}
			commit(g);

			createReadOnlyTransaction(g);
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkinout[k], e.getNextIncidence(Link.class,
						EdgeDirection.INOUT));
				assertEquals(sublinkinout[k], e.getNextIncidence(SubLink.class,
						EdgeDirection.INOUT));
				assertEquals(linkbackinout[k], e.getNextIncidence(
						LinkBack.class, EdgeDirection.INOUT));
				assertEquals(linkout[k], e.getNextIncidence(Link.class,
						EdgeDirection.OUT));
				assertEquals(sublinkout[k], e.getNextIncidence(SubLink.class,
						EdgeDirection.OUT));
				assertEquals(linkbackout[k], e.getNextIncidence(LinkBack.class,
						EdgeDirection.OUT));
				assertEquals(linkin[k], e.getNextIncidence(Link.class,
						EdgeDirection.IN));
				assertEquals(sublinkin[k], e.getNextIncidence(SubLink.class,
						EdgeDirection.IN));
				assertEquals(linkbackin[k], e.getNextIncidence(LinkBack.class,
						EdgeDirection.IN));
			}
			commit(g);
		}
	}

	// tests for the method Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
	// boolean noSubclasses);

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassBoolean0()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextIncidence(ecs[0], false));
		assertNull(e1.getNextIncidence(ecs[1], false));
		assertNull(e1.getNextIncidence(ecs[2], false));
		assertNull(e1.getNextIncidence(ecs[0], true));
		assertNull(e1.getNextIncidence(ecs[1], true));
		assertNull(e1.getNextIncidence(ecs[2], true));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassBoolean1()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[0], false));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[1], false));
		assertEquals(e2, e1.getNextIncidence(ecs[2], false));
		assertEquals(e4, e1.getNextIncidence(ecs[0], true));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[1], true));
		assertEquals(e2, e1.getNextIncidence(ecs[2], true));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[0], false));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[1], false));
		assertEquals(e5, e2.getNextIncidence(ecs[2], false));
		assertEquals(e4, e2.getNextIncidence(ecs[0], true));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[1], true));
		assertEquals(e5, e2.getNextIncidence(ecs[2], true));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[0],
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[1],
				EdgeDirection.IN));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(ecs[0], false));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1], false));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(ecs[2], false));
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(ecs[0], true));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1], true));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(ecs[2], true));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(ecs[0], false));
		assertNull(e4.getNextIncidence(ecs[1], false));
		assertEquals(e5, e4.getNextIncidence(ecs[2], false));
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(ecs[0], true));
		assertNull(e4.getNextIncidence(ecs[1], true));
		assertEquals(e5, e4.getNextIncidence(ecs[2], true));
		// test of edge e4.getReversedEdge
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0], false));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1], false));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(ecs[2], false));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0], true));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1], true));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(ecs[2], true));
		// test of edge e5
		assertNull(e5.getNextIncidence(ecs[0], false));
		assertNull(e5.getNextIncidence(ecs[1], false));
		assertNull(e5.getNextIncidence(ecs[2], false));
		assertNull(e5.getNextIncidence(ecs[0], true));
		assertNull(e5.getNextIncidence(ecs[1], true));
		assertNull(e5.getNextIncidence(ecs[2], true));
		commit(g);
	}

	/**
	 * Test in a randomly built graph
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassBoolean2()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();
		for (int i = 0; i < RANDOM_GRAPH_COUNT; i++) {
			switch (implementationType) {
			case STANDARD:
				g = VertexTestSchema.instance().createVertexTestGraph();
				break;
			case TRANSACTION:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithTransactionSupport();
				break;
			case SAVEMEM:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithSavememSupport();
				break;
			case DATABASE:
				g = dbHandler.createVertexTestGraphWithDatabaseSupport(ID + i);
				break;
			default:
				fail("Implementation " + implementationType
						+ " not yet supported by this test.");
			}
			createTransaction(g);
			DoubleSubNode v0 = g.createDoubleSubNode();
			DoubleSubNode v1 = g.createDoubleSubNode();
			Edge[] edges = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkfalse = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkfalse = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackfalse = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkfalse = 0;
			int lastsublinkfalse = 0;
			int lastlinkbackfalse = 0;
			Edge[] linktrue = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinktrue = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbacktrue = new Edge[RANDOM_EDGE_COUNT];
			int lastlinktrue = 0;
			int lastsublinktrue = 0;
			int lastlinkbacktrue = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < RANDOM_EDGE_COUNT; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? g.createLink(v0, v1) : g.createLink(
							v1, v0).getReversedEdge();
					edges[j] = e;
					if (!first) {
						while (lastlinkfalse < j) {
							linkfalse[lastlinkfalse] = e;
							lastlinkfalse++;
						}
						while (lastlinktrue < j) {
							linktrue[lastlinktrue] = e;
							lastlinktrue++;
						}
					}
				}
				if (edgetype == 1) {
					Edge e = direction ? g.createSubLink(v0, v1) : g
							.createSubLink(v1, v0).getReversedEdge();
					edges[j] = e;
					if (!first) {
						while (lastlinkfalse < j) {
							linkfalse[lastlinkfalse] = e;
							lastlinkfalse++;
						}
						while (lastsublinkfalse < j) {
							sublinkfalse[lastsublinkfalse] = e;
							lastsublinkfalse++;
						}
						while (lastsublinktrue < j) {
							sublinktrue[lastsublinktrue] = e;
							lastsublinktrue++;
						}
					}
				} else {
					Edge e = direction ? g.createLinkBack(v0, v1) : g
							.createLinkBack(v1, v0).getReversedEdge();
					edges[j] = e;
					if (!first) {
						while (lastlinkbackfalse < j) {
							linkbackfalse[lastlinkbackfalse] = e;
							lastlinkbackfalse++;
						}
						while (lastlinkbacktrue < j) {
							linkbacktrue[lastlinkbacktrue] = e;
							lastlinkbacktrue++;
						}
					}
				}
				first = false;
			}
			commit(g);

			createReadOnlyTransaction(g);
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkfalse[k], e.getNextIncidence(ecs[0], false));
				assertEquals(sublinkfalse[k], e.getNextIncidence(ecs[1], false));
				assertEquals(linkbackfalse[k], e
						.getNextIncidence(ecs[2], false));
				assertEquals(linktrue[k], e.getNextIncidence(ecs[0], true));
				assertEquals(sublinktrue[k], e.getNextIncidence(ecs[1], true));
				assertEquals(linkbacktrue[k], e.getNextIncidence(ecs[2], true));
			}
			commit(g);
		}
	}

	// tests for the method Edge getNextEdgeOfClass(Class<? extends Edge>
	// anEdgeClass, boolean noSubclasses);

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClassBoolean0()
			throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextIncidence(Link.class, false));
		assertNull(e1.getNextIncidence(SubLink.class, false));
		assertNull(e1.getNextIncidence(LinkBack.class, false));
		assertNull(e1.getNextIncidence(Link.class, true));
		assertNull(e1.getNextIncidence(SubLink.class, true));
		assertNull(e1.getNextIncidence(LinkBack.class, true));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClassBoolean1()
			throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(Link.class,
				false));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(SubLink.class,
				false));
		assertEquals(e2, e1.getNextIncidence(LinkBack.class, false));
		assertEquals(e4, e1.getNextIncidence(Link.class, true));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(SubLink.class,
				true));
		assertEquals(e2, e1.getNextIncidence(LinkBack.class, true));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(Link.class,
				false));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(SubLink.class,
				false));
		assertEquals(e5, e2.getNextIncidence(LinkBack.class, false));
		assertEquals(e4, e2.getNextIncidence(Link.class, true));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(SubLink.class,
				true));
		assertEquals(e5, e2.getNextIncidence(LinkBack.class, true));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(Link.class,
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(SubLink.class,
				EdgeDirection.IN));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(Link.class,
				false));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class, false));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(LinkBack.class,
				false));
		assertEquals(e4, e3.getReversedEdge()
				.getNextIncidence(Link.class, true));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class, true));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(LinkBack.class,
				true));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(Link.class,
				false));
		assertNull(e4.getNextIncidence(SubLink.class, false));
		assertEquals(e5, e4.getNextIncidence(LinkBack.class, false));
		assertEquals(e4.getReversedEdge(), e4
				.getNextIncidence(Link.class, true));
		assertNull(e4.getNextIncidence(SubLink.class, true));
		assertEquals(e5, e4.getNextIncidence(LinkBack.class, true));
		// test of edge e4.getReversedEdge
		assertNull(e4.getReversedEdge().getNextIncidence(Link.class, false));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class, false));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(LinkBack.class,
				false));
		assertNull(e4.getReversedEdge().getNextIncidence(Link.class, true));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class, true));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(LinkBack.class,
				true));
		// test of edge e5
		assertNull(e5.getNextIncidence(Link.class, false));
		assertNull(e5.getNextIncidence(SubLink.class, false));
		assertNull(e5.getNextIncidence(LinkBack.class, false));
		assertNull(e5.getNextIncidence(Link.class, true));
		assertNull(e5.getNextIncidence(SubLink.class, true));
		assertNull(e5.getNextIncidence(LinkBack.class, true));
		commit(g);
	}

	/**
	 * Test in a randomly built graph
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClassBoolean2()
			throws CommitFailedException {
		for (int i = 0; i < RANDOM_GRAPH_COUNT; i++) {
			switch (implementationType) {
			case STANDARD:
				g = VertexTestSchema.instance().createVertexTestGraph();
				break;
			case TRANSACTION:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithTransactionSupport();
				break;
			case SAVEMEM:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithSavememSupport();
				break;
			case DATABASE:
				g = dbHandler.createVertexTestGraphWithDatabaseSupport(ID + i);
				break;
			default:
				fail("Implementation " + implementationType
						+ " not yet supported by this test.");
			}
			createTransaction(g);
			DoubleSubNode v0 = g.createDoubleSubNode();
			DoubleSubNode v1 = g.createDoubleSubNode();
			Edge[] edges = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkfalse = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkfalse = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackfalse = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkfalse = 0;
			int lastsublinkfalse = 0;
			int lastlinkbackfalse = 0;
			Edge[] linktrue = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinktrue = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbacktrue = new Edge[RANDOM_EDGE_COUNT];
			int lastlinktrue = 0;
			int lastsublinktrue = 0;
			int lastlinkbacktrue = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < RANDOM_EDGE_COUNT; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? g.createLink(v0, v1) : g.createLink(
							v1, v0).getReversedEdge();
					edges[j] = e;
					if (!first) {
						while (lastlinkfalse < j) {
							linkfalse[lastlinkfalse] = e;
							lastlinkfalse++;
						}
						while (lastlinktrue < j) {
							linktrue[lastlinktrue] = e;
							lastlinktrue++;
						}
					}
				}
				if (edgetype == 1) {
					Edge e = direction ? g.createSubLink(v0, v1) : g
							.createSubLink(v1, v0).getReversedEdge();
					edges[j] = e;
					if (!first) {
						while (lastlinkfalse < j) {
							linkfalse[lastlinkfalse] = e;
							lastlinkfalse++;
						}
						while (lastsublinkfalse < j) {
							sublinkfalse[lastsublinkfalse] = e;
							lastsublinkfalse++;
						}
						while (lastsublinktrue < j) {
							sublinktrue[lastsublinktrue] = e;
							lastsublinktrue++;
						}
					}
				} else {
					Edge e = direction ? g.createLinkBack(v0, v1) : g
							.createLinkBack(v1, v0).getReversedEdge();
					edges[j] = e;
					if (!first) {
						while (lastlinkbackfalse < j) {
							linkbackfalse[lastlinkbackfalse] = e;
							lastlinkbackfalse++;
						}
						while (lastlinkbacktrue < j) {
							linkbacktrue[lastlinkbacktrue] = e;
							lastlinkbacktrue++;
						}
					}
				}
				first = false;
			}
			commit(g);
			createReadOnlyTransaction(g);
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkfalse[k], e
						.getNextIncidence(Link.class, false));
				assertEquals(sublinkfalse[k], e.getNextIncidence(SubLink.class,
						false));
				assertEquals(linkbackfalse[k], e.getNextIncidence(
						LinkBack.class, false));
				assertEquals(linktrue[k], e.getNextIncidence(Link.class, true));
				assertEquals(sublinktrue[k], e.getNextIncidence(SubLink.class,
						true));
				assertEquals(linkbacktrue[k], e.getNextIncidence(
						LinkBack.class, true));
			}
			commit(g);
		}
	}

	// tests for the method Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
	// EdgeDirection orientation, boolean noSubclasses);

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirectionBoolean0()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextIncidence(ecs[0], EdgeDirection.INOUT, false));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.INOUT, false));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.INOUT, false));
		assertNull(e1.getNextIncidence(ecs[0], EdgeDirection.OUT, false));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.OUT, false));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.OUT, false));
		assertNull(e1.getNextIncidence(ecs[0], EdgeDirection.IN, false));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.IN, false));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.IN, false));
		assertNull(e1.getNextIncidence(ecs[0], EdgeDirection.INOUT, true));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.INOUT, true));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.INOUT, true));
		assertNull(e1.getNextIncidence(ecs[0], EdgeDirection.OUT, true));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.OUT, true));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.OUT, true));
		assertNull(e1.getNextIncidence(ecs[0], EdgeDirection.IN, true));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.IN, true));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.IN, true));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirectionBoolean1()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[0],
				EdgeDirection.INOUT, false));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[1],
				EdgeDirection.INOUT, false));
		assertEquals(e2, e1
				.getNextIncidence(ecs[2], EdgeDirection.INOUT, false));
		assertEquals(e4, e1.getNextIncidence(ecs[0], EdgeDirection.OUT, false));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.OUT, false));
		assertEquals(e2, e1.getNextIncidence(ecs[2], EdgeDirection.OUT, false));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[0],
				EdgeDirection.IN, false));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[1],
				EdgeDirection.IN, false));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.IN, false));

		assertEquals(e4, e1.getNextIncidence(ecs[0], EdgeDirection.INOUT, true));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[1],
				EdgeDirection.INOUT, true));
		assertEquals(e2, e1.getNextIncidence(ecs[2], EdgeDirection.INOUT, true));
		assertEquals(e4, e1.getNextIncidence(ecs[0], EdgeDirection.OUT, true));
		assertNull(e1.getNextIncidence(ecs[1], EdgeDirection.OUT, true));
		assertEquals(e2, e1.getNextIncidence(ecs[2], EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e1.getNextIncidence(ecs[0],
				EdgeDirection.IN, true));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(ecs[1],
				EdgeDirection.IN, true));
		assertNull(e1.getNextIncidence(ecs[2], EdgeDirection.IN, true));

		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[0],
				EdgeDirection.INOUT, false));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[1],
				EdgeDirection.INOUT, false));
		assertEquals(e5, e2
				.getNextIncidence(ecs[2], EdgeDirection.INOUT, false));
		assertEquals(e4, e2.getNextIncidence(ecs[0], EdgeDirection.OUT, false));
		assertNull(e2.getNextIncidence(ecs[1], EdgeDirection.OUT, false));
		assertEquals(e5, e2.getNextIncidence(ecs[2], EdgeDirection.OUT, false));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[0],
				EdgeDirection.IN, false));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[1],
				EdgeDirection.IN, false));
		assertNull(e2.getNextIncidence(ecs[2], EdgeDirection.IN, false));

		assertEquals(e4, e2.getNextIncidence(ecs[0], EdgeDirection.INOUT, true));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[1],
				EdgeDirection.INOUT, true));
		assertEquals(e5, e2.getNextIncidence(ecs[2], EdgeDirection.INOUT, true));
		assertEquals(e4, e2.getNextIncidence(ecs[0], EdgeDirection.OUT, true));
		assertNull(e2.getNextIncidence(ecs[1], EdgeDirection.OUT, true));
		assertEquals(e5, e2.getNextIncidence(ecs[2], EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e2.getNextIncidence(ecs[0],
				EdgeDirection.IN, true));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(ecs[1],
				EdgeDirection.IN, true));
		assertNull(e2.getNextIncidence(ecs[2], EdgeDirection.IN, true));

		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.INOUT, false));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.INOUT, false));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.INOUT, false));
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.OUT, false));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.OUT, false));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.OUT, false));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextIncidence(ecs[0], EdgeDirection.IN, false));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.IN, false));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.IN, false));

		assertEquals(e4, e3.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.INOUT, true));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.INOUT, true));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.INOUT, true));
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.OUT, true));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.OUT, true));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextIncidence(ecs[0], EdgeDirection.IN, true));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.IN, true));
		assertNull(e3.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.IN, true));

		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(ecs[0],
				EdgeDirection.INOUT, false));
		assertNull(e4.getNextIncidence(ecs[1], EdgeDirection.INOUT, false));
		assertEquals(e5, e4
				.getNextIncidence(ecs[2], EdgeDirection.INOUT, false));
		assertNull(e4.getNextIncidence(ecs[0], EdgeDirection.OUT, false));
		assertNull(e4.getNextIncidence(ecs[1], EdgeDirection.OUT, false));
		assertEquals(e5, e4.getNextIncidence(ecs[2], EdgeDirection.OUT, false));
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(ecs[0],
				EdgeDirection.IN, false));
		assertNull(e4.getNextIncidence(ecs[1], EdgeDirection.IN, false));
		assertNull(e4.getNextIncidence(ecs[2], EdgeDirection.IN, false));

		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(ecs[0],
				EdgeDirection.INOUT, true));
		assertNull(e4.getNextIncidence(ecs[1], EdgeDirection.INOUT, true));
		assertEquals(e5, e4.getNextIncidence(ecs[2], EdgeDirection.INOUT, true));
		assertNull(e4.getNextIncidence(ecs[0], EdgeDirection.OUT, true));
		assertNull(e4.getNextIncidence(ecs[1], EdgeDirection.OUT, true));
		assertEquals(e5, e4.getNextIncidence(ecs[2], EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(ecs[0],
				EdgeDirection.IN, true));
		assertNull(e4.getNextIncidence(ecs[1], EdgeDirection.IN, true));
		assertNull(e4.getNextIncidence(ecs[2], EdgeDirection.IN, true));

		// test of edge e4.getReversedEdge
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.INOUT, false));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.INOUT, false));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.INOUT, false));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.OUT, false));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.OUT, false));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.OUT, false));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.IN, false));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.IN, false));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.IN, false));

		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.INOUT, true));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.INOUT, true));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.INOUT, true));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.OUT, true));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.OUT, true));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.OUT, true));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[0],
				EdgeDirection.IN, true));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[1],
				EdgeDirection.IN, true));
		assertNull(e4.getReversedEdge().getNextIncidence(ecs[2],
				EdgeDirection.IN, true));

		// test of edge e5
		assertNull(e5.getNextIncidence(ecs[0], EdgeDirection.INOUT, false));
		assertNull(e5.getNextIncidence(ecs[1], EdgeDirection.INOUT, false));
		assertNull(e5.getNextIncidence(ecs[2], EdgeDirection.INOUT, false));
		assertNull(e5.getNextIncidence(ecs[0], EdgeDirection.OUT, false));
		assertNull(e5.getNextIncidence(ecs[1], EdgeDirection.OUT, false));
		assertNull(e5.getNextIncidence(ecs[2], EdgeDirection.OUT, false));
		assertNull(e5.getNextIncidence(ecs[0], EdgeDirection.IN, false));
		assertNull(e5.getNextIncidence(ecs[1], EdgeDirection.IN, false));
		assertNull(e5.getNextIncidence(ecs[2], EdgeDirection.IN, false));

		assertNull(e5.getNextIncidence(ecs[0], EdgeDirection.INOUT, true));
		assertNull(e5.getNextIncidence(ecs[1], EdgeDirection.INOUT, true));
		assertNull(e5.getNextIncidence(ecs[2], EdgeDirection.INOUT, true));
		assertNull(e5.getNextIncidence(ecs[0], EdgeDirection.OUT, true));
		assertNull(e5.getNextIncidence(ecs[1], EdgeDirection.OUT, true));
		assertNull(e5.getNextIncidence(ecs[2], EdgeDirection.OUT, true));
		assertNull(e5.getNextIncidence(ecs[0], EdgeDirection.IN, true));
		assertNull(e5.getNextIncidence(ecs[1], EdgeDirection.IN, true));
		assertNull(e5.getNextIncidence(ecs[2], EdgeDirection.IN, true));
		commit(g);
	}

	/**
	 * Test in a randomly built graph
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirectionBoolean2()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();
		for (int i = 0; i < RANDOM_GRAPH_COUNT; i++) {
			switch (implementationType) {
			case STANDARD:
				g = VertexTestSchema.instance().createVertexTestGraph();
				break;
			case TRANSACTION:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithTransactionSupport();
				break;
			case SAVEMEM:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithSavememSupport();
				break;
			case DATABASE:
				g = dbHandler.createVertexTestGraphWithDatabaseSupport(ID + i);
				break;
			default:
				fail("Implementation " + implementationType
						+ " not yet supported by this test.");
			}
			createTransaction(g);
			DoubleSubNode v0 = g.createDoubleSubNode();
			DoubleSubNode v1 = g.createDoubleSubNode();
			Edge[] edges = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkinoutfalse = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkinout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackinout = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkinoutfalse = 0;
			int lastsublinkinout = 0;
			int lastlinkbackinout = 0;
			Edge[] linkoutfalse = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackout = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkoutfalse = 0;
			int lastsublinkout = 0;
			int lastlinkbackout = 0;
			Edge[] linkinfalse = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkin = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackin = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkinfalse = 0;
			int lastsublinkin = 0;
			int lastlinkbackin = 0;
			Edge[] linkinouttrue = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkinouttrue = 0;
			Edge[] linkouttrue = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkouttrue = 0;
			Edge[] linkintrue = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkintrue = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < RANDOM_EDGE_COUNT; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? g.createLink(v0, v1) : g.createLink(
							v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkinoutfalse < j) {
								linkinoutfalse[lastlinkinoutfalse] = e;
								lastlinkinoutfalse++;
							}
							while (lastlinkoutfalse < j) {
								linkoutfalse[lastlinkoutfalse] = e;
								lastlinkoutfalse++;
							}
							while (lastlinkinouttrue < j) {
								linkinouttrue[lastlinkinouttrue] = e;
								lastlinkinouttrue++;
							}
							while (lastlinkouttrue < j) {
								linkouttrue[lastlinkouttrue] = e;
								lastlinkouttrue++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkinoutfalse < j) {
								linkinoutfalse[lastlinkinoutfalse] = e
										.getReversedEdge();
								lastlinkinoutfalse++;
							}
							while (lastlinkinfalse < j) {
								linkinfalse[lastlinkinfalse] = e
										.getReversedEdge();
								lastlinkinfalse++;
							}
							while (lastlinkinouttrue < j) {
								linkinouttrue[lastlinkinouttrue] = e
										.getReversedEdge();
								lastlinkinouttrue++;
							}
							while (lastlinkintrue < j) {
								linkintrue[lastlinkintrue] = e
										.getReversedEdge();
								lastlinkintrue++;
							}
						}
					}
				}
				if (edgetype == 1) {
					Edge e = direction ? g.createSubLink(v0, v1) : g
							.createSubLink(v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkinoutfalse < j) {
								linkinoutfalse[lastlinkinoutfalse] = e;
								lastlinkinoutfalse++;
							}
							while (lastlinkoutfalse < j) {
								linkoutfalse[lastlinkoutfalse] = e;
								lastlinkoutfalse++;
							}
							while (lastsublinkinout < j) {
								sublinkinout[lastsublinkinout] = e;
								lastsublinkinout++;
							}
							while (lastsublinkout < j) {
								sublinkout[lastsublinkout] = e;
								lastsublinkout++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkinoutfalse < j) {
								linkinoutfalse[lastlinkinoutfalse] = e
										.getReversedEdge();
								lastlinkinoutfalse++;
							}
							while (lastlinkinfalse < j) {
								linkinfalse[lastlinkinfalse] = e
										.getReversedEdge();
								lastlinkinfalse++;
							}
							while (lastsublinkinout < j) {
								sublinkinout[lastsublinkinout] = e
										.getReversedEdge();
								lastsublinkinout++;
							}
							while (lastsublinkin < j) {
								sublinkin[lastsublinkin] = e.getReversedEdge();
								lastsublinkin++;
							}
						}
					}
				} else {
					Edge e = direction ? g.createLinkBack(v0, v1) : g
							.createLinkBack(v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkbackinout < j) {
								linkbackinout[lastlinkbackinout] = e;
								lastlinkbackinout++;
							}
							while (lastlinkbackout < j) {
								linkbackout[lastlinkbackout] = e;
								lastlinkbackout++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkbackinout < j) {
								linkbackinout[lastlinkbackinout] = e
										.getReversedEdge();
								lastlinkbackinout++;
							}
							while (lastlinkbackin < j) {
								linkbackin[lastlinkbackin] = e
										.getReversedEdge();
								lastlinkbackin++;
							}
						}
					}
				}
				first = false;
			}
			commit(g);
			createReadOnlyTransaction(g);
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkinoutfalse[k], e.getNextIncidence(ecs[0],
						EdgeDirection.INOUT, false));
				assertEquals(sublinkinout[k], e.getNextIncidence(ecs[1],
						EdgeDirection.INOUT, false));
				assertEquals(linkbackinout[k], e.getNextIncidence(ecs[2],
						EdgeDirection.INOUT, false));
				assertEquals(linkoutfalse[k], e.getNextIncidence(ecs[0],
						EdgeDirection.OUT, false));
				assertEquals(sublinkout[k], e.getNextIncidence(ecs[1],
						EdgeDirection.OUT, false));
				assertEquals(linkbackout[k], e.getNextIncidence(ecs[2],
						EdgeDirection.OUT, false));
				assertEquals(linkinfalse[k], e.getNextIncidence(ecs[0],
						EdgeDirection.IN, false));
				assertEquals(sublinkin[k], e.getNextIncidence(ecs[1],
						EdgeDirection.IN, false));
				assertEquals(linkbackin[k], e.getNextIncidence(ecs[2],
						EdgeDirection.IN, false));

				assertEquals(linkinouttrue[k], e.getNextIncidence(ecs[0],
						EdgeDirection.INOUT, true));
				assertEquals(sublinkinout[k], e.getNextIncidence(ecs[1],
						EdgeDirection.INOUT, true));
				assertEquals(linkbackinout[k], e.getNextIncidence(ecs[2],
						EdgeDirection.INOUT, true));
				assertEquals(linkouttrue[k], e.getNextIncidence(ecs[0],
						EdgeDirection.OUT, true));
				assertEquals(sublinkout[k], e.getNextIncidence(ecs[1],
						EdgeDirection.OUT, true));
				assertEquals(linkbackout[k], e.getNextIncidence(ecs[2],
						EdgeDirection.OUT, true));
				assertEquals(linkintrue[k], e.getNextIncidence(ecs[0],
						EdgeDirection.IN, true));
				assertEquals(sublinkin[k], e.getNextIncidence(ecs[1],
						EdgeDirection.IN, true));
				assertEquals(linkbackin[k], e.getNextIncidence(ecs[2],
						EdgeDirection.IN, true));
			}
			commit(g);
		}
	}

	// tests for the method Edge getNextEdgeOfClass(Class<? extends Edge>
	// anEdgeClass, EdgeDirection orientation, boolean noSubclasses);

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirectionBoolean0()
			throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextIncidence(Link.class, EdgeDirection.INOUT, false));
		assertNull(e1.getNextIncidence(SubLink.class, EdgeDirection.INOUT,
				false));
		assertNull(e1.getNextIncidence(LinkBack.class, EdgeDirection.INOUT,
				false));
		assertNull(e1.getNextIncidence(Link.class, EdgeDirection.OUT, false));
		assertNull(e1.getNextIncidence(SubLink.class, EdgeDirection.OUT, false));
		assertNull(e1
				.getNextIncidence(LinkBack.class, EdgeDirection.OUT, false));
		assertNull(e1.getNextIncidence(Link.class, EdgeDirection.IN, false));
		assertNull(e1.getNextIncidence(SubLink.class, EdgeDirection.IN, false));
		assertNull(e1.getNextIncidence(LinkBack.class, EdgeDirection.IN, false));
		assertNull(e1.getNextIncidence(Link.class, EdgeDirection.INOUT, true));
		assertNull(e1
				.getNextIncidence(SubLink.class, EdgeDirection.INOUT, true));
		assertNull(e1.getNextIncidence(LinkBack.class, EdgeDirection.INOUT,
				true));
		assertNull(e1.getNextIncidence(Link.class, EdgeDirection.OUT, true));
		assertNull(e1.getNextIncidence(SubLink.class, EdgeDirection.OUT, true));
		assertNull(e1.getNextIncidence(LinkBack.class, EdgeDirection.OUT, true));
		assertNull(e1.getNextIncidence(Link.class, EdgeDirection.IN, true));
		assertNull(e1.getNextIncidence(SubLink.class, EdgeDirection.IN, true));
		assertNull(e1.getNextIncidence(LinkBack.class, EdgeDirection.IN, true));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirectionBoolean1()
			throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(e2, e1.getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertEquals(e4, e1.getNextIncidence(Link.class, EdgeDirection.OUT,
				false));
		assertNull(e1.getNextIncidence(SubLink.class, EdgeDirection.OUT, false));
		assertEquals(e2, e1.getNextIncidence(LinkBack.class, EdgeDirection.OUT,
				false));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(Link.class,
				EdgeDirection.IN, false));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(SubLink.class,
				EdgeDirection.IN, false));
		assertNull(e1.getNextIncidence(LinkBack.class, EdgeDirection.IN, false));

		assertEquals(e4, e1.getNextIncidence(Link.class, EdgeDirection.INOUT,
				true));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(e2, e1.getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertEquals(e4, e1.getNextIncidence(Link.class, EdgeDirection.OUT,
				true));
		assertNull(e1.getNextIncidence(SubLink.class, EdgeDirection.OUT, true));
		assertEquals(e2, e1.getNextIncidence(LinkBack.class, EdgeDirection.OUT,
				true));
		assertEquals(e4.getReversedEdge(), e1.getNextIncidence(Link.class,
				EdgeDirection.IN, true));
		assertEquals(e3.getReversedEdge(), e1.getNextIncidence(SubLink.class,
				EdgeDirection.IN, true));
		assertNull(e1.getNextIncidence(LinkBack.class, EdgeDirection.IN, true));

		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(e5, e2.getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertEquals(e4, e2.getNextIncidence(Link.class, EdgeDirection.OUT,
				false));
		assertNull(e2.getNextIncidence(SubLink.class, EdgeDirection.OUT, false));
		assertEquals(e5, e2.getNextIncidence(LinkBack.class, EdgeDirection.OUT,
				false));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(Link.class,
				EdgeDirection.IN, false));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(SubLink.class,
				EdgeDirection.IN, false));
		assertNull(e2.getNextIncidence(LinkBack.class, EdgeDirection.IN, false));

		assertEquals(e4, e2.getNextIncidence(Link.class, EdgeDirection.INOUT,
				true));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(e5, e2.getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertEquals(e4, e2.getNextIncidence(Link.class, EdgeDirection.OUT,
				true));
		assertNull(e2.getNextIncidence(SubLink.class, EdgeDirection.OUT, true));
		assertEquals(e5, e2.getNextIncidence(LinkBack.class, EdgeDirection.OUT,
				true));
		assertEquals(e4.getReversedEdge(), e2.getNextIncidence(Link.class,
				EdgeDirection.IN, true));
		assertEquals(e3.getReversedEdge(), e2.getNextIncidence(SubLink.class,
				EdgeDirection.IN, true));
		assertNull(e2.getNextIncidence(LinkBack.class, EdgeDirection.IN, true));

		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.INOUT, false));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.OUT, false));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.OUT, false));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextIncidence(Link.class, EdgeDirection.IN, false));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.IN, false));
		assertNull(e3.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.IN, false));

		assertEquals(e4, e3.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.INOUT, true));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertEquals(e4, e3.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.OUT, true));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(e5, e3.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextIncidence(Link.class, EdgeDirection.IN, true));
		assertNull(e3.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.IN, true));
		assertNull(e3.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.IN, true));

		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(Link.class,
				EdgeDirection.INOUT, false));
		assertNull(e4.getNextIncidence(SubLink.class, EdgeDirection.INOUT,
				false));
		assertEquals(e5, e4.getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertNull(e4.getNextIncidence(Link.class, EdgeDirection.OUT, false));
		assertNull(e4.getNextIncidence(SubLink.class, EdgeDirection.OUT, false));
		assertEquals(e5, e4.getNextIncidence(LinkBack.class, EdgeDirection.OUT,
				false));
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(Link.class,
				EdgeDirection.IN, false));
		assertNull(e4.getNextIncidence(SubLink.class, EdgeDirection.IN, false));
		assertNull(e4.getNextIncidence(LinkBack.class, EdgeDirection.IN, false));

		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(Link.class,
				EdgeDirection.INOUT, true));
		assertNull(e4
				.getNextIncidence(SubLink.class, EdgeDirection.INOUT, true));
		assertEquals(e5, e4.getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertNull(e4.getNextIncidence(Link.class, EdgeDirection.OUT, true));
		assertNull(e4.getNextIncidence(SubLink.class, EdgeDirection.OUT, true));
		assertEquals(e5, e4.getNextIncidence(LinkBack.class, EdgeDirection.OUT,
				true));
		assertEquals(e4.getReversedEdge(), e4.getNextIncidence(Link.class,
				EdgeDirection.IN, true));
		assertNull(e4.getNextIncidence(SubLink.class, EdgeDirection.IN, true));
		assertNull(e4.getNextIncidence(LinkBack.class, EdgeDirection.IN, true));

		// test of edge e4.getReversedEdge
		assertNull(e4.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.INOUT, false));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertNull(e4.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.OUT, false));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.OUT, false));
		assertNull(e4.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.IN, false));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.IN, false));
		assertNull(e4.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.IN, false));

		assertNull(e4.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.INOUT, true));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertNull(e4.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.OUT, true));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(e5, e4.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.OUT, true));
		assertNull(e4.getReversedEdge().getNextIncidence(Link.class,
				EdgeDirection.IN, true));
		assertNull(e4.getReversedEdge().getNextIncidence(SubLink.class,
				EdgeDirection.IN, true));
		assertNull(e4.getReversedEdge().getNextIncidence(LinkBack.class,
				EdgeDirection.IN, true));

		// test of edge e5
		assertNull(e5.getNextIncidence(Link.class, EdgeDirection.INOUT, false));
		assertNull(e5.getNextIncidence(SubLink.class, EdgeDirection.INOUT,
				false));
		assertNull(e5.getNextIncidence(LinkBack.class, EdgeDirection.INOUT,
				false));
		assertNull(e5.getNextIncidence(Link.class, EdgeDirection.OUT, false));
		assertNull(e5.getNextIncidence(SubLink.class, EdgeDirection.OUT, false));
		assertNull(e5
				.getNextIncidence(LinkBack.class, EdgeDirection.OUT, false));
		assertNull(e5.getNextIncidence(Link.class, EdgeDirection.IN, false));
		assertNull(e5.getNextIncidence(SubLink.class, EdgeDirection.IN, false));
		assertNull(e5.getNextIncidence(LinkBack.class, EdgeDirection.IN, false));

		assertNull(e5.getNextIncidence(Link.class, EdgeDirection.INOUT, true));
		assertNull(e5
				.getNextIncidence(SubLink.class, EdgeDirection.INOUT, true));
		assertNull(e5.getNextIncidence(LinkBack.class, EdgeDirection.INOUT,
				true));
		assertNull(e5.getNextIncidence(Link.class, EdgeDirection.OUT, true));
		assertNull(e5.getNextIncidence(SubLink.class, EdgeDirection.OUT, true));
		assertNull(e5.getNextIncidence(LinkBack.class, EdgeDirection.OUT, true));
		assertNull(e5.getNextIncidence(Link.class, EdgeDirection.IN, true));
		assertNull(e5.getNextIncidence(SubLink.class, EdgeDirection.IN, true));
		assertNull(e5.getNextIncidence(LinkBack.class, EdgeDirection.IN, true));
		commit(g);
	}

	/**
	 * Test in a randomly built graph
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirectionBoolean2()
			throws CommitFailedException {
		for (int i = 0; i < RANDOM_GRAPH_COUNT; i++) {
			switch (implementationType) {
			case STANDARD:
				g = VertexTestSchema.instance().createVertexTestGraph();
				break;
			case TRANSACTION:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithTransactionSupport();
				break;
			case SAVEMEM:
				g = VertexTestSchema.instance()
						.createVertexTestGraphWithSavememSupport();
				break;
			case DATABASE:
				g = dbHandler.createVertexTestGraphWithDatabaseSupport(ID + i);
				break;
			default:
				fail("Implementation " + implementationType
						+ " not yet supported by this test.");
			}
			createTransaction(g);
			DoubleSubNode v0 = g.createDoubleSubNode();
			DoubleSubNode v1 = g.createDoubleSubNode();
			Edge[] edges = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkinoutfalse = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkinout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackinout = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkinoutfalse = 0;
			int lastsublinkinout = 0;
			int lastlinkbackinout = 0;
			Edge[] linkoutfalse = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkout = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackout = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkoutfalse = 0;
			int lastsublinkout = 0;
			int lastlinkbackout = 0;
			Edge[] linkinfalse = new Edge[RANDOM_EDGE_COUNT];
			Edge[] sublinkin = new Edge[RANDOM_EDGE_COUNT];
			Edge[] linkbackin = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkinfalse = 0;
			int lastsublinkin = 0;
			int lastlinkbackin = 0;
			Edge[] linkinouttrue = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkinouttrue = 0;
			Edge[] linkouttrue = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkouttrue = 0;
			Edge[] linkintrue = new Edge[RANDOM_EDGE_COUNT];
			int lastlinkintrue = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < RANDOM_EDGE_COUNT; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? g.createLink(v0, v1) : g.createLink(
							v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkinoutfalse < j) {
								linkinoutfalse[lastlinkinoutfalse] = e;
								lastlinkinoutfalse++;
							}
							while (lastlinkoutfalse < j) {
								linkoutfalse[lastlinkoutfalse] = e;
								lastlinkoutfalse++;
							}
							while (lastlinkinouttrue < j) {
								linkinouttrue[lastlinkinouttrue] = e;
								lastlinkinouttrue++;
							}
							while (lastlinkouttrue < j) {
								linkouttrue[lastlinkouttrue] = e;
								lastlinkouttrue++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkinoutfalse < j) {
								linkinoutfalse[lastlinkinoutfalse] = e
										.getReversedEdge();
								lastlinkinoutfalse++;
							}
							while (lastlinkinfalse < j) {
								linkinfalse[lastlinkinfalse] = e
										.getReversedEdge();
								lastlinkinfalse++;
							}
							while (lastlinkinouttrue < j) {
								linkinouttrue[lastlinkinouttrue] = e
										.getReversedEdge();
								lastlinkinouttrue++;
							}
							while (lastlinkintrue < j) {
								linkintrue[lastlinkintrue] = e
										.getReversedEdge();
								lastlinkintrue++;
							}
						}
					}
				}
				if (edgetype == 1) {
					Edge e = direction ? g.createSubLink(v0, v1) : g
							.createSubLink(v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkinoutfalse < j) {
								linkinoutfalse[lastlinkinoutfalse] = e;
								lastlinkinoutfalse++;
							}
							while (lastlinkoutfalse < j) {
								linkoutfalse[lastlinkoutfalse] = e;
								lastlinkoutfalse++;
							}
							while (lastsublinkinout < j) {
								sublinkinout[lastsublinkinout] = e;
								lastsublinkinout++;
							}
							while (lastsublinkout < j) {
								sublinkout[lastsublinkout] = e;
								lastsublinkout++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkinoutfalse < j) {
								linkinoutfalse[lastlinkinoutfalse] = e
										.getReversedEdge();
								lastlinkinoutfalse++;
							}
							while (lastlinkinfalse < j) {
								linkinfalse[lastlinkinfalse] = e
										.getReversedEdge();
								lastlinkinfalse++;
							}
							while (lastsublinkinout < j) {
								sublinkinout[lastsublinkinout] = e
										.getReversedEdge();
								lastsublinkinout++;
							}
							while (lastsublinkin < j) {
								sublinkin[lastsublinkin] = e.getReversedEdge();
								lastsublinkin++;
							}
						}
					}
				} else {
					Edge e = direction ? g.createLinkBack(v0, v1) : g
							.createLinkBack(v1, v0);
					if (direction) {
						edges[j] = e;
						if (!first) {
							while (lastlinkbackinout < j) {
								linkbackinout[lastlinkbackinout] = e;
								lastlinkbackinout++;
							}
							while (lastlinkbackout < j) {
								linkbackout[lastlinkbackout] = e;
								lastlinkbackout++;
							}
						}
					} else {
						edges[j] = e.getReversedEdge();
						if (!first) {
							while (lastlinkbackinout < j) {
								linkbackinout[lastlinkbackinout] = e
										.getReversedEdge();
								lastlinkbackinout++;
							}
							while (lastlinkbackin < j) {
								linkbackin[lastlinkbackin] = e
										.getReversedEdge();
								lastlinkbackin++;
							}
						}
					}
				}
				first = false;
			}
			commit(g);
			createReadOnlyTransaction(g);
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkinoutfalse[k], e.getNextIncidence(Link.class,
						EdgeDirection.INOUT, false));
				assertEquals(sublinkinout[k], e.getNextIncidence(SubLink.class,
						EdgeDirection.INOUT, false));
				assertEquals(linkbackinout[k], e.getNextIncidence(
						LinkBack.class, EdgeDirection.INOUT, false));
				assertEquals(linkoutfalse[k], e.getNextIncidence(Link.class,
						EdgeDirection.OUT, false));
				assertEquals(sublinkout[k], e.getNextIncidence(SubLink.class,
						EdgeDirection.OUT, false));
				assertEquals(linkbackout[k], e.getNextIncidence(LinkBack.class,
						EdgeDirection.OUT, false));
				assertEquals(linkinfalse[k], e.getNextIncidence(Link.class,
						EdgeDirection.IN, false));
				assertEquals(sublinkin[k], e.getNextIncidence(SubLink.class,
						EdgeDirection.IN, false));
				assertEquals(linkbackin[k], e.getNextIncidence(LinkBack.class,
						EdgeDirection.IN, false));

				assertEquals(linkinouttrue[k], e.getNextIncidence(Link.class,
						EdgeDirection.INOUT, true));
				assertEquals(sublinkinout[k], e.getNextIncidence(SubLink.class,
						EdgeDirection.INOUT, true));
				assertEquals(linkbackinout[k], e.getNextIncidence(
						LinkBack.class, EdgeDirection.INOUT, true));
				assertEquals(linkouttrue[k], e.getNextIncidence(Link.class,
						EdgeDirection.OUT, true));
				assertEquals(sublinkout[k], e.getNextIncidence(SubLink.class,
						EdgeDirection.OUT, true));
				assertEquals(linkbackout[k], e.getNextIncidence(LinkBack.class,
						EdgeDirection.OUT, true));
				assertEquals(linkintrue[k], e.getNextIncidence(Link.class,
						EdgeDirection.IN, true));
				assertEquals(sublinkin[k], e.getNextIncidence(SubLink.class,
						EdgeDirection.IN, true));
				assertEquals(linkbackin[k], e.getNextIncidence(LinkBack.class,
						EdgeDirection.IN, true));
			}
			commit(g);
		}
	}

	// tests for the method Vertex getThis();

	private Vertex[] createRandomGraph(boolean retThis)
			throws CommitFailedException {
		// TODO write comments for this method and think about method name.
		createTransaction(g);
		Vertex[] nodes = new Vertex[] { g.createSubNode(),
				g.createDoubleSubNode(), g.createSuperNode() };
		Vertex[] ret = new Vertex[RANDOM_VERTEX_COUNT];
		for (int i = 0; i < ret.length; i++) {
			int edge = rand.nextInt(3);
			switch (edge) {
			case 0:
				Vertex start = nodes[rand.nextInt(2)];
				Vertex end = nodes[rand.nextInt(2) + 1];
				g.createLink((AbstractSuperNode) start, (SuperNode) end);
				ret[i] = retThis ? start : end;
				break;
			case 1:
				start = nodes[1];
				end = nodes[rand.nextInt(2) + 1];
				g.createSubLink((DoubleSubNode) start, (SuperNode) end);
				ret[i] = retThis ? start : end;
				break;
			case 2:
				start = nodes[rand.nextInt(2) + 1];
				end = nodes[rand.nextInt(2)];
				g.createLinkBack((SuperNode) start, (AbstractSuperNode) end);
				ret[i] = retThis ? start : end;
				break;
			}
		}

		commit(g);
		return ret;
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getThisTest0() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		DoubleSubNode v3 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v2, v3);
		Edge e3 = g.createSubLink(v1, v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v1, e1.getThis());
		assertEquals(v2, e1.getReversedEdge().getThis());
		assertEquals(v2, e2.getThis());
		assertEquals(v3, e2.getReversedEdge().getThis());
		assertEquals(v1, e3.getThis());
		assertEquals(v3, e3.getReversedEdge().getThis());
		commit(g);
	}

	/**
	 * Test in a randomly built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getThisTest1() throws CommitFailedException {
		Vertex[] thisVertices = createRandomGraph(true);

		createReadOnlyTransaction(g);
		for (int i = 0; i < g.getECount(); i++) {
			assertEquals(thisVertices[i], g.getEdge(i + 1).getThis());
		}
		commit(g);
	}

	// tests for the method Vertex getThat();

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getThatTest0() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		DoubleSubNode v3 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v2, v3);
		Edge e3 = g.createSubLink(v1, v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v2, e1.getThat());
		assertEquals(v1, e1.getReversedEdge().getThat());
		assertEquals(v3, e2.getThat());
		assertEquals(v2, e2.getReversedEdge().getThat());
		assertEquals(v3, e3.getThat());
		assertEquals(v1, e3.getReversedEdge().getThat());
		commit(g);
	}

	/**
	 * Test in a randomly built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getThatTest1() throws CommitFailedException {
		Vertex[] thisVertices = createRandomGraph(false);

		createReadOnlyTransaction(g);
		for (int i = 0; i < g.getECount(); i++) {
			assertEquals(thisVertices[i], g.getEdge(i + 1).getThat());
		}
		commit(g);
	}

	// tests for the method String getThisRole();

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getThisRoleTest() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createSubLink(v1, v2);
		Edge e3 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("source", e1.getThisRole());
		assertEquals("target", e1.getReversedEdge().getThisRole());
		assertEquals("sourcec", e2.getThisRole());
		assertEquals("targetc", e2.getReversedEdge().getThisRole());
		assertEquals("sourceb", e3.getThisRole());
		assertEquals("targetb", e3.getReversedEdge().getThisRole());
		commit(g);
	}

	// tests for the method String getThisRole();

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getThatRoleTest() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createSubLink(v1, v2);
		Edge e3 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("target", e1.getThatRole());
		assertEquals("source", e1.getReversedEdge().getThatRole());
		assertEquals("targetc", e2.getThatRole());
		assertEquals("sourcec", e2.getReversedEdge().getThatRole());
		assertEquals("targetb", e3.getThatRole());
		assertEquals("sourceb", e3.getReversedEdge().getThatRole());
		commit(g);
	}

	// tests for the method Edge getNextEdgeInGraph();
	// (already tested in LoadTest.java)

	/**
	 * Test for reversedEdge.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeInGraphTestR0() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createSubLink(v1, v2);
		Edge e3 = g.createLinkBack(v1, v2);
		Edge e1R = e1.getReversedEdge();
		Edge e2R = e2.getReversedEdge();
		Edge e3R = e3.getReversedEdge();
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(e2, e1R.getNextEdge());
		assertEquals(e3, e2R.getNextEdge());
		assertNull(e3R.getNextEdge());
		commit(g);
	}

	// tests for the method Edge getPrevEdgeInGraph();
	/**
	 * Creates an randomly build graph an returns an 2-dim ArrayList of Edges,
	 * which are needed to check the equality in respect to the parameters of
	 * the methods.
	 * 
	 * @param classedge
	 * @param nosubclasses
	 * @return
	 * @throws CommitFailedException
	 */
	private ArrayList<ArrayList<Edge>> createRandomGraph(boolean edgeClass,
			boolean nosubclasses) throws CommitFailedException {
		createTransaction(g);
		Vertex[] nodes = new Vertex[] { g.createSubNode(),
				g.createDoubleSubNode(), g.createSuperNode() };
		ArrayList<ArrayList<Edge>> ret = new ArrayList<ArrayList<Edge>>();
		if (!edgeClass) {
			// edges for getPrevEdgeInGraph() are needed
			ret.add(new ArrayList<Edge>());
		} else if (!nosubclasses) {
			// edges for getNextEdgeOfClassInGraph(EdgeClass anEdgeClass) are
			// needed
			// 0=Link
			ret.add(new ArrayList<Edge>());
			// 1=SubLink
			ret.add(new ArrayList<Edge>());
			// 2=LinkBack
			ret.add(new ArrayList<Edge>());
		} else {
			// edges for getNextEdgeOfClassInGraph(EdgeClass anEdgeClass,
			// boolean noSubclasses) are needed
			// 0=Link false
			ret.add(new ArrayList<Edge>());
			// 1=SubLink false
			ret.add(new ArrayList<Edge>());
			// 2=LinkBack false
			ret.add(new ArrayList<Edge>());
			// 3=Link true
			ret.add(new ArrayList<Edge>());
			// 4=SubLink true
			ret.add(new ArrayList<Edge>());
			// 5=LinkBack true
			ret.add(new ArrayList<Edge>());
		}
		for (int i = 0; i < RANDOM_VERTEX_COUNT; i++) {
			int edge = rand.nextInt(3);
			Edge e = null;
			switch (edge) {
			case 0:
				Vertex start = nodes[rand.nextInt(2)];
				Vertex end = nodes[rand.nextInt(2) + 1];
				e = g.createLink((AbstractSuperNode) start, (SuperNode) end);
				if (!edgeClass) {
					ret.get(0).add(e);
				} else if (!nosubclasses) {
					ret.get(0).add(e);
				} else {
					ret.get(0).add(e);
					ret.get(3).add(e);
				}
				break;
			case 1:
				start = nodes[1];
				end = nodes[rand.nextInt(2) + 1];
				e = g.createSubLink((DoubleSubNode) start, (SuperNode) end);
				if (!edgeClass) {
					ret.get(0).add(e);
				} else if (!nosubclasses) {
					ret.get(0).add(e);
					ret.get(1).add(e);
				} else {
					ret.get(0).add(e);
					ret.get(1).add(e);
					ret.get(4).add(e);
				}
				break;
			case 2:
				start = nodes[rand.nextInt(2) + 1];
				end = nodes[rand.nextInt(2)];
				e = g
						.createLinkBack((SuperNode) start,
								(AbstractSuperNode) end);
				if (!edgeClass) {
					ret.get(0).add(e);
				} else if (!nosubclasses) {
					ret.get(2).add(e);
				} else {
					ret.get(2).add(e);
					ret.get(5).add(e);
				}
				break;
			}
		}
		commit(g);
		return ret;
	}

	/**
	 * Test for reversedEdge.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getPrevEdgeInGraphTestR0() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createSubLink(v1, v2);
		Edge e3 = g.createLinkBack(v1, v2);
		Edge e1R = e1.getReversedEdge();
		Edge e2R = e2.getReversedEdge();
		Edge e3R = e3.getReversedEdge();
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(e2, e3R.getPrevEdge());
		assertEquals(e1, e2R.getPrevEdge());
		assertNull(e1R.getPrevEdge());
		commit(g);
	}

	/**
	 * Tests if an edge has no previous edge in graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getPrevEdgeInGraphTest0() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getPrevEdge());
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getPrevEdgeInGraphTest1() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v1);
		Edge e2 = g.createLink(v2, v2);
		Edge e3 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(e2, e3.getPrevEdge());
		assertEquals(e1, e2.getPrevEdge());
		assertNull(e1.getPrevEdge());
		commit(g);
	}

	/**
	 * Test in a randomly built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getPrevEdgeInGraphTest2() throws CommitFailedException {
		ArrayList<ArrayList<Edge>> result = createRandomGraph(false, false);

		createReadOnlyTransaction(g);
		Edge current = g.getLastEdge();
		for (int i = g.getECount() - 1; i >= 0; i--) {
			assertEquals(result.get(0).get(i), current);
			current = current.getPrevEdge();
		}
		commit(g);
	}

	// tests for the method Edge getNextEdgeOfClassInGraph(EdgeClass
	// anEdgeClass);

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestEdgeClass0()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextEdge(ecs[0]));
		assertNull(e1.getNextEdge(ecs[1]));
		assertNull(e1.getNextEdge(ecs[2]));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestEdgeClass1()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3, e1.getNextEdge(ecs[0]));
		assertEquals(e3, e1.getNextEdge(ecs[1]));
		assertEquals(e2, e1.getNextEdge(ecs[2]));
		// test of edge e2
		assertEquals(e3, e2.getNextEdge(ecs[0]));
		assertEquals(e3, e2.getNextEdge(ecs[1]));
		assertEquals(e5, e2.getNextEdge(ecs[2]));
		// test of edge e3
		assertEquals(e4, e3.getNextEdge(ecs[0]));
		assertNull(e3.getNextEdge(ecs[1]));
		assertEquals(e5, e3.getNextEdge(ecs[2]));
		// test of edge e4
		assertNull(e4.getNextEdge(ecs[0]));
		assertNull(e4.getNextEdge(ecs[1]));
		assertEquals(e5, e4.getNextEdge(ecs[2]));
		// test of edge e5
		assertNull(e5.getNextEdge(ecs[0]));
		assertNull(e5.getNextEdge(ecs[1]));
		assertNull(e5.getNextEdge(ecs[2]));
		// test for reversedEdge
		Edge e1R = e1.getReversedEdge();
		Edge e2R = e2.getReversedEdge();
		Edge e3R = e3.getReversedEdge();
		Edge e4R = e4.getReversedEdge();
		Edge e5R = e5.getReversedEdge();
		// test of edge e1
		assertEquals(e3, e1R.getNextEdge(ecs[0]));
		assertEquals(e3, e1R.getNextEdge(ecs[1]));
		assertEquals(e2, e1R.getNextEdge(ecs[2]));
		// test of edge e2
		assertEquals(e3, e2R.getNextEdge(ecs[0]));
		assertEquals(e3, e2R.getNextEdge(ecs[1]));
		assertEquals(e5, e2R.getNextEdge(ecs[2]));
		// test of edge e3
		assertEquals(e4, e3R.getNextEdge(ecs[0]));
		assertNull(e3R.getNextEdge(ecs[1]));
		assertEquals(e5, e3R.getNextEdge(ecs[2]));
		// test of edge e4
		assertNull(e4R.getNextEdge(ecs[0]));
		assertNull(e4R.getNextEdge(ecs[1]));
		assertEquals(e5, e4R.getNextEdge(ecs[2]));
		// test of edge e5
		assertNull(e5R.getNextEdge(ecs[0]));
		assertNull(e5R.getNextEdge(ecs[1]));
		assertNull(e5R.getNextEdge(ecs[2]));
		commit(g);
	}

	/**
	 * Test in a randomly built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestEdgeClass2()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();
		ArrayList<ArrayList<Edge>> result = createRandomGraph(true, false);

		createReadOnlyTransaction(g);
		Edge counter = g.getFirstEdge(ecs[0]);
		for (Edge e : result.get(0)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(ecs[0]);
		}
		counter = g.getFirstEdge(ecs[1]);
		for (Edge e : result.get(1)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(ecs[1]);
		}
		counter = g.getFirstEdge(ecs[2]);
		for (Edge e : result.get(2)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(ecs[2]);
		}
		commit(g);
	}

	// tests for the method Edge getNextEdgeOfClassInGraph(Class<? extends Edge>
	// anEdgeClass);

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestClass0()
			throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextEdge(Link.class));
		assertNull(e1.getNextEdge(SubLink.class));
		assertNull(e1.getNextEdge(LinkBack.class));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestClass1()
			throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3, e1.getNextEdge(Link.class));
		assertEquals(e3, e1.getNextEdge(SubLink.class));
		assertEquals(e2, e1.getNextEdge(LinkBack.class));
		// test of edge e2
		assertEquals(e3, e2.getNextEdge(Link.class));
		assertEquals(e3, e2.getNextEdge(SubLink.class));
		assertEquals(e5, e2.getNextEdge(LinkBack.class));
		// test of edge e3
		assertEquals(e4, e3.getNextEdge(Link.class));
		assertNull(e3.getNextEdge(SubLink.class));
		assertEquals(e5, e3.getNextEdge(LinkBack.class));
		// test of edge e4
		assertNull(e4.getNextEdge(Link.class));
		assertNull(e4.getNextEdge(SubLink.class));
		assertEquals(e5, e4.getNextEdge(LinkBack.class));
		// test of edge e5
		assertNull(e5.getNextEdge(Link.class));
		assertNull(e5.getNextEdge(SubLink.class));
		assertNull(e5.getNextEdge(LinkBack.class));
		// test of reversedEdges
		e1 = e1.getReversedEdge();
		e2 = e2.getReversedEdge();
		e3 = e3.getReversedEdge();
		e4 = e4.getReversedEdge();
		e5 = e5.getReversedEdge();
		// test of edge e1
		assertEquals(e3.getNormalEdge(), e1.getNextEdge(Link.class));
		assertEquals(e3.getNormalEdge(), e1.getNextEdge(SubLink.class));
		assertEquals(e2.getNormalEdge(), e1.getNextEdge(LinkBack.class));
		// test of edge e2
		assertEquals(e3.getNormalEdge(), e2.getNextEdge(Link.class));
		assertEquals(e3.getNormalEdge(), e2.getNextEdge(SubLink.class));
		assertEquals(e5.getNormalEdge(), e2.getNextEdge(LinkBack.class));
		// test of edge e3
		assertEquals(e4.getNormalEdge(), e3.getNextEdge(Link.class));
		assertNull(e3.getNextEdge(SubLink.class));
		assertEquals(e5.getNormalEdge(), e3.getNextEdge(LinkBack.class));
		// test of edge e4
		assertNull(e4.getNextEdge(Link.class));
		assertNull(e4.getNextEdge(SubLink.class));
		assertEquals(e5.getNormalEdge(), e4.getNextEdge(LinkBack.class));
		// test of edge e5
		assertNull(e5.getNextEdge(Link.class));
		assertNull(e5.getNextEdge(SubLink.class));
		assertNull(e5.getNextEdge(LinkBack.class));
		commit(g);
	}

	/**
	 * Test in a randomly built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestClass2()
			throws CommitFailedException {
		ArrayList<ArrayList<Edge>> result = createRandomGraph(true, false);

		createReadOnlyTransaction(g);
		Edge counter = g.getFirstEdge(Link.class);
		for (Edge e : result.get(0)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(Link.class);
		}
		counter = g.getFirstEdge(SubLink.class);
		for (Edge e : result.get(1)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(SubLink.class);
		}
		counter = g.getFirstEdge(LinkBack.class);
		for (Edge e : result.get(2)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(LinkBack.class);
		}
		commit(g);
	}

	// tests for the method Edge getNextEdgeOfClassInGraph(EdgeClass
	// anEdgeClass);

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestEdgeClassBoolean0()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextEdge(ecs[0], false));
		assertNull(e1.getNextEdge(ecs[1], false));
		assertNull(e1.getNextEdge(ecs[2], false));
		assertNull(e1.getNextEdge(ecs[0], true));
		assertNull(e1.getNextEdge(ecs[1], true));
		assertNull(e1.getNextEdge(ecs[2], true));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestEdgeClassBoolean1()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3, e1.getNextEdge(ecs[0], false));
		assertEquals(e3, e1.getNextEdge(ecs[1], false));
		assertEquals(e2, e1.getNextEdge(ecs[2], false));

		assertEquals(e4, e1.getNextEdge(ecs[0], true));
		assertEquals(e3, e1.getNextEdge(ecs[1], true));
		assertEquals(e2, e1.getNextEdge(ecs[2], true));
		// test of edge e2
		assertEquals(e3, e2.getNextEdge(ecs[0], false));
		assertEquals(e3, e2.getNextEdge(ecs[1], false));
		assertEquals(e5, e2.getNextEdge(ecs[2], false));

		assertEquals(e4, e2.getNextEdge(ecs[0], true));
		assertEquals(e3, e2.getNextEdge(ecs[1], true));
		assertEquals(e5, e2.getNextEdge(ecs[2], true));
		// test of edge e3
		assertEquals(e4, e3.getNextEdge(ecs[0], false));
		assertNull(e3.getNextEdge(ecs[1], false));
		assertEquals(e5, e3.getNextEdge(ecs[2], false));

		assertEquals(e4, e3.getNextEdge(ecs[0], true));
		assertNull(e3.getNextEdge(ecs[1], true));
		assertEquals(e5, e3.getNextEdge(ecs[2], true));
		// test of edge e4
		assertNull(e4.getNextEdge(ecs[0], false));
		assertNull(e4.getNextEdge(ecs[1], false));
		assertEquals(e5, e4.getNextEdge(ecs[2], false));

		assertNull(e4.getNextEdge(ecs[0], true));
		assertNull(e4.getNextEdge(ecs[1], true));
		assertEquals(e5, e4.getNextEdge(ecs[2], true));
		// test of edge e5
		assertNull(e5.getNextEdge(ecs[0], false));
		assertNull(e5.getNextEdge(ecs[1], false));
		assertNull(e5.getNextEdge(ecs[2], false));

		assertNull(e5.getNextEdge(ecs[0], true));
		assertNull(e5.getNextEdge(ecs[1], true));
		assertNull(e5.getNextEdge(ecs[2], true));
		// test for reversedEdge
		Edge e1R = e1.getReversedEdge();
		Edge e2R = e2.getReversedEdge();
		Edge e3R = e3.getReversedEdge();
		Edge e4R = e4.getReversedEdge();
		Edge e5R = e5.getReversedEdge();
		// test of edge e1
		assertEquals(e3, e1R.getNextEdge(ecs[0], false));
		assertEquals(e3, e1R.getNextEdge(ecs[1], false));
		assertEquals(e2, e1R.getNextEdge(ecs[2], false));

		assertEquals(e4, e1R.getNextEdge(ecs[0], true));
		assertEquals(e3, e1R.getNextEdge(ecs[1], true));
		assertEquals(e2, e1R.getNextEdge(ecs[2], true));
		// test of edge e2
		assertEquals(e3, e2R.getNextEdge(ecs[0], false));
		assertEquals(e3, e2R.getNextEdge(ecs[1], false));
		assertEquals(e5, e2R.getNextEdge(ecs[2], false));

		assertEquals(e4, e2R.getNextEdge(ecs[0], true));
		assertEquals(e3, e2R.getNextEdge(ecs[1], true));
		assertEquals(e5, e2R.getNextEdge(ecs[2], true));
		// test of edge e3
		assertEquals(e4, e3R.getNextEdge(ecs[0], false));
		assertNull(e3R.getNextEdge(ecs[1], false));
		assertEquals(e5, e3R.getNextEdge(ecs[2], false));

		assertEquals(e4, e3R.getNextEdge(ecs[0], true));
		assertNull(e3R.getNextEdge(ecs[1], true));
		assertEquals(e5, e3R.getNextEdge(ecs[2], true));
		// test of edge e4
		assertNull(e4R.getNextEdge(ecs[0], false));
		assertNull(e4R.getNextEdge(ecs[1], false));
		assertEquals(e5, e4R.getNextEdge(ecs[2], false));

		assertNull(e4R.getNextEdge(ecs[0], true));
		assertNull(e4R.getNextEdge(ecs[1], true));
		assertEquals(e5, e4R.getNextEdge(ecs[2], true));
		// test of edge e5
		assertNull(e5R.getNextEdge(ecs[0], false));
		assertNull(e5R.getNextEdge(ecs[1], false));
		assertNull(e5R.getNextEdge(ecs[2], false));

		assertNull(e5R.getNextEdge(ecs[0], true));
		assertNull(e5R.getNextEdge(ecs[1], true));
		assertNull(e5R.getNextEdge(ecs[2], true));
		commit(g);
	}

	/**
	 * Test in a randomly built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestEdgeClassBoolean2()
			throws CommitFailedException {
		EdgeClass[] ecs = getEdgeClasses();
		ArrayList<ArrayList<Edge>> result = createRandomGraph(true, true);

		createReadOnlyTransaction(g);
		Edge counter = g.getFirstEdge(ecs[0], false);
		for (Edge e : result.get(0)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(ecs[0], false);
		}
		counter = g.getFirstEdge(ecs[0], true);
		for (Edge e : result.get(3)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(ecs[0], true);
		}
		counter = g.getFirstEdge(ecs[1], false);
		for (Edge e : result.get(1)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(ecs[1], false);
		}
		counter = g.getFirstEdge(ecs[1], true);
		for (Edge e : result.get(4)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(ecs[1], true);
		}
		counter = g.getFirstEdge(ecs[2], false);
		for (Edge e : result.get(2)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(ecs[2], false);
		}
		counter = g.getFirstEdge(ecs[2], true);
		for (Edge e : result.get(5)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(ecs[2], true);
		}
		commit(g);
	}

	// tests for the method Edge getNextEdgeOfClassInGraph(Class<? extends Edge>
	// anEdgeClass, boolean noSubclasses);

	/**
	 * An edge which has no following edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestClassBoolean0()
			throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e1.getNextEdge(Link.class, false));
		assertNull(e1.getNextEdge(SubLink.class, false));
		assertNull(e1.getNextEdge(LinkBack.class, false));
		assertNull(e1.getNextEdge(Link.class, true));
		assertNull(e1.getNextEdge(SubLink.class, true));
		assertNull(e1.getNextEdge(LinkBack.class, true));
		commit(g);
	}

	/**
	 * Test in a manually built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestClassBoolean1()
			throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLinkBack(v1, v2);
		Edge e3 = g.createSubLink(v2, v1);
		Edge e4 = g.createLink(v1, v1);
		Edge e5 = g.createLinkBack(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of edge e1
		assertEquals(e3, e1.getNextEdge(Link.class, false));
		assertEquals(e3, e1.getNextEdge(SubLink.class, false));
		assertEquals(e2, e1.getNextEdge(LinkBack.class, false));

		assertEquals(e4, e1.getNextEdge(Link.class, true));
		assertEquals(e3, e1.getNextEdge(SubLink.class, true));
		assertEquals(e2, e1.getNextEdge(LinkBack.class, true));
		// test of edge e2
		assertEquals(e3, e2.getNextEdge(Link.class, false));
		assertEquals(e3, e2.getNextEdge(SubLink.class, false));
		assertEquals(e5, e2.getNextEdge(LinkBack.class, false));

		assertEquals(e4, e2.getNextEdge(Link.class, true));
		assertEquals(e3, e2.getNextEdge(SubLink.class, true));
		assertEquals(e5, e2.getNextEdge(LinkBack.class, true));
		// test of edge e3
		assertEquals(e4, e3.getNextEdge(Link.class, false));
		assertNull(e3.getNextEdge(SubLink.class, false));
		assertEquals(e5, e3.getNextEdge(LinkBack.class, false));

		assertEquals(e4, e3.getNextEdge(Link.class, true));
		assertNull(e3.getNextEdge(SubLink.class, true));
		assertEquals(e5, e3.getNextEdge(LinkBack.class, true));
		// test of edge e4
		assertNull(e4.getNextEdge(Link.class, false));
		assertNull(e4.getNextEdge(SubLink.class, false));
		assertEquals(e5, e4.getNextEdge(LinkBack.class, false));

		assertNull(e4.getNextEdge(Link.class, true));
		assertNull(e4.getNextEdge(SubLink.class, true));
		assertEquals(e5, e4.getNextEdge(LinkBack.class, true));
		// test of edge e5
		assertNull(e5.getNextEdge(Link.class, false));
		assertNull(e5.getNextEdge(SubLink.class, false));
		assertNull(e5.getNextEdge(LinkBack.class, false));

		assertNull(e5.getNextEdge(Link.class, true));
		assertNull(e5.getNextEdge(SubLink.class, true));
		assertNull(e5.getNextEdge(LinkBack.class, true));
		// test for reversedEdge
		Edge e1R = e1.getReversedEdge();
		Edge e2R = e2.getReversedEdge();
		Edge e3R = e3.getReversedEdge();
		Edge e4R = e4.getReversedEdge();
		Edge e5R = e5.getReversedEdge();
		// test of edge e1
		assertEquals(e3, e1R.getNextEdge(Link.class, false));
		assertEquals(e3, e1R.getNextEdge(SubLink.class, false));
		assertEquals(e2, e1R.getNextEdge(LinkBack.class, false));

		assertEquals(e4, e1R.getNextEdge(Link.class, true));
		assertEquals(e3, e1R.getNextEdge(SubLink.class, true));
		assertEquals(e2, e1R.getNextEdge(LinkBack.class, true));
		// test of edge e2
		assertEquals(e3, e2R.getNextEdge(Link.class, false));
		assertEquals(e3, e2R.getNextEdge(SubLink.class, false));
		assertEquals(e5, e2R.getNextEdge(LinkBack.class, false));

		assertEquals(e4, e2R.getNextEdge(Link.class, true));
		assertEquals(e3, e2R.getNextEdge(SubLink.class, true));
		assertEquals(e5, e2R.getNextEdge(LinkBack.class, true));
		// test of edge e3
		assertEquals(e4, e3R.getNextEdge(Link.class, false));
		assertNull(e3R.getNextEdge(SubLink.class, false));
		assertEquals(e5, e3R.getNextEdge(LinkBack.class, false));

		assertEquals(e4, e3R.getNextEdge(Link.class, true));
		assertNull(e3R.getNextEdge(SubLink.class, true));
		assertEquals(e5, e3R.getNextEdge(LinkBack.class, true));
		// test of edge e4
		assertNull(e4R.getNextEdge(Link.class, false));
		assertNull(e4R.getNextEdge(SubLink.class, false));
		assertEquals(e5, e4R.getNextEdge(LinkBack.class, false));

		assertNull(e4R.getNextEdge(Link.class, true));
		assertNull(e4R.getNextEdge(SubLink.class, true));
		assertEquals(e5, e4R.getNextEdge(LinkBack.class, true));
		// test of edge e5
		assertNull(e5R.getNextEdge(Link.class, false));
		assertNull(e5R.getNextEdge(SubLink.class, false));
		assertNull(e5R.getNextEdge(LinkBack.class, false));

		assertNull(e5R.getNextEdge(Link.class, true));
		assertNull(e5R.getNextEdge(SubLink.class, true));
		assertNull(e5R.getNextEdge(LinkBack.class, true));
		commit(g);
	}

	/**
	 * Test in a randomly built graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNextEdgeOfClassInGraphTestClassBoolean2()
			throws CommitFailedException {
		ArrayList<ArrayList<Edge>> result = createRandomGraph(true, true);

		createReadOnlyTransaction(g);
		Edge counter = g.getFirstEdge(Link.class, false);
		for (Edge e : result.get(0)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(Link.class, false);
		}
		counter = g.getFirstEdge(Link.class, true);
		for (Edge e : result.get(3)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(Link.class, true);
		}
		counter = g.getFirstEdge(SubLink.class, false);
		for (Edge e : result.get(1)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(SubLink.class, false);
		}
		counter = g.getFirstEdge(SubLink.class, true);
		for (Edge e : result.get(4)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(SubLink.class, true);
		}
		counter = g.getFirstEdge(LinkBack.class, false);
		for (Edge e : result.get(2)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(LinkBack.class, false);
		}
		counter = g.getFirstEdge(LinkBack.class, true);
		for (Edge e : result.get(5)) {
			assertEquals(e, counter);
			counter = counter.getNextEdge(LinkBack.class, true);
		}
		commit(g);
	}

	// tests of the method Vertex getAlpha();
	// (tested in IncidenceListTest.java)

	// tests of the method Vertex getOmega();
	// (tested in IncidenceListTest.java)

	// tests of the method boolean isBefore(Edge e);

	/**
	 * Tests if an edge is before itself.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void isBeforeTest0() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertFalse(e1.isBeforeIncidence(e1));
		commit(g);
	}

	/**
	 * Tests if an edge is direct before another.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void isBeforeTest2() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e1.isBeforeIncidence(e2));
		commit(g);
	}

	/**
	 * Tests if an edge is before another.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void isBeforeTest3() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		g.createLink(v1, v2);
		Edge e2 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e1.isBeforeIncidence(e2));
		assertFalse(e2.isBeforeIncidence(e1));
		commit(g);
	}

	// tests of the method boolean isAfter(Edge e);

	/**
	 * Tests if an edge is after itself.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void isAfterTest0() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertFalse(e1.isAfterIncidence(e1));
		commit(g);
	}

	/**
	 * Tests if an edge is direct after another.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void isAfterTest2() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e2 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e2.isAfterIncidence(e1));
		commit(g);
	}

	/**
	 * Tests if an edge is after another.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void isAfterTest3() throws CommitFailedException {
		Edge e1;
		Edge e2;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		g.createLink(v1, v2);
		e2 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e2.isAfterIncidence(e1));
		assertFalse(e1.isAfterIncidence(e2));
		commit(g);

	}

	// tests of the method boolean isBeforeInGraph(Edge e);
	// (tested in EdgeListTest.java)

	/**
	 * Tests if an edge is before itself.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void isBeforeInGraphTest0() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertFalse(e1.isBeforeEdge(e1));
		commit(g);

	}

	// tests of the method boolean isAfterInGraph(Edge e);
	// (tested in EdgeListTest.java)

	/**
	 * Tests if an edge is before itself.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void isAfterInGraphTest0() throws CommitFailedException {
		Edge e1;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertFalse(e1.isAfterEdge(e1));
		commit(g);
	}

	// tests of the method void putBeforeInGraph(Edge e);
	// (tested in EdgeListTest.java)

	// tests of the method void putAfterInGraph(Edge e);
	// (tested in EdgeListTest.java)

	// tests of the method void delete();
	// (tested in EdgeListTest.java)

	// tests of the method void setAlpha(Vertex v);

	/**
	 * Tests if the incident edges of <code>c</code> equals the edges of
	 * <code>incidentEdges</code>.
	 * 
	 * @param v
	 * @param incidentEdges
	 * @throws CommitFailedException
	 */
	private void testIncidenceList(Vertex v, Edge... incidentEdges)
			throws CommitFailedException {
		Iterable<Edge> incidences;
		createTransaction(g);
		assertEquals(incidentEdges.length, v.getDegree());
		incidences = v.incidences();

		int i = 0;
		for (Edge e : incidences) {
			assertEquals(incidentEdges[i], e);
			i++;
		}
		commit(g);
	}

	/**
	 * Alpha of an edge is changed to another vertex.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setAlphaTest0() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		long v1vers;
		long v2vers;
		long v3vers;
		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		v3vers = v3.getIncidenceListVersion();
		e1.setAlpha(v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v3, e1.getAlpha());
		assertTrue(v1.isIncidenceListModified(v1vers));
		assertFalse(v2.isIncidenceListModified(v2vers));
		assertTrue(v3.isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		commit(g);

		testIncidenceList(v1);
		testIncidenceList(v2, reversedEdge);
		testIncidenceList(v3, e1);

	}

	/**
	 * Alpha of an reversedEdge is changed to another vertex.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setAlphaTestR0() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		long v1vers;
		long v2vers;
		long v3vers;
		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2).getReversedEdge();
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		v3vers = v3.getIncidenceListVersion();
		e1.setAlpha(v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v3, e1.getAlpha());
		assertTrue(v1.isIncidenceListModified(v1vers));
		assertFalse(v2.isIncidenceListModified(v2vers));
		assertTrue(v3.isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		commit(g);

		testIncidenceList(v1);
		testIncidenceList(v2, e1);
		testIncidenceList(v3, reversedEdge);

	}

	/**
	 * Alpha of an edge is set to the previous alpha vertex.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setAlphaTest1() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		Edge e1;
		long v1vers;
		long v2vers;
		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		e1.setAlpha(v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v1, e1.getAlpha());
		assertFalse(v1.isIncidenceListModified(v1vers));
		assertFalse(v2.isIncidenceListModified(v2vers));
		Edge reversedEdge = e1.getReversedEdge();
		commit(g);

		testIncidenceList(v1, e1);
		testIncidenceList(v2, reversedEdge);

	}

	/**
	 * Alpha of an edge is changed to the omega vertex.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setAlphaTest2() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		Edge e1;
		long v1vers;
		long v2vers;

		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		commit(g);

		createTransaction(g);
		e1.setAlpha(v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v2, e1.getAlpha());
		assertTrue(v1.isIncidenceListModified(v1vers));
		assertTrue(v2.isIncidenceListModified(v2vers));
		Edge reversedEdge = e1.getReversedEdge();
		commit(g);

		testIncidenceList(v1);
		testIncidenceList(v2, reversedEdge, e1);
	}

	/**
	 * Alpha of an edge is changed to another vertex. And there exists further
	 * edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setAlphaTest3() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		Edge e2;
		Edge e3;
		long v1vers;
		long v2vers;
		long v3vers;

		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v3, v1);
		e2 = g.createLink(v1, v2);
		e3 = g.createLink(v2, v3);
		commit(g);

		createReadOnlyTransaction(g);
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		v3vers = v3.getIncidenceListVersion();
		commit(g);

		createTransaction(g);
		e2.setAlpha(v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v3, e2.getAlpha());
		assertTrue(v1.isIncidenceListModified(v1vers));
		assertFalse(v2.isIncidenceListModified(v2vers));
		assertTrue(v3.isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		Edge reversedEdge2 = e2.getReversedEdge();
		Edge reversedEdge3 = e3.getReversedEdge();
		commit(g);

		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2, reversedEdge2, e3);
		testIncidenceList(v3, e1, reversedEdge3, e2);

	}

	/**
	 * An exception should occur if you try to set alpha to a vertex which type
	 * isn't allowed as an alpha vertex for that edge.
	 * 
	 * @throws CommitFailedException
	 */
	@Test(expected = GraphException.class)
	public void setAlphaTest4() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		SuperNode v2 = g.createSuperNode();
		Edge e1 = g.createLink(v1, v2);
		e1.setAlpha(v2);
		commit(g);
	}

	/**
	 * Creates a random graph and returns an 2-dim ArrayList ret.get(0) =
	 * incident edges of v1 ret.get(1) = incident edges of v2 ret.get(2) =
	 * incident edges of v3
	 * 
	 * @return ret
	 * @throws CommitFailedException
	 */
	private ArrayList<ArrayList<Edge>> createRandomGraph()
			throws CommitFailedException {
		ArrayList<ArrayList<Edge>> ret = new ArrayList<ArrayList<Edge>>(6);
		ret.add(new ArrayList<Edge>());
		ret.add(new ArrayList<Edge>());
		ret.add(new ArrayList<Edge>());

		createTransaction(g);
		Vertex[] nodes = new Vertex[] { g.createSubNode(),
				g.createDoubleSubNode(), g.createSuperNode() };
		for (int i = 0; i < RANDOM_VERTEX_COUNT; i++) {
			int edge = rand.nextInt(3);
			switch (edge) {
			case 0:
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				Edge e = g.createLink((AbstractSuperNode) nodes[start],
						(SuperNode) nodes[end]);
				ret.get(start).add(e);
				ret.get(end).add(e.getReversedEdge());
				break;
			case 1:
				start = 1;
				end = rand.nextInt(2) + 1;
				e = g.createSubLink((DoubleSubNode) nodes[start],
						(SuperNode) nodes[end]);
				ret.get(start).add(e);
				ret.get(end).add(e.getReversedEdge());
				break;
			case 2:
				start = rand.nextInt(2) + 1;
				end = rand.nextInt(2);
				e = g.createLinkBack((SuperNode) nodes[start],
						(AbstractSuperNode) nodes[end]);
				ret.get(start).add(e);
				ret.get(end).add(e.getReversedEdge());
				break;
			}
		}
		commit(g);
		return ret;
	}

	/**
	 * Random Test
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setAlphaTest5() throws CommitFailedException {
		ArrayList<ArrayList<Edge>> incidences = createRandomGraph();
		for (int i = 0; i < RANDOM_VERTEX_COUNT; i++) {

			createReadOnlyTransaction(g);
			int edgeId = rand.nextInt(g.getECount()) + 1;
			Edge e = g.getEdge(edgeId);
			int oldAlphaId = e.getAlpha().getId();
			int newAlphaId = rand.nextInt(3) + 1;
			Vertex newAlpha = g.getVertex(newAlphaId);
			commit(g);

			createTransaction(g);
			try {
				e.setAlpha(newAlpha);
				if (oldAlphaId != newAlphaId) {
					incidences.get(oldAlphaId - 1).remove(e);
					incidences.get(newAlphaId - 1).add(e);
				}

			} catch (GraphException ge) {
				if ((e instanceof SubLink)
						&& (newAlpha instanceof DoubleSubNode)) {
					fail("SubLink can have an alpha of type "
							+ newAlpha.getClass().getName());
				} else if ((e instanceof Link) && !(e instanceof SubLink)
						&& (newAlpha instanceof AbstractSuperNode)) {
					fail("Link can have an alpha of type "
							+ newAlpha.getClass().getName());
				} else if ((e instanceof LinkBack)
						&& (newAlpha instanceof SuperNode)) {
					fail("LinkBack can have an alpha of type "
							+ newAlpha.getClass().getName());
				}
			}
			commit(g);
		}

		createReadOnlyTransaction(g);
		Vertex vertex = g.getVertex(1);
		Edge[] array = incidences.get(0).toArray(new Edge[0]);
		Vertex vertex2 = g.getVertex(2);
		Edge[] array2 = incidences.get(1).toArray(new Edge[0]);
		Vertex vertex3 = g.getVertex(3);
		Edge[] array3 = incidences.get(2).toArray(new Edge[0]);
		commit(g);

		testIncidenceList(vertex, array);
		testIncidenceList(vertex2, array2);
		testIncidenceList(vertex3, array3);

	}

	// tests of the method void setOmega(Vertex v);

	/**
	 * Omega of an edge is changed to another vertex.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setOmegaTest0() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		long v1vers;
		long v2vers;
		long v3vers;
		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		v3vers = v3.getIncidenceListVersion();
		e1.setOmega(v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v3, e1.getOmega());
		assertFalse(v1.isIncidenceListModified(v1vers));
		assertTrue(v2.isIncidenceListModified(v2vers));
		assertTrue(v3.isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		commit(g);

		testIncidenceList(v1, e1);
		testIncidenceList(v2);
		testIncidenceList(v3, reversedEdge);

	}

	/**
	 * Omega of an reversedEdge is changed to another vertex.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setOmegaTestR0() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		long v1vers;
		long v2vers;
		long v3vers;
		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2).getReversedEdge();
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		v3vers = v3.getIncidenceListVersion();
		e1.setOmega(v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v3, e1.getOmega());
		assertFalse(v1.isIncidenceListModified(v1vers));
		assertTrue(v2.isIncidenceListModified(v2vers));
		assertTrue(v3.isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		commit(g);

		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2);
		testIncidenceList(v3, e1);

	}

	/**
	 * Omega of an edge is set to the previous omega vertex.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setOmegaTest1() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		Edge e1;
		long v1vers;
		long v2vers;
		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		e1.setOmega(v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v2, e1.getOmega());
		assertFalse(v1.isIncidenceListModified(v1vers));
		assertFalse(v2.isIncidenceListModified(v2vers));
		Edge reversedEdge = e1.getReversedEdge();
		commit(g);

		testIncidenceList(v1, e1);
		testIncidenceList(v2, reversedEdge);

	}

	/**
	 * Omega of an edge is changed to the alpha vertex.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setOmegaTest2() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		Edge e1;
		long v1vers;
		long v2vers;
		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		e1.setOmega(v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v1, e1.getOmega());
		assertTrue(v1.isIncidenceListModified(v1vers));
		assertTrue(v2.isIncidenceListModified(v2vers));
		Edge reversedEdge = e1.getReversedEdge();
		commit(g);

		testIncidenceList(v1, e1, reversedEdge);
		testIncidenceList(v2);

	}

	/**
	 * Omega of an edge is changed to another vertex. And there exists further
	 * edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setOmegaTest3() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		Edge e2;
		Edge e3;
		long v1vers;
		long v2vers;
		long v3vers;

		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v3, v1);
		e2 = g.createLink(v1, v2);
		e3 = g.createLink(v2, v3);
		commit(g);
		createReadOnlyTransaction(g);
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		v3vers = v3.getIncidenceListVersion();
		commit(g);

		createTransaction(g);
		e2.setOmega(v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v3, e2.getOmega());
		assertFalse(v1.isIncidenceListModified(v1vers));
		assertTrue(v2.isIncidenceListModified(v2vers));
		assertTrue(v3.isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		Edge reversedEdge2 = e2.getReversedEdge();
		Edge reversedEdge3 = e3.getReversedEdge();
		commit(g);

		testIncidenceList(v1, reversedEdge, e2);
		testIncidenceList(v2, e3);
		testIncidenceList(v3, e1, reversedEdge3, reversedEdge2);

	}

	/**
	 * An exception should occur if you try to set omega to a vertex which type
	 * isn't allowed as an omega vertex for that edge.
	 * 
	 * @throws CommitFailedException
	 */
	@Test(expected = GraphException.class)
	public void setOmegaTest4() throws CommitFailedException {
		createTransaction(g);
		SubNode v1 = g.createSubNode();
		SuperNode v2 = g.createSuperNode();
		Edge e1 = g.createLink(v1, v2);
		e1.setOmega(v1);
		commit(g);
	}

	/**
	 * Random Test
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setOmegaTest5() throws CommitFailedException {
		ArrayList<ArrayList<Edge>> incidences = createRandomGraph();
		for (int i = 0; i < RANDOM_VERTEX_COUNT; i++) {
			createReadOnlyTransaction(g);
			int edgeId = rand.nextInt(g.getECount()) + 1;
			Edge e = g.getEdge(edgeId);
			int oldOmegaId = e.getOmega().getId();
			int newOmegaId = rand.nextInt(3) + 1;
			Vertex newOmega = g.getVertex(newOmegaId);
			commit(g);

			try {

				createTransaction(g);
				e.setOmega(newOmega);
				commit(g);

				createReadOnlyTransaction(g);
				if (oldOmegaId != newOmegaId) {
					incidences.get(oldOmegaId - 1).remove(e.getReversedEdge());
					incidences.get(newOmegaId - 1).add(e.getReversedEdge());
				}
				commit(g);

			} catch (GraphException ge) {
				if ((e instanceof Link) && (newOmega instanceof SuperNode)) {
					fail("Link can have an alpha of type "
							+ newOmega.getClass().getName());
				} else if ((e instanceof LinkBack)
						&& (newOmega instanceof AbstractSuperNode)) {
					fail("LinkBack can have an alpha of type "
							+ newOmega.getClass().getName());
				}
			}
		}

		createReadOnlyTransaction(g);
		Vertex vertex = g.getVertex(1);
		Edge[] array = incidences.get(0).toArray(new Edge[0]);
		Vertex vertex2 = g.getVertex(2);
		Edge[] array2 = incidences.get(1).toArray(new Edge[0]);
		Edge[] array3 = incidences.get(2).toArray(new Edge[0]);
		Vertex vertex3 = g.getVertex(3);
		commit(g);

		testIncidenceList(vertex, array);
		testIncidenceList(vertex2, array2);
		testIncidenceList(vertex3, array3);

	}

	// tests of the method void setThis(Vertex v);

	/**
	 * This of an edge is changed to another vertex. And there exists further
	 * edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setThisTest3() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		Edge e2;
		Edge e3;
		long v1vers;
		long v2vers;
		long v3vers;
		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v3, v1);
		e2 = g.createLink(v1, v2);
		e3 = g.createLink(v2, v3);
		commit(g);

		createReadOnlyTransaction(g);
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		v3vers = v3.getIncidenceListVersion();
		commit(g);

		createTransaction(g);
		e2.setThis(v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v3, e2.getThis());
		assertTrue(v1.isIncidenceListModified(v1vers));
		assertFalse(v2.isIncidenceListModified(v2vers));
		assertTrue(v3.isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		Edge reversedEdge2 = e2.getReversedEdge();
		Edge reversedEdge3 = e3.getReversedEdge();
		commit(g);

		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2, reversedEdge2, e3);
		testIncidenceList(v3, e1, reversedEdge3, e2);

		createReadOnlyTransaction(g);
		// test ReversedEdge
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		v3vers = v3.getIncidenceListVersion();
		commit(g);

		createTransaction(g);
		reversedEdge2.setThis(v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v3, e2.getOmega());
		assertFalse(v1.isIncidenceListModified(v1vers));
		assertTrue(v2.isIncidenceListModified(v2vers));
		assertTrue(v3.isIncidenceListModified(v3vers));
		commit(g);

		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2, e3);
		testIncidenceList(v3, e1, reversedEdge3, e2, reversedEdge2);

	}

	// tests of the method void setThat(Vertex v);

	/**
	 * That of an edge is changed to another vertex. And there exists further
	 * edges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setThatTest3() throws CommitFailedException {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		Edge e2;
		Edge e3;
		long v1vers;
		long v2vers;
		long v3vers;
		createTransaction(g);
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v3, v1);
		e2 = g.createLink(v1, v2);
		e3 = g.createLink(v2, v3);
		commit(g);

		createReadOnlyTransaction(g);
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		v3vers = v3.getIncidenceListVersion();
		Edge reversedEdge2 = e2.getReversedEdge();
		commit(g);

		createTransaction(g);
		reversedEdge2.setThat(v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v3, e2.getAlpha());
		assertTrue(v1.isIncidenceListModified(v1vers));
		assertFalse(v2.isIncidenceListModified(v2vers));
		assertTrue(v3.isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		Edge reversedEdge3 = e3.getReversedEdge();
		commit(g);

		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2, reversedEdge2, e3);
		testIncidenceList(v3, e1, reversedEdge3, e2);

		createReadOnlyTransaction(g);
		// test ReversedEdge
		v1vers = v1.getIncidenceListVersion();
		v2vers = v2.getIncidenceListVersion();
		v3vers = v3.getIncidenceListVersion();
		commit(g);

		createTransaction(g);
		e2.setThat(v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(v3, e2.getOmega());
		assertFalse(v1.isIncidenceListModified(v1vers));
		assertTrue(v2.isIncidenceListModified(v2vers));
		assertTrue(v3.isIncidenceListModified(v3vers));
		commit(g);

		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2, e3);
		testIncidenceList(v3, e1, reversedEdge3, e2, reversedEdge2);

	}

	// tests of the method void putEdgeBefore(Edge e);
	// (tested in IncidenceListTest.java)

	// tests of the method void putEdgeAfter(Edge e);
	// (tested in IncidenceListTest.java)

	// tests of the method Edge getNormalEdge();

	/**
	 * Tests on edges and reversedEdges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getNormalEdgeTest0() throws CommitFailedException {
		Edge e1;
		Edge e2;
		Edge e3;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		SuperNode v2 = g.createSuperNode();
		SubNode v3 = g.createSubNode();
		e1 = g.createLink(v3, v2);
		e2 = g.createSubLink(v1, v2);
		e3 = g.createLinkBack(v2, v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(e1, e1.getNormalEdge());
		assertEquals(e1, e1.getReversedEdge().getNormalEdge());
		assertEquals(e2, e2.getNormalEdge());
		assertEquals(e2, e2.getReversedEdge().getNormalEdge());
		assertEquals(e3, e3.getNormalEdge());
		assertEquals(e3, e3.getReversedEdge().getNormalEdge());
		commit(g);
	}

	// tests of the method Edge getReversedEdge();

	/**
	 * Tests on edges and reversedEdges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getReversedEdgeTest0() throws CommitFailedException {
		Edge e1;
		Edge e2;
		Edge e3;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		SuperNode v2 = g.createSuperNode();
		SubNode v3 = g.createSubNode();
		e1 = g.createLink(v3, v2);
		e2 = g.createSubLink(v1, v2);
		e3 = g.createLinkBack(v2, v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(e1.getReversedEdge(), e1.getReversedEdge());
		assertEquals(e1, e1.getReversedEdge().getReversedEdge());
		assertEquals(e2.getReversedEdge(), e2.getReversedEdge());
		assertEquals(e2, e2.getReversedEdge().getReversedEdge());
		assertEquals(e3.getReversedEdge(), e3.getReversedEdge());
		assertEquals(e3, e3.getReversedEdge().getReversedEdge());
		commit(g);
	}

	// tests of the method boolean isNormal();

	/**
	 * Tests on edges and reversedEdges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void isNormalTest0() throws CommitFailedException {
		Edge e1;
		Edge e2;
		Edge e3;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		SuperNode v2 = g.createSuperNode();
		SubNode v3 = g.createSubNode();
		e1 = g.createLink(v3, v2);
		e2 = g.createSubLink(v1, v2);
		e3 = g.createLinkBack(v2, v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertFalse(e1.getReversedEdge().isNormal());
		assertTrue(e1.isNormal());
		assertFalse(e2.getReversedEdge().isNormal());
		assertTrue(e2.isNormal());
		assertFalse(e3.getReversedEdge().isNormal());
		assertTrue(e3.isNormal());
		commit(g);
	}

	// tests of the method boolean isValid();

	/**
	 * Tests on edges and reversedEdges.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void isValidTest0() throws CommitFailedException {
		Edge e1;
		Edge e2;
		Edge e3;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		SuperNode v2 = g.createSuperNode();
		SubNode v3 = g.createSubNode();
		e1 = g.createLink(v3, v2);
		e2 = g.createSubLink(v1, v2);
		e3 = g.createLinkBack(v2, v3);
		e3.delete();
		g.deleteEdge(e2);
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e1.isValid());
		assertFalse(e2.isValid());
		assertFalse(e3.isValid());
		commit(g);
	}

	/*
	 * Test of the Interface GraphElement
	 */

	// tests of the method Graph getGraph();
	/**
	 * Test with edges of two graphs.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getGraphTest() throws CommitFailedException {
		VertexTestGraph anotherGraph = createAnotherGraph();

		createTransaction(g);
		createTransaction(anotherGraph);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		DoubleSubNode v1a = anotherGraph.createDoubleSubNode();
		DoubleSubNode v2a = anotherGraph.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v2);
		Edge e1a = anotherGraph.createLink(v1a, v2a);
		commit(g);
		commit(anotherGraph);

		createReadOnlyTransaction(g);
		createReadOnlyTransaction(anotherGraph);
		assertEquals(g, e1.getGraph());
		assertEquals(anotherGraph, e1a.getGraph());
		assertEquals(g, e1.getReversedEdge().getGraph());
		assertEquals(anotherGraph, e1a.getReversedEdge().getGraph());
		commit(g);
		commit(anotherGraph);
	}

	private VertexTestGraph createAnotherGraph() {
		VertexTestGraph anotherGraph = null;
		switch (implementationType) {
		case STANDARD:
			anotherGraph = VertexTestSchema.instance().createVertexTestGraph();
			break;
		case TRANSACTION:
			anotherGraph = VertexTestSchema.instance()
					.createVertexTestGraphWithTransactionSupport();
			break;
		case SAVEMEM:
			anotherGraph = VertexTestSchema.instance()
					.createVertexTestGraphWithSavememSupport();
			break;
		case DATABASE:
			anotherGraph = dbHandler
					.createVertexTestGraphWithDatabaseSupport("anotherGraph");
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		return anotherGraph;
	}

	// TODO maybe move the following two to graphTest
	/**
	 * Tests if the graphversion is increased by creating a new edge.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void graphModifiedTest1() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		commit(g);

		createReadOnlyTransaction(g);
		long graphversion = g.getGraphVersion();
		commit(g);

		createTransaction(g);
		g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		// assertEquals(graphversion + 1, g.getGraphVersion());
		assertTrue(graphversion < g.getGraphVersion());
		commit(g);
	}

	/**
	 * Tests if the graphversion is increased by deleting an edge.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void graphModifiedTest2() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		long graphversion = g.getGraphVersion();
		commit(g);

		createTransaction(g);
		e1.delete();
		commit(g);

		createReadOnlyTransaction(g);
		// assertEquals(graphversion + 1, g.getGraphVersion());
		assertTrue(graphversion < g.getGraphVersion());
		commit(g);
	}

	/**
	 * Tests if the graphversion is increased by changing the attributes of an
	 * edge.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void graphModifiedTest3() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		long graphversion = g.getGraphVersion();
		commit(g);

		createTransaction(g);
		((Link) e1).set_aString("Test");
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(graphversion < g.getGraphVersion());
		commit(g);
	}

	/*
	 * Test of the Interface AttributedElement
	 */

	// tests of the method AttributedElementClass getAttributedElementClass();
	/**
	 * Some test cases for getAttributedElementClass
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getAttributedElementClassTest() throws CommitFailedException {
		EdgeClass[] edges = getEdgeClasses();

		Edge e1;
		Edge e2;
		Edge e3;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		SubNode v2 = g.createSubNode();
		SuperNode v3 = g.createSuperNode();
		e1 = g.createLink(v2, v3);
		e2 = g.createSubLink(v1, v3);
		e3 = g.createLinkBack(v3, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(edges[0], e1.getAttributedElementClass());
		assertEquals(edges[1], e2.getAttributedElementClass());
		assertEquals(edges[2], e3.getAttributedElementClass());
		commit(g);
	}

	// tests of the method Class<? extends AttributedElement> getM1Class();

	/**
	 * Some test cases for getM1Class
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getM1ClassTest() throws CommitFailedException {
		Edge e1;
		Edge e2;
		Edge e3;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		SubNode v2 = g.createSubNode();
		SuperNode v3 = g.createSuperNode();
		e1 = g.createLink(v2, v3);
		e2 = g.createSubLink(v1, v3);
		e3 = g.createLinkBack(v3, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(Link.class, e1.getM1Class());
		assertEquals(SubLink.class, e2.getM1Class());
		assertEquals(LinkBack.class, e3.getM1Class());
		commit(g);
	}

	// tests of the method GraphClass getGraphClass();

	/**
	 * Some test cases for getGraphClass
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getGraphClassTest() throws CommitFailedException {
		VertexTestGraph anotherGraph = createAnotherGraph();
		GraphClass gc = g.getSchema().getGraphClass();

		createTransaction(g);
		createTransaction(anotherGraph);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v1a = anotherGraph.createDoubleSubNode();
		Edge e1 = g.createLink(v1, v1);
		Edge e1a = anotherGraph.createLink(v1a, v1a);
		commit(g);
		commit(anotherGraph);

		createReadOnlyTransaction(g);
		createReadOnlyTransaction(anotherGraph);
		assertEquals(gc, e1.getGraphClass());
		assertEquals(gc, e1a.getGraphClass());
		assertEquals(gc, e1.getReversedEdge().getGraphClass());
		assertEquals(gc, e1a.getReversedEdge().getGraphClass());
		commit(g);
		commit(anotherGraph);
	}

	// tests of the methods
	// void writeAttributeValues(GraphIO io) throws IOException,
	// GraphIOException;
	// and
	// void readAttributeValues(GraphIO io) throws GraphIOException;

	/**
	 * Test with null values.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void writeReadAttributeValues0() throws GraphIOException,
			IOException, CommitFailedException {
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		SubLink e1 = g.createSubLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		// test of writeAttributeValues
		// TODO check if save command has to be wrapped inside a transaction...
		GraphIO.saveGraphToFile("test.tg", g, null);
		commit(g);

		LineNumberReader reader = new LineNumberReader(
				new FileReader("test.tg"));
		String line = "";
		String[] parts = null;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				line = line.substring(0, line.length() - 1);
			}
			parts = line.split(" ");
			createReadOnlyTransaction(g);
			if (parts[0].equals(((Integer) e1.getId()).toString())
					&& parts[1].equals(e1.getClass().getName())) {
				commit(g);
				break;
			}
			commit(g);
		}
		assertEquals("n", parts[2]);
		assertEquals("0", parts[3]);
		// test of readAttributeValues
		VertexTestGraph loadedgraph = loadTestGraph();

		createReadOnlyTransaction(g);
		createReadOnlyTransaction(loadedgraph);
		SubLink loadede1 = loadedgraph.getFirstSubLink();
		assertEquals(e1.get_aString(), loadede1.get_aString());
		assertEquals(e1.get_anInt(), loadede1.get_anInt());
		commit(loadedgraph);
		commit(g);

		// delete created file
		System.gc();
		reader.close();
		File f = new File("test.tg");
		f.delete();
	}

	private VertexTestGraph loadTestGraph() throws GraphIOException {
		VertexTestGraph loadedgraph = null;
		switch (implementationType) {
		case DATABASE:
		case STANDARD:
			loadedgraph = (VertexTestGraph) GraphIO
					.loadGraphFromFileWithStandardSupport("test.tg", null);
			break;
		case TRANSACTION:
			loadedgraph = (VertexTestGraph) GraphIO
					.loadGraphFromFileWithTransactionSupport("test.tg", null);
			break;
		case SAVEMEM:
			loadedgraph = VertexTestSchema.instance()
					.loadVertexTestGraphWithSavememSupport("test.tg");
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		return loadedgraph;
	}

	/**
	 * Test with values.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void writeReadAttributeValues1() throws GraphIOException,
			IOException, CommitFailedException {

		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		SubLink e1 = g.createSubLink(v1, v2);
		e1.set_anInt(3);
		e1.set_aString("HelloWorld!");
		commit(g);

		createReadOnlyTransaction(g);
		// test of writeAttributeValues
		GraphIO.saveGraphToFile("test.tg", g, null);
		commit(g);

		LineNumberReader reader = new LineNumberReader(
				new FileReader("test.tg"));
		String line = "";
		String[] parts = null;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				line = line.substring(0, line.length() - 1);
			}
			parts = line.split(" ");
			createReadOnlyTransaction(g);
			if (parts[0].equals(((Integer) e1.getId()).toString())
					&& parts[1].equals(e1.getClass().getName())) {
				commit(g);
				break;
			}
			commit(g);
		}
		assertEquals("\"HelloWorld!\"", parts[2]);
		assertEquals("3", parts[3]);
		VertexTestGraph loadedgraph = loadTestGraph();

		createReadOnlyTransaction(g);
		createReadOnlyTransaction(loadedgraph);
		SubLink loadede1 = loadedgraph.getFirstSubLink();
		assertEquals(e1.get_aString(), loadede1.get_aString());
		assertEquals(e1.get_anInt(), loadede1.get_anInt());
		commit(g);
		commit(loadedgraph);

		// delete created file
		System.gc();
		reader.close();
		File f = new File("test.tg");
		f.delete();
	}

	// tests of the method Object getAttribute(String name) throws
	// NoSuchFieldException;

	/**
	 * Tests if the value of the correct attribute is returned.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getAttributeTest0() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v = g.createDoubleSubNode();
		SubLink e = g.createSubLink(v, v);
		e.set_aString("test");
		e.set_anInt(4);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("test", e.getAttribute("aString"));
		assertEquals(4, e.getAttribute("anInt"));
		commit(g);
	}

	/**
	 * Tests if the value of the correct attribute is returned. reversedEdge
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getAttributeTestR0() throws CommitFailedException {
		SubLink e;
		createTransaction(g);
		DoubleSubNode v = g.createDoubleSubNode();
		e = (SubLink) g.createSubLink(v, v).getReversedEdge();
		e.set_aString("test");
		e.set_anInt(4);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("test", e.getAttribute("aString"));
		assertEquals(4, e.getAttribute("anInt"));
		commit(g);
	}

	/**
	 * Tests if an exception is thrown if you want to get an attribute which
	 * doesn't exist.
	 * 
	 * @throws CommitFailedException
	 */
	@Test(expected = NoSuchAttributeException.class)
	public void getAttributeTest1() throws CommitFailedException {
		SubLink e;
		createTransaction(g);
		DoubleSubNode v = g.createDoubleSubNode();
		e = g.createSubLink(v, v);
		commit(g);

		createReadOnlyTransaction(g);
		e.getAttribute("cd");
		commit(g);
	}

	/**
	 * Tests if an exception is thrown if you want to get an attribute with an
	 * empty name.
	 * 
	 * @throws CommitFailedException
	 */
	@Test(expected = NoSuchAttributeException.class)
	public void getAttributeTest2() throws CommitFailedException {
		SubLink e;
		createTransaction(g);
		DoubleSubNode v = g.createDoubleSubNode();
		e = g.createSubLink(v, v);
		commit(g);

		createReadOnlyTransaction(g);
		e.getAttribute("");
		commit(g);
	}

	// tests of the method void setAttribute(String name, Object data) throws
	// NoSuchFieldException;

	/**
	 * Tests if an existing attribute is correct set.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setAttributeTest0() throws CommitFailedException {
		SubLink e;
		createTransaction(g);
		DoubleSubNode v = g.createDoubleSubNode();
		e = g.createSubLink(v, v);
		e.setAttribute("aString", "test");
		e.setAttribute("anInt", 4);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("test", e.getAttribute("aString"));
		assertEquals(4, e.getAttribute("anInt"));
		commit(g);
	}

	/**
	 * Tests if an existing attribute is correct set. reversedEdge
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setAttributeTestR0() throws CommitFailedException {
		SubLink e;
		createTransaction(g);
		DoubleSubNode v = g.createDoubleSubNode();
		e = (SubLink) g.createSubLink(v, v).getReversedEdge();
		e.setAttribute("aString", "test");
		e.setAttribute("anInt", 4);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("test", e.getAttribute("aString"));
		assertEquals(4, e.getAttribute("anInt"));
		commit(g);
	}

	/**
	 * Tests if an existing attribute is set to null.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void setAttributeTest1() throws CommitFailedException {
		SubLink e;
		createTransaction(g);
		DoubleSubNode v = g.createDoubleSubNode();
		e = g.createSubLink(v, v);
		e.setAttribute("aString", null);
		commit(g);

		createReadOnlyTransaction(g);
		assertNull(e.getAttribute("aString"));
		commit(g);
	}

	/**
	 * Tests if an exception is thrown if you want to get an attribute which
	 * doesn't exist.
	 * 
	 * @throws CommitFailedException
	 */
	@Test(expected = NoSuchAttributeException.class)
	public void setAttributeTest2() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v = g.createDoubleSubNode();
		SubLink e = g.createSubLink(v, v);
		e.setAttribute("cd", "a");
		commit(g);
	}

	/**
	 * Tests if an exception is thrown if you want to get an attribute with an
	 * empty name.
	 * 
	 * @throws CommitFailedException
	 */
	@Test(expected = NoSuchAttributeException.class)
	public void setAttributeTest3() throws CommitFailedException {
		createTransaction(g);
		DoubleSubNode v = g.createDoubleSubNode();
		SubLink e = g.createSubLink(v, v);
		e.setAttribute("", "a");
		commit(g);
	}

	// tests of the method Schema getSchema();

	/**
	 * Some tests.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void getSchemaTest() throws CommitFailedException {
		Edge e1;
		Edge e2;
		Edge e3;
		Schema schema;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		SubNode v2 = g.createSubNode();
		SuperNode v3 = g.createSuperNode();
		e1 = g.createLink(v2, v3);
		e2 = g.createSubLink(v1, v3);
		e3 = g.createLinkBack(v3, v2);
		schema = g.getSchema();
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(schema, e1.getSchema());
		assertEquals(schema, e2.getSchema());
		assertEquals(schema, e3.getSchema());
		assertEquals(schema, e1.getReversedEdge().getSchema());
		assertEquals(schema, e2.getReversedEdge().getSchema());
		assertEquals(schema, e3.getReversedEdge().getSchema());
		commit(g);
	}

	/*
	 * Test of the Interface Comparable
	 */

	// tests of the method int compareTo(AttributedElement a);
	/**
	 * Test if a vertex is equal to itself.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void compareToTest0() throws CommitFailedException {
		Edge e1;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(0, e1.compareTo(e1));
		commit(g);
	}

	/**
	 * Test if a vertex is smaller than another.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void compareToTest1() throws CommitFailedException {
		Edge e1;
		Edge e2;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v1);
		e2 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e1.compareTo(e2) < 0);
		commit(g);
	}

	/**
	 * Test if a vertex is greater than another.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void compareToTest2() throws CommitFailedException {
		Edge e1;
		Edge e2;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v1);
		e2 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e2.compareTo(e1) > 0);
		commit(g);
	}

	// tests of the method int compareTo(AttributedElement a); reversedEdge

	/**
	 * Test if a vertex is equal to itself.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void compareToTestR0() throws CommitFailedException {
		Edge e1;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v1).getReversedEdge();
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(0, e1.compareTo(e1));
		commit(g);
	}

	/**
	 * Test if a vertex is smaller than another.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void compareToTestR1() throws CommitFailedException {
		Edge e1;
		Edge e2;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v1).getReversedEdge();
		e2 = g.createLink(v1, v1).getReversedEdge();
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e1.compareTo(e2) < 0);
		commit(g);
	}

	/**
	 * Test if a vertex is greater than another.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void compareToTestR2() throws CommitFailedException {
		Edge e1;
		Edge e2;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v1).getReversedEdge();
		e2 = g.createLink(v1, v1).getReversedEdge();
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e2.compareTo(e1) > 0);
		commit(g);
	}

	// tests of the method int compareTo(AttributedElement a); reversedEdge and
	// normalEdge
	/**
	 * Test if a vertex is equal to itself.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void compareToTestM0() throws CommitFailedException {
		Edge e1;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e1.compareTo(e1.getReversedEdge()) < 0);
		commit(g);
	}

	/**
	 * Test if a vertex is equal to itself.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void compareToTestM1() throws CommitFailedException {
		Edge e1;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e1.getReversedEdge().compareTo(e1) > 0);
		commit(g);
	}

	/*
	 * Test of the generated methods.
	 */

	// test of the methods getAnInt and setAnInt
	@Test
	public void getAnIntTest() throws CommitFailedException {
		SubLink e1;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		e1 = g.createSubLink(v1, v1);
		e1.set_anInt(1);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(1, e1.get_anInt());
		commit(g);

		createTransaction(g);
		e1.set_anInt(2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(2, e1.get_anInt());
		commit(g);

		createTransaction(g);
		e1.set_anInt(3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(3, e1.get_anInt());
		commit(g);
	}

	// test of the methods getAString and setAString
	@Test
	public void getAStringTest() throws CommitFailedException {
		SubLink e1;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		e1 = g.createSubLink(v1, v1);
		e1.set_aString("Test1");
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("Test1", e1.get_aString());
		commit(g);

		createTransaction(g);
		e1.set_aString("Test2");
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("Test2", e1.get_aString());
		commit(g);

		createTransaction(g);
		e1.set_aString("");
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("", e1.get_aString());
		commit(g);
	}

	// test of the method getNextLinkInGraph
	@Test
	public void getNextLinkInGraphTest() throws CommitFailedException {

		SubLink e1;
		SubLink e3;
		Link e4;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		SubNode v2 = g.createSubNode();
		SuperNode v3 = g.createSuperNode();
		e1 = g.createSubLink(v1, v1);
		g.createLinkBack(v3, v2);
		e3 = g.createSubLink(v1, v3);
		e4 = g.createLink(v2, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(e3, e1.getNextLinkInGraph());
		assertEquals(e4, e3.getNextLinkInGraph());
		assertEquals(null, e4.getNextLinkInGraph());

		commit(g);
	}

	// test of the method getNextSubLinkInGraph
	@Test
	public void getNextSubLinkInGraphTest() throws CommitFailedException {
		SubLink e1;
		SubLink e4;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		SubNode v2 = g.createSubNode();
		SuperNode v3 = g.createSuperNode();
		e1 = g.createSubLink(v1, v1);
		g.createLinkBack(v3, v2);
		g.createLink(v2, v1);
		e4 = g.createSubLink(v1, v3);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(e4, e1.getNextSubLinkInGraph());
		assertEquals(null, e4.getNextSubLinkInGraph());
		commit(g);
	}

	// test of the method getNextLink
	@Test
	public void getNextLinkTest() throws CommitFailedException {
		SubLink e1;
		Link e3;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		e1 = g.createSubLink(v1, v1);
		g.createLinkBack(v1, v2);
		e3 = g.createLink(v2, v1);
		g.createSubLink(v2, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(e1.getReversedEdge(), e1.getNextLink());
		assertEquals(e3.getReversedEdge(), ((SubLink) e1.getReversedEdge())
				.getNextLink());
		assertEquals(null, ((Link) e3.getReversedEdge()).getNextLink());
		commit(g);
	}

	// test of the method getNextSubLink
	@Test
	public void getNextSubLinkTest() throws CommitFailedException {
		SubLink e1;
		SubLink e4;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		e1 = g.createSubLink(v1, v1);
		g.createLinkBack(v1, v2);
		g.createLink(v2, v1);
		e4 = g.createSubLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(e1.getReversedEdge(), e1.getNextSubLink());
		assertEquals(e4, ((SubLink) e1.getReversedEdge()).getNextSubLink());
		assertEquals(null, e4.getNextSubLink());
		commit(g);
	}

	// test of the method getNextLink(EdgeDirection orientation)
	@Test
	public void getNextLinkTestEdgeDirection() throws CommitFailedException {
		SubLink e1;
		Link e3;
		Link e5;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		e1 = g.createSubLink(v1, v1);
		g.createLinkBack(v1, v2);
		e3 = g.createLink(v2, v1);
		g.createSubLink(v2, v2);
		e5 = g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(e1.getReversedEdge(), e1.getNextLink(EdgeDirection.INOUT));
		assertEquals(e1.getReversedEdge(), e1.getNextLink(EdgeDirection.IN));
		assertEquals(e5, e1.getNextLink(EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), ((SubLink) e1.getReversedEdge())
				.getNextLink(EdgeDirection.INOUT));
		assertEquals(e3.getReversedEdge(), ((SubLink) e1.getReversedEdge())
				.getNextLink(EdgeDirection.IN));
		assertEquals(e5, ((SubLink) e1.getReversedEdge())
				.getNextLink(EdgeDirection.OUT));
		assertEquals(e5, ((Link) e3.getReversedEdge())
				.getNextLink(EdgeDirection.INOUT));
		assertEquals(null, ((Link) e3.getReversedEdge())
				.getNextLink(EdgeDirection.IN));
		assertEquals(e5, ((Link) e3.getReversedEdge())
				.getNextLink(EdgeDirection.OUT));
		assertEquals(null, e5.getNextLink(EdgeDirection.INOUT));
		assertEquals(null, e5.getNextLink(EdgeDirection.IN));
		assertEquals(null, e5.getNextLink(EdgeDirection.OUT));
		commit(g);
	}

	// test of the method getNextSubLink(EdgeDirection orientation)
	@Test
	public void getNextSubLinkTestEdgeDirection() throws CommitFailedException {
		SubLink e1;
		SubLink e5;
		SubLink e6;
		createTransaction(g);
		DoubleSubNode v1 = g.createDoubleSubNode();
		DoubleSubNode v2 = g.createDoubleSubNode();
		e1 = g.createSubLink(v1, v1);
		g.createLinkBack(v1, v2);
		g.createLink(v2, v1);
		g.createSubLink(v2, v2);
		e5 = g.createSubLink(v1, v2);
		e6 = g.createSubLink(v2, v1);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(e1.getReversedEdge(), e1
				.getNextSubLink(EdgeDirection.INOUT));
		assertEquals(e1.getReversedEdge(), e1.getNextSubLink(EdgeDirection.IN));
		assertEquals(e5, e1.getNextLink(EdgeDirection.OUT));
		assertEquals(e5, ((SubLink) e1.getReversedEdge())
				.getNextSubLink(EdgeDirection.INOUT));
		assertEquals(e6.getReversedEdge(), ((SubLink) e1.getReversedEdge())
				.getNextSubLink(EdgeDirection.IN));
		assertEquals(e5, ((SubLink) e1.getReversedEdge())
				.getNextSubLink(EdgeDirection.OUT));
		assertEquals(e6.getReversedEdge(), e5
				.getNextSubLink(EdgeDirection.INOUT));
		assertEquals(e6.getReversedEdge(), e5.getNextSubLink(EdgeDirection.IN));
		assertEquals(null, e5.getNextSubLink(EdgeDirection.OUT));
		assertEquals(null, ((SubLink) e6.getReversedEdge())
				.getNextSubLink(EdgeDirection.INOUT));
		assertEquals(null, ((SubLink) e6.getReversedEdge())
				.getNextSubLink(EdgeDirection.IN));
		assertEquals(null, ((SubLink) e6.getReversedEdge())
				.getNextSubLink(EdgeDirection.OUT));
		commit(g);
	}
}
