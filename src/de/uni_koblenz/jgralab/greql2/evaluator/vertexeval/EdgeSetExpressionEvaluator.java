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

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.EdgeSetExpression;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;

/**
 * Calculates a subset of the datagraph edges
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class EdgeSetExpressionEvaluator extends ElementSetExpressionEvaluator {

	/**
	 * Creates a new ElementSetExpressionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public EdgeSetExpressionEvaluator(EdgeSetExpression vertex,
			GreqlEvaluator eval) {
		super(vertex, eval);
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		Graph datagraph = getDatagraph();
		// create the resulting set
		JValueSet resultSet = new JValueSet();
		Edge currentEdge = datagraph.getFirstEdgeInGraph();
		JValueTypeCollection typeCollection = getTypeCollection();
		while (currentEdge != null) {
			if ((subgraph == null) || (subgraph.isMarked(currentEdge))) {
				AttributedElementClass edgeClass = currentEdge
						.getAttributedElementClass();
				if (typeCollection.acceptsType(edgeClass))
					resultSet.add(new JValue(currentEdge));
			}
			currentEdge = currentEdge.getNextEdgeInGraph();
		}
		return resultSet;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsEdgeSetExpression(this, graphSize);
	}

	@Override
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCardinalityEdgeSetExpression(this, graphSize);
	}

}
