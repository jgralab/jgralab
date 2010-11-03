/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */

package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;
import java.util.Iterator;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSlice;
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

public class Elements extends Greql2Function {

	{
		JValueType[][] x = { { JValueType.COLLECTION, JValueType.COLLECTION },
				{ JValueType.PATH, JValueType.COLLECTION },
				{ JValueType.PATHSYSTEM, JValueType.COLLECTION },
				{ JValueType.SLICE, JValueType.COLLECTION } };
		signatures = x;

		description = "Returns the set of all elements of the given structure.";

		Category[] c = { Category.COLLECTIONS_AND_MAPS,
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
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
		case 3:
			JValueSlice slice = arguments[0].toSlice();
			set.addAll(slice.edges());
			set.addAll(slice.nodes());
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
		return set;
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0) * 2;
	}

	@Override
	public double getSelectivity() {
		return 0.1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return inElements / 10;
	}

}
