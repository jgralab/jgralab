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
import de.uni_koblenz.jgralab.greql.evaluator.EvaluatorUtilities;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.exception.UndefinedVariableException;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.Identifier;
import de.uni_koblenz.jgralab.greql.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql.schema.IsIdOfStoreClause;
import de.uni_koblenz.jgralab.greql.schema.Variable;

/**
 * Evaluates a GreqlExpression vertex in the GReQL-2 Syntaxgraph. A
 * GReQL2-Expression is the rootvertex of the GReQL-2Syntaxgraph. It contains
 * the bound/free variables, that are defined via "using" and binds them to the
 * values in the variableMap of the GreqlEvaluator.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class GreqlExpressionEvaluator extends VertexEvaluator<GreqlExpression> {

	protected static final int greql2ExpressionCostsFactor = 3;

	private void initializeBoundVariables(InternalGreqlEvaluator evaluator) {
		IsBoundVarOf inc = vertex
				.getFirstIsBoundVarOfIncidence(EdgeDirection.IN);
		while (inc != null) {
			Variable currentBoundVariable = inc.getAlpha();
			Object variableValue = evaluator.getVariable(currentBoundVariable
					.get_name());
			if (variableValue == null) {
				throw new UndefinedVariableException(currentBoundVariable,
						createSourcePositions(inc));
			}
			VariableEvaluator<Variable> variableEval = (VariableEvaluator<Variable>) query
					.getVertexEvaluator(currentBoundVariable);
			variableEval.setValue(variableValue, evaluator);
			inc = inc.getNextIsBoundVarOfIncidence(EdgeDirection.IN);
		}
	}

	public GreqlExpressionEvaluator(GreqlExpression vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	/**
	 * sets the values of all bound variables and evaluates the queryexpression
	 */
	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		EvaluatorUtilities.checkImports(vertex, evaluator.getSchema());
		initializeBoundVariables(evaluator);
		Expression boundExpression = vertex.getFirstIsQueryExprOfIncidence(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator<? extends Expression> eval = query
				.getVertexEvaluator(boundExpression);
		Object result = eval.getResult(evaluator);
		// if the query contains a "store as " - clause, there is a
		// "isIdOfInc"-Incidence connected with the GreqlExpression
		IsIdOfStoreClause storeInc = vertex
				.getFirstIsIdOfStoreClauseIncidence(EdgeDirection.IN);
		if (storeInc != null) {
			VertexEvaluator<Identifier> storeEval = query
					.getVertexEvaluator(storeInc.getAlpha());
			String varName = storeEval.getResult(evaluator).toString();
			// TODO [greqlrenovation] VariableDeclaration has an own
			// toString(InternalGreqlEvaluator)-method. check the use
			evaluator.setVariable(varName, result);
		}
		evaluator.progress(getOwnEvaluationCosts());
		return result;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		GreqlExpression greqlExp = getVertex();
		VertexEvaluator<? extends Expression> queryExpEval = query
				.getVertexEvaluator(greqlExp.getFirstIsQueryExprOfIncidence()
						.getAlpha());
		long queryCosts = queryExpEval.getCurrentSubtreeEvaluationCosts();
		// VertexEvaluator.logger.info("QueryCosts: " + queryCosts);
		IsBoundVarOf boundVarInc = greqlExp.getFirstIsBoundVarOfIncidence();
		int boundVars = 0;
		while (boundVarInc != null) {
			boundVars++;
			boundVarInc = boundVarInc.getNextIsBoundVarOfIncidence();
		}
		long ownCosts = boundVars * greql2ExpressionCostsFactor;
		long iteratedCosts = ownCosts;
		long subtreeCosts = ownCosts + queryCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

}
