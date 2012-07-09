package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.Div;

public class DivTest extends ArithmeticTest {
	private Div div;

	@Before
	public void setUp() {
		div = new Div();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			for (int j = 0; j < intValues.length; j++) {
				if (intValues[j] == 0) {
					try {
						div.evaluate(intValues[i], intValues[j]);
						fail("This is a division by zero and should throw an ArithmeticException.");
					} catch (ArithmeticException e) {
					}
				} else {
					int expected = intValues[i] / intValues[j];
					Number result = div.evaluate(intValues[i], intValues[j]);
					assertTrue(result instanceof Integer);
					assertEquals(expected, result);
				}
			}
			for (int j = 0; j < longValues.length; j++) {
				if (longValues[j] == 0l) {
					try {
						div.evaluate(intValues[i], longValues[j]);
						fail("This is a division by zero and should throw an ArithmeticException.");
					} catch (ArithmeticException e) {
					}
				} else {
					long expected = intValues[i] / longValues[j];
					Number result = div.evaluate(intValues[i], longValues[j]);
					assertTrue(result instanceof Long);
					assertEquals(expected, result);
				}
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double expected = intValues[i] / doubleValues[j];
				Number result = div.evaluate(intValues[i], doubleValues[j]);
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
						div.evaluate(longValues[i], intValues[j]);
						fail("This is a division by zero and should throw an ArithmeticException.");
					} catch (ArithmeticException e) {
					}
				} else {
					long expected = longValues[i] / intValues[j];
					Number result = div.evaluate(longValues[i], intValues[j]);
					assertTrue(result instanceof Long);
					assertEquals(expected, result);
				}
			}
			for (int j = 0; j < longValues.length; j++) {
				if (longValues[j] == 0l) {
					try {
						div.evaluate(longValues[i], longValues[j]);
						fail("This is a division by zero and should throw an ArithmeticException.");
					} catch (ArithmeticException e) {
					}
				} else {
					long expected = longValues[i] / longValues[j];
					Number result = div.evaluate(longValues[i], longValues[j]);
					assertTrue(result instanceof Long);
					assertEquals(expected, result);
				}
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double expected = longValues[i] / doubleValues[j];
				Number result = div.evaluate(longValues[i], doubleValues[j]);
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
				Number result = div.evaluate(doubleValues[i], intValues[j]);
				assertTrue(result instanceof Double);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				double expected = doubleValues[i] / longValues[j];
				Number result = div.evaluate(doubleValues[i], longValues[j]);
				assertTrue(result instanceof Double);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double expected = doubleValues[i] / doubleValues[j];
				Number result = div.evaluate(doubleValues[i], doubleValues[j]);
				assertTrue(result instanceof Double);
				assertEquals(expected, (Double) result,
						RunArithmeticTests.EPSILON);
			}
		}
	}

}
