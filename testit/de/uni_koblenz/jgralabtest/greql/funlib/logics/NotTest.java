package de.uni_koblenz.jgralabtest.greql.funlib.logics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.logics.Not;

public class NotTest extends LogicsTest {
	private Not xor;

	@Before
	public void setUp() {
		xor = new Not();
	}

	@Test
	public void test() {
		for (int i = 0; i < booleanValues.length; i++) {
			boolean expected = !booleanValues[i];
			Boolean result = xor.evaluate(booleanValues[i]);
			assertEquals(expected, result);

		}
	}
}
