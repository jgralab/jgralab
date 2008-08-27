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

import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.schema.*;
import de.uni_koblenz.jgralab.greql2.evaluator.*;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.funlib.*;
import de.uni_koblenz.jgralab.greql2.jvalue.*;

/**
 * Evaluates a ForwardVertexSet
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class ForwardVertexSetEvaluator extends PathSearchEvaluator {

	private ForwardVertexSet vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	public ForwardVertexSetEvaluator(ForwardVertexSet vertex,
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
		Expression startExpression = (Expression) vertex.getFirstIsStartExprOf(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator startEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(startExpression);
		JValue res = startEval.getResult(subgraph);
		/**
		 * check if the result is invalid, this may occur because the
		 * restrictedExpression may return a null-value
		 */
		if (!res.isValid())
			return new JValue();
		Vertex startVertex = null;
		try {
			startVertex = res.toVertex();
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException(
					"Error evaluation ForwardVertexSet, StartExpression doesn't evaluate to a vertex",
					exception);
		}
		if (startVertex == null)
			return new JValue();
		if (searchAutomaton == null) {
			searchAutomaton = new DFA(pathDescEval.getNFA());
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
		arguments[0] = new JValue(startVertex);
		arguments[1] = new JValue(searchAutomaton);
		arguments[2] = new JValue(subgraph);

		return function.evaluate(graph, subgraph, arguments);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsForwardVertexSet(this, graphSize);
	}

	@Override
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCardinalityForwardVertexSet(this, graphSize);
	}

}
