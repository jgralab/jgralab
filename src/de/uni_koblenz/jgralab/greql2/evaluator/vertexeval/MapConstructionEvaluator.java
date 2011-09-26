/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

import org.pcollections.PMap;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.GreqlException;
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
	public Object evaluate() {
		PMap<Object, Object> map = JGraLab.map();
		PVector<Object> keys = JGraLab.vector();
		for (IsKeyExprOfConstruction e : mapConstruction
				.getIsKeyExprOfConstructionIncidences(EdgeDirection.IN)) {
			Vertex exp = e.getAlpha();
			VertexEvaluator expEval = vertexEvalMarker.getMark(exp);
			keys = keys.plus(expEval.getResult());
		}

		PVector<Object> values = JGraLab.vector();
		for (IsValueExprOfConstruction e : mapConstruction
				.getIsValueExprOfConstructionIncidences(EdgeDirection.IN)) {
			Vertex exp = e.getAlpha();
			VertexEvaluator expEval = vertexEvalMarker.getMark(exp);
			values = values.plus(expEval.getResult());
		}

		if (keys.size() != values.size()) {
			throw new GreqlException("Map construction has " + keys.size()
					+ " key(s) and " + values.size() + " value(s).");
		}

		for (int i = 0; i < keys.size(); i++) {
			map = map.plus(keys.get(i), values.get(i));
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
