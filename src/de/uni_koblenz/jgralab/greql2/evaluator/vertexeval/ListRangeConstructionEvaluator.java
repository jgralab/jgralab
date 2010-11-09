/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.ListRangeConstruction;

/**
 * Creates a list of integers. Adds all integer-values to the list, that are
 * between the result of firstElementExpression and lastElementExpression. These
 * borders are also added to the list
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ListRangeConstructionEvaluator extends VertexEvaluator {

	private ListRangeConstruction vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	/**
	 * Creates a new ListRangeConstructionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public ListRangeConstructionEvaluator(ListRangeConstruction vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	private VertexEvaluator firstElementEvaluator = null;

	private VertexEvaluator lastElementEvaluator = null;

	private void getEvals() {
		Expression firstElementExpression = (Expression) vertex
				.getFirstIsFirstValueOfIncidence(EdgeDirection.IN).getAlpha();
		Expression lastElementExpression = (Expression) vertex
				.getFirstIsLastValueOfIncidence(EdgeDirection.IN).getAlpha();
		firstElementEvaluator = greqlEvaluator.getVertexEvaluatorGraphMarker()
				.getMark(firstElementExpression);
		lastElementEvaluator = greqlEvaluator.getVertexEvaluatorGraphMarker()
				.getMark(lastElementExpression);
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		JValueList resultList = new JValueList();
		if (firstElementEvaluator == null) {
			getEvals();
		}
		JValue firstElement = firstElementEvaluator.getResult(subgraph);
		JValue lastElement = lastElementEvaluator.getResult(subgraph);
		try {
			if (firstElement.isInteger() && lastElement.isInteger()) {
				if (firstElement.toInteger() < lastElement.toInteger()) {
					for (int i = firstElement.toInteger(); i < lastElement
							.toInteger() + 1; i++) {
						// +1 needed because the top element should also belong
						// to the list
						resultList.add(new JValueImpl(i));
					}
				} else {
					for (int i = lastElement.toInteger(); i < firstElement
							.toInteger() + 1; i++) {
						resultList.add(new JValueImpl(i));
					}
				}
			}
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException("Error in ListConstruction : "
					+ exception.toString());
		}

		return resultList;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsListRangeConstruction(this, graphSize);
	}

	@Override
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCardinalityListRangeConstruction(this, graphSize);
	}

}
