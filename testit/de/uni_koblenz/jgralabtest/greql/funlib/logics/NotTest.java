package de.uni_koblenz.jgralabtest.greql.funlib.logics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class NotTest extends LogicsTest {

	@Test
	public void test() {
		for (int i = 0; i < booleanValues.length; i++) {
			boolean expected = !booleanValues[i];
			Object result = FunLib.apply("not", booleanValues[i]);
			assertTrue(result instanceof Boolean);
			assertEquals(expected, result);

		}
	}
}
