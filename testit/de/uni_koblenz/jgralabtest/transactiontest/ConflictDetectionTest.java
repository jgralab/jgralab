package de.uni_koblenz.jgralabtest.transactiontest;

import static org.junit.Assert.*; //import junit.framework.JUnit4TestAdapter;

//import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge; //import de.uni_koblenz.jgralab.GraphException;
//import de.uni_koblenz.jgralab.GraphIO;
//import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralab.trans.Transaction;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.City;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.Exit;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.Motorway;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMap;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMapSchema;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.TestRecord;

/**
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public class ConflictDetectionTest {
	private MotorwayMap motorwayMap;
	private Transaction readWriteTransaction1;
	private Transaction readWriteTransaction2;
	private Transaction readOnlyTransaction;
	private Transaction lastTransactionCommitted;
	private final long waitTimeBeforeCommit = 5000;

	private int internalVCount;
	private int internalECount;

	private ThreadGroup threadGroup;
	private Thread thread1;
	private Thread thread2;

	private boolean conflict;
	private String conflictReason;

	private final int V = 1;
	private final int E = 1;
	private final int N = 10;

	@Before
	public void setUp() throws CommitFailedException {
		MotorwayMapSchema schema = MotorwayMapSchema.instance();
		motorwayMap = schema.createMotorwayMapWithTransactionSupport(V, E);
		motorwayMap.newTransaction();
		// city (v1) and motorway (v2) have 10 incidences
		// (<e1,e2,e3,e4,e5,e6,...,e10> and
		// <-e1,-e2,-e3,-e4,-e5,-e6,...,-e10> respectively)
		City city = motorwayMap.createCity();
		internalVCount++;
		Motorway motorway = motorwayMap.createMotorway();
		internalVCount++;
		for (int i = 0; i < N; i++) {
			motorwayMap.createExit(city, motorway);
			internalECount++;
		}

		for (int i = 0; i < N; i++) {
			city = motorwayMap.createCity();
			internalVCount++;
			motorway = motorwayMap.createMotorway();
			internalVCount++;
			motorwayMap.createExit(city, motorway);
			internalECount++;
		}
		motorwayMap.commit();

		readWriteTransaction1 = motorwayMap.newTransaction();
		readWriteTransaction2 = motorwayMap.newTransaction();
		threadGroup = new ThreadGroup("ThreadGroup");
		conflict = false;
		lastTransactionCommitted = null;
	}

	@After
	public void tearDown() {
		motorwayMap = null;
	}

	/**
	 * Adding and deleting of vertices by <code>readWriteTransaction1</code>
	 * and <code>readWriteTransaction2</code>. No conflicts expected.
	 * 
	 * @see dpthesis Tab 4.1
	 */
	@Test
	public void testMergeVset1() {
		try {
			assertEquals(motorwayMap.getVCount(), internalVCount);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City v23 = motorwayMap.createCity();
			assertTrue(v23.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 1);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			assertEquals(motorwayMap.getVCount(), internalVCount);
			assertTrue(!v23.isValid());
			City v24 = motorwayMap.createCity();
			assertTrue(v24.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 1);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			assertTrue(!v24.isValid());
			Vertex v2 = motorwayMap.getVertex(2);
			v2.delete();
			assertTrue(!v2.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			assertTrue(v2.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 1);
			readWriteTransaction2.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			assertTrue(!v2.isValid());
			assertTrue(v23.isValid());
			assertTrue(v24.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 1);
			readOnlyTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Adding and deleting of vertices by <code>readWriteTransaction1</code>
	 * and <code>readWriteTransaction2</code> (in parallel). No conflicts
	 * expected.
	 * 
	 * <code>readWriteTransaction1</code> commits first.
	 * 
	 * @see dpthesis Tab 4.1
	 */
	@Test
	public void testMergeVset1Parallel1() {
		internalMergeVset1Parallel(readWriteTransaction2);
	}

	/**
	 * Adding and deleting of vertices by <code>readWriteTransaction1</code>
	 * and <code>readWriteTransaction2</code> (in parallel). No conflicts
	 * expected.
	 * 
	 * <code>readWriteTransaction2</code> commits first.
	 * 
	 * @see dpthesis Tab 4.1
	 */
	@Test
	public void testMergeVset1Parallel2() {
		internalMergeVset1Parallel(readWriteTransaction1);
	}

	/**
	 * 
	 * @param lastToCommit
	 * @param nameSuffix
	 */
	private void internalMergeVset1Parallel(final Transaction lastToCommit) {
		try {
			thread1 = new Thread(threadGroup, "Thread1") {

				public void run() {
					motorwayMap.setCurrentTransaction(readWriteTransaction1);
					assertEquals(motorwayMap.getVCount(), internalVCount);
					motorwayMap.createCity();
					assertEquals(motorwayMap.getVCount(), internalVCount + 1);

					Vertex v2 = motorwayMap.getVertex(2);
					v2.delete();
					assertTrue(!v2.isValid());
					assertEquals(motorwayMap.getVCount(), internalVCount);

					try {
						long sleepTime = 0;
						if (lastToCommit == motorwayMap.getCurrentTransaction())
							sleepTime = waitTimeBeforeCommit;
						Thread.sleep(sleepTime);
						readWriteTransaction1.commit();
						lastTransactionCommitted = readWriteTransaction1;
					} catch (CommitFailedException e) {
						e.printStackTrace();
						fail();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			};

			thread2 = new Thread(threadGroup, "Thread2") {
				public void run() {
					motorwayMap.setCurrentTransaction(readWriteTransaction2);
					assertEquals(motorwayMap.getVCount(), internalVCount);

					City c24 = motorwayMap.createCity();
					assertTrue(c24.isValid());
					assertEquals(motorwayMap.getVCount(), internalVCount + 1);

					motorwayMap.setCurrentTransaction(readWriteTransaction2);
					Vertex v2 = motorwayMap.getVertex(2);
					assertTrue(v2.isValid());
					assertEquals(motorwayMap.getVCount(), internalVCount + 1);

					try {
						long sleepTime = 0;
						if (lastToCommit == motorwayMap.getCurrentTransaction())
							sleepTime = waitTimeBeforeCommit;
						Thread.sleep(sleepTime);
						readWriteTransaction2.commit();
						lastTransactionCommitted = readWriteTransaction2;
					} catch (CommitFailedException e) {
						e.printStackTrace();
						fail();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			};
			thread1.start();
			thread2.start();
			while (threadGroup.activeCount() > 0) {
			}
			assert (lastTransactionCommitted == lastToCommit);
			assertTrue(!readWriteTransaction1.isValid()
					&& !readWriteTransaction2.isValid());
			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			assertTrue(motorwayMap.getVertex(2) == null);
			assertTrue(motorwayMap.getVertex(internalVCount + 1).isValid());
			assertTrue(motorwayMap.getVertex(internalVCount + 2).isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 1);
			readOnlyTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * <code>readWriteTransaction1</code> trying to delete a
	 * <code>Vertex</code> (v24) which isn't valid within the transaction.
	 * 
	 * Note: test case parallel missing, because scenario is not (easily)
	 * reproducible.
	 * 
	 * @see dpthesis Tab 4.2
	 */
	@Test
	public void testConflictVset1() {
		try {
			assertEquals(motorwayMap.getVCount(), internalVCount);
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City v23 = motorwayMap.createCity();
			assertTrue(v23.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 1);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			assertEquals(motorwayMap.getVCount(), internalVCount);
			assertTrue(!v23.isValid());
			City v24 = motorwayMap.createCity();
			assertTrue(v24.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 1);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			assertTrue(!v24.isValid());
			v24.delete();
			readWriteTransaction1.commit();
			fail();
		} catch (Exception e) {
			System.out.println("\n- testConflictVset1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Adding and deleting of edges by <code>readWriteTransaction1</code> and
	 * <code>readWriteTransaction2</code>. No conflicts expected.
	 * 
	 * @see dpthesis Tab 4.3
	 */
	@Test
	public void testMergeEset1() {
		try {
			assertEquals(motorwayMap.getVCount(), internalVCount);
			assertEquals(motorwayMap.getECount(), internalECount);
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City v23 = motorwayMap.createCity();
			Motorway v24 = motorwayMap.createMotorway();
			assertTrue(v23.isValid());
			assertTrue(v24.isValid());
			Exit e21 = motorwayMap.createExit(v23, v24);
			assertTrue(e21.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 2);
			assertEquals(motorwayMap.getECount(), internalECount + 1);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			assertEquals(motorwayMap.getVCount(), internalVCount);
			assertTrue(!v23.isValid());
			assertTrue(!v24.isValid());
			assertTrue(!e21.isValid());
			City v25 = motorwayMap.createCity();
			Motorway v26 = motorwayMap.createMotorway();
			Exit e22 = motorwayMap.createExit(v25, v26);
			assertTrue(e22.isValid());
			assertTrue(v25.isValid());
			assertTrue(v26.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 2);
			assertEquals(motorwayMap.getECount(), internalECount + 1);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			assertTrue(!e22.isValid());
			assertTrue(!v25.isValid());
			assertTrue(!v26.isValid());
			Edge e2 = motorwayMap.getEdge(2);
			e2.delete();
			assertTrue(!e2.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 2);
			assertEquals(motorwayMap.getECount(), internalECount);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			assertTrue(e2.isValid());
			readWriteTransaction2.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			assertTrue(!e2.isValid());
			assertTrue(e21.isValid());
			assertTrue(e22.isValid());
			assertTrue(v23.isValid());
			assertTrue(v24.isValid());
			assertTrue(v25.isValid());
			assertTrue(v26.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 4);
			assertEquals(motorwayMap.getECount(), internalECount + 1);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Adding and deleting of edges by <code>readWriteTransaction1</code> and
	 * <code>readWriteTransaction2</code> (in parallel). No conflicts
	 * expected.
	 * 
	 * <code>readWriteTransaction1</code> commits first.
	 * 
	 * @see dpthesis Tab 4.3
	 */
	@Test
	public void testMergeEset1Parallel1() {
		internalMergeEset1Parallel(readWriteTransaction2);
	}

	/**
	 * Adding and deleting of edges by <code>readWriteTransaction1</code> and
	 * <code>readWriteTransaction2</code> (in parallel). No conflicts
	 * expected.
	 * 
	 * <code>readWriteTransaction2</code> commits first.
	 * 
	 * @see dpthesis Tab 4.3
	 */
	@Test
	public void testMergeEset1Parallel2() {
		internalMergeEset1Parallel(readWriteTransaction1);
	}

	/**
	 * Apply this to the rest.
	 * 
	 * @param lastToCommit
	 */
	private void internalMergeEset1Parallel(final Transaction lastToCommit) {
		try {
			thread1 = new Thread(threadGroup, "Thread1") {

				public void run() {
					motorwayMap.setCurrentTransaction(readWriteTransaction1);
					assertEquals(motorwayMap.getVCount(), internalVCount);
					assertEquals(motorwayMap.getECount(), internalECount);

					City v23 = motorwayMap.createCity();
					Motorway v24 = motorwayMap.createMotorway();
					assertTrue(v23.isValid());
					assertTrue(v24.isValid());
					Exit e21 = motorwayMap.createExit(v23, v24);
					assertTrue(e21.isValid());
					assertEquals(motorwayMap.getVCount(), internalVCount + 2);
					assertEquals(motorwayMap.getECount(), internalECount + 1);

					Edge e7 = motorwayMap.getEdge(7);
					e7.delete();
					assertTrue(!e7.isValid());
					assertEquals(motorwayMap.getVCount(), internalVCount + 2);
					assertEquals(motorwayMap.getECount(), internalECount);

					try {
						long sleepTime = 0;
						if (lastToCommit == motorwayMap.getCurrentTransaction())
							sleepTime = waitTimeBeforeCommit;
						Thread.sleep(sleepTime);
						readWriteTransaction1.commit();
						lastTransactionCommitted = readWriteTransaction1;
					} catch (CommitFailedException e) {
						e.printStackTrace();
						fail();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			};

			thread2 = new Thread(threadGroup, "Thread2") {
				public void run() {
					motorwayMap.setCurrentTransaction(readWriteTransaction2);
					assertEquals(motorwayMap.getVCount(), internalVCount);
					assertEquals(motorwayMap.getECount(), internalECount);

					City c23 = motorwayMap.createCity();
					Motorway c24 = motorwayMap.createMotorway();
					Exit e21 = motorwayMap.createExit(c23, c24);
					assertTrue(e21.isValid());
					assertTrue(c23.isValid());
					assertTrue(c24.isValid());
					assertEquals(motorwayMap.getVCount(), internalVCount + 2);
					assertEquals(motorwayMap.getECount(), internalECount + 1);

					Edge e7 = motorwayMap.getEdge(7);
					assertTrue(e7.isValid());
					try {
						long sleepTime = 0;
						if (lastToCommit == motorwayMap.getCurrentTransaction())
							sleepTime = waitTimeBeforeCommit;
						Thread.sleep(sleepTime);
						readWriteTransaction2.commit();
						lastTransactionCommitted = readWriteTransaction2;
					} catch (CommitFailedException e) {
						e.printStackTrace();
						fail();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			};
			thread1.start();
			thread2.start();
			while (threadGroup.activeCount() > 0) {
			}
			assertTrue(!readWriteTransaction1.isValid()
					&& !readWriteTransaction2.isValid());
			assertTrue(lastTransactionCommitted == lastToCommit);
			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			assertTrue(motorwayMap.getEdge(7) == null);
			assertTrue(motorwayMap.getEdge(internalECount + 1).isValid());
			assertTrue(motorwayMap.getEdge(internalECount + 2).isValid());
			assertTrue(motorwayMap.getVertex(internalVCount + 1).isValid());
			assertTrue(motorwayMap.getVertex(internalVCount + 2).isValid());
			assertTrue(motorwayMap.getVertex(internalVCount + 3).isValid());
			assertTrue(motorwayMap.getVertex(internalVCount + 4).isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 4);
			assertEquals(motorwayMap.getECount(), internalECount + 1);
			readOnlyTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * <code>readWriteTransaction1</code> trying to delete an
	 * <code>Edge</code> (e22) which isn't valid within the transaction.
	 * 
	 * Note: test case parallel missing, because scenario is not (easily)
	 * reproducible.
	 * 
	 * @see dpthesis Tab 4.4
	 */
	@Test
	public void testConflictEset1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City v23 = motorwayMap.createCity();
			Motorway v24 = motorwayMap.createMotorway();
			Exit e21 = motorwayMap.createExit(v23, v24);
			assertTrue(v23.isValid());
			assertTrue(v24.isValid());
			assertTrue(e21.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 2);
			assertEquals(motorwayMap.getECount(), internalECount + 1);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			assertEquals(motorwayMap.getVCount(), internalVCount);
			assertTrue(!v23.isValid());
			assertTrue(!v24.isValid());
			assertTrue(!e21.isValid());
			City v25 = motorwayMap.createCity();
			Motorway v26 = motorwayMap.createMotorway();
			Exit e22 = motorwayMap.createExit(v25, v26);
			assertTrue(v25.isValid());
			assertTrue(v26.isValid());
			assertTrue(e22.isValid());
			assertEquals(motorwayMap.getVCount(), internalVCount + 2);
			assertEquals(motorwayMap.getECount(), internalECount + 1);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			assertTrue(!e22.isValid());
			e22.delete();
			readWriteTransaction1.commit();
			fail();
		} catch (Exception e) {
			System.out.println("\n- testConflictEset1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> tries to delete a
	 * <code>Vertex</code> (v1) which has been used in
	 * <code>readWriteTransaction1</code> to add a <code>Edge</code> (e21).
	 * 
	 * @see dpthesis Tab 4.5
	 */
	@Test
	public void testConflictEset2() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City v1 = motorwayMap.getFirstCity();
			Motorway v2 = motorwayMap.getFirstMotorway();
			Exit e21 = motorwayMap.createExit(v1, v2);
			assertTrue(v1.isValid());
			assertTrue(v2.isValid());
			assertTrue(e21.isValid());
			assertEquals(motorwayMap.getECount(), internalECount + 1);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			assertEquals(motorwayMap.getVCount(), internalVCount);
			assertTrue(v1.isValid());
			assertTrue(v2.isValid());
			assertTrue(!e21.isValid());
			v1.delete();
			assertEquals(motorwayMap.getVCount(), internalVCount - 1);
			// all incidences of v1 are deleted too
			assertEquals(motorwayMap.getECount(), internalECount - 10);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictEset2 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> tries to delete a
	 * <code>Vertex</code> (v1) which has been used in
	 * <code>readWriteTransaction1</code> to add a <code>Edge</code> (e21)
	 * (in parallel).
	 * 
	 * <code>readWriteTransaction1</code> commits first.
	 * 
	 * @see dpthesis Tab 4.5
	 */
	@Test
	public void testConflictEset2Parallel1() {
		internalConflictEset2Parallel(readWriteTransaction2, "1");
		;
	}

	/**
	 * <code>readWriteTransaction2</code> tries to delete a
	 * <code>Vertex</code> (v1) which has been used in
	 * <code>readWriteTransaction1</code> to add a <code>Edge</code> (e21)
	 * (in parallel).
	 * 
	 * <code>readWriteTransaction2</code> commits first.
	 * 
	 * @see dpthesis Tab 4.5
	 */
	@Test
	public void testConflictEset2Parallel2() {
		internalConflictEset2Parallel(readWriteTransaction1, "2");
	}

	/**
	 * 
	 * @param lastToCommit
	 * @param nameSuffix
	 */
	private void internalConflictEset2Parallel(final Transaction lastToCommit,
			String nameSuffix) {
		final Vertex v1 = motorwayMap.getFirstCity();
		final Vertex v2 = motorwayMap.getFirstMotorway();

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				City v1 = motorwayMap.getFirstCity();
				Motorway v2 = motorwayMap.getFirstMotorway();
				Exit e21 = motorwayMap.createExit(v1, v2);
				assertTrue(v1.isValid());
				assertTrue(v2.isValid());
				assertTrue(e21.isValid());
				assertEquals(motorwayMap.getECount(), internalECount + 1);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				assertEquals(motorwayMap.getVCount(), internalVCount);
				assertTrue(v1.isValid());
				assertTrue(v2.isValid());
				assertTrue(motorwayMap.getEdge(internalECount + 1) == null);
				v1.delete();
				assertEquals(motorwayMap.getVCount(), internalVCount - 1);
				// all incidences of v1 are deleted too
				assertEquals(motorwayMap.getECount(), internalECount - 10);

				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assert (lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testConflictEseq2Parallel" + nameSuffix + " -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Both transactions should previous vertex of v4 in Vseq explicitly.
	 * Conflict must be detected.
	 * 
	 * @see dpthesis Tab 4.6
	 */
	@Test
	public void testConflictVseq1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v2 = motorwayMap.getVertex(2);
			Vertex v4 = motorwayMap.getVertex(4);
			v4.putAfter(v2);
			assertTrue(v4.isAfter(v2));
			assertEquals(v4.getPrevVertex(), v2);
			assertEquals(v2.getNextVertex(), v4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			Vertex v1 = motorwayMap.getVertex(1);
			v4 = motorwayMap.getVertex(4);
			v4.putAfter(v1);
			assertTrue(v4.isAfter(v1));
			assertEquals(v4.getPrevVertex(), v1);
			assertEquals(v1.getNextVertex(), v4);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictVseq1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Both transactions should previous vertex of v4 in Vseq explicitly (in
	 * parallel). Conflict must be detected.
	 * 
	 * <code>readWriteTransaction1</code> commits first.
	 * 
	 * @see dpthesis Tab 4.6
	 */
	@Test
	public void testConflictVseq1Parallel1() {
		internalConflictVseq1Parallel(readWriteTransaction2, "1");
	}

	/**
	 * Both transactions should previous vertex of v4 in Vseq explicitly (in
	 * parallel). Conflict must be detected.
	 * 
	 * <code>readWriteTransaction2</code> commits first.
	 * 
	 * @see dpthesis Tab 4.6
	 */
	@Test
	public void testConflictVseq1Parallel2() {
		internalConflictVseq1Parallel(readWriteTransaction1, "2");
	}

	/**
	 * TODO apply this to the rest of the parallel tests.
	 * 
	 * @param lastToCommit
	 * @param nameSuffix
	 */
	private void internalConflictVseq1Parallel(final Transaction lastToCommit,
			String nameSuffix) {
		final Vertex v2 = motorwayMap.getVertex(2);
		final Vertex v4 = motorwayMap.getVertex(4);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				v4.putAfter(v2);
				assertTrue(v4.isAfter(v2));
				assertEquals(v4.getPrevVertex(), v2);
				assertEquals(v2.getNextVertex(), v4);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				Vertex v1 = motorwayMap.getVertex(1);
				v4.putAfter(v1);
				assertTrue(v4.isAfter(v1));
				assertEquals(v4.getPrevVertex(), v1);
				assertEquals(v1.getNextVertex(), v4);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testConflictVseq1Parallel" + nameSuffix + " -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * In both transactions the position of Vertex v2 in Vseq is changed
	 * explicitly. Conflict must be detected.
	 * 
	 * Note: test in parallel missing, because this causes only a conflict if
	 * <code>readWriteTransaction1</code> commits before
	 * <code>readWriteTransaction2</code>.
	 * 
	 * @see dpthesis Tab 4.7
	 */
	@Test
	public void testConflictVseq2() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v2 = motorwayMap.getVertex(2);
			Vertex v4 = motorwayMap.getVertex(4);
			v4.putAfter(v2);
			assertTrue(v4.isAfter(v2));
			assertEquals(v4.getPrevVertex(), v2);
			assertEquals(v2.getNextVertex(), v4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			Vertex v5 = motorwayMap.getVertex(5);
			v2.putAfter(v5);
			assertTrue(v2.isAfter(v5));
			assertEquals(v2.getPrevVertex(), v5);
			assertEquals(v5.getNextVertex(), v2);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictVseq2 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes v4 whose position has been
	 * explicitly changed in <code>readWriteTransaction1</code>.
	 * 
	 * @see dpthesis Tab 4.10
	 */
	@Test
	public void testConflictVseq3() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v2 = motorwayMap.getVertex(2);
			Vertex v4 = motorwayMap.getVertex(4);
			v4.putAfter(v2);
			assertTrue(v4.isAfter(v2));
			assertEquals(v4.getPrevVertex(), v2);
			assertEquals(v2.getNextVertex(), v4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			motorwayMap.deleteVertex(v4);
			assertTrue(!v4.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictVseq3 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes v4 whose position has been
	 * explicitly changed in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * <code>readWriteTransaction1</code> commits first.
	 * 
	 * @see dpthesis Tab 4.10 and Tab 4.11
	 */
	@Test
	public void testConflictVseq3Parallel() {
		internalConflictVseq3And4Parallel(readWriteTransaction2, "3");
	}

	/**
	 * v4 phantom for <code>readWriteTransaction1</code>.
	 * 
	 * @see dpthesis Tab 4.11
	 */
	@Test
	public void testConflictVseq4() {
		try {
			Vertex v2 = motorwayMap.getVertex(2);
			Vertex v4 = motorwayMap.getVertex(4);
			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			v4 = motorwayMap.getVertex(4);
			v4.delete();
			assertTrue(!v4.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			v4.putAfter(v2);
			assertTrue(v4.isAfter(v2));
			assertEquals(v4.getPrevVertex(), v2);
			assertEquals(v2.getNextVertex(), v4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictVseq4 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes v4 whose position has been
	 * explicitly changed in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * <code>readWriteTransaction2</code> commits first.
	 * 
	 * @see dpthesis Tab 4.10 and Tab 4.11
	 */
	@Test
	public void testConflictVseq4Parallel() {
		internalConflictVseq3And4Parallel(readWriteTransaction1, "4");
	}

	private void internalConflictVseq3And4Parallel(
			final Transaction lastToCommit, String nameSuffix) {
		final Vertex v2 = motorwayMap.getVertex(2);
		final Vertex v4 = motorwayMap.getVertex(4);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				v4.putAfter(v2);
				assertTrue(v4.isAfter(v2));
				assertEquals(v4.getPrevVertex(), v2);
				assertEquals(v2.getNextVertex(), v4);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				motorwayMap.deleteVertex(v4);
				assertTrue(!v4.isValid());
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testConflictVseq" + nameSuffix + "Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Merging changes in Vseq. Implicit change in
	 * <code>readWriteTransaction2</code>
	 * 
	 * @see dpthesis Tab 4.8
	 */
	@Test
	public void testMergeVseq1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v1 = motorwayMap.getVertex(1);
			Vertex v2 = motorwayMap.getVertex(2);
			Vertex v3 = motorwayMap.getVertex(3);
			Vertex v4 = motorwayMap.getVertex(4);
			Vertex v5 = motorwayMap.getVertex(5);
			Vertex v6 = motorwayMap.getVertex(6);
			// Vseq at beginning <v1, v2, v3, v4, v5, v6,...>
			assertEquals(motorwayMap.getFirstVertex(), v1);
			assertEquals(v1.getPrevVertex(), null);
			assertEquals(v1.getNextVertex(), v2);
			assertEquals(v2.getPrevVertex(), v1);
			assertEquals(v2.getNextVertex(), v3);
			assertEquals(v3.getPrevVertex(), v2);
			assertEquals(v3.getNextVertex(), v4);
			assertEquals(v4.getPrevVertex(), v3);
			assertEquals(v4.getNextVertex(), v5);
			assertEquals(v5.getPrevVertex(), v4);
			assertEquals(v5.getNextVertex(), v6);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			v4.putAfter(v1);
			// <v1, v4, v2, v3, v5, v6,...>
			assertEquals(motorwayMap.getFirstVertex(), v1);
			assertEquals(v1.getPrevVertex(), null);
			assertEquals(v1.getNextVertex(), v4);
			assertEquals(v4.getPrevVertex(), v1);
			assertEquals(v4.getNextVertex(), v2);
			assertEquals(v2.getPrevVertex(), v4);
			assertEquals(v2.getNextVertex(), v3);
			assertEquals(v3.getPrevVertex(), v2);
			assertEquals(v3.getNextVertex(), v5);
			assertEquals(v5.getPrevVertex(), v3);
			assertEquals(v5.getNextVertex(), v6);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			v3.delete();
			assertTrue(!v3.isValid());
			// <v1, v2, v4, v5, v6,...>
			assertEquals(motorwayMap.getFirstVertex(), v1);
			assertEquals(v1.getPrevVertex(), null);
			assertEquals(v1.getNextVertex(), v2);
			assertEquals(v2.getPrevVertex(), v1);
			assertEquals(v2.getNextVertex(), v4);
			assertEquals(v4.getPrevVertex(), v2);
			assertEquals(v4.getNextVertex(), v5);
			assertEquals(v5.getPrevVertex(), v4);
			assertEquals(v5.getNextVertex(), v6);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			// <v1, v4, v2, v5, v6,...>
			assertEquals(motorwayMap.getFirstVertex(), v1);
			assertEquals(v1.getPrevVertex(), null);
			assertEquals(v1.getNextVertex(), v4);
			assertEquals(v4.getPrevVertex(), v1);
			assertEquals(v4.getNextVertex(), v2);
			assertEquals(v2.getPrevVertex(), v4);
			assertEquals(v2.getNextVertex(), v5);
			assertEquals(v5.getPrevVertex(), v2);
			assertEquals(v5.getNextVertex(), v6);
			assertTrue(!v3.isValid());
		} catch (CommitFailedException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Merging changes in Vseq (in parallel). Implicit change in
	 * <code>readWriteTransaction2</code>
	 * 
	 * @see dpthesis Tab 4.8
	 */
	@Test
	public void testMergeVseq1Parallel1() {
		internalMergeVseq1Parallel(readWriteTransaction2);
	}

	/**
	 * Merging changes in Vseq (in parallel). Implicit change in
	 * <code>readWriteTransaction2</code>
	 * 
	 * @see dpthesis Tab 4.8
	 */
	@Test
	public void testMergeVseq1Parallel2() {
		internalMergeVseq1Parallel(readWriteTransaction1);
	}

	private void internalMergeVseq1Parallel(final Transaction lastToCommit) {
		final Vertex v1 = motorwayMap.getVertex(1);
		final Vertex v2 = motorwayMap.getVertex(2);
		final Vertex v3 = motorwayMap.getVertex(3);
		final Vertex v4 = motorwayMap.getVertex(4);
		final Vertex v5 = motorwayMap.getVertex(5);
		final Vertex v6 = motorwayMap.getVertex(6);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				// Vseq at beginning <v1, v2, v3, v4, v5, v6,...>
				assertEquals(motorwayMap.getFirstVertex(), v1);
				assertEquals(v1.getPrevVertex(), null);
				assertEquals(v1.getNextVertex(), v2);
				assertEquals(v2.getPrevVertex(), v1);
				assertEquals(v2.getNextVertex(), v3);
				assertEquals(v3.getPrevVertex(), v2);
				assertEquals(v3.getNextVertex(), v4);
				assertEquals(v4.getPrevVertex(), v3);
				assertEquals(v4.getNextVertex(), v5);
				assertEquals(v5.getPrevVertex(), v4);
				assertEquals(v5.getNextVertex(), v6);

				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				v4.putAfter(v1);
				// <v1, v4, v2, v3, v5, v6,...>
				assertEquals(motorwayMap.getFirstVertex(), v1);
				assertEquals(v1.getPrevVertex(), null);
				assertEquals(v1.getNextVertex(), v4);
				assertEquals(v4.getPrevVertex(), v1);
				assertEquals(v4.getNextVertex(), v2);
				assertEquals(v2.getPrevVertex(), v4);
				assertEquals(v2.getNextVertex(), v3);
				assertEquals(v3.getPrevVertex(), v2);
				assertEquals(v3.getNextVertex(), v5);
				assertEquals(v5.getPrevVertex(), v3);
				assertEquals(v5.getNextVertex(), v6);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				v3.delete();
				assertTrue(!v3.isValid());
				// <v1, v2, v4, v5, v6,...>
				assertEquals(motorwayMap.getFirstVertex(), v1);
				assertEquals(v1.getPrevVertex(), null);
				assertEquals(v1.getNextVertex(), v2);
				assertEquals(v2.getPrevVertex(), v1);
				assertEquals(v2.getNextVertex(), v4);
				assertEquals(v4.getPrevVertex(), v2);
				assertEquals(v4.getNextVertex(), v5);
				assertEquals(v5.getPrevVertex(), v4);
				assertEquals(v5.getNextVertex(), v6);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
		// <v1, v4, v2, v5, v6,...>
		assertEquals(motorwayMap.getFirstVertex(), v1);
		assertEquals(v1.getPrevVertex(), null);
		assertEquals(v1.getNextVertex(), v4);
		assertEquals(v4.getPrevVertex(), v1);
		assertEquals(v4.getNextVertex(), v2);
		assertEquals(v2.getPrevVertex(), v4);
		assertEquals(v2.getNextVertex(), v5);
		assertEquals(v5.getPrevVertex(), v2);
		assertEquals(v5.getNextVertex(), v6);
		assertTrue(!v3.isValid());
	}

	/**
	 * Both transactions are changing positions of respectively different
	 * vertices in Vseq. Merging should work here.
	 * 
	 * @see dpthesis Tab 4.9
	 */
	@Test
	public void testMergeVseq2() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v1 = motorwayMap.getVertex(1);
			Vertex v2 = motorwayMap.getVertex(2);
			Vertex v3 = motorwayMap.getVertex(3);
			Vertex v4 = motorwayMap.getVertex(4);
			Vertex v5 = motorwayMap.getVertex(5);
			Vertex v6 = motorwayMap.getVertex(6);
			// Vseq at beginning <v1, v2, v3, v4, v5, v6,...>
			assertEquals(motorwayMap.getFirstVertex(), v1);
			assertEquals(v1.getPrevVertex(), null);
			assertEquals(v1.getNextVertex(), v2);
			assertEquals(v2.getPrevVertex(), v1);
			assertEquals(v2.getNextVertex(), v3);
			assertEquals(v3.getPrevVertex(), v2);
			assertEquals(v3.getNextVertex(), v4);
			assertEquals(v4.getPrevVertex(), v3);
			assertEquals(v4.getNextVertex(), v5);
			assertEquals(v5.getPrevVertex(), v4);
			assertEquals(v5.getNextVertex(), v6);

			v4.putAfter(v2);
			// Vseq temporary in t2 <v1, v2, v4, v3, v5, v6,...>
			assertEquals(motorwayMap.getFirstVertex(), v1);
			assertEquals(v1.getPrevVertex(), null);
			assertEquals(v1.getNextVertex(), v2);
			assertEquals(v2.getPrevVertex(), v1);
			assertEquals(v2.getNextVertex(), v4);
			assertEquals(v4.getPrevVertex(), v2);
			assertEquals(v4.getNextVertex(), v3);
			assertEquals(v3.getPrevVertex(), v4);
			assertEquals(v3.getNextVertex(), v5);
			assertEquals(v5.getPrevVertex(), v3);
			assertEquals(v5.getNextVertex(), v6);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			// Vseq at beginning of t2 and t3
			assertEquals(motorwayMap.getFirstVertex(), v1);
			assertEquals(v1.getPrevVertex(), null);
			assertEquals(v1.getNextVertex(), v2);
			assertEquals(v2.getPrevVertex(), v1);
			assertEquals(v2.getNextVertex(), v3);
			assertEquals(v3.getPrevVertex(), v2);
			assertEquals(v3.getNextVertex(), v4);
			assertEquals(v4.getPrevVertex(), v3);
			assertEquals(v4.getNextVertex(), v5);
			assertEquals(v5.getPrevVertex(), v4);
			assertEquals(v5.getNextVertex(), v6);
			v1.putAfter(v3);
			// <v2, v3, v1, v4, v5, v6,...>
			assertEquals(motorwayMap.getFirstVertex(), v2);
			assertEquals(v2.getPrevVertex(), null);
			assertEquals(v2.getNextVertex(), v3);
			assertEquals(v3.getPrevVertex(), v2);
			assertEquals(v3.getNextVertex(), v1);
			assertEquals(v1.getPrevVertex(), v3);
			assertEquals(v1.getNextVertex(), v4);
			assertEquals(v4.getPrevVertex(), v1);
			assertEquals(v4.getNextVertex(), v5);
			assertEquals(v5.getPrevVertex(), v4);
			assertEquals(v5.getNextVertex(), v6);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			// Vseq at BOT of t4 <v1, v2, v4, v3, v5, v6,...>
			assertEquals(motorwayMap.getFirstVertex(), v1);
			assertEquals(v1.getPrevVertex(), null);
			assertEquals(v1.getNextVertex(), v2);
			assertEquals(v2.getPrevVertex(), v1);
			assertEquals(v2.getNextVertex(), v4);
			assertEquals(v4.getPrevVertex(), v2);
			assertEquals(v4.getNextVertex(), v3);
			assertEquals(v3.getPrevVertex(), v4);
			assertEquals(v3.getNextVertex(), v5);
			assertEquals(v5.getPrevVertex(), v3);
			assertEquals(v5.getNextVertex(), v6);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			// <v2, v4, v3, v1, v5, v6,...>
			assertEquals(motorwayMap.getFirstVertex(), v2);
			assertEquals(v2.getPrevVertex(), null);
			assertEquals(v2.getNextVertex(), v4);
			assertEquals(v4.getPrevVertex(), v2);
			assertEquals(v4.getNextVertex(), v3);
			assertEquals(v3.getPrevVertex(), v4);
			assertEquals(v3.getNextVertex(), v1);
			assertEquals(v1.getPrevVertex(), v3);
			assertEquals(v1.getNextVertex(), v5);
			assertEquals(v5.getPrevVertex(), v1);
			assertEquals(v5.getNextVertex(), v6);
		} catch (CommitFailedException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Both transactions are changing positions of respectively different
	 * vertices in Vseq (in parallel). Merging should work here.
	 * 
	 * @see dpthesis Tab 4.9
	 */
	@Test
	public void testMergeVseq2Parallel1() {
		internalMergeVseq2Parallel(readWriteTransaction2);
	}

	/**
	 * Both transactions are changing positions of respectively different
	 * vertices in Vseq (in parallel). Merging should work here.
	 * 
	 * @see dpthesis Tab 4.9
	 */
	@Test
	public void testMergeVseq2Parallel2() {
		internalMergeVseq2Parallel(readWriteTransaction1);
	}

	private void internalMergeVseq2Parallel(final Transaction lastToCommit) {
		final Vertex v1 = motorwayMap.getVertex(1);
		final Vertex v2 = motorwayMap.getVertex(2);
		final Vertex v3 = motorwayMap.getVertex(3);
		final Vertex v4 = motorwayMap.getVertex(4);
		final Vertex v5 = motorwayMap.getVertex(5);
		final Vertex v6 = motorwayMap.getVertex(6);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				// Vseq at beginning <v1, v2, v3, v4, v5, v6,...>
				assertEquals(motorwayMap.getFirstVertex(), v1);
				assertEquals(v1.getPrevVertex(), null);
				assertEquals(v1.getNextVertex(), v2);
				assertEquals(v2.getPrevVertex(), v1);
				assertEquals(v2.getNextVertex(), v3);
				assertEquals(v3.getPrevVertex(), v2);
				assertEquals(v3.getNextVertex(), v4);
				assertEquals(v4.getPrevVertex(), v3);
				assertEquals(v4.getNextVertex(), v5);
				assertEquals(v5.getPrevVertex(), v4);
				assertEquals(v5.getNextVertex(), v6);

				v4.putAfter(v2);
				// Vseq temporary in t2 <v1, v2, v4, v3, v5, v6,...>
				assertEquals(motorwayMap.getFirstVertex(), v1);
				assertEquals(v1.getPrevVertex(), null);
				assertEquals(v1.getNextVertex(), v2);
				assertEquals(v2.getPrevVertex(), v1);
				assertEquals(v2.getNextVertex(), v4);
				assertEquals(v4.getPrevVertex(), v2);
				assertEquals(v4.getNextVertex(), v3);
				assertEquals(v3.getPrevVertex(), v4);
				assertEquals(v3.getNextVertex(), v5);
				assertEquals(v5.getPrevVertex(), v3);
				assertEquals(v5.getNextVertex(), v6);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				// Vseq at beginning
				assertEquals(motorwayMap.getFirstVertex(), v1);
				assertEquals(v1.getPrevVertex(), null);
				assertEquals(v1.getNextVertex(), v2);
				assertEquals(v2.getPrevVertex(), v1);
				assertEquals(v2.getNextVertex(), v3);
				assertEquals(v3.getPrevVertex(), v2);
				assertEquals(v3.getNextVertex(), v4);
				assertEquals(v4.getPrevVertex(), v3);
				assertEquals(v4.getNextVertex(), v5);
				assertEquals(v5.getPrevVertex(), v4);
				assertEquals(v5.getNextVertex(), v6);
				v1.putAfter(v3);
				// <v2, v3, v1, v4, v5, v6,...>
				assertEquals(motorwayMap.getFirstVertex(), v2);
				assertEquals(v2.getPrevVertex(), null);
				assertEquals(v2.getNextVertex(), v3);
				assertEquals(v3.getPrevVertex(), v2);
				assertEquals(v3.getNextVertex(), v1);
				assertEquals(v1.getPrevVertex(), v3);
				assertEquals(v1.getNextVertex(), v4);
				assertEquals(v4.getPrevVertex(), v1);
				assertEquals(v4.getNextVertex(), v5);
				assertEquals(v5.getPrevVertex(), v4);
				assertEquals(v5.getNextVertex(), v6);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
		// <v2, v4, v3, v1, v5, v6,...>
		assertEquals(motorwayMap.getFirstVertex(), v2);
		assertEquals(v2.getPrevVertex(), null);
		assertEquals(v2.getNextVertex(), v4);
		assertEquals(v4.getPrevVertex(), v2);
		assertEquals(v4.getNextVertex(), v3);
		assertEquals(v3.getPrevVertex(), v4);
		assertEquals(v3.getNextVertex(), v1);
		assertEquals(v1.getPrevVertex(), v3);
		assertEquals(v1.getNextVertex(), v5);
		assertEquals(v5.getPrevVertex(), v1);
		assertEquals(v5.getNextVertex(), v6);
	}

	/**
	 * Both transactions change position of e4 in Eseq explicitly. Conflict
	 * expected.
	 * 
	 * @see dpthesis Tab 4.12
	 */
	@Test
	public void testConflictEseq1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Edge e2 = motorwayMap.getEdge(2);
			Edge e4 = motorwayMap.getEdge(4);
			e4.putAfterInGraph(e2);
			assertTrue(e4.isAfterInGraph(e2));
			assertEquals(e4.getPrevEdgeInGraph(), e2);
			assertEquals(e2.getNextEdgeInGraph(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			Edge e1 = motorwayMap.getEdge(1);
			e4.putAfterInGraph(e1);
			assertTrue(e4.isAfterInGraph(e1));
			assertEquals(e4.getPrevEdgeInGraph(), e1);
			assertEquals(e1.getNextEdgeInGraph(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictEseq1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Both transactions change position of e4 in Eseq explicitly (in parallel).
	 * Conflict expected.
	 * 
	 * @see dpthesis Tab 4.12
	 */
	@Test
	public void testConflictEseq1Parallel1() {
		internalConflictEseq1Parallel(readWriteTransaction2, "1");
	}

	/**
	 * Both transactions change position of e4 in Eseq explicitly (in parallel).
	 * Conflict expected.
	 * 
	 * @see dpthesis Tab 4.12
	 */
	@Test
	public void testConflictEseq1Parallel2() {
		internalConflictEseq1Parallel(readWriteTransaction1, "2");
	}

	private void internalConflictEseq1Parallel(final Transaction lastToCommit,
			String nameSuffix) {
		final Edge e1 = motorwayMap.getEdge(1);
		final Edge e2 = motorwayMap.getEdge(2);
		final Edge e4 = motorwayMap.getEdge(4);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				e4.putAfterInGraph(e2);
				assertTrue(e4.isAfterInGraph(e2));
				assertEquals(e4.getPrevEdgeInGraph(), e2);
				assertEquals(e2.getNextEdgeInGraph(), e4);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				e4.putAfterInGraph(e1);
				assertTrue(e4.isAfterInGraph(e1));
				assertEquals(e4.getPrevEdgeInGraph(), e1);
				assertEquals(e1.getNextEdgeInGraph(), e4);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testConflictEseq1Parallel" + nameSuffix + " -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Both transactions change position of e4 in Eseq explicitly (in parallel).
	 * Conflict expected.
	 * 
	 * Note: test in parallel missing, because this causes only a conflict if
	 * <code>readWriteTransaction1</code> commits before
	 * <code>readWriteTransaction2</code>.
	 * 
	 * @see dpthesis Tab 4.13
	 */
	@Test
	public void testConflictEseq2() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Edge e2 = motorwayMap.getEdge(2);
			Edge e4 = motorwayMap.getEdge(4);
			e4.putAfterInGraph(e2);
			assertTrue(e4.isAfterInGraph(e2));
			assertEquals(e4.getPrevEdgeInGraph(), e2);
			assertEquals(e2.getNextEdgeInGraph(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			Edge e5 = motorwayMap.getEdge(5);
			e2.putAfterInGraph(e5);
			assertTrue(e2.isAfterInGraph(e5));
			assertEquals(e2.getPrevEdgeInGraph(), e5);
			assertEquals(e5.getNextEdgeInGraph(), e2);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictEseq2 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Lost Update of the changes made in <code>readWriteTransaction1</code>
	 * for <code>Edge</code> e4.
	 * 
	 * @see dpthesis Tab. 4.16
	 */
	@Test
	public void testConflictEseq3() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Edge e2 = motorwayMap.getEdge(2);
			Edge e4 = motorwayMap.getEdge(4);
			e4.putAfterInGraph(e2);
			assertTrue(e4.isAfterInGraph(e2));
			assertEquals(e4.getPrevEdgeInGraph(), e2);
			assertEquals(e2.getNextEdgeInGraph(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			motorwayMap.deleteEdge(e4);
			assertTrue(!e4.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictEseq3 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes e4 whose position has been
	 * explicitly changed in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.16 and Tab 4.17
	 */
	@Test
	public void testConflictEseq3Parallel() {
		internalConflictEseq3and4Parallel(readWriteTransaction2, "3");
	}

	/**
	 * e4 is a phantom for <code>readWriteTransaction1</code>.
	 * 
	 * @see dpthesis Tab. 4.17
	 */
	@Test
	public void testConflictEseq4() {
		try {
			Edge e2 = motorwayMap.getEdge(2);
			Edge e4 = motorwayMap.getEdge(4);
			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			e4.delete();
			assertTrue(!e4.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			e4.putAfterInGraph(e2);
			assertTrue(e4.isAfterInGraph(e2));
			assertEquals(e4.getPrevEdgeInGraph(), e2);
			assertEquals(e2.getNextEdgeInGraph(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictEseq4 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes e4 whose position has been
	 * explicitly changed in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.16 and Tab 4.17
	 */
	@Test
	public void testConflictEseq4Parallel() {
		internalConflictEseq3and4Parallel(readWriteTransaction1, "4");
	}

	private void internalConflictEseq3and4Parallel(
			final Transaction lastToCommit, String nameSuffix) {
		final Edge e2 = motorwayMap.getEdge(2);
		final Edge e4 = motorwayMap.getEdge(4);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				e4.putAfterInGraph(e2);
				assertTrue(e4.isAfterInGraph(e2));
				assertEquals(e4.getPrevEdgeInGraph(), e2);
				assertEquals(e2.getNextEdgeInGraph(), e4);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				motorwayMap.deleteEdge(e4);
				assertTrue(!e4.isValid());
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testConflictEseq" + nameSuffix + "Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Merging changes in Eseq. Implicit change in
	 * <code>readWriteTransaction2</code>
	 * 
	 * @see dpthesis Tab 4.14
	 */
	@Test
	public void testMergeEseq1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			// t2
			Edge e1 = motorwayMap.getEdge(1);
			Edge e2 = motorwayMap.getEdge(2);
			Edge e3 = motorwayMap.getEdge(3);
			Edge e4 = motorwayMap.getEdge(4);
			Edge e5 = motorwayMap.getEdge(5);
			Edge e6 = motorwayMap.getEdge(6);
			// Eseq at beginning <e1, e2, e3, e4, e5, e6,...>
			assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
			assertEquals(e1.getPrevEdgeInGraph(), null);
			assertEquals(e1.getNextEdgeInGraph(), e2);
			assertEquals(e2.getPrevEdgeInGraph(), e1);
			assertEquals(e2.getNextEdgeInGraph(), e3);
			assertEquals(e3.getPrevEdgeInGraph(), e2);
			assertEquals(e3.getNextEdgeInGraph(), e4);
			assertEquals(e4.getPrevEdgeInGraph(), e3);
			assertEquals(e4.getNextEdgeInGraph(), e5);
			assertEquals(e5.getPrevEdgeInGraph(), e4);
			assertEquals(e5.getNextEdgeInGraph(), e6);

			e4.putAfterInGraph(e1);
			// <e1, e4, e2, e3, e5, e6,...>
			assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
			assertEquals(e1.getPrevEdgeInGraph(), null);
			assertEquals(e1.getNextEdgeInGraph(), e4);
			assertEquals(e4.getPrevEdgeInGraph(), e1);
			assertEquals(e4.getNextEdgeInGraph(), e2);
			assertEquals(e2.getPrevEdgeInGraph(), e4);
			assertEquals(e2.getNextEdgeInGraph(), e3);
			assertEquals(e3.getPrevEdgeInGraph(), e2);
			assertEquals(e3.getNextEdgeInGraph(), e5);
			assertEquals(e5.getPrevEdgeInGraph(), e3);
			assertEquals(e5.getNextEdgeInGraph(), e6);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			e3.delete();
			assertTrue(!e3.isValid());
			// <e1, e2, e4, e5, e6,...>
			assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
			assertEquals(e1.getPrevEdgeInGraph(), null);
			assertEquals(e1.getNextEdgeInGraph(), e2);
			assertEquals(e2.getPrevEdgeInGraph(), e1);
			assertEquals(e2.getNextEdgeInGraph(), e4);
			assertEquals(e4.getPrevEdgeInGraph(), e2);
			assertEquals(e4.getNextEdgeInGraph(), e5);
			assertEquals(e5.getPrevEdgeInGraph(), e4);
			assertEquals(e5.getNextEdgeInGraph(), e6);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			// <e1, e4, e2, e5, e6,...>
			assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
			assertEquals(e1.getPrevEdgeInGraph(), null);
			assertEquals(e1.getNextEdgeInGraph(), e4);
			assertEquals(e4.getPrevEdgeInGraph(), e1);
			assertEquals(e4.getNextEdgeInGraph(), e2);
			assertEquals(e2.getPrevEdgeInGraph(), e4);
			assertEquals(e2.getNextEdgeInGraph(), e5);
			assertEquals(e5.getPrevEdgeInGraph(), e2);
			assertEquals(e5.getNextEdgeInGraph(), e6);
			assertTrue(!e3.isValid());
		} catch (CommitFailedException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Merging changes in Eseq (in parallel). Implicit change in
	 * <code>readWriteTransaction2</code>.
	 * 
	 * @see dpthesis Tab 4.14
	 */
	@Test
	public void testMergeEseq1Parallel1() {
		internalMergeEseq1Parallel(readWriteTransaction2);
	}

	/**
	 * Merging changes in Eseq (in parallel). Implicit change in
	 * <code>readWriteTransaction2</code>.
	 * 
	 * @see dpthesis Tab 4.14
	 */
	@Test
	public void testMergeEseq1Parallel2() {
		internalMergeEseq1Parallel(readWriteTransaction1);
	}

	private void internalMergeEseq1Parallel(final Transaction lastToCommit) {
		final Edge e1 = motorwayMap.getEdge(1);
		final Edge e2 = motorwayMap.getEdge(2);
		final Edge e3 = motorwayMap.getEdge(3);
		final Edge e4 = motorwayMap.getEdge(4);
		final Edge e5 = motorwayMap.getEdge(5);
		final Edge e6 = motorwayMap.getEdge(6);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				Edge e1 = motorwayMap.getEdge(1);
				Edge e2 = motorwayMap.getEdge(2);
				Edge e3 = motorwayMap.getEdge(3);
				Edge e4 = motorwayMap.getEdge(4);
				Edge e5 = motorwayMap.getEdge(5);
				Edge e6 = motorwayMap.getEdge(6);
				// Eseq at beginning <e1, e2, e3, e4, e5, e6,...>
				assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
				assertEquals(e1.getPrevEdgeInGraph(), null);
				assertEquals(e1.getNextEdgeInGraph(), e2);
				assertEquals(e2.getPrevEdgeInGraph(), e1);
				assertEquals(e2.getNextEdgeInGraph(), e3);
				assertEquals(e3.getPrevEdgeInGraph(), e2);
				assertEquals(e3.getNextEdgeInGraph(), e4);
				assertEquals(e4.getPrevEdgeInGraph(), e3);
				assertEquals(e4.getNextEdgeInGraph(), e5);
				assertEquals(e5.getPrevEdgeInGraph(), e4);
				assertEquals(e5.getNextEdgeInGraph(), e6);

				e4.putAfterInGraph(e1);
				// <e1, e4, e2, e3, e5, e6,...>
				assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
				assertEquals(e1.getPrevEdgeInGraph(), null);
				assertEquals(e1.getNextEdgeInGraph(), e4);
				assertEquals(e4.getPrevEdgeInGraph(), e1);
				assertEquals(e4.getNextEdgeInGraph(), e2);
				assertEquals(e2.getPrevEdgeInGraph(), e4);
				assertEquals(e2.getNextEdgeInGraph(), e3);
				assertEquals(e3.getPrevEdgeInGraph(), e2);
				assertEquals(e3.getNextEdgeInGraph(), e5);
				assertEquals(e5.getPrevEdgeInGraph(), e3);
				assertEquals(e5.getNextEdgeInGraph(), e6);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e21) {
					e21.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				e3.delete();
				assertTrue(!e3.isValid());
				// <e1, e2, e4, e5, e6,...>
				assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
				assertEquals(e1.getPrevEdgeInGraph(), null);
				assertEquals(e1.getNextEdgeInGraph(), e2);
				assertEquals(e2.getPrevEdgeInGraph(), e1);
				assertEquals(e2.getNextEdgeInGraph(), e4);
				assertEquals(e4.getPrevEdgeInGraph(), e2);
				assertEquals(e4.getNextEdgeInGraph(), e5);
				assertEquals(e5.getPrevEdgeInGraph(), e4);
				assertEquals(e5.getNextEdgeInGraph(), e6);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
		// <e1, e4, e2, e5, e6,...>
		assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
		assertEquals(e1.getPrevEdgeInGraph(), null);
		assertEquals(e1.getNextEdgeInGraph(), e4);
		assertEquals(e4.getPrevEdgeInGraph(), e1);
		assertEquals(e4.getNextEdgeInGraph(), e2);
		assertEquals(e2.getPrevEdgeInGraph(), e4);
		assertEquals(e2.getNextEdgeInGraph(), e5);
		assertEquals(e5.getPrevEdgeInGraph(), e2);
		assertEquals(e5.getNextEdgeInGraph(), e6);
		assertTrue(!e3.isValid());
	}

	/**
	 * Both transactions changing explicitly positions of edges which are
	 * respectively different. Merging should work.
	 * 
	 * @see dpthesis 4.15
	 */
	@Test
	public void testMergeEseq2() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Edge e1 = motorwayMap.getEdge(1);
			Edge e2 = motorwayMap.getEdge(2);
			Edge e3 = motorwayMap.getEdge(3);
			Edge e4 = motorwayMap.getEdge(4);
			Edge e5 = motorwayMap.getEdge(5);
			Edge e6 = motorwayMap.getEdge(6);
			// Eseq at beginning <e1, e2, e3, e4, e5, e6,...>
			assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
			assertEquals(e1.getPrevEdgeInGraph(), null);
			assertEquals(e1.getNextEdgeInGraph(), e2);
			assertEquals(e2.getPrevEdgeInGraph(), e1);
			assertEquals(e2.getNextEdgeInGraph(), e3);
			assertEquals(e3.getPrevEdgeInGraph(), e2);
			assertEquals(e3.getNextEdgeInGraph(), e4);
			assertEquals(e4.getPrevEdgeInGraph(), e3);
			assertEquals(e4.getNextEdgeInGraph(), e5);
			assertEquals(e5.getPrevEdgeInGraph(), e4);
			assertEquals(e5.getNextEdgeInGraph(), e6);

			e4.putAfterInGraph(e2);
			// Vseq temporary <e1, e2, e4, e3, e5, e6,...>
			assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
			assertEquals(e1.getPrevEdgeInGraph(), null);
			assertEquals(e1.getNextEdgeInGraph(), e2);
			assertEquals(e2.getPrevEdgeInGraph(), e1);
			assertEquals(e2.getNextEdgeInGraph(), e4);
			assertEquals(e4.getPrevEdgeInGraph(), e2);
			assertEquals(e4.getNextEdgeInGraph(), e3);
			assertEquals(e3.getPrevEdgeInGraph(), e4);
			assertEquals(e3.getNextEdgeInGraph(), e5);
			assertEquals(e5.getPrevEdgeInGraph(), e3);
			assertEquals(e5.getNextEdgeInGraph(), e6);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			// Eseq at beginning
			assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
			assertEquals(e1.getPrevEdgeInGraph(), null);
			assertEquals(e1.getNextEdgeInGraph(), e2);
			assertEquals(e2.getPrevEdgeInGraph(), e1);
			assertEquals(e2.getNextEdgeInGraph(), e3);
			assertEquals(e3.getPrevEdgeInGraph(), e2);
			assertEquals(e3.getNextEdgeInGraph(), e4);
			assertEquals(e4.getPrevEdgeInGraph(), e3);
			assertEquals(e4.getNextEdgeInGraph(), e5);
			assertEquals(e5.getPrevEdgeInGraph(), e4);
			assertEquals(e5.getNextEdgeInGraph(), e6);
			e1.putAfterInGraph(e3);
			// <e2, e3, e1, e4, e5, e6,...>
			assertEquals(motorwayMap.getFirstEdgeInGraph(), e2);
			assertEquals(e2.getPrevEdgeInGraph(), null);
			assertEquals(e2.getNextEdgeInGraph(), e3);
			assertEquals(e3.getPrevEdgeInGraph(), e2);
			assertEquals(e3.getNextEdgeInGraph(), e1);
			assertEquals(e1.getPrevEdgeInGraph(), e3);
			assertEquals(e1.getNextEdgeInGraph(), e4);
			assertEquals(e4.getPrevEdgeInGraph(), e1);
			assertEquals(e4.getNextEdgeInGraph(), e5);
			assertEquals(e5.getPrevEdgeInGraph(), e4);
			assertEquals(e5.getNextEdgeInGraph(), e6);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			// Eseq at BOT <e1, e2, e4, e3, e5, e6,...>
			assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
			assertEquals(e1.getPrevEdgeInGraph(), null);
			assertEquals(e1.getNextEdgeInGraph(), e2);
			assertEquals(e2.getPrevEdgeInGraph(), e1);
			assertEquals(e2.getNextEdgeInGraph(), e4);
			assertEquals(e4.getPrevEdgeInGraph(), e2);
			assertEquals(e4.getNextEdgeInGraph(), e3);
			assertEquals(e3.getPrevEdgeInGraph(), e4);
			assertEquals(e3.getNextEdgeInGraph(), e5);
			assertEquals(e5.getPrevEdgeInGraph(), e3);
			assertEquals(e5.getNextEdgeInGraph(), e6);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			// <e2, e4, e3, e1, e5, e6,...>
			assertEquals(motorwayMap.getFirstEdgeInGraph(), e2);
			assertEquals(e2.getPrevEdgeInGraph(), null);
			assertEquals(e2.getNextEdgeInGraph(), e4);
			assertEquals(e4.getPrevEdgeInGraph(), e2);
			assertEquals(e4.getNextEdgeInGraph(), e3);
			assertEquals(e3.getPrevEdgeInGraph(), e4);
			assertEquals(e3.getNextEdgeInGraph(), e1);
			assertEquals(e1.getPrevEdgeInGraph(), e3);
			assertEquals(e1.getNextEdgeInGraph(), e5);
			assertEquals(e5.getPrevEdgeInGraph(), e1);
			assertEquals(e5.getNextEdgeInGraph(), e6);
		} catch (CommitFailedException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Both transactions changing explicitly positions of edges which are
	 * respectively different (in parallel). Merging should work.
	 * 
	 * @see dpthesis 4.15
	 */
	@Test
	public void testMergeEseq2Parallel1() {
		internalMergeEseq2Parallel(readWriteTransaction2);
	}

	/**
	 * Both transactions changing explicitly positions of edges which are
	 * respectively different (in parallel). Merging should work.
	 * 
	 * @see dpthesis 4.15
	 */
	@Test
	public void testMergeEseq2Parallel2() {
		internalMergeEseq2Parallel(readWriteTransaction1);
	}

	private void internalMergeEseq2Parallel(final Transaction lastToCommit) {
		final Edge e1 = motorwayMap.getEdge(1);
		final Edge e2 = motorwayMap.getEdge(2);
		final Edge e3 = motorwayMap.getEdge(3);
		final Edge e4 = motorwayMap.getEdge(4);
		final Edge e5 = motorwayMap.getEdge(5);
		final Edge e6 = motorwayMap.getEdge(6);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				Edge e1 = motorwayMap.getEdge(1);
				Edge e2 = motorwayMap.getEdge(2);
				Edge e3 = motorwayMap.getEdge(3);
				Edge e4 = motorwayMap.getEdge(4);
				Edge e5 = motorwayMap.getEdge(5);
				Edge e6 = motorwayMap.getEdge(6);
				// Eseq at beginning <e1, e2, e3, e4, e5, e6,...>
				assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
				assertEquals(e1.getPrevEdgeInGraph(), null);
				assertEquals(e1.getNextEdgeInGraph(), e2);
				assertEquals(e2.getPrevEdgeInGraph(), e1);
				assertEquals(e2.getNextEdgeInGraph(), e3);
				assertEquals(e3.getPrevEdgeInGraph(), e2);
				assertEquals(e3.getNextEdgeInGraph(), e4);
				assertEquals(e4.getPrevEdgeInGraph(), e3);
				assertEquals(e4.getNextEdgeInGraph(), e5);
				assertEquals(e5.getPrevEdgeInGraph(), e4);
				assertEquals(e5.getNextEdgeInGraph(), e6);

				e4.putAfterInGraph(e2);
				// Vseq temporary <e1, e2, e4, e3, e5, e6,...>
				assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
				assertEquals(e1.getPrevEdgeInGraph(), null);
				assertEquals(e1.getNextEdgeInGraph(), e2);
				assertEquals(e2.getPrevEdgeInGraph(), e1);
				assertEquals(e2.getNextEdgeInGraph(), e4);
				assertEquals(e4.getPrevEdgeInGraph(), e2);
				assertEquals(e4.getNextEdgeInGraph(), e3);
				assertEquals(e3.getPrevEdgeInGraph(), e4);
				assertEquals(e3.getNextEdgeInGraph(), e5);
				assertEquals(e5.getPrevEdgeInGraph(), e3);
				assertEquals(e5.getNextEdgeInGraph(), e6);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				// Eseq at beginning
				assertEquals(motorwayMap.getFirstEdgeInGraph(), e1);
				assertEquals(e1.getPrevEdgeInGraph(), null);
				assertEquals(e1.getNextEdgeInGraph(), e2);
				assertEquals(e2.getPrevEdgeInGraph(), e1);
				assertEquals(e2.getNextEdgeInGraph(), e3);
				assertEquals(e3.getPrevEdgeInGraph(), e2);
				assertEquals(e3.getNextEdgeInGraph(), e4);
				assertEquals(e4.getPrevEdgeInGraph(), e3);
				assertEquals(e4.getNextEdgeInGraph(), e5);
				assertEquals(e5.getPrevEdgeInGraph(), e4);
				assertEquals(e5.getNextEdgeInGraph(), e6);
				e1.putAfterInGraph(e3);
				// <e2, e3, e1, e4, e5, e6,...>
				assertEquals(motorwayMap.getFirstEdgeInGraph(), e2);
				assertEquals(e2.getPrevEdgeInGraph(), null);
				assertEquals(e2.getNextEdgeInGraph(), e3);
				assertEquals(e3.getPrevEdgeInGraph(), e2);
				assertEquals(e3.getNextEdgeInGraph(), e1);
				assertEquals(e1.getPrevEdgeInGraph(), e3);
				assertEquals(e1.getNextEdgeInGraph(), e4);
				assertEquals(e4.getPrevEdgeInGraph(), e1);
				assertEquals(e4.getNextEdgeInGraph(), e5);
				assertEquals(e5.getPrevEdgeInGraph(), e4);
				assertEquals(e5.getNextEdgeInGraph(), e6);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assert (lastTransactionCommitted == lastToCommit);
		readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
		// <e2, e4, e3, e1, e5, e6,...>
		assertEquals(motorwayMap.getFirstEdgeInGraph(), e2);
		assertEquals(e2.getPrevEdgeInGraph(), null);
		assertEquals(e2.getNextEdgeInGraph(), e4);
		assertEquals(e4.getPrevEdgeInGraph(), e2);
		assertEquals(e4.getNextEdgeInGraph(), e3);
		assertEquals(e3.getPrevEdgeInGraph(), e4);
		assertEquals(e3.getNextEdgeInGraph(), e1);
		assertEquals(e1.getPrevEdgeInGraph(), e3);
		assertEquals(e1.getNextEdgeInGraph(), e5);
		assertEquals(e5.getPrevEdgeInGraph(), e1);
		assertEquals(e5.getNextEdgeInGraph(), e6);
	}

	/**
	 * Both transactions changing positions of e4 in Iseq(v1). Conflict must be
	 * detected.
	 * 
	 * @see dpthesis Tab. 4.19
	 */
	@Test
	public void testConflictIseq1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Edge e2 = motorwayMap.getEdge(2);
			Edge e4 = motorwayMap.getEdge(4);
			e4.putEdgeAfter(e2);
			assertTrue(e4.isAfter(e2));
			assertEquals(e4.getPrevEdge(), e2);
			assertEquals(e2.getNextEdge(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			Edge e1 = motorwayMap.getEdge(1);
			e4.putEdgeAfter(e1);
			assertTrue(e4.isAfter(e1));
			assertEquals(e4.getPrevEdge(), e1);
			assertEquals(e1.getNextEdge(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictIseq1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Both transactions changing positions of e4 in Iseq(v1) (in parallel).
	 * Conflict must be detected.
	 * 
	 * @see dpthesis Tab. 4.19
	 */
	@Test
	public void testConflictIseq1Parallel1() {
		internalConflictIseq1Parallel(readWriteTransaction2, "1");
	}

	/**
	 * Both transactions changing positions of e4 in Iseq(v1) (in parallel).
	 * Conflict must be detected.
	 * 
	 * @see dpthesis Tab. 4.19
	 */
	@Test
	public void testConflictIseq1Parallel2() {
		internalConflictIseq1Parallel(readWriteTransaction1, "2");
	}

	private void internalConflictIseq1Parallel(final Transaction lastToCommit,
			String nameSuffix) {
		final Edge e2 = motorwayMap.getEdge(2);
		final Edge e4 = motorwayMap.getEdge(4);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				e4.putEdgeAfter(e2);
				assertTrue(e4.isAfter(e2));
				assertEquals(e4.getPrevEdge(), e2);
				assertEquals(e2.getNextEdge(), e4);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				Edge e1 = motorwayMap.getEdge(1);
				e4.putEdgeAfter(e1);
				assertTrue(e4.isAfter(e1));
				assertEquals(e4.getPrevEdge(), e1);
				assertEquals(e1.getNextEdge(), e4);

				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testConflictVseq1" + nameSuffix + "Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * In both transactions the position of Edge e2 in Iseq(v1) is changed
	 * explicitly. Conflict must be detected.
	 * 
	 * Note: test in parallel missing, because this causes only a conflict if
	 * <code>readWriteTransaction1</code> commits before
	 * <code>readWriteTransaction2</code>.
	 * 
	 * @see dpthesis Tab 4.20
	 */
	@Test
	public void testConflictIseq2() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Edge e2 = motorwayMap.getEdge(2);
			Edge e4 = motorwayMap.getEdge(4);
			e4.putEdgeAfter(e2);
			assertTrue(e4.isAfter(e2));
			assertEquals(e4.getPrevEdge(), e2);
			assertEquals(e2.getNextEdge(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			Edge e5 = motorwayMap.getEdge(5);
			e2.putEdgeAfter(e5);
			assertTrue(e2.isAfter(e5));
			assertEquals(e2.getPrevEdge(), e5);
			assertEquals(e5.getNextEdge(), e2);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictIseq2 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Changes within <code>readWriteTransaction1</code> become Lost Update,
	 * because <code>readWriteTransaction2</code> deletes v1.
	 * 
	 * @see dpthesis Tab 4.23
	 */
	@Test
	public void testConflictIseq3() {
		try {
			Edge e2 = motorwayMap.getEdge(2);
			Edge e4 = motorwayMap.getEdge(4);
			Vertex incidentVertex = e2.getAlpha();

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			e4.putEdgeAfter(e2);
			assertTrue(e4.isAfter(e2));
			assertEquals(e4.getPrevEdge(), e2);
			assertEquals(e2.getNextEdge(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			incidentVertex.delete();
			assertTrue(!e4.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictIseq3 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes v1 whose incidence list has
	 * been explicitly changed in <code>readWriteTransaction1</code> (in
	 * parallel).
	 * 
	 * @see dpthesis Tab 4.24
	 */
	@Test
	public void testConflictIseq3Parallel() {
		internalConflictIseq3And4Parallel(readWriteTransaction1, "3");
	}

	/**
	 * v1 phantom for <code>readWriteTransaction2</code>
	 * 
	 * @see dpthesis Tab 4.24
	 */
	@Test
	public void testConflictIseq4() {
		try {
			Edge e2 = motorwayMap.getEdge(2);
			Edge e4 = motorwayMap.getEdge(4);
			Vertex incidentVertex = e2.getAlpha();
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			incidentVertex.delete();
			assertTrue(!e4.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			e4.putEdgeAfter(e2);
			assertTrue(e4.isAfter(e2));
			assertEquals(e4.getPrevEdge(), e2);
			assertEquals(e2.getNextEdge(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictIseq4 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes v1 whose incidence list has
	 * been explicitly changed in <code>readWriteTransaction1</code> (in
	 * parallel).
	 * 
	 * @see dpthesis Tab 4.24
	 */
	@Test
	public void testConflictIseq4Parallel() {
		internalConflictIseq3And4Parallel(readWriteTransaction1, "4");
	}

	private void internalConflictIseq3And4Parallel(
			final Transaction lastToCommit, String nameSuffix) {
		final Edge e2 = motorwayMap.getEdge(2);
		final Edge e4 = motorwayMap.getEdge(4);
		final Vertex incidentVertex = e2.getAlpha();

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				incidentVertex.delete();
				assertTrue(!e4.isValid());
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				e4.putEdgeAfter(e2);
				assertTrue(e4.isAfter(e2));
				assertEquals(e4.getPrevEdge(), e2);
				assertEquals(e2.getNextEdge(), e4);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testConflictIseq" + nameSuffix + "Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Changes within <code>readWriteTransaction1</code> become Lost Update,
	 * because <code>readWriteTransaction2</code> deletes v1.
	 * 
	 * @see dpthesis Tab 4.24
	 */
	@Test
	public void testConflictIseq5() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Edge e2 = motorwayMap.getEdge(2);
			Edge e4 = motorwayMap.getEdge(4);
			e4.putEdgeAfter(e2);
			assertTrue(e4.isAfter(e2));
			assertEquals(e4.getPrevEdge(), e2);
			assertEquals(e2.getNextEdge(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			motorwayMap.deleteEdge(e4);
			assertTrue(!e4.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictIseq5 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes e4 whose position has been
	 * explicitly changed in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.24
	 */
	@Test
	public void testConflictIseq5Parallel() {
		internalConflictIseq5And6Parallel(readWriteTransaction2, "5");
	}

	/**
	 * e4 phantom for <code>readWriteTransaction2</code>
	 * 
	 * @see dpthesis Tab 4.24
	 */
	@Test
	public void testConflictIseq6() {
		try {
			Edge e2 = motorwayMap.getEdge(2);
			Edge e4 = motorwayMap.getEdge(4);
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			e4.delete();
			assertTrue(!e4.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			e4.putEdgeAfter(e2);
			assertTrue(e4.isAfter(e2));
			assertEquals(e4.getPrevEdge(), e2);
			assertEquals(e2.getNextEdge(), e4);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testConflictIseq6 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes e4 whose position has been
	 * explicitly changed in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.24
	 */
	@Test
	public void testConflictIseq6Parallel() {
		internalConflictIseq5And6Parallel(readWriteTransaction1, "6");
	}

	private void internalConflictIseq5And6Parallel(
			final Transaction lastToCommit, String nameSuffix) {
		final Edge e2 = motorwayMap.getEdge(2);
		final Edge e4 = motorwayMap.getEdge(4);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				e4.putEdgeAfter(e2);
				assertTrue(e4.isAfter(e2));
				assertEquals(e4.getPrevEdge(), e2);
				assertEquals(e2.getNextEdge(), e4);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				motorwayMap.deleteEdge(e4);
				assertTrue(!e4.isValid());
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testConflictIseq" + nameSuffix + "Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Implicit change in Iseq(v1).
	 * 
	 * @see dpthesis 4.21
	 */
	@Test
	public void testMergeIseq1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Edge e1 = motorwayMap.getEdge(1);
			Edge e2 = motorwayMap.getEdge(2);
			Edge e3 = motorwayMap.getEdge(3);
			Edge e4 = motorwayMap.getEdge(4);
			Edge e5 = motorwayMap.getEdge(5);
			Edge e6 = motorwayMap.getEdge(6);
			Vertex incidentVertex = e1.getAlpha();
			// Iseq(v1) at beginning <+e1, +e2, +e3, +e4, +e5, +e6,...>
			assertEquals(incidentVertex.getFirstEdge(), e1);
			assertEquals(e1.getPrevEdge(), null);
			assertEquals(e1.getNextEdge(), e2);
			assertEquals(e2.getPrevEdge(), e1);
			assertEquals(e2.getNextEdge(), e3);
			assertEquals(e3.getPrevEdge(), e2);
			assertEquals(e3.getNextEdge(), e4);
			assertEquals(e4.getPrevEdge(), e3);
			assertEquals(e4.getNextEdge(), e5);
			assertEquals(e5.getPrevEdge(), e4);
			assertEquals(e5.getNextEdge(), e6);

			e4.putEdgeAfter(e1);
			// <+e1, +e4, +e2, +e3, +e5, +e6,...>
			assertEquals(incidentVertex.getFirstEdge(), e1);
			assertEquals(e1.getPrevEdge(), null);
			assertEquals(e1.getNextEdge(), e4);
			assertEquals(e4.getPrevEdge(), e1);
			assertEquals(e4.getNextEdge(), e2);
			assertEquals(e2.getPrevEdge(), e4);
			assertEquals(e2.getNextEdge(), e3);
			assertEquals(e3.getPrevEdge(), e2);
			assertEquals(e3.getNextEdge(), e5);
			assertEquals(e5.getPrevEdge(), e3);
			assertEquals(e5.getNextEdge(), e6);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			e3.delete();
			assertTrue(!e3.isValid());
			// <+e1, +e2, +e4, +e5, +e6,...>
			assertEquals(incidentVertex.getFirstEdge(), e1);
			assertEquals(e1.getPrevEdge(), null);
			assertEquals(e1.getNextEdge(), e2);
			assertEquals(e2.getPrevEdge(), e1);
			assertEquals(e2.getNextEdge(), e4);
			assertEquals(e4.getPrevEdge(), e2);
			assertEquals(e4.getNextEdge(), e5);
			assertEquals(e5.getPrevEdge(), e4);
			assertEquals(e5.getNextEdge(), e6);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			// <+e1, +e4, +e2, +e5, +e6,...>
			assertEquals(incidentVertex.getFirstEdge(), e1);
			assertEquals(e1.getPrevEdge(), null);
			assertEquals(e1.getNextEdge(), e4);
			assertEquals(e4.getPrevEdge(), e1);
			assertEquals(e4.getNextEdge(), e2);
			assertEquals(e2.getPrevEdge(), e4);
			assertEquals(e2.getNextEdge(), e5);
			assertEquals(e5.getPrevEdge(), e2);
			assertEquals(e5.getNextEdge(), e6);
			assertTrue(!e3.isValid());
		} catch (CommitFailedException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Implicit change in Iseq(v1).
	 * 
	 * @see dpthesis Tab 4.21
	 */
	@Test
	public void testMergeIseq1Parallel1() {
		internalMergeIseq1Parallel(readWriteTransaction2);
	}

	/**
	 * Implicit change in Iseq(v1).
	 * 
	 * @see dpthesis Tab 4.21
	 */
	@Test
	public void testMergeIseq1Parallel2() {
		internalMergeIseq1Parallel(readWriteTransaction1);
	}

	private void internalMergeIseq1Parallel(final Transaction lastToCommit) {
		final Edge e1 = motorwayMap.getEdge(1);
		final Edge e2 = motorwayMap.getEdge(2);
		final Edge e3 = motorwayMap.getEdge(3);
		final Edge e4 = motorwayMap.getEdge(4);
		final Edge e5 = motorwayMap.getEdge(5);
		final Edge e6 = motorwayMap.getEdge(6);
		final Vertex incidentVertex = e1.getAlpha();

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				// Iseq(v1) at beginning <+e1, +e2, +e3, +e4, +e5, +e6,...>
				assertEquals(incidentVertex.getFirstEdge(), e1);
				assertEquals(e1.getPrevEdge(), null);
				assertEquals(e1.getNextEdge(), e2);
				assertEquals(e2.getPrevEdge(), e1);
				assertEquals(e2.getNextEdge(), e3);
				assertEquals(e3.getPrevEdge(), e2);
				assertEquals(e3.getNextEdge(), e4);
				assertEquals(e4.getPrevEdge(), e3);
				assertEquals(e4.getNextEdge(), e5);
				assertEquals(e5.getPrevEdge(), e4);
				assertEquals(e5.getNextEdge(), e6);

				e4.putEdgeAfter(e1);
				// <+e1, +e4, +e2, +e3, +e5, +e6,...>
				assertEquals(incidentVertex.getFirstEdge(), e1);
				assertEquals(e1.getPrevEdge(), null);
				assertEquals(e1.getNextEdge(), e4);
				assertEquals(e4.getPrevEdge(), e1);
				assertEquals(e4.getNextEdge(), e2);
				assertEquals(e2.getPrevEdge(), e4);
				assertEquals(e2.getNextEdge(), e3);
				assertEquals(e3.getPrevEdge(), e2);
				assertEquals(e3.getNextEdge(), e5);
				assertEquals(e5.getPrevEdge(), e3);
				assertEquals(e5.getNextEdge(), e6);

				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				e3.delete();
				assertTrue(!e3.isValid());
				// <+e1, +e2, +e4, +e5, +e6,...>
				assertEquals(incidentVertex.getFirstEdge(), e1);
				assertEquals(e1.getPrevEdge(), null);
				assertEquals(e1.getNextEdge(), e2);
				assertEquals(e2.getPrevEdge(), e1);
				assertEquals(e2.getNextEdge(), e4);
				assertEquals(e4.getPrevEdge(), e2);
				assertEquals(e4.getNextEdge(), e5);
				assertEquals(e5.getPrevEdge(), e4);
				assertEquals(e5.getNextEdge(), e6);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
		// <+e1, +e4, +e2, +e5, +e6,...>
		assertEquals(incidentVertex.getFirstEdge(), e1);
		assertEquals(e1.getPrevEdge(), null);
		assertEquals(e1.getNextEdge(), e4);
		assertEquals(e4.getPrevEdge(), e1);
		assertEquals(e4.getNextEdge(), e2);
		assertEquals(e2.getPrevEdge(), e4);
		assertEquals(e2.getNextEdge(), e5);
		assertEquals(e5.getPrevEdge(), e2);
		assertEquals(e5.getNextEdge(), e6);
		assertTrue(!e3.isValid());
	}

	/**
	 * Both transactions changing explicitly positions of incidences which are
	 * respectively different. Merging should work.
	 * 
	 * @see dpthesis 4.22
	 */
	@Test
	public void testMergeIseq2() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Edge e1 = motorwayMap.getEdge(1);
			Edge e2 = motorwayMap.getEdge(2);
			Edge e3 = motorwayMap.getEdge(3);
			Edge e4 = motorwayMap.getEdge(4);
			Edge e5 = motorwayMap.getEdge(5);
			Edge e6 = motorwayMap.getEdge(6);
			Vertex incidentVertex = e1.getAlpha();
			// Iseq(v1) at beginning <+e1, +e2, +e3, +e4, +e5, +e6,...>
			assertEquals(incidentVertex.getFirstEdge(), e1);
			assertEquals(e1.getPrevEdge(), null);
			assertEquals(e1.getNextEdge(), e2);
			assertEquals(e2.getPrevEdge(), e1);
			assertEquals(e2.getNextEdge(), e3);
			assertEquals(e3.getPrevEdge(), e2);
			assertEquals(e3.getNextEdge(), e4);
			assertEquals(e4.getPrevEdge(), e3);
			assertEquals(e4.getNextEdge(), e5);
			assertEquals(e5.getPrevEdge(), e4);
			assertEquals(e5.getNextEdge(), e6);

			e4.putEdgeAfter(e2);
			// Iseq(v1) temporary <+e1, +e2, +e3, +e4, +e5, +e6,...>
			assertEquals(incidentVertex.getFirstEdge(), e1);
			assertEquals(e1.getPrevEdge(), null);
			assertEquals(e1.getNextEdge(), e2);
			assertEquals(e2.getPrevEdge(), e1);
			assertEquals(e2.getNextEdge(), e4);
			assertEquals(e4.getPrevEdge(), e2);
			assertEquals(e4.getNextEdge(), e3);
			assertEquals(e3.getPrevEdge(), e4);
			assertEquals(e3.getNextEdge(), e5);
			assertEquals(e5.getPrevEdge(), e3);
			assertEquals(e5.getNextEdge(), e6);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			// Iseq(v1) at beginning
			assertEquals(incidentVertex.getFirstEdge(), e1);
			assertEquals(e1.getPrevEdge(), null);
			assertEquals(e1.getNextEdge(), e2);
			assertEquals(e2.getPrevEdge(), e1);
			assertEquals(e2.getNextEdge(), e3);
			assertEquals(e3.getPrevEdge(), e2);
			assertEquals(e3.getNextEdge(), e4);
			assertEquals(e4.getPrevEdge(), e3);
			assertEquals(e4.getNextEdge(), e5);
			assertEquals(e5.getPrevEdge(), e4);
			assertEquals(e5.getNextEdge(), e6);
			e1.putEdgeAfter(e3);
			// <+e2, +e3, +e1, +e4, +e5, +e6,...>
			assertEquals(incidentVertex.getFirstEdge(), e2);
			assertEquals(e2.getPrevEdge(), null);
			assertEquals(e2.getNextEdge(), e3);
			assertEquals(e3.getPrevEdge(), e2);
			assertEquals(e3.getNextEdge(), e1);
			assertEquals(e1.getPrevEdge(), e3);
			assertEquals(e1.getNextEdge(), e4);
			assertEquals(e4.getPrevEdge(), e1);
			assertEquals(e4.getNextEdge(), e5);
			assertEquals(e5.getPrevEdge(), e4);
			assertEquals(e5.getNextEdge(), e6);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			// Iseq(v1) at BOT <+e1, +e2, +e4, +e3, +e5, +e6,...>
			assertEquals(incidentVertex.getFirstEdge(), e1);
			assertEquals(e1.getPrevEdge(), null);
			assertEquals(e1.getNextEdge(), e2);
			assertEquals(e2.getPrevEdge(), e1);
			assertEquals(e2.getNextEdge(), e4);
			assertEquals(e4.getPrevEdge(), e2);
			assertEquals(e4.getNextEdge(), e3);
			assertEquals(e3.getPrevEdge(), e4);
			assertEquals(e3.getNextEdge(), e5);
			assertEquals(e5.getPrevEdge(), e3);
			assertEquals(e5.getNextEdge(), e6);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			// <+e2, +e4, +e3, +e1, +e5, +e6,...>
			assertEquals(incidentVertex.getFirstEdge(), e2);
			assertEquals(e2.getPrevEdge(), null);
			assertEquals(e2.getNextEdge(), e4);
			assertEquals(e4.getPrevEdge(), e2);
			assertEquals(e4.getNextEdge(), e3);
			assertEquals(e3.getPrevEdge(), e4);
			assertEquals(e3.getNextEdge(), e1);
			assertEquals(e1.getPrevEdge(), e3);
			assertEquals(e1.getNextEdge(), e5);
			assertEquals(e5.getPrevEdge(), e1);
			assertEquals(e5.getNextEdge(), e6);
		} catch (CommitFailedException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Both transactions changing explicitly positions of incidences which are
	 * respectively different (in parallel). Merging should work.
	 * 
	 * @see dpthesis 4.22
	 */
	@Test
	public void testMergeIseq2Parallel1() {
		internalMergeIseq2Parallel(readWriteTransaction2);
	}

	/**
	 * Both transactions changing explicitly positions of incidences which are
	 * respectively different (in parallel). Merging should work.
	 * 
	 * @see dpthesis 4.22
	 */
	@Test
	public void testMergeIseq2Parallel2() {
		internalMergeIseq2Parallel(readWriteTransaction1);
	}

	private void internalMergeIseq2Parallel(final Transaction lastToCommit) {
		final Edge e1 = motorwayMap.getEdge(1);
		final Edge e2 = motorwayMap.getEdge(2);
		final Edge e3 = motorwayMap.getEdge(3);
		final Edge e4 = motorwayMap.getEdge(4);
		final Edge e5 = motorwayMap.getEdge(5);
		final Edge e6 = motorwayMap.getEdge(6);
		final Vertex incidentVertex = e1.getAlpha();

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				// Iseq(v1) at beginning <+e1, +e2, +e3, +e4, +e5, +e6,...>
				assertEquals(incidentVertex.getFirstEdge(), e1);
				assertEquals(e1.getPrevEdge(), null);
				assertEquals(e1.getNextEdge(), e2);
				assertEquals(e2.getPrevEdge(), e1);
				assertEquals(e2.getNextEdge(), e3);
				assertEquals(e3.getPrevEdge(), e2);
				assertEquals(e3.getNextEdge(), e4);
				assertEquals(e4.getPrevEdge(), e3);
				assertEquals(e4.getNextEdge(), e5);
				assertEquals(e5.getPrevEdge(), e4);
				assertEquals(e5.getNextEdge(), e6);

				e4.putEdgeAfter(e2);
				// Iseq(v1) temporary <+e1, +e2, +e3, +e4, +e5, +e6,...>
				assertEquals(incidentVertex.getFirstEdge(), e1);
				assertEquals(e1.getPrevEdge(), null);
				assertEquals(e1.getNextEdge(), e2);
				assertEquals(e2.getPrevEdge(), e1);
				assertEquals(e2.getNextEdge(), e4);
				assertEquals(e4.getPrevEdge(), e2);
				assertEquals(e4.getNextEdge(), e3);
				assertEquals(e3.getPrevEdge(), e4);
				assertEquals(e3.getNextEdge(), e5);
				assertEquals(e5.getPrevEdge(), e3);
				assertEquals(e5.getNextEdge(), e6);

				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				// Iseq(v1) at beginning
				assertEquals(incidentVertex.getFirstEdge(), e1);
				assertEquals(e1.getPrevEdge(), null);
				assertEquals(e1.getNextEdge(), e2);
				assertEquals(e2.getPrevEdge(), e1);
				assertEquals(e2.getNextEdge(), e3);
				assertEquals(e3.getPrevEdge(), e2);
				assertEquals(e3.getNextEdge(), e4);
				assertEquals(e4.getPrevEdge(), e3);
				assertEquals(e4.getNextEdge(), e5);
				assertEquals(e5.getPrevEdge(), e4);
				assertEquals(e5.getNextEdge(), e6);
				e1.putEdgeAfter(e3);
				// <+e2, +e3, +e1, +e4, +e5, +e6,...>
				assertEquals(incidentVertex.getFirstEdge(), e2);
				assertEquals(e2.getPrevEdge(), null);
				assertEquals(e2.getNextEdge(), e3);
				assertEquals(e3.getPrevEdge(), e2);
				assertEquals(e3.getNextEdge(), e1);
				assertEquals(e1.getPrevEdge(), e3);
				assertEquals(e1.getNextEdge(), e4);
				assertEquals(e4.getPrevEdge(), e1);
				assertEquals(e4.getNextEdge(), e5);
				assertEquals(e5.getPrevEdge(), e4);
				assertEquals(e5.getNextEdge(), e6);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					fail();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assertTrue(lastTransactionCommitted == lastToCommit);
		readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
		// <+e2, +e4, +e3, +e1, +e5, +e6,...>
		assertEquals(incidentVertex.getFirstEdge(), e2);
		assertEquals(e2.getPrevEdge(), null);
		assertEquals(e2.getNextEdge(), e4);
		assertEquals(e4.getPrevEdge(), e2);
		assertEquals(e4.getNextEdge(), e3);
		assertEquals(e3.getPrevEdge(), e4);
		assertEquals(e3.getNextEdge(), e1);
		assertEquals(e1.getPrevEdge(), e3);
		assertEquals(e1.getNextEdge(), e5);
		assertEquals(e5.getPrevEdge(), e1);
		assertEquals(e5.getNextEdge(), e6);
	}

	/**
	 * Both transactions try to set different alpha-vertex for edge e2. Conflict
	 * must be detected.
	 * 
	 * @see dpthesis Tab 4.25
	 */
	@Test
	public void testConflictSetAlpha1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v11 = motorwayMap.getVertex(11);
			Edge e1 = motorwayMap.getEdge(1);
			e1.setAlpha(v11);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			Vertex v13 = motorwayMap.getVertex(13);
			e1.setAlpha(v13);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetAlphaConflict1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Both transactions try to set different alpha-vertex for edge e2 (in
	 * parallel). Conflict must be detected.
	 * 
	 * @see dpthesis Tab 4.25
	 */
	@Test
	public void testConflictSetAlpha1Parallel1() {
		internalConflictSetAlpha1Parallel(readWriteTransaction2);
	}

	/**
	 * Both transactions try to set different alpha-vertex for edge e2 (in
	 * parallel). Conflict must be detected.
	 * 
	 * @see dpthesis Tab 4.25
	 */
	@Test
	public void testConflictSetAlpha1Parallel2() {
		internalConflictSetAlpha1Parallel(readWriteTransaction1);
	}

	private void internalConflictSetAlpha1Parallel(
			final Transaction lastToCommit) {
		final Edge e1 = motorwayMap.getEdge(1);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				Vertex v11 = motorwayMap.getVertex(11);
				e1.setAlpha(v11);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				Vertex v13 = motorwayMap.getVertex(13);
				e1.setAlpha(v13);

				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assert (lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testSetAlphaConflict1Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Lost update in <code>readWriteTransaction1</code>, because
	 * <code>readWriteTransaction2</code> deletes e2.
	 * 
	 * @see dpthesis Tab 4.27
	 */
	@Test
	public void testConflictSetAlpha2() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v11 = motorwayMap.getVertex(11);
			Edge e1 = motorwayMap.getEdge(1);
			e1.setAlpha(v11);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			e1.delete();
			assertTrue(!e1.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetAlphaConflict2 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes e1 whose alpha vertex is
	 * changed in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.27
	 */
	@Test
	public void testConflictSetAlpha2Parallel() {
		internalConflictSetAlpha2And3Parallel(readWriteTransaction1, "2");
	}

	/**
	 * e1 phantom in <code>readWriteTransaction2</code> .
	 * 
	 * @see dpthesis Tab 4.27
	 */
	@Test
	public void testConflictSetAlpha3() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v11 = motorwayMap.getVertex(11);
			Edge e1 = motorwayMap.getEdge(1);
			e1.delete();
			assertTrue(!e1.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			e1.setAlpha(v11);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetAlphaConflict3 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes e1 whose alpha vertex is
	 * changed in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.27
	 */
	@Test
	public void testConflictSetAlpha3Parallel() {
		internalConflictSetAlpha2And3Parallel(readWriteTransaction1, "3");
	}

	private void internalConflictSetAlpha2And3Parallel(
			final Transaction lastToCommit, String nameSuffix) {
		final Edge e1 = motorwayMap.getEdge(1);
		final Vertex v11 = motorwayMap.getVertex(11);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				e1.setAlpha(v11);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				e1.delete();
				assertTrue(!e1.isValid());

				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assert (lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testSetAlphaConflict" + nameSuffix
				+ "Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Lost update in <code>readWriteTransaction1</code>, because
	 * <code>readWriteTransaction2</code> deletes v11.
	 * 
	 * @see dpthesis Tab 4.28
	 */
	@Test
	public void testConflictSetAlpha4() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v11 = motorwayMap.getVertex(11);
			Edge e1 = motorwayMap.getEdge(1);
			e1.setAlpha(v11);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			v11.delete();
			assertTrue(!v11.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetAlphaConflict4 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes v11 which has been set as
	 * alpha-vertex of e1 in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.28
	 */
	@Test
	public void testConflictSetAlpha4Parallel() {
		internalConflictSetAlpha4And5Parallel(readWriteTransaction2, "4");
	}

	/**
	 * v11 phantom in <code>readWriteTransaction2</code> .
	 * 
	 * @see dpthesis Tab 4.28
	 */
	@Test
	public void testConflictSetAlpha5() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v11 = motorwayMap.getVertex(11);
			v11.delete();
			assertTrue(!v11.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			Edge e2 = motorwayMap.getEdge(2);
			e2.setAlpha(v11);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetAlphaConflict5 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes v11 which has been set as
	 * alpha-vertex of e1 in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.28
	 */
	@Test
	public void testConflictSetAlpha5Parallel() {
		internalConflictSetAlpha4And5Parallel(readWriteTransaction1, "5");
	}

	/**
	 * <code>readWriteTransaction2</code> deletes v11 which has been set as
	 * alpha-vertex of e1 in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * Depending on which transaction is faster, it considers Lost Update and
	 * Phantoms.
	 * 
	 * @see dpthesis Tab 4.28
	 */
	private void internalConflictSetAlpha4And5Parallel(
			final Transaction lastToCommit, String nameSuffix) {
		final Edge e1 = motorwayMap.getEdge(1);
		final Vertex v11 = motorwayMap.getVertex(11);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				e1.setAlpha(v11);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				v11.delete();
				assertTrue(!v11.isValid());

				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assert (lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testSetAlphaConflict" + nameSuffix
				+ "Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Both transactions try to set different alpha-vertex for edge e1. Conflict
	 * must be detected.
	 * 
	 * @see dpthesis Tab 4.25
	 */
	@Test
	public void testConflictSetOmega1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v10 = motorwayMap.getVertex(10);
			Edge e1 = motorwayMap.getEdge(1);
			e1.setOmega(v10);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			Vertex v12 = motorwayMap.getVertex(12);
			e1.setOmega(v12);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetOmegaConflict1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Both transactions try to set different alpha-vertex for edge e1 (in
	 * parallel). Conflict must be detected.
	 * 
	 * @see dpthesis Tab 4.25
	 */
	@Test
	public void testConflictSetOmega1Parallel1() {
		internalConflictSetOmega1Parallel(readWriteTransaction2);
	}

	/**
	 * Both transactions try to set different alpha-vertex for edge e1 (in
	 * parallel). Conflict must be detected.
	 * 
	 * @see dpthesis Tab 4.25
	 */
	@Test
	public void testConflictSetOmega1Parallel2() {
		internalConflictSetOmega1Parallel(readWriteTransaction1);
	}

	/**
	 * Both transactions try to set different alpha-vertex for edge e1 (in
	 * parallel). Conflict must be detected.
	 * 
	 * @see dpthesis Tab 4.25
	 */
	private void internalConflictSetOmega1Parallel(
			final Transaction lastToCommit) {
		final Edge e1 = motorwayMap.getEdge(1);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				Vertex v10 = motorwayMap.getVertex(10);
				e1.setOmega(v10);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				Vertex v12 = motorwayMap.getVertex(12);
				e1.setOmega(v12);

				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assert (lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testSetOmegaConflict1Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Lost update in <code>readWriteTransaction1</code>, because
	 * <code>readWriteTransaction2</code> deletes e1.
	 * 
	 * @see dpthesis Tab 4.27
	 */
	@Test
	public void testConflictSetOmega2() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v10 = motorwayMap.getVertex(10);
			Edge e1 = motorwayMap.getEdge(1);
			e1.setOmega(v10);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			e1.delete();
			assertTrue(!e1.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetOmegaConflict2 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes e1 whose alpha vertex is
	 * changed in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.27
	 */
	@Test
	public void testConflictSetOmega2Parallel() {
		internalConflictSetOmega2And3Parallel(readWriteTransaction1, "2");
	}

	/**
	 * e1 phantom in <code>readWriteTransaction2</code> .
	 * 
	 * @see dpthesis Tab 4.27
	 */
	@Test
	public void testConflictSetOmega3() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v10 = motorwayMap.getVertex(10);
			Edge e1 = motorwayMap.getEdge(1);
			e1.delete();
			assertTrue(!e1.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			e1.setOmega(v10);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetOmegaConflict3 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes e1 whose alpha vertex is
	 * changed in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.27
	 */
	@Test
	public void testConflictSetOmega3Parallel() {
		internalConflictSetOmega2And3Parallel(readWriteTransaction1, "3");
	}

	/**
	 * <code>readWriteTransaction2</code> deletes e1 whose alpha vertex is
	 * changed in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * Depending on which transaction is faster, it considers Lost Update and
	 * Phantoms.
	 * 
	 * @see dpthesis Tab 4.27
	 */
	private void internalConflictSetOmega2And3Parallel(
			final Transaction lastToCommit, String nameSuffix) {
		final Edge e1 = motorwayMap.getEdge(1);
		final Vertex v10 = motorwayMap.getVertex(10);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				e1.setOmega(v10);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				e1.delete();
				assertTrue(!e1.isValid());

				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assert (lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testSetOmegaConflict" + nameSuffix
				+ "Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Lost update in <code>readWriteTransaction1</code>, because
	 * <code>readWriteTransaction2</code> deletes v10.
	 * 
	 * @see dpthesis Tab 4.28
	 */
	@Test
	public void testConflictSetOmega4() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v10 = motorwayMap.getVertex(10);
			Edge e1 = motorwayMap.getEdge(1);
			e1.setOmega(v10);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			v10.delete();
			assertTrue(!v10.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetOmegaConflict4 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes v10 which has been set as
	 * omega-vertex of e1 in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.28
	 */
	@Test
	public void testConflictSetOmega4Parallel() {
		internalConflictSetOmega4And5Parallel(readWriteTransaction2, "4");
	}

	/**
	 * v10 phantom in <code>readWriteTransaction2</code> .
	 * 
	 * @see dpthesis Tab 4.28
	 */
	@Test
	public void testConflictSetOmega5() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v10 = motorwayMap.getVertex(10);
			v10.delete();
			assertTrue(!v10.isValid());

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			Edge e1 = motorwayMap.getEdge(1);
			e1.setOmega(v10);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetOmegaConflict5 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * <code>readWriteTransaction2</code> deletes v10 which has been set as
	 * omega-vertex of e1 in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * @see dpthesis Tab 4.28
	 */
	@Test
	public void testConflictSetOmega5Parallel() {
		internalConflictSetOmega4And5Parallel(readWriteTransaction1, "5");
	}

	/**
	 * <code>readWriteTransaction2</code> deletes v10 which has been set as
	 * omega-vertex of e1 in <code>readWriteTransaction1</code> (in parallel).
	 * 
	 * Depending on which transaction is faster, it considers Lost Update and
	 * Phantoms.
	 * 
	 * @see dpthesis Tab 4.28
	 */
	private void internalConflictSetOmega4And5Parallel(
			final Transaction lastToCommit, String nameSuffix) {
		final Edge e1 = motorwayMap.getEdge(1);
		final Vertex v10 = motorwayMap.getVertex(10);

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				e1.setOmega(v10);
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				v10.delete();
				assertTrue(!v10.isValid());

				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assert (lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testSetOmegaConflict" + nameSuffix
				+ "Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * <code>readWriteTransaction1</code> changes alpha-vertex,
	 * <code>readWriteTransaction2</code> changes omega-vertex of e1. Merge
	 * should work.
	 * 
	 * @see dpthesisTab 4.26
	 */
	@Test
	public void testMergeSetAlphaOmega() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v11 = motorwayMap.getVertex(11);
			Edge e1 = motorwayMap.getEdge(1);
			e1.setAlpha(v11);

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			Vertex v12 = motorwayMap.getVertex(12);
			e1.setOmega(v12);

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();

			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			assertTrue(e1.getAlpha() == v11);
			assertTrue(e1.getOmega() == v12);
			readOnlyTransaction.commit();
		} catch (CommitFailedException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * <code>readWriteTransaction1</code> changes alpha-vertex,
	 * <code>readWriteTransaction2</code> changes omega-vertex of e1 (in
	 * parallel). Merge should work.
	 * 
	 * @see dpthesisTab 4.26
	 */
	@Test
	public void testMergeSetAlphaOmegaParallel1() {
		internalMergeSetAlphaOmegaParallel(readWriteTransaction2);
	}

	/**
	 * <code>readWriteTransaction1</code> changes alpha-vertex,
	 * <code>readWriteTransaction2</code> changes omega-vertex of e1 (in
	 * parallel). Merge should work.
	 * 
	 * @see dpthesisTab 4.26
	 */
	@Test
	public void testMergeSetAlphaOmegaParallel2() {
		internalMergeSetAlphaOmegaParallel(readWriteTransaction1);
	}

	/**
	 * 
	 * @param lastToCommit
	 */
	private void internalMergeSetAlphaOmegaParallel(
			final Transaction lastToCommit) {
		final Edge e1 = motorwayMap.getEdge(1);
		final Vertex v11 = motorwayMap.getVertex(11);
		final Vertex v12 = motorwayMap.getVertex(12);
		try {
			thread1 = new Thread(threadGroup, "Thread1") {

				public void run() {
					motorwayMap.setCurrentTransaction(readWriteTransaction1);
					e1.setAlpha(v11);

					try {
						long sleepTime = 0;
						if (lastToCommit == motorwayMap.getCurrentTransaction())
							sleepTime = waitTimeBeforeCommit;
						Thread.sleep(sleepTime);
						readWriteTransaction1.commit();
						lastTransactionCommitted = readWriteTransaction1;
					} catch (CommitFailedException e) {
						e.printStackTrace();
						fail();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			};

			thread2 = new Thread(threadGroup, "Thread2") {
				public void run() {
					motorwayMap.setCurrentTransaction(readWriteTransaction2);
					e1.setOmega(v12);
					try {
						long sleepTime = 0;
						if (lastToCommit == motorwayMap.getCurrentTransaction())
							sleepTime = waitTimeBeforeCommit;
						Thread.sleep(sleepTime);
						readWriteTransaction2.commit();
						lastTransactionCommitted = readWriteTransaction2;
					} catch (CommitFailedException e) {
						e.printStackTrace();
						fail();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			};
			thread1.start();
			thread2.start();
			while (threadGroup.activeCount() > 0) {
			}
			assert (lastTransactionCommitted == lastToCommit);
			assertTrue(!readWriteTransaction1.isValid()
					&& !readWriteTransaction2.isValid());
			readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
			assertTrue(e1.getAlpha() == v11);
			assertTrue(e1.getOmega() == v12);
			readOnlyTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * @see dpthesis Tab 4.29
	 */
	@Test
	public void testConflictSetAttribute1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City city = motorwayMap.getFirstCity();
			city.set_name("name1");

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			city.set_name("name2");
			;

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetAttributeConflict1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * @see dpthesis Tab 4.29
	 */
	@Test
	public void testConflictSetAttribute1Parallel1() {
		internalConflictSetAttribute1Parallel(readWriteTransaction2);
	}

	/**
	 * @see dpthesis Tab 4.29
	 */
	@Test
	public void testConflictSetAttribute1Parallel2() {
		internalConflictSetAttribute1Parallel(readWriteTransaction1);
	}

	private void internalConflictSetAttribute1Parallel(
			final Transaction lastToCommit) {
		final City city = motorwayMap.getFirstCity();

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				city.set_name("name1");
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				city.set_name("name2");

				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assert (lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testSetAttributeConflict1Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	/**
	 * Lost update
	 * 
	 * @see dpthesis Tab 4.30
	 */
	@Test
	public void testConflictSetAttribute2() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City city = motorwayMap.getFirstCity();
			city.set_name("name1");

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			city.delete();

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetAttributeConflict2 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Lost update (in parallel).
	 * 
	 * <code>readWriteTransaction1</code> commits first.
	 * 
	 * @see dpthesis Tab 4.30
	 */
	@Test
	public void testConflictSetAttribute2Parallel() {
		internalConflictSetAttribute2And3Parallel(readWriteTransaction2, "2");
	}

	/**
	 * vertex phantom
	 * 
	 * @see dpthesis Tab 4.31
	 */
	@Test
	public void testConflictSetAttribute3() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City city = motorwayMap.getFirstCity();
			city.delete();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			city.set_name("name1");
			assertEquals(city.get_name(), "name1");

			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			readWriteTransaction1.commit();

			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			readWriteTransaction2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- testSetAttributeConflict3 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * vertex phantom (in parallel).
	 * 
	 * <code>readWriteTransaction2</code> commits first.
	 * 
	 * @see dpthesis Tab 4.31
	 */
	@Test
	public void setConflictAttribute3Parallel() {
		internalConflictSetAttribute2And3Parallel(readWriteTransaction1, "3");
	}

	/**
	 * 
	 * @param lastToCommit
	 * @param nameSuffix
	 */
	private void internalConflictSetAttribute2And3Parallel(
			final Transaction lastToCommit, String nameSuffix) {
		final City city = motorwayMap.getFirstCity();

		thread1 = new Thread(threadGroup, "Thread1") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction1);
				city.set_name("name1");
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction1.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction1;
			}
		};

		thread2 = new Thread(threadGroup, "Thread2") {
			public void run() {
				motorwayMap.setCurrentTransaction(readWriteTransaction2);
				city.delete();
				try {
					long sleepTime = 0;
					if (lastToCommit == motorwayMap.getCurrentTransaction())
						sleepTime = waitTimeBeforeCommit;
					Thread.sleep(sleepTime);
					readWriteTransaction2.commit();
				} catch (CommitFailedException e) {
					conflict = true;
					conflictReason = e.getMessage();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lastTransactionCommitted = readWriteTransaction2;
			}
		};
		thread1.start();
		thread2.start();
		while (threadGroup.activeCount() > 0) {
		}
		assert (lastTransactionCommitted == lastToCommit);
		System.out.println("\n- testSetAttributeConflict" + nameSuffix
				+ "Parallel -");
		System.out.println("##########################");
		System.out.println(conflictReason);
		assertTrue(conflict);
	}

	@Test
	public void changeSetWithoutSetterConflict1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City city = motorwayMap.getFirstCity();
			Set<String> set = city.getGraph().createSet(String.class);
			set.add("Test1");
			set.add("Test2");
			assertEquals(2, set.size());
			city.set_testSet(set);
			readWriteTransaction1.commit();

			Transaction t1 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t1);
			Set<String> t1_set = city.get_testSet();
			assertEquals(2, t1_set.size());
			t1_set.add("Test3");
			assertEquals(3, t1_set.size());

			Transaction t2 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t2);
			Set<String> t2_set = city.get_testSet();
			assertEquals(2, t2_set.size());
			t2_set.add("Test4");
			assertEquals(3, t2_set.size());

			motorwayMap.setCurrentTransaction(t1);
			t1.commit();
			motorwayMap.setCurrentTransaction(t2);
			t2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- changeSetWithoutSetterConflict1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	@Test
	public void changeSetWithoutSetterNoConflict1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City city = motorwayMap.getFirstCity();
			Set<String> set = city.getGraph().createSet(String.class);
			set.add("Test1");
			set.add("Test2");
			assertEquals(2, set.size());
			city.set_testSet(set);
			readWriteTransaction1.commit();

			Transaction t1 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t1);
			Set<String> t1_set = city.get_testSet();
			assertEquals(2, t1_set.size());
			t1_set.add("Test3");
			assertEquals(3, t1_set.size());

			Transaction t2 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t2);
			Set<String> t2_set = city.get_testSet();
			assertEquals(2, t2_set.size());
			t2_set.add("Test3");
			assertEquals(3, t2_set.size());

			motorwayMap.setCurrentTransaction(t1);
			t1.commit();
			motorwayMap.setCurrentTransaction(t2);
			t2.commit();
			assertTrue(true);
		} catch (CommitFailedException e) {
			System.out.println("\n- changeSetWithoutSetterNoConflict1 -");
			System.out.println("\n- This should not have happened. -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			fail();
		}
	}

	@Test
	public void changeListWithoutSetterConflict1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City city = motorwayMap.getFirstCity();
			List<TestRecord> list = motorwayMap.createList(TestRecord.class);
			list.add(motorwayMap.createTestRecord("Test1", motorwayMap
					.createList(String.class), motorwayMap
					.createSet(String.class), 3, 3, 3, true));
			list.add(motorwayMap.createTestRecord("Test2", motorwayMap
					.createList(String.class), motorwayMap
					.createSet(String.class), 3, 3, 3, true));
			assertEquals(2, list.size());
			city.set_testList(list);
			readWriteTransaction1.commit();

			Transaction t1 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t1);
			List<TestRecord> t1_list = city.get_testList();
			assertEquals(2, t1_list.size());
			t1_list.add(motorwayMap.createTestRecord("Test3", motorwayMap
					.createList(String.class), motorwayMap
					.createSet(String.class), 3, 3, 3, true));
			assertEquals(3, t1_list.size());
			Transaction t2 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t2);
			List<TestRecord> t2_list = city.get_testList();
			assertEquals(2, t2_list.size());
			t2_list.add(motorwayMap.createTestRecord("Test4", motorwayMap
					.createList(String.class), motorwayMap
					.createSet(String.class), 3, 3, 4, true));
			assertEquals(3, t2_list.size());
			motorwayMap.setCurrentTransaction(t1);
			t1.commit();
			motorwayMap.setCurrentTransaction(t2);
			t2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- changeListWithoutSetterConflict1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	@Test
	public void changeListWithoutSetterNoConflict1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City city = motorwayMap.getFirstCity();
			List<TestRecord> list = motorwayMap.createList(TestRecord.class);
			TestRecord test = motorwayMap.createTestRecord("Test1", motorwayMap
					.createList(String.class), motorwayMap
					.createSet(String.class), 3, 3, 3, true);
			list.add(test);
			list.add(motorwayMap.createTestRecord("Test2", motorwayMap
					.createList(String.class), motorwayMap
					.createSet(String.class), 3, 3, 3, true));
			assertEquals(2, list.size());
			city.set_testList(list);
			readWriteTransaction1.commit();

			Transaction t1 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t1);
			TestRecord test2 = motorwayMap.createTestRecord("Test3",
					motorwayMap.createList(String.class), motorwayMap
							.createSet(String.class), 3, 3, 3, true);
			List<TestRecord> t1List = city.get_testList();
			assertEquals(2, t1List.size());
			t1List.add(test2);
			t1List = city.get_testList();
			assertEquals(3, t1List.size());

			Transaction t2 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t2);
			TestRecord test3 = motorwayMap.createTestRecord("Test3",
					motorwayMap.createList(String.class), motorwayMap
							.createSet(String.class), 3, 3, 3, true);
			List<TestRecord> t2List = city.get_testList();
			assertEquals(2, t2List.size());
			t2List.add(test3);
			t2List = city.get_testList();
			assertEquals(3, t2List.size());

			motorwayMap.setCurrentTransaction(t1);
			t1.commit();

			motorwayMap.setCurrentTransaction(t2);
			t2.commit();
			assertTrue(true);
			// motorwayMap.newReadOnlyTransaction();
			// GraphIO.saveGraphToFile("test", motorwayMap, null);
		} catch (CommitFailedException e) {
			System.out.println("\n- changeListWithoutSetterNoConflict1 -");
			System.out.println("\n- This should not have happened. -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			fail();
			/*
			 * } catch (GraphIOException e) { System.out.println("\n-
			 * changeListWithoutSetterNoConflict1 -"); System.out.println("\n-
			 * This should not have happened. -");
			 * System.out.println("##########################");
			 * e.printStackTrace(); fail();
			 */
		}
	}

	@Test
	public void changeMapWithoutSetterConflict1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City city = motorwayMap.getFirstCity();
			Map<String, String> map = motorwayMap.createMap(String.class,
					String.class);
			map.put("Key1", "Value1");
			map.put("Key2", "Value2");
			assertEquals(2, map.size());
			assertEquals(2, map.keySet().size());
			assertEquals(2, map.values().size());
			city.set_testMap(map);
			readWriteTransaction1.commit();

			Transaction t1 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t1);
			Map<String, String> t1_map = city.get_testMap();
			assertEquals(2, t1_map.size());
			t1_map.put("Key3", "Value3");
			assertEquals(3, t1_map.size());

			Transaction t2 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t2);
			Map<String, String> t2_map = city.get_testMap();
			assertEquals(2, t2_map.size());
			t2_map.put("Key4", "Value4");
			assertEquals(3, t2_map.size());

			motorwayMap.setCurrentTransaction(t1);
			t1.commit();
			motorwayMap.setCurrentTransaction(t2);
			t2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- changeMapWithoutSetterConflict1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	@Test
	public void changeMapWithoutSetterNoConflict1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City city = motorwayMap.getFirstCity();
			Map<String, String> map = motorwayMap.createMap(String.class,
					String.class);
			map.put("Key1", "Value1");
			map.put("Key2", "Value2");
			assertEquals(2, map.size());
			assertEquals(2, map.keySet().size());
			assertEquals(2, map.values().size());
			city.set_testMap(map);
			readWriteTransaction1.commit();

			Transaction t1 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t1);
			Map<String, String> t1_map = city.get_testMap();
			assertEquals(2, t1_map.size());
			t1_map.put("Key3", "Value3");
			assertEquals(3, t1_map.size());

			Transaction t2 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t2);
			Map<String, String> t2_map = city.get_testMap();
			assertEquals(2, t2_map.size());
			t2_map.put("Key3", "Value3");
			assertEquals(3, t2_map.size());

			motorwayMap.setCurrentTransaction(t1);
			t1.commit();
			motorwayMap.setCurrentTransaction(t2);
			t2.commit();
			assertTrue(true);
		} catch (CommitFailedException e) {
			System.out.println("\n- changeMapWithoutSetterNoConflict1 -");
			System.out.println("\n- This should not have happened. -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			fail();
		}
	}

	@Test
	public void changeRecordWithoutSetterConflict1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City city = motorwayMap.getFirstCity();
			TestRecord record = motorwayMap.createTestRecord("Test1", motorwayMap
					.createList(String.class), motorwayMap
					.createSet(String.class), 3, 3, 3, true);
			city.set_testRecord(record);
			readWriteTransaction1.commit();

			Transaction t1 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t1);
			TestRecord t1_record = city.get_testRecord();
			t1_record.set_c1("Test2");

			Transaction t2 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t2);
			TestRecord t2_record = city.get_testRecord();
			t2_record.set_c1("Test3");

			motorwayMap.setCurrentTransaction(t1);
			t1.commit();
			motorwayMap.setCurrentTransaction(t2);
			t2.commit();
			fail();
		} catch (CommitFailedException e) {
			System.out.println("\n- changeRecordWithoutSetterConflict1 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}
	
	@Test
	public void changeRecordWithoutSetterNoConflict1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City city = motorwayMap.getFirstCity();
			TestRecord record = motorwayMap.createTestRecord("Test1", motorwayMap
					.createList(String.class), motorwayMap
					.createSet(String.class), 3, 3, 3, true);
			city.set_testRecord(record);
			readWriteTransaction1.commit();

			Transaction t1 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t1);
			TestRecord t1_record = city.get_testRecord();
			t1_record.set_c1("Test2");

			Transaction t2 = motorwayMap.newTransaction();
			motorwayMap.setCurrentTransaction(t2);
			TestRecord t2_record = city.get_testRecord();
			t2_record.set_c1("Test2");

			motorwayMap.setCurrentTransaction(t1);
			t1.commit();
			motorwayMap.setCurrentTransaction(t2);
			t2.commit();
			assertTrue(true);
		} catch (CommitFailedException e) {
			System.out.println("\n- changeRecordWithoutSetterNoConflict1 -");
			System.out.println("\n- This should not have happened. -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * 
	 * @return
	 */
	/*
	 * public static junit.framework.Test suite() { return new
	 * JUnit4TestAdapter(ConflictDetectionTest.class); }
	 */
}
