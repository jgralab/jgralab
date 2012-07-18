package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class ToIntegerTest extends ArithmeticTest {

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			int expected = Integer.valueOf(intValues[i]).intValue();
			Object result = FunLib.apply("toInteger", intValues[i]);
			assertTrue(result instanceof Integer);
			assertEquals(expected, result);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			int expected = Long.valueOf(longValues[i]).intValue();
			Object result = FunLib.apply("toInteger", longValues[i]);
			assertTrue(result instanceof Integer);
			assertEquals(expected, result);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			int expected = Double.valueOf(doubleValues[i]).intValue();
			Object result = FunLib.apply("toInteger", doubleValues[i]);
			assertTrue(result instanceof Integer);
			assertEquals(expected, result);
		}
	}
}
