package de.uni_koblenz.jgralabtest.algolib.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.algolib.util.PriorityQueue;

public class PriorityQueueTest {

	private PriorityQueue<String> pQueue;

	@Before
	public void setUp() {
		pQueue = new PriorityQueue<String>();
	}

	@Test
	public void testClear() {
		assertTrue("Clear did not return \"this\".", pQueue == pQueue.clear());
		pQueue.put("Hugo", 0.4);
		pQueue.put("foo", 0.0002);
		assertTrue("Clear did not return \"this\"", pQueue == pQueue.clear());
	}

	@Test
	public void testIsEmpty() {
		assertTrue("Priority queue not empty after initialized.",
				pQueue.isEmpty());

		pQueue.put("Hugo", 1.0);
		assertFalse("Priority queue empty after first element was added.",
				pQueue.isEmpty());

		pQueue.put("foo", 5.1);
		assertFalse("Priority queue empty after second element was added.",
				pQueue.isEmpty());

		pQueue.clear();
		assertTrue("Priority queue not empty after being cleared.",
				pQueue.isEmpty());

		pQueue.put("Hugo", 2.0);
		assertFalse(
				"Priority queue empty, although there should be more elements in it.",
				pQueue.isEmpty());

		pQueue.put("foo", 16.42);
		assertFalse(
				"Priority queue empty, although there should be more elements in it.",
				pQueue.isEmpty());

		pQueue.getNext();
		pQueue.getNext();
		assertTrue(
				"Priority queue not empty, althoug all elements have been removed.",
				pQueue.isEmpty());
	}

	@Test
	public void testGetNextAndPut() {
		// try getting from initialized object
		try {
			String element = pQueue.getNext();
			fail("Found element \"" + element
					+ "\" in initialized priority queue.");
		} catch (NoSuchElementException e) {
		}

		// check order in normal case (no duplicates)
		pQueue.put("Hugo", 0.4);
		pQueue.put("foo", 8.0);
		pQueue.put("bar", 0.01);
		assertEquals("bar", pQueue.getNext());
		assertEquals("Hugo", pQueue.getNext());
		assertEquals("foo", pQueue.getNext());

		// try getting from empty queue after all elements have been removed
		try {
			String element = pQueue.getNext();
			fail("Found element \""
					+ element
					+ "\" in empty priority queue, after all elements have been removed.");
		} catch (NoSuchElementException e) {
		}

		// check order in case of duplicates (check if all duplicates are
		// returned)
		pQueue.put("Hugo", 1000.0);
		pQueue.put("foo", 34);
		pQueue.put("Hugo", 1.0);
		pQueue.put("bar", 0.5);
		pQueue.put("bar", 200.3);
		pQueue.put("Hugo", 35);
		assertEquals("bar", pQueue.getNext());
		// check if duplicate entry with lower priority is returned first
		assertEquals("Hugo", pQueue.getNext());
		assertEquals("foo", pQueue.getNext());
		// check also if duplicate entries are also returned (no implicit
		// update)
		assertEquals("Hugo", pQueue.getNext());
		assertEquals("bar", pQueue.getNext());
		assertEquals("Hugo", pQueue.getNext());

		// try getting from empty queue after all elements have again been
		// removed
		try {
			String element = pQueue.getNext();
			fail("Found element \""
					+ element
					+ "\" in empty priority queue, after all elements have been removed.");
		} catch (NoSuchElementException e) {
		}

		pQueue.put("Hugo", 0.4);
		pQueue.put("foo", 8.0);
		pQueue.put("bar", 0.01);
		pQueue.clear();
		// try getting from empty queue after it has been cleared
		try {
			String element = pQueue.getNext();
			fail("Found element \""
					+ element
					+ "\" in empty priority queue, after all elements have been removed.");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testSize() {
		assertEquals(0, pQueue.size());
		pQueue.clear();
		assertEquals(0, pQueue.size());
		pQueue.put("Hugo", 0.4);
		assertEquals(1, pQueue.size());
		pQueue.put("foo", 2.09);
		assertEquals(2, pQueue.size());
		pQueue.put("bar", 1.1);
		assertEquals(3, pQueue.size());
		pQueue.getNext();
		assertEquals(2, pQueue.size());
		pQueue.getNext();
		assertEquals(1, pQueue.size());
		pQueue.put("Hugo", 16.47);
		assertEquals(2, pQueue.size());
		pQueue.getNext();
		assertEquals(1, pQueue.size());
		pQueue.getNext();
		assertEquals(0, pQueue.size());
		pQueue.clear();
		assertEquals(0, pQueue.size());
		pQueue.put("Hugo", 300.003);
		assertEquals(1, pQueue.size());
		pQueue.put("foo", 34.9);
		assertEquals(2, pQueue.size());
		pQueue.clear();
		assertEquals(0, pQueue.size());
	}
}
