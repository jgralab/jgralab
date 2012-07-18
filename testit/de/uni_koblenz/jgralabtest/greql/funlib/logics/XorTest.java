package de.uni_koblenz.jgralabtest.greql.funlib.logics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class XorTest extends LogicsTest {

	@Test
	public void test() {
		for (int i = 0; i < booleanValues.length; i++) {
			for (int j = 0; j < booleanValues.length; j++) {
				boolean expected = booleanValues[i] ^ booleanValues[j];
				Object result = FunLib.apply("xor", booleanValues[i],
						booleanValues[j]);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}
	}
}
