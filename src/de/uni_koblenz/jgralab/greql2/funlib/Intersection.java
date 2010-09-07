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
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns the intersection of two given sets. That means, a set, whose elements
 * are elements in the first given set and in the second given set.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd>
 * <code>SET&lt;OBJECT&gt; intersection(set1:SET&lt;OBJECT&gt;, set2:SET&lt;OBJECT&gt;)</code>
 * </dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>set1</code> - first set to compute intersection for</dd>
 * <dd><code>set2</code> - second set to compute intersection for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a set whose elements are elements in the first given set and in the
 * second given set.</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @see Difference
 * @see SymDifference
 * @see Union
 * @author ist@uni-koblenz.de
 * 
 */

public class Intersection extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.COLLECTION, JValueType.COLLECTION,
				JValueType.COLLECTION } };
		signatures = x;

		description = "Returns the intersection of two given collections.\n"
				+ "Both collections are converted to sets first.";

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
		return firstSet.intersection(secondSet);
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		long sum = 0;
		for (Long i : inElements) {
			sum += i;
		}
		return sum * 2;
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
