package de.uni_koblenz.jgralabtest.greql.funlib.strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class LengthTest extends StringsTest {

	@Test
	public void testString() {
		for (String current : stringValues) {
			int expected = current.length();
			Object result = FunLib.apply("length", current);
			assertTrue(result instanceof Integer);
			assertEquals(expected, result);
		}
	}
}
