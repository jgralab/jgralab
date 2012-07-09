package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.Sqrt;

public class SqrtTest extends ArithmeticTest {
	private Sqrt sqrt;

	@Before
	public void setUp() {
		sqrt = new Sqrt();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals(Math.sqrt(intValues[i]), sqrt.evaluate(intValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals(Math.sqrt(longValues[i]),
					sqrt.evaluate(longValues[i]), RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals(Math.sqrt(doubleValues[i]),
					sqrt.evaluate(doubleValues[i]), RunArithmeticTests.EPSILON);
		}
	}
}
