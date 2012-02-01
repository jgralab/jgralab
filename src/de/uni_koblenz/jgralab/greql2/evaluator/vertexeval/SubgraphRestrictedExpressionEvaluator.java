package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.greql2.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.Query;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Graph;
import de.uni_koblenz.jgralab.greql2.schema.IsExpressionOnSubgraph;
import de.uni_koblenz.jgralab.greql2.schema.IsSubgraphDefinitionOf;
import de.uni_koblenz.jgralab.greql2.schema.SubgraphDefinition;
import de.uni_koblenz.jgralab.greql2.schema.SubgraphRestrictedExpression;

public class SubgraphRestrictedExpressionEvaluator extends
		VertexEvaluator<SubgraphRestrictedExpression> {

	SubgraphDefinitionEvaluator<?> subgraphDefinitionEval;

	VertexEvaluator<? extends Expression> exprEval;

	public SubgraphRestrictedExpressionEvaluator(
			SubgraphRestrictedExpression vertex, Query query) {
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

		Greql2Graph graph = query.getQueryGraph();

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

	// @Override
	// protected VertexCosts calculateSubtreeEvaluationCosts() {
	// // return
	// //
	// greqlEvaluator.getCostModel().calculateCostsSubgraphRestrictedExpression(this);
	// if (subgraphDefinitionEval == null) {
	// IsSubgraphDefinitionOf isSubgraphDef = vertex
	// .getFirstIsSubgraphDefinitionOfIncidence(EdgeDirection.IN);
	// SubgraphDefinition defVertex = (SubgraphDefinition) isSubgraphDef
	// .getThat();
	// subgraphDefinitionEval = (SubgraphDefinitionEvaluator) vertexEvalMarker
	// .getMark(defVertex);
	// }
	//
	// // take restricted expression
	// if (exprEval == null) {
	// IsExpressionOnSubgraph isExprOn = vertex
	// .getFirstIsExpressionOnSubgraphIncidence(EdgeDirection.IN);
	// Expression expr = (Expression) isExprOn.getThat();
	// exprEval = vertexEvalMarker.getMark(expr);
	// }
	// long ownCosts = 10;
	// long iteratedCosts = ownCosts * getVariableCombinations();
	// long subtree = subgraphDefinitionEval
	// .getCurrentSubtreeEvaluationCosts()
	// + exprEval.getCurrentSubtreeEvaluationCosts() + iteratedCosts;
	//
	// return new VertexCosts(ownCosts, iteratedCosts, subtree);
	// }

}
