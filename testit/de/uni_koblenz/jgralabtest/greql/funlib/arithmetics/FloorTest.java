package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.Floor;

public class FloorTest extends UnaryFunctionTest {
	private Floor floor;

	@Before
	public void setUp() {
		floor = new Floor();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals(Math.floor(intValues[i]), floor.evaluate(intValues[i]));
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals(Math.floor(longValues[i]),
					floor.evaluate(longValues[i]));
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals(Math.floor(doubleValues[i]),
					(Double) floor.evaluate(doubleValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}
}
