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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.FunctionUnknownFieldException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord;

/**
 * Returns the given attribute or element value for a vertex, an edge or a
 * record. The attribute is called by its name.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>OBJECT getValue(elem:ATTRIBUTEDELEMENT, name:String)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dd>This function can be used with the (.)-Operator: <code>elem.name</code></dd>
 * <dd>&nbsp;</dd>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>elem</code> - the attributed element to get the value for</dd>
 * <dd><code>name</code> - the name of the attribute to be returned</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the value of the attribute with the given name</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 *
 * @author ist@uni-koblenz.de
 *
 */

/*
 * Returns the given attribute or element value for a vertex, an edge or a
 * record. The attribute is called by its name. <br /><br />
 * <strong>Parameters:</strong> <ul> <li> elem: (AttributedElement |
 * JValueRecord) (vertex, edge or Record to acces)</li> <li> name: String (name
 * of the Attribute or RecordElement to return)</li> </ul>
 * <strong>Returns:</strong> the value of the given attribute or RecordElement,
 * encapsulated in a JValue
 *
 * @author ist@uni-koblenz.de
 */
public class GetValue implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (arguments.length < 2) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		String fieldName = arguments[1].toString();
		AttributedElement elem = null;
		try {
			// check if the first argument is a graphelement, then access the
			// attribute with the given name of this graph element
			if (arguments[0].isEdge()) {
				elem = arguments[0].toEdge();
			} else if (arguments[0].isVertex()) {
				elem = arguments[0].toVertex();
			}
			if (elem != null) {
				return JValue.fromObject(elem.getAttribute(fieldName), elem);
			}
			// check if the first argument is a JValueCollection
			if (arguments[0].isCollection()) {
				JValueCollection col = arguments[0].toCollection();
				if (col.isJValueRecord()) {
					JValueRecord rec = col.toJValueRecord();
					return rec.get(arguments[1].toString());
				}
			}
		} catch (SecurityException ex) {
			throw new FunctionUnknownFieldException(elem.getClass().getName(),
					fieldName, null);
		} catch (IllegalArgumentException ex) {
			throw new FunctionUnknownFieldException(elem.getClass().getName(),
					fieldName, null);
		} catch (Exception ex) {
			// JValueInvalidTypeException,
			// NoSuchFieldException,
			// IndexOutOfBoundsException
			throw new WrongFunctionParameterException(this, null, arguments, ex);
		}

		return new JValue();
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
		return "(AttributedElement, String)";
	}

}
