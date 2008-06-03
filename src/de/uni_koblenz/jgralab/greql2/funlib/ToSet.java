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

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;

/**
 * Converts a collection or even a single element into a JValueSet
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET contains(c:COLLECTION or ELEMENT)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>c</code> - collection or element to convert to a setk</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a set containing all elements of c or the single given element</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Feb 2008
 * 
 */

public class ToSet implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (arguments.length < 1) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		try {
			if (arguments[0].isCollection()) {
				JValueCollection col = arguments[0].toCollection();
				return col.toJValueSet();
			} else {
				JValueSet set = new JValueSet();
				set.add(arguments[0]);
				return set;
			}
		} catch (Exception ex) { // JValueInvalidTypeException,
									// NoSuchFieldException,
									// IndexOutOfBoundsException
		}
		throw new WrongFunctionParameterException(this, null, arguments);
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
		return "(JValueCollection or Element)";
	}

	@Override
	public boolean isPredicate() {
		return true;
	}

}
