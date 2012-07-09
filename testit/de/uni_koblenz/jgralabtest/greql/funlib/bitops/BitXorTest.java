package de.uni_koblenz.jgralabtest.greql.funlib.bitops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.bitops.BitXor;

public class BitXorTest extends BitOpTest {

	private BitXor bitXor;

	@Before
	public void setUp() {
		bitXor = new BitXor();
	}

	@Test
	public void testInteger() {
		for (int i = 0; i < intValues.length; i++) {
			for (int j = 0; j < intValues.length; j++) {
				int expected = intValues[i] ^ intValues[j];
				Number result = bitXor.evaluate(intValues[i], intValues[j]);
				assertTrue(result instanceof Integer);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long expected = intValues[i] ^ longValues[j];
				Number result = bitXor.evaluate(intValues[i], longValues[j]);
				assertTrue(result instanceof Long);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			for (int j = 0; j < intValues.length; j++) {
				long expected = longValues[i] ^ intValues[j];
				Number result = bitXor.evaluate(longValues[i], intValues[j]);
				assertTrue(result instanceof Long);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long expected = longValues[i] ^ longValues[j];
				Number result = bitXor.evaluate(longValues[i], longValues[j]);
				assertTrue(result instanceof Long);
				assertEquals(expected, result);
			}
		}
	}
}
