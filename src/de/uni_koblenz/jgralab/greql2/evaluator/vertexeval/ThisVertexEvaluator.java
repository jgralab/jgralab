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
import de.uni_koblenz.jgralab.greql2.schema.ThisVertex;


/**
 * Evaluates a Variable vertex in the GReQL-2 Syntaxgraph. Provides access to
 * the variable value using the method getResult(..), because it should make no
 * difference for other VertexEvaluators, if a vertex is root of a complex
 * subgraph or a variable. Also provides a method to set the variable value.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ThisVertexEvaluator extends VariableEvaluator {
//
//	private long estimatedAssignments = Long.MIN_VALUE;
//	
//	/**
//	 * The variable this VariableEvaluator "evaluates"
//	 */
//	protected ThisVertex vertex;
//	
//	/**
//	 * This is the current value of this evaluator
//	 */
//	protected JValue thisVertexValue;
//
//	/**
//	 * returns the vertex this VertexEvaluator evaluates
//	 */
//	@Override
//	public Vertex getVertex() {
//		return vertex;
//	}
//
//	
//	public void setThisVertex(JValue thisVertex) {
//		thisVertexValue = thisVertex;
//	}
	
	/**
	 * @param eval
	 *            the GreqlEvaluator this VertexEvaluator belongs to
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public ThisVertexEvaluator(ThisVertex vertex, GreqlEvaluator eval) {
		super(vertex, eval);
	//	this.vertex = vertex;
	}

//	@Override
//	public JValue evaluate() throws EvaluateException {
//		return thisVertexValue;
//	}
//
//	@Override
//	public JValue getResult(BooleanGraphMarker subgraphMarker)
//			throws EvaluateException {
//		return thisVertexValue;
//	}
//
//	@Override
//	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
//	//	return this.greqlEvaluator.getCostModel().calculateCostsVariable(this,
//	//			graphSize);
//			return new VertexCosts(1, 1, 1);
//	}
//
//	@Override
//	public Set<Variable> getNeededVariables() {
//		if (neededVariables == null) {
//			neededVariables = new HashSet<Variable>();
//			neededVariables.add(vertex);
//		}
//		return neededVariables;
//	}
//
//	@Override
//	public Set<Variable> getDefinedVariables() {
//		if (definedVariables == null) {
//			definedVariables = new HashSet<Variable>();
//		}
//		return definedVariables;
//	}
//
//	@Override
//	public void calculateNeededAndDefinedVariables() {
//		// for this literals, this method is not used
//	}
//
//	/**
//	 * @return the estimated number of possible different values this variable
//	 *         may get during evaluation
//	 */
//	@Override
//	public long getVariableCombinations(GraphSize graphSize) {
//		if (estimatedAssignments == Long.MIN_VALUE) {
//			estimatedAssignments = calculateEstimatedAssignments(graphSize);
//		}
//		return estimatedAssignments;
//	}
//
//	/**
//	 * calculated the estimated number of possible different values this
//	 * variable may get during evaluation
//	 */
//	public long calculateEstimatedAssignments(GraphSize graphSize) {
//		//return this.greqlEvaluator.getCostModel().calculateVariableAssignments(
//		//		this, graphSize);
//		return 1;
//	}

}
