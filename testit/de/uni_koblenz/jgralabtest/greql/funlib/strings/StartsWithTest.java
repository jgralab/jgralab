package de.uni_koblenz.jgralabtest.greql.funlib.strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class StartsWithTest extends StringsTest {

	@Test
	public void testString() {
		for (int i = 0; i < stringValues.length; i++) {
			String first = stringValues[i];
			for (int j = 0; j < stringValues.length; j++) {
				String second = stringValues[j];
				boolean expected = second.startsWith(first);
				Object result = FunLib.apply("startsWith", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testStringWithOffset() {
		for (int i = 0; i < 30; i++) {
			for (int j = 0; j < stringValues.length; j++) {
				String first = stringValues[j];
				for (int k = 0; k < stringValues.length; k++) {
					String second = stringValues[k];
					boolean expected = second.startsWith(first, i);
					Object result = FunLib
							.apply("startsWith", first, second, i);
					assertTrue(result instanceof Boolean);
					assertEquals(expected, result);
				}
			}
		}
	}
}
