/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.funlib.ReachableVertices;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ForwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;

/**
 * Evaluates a ForwardVertexSet
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ForwardVertexSetEvaluator extends PathSearchEvaluator {

	private ForwardVertexSet vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	public ForwardVertexSetEvaluator(ForwardVertexSet vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	private boolean initialized = false;
	
	private VertexEvaluator startEval = null;

	private final void initialize() {
		PathDescription p = (PathDescription) vertex.getFirstIsPathOf(EdgeDirection.IN).getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(p);
		
		Expression startExpression = (Expression) vertex.getFirstIsStartExprOf(
				EdgeDirection.IN).getAlpha();
		startEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(startExpression);
		searchAutomaton = new DFA(pathDescEval.getNFA());

			
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
		Vertex startVertex = null;
		try {
			startVertex = startEval.getResult(subgraph).toVertex();
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException(
					"Error evaluation ForwardVertexSet, StartExpression doesn't evaluate to a vertex",
					exception);
		}
		return ReachableVertices.search(startVertex, searchAutomaton, subgraph);
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
