/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralabtest.algolib.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.KahnKnuthAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.ArrayPermutation;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleEdge;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class BreadthFirstSearchTest extends GraphAlgorithmTest {

	private SimpleGraph g;
	private SearchVisitor normalvisitor;
	// private SearchVisitor reversedVisitor;
	// private SearchVisitor undirectedVisitor;
	private List<SimpleEdge> normalTreeEdges;
	// private List<SimpleEdge> reverseTreeEdges;
	// private List<SimpleEdge> undirectedTreeEdges;
	private List<SimpleEdge> normalFronds;
	// private List<SimpleEdge> reversedFronds;
	// private List<SimpleEdge> undirectedFronds;

	SimpleVertex v1;
	SimpleVertex v2;
	SimpleVertex v3;
	SimpleVertex v4;
	SimpleEdge e1;
	SimpleEdge e2;
	SimpleEdge e3;
	SimpleEdge e4;
	SimpleEdge e5;
	SimpleEdge e6;
	BreadthFirstSearch bfs;

	@Before
	public void setUp() {
		normalTreeEdges = new LinkedList<SimpleEdge>();
		normalFronds = new LinkedList<SimpleEdge>();

		g = SimpleSchema.instance().createSimpleGraph();
		v1 = g.createSimpleVertex();
		v2 = g.createSimpleVertex();
		v3 = g.createSimpleVertex();
		v4 = g.createSimpleVertex();
		normalTreeEdges.add(e1 = g.createSimpleEdge(v1, v2));
		normalTreeEdges.add(e2 = g.createSimpleEdge(v1, v3));
		normalTreeEdges.add(e3 = g.createSimpleEdge(v1, v4));
		normalFronds.add(e4 = g.createSimpleEdge(v2, v4));
		normalFronds.add(e5 = g.createSimpleEdge(v3, v2));
		normalFronds.add(e6 = g.createSimpleEdge(v4, v1));
		normalvisitor = new SearchVisitorAdapter() {

			@Override
			public void visitFrond(Edge e) throws AlgorithmTerminatedException {
				assertTrue(normalFronds.contains(e));
			}

			@Override
			public void visitRoot(Vertex v) throws AlgorithmTerminatedException {
				assertEquals(v1, v);
			}

			@Override
			public void visitTreeEdge(Edge e)
					throws AlgorithmTerminatedException {
				assertTrue(normalTreeEdges.contains(e));
				assertTrue(algorithm.getVisitedVertices().get(e.getThis()));
				assertFalse(algorithm.getVisitedVertices().get(e.getThat()));
			}

			@Override
			public void visitEdge(Edge e) throws AlgorithmTerminatedException {
				assertFalse(algorithm.getVisitedEdges().get(e));
			}

			@Override
			public void visitVertex(Vertex v)
					throws AlgorithmTerminatedException {
				assertFalse(algorithm.getVisitedVertices().get(v));
			}

		};

		// reversedVisitor = new SearchVisitorAdapter(){
		// @Override
		// public void visitFrond(Edge e) throws AlgorithmTerminatedException {
		// assertTrue(normalFronds.contains(e));
		// }
		//
		// @Override
		// public void visitRoot(Vertex v) throws AlgorithmTerminatedException {
		// assertEquals(v1, v);
		// }
		//
		// @Override
		// public void visitTreeEdge(Edge e)
		// throws AlgorithmTerminatedException {
		// assertTrue(normalTreeEdges.contains(e));
		// assertTrue(algorithm.getVisitedVertices()
		// .get(e.getThis()));
		// assertFalse(algorithm.getVisitedVertices().get(
		// e.getThat()));
		// }
		//
		// @Override
		// public void visitEdge(Edge e) throws AlgorithmTerminatedException {
		// assertFalse(algorithm.getVisitedEdges().get(e));
		// }
		//
		// @Override
		// public void visitVertex(Vertex v)
		// throws AlgorithmTerminatedException {
		// assertFalse(algorithm.getVisitedVertices().get(v));
		// }
		// };

		bfs = new BreadthFirstSearch(g);
	}

	@Override
	@Test(expected = IllegalArgumentException.class)
	public void testAddVisitorForException() {
		KahnKnuthAlgorithm kk = new KahnKnuthAlgorithm(g);
		kk.addVisitor(normalvisitor);
	}

	@Override
	@Test
	public void testAlgorithm() {

		bfs.addVisitor(normalvisitor);
		// run algorithm
		try {
			bfs.withNumber().withLevel().withParent().execute(v1);
		} catch (AlgorithmTerminatedException e) {
			fail("Early termination not expected");
		}

		checkResultsForNormal();

		// TODO test with subgraph enabled
		SubGraphMarker subgraph = new SubGraphMarker(g);
		for (Vertex current : g.vertices()) {
			subgraph.mark(current);
		}
		for (Edge current : g.edges()) {
			subgraph.mark(current);
		}

		// TODO test with navigable enabled
		// TODO test with both enabled

	}

	private void checkResultsForNormal() {
		// set expected results
		Permutation<Vertex> expectedVertexOrder = new ArrayPermutation<Vertex>(
				new Vertex[] { null, v1, v2, v3, v4 });
		Permutation<Edge> expectedEdgeOrder = new ArrayPermutation<Edge>(
				new Edge[] { null, e1, e2, e3, e4, e5, e6 });
		Function<Vertex, Edge> expectedParent = new ArrayVertexMarker<Edge>(g);
		expectedParent.set(v1, null);
		expectedParent.set(v2, e1);
		expectedParent.set(v3, e2);
		expectedParent.set(v4, e3);
		IntFunction<Vertex> expectedLevel = new IntegerVertexMarker(g);
		expectedLevel.set(v1, 0);
		expectedLevel.set(v2, 1);
		expectedLevel.set(v3, 1);
		expectedLevel.set(v4, 1);
		IntFunction<Vertex> expectedNumber = new IntegerVertexMarker(g);
		expectedNumber.set(v1, 1);
		expectedNumber.set(v2, 2);
		expectedNumber.set(v3, 3);
		expectedNumber.set(v4, 4);

		// evaluate results
		Iterator<Vertex> vi1 = expectedVertexOrder.getRangeElements()
				.iterator();
		Iterator<Vertex> vi2 = bfs.getVertexOrder().getRangeElements()
				.iterator();

		while (vi1.hasNext()) {
			assertTrue(vi2.hasNext());
			assertEquals(vi1.next(), vi2.next());
		}

		Iterator<Edge> ei1 = expectedEdgeOrder.getRangeElements().iterator();
		Iterator<Edge> ei2 = bfs.getEdgeOrder().getRangeElements().iterator();
		while (ei1.hasNext()) {
			assertTrue(ei2.hasNext());
			assertEquals(ei1.next(), ei2.next());
		}

		IntFunction<Vertex> resultNumber = bfs.getNumber();
		assertNotNull(resultNumber);
		for (Vertex current : expectedNumber.getDomainElements()) {
			assertEquals(expectedNumber.get(current), resultNumber.get(current));
		}

		IntFunction<Vertex> resultLevel = bfs.getLevel();
		assertNotNull(resultLevel);
		for (Vertex current : expectedLevel.getDomainElements()) {
			assertEquals(expectedLevel.get(current), resultLevel.get(current));
		}

		Function<Vertex, Edge> resultParent = bfs.getParent();
		assertNotNull(resultParent);
		for (Vertex current : expectedParent.getDomainElements()) {
			assertEquals(expectedParent.get(current), resultParent.get(current));
		}
	}

	@Override
	public void testCancel() {

	}

	@Override
	public void testEarlyTermination() {
		// TODO Auto-generated method stub

	}

	@Override
	public void testGetEdgeCount() {
		// TODO Auto-generated method stub

	}

	@Override
	public void testGetVertexCount() {
		// TODO Auto-generated method stub

	}

	@Override
	public void testRemoveVisitor() {
		// TODO Auto-generated method stub

	}

	@Override
	public void testReset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void testResetParameters() {
		// TODO Auto-generated method stub

	}

	@Override
	public void testStates() {
		// TODO Auto-generated method stub

	}

}
