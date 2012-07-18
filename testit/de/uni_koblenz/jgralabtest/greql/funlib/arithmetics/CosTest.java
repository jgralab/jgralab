package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class CosTest extends ArithmeticTest {

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			double expected = Math.cos(intValues[i]);
			Object result = FunLib.apply("cos", intValues[i]);
			assertTrue(result instanceof Double);
			assertEquals(expected, (Double) result, RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			double expected = Math.cos(longValues[i]);
			Object result = FunLib.apply("cos", longValues[i]);
			assertTrue(result instanceof Double);
			assertEquals(expected, (Double) result, RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			double expected = Math.cos(doubleValues[i]);
			Object result = FunLib.apply("cos", doubleValues[i]);
			assertTrue(result instanceof Double);
			assertEquals(expected, (Double) result, RunArithmeticTests.EPSILON);
		}
	}
}
