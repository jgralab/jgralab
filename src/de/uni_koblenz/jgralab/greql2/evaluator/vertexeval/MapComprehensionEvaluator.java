/**
 *
 */
package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.MapComprehension;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 *
 */
public class MapComprehensionEvaluator extends VertexEvaluator {
	private MapComprehension vertex;

	public MapComprehensionEvaluator(MapComprehension vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator#
	 * calculateSubtreeEvaluationCosts
	 * (de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	protected VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsMapComprehension(this, graphSize);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator#evaluate
	 * ()
	 */
	@Override
	public JValue evaluate() throws EvaluateException {
		Declaration d = (Declaration) vertex.getFirstIsCompDeclOf(
				EdgeDirection.IN).getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(d);
		VariableDeclarationLayer declLayer = null;
		try {
			declLayer = declEval.getResult(subgraph).toDeclarationLayer();
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException("Error evaluating MapComprehension",
					exception);
		}

		JValueMap resultMap = new JValueMap();

		Vertex key = vertex.getFirstIsKeyExprOfComprehension(EdgeDirection.IN)
				.getAlpha();
		VertexEvaluator keyEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(key);
		Vertex val = vertex
				.getFirstIsValueExprOfComprehension(EdgeDirection.IN)
				.getAlpha();
		VertexEvaluator valEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(val);

		while (declLayer.iterate(subgraph)) {
			resultMap.put(keyEval.getResult(subgraph), valEval
					.getResult(subgraph));
		}
		return resultMap;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator#getVertex
	 * ()
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	@Override
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCardinalityMapComprehension(this, graphSize);
	}

}
