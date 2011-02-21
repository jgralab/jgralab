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
import de.uni_koblenz.jgralab.EdgeDirection;
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
 * Returns all child-vertices of the given vertex as set. If a pathsystem is
 * given, only the child-vertices that are in the pathsystem are returned.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET&lt;VERTEX&gt; children(v:VERTEX)</code></dd>
 * <dd><code>SET&lt;VERTEX&gt; children(v:VERTEX, ps:PATHSYSTEM)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>v</code> - vertex to calculate the child-vertices for</dd>
 * <dd><code>ps</code> - pathsystem to limit the calculation</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a set of vertices that contains all child-vertices of the given vertex.
 * If a pathsystem is specified, only the child-vertices that are contained in
 * the pathsystem are returned.</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class Children extends Greql2Function {

	{
		JValueType[][] x = {
				{ JValueType.VERTEX, JValueType.COLLECTION },
				{ JValueType.VERTEX, JValueType.PATHSYSTEM,
						JValueType.COLLECTION } };
		signatures = x;

		description = "Returns the set of all children of the given vertex.\n"
				+ "They may be restricted by an optional pathsystem.\n"
				+ "Children are those vertices, from which edges point to the given vertex. The\n"
				+ "child vertices are returned as a set.";

		Category[] c = { Category.GRAPH };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		Vertex vertex = null;
		JValuePathSystem pathSystem = null;

		switch (checkArguments(arguments)) {
		case 1:
			pathSystem = arguments[1].toPathSystem();
		case 0:
			vertex = arguments[0].toVertex();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}

		if (pathSystem != null) {
			return pathSystem.children(vertex);
		} else {
			Edge inc = vertex.getFirstIncidence(EdgeDirection.IN);
			Vertex other = null;
			JValueSet resultSet = new JValueSet();
			while (inc != null) {
				other = inc.getThat();
				if ((subgraph == null) || subgraph.isMarked(other)) {
					resultSet.add(new JValueImpl(other));
				}
				inc = inc.getNextIncidence(EdgeDirection.OUT);
			}
			return resultSet;
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		// TODO Auto-generated method stub
		return 5;
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
