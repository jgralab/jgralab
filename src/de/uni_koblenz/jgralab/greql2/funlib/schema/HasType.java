package de.uni_koblenz.jgralab.greql2.funlib.schema;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.exception.GreqlException;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class HasType extends Function {

	public HasType() {
		super(
				"Returns true, iff the given attributed element or attributed element class has an attribute with the given name.",
				Category.SCHEMA_ACCESS);
	}

	public <SC extends AttributedElementClass<SC, IC>, IC extends AttributedElement<SC, IC>> Boolean evaluate(
			IC el, String qn) {
		SC aec = el.getSchema().getAttributedElementClass(qn);
		if (aec == null) {
			throw new GreqlException("hasType: Schema doesn't contain a type '");
		}
		return evaluate(el, aec);
	}

	private <SC extends AttributedElementClass<SC, IC>, IC extends AttributedElement<SC, IC>> Boolean evaluate(
			IC el, SC aec) {
		SC c = el.getAttributedElementClass();
		return c.equals(aec) || c.isSubClassOf(aec);
	}

	public <SC extends AttributedElementClass<SC, IC>, IC extends AttributedElement<SC, IC>> Boolean evaluate(
			IC el, TypeCollection tc) {
		SC c = el.getAttributedElementClass();
		return tc.acceptsType(c);
	}

}

/*

*/