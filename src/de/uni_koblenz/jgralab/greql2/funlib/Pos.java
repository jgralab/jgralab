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
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

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

public class Pos extends AbstractGreql2Function {
	{
		JValueType[][] x = { { JValueType.COLLECTION, JValueType.OBJECT } };
		signatures = x;
	}

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) == -1) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
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
		throw new EvaluateException(
				"Pos has to be called with a LIST or TUPLE.");
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
