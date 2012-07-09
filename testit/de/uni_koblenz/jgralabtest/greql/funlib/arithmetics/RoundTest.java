package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.Round;

public class RoundTest extends ArithmeticTest {
	private Round round;

	@Before
	public void setUp() {
		round = new Round();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals((long) Math.round(intValues[i]),
					(long) round.evaluate(intValues[i]));
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals(Math.round((double) longValues[i]),
					(long) round.evaluate(longValues[i]));
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals(Math.round(doubleValues[i]),
					(long) round.evaluate(doubleValues[i]));
		}
	}
}
