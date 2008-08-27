/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;

/**
 * Calculates a-b for given scalar values a and b.
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>INTEGER minus(a: INTEGER, b: INTEGER)</code></dd>
 * <dd><code>LONG minus(a: LONG, b: INTEGER)</code></dd>
 * <dd><code>LONG minus(a: INTEGER, b: LONG)</code></dd>
 * <dd><code>LONG minus(a: LONG, b: LONG)</code></dd>
 * <dd><code>DOUBLE minus(a: DOUBLE, b: INTEGER)</code></dd>
 * <dd><code>DOUBLE minus(a: DOUBLE, b: LONG)</code></dd>
 * <dd><code>DOUBLE minus(a: INTEGER, b: DOUBLE)</code></dd>
 * <dd><code>DOUBLE minus(a: LONG, b: DOUBLE)</code></dd>
 * <dd><code>DOUBLE minus(a: DOUBLE, b: DOUBLE)</code></dd>
 * <dd>&nbsp;</dd>
 * <dd>This function can be used with the (-)-Operator: <code>a - b</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>a: INTEGER</code> - minuend</dd>
 * <dd><code>a: LONG</code> - minuend</dd>
 * <dd><code>a: DOUBLE</code> - minuend</dd>
 * <dd><code>b: INTEGER</code> - subtrahend</dd>
 * <dd><code>b: LONG</code> - subtrahend</dd>
 * <dd><code>b: DOUBLE</code> - subtrahend</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the difference <code>a - b</code></dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class Minus implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (arguments.length < 2) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		try {
			if (arguments[0].isDouble() || arguments[1].isDouble()) {
				Double d = arguments[0].toDouble() - arguments[1].toDouble();
				return new JValue(d);
			}
			if (arguments[0].isLong() || arguments[1].isLong()) {
				Long l = arguments[0].toLong() - arguments[1].toLong();
				return new JValue(l);
			}
			Integer i = arguments[0].toInteger() - arguments[1].toInteger();
			return new JValue(i);
		} catch (Exception ex) {
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

	public String getExpectedParameters() {
		return "(Double, Double)";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}
}
