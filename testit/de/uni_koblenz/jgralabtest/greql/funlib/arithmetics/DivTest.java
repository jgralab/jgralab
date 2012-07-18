package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class DivTest extends ArithmeticTest {

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			for (int j = 0; j < intValues.length; j++) {
				if (intValues[j] == 0) {
					try {
						FunLib.apply("div", intValues[i], intValues[j]);
						fail("This is a division by zero and should throw a GreqlException.");
					} catch (GreqlException e) {
					}
				} else {
					int expected = intValues[i] / intValues[j];
					Object result = FunLib.apply("div", intValues[i],
							intValues[j]);
					assertTrue(result instanceof Integer);
					assertEquals(expected, result);
				}
			}
			for (int j = 0; j < longValues.length; j++) {
				if (longValues[j] == 0l) {
					try {
						FunLib.apply("div", intValues[i], longValues[j]);
						fail("This is a division by zero and should throw a GreqlException.");
					} catch (GreqlException e) {
					}
				} else {
					long expected = intValues[i] / longValues[j];
					Object result = FunLib.apply("div", intValues[i],
							longValues[j]);
					assertTrue(result instanceof Long);
					assertEquals(expected, result);
				}
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double expected = intValues[i] / doubleValues[j];
				Object result = FunLib.apply("div", intValues[i],
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
				if (intValues[j] == 0) {
					try {
						FunLib.apply("div", longValues[i], intValues[j]);
						fail("This is a division by zero and should throw an GreqlException.");
					} catch (GreqlException e) {
					}
				} else {
					long expected = longValues[i] / intValues[j];
					Object result = FunLib.apply("div", longValues[i],
							intValues[j]);
					assertTrue(result instanceof Long);
					assertEquals(expected, result);
				}
			}
			for (int j = 0; j < longValues.length; j++) {
				if (longValues[j] == 0l) {
					try {
						FunLib.apply("div", longValues[i], longValues[j]);
						fail("This is a division by zero and should throw an GreqlException.");
					} catch (GreqlException e) {
					}
				} else {
					long expected = longValues[i] / longValues[j];
					Object result = FunLib.apply("div", longValues[i],
							longValues[j]);
					assertTrue(result instanceof Long);
					assertEquals(expected, result);
				}
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double expected = longValues[i] / doubleValues[j];
				Object result = FunLib.apply("div", longValues[i],
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
				double expected = doubleValues[i] / intValues[j];
				Object result = FunLib.apply("div", doubleValues[i],
						intValues[j]);
				assertTrue(result instanceof Double);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				double expected = doubleValues[i] / longValues[j];
				Object result = FunLib.apply("div", doubleValues[i],
						longValues[j]);
				assertTrue(result instanceof Double);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double expected = doubleValues[i] / doubleValues[j];
				Object result = FunLib.apply("div", doubleValues[i],
						doubleValues[j]);
				assertTrue(result instanceof Double);
				assertEquals(expected, (Double) result,
						RunArithmeticTests.EPSILON);
			}
		}
	}

}
