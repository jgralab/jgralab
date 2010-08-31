package de.uni_koblenz.jgralab.impl.trans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralab.trans.ListPosition;
import de.uni_koblenz.jgralab.trans.VersionedDataObject;
import de.uni_koblenz.jgralab.trans.VertexPosition;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.City;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.Exit;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.Motorway;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMap;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMapSchema;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.TestEnum;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.TestRecord;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.impl.trans.CityImpl;

/**
 * Test cases for class TransactionImpl.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 * 
 *         TODO think about, if protected-methods should really be tested
 */
public class TransactionImplTest {
	private MotorwayMap motorwayMap;
	private TransactionImpl readWriteTransaction1;
	private TransactionImpl readWriteTransaction2;
	private TransactionImpl readOnlyTransaction;

	private City c1;
	private City c2;
	private City c3;

	private Motorway mw1;
	private Motorway mw2;

	private Exit ex1;
	private Exit ex2;
	private Exit ex3;

	private static final int V = 10;
	private static final int E = 10;

	@Before
	public void setUp() throws InterruptedException {
		motorwayMap = MotorwayMapSchema.instance()
				.createMotorwayMapWithTransactionSupport(V, E);

		readWriteTransaction1 = (TransactionImpl) motorwayMap.newTransaction();
		readOnlyTransaction = (TransactionImpl) motorwayMap
				.newReadOnlyTransaction();
		motorwayMap.setCurrentTransaction(readWriteTransaction1);
	}

	@After
	public void tearDown() {
		motorwayMap = null;
		readWriteTransaction1 = null;
		readOnlyTransaction = null;
	}

