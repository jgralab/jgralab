package de.uni_koblenz.jgralab.greql2.funlib.schema;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class HasAttribute extends Function {

	public HasAttribute() {
		super(
				"Returns true, iff the attribute given by its name is defined for the given attributed element or attributed element class.",
				Category.SCHEMA_ACCESS);
	}

	public Boolean evaluate(AttributedElementClass<?, ?> aec, String name) {
		return aec.containsAttribute(name);
	}

	public Boolean evaluate(AttributedElement<?, ?> el, String name) {
		return evaluate(el.getAttributedElementClass(), name);
	}
}
