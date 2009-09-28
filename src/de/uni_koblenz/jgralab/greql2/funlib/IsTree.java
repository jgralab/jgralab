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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
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
 * <dd><code>BOOLEAN isTree()</code></dd>
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
public class IsTree extends AbstractGreql2Function {

	{
		JValueType[][] x = { {}, { JValueType.SUBGRAPHTEMPATTRIBUTE } };
		signatures = x;
	}

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			break;
		case 1:
			subgraph = arguments[0].toSubgraphTempAttribute();
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
					return new JValue(JValueBoolean.getFalseValue());
				}
				if (inDegree == 0) {
					foundOneRoot = true;
				}
			}
		} else {
			for (Vertex v : graph.vertices()) {
				int inDegree = v.getDegree(EdgeDirection.IN);
				if ((inDegree > 1) || ((inDegree == 0) && foundOneRoot)) {
					return new JValue(JValueBoolean.getFalseValue());
				}
				if (inDegree == 0) {
					foundOneRoot = true;
				}
			}
		}
		return new JValue(foundOneRoot);
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 5;
	}

	public double getSelectivity() {
		return 0.01;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
