/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.QuantificationType;
import de.uni_koblenz.jgralab.greql2.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql2.schema.Quantifier;

/**
 * Evaluates a QuantifiedExpression, a QuantifiedExpression is something like
 * "using FOO: exists s: FOO @ s = true".
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class QuantifiedExpressionEvaluator extends VertexEvaluator {

	private QuantifiedExpression vertex;
	
	private VariableDeclarationLayer declarationLayer = null;
	
	private QuantificationType quantificationType = null;
	
	private boolean initialized = false;
	
	private VertexEvaluator predicateEvaluator = null;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
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
	
	
	private void initialize() {
		Declaration d = (Declaration) vertex.getFirstIsQuantifiedDeclOf(EdgeDirection.IN).getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(d);
		declarationLayer = (VariableDeclarationLayer) declEval.getResult(subgraph).toObject();
		Quantifier quantifier = (Quantifier) vertex.getFirstIsQuantifierOf(
				EdgeDirection.IN).getAlpha();
		quantificationType = quantifier.get_type();
		Expression b = (Expression) vertex.getFirstIsBoundExprOf(
				EdgeDirection.IN).getAlpha();
		predicateEvaluator = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(b);		
		initialized = true;
	}

	/**
	 * evaluates the QuantifiedEx
	 */
	@Override
	public JValue evaluate() throws EvaluateException {
		if (!initialized) {
			initialize();
		}

		int noOfVariableCombinations = 0;
		boolean foundTrue = false;
		declarationLayer.reset();
		switch (quantificationType) {
		case EXISTS:
			while (declarationLayer.iterate(subgraph)) {
				noOfVariableCombinations++;
				JValue tempResult = predicateEvaluator.getResult(subgraph);
				if (tempResult.isBoolean()) {
					try {
						if (tempResult.toBoolean() == JValueBoolean.getTrueValue()) {
							return new JValueImpl(JValueBoolean.getTrueValue());
						} 
					} catch (JValueInvalidTypeException exception) {
						throw new EvaluateException(
								"Error evaluation Exists clause", exception);
					}
				}
			}
			return new JValueImpl(JValueBoolean.getFalseValue());
		case EXISTSONE:
			while (declarationLayer.iterate(subgraph)) {
				noOfVariableCombinations++;
				JValue tempResult = predicateEvaluator.getResult(subgraph);
				if (tempResult.isBoolean()) {
					try {
						if (tempResult.toBoolean().equals(
								JValueBoolean.getTrueValue())) {
							if (foundTrue == true) {
								return new JValueImpl(JValueBoolean.getFalseValue());
							} else {
								foundTrue = true;
							}
						}
					} catch (JValueInvalidTypeException exception) {
						throw new EvaluateException(
								"Error evaluation Exists! clause", exception);
					}
				}
			}
			if (foundTrue) {
				return new JValueImpl(JValueBoolean.getTrueValue());
			}
			return new JValueImpl(JValueBoolean.getFalseValue());
		case FORALL:	
			while (declarationLayer.iterate(subgraph)) {
				noOfVariableCombinations++;
				JValue tempResult = predicateEvaluator.getResult(subgraph);
				if (tempResult.isBoolean()) {
					try {
						if (tempResult.toBoolean().equals(
								JValueBoolean.getFalseValue())) {
							return new JValueImpl(JValueBoolean.getFalseValue());
						}
					} catch (JValueInvalidTypeException exception) {
						throw new EvaluateException(
								"Error evaluation Forall clause", exception);
					}
				}
			}
			return new JValueImpl(Boolean.TRUE);
		default:
			throw new EvaluateException(
					"Found QuantifiedExpression that is neither exists, existis! not forall");
		}
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCostsQuantifiedExpression(this, graphSize);
	}

}
