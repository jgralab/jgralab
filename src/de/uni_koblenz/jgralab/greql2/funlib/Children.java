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
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;

/**
 * Returns all child-vertices of the given vertex as set. If a pathsystem is given, only the child-vertices that are in the pathsystem are returned.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET&lt;VERTEX&gt; children(v:VERTEX)</code></dd>
 * <dd><code>SET&lt;VERTEX&gt; children(v:VERTEX, ps:PATHSYSTEM)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl><dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>v</code> - vertex to calculate the child-vertices for</dd>
 * <dd><code>ps</code> - pathsystem to limit the calculation</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a set of vertices that contains all child-vertices of the given vertex. If a pathsystem is specified, only the child-vertices that are contained in the pathsystem are returned.</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * @author ist@uni-koblenz.de
 * 
 */

public class Children implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph, JValue[] arguments) throws EvaluateException {
		Vertex vertex;
		JValuePathSystem pathSystem;
		try {
			vertex = arguments[0].toVertex();
			if ( (arguments.length > 1) && (arguments[1] != null) && (arguments[1].isPathSystem()) ) {
				pathSystem = arguments[1].toPathSystem();
				return pathSystem.children(vertex);
			}
			Edge inc = vertex.getFirstEdge(EdgeDirection.IN);
			Vertex other = null;
			JValueSet resultSet = new JValueSet();
			while (inc != null) {
				other = inc.getThat();
				if ((subgraph==null) || (subgraph.isMarked(other)))
					resultSet.add(new JValue(other));
				inc = inc.getNextEdge(EdgeDirection.OUT);
			}
			return resultSet;
		} catch (Exception ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getSelectivity() {
		return 1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 2;
	}

	public String getExpectedParameters() {
		return "(Vertex [,PathSystem])";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}

}
