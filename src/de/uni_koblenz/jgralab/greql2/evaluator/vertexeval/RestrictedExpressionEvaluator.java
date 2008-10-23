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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.RestrictedExpression;

/**
 * Evaluates the given RestrictedExpression. A RestrictedExpression contains a
 * Expression A which gets evaluates and a boolean Expression B as restriction.
 * If the result of B ist true, the result of the RestrictedExpression is the
 * result of A, otherwise (B ist false or null) the result of the
 * RestrictedExpression is null
 */
public class RestrictedExpressionEvaluator extends VertexEvaluator {

	private RestrictedExpression vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	public RestrictedExpressionEvaluator(RestrictedExpression vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		Expression restrictedExp = (Expression) vertex
				.getFirstIsRestrictedExprOf(EdgeDirection.IN).getAlpha();
		VertexEvaluator restExprEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(restrictedExp);
		Expression restriction = (Expression) vertex
				.getFirstIsRestrictionOf(EdgeDirection.IN).getAlpha();
		VertexEvaluator restrictionEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(restriction);

		JValue condition = (JValue) restrictionEval.getResult(subgraph);
		if (condition.isBoolean()) {
			try {
				if (condition.toBoolean() == Boolean.TRUE) {
					return restExprEval.getResult(subgraph);
				} else {
					return new JValue();
				}
			} catch (JValueInvalidTypeException exception) {
				throw new EvaluateException(
						"Error evaluating a restricted Expression : "
								+ exception.toString());
			}
		}
		throw new EvaluateException(
				"Error evaluating a restricted Expression : Condition doesn't evaluate to a TrivalentBoolean Value");
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsRestrictedExpression(this, graphSize);
	}

}
