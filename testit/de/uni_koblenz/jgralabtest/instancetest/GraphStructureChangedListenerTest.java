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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphStructureChangedAdapter;
import de.uni_koblenz.jgralab.GraphStructureChangedAdapterWithAutoRemove;
import de.uni_koblenz.jgralab.GraphStructureChangedListener;
import de.uni_koblenz.jgralab.GraphStructureChangedListenerWithAutoRemove;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

@RunWith(Parameterized.class)
public class GraphStructureChangedListenerTest extends InstanceTest {

	private static final int LISTENERS = 10;

	public GraphStructureChangedListenerTest(
			ImplementationType implementationType, String dbURL) {
		super(implementationType, dbURL);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	protected final int V = 4; // initial max vertex count
	protected final int E = 4; // initial max edge count

	private class TestListener extends GraphStructureChangedAdapter {

		protected void trigger() {
			trigger2 = true;
		}

		@Override
		public void vertexAdded(Vertex v) {
			trigger();
			assertTrue("The given vertex has not been added to the graph.",
					v.isValid() && (v.getGraph() == g));
		}

		@Override
		public void vertexDeleted(Vertex v) {
			trigger();
			assertTrue("The given vertex is not valid, but it should be.",
					v.isValid() && (v.getGraph() == g));
		}

		@Override
		public void edgeAdded(Edge e) {
			trigger();
			assertTrue("The given edge has not been added to the graph",
					e.isValid() && (e.getGraph() == g));
		}

		@Override
		public void edgeDeleted(Edge e) {
			trigger();
			assertTrue("The given edge is not valid, but it should be.",
					e.isValid() && (e.getGraph() == g));
		}

		@Override
		public void maxVertexCountIncreased(int newValue) {
			trigger();
			assertTrue(
					"The vertex count of the graph does not match the new vertex count",
					newValue == ((InternalGraph) g).getMaxVCount());
		}

		@Override
		public void maxEdgeCountIncreased(int newValue) {
			trigger();
			assertTrue(
					"The edge count of the graph does not match the new vertex count",
					newValue == ((InternalGraph) g).getMaxECount());
		}

	}

	private class TestListenerWithAutoRemove extends TestListener implements
			GraphStructureChangedListenerWithAutoRemove {
		@Override
		protected void trigger() {
			trigger1 = true;
		}
	}

	private MinimalGraph g;

	@Before
	public void setup() {
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(
					ImplementationType.STANDARD, null, V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		trigger1 = false;
		trigger2 = false;
	}

	private boolean trigger1;
	private boolean trigger2;

	@Test
	public void testVertexAdded() {

		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();

		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);

		g.createNode();

		assertTrue("The method \"vertexAdded\" has not been called.", trigger1);
		assertTrue("The method  \"vertexAdded\" has not been called.", trigger2);

	}

	@Test
	public void testVertexDeleted() {
		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();

		assertFalse("The trigger has not been resetted", trigger1);
		assertFalse("The trigger has not been resetted", trigger2);

		Node n = g.createNode();

		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);

		g.deleteVertex(n);

		assertTrue("The method \"vertexDeleted\" has not been called.",
				trigger1);
		assertTrue("The method \"vertexDeleted\" has not been called.",
				trigger2);
	}

	@Test
	public void testEdgeAdded() {
		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();

		Node n1 = g.createNode();
		Node n2 = g.createNode();

		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);

		assertFalse("The trigger has not been resetted", trigger1);
		assertFalse("The trigger has not been resetted", trigger2);

		g.createLink(n1, n2);

		assertTrue("The method \"edgeAdded\" has not been called.", trigger1);
		assertTrue("The method \"edgeAdded\" has not been called.", trigger2);
	}

	@Test
	public void testEdgeDeleted() {
		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();

		assertFalse("The trigger has not been resetted", trigger1);
		assertFalse("The trigger has not been resetted", trigger2);

		Node n1 = g.createNode();
		Node n2 = g.createNode();
		Link l1 = g.createLink(n1, n2);

		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);

		g.deleteEdge(l1);

		assertTrue("The method \"edgeDeleted\" has not been called.", trigger1);
		assertTrue("The method \"edgeDeleted\" has not been called.", trigger2);
	}

	@Test
	public void testMaxVertexCountIncreased() {
		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();

		for (int i = 0; i < V; i++) {
			g.createNode();
		}

		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);

		assertFalse("The trigger has not been resetted", trigger1);
		assertFalse("The trigger has not been resetted", trigger2);

		g.createNode();

		assertTrue(
				"The method \"maxVertexCountIncreased\" has not been called.",
				trigger1);
	}

	@Test
	public void testMaxEdgeCountIncreased() {
		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();

		Node n1 = g.createNode();
		Node n2 = g.createNode();
		for (int i = 0; i < E; i++) {
			g.createLink(n1, n2);
		}

		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);

		assertFalse("The trigger has not been resetted", trigger1);

		g.createLink(n1, n2);

		assertTrue("The method \"maxEdgeCountIncreased\" has not been called.",
				trigger1);

	}

	@Test
	public void testAutomaticRemovalOfWeakReferences() {
		GraphStructureChangedListener[] listenersWithAutoRemove = new GraphStructureChangedListener[LISTENERS];
		GraphStructureChangedListener[] normalListeners = new GraphStructureChangedListener[LISTENERS];
		for (int i = 0; i < listenersWithAutoRemove.length; i++) {
			listenersWithAutoRemove[i] = new GraphStructureChangedAdapterWithAutoRemove() {
			};
			normalListeners[i] = new GraphStructureChangedAdapter() {
			};
			g.addGraphStructureChangedListener(listenersWithAutoRemove[i]);
			g.addGraphStructureChangedListener(normalListeners[i]);
		}

		// test explicit unregister
		assertTrue("The wrong amount of listeners was created.",
				g.getGraphStructureChangedListenerCount() == LISTENERS * 2);

		g.removeGraphStructureChangedListener(listenersWithAutoRemove[1]);
		g.removeGraphStructureChangedListener(normalListeners[1]);

		assertEquals(LISTENERS * 2 - 2,
				g.getGraphStructureChangedListenerCount());

		// test implicit unregister
		listenersWithAutoRemove[0] = null;
		normalListeners[0] = null;

		System.gc();
		// wait a second
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		// invoke implicit notification
		g.createNode();

		// only the auto removal one is expected to be deleted
		assertEquals(LISTENERS * 2 - 3,
				g.getGraphStructureChangedListenerCount());

		for (int i = 0; i < LISTENERS; i++) {
			listenersWithAutoRemove[i] = null;
			normalListeners[i] = null;
		}
		System.gc();
		// wait a second
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		// invoke implicit notification
		g.createNode();

		assertEquals(LISTENERS - 1, g.getGraphStructureChangedListenerCount());
	}
}
