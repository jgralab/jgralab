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
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;


/**
 * Returns all inner nodes of the given pathsystem as set. Inner nodes are all vertices besides leaves of the pathsystem. That means, all vertices, that are not an end of a path.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET&lt;VERTEX&gt; innerNodes(ps:PATHSYSTEM)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl><dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>ps</code> - pathsystem to calculate the inner nodes for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a set of vertices that contains all inner nodes of the given pathsystem</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * @see Leaves
 * @author ist@uni-koblenz.de
 * 
 */

public class InnerNodes implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph, JValue[] arguments) throws EvaluateException {
		JValuePathSystem pathSystem;
		try {
			pathSystem = arguments[0].toPathSystem();
		} catch (Exception ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		return pathSystem.innerNodes();
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getSelectivity() {
		return 1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	public String getExpectedParameters() {
		return "(PathSystem)";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}

}
