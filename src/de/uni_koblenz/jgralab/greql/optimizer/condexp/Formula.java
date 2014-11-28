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
/**
 *
 */
package de.uni_koblenz.jgralab.greql.optimizer.condexp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql.schema.IsArgumentOf;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class Formula {
	/**
	 * The maximum number of non-constant terms a formula may have to try to
	 * calculate the optimal ordering. If there are less NCTs, then the current
	 * order will be used.
	 */
	private static final int MAX_NON_CONSTANT_TERM_NUMBER = 3;

	protected static Logger logger = JGraLab.getLogger(Formula.class);

	protected GreqlQuery query;

	@Override
	public abstract String toString();

	public abstract Expression toExpression();

	public static Formula createFormulaFromExpression(Expression exp,
			GreqlQuery query) {
		Formula formula = createFormulaFromExpressionInternal(query, exp);
		OptimizerUtility.deleteOrphanedVerticesBelow(exp, new HashSet<Vertex>(
				formula.getNonConstantTermExpressions()));
		return formula;
	}

	public Formula(GreqlQuery query) {
		this.query = query;
	}

	private static Formula createFormulaFromExpressionInternal(
			GreqlQuery query, Expression exp) {
		assert exp.isValid() : exp + " is not valid!";
		if (exp instanceof BoolLiteral) {
			BoolLiteral bool = (BoolLiteral) exp;
			if (bool.is_boolValue()) {
				return new True(query);
			} else {
				return new False(query);
			}
		}

		if (exp instanceof FunctionApplication) {
			FunctionApplication funApp = (FunctionApplication) exp;
			if (OptimizerUtility.isAnd(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOfIncidence(EdgeDirection.IN);
				Expression leftArg = inc.getAlpha();
				Expression rightArg = inc.getNextIsArgumentOfIncidence(
						EdgeDirection.IN).getAlpha();
				return new And(query, createFormulaFromExpressionInternal(
						query, leftArg), createFormulaFromExpressionInternal(
						query, rightArg));
			}
			if (OptimizerUtility.isOr(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOfIncidence(EdgeDirection.IN);
				Expression leftArg = inc.getAlpha();
				Expression rightArg = inc.getNextIsArgumentOfIncidence(
						EdgeDirection.IN).getAlpha();
				return new Or(query, createFormulaFromExpressionInternal(query,
						leftArg), createFormulaFromExpressionInternal(query,
						rightArg));
			}
			if (OptimizerUtility.isNot(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOfIncidence(EdgeDirection.IN);
				Expression arg = inc.getAlpha();
				return new Not(query, createFormulaFromExpressionInternal(
						query, arg));
			}
		}

		return new NonConstantTerm(query, exp);
	}

	public Formula optimize() {
		ArrayList<Expression> nctExpressions = getNonConstantTermExpressions();
		if (nctExpressions.size() < 2) {
			// This formula is a literal or a formula containing only one
			// non-constant term, so there's nothing to optimize.
			return this;
		}

		ConditionalExpressionUnit bestUnit = calculateBestConditionalExpressionUnit(nctExpressions);
		return bestUnit.toConditionalExpression();
	}

	/**
	 * @param nonConstantTermExpressions
	 *            A list of all expressions that are contained in any
	 *            {@link NonConstantTerm}s
	 * @return the {@link ConditionalExpressionUnit} with the highest
	 *         <code>selectivity(booleanDifference) / costs(expression)</code>
	 *         ratio
	 */
	private ConditionalExpressionUnit calculateBestConditionalExpressionUnit(
			ArrayList<Expression> nonConstantTermExpressions) {
		if (nonConstantTermExpressions.size() > MAX_NON_CONSTANT_TERM_NUMBER) {
			logger.fine("Formula: " + nonConstantTermExpressions.size()
					+ " NCTEs ==> shortcutting...");
			return new ConditionalExpressionUnit(
					nonConstantTermExpressions.get(0), this);
		}
		ConditionalExpressionUnit current, best = null;
		for (Expression exp : nonConstantTermExpressions) {
			current = new ConditionalExpressionUnit(exp, this);

			// initialize best with the first one.
			if (best == null) {
				best = current;
			}

			if ((best == null)
					|| (best.getInfluenceCostRatio() < current
							.getInfluenceCostRatio())) {
				best = current;
			}
		}
		return best;
	}

	protected abstract ArrayList<Expression> getNonConstantTermExpressions();

	/**
	 * Create a new {@link Formula} where each {@link NonConstantTerm} that
	 * represents the {@link Expression} <code>exp</code> is replaced by
	 * <code>literal</code>.
	 * 
	 * @param exp
	 *            the {@link Expression} whose {@link NonConstantTerm}s should
	 *            be replaced
	 * @param literal
	 *            the replacement {@link Literal}
	 * @return a new {@link Formula}
	 */
	protected abstract Formula calculateReplacementFormula(Expression exp,
			Literal literal);

	/**
	 * Create a new {@link Formula} which is simplified according these rules:
	 * <code>a and true = a</code>, <code>a and false = false</code>,
	 * <code>a or true = true</code>, <code>a or false = a</code>,
	 * <code>not true = false</code>, <code>not false = true</code>,
	 * <code>not not a = a</code>.
	 * 
	 * @return a simplified {@link Formula}
	 */
	public abstract Formula simplify();

	public abstract double getSelectivity();

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();
}
