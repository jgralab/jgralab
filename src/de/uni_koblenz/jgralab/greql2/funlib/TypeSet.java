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
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns a set of all types that occure in the given structure.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET&lt;ATTRIBUTEDELEMENTCLASS&gt; TypeSet(c:COLLECTION)</code></dd>
 * <dd><code>SET&lt;ATTRIBUTEDELEMENTCLASS&gt; TypeSet(p:PATH)</code></dd>
 * <dd><code>SET&lt;ATTRIBUTEDELEMENTCLASS&gt; TypeSet(ps:PATHSYSTEM)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>c</code> - collection to return types for</dd>
 * <dd><code>p</code> - path to return types for</dd>
 * <dd><code>ps</code> - path system to return types for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a set of all types that occure in the given structure</dd>
 * </dl>
 * </dd>
 * </dl>
 *
 * @author ist@uni-koblenz.de
 *
 */
public class TypeSet extends AbstractGreql2Function {

	{
		JValueType[][] x = { { JValueType.COLLECTION }, { JValueType.PATH },
				{ JValueType.PATHSYSTEM } };
		signatures = x;
	}

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			JValueSet resultSet = new JValueSet();
			for (JValue v : arguments[0].toCollection()) {
				if (!v.isAttributedElement()) {
					throw new EvaluateException(
							"Cannot calculate the typeSet for a collection that"
									+ " doesn't contain attributed elements.");
				}
				resultSet.add(new JValue(v.toAttributedElement()
						.getAttributedElementClass(), v.toAttributedElement()));
			}
			return resultSet;
		case 1:
			return arguments[0].toPath().types();
		case 2:
			return arguments[0].toPathSystem().types();
		default:
			throw new WrongFunctionParameterException(this, null, arguments);
		}
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 50;
	}

	public double getSelectivity() {
		return 1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 20;
	}

}
