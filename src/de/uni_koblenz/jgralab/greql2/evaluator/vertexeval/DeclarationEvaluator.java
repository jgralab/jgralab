/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.greql2.evaluator.*;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.*;
import de.uni_koblenz.jgralab.greql2.schema.*;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Evaluates a Declaration vertex in the GReQL-2 Syntaxgraph
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
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
	public Vertex getVertex() {
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
		BooleanGraphMarker newSubgraph = null;
		Edge edge = vertex.getFirstIsSubgraphOf();
		if (edge != null) {
			SubgraphExpression subgraphExp = (SubgraphExpression) edge
					.getAlpha();
			if (subgraphExp != null) {
				VertexEvaluator subgraphEval = greqlEvaluator
						.getVertexEvaluatorGraphMarker().getMark(subgraphExp);
				JValue tempAttribute = subgraphEval.getResult(subgraph);
				if (tempAttribute.isSubgraphTempAttribute()) {
					try {
						newSubgraph = tempAttribute.toSubgraphTempAttribute();
					} catch (JValueInvalidTypeException exception) {
						throw new EvaluateException(
								"Error evaluating a Declaration : "
										+ exception.toString());
					}
				}
			}
		}
		if (newSubgraph == null)
			newSubgraph = subgraph;
		ArrayList<VertexEvaluator> constraintList = new ArrayList<VertexEvaluator>();
		IsConstraintOf consInc = vertex
				.getFirstIsConstraintOf(EdgeDirection.IN);
		while (consInc != null) {
			VertexEvaluator curEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker()
					.getMark(consInc.getAlpha());
			if (curEval != null)
				constraintList.add(curEval);
			consInc = consInc.getNextIsConstraintOf(EdgeDirection.IN);
		}
		VariableDeclarationLayer declarationLayer = new VariableDeclarationLayer(
				constraintList, evaluationLogger);
		IsSimpleDeclOf inc = vertex.getFirstIsSimpleDeclOf(EdgeDirection.IN);

		while (inc != null) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) inc.getAlpha();
			SimpleDeclarationEvaluator simpleDeclEval = (SimpleDeclarationEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(simpleDecl);
			try {
				JValue simpleResult = simpleDeclEval.getResult(newSubgraph);
				if (simpleResult == null)
					throw new EvaluateException(
							"Error creating variable declaration layer, one of the simple declarations returned a nullpointer");
				JValueCollection resultCollection = simpleResult.toCollection();
				Iterator<JValue> iter = resultCollection.iterator();
				while (iter.hasNext()) {
					VariableDeclaration varDecl = iter.next()
							.toVariableDeclaration();
					declarationLayer.addVariableDeclaration(varDecl);
				}
			} catch (JValueInvalidTypeException ex) {
				throw new EvaluateException(
						"Error creating variable declaration layer, one of the simple declarations returned an invalid value",
						ex);
			}
			inc = inc.getNextIsSimpleDeclOf(EdgeDirection.IN);
		}

		return new JValue(declarationLayer);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateCostsDeclaration(
				this, graphSize);
	}

	/**
	 * Returns the number of combinations of the variables this vertex defines
	 */
	public int getDefinedVariableCombinations(GraphSize graphSize) {
		int combinations = 1;
		Iterator<Variable> iter = getDefinedVariables().iterator();
		while (iter.hasNext()) {
			VariableEvaluator veval = (VariableEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(iter.next());
			combinations *= veval.getVariableCombinations(graphSize);
		}
		return combinations;
	}

	@Override
	public int calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel().calculateCardinalityDeclaration(
				this, graphSize);
	}

}
