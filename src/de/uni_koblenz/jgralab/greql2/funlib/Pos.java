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

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;

/**
 * Returns the index of the given object in the given List or Tuple.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>INTEGER pos(list:LIST&lt;OBJECT&gt;, element:OBJECT)</code></dd>
 * <dd><code>INTEGER pos(tuple:TUPLE&lt;OBJECT&gt;, element:OBJECT)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>list</code> - list to work with</dd>
 * <dd><code>tuple</code> - tuple to work with</dd>
 * <dd><code>element</code> - element to return index for in the given list or
 * tuple</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the index of the given object in the given list or tuple</dd>
 * <dd><code>-1</code> if the given Object is not in the given list or tuple</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 *
 * @author ist@uni-koblenz.de
 *
 */

/*
 * Gets a ordered collection (list or tuple) and a objet as parameters and
 * returns the position of the element in the collection. The first element is,
 * as it is in Java too, the element 0. @param orderedCollection a JValueList or
 * JValueTuple @param element the element to calculate the position for @return
 * the position of the given element in the givne collection or -1, if the given
 * object is not a member of the given collection
 *
 * @author ist@uni-koblenz.de
 */

public class Pos implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (arguments.length < 2) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		try {
			if (arguments[0].isCollection()) {
				JValue object = arguments[1];
				JValueCollection col = arguments[0].toCollection();
				if (col.isJValueList()) {
					JValueList list = col.toJValueList();
					return new JValue(list.indexOf(object));
				}
				if (col.isJValueTuple()) {
					JValueTuple tup = col.toJValueTuple();
					return new JValue(tup.indexOf(object));
				}
			}
		} catch (Exception ex) { // JValueInvalidTypeException,
			// NoSuchFieldException,
			// IndexOutOfBoundsException
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		return new JValue(-1);
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
		return "(JValueList or JValueTuple, Object )";
	}

}
