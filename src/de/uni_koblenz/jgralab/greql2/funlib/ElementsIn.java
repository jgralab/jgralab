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
import java.util.Iterator;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTable;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Extracts the set of elements that are part of the given structure
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET elementsIn(structure:COLLECTION)</code></dd>
 * <dd><code>SET elementsIn(structure:PATH)</code></dd>
 * <dd><code>SET elementsIn(structure:PATHSYSTEM)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>structure</code> - the structure which should be flattened</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>The set of elements that are part of <code>structure</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class ElementsIn extends AbstractGreql2Function {

	{
		JValueType[][] x = { { JValueType.COLLECTION }, { JValueType.PATH },
				{ JValueType.PATHSYSTEM } };
		signatures = x;
	}

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		JValueSet set = new JValueSet();
		JValue structure = arguments[0];
		switch (checkArguments(arguments)) {
		case 0:
			JValueCollection col = structure.toCollection();
			if (col.isJValueTable()) {
				JValueTable tab = col.toJValueTable();
				Iterator<JValue> iter = tab.iterator();
				while (iter.hasNext()) {
					JValue next = iter.next();
					if (next.isCollection() || next.isPath()
							|| next.isPathSystem()) {
						JValue[] params = { next };
						set
								.addAll((JValueSet) evaluate(graph, subgraph,
										params));
					} else {
						set.add(next);
					}
				}
			} else {
				set = col.toJValueSet();
			}
			break;
		case 1:
			JValuePath path = arguments[0].toPath();
			set.addAll(path.edgeTraceAsJValue());
			set.addAll(path.nodeTraceAsJValue());
			break;
		case 2:
			JValuePathSystem pathSystem = arguments[0].toPathSystem();
			set.addAll(pathSystem.edges());
			set.addAll(pathSystem.nodes());
			break;
		default:
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		return set;
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0) * 2;
	}

	public double getSelectivity() {
		return 0.1;
	}

	public long getEstimatedCardinality(int inElements) {
		return inElements / 10;
	}

}
