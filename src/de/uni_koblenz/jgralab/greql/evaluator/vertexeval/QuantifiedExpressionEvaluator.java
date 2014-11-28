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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.schema.Declaration;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.QuantificationType;
import de.uni_koblenz.jgralab.greql.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql.schema.Quantifier;

/**
 * Evaluates a QuantifiedExpression, a QuantifiedExpression is something like
 * "using FOO: exists s: FOO @ s = true".
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class QuantifiedExpressionEvaluator extends
		VertexEvaluator<QuantifiedExpression> {

	private QuantificationType quantificationType = null;

	private boolean initialized = false;

	private VertexEvaluator<? extends Expression> predicateEvaluator = null;

	public QuantifiedExpressionEvaluator(QuantifiedExpression vertex,
			GreqlQueryImpl query) {
		super(vertex, query);
	}

	private void initialize(InternalGreqlEvaluator evaluator) {
		Quantifier quantifier = vertex.getFirstIsQuantifierOfIncidence(
				EdgeDirection.IN).getAlpha();
		quantificationType = quantifier.get_type();
		Expression b = vertex.getFirstIsBoundExprOfIncidence(EdgeDirection.IN)
				.getAlpha();
		predicateEvaluator = query.getVertexEvaluator(b);
		initialized = true;
	}

	/**
	 * evaluates the QuantifiedEx
	 */
	@Override
	public Boolean evaluate(InternalGreqlEvaluator evaluator) {
		if (!initialized) {
			initialize(evaluator);
		}
		evaluator.progress(getOwnEvaluationCosts());

		boolean foundTrue = false;
		Declaration d = vertex.getFirstIsQuantifiedDeclOfIncidence(
				EdgeDirection.IN).getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) query
				.getVertexEvaluator(d);
		VariableDeclarationLayer declarationLayer = (VariableDeclarationLayer) declEval
				.getResult(evaluator);
		declarationLayer.reset();
		switch (quantificationType) {
		case EXISTS:
			while (declarationLayer.iterate(evaluator)) {
				Object tempResult = predicateEvaluator.getResult(evaluator);
				if (tempResult instanceof Boolean) {
					if ((Boolean) tempResult) {
						return Boolean.TRUE;
					}
				} else {
					// every result which is not a Boolean evaluates to true
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		case EXISTSONE:
			while (declarationLayer.iterate(evaluator)) {
				Object tempResult = predicateEvaluator.getResult(evaluator);
				if (tempResult instanceof Boolean) {
					if ((Boolean) tempResult) {
						if (foundTrue == true) {
							return Boolean.FALSE;
						} else {
							foundTrue = true;
						}
					}
				} else {
					// every result which is not a Boolean evaluates to true
					if (foundTrue == true) {
						return Boolean.FALSE;
					} else {
						foundTrue = true;
					}
				}
			}
			if (foundTrue) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		case FORALL:
			while (declarationLayer.iterate(evaluator)) {
				Object tempResult = predicateEvaluator.getResult(evaluator);
				if (tempResult instanceof Boolean) {
					if (!(Boolean) tempResult) {
						return Boolean.FALSE;
					}
				}
			}
			return Boolean.TRUE;
		default:
			throw new RuntimeException("FIXME: Unhandled quantification type "
					+ quantificationType);
		}
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		QuantifiedExpression quantifiedExpr = getVertex();

		VertexEvaluator<? extends Declaration> declEval = query
				.getVertexEvaluator(quantifiedExpr
						.getFirstIsQuantifiedDeclOfIncidence(EdgeDirection.IN)
						.getAlpha());
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts();

		VertexEvaluator<? extends Expression> boundExprEval = query
				.getVertexEvaluator(quantifiedExpr
						.getFirstIsBoundExprOfQuantifiedExpressionIncidence(
								EdgeDirection.IN).getAlpha());
		long boundExprCosts = boundExprEval.getCurrentSubtreeEvaluationCosts();

		long ownCosts = 20;
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = iteratedCosts + declCosts + boundExprCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

}
