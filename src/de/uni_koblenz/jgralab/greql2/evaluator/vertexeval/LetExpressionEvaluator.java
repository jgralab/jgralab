/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.Definition;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsDefinitionOf;
import de.uni_koblenz.jgralab.greql2.schema.LetExpression;

/**
 * Evaluates a LetExpression vertex in the GReQL-2 Syntaxgraph. A defined
 * variable v is valid in the bound expression and in all variables z that are
 * defined right of v
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class LetExpressionEvaluator extends DefinitionExpressionEvaluator {

	/**
	 * Creates a new LetExpressionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public LetExpressionEvaluator(LetExpression vertex, GreqlEvaluator eval) {
		super(vertex, eval);
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		IsDefinitionOf inc = vertex.getFirstIsDefinitionOf(EdgeDirection.IN);
		while (inc != null) {
			Definition currentDefinition = (Definition) inc.getAlpha();
			DefinitionEvaluator definEval = (DefinitionEvaluator) greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(currentDefinition);
			definEval.getResult(subgraph);
			inc = inc.getNextIsDefinitionOf(EdgeDirection.IN);
		}
		Expression boundExp = (Expression) vertex
				.getFirstIsBoundExprOf(EdgeDirection.IN).getAlpha();
		VertexEvaluator boundExpEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(boundExp);
		return boundExpEval.getResult(subgraph);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateCostsLetExpression(
				this, graphSize);
	}

}
