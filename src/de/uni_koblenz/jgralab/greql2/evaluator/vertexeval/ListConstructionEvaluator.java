/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsPartOf;
import de.uni_koblenz.jgralab.greql2.schema.ListConstruction;

/**
 * Evaluates a ListConstruction vertex in the GReQL 2 syntaxgraph
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ListConstructionEvaluator extends
		ValueConstructionEvaluator<ListConstruction> {

	public ListConstructionEvaluator(ListConstruction vertex, QueryImpl query) {
		super(vertex, query);
	}

	@Override
	public PCollection<Object> evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		return createValue(JGraLab.vector(), evaluator);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		ListConstruction listCons = getVertex();
		IsPartOf inc = listCons.getFirstIsPartOfIncidence();
		long parts = 0;
		long partCosts = 0;
		while (inc != null) {
			VertexEvaluator<? extends Expression> veval = query
					.getVertexEvaluator((Expression) inc.getAlpha());
			partCosts += veval.getCurrentSubtreeEvaluationCosts();
			parts++;
			inc = inc.getNextIsPartOfIncidence();
		}

		long ownCosts = (parts * addToListCosts) + 2;
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = iteratedCosts + partCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public long calculateEstimatedCardinality() {
		ListConstruction listCons = getVertex();
		IsPartOf inc = listCons.getFirstIsPartOfIncidence();
		long parts = 0;
		while (inc != null) {
			parts++;
			inc = inc.getNextIsPartOfIncidence();
		}
		return parts;
	}

}
