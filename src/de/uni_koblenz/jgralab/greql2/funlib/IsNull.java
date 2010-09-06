package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

public class IsNull extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.OBJECT, JValueType.BOOL } };
		signatures = x;

		description = "Returns true iff the given object is null.\n"
				+ "Most probably, the only usecase is checking if an\n"
				+ "attribute is not set: isNull(elem.someAttr).";

		Category[] c = { Category.GRAPH };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, AbstractGraphMarker<AttributedElement> subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) < 0) {
			throw new WrongFunctionParameterException(this, arguments);
		}
		return new JValueImpl(!arguments[0].isValid());
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
		return 0.1;
	}

}
