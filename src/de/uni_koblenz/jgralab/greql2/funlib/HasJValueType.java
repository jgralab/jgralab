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

public class HasJValueType extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.OBJECT, JValueType.STRING,
				JValueType.BOOL } };
		signatures = x;

		description = "Checks if the given value has (or can convert to) the given JValueType.\n"
				+ "The JValueType is given as string.";

		Category[] c = { Category.UNDEFINED };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		if (checkArguments(arguments) < 0) {
			throw new WrongFunctionParameterException(this, arguments);
		}
		JValue var = arguments[0];
		String typeName = arguments[1].toString();
		JValueType type = JValueType.valueOf(typeName);
		if (type == null) {
			throw new EvaluateException("No such JValueType '" + typeName
					+ "'.");
		}
		return new JValueImpl(var.canConvert(type));
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSelectivity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		// TODO Auto-generated method stub
		return 0;
	}

}
