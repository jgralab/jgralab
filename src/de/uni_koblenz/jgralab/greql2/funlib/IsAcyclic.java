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

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Checks if the current graph or subgraph is cycle-free.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN isAcyclic()</code></dd>
 * <dd><code>BOOLEAN isAcyclic(subgraph : SubgraphTempAttribute)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>subgraph</code> - the subgraph to be checked (optional)</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if the current or given graph or subgraph is acyclic</dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class IsAcyclic extends Greql2Function {
	{
		JValueType[][] x = { {}, { JValueType.SUBGRAPHTEMPATTRIBUTE } };
		signatures = x;

		description = "Return true, if the current graph or the given subgraph is cycle-free.\n"
				+ "Also have a look at the function `topologicalSort'.";
	}

	@Override
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

		Greql2Function topoSort = Greql2FunctionLibrary.instance()
				.getGreqlFunction("topologicalSort");
		JValue result = topoSort.evaluate(graph, subgraph, arguments);
		// when topological sort returns a valid list, then it cannot be cyclic
		System.out.println("isAcyclic(topoSort)= " + result);
		System.out.println("valid? " + result.isValid());
		System.out.println("collection? " + result.isCollection());
		return new JValue(result.isValid() && result.isCollection());
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 100;
	}

	@Override
	public double getSelectivity() {
		return 0.1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
