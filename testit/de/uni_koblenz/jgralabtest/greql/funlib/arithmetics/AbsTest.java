package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class AbsTest extends ArithmeticTest {

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			int expected = Math.abs(intValues[i]);
			Object result = FunLib.apply("abs", intValues[i]);
			assertEquals(expected, result);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			long expected = Math.abs(longValues[i]);
			Object result = FunLib.apply("abs", longValues[i]);
			assertEquals(expected, result);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			double expected = Math.abs(doubleValues[i]);
			Object result = FunLib.apply("abs", doubleValues[i]);
			assertTrue(result instanceof Double);
			assertEquals(expected, (Double) result, RunArithmeticTests.EPSILON);
		}
	}
}
