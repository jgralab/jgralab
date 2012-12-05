package de.uni_koblenz.jgralab.greql.funlib.strings;

import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;

public class Replace extends Function {
	@Description(params = { "s", "old", "new" }, description = "Replaces all occurences of old in s with new.", categories = Category.STRINGS)
	public String evaluate(String input, String match, String replacement) {
		return input.replace(match, replacement);
	}
}
