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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.BackwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;

/**
 * evaluates a BackwardVertexSet
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class BackwardVertexSetEvaluator extends PathSearchEvaluator {

	BackwardVertexSet vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	public Vertex getVertex() {
		return vertex;
	}

	public BackwardVertexSetEvaluator(BackwardVertexSet vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		PathDescription p = (PathDescription) vertex.getFirstIsPathOf(
				EdgeDirection.IN).getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(p);
		Expression targetExpression = (Expression) vertex
				.getFirstIsTargetExprOf(EdgeDirection.IN).getAlpha();
		VertexEvaluator targetEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(targetExpression);
		JValue res = targetEval.getResult(subgraph);
		/**
		 * check if the result is invalid, this may occur because the
		 * restrictedExpression may return a null-value
		 */
		if (!res.isValid())
			return new JValue();
		Vertex targetVertex = null;
		try {
			targetVertex = res.toVertex();
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException(
					"Error evaluation BackwardVertexSet, TargetExpression doesn't evaluate to a vertex",
					exception);
		}
		if (targetVertex == null) {
			return new JValue();
		}
		if (searchAutomaton == null) {
			NFA revertedNFA = NFA.revertNFA(pathDescEval.getNFA());
			searchAutomaton = new DFA(revertedNFA);
			// We log the number of states as the result size of the underlying
			// PathDescription.
			if (evaluationLogger != null) {
				evaluationLogger.logResultSize("PathDescription",
						searchAutomaton.stateList.size());
			}
		}
		if (function == null) {
			function = Greql2FunctionLibrary.instance().getGreqlFunction(
					"reachableVertices");
		}
		JValue[] arguments = new JValue[4];
		arguments[0] = new JValue(targetVertex);
		arguments[1] = new JValue(searchAutomaton);
		arguments[2] = new JValue(subgraph);

		return function.evaluate(graph, subgraph, arguments);
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
