package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.InheritanceException;
import de.uni_koblenz.jgralabtest.schemas.vertextest.A;
import de.uni_koblenz.jgralabtest.schemas.vertextest.B;
import de.uni_koblenz.jgralabtest.schemas.vertextest.C;
import de.uni_koblenz.jgralabtest.schemas.vertextest.C2;
import de.uni_koblenz.jgralabtest.schemas.vertextest.D;
import de.uni_koblenz.jgralabtest.schemas.vertextest.D2;
import de.uni_koblenz.jgralabtest.schemas.vertextest.E;
import de.uni_koblenz.jgralabtest.schemas.vertextest.F;
import de.uni_koblenz.jgralabtest.schemas.vertextest.G;
import de.uni_koblenz.jgralabtest.schemas.vertextest.H;
import de.uni_koblenz.jgralabtest.schemas.vertextest.I;
import de.uni_koblenz.jgralabtest.schemas.vertextest.J;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

@RunWith(Parameterized.class)
public class RoleNameTest extends InstanceTest {

	public RoleNameTest(boolean transactionsEnabled) {
		super(transactionsEnabled);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private VertexTestGraph graph;
	private Random rand;

	/**
	 * Creates a new graph for each test.
	 */
	@Before
	public void setUp() {
		graph = transactionsEnabled ? VertexTestSchema.instance()
				.createVertexTestGraphWithTransactionSupport(100, 100)
				: VertexTestSchema.instance().createVertexTestGraph(100, 100);
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

	/**
	 * Tests the incidences.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v5
	 * @param v6
	 * @param v7
	 * @param v8
	 * @param v1Inci
	 * @param v2Inci
	 * @param v3Inci
	 * @param v4Inci
	 * @param v5Inci
	 * @param v6Inci
	 * @param v7Inci
	 * @param v8Inci
	 * @param v9Inci
	 */
	private void testIncidences(A v1, C v2, B v3, D v4, B v5, D v6, A v7, C v8,
			LinkedList<Edge> v1Inci, LinkedList<Edge> v2Inci,
			LinkedList<Edge> v3Inci, LinkedList<Edge> v4Inci,
			LinkedList<Edge> v5Inci, LinkedList<Edge> v6Inci,
			LinkedList<Edge> v7Inci, LinkedList<Edge> v8Inci) {
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, null, null, null, null,
				v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci,
				null, null, null, null);
	}

	/**
	 * Tests the incidences.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v5
	 * @param v6
	 * @param v7
	 * @param v8
	 * @param v9
	 * @param v10
	 * @param v11
	 * @param v12
	 * @param v1Inci
	 * @param v2Inci
	 * @param v3Inci
	 * @param v4Inci
	 * @param v5Inci
	 * @param v6Inci
	 * @param v7Inci
	 * @param v8Inci
	 * @param v9Inci
	 * @param v10Inci
	 * @param v11Inci
	 * @param v12Inci
	 */
	private void testIncidences(A v1, C v2, B v3, D v4, B v5, D v6, A v7, C v8,
			C2 v9, C2 v10, D2 v11, D2 v12, LinkedList<Edge> v1Inci,
			LinkedList<Edge> v2Inci, LinkedList<Edge> v3Inci,
			LinkedList<Edge> v4Inci, LinkedList<Edge> v5Inci,
			LinkedList<Edge> v6Inci, LinkedList<Edge> v7Inci,
			LinkedList<Edge> v8Inci, LinkedList<Edge> v9Inci,
			LinkedList<Edge> v10Inci, LinkedList<Edge> v11Inci,
			LinkedList<Edge> v12Inci) {
		testIncidenceList(v1, v1Inci.toArray(new Edge[0]));
		testIncidenceList(v2, v2Inci.toArray(new Edge[0]));
		testIncidenceList(v3, v3Inci.toArray(new Edge[0]));
		testIncidenceList(v4, v4Inci.toArray(new Edge[0]));
		testIncidenceList(v5, v5Inci.toArray(new Edge[0]));
		testIncidenceList(v6, v6Inci.toArray(new Edge[0]));
		testIncidenceList(v7, v7Inci.toArray(new Edge[0]));
		testIncidenceList(v8, v8Inci.toArray(new Edge[0]));
		if (v9 != null) {
			testIncidenceList(v9, v9Inci.toArray(new Edge[0]));
			testIncidenceList(v10, v10Inci.toArray(new Edge[0]));
			testIncidenceList(v11, v11Inci.toArray(new Edge[0]));
			testIncidenceList(v12, v12Inci.toArray(new Edge[0]));
		}
	}

	/**
	 * Deletes 500 random Edges.
	 * 
	 * @param incidentLists
	 */
	private void deleteRandomEdges(LinkedList<Edge> v1Inci,
			LinkedList<Edge> v2Inci, LinkedList<Edge> v3Inci,
			LinkedList<Edge> v4Inci, LinkedList<Edge> v5Inci,
			LinkedList<Edge> v6Inci, LinkedList<Edge> v7Inci,
			LinkedList<Edge> v8Inci) {
		deleteRandomEdges(v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci,
				v7Inci, v8Inci, null, null, null, null);
	}

	/**
	 * Deletes 500 random Edges.
	 * 
	 * @param incidentLists
	 */
	private void deleteRandomEdges(LinkedList<Edge> v1Inci,
			LinkedList<Edge> v2Inci, LinkedList<Edge> v3Inci,
			LinkedList<Edge> v4Inci, LinkedList<Edge> v5Inci,
			LinkedList<Edge> v6Inci, LinkedList<Edge> v7Inci,
			LinkedList<Edge> v8Inci, LinkedList<Edge> v9Inci,
			LinkedList<Edge> v10Inci, LinkedList<Edge> v11Inci,
			LinkedList<Edge> v12Inci) {
		for (int i = 0; i < 500; i++) {
			Edge e = null;
			while (e == null) {
				int random = rand.nextInt(graph.getECount()) + 1;
				e = graph.getEdge(random);
			}
			v1Inci.remove(e);
			v2Inci.remove(e);
			v1Inci.remove(e.getReversedEdge());
			v2Inci.remove(e.getReversedEdge());
			v3Inci.remove(e.getReversedEdge());
			v4Inci.remove(e.getReversedEdge());
			v5Inci.remove(e.getReversedEdge());
			v6Inci.remove(e.getReversedEdge());
			v7Inci.remove(e);
			v8Inci.remove(e);
			v7Inci.remove(e.getReversedEdge());
			v8Inci.remove(e.getReversedEdge());
			if (v9Inci != null) {
				v9Inci.remove(e);
				v9Inci.remove(e.getReversedEdge());
				v10Inci.remove(e);
				v10Inci.remove(e.getReversedEdge());
				v11Inci.remove(e);
				v11Inci.remove(e.getReversedEdge());
				v12Inci.remove(e);
				v12Inci.remove(e.getReversedEdge());
			}
			e.delete();
		}
	}

	/**
	 * Creates a randomGraph with four vertices and 1000 edges. The incident
	 * edges for each vertex are saved in the corresponding LinkedList.
	 * 
	 * @param useAddTarget
	 *            true: addTargetRoleName is used, false: addSourceRoleName is
	 *            used
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v6
	 * @param v5
	 * @param v7
	 * @param v8
	 * @param v1Inci
	 * @param v2Inci
	 * @param v3Inci
	 * @param v4Inci
	 * @param v5Inci
	 * @param v6Inci
	 * @param v7Inci
	 * @param v8Inci
	 */
	private void createRandomGraph(boolean useAddTarget, A v1, C v2, B v3,
			D v4, B v5, D v6, A v7, C v8, LinkedList<Edge> v1Inci,
			LinkedList<Edge> v2Inci, LinkedList<Edge> v3Inci,
			LinkedList<Edge> v4Inci, LinkedList<Edge> v5Inci,
			LinkedList<Edge> v6Inci, LinkedList<Edge> v7Inci,
			LinkedList<Edge> v8Inci) {
		createRandomGraph(useAddTarget, v1, v2, v3, v4, v5, v6, v7, v8, null,
				null, null, null, v1Inci, v2Inci, v3Inci, v4Inci, v5Inci,
				v6Inci, v7Inci, v8Inci, null, null, null, null);
	}

	/**
	 * Creates a randomGraph with four vertices and 1000 edges. The incident
	 * edges for each vertex are saved in the corresponding LinkedList.
	 * 
	 * @param useAddTarget
	 *            true: addTargetRoleName is used, false: addSourceRoleName is
	 *            used
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v6
	 * @param v5
	 * @param v7
	 * @param v8
	 * @param v9
	 * @param v10
	 * @param v11
	 * @param v12
	 * @param v1Inci
	 * @param v2Inci
	 * @param v3Inci
	 * @param v4Inci
	 * @param v5Inci
	 * @param v6Inci
	 * @param v7Inci
	 * @param v8Inci
	 * @param v9Inci
	 * @param v10Inci
	 * @param v11Inci
	 * @param v12Inci
	 */
	private void createRandomGraph(boolean useAddTarget, A v1, C v2, B v3,
			D v4, B v5, D v6, A v7, C v8, C2 v9, C2 v10, D2 v11, D2 v12,
			LinkedList<Edge> v1Inci, LinkedList<Edge> v2Inci,
			LinkedList<Edge> v3Inci, LinkedList<Edge> v4Inci,
			LinkedList<Edge> v5Inci, LinkedList<Edge> v6Inci,
			LinkedList<Edge> v7Inci, LinkedList<Edge> v8Inci,
			LinkedList<Edge> v9Inci, LinkedList<Edge> v10Inci,
			LinkedList<Edge> v11Inci, LinkedList<Edge> v12Inci) {
		for (int i = 0; i < 1000; i++) {
			int howToCreate = rand.nextInt(2);
			int whichEdge = rand.nextInt(useAddTarget ? 5 : 6);
			if (whichEdge == 0) {
				// edge E
				int end = rand.nextInt(4);
				int start = rand.nextInt(2);
				E e = null;
				switch (end) {
				case 0:
					e = howToCreate == 0 ? graph.createE(start == 0 ? v1 : v7,
							v3) : (useAddTarget ? (start == 0 ? v1 : v7)
							.addX(v3) : v3.addSourceE(start == 0 ? v1 : v7));
					(start == 0 ? v1Inci : v7Inci).add(e);
					v3Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createE(start == 0 ? v1 : v7,
							v4) : (useAddTarget ? (start == 0 ? v1 : v7)
							.addX(v4) : v4.addSourceE(start == 0 ? v1 : v7));
					(start == 0 ? v1Inci : v7Inci).add(e);
					v4Inci.add(e.getReversedEdge());
					break;
				case 2:
					e = howToCreate == 0 ? graph.createE(start == 0 ? v1 : v7,
							v5) : (useAddTarget ? (start == 0 ? v1 : v7)
							.addX(v5) : v5.addSourceE(start == 0 ? v1 : v7));
					(start == 0 ? v1Inci : v7Inci).add(e);
					v5Inci.add(e.getReversedEdge());
					break;
				case 3:
					e = howToCreate == 0 ? graph.createE(start == 0 ? v1 : v7,
							v6) : (useAddTarget ? (start == 0 ? v1 : v7)
							.addX(v6) : v6.addSourceE(start == 0 ? v1 : v7));
					(start == 0 ? v1Inci : v7Inci).add(e);
					v6Inci.add(e.getReversedEdge());
					break;
				}
			} else if (whichEdge == 1) {
				// edge F
				int end = rand.nextInt(2);
				int start = rand.nextInt(2);
				F e = null;
				switch (end) {
				case 0:
					e = howToCreate == 0 ? graph.createF(start == 0 ? v2 : v8,
							v4) : (useAddTarget ? (start == 0 ? v2 : v8)
							.addY(v4) : v4.addSourceF(start == 0 ? v2 : v8));
					(start == 0 ? v2Inci : v8Inci).add(e);
					v4Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createF(start == 0 ? v2 : v8,
							v6) : (useAddTarget ? (start == 0 ? v2 : v8)
							.addY(v6) : v6.addSourceF(start == 0 ? v2 : v8));
					(start == 0 ? v2Inci : v8Inci).add(e);
					v6Inci.add(e.getReversedEdge());
					break;
				}
			} else if (whichEdge == 2) {
				// edge G
				int end = rand.nextInt(2);
				int start = rand.nextInt(2);
				G e = null;
				switch (end) {
				case 0:
					e = howToCreate == 0 ? graph.createG(start == 0 ? v2 : v8,
							v4) : (useAddTarget ? (start == 0 ? v2 : v8)
							.addZ(v4) : v4.addSourceG(start == 0 ? v2 : v8));
					(start == 0 ? v2Inci : v8Inci).add(e);
					v4Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createG(start == 0 ? v2 : v8,
							v6) : (useAddTarget ? (start == 0 ? v2 : v8)
							.addZ(v6) : v6.addSourceG(start == 0 ? v2 : v8));
					(start == 0 ? v2Inci : v8Inci).add(e);
					v6Inci.add(e.getReversedEdge());
					break;
				}
			} else if (whichEdge == 3) {
				// edge H
				int end = rand.nextInt(useAddTarget ? 4 : 6);
				int start = rand.nextInt(useAddTarget ? 4 : 6);
				H e = null;
				switch (end) {
				case 0:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createH(v1, v3)
								: (useAddTarget ? v1.addW(v3) : v3
										.addSourceH(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v3)
								: (useAddTarget ? v2.addW(v3) : v3
										.addSourceH(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v3)
								: (useAddTarget ? v7.addW(v3) : v3
										.addSourceH(v7));
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v3)
								: (useAddTarget ? v8.addW(v3) : v3
										.addSourceH(v8));
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v3)
								: (useAddTarget ? v9.addW(v3) : v3
										.addSourceH(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v3)
								: (useAddTarget ? v10.addW(v3) : v3
										.addSourceH(v10));
						v10Inci.add(e);
						break;
					}
					v3Inci.add(e.getReversedEdge());
					break;
				case 1:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createH(v1, v4)
								: (useAddTarget ? v1.addW(v4) : v4
										.addSourceH(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v4)
								: (useAddTarget ? v2.addW(v4) : v4
										.addSourceH(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v4)
								: (useAddTarget ? v7.addW(v4) : v4
										.addSourceH(v7));
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v4)
								: (useAddTarget ? v8.addW(v4) : v4
										.addSourceH(v8));
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v4)
								: (useAddTarget ? v9.addW(v4) : v4
										.addSourceH(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v4)
								: (useAddTarget ? v10.addW(v4) : v4
										.addSourceH(v10));
						v10Inci.add(e);
						break;
					}
					v4Inci.add(e.getReversedEdge());
					break;
				case 2:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createH(v1, v5)
								: (useAddTarget ? v1.addW(v5) : v5
										.addSourceH(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v5)
								: (useAddTarget ? v2.addW(v5) : v5
										.addSourceH(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v5)
								: (useAddTarget ? v7.addW(v5) : v5
										.addSourceH(v7));
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v5)
								: (useAddTarget ? v8.addW(v5) : v5
										.addSourceH(v8));
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v5)
								: (useAddTarget ? v9.addW(v5) : v5
										.addSourceH(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v5)
								: (useAddTarget ? v10.addW(v5) : v5
										.addSourceH(v10));
						v10Inci.add(e);
						break;
					}
					v5Inci.add(e.getReversedEdge());
					break;
				case 3:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createH(v1, v6)
								: (useAddTarget ? v1.addW(v6) : v6
										.addSourceH(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v6)
								: (useAddTarget ? v2.addW(v6) : v6
										.addSourceH(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v6)
								: (useAddTarget ? v7.addW(v6) : v6
										.addSourceH(v7));
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v6)
								: (useAddTarget ? v8.addW(v6) : v6
										.addSourceH(v8));
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v6)
								: (useAddTarget ? v9.addW(v6) : v6
										.addSourceH(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v6)
								: (useAddTarget ? v10.addW(v6) : v6
										.addSourceH(v10));
						v10Inci.add(e);
						break;
					}
					v6Inci.add(e.getReversedEdge());
					break;
				case 4:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createH(v1, v11)
								: (useAddTarget ? v1.addW(v11) : v11
										.addSourceH(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v11)
								: (useAddTarget ? v2.addW(v11) : v11
										.addSourceH(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v11)
								: (useAddTarget ? v7.addW(v11) : v11
										.addSourceH(v7));
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v11)
								: (useAddTarget ? v8.addW(v11) : v11
										.addSourceH(v8));
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v11)
								: (useAddTarget ? v9.addW(v11) : v11
										.addSourceH(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v11)
								: (useAddTarget ? v10.addW(v11) : v11
										.addSourceH(v10));
						v10Inci.add(e);
						break;
					}
					v11Inci.add(e.getReversedEdge());
					break;
				case 5:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createH(v1, v12)
								: (useAddTarget ? v1.addW(v12) : v12
										.addSourceH(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v12)
								: (useAddTarget ? v2.addW(v12) : v12
										.addSourceH(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v12)
								: (useAddTarget ? v7.addW(v12) : v12
										.addSourceH(v7));
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v12)
								: (useAddTarget ? v8.addW(v12) : v12
										.addSourceH(v8));
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v12)
								: (useAddTarget ? v9.addW(v12) : v12
										.addSourceH(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v12)
								: (useAddTarget ? v10.addW(v12) : v12
										.addSourceH(v10));
						v10Inci.add(e);
						break;
					}
					v12Inci.add(e.getReversedEdge());
					break;
				}
			} else if (whichEdge == 4) {
				// edge I
				int end = rand.nextInt(useAddTarget ? 4 : 6);
				int start = rand.nextInt(useAddTarget ? 4 : 6);
				I e = null;
				switch (end) {
				case 0:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createI(v1, v1)
								: (useAddTarget ? v1.addV(v1) : v1
										.addSourceI(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createI(v2, v1)
								: (useAddTarget ? v2.addV(v1) : v1
										.addSourceI(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createI(v7, v1)
								: (useAddTarget ? v7.addV(v1) : v1
										.addSourceI(v7));
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createI(v8, v1)
								: (useAddTarget ? v8.addV(v1) : v1
										.addSourceI(v8));
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createI(v9, v1)
								: (useAddTarget ? v9.addV(v1) : v1
										.addSourceI(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createI(v10, v1)
								: (useAddTarget ? v10.addV(v1) : v1
										.addSourceI(v10));
						v10Inci.add(e);
						break;
					}
					v1Inci.add(e.getReversedEdge());
					break;
				case 1:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createI(v1, v2)
								: (useAddTarget ? v1.addV(v2) : v2
										.addSourceI(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createI(v2, v2)
								: (useAddTarget ? v2.addV(v2) : v2
										.addSourceI(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createI(v7, v2)
								: (useAddTarget ? v7.addV(v2) : v2
										.addSourceI(v7));
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createI(v8, v2)
								: (useAddTarget ? v8.addV(v2) : v2
										.addSourceI(v8));
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createI(v9, v2)
								: (useAddTarget ? v9.addV(v2) : v2
										.addSourceI(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createI(v10, v2)
								: (useAddTarget ? v10.addV(v2) : v2
										.addSourceI(v10));
						v10Inci.add(e);
						break;
					}
					v2Inci.add(e.getReversedEdge());
					break;
				case 2:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createI(v1, v7)
								: (useAddTarget ? v1.addV(v7) : v7
										.addSourceI(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createI(v2, v7)
								: (useAddTarget ? v2.addV(v7) : v7
										.addSourceI(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createI(v7, v7)
								: (useAddTarget ? v7.addV(v7) : v7
										.addSourceI(v7));
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createI(v8, v7)
								: (useAddTarget ? v8.addV(v7) : v7
										.addSourceI(v8));
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createI(v9, v7)
								: (useAddTarget ? v9.addV(v7) : v7
										.addSourceI(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createI(v10, v7)
								: (useAddTarget ? v10.addV(v7) : v7
										.addSourceI(v10));
						v10Inci.add(e);
						break;
					}
					v7Inci.add(e.getReversedEdge());
					break;
				case 3:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createI(v1, v8)
								: (useAddTarget ? v1.addV(v8) : v8
										.addSourceI(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createI(v2, v8)
								: (useAddTarget ? v2.addV(v8) : v8
										.addSourceI(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createI(v7, v8)
								: (useAddTarget ? v7.addV(v8) : v8
										.addSourceI(v7));
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createI(v8, v8)
								: (useAddTarget ? v8.addV(v8) : v8
										.addSourceI(v8));
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createI(v9, v8)
								: (useAddTarget ? v9.addV(v8) : v8
										.addSourceI(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createI(v10, v8)
								: (useAddTarget ? v10.addV(v8) : v8
										.addSourceI(v10));
						v10Inci.add(e);
						break;
					}
					v8Inci.add(e.getReversedEdge());
					break;
				case 4:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createI(v1, v9)
								: (useAddTarget ? v1.addV(v9) : v9
										.addSourceI(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createI(v2, v9)
								: (useAddTarget ? v2.addV(v9) : v9
										.addSourceI(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createI(v9, v9)
								: (useAddTarget ? v9.addV(v9) : v9
										.addSourceI(v9));
						v9Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createI(v8, v9)
								: (useAddTarget ? v8.addV(v9) : v9
										.addSourceI(v8));
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createI(v9, v9)
								: (useAddTarget ? v9.addV(v9) : v9
										.addSourceI(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createI(v10, v9)
								: (useAddTarget ? v10.addV(v9) : v9
										.addSourceI(v10));
						v10Inci.add(e);
						break;
					}
					v9Inci.add(e.getReversedEdge());
					break;
				case 5:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createI(v1, v10)
								: (useAddTarget ? v1.addV(v10) : v10
										.addSourceI(v1));
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createI(v2, v10)
								: (useAddTarget ? v2.addV(v10) : v10
										.addSourceI(v2));
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createI(v7, v10)
								: (useAddTarget ? v7.addV(v10) : v10
										.addSourceI(v7));
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createI(v10, v10)
								: (useAddTarget ? v10.addV(v10) : v10
										.addSourceI(v10));
						v10Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createI(v9, v10)
								: (useAddTarget ? v9.addV(v10) : v10
										.addSourceI(v9));
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createI(v10, v10)
								: (useAddTarget ? v10.addV(v10) : v10
										.addSourceI(v10));
						v10Inci.add(e);
						break;
					}
					v10Inci.add(e.getReversedEdge());
					break;
				}
			} else {
				// edge J
				int end = rand.nextInt(2);
				int start = rand.nextInt(2);
				J e = null;
				switch (end) {
				case 0:
					e = howToCreate == 0 ? graph.createJ(start == 0 ? v9 : v10,
							v11) : (useAddTarget ? (start == 0 ? v9 : v10)
							.addU(v11) : v11.addSourceJ(start == 0 ? v9 : v10));
					(start == 0 ? v9Inci : v10Inci).add(e);
					v11Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createJ(start == 0 ? v9 : v10,
							v12) : (useAddTarget ? (start == 0 ? v9 : v10)
							.addU(v12) : v12.addSourceJ(start == 0 ? v9 : v10));
					(start == 0 ? v9Inci : v10Inci).add(e);
					v12Inci.add(e.getReversedEdge());
					break;
				}
			}
		}
	}

	/**
	 * Deletes all Edges <code>from</code>--&gt<code>to</code> with the rolename
	 * <code>rolenames</code>.
	 * 
	 * @param useTarget
	 *            true: <code>rolenames</code> are target rolenames, false:
	 *            <code>rolenames</code> are source rolenames
	 * @param from
	 * @param to
	 * @param rolenames
	 * @param inciFrom
	 * @param inciTo
	 */
	private void deleteAll(boolean useTarget, Vertex from, Vertex to,
			LinkedList<Edge> inciFrom, LinkedList<Edge> inciTo,
			String... rolenames) {
		for (int i = 0; i < inciFrom.size(); i++) {
			Edge e = inciFrom.get(i).getNormalEdge();
			if ((e.getAlpha() == from)
					&& (e.getOmega() == to)
					&& checkRoleName(useTarget ? e.getThatRole() : e
							.getThisRole(), rolenames)) {
				inciFrom.remove(i--);
			}
		}
		for (int i = 0; i < inciTo.size(); i++) {
			Edge e = inciTo.get(i).getNormalEdge();
			if ((e.getAlpha() == from)
					&& (e.getOmega() == to)
					&& checkRoleName(useTarget ? e.getThatRole() : e
							.getThisRole(), rolenames)) {
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
	private boolean checkRoleName(String thatRole, String[] rolenames) {
		boolean contains = false;
		for (String s : rolenames) {
			contains = contains || s.equals(thatRole);
		}
		return contains;
	}

	/**
	 * Returns a List of all adjacent <code>rolenames</code>-vertices.
	 * 
	 * @param inci
	 *            list of incident Edges of one node
	 * @param rolenames
	 * @return LinkedList&ltVertex&gt which contains the relevant edges
	 */
	private LinkedList<Vertex> getAllVerticesWithRolename(
			LinkedList<Edge> inci, String... rolenames) {
		LinkedList<Vertex> ret = new LinkedList<Vertex>();
		for (Edge e : inci) {
			if (e.isNormal() && checkRoleName(e.getThatRole(), rolenames)) {
				ret.add(e.getOmega());
			}
		}
		return ret;
	}

	/**
	 * Returns a List of all adjacent <code>rolenames</code>-vertices.
	 * 
	 * @param to
	 * @param rolenames
	 * @return LinkedList&ltVertex&gt which contains the relevant edges
	 */
	private LinkedList<Vertex> getAllVerticesWithRolename(Vertex to,
			String... rolenames) {
		LinkedList<Vertex> ret = new LinkedList<Vertex>();
		PriorityQueue<Edge> edges = new PriorityQueue<Edge>();
		for (Edge e : graph.edges()) {
			if ((e.getOmega() == to)
					&& checkRoleName(e.getThisRole(), rolenames)) {
				edges.add(e);
			}
		}
		for (Edge e : edges) {
			ret.add(e.getAlpha());
		}
		return ret;
	}

	/**
	 * Compares the equity of the elemente of <code>expected</code> and
	 * <code>actual</code>.
	 * 
	 * @param expected
	 * @param actual
	 */
	private void compareLists(List<Vertex> expected, Vertex[] actual) {
		assertEquals(expected.size(), actual.length);
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual[i]);
		}
	}

	/**
	 * Compiles the schema defined in schemaString.
	 * 
	 * @param schemaString
	 * @return the schema
	 * @throws GraphIOException
	 */
	private Schema compileSchema(String schemaString) throws GraphIOException {
		ByteArrayInputStream input = new ByteArrayInputStream(schemaString
				.getBytes());
		Schema s = null;
		s = GraphIO.loadSchemaFromStream(input);
		s.compile(true);
		return s;
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
		onlyTestWithoutTransactionSupport();
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
		onlyTestWithoutTransactionSupport();
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
	 * Test with cyclic edges.
	 */
	@Test
	public void eAddTargetrolenameTest2() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		A v2 = graph.createA();
		C v3 = graph.createC();
		I e1 = graph.createI(v1, v1);
		I e2 = v1.addV(v2);
		I e3 = v2.addV(v2);
		I e4 = graph.createI(v2, v3);
		testIncidenceList(v1, e1, e1.getReversedEdge(), e2);
		testIncidenceList(v2, e2.getReversedEdge(), e3, e3.getReversedEdge(),
				e4);
		testIncidenceList(v3, e4.getReversedEdge());
	}

	/**
	 * Test if an error occurs if an E-edge is created via addX starting at a
	 * C-vertex.
	 */
	@Test(expected = GraphException.class)
	public void eAddTargetrolenameTestException0() {
		onlyTestWithoutTransactionSupport();
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
		onlyTestWithoutTransactionSupport();
		C v1 = graph.createC();
		B v2 = graph.createB();
		graph.createE(v1, v2);
	}

	/**
	 * Test if an error occurs if an E-edge is created via createE starting at a
	 * C2-vertex.
	 */
	@Test(expected = GraphException.class)
	public void eAddTargetrolenameTestException2() {
		onlyTestWithoutTransactionSupport();
		C2 v1 = graph.createC2();
		B v2 = graph.createB();
		graph.createE(v1, v2);
	}

	/**
	 * Test if an error occurs if you try to build an edge with null as omega.
	 */
	@Test(expected = GraphException.class)
	public void eAddTargetrolenameTestException3() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		v1.addX(null);
	}

	/**
	 * Test if only edges of one type are created via addX.
	 */
	@Test
	public void fAddTargetrolenameTest0() {
		onlyTestWithoutTransactionSupport();
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
		onlyTestWithoutTransactionSupport();
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
		onlyTestWithoutTransactionSupport();
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
		onlyTestWithoutTransactionSupport();
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
	public void mixedAddTargetrolenameTest0() {
		onlyTestWithoutTransactionSupport();
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
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		A v7 = graph.createA();
		C v8 = graph.createC();
		LinkedList<Edge> v1Inci = new LinkedList<Edge>();
		LinkedList<Edge> v2Inci = new LinkedList<Edge>();
		LinkedList<Edge> v3Inci = new LinkedList<Edge>();
		LinkedList<Edge> v4Inci = new LinkedList<Edge>();
		LinkedList<Edge> v5Inci = new LinkedList<Edge>();
		LinkedList<Edge> v6Inci = new LinkedList<Edge>();
		LinkedList<Edge> v7Inci = new LinkedList<Edge>();
		LinkedList<Edge> v8Inci = new LinkedList<Edge>();
		createRandomGraph(true, v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci,
				v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		deleteRandomEdges(v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci,
				v7Inci, v8Inci);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		createRandomGraph(true, v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci,
				v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
	}

	/*
	 * 1.2 Test of removeRoleName
	 */

	/**
	 * call removeX when no x exists.
	 */
	@Test
	public void removeTargetRoleNameTest0() {
		onlyTestWithoutTransactionSupport();
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
		onlyTestWithoutTransactionSupport();
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
	 * Test with cyclic edges.
	 */
	@Test
	public void removeTargetrolenameTest2() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		A v2 = graph.createA();
		C v3 = graph.createC();
		I e1 = graph.createI(v1, v1);
		I e2 = v1.addV(v2);
		v2.addV(v2);
		I e4 = graph.createI(v2, v3);
		v2.removeV(v2);
		testIncidenceList(v1, e1, e1.getReversedEdge(), e2);
		testIncidenceList(v2, e2.getReversedEdge(), e4);
		testIncidenceList(v3, e4.getReversedEdge());
	}

	/**
	 * Test if an error occurs if an E-edge is removed via removeX starting at a
	 * C-vertex.
	 */
	@Test(expected = GraphException.class)
	public void removeTargetrolenameTestException0() {
		onlyTestWithoutTransactionSupport();
		C v1 = graph.createC();
		B v2 = graph.createB();
		v1.removeX(v2);
	}

	/**
	 * Test if an error occurs if an E-edge is removed via removeX starting at a
	 * C-vertex.
	 */
	@Test(expected = GraphException.class)
	public void removeTargetrolenameTestException3() {
		onlyTestWithoutTransactionSupport();
		C2 v1 = graph.createC2();
		B v2 = graph.createB();
		v1.removeX(v2);
	}

	/**
	 * Random test
	 */
	@Test
	public void removeTargetrolenameRandomTest0() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		A v7 = graph.createA();
		C v8 = graph.createC();
		LinkedList<Edge> v1Inci = new LinkedList<Edge>();
		LinkedList<Edge> v2Inci = new LinkedList<Edge>();
		LinkedList<Edge> v3Inci = new LinkedList<Edge>();
		LinkedList<Edge> v4Inci = new LinkedList<Edge>();
		LinkedList<Edge> v5Inci = new LinkedList<Edge>();
		LinkedList<Edge> v6Inci = new LinkedList<Edge>();
		LinkedList<Edge> v7Inci = new LinkedList<Edge>();
		LinkedList<Edge> v8Inci = new LinkedList<Edge>();
		createRandomGraph(true, v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci,
				v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		deleteAll(true, v1, v3, v1Inci, v3Inci, "x", "w");
		v1.removeX(v3);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		deleteAll(true, v1, v4, v1Inci, v4Inci, "x", "w");
		v1.removeX(v4);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		deleteAll(true, v7, v6, v7Inci, v6Inci, "w");
		v7.removeW(v6);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
	}

	/*
	 * 1.3 Test of getRoleNameList
	 */

	/**
	 * Test a vertex which has no adjacent x-vertices.
	 */
	@Test
	public void getRoleNameListTest0() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		compareLists(new LinkedList<Vertex>(), v1.getXList().toArray(
				new Vertex[0]));
	}

	/**
	 * Test a vertex which has adjacent w-vertices.
	 */
	@Test
	public void getRoleNameListTest1() {
		onlyTestWithoutTransactionSupport();
		C v1 = graph.createC();
		D v2 = graph.createD();
		v1.addW(v2);
		v1.addY(v2);
		v1.addW(v2);
		v1.addZ(v2);
		v1.addW(v2);
		LinkedList<Vertex> expected = new LinkedList<Vertex>();
		expected.add(v2);
		expected.add(v2);
		expected.add(v2);
		compareLists(expected, v1.getWList().toArray(new Vertex[0]));
		expected = new LinkedList<Vertex>();
		expected.add(v2);
		compareLists(expected, v1.getYList().toArray(new Vertex[0]));
		compareLists(expected, v1.getZList().toArray(new Vertex[0]));
	}

	/**
	 * Test with cyclic edges.
	 */
	@Test
	public void getRoleNameListTest2() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		v1.addV(v1);
		graph.createI(v1, v1);
		LinkedList<Vertex> expected = new LinkedList<Vertex>();
		expected.add(v1);
		expected.add(v1);
		compareLists(expected, v1.getVList().toArray(new Vertex[0]));
	}

	/**
	 * Test if an error occurs if you try to get a list of all x-vertices of one
	 * C-vertex.
	 */
	@Test(expected = GraphException.class)
	public void getRoleNameListTestException0() {
		onlyTestWithoutTransactionSupport();
		C v1 = graph.createC();
		v1.getXList();
	}

	/**
	 * Test if an error occurs if you try to get a list of all x-vertices of one
	 * C2-vertex.
	 */
	@Test(expected = GraphException.class)
	public void getRoleNameListTestException1() {
		onlyTestWithoutTransactionSupport();
		C2 v1 = graph.createC2();
		v1.getXList();
	}

	/**
	 * Random test
	 */
	@Test
	public void getRoleNameListRandomTest0() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		A v7 = graph.createA();
		C v8 = graph.createC();
		LinkedList<Edge> v1Inci = new LinkedList<Edge>();
		LinkedList<Edge> v2Inci = new LinkedList<Edge>();
		LinkedList<Edge> v3Inci = new LinkedList<Edge>();
		LinkedList<Edge> v4Inci = new LinkedList<Edge>();
		LinkedList<Edge> v5Inci = new LinkedList<Edge>();
		LinkedList<Edge> v6Inci = new LinkedList<Edge>();
		LinkedList<Edge> v7Inci = new LinkedList<Edge>();
		LinkedList<Edge> v8Inci = new LinkedList<Edge>();
		createRandomGraph(true, v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci,
				v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);

		LinkedList<Vertex> expected = getAllVerticesWithRolename(v1Inci, "x",
				"w");
		compareLists(expected, v1.getXList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v1Inci, "w");
		compareLists(expected, v1.getWList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v1Inci, "v");
		compareLists(expected, v1.getVList().toArray(new Vertex[0]));

		expected = getAllVerticesWithRolename(v2Inci, "w");
		compareLists(expected, v2.getWList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v2Inci, "v");
		compareLists(expected, v2.getVList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v2Inci, "y");
		compareLists(expected, v2.getYList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v2Inci, "z");
		compareLists(expected, v2.getZList().toArray(new Vertex[0]));

		expected = getAllVerticesWithRolename(v7Inci, "x", "w");
		compareLists(expected, v7.getXList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v7Inci, "w");
		compareLists(expected, v7.getWList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v7Inci, "v");
		compareLists(expected, v7.getVList().toArray(new Vertex[0]));

		expected = getAllVerticesWithRolename(v8Inci, "w");
		compareLists(expected, v8.getWList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v8Inci, "v");
		compareLists(expected, v8.getVList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v8Inci, "y");
		compareLists(expected, v8.getYList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v8Inci, "z");
		compareLists(expected, v8.getZList().toArray(new Vertex[0]));
	}

	/*
	 * 2. Test of source rolename.
	 */

	/*
	 * 2.1 Test of addRoleName
	 */

	/**
	 * Test if only edges of one type are created via addSourceE.
	 */
	@Test
	public void addSourcerolenameTest0() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		B v2 = graph.createB();
		D v3 = graph.createD();
		E e1 = v2.addSourceE(v1);
		E e2 = v2.addSourceE(v1);
		E e3 = v3.addSourceE(v1);
		E e4 = v2.addSourceE(v1);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(), e4
				.getReversedEdge());
		testIncidenceList(v3, e3.getReversedEdge());
	}

	/**
	 * Test if only edges of one type are created via addSourceE and manually.
	 */
	@Test
	public void addSourcerolenameTest1() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		B v2 = graph.createB();
		E e1 = v2.addSourceE(v1);
		E e2 = graph.createE(v1, v2);
		E e3 = v2.addSourceE(v1);
		E e4 = v2.addSourceE(v1);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(), e3
				.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test with cyclic edges.
	 */
	@Test
	public void addSourcerolenameTest2() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		A v2 = graph.createA();
		C v3 = graph.createC();
		I e1 = graph.createI(v1, v1);
		I e2 = v2.addSourceI(v1);
		I e3 = v2.addSourceI(v2);
		I e4 = graph.createI(v2, v3);
		testIncidenceList(v1, e1, e1.getReversedEdge(), e2);
		testIncidenceList(v2, e2.getReversedEdge(), e3, e3.getReversedEdge(),
				e4);
		testIncidenceList(v3, e4.getReversedEdge());
	}

	/**
	 * Test if an error occurs if an E-edge is created via addSourceE starting
	 * at a D2-vertex.
	 */
	@Test(expected = GraphException.class)
	public void addSourcerolenameTestException0() {
		onlyTestWithoutTransactionSupport();
		D2 v1 = graph.createD2();
		A v2 = graph.createA();
		v1.addSourceE(v2);
	}

	/**
	 * Test if an error occurs if an E-edge is created via createE starting at a
	 * D2-vertex.
	 */
	@Test(expected = GraphException.class)
	public void addSourcerolenameTestException1() {
		onlyTestWithoutTransactionSupport();
		D2 v1 = graph.createD2();
		A v2 = graph.createA();
		graph.createE(v2, v1);
	}

	/**
	 * Test if an error occurs if you try to build an edge with null as alpha.
	 */
	@Test(expected = GraphException.class)
	public void addSourcerolenameTestException2() {
		onlyTestWithoutTransactionSupport();
		B v1 = graph.createB();
		v1.addSourceE(null);
	}

	/**
	 * Test if edges of different types are created via addSourceE and manually.
	 */
	@Test
	public void mixedAddSourcerolenameTest0() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		B v2 = graph.createB();
		C v3 = graph.createC();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		E e1 = v2.addSourceE(v1);
		E e2 = v4.addSourceE(v1);
		F e3 = v4.addSourceF(v3);
		G e4 = v4.addSourceG(v3);
		F e5 = graph.createF(v3, v4);
		E e6 = graph.createE(v1, v4);
		G e7 = graph.createG(v3, v4);
		E e8 = graph.createE(v1, v2);
		E e9 = v5.addSourceE(v1);
		E e10 = v6.addSourceE(v1);
		F e11 = v6.addSourceF(v3);
		G e12 = v6.addSourceG(v3);
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
	public void addSourcerolenameRandomTest0() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		A v7 = graph.createA();
		C v8 = graph.createC();
		C2 v9 = graph.createC2();
		C2 v10 = graph.createC2();
		D2 v11 = graph.createD2();
		D2 v12 = graph.createD2();
		LinkedList<Edge> v1Inci = new LinkedList<Edge>();
		LinkedList<Edge> v2Inci = new LinkedList<Edge>();
		LinkedList<Edge> v3Inci = new LinkedList<Edge>();
		LinkedList<Edge> v4Inci = new LinkedList<Edge>();
		LinkedList<Edge> v5Inci = new LinkedList<Edge>();
		LinkedList<Edge> v6Inci = new LinkedList<Edge>();
		LinkedList<Edge> v7Inci = new LinkedList<Edge>();
		LinkedList<Edge> v8Inci = new LinkedList<Edge>();
		LinkedList<Edge> v9Inci = new LinkedList<Edge>();
		LinkedList<Edge> v10Inci = new LinkedList<Edge>();
		LinkedList<Edge> v11Inci = new LinkedList<Edge>();
		LinkedList<Edge> v12Inci = new LinkedList<Edge>();
		createRandomGraph(false, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci,
				v8Inci, v9Inci, v10Inci, v11Inci, v12Inci);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		deleteRandomEdges(v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci,
				v7Inci, v8Inci, v9Inci, v10Inci, v11Inci, v12Inci);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		createRandomGraph(false, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci,
				v8Inci, v9Inci, v10Inci, v11Inci, v12Inci);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
	}

	/*
	 * 2.2 Test of removeRoleName
	 */

	/**
	 * call removeSourceE when no sourceE exists.
	 */
	@Test
	public void removeSourceRoleNameTest0() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		C v2 = graph.createC();
		D v3 = graph.createD();
		F e1 = v2.addY(v3);
		v3.removeSourceE(v1);
		testIncidenceList(v1);
		testIncidenceList(v2, e1);
		testIncidenceList(v3, e1.getReversedEdge());
	}

	/**
	 * Remove all sourceE of one vertex.
	 */
	@Test
	public void removeSourceRoleNameTest1() {
		onlyTestWithoutTransactionSupport();
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
		v3.removeSourceE(v1);
		testIncidenceList(v1, e3, e6);
		testIncidenceList(v2, e4);
		testIncidenceList(v3, e4.getReversedEdge());
		testIncidenceList(v4, e3.getReversedEdge(), e6.getReversedEdge());
	}

	/**
	 * Test with cyclic edges.
	 */
	@Test
	public void removeSourcerolenameTest2() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		A v2 = graph.createA();
		C v3 = graph.createC();
		I e1 = graph.createI(v1, v1);
		I e2 = v1.addV(v2);
		v2.addV(v2);
		I e4 = graph.createI(v2, v3);
		v2.removeSourceI(v2);
		testIncidenceList(v1, e1, e1.getReversedEdge(), e2);
		testIncidenceList(v2, e2.getReversedEdge(), e4);
		testIncidenceList(v3, e4.getReversedEdge());
	}

	/**
	 * Test if an error occurs if an E-edge is removed via removeSourceE ending
	 * at a D2-vertex.
	 */
	@Test(expected = GraphException.class)
	public void removeSourcerolenameTestException0() {
		onlyTestWithoutTransactionSupport();
		C2 v1 = graph.createC2();
		D2 v2 = graph.createD2();
		v2.removeSourceE(v1);
	}

	/**
	 * Random test
	 */
	@Test
	public void removeSourcerolenameRandomTest0() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		A v7 = graph.createA();
		C v8 = graph.createC();
		C2 v9 = graph.createC2();
		C2 v10 = graph.createC2();
		D2 v11 = graph.createD2();
		D2 v12 = graph.createD2();
		LinkedList<Edge> v1Inci = new LinkedList<Edge>();
		LinkedList<Edge> v2Inci = new LinkedList<Edge>();
		LinkedList<Edge> v3Inci = new LinkedList<Edge>();
		LinkedList<Edge> v4Inci = new LinkedList<Edge>();
		LinkedList<Edge> v5Inci = new LinkedList<Edge>();
		LinkedList<Edge> v6Inci = new LinkedList<Edge>();
		LinkedList<Edge> v7Inci = new LinkedList<Edge>();
		LinkedList<Edge> v8Inci = new LinkedList<Edge>();
		LinkedList<Edge> v9Inci = new LinkedList<Edge>();
		LinkedList<Edge> v10Inci = new LinkedList<Edge>();
		LinkedList<Edge> v11Inci = new LinkedList<Edge>();
		LinkedList<Edge> v12Inci = new LinkedList<Edge>();
		createRandomGraph(false, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci,
				v8Inci, v9Inci, v10Inci, v11Inci, v12Inci);
		deleteAll(false, v1, v3, v1Inci, v3Inci, "sourceE", "sourceH");
		v3.removeSourceE(v1);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12,
				v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci,
				v9Inci, v10Inci, v11Inci, v12Inci);
		deleteAll(false, v1, v4, v1Inci, v4Inci, "sourceE", "sourceH");
		v4.removeSourceE(v1);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12,
				v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci,
				v9Inci, v10Inci, v11Inci, v12Inci);
		deleteAll(false, v7, v6, v7Inci, v6Inci, "sourceH");
		v6.removeSourceH(v7);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12,
				v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci,
				v9Inci, v10Inci, v11Inci, v12Inci);
		deleteAll(false, v9, v11, v9Inci, v11Inci, "sourceJ");
		v11.removeSourceJ(v9);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12,
				v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci,
				v9Inci, v10Inci, v11Inci, v12Inci);
	}

	/*
	 * 2.3 Test of getRoleNameList
	 */

	/**
	 * Test a vertex which has no adjacent sourceE-vertices.
	 */
	@Test
	public void getSourceRoleNameListTest0() {
		onlyTestWithoutTransactionSupport();
		B v1 = graph.createB();
		compareLists(new LinkedList<Vertex>(), v1.getSourceEList().toArray(
				new Vertex[0]));
	}

	/**
	 * Test a vertex which has adjacent sourceH-vertices.
	 */
	@Test
	public void getSourceRoleNameListTest1() {
		onlyTestWithoutTransactionSupport();
		C v1 = graph.createC();
		D v2 = graph.createD();
		v2.addSourceH(v1);
		v2.addSourceF(v1);
		v2.addSourceH(v1);
		v2.addSourceG(v1);
		v2.addSourceH(v1);
		LinkedList<Vertex> expected = new LinkedList<Vertex>();
		expected.add(v1);
		expected.add(v1);
		expected.add(v1);
		compareLists(expected, v2.getSourceHList().toArray(new Vertex[0]));
		expected = new LinkedList<Vertex>();
		expected.add(v1);
		compareLists(expected, v2.getSourceFList().toArray(new Vertex[0]));
		compareLists(expected, v2.getSourceGList().toArray(new Vertex[0]));
	}

	/**
	 * Test with cyclic edges.
	 */
	@Test
	public void getSourceRoleNameListTest2() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		v1.addSourceI(v1);
		graph.createI(v1, v1);
		LinkedList<Vertex> expected = new LinkedList<Vertex>();
		expected.add(v1);
		expected.add(v1);
		compareLists(expected, v1.getSourceIList().toArray(new Vertex[0]));
	}

	/**
	 * Test if an error occurs if you try to get a list of all sourceE-vertices
	 * of one D2-vertex.
	 */
	@Test(expected = GraphException.class)
	public void getSourceRoleNameListTestException0() {
		onlyTestWithoutTransactionSupport();
		D2 v1 = graph.createD2();
		v1.getSourceEList();
	}

	/**
	 * Random test
	 */
	@Test
	public void getSourceRoleNameListRandomTest0() {
		onlyTestWithoutTransactionSupport();
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		A v7 = graph.createA();
		C v8 = graph.createC();
		C2 v9 = graph.createC2();
		C2 v10 = graph.createC2();
		D2 v11 = graph.createD2();
		D2 v12 = graph.createD2();
		LinkedList<Edge> v1Inci = new LinkedList<Edge>();
		LinkedList<Edge> v2Inci = new LinkedList<Edge>();
		LinkedList<Edge> v3Inci = new LinkedList<Edge>();
		LinkedList<Edge> v4Inci = new LinkedList<Edge>();
		LinkedList<Edge> v5Inci = new LinkedList<Edge>();
		LinkedList<Edge> v6Inci = new LinkedList<Edge>();
		LinkedList<Edge> v7Inci = new LinkedList<Edge>();
		LinkedList<Edge> v8Inci = new LinkedList<Edge>();
		LinkedList<Edge> v9Inci = new LinkedList<Edge>();
		LinkedList<Edge> v10Inci = new LinkedList<Edge>();
		LinkedList<Edge> v11Inci = new LinkedList<Edge>();
		LinkedList<Edge> v12Inci = new LinkedList<Edge>();
		createRandomGraph(false, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci,
				v8Inci, v9Inci, v10Inci, v11Inci, v12Inci);

		LinkedList<Vertex> expected = getAllVerticesWithRolename(v1, "sourceI");
		compareLists(expected, v1.getSourceIList().toArray(new Vertex[0]));

		expected = getAllVerticesWithRolename(v3, "sourceE", "sourceH");
		compareLists(expected, v3.getSourceEList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v3, "sourceH");
		compareLists(expected, v3.getSourceHList().toArray(new Vertex[0]));

		expected = getAllVerticesWithRolename(v4, "sourceE", "sourceF",
				"sourceG", "sourceH");
		compareLists(expected, v4.getSourceEList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v4, "sourceF");
		compareLists(expected, v4.getSourceFList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v4, "sourceG");
		compareLists(expected, v4.getSourceGList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v4, "sourceH");
		compareLists(expected, v4.getSourceHList().toArray(new Vertex[0]));

		expected = getAllVerticesWithRolename(v5, "sourceE", "sourceH");
		compareLists(expected, v5.getSourceEList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v5, "sourceH");
		compareLists(expected, v5.getSourceHList().toArray(new Vertex[0]));

		expected = getAllVerticesWithRolename(v6, "sourceE", "sourceF",
				"sourceG", "sourceH");
		compareLists(expected, v6.getSourceEList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v6, "sourceF");
		compareLists(expected, v6.getSourceFList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v6, "sourceG");
		compareLists(expected, v6.getSourceGList().toArray(new Vertex[0]));
		expected = getAllVerticesWithRolename(v6, "sourceH");
		compareLists(expected, v6.getSourceHList().toArray(new Vertex[0]));

		expected = getAllVerticesWithRolename(v7, "sourceI");
		compareLists(expected, v7.getSourceIList().toArray(new Vertex[0]));

		expected = getAllVerticesWithRolename(v11, "sourceJ");
		compareLists(expected, v11.getSourceJList().toArray(new Vertex[0]));

		expected = getAllVerticesWithRolename(v12, "sourceJ");
		compareLists(expected, v12.getSourceJList().toArray(new Vertex[0]));
	}

	/*
	 * 3. Test of illegal rolenames.
	 */

	/*
	 * 3.1 Rolename and subset of rolename.
	 */

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF A--&gt{F}B targetF<br>
	 * All rolenames are unique.
	 */
	@Test
	public void illegalRolenamesTest0() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from A (0,*) role sourceF to B (0,*) role sourceF;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF A--&gt{F:E}B targetF<br>
	 * All rolenames are unique with inheritance.
	 */
	@Test
	public void illegalRolenamesTest1() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F:E from A (0,*) role sourceF to B (0,*) role sourceF;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceE C--&gt{F}D targetE<br>
	 * Same rolenames at different vertices.
	 */
	@Test
	public void illegalRolenamesTest2() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C;"
				+ "VertexClass D;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from C (0,*) role sourceE to D (0,*) role targetE;");
	}

	/**
	 * A--&gt{E}B targetE<br>
	 * A--&gt{F}B targetE<br>
	 * Target rolename are the same and no source rolenames exist.<br>
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest3() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;" + "VertexClass A;" + "VertexClass B;"
				+ "EdgeClass E from A (0,*) to B (0,*) role targetE;"
				+ "EdgeClass F from A (0,*) to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceE A--&gt{F}B targetE<br>
	 * Target and source rolenames are the same.
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest4() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from A (0,*) role sourceE to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF A--&gt{F}B targetE<br>
	 * Target rolename are the same, but source rolenames are different.
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest5() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from A (0,*) role sourceF to B (0,*) role targetE;");
	}

	/**
	 * targetE A--&gt{E}B targetE<br>
	 * Source and target rolename are the same.
	 */
	@Test
	public void illegalRolenamesTest6() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role targetE to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B:A targetE<br>
	 * A cyclic edge with different rolenames.
	 */
	@Test
	public void illegalRolenamesTest7() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B:A;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;");
	}

	/**
	 * targetE A--&gt{E}A targetE<br>
	 * A cyclic edge with the same source and target rolename.
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest8() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "EdgeClass E from A (0,*) role targetE to A (0,*) role targetE;");
	}

	/**
	 * targetE A--&gt{E}B:A targetE<br>
	 * Source and target rolename are the same and the edge ends at a subvertex
	 * of its alpha vertex.
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest9() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B:A;"
				+ "EdgeClass E from A (0,*) role targetE to B (0,*) role targetE;");
	}

	/**
	 * targetE C:A--&gt{E}B:A targetE<br>
	 * Source and target rolename are the same. Alpha and omega are subclasses
	 * of the same vertexclass.
	 */
	@Test
	public void illegalRolenamesTest10() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B:A;"
				+ "VertexClass C:A;"
				+ "EdgeClass E from C (0,*) role targetE to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B:A targetE<br>
	 * sourceE B:A--&gt{F}B:A targetE
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest11() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from B (0,*) role sourceE to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B:A targetE<br>
	 * sourceF B:A--&gt{F}B:A targetE
	 */
	@Test
	public void illegalRolenamesTest12() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from B (0,*) role sourceF to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF C:A--&gt{F:E}D:B targetF<br>
	 */
	@Test
	public void illegalRolenamesTest13() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C:A;"
				+ "VertexClass D:B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F:E from C (0,*) role sourceF to D (0,*) role targetF;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceE C:A--&gt{F:E}D:B targetF<br>
	 */
	@Test
	public void illegalRolenamesTest14() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C:A;"
				+ "VertexClass D:B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F:E from C (0,*) role sourceE to D (0,*) role targetF;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceE C:A--&gt{F:E}D:B targetE<br>
	 *TODO is this wanted??
	 */
	@Test
	public void illegalRolenamesTest15() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C:A;"
				+ "VertexClass D:B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F:E from C (0,*) role sourceE to D (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceE C:A--&gt{F}D:B targetE<br>
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest16() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C:A;"
				+ "VertexClass D:B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from C (0,*) role sourceE to D (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF C--&gt{F}B targetE<br>
	 */
	@Test
	public void illegalRolenamesTest17() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from C (0,*) role sourceF to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceE C--&gt{F}B targetE<br>
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest18() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from C (0,*) role sourceE to B (0,*) role targetE;");
	}

	/*
	 * 3.2 redefines
	 */

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF C--&gt{F}D targetF redefines targetE<br>
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest19() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C;"
				+ "VertexClass D;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from C (0,*) role sourceF to D (0,*) role targetF redefines targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF A--&gt{F}B targetF redefines targetE<br>
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest23() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from A (0,*) role sourceF to B (0,*) role targetF redefines targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF C:A--&gt{F}D:B targetF redefines targetE<br>
	 */
	@Test
	public void illegalRolenamesTest20() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C:A;"
				+ "VertexClass D:B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from C (0,*) role sourceF to D (0,*) role targetF redefines targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF C:A--&gt{F}D:B targetE redefines targetE<br>
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest21() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C:A;"
				+ "VertexClass D:B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from C (0,*) role sourceF to D (0,*) role targetE redefines targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF C--&gt{F}D targetF redefines targetE<br>
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest22() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C;"
				+ "VertexClass D;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from C (0,*) role sourceF to D (0,*) role targetF redefines targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF A--&gt{F:E}B targetF redefines targetE<br>
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest24() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F:E from A (0,*) role sourceF to B (0,*) role targetF redefines targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF C:A--&gt{F:E}D:B targetF redefines targetE<br>
	 */
	@Test
	public void illegalRolenamesTest25() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C:A;"
				+ "VertexClass D:B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F:E from C (0,*) role sourceF to D (0,*) role targetF redefines targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF C:A--&gt{F:E}D:B targetE redefines targetE<br>
	 * <br>
	 * "x redefines x": this is currently OK in JGraLab, may change in future
	 * versions
	 */
	public void illegalRolenamesTest26() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C:A;"
				+ "VertexClass D:B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F:E from C (0,*) role sourceF to D (0,*) role targetE redefines targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF redefines sourceE C--&gt{F}B targetE<br>
	 */
	@Test(expected = InheritanceException.class)
	public void illegalRolenamesTest27() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from C (0,*) role sourceF redefines sourceE to B (0,*) role targetF;");
	}

	/**
	 * a A--&gt{H}B b<br>
	 * x redefines y C:D--&gt{F}B v<br>
	 * y D--&gt{G}E z<br>
	 */
	@Test
	public void illegalRolenamesTest28() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass D;"
				+ "VertexClass C:D;"
				+ "VertexClass E;"
				+ "EdgeClass H from A (0,*) role a to B (0,*) role b;"
				+ "EdgeClass G from D (0,*) role z to E (0,*) role y;"
				+ "EdgeClass F from C (0,*) role v to B (0,*) role x redefines y;");
	}

	/**
	 * d D--&gt{E}A a<br>
	 * b B:D--&gt{F}C c redefines a<br>
	 */
	@Test
	public void illegalRolenamesTest29() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B:D;"
				+ "VertexClass C;"
				+ "VertexClass D;"
				+ "EdgeClass E from D (0,*) role d to A (0,*) role a;"
				+ "EdgeClass F from B (0,*) role b to C (0,*) role c redefines a;");
	}

	/**
	 * Creation of Methods with equal name. TODO
	 */
	@Test
	public void illegalRolenamesTest30() throws Exception {
		onlyTestWithoutTransactionSupport();
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;" + "VertexClass A {xList: Integer};"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) to B (0,*) role x;");
	}
}
