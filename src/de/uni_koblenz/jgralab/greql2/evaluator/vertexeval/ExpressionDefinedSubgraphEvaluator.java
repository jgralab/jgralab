package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.Query;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ExpressionDefinedSubgraph;
import de.uni_koblenz.jgralab.greql2.schema.IsSubgraphDefiningExpression;

public class ExpressionDefinedSubgraphEvaluator extends
		SubgraphDefinitionEvaluator<ExpressionDefinedSubgraph> {

	VertexEvaluator<? extends Expression> subgraphDefExprEvaluator = null;

	public ExpressionDefinedSubgraphEvaluator(ExpressionDefinedSubgraph vertex,
			Query query) {
		super(vertex, query);
	}

	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		if (subgraphDefExprEvaluator == null) {
			ExpressionDefinedSubgraph exprDefinedSubgraph = vertex;
			IsSubgraphDefiningExpression isSubgraphDefiningExpression = exprDefinedSubgraph
					.getFirstIsSubgraphDefiningExpressionIncidence(EdgeDirection.IN);
			Expression subgraphDefExpr = (Expression) isSubgraphDefiningExpression
					.getThat();
			subgraphDefExprEvaluator = query
					.getVertexEvaluator(subgraphDefExpr);
		}
		return subgraphDefExprEvaluator.getResult(evaluator);
	}

	// @Override
	// protected VertexCosts calculateSubtreeEvaluationCosts() {
	// ExpressionDefinedSubgraph exprDefinedSubgraph =
	// (ExpressionDefinedSubgraph) vertex;
	// IsSubgraphDefiningExpression isSubgraphDefiningExpression =
	// exprDefinedSubgraph
	// .getFirstIsSubgraphDefiningExpressionIncidence(EdgeDirection.IN);
	// Expression subgraphDefExpr = (Expression) isSubgraphDefiningExpression
	// .getThat();
	// subgraphDefExprEvaluator = vertexEvalMarker.getMark(subgraphDefExpr);
	// return subgraphDefExprEvaluator.calculateSubtreeEvaluationCosts();
	// }

}
