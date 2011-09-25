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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclaration;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * Evaluates a Declaration vertex in the GReQL-2 Syntaxgraph
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class DeclarationEvaluator extends VertexEvaluator {

	/**
	 * This is the declaration vertex
	 */
	private Declaration vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	/**
	 * @param eval
	 *            the DeclarationEvaluator this VertexEvaluator belongs to
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public DeclarationEvaluator(Declaration vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public VariableDeclarationLayer evaluate() {
		ArrayList<VertexEvaluator> constraintList = new ArrayList<VertexEvaluator>();
		for (IsConstraintOf consInc : vertex
				.getIsConstraintOfIncidences(EdgeDirection.IN)) {
			VertexEvaluator curEval = vertexEvalMarker.getMark(consInc
					.getAlpha());
			if (curEval != null) {
				constraintList.add(curEval);
			}
		}
		/* create list of VariableDeclaration objects */
		List<VariableDeclaration> varDeclList = new ArrayList<VariableDeclaration>();
		for (IsSimpleDeclOf inc : vertex
				.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) inc.getAlpha();
			SimpleDeclarationEvaluator simpleDeclEval = (SimpleDeclarationEvaluator) vertexEvalMarker
					.getMark(simpleDecl);
			@SuppressWarnings("unchecked")
			PVector<VariableDeclaration> resultCollection = (PVector<VariableDeclaration>) simpleDeclEval
					.getResult();
			for (VariableDeclaration v : resultCollection) {
				varDeclList.add(v);
			}
		}
		VariableDeclarationLayer declarationLayer = new VariableDeclarationLayer(
				vertex, varDeclList, constraintList);
		return declarationLayer;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateCostsDeclaration(
				this, graphSize);
	}

	/**
	 * Returns the number of combinations of the variables this vertex defines
	 */
	public long getDefinedVariableCombinations(GraphSize graphSize) {
		long combinations = 1;
		Iterator<Variable> iter = getDefinedVariables().iterator();
		while (iter.hasNext()) {
			VariableEvaluator veval = (VariableEvaluator) vertexEvalMarker
					.getMark(iter.next());
			combinations *= veval.getVariableCombinations(graphSize);
		}
		return combinations;
	}

	@Override
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel().calculateCardinalityDeclaration(
				this, graphSize);
	}

}
