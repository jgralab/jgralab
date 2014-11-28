/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;
import de.uni_koblenz.jgralab.greql.funlib.FunLib.FunctionInfo;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.PathDescription;
import de.uni_koblenz.jgralab.greql.schema.PathExistence;

/**
 * Evaluates a path existence, that's the question if there is a path of a
 * specific regular form form startVertex to targetVertex
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class PathExistenceEvaluator extends PathSearchEvaluator<PathExistence> {

	private FunctionInfo fi;

	public PathExistenceEvaluator(PathExistence vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		PathDescription p = (PathDescription) vertex.getFirstIsPathOfIncidence(
				EdgeDirection.IN).getAlpha();
		PathDescriptionEvaluator<?> pathDescEval = (PathDescriptionEvaluator<?>) query
				.getVertexEvaluator(p);
		Expression startExpression = vertex.getFirstIsStartExprOfIncidence(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator<? extends Expression> startEval = query
				.getVertexEvaluator(startExpression);
		Object res = startEval.getResult(evaluator);
		/**
		 * check if the result is invalid, this may occur because the
		 * restrictedExpression may return a null-value
		 */
		if (res == null) {
			return null;
		}
		Vertex startVertex = (Vertex) res;

		Expression targetExpression = vertex.getFirstIsTargetExprOfIncidence(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator<? extends Expression> targetEval = query
				.getVertexEvaluator(targetExpression);
		Vertex targetVertex = null;
		res = targetEval.getResult(evaluator);
		if (res == null) {
			return null;
		}
		targetVertex = (Vertex) res;

		DFA searchAutomaton = (DFA) evaluator.getLocalAutomaton(vertex);
		if (searchAutomaton == null) {
			searchAutomaton = pathDescEval.getNFA(evaluator).getDFA();
			// searchAutomaton.printAscii();
			evaluator.setLocalAutomaton(vertex, searchAutomaton);
		}
		Object[] arguments = new Object[4];
		arguments[0] = evaluator;
		arguments[1] = startVertex;
		arguments[2] = targetVertex;
		arguments[3] = searchAutomaton;
		if (fi == null) {
			fi = FunLib.getFunctionInfo("isReachable");
		}
		return FunLib.apply(fi, arguments);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		PathExistence existence = getVertex();
		Expression startExpression = existence.getFirstIsStartExprOfIncidence()
				.getAlpha();
		VertexEvaluator<? extends Expression> vertexEval = query
				.getVertexEvaluator(startExpression);
		long startCosts = vertexEval.getCurrentSubtreeEvaluationCosts();
		Expression targetExpression = existence
				.getFirstIsTargetExprOfIncidence().getAlpha();
		vertexEval = query.getVertexEvaluator(targetExpression);
		long targetCosts = vertexEval.getCurrentSubtreeEvaluationCosts();
		PathDescription p = (PathDescription) existence
				.getFirstIsPathOfIncidence().getAlpha();
		PathDescriptionEvaluator<? extends PathDescription> pathDescEval = (PathDescriptionEvaluator<? extends PathDescription>) query
				.getVertexEvaluator(p);
		long pathDescCosts = pathDescEval.getCurrentSubtreeEvaluationCosts();
		long searchCosts = Math.round(((pathDescCosts * searchFactor) / 2.0)
				* Math.sqrt(query.getOptimizer().getOptimizerInfo()
						.getAverageEdgeCount()));
		long ownCosts = searchCosts;
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = targetCosts + pathDescCosts + iteratedCosts
				+ startCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public double calculateEstimatedSelectivity() {
		return 0.1;
	}

}
