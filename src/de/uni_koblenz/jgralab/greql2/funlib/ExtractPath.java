/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns a path from the root of the given pathsystem to the given vertex. If
 * the given vertex is contained more then once, the first occurrence will be
 * used. If no vertex is given, the paths from the root to all leaves are
 * returned as set. If an integer is given instead of a vertex, all paths are
 * returned that have the length of this integer.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>PATH extractPath(ps:PATHSYSTEM, v:VERTEX)</code></dd>
 * <dd><code>SET&lt;PATH&gt; extractPath(ps:PATHSYSTEM)</code></dd>
 * <dd><code>SET&lt;PATH&gt; extractPath(ps:PATHSYSTEM, length:INT)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>ps</code> - pathsystem to extract the path(s) from</dd>
 * <dd><code>length</code> - length of the paths to be returned</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a path from the root of the given pathsystem to the gives vertex
 * <dd>the paths from the root to all leaves, if no vertex is given
 * <dd>all paths of given length, if an integer is given instead of a vertex
 * <dd><code>Null</code> if the given vertex is not in the pathsystem or one of
 * the parameters is <code>Null</code>.
 * </dl>
 * </dd>
 * </dl>
 * 
 * @see Leaves
 * @author ist@uni-koblenz.de
 * 
 */

public class ExtractPath extends Greql2Function {

	{
		JValueType[][] x = {
				{ JValueType.PATHSYSTEM, JValueType.COLLECTION },
				{ JValueType.PATHSYSTEM, JValueType.VERTEX, JValueType.PATH },
				{ JValueType.PATHSYSTEM, JValueType.INT, JValueType.COLLECTION } };
		signatures = x;

		description = "Returns a path from the given pathsystem's root to the given vertex.\n"
				+ "If the given vertex is contained more then once, the first occurrence will be\n"
				+ "used. If no vertex is given, the paths from the root to all leaves are returned\n"
				+ "as set. If an integer is given instead of a vertex, all paths are returned that\n"
				+ "have the length of this integer, again as set of paths.";

		Category[] c = { Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		Vertex vertex = null;
		Integer length = null;
		switch (checkArguments(arguments)) {
		case 0:
			break;
		case 1:
			vertex = arguments[1].toVertex();
			break;
		case 2:
			length = arguments[1].toInteger();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}

		JValuePathSystem pathSystem = arguments[0].toPathSystem();

		if (vertex != null) {
			return pathSystem.extractPath(vertex);
		}
		if (length != null) {
			JValueSet paths = pathSystem.extractPaths(length);
			return paths;
		}
		return pathSystem.extractPaths();
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 50;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 2;
	}

}
