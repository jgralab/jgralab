/**
 *
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public abstract class BooleanFunction extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.BOOL, JValueType.BOOL,
				JValueType.BOOL } };
		signatures = x;

		description = "Return the value of this logical operation on the given two booleans.";

		Category[] c = { Category.LOGICS };
		categories = c;
	}

	protected enum BooleanOperator {
		AND, OR, XOR
	};

	public JValue evaluate(JValue[] arguments, BooleanOperator operator)
			throws EvaluateException {
		if (checkArguments(arguments) == -1) {
			throw new WrongFunctionParameterException(this, arguments);
		}

		return applyFunction(arguments[0], arguments[1]);
	}

	protected abstract JValue applyFunction(JValue leftHandSide,
			JValue rightHandSide);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#getEstimatedCardinality
	 * (int)
	 */
	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#getEstimatedCosts
	 * (java.util.ArrayList)
	 */
	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#getSelectivity()
	 */
	@Override
	public double getSelectivity() {
		return 0.5;
	}

}
