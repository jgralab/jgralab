package de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Joins all JValues in the provides list as Strings separated by a supplied
 * delimiter.
 * 
 * <dl>
 * <dt><b>GReQL-signatures</b></dt>
 * <dd><code>STRING join(delimiter:STRING, list:COLLECTION)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>delimiter</code> - Delimiter string set between every joined
 * String.</dd>
 * <dd><code>list</code> - Collection a JValues.</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>Joined List</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class Join extends Greql2Function {

	{
		JValueType[][] x = { { JValueType.STRING, JValueType.STRING,
				JValueType.COLLECTION } };
		signatures = x;

		description = "Returns a string of joined element of a given collection and a delimiter.";

		Category[] c = { Category.STRINGS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {

		switch (checkArguments(arguments)) {
		case 0:
			StringBuilder builder = new StringBuilder(100);
			String delimiter = arguments[0].toString();

			JValueCollection collection = arguments[1].toCollection();

			for (JValue value : collection) {
				builder.append(value.toString());
				builder.append(delimiter);
			}

			return new JValueImpl(builder.toString());
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
		return 1;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

}
