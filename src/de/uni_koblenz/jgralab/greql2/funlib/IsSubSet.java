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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Checks if the first given set is a subset of the second given set. That
 * means, all elements from the first set are also elements in the second set.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd>
 * <code>BOOL isSubSet(set1:SET&lt;OBJECT&gt;, set2:SET&lt;OBJECT&gt;)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>set1</code> - potential subset</dd>
 * <dd><code>set2</code> - potential superset</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if all elements in the first given set are also
 * elements in the second given set</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class IsSubSet extends Greql2Function {

	{
		JValueType[][] x = { { JValueType.COLLECTION, JValueType.COLLECTION,
				JValueType.BOOL } };
		signatures = x;

		description = "Returns true iff the first collection is a subset of the second collection.\n"
				+ "That means, all elements from the first collection are also elements in the\n"
				+ "second collection.  Both arguments are converted to sets first.";

		Category[] c = { Category.COLLECTIONS_AND_MAPS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, AbstractGraphMarker<AttributedElement> subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) == -1) {
			throw new WrongFunctionParameterException(this, arguments);
		}

		JValueSet firstSet = arguments[0].toCollection().toJValueSet();
		JValueSet secondSet = arguments[1].toCollection().toJValueSet();
		return JValueBoolean.getValue(firstSet.isSubset(secondSet));

	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		long elems = 1;
		for (Long i : inElements) {
			elems *= i;
		}
		return elems * 2;
	}

	@Override
	public double getSelectivity() {
		return 0.5;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
