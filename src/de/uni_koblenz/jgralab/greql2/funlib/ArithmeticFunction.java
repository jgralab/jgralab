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
public abstract class ArithmeticFunction extends Greql2Function {
	{
		JValueType[][] x = {
				{ JValueType.DOUBLE, JValueType.DOUBLE, JValueType.DOUBLE },
				{ JValueType.LONG, JValueType.LONG, JValueType.LONG },
				{ JValueType.INT, JValueType.INT, JValueType.INT } };
		signatures = x;
		description = "Performs arithmetic operation on the given operands.";

		Category[] c = { Category.ARITHMETICS };
		categories = c;
	}

	protected enum ArithmeticOperator {
		ADD, SUB, DIV, MUL, MOD
	};

	public JValue evaluate(JValue[] arguments, ArithmeticOperator operator)
			throws EvaluateException {

		JValue result = null;

		switch (checkArguments(arguments)) {
		case 0:
			Double d1 = arguments[0].toDouble();
			Double d2 = arguments[1].toDouble();
			result = applyFunction(d1, d2);
			break;
		case 1:
			Long l1 = arguments[0].toLong();
			Long l2 = arguments[1].toLong();
			result = applyFunction(l1, l2);
			break;
		case 2:
			Integer i1 = arguments[0].toInteger();
			Integer i2 = arguments[1].toInteger();
			result = applyFunction(i1, i2);
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}

		return result;
	}

	protected abstract JValue applyFunction(Integer leftHandSide,
			Integer rightHandSide);

	protected abstract JValue applyFunction(Long leftHandSide,
			Long rightHandSide);

	protected abstract JValue applyFunction(Double leftHandSide,
			Double rightHandSide);

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
		return 4;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#getSelectivity()
	 */
	@Override
	public double getSelectivity() {
		return 1;
	}

}
