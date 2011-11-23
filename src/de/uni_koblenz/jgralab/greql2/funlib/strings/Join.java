package de.uni_koblenz.jgralab.greql2.funlib.strings;

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Join extends Function {
	public Join() {
		super(
				"Joins the strings in the given collection by interleaving with the given delimiter.",
				10, 1, 0.1, Category.STRINGS);
	}

	public String evaluate(PCollection<String> l, String delimiter) {
		StringBuilder sb = new StringBuilder();
		String d = "";
		for (String s : l) {
			sb.append(d).append(s);
			d = delimiter;
		}
		return sb.toString();
	}
}
