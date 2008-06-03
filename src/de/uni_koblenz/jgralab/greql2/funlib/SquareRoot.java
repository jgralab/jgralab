/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;

/**
 * Calculates the squareroot of a given scalar.
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>DOUBLE squareRoot(a: INTEGER)</code></dd>
 * <dd><code>DOUBLE squareRoot(a: LONG)</code></dd>
 * <dd><code>DOUBLE squareRoot(a: DOUBLE)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>a: INTEGER</code> - value to calculate squareroot for</dd>
 * <dd><code>a: LONG</code> - value to calculate squareroot for</dd>
 * <dd><code>a: DOUBLE</code> - value to calculate squareroot for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the squareroot of <code>a</code>.</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

/*
 * Calculates the squareRoot of the given value @param value a double value to
 * create the squareroot of @return the squareroot as double @author Daniel
 * Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class SquareRoot implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) {
		try {
			Double arg1 = arguments[1].toDouble();
			return new JValue(Math.sqrt(arg1));
		} catch (Exception ex) {
			return new JValue();
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
		return "(Number)";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}
}
