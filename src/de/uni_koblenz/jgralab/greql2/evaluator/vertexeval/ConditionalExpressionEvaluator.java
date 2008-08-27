/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import de.uni_koblenz.jgralab.greql2.evaluator.*;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.*;
import de.uni_koblenz.jgralab.greql2.jvalue.*;
import de.uni_koblenz.jgralab.greql2.schema.*;
import de.uni_koblenz.jgralab.*;

/**
 * Evaluates a ConditionalExpression vertex in the GReQL-2 Syntaxgraph
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class ConditionalExpressionEvaluator extends VertexEvaluator {

	/**
	 * The ConditionalExpression-Vertex this evaluator evaluates
	 */
	private ConditionalExpression vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * Creates a new ConditionExpressionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public ConditionalExpressionEvaluator(ConditionalExpression vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	/**
	 * evaluates the conditional expression
	 */
	@Override
	public JValue evaluate() throws EvaluateException {
		Expression condition = (Expression) vertex.getFirstIsConditionOf()
				.getAlpha();
		VertexEvaluator conditionEvaluator = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(condition);
		JValue conditionResult = conditionEvaluator.getResult(subgraph);
		Expression expressionToEvaluate = null;
		if (conditionResult.isBoolean()) {
			if (conditionResult.toBoolean() == Boolean.TRUE) {
				expressionToEvaluate = (Expression) vertex
						.getFirstIsTrueExprOf(EdgeDirection.IN).getAlpha();
			} else if (conditionResult.toBoolean() == Boolean.FALSE) {
				expressionToEvaluate = (Expression) vertex
						.getFirstIsFalseExprOf(EdgeDirection.IN).getAlpha();
			} else {
				expressionToEvaluate = (Expression) vertex
						.getFirstIsNullExprOf(EdgeDirection.IN).getAlpha();
			}
		} else {
			expressionToEvaluate = (Expression) vertex.getFirstIsNullExprOf(
					EdgeDirection.IN).getAlpha();
		}
		VertexEvaluator exprEvaluator = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(expressionToEvaluate);
		result = exprEvaluator.getResult(subgraph);
		return result;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsConditionalExpression(this, graphSize);
	}

}
