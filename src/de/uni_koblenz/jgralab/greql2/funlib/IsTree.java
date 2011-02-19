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
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Checks if the current graph or subgraph is a tree. That means, the graph is
 * acyclic, has exactly one vertex without incoming edges and all other vertices
 * have exactly one incoming edge.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOL isTree()</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if the given graph is a tree</dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class IsTree extends Greql2Function {

	{
		JValueType[][] x = { { JValueType.BOOL },
				{ JValueType.MARKER, JValueType.BOOL } };
		signatures = x;

		description = "Returns true iff the current graph or the given subgraph is a tree.\n"
				+ "That means, the graph is acyclic, has exactly one vertex without\n"
				+ "incoming edges and all other vertices have exactly one incoming edge.";

		Category[] c = { Category.GRAPH };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			break;
		case 1:
			subgraph = arguments[0].toGraphMarker();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
		boolean foundOneRoot = false;

		if (subgraph != null) {
			for (AttributedElement ae : subgraph.getMarkedElements()) {
				if (!(ae instanceof Vertex)) {
					continue;
				}
				Vertex v = (Vertex) ae;
				int inDegree = v.getDegree(EdgeDirection.IN);
				if ((inDegree > 1) || ((inDegree == 0) && foundOneRoot)) {
					return JValueBoolean
							.getValue(JValueBoolean.getFalseValue());
				}
				if (inDegree == 0) {
					foundOneRoot = true;
				}
			}
		} else {
			for (Vertex v : graph.vertices()) {
				int inDegree = v.getDegree(EdgeDirection.IN);
				if ((inDegree > 1) || ((inDegree == 0) && foundOneRoot)) {
					return JValueBoolean
							.getValue(JValueBoolean.getFalseValue());
				}
				if (inDegree == 0) {
					foundOneRoot = true;
				}
			}
		}
		return JValueBoolean.getValue(foundOneRoot);
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 5;
	}

	@Override
	public double getSelectivity() {
		return 0.01;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
