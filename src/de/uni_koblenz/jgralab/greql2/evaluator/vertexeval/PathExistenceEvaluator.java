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
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.PathExistence;

/**
 * Evaluates a path existence, that's the question if there is a path of a
 * specific regular form form startVertex to targetVertex
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class PathExistenceEvaluator extends PathSearchEvaluator {

	/**
	 * this is the PathExistence vertex in the GReQL Syntaxgraph this evaluator
	 * evaluates
	 */
	private PathExistence vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	public PathExistenceEvaluator(PathExistence vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		PathDescription p = (PathDescription) vertex.getFirstIsPathOf(
				EdgeDirection.IN).getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(p);
		Expression startExpression = (Expression) vertex.getFirstIsStartExprOf(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator startEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(startExpression);
		JValue res = startEval.getResult(subgraph);
		/**
		 * check if the result is invalid, this may occur because the
		 * restrictedExpression may return a null-value
		 */
		if (!res.isValid()) {
			return new JValueImpl();
		}
		Vertex startVertex = null;
		try {
			startVertex = res.toVertex();
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException(
					"Error evaluation ForwardVertexSet, StartExpression doesn't evaluate to a vertex",
					exception);
		}
		if (startVertex == null) {
			return new JValueImpl();
		}
		Expression targetExpression = (Expression) vertex
				.getFirstIsTargetExprOf(EdgeDirection.IN).getAlpha();
		VertexEvaluator targetEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(targetExpression);
		Vertex targetVertex = null;
		res = targetEval.getResult(subgraph);
		if (!res.isValid()) {
			return new JValueImpl();
		}
		try {
			targetVertex = res.toVertex();
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException(
					"Error evaluation ForwardVertexSet, TargetExpression doesn't evaluate to a vertex",
					exception);
		}
		if (targetVertex == null) {
			return new JValueImpl();
		}
		if (searchAutomaton == null) {
			searchAutomaton = pathDescEval.getNFA().getDFA();
			// searchAutomaton.printAscii();
			// We log the number of states as the result size of the underlying
			// PathDescription.
			if (evaluationLogger != null) {
				evaluationLogger.logResultSize("PathDescription",
						searchAutomaton.stateList.size());
			}
		}
		if (function == null) {
			function = Greql2FunctionLibrary.instance().getGreqlFunction(
					"isReachable");
		}
		JValueImpl[] arguments = new JValueImpl[3];
		arguments[0] = new JValueImpl(startVertex);
		arguments[1] = new JValueImpl(targetVertex);
		arguments[2] = new JValueImpl(searchAutomaton);

		JValue tempResult = function.evaluate(graph, subgraph, arguments);
		return tempResult;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateCostsPathExistence(
				this, graphSize);
	}

	@Override
	public double calculateEstimatedSelectivity(GraphSize graphSize) {
		return greqlEvaluator.getCostModel().calculateSelectivityPathExistence(
				this, graphSize);
	}

}
