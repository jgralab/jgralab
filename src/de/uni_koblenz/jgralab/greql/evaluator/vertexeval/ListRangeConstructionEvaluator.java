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

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.ListRangeConstruction;

/**
 * Creates a list of integers. Adds all integer-values to the list, that are
 * between the result of firstElementExpression and lastElementExpression. These
 * borders are also added to the list
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ListRangeConstructionEvaluator extends
		VertexEvaluator<ListRangeConstruction> {

	/**
	 * the default value that is estimated if the size of a listrange cannot be
	 * estimated
	 */
	protected static final int defaultListRangeSize = 50;

	public ListRangeConstructionEvaluator(ListRangeConstruction vertex,
			GreqlQueryImpl query) {
		super(vertex, query);
	}

	private VertexEvaluator<? extends Expression> firstElementEvaluator = null;

	private VertexEvaluator<? extends Expression> lastElementEvaluator = null;

	private void getEvals() {
		Expression firstElementExpression = vertex
				.getFirstIsFirstValueOfIncidence(EdgeDirection.IN).getAlpha();
		Expression lastElementExpression = vertex
				.getFirstIsLastValueOfIncidence(EdgeDirection.IN).getAlpha();
		firstElementEvaluator = query
				.getVertexEvaluator(firstElementExpression);
		lastElementEvaluator = query.getVertexEvaluator(lastElementExpression);
	}

	@Override
	public PVector<Integer> evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		PVector<Integer> resultList = JGraLab.vector();
		if (firstElementEvaluator == null) {
			getEvals();
		}
		Object firstElement = firstElementEvaluator.getResult(evaluator);
		Object lastElement = lastElementEvaluator.getResult(evaluator);
		if (firstElement instanceof Integer && lastElement instanceof Integer) {
			int firstInt = (Integer) firstElement;
			int lastInt = (Integer) lastElement;
			if (firstInt == lastInt) {
				resultList = resultList.plus(firstInt);
			} else {
				if (firstInt < lastInt) {
					for (int i = firstInt; i <= lastInt; i++) {
						resultList = resultList.plus(i);
					}
				} else {
					for (int i = firstInt; i >= lastInt; i--) {
						resultList = resultList.plus(i);
					}
				}
			}
		}

		return resultList;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		ListRangeConstruction exp = getVertex();
		VertexEvaluator<? extends Expression> startExpEval = query
				.getVertexEvaluator(exp.getFirstIsFirstValueOfIncidence()
						.getAlpha());
		VertexEvaluator<? extends Expression> targetExpEval = query
				.getVertexEvaluator(exp.getFirstIsLastValueOfIncidence()
						.getAlpha());
		long startCosts = startExpEval.getCurrentSubtreeEvaluationCosts();
		long targetCosts = targetExpEval.getCurrentSubtreeEvaluationCosts();
		long range = 0;
		if (startExpEval instanceof IntLiteralEvaluator) {
			if (targetExpEval instanceof IntLiteralEvaluator) {
				try {
					range = (((Number) targetExpEval.getResult(null))
							.longValue() - ((Number) startExpEval
							.getResult(null)).longValue()) + 1;
				} catch (Exception ex) {
					// if an exception occurs, the default value is used, so no
					// exceptionhandling is needed
				}
			}
		}
		if (range <= 0) {
			range = defaultListRangeSize;
		}
		long ownCosts = addToListCosts * range;
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = iteratedCosts + startCosts + targetCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public long calculateEstimatedCardinality() {
		ListRangeConstruction exp = getVertex();
		VertexEvaluator<? extends Expression> startExpEval = query
				.getVertexEvaluator(exp.getFirstIsFirstValueOfIncidence(
						EdgeDirection.IN).getAlpha());
		VertexEvaluator<? extends Expression> targetExpEval = query
				.getVertexEvaluator(exp.getFirstIsLastValueOfIncidence(
						EdgeDirection.IN).getAlpha());
		long range = 0;
		if (startExpEval instanceof IntLiteralEvaluator) {
			if (targetExpEval instanceof IntLiteralEvaluator) {
				try {
					range = (((Number) targetExpEval.getResult(null))
							.longValue() - ((Number) startExpEval
							.getResult(null)).longValue()) + 1;
				} catch (Exception ex) {
					// if an exception occurs, the default value is used, so no
					// exceptionhandling is needed
				}
			}
		}
		if (range > 0) {
			return range;
		} else {
			return defaultListRangeSize;
		}
	}

}
