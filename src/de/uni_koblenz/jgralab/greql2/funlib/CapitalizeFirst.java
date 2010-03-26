/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Capitalizes the first character of the given string.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>STRING capitalizeFirst(str:STRING)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>str</code> - a STRING</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>The input string with the first character capitalized
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class CapitalizeFirst extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.STRING, JValueType.STRING } };
		signatures = x;

		description = "Returns the given string with the first character made uppercase.";

		Category[] c = { Category.STRINGS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) < 0) {
			throw new WrongFunctionParameterException(this, arguments);
		}
		String str = arguments[0].toString();
		if (str.length() == 0) {
			return arguments[0];
		}
		if (str.length() == 1) {
			return new JValueImpl(str.toUpperCase());
		}
		return new JValueImpl(Character.toUpperCase(str.charAt(0))
				+ str.substring(1));
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
