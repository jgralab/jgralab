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
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns a set of edges which are connected to the given vertex and which are
 * part of the given structure. If no structure is given, the graph to which the
 * vertex belongs to, is used as structure.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET&lt;EDGE&gt; edgesConnected(v:Vertex)</code></dd>
 * <dd><code>SET&lt;EDGE&gt; edgesConnected(v:Vertex, ps:PATH)</code></dd>
 * <dd><code>SET&lt;EDGE&gt; edgesConnected(v:Vertex, ps:PATHSYSTEM)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>v</code> - vertex to calculate the connected edges for</dd>
 * <dd><code>p</code> - path to limit scope to</dd>
 * <dd><code>ps</code> - pathsystem to limit scope to</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a set of edges which are connected to the given vertex</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 *
 * @see Degree
 * @see EdgesFrom
 * @see EdgesTo
 * @author ist@uni-koblenz.de
 *
 */

public class EdgesConnected extends AbstractGreql2Function {
	{
		JValueType[][] x = { { JValueType.VERTEX },
				{ JValueType.VERTEX, JValueType.PATH },
				{ JValueType.VERTEX, JValueType.PATHSYSTEM } };
		signatures = x;
	}

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		JValuePath path = null;
		JValuePathSystem pathSystem = null;
		switch (checkArguments(arguments)) {
		case 0:
			break;
		case 1:
			path = arguments[1].toPath();
			break;
		case 2:
			pathSystem = arguments[1].toPathSystem();
			break;
		default:
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		Vertex vertex = arguments[0].toVertex();

		if (path != null) {
			return path.edgesConnected(vertex);
		}
		if (pathSystem != null) {
			return pathSystem.edgesConnected(vertex);
		}

		JValueSet resultSet = new JValueSet();
		Edge inc = vertex.getFirstEdge();
		while (inc != null) {
			if ((subgraph == null) || (subgraph.isMarked(inc))) {
				resultSet.add(new JValue(inc));
			}
			inc = inc.getNextEdge();
		}
		return resultSet;
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 10;
	}

	public double getSelectivity() {
		return 1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 4;
	}

}
