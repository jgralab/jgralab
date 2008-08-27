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

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.*;
import de.uni_koblenz.jgralab.greql2.schema.*;
import de.uni_koblenz.jgralab.*;

/**
 * Evaluates a QuantifiedExpression, a QuantifiedExpression is something like
 * "using FOO: exists s: FOO @ s = true".
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class QuantifiedExpressionEvaluator extends VertexEvaluator {

	private QuantifiedExpression vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * @param eval
	 *            the GreqlEvaluator this VertexEvaluator belongs to
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public QuantifiedExpressionEvaluator(QuantifiedExpression vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	/**
	 * evaluates the QuantifiedEx
	 */
	@Override
	public JValue evaluate() throws EvaluateException {
		Declaration d = (Declaration) vertex.getFirstIsQuantifiedDeclOf(
				EdgeDirection.IN).getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(d);
		VariableDeclarationLayer declLayer = null;
		try {
			declLayer = declEval.getResult(subgraph).toDeclarationLayer();
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException(
					"Error evaluating QuantifiedExpression", exception);
		}
		Quantifier quantifier = (Quantifier) vertex.getFirstIsQuantifierOf(
				EdgeDirection.IN).getAlpha();
		Expression b = (Expression) vertex.getFirstIsBoundExprOf(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator vertexEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(b);

		int noOfVariableCombinations = 0;
		if (quantifier.getName().equals("exists")) {
			// exists at least one
			boolean foundNull = false;
			while (declLayer.iterate(subgraph)) {
				noOfVariableCombinations++;
				JValue tempResult = vertexEval.getResult(subgraph);
				if (tempResult.isBoolean()) {
					try {
						if (tempResult.toBoolean() == JValueBoolean
								.getTrueValue()) {
							return new JValue(JValueBoolean.getTrueValue());
						} else if (tempResult.toBoolean() == null)
							foundNull = true;
					} catch (JValueInvalidTypeException exception) {
						throw new EvaluateException(
								"Error evaluation Exists clause", exception);
					}
				}
			}

			if (foundNull)
				return new JValue(JValueBoolean.getNullValue());
			else
				return new JValue(JValueBoolean.getFalseValue());
		} else if (quantifier.getName().equals("exists!")) {
			// exists exactly one
			boolean foundTrue = false;
			boolean foundNull = false;
			while (declLayer.iterate(subgraph)) {
				noOfVariableCombinations++;
				JValue tempResult = vertexEval.getResult(subgraph);
				if (tempResult.isBoolean()) {
					try {
						// GreqlEvaluator.println("Current Value is: " +
						// tempResult.toBoolean());
						if (tempResult.toBoolean() == JValueBoolean
								.getTrueValue()) {
							if (foundTrue == true) {
								// GreqlEvaluator.println("Returning false cause
								// double true found");
								return new JValue(JValueBoolean.getFalseValue());
							} else
								foundTrue = true;
						} else if (tempResult.toBoolean() == JValueBoolean
								.getNullValue())
							foundNull = true;
					} catch (JValueInvalidTypeException exception) {
						throw new EvaluateException(
								"Error evaluation Exists! clause", exception);
					}
				}
			}

			if (foundNull) {
				return new JValue(JValueBoolean.getNullValue());
			}
			if (foundTrue) {
				return new JValue(JValueBoolean.getTrueValue());
			}
			return new JValue(JValueBoolean.getFalseValue());
		} else if (quantifier.getName().equals("forall")) {
			// for all
			while (declLayer.iterate(subgraph)) {
				noOfVariableCombinations++;
				JValue tempResult = vertexEval.getResult(subgraph);
				if (tempResult.isBoolean()) {
					try {
						if (tempResult.toBoolean() == JValueBoolean
								.getFalseValue()) {
							return new JValue(JValueBoolean.getFalseValue());
						}
						if (tempResult.toBoolean() == null) {
							return new JValue(JValueBoolean.getNullValue());
						}
					} catch (JValueInvalidTypeException exception) {
						throw new EvaluateException(
								"Error evaluation Forall clause", exception);
					}
				}
			}

			return new JValue(Boolean.TRUE);
		} else {
			throw new EvaluateException(
					"Found QuantifiedExpression that is neither exists, existis! not forall");
		}
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsQuantifiedExpression(this, graphSize);
	}

}
