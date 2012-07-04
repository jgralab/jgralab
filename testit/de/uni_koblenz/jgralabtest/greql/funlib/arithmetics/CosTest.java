package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.Cos;

public class CosTest extends UnaryFunctionTest {
	private Cos cos;

	@Before
	public void setUp() {
		cos = new Cos();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals(Math.cos(intValues[i]), cos.evaluate(intValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals(Math.cos(longValues[i]), cos.evaluate(longValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals(Math.cos(doubleValues[i]),
					cos.evaluate(doubleValues[i]), RunArithmeticTests.EPSILON);
		}
	}
}
