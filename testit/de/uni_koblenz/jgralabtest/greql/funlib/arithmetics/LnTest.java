package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.Ln;

public class LnTest extends ArithmeticTest {
	private Ln log;

	@Before
	public void setUp() {
		log = new Ln();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals(Math.log(intValues[i]), log.evaluate(intValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals(Math.log(longValues[i]), log.evaluate(longValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals(Math.log(doubleValues[i]),
					log.evaluate(doubleValues[i]), RunArithmeticTests.EPSILON);
		}
	}
}
