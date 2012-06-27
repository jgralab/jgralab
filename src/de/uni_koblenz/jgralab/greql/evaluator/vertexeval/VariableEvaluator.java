/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

package de.uni_koblenz.jgralab.greql.evaluator.vertexeval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.schema.Declaration;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.Greql2Aggregation;
import de.uni_koblenz.jgralab.greql.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql.schema.Variable;

/**
 * Evaluates a Variable vertex in the GReQL-2 Syntaxgraph. Provides access to
 * the variable value using the method getResult(..), because it should make no
 * difference for other VertexEvaluators, if a vertex is root of a complex
 * subgraph or a variable. Also provides a method to set the variable value.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class VariableEvaluator<V extends Variable> extends VertexEvaluator<V> {

	private List<VertexEvaluator<? extends Expression>> dependingExpressions;

	/**
	 * This is the estimated cardinality of the definitionset of this variable
	 */
	private long estimatedAssignments = Long.MIN_VALUE;

	/**
	 * Sets the given value as "result" of this variable, so it can be uses via
	 * the getResult(graph) method
	 * 
	 * @param variableValue
	 * @param evaluator
	 */
	public void setValue(Object variableValue, InternalGreqlEvaluator evaluator) {
		if (dependingExpressions == null) {
			dependingExpressions = calculateDependingExpressions();
		}

		int size = dependingExpressions.size();
		for (int i = 0; i < size; i++) {
			dependingExpressions.get(i).clear(evaluator);
		}
		evaluator.setLocalEvaluationResult(vertex, variableValue);
	}

	/**
	 * returns the variableValue
	 */
	public Object getValue(InternalGreqlEvaluator evaluator) {
		return evaluator.getLocalEvaluationResult(vertex);
	}

	/**
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public VariableEvaluator(V vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		return getValue(evaluator);
	}

	@Override
	public Object getResult(InternalGreqlEvaluator evaluator) {
		return getValue(evaluator);
	}

	@Override
	public Set<Variable> getNeededVariables() {
		if (neededVariables == null) {
			neededVariables = new HashSet<Variable>();
			neededVariables.add(vertex);
		}
		return neededVariables;
	}

	@Override
	public Set<Variable> getDefinedVariables() {
		if (definedVariables == null) {
			definedVariables = new HashSet<Variable>();
		}
		return definedVariables;
	}

	@SuppressWarnings("unchecked")
	public List<VertexEvaluator<? extends Expression>> calculateDependingExpressions() {
		Queue<Greql2Vertex> queue = new LinkedList<Greql2Vertex>();
		List<VertexEvaluator<? extends Expression>> dependingEvaluators = new ArrayList<VertexEvaluator<? extends Expression>>();
		List<Vertex> forbiddenVertices = new ArrayList<Vertex>();
		SimpleDeclaration simpleDecl = null;
		if (vertex.getFirstIsDeclaredVarOfIncidence(EdgeDirection.OUT) != null) {
			simpleDecl = (SimpleDeclaration) vertex
					.getFirstIsDeclaredVarOfIncidence(EdgeDirection.OUT)
					.getThat();
		}
		if (simpleDecl != null) {
			forbiddenVertices.add(simpleDecl);
			Declaration declaringVertex = (Declaration) simpleDecl
					.getFirstIsSimpleDeclOfIncidence().getThat();
			if (declaringVertex
					.getFirstIsCompDeclOfIncidence(EdgeDirection.OUT) != null) {
				forbiddenVertices.add(declaringVertex
						.getFirstIsCompDeclOfIncidence(EdgeDirection.OUT)
						.getThat());
			} else {
				forbiddenVertices.add(declaringVertex
						.getFirstIsQuantifiedDeclOfIncidence(EdgeDirection.OUT)
						.getThat());
			}
		}

		queue.add(vertex);
		while (!queue.isEmpty()) {
			Greql2Vertex currentVertex = queue.poll();
			VertexEvaluator<?> eval = query.getVertexEvaluator(currentVertex);

			if ((eval != null) && (!dependingEvaluators.contains(eval))
					&& (!(eval instanceof PathDescriptionEvaluator))
					&& (!(eval instanceof DeclarationEvaluator))
					&& (!(eval instanceof SimpleDeclarationEvaluator))) {
				dependingEvaluators
						.add((VertexEvaluator<? extends Expression>) eval);
			}
			Greql2Aggregation currentEdge = currentVertex
					.getFirstGreql2AggregationIncidence(EdgeDirection.OUT);
			while (currentEdge != null) {
				Greql2Vertex nextVertex = (Greql2Vertex) currentEdge.getThat();
				if (!forbiddenVertices.contains(nextVertex)) {
					// if (!(nextVertex instanceof SimpleDeclaration)) {
					queue.add(nextVertex);
				}
				currentEdge = currentEdge
						.getNextGreql2AggregationIncidence(EdgeDirection.OUT);
			}
		}
		return dependingEvaluators;
	}

	@Override
	public void calculateNeededAndDefinedVariables() {
		// for variables, this method is not used
	}

	/**
	 * @return the estimated number of possible different values this variable
	 *         may get during evaluation
	 */
	@Override
	public long getVariableCombinations() {
		if (estimatedAssignments == Long.MIN_VALUE) {
			estimatedAssignments = calculateEstimatedAssignments();
		}
		return estimatedAssignments;
	}

	/**
	 * calculated the estimated number of possible different values this
	 * variable may get during evaluation
	 */
	public long calculateEstimatedAssignments() {
		Variable v = getVertex();
		IsDeclaredVarOf inc = v.getFirstIsDeclaredVarOfIncidence();
		if (inc != null) {
			SimpleDeclaration decl = (SimpleDeclaration) inc.getOmega();
			VertexEvaluator<? extends Expression> typeExpEval = query
					.getVertexEvaluator((Expression) decl
							.getFirstIsTypeExprOfIncidence().getAlpha());
			return typeExpEval.getEstimatedCardinality();
		} else {
			// if there exists no "isDeclaredVarOf"-Edge the variable is not
			// declared but defined, so there exists only 1 possible assignment
			return 1;
		}
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		return new VertexCosts(1, 1, 1);
	}

}
