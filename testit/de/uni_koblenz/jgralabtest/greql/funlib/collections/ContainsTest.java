package de.uni_koblenz.jgralabtest.greql.funlib.collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class ContainsTest {
	private PVector<Integer> integerVector;
	private PSet<Integer> integerSet;

	@Before
	public void setUp() {
		integerVector = JGraLab.vector();
		integerVector = integerVector.plus(16);
		integerVector = integerVector.plus(42);
		integerVector = integerVector.plus(42);
		integerVector = integerVector.plus(-1);
		integerVector = integerVector.plus(0);
		integerVector = integerVector.plus(Integer.MIN_VALUE);
		integerVector = integerVector.plus(Integer.MAX_VALUE);

		integerSet = JGraLab.set();
		integerSet = integerSet.plus(16);
		integerSet = integerSet.plus(42);
		integerSet = integerSet.plus(42);
		integerSet = integerSet.plus(-1);
		integerSet = integerSet.plus(0);
		integerSet = integerSet.plus(Integer.MIN_VALUE);
		integerSet = integerSet.plus(Integer.MAX_VALUE);
	}

	@Test
	public void testVector() {
		assertTrue((Boolean) FunLib.apply("contains", integerVector, 16));
		assertTrue((Boolean) FunLib.apply("contains", integerVector, 42));
		assertTrue((Boolean) FunLib.apply("contains", integerVector, -1));
		assertTrue((Boolean) FunLib.apply("contains", integerVector, 0));
		assertTrue((Boolean) FunLib.apply("contains", integerVector,
				Integer.MIN_VALUE));
		assertTrue((Boolean) FunLib.apply("contains", integerVector,
				Integer.MAX_VALUE));
		assertFalse((Boolean) FunLib.apply("contains", integerVector, 17));
		assertFalse((Boolean) FunLib.apply("contains", integerVector, 15));
		assertFalse((Boolean) FunLib.apply("contains", integerVector, 41));
		assertFalse((Boolean) FunLib.apply("contains", integerVector, 43));
		assertFalse((Boolean) FunLib.apply("contains", integerVector, 100));
		assertFalse((Boolean) FunLib.apply("contains", integerVector, 1));
	}

	@Test
	public void testSet() {
		assertTrue((Boolean) FunLib.apply("contains", integerSet, 16));
		assertTrue((Boolean) FunLib.apply("contains", integerSet, 42));
		assertTrue((Boolean) FunLib.apply("contains", integerSet, -1));
		assertTrue((Boolean) FunLib.apply("contains", integerSet, 0));
		assertTrue((Boolean) FunLib.apply("contains", integerSet,
				Integer.MIN_VALUE));
		assertTrue((Boolean) FunLib.apply("contains", integerSet,
				Integer.MAX_VALUE));
		assertFalse((Boolean) FunLib.apply("contains", integerSet, 17));
		assertFalse((Boolean) FunLib.apply("contains", integerSet, 15));
		assertFalse((Boolean) FunLib.apply("contains", integerSet, 41));
		assertFalse((Boolean) FunLib.apply("contains", integerSet, 43));
		assertFalse((Boolean) FunLib.apply("contains", integerSet, 100));
		assertFalse((Boolean) FunLib.apply("contains", integerSet, 1));
	}

}
