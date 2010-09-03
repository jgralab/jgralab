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

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.VertexSubgraphExpression;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * Evaluates the given vertex subgraph expression. All Vertices and Edges that
 * belong to the generated subgraph are marked with a temporary attribut
 * <code>SubgraphTempAttribute</code>
 */
public class VertexSubgraphExpressionEvaluator extends
		SubgraphExpressionEvaluator {

	public VertexSubgraphExpressionEvaluator(VertexSubgraphExpression vertex,
			GreqlEvaluator eval) {
		super(vertex, eval);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JValue evaluate() throws EvaluateException {
		BooleanGraphMarker subgraphAttr = new BooleanGraphMarker(getDatagraph());
		Vertex currentVertex = getDatagraph().getFirstVertex();
		while (currentVertex != null) {
			JValueTypeCollection typeCollection = getTypeCollection();
			if ((subgraph == null) || (subgraph.isMarked(currentVertex))) {
				AttributedElementClass vertexClass = currentVertex
						.getAttributedElementClass();
				if (typeCollection.acceptsType(vertexClass))
					subgraphAttr.mark(currentVertex);
			}
			currentVertex = currentVertex.getNextVertex();
		}
		// add all edges
		Edge currentEdge = getDatagraph().getFirstEdgeInGraph();
		while (currentEdge != null) {
			if (subgraphAttr.isMarked(currentEdge.getAlpha())
					&& subgraphAttr.isMarked(currentEdge.getOmega()))
				subgraphAttr.mark(currentEdge);
			currentEdge = currentEdge.getNextEdgeInGraph();
		}
		return new JValueImpl(subgraphAttr);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsVertexSubgraphExpression(this, graphSize);
	}

	public GraphSize calculateSubgraphSize(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateVertexSubgraphSize(
				this, graphSize);
	}

}
