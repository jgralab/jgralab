package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsExpressionOnSubgraph;
import de.uni_koblenz.jgralab.greql2.schema.IsSubgraphDefinitionOf;
import de.uni_koblenz.jgralab.greql2.schema.SubgraphDefinition;
import de.uni_koblenz.jgralab.greql2.schema.SubgraphRestrictedExpression;


public class SubgraphRestrictedExpressionEvaluator extends VertexEvaluator {

	SubgraphRestrictedExpression vertex;
	
	SubgraphDefinitionEvaluator subgraphDefinitionEval;
	
	VertexEvaluator exprEval;
	
	public SubgraphRestrictedExpressionEvaluator(SubgraphRestrictedExpression vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	@Override
	public Object evaluate() {
		//take traversal context for subgraph 
		if (subgraphDefinitionEval == null) {
			IsSubgraphDefinitionOf isSubgraphDef = vertex.getFirstIsSubgraphDefinitionOfIncidence(EdgeDirection.IN);
			SubgraphDefinition defVertex = (SubgraphDefinition) isSubgraphDef.getThat();
			subgraphDefinitionEval = (SubgraphDefinitionEvaluator) vertexEvalMarker.getMark(defVertex);
		}
		TraversalContext subgraph = (TraversalContext) subgraphDefinitionEval.getResult();
		
		//take restricted expression
		if (exprEval == null) {
			IsExpressionOnSubgraph isExprOn = vertex.getFirstIsExpressionOnSubgraphIncidence(EdgeDirection.IN);
			Expression expr = (Expression) isExprOn.getThat();
			exprEval = (VertexEvaluator) vertexEvalMarker.getMark(expr);
		}
		
		//set traversal context
		TraversalContext oldTraversalContext = graph.getTraversalContext();
		graph.setTraversalContext(subgraph);
		
		//evaluate restricted expression with traversal context
		result = exprEval.getResult();
		
		//release traversal context
		graph.setTraversalContext(oldTraversalContext);
		return result;
	}

	@Override
	protected VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		//return greqlEvaluator.getCostModel().calculateCostsSubgraphRestrictedExpression(this, graphSize);
		if (subgraphDefinitionEval == null) {
			IsSubgraphDefinitionOf isSubgraphDef = vertex.getFirstIsSubgraphDefinitionOfIncidence(EdgeDirection.IN);
			SubgraphDefinition defVertex = (SubgraphDefinition) isSubgraphDef.getThat();
			subgraphDefinitionEval = (SubgraphDefinitionEvaluator) vertexEvalMarker.getMark(defVertex);
		}
		
		//take restricted expression
		if (exprEval == null) {
			IsExpressionOnSubgraph isExprOn = vertex.getFirstIsExpressionOnSubgraphIncidence(EdgeDirection.IN);
			Expression expr = (Expression) isExprOn.getThat();
			exprEval = (VertexEvaluator) vertexEvalMarker.getMark(expr);
		}
		long ownCosts = 10;
		long iteratedCosts = ownCosts *  getVariableCombinations(graphSize);
		long subtree = subgraphDefinitionEval.getCurrentSubtreeEvaluationCosts(graphSize) + exprEval.getCurrentSubtreeEvaluationCosts(graphSize) + iteratedCosts; 

		return new VertexCosts(ownCosts, iteratedCosts, subtree);
	}

}
