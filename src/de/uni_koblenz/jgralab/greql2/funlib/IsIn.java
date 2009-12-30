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
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Checks if a given object is included in a given structure. The object can be
 * anything (for example a vertex or an edge). The structure can be something
 * like a graph, a subgraph or a pathsystem.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN isIn(obj:OBJECT, struct:STRUCTURE)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>obj</code> - object to check</dd>
 * <dd><code>struct</code> - structure to check if the object is included</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if the given object is included in the given structure</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class IsIn extends Greql2Function {
	{
		JValueType[][] x = {
				{ JValueType.OBJECT, JValueType.COLLECTION },
				{ JValueType.ATTRIBUTEDELEMENT, JValueType.PATH },
				{ JValueType.ATTRIBUTEDELEMENT, JValueType.PATHSYSTEM },
				{ JValueType.PATH, JValueType.PATHSYSTEM },
				{ JValueType.ATTRIBUTEDELEMENT,
						JValueType.SUBGRAPHTEMPATTRIBUTE },
				{ JValueType.ATTRIBUTEDELEMENT } };
		signatures = x;

		description = "Return true, iff the given element is part of the given structure.\n"
				+ "If only an attributed element is given, then check if that is\n"
				+ "contained in the graph the query is evaluated on.";
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			return new JValue(arguments[1].toCollection().toJValueSet()
					.contains(arguments[0]));
		case 1:
			return new JValue(arguments[1].toPath().contains(
					(GraphElement) arguments[0].toAttributedElement()));
		case 2:
			return new JValue(arguments[1].toPathSystem().contains(
					(GraphElement) arguments[0].toAttributedElement()));
		case 3:
			return new JValue(arguments[1].toPathSystem().containsPath(
					arguments[0].toPath()));
		case 4:
			return new JValue(arguments[1].toSubgraphTempAttribute().isMarked(
					arguments[0].toAttributedElement()));
		case 5:
			if (subgraph == null) {
				throw new EvaluateException(
						"There was no subgraph when evaluating IsIn.  When there's only one "
								+ "arg to IsIn, the subgraph has to be bound in a surrounding FWR.");
			}
			AttributedElement ae = arguments[0].toAttributedElement();
			return new JValue(subgraph.isMarked(ae));
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0);
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
