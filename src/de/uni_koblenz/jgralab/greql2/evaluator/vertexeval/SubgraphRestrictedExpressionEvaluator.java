/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.greql2.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsExpressionOnSubgraph;
import de.uni_koblenz.jgralab.greql2.schema.IsSubgraphDefinitionOf;
import de.uni_koblenz.jgralab.greql2.schema.SubgraphDefinition;
import de.uni_koblenz.jgralab.greql2.schema.SubgraphRestrictedExpression;

public class SubgraphRestrictedExpressionEvaluator extends
		VertexEvaluator<SubgraphRestrictedExpression> {

	SubgraphDefinitionEvaluator<?> subgraphDefinitionEval;

	VertexEvaluator<? extends Expression> exprEval;

	public SubgraphRestrictedExpressionEvaluator(
			SubgraphRestrictedExpression vertex, QueryImpl query) {
		super(vertex, query);
	}

	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		// take traversal context for subgraph
		if (subgraphDefinitionEval == null) {
			IsSubgraphDefinitionOf isSubgraphDef = vertex
					.getFirstIsSubgraphDefinitionOfIncidence(EdgeDirection.IN);
			SubgraphDefinition defVertex = (SubgraphDefinition) isSubgraphDef
					.getThat();
			subgraphDefinitionEval = (SubgraphDefinitionEvaluator<?>) query
					.getVertexEvaluator(defVertex);
		}
		TraversalContext subgraph = (TraversalContext) subgraphDefinitionEval
				.getResult(evaluator);

		// take restricted expression
		if (exprEval == null) {
			IsExpressionOnSubgraph isExprOn = vertex
					.getFirstIsExpressionOnSubgraphIncidence(EdgeDirection.IN);
			Expression expr = (Expression) isExprOn.getThat();
			exprEval = query.getVertexEvaluator(expr);
		}

		Graph graph = evaluator.getDataGraph();

		// set traversal context
		TraversalContext oldTraversalContext = graph.getTraversalContext();
		graph.setTraversalContext(subgraph);

		// evaluate restricted expression with traversal context
		Object result = exprEval.getResult(evaluator);
		evaluator.setLocalEvaluationResult(vertex, result);

		// release traversal context
		graph.setTraversalContext(oldTraversalContext);
		return result;
	}

	@Override
	protected VertexCosts calculateSubtreeEvaluationCosts() {
		// return
		// greqlEvaluator.getCostModel().calculateCostsSubgraphRestrictedExpression(this);
		if (subgraphDefinitionEval == null) {
			IsSubgraphDefinitionOf isSubgraphDef = vertex
					.getFirstIsSubgraphDefinitionOfIncidence(EdgeDirection.IN);
			SubgraphDefinition defVertex = (SubgraphDefinition) isSubgraphDef
					.getThat();
			subgraphDefinitionEval = (SubgraphDefinitionEvaluator<?>) query
					.getVertexEvaluator(defVertex);
		}

		// take restricted expression
		if (exprEval == null) {
			IsExpressionOnSubgraph isExprOn = vertex
					.getFirstIsExpressionOnSubgraphIncidence(EdgeDirection.IN);
			Expression expr = (Expression) isExprOn.getThat();
			exprEval = query.getVertexEvaluator(expr);
		}
		long ownCosts = 10;
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtree = subgraphDefinitionEval
				.getCurrentSubtreeEvaluationCosts()
				+ exprEval.getCurrentSubtreeEvaluationCosts() + iteratedCosts;

		return new VertexCosts(ownCosts, iteratedCosts, subtree);
	}

}
