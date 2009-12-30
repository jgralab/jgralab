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
		JValueType[][] x = { { JValueType.DOUBLE, JValueType.DOUBLE },
				{ JValueType.LONG, JValueType.LONG },
				{ JValueType.INTEGER, JValueType.INTEGER } };
		signatures = x;
		description = "Perform arithmetic operation on the given 2 number.";
	}

	protected enum ArithmeticOperator {
		PLUS, MINUS, DIV, TIMES
	};

	public JValue evaluate(JValue[] arguments, ArithmeticOperator operator)
			throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			Double d1 = arguments[0].toDouble();
			Double d2 = arguments[1].toDouble();
			switch (operator) {
			case PLUS:
				return new JValue(d1 + d2);
			case MINUS:
				return new JValue(d1 - d2);
			case DIV:
				return new JValue(d1 / d2);
			case TIMES:
				return new JValue(d1 * d2);
			default:
				throw new EvaluateException("Unknown ArithmeticOperator "
						+ operator + ".");
			}
		case 1:
			Long l1 = arguments[0].toLong();
			Long l2 = arguments[1].toLong();
			switch (operator) {
			case PLUS:
				return new JValue(l1 + l2);
			case MINUS:
				return new JValue(l1 - l2);
			case DIV:
				return new JValue(Double.valueOf(l1) / l2);
			case TIMES:
				return new JValue(l1 * l2);
			default:
				throw new EvaluateException("Unknown ArithmeticOperator "
						+ operator + ".");
			}
		case 2:
			Integer i1 = arguments[0].toInteger();
			Integer i2 = arguments[1].toInteger();
			switch (operator) {
			case PLUS:
				return new JValue(i1 + i2);
			case MINUS:
				return new JValue(i1 - i2);
			case DIV:
				return new JValue(Double.valueOf(i1) / i2);
			case TIMES:
				return new JValue(i1 * i2);
			default:
				throw new EvaluateException("Unknown ArithmeticOperator "
						+ operator + ".");
			}
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}

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
