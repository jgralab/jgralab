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

import java.util.HashSet;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VariableDeclaration;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql.schema.IsTypeExprOf;
import de.uni_koblenz.jgralab.greql.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql.schema.Variable;

/**
 * Evaluates a simple declaration. Creates a VariableDeclaration-object, that
 * provides methods to iterate over all possible values.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class SimpleDeclarationEvaluator extends
		VertexEvaluator<SimpleDeclaration> {

	/**
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public SimpleDeclarationEvaluator(SimpleDeclaration vertex,
			GreqlQueryImpl query) {
		super(vertex, query);
	}

	/**
	 * returns a JValueList of VariableDeclaration objects
	 */
	@Override
	public PVector<VariableDeclaration> evaluate(
			InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		IsTypeExprOf inc = vertex
				.getFirstIsTypeExprOfIncidence(EdgeDirection.IN);
		Expression typeExpression = inc.getAlpha();
		VertexEvaluator<? extends Expression> exprEval = query
				.getVertexEvaluator(typeExpression);
		PVector<VariableDeclaration> varDeclList = JGraLab.vector();
		IsDeclaredVarOf varInc = vertex
				.getFirstIsDeclaredVarOfIncidence(EdgeDirection.IN);
		while (varInc != null) {
			VariableDeclaration varDecl = new VariableDeclaration(
					varInc.getAlpha(), exprEval,
					(VariableEvaluator<Variable>) query
							.getVertexEvaluator(varInc.getAlpha()));
			varDeclList = varDeclList.plus(varDecl);
			varInc = varInc.getNextIsDeclaredVarOfIncidence(EdgeDirection.IN);
		}
		return varDeclList;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		SimpleDeclaration simpleDecl = getVertex();

		// Calculate the costs for the type definition
		VertexEvaluator<? extends Expression> typeExprEval = query
				.getVertexEvaluator(simpleDecl.getFirstIsTypeExprOfIncidence()
						.getAlpha());

		long typeCosts = typeExprEval.getCurrentSubtreeEvaluationCosts();

		// Calculate the costs for the declared variables
		long declaredVarCosts = 0;
		IsDeclaredVarOf inc = simpleDecl
				.getFirstIsDeclaredVarOfIncidence(EdgeDirection.IN);
		while (inc != null) {
			VariableEvaluator<? extends Variable> varEval = (VariableEvaluator<? extends Variable>) query
					.getVertexEvaluator(inc.getAlpha());
			declaredVarCosts += varEval.getCurrentSubtreeEvaluationCosts();
			inc = inc.getNextIsDeclaredVarOfIncidence(EdgeDirection.IN);
		}

		long ownCosts = 2;
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = iteratedCosts + declaredVarCosts + typeCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public void calculateNeededAndDefinedVariables() {
		neededVariables = new HashSet<>();
		definedVariables = new HashSet<>();
		IsDeclaredVarOf varInc = vertex
				.getFirstIsDeclaredVarOfIncidence(EdgeDirection.IN);
		while (varInc != null) {
			definedVariables.add(varInc.getAlpha());
			varInc = varInc.getNextIsDeclaredVarOfIncidence(EdgeDirection.IN);
		}
		IsTypeExprOf typeInc = vertex
				.getFirstIsTypeExprOfIncidence(EdgeDirection.IN);
		if (typeInc != null) {
			VertexEvaluator<? extends Expression> veval = query
					.getVertexEvaluator(typeInc.getAlpha());
			if (veval != null) {
				neededVariables.addAll(veval.getNeededVariables());
			}
		}
	}

	@Override
	public long calculateEstimatedCardinality() {
		SimpleDeclaration decl = getVertex();
		VertexEvaluator<? extends Expression> typeExprEval = query
				.getVertexEvaluator(decl.getFirstIsTypeExprOfIncidence(
						EdgeDirection.IN).getAlpha());
		long singleCardinality = typeExprEval.getEstimatedCardinality();
		long wholeCardinality = singleCardinality
				* getDefinedVariables().size();
		return wholeCardinality;
	}

}
