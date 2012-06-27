/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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
/**
 *
 */
package de.uni_koblenz.jgralab.greql.evaluator.vertexeval;

import org.pcollections.PCollection;
import org.pcollections.PMap;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.schema.Declaration;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.MapComprehension;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class MapComprehensionEvaluator extends
		ComprehensionEvaluator<MapComprehension> {

	public MapComprehensionEvaluator(MapComprehension vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	@Override
	protected VertexCosts calculateSubtreeEvaluationCosts() {
		MapComprehension mapComp = getVertex();
		Declaration decl = (Declaration) mapComp
				.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) query
				.getVertexEvaluator(decl);
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts();

		Expression key = (Expression) mapComp
				.getFirstIsKeyExprOfComprehensionIncidence(EdgeDirection.IN)
				.getAlpha();
		VertexEvaluator<? extends Expression> keyEval = query
				.getVertexEvaluator(key);
		Expression value = (Expression) mapComp
				.getFirstIsValueExprOfComprehensionIncidence(EdgeDirection.IN)
				.getAlpha();
		VertexEvaluator<? extends Expression> valEval = query
				.getVertexEvaluator(value);

		long resultCosts = keyEval.getCurrentSubtreeEvaluationCosts()
				+ valEval.getCurrentSubtreeEvaluationCosts();

		long ownCosts = keyEval.getEstimatedCardinality()
				+ (valEval.getEstimatedCardinality() * addToSetCosts);
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = iteratedCosts + resultCosts + declCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		initializeMaxCount(evaluator);
		VariableDeclarationLayer declLayer = getVariableDeclationLayer(evaluator);
		PMap<Object, Object> resultMap = JGraLab.map();

		Expression key = (Expression) vertex
				.getFirstIsKeyExprOfComprehensionIncidence(EdgeDirection.IN)
				.getAlpha();
		VertexEvaluator<? extends Expression> keyEval = query
				.getVertexEvaluator(key);
		Expression val = (Expression) vertex
				.getFirstIsValueExprOfComprehensionIncidence(EdgeDirection.IN)
				.getAlpha();
		VertexEvaluator<? extends Expression> valEval = query
				.getVertexEvaluator(val);
		declLayer.reset();
		while (declLayer.iterate(evaluator) && (resultMap.size() < maxCount)) {
			Object jkey = keyEval.getResult(evaluator);
			Object jval = valEval.getResult(evaluator);
			resultMap = resultMap.plus(jkey, jval);
		}
		return resultMap;
	}

	@Override
	public long calculateEstimatedCardinality() {
		MapComprehension setComp = getVertex();
		Declaration decl = (Declaration) setComp
				.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) query
				.getVertexEvaluator(decl);
		return declEval.getEstimatedCardinality();
	}

	@Override
	protected PCollection<Object> getResultDatastructure(
			InternalGreqlEvaluator evaluator) {
		return null;
	}

}
