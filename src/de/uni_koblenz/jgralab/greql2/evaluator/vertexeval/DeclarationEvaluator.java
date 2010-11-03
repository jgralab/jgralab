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
import java.util.Iterator;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclaration;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.SubgraphExpression;
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
	public JValue evaluate() throws EvaluateException {
		AbstractGraphMarker<AttributedElement> newSubgraph = null;
		Edge edge = vertex.getFirstIsSubgraphOf();
		if (edge != null) {
			SubgraphExpression subgraphExp = (SubgraphExpression) edge
					.getAlpha();
			if (subgraphExp != null) {
				VertexEvaluator subgraphEval = greqlEvaluator
						.getVertexEvaluatorGraphMarker().getMark(subgraphExp);
				JValue tempAttribute = subgraphEval.getResult(subgraph);
				if (tempAttribute.isGraphMarker()) {
					try {
						newSubgraph = tempAttribute.toGraphMarker();
					} catch (JValueInvalidTypeException exception) {
						throw new EvaluateException(
								"Error evaluating a Declaration : "
										+ exception.toString());
					}
				}
			}
		}
		if (newSubgraph == null) {
			newSubgraph = subgraph;
		}
		ArrayList<VertexEvaluator> constraintList = new ArrayList<VertexEvaluator>();
		for (IsConstraintOf consInc : vertex
				.getIsConstraintOfIncidences(EdgeDirection.IN)) {
			VertexEvaluator curEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker()
					.getMark(consInc.getAlpha());
			if (curEval != null) {
				constraintList.add(curEval);
			}
		}
		/* create list of VariableDeclaration objects */
		List<VariableDeclaration> varDeclList = new ArrayList<VariableDeclaration>();
		for (IsSimpleDeclOf inc : vertex
				.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) inc.getAlpha();
			SimpleDeclarationEvaluator simpleDeclEval = (SimpleDeclarationEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(simpleDecl);
			JValue simpleResult = simpleDeclEval.getResult(newSubgraph);
			JValueCollection resultCollection = simpleResult.toCollection();
			for (JValue v : resultCollection) {
				varDeclList.add((VariableDeclaration) v.toObject());
			}
		}
		VariableDeclarationLayer declarationLayer = new VariableDeclarationLayer(
				vertex, varDeclList, constraintList, evaluationLogger);
		return new JValueImpl(declarationLayer);
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
			VariableEvaluator veval = (VariableEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(iter.next());
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
