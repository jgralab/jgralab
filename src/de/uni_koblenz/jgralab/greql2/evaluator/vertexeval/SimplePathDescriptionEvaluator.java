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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.IsEdgeRestrOf;
import de.uni_koblenz.jgralab.greql2.schema.SimplePathDescription;

/**
 * Evaluates a SimplePathDescription, that is something link v -->{isExprOf} w.
 * Creates a NFA which accepts the simplePath the vertex to evaluate describes.
 * 
 * @author ist@uni-koblenz.de Summer 2006, Diploma Thesis
 * 
 */
public class SimplePathDescriptionEvaluator extends
		PrimaryPathDescriptionEvaluator {

	public SimplePathDescriptionEvaluator(SimplePathDescription vertex,
			GreqlEvaluator eval) {
		super(vertex, eval);
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		JValueTypeCollection typeCollection = new JValueTypeCollection();
		EdgeRestrictionEvaluator edgeRestEval = null;
		VertexEvaluator predicateEvaluator = null;
		for (IsEdgeRestrOf inc : vertex
				.getIsEdgeRestrOfIncidences(EdgeDirection.IN)) {
			edgeRestEval = (EdgeRestrictionEvaluator) vertexEvalMarker
					.getMark(inc.getAlpha());
			typeCollection.addTypes(edgeRestEval.getTypeCollection());
			predicateEvaluator = edgeRestEval.getPredicateEvaluator();
		}
		createdNFA = NFA.createSimplePathDescriptionNFA(
				getEdgeDirection(vertex), typeCollection,
				getEdgeRoles(edgeRestEval), predicateEvaluator,
				vertexEvalMarker);
		return new JValueImpl(createdNFA);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsSimplePathDescription(this, graphSize);
	}

}
