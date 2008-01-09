/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

/**
 * Returns the number of outgoing edges, which are connected to the given vertex
 * and which are part of the given structure. If no structure is given, the
 * graph to which the vertex belongs to is used as structure.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>INTEGER outDegree(v:Vertex)</code></dd>
 * <dd><code>INTEGER outDegree(v:Vertex, ps:PATH)</code></dd>
 * <dd><code>INTEGER outDegree(v:Vertex, ps:PATHSYSTEM)</code></dd>
 * <dd><code>INTEGER outDegree(v:Vertex, ps:PATH, tc:TYPECOLLECTION)</code></dd>
 * <dd><code>INTEGER outDegree(v:Vertex, ps:PATHSYSTEM, tc:TYPECOLLECTION)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>v</code> - vertex to calculate the outdegree for</dd>
 * <dd><code>p</code> - path to limit scope to</dd>
 * <dd><code>ps</code> - pathsystem to limit scope to</dd>
 * <dd><code>tc</code> - typecollection to limit types that are taken into
 * account</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the indegree of the given vertex</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @see Degree
 * @see InDegree
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class OutDegree implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		try {
			JValueTypeCollection typeCol = null;
			Vertex vertex = arguments[0].toVertex();
			if ((arguments.length > 1) && (arguments[1] != null)) {
				if (arguments[1].isJValueTypeCollection()) {
					typeCol = arguments[1].toJValueTypeCollection();
				} else {
					if ((arguments.length > 2) && (arguments[2] != null)
							&& (arguments[2].isJValueTypeCollection())) {
						typeCol = arguments[2].toJValueTypeCollection();
					}
					if (arguments[1].isPathSystem()) {
						return new JValue(arguments[1].toPathSystem().degree(
								vertex, false, typeCol));
					} else if (arguments[1].isPath()) {
						return new JValue(arguments[1].toPath().degree(vertex,
								false));
					}
				}
			}
			Edge inc = vertex.getFirstEdge(EdgeDirection.OUT);
			int count = 0;
			while (inc != null) {
				if (((subgraph == null) || (subgraph.isMarked(inc)))
						&& ((typeCol == null) || (typeCol.acceptsType(inc
								.getAttributedElementClass()))))
					count++;
				inc = inc.getNextEdge(EdgeDirection.OUT);
			}
			return new JValue(count);

		} catch (Exception ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
	}

	public int getEstimatedCosts(ArrayList<Integer> inElements) {
		return 10;
	}

	public double getSelectivity() {
		return 1;
	}

	public int getEstimatedCardinality(int inElements) {
		return 2;
	}

	public String getExpectedParameters() {
		return "(Vertex, PathSystem or Path or [Graph])";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}
}
