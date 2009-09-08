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

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Checks if the first given set is a superset of the second given set. That
 * means, the second given set contains only elements, that are element in the
 * first given set.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd>
 * <code>BOOLEAN isSuperSet(set1:SET&lt;OBJECT&gt;, set2:SET&lt;OBJECT&gt;)</code>
 * </dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>set1</code> - potential superset</dd>
 * <dd><code>set2</code> - potential subset</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if the second given set contains only elements that are
 * element in the first given set.</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class IsSuperSet extends AbstractGreql2Function {
	{
		JValueType[][] x = { { JValueType.COLLECTION, JValueType.COLLECTION } };
		signatures = x;
	}

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) == -1) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}

		JValueSet firstSet = arguments[0].toCollection().toJValueSet();
		JValueSet secondSet = arguments[1].toCollection().toJValueSet();
		return new JValue(firstSet.isSuperset(secondSet));
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		long elems = 1;
		for (Long i : inElements) {
			elems *= i;
		}
		return elems * 2;
	}

	public double getSelectivity() {
		return 0.5;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
