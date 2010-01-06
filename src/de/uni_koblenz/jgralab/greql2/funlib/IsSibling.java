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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Checks if two given vertices are siblings. That means, they have at least one
 * same parent. If a pathsystem is given, this check is performed on two
 * vertices in this pathsystem.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN isSibling(v1:VERTEX, v2:VERTEX)</code></dd>
 * <dd><code>BOOLEAN isSibling(v1:VERTEX, v2:VERTEX, ps:PATHSYSTEM)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>v1</code> - first vertex to check</dd>
 * <dd><code>v2</code> - second vertex to check</dd>
 * <dd><code>ps</code> - optional pathsystem to perform this check for two
 * vertices in this pathsystem</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if there is at least one same parent for the two given
 * vertices. If a pathsystem is specified, both vertices and at least one parent
 * must be in it.</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class IsSibling extends Greql2Function {
	{
		JValueType[][] x = {
				{ JValueType.VERTEX, JValueType.VERTEX, JValueType.BOOLEAN },
				{ JValueType.VERTEX, JValueType.VERTEX, JValueType.PATHSYSTEM,
						JValueType.BOOLEAN } };
		signatures = x;

		description = "Return true, iff the given two vertices are siblings.\n"
				+ "That means, they have at least one same parent. If a pathsystem is\n"
				+ "given, this check is performed on two vertices in this pathsystem.";

		Category[] c = { Category.GRAPH, Category.PATHS_AND_PATHSYSTEMS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		JValuePathSystem pathSystem = null;
		switch (checkArguments(arguments)) {
		case 0:
			break;
		case 1:
			pathSystem = arguments[2].toPathSystem();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
		Vertex firstVertex = arguments[0].toVertex();
		Vertex secondVertex = arguments[1].toVertex();

		firstVertex = arguments[0].toVertex();
		secondVertex = arguments[1].toVertex();

		if (pathSystem != null) {
			return new JValue(pathSystem.isSibling(firstVertex, secondVertex),
					firstVertex);
		}

		// check if the vertices are siblings in the graph
		Edge inc1 = firstVertex.getFirstEdge();
		while (inc1 != null) {
			Edge inc2 = secondVertex.getFirstEdge();
			Vertex firstFather;
			if (inc1.getAlpha() == firstVertex) {
				firstFather = inc1.getOmega();
			} else {
				firstFather = inc1.getAlpha();
			}
			if ((subgraph == null) || (subgraph.isMarked(firstFather))) {
				while (inc2 != null) {
					Vertex secondFather;
					if (inc2.getAlpha() == secondVertex) {
						secondFather = inc2.getOmega();
					} else {
						secondFather = inc2.getAlpha();
					}
					if (firstFather.equals(secondFather)) {
						return new JValue(JValueBoolean.getTrueValue(),
								firstVertex);
					}
					inc2 = inc2.getNextEdge();
				}
			}
			inc1 = inc1.getNextEdge();
		}
		return new JValue(JValueBoolean.getFalseValue(), firstVertex);

	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 20;
	}

	@Override
	public double getSelectivity() {
		return 0.1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 2;
	}

}
