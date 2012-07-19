package de.uni_koblenz.jgralabtest.greql.funlib.strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class CapitalizeFirstTest extends StringsTest {

	@Test
	public void testString() {
		for (int i = 0; i < stringValues.length; i++) {
			String s = stringValues[i];
			String expected = s.equals("") ? "" : Character.toUpperCase(s
					.charAt(0)) + s.substring(1);
			Object result = FunLib.apply("capitalizeFirst", s);
			assertTrue(result instanceof String);
			assertEquals(expected, result);
		}
	}

}
