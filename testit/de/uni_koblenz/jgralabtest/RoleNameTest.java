package de.uni_koblenz.jgralabtest;

import java.util.LinkedList;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralabtest.schemas.vertextest.A;
import de.uni_koblenz.jgralabtest.schemas.vertextest.B;
import de.uni_koblenz.jgralabtest.schemas.vertextest.C;
import de.uni_koblenz.jgralabtest.schemas.vertextest.D;
import de.uni_koblenz.jgralabtest.schemas.vertextest.E;
import de.uni_koblenz.jgralabtest.schemas.vertextest.F;
import de.uni_koblenz.jgralabtest.schemas.vertextest.G;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

public class RoleNameTest {

	private VertexTestGraph graph;
	private Random rand;

	/**
	 * Creates a new graph for each test.
	 */
	@Before
	public void setUp() {
		graph = VertexTestSchema.instance().createVertexTestGraph(100, 100);
		rand = new Random(System.currentTimeMillis());
	}

	/**
	 * Tests if the incident edges of <code>v</code> equals the edges of
	 * <code>incidentEdges</code>.
	 * 
	 * @param v
	 * @param incidentEdges
	 */
	private void testIncidenceList(Vertex v, Edge... incidentEdges) {
		assertEquals(incidentEdges.length, v.getDegree());
		int i = 0;
		for (Edge e : v.incidences()) {
			assertEquals(incidentEdges[i], e);
			i++;
		}
	}

	/**
	 * Tests if the incident edges of type <code>ec</code> of <code>v</code>
	 * equals the edges of <code>incidentEdges</code>.
	 * 
	 * @param ec
	 *            (subclasses are ignored)
	 * @param v
	 * @param incidentEdges
	 */
	private void testIncidenceListOfOneEdge(Class<? extends Edge> ec, Vertex v,
			Edge... incidentEdges) {
		assertEquals(incidentEdges.length, v.getDegree(ec, true));
		int i = 0;
		for (Edge e : v.incidences(ec)) {
			if (!(e instanceof F) && !(e instanceof G)) {
				assertEquals(incidentEdges[i], e);
				i++;
			}
		}
	}

	/*
	 * 1. Test of target rolename.
	 */
	
	/*
	 * 1.1 Test of addRoleName
	 */

