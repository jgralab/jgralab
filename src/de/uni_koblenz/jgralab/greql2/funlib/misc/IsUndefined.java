package de.uni_koblenz.jgralab.greql2.funlib.misc;

import de.uni_koblenz.jgralab.greql2.funlib.AcceptsUndefinedArguments;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Undefined;

@AcceptsUndefinedArguments
public class IsUndefined extends Function {

	public IsUndefined() {
		super("Returns true, iff the given object is undefined.",
				Category.MISCELLANEOUS);
	}

	public Boolean evaluate(Object val) {
		return (val instanceof Undefined);
	}
}
