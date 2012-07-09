package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

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
			assertEquals(Math.sin(intValues[i]), sin.evaluate(intValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals(Math.sin(longValues[i]), sin.evaluate(longValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals(Math.sin(doubleValues[i]),
					sin.evaluate(doubleValues[i]), RunArithmeticTests.EPSILON);
		}
	}
}
