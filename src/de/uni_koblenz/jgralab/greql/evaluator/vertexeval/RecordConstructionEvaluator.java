/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.schema.IsPartOf;
import de.uni_koblenz.jgralab.greql.schema.IsRecordElementOf;
import de.uni_koblenz.jgralab.greql.schema.RecordConstruction;
import de.uni_koblenz.jgralab.greql.schema.RecordElement;
import de.uni_koblenz.jgralab.impl.RecordImpl;

/**
 * Evaluates a record construction, this is for instance rec( name:"element")
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class RecordConstructionEvaluator extends
		VertexEvaluator<RecordConstruction> {

	/**
	 * describes, how much interpretation steps it takes to add a element to a
	 * record
	 */
	protected static final int addToRecordCosts = 10;

	public RecordConstructionEvaluator(RecordConstruction vertex,
			GreqlQueryImpl query) {
		super(vertex, query);
	}

	@Override
	public Record evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		RecordImpl resultRecord = RecordImpl.empty();
		IsRecordElementOf inc = vertex
				.getFirstIsRecordElementOfIncidence(EdgeDirection.IN);
		while (inc != null) {
			RecordElement currentElement = inc.getAlpha();
			RecordElementEvaluator vertexEval = (RecordElementEvaluator) query
					.getVertexEvaluator(currentElement);
			resultRecord = resultRecord.plus(vertexEval.getId(),
					vertexEval.getResult(evaluator));
			inc = inc.getNextIsRecordElementOfIncidence(EdgeDirection.IN);
		}
		return resultRecord;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		RecordConstruction recCons = getVertex();
		IsPartOf inc = recCons.getFirstIsPartOfIncidence(EdgeDirection.IN);
		long recElems = 0;
		long recElemCosts = 0;
		while (inc != null) {
			RecordElement recElem = (RecordElement) inc.getAlpha();
			VertexEvaluator<RecordElement> veval = query
					.getVertexEvaluator(recElem);
			recElemCosts += veval.getCurrentSubtreeEvaluationCosts();
			recElems++;
			inc = inc.getNextIsPartOfIncidence(EdgeDirection.IN);
		}

		long ownCosts = (recElems * addToRecordCosts) + 2;
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = iteratedCosts + recElemCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public long calculateEstimatedCardinality() {
		RecordConstruction recCons = getVertex();
		IsRecordElementOf inc = recCons
				.getFirstIsRecordElementOfIncidence(EdgeDirection.IN);
		long parts = 0;
		while (inc != null) {
			parts++;
			inc = inc.getNextIsRecordElementOfIncidence(EdgeDirection.IN);
		}
		return parts;
	}

}
