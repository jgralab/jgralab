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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.Query;
import de.uni_koblenz.jgralab.greql2.schema.ConditionalExpression;
import de.uni_koblenz.jgralab.greql2.schema.Expression;

/**
 * Evaluates a ConditionalExpression vertex in the GReQL-2 Syntaxgraph
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ConditionalExpressionEvaluator extends
		VertexEvaluator<ConditionalExpression> {

	/**
	 * Creates a new ConditionExpressionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public ConditionalExpressionEvaluator(ConditionalExpression vertex,
			Query query) {
		super(vertex, query);
	}

	/**
	 * evaluates the conditional expression
	 */
	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		Expression condition = vertex.getFirstIsConditionOfIncidence(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator<? extends Expression> conditionEvaluator = query
				.getVertexEvaluator(condition);
		Object conditionResult = conditionEvaluator.getResult(evaluator);
		Expression expressionToEvaluate = null;

		Boolean value = (Boolean) conditionResult;
		if (value.booleanValue()) {
			expressionToEvaluate = vertex.getFirstIsTrueExprOfIncidence(
					EdgeDirection.IN).getAlpha();
		} else {
			expressionToEvaluate = vertex.getFirstIsFalseExprOfIncidence(
					EdgeDirection.IN).getAlpha();
		}

		Object result = null;
		if (expressionToEvaluate != null) {
			VertexEvaluator<? extends Expression> exprEvaluator = query
					.getVertexEvaluator(expressionToEvaluate);
			result = exprEvaluator.getResult(evaluator);
			evaluator.setLocalEvaluationResult(vertex, result);
		} else {
			evaluator.removeLocalEvaluationResult(vertex);
			result = null;
		}
		return result;
	}

	// @Override
	// public VertexCosts calculateSubtreeEvaluationCosts() {
	// return greqlEvaluator.getCostModel()
	// .calculateCostsConditionalExpression(this);
	// }

}
