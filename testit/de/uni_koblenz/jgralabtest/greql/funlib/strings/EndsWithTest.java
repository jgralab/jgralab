package de.uni_koblenz.jgralabtest.greql.funlib.strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class EndsWithTest extends StringsTest {
	@Test
	public void testString() {
		for (int i = 0; i < stringValues.length; i++) {
			String first = stringValues[i];
			for (int j = 0; j < stringValues.length; j++) {
				String second = stringValues[j];
				boolean expected = second.endsWith(first);
				Object result = FunLib.apply("endsWith", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}
	}
}
