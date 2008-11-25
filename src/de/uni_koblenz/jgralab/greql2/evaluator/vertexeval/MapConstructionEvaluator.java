package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.greql2.schema.IsKeyValueTupleOf;
import de.uni_koblenz.jgralab.greql2.schema.MapConstruction;
import de.uni_koblenz.jgralab.greql2.schema.TupleConstruction;

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
		for (IsKeyValueTupleOf ikvt : mapConstruction
				.getIsKeyValueTupleOfIncidences()) {
			TupleConstruction tupConstr = (TupleConstruction) ikvt.getAlpha();
			VertexEvaluator tupEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(tupConstr);
			JValueTuple tup = (JValueTuple) tupEval.getResult(subgraph);
			if (tup.size() != 2) {
				throw new EvaluateException(
						"The tuples of a MapConstruction must have exactly 2 entries!");
			}
			map.put(tup.get(0), tup.get(1));
		}
		return map;
	}

	@Override
	public Vertex getVertex() {
		return mapConstruction;
	}

}
