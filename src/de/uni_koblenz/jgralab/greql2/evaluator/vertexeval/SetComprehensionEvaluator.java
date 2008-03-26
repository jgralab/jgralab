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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.SetComprehension;

/**
 * Evaluates a SetComprehension vertex in the GReQL-2 Syntaxgraph
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class SetComprehensionEvaluator extends VertexEvaluator {

	/**
	 * The SetComprehension-Vertex this evaluator evaluates
	 */
	private SetComprehension vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * Creates a new SetComprehensionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public SetComprehensionEvaluator(SetComprehension vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		Declaration d = (Declaration) vertex.getFirstIsCompDeclOf(
				EdgeDirection.IN).getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(d);
		VariableDeclarationLayer declLayer = null;
		try {
			declLayer = declEval.getResult(subgraph).toDeclarationLayer();
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException("Error evaluating BagComprehension",
					exception);
		}
		Expression resultDef = (Expression) vertex.getFirstIsCompResultDefOf(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator resultDefEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(resultDef);
		JValueSet resultSet = new JValueSet();

		int noOfVarCombinations = 0;
		while (declLayer.iterate(subgraph)) {
			noOfVarCombinations++;
			resultSet.add(resultDefEval.getResult(subgraph));
		}

		return resultSet;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsSetComprehension(this, graphSize);
	}

	@Override
	public int calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCardinalitySetComprehension(this, graphSize);
	}

}
