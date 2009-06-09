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
import de.uni_koblenz.jgralabtest.schemas.vertextest.H;
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
	public void eAddTargetrolenameTest0() {
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
	public void eAddTargetrolenameTest1() {
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
	public void eAddTargetrolenameTestException0() {
		C v1 = graph.createC();
		B v2 = graph.createB();
		v1.addX(v2);
	}

	/**
	 * Test if an error occurs if an E-edge is created via createE starting at a
	 * C-vertex.
	 */
	@Test(expected = GraphException.class)
	public void eAddTargetrolenameTestException1() {
		C v1 = graph.createC();
		B v2 = graph.createB();
		graph.createE(v1, v2);
	}

	/**
	 * Test if only edges of one type are created via addX.
	 */
	@Test
	public void fAddTargetrolenameTest0() {
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
	public void fAddTargetrolenameTest1() {
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
	public void gAddTargetrolenameTest0() {
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
	public void gAddTargetrolenameTest1() {
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
	public void MixedAddTargetrolenameTest0() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		C v3 = graph.createC();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		E e1 = v1.addX(v2);
		E e2 = v1.addX(v4);
		F e3 = v3.addY(v4);
		G e4 = v3.addZ(v4);
		F e5 = graph.createF(v3, v4);
		E e6 = graph.createE(v1, v4);
		G e7 = graph.createG(v3, v4);
		E e8 = graph.createE(v1, v2);
		E e9 = v1.addX(v5);
		E e10 = v1.addX(v6);
		F e11 = v3.addY(v6);
		G e12 = v3.addZ(v6);
		testIncidenceList(v1, e1, e2, e6, e8, e9, e10);
		testIncidenceListOfOneEdge(E.class, v1, e1, e2, e6, e8, e9, e10);
		testIncidenceListOfOneEdge(F.class, v1);
		testIncidenceListOfOneEdge(G.class, v1);
		testIncidenceList(v2, e1.getReversedEdge(), e8.getReversedEdge());
		testIncidenceListOfOneEdge(E.class, v2, e1.getReversedEdge(), e8
				.getReversedEdge());
		testIncidenceListOfOneEdge(F.class, v2);
		testIncidenceListOfOneEdge(G.class, v2);
		testIncidenceList(v3, e3, e4, e5, e7, e11, e12);
		testIncidenceListOfOneEdge(E.class, v3);
		testIncidenceListOfOneEdge(F.class, v3, e3, e5, e11);
		testIncidenceListOfOneEdge(G.class, v3, e4, e7, e12);
		testIncidenceList(v4, e2.getReversedEdge(), e3.getReversedEdge(), e4
				.getReversedEdge(), e5.getReversedEdge(), e6.getReversedEdge(),
				e7.getReversedEdge());
		testIncidenceListOfOneEdge(E.class, v4, e2.getReversedEdge(), e6
				.getReversedEdge());
		testIncidenceListOfOneEdge(F.class, v4, e3.getReversedEdge(), e5
				.getReversedEdge());
		testIncidenceListOfOneEdge(G.class, v4, e4.getReversedEdge(), e7
				.getReversedEdge());
		testIncidenceList(v5, e9.getReversedEdge());
		testIncidenceListOfOneEdge(E.class, v5, e9.getReversedEdge());
		testIncidenceListOfOneEdge(F.class, v5);
		testIncidenceListOfOneEdge(G.class, v5);
		testIncidenceList(v6, e10.getReversedEdge(), e11.getReversedEdge(), e12
				.getReversedEdge());
		testIncidenceListOfOneEdge(E.class, v6, e10.getReversedEdge());
		testIncidenceListOfOneEdge(F.class, v6, e11.getReversedEdge());
		testIncidenceListOfOneEdge(G.class, v6, e12.getReversedEdge());
	}

	/**
	 * Random test
	 */
	@Test
	public void addTargetrolenameRandomTest0() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		LinkedList<Edge> v1Inci = new LinkedList<Edge>();
		LinkedList<Edge> v2Inci = new LinkedList<Edge>();
		LinkedList<Edge> v3Inci = new LinkedList<Edge>();
		LinkedList<Edge> v4Inci = new LinkedList<Edge>();
		LinkedList<Edge> v5Inci = new LinkedList<Edge>();
		LinkedList<Edge> v6Inci = new LinkedList<Edge>();
		createRandomGraph(v1, v2, v3, v4, v5, v6, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci);
		testIncidences(v1, v2, v3, v4, v5, v6, v1Inci, v2Inci, v3Inci, v4Inci,
				v5Inci, v6Inci);
		deleteRandomEdges(v1, v2, v3, v4, v5, v6, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci);
		testIncidences(v1, v2, v3, v4, v5, v6, v1Inci, v2Inci, v3Inci, v4Inci,
				v5Inci, v6Inci);
		createRandomGraph(v1, v2, v3, v4, v5, v6, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci);
		testIncidences(v1, v2, v3, v4, v5, v6, v1Inci, v2Inci, v3Inci, v4Inci,
				v5Inci, v6Inci);
	}

	/**
	 * Tests the incidences.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v6
	 * @param v5
	 * @param v1Inci
	 * @param v2Inci
	 * @param v3Inci
	 * @param v4Inci
	 * @param inci2
	 * @param inci
	 */
	private void testIncidences(A v1, C v2, B v3, D v4, B v5, D v6,
			LinkedList<Edge> v1Inci, LinkedList<Edge> v2Inci,
			LinkedList<Edge> v3Inci, LinkedList<Edge> v4Inci,
			LinkedList<Edge> v5Inci, LinkedList<Edge> v6Inci) {
		testIncidenceList(v1, v1Inci.toArray(new Edge[0]));
		testIncidenceList(v2, v2Inci.toArray(new Edge[0]));
		testIncidenceList(v3, v3Inci.toArray(new Edge[0]));
		testIncidenceList(v4, v4Inci.toArray(new Edge[0]));
		testIncidenceList(v5, v5Inci.toArray(new Edge[0]));
		testIncidenceList(v6, v6Inci.toArray(new Edge[0]));
	}

	/**
	 * Deletes 500 random Edges.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v6
	 * @param v5
	 * @param v1Inci
	 * @param v2Inci
	 * @param v3Inci
	 * @param v4Inci
	 * @param inci2
	 * @param inci
	 */
	private void deleteRandomEdges(A v1, C v2, B v3, D v4, B v5, D v6,
			LinkedList<Edge> v1Inci, LinkedList<Edge> v2Inci,
			LinkedList<Edge> v3Inci, LinkedList<Edge> v4Inci,
			LinkedList<Edge> v5Inci, LinkedList<Edge> v6Inci) {
		for (int i = 0; i < 500; i++) {
			Edge e = null;
			while (e == null) {
				int random = rand.nextInt(graph.getECount()) + 1;
				e = graph.getEdge(random);
			}
			v1Inci.remove(e);
			v2Inci.remove(e);
			v3Inci.remove(e.getReversedEdge());
			v4Inci.remove(e.getReversedEdge());
			v5Inci.remove(e.getReversedEdge());
			v6Inci.remove(e.getReversedEdge());
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
	 * @param v6
	 * @param v5
	 * @param v1Inci
	 * @param v2Inci
	 * @param v3Inci
	 * @param v4Inci
	 * @param inci2
	 * @param inci
	 */
	private void createRandomGraph(A v1, C v2, B v3, D v4, B v5, D v6,
			LinkedList<Edge> v1Inci, LinkedList<Edge> v2Inci,
			LinkedList<Edge> v3Inci, LinkedList<Edge> v4Inci,
			LinkedList<Edge> v5Inci, LinkedList<Edge> v6Inci) {
		for (int i = 0; i < 1000; i++) {
			int howToCreate = rand.nextInt(2);
			int whichEdge = rand.nextInt(4);
			if (whichEdge == 0) {
				int end = rand.nextInt(4);
				E e = null;
				switch (end) {
				case 0:
					e = howToCreate == 0 ? graph.createE(v1, v3) : v1.addX(v3);
					v1Inci.add(e);
					v3Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createE(v1, v4) : v1.addX(v4);
					v1Inci.add(e);
					v4Inci.add(e.getReversedEdge());
					break;
				case 2:
					e = howToCreate == 0 ? graph.createE(v1, v5) : v1.addX(v5);
					v1Inci.add(e);
					v5Inci.add(e.getReversedEdge());
					break;
				case 3:
					e = howToCreate == 0 ? graph.createE(v1, v6) : v1.addX(v6);
					v1Inci.add(e);
					v6Inci.add(e.getReversedEdge());
					break;
				}
			} else if (whichEdge == 1) {
				int end = rand.nextInt(2);
				F e = null;
				switch (end) {
				case 0:
					e = howToCreate == 0 ? graph.createF(v2, v4) : v2.addY(v4);
					v2Inci.add(e);
					v4Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createF(v2, v6) : v2.addY(v6);
					v2Inci.add(e);
					v6Inci.add(e.getReversedEdge());
					break;
				}
			} else if (whichEdge == 2) {
				int end = rand.nextInt(2);
				G e = null;
				switch (end) {
				case 0:
					e = howToCreate == 0 ? graph.createG(v2, v4) : v2.addZ(v4);
					v2Inci.add(e);
					v4Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createG(v2, v6) : v2.addZ(v6);
					v2Inci.add(e);
					v6Inci.add(e.getReversedEdge());
					break;
				}
			} else {
				int end = rand.nextInt(4);
				int start = rand.nextInt(2);
				H e = null;
				switch (end) {
				case 0:
					e = howToCreate == 0 ? graph.createH(start == 0 ? v1 : v2,
							v3) : (start == 0 ? v1 : v2).addW(v3);
					(start == 0 ? v1Inci : v2Inci).add(e);
					v3Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createH(start == 0 ? v1 : v2,
							v4) : (start == 0 ? v1 : v2).addW(v4);
					(start == 0 ? v1Inci : v2Inci).add(e);
					v4Inci.add(e.getReversedEdge());
					break;
				case 2:
					e = howToCreate == 0 ? graph.createH(start == 0 ? v1 : v2,
							v5) : (start == 0 ? v1 : v2).addW(v5);
					(start == 0 ? v1Inci : v2Inci).add(e);
					v5Inci.add(e.getReversedEdge());
					break;
				case 3:
					e = howToCreate == 0 ? graph.createH(start == 0 ? v1 : v2,
							v6) : (start == 0 ? v1 : v2).addW(v6);
					(start == 0 ? v1Inci : v2Inci).add(e);
					v6Inci.add(e.getReversedEdge());
					break;
				}
			}
		}
	}

	/*
	 * 1.2 Test of removeRoleName
	 */

	/**
	 * call removeX when no x exists.
	 */
	@Test
	public void removeTargetRoleNameTest0() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		D v3 = graph.createD();
		F e1 = v2.addY(v3);
		v1.removeX(v3);
		testIncidenceList(v1);
		testIncidenceList(v2, e1);
		testIncidenceList(v3, e1.getReversedEdge());
	}

	/**
	 * Remove all x of one vertex.
	 */
	@Test
	public void removeTargetRoleNameTest1() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		D v3 = graph.createD();
		B v4 = graph.createB();
		v1.addX(v3);
		v1.addW(v3);
		E e3 = v1.addX(v4);
		F e4 = v2.addY(v3);
		graph.createE(v1, v3);
		H e6 = v1.addW(v4);
		v1.removeX(v3);
		testIncidenceList(v1, e3, e6);
		testIncidenceList(v2, e4);
		testIncidenceList(v3, e4.getReversedEdge());
		testIncidenceList(v4, e3.getReversedEdge(), e6.getReversedEdge());
	}

	/**
	 * Random test
	 */
	@Test
	public void removeTargetrolenameRandomTest0() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		LinkedList<Edge> v1Inci = new LinkedList<Edge>();
		LinkedList<Edge> v2Inci = new LinkedList<Edge>();
		LinkedList<Edge> v3Inci = new LinkedList<Edge>();
		LinkedList<Edge> v4Inci = new LinkedList<Edge>();
		LinkedList<Edge> v5Inci = new LinkedList<Edge>();
		LinkedList<Edge> v6Inci = new LinkedList<Edge>();
		createRandomGraph(v1, v2, v3, v4, v5, v6, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci);
		deleteAll(v1, v3, v1Inci, v3Inci, "x", "w");
		v1.removeX(v3);
		testIncidences(v1, v2, v3, v4, v5, v6, v1Inci, v2Inci, v3Inci, v4Inci,
				v5Inci, v6Inci);
		deleteAll(v1, v4, v1Inci, v4Inci, "x", "w");
		v1.removeX(v4);
		testIncidences(v1, v2, v3, v4, v5, v6, v1Inci, v2Inci, v3Inci, v4Inci,
				v5Inci, v6Inci);
	}

	/**
	 * Deletes all Edges <code>from</code>--&gt<code>to</code> whith the
	 * rolename <code>rolename</code>.
	 * 
	 * @param from
	 * @param to
	 * @param rolename
	 * @param inciFrom
	 * @param inciTo
	 */
	private void deleteAll(Vertex from, Vertex to, LinkedList<Edge> inciFrom,
			LinkedList<Edge> inciTo, String... rolenames) {
		for (int i = 0; i < inciFrom.size(); i++) {
			Edge e = inciFrom.get(i).getNormalEdge();
			if (e.getAlpha() == from && e.getOmega() == to
					&& rolenameToDelete(e.getThatRole(), rolenames)) {
				inciFrom.remove(i--);
			}
		}
		for (int i = 0; i < inciTo.size(); i++) {
			Edge e = inciTo.get(i).getNormalEdge();
			if (e.getAlpha() == from && e.getOmega() == to
					&& rolenameToDelete(e.getThatRole(), rolenames)) {
				inciTo.remove(i--);
			}
		}
	}

	/**
	 * Checks if <code>thatRole</code> is contained in <code>rolenames</code>.
	 * 
	 * @param thatRole
	 * @param rolenames
	 * @return
	 */
	private boolean rolenameToDelete(String thatRole, String[] rolenames) {
		boolean contains = false;
		for (String s : rolenames) {
			contains = contains || s.equals(thatRole);
		}
		return contains;
	}

	// TODO Testen von getFirstX

	// TODO Testen on get NextX
	/*
	 * TODO testen von source rolename
	 */
}
