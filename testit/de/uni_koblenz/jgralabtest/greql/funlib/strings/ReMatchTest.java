package de.uni_koblenz.jgralabtest.greql.funlib.strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class ReMatchTest extends StringsTest {

	private String[] regExes = new String[] { "[a-z]*", "[a-z]+", ".*", "\\d*",
			"\\d+", ".*oo.+" };

	@Test
	public void testString() {
		for (String current : stringValues) {
			for (String currentRegExp : regExes) {
				boolean expected = Pattern.matches(currentRegExp, current);
				Object result = FunLib.apply("reMatch", current, currentRegExp);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}
	}
}
