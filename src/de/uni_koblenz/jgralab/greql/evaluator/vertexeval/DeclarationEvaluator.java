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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VariableDeclaration;
import de.uni_koblenz.jgralab.greql.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.schema.Declaration;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql.schema.Variable;

/**
 * Evaluates a Declaration vertex in the GReQL-2 Syntaxgraph
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class DeclarationEvaluator extends VertexEvaluator<Declaration> {

	/**
	 * A factor that will be multiplied with the number of variable combinations
	 * to estimate the own costs of a {@link Declaration}.
	 */
	protected static final int declarationCostsFactor = 5;

	/**
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public DeclarationEvaluator(Declaration vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	@Override
	public VariableDeclarationLayer evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		ArrayList<VertexEvaluator<? extends Expression>> constraintList = new ArrayList<>();
		for (IsConstraintOf consInc : vertex
				.getIsConstraintOfIncidences(EdgeDirection.IN)) {
			VertexEvaluator<? extends Expression> curEval = query
					.getVertexEvaluator(consInc.getAlpha());
			if (curEval != null) {
				constraintList.add(curEval);
			}
		}
		/* create list of VariableDeclaration objects */
		List<VariableDeclaration> varDeclList = new ArrayList<>();
		for (IsSimpleDeclOf inc : vertex
				.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			SimpleDeclaration simpleDecl = inc.getAlpha();
			SimpleDeclarationEvaluator simpleDeclEval = (SimpleDeclarationEvaluator) query
					.getVertexEvaluator(simpleDecl);
			@SuppressWarnings("unchecked")
			PVector<VariableDeclaration> resultCollection = (PVector<VariableDeclaration>) simpleDeclEval
					.getResult(evaluator);
			for (VariableDeclaration v : resultCollection) {
				varDeclList.add(v);
			}
		}
		VariableDeclarationLayer declarationLayer = new VariableDeclarationLayer(
				vertex, varDeclList, constraintList);
		return declarationLayer;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		Declaration decl = getVertex();

		IsSimpleDeclOf inc = decl.getFirstIsSimpleDeclOfIncidence();
		long simpleDeclCosts = 0;
		while (inc != null) {
			SimpleDeclaration simpleDecl = inc.getAlpha();
			SimpleDeclarationEvaluator simpleEval = (SimpleDeclarationEvaluator) query
					.getVertexEvaluator(simpleDecl);
			simpleDeclCosts += simpleEval.getCurrentSubtreeEvaluationCosts();
			inc = inc.getNextIsSimpleDeclOfIncidence();
		}

		IsConstraintOf consInc = decl.getFirstIsConstraintOfIncidence();
		long constraintsCosts = 0;
		while (consInc != null) {
			VertexEvaluator<? extends Expression> constraint = query
					.getVertexEvaluator(consInc.getAlpha());
			constraintsCosts += constraint.getCurrentSubtreeEvaluationCosts();
			consInc = consInc.getNextIsConstraintOfIncidence();
		}

		long iterationCosts = getDefinedVariableCombinations()
				* declarationCostsFactor;
		long ownCosts = iterationCosts + 2;
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = iteratedCosts + constraintsCosts + simpleDeclCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/**
	 * Returns the number of combinations of the variables this vertex defines
	 */
	public long getDefinedVariableCombinations() {
		long combinations = 1;
		Iterator<Variable> iter = getDefinedVariables().iterator();
		while (iter.hasNext()) {
			VariableEvaluator<? extends Variable> veval = (VariableEvaluator<? extends Variable>) query
					.getVertexEvaluator(iter.next());
			combinations *= veval.getVariableCombinations();
		}
		return combinations;
	}

	@Override
	public long calculateEstimatedCardinality() {
		Declaration decl = getVertex();
		IsConstraintOf inc = decl
				.getFirstIsConstraintOfIncidence(EdgeDirection.IN);
		double selectivity = 1.0;
		while (inc != null) {
			VertexEvaluator<? extends Expression> constEval = query
					.getVertexEvaluator(inc.getAlpha());
			selectivity *= constEval.getEstimatedSelectivity();
			inc = inc.getNextIsConstraintOfIncidence(EdgeDirection.IN);
		}
		return Math.round(getDefinedVariableCombinations() * selectivity);
	}

}
