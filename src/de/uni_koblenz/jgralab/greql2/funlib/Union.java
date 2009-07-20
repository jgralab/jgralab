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
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns the union of two given sets or maps. That means a set, that contains
 * all elements that are in the first given set or in the second given set.
 * Elements that are in both sets are also included.
 * 
 * In case of maps be aware of the fact that the keys have to be disjoint.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd>
 * <code>SET&lt;OBJECT&gt; union(s1:SET&lt;OBJECT&gt;, s2:SET&lt;OBJECT&gt;)
 * </code><br/>
 * <code>SET&lt;OBJECT&gt; union(s:SET&lt;OBJECT&gt;)
 * </code><br/>
 * <code>MAP&lt;OBJECT,OBJECT&gt; union(s1:MAP&lt;OBJECT,OBJECT&gt;, s2:MAP&lt;OBJECT,OBJECT&gt;)</code>
 * </dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>s1</code> - first set or map</dd>
 * <dd><code>s2</code> - second set or map</dd>
 * <dd><code>s</code> - a set of sets</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the union of the two given sets or maps. If only one set is given, it has
 * to be a set of sets and the union of those nested sets are computed.</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @see Difference
 * @see SymDifference
 * @see Intersection
 * @see MergeMaps
 * @author ist@uni-koblenz.de
 * 
 */
public class Union extends AbstractGreql2Function {
	{
		JValueType[][] x = { { JValueType.COLLECTION, JValueType.COLLECTION },
				{ JValueType.MAP, JValueType.MAP }, { JValueType.COLLECTION } };
		signatures = x;
	}

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			JValueSet firstSet = arguments[0].toCollection().toJValueSet();
			JValueSet secondSet = arguments[1].toCollection().toJValueSet();
			return firstSet.union(secondSet);
		case 1:
			JValueMap firstMap = arguments[0].toJValueMap();
			JValueMap secondMap = arguments[1].toJValueMap();
			return firstMap.union(secondMap);
		case 2:
			JValueSet set = arguments[0].toCollection().toJValueSet();
			JValueSet result = new JValueSet();
			for (JValue jv : set) {
				if (!(jv instanceof JValueCollection)) {
					throw new WrongFunctionParameterException(this, null,
							arguments);
				}
				for (JValue jv2 : jv.toCollection().toJValueSet()) {
					result.add(jv2);
				}
			}
			return result;
		default:
			throw new WrongFunctionParameterException(this, null, arguments);
		}
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		long elems = 0;
		for (Long i : inElements) {
			elems += i;
		}
		return elems * 2;
	}

	public double getSelectivity() {
		return 1;
	}

	public long getEstimatedCardinality(int inElements) {
		return inElements;
	}

}
