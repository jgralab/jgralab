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
 * Calculates a*b for given scalar values a and b or the n-fold concatenation of
 * a given string.
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>INT times(a: INT, b: INT)</code></dd>
 * <dd><code>LONG times(a: LONG, b: INT)</code></dd>
 * <dd><code>LONG times(a: INT, b: LONG)</code></dd>
 * <dd><code>LONG times(a: LONG, b: LONG)</code></dd>
 * <dd><code>DOUBLE times(a: DOUBLE, b: INT)</code></dd>
 * <dd><code>DOUBLE times(a: DOUBLE, b: LONG)</code></dd>
 * <dd><code>DOUBLE times(a: INT, b: DOUBLE)</code></dd>
 * <dd><code>DOUBLE times(a: LONG, b: DOUBLE)</code></dd>
 * <dd><code>DOUBLE times(a: DOUBLE, b: DOUBLE)</code></dd>
 * <dd><code>STRING times(s: STRING, n: INT)</code></dd>
 * <dd><code>STRING times(s: STRING, n: LONG)</code></dd>
 * <dd>&nbsp;</dd>
 * <dd>This function can be used with the (*)-Operator: <code>a * b</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>a: INT</code> - left factor</dd>
 * <dd><code>a: LONG</code> - left factor</dd>
 * <dd><code>a: DOUBLE</code> - left factor</dd>
 * <dd><code>b: INT</code> - right factor</dd>
 * <dd><code>b: LONG</code> - right factor</dd>
 * <dd><code>b: DOUBLE</code> - right factor</dd>
 * <dd><code>s: STRING</code> - string for n-fold concatenation
 * <dd><code>n: INT</code> - amount of corcatenations
 * <dd><code>n: LONG</code> - amount of concatenations
 * <dt><b>Returns:</b></dt>
 * <dd>the product <code>a * b</code> or the n-fold concatenation
 * <code>s * n<code></dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class Times extends ArithmeticFunction {
	{
		JValueType[][] x = {
				{ JValueType.DOUBLE, JValueType.DOUBLE, JValueType.DOUBLE },
				{ JValueType.LONG, JValueType.LONG, JValueType.LONG },
				{ JValueType.INT, JValueType.INT, JValueType.INT },
				{ JValueType.STRING, JValueType.LONG, JValueType.STRING } };
		signatures = x;

		description = "Multiply the given two numbers, or concat the given string many times.";

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
			return evaluate(arguments, ArithmeticOperator.TIMES);
		case 3:
			String s = arguments[0].toString();
			long l = arguments[1].toLong();
			StringBuffer sb = new StringBuffer();
			while (l > 0) {
				--l;
				sb.append(s);
			}
			return new JValue(sb.toString());
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}
}
