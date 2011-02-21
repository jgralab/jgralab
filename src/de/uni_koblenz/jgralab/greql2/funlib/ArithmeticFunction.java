/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
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

	public JValue evaluate(JValue[] arguments) throws EvaluateException {

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
