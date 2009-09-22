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
public abstract class CompareFunction extends AbstractGreql2Function {

	protected enum CompareOperator {
		GR_THAN, GR_EQUAL, LE_THAN, LE_EQUAL, NOT_EQUAL
	}

	{
		JValueType[][] x = { { JValueType.NUMBER, JValueType.NUMBER },
				{ JValueType.STRING, JValueType.STRING } };
		signatures = x;
	}

	public JValue evaluate(JValue[] arguments, CompareOperator op)
			throws EvaluateException {
		String s1 = null, s2 = null;
		double d1 = 0, d2 = 0;
		switch (checkArguments(arguments)) {
		case 0:
			d1 = arguments[0].toDouble();
			d2 = arguments[1].toDouble();
			switch (op) {
			case GR_EQUAL:
				return new JValue(d1 >= d2);
			case GR_THAN:
				return new JValue(d1 > d2);
			case LE_EQUAL:
				return new JValue(d1 <= d2);
			case LE_THAN:
				return new JValue(d1 < d2);
			case NOT_EQUAL:
				return new JValue(d1 != d2);
			}
			break;
		case 1:
			s1 = arguments[0].toString();
			s2 = arguments[1].toString();
			switch (op) {
			case GR_EQUAL:
				return new JValue(s1.compareTo(s2) >= 0);
			case GR_THAN:
				return new JValue(s1.compareTo(s2) == 1);
			case LE_EQUAL:
				return new JValue(s1.compareTo(s2) <= 0);
			case LE_THAN:
				return new JValue(s1.compareTo(s2) == -1);
			case NOT_EQUAL:
				return new JValue(s1.compareTo(s2) != 0);
			}
			break;
		default:
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		throw new EvaluateException("You mustn't come here!");
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
		return 1;
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
