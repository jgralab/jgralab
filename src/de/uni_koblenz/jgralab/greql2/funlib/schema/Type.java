package de.uni_koblenz.jgralab.greql2.funlib.schema;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class Type extends Function {

	public Type() {
		super("Returns the AttributedElementClass of the given element.",
				Category.SCHEMA_ACCESS);
	}

	public AttributedElementClass<?, ?> evaluate(AttributedElement<?, ?> el) {
		return el.getAttributedElementClass();
	}
}
