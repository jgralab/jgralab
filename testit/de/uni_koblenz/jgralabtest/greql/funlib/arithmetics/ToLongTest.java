package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class ToLongTest extends ArithmeticTest {

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			long expected = Integer.valueOf(intValues[i]).longValue();
			Object result = FunLib.apply("toLong", intValues[i]);
			assertTrue(result instanceof Long);
			assertEquals(expected, result);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			long expected = Long.valueOf(longValues[i]).longValue();
			Object result = FunLib.apply("toLong", longValues[i]);
			assertTrue(result instanceof Long);
			assertEquals(expected, result);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			long expected = Double.valueOf(doubleValues[i]).longValue();
			Object result = FunLib.apply("toLong", doubleValues[i]);
			assertTrue(result instanceof Long);
			assertEquals(expected, result);
		}
	}
}
