package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsKeyExprOfConstruction;
import de.uni_koblenz.jgralab.greql2.schema.IsValueExprOfConstruction;
import de.uni_koblenz.jgralab.greql2.schema.MapConstruction;

public class MapConstructionEvaluator extends VertexEvaluator {
	private MapConstruction mapConstruction;

	public MapConstructionEvaluator(MapConstruction vertex, GreqlEvaluator eval) {
		super(eval);
		mapConstruction = vertex;
	}

	@Override
	protected VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsMapConstruction(this, graphSize);
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		JValueMap map = new JValueMap();
		JValueList keys = new JValueList();
		for (IsKeyExprOfConstruction e : mapConstruction
				.getIsKeyExprOfConstructionIncidences(EdgeDirection.IN)) {
			Vertex exp = e.getAlpha();
			VertexEvaluator expEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(exp);
			keys.add(expEval.getResult(subgraph));
		}

		JValueList values = new JValueList();
		for (IsValueExprOfConstruction e : mapConstruction
				.getIsValueExprOfConstructionIncidences(EdgeDirection.IN)) {
			Vertex exp = e.getAlpha();
			VertexEvaluator expEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(exp);
			values.add(expEval.getResult(subgraph));
		}

		if (keys.size() != values.size()) {
			throw new EvaluateException(
					"The map construction has a different key than value number!");
		}

		for (int i = 0; i < keys.size(); i++) {
			map.put(keys.get(i), values.get(i));
		}

		return map;
	}

	@Override
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCardinalityMapConstruction(this, graphSize);
	}

	@Override
	public Greql2Vertex getVertex() {
		return mapConstruction;
	}

}
