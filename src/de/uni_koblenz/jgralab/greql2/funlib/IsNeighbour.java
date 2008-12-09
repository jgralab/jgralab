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

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Checks if two given vertices are neighbours. That means, there is an edge
 * between these two vertices. If a pathsystem is given, this check is performed
 * on two vertices in this pathsystem.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN isNeighbour(v1:VERTEX, v2:VERTEX)</code></dd>
 * <dd><code>BOOLEAN isNeighbour(v1:VERTEX, v2:VERTEX, ps:PATHSYSTEM)</code></dd>
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
 * <dd><code>true</code> if there is at least one edge between the two given
 * vertices. If a pathsystem is specified, both vertices and at least one edge
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

public class IsNeighbour extends AbstractGreql2Function {
	{
		JValueType[][] x = { { JValueType.VERTEX, JValueType.VERTEX },
				{ JValueType.VERTEX, JValueType.VERTEX, JValueType.PATHSYSTEM } };
		signatures = x;
	}

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
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		Vertex firstVertex = arguments[0].toVertex();
		Vertex secondVertex = arguments[1].toVertex();

		if (pathSystem != null) {
			return new JValue(
					pathSystem.isNeighbour(firstVertex, secondVertex),
					firstVertex);
		}

		Edge inc = firstVertex.getFirstEdge();
		while (inc != null) {
			if ((inc.getAlpha() == secondVertex)
					|| (inc.getOmega() == secondVertex)) {
				return new JValue(JValueBoolean.getTrueValue(), firstVertex);
			}
			inc = inc.getNextEdge();
		}
		return new JValue(JValueBoolean.getFalseValue(), firstVertex);
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 10;
	}

	public double getSelectivity() {
		return 1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 2;
	}

}
