package de.uni_koblenz.jgralabtest.greql.funlib.strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class ConcatTest extends StringsTest {

	@Test
	public void testString() {
		for (int i = 0; i < stringValues.length; i++) {
			String first = stringValues[i];
			for (int j = 0; j < objectValues.length; j++) {
				Object second = objectValues[j];
				String expected = first + second.toString();
				Object result = FunLib.apply("concat", first, second);
				assertTrue(result instanceof String);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testObject() {
		for (int i = 0; i < objectValues.length; i++) {
			Object first = objectValues[i];
			for (int j = 0; j < stringValues.length; j++) {
				String second = stringValues[j];
				String expected = first.toString() + second;
				Object result = FunLib.apply("concat", first, second);
				assertTrue(result instanceof String);
				assertEquals(expected, result);
			}
		}
	}

}
