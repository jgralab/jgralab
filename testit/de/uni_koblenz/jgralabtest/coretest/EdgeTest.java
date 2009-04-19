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
					edges[j]=e;
					if (!first) {
						v0inout[j-1]=e;
						v1inout[j-1]=e.getReversedEdge();
						while (lastv0o < j) {
							v0out[lastv0o]=e;
							lastv0o++;
						}
						while (lastv1i < j) {
							v1in[lastv1i]=e.getReversedEdge();
							lastv1i++;
						}
					}
				} else {
					Edge e = graph.createLink(v1, v0);
					edges[j]=e;
					if (!first) {
						v0inout[j-1]=e.getReversedEdge();
						v1inout[j-1]=e;
						while (lastv1o < j) {
							v1out[lastv1o]=e;
							lastv1o++;
						}
						while (lastv0i < j) {
							v0in[lastv0i]=e.getReversedEdge();
							lastv0i++;
						}
					}
				}
				first = false;
			}
			for(int k=0;k<edges.length;k++){
				Edge e=edges[k];
				if(e.getAlpha()==v0){
					assertEquals(v0inout[k],e.getNextEdge(EdgeDirection.INOUT));
					assertEquals(v0out[k],e.getNextEdge(EdgeDirection.OUT));
					assertEquals(v0in[k],e.getNextEdge(EdgeDirection.IN));
					assertEquals(v1inout[k],e.getReversedEdge().getNextEdge(EdgeDirection.INOUT));
					assertEquals(v1out[k],e.getReversedEdge().getNextEdge(EdgeDirection.OUT));
					assertEquals(v1in[k],e.getReversedEdge().getNextEdge(EdgeDirection.IN));
				}else{
					assertEquals(v0inout[k],e.getReversedEdge().getNextEdge(EdgeDirection.INOUT));
					assertEquals(v0out[k],e.getReversedEdge().getNextEdge(EdgeDirection.OUT));
					assertEquals(v0in[k],e.getReversedEdge().getNextEdge(EdgeDirection.IN));
					assertEquals(v1inout[k],e.getNextEdge(EdgeDirection.INOUT));
					assertEquals(v1out[k],e.getNextEdge(EdgeDirection.OUT));
					assertEquals(v1in[k],e.getNextEdge(EdgeDirection.IN));
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
	public void getNextEdgeOfClassTestEdgeClass0(){
		EdgeClass[] ecs=getEdgeClasses();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		assertEquals(null,e1.getNextEdgeOfClass(ecs[0]));
		assertEquals(null,e1.getNextEdgeOfClass(ecs[1]));
		assertEquals(null,e1.getNextEdgeOfClass(ecs[2]));
	}
	
	/**
	 * Test in a manually built graph.
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClass1(){
		EdgeClass[] ecs=getEdgeClasses();
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		Edge e2 = graph.createLinkBack(v1, v2);
		Edge e3 = graph.createSubLink(v2, v1);
		Edge e4 = graph.createLink(v1, v1);
		Edge e5 = graph.createLinkBack(v1, v2);
		// test of edge e1
		assertEquals(e3.getReversedEdge(),e1.getNextEdgeOfClass(ecs[0]));
		assertEquals(e3.getReversedEdge(),e1.getNextEdgeOfClass(ecs[1]));
		assertEquals(e2,e1.getNextEdgeOfClass(ecs[2]));
		// test of edge e2
		assertEquals(e3.getReversedEdge(),e2.getNextEdgeOfClass(ecs[0]));
		assertEquals(e3.getReversedEdge(),e2.getNextEdgeOfClass(ecs[1]));
		assertEquals(e5,e2.getNextEdgeOfClass(ecs[2]));
		// test of edge e3
		assertEquals(e4,e3.getReversedEdge().getNextEdgeOfClass(ecs[0]));
		assertEquals(null,e3.getReversedEdge().getNextEdgeOfClass(ecs[1]));
		assertEquals(e5,e3.getReversedEdge().getNextEdgeOfClass(ecs[2]));
		// test of edge e4
		assertEquals(e4.getReversedEdge(),e4.getNextEdgeOfClass(ecs[0]));
		assertEquals(null,e4.getNextEdgeOfClass(ecs[1]));
		assertEquals(e5,e4.getNextEdgeOfClass(ecs[2]));
		// test of edge e4.getReversedEdge
		assertEquals(null,e4.getReversedEdge().getNextEdgeOfClass(ecs[0]));
		assertEquals(null,e4.getReversedEdge().getNextEdgeOfClass(ecs[1]));
		assertEquals(e5,e4.getReversedEdge().getNextEdgeOfClass(ecs[2]));
		// test of edge e5
		assertEquals(null,e5.getNextEdgeOfClass(ecs[0]));
		assertEquals(null,e5.getNextEdgeOfClass(ecs[1]));
		assertEquals(null,e5.getNextEdgeOfClass(ecs[2]));
	}
	
	/**
	 * Test in a randomly built graph
	 */
	@Test
	public void getNextEdgeOfClassTestEdgeClass2() {
		EdgeClass[] ecs=getEdgeClasses();
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
					edges[j]=e;
					if (!first) {
						link[j-1]=e;
						while (lastlink < j) {
							link[lastlink]=e;
							lastlink++;
						}
					}
				}if (edgetype == 1) {
					Edge e = graph.createSubLink(v0, v1);
					edges[j]=e;
					if (!first) {
						link[j-1]=e;
						while (lastlink < j) {
							link[lastlink]=e;
							lastlink++;
						}
						sublink[j-1]=e;
						while (lastsublink < j) {
							sublink[lastsublink]=e;
							lastsublink++;
						}
					}
				} else {
					Edge e = graph.createLinkBack(v0, v1);
					edges[j]=e;
					if (!first) {
						linkback[j-1]=e;
						while (lastlinkback < j) {
							linkback[lastlinkback]=e;
							lastlinkback++;
						}
					}
				}
				first = false;
			}
			for(int k=0;k<edges.length;k++){
				Edge e=edges[k];
					assertEquals(link[k],e.getNextEdgeOfClass(ecs[0]));
					assertEquals(sublink[k],e.getNextEdgeOfClass(ecs[1]));
					assertEquals(linkback[k],e.getNextEdgeOfClass(ecs[2]));
			}
		}
	}
	
	// tests for the method Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass);
	
	/**
	 * An edge which has no following edges.
	 */
	@Test
	public void getNextEdgeOfClassTestClass0(){
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		assertEquals(null,e1.getNextEdgeOfClass(Link.class));
		assertEquals(null,e1.getNextEdgeOfClass(SubLink.class));
		assertEquals(null,e1.getNextEdgeOfClass(LinkBack.class));
	}
	
	/**
	 * Test in a manually built graph.
	 */
	@Test
	public void getNextEdgeOfClassTestClass1(){
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink(v1, v2);
		Edge e2 = graph.createLinkBack(v1, v2);
		Edge e3 = graph.createSubLink(v2, v1);
		Edge e4 = graph.createLink(v1, v1);
		Edge e5 = graph.createLinkBack(v1, v2);
		// test of edge e1
		assertEquals(e3.getReversedEdge(),e1.getNextEdgeOfClass(Link.class));
		assertEquals(e3.getReversedEdge(),e1.getNextEdgeOfClass(SubLink.class));
		assertEquals(e2,e1.getNextEdgeOfClass(LinkBack.class));
		// test of edge e2
		assertEquals(e3.getReversedEdge(),e2.getNextEdgeOfClass(Link.class));
		assertEquals(e3.getReversedEdge(),e2.getNextEdgeOfClass(SubLink.class));
		assertEquals(e5,e2.getNextEdgeOfClass(LinkBack.class));
		// test of edge e3
		assertEquals(e4,e3.getReversedEdge().getNextEdgeOfClass(Link.class));
		assertEquals(null,e3.getReversedEdge().getNextEdgeOfClass(SubLink.class));
		assertEquals(e5,e3.getReversedEdge().getNextEdgeOfClass(LinkBack.class));
		// test of edge e4
		assertEquals(e4.getReversedEdge(),e4.getNextEdgeOfClass(Link.class));
		assertEquals(null,e4.getNextEdgeOfClass(SubLink.class));
		assertEquals(e5,e4.getNextEdgeOfClass(LinkBack.class));
		// test of edge e4.getReversedEdge
		assertEquals(null,e4.getReversedEdge().getNextEdgeOfClass(Link.class));
		assertEquals(null,e4.getReversedEdge().getNextEdgeOfClass(SubLink.class));
		assertEquals(e5,e4.getReversedEdge().getNextEdgeOfClass(LinkBack.class));
		// test of edge e5
		assertEquals(null,e5.getNextEdgeOfClass(Link.class));
		assertEquals(null,e5.getNextEdgeOfClass(SubLink.class));
		assertEquals(null,e5.getNextEdgeOfClass(LinkBack.class));
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
					edges[j]=e;
					if (!first) {
						link[j-1]=e;
						while (lastlink < j) {
							link[lastlink]=e;
							lastlink++;
						}
					}
				}if (edgetype == 1) {
					Edge e = graph.createSubLink(v0, v1);
					edges[j]=e;
					if (!first) {
						link[j-1]=e;
						while (lastlink < j) {
							link[lastlink]=e;
							lastlink++;
						}
						sublink[j-1]=e;
						while (lastsublink < j) {
							sublink[lastsublink]=e;
							lastsublink++;
						}
					}
				} else {
					Edge e = graph.createLinkBack(v0, v1);
					edges[j]=e;
					if (!first) {
						linkback[j-1]=e;
						while (lastlinkback < j) {
							linkback[lastlinkback]=e;
							lastlinkback++;
						}
					}
				}
				first = false;
			}
			for(int k=0;k<edges.length;k++){
				Edge e=edges[k];
					assertEquals(link[k],e.getNextEdgeOfClass(Link.class));
					assertEquals(sublink[k],e.getNextEdgeOfClass(SubLink.class));
					assertEquals(linkback[k],e.getNextEdgeOfClass(LinkBack.class));
			}
		}
	}
}
