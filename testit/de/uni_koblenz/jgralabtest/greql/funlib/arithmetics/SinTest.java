package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;
import de.uni_koblenz.jgralab.greql.funlib.artithmetics.Sin;

public class SinTest extends ArithmeticTest {
	private Sin sin;

	@Before
	public void setUp() {
		sin = new Sin();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			double expected = Math.sin(intValues[i]);
			Object result = FunLib.apply("sin", intValues[i]);
			assertTrue(result instanceof Double);
			assertEquals(expected, (Double) result, RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			double expected = Math.sin(longValues[i]);
			Object result = sin.evaluate(longValues[i]);
			assertTrue(result instanceof Double);
			assertEquals(expected, (Double) result, RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			double expected = Math.sin(doubleValues[i]);
			Object result = sin.evaluate(doubleValues[i]);
			assertTrue(result instanceof Double);
			assertEquals(expected, (Double) result, RunArithmeticTests.EPSILON);
		}
	}
}
