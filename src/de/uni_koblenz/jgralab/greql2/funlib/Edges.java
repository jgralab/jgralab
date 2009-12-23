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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;

/**
 * Returns (a part of) the edge sequence of the graph
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>List&lt;EDGE&gt; edges(EDGE start, EDGE end)</code></dd>
 * <code>List&lt;EDGE&gt; edges(EDGE start, EDGE end, tc:TYPECOLLECTION)</code>
 * </dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>start</code> - the first edge of the subsequence of Eseq to return</dd>
 * <dd><code>end</code> - the last rdge of the subsequence of Eseq to return</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the subsequence of Eseq containing all edges between start and end
 * (including both)</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @see Vertices
 * @author ist@uni-koblenz.de
 * 
 */

public class Edges extends AbstractGreql2Function {
	{
		JValueType[][] x = { { JValueType.EDGE, JValueType.EDGE },
				{ JValueType.EDGE, JValueType.EDGE, JValueType.TYPECOLLECTION } };
		signatures = x;

		description = "Returns the global edge sequence from the first to the second given edge.\n"
				+ "The edge types may be restricted by a type collection.";
	}

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		JValueSet edges = new JValueSet();
		Edge start = arguments[0].toEdge();
		Edge end = arguments[1].toEdge();
		Edge current = start;
		switch (checkArguments(arguments)) {
		case 0:
			while (current != null) {
				edges.add(new JValue(current));
				if (current == end) {
					return edges;
				}
				current = current.getNextEdge();
			}
			return edges;
		case 1:
			JValueTypeCollection tc = arguments[2].toJValueTypeCollection();
			while (current != null) {
				if (tc.acceptsType(current.getAttributedElementClass())) {
					edges.add(new JValue(current));
				}
				if (current == end) {
					return edges;
				}
				current = current.getNextEdge();
			}
			return edges;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 1000;
	}

	public double getSelectivity() {
		return 0.2;
	}

	public long getEstimatedCardinality(int inElements) {
		return 100;
	}

}
