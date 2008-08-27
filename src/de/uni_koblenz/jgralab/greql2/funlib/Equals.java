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
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;

/**
 * Checks if two different objects are equal.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN equals(obj1:OBJECT, obj2:OBJECT)</code></dd>
 * <dd>&nbsp;</dd>
 * <dd>This function can be used with the (=)-Operator: <code>obj1 = obj2</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl><dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>obj1</code> - first object to compare</dd>
 * <dd><code>obj2</code> - second object to compare</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if both objects are equal</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class Equals implements Greql2Function {

	/**
	 * checks if the two function parameters are semanticly identical
	 */
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph, JValue[] arguments)  throws EvaluateException {
		if ((arguments == null) | (arguments.length < 2)) {
			throw new WrongFunctionParameterException(this, null, arguments );
		}	
		boolean value = arguments[0].equals(arguments[1]);
		if (value)
			return new JValue(JValueBoolean.getTrueValue());
		return new JValue(JValueBoolean.getFalseValue());
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
	}

	public double getSelectivity() {
		return 0.05;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}
	
	public String getExpectedParameters() {
		return "(Object, Object)";
	}

	@Override
	public boolean isPredicate() {
		return true;
	}

}
