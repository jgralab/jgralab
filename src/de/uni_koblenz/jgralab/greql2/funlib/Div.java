/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.funlib;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;

/**
 * Calculates the quotient (a/b) for given scalar values a and b. The quotient
 * (a / 0) will return the value positive or negative infinity.
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class Div extends ArithmeticFunction {
	{
		description = "Calculates the quotient $a / b$. "
				+ "$a / 0$ will return either a positive or a negative infinity. Alternative usage: a / b.";
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		return evaluate(arguments, ArithmeticOperator.DIV);
	}

	@Override
	protected JValue applyFunction(Integer leftHandSide, Integer rightHandSide) {
		return new JValue(Double.valueOf(leftHandSide) / rightHandSide);
	}

	@Override
	protected JValue applyFunction(Long leftHandSide, Long rightHandSide) {
		return new JValue(Double.valueOf(leftHandSide) / rightHandSide);
	}

	@Override
	protected JValue applyFunction(Double leftHandSide, Double rightHandSide) {
		return new JValue(leftHandSide / rightHandSide);
	}

}
