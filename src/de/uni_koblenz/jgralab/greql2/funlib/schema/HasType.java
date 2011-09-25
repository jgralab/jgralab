package de.uni_koblenz.jgralab.greql2.funlib.schema;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.exception.GreqlException;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class HasType extends Function {

	public HasType() {
		super("Returns the AttributedElementClass of the element $el$.",
				Category.SCHEMA_ACCESS);
	}

	public Boolean evaluate(AttributedElement el, String qn) {
		AttributedElementClass aec = el.getSchema().getAttributedElementClass(
				qn);
		if (aec == null) {
			throw new GreqlException("hasType: Schema doesn't contain a type '");
		}
		return evaluate(el, aec);
	}

	public Boolean evaluate(AttributedElement el, AttributedElementClass aec) {
		AttributedElementClass c = el.getAttributedElementClass();
		return c.equals(aec) || c.isSubClassOf(aec);
	}
}
