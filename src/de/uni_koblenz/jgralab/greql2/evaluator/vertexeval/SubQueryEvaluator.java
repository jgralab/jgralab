/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

public class SubQueryEvaluator extends FunctionApplicationEvaluator {
	private Greql2 subQuery;

	protected GraphMarker<VertexEvaluator> vertexEvalGraphMarker;

	private String subQueryName;
	private Greql2ExpressionEvaluator subQueryVertexEval;

	public SubQueryEvaluator(FunctionApplication vertex, GreqlEvaluator eval) {
		super(vertex, eval);
		subQueryName = vertex.get_functionId().get_name();
		subQuery = greqlEvaluator.getSubQuery(subQueryName);
		if (subQuery == null) {
			throw new GraphException("No subquery with name '" + subQueryName
					+ "'.");
		}
	}

	public void createVertexEvaluators() throws EvaluateException {
		vertexEvalGraphMarker = new GraphMarker<VertexEvaluator>(subQuery);
		Vertex currentVertex = subQuery.getFirstVertex();
		while (currentVertex != null) {
			VertexEvaluator vertexEval = VertexEvaluator.createVertexEvaluator(
					currentVertex, greqlEvaluator);
			if (vertexEval != null) {
				vertexEval.setVertexEvalMarker(vertexEvalGraphMarker);
				vertexEvalGraphMarker.mark(currentVertex, vertexEval);
			}
			currentVertex = currentVertex.getNextVertex();
		}
		subQueryVertexEval = (Greql2ExpressionEvaluator) vertexEvalGraphMarker
				.getMark(subQuery.getFirstGreql2Expression());
	}

	@Override
	public String getLoggingName() {
		return subQueryName;
	}

	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		createVertexEvaluators();
		if (!listCreated) {
			parameterEvaluators = createVertexEvaluatorList();
			int parameterCount = parameterEvaluators.size();
			if (typeArgument != null) {
				parameterCount++;
			}
			parameters = new JValue[parameterCount];
			if (typeArgument != null) {
				parameters[parameterCount - 1] = typeArgument;
			}
			paramEvalCount = parameterEvaluators.size();
			listCreated = true;
		}

		for (int i = 0; i < paramEvalCount; i++) {
			parameters[i] = parameterEvaluators.get(i).getResult(subgraph);
		}

		int i = 0;
		Greql2Expression root = subQuery.getFirstGreql2Expression();

		if (root.getDegree(IsBoundVarOf.class) != paramEvalCount) {
			throw new EvaluateException("Given " + paramEvalCount
					+ " args to a subquery expecting "
					+ root.getDegree(IsBoundVarOf.class) + " arguments.");
		}

		Map<String, JValue> boundVariables = new HashMap<String, JValue>();
		for (IsBoundVarOf ibv : root.getIsBoundVarOfIncidences()) {
			Variable var = (Variable) ibv.getAlpha();
			boundVariables.put(var.get_name(), parameters[i]);
			i++;
		}
		subQueryVertexEval.setBoundVariables(boundVariables);

		result = subQueryVertexEval.getResult(subgraph);
		return result;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return subQueryVertexEval.calculateSubtreeEvaluationCosts(graphSize);
	}

}
