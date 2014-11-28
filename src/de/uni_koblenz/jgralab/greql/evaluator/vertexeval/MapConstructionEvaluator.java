/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralab.greql.evaluator.vertexeval;

import org.pcollections.PMap;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.IsKeyExprOfConstruction;
import de.uni_koblenz.jgralab.greql.schema.IsValueExprOfConstruction;
import de.uni_koblenz.jgralab.greql.schema.MapConstruction;

public class MapConstructionEvaluator extends VertexEvaluator<MapConstruction> {

	public MapConstructionEvaluator(MapConstruction vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	@Override
	protected VertexCosts calculateSubtreeEvaluationCosts() {
		MapConstruction mapCons = getVertex();
		IsKeyExprOfConstruction keyInc = mapCons
				.getFirstIsKeyExprOfConstructionIncidence(EdgeDirection.IN);
		IsValueExprOfConstruction valInc = mapCons
				.getFirstIsValueExprOfConstructionIncidence(EdgeDirection.IN);
		long parts = 0;
		long partCosts = 0;
		while (keyInc != null) {
			VertexEvaluator<? extends Expression> keyEval = query
					.getVertexEvaluator(keyInc.getAlpha());
			partCosts += keyEval.getCurrentSubtreeEvaluationCosts();
			VertexEvaluator<? extends Expression> valueEval = query
					.getVertexEvaluator(valInc.getAlpha());
			partCosts += keyEval.getCurrentSubtreeEvaluationCosts()
					+ valueEval.getCurrentSubtreeEvaluationCosts();
			parts++;
			keyInc = keyInc
					.getNextIsKeyExprOfConstructionIncidence(EdgeDirection.IN);
			valInc = valInc
					.getNextIsValueExprOfConstructionIncidence(EdgeDirection.IN);
		}

		long ownCosts = (parts * addToSetCosts) + 2;
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = iteratedCosts + partCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		PMap<Object, Object> map = JGraLab.map();
		PVector<Object> keys = JGraLab.vector();
		for (IsKeyExprOfConstruction e : vertex
				.getIsKeyExprOfConstructionIncidences(EdgeDirection.IN)) {
			Expression exp = e.getAlpha();
			VertexEvaluator<? extends Expression> expEval = query
					.getVertexEvaluator(exp);
			keys = keys.plus(expEval.getResult(evaluator));
		}

		PVector<Object> values = JGraLab.vector();
		for (IsValueExprOfConstruction e : vertex
				.getIsValueExprOfConstructionIncidences(EdgeDirection.IN)) {
			Expression exp = e.getAlpha();
			VertexEvaluator<? extends Expression> expEval = query
					.getVertexEvaluator(exp);
			values = values.plus(expEval.getResult(evaluator));
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
	public long calculateEstimatedCardinality() {
		long mappings = 0;
		MapConstruction mapCons = getVertex();
		IsKeyExprOfConstruction inc = mapCons
				.getFirstIsKeyExprOfConstructionIncidence(EdgeDirection.IN);
		while (inc != null) {
			mappings++;
			inc = inc.getNextIsKeyExprOfConstructionIncidence(EdgeDirection.IN);
		}
		return mappings;
	}

}
