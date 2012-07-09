package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.ToInteger;

public class ToIntegerTest extends ArithmeticTest {
	private ToInteger toInteger;

	@Before
	public void setUp() {
		toInteger = new ToInteger();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals((Integer) Integer.valueOf(intValues[i]).intValue(),
					toInteger.evaluate(intValues[i]));
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals((Integer) Long.valueOf(longValues[i]).intValue(),
					toInteger.evaluate(longValues[i]));
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals((Integer) Double.valueOf(doubleValues[i]).intValue(),
					toInteger.evaluate(doubleValues[i]));
		}
	}
}
