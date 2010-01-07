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
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Calculates a+b for given scalar values a and b or calculates the string
 * concatenation of two given strings s1 and s2.
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>INT plus(a: INT, b: INT)</code></dd>
 * <dd><code>LONG plus(a: LONG, b: INT)</code></dd>
 * <dd><code>LONG plus(a: INT, b: LONG)</code></dd>
 * <dd><code>LONG plus(a: LONG, b: LONG)</code></dd>
 * <dd><code>DOUBLE plus(a: DOUBLE, b: INT)</code></dd>
 * <dd><code>DOUBLE plus(a: DOUBLE, b: LONG)</code></dd>
 * <dd><code>DOUBLE plus(a: INT, b: DOUBLE)</code></dd>
 * <dd><code>DOUBLE plus(a: LONG, b: DOUBLE)</code></dd>
 * <dd><code>DOUBLE plus(a: DOUBLE, b: DOUBLE)</code></dd>
 * <dd><code>STRING plus(s1: STRING, s2: STRING)</code></dd>
 * <dd>&nbsp;</dd>
 * <dd>This function can be used with the (+)-Operator: <code>a + b</code></dd>
 * <dd>If one of the parameters does not match any of these signatures, both
 * parameters are converted to their string representation and a string
 * concatenation will be calculated.</dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>a: INT</code> - first summand</dd>
 * <dd><code>a: LONG</code> - first summand</dd>
 * <dd><code>a: DOUBLE</code> - first summand</dd>
 * <dd><code>s1: STRING</code> - first string to concatenate</dd>
 * <dd><code>b: INT</code> - second summand</dd>
 * <dd><code>b: LONG</code> - second summand</dd>
 * <dd><code>b: DOUBLE</code> - second summand</dd>
 * <dd><code>s2: STRING</code> - second string to concatenate</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the sum <code>a + b</code></dd>
 * <dd>the string concatenation <code>s1 + s2</code></dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class Plus extends ArithmeticFunction {
	{
		JValueType[][] x = {
				{ JValueType.DOUBLE, JValueType.DOUBLE, JValueType.DOUBLE },
				{ JValueType.LONG, JValueType.LONG, JValueType.LONG },
				{ JValueType.INT, JValueType.INT, JValueType.INT },
				{ JValueType.STRING, JValueType.STRING, JValueType.STRING } };
		signatures = x;

		description = "Calculates the product $a+b$ or concatenate the two given strings.";

		Category[] c = { Category.ARITHMETICAL, Category.STRINGS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
		case 1:
		case 2:
			return evaluate(arguments, ArithmeticOperator.PLUS);
		case 3:
			return new JValue(arguments[0].toString() + arguments[1].toString());
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}
}
