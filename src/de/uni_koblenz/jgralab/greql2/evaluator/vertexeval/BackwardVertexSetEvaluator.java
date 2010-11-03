/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.funlib.ReachableVertices;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.BackwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;

/**
 * evaluates a BackwardVertexSet
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class BackwardVertexSetEvaluator extends PathSearchEvaluator {

	BackwardVertexSet vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	public BackwardVertexSetEvaluator(BackwardVertexSet vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	private boolean initialized = false;

	private VertexEvaluator targetEval = null;

	private final void initialize() {
		PathDescription p = (PathDescription) vertex.getFirstIsPathOf(
				EdgeDirection.IN).getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(p);

		Expression targetExpression = (Expression) vertex
				.getFirstIsTargetExprOf(EdgeDirection.IN).getAlpha();
		targetEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(
				targetExpression);
		NFA revertedNFA = NFA.revertNFA(pathDescEval.getNFA());
		searchAutomaton = new DFA(revertedNFA);

		// We log the number of states as the result size of the underlying
		// PathDescription.
		if (evaluationLogger != null) {
			evaluationLogger.logResultSize("PathDescription",
					searchAutomaton.stateList.size());
		}
		initialized = true;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		if (!initialized) {
			initialize();
		}
		Vertex targetVertex = null;
		try {
			targetVertex = targetEval.getResult(subgraph).toVertex();
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException(
					"Error evaluation BackwardVertexSet, TargetExpression doesn't evaluate to a vertex",
					exception);
		}
		return ReachableVertices
				.search(targetVertex, searchAutomaton, subgraph);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return greqlEvaluator.getCostModel().calculateCostsBackwardVertexSet(
				this, graphSize);
	}

	@Override
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCardinalityBackwardVertexSet(this, graphSize);
	}

}
