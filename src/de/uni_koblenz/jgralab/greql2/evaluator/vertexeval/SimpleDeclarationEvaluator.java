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

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.Query;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeExprOf;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;

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
	public SimpleDeclarationEvaluator(SimpleDeclaration vertex, Query query) {
		super(vertex, query);
	}

	/**
	 * returns a JValueList of VariableDeclaration objects
	 */
	@Override
	public PVector<VariableDeclaration> evaluate(
			InternalGreqlEvaluator evaluator) {
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
					(VariableEvaluator) query.getVertexEvaluator(varInc
							.getAlpha()));
			varDeclList = varDeclList.plus(varDecl);
			varInc = varInc.getNextIsDeclaredVarOfIncidence(EdgeDirection.IN);
		}
		return varDeclList;
	}

	// @Override
	// public VertexCosts calculateSubtreeEvaluationCosts() {
	// return greqlEvaluator.getCostModel().calculateCostsSimpleDeclaration(
	// this);
	// }

	// @Override
	// public void calculateNeededAndDefinedVariables() {
	// neededVariables = new HashSet<Variable>();
	// definedVariables = new HashSet<Variable>();
	// IsDeclaredVarOf varInc = vertex
	// .getFirstIsDeclaredVarOfIncidence(EdgeDirection.IN);
	// while (varInc != null) {
	// definedVariables.add(varInc.getAlpha());
	// varInc = varInc.getNextIsDeclaredVarOfIncidence(EdgeDirection.IN);
	// }
	// IsTypeExprOf typeInc = vertex
	// .getFirstIsTypeExprOfIncidence(EdgeDirection.IN);
	// if (typeInc != null) {
	// VertexEvaluator veval = vertexEvalMarker
	// .getMark(typeInc.getAlpha());
	// if (veval != null) {
	// neededVariables.addAll(veval.getNeededVariables());
	// }
	// }
	// }
	//
	// @Override
	// public long calculateEstimatedCardinality() {
	// return greqlEvaluator.getCostModel()
	// .calculateCardinalitySimpleDeclaration(this);
	// }

}
