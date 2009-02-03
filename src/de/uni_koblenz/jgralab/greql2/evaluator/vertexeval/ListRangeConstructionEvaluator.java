/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
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
	public Vertex getVertex() {
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

	@Override
	public JValue evaluate() throws EvaluateException {
		JValueList resultList = new JValueList();
		Expression firstElementExpression = (Expression) vertex
				.getFirstIsFirstValueOf(EdgeDirection.IN).getAlpha();
		Expression lastElementExpression = (Expression) vertex
				.getFirstIsLastValueOf(EdgeDirection.IN).getAlpha();
		VertexEvaluator firstElementEvaluator = greqlEvaluator
				.getVertexEvaluatorGraphMarker()
				.getMark(firstElementExpression);
		VertexEvaluator lastElementEvaluator = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(lastElementExpression);
		JValue firstElement = firstElementEvaluator.getResult(subgraph);
		JValue lastElement = lastElementEvaluator.getResult(subgraph);
		try {
			if (firstElement.isInteger() && lastElement.isInteger()) {
				if (firstElement.toInteger() < lastElement.toInteger())
					for (int i = firstElement.toInteger(); i < lastElement
							.toInteger() + 1; i++)
						// +1 needed because the top element should also belong
						// to the list
						resultList.add(new JValue(i));
				else
					for (int i = lastElement.toInteger(); i < firstElement
							.toInteger() + 1; i++)
						resultList.add(new JValue(i));
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
