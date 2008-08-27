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

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

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
 * <dd><code>true</code> if there is at least one same parent for the two
 * given vertices. If a pathsystem is specified, both vertices and at least one
 * parent must be in it.</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class IsSibling implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		Vertex firstVertex = null;
		Vertex secondVertex = null;
		JValuePathSystem pathSystem;
		try {
			firstVertex = arguments[0].toVertex();
			secondVertex = arguments[1].toVertex();
			if ((arguments.length > 2) && (arguments[2] != null)) {
				if (arguments[2].isPathSystem()) {
					pathSystem = arguments[2].toPathSystem();
					return new JValue(pathSystem.isSibling(firstVertex,
							secondVertex), firstVertex);
				}
			}
			// check if the vertices are siblings in the graph
			Edge inc1 = firstVertex.getFirstEdge();
			while (inc1 != null) {
				Edge inc2 = secondVertex.getFirstEdge();
				Vertex firstFather;
				if (inc1.getAlpha() == firstVertex)
					firstFather = inc1.getOmega();
				else
					firstFather = inc1.getAlpha();
				if ((subgraph == null) || (subgraph.isMarked(firstFather))) {
					while (inc2 != null) {
						Vertex secondFather;
						if (inc2.getAlpha() == secondVertex)
							secondFather = inc2.getOmega();
						else
							secondFather = inc2.getAlpha();
						if (firstFather.equals(secondFather))
							return new JValue(JValueBoolean.getTrueValue(),
									firstVertex);
						inc2 = inc2.getNextEdge();
					}
				}
				inc1 = inc1.getNextEdge();
			}
			return new JValue(JValueBoolean.getFalseValue(), firstVertex);
		} catch (JValueInvalidTypeException ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}

	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 20;
	}

	public double getSelectivity() {
		return 0.1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 2;
	}

	public String getExpectedParameters() {
		return "(Vertex, Vertex [, PathSystem or Subgraph])";
	}

	@Override
	public boolean isPredicate() {
		return true;
	}
}
