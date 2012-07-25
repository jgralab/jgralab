package de.uni_koblenz.jgralabtest.greql.funlib.strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class ToStringTest extends StringsTest {

	@Test
	public void testObject() {
		for (Object current : objectValues) {
			String expected = current.toString();
			Object result = FunLib.apply("toString", current);
			assertTrue(result instanceof String);
			assertEquals(expected, result);
		}
	}
}
