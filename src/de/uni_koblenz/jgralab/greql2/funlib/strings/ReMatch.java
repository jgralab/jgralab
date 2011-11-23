package de.uni_koblenz.jgralab.greql2.funlib.strings;

import java.util.HashMap;
import java.util.regex.Pattern;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class ReMatch extends Function {
	public ReMatch() {
		super(
				"Returns true, iff the given string matches the given regular expression. \n"
						+ "Can be used as infix operator: myString =\\textasciitilde{} myRegexp.",
				50, 1, 0.1, Category.STRINGS);
	}

	// cache is also used by Split function
	static HashMap<String, Pattern> patternCache = new HashMap<String, Pattern>();

	public Boolean evaluate(String s, String regex) {
		Pattern pat = patternCache.get(regex);
		if (pat == null) {
			pat = Pattern.compile(regex);
			patternCache.put(regex, pat);
		}
		return pat.matcher(s).matches();
	}
}
