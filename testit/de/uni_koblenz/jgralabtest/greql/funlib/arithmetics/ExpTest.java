package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class ExpTest extends ArithmeticTest {

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			double expected = Math.exp(intValues[i]);
			Object result = FunLib.apply("exp", intValues[i]);
			assertTrue(result instanceof Double);
			assertEquals(expected, (Double) result, RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			double expected = Math.exp(longValues[i]);
			Object result = FunLib.apply("exp", longValues[i]);
			assertTrue(result instanceof Double);
			assertEquals(expected, (Double) result, RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			double expected = Math.exp(doubleValues[i]);
			Object result = FunLib.apply("exp", doubleValues[i]);
			assertTrue(result instanceof Double);
			assertEquals(expected, (Double) result, RunArithmeticTests.EPSILON);
		}
	}
}
