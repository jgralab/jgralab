package de.uni_koblenz.jgralabtest.coretest;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralabtest.schemas.vertextest.*;

public class EdgeTest {
	private VertexTestGraph graph;
	private Random rand;

	/**
	 * Creates a new graph for each test.
	 */
	@Before
	public void setUp() {
		graph = VertexTestSchema.instance().createVertexTestGraph();
		rand = new Random(System.currentTimeMillis());
	}

	/*
	 * Test of the Interface Edge
	 */

	// tests for the method int getId();
	/**
	 * If you create several edges and you delete one, the next edge should get
	 * the id of the deleted edge. If you create a further edge it should get
	 * the next free id.
	 */
	@Test
	public void getIdTest0() {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		Edge e0 = graph.createLink(v0, v1);
		assertEquals(1, e0.getId());
		Edge e1 = graph.createLink(v0, v1);
		assertEquals(2, e1.getId());
		graph.deleteEdge(e0);
		e0 = graph.createLink(v1, v0);
		assertEquals(1, e0.getId());
		e0 = graph.createLink(v1, v0);
		assertEquals(3, e0.getId());
	}

	// tests of the method Edge getNextEdge();
	// (tested in IncidenceListTest)

	// tests of the method Edge getPrevEdge();
	// (tested in IncidenceListTest)

	// tests of the method Edge getNextEdge(EdgeDirection orientation);
	/**
	 * There exists only one edge in the graph
	 */
	@Test
	public void getNextEdgeTestEdgeDirection0() {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		Edge e0 = graph.createLink(v0, v1);
		// edges of vertex v0
		assertEquals(null, e0.getNextEdge(EdgeDirection.INOUT));
		assertEquals(null, e0.getNextEdge(EdgeDirection.OUT));
		assertEquals(null, e0.getNextEdge(EdgeDirection.IN));
		// edges of vertex v1
		assertEquals(null, e0.getReversedEdge()
				.getNextEdge(EdgeDirection.INOUT));
		assertEquals(null, e0.getReversedEdge().getNextEdge(EdgeDirection.OUT));
		assertEquals(null, e0.getReversedEdge().getNextEdge(EdgeDirection.IN));
	}

