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
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns all siblings of the given vertex as set. If a pathsystem is given,
 * all siblings that are in the pathsystem are returned. Two ore more vertices
 * are siblings, if they have at least one same parent.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET&lt;VERTEX&gt; siblings(v:VERTEX)</code></dd>
 * <dd><code>SET&lt;VERTEX&gt; siblings(v:VERTEX, ps:PATHSYSTEM)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>v</code> - vertex to calculate siblings for</dd>
 * <dd><code>ps</code> - pathsystem to limit the calculation</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a set of vertices that contains all siblings of the given vertex. If a
 * pathsystem is specified, only siblings that are contained in the pathsystem
 * are returned.</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class Siblings extends Greql2Function {

	{
		JValueType[][] x = {
				{ JValueType.VERTEX, JValueType.COLLECTION },
				{ JValueType.VERTEX, JValueType.PATHSYSTEM,
						JValueType.COLLECTION } };
		signatures = x;

		description = "Returns the set of all siblings of the given vertex.\n"
				+ "If a pathsystem is given, all siblings that are in the pathsystem are\n"
				+ "returned.  Two or more vertices are siblings, if they have at least one\n"
				+ "same parent.";

		Category[] c = { Category.GRAPH,
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		JValuePathSystem pathSystem = null;
		switch (checkArguments(arguments)) {
		case 0:
			break;
		case 1:
			pathSystem = arguments[1].toPathSystem();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}

		Vertex vertex = arguments[0].toVertex();

		if (pathSystem != null) {
			return pathSystem.siblings(vertex);
		}

		Edge inc1 = vertex.getFirstIncidence();
		JValueSet returnSet = new JValueSet();
		while (inc1 != null) {
			Vertex father = inc1.getThat();
			Edge inc2 = father.getFirstIncidence();
			while (inc2 != null) {
				Vertex anotherVertex = inc2.getThat();
				if (anotherVertex != vertex) {
					returnSet.add(new JValueImpl(anotherVertex, vertex));
				}
				inc2 = inc2.getNextIncidence();
			}
			inc1 = inc1.getNextIncidence();
		}
		return returnSet;
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 10;
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
