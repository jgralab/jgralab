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

import java.util.ArrayList;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.exception.QuerySourceException;
import de.uni_koblenz.jgralab.greql2.exception.UndefinedFunctionException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeExprOf;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;

/**
 * Evaluates a FunctionApplication vertex in the GReQL-2 Syntaxgraph
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class FunctionApplicationEvaluator extends VertexEvaluator {

	private FunctionApplication vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	private ArrayList<VertexEvaluator> parameterEvaluators = null;

	private JValueTypeCollection typeArgument = null;

	private JValue[] parameters = null;

	private int paramEvalCount = 0;

	boolean listCreated = false;

	/**
	 * The name of this function
	 */
	private String functionName = null;

	/**
	 * Returns the name of the Greql2Function
	 */
	public String getFunctionName() {
		return functionName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator#
	 * getLoggingName()
	 */
	@Override
	public String getLoggingName() {
		if (functionName == null) {
			FunctionId id = (FunctionId) vertex.getFirstIsFunctionIdOfIncidence(
					EdgeDirection.IN).getAlpha();
			functionName = id.get_name();
		}
		return functionName;
	}

	/**
	 * Reference to the function of the GReQL function library
	 */
	private Greql2Function greql2Function = null;

	public final Greql2Function getGreql2Function() {
		if (greql2Function == null) {
			if (functionName == null) {
				FunctionId id = (FunctionId) vertex.getFirstIsFunctionIdOfIncidence(
						EdgeDirection.IN).getAlpha();
				functionName = id.get_name();
			}
			greql2Function = Greql2FunctionLibrary.instance().getGreqlFunction(
					functionName);
			if (greql2Function == null) {
				throw new UndefinedFunctionException(vertex, functionName,
						createPossibleSourcePositions());
			}
		}
		return greql2Function;
	}

	/**
	 * @param eval
	 *            the GreqlEvaluator this VertexEvaluator belongs to
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public FunctionApplicationEvaluator(FunctionApplication vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	/**
	 * creates the list of parameter evaluators so that it would not be
	 * necessary to build it up each time the function gets evaluated
	 */
	private ArrayList<VertexEvaluator> createVertexEvaluatorList() {
		ArrayList<VertexEvaluator> vertexEvalList = new ArrayList<VertexEvaluator>();
		IsArgumentOf inc = vertex.getFirstIsArgumentOfIncidence(EdgeDirection.IN);
		while (inc != null) {
			Expression currentParameterExpr = (Expression) inc.getAlpha();
			// maybe the vertex has no evaluator
			VertexEvaluator paramEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(
							currentParameterExpr);
			vertexEvalList.add(paramEval);
			inc = inc.getNextIsArgumentOf(EdgeDirection.IN);
		}
		return vertexEvalList;
	}

	/**
	 * creates the type-argument
	 */
	private JValueTypeCollection createTypeArgument() throws EvaluateException {
		TypeId typeId;
		IsTypeExprOf typeEdge = vertex.getFirstIsTypeExprOfIncidence(EdgeDirection.IN);
		JValueTypeCollection typeCollection = null;
		if (typeEdge != null) {
			typeCollection = new JValueTypeCollection();
			while (typeEdge != null) {
				typeId = (TypeId) typeEdge.getAlpha();
				TypeIdEvaluator typeEval = (TypeIdEvaluator) greqlEvaluator
						.getVertexEvaluatorGraphMarker().getMark(typeId);
				try {
					typeCollection.addTypes(typeEval.getResult(subgraph)
							.toJValueTypeCollection());
				} catch (JValueInvalidTypeException ex) {
					throw new EvaluateException(
							"Result of TypeId-vertex was not JValueTypeCollection ");
				}
				typeEdge = typeEdge.getNextIsTypeExprOf(EdgeDirection.IN);
			}
		}
		return typeCollection;
	}

	/**
	 * evaluates the function, calls the right function of the function libary
	 */
	@Override
	public JValue evaluate() throws EvaluateException {
		if (!listCreated) {
			typeArgument = createTypeArgument();
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
			getGreql2Function();
			listCreated = true;
		}

		for (int i = 0; i < paramEvalCount; i++) {
			parameters[i] = parameterEvaluators.get(i).getResult(subgraph);
		}

		try {
			result = greql2Function.evaluate(graph, subgraph, parameters);
		} catch (EvaluateException ex) {
			throw new QuerySourceException(ex.getMessage(), vertex,
					createPossibleSourcePositions(), ex);
		}
		return result;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsFunctionApplication(this, graphSize);
	}

	@Override
	public double calculateEstimatedSelectivity(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateSelectivityFunctionApplication(this, graphSize);
	}

	@Override
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCardinalityFunctionApplication(this, graphSize);
	}

}