	/**
	 * Test if only edges of one type are created via addX.
	 */
	@Test
	public void ETargetrolenameTest0() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		D v3 = graph.createD();
		E e1 = v1.addX(v2);
		E e2 = v1.addX(v2);
		E e3 = v1.addX(v3);
		E e4 = v1.addX(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(), e4
				.getReversedEdge());
		testIncidenceList(v3, e3.getReversedEdge());
	}

	/**
	 * Test if only edges of one type are created via addX and manually.
	 */
	@Test
	public void ETargetrolenameTest1() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		E e1 = v1.addX(v2);
		E e2 = graph.createE(v1, v2);
		E e3 = v1.addX(v2);
		E e4 = v1.addX(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(), e3
				.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test if an error occurs if an E-edge is created via addX starting at a
	 * C-vertex.
	 */
	@Test(expected = GraphException.class)
	public void ETargetrolenameTestException0() {
		C v1 = graph.createC();
		B v2 = graph.createB();
		v1.addX(v2);
	}

	/**
	 * Test if an error occurs if an E-edge is created via createE starting at a
	 * C-vertex.
	 */
	@Test(expected = GraphException.class)
	public void ETargetrolenameTestException1() {
		C v1 = graph.createC();
		B v2 = graph.createB();
		graph.createE(v1, v2);
	}

	/**
	 * Test if only edges of one type are created via addX.
	 */
	@Test
	public void FTargetrolenameTest0() {
		C v1 = graph.createC();
		D v2 = graph.createD();
		F e1 = v1.addY(v2);
		F e2 = v1.addY(v2);
		F e3 = v1.addY(v2);
		F e4 = v1.addY(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(), e3
				.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test if only edges of one type are created via addX and manually.
	 */
	@Test
	public void FTargetrolenameTest1() {
		C v1 = graph.createC();
		D v2 = graph.createD();
		F e1 = v1.addY(v2);
		F e2 = graph.createF(v1, v2);
		F e3 = v1.addY(v2);
		F e4 = v1.addY(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(), e3
				.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test if only edges of one type are created via addX.
	 */
	@Test
	public void GTargetrolenameTest0() {
		C v1 = graph.createC();
		D v2 = graph.createD();
		G e1 = v1.addZ(v2);
		G e2 = v1.addZ(v2);
		G e3 = v1.addZ(v2);
		G e4 = v1.addZ(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(), e3
				.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test if only edges of one type are created via addX and manually.
	 */
	@Test
	public void GTargetrolenameTest1() {
		C v1 = graph.createC();
		D v2 = graph.createD();
		G e1 = v1.addZ(v2);
		G e2 = graph.createG(v1, v2);
		G e3 = v1.addZ(v2);
		G e4 = v1.addZ(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(), e3
				.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test if edges of different types are created via addX and manually.
	 */
	@Test
	public void MixedTargetrolenameTest0() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		C v3 = graph.createC();
		D v4 = graph.createD();
		E e1 = v1.addX(v2);
		E e2 = v1.addX(v4);
		F e3 = v3.addY(v4);
		G e4 = v3.addZ(v4);
		F e5 = graph.createF(v3, v4);
		E e6 = graph.createE(v1, v4);
		G e7 = graph.createG(v3, v4);
		E e8 = graph.createE(v1, v2);
		testIncidenceList(v1, e1, e2, e6, e8);
		testIncidenceListOfOneEdge(E.class, v1, e1, e2, e6, e8);
		testIncidenceListOfOneEdge(F.class, v1);
		testIncidenceListOfOneEdge(G.class, v1);
		testIncidenceList(v2, e1.getReversedEdge(), e8.getReversedEdge());
		testIncidenceListOfOneEdge(E.class, v2, e1.getReversedEdge(), e8
				.getReversedEdge());
		testIncidenceListOfOneEdge(F.class, v2);
		testIncidenceListOfOneEdge(G.class, v2);
		testIncidenceList(v3, e3, e4, e5, e7);
		testIncidenceListOfOneEdge(E.class, v3);
		testIncidenceListOfOneEdge(F.class, v3, e3, e5);
		testIncidenceListOfOneEdge(G.class, v3, e4, e7);
		testIncidenceList(v4, e2.getReversedEdge(), e3.getReversedEdge(), e4
				.getReversedEdge(), e5.getReversedEdge(), e6.getReversedEdge(),
				e7.getReversedEdge());
		testIncidenceListOfOneEdge(E.class, v4, e2.getReversedEdge(), e6
				.getReversedEdge());
		testIncidenceListOfOneEdge(F.class, v4, e3.getReversedEdge(), e5
				.getReversedEdge());
		testIncidenceListOfOneEdge(G.class, v4, e4.getReversedEdge(), e7
				.getReversedEdge());
	}

	/**
	 * Random test
	 */
	@Test
	public void targetrolenameRandomTest0() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		LinkedList<Edge> v1Inci = new LinkedList<Edge>();
		LinkedList<Edge> v2Inci = new LinkedList<Edge>();
		LinkedList<Edge> v3Inci = new LinkedList<Edge>();
		LinkedList<Edge> v4Inci = new LinkedList<Edge>();
		createRandomGraph(v1, v2, v3, v4, v1Inci, v2Inci, v3Inci, v4Inci);
		testIncidences(v1, v2, v3, v4, v1Inci, v2Inci, v3Inci, v4Inci);
		deleteRandomEdges(v1, v2, v3, v4, v1Inci, v2Inci, v3Inci, v4Inci);
		testIncidences(v1, v2, v3, v4, v1Inci, v2Inci, v3Inci, v4Inci);
		createRandomGraph(v1, v2, v3, v4, v1Inci, v2Inci, v3Inci, v4Inci);
		testIncidences(v1, v2, v3, v4, v1Inci, v2Inci, v3Inci, v4Inci);
	}
	
	/**
	 * Tests the incidences.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v1Inci
	 * @param v2Inci
	 * @param v3Inci
	 * @param v4Inci
	 */
	private void testIncidences(A v1, C v2, B v3, D v4,
			LinkedList<Edge> v1Inci, LinkedList<Edge> v2Inci,
			LinkedList<Edge> v3Inci, LinkedList<Edge> v4Inci) {
		testIncidenceList(v1, v1Inci.toArray(new Edge[0]));
		testIncidenceList(v2, v2Inci.toArray(new Edge[0]));
		testIncidenceList(v3, v3Inci.toArray(new Edge[0]));
		testIncidenceList(v4, v4Inci.toArray(new Edge[0]));
	}

	/**
	 * Deletes 500 random Edges.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v1Inci
	 * @param v2Inci
	 * @param v3Inci
	 * @param v4Inci
	 */
	private void deleteRandomEdges(A v1, C v2, B v3, D v4,
			LinkedList<Edge> v1Inci, LinkedList<Edge> v2Inci,
			LinkedList<Edge> v3Inci, LinkedList<Edge> v4Inci) {
		for(int i=0;i<500;i++){
			Edge e=null;
			while(e==null){
				e=graph.getEdge(rand.nextInt(graph.getECount())+1);
			}
			v1Inci.remove(e);
			v2Inci.remove(e);
			v3Inci.remove(e);
			v4Inci.remove(e);
			v1Inci.remove(e.getReversedEdge());
			v2Inci.remove(e.getReversedEdge());
			v3Inci.remove(e.getReversedEdge());
			v4Inci.remove(e.getReversedEdge());
			e.delete();
		}
	}

	/**
	 * Creates a randomGraph with four vertices and 1000 edges. The incident
	 * edges for each vertex are saved in the corresponding LinkedList.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v1Inci
	 * @param v2Inci
	 * @param v3Inci
	 * @param v4Inci
	 */
	private void createRandomGraph(A v1, C v2, B v3, D v4,
			LinkedList<Edge> v1Inci, LinkedList<Edge> v2Inci,
			LinkedList<Edge> v3Inci, LinkedList<Edge> v4Inci) {
		for (int i = 0; i < 1000; i++) {
			int howToCreate = rand.nextInt(2);
			int whichEdge = rand.nextInt(3);
			if (whichEdge == 0) {
				int end = rand.nextInt(2);
				if (end == 0) {
					E e = howToCreate == 0 ? graph.createE(v1, v3) : v1
							.addX(v3);
					v1Inci.add(e);
					v3Inci.add(e.getReversedEdge());
				} else {
					E e = howToCreate == 0 ? graph.createE(v1, v4) : v1
							.addX(v4);
					v1Inci.add(e);
					v4Inci.add(e.getReversedEdge());
				}
			} else if (whichEdge == 1) {
				F e = howToCreate == 0 ? graph.createF(v2, v4) : v2.addY(v4);
				v2Inci.add(e);
				v4Inci.add(e.getReversedEdge());
			} else {
				G e = howToCreate == 0 ? graph.createG(v2, v4) : v2.addZ(v4);
				v2Inci.add(e);
				v4Inci.add(e.getReversedEdge());
			}
		}
	}
	
	//TODO Testen mit mehreren Bs
	
	//TODO Testem von removeX
	
	//TODO Testen von getFirstX
	
	//TODO Testen on get NextX
}
