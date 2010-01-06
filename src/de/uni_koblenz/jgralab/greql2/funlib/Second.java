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

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns the second element of the given list, set or tuple.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>OBJECT second(list:LIST&lt;OBJECT&gt;)</code></dd>
 * <dd><code>OBJECT second(list:SET&lt;OBJECT&gt;)</code></dd>
 * <dd><code>OBJECT second(tuple:TUPLE&lt;OBJECT&gt;)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>list</code> - list to return second element for</dd>
 * <dd><code>set</code> - sorted set to return second element for</dd>
 * <dd><code>tuple</code> - tuple to return second element for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the second element of the given list, set or tuple</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class Second extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.COLLECTION, JValueType.OBJECT } };
		signatures = x;

		description = "Return the second element of the given collection.\n"
				+ "The collection will be converted to a list before.";

		Category[] c = { Category.COLLECTIONS_AND_MAPS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) == -1) {
			throw new WrongFunctionParameterException(this, arguments);
		}
		JValueCollection col = arguments[0].toCollection();
		if (col.size() < 2) {
			throw new EvaluateException(
					"The given collection fewer than 2 elements.");
		}

		return col.toJValueList().get(1);
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
