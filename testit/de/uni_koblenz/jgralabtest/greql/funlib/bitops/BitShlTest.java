package de.uni_koblenz.jgralabtest.greql.funlib.bitops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.bitops.BitShl;

public class BitShlTest extends BitShiftTest {

	private BitShl bitShl;

	@Before
	public void setUp() {
		bitShl = new BitShl();
	}

	@Test
	public void testInteger() {
		for (int i = 0; i < intValues.length; i++) {
			for (int j = 0; j < shiftAmounts.length; j++) {
				int expected = intValues[i] << shiftAmounts[j];
				Number result = bitShl.evaluate(intValues[i], shiftAmounts[j]);
				assertTrue(result instanceof Integer);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			for (int j = 0; j < shiftAmounts.length; j++) {
				long expected = longValues[i] << shiftAmounts[j];
				Number result = bitShl.evaluate(longValues[i], shiftAmounts[j]);
				assertTrue(result instanceof Long);
				assertEquals(expected, result);
			}
		}
	}
}
