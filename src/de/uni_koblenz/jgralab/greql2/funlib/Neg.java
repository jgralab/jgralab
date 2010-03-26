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

package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Calculates the negation of a given scalar.
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>INT uminus (a: INT)</code></dd>
 * <dd><code>LONG uminus (a: LONG)</code></dd>
 * <dd><code>DOUBLE uminus (a: DOUBLE)</code></dd>
 * <dd></dd>
 * <dd>This function can be used with the (-)-Operator: <code>-a</code></dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>a: INT</code> - value to be negated</dd>
 * <dd><code>a: LONG</code> - value to be negated</dd>
 * <dd><code>a: DOUBLE</code> - value to be negated</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the negation of <code>a</code>.</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class Neg extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.LONG, JValueType.LONG },
				{ JValueType.INT, JValueType.INT },
				{ JValueType.DOUBLE, JValueType.DOUBLE } };
		signatures = x;

		description = "Calculates the negation $-a$ of a given number $a$. Alternative usage: -a.";

		Category[] c = { Category.ARITHMETICS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			return new JValueImpl(-arguments[0].toLong());
		case 1:
			return new JValueImpl(-arguments[0].toInteger());
		case 2:
			return new JValueImpl(-arguments[0].toDouble());
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 1;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
