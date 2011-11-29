package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ExpressionDefinedSubgraph;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsSubgraphDefiningExpression;

public class ExpressionDefinedSubgraphEvaluator extends VertexEvaluator {

	VertexEvaluator subgraphDefExprEvaluator = null;
	
	ExpressionDefinedSubgraph vertex;
	
	public ExpressionDefinedSubgraphEvaluator(ExpressionDefinedSubgraph vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}
	

	@Override
	public Object evaluate() {
		if (subgraphDefExprEvaluator == null) {	
			ExpressionDefinedSubgraph exprDefinedSubgraph = (ExpressionDefinedSubgraph) vertex;
			IsSubgraphDefiningExpression isSubgraphDefiningExpression = exprDefinedSubgraph.getFirstIsSubgraphDefiningExpressionIncidence(EdgeDirection.IN);
			Expression subgraphDefExpr = (Expression) isSubgraphDefiningExpression.getThat();
			subgraphDefExprEvaluator = vertexEvalMarker.getMark(subgraphDefExpr);
		}	
		Object subgraph = subgraphDefExprEvaluator.getResult();
		if (subgraph instanceof TraversalContext) {
			result = subgraph;
		} else {
			//create new traversal context based on element set
			@SuppressWarnings("unchecked")
			final PSet<GraphElement> elemSet = (PSet<GraphElement>) subgraph;
			result = new TraversalContext() {
				@Override
				public boolean containsVertex(Vertex v) {
					return elemSet.contains(v);
				}
				@Override
				public boolean containsEdge(Edge e) {
					return elemSet.contains(e);
				}
			};
		}
		return result;
	}

	@Override
	protected VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		ExpressionDefinedSubgraph exprDefinedSubgraph = (ExpressionDefinedSubgraph) vertex;
		IsSubgraphDefiningExpression isSubgraphDefiningExpression = exprDefinedSubgraph.getFirstIsSubgraphDefiningExpressionIncidence(EdgeDirection.IN);
		Expression subgraphDefExpr = (Expression) isSubgraphDefiningExpression.getThat();
		subgraphDefExprEvaluator = vertexEvalMarker.getMark(subgraphDefExpr);
		return subgraphDefExprEvaluator.calculateSubtreeEvaluationCosts(graphSize);
	}


	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

}
