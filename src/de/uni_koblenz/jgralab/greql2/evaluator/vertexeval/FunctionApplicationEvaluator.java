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

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
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
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeExprOf;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;

/**
 * Evaluates a FunctionApplication vertex in the GReQL-2 Syntaxgraph
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class FunctionApplicationEvaluator extends VertexEvaluator {

	private FunctionApplication vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	private ArrayList<VertexEvaluator> parameterEvaluators = null;

	private boolean firstEvaluation = true;

	private JValueTypeCollection typeArgument;

	private JValue[] parameters;

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
	 * @see de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator#getLoggingName()
	 */
	@Override
	public String getLoggingName() {
		if (functionName == null) {
			FunctionId id = (FunctionId) vertex.getFirstIsFunctionIdOf(
					EdgeDirection.IN).getAlpha();
			functionName = id.getName();
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
				FunctionId id = (FunctionId) vertex.getFirstIsFunctionIdOf(
						EdgeDirection.IN).getAlpha();
				functionName = id.getName();
			}
			greql2Function = Greql2FunctionLibrary.instance().getGreqlFunction(
					functionName);
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
		IsArgumentOf inc = vertex.getFirstIsArgumentOf(EdgeDirection.IN);
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
		IsTypeExprOf typeEdge = vertex.getFirstIsTypeExprOf(EdgeDirection.IN);
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
		if (firstEvaluation) {
			firstEvaluation = false;
			typeArgument = createTypeArgument();
			parameterEvaluators = createVertexEvaluatorList();
			int parameterCount = parameterEvaluators.size();
			if (typeArgument != null)
				parameterCount++;
			parameters = new JValue[parameterCount];
			if (typeArgument != null)
				parameters[parameterCount - 1] = typeArgument;
			Greql2Function func = getGreql2Function();
			if (func == null)
				throw new UndefinedFunctionException(functionName,
						createPossibleSourcePositions());
		}

		for (int i = 0; i < parameterEvaluators.size(); i++) {
			parameters[i] = parameterEvaluators.get(i).getResult(subgraph);
		}

		try {
			result = greql2Function.evaluate(graph, subgraph, parameters);
		} catch (QuerySourceException ex) {
			List<SourcePosition> positionList = ex.getSourcePositions();
			positionList.addAll(createPossibleSourcePositions());
			throw ex;
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
