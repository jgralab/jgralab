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
package de.uni_koblenz.jgralabtest.impl.trans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralab.trans.InvalidSavepointException;
import de.uni_koblenz.jgralab.trans.Savepoint;
import de.uni_koblenz.jgralab.trans.Transaction;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.City;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.Exit;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.Motorway;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMap;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMapSchema;

/**
 * Test cases for class SavepointImpl.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public class SavepointImplTest {
	private MotorwayMap motorwayMap;
	private Transaction readWriteTransaction1;
	private Transaction readWriteTransaction2;
	private Transaction readOnlyTransaction;

	private static final int V = 1;
	private static final int E = 1;

	// private final int N = 10;

	@Before
	public void setUp() {
		MotorwayMapSchema schema = MotorwayMapSchema.instance();
		motorwayMap = schema.createMotorwayMapWithTransactionSupport(V, E);
		readWriteTransaction1 = motorwayMap.newTransaction();
		readWriteTransaction2 = motorwayMap.newTransaction();
		readOnlyTransaction = motorwayMap.newReadOnlyTransaction();
	}

	@After
	public void tearDown() {
		motorwayMap = null;
	}

	/**
	 * A save-point is defined, but no transaction is active in current thread.
	 * A GraphException should be thrown.
	 */
	@Test
	public void testDefineSavepointNoCurrentTransaction() {
		try {
			readOnlyTransaction.commit();
			motorwayMap.defineSavepoint();
		} catch (GraphException e) {
			System.out.println("\n- testDefineSavepointNoCurrentTransaction -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		} catch (CommitFailedException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * <code>readOnlyTransaction</code> tries to define a save-point. Because
	 * read-only transactions should not define save-points, a GraphException is
	 * expected.
	 */
	@Test
	public void testDefineSavepointReadOnly() {
		try {
			readOnlyTransaction.defineSavepoint();
		} catch (GraphException e) {
			System.out.println("\n- testDefineSavepointReadOnly -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Three save-points are defined within <code>readWriteTransaction</code>.
	 * All three save-points should be in <code>readWriteTransaction</code>
	 * .savepointList.
	 */
	@Test
	public void testDefineAndGetSavepoints() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Savepoint sp1 = readWriteTransaction1.defineSavepoint();
			Savepoint sp2 = readWriteTransaction1.defineSavepoint();
			Savepoint sp3 = readWriteTransaction1.defineSavepoint();
			assertEquals(readWriteTransaction1.getSavepoints(), Arrays.asList(
					sp1, sp2, sp3));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * No save-points defined in <code>readWriteTransaction</code>.
	 * <code>readWriteTransaction</code>.savepointList should be empty.
	 */
	@Test
	public void testGetSavepoints() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			assertEquals(readWriteTransaction1.getSavepoints(), Arrays.asList());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Within <code>readWriteTransaction</code> a save-point sp1 is defined. For
	 * sp1 only vertex c1 is valid.
	 */
	@Test
	public void testRestoreSavepoint1() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City c1 = motorwayMap.createCity();
			Savepoint sp1 = readWriteTransaction1.defineSavepoint();
			assertTrue(c1.isValid());

			City c2 = motorwayMap.createCity();
			assertTrue(c1.isValid());
			assertTrue(c2.isValid());
			// sp1 is restored
			readWriteTransaction1.restoreSavepoint(sp1);
			City c3 = motorwayMap.createCity();
			assertTrue(c1.isValid());
			assertTrue(!c2.isValid());
			assertTrue(c3.isValid());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Within <code>readWriteTransaction</code> a save-point sp1 is defined. For
	 * sp1 only vertex c1 is valid.
	 */
	@Test
	public void testRestoreSavepoint2() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City c1 = motorwayMap.createCity();
			Savepoint sp1 = readWriteTransaction1.defineSavepoint();
			assertTrue(c1.isValid());

			City c2 = motorwayMap.createCity();
			assertTrue(c1.isValid());
			assertTrue(c2.isValid());
			// sp1 is restored
			readWriteTransaction1.restoreSavepoint(sp1);
			assertTrue(c1.isValid());
			assertTrue(!c2.isValid());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testRestoreSavepoint3() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City c1 = motorwayMap.createCity();
			Savepoint sp1 = readWriteTransaction1.defineSavepoint();
			assertTrue(c1.isValid());

			City c2 = motorwayMap.createCity();
			Savepoint sp2 = readWriteTransaction1.defineSavepoint();
			assertTrue(c1.isValid());
			assertTrue(c2.isValid());

			// restoring sp1
			readWriteTransaction1.restoreSavepoint(sp1);
			assertTrue(c1.isValid());
			assertTrue(!c2.isValid());
			assertTrue(sp1.isValid());
			assertTrue(sp2.isValid());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testRestoreSavepoint4() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City c1 = motorwayMap.createCity();
			Savepoint sp1 = readWriteTransaction1.defineSavepoint();
			assertTrue(c1.isValid());

			City c2 = motorwayMap.createCity();
			Savepoint sp2 = readWriteTransaction1.defineSavepoint();
			assertTrue(c1.isValid());
			assertTrue(c2.isValid());

			readWriteTransaction1.restoreSavepoint(sp1);
			City c3 = motorwayMap.createCity();
			assertTrue(c1.isValid());
			assertTrue(!c2.isValid());
			assertTrue(c3.isValid());
			assertTrue(sp1.isValid());
			assertTrue(!sp2.isValid());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testRestoreSavepoint5() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			City c1 = motorwayMap.createCity();
			Motorway mw1 = motorwayMap.createMotorway();
			Exit ex1 = motorwayMap.createExit(c1, mw1);
			Exit ex2 = motorwayMap.createExit(c1, mw1);
			Exit ex3 = motorwayMap.createExit(c1, mw1);
			Savepoint sp1 = readWriteTransaction1.defineSavepoint();
			assertTrue(c1.isValid());
			assertTrue(mw1.isValid());
			assertTrue(ex1.isValid());
			assertTrue(ex2.isValid());
			assertTrue(ex3.isValid());

			ex3.putIncidenceAfter(ex1);
			City c2 = motorwayMap.createCity();
			Savepoint sp2 = readWriteTransaction1.defineSavepoint();
			assertTrue(c1.isValid());
			assertTrue(c2.isValid());
			assertTrue(mw1.isValid());
			assertTrue(ex1.isValid());
			assertTrue(ex2.isValid());
			assertTrue(ex3.isValid());
			assertEquals(ex1.getNextIncidence(), ex3);
			assertEquals(ex3.getPrevIncidence(), ex1);

			City c3 = motorwayMap.createCity();
			Savepoint sp3 = readWriteTransaction1.defineSavepoint();
			assertTrue(c1.isValid());
			assertTrue(c2.isValid());
			assertTrue(c3.isValid());
			assertTrue(mw1.isValid());
			assertTrue(ex1.isValid());
			assertTrue(ex2.isValid());
			assertTrue(ex3.isValid());
			assertEquals(ex1.getNextIncidence(), ex3);
			assertEquals(ex3.getPrevIncidence(), ex1);

			readWriteTransaction1.restoreSavepoint(sp1);
			City c4 = motorwayMap.createCity();
			assertTrue(c1.isValid());
			assertTrue(!c2.isValid());
			assertTrue(mw1.isValid());
			assertTrue(ex1.isValid());
			assertTrue(ex2.isValid());
			assertTrue(ex3.isValid());
			assertTrue(!c3.isValid());
			assertTrue(c4.isValid());
			assertTrue(sp1.isValid());
			assertTrue(!sp2.isValid());
			assertTrue(sp3.isValid());
			assertEquals(ex1.getNextIncidence(), ex2);
			assertEquals(ex2.getNextIncidence(), ex3);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * 
	 */
	@Test
	public void restoreSavepoint6() {
		motorwayMap.setCurrentTransaction(readWriteTransaction1);
		Savepoint sp1 = readWriteTransaction1.defineSavepoint();
		motorwayMap.setCurrentTransaction(readWriteTransaction2);
		try {
			readWriteTransaction2.restoreSavepoint(sp1);
		} catch (InvalidSavepointException e) {
			System.out.println("\n- restoreSavepoint6 -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * 
	 */
	@Test
	public void removeSavepoint1() {
		testDefineAndGetSavepoints();
		Savepoint sp1 = readWriteTransaction1.getSavepoints().get(0);
		readWriteTransaction1.removeSavepoint(sp1);
		assertTrue(!sp1.isValid());
		assertTrue(!readWriteTransaction1.getSavepoints().contains(sp1));
	}

	/**
	 * 
	 */
	@Test
	public void removeAndRestoreSavepoint1() {
		testDefineAndGetSavepoints();
		Savepoint sp1 = readWriteTransaction1.getSavepoints().get(0);
		readWriteTransaction1.removeSavepoint(sp1);
		assertTrue(!sp1.isValid());
		assertTrue(!readWriteTransaction1.getSavepoints().contains(sp1));
		try {
			readWriteTransaction1.restoreSavepoint(sp1);
		} catch (InvalidSavepointException e) {
			System.out.println("\n- removeAndRestoreSavepoint1 -");
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
	 * JUnit4TestAdapter(SavepointImplTest.class); }
	 */
}
