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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;
import de.uni_koblenz.jgralab.greql.funlib.FunLib.FunctionInfo;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql.schema.FunctionId;
import de.uni_koblenz.jgralab.greql.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql.schema.IsTypeExprOf;
import de.uni_koblenz.jgralab.greql.schema.TypeId;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;

/**
 * Evaluates a FunctionApplication vertex in the GReQL-2 Syntaxgraph
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class FunctionApplicationEvaluator extends
		VertexEvaluator<FunctionApplication> {

	protected ArrayList<VertexEvaluator<? extends Expression>> parameterEvaluators = null;

	protected int paramEvalCount = 0;

	protected boolean listCreated = false;

	TypeCollection typeArgument = null;

	/**
	 * The name of this function
	 */
	private String functionName = null;

	private FunctionInfo fi = null;

	/**
	 * Returns the name of the GreqlFunction
	 */
	public String getFunctionName() {
		if (functionName == null) {
			FunctionId id = vertex
					.getFirstIsFunctionIdOfIncidence(EdgeDirection.IN)
					.getAlpha();
			functionName = id.get_name();
		}
		return functionName;
	}

	public FunctionInfo getFunctionInfo() {
		if (fi == null) {
			fi = FunLib.getFunctionInfo(getFunctionName());
			if (fi == null) {
				throw new GreqlException("Call to unknown function '"
						+ getFunctionName() + "'");
			}
		}
		return fi;
	}

	public Function getFunction() {
		return getFunctionInfo().getFunction();
	}

	@Override
	public String getLoggingName() {
		return getFunctionName();
	}

	/**
	 * @param eval
	 *            the GreqlEvaluator this VertexEvaluator belongs to
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public FunctionApplicationEvaluator(FunctionApplication vertex,
			GreqlQueryImpl query) {
		super(vertex, query);
	}

	/**
	 * creates the list of parameter evaluators so that it would not be
	 * necessary to build it up each time the function gets evaluated
	 */
	protected ArrayList<VertexEvaluator<? extends Expression>> createVertexEvaluatorList() {
		ArrayList<VertexEvaluator<? extends Expression>> vertexEvalList = new ArrayList<VertexEvaluator<? extends Expression>>();
		IsArgumentOf inc = vertex
				.getFirstIsArgumentOfIncidence(EdgeDirection.IN);
		while (inc != null) {
			Expression currentParameterExpr = inc.getAlpha();
			// maybe the vertex has no evaluator
			VertexEvaluator<? extends Expression> paramEval = query
					.getVertexEvaluator(currentParameterExpr);
			vertexEvalList.add(paramEval);
			inc = inc.getNextIsArgumentOfIncidence(EdgeDirection.IN);
		}
		return vertexEvalList;
	}

	/**
	 * creates the type-argument
	 */
	private TypeCollection createTypeArgument(InternalGreqlEvaluator evaluator) {
		TypeId typeId;
		IsTypeExprOf typeEdge = vertex
				.getFirstIsTypeExprOfIncidence(EdgeDirection.IN);
		TypeCollection typeCollection = null;
		if (typeEdge != null) {
			typeCollection = new TypeCollection();
			while (typeEdge != null) {
				typeId = (TypeId) typeEdge.getAlpha();
				TypeIdEvaluator typeEval = (TypeIdEvaluator) query
						.getVertexEvaluator(typeId);
				typeCollection.addTypes((TypeCollection) typeEval
						.getResult(evaluator));
				typeEdge = typeEdge
						.getNextIsTypeExprOfIncidence(EdgeDirection.IN);
			}
		}
		return typeCollection;
	}

	/**
	 * evaluates the function, calls the right function of the function libary
	 */
	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		FunctionInfo fi = getFunctionInfo();

		if (!listCreated) {
			typeArgument = createTypeArgument(evaluator);
			parameterEvaluators = createVertexEvaluatorList();
			paramEvalCount = parameterEvaluators.size();
			listCreated = true;
		}

		int parameterCount = parameterEvaluators.size();
		if (fi.needsGraphArgument()) {
			parameterCount++;
		}
		if (typeArgument != null) {
			parameterCount++;
		}
		if (fi.needsEvaluatorArgument()) {
			parameterCount++;
		}
		Object[] parameters = new Object[parameterCount];

		int p = 0;

		if (fi.needsEvaluatorArgument()) {
			parameters[p++] = evaluator;
		}

		if (fi.needsGraphArgument()) {
			parameters[p++] = evaluator.getDataGraph();
		}

		for (int i = 0; i < paramEvalCount; i++) {
			parameters[p++] = parameterEvaluators.get(i).getResult(evaluator);
		}

		if (typeArgument != null) {
			parameters[p] = typeArgument;
		}

		return FunLib.apply(fi, parameters);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		FunctionApplication funApp = getVertex();

		IsArgumentOf inc = funApp
				.getFirstIsArgumentOfIncidence(EdgeDirection.IN);
		long argCosts = 0;
		ArrayList<Long> elements = new ArrayList<Long>();
		while (inc != null) {
			VertexEvaluator<? extends Expression> argEval = query
					.getVertexEvaluator(inc.getAlpha());
			argCosts += argEval.getCurrentSubtreeEvaluationCosts();
			elements.add(argEval.getEstimatedCardinality());
			inc = inc.getNextIsArgumentOfIncidence(EdgeDirection.IN);
		}

		Function func = getFunction();
		long ownCosts = func.getEstimatedCosts(elements);
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = iteratedCosts + argCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public double calculateEstimatedSelectivity() {
		Function func = getFunction();
		if (func != null) {
			return func.getSelectivity();
		} else {
			return 1;
		}
	}

	@Override
	public long calculateEstimatedCardinality() {
		FunctionApplication funApp = getVertex();
		IsArgumentOf inc = funApp
				.getFirstIsArgumentOfIncidence(EdgeDirection.IN);
		int elements = 0;
		while (inc != null) {
			VertexEvaluator<? extends Expression> argEval = query
					.getVertexEvaluator(inc.getAlpha());
			elements += argEval.getEstimatedCardinality();
			inc = inc.getNextIsArgumentOfIncidence(EdgeDirection.IN);
		}

		Function func = getFunction();
		if (func != null) {
			return func.getEstimatedCardinality(elements);
		} else {
			return 1;
		}
	}

}
