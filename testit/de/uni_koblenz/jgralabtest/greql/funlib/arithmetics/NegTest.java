package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.Neg;

public class NegTest extends UnaryFunctionTest {
	private Neg neg;

	@Before
	public void setUp() {
		neg = new Neg();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals(-intValues[i], neg.evaluate(intValues[i]));
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals(-longValues[i], neg.evaluate(longValues[i]));
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals(-doubleValues[i],
					(Double) neg.evaluate(doubleValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}
}
