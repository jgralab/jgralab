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

import java.util.ArrayList;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Calculates the remainder of the integer-division (a / b) for given integer
 * values a and b.
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>INTEGER modulo(a: INTEGER, b: INTEGER)</code></dd>
 * <dd><code>LONG modulo(a: INTEGER, b: LONG)</code>
 * <dd><code>LONG modulo(a: LONG, b: INTEGER)</code></dd>
 * <dd><code>LONG modulo(a: LONG, b: LONG)</code></dd>
 * <dd>&nbsp;</dd>
 * <dd>This function can be used with the (%)-Operator: <code>a % b</code></dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>a: INTEGER</code> - dividend</dd>
 * <dd><code>a: LONG</code> - dividend</dd>
 * <dd><code>b: INTEGER</code> - divisor</dd>
 * <dd><code>b: LONG</code> - divisor</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the remainder of the integer-division <code>a / b</code></dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class Modulo extends AbstractGreql2Function {
	{
		JValueType[][] x = { { JValueType.LONG, JValueType.LONG },
				{ JValueType.INTEGER, JValueType.INTEGER } };
		signatures = x;
	}

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			return new JValue(arguments[0].toLong() % arguments[1].toLong());
		case 1:
			return new JValue(arguments[0].toInteger()
					% arguments[1].toInteger());
		default:
			throw new WrongFunctionParameterException(this, null, arguments);
		}
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
	}

	public double getSelectivity() {
		return 1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
