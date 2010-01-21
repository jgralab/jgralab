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
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns the weight of the given pathsystem. The weight of a pathsystem is its
 * amount of vertices. If vertices occur multiply, they are counted multiply as
 * well.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>INT weight(ps:PATHSYSTEM)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>ps</code> - pathsystem to calculate the weight for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the weight of the given pathsystem</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class Weight extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.PATHSYSTEM, JValueType.INT } };
		signatures = x;

		description = "Returns the weight of the given pathsystem.\n"
				+ "The weight of a pathsystem is its amount of vertices. If vertices occur\n"
				+ "multiple times, they are counted multiple times as well.";

		Category[] c = { Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) == -1) {
			throw new WrongFunctionParameterException(this, arguments);
		}

		JValuePathSystem pathSystem = arguments[0].toPathSystem();
		return new JValue(pathSystem.weight());
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 20;
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
