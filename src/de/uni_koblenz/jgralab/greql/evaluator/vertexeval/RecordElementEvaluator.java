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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.IsRecordExprOf;
import de.uni_koblenz.jgralab.greql.schema.RecordElement;
import de.uni_koblenz.jgralab.greql.schema.RecordId;

/**
 * Evaluates a record element, this is for instance name:"element" in the
 * record-construction rec( name:"element")
 * 
 * @author ist@uni-koblenz.de November 2006
 * 
 */
public class RecordElementEvaluator extends VertexEvaluator<RecordElement> {

	private String id = null;

	private VertexEvaluator<? extends Expression> expEval = null;

	public String getId() {
		if (id == null) {
			RecordId idVertex = vertex.getFirstIsRecordIdOfIncidence(
					EdgeDirection.IN).getAlpha();
			id = idVertex.get_name();
		}
		return id;
	}

	public RecordElementEvaluator(RecordElement vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		if (expEval == null) {
			Expression recordElementExp = vertex
					.getFirstIsRecordExprOfIncidence(EdgeDirection.IN)
					.getAlpha();
			expEval = query.getVertexEvaluator(recordElementExp);
		}
		return expEval.getResult(evaluator);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		RecordElement recElem = getVertex();

		IsRecordExprOf inc = recElem.getFirstIsRecordExprOfIncidence();
		VertexEvaluator<? extends Expression> veval = query
				.getVertexEvaluator(inc.getAlpha());
		long recordExprCosts = veval.getCurrentSubtreeEvaluationCosts();

		long ownCosts = 3;
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = recordExprCosts + iteratedCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

}
