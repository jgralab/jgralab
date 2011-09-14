package de.uni_koblenz.jgralab.greql2.funlib.schema;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class HasAttribute extends Function {

	public HasAttribute() {
		super(
				"Determines whether the attribute $name$ is defined for an element $el$ or class $aec$.",
				Category.GRAPH);
	}

	public Boolean evaluate(AttributedElementClass aec, String name) {
		return aec.containsAttribute(name);
	}

	public Boolean evaluate(AttributedElement el, String name) {
		return evaluate(el.getAttributedElementClass(), name);
	}
}
