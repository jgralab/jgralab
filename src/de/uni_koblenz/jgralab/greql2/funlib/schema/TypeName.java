package de.uni_koblenz.jgralab.greql2.funlib.schema;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class TypeName extends Function {

	public TypeName() {
		super("Returns the qualified name of the given element's type.",
				Category.SCHEMA_ACCESS);
	}

	public String evaluate(AttributedElement<?, ?> el) {
		return el.getAttributedElementClass().getQualifiedName();
	}
}
