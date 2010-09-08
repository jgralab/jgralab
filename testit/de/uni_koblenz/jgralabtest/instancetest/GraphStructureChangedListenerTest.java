/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
import de.uni_koblenz.jgralab.GraphStructureChangedAdapterWithAutoRevome;
import de.uni_koblenz.jgralab.GraphStructureChangedListener;
import de.uni_koblenz.jgralab.GraphStructureChangedListenerWithAutoRemove;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

@RunWith(Parameterized.class)
public class GraphStructureChangedListenerTest extends InstanceTest {

	private static final int LISTENERS = 10;

	public GraphStructureChangedListenerTest(
			ImplementationType implementationType) {
		super(implementationType);
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
			assertTrue("The given vertex has not been added to the graph.", v
					.isValid()
					&& (v.getGraph() == g));
		}

		@Override
		public void vertexDeleted(Vertex v) {
			trigger();
			assertTrue("The given vertex is not valid, but it should be.", v
					.isValid()
					&& (v.getGraph() == g));
		}

		@Override
		public void edgeAdded(Edge e) {
			trigger();
			assertTrue("The given edge has not been added to the graph", e
					.isValid()
					&& (e.getGraph() == g));
		}
		
		@Override
		public void edgeDeleted(Edge e) {
			trigger();
			assertTrue("The given edge is not valid, but it should be.", e
					.isValid()
					&& (e.getGraph() == g));
		}
		
		@Override
		public void maxVertexCountIncreased(int newValue) {
			trigger();
			assertTrue(
					"The vertex count of the graph does not match the new vertex count",
					newValue == g.getMaxVCount());
		}
		
		@Override
		public void maxEdgeCountIncreased(int newValue) {
			trigger();
			assertTrue(
					"The edge count of the graph does not match the new vertex count",
					newValue == g.getMaxECount());
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
	public void setup() throws CommitFailedException {
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(V, E);
			break;
		case TRANSACTION:
			g = MinimalSchema.instance()
					.createMinimalGraphWithTransactionSupport(V, E);
			break;
		case SAVEMEM:
			g = MinimalSchema.instance().createMinimalGraphWithSavememSupport(
					V, E);
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
	public void testVertexAdded() throws CommitFailedException {

		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();

		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);

		createTransaction(g);
		g.createNode();
		commit(g);

		assertTrue("The method \"vertexAdded\" has not been called.", trigger1);
		assertTrue("The method  \"vertexAdded\" has not been called.", trigger2);

	}

	@Test
	public void testVertexDeleted() throws CommitFailedException {
		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();
		
		assertFalse("The trigger has not been resetted", trigger1);
		assertFalse("The trigger has not been resetted", trigger2);
		
		createTransaction(g);
		Node n = g.createNode();
		commit(g);

		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);

		createTransaction(g);
		g.deleteVertex(n);
		commit(g);

		assertTrue("The method \"vertexDeleted\" has not been called.",
				trigger1);
		assertTrue("The method \"vertexDeleted\" has not been called.",
				trigger2);
	}

	@Test
	public void testEdgeAdded() throws CommitFailedException {
		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();
		
		createTransaction(g);
		Node n1 = g.createNode();
		Node n2 = g.createNode();
		commit(g);
		
		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);

		assertFalse("The trigger has not been resetted", trigger1);
		assertFalse("The trigger has not been resetted", trigger2);

		createTransaction(g);
		g.createLink(n1, n2);
		commit(g);

		assertTrue("The method \"edgeAdded\" has not been called.", trigger1);
		assertTrue("The method \"edgeAdded\" has not been called.", trigger2);
	}

	@Test
	public void testEdgeDeleted() throws CommitFailedException {
		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();
		
		assertFalse("The trigger has not been resetted", trigger1);
		assertFalse("The trigger has not been resetted", trigger2);
		
		createTransaction(g);
		Node n1 = g.createNode();
		Node n2 = g.createNode();
		Link l1 = g.createLink(n1, n2);
		commit(g);
	
		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);
			
		createTransaction(g);
		g.deleteEdge(l1);
		commit(g);

		assertTrue("The method \"edgeDeleted\" has not been called.", trigger1);
		assertTrue("The method \"edgeDeleted\" has not been called.", trigger2);
	}

	@Test
	public void testMaxVertexCountIncreased() throws CommitFailedException {
		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();
		
		createTransaction(g);
		for (int i = 0; i < V; i++) {
			g.createNode();
		}
		commit(g);
		
		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);

		
		assertFalse("The trigger has not been resetted", trigger1);
		assertFalse("The trigger has not been resetted", trigger2);

		createTransaction(g);
		g.createNode();
		commit(g);

		assertTrue(
				"The method \"maxVertexCountIncreased\" has not been called.",
				trigger1);
	}

	@Test
	public void testMaxEdgeCountIncreased() throws CommitFailedException {
		GraphStructureChangedListener listener1 = new TestListenerWithAutoRemove();
		GraphStructureChangedListener listener2 = new TestListener();
		
		createTransaction(g);
		Node n1 = g.createNode();
		Node n2 = g.createNode();
		for (int i = 0; i < E; i++) {
			g.createLink(n1, n2);
		}
		commit(g);
		
		g.addGraphStructureChangedListener(listener1);
		g.addGraphStructureChangedListener(listener2);
		
		assertFalse("The trigger has not been resetted", trigger1);

		createTransaction(g);
		g.createLink(n1, n2);
		commit(g);

		assertTrue("The method \"maxEdgeCountIncreased\" has not been called.",
				trigger1);

	}

	@Test
	public void testAutomaticRemovalOfWeakReferences()
			throws CommitFailedException {
		GraphStructureChangedListener[] listenersWithAutoRemove = new GraphStructureChangedListener[LISTENERS];
		GraphStructureChangedListener[] normalListeners = new GraphStructureChangedListener[LISTENERS];
		for (int i = 0; i < listenersWithAutoRemove.length; i++) {
			listenersWithAutoRemove[i] = new GraphStructureChangedAdapterWithAutoRevome() {
			};
			normalListeners[i] = new GraphStructureChangedAdapter() {
			};
			g.addGraphStructureChangedListener(listenersWithAutoRemove[i]);
			g.addGraphStructureChangedListener(normalListeners[i]);
		}

		// test explicit unregister
		createReadOnlyTransaction(g);
		assertTrue("The wrong amount of listeners was created.", g
				.getGraphStructureChangedListenerCount() == LISTENERS * 2);
		commit(g);

		g.removeGraphStructureChangedListener(listenersWithAutoRemove[1]);
		g.removeGraphStructureChangedListener(normalListeners[1]);

		createReadOnlyTransaction(g);
		assertEquals(LISTENERS * 2 - 2, g
				.getGraphStructureChangedListenerCount());
		commit(g);

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
		createTransaction(g);
		g.createNode();
		commit(g);

		createReadOnlyTransaction(g);
		// only the auto removal one is expected to be deleted
		assertEquals(LISTENERS * 2 - 3, g
				.getGraphStructureChangedListenerCount());
		commit(g);

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
		createTransaction(g);
		g.createNode();
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals(LISTENERS - 1, g.getGraphStructureChangedListenerCount());
		commit(g);
	}
}
