package de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Escapes all illegal characters in given DOT-string.
 * 
 * <dl>
 * <dt><b>GReQL-signatures</b></dt>
 * <dd><code>STRING toDotString(string:STRING)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>string</code> - String, which is converted to a DOT-string.</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>An escaped String.</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ToDotString extends Greql2Function {

	{
		JValueType[][] x = { { JValueType.STRING, JValueType.STRING } };
		signatures = x;

		description = "Returns a converted DOT string representation of the given string.";

		Category[] c = { Category.STRINGS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {

		switch (checkArguments(arguments)) {
		case 0:
			String string = arguments[0].toString();
			if (arguments[0].isString()) {
				string = "\"" + string + "\"";
			}
			string = string.replace("\\", "\\\\");
			string = string.replace("|", "\\|");
			string = string.replace("{", "\\{");
			string = string.replace("}", "\\}");
			string = string.replace("\n", "\\n");
			string = string.replace("\"", "\\\"");
			string = string.replace("<", "\\<");
			string = string.replace(">", "\\>");
			return new JValueImpl(string);
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}
}
