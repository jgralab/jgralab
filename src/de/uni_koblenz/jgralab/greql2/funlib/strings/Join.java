package de.uni_koblenz.jgralab.greql2.funlib.strings;

import java.util.List;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Join extends Function {
	public Join() {
		super(
				"Joins the given strings in $l$ by interleaving with $delimiter$.",
				10, 1, 0.1, Category.STRINGS);
	}

	public String evaluate(List<String> l, String delimiter) {
		StringBuilder sb = new StringBuilder();
		String d = "";
		for (String s : l) {
			sb.append(d).append(s);
			d = delimiter;
		}
		return sb.toString();
	}
}
