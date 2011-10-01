package de.uni_koblenz.jgralab.greql2.funlib.misc;

import de.uni_koblenz.jgralab.greql2.funlib.AcceptsUndefinedArguments;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Undefined;

@AcceptsUndefinedArguments
public class isUndefined extends Function {

	public isUndefined() {
		super("Returns true if $val$ is undefined.", Category.DEBUGGING);
	}

	public Object evaluate(Object val) {
		return (val instanceof Undefined);
	}
}
