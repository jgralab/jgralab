/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns all inner nodes of the given pathsystem as set. Inner nodes are all
 * vertices besides leaves of the pathsystem. That means, all vertices, that are
 * not an end of a path.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET&lt;VERTEX&gt; innerNodes(ps:PATHSYSTEM)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
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
 * 
 * @see Leaves
 * @author ist@uni-koblenz.de
 * 
 */

public class InnerNodes extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.PATHSYSTEM, JValueType.COLLECTION },
				{ JValueType.SLICE, JValueType.COLLECTION } };
		signatures = x;

		description = "Returns all inner nodes of the given pathsystem or slice as set.\n"
				+ "Inner nodes are all vertices besides leaves of the pathsystem or slice. That\n"
				+ "means, all vertices, that are neither the end nor the root of a path.";

		Category[] c = { Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, AbstractGraphMarker<?> subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			return arguments[0].toPathSystem().innerNodes();
		case 1:
			return arguments[0].toSlice().innerNodes();
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 5;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
