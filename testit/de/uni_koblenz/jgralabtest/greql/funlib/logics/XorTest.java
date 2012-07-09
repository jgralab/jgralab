package de.uni_koblenz.jgralabtest.greql.funlib.logics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.logics.Xor;

public class XorTest extends LogicsTest {
	private Xor xor;

	@Before
	public void setUp() {
		xor = new Xor();
	}

	@Test
	public void test() {
		for (int i = 0; i < booleanValues.length; i++) {
			for (int j = 0; j < booleanValues.length; j++) {
				boolean expected = booleanValues[i] ^ booleanValues[j];
				Boolean result = xor.evaluate(booleanValues[i],
						booleanValues[j]);
				assertEquals(expected, result);
			}
		}
	}
}
