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

import java.util.Map;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.UndefinedVariableException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsIdOf;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;

/**
 * Evaluates a Greql2Expression vertex in the GReQL-2 Syntaxgraph. A
 * GReQL2-Expression is the rootvertex of the GReQL-2Syntaxgraph. It contains
 * the bound/free variables, that are defined via "using" and binds them to the
 * values in the variableMap of the Greql2Evaluator.
 *
 * @author ist@uni-koblenz.de
 *
 */
public class Greql2ExpressionEvaluator extends VertexEvaluator {

	/**
	 * The Greql2Expression-Vertex this evaluator evaluates
	 */
	private Greql2Expression vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * The varibles that are defined via the <code>using</code> clause. They are
	 * called bound or also free variables
	 */
	private Map<String, JValue> boundVariables;

	/**
	 * @param eval
	 *            the GreqlEvaluator this VertexEvaluator belongs to
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public Greql2ExpressionEvaluator(Greql2Expression vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
		this.boundVariables = eval.getVariables();
	}

	/**
	 * sets the values of all bound variables and evaluates the queryexpression
	 */
	@Override
	public JValue evaluate() throws EvaluateException {
		for (String importedType : vertex.getImportedTypes()) {
			AttributedElementClass elemClass = (AttributedElementClass) vertex.getSchema()
			.getAttributedElementClass(new QualifiedName(importedType));
			greqlEvaluator.addKnownType(elemClass);
		}
		IsBoundVarOf inc = vertex.getFirstIsBoundVarOf(EdgeDirection.IN);
		while (inc != null) {
			Variable currentBoundVariable = (Variable) inc.getAlpha();
			JValue variableValue = boundVariables.get(currentBoundVariable
					.getName());
			if (variableValue == null) {
				throw new UndefinedVariableException(currentBoundVariable
						.getName(), createSourcePositions(inc));
			}
			VariableEvaluator variableEval = (VariableEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(
							currentBoundVariable);
			variableEval.setValue(variableValue);
			inc = inc.getNextIsBoundVarOf(EdgeDirection.IN);
		}
		Expression boundExpression = (Expression) vertex.getFirstIsQueryExprOf(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator eval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(boundExpression);
		JValue result = eval.getResult(subgraph);
		// if the query contains a "store as " - clause, there is a
		// "isIdOfInc"-Incidence connected with the Greql2Expression
		IsIdOf storeInc = vertex.getFirstIsIdOf(EdgeDirection.IN);
		if (storeInc != null) {
			VertexEvaluator storeEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(
							storeInc.getAlpha());
			String varName = storeEval.getResult(null).toString();
			boundVariables.put(varName, result);
		}
		return result;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsGreql2Expression(this, graphSize);
	}

}
