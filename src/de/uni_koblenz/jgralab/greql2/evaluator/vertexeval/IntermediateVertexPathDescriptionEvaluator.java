/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IntermediateVertexPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.IsSubPathOf;

/**
 * Evaluates an IntermediateVertexPathDescription.
 * 
 * @author ist@uni-koblenz.de Summer 2006, Diploma Thesis
 * 
 */
public class IntermediateVertexPathDescriptionEvaluator extends
		PathDescriptionEvaluator {

	/**
	 * The IntermediateVertexPathDescription-Vertex this evaluator evaluates
	 */
	private IntermediateVertexPathDescription vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	/**
	 * Creates a new IntermediateVertexPathDescriptionEvaluator for the given
	 * vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public IntermediateVertexPathDescriptionEvaluator(
			IntermediateVertexPathDescription vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		IsSubPathOf inc = vertex.getFirstIsSubPathOf(EdgeDirection.IN);
		PathDescriptionEvaluator firstEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
		NFA firstNFA = firstEval.getNFA();
		inc = inc.getNextIsSubPathOf(EdgeDirection.IN);
		PathDescriptionEvaluator secondEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
		NFA secondNFA = secondEval.getNFA();
		VertexEvaluator vertexEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						vertex.getFirstIsIntermediateVertexOf(EdgeDirection.IN)
								.getAlpha());
		return new JValueImpl(NFA.createIntermediateVertexPathDescriptionNFA(
				firstNFA, vertexEval, secondNFA));
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsIntermediateVertexPathDescription(this,
						graphSize);
	}

}