	/**
	 * New transaction <code>readWriteTransaction2</code> is created and set as
	 * active transaction for <code>motorwayMap</code>. Trying to commit
	 * <code>readWriteTransaction1</code> should then fail, because
	 * <code>readWriteTransaction1</code> is not active in current thread.
	 */
	@Test
	public void testInvalidCommit() {
		try {
			readWriteTransaction2 = (TransactionImpl) motorwayMap
					.newTransaction();
			readWriteTransaction1.commit();
			fail();
		} catch (GraphException e) {
			System.out.println("\n- testInvalidCommit -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		} catch (CommitFailedException e) {
			fail();
		}
	}

	/**
	 * New transaction <code>readWriteTransaction2</code> is created and set as
	 * active transaction for <code>motorwayMap</code>. Trying to abort
	 * <code>readWriteTransaction1</code> should then fail, because
	 * <code>readWriteTransaction1</code> is not active in current thread.
	 */
	@Test
	public void testInvalidAbort() {
		try {
			readWriteTransaction2 = (TransactionImpl) motorwayMap
					.newTransaction();
			readWriteTransaction1.abort();
			fail();
		} catch (GraphException e) {
			System.out.println("\n- testInvalidAbort -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * After the commit of <code>readWriteTransaction1</code> no transaction is
	 * active in current thread. Executing an operation on
	 * <code>motorwayMap</code> should throw a GraphException.
	 */
	@Test
	public void testNoCurrentTransaction() {
		try {
			readWriteTransaction1.commit();
			motorwayMap.getFirstVertex();
			fail();
		} catch (GraphException e) {
			System.out.println("\n- testNoCurrentTransaction -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		} catch (CommitFailedException e) {
			fail();
		}
	}

	/**
	 * Add vertices in <code>readWriteTransaction1</code>. They should be all in
	 * List <code>readWriteTransaction1</code>.addedVertices.
	 */
	@Test
	public void testAddVertex() {
		c1 = motorwayMap.createCity();
		c2 = motorwayMap.createCity();
		c3 = motorwayMap.createCity();
		mw1 = motorwayMap.createMotorway();

		List<Vertex> addedVerticesList = new ArrayList<Vertex>();
		addedVerticesList.add(c1);
		addedVerticesList.add(c2);
		addedVerticesList.add(c3);
		addedVerticesList.add(mw1);
		assertEquals(readWriteTransaction1.addedVertices, addedVerticesList);
	}

	/**
	 * Add vertex in <code>readOnlyTransaction</code>. This is not allowed, so a
	 * GraphException is expected to be thrown.
	 */
	@Test
	public void testAddVertexReadOnly() {
		try {
			motorwayMap.setCurrentTransaction(readOnlyTransaction);
			c1 = motorwayMap.createCity();
			fail();
		} catch (GraphException e) {
			System.out.println("\n- testAddVertexReadOnly -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Add edges in <code>readWriteTransaction1</code>. They should be all in
	 * List <code>readWriteTransaction1</code>.addedEdges.
	 */
	@Test
	public void testAddEdge() {
		testAddVertex();
		ex1 = motorwayMap.createExit(c1, mw1);
		ex2 = motorwayMap.createExit(c1, mw1);
		ex3 = motorwayMap.createExit(c1, mw1);

		List<Edge> addedEdgesList = new ArrayList<Edge>();
		addedEdgesList.add(ex1);
		addedEdgesList.add(ex2);
		addedEdgesList.add(ex3);
		assertEquals(readWriteTransaction1.addedEdges, addedEdgesList);
	}

	/**
	 * Add edge in <code>readOnlyTransaction</code>. This is not allowed, so a
	 * GraphException is expected to be thrown.
	 */
	@Test
	public void testAddEdgeReadOnly() {
		try {
			testAddVertex();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			ex1 = motorwayMap.createExit(c1, mw1);
			fail();
		} catch (GraphException e) {
			System.out.println("\n- testAddVertexReadOnly -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {

		}
	}

	/**
	 * Delete all vertices which were added in
	 * <code>readWriteTransaction1</code>. None of them should appear in List
	 * <code>readWriteTransaction1</code>.deletedVertices.
	 */
	@Test
	public void test1DeleteVertex() {
		testAddVertex();
		c1.delete();
		c2.delete();
		c3.delete();
		// Should be null, because deleteVertices should only be initialized, if
		// at least one already existing vertex is deleted.
		assertNull(readWriteTransaction1.deletedVertices);
	}

	/**
	 * Delete all vertices in <code>readWriteTransaction2</code>. All of them
	 * should appear in List <code>readWriteTransaction2</code>
	 * .deletedVertices.
	 */
	@Test
	public void test2DeleteVertex() {
		testAddVertex();
		try {
			// first commit changes...
			motorwayMap.commit();
			// so that added vertices are valid in
			// <code>readWriteTransaction2</code> from beginning
			readWriteTransaction2 = (TransactionImpl) motorwayMap
					.newTransaction();
			c1.delete();
			c2.delete();
			c3.delete();

			List<Vertex> deletedVerticesList = new ArrayList<Vertex>();
			deletedVerticesList.add(c1);
			deletedVerticesList.add(c2);
			deletedVerticesList.add(c3);
			assertEquals(readWriteTransaction2.deletedVertices,
					deletedVerticesList);
		} catch (CommitFailedException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Trying to delete an already existing vertex in
	 * <code>readOnlyTransaction</code>. This is not allowed, so a
	 * GraphException is expected to be thrown.
	 */
	@Test
	public void testDeleteVertexReadOnly() {
		try {
			testAddVertex();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			c1.delete();
			fail();
		} catch (GraphException ge) {
			System.out.println("\n- testDeleteVertexReadOnly -");
			System.out.println("##########################");
			System.out.println(ge.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {
			fail();
		}
	}

	/**
	 * Delete all edges which were added in <code>readWriteTransaction1</code>.
	 * None of them should appear in List <code>readWriteTransaction1</code>
	 * .deletedEdges.
	 */
	@Test
	public void test1DeleteEdge() {
		testAddEdge();
		ex1.delete();
		ex2.delete();
		ex3.delete();
		// Should be null, because deleteEdges should only be initialized, if
		// at least one already existing edge is deleted.
		assertNull(readWriteTransaction1.deletedEdges);
	}

	/**
	 * Delete all vertices in <code>readWriteTransaction2</code>. All of them
	 * should appear in List <code>readWriteTransaction2</code>
	 * .deletedVertices.
	 */
	@Test
	public void test2DeleteEdge() {
		testAddEdge();
		try {
			// first commit changes...
			motorwayMap.commit();
			// so that added vertices are valid in
			// <code>readWriteTransaction2</code> from beginning
			readWriteTransaction2 = (TransactionImpl) motorwayMap
					.newTransaction();
			ex1.delete();
			ex2.delete();
			ex3.delete();

			List<Edge> deletedEdgesList = new ArrayList<Edge>();
			deletedEdgesList.add(ex1);
			deletedEdgesList.add(ex2);
			deletedEdgesList.add(ex3);
			assertEquals(readWriteTransaction2.deletedEdges, deletedEdgesList);
		} catch (CommitFailedException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Trying to delete an already existing vertex in
	 * <code>readOnlyTransaction</code>. This is not allowed, so a
	 * GraphException is expected to be thrown.
	 */
	@Test
	public void testDeleteEdgeReadOnly() {
		try {
			testAddEdge();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			ex1.delete();
			fail();
		} catch (GraphException ge) {
			System.out.println("\n- testDeleteEdgeReadOnly -");
			System.out.println("##########################");
			System.out.println(ge.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {
			fail();
		}
	}

	/**
	 * Vertex c1 is put after c2 in Vseq within
	 * <code>readWriteTransaction1</code>. c1 is movedVertex with previous
	 * vertex changed, while c2 is targetVertex with next vertex changed. This
	 * should be remarked in <code>readWriteTransaction1</code>
	 * .changedVseqVertices.
	 */
	@Test
	public void testPutVertexAfter() {
		testAddVertex();
		c1.putAfter(c2);

		Map<Vertex, Map<ListPosition, Boolean>> changedVseqVerticesMap = new HashMap<Vertex, Map<ListPosition, Boolean>>();
		Map<ListPosition, Boolean> changedPosition1 = new HashMap<ListPosition, Boolean>();
		changedPosition1.put(ListPosition.PREV, true);
		Map<ListPosition, Boolean> changedPosition2 = new HashMap<ListPosition, Boolean>();
		changedPosition2.put(ListPosition.NEXT, false);
		changedVseqVerticesMap.put(c1, changedPosition1);
		changedVseqVerticesMap.put(c2, changedPosition2);
		assertEquals(readWriteTransaction1.changedVseqVertices,
				changedVseqVerticesMap);
	}

	/**
	 * Vertex c1 is put after c2 in Vseq within <code>readOnlyTransaction</code>
	 * . This is not allowed, so a GraphException is expected to be thrown.
	 */
	@Test
	public void testPutVertexAfterReadOnly() {
		try {
			testAddVertex();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			c1.putAfter(c2);
			fail();
		} catch (GraphException ge) {
			System.out.println("\n- testPutVertexAfterReadOnly -");
			System.out.println("##########################");
			System.out.println(ge.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {
			fail();
		}
	}

	/**
	 * Vertex c2 is put before c2 in Vseq within
	 * <code>readWriteTransaction1</code>. c2 is movedVertex with next vertex
	 * changed, while c1 is targetVertex with previous vertex changed. This
	 * should be remarked in <code>readWriteTransaction1</code>
	 * .changedVseqVertices.
	 */
	@Test
	public void testPutVertexBefore() {
		testAddVertex();
		c2.putBefore(c1);

		Map<Vertex, Map<ListPosition, Boolean>> changedVseqVerticesMap = new HashMap<Vertex, Map<ListPosition, Boolean>>();
		Map<ListPosition, Boolean> changedPosition1 = new HashMap<ListPosition, Boolean>();
		changedPosition1.put(ListPosition.NEXT, true);
		Map<ListPosition, Boolean> changedPosition2 = new HashMap<ListPosition, Boolean>();
		changedPosition2.put(ListPosition.PREV, false);
		changedVseqVerticesMap.put(c2, changedPosition1);
		changedVseqVerticesMap.put(c1, changedPosition2);
		assertEquals(readWriteTransaction1.changedVseqVertices,
				changedVseqVerticesMap);
	}

	/**
	 * Vertex c2 is put before c1 in Vseq within
	 * <code>readOnlyTransaction</code> . This is not allowed, so a
	 * GraphException is expected to be thrown.
	 */
	@Test
	public void testPutVertexBeforeReadOnly() {
		try {
			testAddVertex();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			c2.putBefore(c1);
			fail();
		} catch (GraphException ge) {
			System.out.println("\n- testPutVertexBeforeReadOnly -");
			System.out.println("##########################");
			System.out.println(ge.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {
			fail();
		}
	}

	/**
	 * Edge ex1 is put after ex2 in Eseq within
	 * <code>readWriteTransaction1</code>. ex1 is movedEdge with previous edge
	 * changed, while ex2 is targetEdge with next vertex changed. This should be
	 * remarked in <code>readWriteTransaction1</code> .changedEseqEdges.
	 */
	@Test
	public void testPutEdgeAfterInGraph() {
		testAddEdge();
		ex1.putAfterInGraph(ex2);

		Map<Edge, Map<ListPosition, Boolean>> changedEseqEdgesMap = new HashMap<Edge, Map<ListPosition, Boolean>>();
		Map<ListPosition, Boolean> changedPosition1 = new HashMap<ListPosition, Boolean>();
		changedPosition1.put(ListPosition.PREV, true);
		Map<ListPosition, Boolean> changedPosition2 = new HashMap<ListPosition, Boolean>();
		changedPosition2.put(ListPosition.NEXT, false);
		changedEseqEdgesMap.put(ex1, changedPosition1);
		changedEseqEdgesMap.put(ex2, changedPosition2);
		assertEquals(readWriteTransaction1.changedEseqEdges,
				changedEseqEdgesMap);
	}

	/**
	 * Edge ex1 is put after ex2 in Eseq within <code>readOnlyTransaction</code>
	 * . This is not allowed, so a GraphException is expected to be thrown.
	 */
	@Test
	public void testPutEdgeAfterInGraphReadOnly() {
		try {
			testAddEdge();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			ex1.putAfterInGraph(ex2);
			fail();
		} catch (GraphException ge) {
			System.out.println("\n- testPutEdgeAfterInGraphReadOnly -");
			System.out.println("##########################");
			System.out.println(ge.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {
			fail();
		}
	}

	/**
	 * Edge ex2 is put before ex1 in Eseq within
	 * <code>readWriteTransaction1</code>. ex2 is movedEdge with next edge
	 * changed, while ex1 is targetEdge with previous vertex changed. This
	 * should be remarked in <code>readWriteTransaction1</code>
	 * .changedEseqEdges.
	 */
	@Test
	public void testPutEdgeBeforeInGraph() {
		testAddEdge();
		ex2.putBeforeInGraph(ex1);

		Map<Edge, Map<ListPosition, Boolean>> changedEseqEdgesMap = new HashMap<Edge, Map<ListPosition, Boolean>>();
		Map<ListPosition, Boolean> changedPosition1 = new HashMap<ListPosition, Boolean>();
		changedPosition1.put(ListPosition.NEXT, true);
		Map<ListPosition, Boolean> changedPosition2 = new HashMap<ListPosition, Boolean>();
		changedPosition2.put(ListPosition.PREV, false);
		changedEseqEdgesMap.put(ex2, changedPosition1);
		changedEseqEdgesMap.put(ex1, changedPosition2);
		assertEquals(readWriteTransaction1.changedEseqEdges,
				changedEseqEdgesMap);
	}

	/**
	 * Edge ex2 is put before ex1 in Eseq within
	 * <code>readOnlyTransaction</code> . This is not allowed, so a
	 * GraphException is expected to be thrown.
	 */
	@Test
	public void testPutEdgeBeforeInGraphReadOnly() {
		try {
			testAddEdge();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			ex2.putBeforeInGraph(ex1);
			fail();
		} catch (GraphException ge) {
			System.out.println("\n- testPutEdgeBeforeInGraphReadOnly -");
			System.out.println("##########################");
			System.out.println(ge.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {
			fail();
		}
	}

	/**
	 * Edge ex1 is put after ex2 in Iseq(alpha) within
	 * <code>readWriteTransaction1</code>. ex1 is movedEdge with previous edge
	 * changed, while ex2 is targetEdge with next vertex changed. This should be
	 * remarked in <code>readWriteTransaction1</code> .changedEseqEdges.
	 */
	@Test
	public void testPutEdgeAfter() {
		testAddEdge();
		ex1.putEdgeAfter(ex2);

		Vertex alpha = ex1.getAlpha();
		Map<Vertex, Map<Edge, Map<ListPosition, Boolean>>> vertexChangedIncidencesMap = new HashMap<Vertex, Map<Edge, Map<ListPosition, Boolean>>>();
		Map<Edge, Map<ListPosition, Boolean>> changedIncidencesMap = new HashMap<Edge, Map<ListPosition, Boolean>>();
		Map<ListPosition, Boolean> changedPosition1 = new HashMap<ListPosition, Boolean>();
		changedPosition1.put(ListPosition.PREV, true);
		Map<ListPosition, Boolean> changedPosition2 = new HashMap<ListPosition, Boolean>();
		changedPosition2.put(ListPosition.NEXT, false);
		changedIncidencesMap.put(ex1, changedPosition1);
		changedIncidencesMap.put(ex2, changedPosition2);
		vertexChangedIncidencesMap.put(alpha, changedIncidencesMap);
		assertEquals(readWriteTransaction1.changedIncidences,
				vertexChangedIncidencesMap);
	}

	/**
	 * Edge ex1 is put after ex2 in Iseq(alpha) within
	 * <code>readOnlyTransaction</code>. This is not allowed, so a
	 * GraphException is expected to be thrown.
	 */
	@Test
	public void testPutEdgeAfterReadOnly() {
		try {
			testAddEdge();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			ex1.putEdgeAfter(ex2);
			fail();
		} catch (GraphException ge) {
			System.out.println("\n- testPutEdgeAfterReadOnly -");
			System.out.println("##########################");
			System.out.println(ge.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {
			fail();
		}
	}

	/**
	 * Edge ex2 is put before ex1 in Iseq(alpha) within
	 * <code>readWriteTransaction1</code>. ex2 is movedEdge with next edge
	 * changed, while ex1 is targetEdge with previous vertex changed. This
	 * should be remarked in <code>readWriteTransaction1</code>
	 * .changedEseqEdges.
	 */
	@Test
	public void testPutEdgeBefore() {
		testAddEdge();
		ex2.putEdgeBefore(ex1);
		Vertex alpha = ex1.getAlpha();

		Map<Vertex, Map<Edge, Map<ListPosition, Boolean>>> vertexChangedIncidencesMap = new HashMap<Vertex, Map<Edge, Map<ListPosition, Boolean>>>();
		Map<Edge, Map<ListPosition, Boolean>> changedIncidencesMap = new HashMap<Edge, Map<ListPosition, Boolean>>();
		Map<ListPosition, Boolean> changedPosition1 = new HashMap<ListPosition, Boolean>();
		changedPosition1.put(ListPosition.NEXT, true);
		Map<ListPosition, Boolean> changedPosition2 = new HashMap<ListPosition, Boolean>();
		changedPosition2.put(ListPosition.PREV, false);
		changedIncidencesMap.put(ex2, changedPosition1);
		changedIncidencesMap.put(ex1, changedPosition2);
		vertexChangedIncidencesMap.put(alpha, changedIncidencesMap);
		assertEquals(readWriteTransaction1.changedIncidences,
				vertexChangedIncidencesMap);
	}

	/**
	 * Edge ex2 is put before ex1 in Iseq(alpha) within
	 * <code>readOnlyTransaction</code>. This is not allowed, so a
	 * GraphException is expected to be thrown.
	 */
	@Test
	public void testPutEdgeBeforeReadOnly() {
		try {
			testAddEdge();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			ex2.putEdgeBefore(ex1);
			fail();
		} catch (GraphException ge) {
			System.out.println("\n- testPutEdgeBeforeReadOnly -");
			System.out.println("##########################");
			System.out.println(ge.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {
			fail();
		}
	}

	/**
	 * Change the alpha-vertex of edge ex1 from c1 to c2 within
	 * <code>readWriteTransaction1</code>. This should be remarked in
	 * <code>readWriteTransaction1</code> .changedEdges.
	 */
	@Test
	public void testSetAlpha() {
		testAddEdge();
		c2 = motorwayMap.createCity();
		ex1.setAlpha(c2);
		Map<Edge, VertexPosition> changedEdgesMap = new HashMap<Edge, VertexPosition>();
		changedEdgesMap.put(ex1, VertexPosition.ALPHA);
		assertEquals(readWriteTransaction1.changedEdges, changedEdgesMap);
	}

	/**
	 * Change the alpha-vertex of edge ex1 from c1 to c2 within
	 * <code>readOnlyTransaction</code>. This is not allowed, so a
	 * GraphException is expected to be thrown.
	 */
	@Test
	public void testSetAlphaReadOnly() {
		try {
			testAddEdge();
			c2 = motorwayMap.createCity();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			ex1.setAlpha(c2);
			fail();
		} catch (GraphException ge) {
			System.out.println("\n- testSetAlphaReadOnly -");
			System.out.println("##########################");
			System.out.println(ge.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {
			fail();
		}
	}

	/**
	 * Change the omega-vertex of edge ex1 from mw1 to mw2 within
	 * <code>readWriteTransaction1</code>. This should be remarked in
	 * <code>readWriteTransaction1</code> .changedEdges.
	 */
	@Test
	public void testSetOmega() {
		testAddEdge();
		mw2 = motorwayMap.createMotorway();
		ex1.setOmega(mw2);
		Map<Edge, VertexPosition> changedEdgesMap = new HashMap<Edge, VertexPosition>();
		changedEdgesMap.put(ex1, VertexPosition.OMEGA);
		assertEquals(readWriteTransaction1.changedEdges, changedEdgesMap);
	}

	/**
	 * Change the omega-vertex of edge ex1 from mw1 to mw2 within
	 * <code>readOnlyTransaction</code>. This is not allowed, so a
	 * GraphException is expected to be thrown.
	 */
	@Test
	public void testSetOmegaReadOnly() {
		try {
			testAddEdge();
			mw2 = motorwayMap.createMotorway();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			ex1.setOmega(mw2);
			fail();
		} catch (GraphException ge) {
			System.out.println("\n- testSetOmegaReadOnly -");
			System.out.println("##########################");
			System.out.println(ge.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {
			fail();
		}
	}

	/**
	 * Change the alpha-vertex of edge ex1 from c1 to c2 and change the
	 * omega-vertex of edge ex1 from mw1 to mw2 within
	 * <code>readWriteTransaction1</code>. This should be remarked in
	 * <code>readWriteTransaction1</code> .changedEdges.
	 */
	@Test
	public void testSetAlphaOmega() {
		testAddEdge();
		c2 = motorwayMap.createCity();
		ex1.setAlpha(c2);
		mw2 = motorwayMap.createMotorway();
		ex1.setOmega(mw2);
		Map<Edge, VertexPosition> changedEdgesMap = new HashMap<Edge, VertexPosition>();
		changedEdgesMap.put(ex1, VertexPosition.ALPHAOMEGA);
		assertEquals(readWriteTransaction1.changedEdges, changedEdgesMap);
	}

	/**
	 * Change all attributes of vertex c1 in <code>readWriteTransaction1</code>.
	 * This should be remarked in <code>readWriteTransaction1</code>
	 * .changedAttributes.
	 * 
	 * @throws ClassNotFoundException
	 */
	@Test
	public void testSetAttribute() throws ClassNotFoundException {
		testAddVertex();
		CityImpl c1Impl = (CityImpl) c1;
		
		c1Impl.set_name("test");
		
		c1Impl.set_testEnum(TestEnum.Test1);
		
		List<TestRecord> list1 = motorwayMap.createList();
		c1Impl.set_testList(list1);
		
		Map<String, String> map1 = motorwayMap.createMap();
		c1Impl.set_testMap(map1);
		
		Set<String> set1 = motorwayMap.createSet();
		c1Impl.set_testSet(set1);
		
		List<String> list2 = motorwayMap.createList();
		Set<String> set2 = motorwayMap.createSet();
		c1Impl.set_testRecord(motorwayMap.createTestRecord("test", list2, set2,
				2, 2D, 2L, false));
		
		Map<AttributedElement, Set<VersionedDataObject<?>>> changedAttributesMap = new HashMap<AttributedElement, Set<VersionedDataObject<?>>>();
		changedAttributesMap.put(c1, c1Impl.attributes());
		
		assertEquals(readWriteTransaction1.changedAttributes,
				changedAttributesMap);
	}

	/**
	 * Change attribute of vertex c1 in <code>readOnlyTransaction</code>. This
	 * is not allowed, so a GraphException is expected to be thrown.
	 */
	@Test
	public void testSetAttributeReadOnly() {
		try {
			testAddVertex();
			motorwayMap.commit();
			readOnlyTransaction = (TransactionImpl) motorwayMap
					.newReadOnlyTransaction();
			CityImpl c1Impl = (CityImpl) c1;
			c1Impl.set_name("test");
			fail();
		} catch (GraphException ge) {
			System.out.println("\n- testSetAttributeReadOnly -");
			System.out.println("##########################");
			System.out.println(ge.getMessage());
			assertTrue(true);
		} catch (CommitFailedException cfe) {
			fail();
		}
	}

	/**
	 * Making changes to Vertex c1 (changing attribute, change position in Vseq
	 * and changing incidence list) c1 is then deleted. c1 should only be
	 * present in
	 * <code>readWriteTransaction2<code>.deletedVertices and not in the other change sets.
	 */
	@Test
	public void testDeleteVertexAfterChanges() {
		try {
			testAddEdge();
			motorwayMap.commit();
			readWriteTransaction2 = (TransactionImpl) motorwayMap
					.newTransaction();
			c1.set_name("name");
			assertTrue(readWriteTransaction2.changedAttributes.containsKey(c1));
			c1.putAfter(c3);
			assertTrue(readWriteTransaction2.changedVseqVertices
					.containsKey(c1)
					&& readWriteTransaction2.changedVseqVertices
							.containsKey(c3));
			ex1.putEdgeAfter(ex3);
			assertTrue(readWriteTransaction2.changedIncidences.containsKey(c1));
			c1.delete();
			assertTrue(!readWriteTransaction2.changedAttributes.containsKey(c1));
			assertTrue(!(readWriteTransaction2.changedVseqVertices
					.containsKey(c1)));
			assertTrue(!readWriteTransaction2.changedIncidences.containsKey(c1));
			assertTrue(readWriteTransaction2.deletedVertices.contains(c1));
		} catch (Exception e) {
			fail();
		}
	}

	/**
	 * Making changes to Edge ex1 (changing attribute, change position in Eseq
	 * and changing position in incidence list) ex1 is then deleted. ex1 should
	 * only be present in
	 * <code>readWriteTransaction2<code>.deletedEdges and not in the other change sets.
	 */
	@Test
	public void testDeleteEdgeAfterChanges() {
		try {
			testAddEdge();
			motorwayMap.commit();
			readWriteTransaction2 = (TransactionImpl) motorwayMap
					.newTransaction();
			ex1.set_number(2);
			assertTrue(readWriteTransaction2.changedAttributes.containsKey(ex1));
			ex1.putAfterInGraph(ex3);
			assertTrue(readWriteTransaction2.changedEseqEdges.containsKey(ex1)
					&& readWriteTransaction2.changedEseqEdges.containsKey(ex3));
			ex1.putEdgeAfter(ex3);
			assertTrue(readWriteTransaction2.changedIncidences.get(c1)
					.containsKey(ex1)
					&& readWriteTransaction2.changedIncidences.get(c1)
							.containsKey(ex3));
			ex1.delete();
			ex3.delete();
			assertTrue(!readWriteTransaction2.changedAttributes
					.containsKey(ex1));
			// FindBugs can be ignored in the following lines
			assertTrue(!(readWriteTransaction2.changedEseqEdges
					.containsKey(ex1))
					&& !(readWriteTransaction2.changedEseqEdges
							.containsKey(ex3)));
			assertTrue(!(readWriteTransaction2.changedIncidences.get(c1)
					.containsKey(ex1))
					&& !(readWriteTransaction2.changedIncidences.get(c1)
							.containsKey(ex3)));
			assertTrue(readWriteTransaction2.deletedEdges.contains(ex1));
			assertTrue(readWriteTransaction2.deletedEdges.contains(ex3));
		} catch (Exception e) {
			// denounced by FindBugs, but can be ignored
			fail();
		}
	}

	/**
	 * 
	 * @return
	 */
	/*
	 * public static junit.framework.Test suite() { return new
	 * JUnit4TestAdapter(TransactionImplTest.class); }
	 */
}
