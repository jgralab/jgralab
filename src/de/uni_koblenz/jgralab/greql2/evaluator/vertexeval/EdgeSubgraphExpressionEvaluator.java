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

import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.greql2.evaluator.*;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.*;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.*;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;


/**
 * Evaluates the given edgesubgraph expression. All Vertices and Edges
 * that belong to the generated subgraph are marked with a temporary
 * attribut <code>SubgraphTempAttribute</code>
 */
public class EdgeSubgraphExpressionEvaluator extends
		SubgraphExpressionEvaluator {
	
	public EdgeSubgraphExpressionEvaluator(EdgeSubgraphExpression vertex,
			GreqlEvaluator eval) {
		super(vertex, eval);
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		BooleanGraphMarker subgraphAttr = new BooleanGraphMarker(getDatagraph());
		Edge currentEdge = getDatagraph().getFirstEdgeInGraph();
		JValueTypeCollection typeCollection = getTypeCollection();
		while (currentEdge != null) {
			if ((subgraph==null) || (subgraph.isMarked(currentEdge))) {
				AttributedElementClass edgeClass = currentEdge
						.getAttributedElementClass();
				if (typeCollection.acceptsType(edgeClass))
					subgraphAttr.mark(currentEdge);
			}
			currentEdge = currentEdge.getNextEdgeInGraph();
		}
		// add all vertices
		Vertex currentVertex = getDatagraph().getFirstVertex();
		while (currentVertex != null){
		//	System.out.println("Current vertex is: " + currentVertex);
			Edge inc = currentVertex.getFirstEdge();
			while (inc != null) {
			//	System.out.println("Edge is: " + inc);
				if (subgraphAttr.isMarked(inc)) {
					subgraphAttr.mark(currentVertex);
			//		System.out.println("Marking vertex: " + currentVertex);
					break;
				}	
				inc = inc.getNextEdge();
			}
			currentVertex = currentVertex.getNextVertex();
		}
		return new JValue(subgraphAttr);
	}
	
	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateCostsEdgeSubgraphExpression(this, graphSize);
	}

	public GraphSize calculateSubgraphSize(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateEdgeSubgraphSize(this, graphSize);
	}

}
