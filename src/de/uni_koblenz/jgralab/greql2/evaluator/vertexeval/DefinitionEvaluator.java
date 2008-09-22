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

import java.util.HashSet;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.Definition;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsVarOf;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * Evaluates a definition in a Where- or LetExpression.
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> 
 * Summer 2006, Diploma Thesis
 *
 */
public class DefinitionEvaluator extends VertexEvaluator {

	private Definition vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * Creates a new DefinitionExpressionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public DefinitionEvaluator(Definition vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	/**
	 * sets the result of the value expression as value of the variable
	 */
	@Override
	public JValue evaluate() throws EvaluateException {
		Expression definExp = (Expression) vertex.getFirstIsExprOf()
				.getAlpha();
		Variable v = (Variable) vertex.getFirstIsVarOf().getAlpha();
		VertexEvaluator vertexEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(definExp);
		VariableEvaluator variableEval = (VariableEvaluator) greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(v);
		variableEval.setValue(vertexEval.getResult(subgraph));
		return new JValue(true);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateCostsDefinition(
				this, graphSize);
	}
	
	@Override
	public void calculateNeededAndDefinedVariables() {
		neededVariables = new HashSet<Variable>();
		definedVariables = new HashSet<Variable>();
		IsVarOf varInc = vertex.getFirstIsVarOf(EdgeDirection.IN);
		if (varInc != null) {
			definedVariables.add( (Variable) varInc.getAlpha());
			varInc = varInc.getNextIsVarOf(EdgeDirection.IN);
		}
		IsExprOf valueInc = vertex.getFirstIsExprOf(EdgeDirection.IN);
		if (valueInc != null) {
			VertexEvaluator veval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(valueInc.getAlpha());
			if (veval != null) {
				neededVariables.addAll(veval.getNeededVariables());
			}	
			neededVariables.removeAll(definedVariables);
		}
	}

}
