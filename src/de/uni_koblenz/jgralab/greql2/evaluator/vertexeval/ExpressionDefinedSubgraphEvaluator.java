package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ExpressionDefinedSubgraph;
import de.uni_koblenz.jgralab.greql2.schema.IsSubgraphDefiningExpression;

public class ExpressionDefinedSubgraphEvaluator extends
		SubgraphDefinitionEvaluator {

	VertexEvaluator subgraphDefExprEvaluator = null;

	// ExpressionDefinedSubgraph vertex;

	public ExpressionDefinedSubgraphEvaluator(ExpressionDefinedSubgraph vertex,
			GreqlEvaluator eval) {
		super(vertex, eval);
		// this.vertex = vertex;
	}

	@Override
	public Object evaluate(Graph graph) {
		if (subgraphDefExprEvaluator == null) {
			ExpressionDefinedSubgraph exprDefinedSubgraph = (ExpressionDefinedSubgraph) vertex;
			IsSubgraphDefiningExpression isSubgraphDefiningExpression = exprDefinedSubgraph
					.getFirstIsSubgraphDefiningExpressionIncidence(EdgeDirection.IN);
			Expression subgraphDefExpr = (Expression) isSubgraphDefiningExpression
					.getThat();
			subgraphDefExprEvaluator = vertexEvalMarker
					.getMark(subgraphDefExpr);
		}
		return subgraphDefExprEvaluator.getResult(graph);
	}

	@Override
	protected VertexCosts calculateSubtreeEvaluationCosts() {
		ExpressionDefinedSubgraph exprDefinedSubgraph = (ExpressionDefinedSubgraph) vertex;
		IsSubgraphDefiningExpression isSubgraphDefiningExpression = exprDefinedSubgraph
				.getFirstIsSubgraphDefiningExpressionIncidence(EdgeDirection.IN);
		Expression subgraphDefExpr = (Expression) isSubgraphDefiningExpression
				.getThat();
		subgraphDefExprEvaluator = vertexEvalMarker.getMark(subgraphDefExpr);
		return subgraphDefExprEvaluator.calculateSubtreeEvaluationCosts();
	}

}
