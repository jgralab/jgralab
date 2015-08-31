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
package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Iterator;
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
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphException;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;
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

	public RoleNameTest(ImplementationType implementationType, String dbURL) {
		super(implementationType, dbURL);
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
		switch (implementationType) {
		case STANDARD:
			graph = VertexTestSchema.instance().createVertexTestGraph(
					ImplementationType.STANDARD);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
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
							v3) : useAddTarget ? (start == 0 ? v1 : v7)
							.add_x(v3) : v3.add_sourceE(start == 0 ? v1 : v7);
					(start == 0 ? v1Inci : v7Inci).add(e);
					v3Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createE(start == 0 ? v1 : v7,
							v4) : useAddTarget ? (start == 0 ? v1 : v7)
							.add_x(v4) : v4.add_sourceE(start == 0 ? v1 : v7);
					(start == 0 ? v1Inci : v7Inci).add(e);
					v4Inci.add(e.getReversedEdge());
					break;
				case 2:
					e = howToCreate == 0 ? graph.createE(start == 0 ? v1 : v7,
							v5) : useAddTarget ? (start == 0 ? v1 : v7)
							.add_x(v5) : v5.add_sourceE(start == 0 ? v1 : v7);
					(start == 0 ? v1Inci : v7Inci).add(e);
					v5Inci.add(e.getReversedEdge());
					break;
				case 3:
					e = howToCreate == 0 ? graph.createE(start == 0 ? v1 : v7,
							v6) : useAddTarget ? (start == 0 ? v1 : v7)
							.add_x(v6) : v6.add_sourceE(start == 0 ? v1 : v7);
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
							v4) : useAddTarget ? (start == 0 ? v2 : v8)
							.add_y(v4) : v4.add_sourceF(start == 0 ? v2 : v8);
					(start == 0 ? v2Inci : v8Inci).add(e);
					v4Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createF(start == 0 ? v2 : v8,
							v6) : useAddTarget ? (start == 0 ? v2 : v8)
							.add_y(v6) : v6.add_sourceF(start == 0 ? v2 : v8);
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
							v4) : useAddTarget ? (start == 0 ? v2 : v8)
							.add_z(v4) : v4.add_sourceG(start == 0 ? v2 : v8);
					(start == 0 ? v2Inci : v8Inci).add(e);
					v4Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createG(start == 0 ? v2 : v8,
							v6) : useAddTarget ? (start == 0 ? v2 : v8)
							.add_z(v6) : v6.add_sourceG(start == 0 ? v2 : v8);
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
								: useAddTarget ? v1.add_w(v3) : v3
										.add_sourceH(v1);
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v3)
								: useAddTarget ? v2.add_w(v3) : v3
										.add_sourceH(v2);
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v3)
								: useAddTarget ? v7.add_w(v3) : v3
										.add_sourceH(v7);
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v3)
								: useAddTarget ? v8.add_w(v3) : v3
										.add_sourceH(v8);
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v3)
								: useAddTarget ? v9.add_w(v3) : v3
										.add_sourceH(v9);
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v3)
								: useAddTarget ? v10.add_w(v3) : v3
										.add_sourceH(v10);
						v10Inci.add(e);
						break;
					}
					v3Inci.add(e.getReversedEdge());
					break;
				case 1:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createH(v1, v4)
								: useAddTarget ? v1.add_w(v4) : v4
										.add_sourceH(v1);
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v4)
								: useAddTarget ? v2.add_w(v4) : v4
										.add_sourceH(v2);
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v4)
								: useAddTarget ? v7.add_w(v4) : v4
										.add_sourceH(v7);
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v4)
								: useAddTarget ? v8.add_w(v4) : v4
										.add_sourceH(v8);
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v4)
								: useAddTarget ? v9.add_w(v4) : v4
										.add_sourceH(v9);
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v4)
								: useAddTarget ? v10.add_w(v4) : v4
										.add_sourceH(v10);
						v10Inci.add(e);
						break;
					}
					v4Inci.add(e.getReversedEdge());
					break;
				case 2:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createH(v1, v5)
								: useAddTarget ? v1.add_w(v5) : v5
										.add_sourceH(v1);
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v5)
								: useAddTarget ? v2.add_w(v5) : v5
										.add_sourceH(v2);
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v5)
								: useAddTarget ? v7.add_w(v5) : v5
										.add_sourceH(v7);
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v5)
								: useAddTarget ? v8.add_w(v5) : v5
										.add_sourceH(v8);
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v5)
								: useAddTarget ? v9.add_w(v5) : v5
										.add_sourceH(v9);
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v5)
								: useAddTarget ? v10.add_w(v5) : v5
										.add_sourceH(v10);
						v10Inci.add(e);
						break;
					}
					v5Inci.add(e.getReversedEdge());
					break;
				case 3:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createH(v1, v6)
								: useAddTarget ? v1.add_w(v6) : v6
										.add_sourceH(v1);
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v6)
								: useAddTarget ? v2.add_w(v6) : v6
										.add_sourceH(v2);
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v6)
								: useAddTarget ? v7.add_w(v6) : v6
										.add_sourceH(v7);
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v6)
								: useAddTarget ? v8.add_w(v6) : v6
										.add_sourceH(v8);
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v6)
								: useAddTarget ? v9.add_w(v6) : v6
										.add_sourceH(v9);
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v6)
								: useAddTarget ? v10.add_w(v6) : v6
										.add_sourceH(v10);
						v10Inci.add(e);
						break;
					}
					v6Inci.add(e.getReversedEdge());
					break;
				case 4:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createH(v1, v11)
								: useAddTarget ? v1.add_w(v11) : v11
										.add_sourceH(v1);
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v11)
								: useAddTarget ? v2.add_w(v11) : v11
										.add_sourceH(v2);
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v11)
								: useAddTarget ? v7.add_w(v11) : v11
										.add_sourceH(v7);
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v11)
								: useAddTarget ? v8.add_w(v11) : v11
										.add_sourceH(v8);
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v11)
								: useAddTarget ? v9.add_w(v11) : v11
										.add_sourceH(v9);
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v11)
								: useAddTarget ? v10.add_w(v11) : v11
										.add_sourceH(v10);
						v10Inci.add(e);
						break;
					}
					v11Inci.add(e.getReversedEdge());
					break;
				case 5:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createH(v1, v12)
								: useAddTarget ? v1.add_w(v12) : v12
										.add_sourceH(v1);
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createH(v2, v12)
								: useAddTarget ? v2.add_w(v12) : v12
										.add_sourceH(v2);
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createH(v7, v12)
								: useAddTarget ? v7.add_w(v12) : v12
										.add_sourceH(v7);
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createH(v8, v12)
								: useAddTarget ? v8.add_w(v12) : v12
										.add_sourceH(v8);
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createH(v9, v12)
								: useAddTarget ? v9.add_w(v12) : v12
										.add_sourceH(v9);
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createH(v10, v12)
								: useAddTarget ? v10.add_w(v12) : v12
										.add_sourceH(v10);
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
						e = doMagic(useAddTarget, v1, v1, v1Inci, howToCreate);
						break;
					case 1:
						e = doMagic(useAddTarget, v2, v1, v2Inci, howToCreate);
						break;
					case 2:
						e = doMagic(useAddTarget, v7, v1, v7Inci, howToCreate);
						break;
					case 3:
						e = doMagic(useAddTarget, v8, v1, v8Inci, howToCreate);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createI(v9, v1)
								: useAddTarget ? v9.add_v(v1) : v1
										.add_sourceI(v9);
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createI(v10, v1)
								: useAddTarget ? v10.add_v(v1) : v1
										.add_sourceI(v10);
						v10Inci.add(e);
						break;
					}
					v1Inci.add(e.getReversedEdge());
					break;
				case 1:
					switch (start) {
					case 0:
						e = doMagic(useAddTarget, v1, v2, v1Inci, howToCreate);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createI(v2, v2)
								: useAddTarget ? v2.add_v(v2) : v2
										.add_sourceI(v2);
						v2Inci.add(e);
						break;
					case 2:
						e = doMagic(useAddTarget, v7, v2, v7Inci, howToCreate);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createI(v8, v2)
								: useAddTarget ? v8.add_v(v2) : v2
										.add_sourceI(v8);
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createI(v9, v2)
								: useAddTarget ? v9.add_v(v2) : v2
										.add_sourceI(v9);
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createI(v10, v2)
								: useAddTarget ? v10.add_v(v2) : v2
										.add_sourceI(v10);
						v10Inci.add(e);
						break;
					}
					v2Inci.add(e.getReversedEdge());
					break;
				case 2:
					switch (start) {
					case 0:
						e = doMagic(useAddTarget, v1, v7, v1Inci, howToCreate);
						break;
					case 1:
						e = doMagic(useAddTarget, v2, v7, v2Inci, howToCreate);
						break;
					case 2:
						e = doMagic(useAddTarget, v7, v7, v7Inci, howToCreate);
						break;
					case 3:
						e = doMagic(useAddTarget, v8, v7, v8Inci, howToCreate);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createI(v9, v7)
								: useAddTarget ? v9.add_v(v7) : v7
										.add_sourceI(v9);
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createI(v10, v7)
								: useAddTarget ? v10.add_v(v7) : v7
										.add_sourceI(v10);
						v10Inci.add(e);
						break;
					}
					v7Inci.add(e.getReversedEdge());
					break;
				case 3:
					switch (start) {
					case 0:
						e = doMagic(useAddTarget, v1, v8, v1Inci, howToCreate);
						break;
					case 1:
						e = doMagic(useAddTarget, v2, v8, v2Inci, howToCreate);
						break;
					case 2:
						e = doMagic(useAddTarget, v7, v8, v7Inci, howToCreate);
						break;
					case 3:
						e = doMagic(useAddTarget, v8, v8, v8Inci, howToCreate);
						break;
					case 4:
						e = doMagic(useAddTarget, v9, v8, v9Inci, howToCreate);
						break;
					case 5:
						e = doMagic(useAddTarget, v10, v8, v10Inci, howToCreate);
						break;
					}
					v8Inci.add(e.getReversedEdge());
					break;
				case 4:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createI(v1, v9)
								: useAddTarget ? v1.add_v(v9) : v9
										.add_sourceI(v1);
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createI(v2, v9)
								: useAddTarget ? v2.add_v(v9) : v9
										.add_sourceI(v2);
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createI(v9, v9)
								: useAddTarget ? v9.add_v(v9) : v9
										.add_sourceI(v9);
						v9Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createI(v8, v9)
								: useAddTarget ? v8.add_v(v9) : v9
										.add_sourceI(v8);
						v8Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createI(v9, v9)
								: useAddTarget ? v9.add_v(v9) : v9
										.add_sourceI(v9);
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createI(v10, v9)
								: useAddTarget ? v10.add_v(v9) : v9
										.add_sourceI(v10);
						v10Inci.add(e);
						break;
					}
					v9Inci.add(e.getReversedEdge());
					break;
				case 5:
					switch (start) {
					case 0:
						e = howToCreate == 0 ? graph.createI(v1, v10)
								: useAddTarget ? v1.add_v(v10) : v10
										.add_sourceI(v1);
						v1Inci.add(e);
						break;
					case 1:
						e = howToCreate == 0 ? graph.createI(v2, v10)
								: useAddTarget ? v2.add_v(v10) : v10
										.add_sourceI(v2);
						v2Inci.add(e);
						break;
					case 2:
						e = howToCreate == 0 ? graph.createI(v7, v10)
								: useAddTarget ? v7.add_v(v10) : v10
										.add_sourceI(v7);
						v7Inci.add(e);
						break;
					case 3:
						e = howToCreate == 0 ? graph.createI(v10, v10)
								: useAddTarget ? v10.add_v(v10) : v10
										.add_sourceI(v10);
						v10Inci.add(e);
						break;
					case 4:
						e = howToCreate == 0 ? graph.createI(v9, v10)
								: useAddTarget ? v9.add_v(v10) : v10
										.add_sourceI(v9);
						v9Inci.add(e);
						break;
					case 5:
						e = howToCreate == 0 ? graph.createI(v10, v10)
								: useAddTarget ? v10.add_v(v10) : v10
										.add_sourceI(v10);
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
							v11) : useAddTarget ? (start == 0 ? v9 : v10)
							.add_u(v11) : v11
							.add_sourceJ(start == 0 ? v9 : v10);
					(start == 0 ? v9Inci : v10Inci).add(e);
					v11Inci.add(e.getReversedEdge());
					break;
				case 1:
					e = howToCreate == 0 ? graph.createJ(start == 0 ? v9 : v10,
							v12) : useAddTarget ? (start == 0 ? v9 : v10)
							.add_u(v12) : v12
							.add_sourceJ(start == 0 ? v9 : v10);
					(start == 0 ? v9Inci : v10Inci).add(e);
					v12Inci.add(e.getReversedEdge());
					break;
				}
			}
		}
	}

	private I doMagic(boolean useAddTarget, A v1, A v8,
			LinkedList<Edge> v1Inci, int howToCreate) {
		I e;
		e = howToCreate == 0 ? graph.createI(v1, v8) : useAddTarget ? v1
				.add_v(v8) : v8.add_sourceI(v1);
		v1Inci.add(e);
		return e;
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
			if (e.getAlpha() == from
					&& e.getOmega() == to
					&& checkRoleName(
							useTarget ? e.getThatRole() : e.getThisRole(),
							rolenames)) {
				inciFrom.remove(i--);
			}
		}
		for (int i = 0; i < inciTo.size(); i++) {
			Edge e = inciTo.get(i).getNormalEdge();
			if (e.getAlpha() == from
					&& e.getOmega() == to
					&& checkRoleName(
							useTarget ? e.getThatRole() : e.getThisRole(),
							rolenames)) {
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
		LinkedList<Vertex> ret = new LinkedList<>();
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
		LinkedList<Vertex> ret = new LinkedList<>();
		PriorityQueue<Edge> edges = new PriorityQueue<>();
		for (Edge e : graph.edges()) {
			if (e.getOmega() == to && checkRoleName(e.getThisRole(), rolenames)) {
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
	private void compareLists(List<Vertex> expected,
			Iterable<? extends Vertex> actual) {
		Iterator<? extends Vertex> it = actual.iterator();
		for (int i = 0; i < expected.size(); i++) {
			assertTrue(it.hasNext());
			assertEquals(expected.get(i), it.next());
		}
		assertFalse(it.hasNext());
	}

	/**
	 * Compiles the schema defined in schemaString.
	 * 
	 * @param schemaString
	 * @return the schema
	 * @throws GraphIOException
	 */
	private Schema compileSchema(String schemaString) throws GraphIOException {
		ByteArrayInputStream input = new ByteArrayInputStream(
				schemaString.getBytes());
		Schema s = null;
		s = GraphIO.loadSchemaFromStream(input);
		s.compile(CodeGeneratorConfiguration.NORMAL);
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
	 * 
	 * @
	 */
	@Test
	public void eAddTargetrolenameTest0() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		D v3 = graph.createD();
		E e1 = v1.add_x(v2);
		E e2 = v1.add_x(v2);
		E e3 = v1.add_x(v3);
		E e4 = v1.add_x(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(),
				e4.getReversedEdge());
		testIncidenceList(v3, e3.getReversedEdge());
	}

	/**
	 * Test if only edges of one type are created via addX and manually.
	 * 
	 * @
	 */
	@Test
	public void eAddTargetrolenameTest1() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		E e1 = v1.add_x(v2);
		E e2 = graph.createE(v1, v2);
		E e3 = v1.add_x(v2);
		E e4 = v1.add_x(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(),
				e3.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test with cyclic edges.
	 * 
	 * @
	 */
	@Test
	public void eAddTargetrolenameTest2() {
		A v1 = graph.createA();
		A v2 = graph.createA();
		C v3 = graph.createC();
		I e1 = graph.createI(v1, v1);
		I e2 = v1.add_v(v2);
		I e3 = v2.add_v(v2);
		I e4 = graph.createI(v2, v3);
		testIncidenceList(v1, e1, e1.getReversedEdge(), e2);
		testIncidenceList(v2, e2.getReversedEdge(), e3, e3.getReversedEdge(),
				e4);
		testIncidenceList(v3, e4.getReversedEdge());
	}

	/**
	 * Test if only edges of one type are created via addX.
	 * 
	 * @
	 */
	@Test
	public void fAddTargetrolenameTest0() {
		C v1 = graph.createC();
		D v2 = graph.createD();
		F e1 = v1.add_y(v2);
		F e2 = v1.add_y(v2);
		F e3 = v1.add_y(v2);
		F e4 = v1.add_y(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(),
				e3.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test if only edges of one type are created via addX and manually.
	 * 
	 * @
	 */
	@Test
	public void fAddTargetrolenameTest1() {
		C v1 = graph.createC();
		D v2 = graph.createD();
		F e1 = v1.add_y(v2);
		F e2 = graph.createF(v1, v2);
		F e3 = v1.add_y(v2);
		F e4 = v1.add_y(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(),
				e3.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test if only edges of one type are created via addX.
	 * 
	 * @
	 */
	@Test
	public void gAddTargetrolenameTest0() {
		C v1 = graph.createC();
		D v2 = graph.createD();
		G e1 = v1.add_z(v2);
		G e2 = v1.add_z(v2);
		G e3 = v1.add_z(v2);
		G e4 = v1.add_z(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(),
				e3.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test if only edges of one type are created via addX and manually.
	 * 
	 * @
	 */
	@Test
	public void gAddTargetrolenameTest1() {
		C v1 = graph.createC();
		D v2 = graph.createD();
		G e1 = v1.add_z(v2);
		G e2 = graph.createG(v1, v2);
		G e3 = v1.add_z(v2);
		G e4 = v1.add_z(v2);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(),
				e3.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test if edges of different types are created via addX and manually.
	 * 
	 * @
	 */
	@Test
	public void mixedAddTargetrolenameTest0() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		C v3 = graph.createC();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		E e1 = v1.add_x(v2);
		E e2 = v1.add_x(v4);
		F e3 = v3.add_y(v4);
		G e4 = v3.add_z(v4);
		F e5 = graph.createF(v3, v4);
		E e6 = graph.createE(v1, v4);
		G e7 = graph.createG(v3, v4);
		E e8 = graph.createE(v1, v2);
		E e9 = v1.add_x(v5);
		E e10 = v1.add_x(v6);
		F e11 = v3.add_y(v6);
		G e12 = v3.add_z(v6);
		testIncidenceList(v1, e1, e2, e6, e8, e9, e10);
		testIncidenceList(v2, e1.getReversedEdge(), e8.getReversedEdge());
		testIncidenceList(v3, e3, e4, e5, e7, e11, e12);
		testIncidenceList(v4, e2.getReversedEdge(), e3.getReversedEdge(),
				e4.getReversedEdge(), e5.getReversedEdge(),
				e6.getReversedEdge(), e7.getReversedEdge());
		testIncidenceList(v5, e9.getReversedEdge());
		testIncidenceList(v6, e10.getReversedEdge(), e11.getReversedEdge(),
				e12.getReversedEdge());
	}

	/**
	 * Random test
	 * 
	 * @
	 */
	@Test
	public void addTargetrolenameRandomTest0() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		A v7 = graph.createA();
		C v8 = graph.createC();
		LinkedList<Edge> v1Inci = new LinkedList<>();
		LinkedList<Edge> v2Inci = new LinkedList<>();
		LinkedList<Edge> v3Inci = new LinkedList<>();
		LinkedList<Edge> v4Inci = new LinkedList<>();
		LinkedList<Edge> v5Inci = new LinkedList<>();
		LinkedList<Edge> v6Inci = new LinkedList<>();
		LinkedList<Edge> v7Inci = new LinkedList<>();
		LinkedList<Edge> v8Inci = new LinkedList<>();
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
	 * 
	 * @
	 */
	@Test
	public void removeTargetRoleNameTest0() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		D v3 = graph.createD();
		F e1 = v2.add_y(v3);
		v1.remove_x(v3);
		testIncidenceList(v1);
		testIncidenceList(v2, e1);
		testIncidenceList(v3, e1.getReversedEdge());
	}

	/**
	 * Remove all x of one vertex.
	 * 
	 * @
	 */
	@Test
	public void removeTargetRoleNameTest1() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		D v3 = graph.createD();
		B v4 = graph.createB();
		v1.add_x(v3);
		v1.add_w(v3);
		E e3 = v1.add_x(v4);
		F e4 = v2.add_y(v3);
		graph.createE(v1, v3);
		H e6 = v1.add_w(v4);
		v1.remove_x(v3);
		testIncidenceList(v1, e3, e6);
		testIncidenceList(v2, e4);
		testIncidenceList(v3, e4.getReversedEdge());
		testIncidenceList(v4, e3.getReversedEdge(), e6.getReversedEdge());
	}

	/**
	 * Test with cyclic edges.
	 * 
	 * @
	 */
	@Test
	public void removeTargetrolenameTest2() {
		A v1 = graph.createA();
		A v2 = graph.createA();
		C v3 = graph.createC();
		I e1 = graph.createI(v1, v1);
		I e2 = v1.add_v(v2);
		v2.add_v(v2);
		I e4 = graph.createI(v2, v3);
		v2.remove_v(v2);
		testIncidenceList(v1, e1, e1.getReversedEdge(), e2);
		testIncidenceList(v2, e2.getReversedEdge(), e4);
		testIncidenceList(v3, e4.getReversedEdge());
	}

	/**
	 * Test if an error occurs if an E-edge is removed via removeX starting at a
	 * C-vertex.
	 * 
	 * There should occur no exception and removeX should return 'false'.
	 * 
	 * @
	 */
	@Test
	public void removeTargetRolenameTest0() {

		C v1 = graph.createC();
		B v2 = graph.createB();

		assertFalse(v1.remove_x(v2));

	}

	/**
	 * Test if an error occurs if an E-edge is removed via removeX starting at a
	 * C-vertex.
	 * 
	 * There should occur no exception and removeX should return 'false'.
	 * 
	 * @
	 */
	@Test
	public void removeTargetRolenameTest3() {

		C2 v1 = graph.createC2();
		B v2 = graph.createB();
		assertFalse(v1.remove_x(v2));

	}

	/**
	 * Random test
	 * 
	 * @
	 */
	@Test
	public void removeTargetrolenameRandomTest0() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		A v7 = graph.createA();
		C v8 = graph.createC();
		LinkedList<Edge> v1Inci = new LinkedList<>();
		LinkedList<Edge> v2Inci = new LinkedList<>();
		LinkedList<Edge> v3Inci = new LinkedList<>();
		LinkedList<Edge> v4Inci = new LinkedList<>();
		LinkedList<Edge> v5Inci = new LinkedList<>();
		LinkedList<Edge> v6Inci = new LinkedList<>();
		LinkedList<Edge> v7Inci = new LinkedList<>();
		LinkedList<Edge> v8Inci = new LinkedList<>();
		createRandomGraph(true, v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci,
				v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		deleteAll(true, v1, v3, v1Inci, v3Inci, "x", "w");
		v1.remove_x(v3);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		deleteAll(true, v1, v4, v1Inci, v4Inci, "x", "w");
		v1.remove_x(v4);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		deleteAll(true, v7, v6, v7Inci, v6Inci, "w");
		v7.remove_w(v6);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci, v3Inci,
				v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
	}

	/*
	 * 1.3 Test of getRoleNameList
	 */

	/**
	 * Test a vertex which has no adjacent x-vertices.
	 * 
	 * @
	 */
	@Test
	public void getRoleNameListTest0() {
		A v1 = graph.createA();
		compareLists(new LinkedList<Vertex>(), v1.get_x());
	}

	/**
	 * Test a vertex which has adjacent w-vertices.
	 * 
	 * @
	 */
	@Test
	public void getRoleNameListTest1() {
		C v1 = graph.createC();
		D v2 = graph.createD();
		v1.add_w(v2);
		v1.add_y(v2);
		v1.add_w(v2);
		v1.add_z(v2);
		v1.add_w(v2);

		LinkedList<Vertex> expected = new LinkedList<>();
		expected.add(v2);
		expected.add(v2);
		expected.add(v2);
		compareLists(expected, v1.get_w());
		expected = new LinkedList<>();
		expected.add(v2);
		compareLists(expected, v1.get_y());
		compareLists(expected, v1.get_z());
	}

	/**
	 * Test with cyclic edges.
	 * 
	 * @
	 */
	@Test
	public void getRoleNameListTest2() {
		A v1 = graph.createA();
		v1.add_v(v1);
		graph.createI(v1, v1);
		LinkedList<Vertex> expected = new LinkedList<>();
		expected.add(v1);
		expected.add(v1);
		compareLists(expected, v1.get_v());
	}

	/**
	 * Random test
	 * 
	 * @
	 */
	@Test
	public void getRoleNameListRandomTest0() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		B v3 = graph.createB();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		A v7 = graph.createA();
		C v8 = graph.createC();
		LinkedList<Edge> v1Inci = new LinkedList<>();
		LinkedList<Edge> v2Inci = new LinkedList<>();
		LinkedList<Edge> v3Inci = new LinkedList<>();
		LinkedList<Edge> v4Inci = new LinkedList<>();
		LinkedList<Edge> v5Inci = new LinkedList<>();
		LinkedList<Edge> v6Inci = new LinkedList<>();
		LinkedList<Edge> v7Inci = new LinkedList<>();
		LinkedList<Edge> v8Inci = new LinkedList<>();
		createRandomGraph(true, v1, v2, v3, v4, v5, v6, v7, v8, v1Inci, v2Inci,
				v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci);
		LinkedList<Vertex> expected = getAllVerticesWithRolename(v1Inci, "x",
				"w");
		compareLists(expected, v1.get_x());
		expected = getAllVerticesWithRolename(v1Inci, "w");
		compareLists(expected, v1.get_w());
		expected = getAllVerticesWithRolename(v1Inci, "v");
		compareLists(expected, v1.get_v());

		expected = getAllVerticesWithRolename(v2Inci, "w");
		compareLists(expected, v2.get_w());
		expected = getAllVerticesWithRolename(v2Inci, "v");
		compareLists(expected, v2.get_v());
		expected = getAllVerticesWithRolename(v2Inci, "y");
		compareLists(expected, v2.get_y());
		expected = getAllVerticesWithRolename(v2Inci, "z");
		compareLists(expected, v2.get_z());

		expected = getAllVerticesWithRolename(v7Inci, "x", "w");
		compareLists(expected, v7.get_x());
		expected = getAllVerticesWithRolename(v7Inci, "w");
		compareLists(expected, v7.get_w());
		expected = getAllVerticesWithRolename(v7Inci, "v");
		compareLists(expected, v7.get_v());

		expected = getAllVerticesWithRolename(v8Inci, "w");
		compareLists(expected, v8.get_w());
		expected = getAllVerticesWithRolename(v8Inci, "v");
		compareLists(expected, v8.get_v());
		expected = getAllVerticesWithRolename(v8Inci, "y");
		compareLists(expected, v8.get_y());
		expected = getAllVerticesWithRolename(v8Inci, "z");
		compareLists(expected, v8.get_z());
	}

	/*
	 * 2. Test of source rolename.
	 */

	/*
	 * 2.1 Test of addRoleName
	 */

	/**
	 * Test if only edges of one type are created via addSourceE.
	 * 
	 * @
	 */
	@Test
	public void addSourcerolenameTest0() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		D v3 = graph.createD();
		E e1 = v2.add_sourceE(v1);
		E e2 = v2.add_sourceE(v1);
		E e3 = v3.add_sourceE(v1);
		E e4 = v2.add_sourceE(v1);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(),
				e4.getReversedEdge());
		testIncidenceList(v3, e3.getReversedEdge());
	}

	/**
	 * Test if only edges of one type are created via addSourceE and manually.
	 * 
	 * @
	 */
	@Test
	public void addSourcerolenameTest1() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		E e1 = v2.add_sourceE(v1);
		E e2 = graph.createE(v1, v2);
		E e3 = v2.add_sourceE(v1);
		E e4 = v2.add_sourceE(v1);
		testIncidenceList(v1, e1, e2, e3, e4);
		testIncidenceList(v2, e1.getReversedEdge(), e2.getReversedEdge(),
				e3.getReversedEdge(), e4.getReversedEdge());
	}

	/**
	 * Test with cyclic edges.
	 * 
	 * @
	 */
	@Test
	public void addSourcerolenameTest2() {
		A v1 = graph.createA();
		A v2 = graph.createA();
		C v3 = graph.createC();
		I e1 = graph.createI(v1, v1);
		I e2 = v2.add_sourceI(v1);
		I e3 = v2.add_sourceI(v2);
		I e4 = graph.createI(v2, v3);
		testIncidenceList(v1, e1, e1.getReversedEdge(), e2);
		testIncidenceList(v2, e2.getReversedEdge(), e3, e3.getReversedEdge(),
				e4);
		testIncidenceList(v3, e4.getReversedEdge());
	}

	/**
	 * Test if an error occurs if you try to build an edge with null as alpha.
	 * 
	 * @
	 */
	@Test(expected = GraphException.class)
	public void addSourcerolenameTestException2() {
		B v1 = graph.createB();
		v1.add_sourceE(null);
	}

	/**
	 * Test if edges of different types are created via addSourceE and manually.
	 * 
	 * @
	 */
	@Test
	public void mixedAddSourcerolenameTest0() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		C v3 = graph.createC();
		D v4 = graph.createD();
		B v5 = graph.createB();
		D v6 = graph.createD();
		E e1 = v2.add_sourceE(v1);
		E e2 = v4.add_sourceE(v1);
		F e3 = v4.add_sourceF(v3);
		G e4 = v4.add_sourceG(v3);
		F e5 = graph.createF(v3, v4);
		E e6 = graph.createE(v1, v4);
		G e7 = graph.createG(v3, v4);
		E e8 = graph.createE(v1, v2);
		E e9 = v5.add_sourceE(v1);
		E e10 = v6.add_sourceE(v1);
		F e11 = v6.add_sourceF(v3);
		G e12 = v6.add_sourceG(v3);
		testIncidenceList(v1, e1, e2, e6, e8, e9, e10);
		testIncidenceList(v2, e1.getReversedEdge(), e8.getReversedEdge());
		testIncidenceList(v3, e3, e4, e5, e7, e11, e12);
		testIncidenceList(v4, e2.getReversedEdge(), e3.getReversedEdge(),
				e4.getReversedEdge(), e5.getReversedEdge(),
				e6.getReversedEdge(), e7.getReversedEdge());
		testIncidenceList(v5, e9.getReversedEdge());
		testIncidenceList(v6, e10.getReversedEdge(), e11.getReversedEdge(),
				e12.getReversedEdge());
	}

	/**
	 * Random test
	 * 
	 * @
	 */
	@Test
	public void addSourcerolenameRandomTest0() {
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
		LinkedList<Edge> v1Inci = new LinkedList<>();
		LinkedList<Edge> v2Inci = new LinkedList<>();
		LinkedList<Edge> v3Inci = new LinkedList<>();
		LinkedList<Edge> v4Inci = new LinkedList<>();
		LinkedList<Edge> v5Inci = new LinkedList<>();
		LinkedList<Edge> v6Inci = new LinkedList<>();
		LinkedList<Edge> v7Inci = new LinkedList<>();
		LinkedList<Edge> v8Inci = new LinkedList<>();
		LinkedList<Edge> v9Inci = new LinkedList<>();
		LinkedList<Edge> v10Inci = new LinkedList<>();
		LinkedList<Edge> v11Inci = new LinkedList<>();
		LinkedList<Edge> v12Inci = new LinkedList<>();
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
	 * 
	 * @
	 */
	@Test
	public void removeSourceRoleNameTest0() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		D v3 = graph.createD();
		F e1 = v2.add_y(v3);
		v3.remove_sourceE(v1);
		testIncidenceList(v1);
		testIncidenceList(v2, e1);
		testIncidenceList(v3, e1.getReversedEdge());
	}

	/**
	 * Remove all sourceE of one vertex.
	 * 
	 * @
	 */
	@Test
	public void removeSourceRoleNameTest1() {
		A v1 = graph.createA();
		C v2 = graph.createC();
		D v3 = graph.createD();
		B v4 = graph.createB();
		v1.add_x(v3);
		v1.add_w(v3);
		E e3 = v1.add_x(v4);
		F e4 = v2.add_y(v3);
		graph.createE(v1, v3);
		H e6 = v1.add_w(v4);
		v3.remove_sourceE(v1);
		testIncidenceList(v1, e3, e6);
		testIncidenceList(v2, e4);
		testIncidenceList(v3, e4.getReversedEdge());
		testIncidenceList(v4, e3.getReversedEdge(), e6.getReversedEdge());
	}

	/**
	 * Test with cyclic edges.
	 * 
	 * @
	 */
	@Test
	public void removeSourcerolenameTest2() {
		A v1 = graph.createA();
		A v2 = graph.createA();
		C v3 = graph.createC();
		I e1 = graph.createI(v1, v1);
		I e2 = v1.add_v(v2);
		v2.add_v(v2);
		I e4 = graph.createI(v2, v3);
		v2.remove_sourceI(v2);
		testIncidenceList(v1, e1, e1.getReversedEdge(), e2);
		testIncidenceList(v2, e2.getReversedEdge(), e4);
		testIncidenceList(v3, e4.getReversedEdge());
	}

	/**
	 * Random test
	 * 
	 * @
	 */
	@Test
	public void removeSourcerolenameRandomTest0() {
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
		LinkedList<Edge> v1Inci = new LinkedList<>();
		LinkedList<Edge> v2Inci = new LinkedList<>();
		LinkedList<Edge> v3Inci = new LinkedList<>();
		LinkedList<Edge> v4Inci = new LinkedList<>();
		LinkedList<Edge> v5Inci = new LinkedList<>();
		LinkedList<Edge> v6Inci = new LinkedList<>();
		LinkedList<Edge> v7Inci = new LinkedList<>();
		LinkedList<Edge> v8Inci = new LinkedList<>();
		LinkedList<Edge> v9Inci = new LinkedList<>();
		LinkedList<Edge> v10Inci = new LinkedList<>();
		LinkedList<Edge> v11Inci = new LinkedList<>();
		LinkedList<Edge> v12Inci = new LinkedList<>();
		createRandomGraph(false, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci,
				v8Inci, v9Inci, v10Inci, v11Inci, v12Inci);
		deleteAll(false, v1, v3, v1Inci, v3Inci, "sourceE", "sourceH");
		v3.remove_sourceE(v1);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12,
				v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci,
				v9Inci, v10Inci, v11Inci, v12Inci);
		deleteAll(false, v1, v4, v1Inci, v4Inci, "sourceE", "sourceH");
		v4.remove_sourceE(v1);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12,
				v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci,
				v9Inci, v10Inci, v11Inci, v12Inci);
		deleteAll(false, v7, v6, v7Inci, v6Inci, "sourceH");
		v6.remove_sourceH(v7);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12,
				v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci,
				v9Inci, v10Inci, v11Inci, v12Inci);
		deleteAll(false, v9, v11, v9Inci, v11Inci, "sourceJ");
		v11.remove_sourceJ(v9);
		testIncidences(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12,
				v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci, v8Inci,
				v9Inci, v10Inci, v11Inci, v12Inci);
	}

	/*
	 * 2.3 Test of getRoleNameList
	 */

	/**
	 * Test a vertex which has no adjacent sourceE-vertices.
	 * 
	 * @
	 */
	@Test
	public void getSourceRoleNameListTest0() {
		B v1 = graph.createB();
		compareLists(new LinkedList<Vertex>(), v1.get_sourceE());
	}

	/**
	 * Test a vertex which has adjacent sourceH-vertices.
	 * 
	 * @
	 */
	@Test
	public void getSourceRoleNameListTest1() {
		C v1 = graph.createC();
		D v2 = graph.createD();
		v2.add_sourceH(v1);
		v2.add_sourceF(v1);
		v2.add_sourceH(v1);
		v2.add_sourceG(v1);
		v2.add_sourceH(v1);
		LinkedList<Vertex> expected = new LinkedList<>();
		expected.add(v1);
		expected.add(v1);
		expected.add(v1);
		compareLists(expected, v2.get_sourceH());
		expected = new LinkedList<>();
		expected.add(v1);
		compareLists(expected, v2.get_sourceF());
		compareLists(expected, v2.get_sourceG());
	}

	/**
	 * Test with cyclic edges.
	 * 
	 * @
	 */
	@Test
	public void getSourceRoleNameListTest2() {
		A v1 = graph.createA();
		v1.add_sourceI(v1);
		graph.createI(v1, v1);
		LinkedList<Vertex> expected = new LinkedList<>();
		expected.add(v1);
		expected.add(v1);
		compareLists(expected, v1.get_sourceI());
	}

	/**
	 * Random test
	 * 
	 * @
	 */
	@Test
	public void getSourceRoleNameListRandomTest0() {
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
		LinkedList<Edge> v1Inci = new LinkedList<>();
		LinkedList<Edge> v2Inci = new LinkedList<>();
		LinkedList<Edge> v3Inci = new LinkedList<>();
		LinkedList<Edge> v4Inci = new LinkedList<>();
		LinkedList<Edge> v5Inci = new LinkedList<>();
		LinkedList<Edge> v6Inci = new LinkedList<>();
		LinkedList<Edge> v7Inci = new LinkedList<>();
		LinkedList<Edge> v8Inci = new LinkedList<>();
		LinkedList<Edge> v9Inci = new LinkedList<>();
		LinkedList<Edge> v10Inci = new LinkedList<>();
		LinkedList<Edge> v11Inci = new LinkedList<>();
		LinkedList<Edge> v12Inci = new LinkedList<>();
		createRandomGraph(false, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v1Inci, v2Inci, v3Inci, v4Inci, v5Inci, v6Inci, v7Inci,
				v8Inci, v9Inci, v10Inci, v11Inci, v12Inci);

		LinkedList<Vertex> expected = getAllVerticesWithRolename(v1, "sourceI");
		compareLists(expected, v1.get_sourceI());

		expected = getAllVerticesWithRolename(v3, "sourceE", "sourceH");
		compareLists(expected, v3.get_sourceE());
		expected = getAllVerticesWithRolename(v3, "sourceH");
		compareLists(expected, v3.get_sourceH());

		expected = getAllVerticesWithRolename(v4, "sourceE", "sourceF",
				"sourceG", "sourceH");
		compareLists(expected, v4.get_sourceE());
		expected = getAllVerticesWithRolename(v4, "sourceF");
		compareLists(expected, v4.get_sourceF());
		expected = getAllVerticesWithRolename(v4, "sourceG");
		compareLists(expected, v4.get_sourceG());
		expected = getAllVerticesWithRolename(v4, "sourceH");
		compareLists(expected, v4.get_sourceH());

		expected = getAllVerticesWithRolename(v5, "sourceE", "sourceH");
		compareLists(expected, v5.get_sourceE());
		expected = getAllVerticesWithRolename(v5, "sourceH");
		compareLists(expected, v5.get_sourceH());

		expected = getAllVerticesWithRolename(v6, "sourceE", "sourceF",
				"sourceG", "sourceH");
		compareLists(expected, v6.get_sourceE());
		expected = getAllVerticesWithRolename(v6, "sourceF");
		compareLists(expected, v6.get_sourceF());
		expected = getAllVerticesWithRolename(v6, "sourceG");
		compareLists(expected, v6.get_sourceG());
		expected = getAllVerticesWithRolename(v6, "sourceH");
		compareLists(expected, v6.get_sourceH());

		expected = getAllVerticesWithRolename(v7, "sourceI");
		compareLists(expected, v7.get_sourceI());

		expected = getAllVerticesWithRolename(v11, "sourceJ");
		compareLists(expected, v11.get_sourceJ());

		expected = getAllVerticesWithRolename(v12, "sourceJ");
		compareLists(expected, v12.get_sourceJ());
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
	 * 
	 * All rolenames are unique. There should be no exception.
	 */
	@Test
	public void legalRolenamesTest0() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from A (0,*) role sourceF to B (0,*) role sourceF;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF A--&gt{F:E}B targetF<br>
	 * 
	 * All rolenames are unique with inheritance. This test should succeed.
	 */
	@Test
	public void legalRolenamesTest1() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F:E from A (0,*) role sourceF to B (0,*) role sourceF;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceE C--&gt{F}D targetE<br>
	 * 
	 * Same rolenames at different vertices. This test should succeed.
	 */
	@Test
	public void legalRolenamesTest2() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
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
	 * 
	 * Target rolename are the same and no source rolenames exist.<br>
	 * This test fails, because at vertex A the rolename 'targetE' does not
	 * tell, if you have an edge of type E or F.
	 */
	@Test(expected = GraphIOException.class)
	public void illegalRolenamesTest3() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;" + "VertexClass A;" + "VertexClass B;"
				+ "EdgeClass E from A (0,*) to B (0,*) role targetE;"
				+ "EdgeClass F from A (0,*) to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceE A--&gt{F}B targetE<br>
	 * 
	 * Target and source rolenames are the same.
	 */
	@Test(expected = GraphIOException.class)
	public void illegalRolenamesTest4() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from A (0,*) role sourceE to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF A--&gt{F}B targetE<br>
	 * 
	 * Target rolename are the same, but source rolenames are different.
	 */
	@Test(expected = GraphIOException.class)
	public void illegalRolenamesTest5() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from A (0,*) role sourceF to B (0,*) role targetE;");
	}

	/**
	 * targetE A--&gt{E}B targetE<br>
	 * 
	 * Source and target rolename are the same.
	 */
	@Test
	public void legalRolenamesTest6() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role targetE to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B:A targetE<br>
	 * 
	 * A cyclic edge with different rolenames.
	 */
	@Test
	public void legalRolenamesTest7() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B:A;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;");
	}

	/**
	 * targetE A--&gt{E}A targetE<br>
	 * 
	 * A cyclic edge with the same source and target rolename.
	 */
	@Test(expected = GraphIOException.class)
	public void illegalRolenamesTest8() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "EdgeClass E from A (0,*) role targetE to A (0,*) role targetE;");
	}

	/**
	 * targetE A--&gt{E}B:A targetE<br>
	 * 
	 * Source and target rolename are the same and the edge ends at a subvertex
	 * of its alpha vertex.
	 */
	@Test(expected = GraphIOException.class)
	public void illegalRolenamesTest9() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B:A;"
				+ "EdgeClass E from A (0,*) role targetE to B (0,*) role targetE;");
	}

	/**
	 * targetE C:A--&gt{E}B:A targetE<br>
	 * Source and target rolename are the same. Alpha and omega are subclasses
	 * of the same vertexclass.
	 * 
	 * This schema should be valid.
	 */
	@Test
	public void legalRolenamesTest10() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B:A;"
				+ "VertexClass C:A;"
				+ "EdgeClass E from C (0,*) role targetE to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B:A targetE<br>
	 * sourceE B:A--&gt{F}B:A targetE
	 * 
	 * This schema should be valid.
	 */
	@Test(expected = GraphIOException.class)
	public void illegalRolenamesTest11() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from B (0,*) role sourceE to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B:A targetE<br>
	 * sourceF B:A--&gt{F}B:A targetE
	 * 
	 * This schema should be valid.
	 */
	@Test
	public void legalRolenamesTest12() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from B (0,*) role sourceF to B (0,*) role targetE;");
	}

	/**
	 * sourceE A--&gt{E}B targetE<br>
	 * sourceF C:A--&gt{F:E}D:B targetF<br>
	 * 
	 * This schema should be valid.
	 */
	@Test
	public void legalRolenamesTest13() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
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
	 * 
	 * There can only be one rolename in the inheritance tree of a vertex. In
	 * this case B and D.
	 */
	@Test(expected = GraphIOException.class)
	public void illegalRolenamesTest14() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
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
	 * 
	 * A rolename cannot be defined twice in the same inheritance tree.
	 */
	@Test(expected = GraphIOException.class)
	public void illegalRolenamesTest15() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
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
	 * 
	 * A rolename cannot be defined twice in the same inheritance tree.
	 */
	@Test(expected = GraphIOException.class)
	public void illegalRolenamesTest16() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
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
	 * 
	 * A rolename cannot be defined twice in the same inheritance tree.
	 */
	@Test
	public void legalRolenamesTest17() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
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
	 * 
	 * A rolename cannot be defined twice for the same vertex. Remember: A
	 * rolename will become a attribute in the opposite vertex
	 */
	@Test(expected = GraphIOException.class)
	public void illegalRolenamesTest18() throws GraphIOException {
		compileSchema("TGraph 2;"
				+ "Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;"
				+ "VertexClass A;"
				+ "VertexClass B;"
				+ "VertexClass C;"
				+ "EdgeClass E from A (0,*) role sourceE to B (0,*) role targetE;"
				+ "EdgeClass F from C (0,*) role sourceE to B (0,*) role targetE;");
	}
}
