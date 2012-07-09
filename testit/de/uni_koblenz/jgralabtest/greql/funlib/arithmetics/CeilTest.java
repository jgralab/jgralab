package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.Ceil;

public class CeilTest extends ArithmeticTest {
	private Ceil ceil;

	@Before
	public void setUp() {
		ceil = new Ceil();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals(Math.ceil(intValues[i]), ceil.evaluate(intValues[i]));
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals(Math.ceil(longValues[i]), ceil.evaluate(longValues[i]));
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals(Math.ceil(doubleValues[i]),
					(Double) ceil.evaluate(doubleValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}
}
