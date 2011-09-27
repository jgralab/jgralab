package de.uni_koblenz.jgralab.greql2.funlib.misc;

import de.uni_koblenz.jgralab.greql2.funlib.AcceptsUndefinedArguments;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Types;

public class ValueType extends Function implements AcceptsUndefinedArguments {

	public ValueType() {
		super("Returns a String denoting the value type of $val$.",
				Category.REFLECTION);
	}

	public String evaluate(Object val) {
		return Types.getGreqlTypeName(val);
	}
}
