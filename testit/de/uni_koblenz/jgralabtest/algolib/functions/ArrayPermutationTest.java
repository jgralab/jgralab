package de.uni_koblenz.jgralabtest.algolib.functions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.algolib.functions.ArrayPermutation;
import de.uni_koblenz.jgralab.algolib.functions.entries.PermutationEntry;

public class ArrayPermutationTest {

	private String[] permArray = new String[] { null, "first", "second",
			"third", "fourth", "fifth", null };

	private ArrayPermutation<String> perm;
	private ArrayPermutation<String> empty;

	@Before
	public void setUp() {
		perm = new ArrayPermutation<String>(permArray);
		empty = new ArrayPermutation<String>(new String[] {});
	}

	@Test
	public void testIsDefined() {
		assertFalse(perm.isDefined(0));
		assertFalse(perm.isDefined(6));
		assertFalse(perm.isDefined(7));
		assertFalse(perm.isDefined(-12));
		assertFalse(perm.isDefined(12));

		assertTrue(perm.isDefined(1));
		assertTrue(perm.isDefined(5));
		assertTrue(perm.isDefined(3));

		assertFalse(empty.isDefined(0));
		assertFalse(empty.isDefined(-1));
		assertFalse(empty.isDefined(1));
	}

	@Test
	public void testAdd() {
		try {
			perm.add("NewString");
			fail("This is a write-only wrapper and should therefore not be changeable.");
		} catch (UnsupportedOperationException e) {
		}
	}

	@Test
	public void testLength() {
		assertEquals(5, perm.length());
		assertEquals(0, empty.length());
	}

	@Test
	public void testGetRangeElements() {
		// this test case also tests the internal iterator class
		Iterable<String> elements = perm.getRangeElements();

		Iterator<String> iter = elements.iterator();
		assertTrue(iter.hasNext());
		assertEquals("first", iter.next());
		assertTrue(iter.hasNext());
		assertEquals("second", iter.next());
		assertTrue(iter.hasNext());
		assertEquals("third", iter.next());
		assertTrue(iter.hasNext());
		assertEquals("fourth", iter.next());
		assertTrue(iter.hasNext());
		assertEquals("fifth", iter.next());
		assertFalse(iter.hasNext());
		try {
			iter.next();
			fail("There should not be any more elements.");
		} catch (NoSuchElementException e) {
		}

		int i = 1;
		for (String current : elements) {
			assertEquals(permArray[i++], current);
		}

		// empty permutation
		elements = empty.getRangeElements();
		iter = elements.iterator();
		assertFalse(iter.hasNext());
		try {
			String current = iter.next();
			fail("There should not be any element at all, but found: "
					+ current);
		} catch (NoSuchElementException e) {
		}

		for (String current : elements) {
			fail("There should not be any element at all, but found: "
					+ current);
		}
	}

	@Test
	public void testIterator() {
		Iterator<PermutationEntry<String>> iter = perm.iterator();

		assertTrue(iter.hasNext());
		PermutationEntry<String> current = iter.next();
		assertEquals(1, current.getFirst());
		assertEquals("first", current.getSecond());

		assertTrue(iter.hasNext());
		current = iter.next();
		assertEquals(2, current.getFirst());
		assertEquals("second", current.getSecond());

		assertTrue(iter.hasNext());
		current = iter.next();
		assertEquals(3, current.getFirst());
		assertEquals("third", current.getSecond());

		assertTrue(iter.hasNext());
		current = iter.next();
		assertEquals(4, current.getFirst());
		assertEquals("fourth", current.getSecond());

		assertTrue(iter.hasNext());
		current = iter.next();
		assertEquals(5, current.getFirst());
		assertEquals("fifth", current.getSecond());

		assertFalse(iter.hasNext());
		try {
			iter.next();
			fail("There should not be any more elements.");
		} catch (NoSuchElementException e) {
		}

		iter = empty.iterator();
		assertFalse(iter.hasNext());
		try {
			iter.next();
			fail("There should not be any more elements.");
		} catch (NoSuchElementException e) {
		}

	}
}
