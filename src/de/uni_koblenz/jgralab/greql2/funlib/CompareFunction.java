/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/**
 *
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public abstract class CompareFunction extends Greql2Function {

	protected enum CompareOperator {
		GR_THAN, GR_EQUAL, LE_THAN, LE_EQUAL, NOT_EQUAL
	}

	{
		JValueType[][] x = {
				{ JValueType.NUMBER, JValueType.NUMBER, JValueType.BOOL },
				{ JValueType.STRING, JValueType.STRING, JValueType.BOOL } };
		signatures = x;

		description = "Compare 2 number or strings, and return a boolean.";

		Category[] c = { Category.COMPARISONS };
		categories = c;
	}

	public JValue evaluate(JValue[] arguments, CompareOperator op)
			throws EvaluateException {
		String s1 = null, s2 = null;
		double d1 = 0, d2 = 0;
		JValue result = null;
		switch (checkArguments(arguments)) {
		case 0:
			d1 = arguments[0].toDouble();
			d2 = arguments[1].toDouble();
			switch (op) {
			case GR_EQUAL:
				result = JValueBoolean.getValue(d1 >= d2);
				break;
			case GR_THAN:
				result = JValueBoolean.getValue(d1 > d2);
				break;
			case LE_EQUAL:
				result = JValueBoolean.getValue(d1 <= d2);
				break;
			case LE_THAN:
				result = JValueBoolean.getValue(d1 < d2);
				break;
			case NOT_EQUAL:
				result = JValueBoolean.getValue(d1 != d2);
				break;
			default:
				throw new EvaluateException("Unknown operator: " + op);
			}
			break;
		case 1:
			s1 = arguments[0].toString();
			s2 = arguments[1].toString();
			switch (op) {
			case GR_EQUAL:
				result = JValueBoolean.getValue(s1.compareTo(s2) >= 0);
				break;
			case GR_THAN:
				result = JValueBoolean.getValue(s1.compareTo(s2) == 1);
				break;
			case LE_EQUAL:
				result = JValueBoolean.getValue(s1.compareTo(s2) <= 0);
				break;
			case LE_THAN:
				result = JValueBoolean.getValue(s1.compareTo(s2) == -1);
				break;
			case NOT_EQUAL:
				result = JValueBoolean.getValue(s1.compareTo(s2) != 0);
				break;
			default:
				throw new EvaluateException("Unknown operator: " + op);
			}
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
		return result;
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
