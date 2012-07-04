package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.ToLong;

public class ToLongTest extends UnaryFunctionTest {
	private ToLong toLong;

	@Before
	public void setUp() {
		toLong = new ToLong();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals((Long) Integer.valueOf(intValues[i]).longValue(),
					toLong.evaluate(intValues[i]));
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals((Long) Long.valueOf(longValues[i]).longValue(),
					toLong.evaluate(longValues[i]));
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals((Long) Double.valueOf(doubleValues[i]).longValue(),
					toLong.evaluate(doubleValues[i]));
		}
	}
}
