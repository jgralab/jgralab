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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.EdgePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeRestrOf;

/**
 * Creates a NFA wich accepts a single edge out of the --edge-> - clause
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EdgePathDescriptionEvaluator extends
		PrimaryPathDescriptionEvaluator {

	public EdgePathDescriptionEvaluator(EdgePathDescription vertex,
			GreqlEvaluator eval) {
		super(vertex, eval);
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		Edge evalEdge = vertex.getFirstIsEdgeExprOf();
		VertexEvaluator edgeEval = null;
		if (evalEdge != null) {
			edgeEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(
					evalEdge.getAlpha());
		}
		JValueTypeCollection typeCollection = new JValueTypeCollection();
		IsTypeRestrOf inc = vertex.getFirstIsTypeRestrOf(EdgeDirection.IN);
		EdgeRestrictionEvaluator edgeRestEval = null;
		VertexEvaluator predicateEvaluator = null;
		if (inc != null) {
			edgeRestEval = (EdgeRestrictionEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			typeCollection.addTypes(edgeRestEval.getTypeCollection());
			predicateEvaluator = edgeRestEval.getPredicateEvaluator();
		}
		createdNFA = NFA.createEdgePathDescriptionNFA(getEdgeDirection(vertex),
				typeCollection, getEdgeRoles(edgeRestEval), edgeEval, predicateEvaluator, greqlEvaluator.getVertexEvaluatorGraphMarker());
		return new JValueImpl(createdNFA);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsEdgePathDescription(this, graphSize);
	}

}
