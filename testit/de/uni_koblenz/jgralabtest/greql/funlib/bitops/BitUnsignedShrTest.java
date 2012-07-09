package de.uni_koblenz.jgralabtest.greql.funlib.bitops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.bitops.BitUnsignedShr;

public class BitUnsignedShrTest extends BitShiftTest {

	private BitUnsignedShr bitunsignedShr;

	@Before
	public void setUp() {
		bitunsignedShr = new BitUnsignedShr();
	}

	@Test
	public void testInteger() {
		for (int i = 0; i < intValues.length; i++) {
			for (int j = 0; j < shiftAmounts.length; j++) {
				int expected = intValues[i] >>> shiftAmounts[j];
				Number result = bitunsignedShr.evaluate(intValues[i],
						shiftAmounts[j]);
				assertTrue(result instanceof Integer);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			for (int j = 0; j < shiftAmounts.length; j++) {
				long expected = longValues[i] >>> shiftAmounts[j];
				Number result = bitunsignedShr.evaluate(longValues[i],
						shiftAmounts[j]);
				assertTrue(result instanceof Long);
				assertEquals(expected, result);
			}
		}
	}
}
