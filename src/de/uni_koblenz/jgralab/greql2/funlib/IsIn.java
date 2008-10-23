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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Checks if a given object is included in a given structure. The object can be
 * anything (for example a vertex or an edge). The structure can be something
 * like a graph, a subgraph or a pathsystem.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN isIn(obj:OBJECT, struct:STRUCTURE)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>obj</code> - object to check</dd>
 * <dd><code>struct</code> - structure to check if the object is included</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if the given object is included in the given
 * structure</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class IsIn implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		try {
			if (arguments.length < 2)
				throw new WrongFunctionParameterException(this, null, arguments);
			JValue value = arguments[0];
			AttributedElement attrElem = null;
			if (arguments[0].canConvert(JValueType.ATTRIBUTEDELEMENT))
				attrElem = value.toAttributedElement();
			if (arguments[1].isCollection()) {
				JValueSet firstSet = arguments[1].toCollection().toJValueSet();
				return new JValue(firstSet.contains(value), attrElem);
			} else {
				if (arguments[0].canConvert(JValueType.ATTRIBUTEDELEMENT)) {
					GraphElement elem = (GraphElement) arguments[0]
							.toAttributedElement();
					if (arguments[1].isPath()) {
						JValuePath path = arguments[1].toPath();
						return new JValue(path.contains(elem), elem);
					}
					if (arguments[1].isPathSystem()) {
						JValuePathSystem system = arguments[1].toPathSystem();
						return new JValue(system.contains(elem), elem);
					}
					// use subgraph
					return new JValue((subgraph == null)
							|| subgraph.isMarked(elem), elem);
				}
				if (arguments[0].isPath()) {
					if (arguments[1].isPathSystem()) {
						JValuePathSystem system = arguments[1].toPathSystem();
						return new JValue(system.containsPath(arguments[0]
								.toPath()));
					}
				}
				return new JValue(JValueBoolean.NULL);
			}
		} catch (JValueInvalidTypeException ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0);
	}

	public double getSelectivity() {
		return 0.1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	public String getExpectedParameters() {
		return "(Set or Path or Pathsystem, Object)";
	}

	@Override
	public boolean isPredicate() {
		return true;
	}

}
