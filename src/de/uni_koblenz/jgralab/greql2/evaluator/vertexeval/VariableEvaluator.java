/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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
import java.util.Set;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.*;
import de.uni_koblenz.jgralab.greql2.jvalue.*;
import de.uni_koblenz.jgralab.greql2.schema.*;
import de.uni_koblenz.jgralab.*;

/**
 * Evaluates a Variable vertex in the GReQL-2 Syntaxgraph. Provides access to
 * the variable value using the method getResult(..), because it should make no
 * difference for other VertexEvaluators, if a vertex is root of a complex
 * subgraph or a variable. Also provides a method to set the variable value.
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class VariableEvaluator extends VertexEvaluator {

	/**
	 * The variable this VariableEvaluator "evaluates"
	 */
	private Variable vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * This is the value that has been set from outside
	 */
	private JValue variableValue;

	/**
	 * This is the estimated cardinality of the definitionset of this variable
	 */
	private int estimatedAssignments = -1;

	/**
	 * Sets the given value as "result" of this variable, so it can be uses via
	 * the getResult() method
	 * 
	 * @param value
	 */
	public void setValue(JValue value) {
		variableValue = value;
	}

	/**
	 * returns the variableValue
	 */
	public JValue getValue() {
		return variableValue;
	}

	/**
	 * @param eval
	 *            the GreqlEvaluator this VertexEvaluator belongs to
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public VariableEvaluator(Variable vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		return variableValue;
	}

	@Override
	public JValue getResult(BooleanGraphMarker subgraphMarker)
			throws EvaluateException {
			return variableValue;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateCostsVariable(this,
				graphSize);
	}

	@Override
	public Set<Variable> getNeededVariables() {
		if (neededVariables == null) {
			neededVariables = new HashSet<Variable>();
			neededVariables.add(vertex);
		}
		return neededVariables;
	}

	/**
	 * @return the estimated number of possible different values this variable
	 *         may get during evaluation
	 */
	@Override
	public int getVariableCombinations(GraphSize graphSize) {
		if (estimatedAssignments == -1) {
			estimatedAssignments = calculateEstimatedAssignments(graphSize);
		}
		return estimatedAssignments;
	}

	/**
	 * calculated the estimated number of possible different values this variable may
	 * get during evlauation
	 */
	public int calculateEstimatedAssignments(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateVariableAssignments(this, graphSize);
	}

}
