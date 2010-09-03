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
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns the index of the given object in the given List or Tuple.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>INT pos(list:LIST&lt;OBJECT&gt;, element:OBJECT)</code></dd>
 * <dd><code>INT pos(tuple:TUPLE&lt;OBJECT&gt;, element:OBJECT)</code></dd>
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

public class Pos extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.COLLECTION, JValueType.OBJECT,
				JValueType.INT } };
		signatures = x;

		description = "Returns the index of the given object in the given collection.";

		Category[] c = { Category.COLLECTIONS_AND_MAPS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, AbstractGraphMarker<?> subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) == -1) {
			throw new WrongFunctionParameterException(this, arguments);
		}
		JValue object = arguments[1];
		JValueCollection col = arguments[0].toCollection();
		if (col.isJValueList()) {
			JValueList list = col.toJValueList();
			return new JValueImpl(list.indexOf(object));
		}
		if (col.isJValueTuple()) {
			JValueTuple tup = col.toJValueTuple();
			return new JValueImpl(tup.indexOf(object));
		}
		if (col.isJValueSet()) {
			JValueSet set = col.toJValueSet();
			return new JValueImpl(set.indexOf(object));
		}
		throw new EvaluateException(
				"Pos has to be called with a LIST, TUPLE or sorted SET.");
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
