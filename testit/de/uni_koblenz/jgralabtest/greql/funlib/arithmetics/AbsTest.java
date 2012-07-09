package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.Abs;

public class AbsTest extends ArithmeticTest {

	private Abs abs;

	@Before
	public void setUp() {
		abs = new Abs();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals(Math.abs(intValues[i]), abs.evaluate(intValues[i]));
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals(Math.abs(longValues[i]), abs.evaluate(longValues[i]));
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals(Math.abs(doubleValues[i]),
					(Double) abs.evaluate(doubleValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}
}
