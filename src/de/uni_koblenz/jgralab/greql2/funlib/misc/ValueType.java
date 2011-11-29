package de.uni_koblenz.jgralab.greql2.funlib.misc;

import java.util.Arrays;

import de.uni_koblenz.jgralab.greql2.funlib.AcceptsUndefinedArguments;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Types;

@AcceptsUndefinedArguments
public class ValueType extends Function {

	public ValueType() {
		super(
				"Returns a String denoting the value type of the given object. Possible value types are: "
						+ Arrays.toString(Types.getGreqlTypeNames()),
				Category.REFLECTION);
	}

	public String evaluate(Object val) {
		return Types.getGreqlTypeName(val);
	}
}
