package de.uni_koblenz.jgralab.greql2.funlib.strings;

import java.util.List;
import java.util.regex.Pattern;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Split extends Function {
	public Split() {
		super(
				"Splits the given string according to the given regular expression and returns the parts as list.",
				10, 3, 0.1, Category.STRINGS);
	}

	public List<String> evaluate(String s, String regex) {
		Pattern pat = ReMatch.patternCache.get(regex);
		if (pat == null) {
			pat = Pattern.compile(regex);
			ReMatch.patternCache.put(regex, pat);
		}
		String[] parts = pat.split(s);
		PVector<String> result = JGraLab.vector();
		for (String part : parts) {
			result = result.plus(part);
		}
		return result;
	}
}
