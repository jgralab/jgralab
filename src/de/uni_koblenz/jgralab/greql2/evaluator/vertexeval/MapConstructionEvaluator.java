/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
