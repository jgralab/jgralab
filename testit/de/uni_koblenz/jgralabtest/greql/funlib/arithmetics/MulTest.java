package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class MulTest extends ArithmeticTest {

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			for (int j = 0; j < intValues.length; j++) {
				int expected = intValues[i] * intValues[j];
				Object result = FunLib.apply("mul", intValues[i], intValues[j]);
				assertTrue(result instanceof Integer);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long expected = intValues[i] * longValues[j];
				Object result = FunLib
						.apply("mul", intValues[i], longValues[j]);
				assertTrue(result instanceof Long);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double expected = intValues[i] * doubleValues[j];
				Object result = FunLib.apply("mul", intValues[i],
						doubleValues[j]);
				assertTrue(result instanceof Double);
				assertEquals(expected, (Double) result,
						RunArithmeticTests.EPSILON);
			}
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			for (int j = 0; j < intValues.length; j++) {
				long expected = longValues[i] * intValues[j];
				Object result = FunLib
						.apply("mul", longValues[i], intValues[j]);
				assertTrue(result instanceof Long);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long expected = longValues[i] * longValues[j];
				Object result = FunLib.apply("mul", longValues[i],
						longValues[j]);
				assertTrue(result instanceof Long);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double expected = longValues[i] * doubleValues[j];
				Object result = FunLib.apply("mul", longValues[i],
						doubleValues[j]);
				assertTrue(result instanceof Double);
				assertEquals(expected, (Double) result,
						RunArithmeticTests.EPSILON);
			}
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			for (int j = 0; j < intValues.length; j++) {
				double expected = doubleValues[i] * intValues[j];
				Object result = FunLib.apply("mul", doubleValues[i],
						intValues[j]);
				assertTrue(result instanceof Double);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				double expected = doubleValues[i] * longValues[j];
				Object result = FunLib.apply("mul", doubleValues[i],
						longValues[j]);
				assertTrue(result instanceof Double);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double expected = doubleValues[i] * doubleValues[j];
				Object result = FunLib.apply("mul", doubleValues[i],
						doubleValues[j]);
				assertTrue(result instanceof Double);
				assertEquals(expected, (Double) result,
						RunArithmeticTests.EPSILON);
			}
		}
	}

}
