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
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;

/**
 * Returns the last vertex (of a type matching the given TypeCollection) in the
 * graph
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>VERTEX lastVertex()</code></dd>
 * <dd><code>VERTEX lastVertex(tc: TYPECOLLECTION)</code></dd>
 * </dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dd>the last vertex (of a type matching tc) in Vseq
 * </dl>
 * </dd> </dl>
 * 
 * @see FirstVertex, LastEdge
 * @author ist@uni-koblenz.de
 * 
 */

public class LastVertex extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.VERTEX },
				{ JValueType.TYPECOLLECTION, JValueType.VERTEX } };
		signatures = x;

		description = "Returns the last vertex (optional restricted by TypeCollection) in the graph.";

		Category[] c = { Category.GRAPH };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, AbstractGraphMarker<?> subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			return new JValueImpl(graph.getLastVertex());
		case 1:
			Vertex current = graph.getLastVertex();
			JValueTypeCollection tc = arguments[2].toJValueTypeCollection();
			while (current != null) {
				if (tc.acceptsType(current.getAttributedElementClass())) {
					return new JValueImpl(current);
				}
				current = current.getPrevVertex();
			}
			return new JValueImpl((Vertex) null);
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 1000;
	}

	@Override
	public double getSelectivity() {
		return 0.2;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 100;
	}

}
