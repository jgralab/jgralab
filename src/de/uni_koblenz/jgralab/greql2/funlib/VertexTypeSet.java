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

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;

import java.util.ArrayList;
import java.util.Iterator;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;

/**
 * Returns a set of all vertex-types that occure in the given structure.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET&lt;ATTRIBUTEDELEMENTCLASS&gt; vertexTypeSet(c:COLLECTION)</code></dd>
 * <dd><code>SET&lt;ATTRIBUTEDELEMENTCLASS&gt; vertexTypeSet(p:PATH)</code></dd>
 * <dd><code>SET&lt;ATTRIBUTEDELEMENTCLASS&gt; vertexTypeSet(ps:PATHSYSTEM)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>c</code> - collection to return vertex-types for</dd>
 * <dd><code>p</code> - path to return vertex-types for</dd>
 * <dd><code>ps</code> - path system to return vertex-types for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a set of all types that occure in the given structure</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

/*
 * returns a set of the typenames of the vertices in the given structure
 * 
 * @param structure a JValueCollection, Path or Pathsystem @return a JValueSet,
 * which contains all types that exist in the given structure
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class VertexTypeSet implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		try {
			if (arguments[0].isCollection()) {
				JValueSet resultSet = new JValueSet();
				JValueCollection collection = arguments[0].toCollection();
				Iterator<JValue> iter = collection.iterator();
				while (iter.hasNext()) {
					JValue value = iter.next();
					GraphElement elem;
					if (value.isVertex()) {
						elem = value.toVertex();
						resultSet.add(new JValue(elem
								.getAttributedElementClass(), elem));
					}
				}
				return resultSet;
			}
			if (arguments[0].isPathSystem()) {
				return arguments[0].toPathSystem().vertexTypes();
			}
			if (arguments[0].isPath()) {
				return arguments[0].toPath().vertexTypes();
			}
			return null;

		} catch (Exception ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 30;
	}

	public double getSelectivity() {
		return 1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 10;
	}

	public String getExpectedParameters() {
		return "(JValueCollection or PathSystem or Path)";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}
}
