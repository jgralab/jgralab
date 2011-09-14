package de.uni_koblenz.jgralab.greql2.funlib.misc;

import de.uni_koblenz.jgralab.greql2.funlib.AcceptsUndefinedArguments;
import de.uni_koblenz.jgralab.greql2.funlib.FunLib;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Log extends Function implements AcceptsUndefinedArguments {

	public Log() {
		super(
				"Logs a line of the form  $s$ ++ toString($o$) to sysout and returns $o$.",
				Category.DEBUGGING);
	}

	public Object evaluate(String s, Object o) {
		FunLib.instance().getLogger().info(s + o.toString());
		return o;
	}
}
