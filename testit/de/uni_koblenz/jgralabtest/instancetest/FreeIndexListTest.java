package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

import de.uni_koblenz.jgralab.impl.FreeIndexList;

public class FreeIndexListTest {

	/**
	 * Checks if the FreeIndexList fullfills:<br>
	 * vList.getUsed()==<code>used</code><br>
	 * vList.getFree()==<code>free</code><br>
	 * vList.runs.length==<code>runsLength</code><br>
	 * vList.runs starts with the elements of <code>runsValues</code>. The other
	 * elements of runs must be 0.
	 * 
	 * @param vList
	 * @param used
	 * @param free
	 * @param runsLength
	 * @param runsValues
	 */
	private void checkFreeIndexList(FreeIndexList vList, int used, int free,
			int runsLength, int... runsValues) {
		assertNotNull("vList is null", vList);
		assertEquals("used isn't equal", used, vList.getUsed());
		assertEquals("free isn't equal", free, vList.getFree());
		assertEquals("size isn't equal", used + free, vList.getSize());
		int[] runs = getRunsOfFreeIndexList(vList);
		assertNotNull("runs is null", runs);
		assertEquals("runs has an unexpected length", runsLength, runs.length);
		assertTrue("runsValues.length<=runs.length",
				runsValues.length <= runs.length);
		for (int i = 0; i < runs.length; i++) {
			if (i < runsValues.length) {
				assertEquals("runs[" + i + "] isn't equal", runsValues[i],
						runs[i]);
			} else {
				assertEquals("runs[" + i + "] isn't equal", 0, runs[i]);
			}
		}
	}

	/**
	 * Returns the runs-Array of <code>fil</code>.
	 * 
	 * @param fil
	 *            a FreeIndexList
	 * @return runs
	 */
	private int[] getRunsOfFreeIndexList(FreeIndexList fil) {
		try {
			Field f = fil.getClass().getDeclaredField("runs");
			f.setAccessible(true);
			return (int[]) f.get(fil);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Test
	public void testAllocateIndexInt_ASingleIdOfASingleRun() {
		FreeIndexList list = new FreeIndexList(1);
		checkFreeIndexList(list, 0, 1, 16, 1);
		list.allocateIndex(1);
		checkFreeIndexList(list, 1, 0, 16, -1);
	}

	@Test
	public void testAllocateIndexInt_ASingleIdOfFirstRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(2);
		list.allocateIndex(4);
		list.allocateIndex(6);
		list.allocateIndex(8);
		checkFreeIndexList(list, 4, 6, 16, 1, -1, 1, -1, 1, -1, 1, -1, 2);

		list.allocateIndex(1);
		checkFreeIndexList(list, 5, 5, 16, -2, 1, -1, 1, -1, 1, -1, 2);
	}

	@Test
	public void testAllocateIndexInt_ASingleIdOfMidlleRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(2);
		list.allocateIndex(4);
		list.allocateIndex(6);
		list.allocateIndex(8);
		checkFreeIndexList(list, 4, 6, 16, 1, -1, 1, -1, 1, -1, 1, -1, 2);

		list.allocateIndex(3);
		checkFreeIndexList(list, 5, 5, 16, 1, -3, 1, -1, 1, -1, 2);
	}

	@Test
	public void testAllocateIndexInt_ASingleIdOfLastRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(5);
		list.allocateIndex(7);
		list.allocateIndex(9);
		checkFreeIndexList(list, 3, 7, 16, 4, -1, 1, -1, 1, -1, 1);

		list.allocateIndex(10);
		checkFreeIndexList(list, 4, 6, 16, 4, -1, 1, -1, 1, -2);
	}

	@Test
	public void testAllocateIndexInt_FirstIdOfSingleRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(1);
		checkFreeIndexList(list, 1, 9, 16, -1, 9);
	}

	@Test
	public void testAllocateIndexInt_LastIdOfSingleRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(10);
		checkFreeIndexList(list, 1, 9, 16, 9, -1);
	}

	@Test
	public void testAllocateIndexInt_MiddleIdOfSingleRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(5);
		checkFreeIndexList(list, 1, 9, 16, 4, -1, 5);
	}

	@Test
	public void testAllocateIndexInt_FirstIdOfFirstRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(4);
		list.allocateIndex(8);
		checkFreeIndexList(list, 2, 8, 16, 3, -1, 3, -1, 2);

		list.allocateIndex(1);
		checkFreeIndexList(list, 3, 7, 16, -1, 2, -1, 3, -1, 2);
	}

	@Test
	public void testAllocateIndexInt_MiddleIdOfFirstRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(4);
		list.allocateIndex(8);
		checkFreeIndexList(list, 2, 8, 16, 3, -1, 3, -1, 2);

		list.allocateIndex(2);
		checkFreeIndexList(list, 3, 7, 16, 1, -1, 1, -1, 3, -1, 2);
	}

	@Test
	public void testAllocateIndexInt_LastIdOfFirstRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(4);
		list.allocateIndex(8);
		checkFreeIndexList(list, 2, 8, 16, 3, -1, 3, -1, 2);

		list.allocateIndex(3);
		checkFreeIndexList(list, 3, 7, 16, 2, -2, 3, -1, 2);
	}

	@Test
	public void testAllocateIndexInt_FirstIdOfMiddleRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(4);
		list.allocateIndex(8);
		checkFreeIndexList(list, 2, 8, 16, 3, -1, 3, -1, 2);

		list.allocateIndex(5);
		checkFreeIndexList(list, 3, 7, 16, 3, -2, 2, -1, 2);
	}

	@Test
	public void testAllocateIndexInt_MiddleIdOfMiddleRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(4);
		list.allocateIndex(8);
		checkFreeIndexList(list, 2, 8, 16, 3, -1, 3, -1, 2);

		list.allocateIndex(6);
		checkFreeIndexList(list, 3, 7, 16, 3, -1, 1, -1, 1, -1, 2);
	}

	@Test
	public void testAllocateIndexInt_LastIdOfMiddleRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(4);
		list.allocateIndex(8);
		checkFreeIndexList(list, 2, 8, 16, 3, -1, 3, -1, 2);

		list.allocateIndex(7);
		checkFreeIndexList(list, 3, 7, 16, 3, -1, 2, -2, 2);
	}

	@Test
	public void testAllocateIndexInt_FirstIdOfLastRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(3);
		list.allocateIndex(7);
		checkFreeIndexList(list, 2, 8, 16, 2, -1, 3, -1, 3);

		list.allocateIndex(8);
		checkFreeIndexList(list, 3, 7, 16, 2, -1, 3, -2, 2);
	}

	@Test
	public void testAllocateIndexInt_MiddleIdOfLastRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(3);
		list.allocateIndex(7);
		checkFreeIndexList(list, 2, 8, 16, 2, -1, 3, -1, 3);

		list.allocateIndex(9);
		checkFreeIndexList(list, 3, 7, 16, 2, -1, 3, -1, 1, -1, 1);
	}

	@Test
	public void testAllocateIndexInt_LastIdOfLastRun() {
		FreeIndexList list = new FreeIndexList(10);
		checkFreeIndexList(list, 0, 10, 16, 10);
		list.allocateIndex(3);
		list.allocateIndex(7);
		checkFreeIndexList(list, 2, 8, 16, 2, -1, 3, -1, 3);

		list.allocateIndex(10);
		checkFreeIndexList(list, 3, 7, 16, 2, -1, 3, -1, 2, -1);
	}

}
