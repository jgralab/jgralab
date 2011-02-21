/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */

package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns the given attribute or element value for a vertex, an edge or a
 * record. The attribute is called by its name. Furthermore, values assigned to
 * an graph element by a graph marker can be accessed by providing the graph
 * marker as second parameter to the function instead of the attribute name.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>OBJECT getValue(elem:ATTRELEM, name:String)</code></dd>
 * <dd><code>OBJECT getValue(elem:RECORD, name:String)</code></dd>
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
public class GetValue extends Greql2Function {
	{
		JValueType[][] x = {
				{ JValueType.ATTRELEM, JValueType.STRING, JValueType.OBJECT },
				{ JValueType.RECORD, JValueType.STRING, JValueType.OBJECT },
				{ JValueType.ATTRELEM, JValueType.MARKER, JValueType.OBJECT } };
		signatures = x;

		description = "Returns the value of the given AttrElem's or Record's (temporary) attribute or component.\n"
				+ "Alternative usage: elem.attr or elem.recordAttr.recComponent.";

		Category[] c = { Category.GRAPH };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		AttributedElement attrElem = null;
		JValueRecord record = null;
		GraphMarker<?> marker = null;

		switch (checkArguments(arguments)) {
		case 0:
			attrElem = arguments[0].toAttributedElement();
			break;
		case 1:
			record = arguments[0].toJValueRecord();
			break;
		case 2:
			attrElem = arguments[0].toAttributedElement();
			marker = (GraphMarker<?>) arguments[1].toGraphMarker();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
		String fieldName = arguments[1].toString();

		if (attrElem != null) {
			if (marker != null) {
				return JValueImpl
						.fromObject(marker.getMark(attrElem), attrElem);
			} else {
				try {
					return JValueImpl.fromObject(
							attrElem.getAttribute(fieldName), attrElem);
				} catch (NoSuchAttributeException e) {
					e.printStackTrace();
					throw new EvaluateException("GetValue failed!", e);
				}
			}
		}
		return record.get(fieldName);
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
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
