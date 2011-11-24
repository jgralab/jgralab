package de.uni_koblenz.jgralab.greql2.funlib.misc;

import de.uni_koblenz.jgralab.greql2.funlib.AcceptsUndefinedArguments;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

@AcceptsUndefinedArguments
public class Log extends Function {

	public Log() {
		super(
				"Logs a line of the form  $s$ ++ toString($o$) to sysout and returns $o$.",
				Category.MISCELLANEOUS);
	}

	public Object evaluate(String s, Object o) {
		System.out.println(s + ": " + o.toString());
		// FunLib.instance().getLogger().info(s + o.toString());
		return o;
	}
}