	/**
	 * Test in a manually built graph.
	 */
	@Test
	public void getNextEdgeTestEdgeDirection1() {
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		Edge e2 = graph.createLink(v1, v2);
		Edge e3 = graph.createLink(v2, v1);
		Edge e4 = graph.createSubLink(v1, v2);
		Edge e5 = graph.createLinkBack(v2, v1);
		// edges of vertex v0
		assertEquals(e2, e1.getNextEdge(EdgeDirection.INOUT));
		assertEquals(e2, e1.getNextEdge(EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e1.getNextEdge(EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e2.getNextEdge(EdgeDirection.INOUT));
		assertEquals(e4, e2.getNextEdge(EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e2.getNextEdge(EdgeDirection.IN));
		assertEquals(e4, e3.getReversedEdge().getNextEdge(EdgeDirection.INOUT));
		assertEquals(e4, e3.getReversedEdge().getNextEdge(EdgeDirection.OUT));
		assertEquals(e5.getReversedEdge(), e3.getReversedEdge().getNextEdge(
				EdgeDirection.IN));
		assertEquals(e5.getReversedEdge(), e4.getNextEdge(EdgeDirection.INOUT));
		assertEquals(null, e4.getNextEdge(EdgeDirection.OUT));
		assertEquals(e5.getReversedEdge(), e4.getNextEdge(EdgeDirection.IN));
		assertEquals(null, e5.getNextEdge(EdgeDirection.INOUT));
		assertEquals(null, e5.getNextEdge(EdgeDirection.OUT));
		assertEquals(null, e5.getNextEdge(EdgeDirection.IN));
		// edges of vertex v1
		assertEquals(e2.getReversedEdge(), e1.getReversedEdge().getNextEdge(
				EdgeDirection.INOUT));
		assertEquals(e3, e1.getReversedEdge().getNextEdge(EdgeDirection.OUT));
		assertEquals(e2.getReversedEdge(), e1.getReversedEdge().getNextEdge(
				EdgeDirection.IN));
		assertEquals(e3, e2.getReversedEdge().getNextEdge(EdgeDirection.INOUT));
		assertEquals(e3, e2.getReversedEdge().getNextEdge(EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e2.getReversedEdge().getNextEdge(
				EdgeDirection.IN));
		assertEquals(e4.getReversedEdge(), e3.getNextEdge(EdgeDirection.INOUT));
		assertEquals(e5, e3.getNextEdge(EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e3.getNextEdge(EdgeDirection.IN));
		assertEquals(e5, e4.getReversedEdge().getNextEdge(EdgeDirection.INOUT));
		assertEquals(e5, e4.getReversedEdge().getNextEdge(EdgeDirection.OUT));
		assertEquals(null, e4.getReversedEdge().getNextEdge(EdgeDirection.IN));
		assertEquals(null, e5.getReversedEdge()
				.getNextEdge(EdgeDirection.INOUT));
		assertEquals(null, e5.getReversedEdge().getNextEdge(EdgeDirection.OUT));
		assertEquals(null, e5.getReversedEdge().getNextEdge(EdgeDirection.IN));
	}

	/**
	 * Test in a randomly built graph
	 */
	@Test
	public void getNextEdgeTestEdgeDirection2() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			DoubleSubNode v0 = graph.createDoubleSubNode();
			DoubleSubNode v1 = graph.createDoubleSubNode();
			Edge[] edges = new Edge[30];
			Edge[] v0inout = new Edge[30];
			Edge[] v0out = new Edge[30];
			Edge[] v0in = new Edge[30];
			Edge[] v1inout = new Edge[30];
			Edge[] v1out = new Edge[30];
			Edge[] v1in = new Edge[30];
			int lastv0o = 0;
			int lastv0i = 0;
			int lastv1o = 0;
			int lastv1i = 0;
			int dir = 0;
			boolean first = true;
			for (int j = 0; j < 30; j++) {
				dir = rand.nextInt(2);
				if (dir == 0) {
					Edge e = graph.createLink(v0, v1);
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
					Edge e = graph.createLink(v1, v0);
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
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				if (e.getAlpha() == v0) {
					assertEquals(v0inout[k], e.getNextEdge(EdgeDirection.INOUT));
					assertEquals(v0out[k], e.getNextEdge(EdgeDirection.OUT));
					assertEquals(v0in[k], e.getNextEdge(EdgeDirection.IN));
					assertEquals(v1inout[k], e.getReversedEdge().getNextEdge(
							EdgeDirection.INOUT));
					assertEquals(v1out[k], e.getReversedEdge().getNextEdge(
							EdgeDirection.OUT));
					assertEquals(v1in[k], e.getReversedEdge().getNextEdge(
							EdgeDirection.IN));
				} else {
					assertEquals(v0inout[k], e.getReversedEdge().getNextEdge(
							EdgeDirection.INOUT));
					assertEquals(v0out[k], e.getReversedEdge().getNextEdge(
							EdgeDirection.OUT));
					assertEquals(v0in[k], e.getReversedEdge().getNextEdge(
							EdgeDirection.IN));
					assertEquals(v1inout[k], e.getNextEdge(EdgeDirection.INOUT));
					assertEquals(v1out[k], e.getNextEdge(EdgeDirection.OUT));
					assertEquals(v1in[k], e.getNextEdge(EdgeDirection.IN));
				}
			}
		}
	}

	// tests for the method Edge getNextEdgeOfClass(EdgeClass anEdgeClass);

	/**
	 * Creates an array of the EdgeClasses.
	 * 
	 * @return {Link, SubLink, LinkBack}
	 */
	private EdgeClass[] getEdgeClasses() {
		EdgeClass[] ecs = new EdgeClass[3];
		List<EdgeClass> a = graph.getSchema()
				.getEdgeClassesInTopologicalOrder();
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
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClass0() {
		EdgeClass[] ecs = getEdgeClasses();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		assertEquals(null, e1.getNextEdgeOfClass(ecs[0]));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1]));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2]));
	}

	/**
	 * Test in a manually built graph.
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClass1() {
		EdgeClass[] ecs = getEdgeClasses();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		Edge e2 = graph.createLinkBack(v1, v2);
		Edge e3 = graph.createSubLink(v2, v1);
		Edge e4 = graph.createLink(v1, v1);
		Edge e5 = graph.createLinkBack(v1, v2);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[0]));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[1]));
		assertEquals(e2, e1.getNextEdgeOfClass(ecs[2]));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[0]));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[1]));
		assertEquals(e5, e2.getNextEdgeOfClass(ecs[2]));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(ecs[0]));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[1]));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(ecs[2]));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(ecs[0]));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[1]));
		assertEquals(e5, e4.getNextEdgeOfClass(ecs[2]));
		// test of edge e4.getReversedEdge
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[0]));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[1]));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(ecs[2]));
		// test of edge e5
		assertEquals(null, e5.getNextEdgeOfClass(ecs[0]));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[1]));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[2]));
	}

	/**
	 * Test in a randomly built graph
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClass2() {
		EdgeClass[] ecs = getEdgeClasses();
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			DoubleSubNode v0 = graph.createDoubleSubNode();
			DoubleSubNode v1 = graph.createDoubleSubNode();
			Edge[] edges = new Edge[30];
			Edge[] link = new Edge[30];
			Edge[] sublink = new Edge[30];
			Edge[] linkback = new Edge[30];
			int lastlink = 0;
			int lastsublink = 0;
			int lastlinkback = 0;
			int edgetype = 0;
			boolean first = true;
			for (int j = 0; j < 30; j++) {
				edgetype = rand.nextInt(2);
				if (edgetype == 0) {
					Edge e = graph.createLink(v0, v1);
					edges[j] = e;
					if (!first) {
						while (lastlink < j) {
							link[lastlink] = e;
							lastlink++;
						}
					}
				}
				if (edgetype == 1) {
					Edge e = graph.createSubLink(v0, v1);
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
					Edge e = graph.createLinkBack(v0, v1);
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
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(link[k], e.getNextEdgeOfClass(ecs[0]));
				assertEquals(sublink[k], e.getNextEdgeOfClass(ecs[1]));
				assertEquals(linkback[k], e.getNextEdgeOfClass(ecs[2]));
			}
		}
	}

	// tests for the method Edge getNextEdgeOfClass(Class<? extends Edge>
	// anEdgeClass);

	/**
	 * An edge which has no following edges.
	 */
	@Test
	public void getNextEdgeOfClassTestClass0() {
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		assertEquals(null, e1.getNextEdgeOfClass(Link.class));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class));
	}

	/**
	 * Test in a manually built graph.
	 */
	@Test
	public void getNextEdgeOfClassTestClass1() {
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		Edge e2 = graph.createLinkBack(v1, v2);
		Edge e3 = graph.createSubLink(v2, v1);
		Edge e4 = graph.createLink(v1, v1);
		Edge e5 = graph.createLinkBack(v1, v2);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(Link.class));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(SubLink.class));
		assertEquals(e2, e1.getNextEdgeOfClass(LinkBack.class));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(Link.class));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(SubLink.class));
		assertEquals(e5, e2.getNextEdgeOfClass(LinkBack.class));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(Link.class));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class));
		assertEquals(e5, e3.getReversedEdge()
				.getNextEdgeOfClass(LinkBack.class));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(Link.class));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class));
		assertEquals(e5, e4.getNextEdgeOfClass(LinkBack.class));
		// test of edge e4.getReversedEdge
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class));
		assertEquals(e5, e4.getReversedEdge()
				.getNextEdgeOfClass(LinkBack.class));
		// test of edge e5
		assertEquals(null, e5.getNextEdgeOfClass(Link.class));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class));
	}

	/**
	 * Test in a randomly built graph
	 */
	@Test
	public void getNextEdgeOfClassTestClass2() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			DoubleSubNode v0 = graph.createDoubleSubNode();
			DoubleSubNode v1 = graph.createDoubleSubNode();
			Edge[] edges = new Edge[30];
			Edge[] link = new Edge[30];
			Edge[] sublink = new Edge[30];
			Edge[] linkback = new Edge[30];
			int lastlink = 0;
			int lastsublink = 0;
			int lastlinkback = 0;
			int edgetype = 0;
			boolean first = true;
			for (int j = 0; j < 30; j++) {
				edgetype = rand.nextInt(2);
				if (edgetype == 0) {
					Edge e = graph.createLink(v0, v1);
					edges[j] = e;
					if (!first) {
						while (lastlink < j) {
							link[lastlink] = e;
							lastlink++;
						}
					}
				}
				if (edgetype == 1) {
					Edge e = graph.createSubLink(v0, v1);
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
					Edge e = graph.createLinkBack(v0, v1);
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
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(link[k], e.getNextEdgeOfClass(Link.class));
				assertEquals(sublink[k], e.getNextEdgeOfClass(SubLink.class));
				assertEquals(linkback[k], e.getNextEdgeOfClass(LinkBack.class));
			}
		}
	}

	// tests for the method Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
	// EdgeDirection orientation);

	/**
	 * An edge which has no following edges.
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirection0() {
		EdgeClass[] ecs = getEdgeClasses();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		assertEquals(null, e1.getNextEdgeOfClass(ecs[0], EdgeDirection.INOUT));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], EdgeDirection.INOUT));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[0], EdgeDirection.IN));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], EdgeDirection.IN));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.IN));
	}

	/**
	 * Test in a manually built graph.
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirection1() {
		EdgeClass[] ecs = getEdgeClasses();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		Edge e2 = graph.createLinkBack(v1, v2);
		Edge e3 = graph.createSubLink(v2, v1);
		Edge e4 = graph.createLink(v1, v1);
		Edge e5 = graph.createLinkBack(v1, v2);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT));
		assertEquals(e2, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT));
		assertEquals(e4, e1.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT));
		assertEquals(e2, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.IN));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT));
		assertEquals(e5, e2.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT));
		assertEquals(e4, e2.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT));
		assertEquals(null, e2.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT));
		assertEquals(e5, e2.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN));
		assertEquals(null, e2.getNextEdgeOfClass(ecs[2], EdgeDirection.IN));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.INOUT));
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.OUT));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.OUT));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextEdgeOfClass(ecs[0], EdgeDirection.IN));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.IN));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[1], EdgeDirection.INOUT));
		assertEquals(e5, e4.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT));
		assertEquals(e5, e4.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[1], EdgeDirection.IN));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[2], EdgeDirection.IN));
		// test of edge e4.getReversedEdge
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.INOUT));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.OUT));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.OUT));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.OUT));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.IN));
		// test of edge e5
		assertEquals(null, e5.getNextEdgeOfClass(ecs[0], EdgeDirection.INOUT));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[1], EdgeDirection.INOUT));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[0], EdgeDirection.IN));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[1], EdgeDirection.IN));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[2], EdgeDirection.IN));
	}

	/**
	 * Test in a randomly built graph
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirection2() {
		EdgeClass[] ecs = getEdgeClasses();
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			DoubleSubNode v0 = graph.createDoubleSubNode();
			DoubleSubNode v1 = graph.createDoubleSubNode();
			Edge[] edges = new Edge[30];
			Edge[] linkinout = new Edge[30];
			Edge[] sublinkinout = new Edge[30];
			Edge[] linkbackinout = new Edge[30];
			int lastlinkinout = 0;
			int lastsublinkinout = 0;
			int lastlinkbackinout = 0;
			Edge[] linkout = new Edge[30];
			Edge[] sublinkout = new Edge[30];
			Edge[] linkbackout = new Edge[30];
			int lastlinkout = 0;
			int lastsublinkout = 0;
			int lastlinkbackout = 0;
			Edge[] linkin = new Edge[30];
			Edge[] sublinkin = new Edge[30];
			Edge[] linkbackin = new Edge[30];
			int lastlinkin = 0;
			int lastsublinkin = 0;
			int lastlinkbackin = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < 30; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? graph.createLink(v0, v1) : graph
							.createLink(v1, v0);
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
					Edge e = direction ? graph.createSubLink(v0, v1) : graph
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
					Edge e = direction ? graph.createLinkBack(v0, v1) : graph
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
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkinout[k], e.getNextEdgeOfClass(ecs[0],
						EdgeDirection.INOUT));
				assertEquals(sublinkinout[k], e.getNextEdgeOfClass(ecs[1],
						EdgeDirection.INOUT));
				assertEquals(linkbackinout[k], e.getNextEdgeOfClass(ecs[2],
						EdgeDirection.INOUT));
				assertEquals(linkout[k], e.getNextEdgeOfClass(ecs[0],
						EdgeDirection.OUT));
				assertEquals(sublinkout[k], e.getNextEdgeOfClass(ecs[1],
						EdgeDirection.OUT));
				assertEquals(linkbackout[k], e.getNextEdgeOfClass(ecs[2],
						EdgeDirection.OUT));
				assertEquals(linkin[k], e.getNextEdgeOfClass(ecs[0],
						EdgeDirection.IN));
				assertEquals(sublinkin[k], e.getNextEdgeOfClass(ecs[1],
						EdgeDirection.IN));
				assertEquals(linkbackin[k], e.getNextEdgeOfClass(ecs[2],
						EdgeDirection.IN));
			}
		}
	}

	// tests for the method Edge getNextEdgeOfClass(Class<? extends Edge>
	// anEdgeClass, EdgeDirection orientation);

	/**
	 * An edge which has no following edges.
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirection0() {
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		assertEquals(null, e1.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));
		assertEquals(null, e1.getNextEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));
		assertEquals(null, e1.getNextEdgeOfClass(Link.class, EdgeDirection.IN));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
	}

	/**
	 * Test in a manually built graph.
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirection1() {
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		Edge e2 = graph.createLinkBack(v1, v2);
		Edge e3 = graph.createSubLink(v2, v1);
		Edge e4 = graph.createLink(v1, v1);
		Edge e5 = graph.createLinkBack(v1, v2);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(e2, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));
		assertEquals(e4, e1.getNextEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(e2, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(Link.class,
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(e5, e2.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));
		assertEquals(e4, e2.getNextEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, e2.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(e5, e2.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(Link.class,
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(null, e2.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.INOUT));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.INOUT));
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.OUT));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.OUT));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextEdgeOfClass(Link.class, EdgeDirection.IN));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.IN));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.IN));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(e5, e4.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));
		assertEquals(null, e4.getNextEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(e5, e4.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(Link.class,
				EdgeDirection.IN));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(null, e4.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
		// test of edge e4.getReversedEdge
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.INOUT));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.INOUT));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.OUT));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.OUT));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.OUT));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.IN));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.IN));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.IN));
		// test of edge e5
		assertEquals(null, e5.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));
		assertEquals(null, e5.getNextEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));
		assertEquals(null, e5.getNextEdgeOfClass(Link.class, EdgeDirection.IN));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
	}

	/**
	 * Test in a randomly built graph
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirection2() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			DoubleSubNode v0 = graph.createDoubleSubNode();
			DoubleSubNode v1 = graph.createDoubleSubNode();
			Edge[] edges = new Edge[30];
			Edge[] linkinout = new Edge[30];
			Edge[] sublinkinout = new Edge[30];
			Edge[] linkbackinout = new Edge[30];
			int lastlinkinout = 0;
			int lastsublinkinout = 0;
			int lastlinkbackinout = 0;
			Edge[] linkout = new Edge[30];
			Edge[] sublinkout = new Edge[30];
			Edge[] linkbackout = new Edge[30];
			int lastlinkout = 0;
			int lastsublinkout = 0;
			int lastlinkbackout = 0;
			Edge[] linkin = new Edge[30];
			Edge[] sublinkin = new Edge[30];
			Edge[] linkbackin = new Edge[30];
			int lastlinkin = 0;
			int lastsublinkin = 0;
			int lastlinkbackin = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < 30; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? graph.createLink(v0, v1) : graph
							.createLink(v1, v0);
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
					Edge e = direction ? graph.createSubLink(v0, v1) : graph
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
					Edge e = direction ? graph.createLinkBack(v0, v1) : graph
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
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkinout[k], e.getNextEdgeOfClass(Link.class,
						EdgeDirection.INOUT));
				assertEquals(sublinkinout[k], e.getNextEdgeOfClass(
						SubLink.class, EdgeDirection.INOUT));
				assertEquals(linkbackinout[k], e.getNextEdgeOfClass(
						LinkBack.class, EdgeDirection.INOUT));
				assertEquals(linkout[k], e.getNextEdgeOfClass(Link.class,
						EdgeDirection.OUT));
				assertEquals(sublinkout[k], e.getNextEdgeOfClass(SubLink.class,
						EdgeDirection.OUT));
				assertEquals(linkbackout[k], e.getNextEdgeOfClass(
						LinkBack.class, EdgeDirection.OUT));
				assertEquals(linkin[k], e.getNextEdgeOfClass(Link.class,
						EdgeDirection.IN));
				assertEquals(sublinkin[k], e.getNextEdgeOfClass(SubLink.class,
						EdgeDirection.IN));
				assertEquals(linkbackin[k], e.getNextEdgeOfClass(
						LinkBack.class, EdgeDirection.IN));
			}
		}
	}

	// tests for the method Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
	// boolean noSubclasses);

	/**
	 * An edge which has no following edges.
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassBoolean0() {
		EdgeClass[] ecs = getEdgeClasses();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		assertEquals(null, e1.getNextEdgeOfClass(ecs[0], false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[0], true));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], true));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], true));
	}

	/**
	 * Test in a manually built graph.
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassBoolean1() {
		EdgeClass[] ecs = getEdgeClasses();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		Edge e2 = graph.createLinkBack(v1, v2);
		Edge e3 = graph.createSubLink(v2, v1);
		Edge e4 = graph.createLink(v1, v1);
		Edge e5 = graph.createLinkBack(v1, v2);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[0], false));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[1], false));
		assertEquals(e2, e1.getNextEdgeOfClass(ecs[2], false));
		assertEquals(e4, e1.getNextEdgeOfClass(ecs[0], true));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[1], true));
		assertEquals(e2, e1.getNextEdgeOfClass(ecs[2], true));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[0], false));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[1], false));
		assertEquals(e5, e2.getNextEdgeOfClass(ecs[2], false));
		assertEquals(e4, e2.getNextEdgeOfClass(ecs[0], true));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[1], true));
		assertEquals(e5, e2.getNextEdgeOfClass(ecs[2], true));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(ecs[0], false));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[1],
				false));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(ecs[2], false));
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(ecs[0], true));
		assertEquals(null, e3.getReversedEdge()
				.getNextEdgeOfClass(ecs[1], true));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(ecs[2], true));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(ecs[0], false));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[1], false));
		assertEquals(e5, e4.getNextEdgeOfClass(ecs[2], false));
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(ecs[0], true));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[1], true));
		assertEquals(e5, e4.getNextEdgeOfClass(ecs[2], true));
		// test of edge e4.getReversedEdge
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[0],
				false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[1],
				false));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(ecs[2], false));
		assertEquals(null, e4.getReversedEdge()
				.getNextEdgeOfClass(ecs[0], true));
		assertEquals(null, e4.getReversedEdge()
				.getNextEdgeOfClass(ecs[1], true));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(ecs[2], true));
		// test of edge e5
		assertEquals(null, e5.getNextEdgeOfClass(ecs[0], false));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[1], false));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[2], false));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[0], true));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[1], true));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[2], true));
	}

	/**
	 * Test in a randomly built graph
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassBoolean2() {
		EdgeClass[] ecs = getEdgeClasses();
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			DoubleSubNode v0 = graph.createDoubleSubNode();
			DoubleSubNode v1 = graph.createDoubleSubNode();
			Edge[] edges = new Edge[30];
			Edge[] linkfalse = new Edge[30];
			Edge[] sublinkfalse = new Edge[30];
			Edge[] linkbackfalse = new Edge[30];
			int lastlinkfalse = 0;
			int lastsublinkfalse = 0;
			int lastlinkbackfalse = 0;
			Edge[] linktrue = new Edge[30];
			Edge[] sublinktrue = new Edge[30];
			Edge[] linkbacktrue = new Edge[30];
			int lastlinktrue = 0;
			int lastsublinktrue = 0;
			int lastlinkbacktrue = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < 30; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? graph.createLink(v0, v1) : graph
							.createLink(v1, v0).getReversedEdge();
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
					Edge e = direction ? graph.createSubLink(v0, v1) : graph
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
					Edge e = direction ? graph.createLinkBack(v0, v1) : graph
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
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkfalse[k], e.getNextEdgeOfClass(ecs[0], false));
				assertEquals(sublinkfalse[k], e.getNextEdgeOfClass(ecs[1],
						false));
				assertEquals(linkbackfalse[k], e.getNextEdgeOfClass(ecs[2],
						false));
				assertEquals(linktrue[k], e.getNextEdgeOfClass(ecs[0], true));
				assertEquals(sublinktrue[k], e.getNextEdgeOfClass(ecs[1], true));
				assertEquals(linkbacktrue[k], e
						.getNextEdgeOfClass(ecs[2], true));
			}
		}
	}

	// tests for the method Edge getNextEdgeOfClass(Class<? extends Edge>
	// anEdgeClass, boolean noSubclasses);

	/**
	 * An edge which has no following edges.
	 */
	@Test
	public void getNextEdgeOfClassTestClassBoolean0() {
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		assertEquals(null, e1.getNextEdgeOfClass(Link.class, false));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class, false));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class, false));
		assertEquals(null, e1.getNextEdgeOfClass(Link.class, true));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class, true));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class, true));
	}

	/**
	 * Test in a manually built graph.
	 */
	@Test
	public void getNextEdgeOfClassTestClassBoolean1() {
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		Edge e2 = graph.createLinkBack(v1, v2);
		Edge e3 = graph.createSubLink(v2, v1);
		Edge e4 = graph.createLink(v1, v1);
		Edge e5 = graph.createLinkBack(v1, v2);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(Link.class,
				false));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(SubLink.class,
				false));
		assertEquals(e2, e1.getNextEdgeOfClass(LinkBack.class, false));
		assertEquals(e4, e1.getNextEdgeOfClass(Link.class, true));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(SubLink.class,
				true));
		assertEquals(e2, e1.getNextEdgeOfClass(LinkBack.class, true));
		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(Link.class,
				false));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(SubLink.class,
				false));
		assertEquals(e5, e2.getNextEdgeOfClass(LinkBack.class, false));
		assertEquals(e4, e2.getNextEdgeOfClass(Link.class, true));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(SubLink.class,
				true));
		assertEquals(e5, e2.getNextEdgeOfClass(LinkBack.class, true));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(Link.class,
				EdgeDirection.IN));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(Link.class,
				false));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, false));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, false));
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(Link.class,
				true));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, true));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, true));
		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(Link.class,
				false));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class, false));
		assertEquals(e5, e4.getNextEdgeOfClass(LinkBack.class, false));
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(Link.class,
				true));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class, true));
		assertEquals(e5, e4.getNextEdgeOfClass(LinkBack.class, true));
		// test of edge e4.getReversedEdge
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class,
				false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, false));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class,
				true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, true));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, true));
		// test of edge e5
		assertEquals(null, e5.getNextEdgeOfClass(Link.class, false));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class, false));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class, false));
		assertEquals(null, e5.getNextEdgeOfClass(Link.class, true));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class, true));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class, true));
	}

	/**
	 * Test in a randomly built graph
	 */
	@Test
	public void getNextEdgeOfClassTestClassBoolean2() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			DoubleSubNode v0 = graph.createDoubleSubNode();
			DoubleSubNode v1 = graph.createDoubleSubNode();
			Edge[] edges = new Edge[30];
			Edge[] linkfalse = new Edge[30];
			Edge[] sublinkfalse = new Edge[30];
			Edge[] linkbackfalse = new Edge[30];
			int lastlinkfalse = 0;
			int lastsublinkfalse = 0;
			int lastlinkbackfalse = 0;
			Edge[] linktrue = new Edge[30];
			Edge[] sublinktrue = new Edge[30];
			Edge[] linkbacktrue = new Edge[30];
			int lastlinktrue = 0;
			int lastsublinktrue = 0;
			int lastlinkbacktrue = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < 30; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? graph.createLink(v0, v1) : graph
							.createLink(v1, v0).getReversedEdge();
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
					Edge e = direction ? graph.createSubLink(v0, v1) : graph
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
					Edge e = direction ? graph.createLinkBack(v0, v1) : graph
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
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkfalse[k], e.getNextEdgeOfClass(Link.class,
						false));
				assertEquals(sublinkfalse[k], e.getNextEdgeOfClass(
						SubLink.class, false));
				assertEquals(linkbackfalse[k], e.getNextEdgeOfClass(
						LinkBack.class, false));
				assertEquals(linktrue[k], e
						.getNextEdgeOfClass(Link.class, true));
				assertEquals(sublinktrue[k], e.getNextEdgeOfClass(
						SubLink.class, true));
				assertEquals(linkbacktrue[k], e.getNextEdgeOfClass(
						LinkBack.class, true));
			}
		}
	}

	// tests for the method Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
	// EdgeDirection orientation, boolean noSubclasses);

	/**
	 * An edge which has no following edges.
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirectionBoolean0() {
		EdgeClass[] ecs = getEdgeClasses();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		assertEquals(null, e1.getNextEdgeOfClass(ecs[0], EdgeDirection.INOUT,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], EdgeDirection.INOUT,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[0], EdgeDirection.IN,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], EdgeDirection.IN,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.IN,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[0], EdgeDirection.INOUT,
				true));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], EdgeDirection.INOUT,
				true));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT,
				true));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT,
				true));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT,
				true));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT,
				true));
		assertEquals(null, e1
				.getNextEdgeOfClass(ecs[0], EdgeDirection.IN, true));
		assertEquals(null, e1
				.getNextEdgeOfClass(ecs[1], EdgeDirection.IN, true));
		assertEquals(null, e1
				.getNextEdgeOfClass(ecs[2], EdgeDirection.IN, true));
	}

	/**
	 * Test in a manually built graph.
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirectionBoolean1() {
		EdgeClass[] ecs = getEdgeClasses();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		Edge e2 = graph.createLinkBack(v1, v2);
		Edge e3 = graph.createSubLink(v2, v1);
		Edge e4 = graph.createLink(v1, v1);
		Edge e5 = graph.createLinkBack(v1, v2);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT, false));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT, false));
		assertEquals(e2, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT,
				false));
		assertEquals(e4, e1
				.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT, false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT,
				false));
		assertEquals(e2, e1
				.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT, false));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN, false));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN, false));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.IN,
				false));

		assertEquals(e4, e1.getNextEdgeOfClass(ecs[0], EdgeDirection.INOUT,
				true));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT, true));
		assertEquals(e2, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT,
				true));
		assertEquals(e4, e1.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT, true));
		assertEquals(null, e1.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT,
				true));
		assertEquals(e2, e1.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e1.getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN, true));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN, true));
		assertEquals(null, e1
				.getNextEdgeOfClass(ecs[2], EdgeDirection.IN, true));

		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT, false));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT, false));
		assertEquals(e5, e2.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT,
				false));
		assertEquals(e4, e2
				.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT, false));
		assertEquals(null, e2.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT,
				false));
		assertEquals(e5, e2
				.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT, false));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN, false));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN, false));
		assertEquals(null, e2.getNextEdgeOfClass(ecs[2], EdgeDirection.IN,
				false));

		assertEquals(e4, e2.getNextEdgeOfClass(ecs[0], EdgeDirection.INOUT,
				true));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT, true));
		assertEquals(e5, e2.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT,
				true));
		assertEquals(e4, e2.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT, true));
		assertEquals(null, e2.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT,
				true));
		assertEquals(e5, e2.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e2.getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN, true));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN, true));
		assertEquals(null, e2
				.getNextEdgeOfClass(ecs[2], EdgeDirection.IN, true));

		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT, false));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT, false));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.INOUT, false));
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.OUT, false));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.OUT, false));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.OUT, false));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextEdgeOfClass(ecs[0], EdgeDirection.IN, false));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN, false));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.IN, false));

		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT, true));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT, true));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.INOUT, true));
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.OUT, true));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.OUT, true));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextEdgeOfClass(ecs[0], EdgeDirection.IN, true));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN, true));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.IN, true));

		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT, false));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[1], EdgeDirection.INOUT,
				false));
		assertEquals(e5, e4.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT,
				false));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT,
				false));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT,
				false));
		assertEquals(e5, e4
				.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT, false));
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN, false));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[1], EdgeDirection.IN,
				false));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[2], EdgeDirection.IN,
				false));

		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT, true));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[1], EdgeDirection.INOUT,
				true));
		assertEquals(e5, e4.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT,
				true));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT,
				true));
		assertEquals(null, e4.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT,
				true));
		assertEquals(e5, e4.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN, true));
		assertEquals(null, e4
				.getNextEdgeOfClass(ecs[1], EdgeDirection.IN, true));
		assertEquals(null, e4
				.getNextEdgeOfClass(ecs[2], EdgeDirection.IN, true));

		// test of edge e4.getReversedEdge
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT, false));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.INOUT, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.OUT, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.OUT, false));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.OUT, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.IN, false));

		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.INOUT, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.INOUT, true));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.INOUT, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.OUT, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.OUT, true));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.OUT, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[0],
				EdgeDirection.IN, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[1],
				EdgeDirection.IN, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(ecs[2],
				EdgeDirection.IN, true));

		// test of edge e5
		assertEquals(null, e5.getNextEdgeOfClass(ecs[0], EdgeDirection.INOUT,
				false));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[1], EdgeDirection.INOUT,
				false));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT,
				false));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT,
				false));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT,
				false));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT,
				false));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[0], EdgeDirection.IN,
				false));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[1], EdgeDirection.IN,
				false));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[2], EdgeDirection.IN,
				false));

		assertEquals(null, e5.getNextEdgeOfClass(ecs[0], EdgeDirection.INOUT,
				true));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[1], EdgeDirection.INOUT,
				true));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[2], EdgeDirection.INOUT,
				true));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[0], EdgeDirection.OUT,
				true));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[1], EdgeDirection.OUT,
				true));
		assertEquals(null, e5.getNextEdgeOfClass(ecs[2], EdgeDirection.OUT,
				true));
		assertEquals(null, e5
				.getNextEdgeOfClass(ecs[0], EdgeDirection.IN, true));
		assertEquals(null, e5
				.getNextEdgeOfClass(ecs[1], EdgeDirection.IN, true));
		assertEquals(null, e5
				.getNextEdgeOfClass(ecs[2], EdgeDirection.IN, true));
	}

	/**
	 * Test in a randomly built graph
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClassEdgeDirectionBoolean2() {
		EdgeClass[] ecs = getEdgeClasses();
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			DoubleSubNode v0 = graph.createDoubleSubNode();
			DoubleSubNode v1 = graph.createDoubleSubNode();
			Edge[] edges = new Edge[30];
			Edge[] linkinoutfalse = new Edge[30];
			Edge[] sublinkinout = new Edge[30];
			Edge[] linkbackinout = new Edge[30];
			int lastlinkinoutfalse = 0;
			int lastsublinkinout = 0;
			int lastlinkbackinout = 0;
			Edge[] linkoutfalse = new Edge[30];
			Edge[] sublinkout = new Edge[30];
			Edge[] linkbackout = new Edge[30];
			int lastlinkoutfalse = 0;
			int lastsublinkout = 0;
			int lastlinkbackout = 0;
			Edge[] linkinfalse = new Edge[30];
			Edge[] sublinkin = new Edge[30];
			Edge[] linkbackin = new Edge[30];
			int lastlinkinfalse = 0;
			int lastsublinkin = 0;
			int lastlinkbackin = 0;
			Edge[] linkinouttrue = new Edge[30];
			int lastlinkinouttrue = 0;
			Edge[] linkouttrue = new Edge[30];
			int lastlinkouttrue = 0;
			Edge[] linkintrue = new Edge[30];
			int lastlinkintrue = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < 30; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? graph.createLink(v0, v1) : graph
							.createLink(v1, v0);
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
					Edge e = direction ? graph.createSubLink(v0, v1) : graph
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
					Edge e = direction ? graph.createLinkBack(v0, v1) : graph
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
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkinoutfalse[k], e.getNextEdgeOfClass(ecs[0],
						EdgeDirection.INOUT, false));
				assertEquals(sublinkinout[k], e.getNextEdgeOfClass(ecs[1],
						EdgeDirection.INOUT, false));
				assertEquals(linkbackinout[k], e.getNextEdgeOfClass(ecs[2],
						EdgeDirection.INOUT, false));
				assertEquals(linkoutfalse[k], e.getNextEdgeOfClass(ecs[0],
						EdgeDirection.OUT, false));
				assertEquals(sublinkout[k], e.getNextEdgeOfClass(ecs[1],
						EdgeDirection.OUT, false));
				assertEquals(linkbackout[k], e.getNextEdgeOfClass(ecs[2],
						EdgeDirection.OUT, false));
				assertEquals(linkinfalse[k], e.getNextEdgeOfClass(ecs[0],
						EdgeDirection.IN, false));
				assertEquals(sublinkin[k], e.getNextEdgeOfClass(ecs[1],
						EdgeDirection.IN, false));
				assertEquals(linkbackin[k], e.getNextEdgeOfClass(ecs[2],
						EdgeDirection.IN, false));

				assertEquals(linkinouttrue[k], e.getNextEdgeOfClass(ecs[0],
						EdgeDirection.INOUT, true));
				assertEquals(sublinkinout[k], e.getNextEdgeOfClass(ecs[1],
						EdgeDirection.INOUT, true));
				assertEquals(linkbackinout[k], e.getNextEdgeOfClass(ecs[2],
						EdgeDirection.INOUT, true));
				assertEquals(linkouttrue[k], e.getNextEdgeOfClass(ecs[0],
						EdgeDirection.OUT, true));
				assertEquals(sublinkout[k], e.getNextEdgeOfClass(ecs[1],
						EdgeDirection.OUT, true));
				assertEquals(linkbackout[k], e.getNextEdgeOfClass(ecs[2],
						EdgeDirection.OUT, true));
				assertEquals(linkintrue[k], e.getNextEdgeOfClass(ecs[0],
						EdgeDirection.IN, true));
				assertEquals(sublinkin[k], e.getNextEdgeOfClass(ecs[1],
						EdgeDirection.IN, true));
				assertEquals(linkbackin[k], e.getNextEdgeOfClass(ecs[2],
						EdgeDirection.IN, true));
			}
		}
	}

	// tests for the method Edge getNextEdgeOfClass(Class<? extends Edge>
	// anEdgeClass, EdgeDirection orientation, boolean noSubclasses);

	/**
	 * An edge which has no following edges.
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirectionBoolean0() {
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		assertEquals(null, e1.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, e1.getNextEdgeOfClass(Link.class, EdgeDirection.OUT,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));
		assertEquals(null, e1.getNextEdgeOfClass(Link.class, EdgeDirection.IN,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));
		assertEquals(null, e1.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, e1.getNextEdgeOfClass(Link.class, EdgeDirection.OUT,
				true));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));
		assertEquals(null, e1.getNextEdgeOfClass(Link.class, EdgeDirection.IN,
				true));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));
	}

	/**
	 * Test in a manually built graph.
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirectionBoolean1() {
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		Edge e2 = graph.createLinkBack(v1, v2);
		Edge e3 = graph.createSubLink(v2, v1);
		Edge e4 = graph.createLink(v1, v1);
		Edge e5 = graph.createLinkBack(v1, v2);
		// test of edge e1
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(e2, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertEquals(e4, e1.getNextEdgeOfClass(Link.class, EdgeDirection.OUT,
				false));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(e2, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));

		assertEquals(e4, e1.getNextEdgeOfClass(Link.class, EdgeDirection.INOUT,
				true));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(e2, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertEquals(e4, e1.getNextEdgeOfClass(Link.class, EdgeDirection.OUT,
				true));
		assertEquals(null, e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(e2, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e1.getNextEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(e3.getReversedEdge(), e1.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, e1.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));

		// test of edge e2
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(e5, e2.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertEquals(e4, e2.getNextEdgeOfClass(Link.class, EdgeDirection.OUT,
				false));
		assertEquals(null, e2.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(e5, e2.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, e2.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));

		assertEquals(e4, e2.getNextEdgeOfClass(Link.class, EdgeDirection.INOUT,
				true));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(e5, e2.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertEquals(e4, e2.getNextEdgeOfClass(Link.class, EdgeDirection.OUT,
				true));
		assertEquals(null, e2.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(e5, e2.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e2.getNextEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(e3.getReversedEdge(), e2.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, e2.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));

		// test of edge e3
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.INOUT, false));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.INOUT, false));
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.OUT, false));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.OUT, false));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.OUT, false));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextEdgeOfClass(Link.class, EdgeDirection.IN, false));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.IN, false));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.IN, false));

		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.INOUT, true));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.INOUT, true));
		assertEquals(e4, e3.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.OUT, true));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.OUT, true));
		assertEquals(e5, e3.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e3.getReversedEdge()
				.getNextEdgeOfClass(Link.class, EdgeDirection.IN, true));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.IN, true));
		assertEquals(null, e3.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.IN, true));

		// test of edge e4
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(e5, e4.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, e4.getNextEdgeOfClass(Link.class, EdgeDirection.OUT,
				false));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(e5, e4.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, e4.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));

		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(e5, e4.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, e4.getNextEdgeOfClass(Link.class, EdgeDirection.OUT,
				true));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(e5, e4.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));
		assertEquals(e4.getReversedEdge(), e4.getNextEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(null, e4.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, e4.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));

		// test of edge e4.getReversedEdge
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.INOUT, false));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.INOUT, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.OUT, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.OUT, false));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.OUT, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.IN, false));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.IN, false));

		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.INOUT, true));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.INOUT, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.OUT, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.OUT, true));
		assertEquals(e5, e4.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.OUT, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				SubLink.class, EdgeDirection.IN, true));
		assertEquals(null, e4.getReversedEdge().getNextEdgeOfClass(
				LinkBack.class, EdgeDirection.IN, true));

		// test of edge e5
		assertEquals(null, e5.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, e5.getNextEdgeOfClass(Link.class, EdgeDirection.OUT,
				false));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));
		assertEquals(null, e5.getNextEdgeOfClass(Link.class, EdgeDirection.IN,
				false));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));

		assertEquals(null, e5.getNextEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, e5.getNextEdgeOfClass(Link.class, EdgeDirection.OUT,
				true));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));
		assertEquals(null, e5.getNextEdgeOfClass(Link.class, EdgeDirection.IN,
				true));
		assertEquals(null, e5.getNextEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, e5.getNextEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));
	}

	/**
	 * Test in a randomly built graph
	 */
	@Test
	public void getNextEdgeOfClassTestClassEdgeDirectionBoolean2() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			DoubleSubNode v0 = graph.createDoubleSubNode();
			DoubleSubNode v1 = graph.createDoubleSubNode();
			Edge[] edges = new Edge[30];
			Edge[] linkinoutfalse = new Edge[30];
			Edge[] sublinkinout = new Edge[30];
			Edge[] linkbackinout = new Edge[30];
			int lastlinkinoutfalse = 0;
			int lastsublinkinout = 0;
			int lastlinkbackinout = 0;
			Edge[] linkoutfalse = new Edge[30];
			Edge[] sublinkout = new Edge[30];
			Edge[] linkbackout = new Edge[30];
			int lastlinkoutfalse = 0;
			int lastsublinkout = 0;
			int lastlinkbackout = 0;
			Edge[] linkinfalse = new Edge[30];
			Edge[] sublinkin = new Edge[30];
			Edge[] linkbackin = new Edge[30];
			int lastlinkinfalse = 0;
			int lastsublinkin = 0;
			int lastlinkbackin = 0;
			Edge[] linkinouttrue = new Edge[30];
			int lastlinkinouttrue = 0;
			Edge[] linkouttrue = new Edge[30];
			int lastlinkouttrue = 0;
			Edge[] linkintrue = new Edge[30];
			int lastlinkintrue = 0;
			int edgetype = 0;
			boolean direction = false;
			boolean first = true;
			for (int j = 0; j < 30; j++) {
				edgetype = rand.nextInt(2);
				direction = rand.nextBoolean();
				if (edgetype == 0) {
					Edge e = direction ? graph.createLink(v0, v1) : graph
							.createLink(v1, v0);
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
					Edge e = direction ? graph.createSubLink(v0, v1) : graph
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
					Edge e = direction ? graph.createLinkBack(v0, v1) : graph
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
			for (int k = 0; k < edges.length; k++) {
				Edge e = edges[k];
				assertEquals(linkinoutfalse[k], e.getNextEdgeOfClass(
						Link.class, EdgeDirection.INOUT, false));
				assertEquals(sublinkinout[k], e.getNextEdgeOfClass(
						SubLink.class, EdgeDirection.INOUT, false));
				assertEquals(linkbackinout[k], e.getNextEdgeOfClass(
						LinkBack.class, EdgeDirection.INOUT, false));
				assertEquals(linkoutfalse[k], e.getNextEdgeOfClass(Link.class,
						EdgeDirection.OUT, false));
				assertEquals(sublinkout[k], e.getNextEdgeOfClass(SubLink.class,
						EdgeDirection.OUT, false));
				assertEquals(linkbackout[k], e.getNextEdgeOfClass(
						LinkBack.class, EdgeDirection.OUT, false));
				assertEquals(linkinfalse[k], e.getNextEdgeOfClass(Link.class,
						EdgeDirection.IN, false));
				assertEquals(sublinkin[k], e.getNextEdgeOfClass(SubLink.class,
						EdgeDirection.IN, false));
				assertEquals(linkbackin[k], e.getNextEdgeOfClass(
						LinkBack.class, EdgeDirection.IN, false));

				assertEquals(linkinouttrue[k], e.getNextEdgeOfClass(Link.class,
						EdgeDirection.INOUT, true));
				assertEquals(sublinkinout[k], e.getNextEdgeOfClass(
						SubLink.class, EdgeDirection.INOUT, true));
				assertEquals(linkbackinout[k], e.getNextEdgeOfClass(
						LinkBack.class, EdgeDirection.INOUT, true));
				assertEquals(linkouttrue[k], e.getNextEdgeOfClass(Link.class,
						EdgeDirection.OUT, true));
				assertEquals(sublinkout[k], e.getNextEdgeOfClass(SubLink.class,
						EdgeDirection.OUT, true));
				assertEquals(linkbackout[k], e.getNextEdgeOfClass(
						LinkBack.class, EdgeDirection.OUT, true));
				assertEquals(linkintrue[k], e.getNextEdgeOfClass(Link.class,
						EdgeDirection.IN, true));
				assertEquals(sublinkin[k], e.getNextEdgeOfClass(SubLink.class,
						EdgeDirection.IN, true));
				assertEquals(linkbackin[k], e.getNextEdgeOfClass(
						LinkBack.class, EdgeDirection.IN, true));
			}
		}
	}
}
