package de.uni_koblenz.jgralabtest.greql.funlib.logics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.logics.Or;

public class OrTest extends LogicsTest {
	private Or or;

	@Before
	public void setUp() {
		or = new Or();
	}

	@Test
	public void test() {
		for (int i = 0; i < booleanValues.length; i++) {
			for (int j = 0; j < booleanValues.length; j++) {
				boolean expected = booleanValues[i] | booleanValues[j];
				Boolean result = or
						.evaluate(booleanValues[i], booleanValues[j]);
				assertEquals(expected, result);
			}
		}
	}
}
