package de.uni_koblenz.jgralabtest.greql.funlib.strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class SplitTest extends StringsTest {

	private String[] regExes = new String[] { "o", "oo", " ", "", "Hello", "a" };

	@Test
	public void testString() {
		for (String current : stringValues) {
			for (String currentRegexp : regExes) {
				PVector<String> expected = computeExpected(current,
						currentRegexp);
				Object result = FunLib.apply("split", current, currentRegexp);
				assertTrue(result instanceof PVector);
				assertEquals("Tried to split \"" + current
						+ "\" with pattern \"" + currentRegexp
						+ "\". Expected " + expected + " but the result was "
						+ result, expected, result);
			}
		}
	}

	private PVector<String> computeExpected(String s, String regExp) {
		String[] splitted = s.split(regExp);
		PVector<String> out = JGraLab.vector();
		for (String current : splitted) {
			out = out.plus(current);
		}
		return out;
	}

}
