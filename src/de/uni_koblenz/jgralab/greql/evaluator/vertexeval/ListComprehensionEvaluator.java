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

import java.util.ArrayList;
import java.util.List;

import org.pcollections.PCollection;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.schema.Declaration;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.IsTableHeaderOf;
import de.uni_koblenz.jgralab.greql.schema.ListComprehension;
import de.uni_koblenz.jgralab.greql.types.Table;

/**
 * Evaluates a ListComprehensionvertex in the GReQL-2 Syntaxgraph
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ListComprehensionEvaluator extends
		ComprehensionEvaluator<ListComprehension> {

	/**
	 * Creates a new ListComprehensionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public ListComprehensionEvaluator(ListComprehension vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	private Boolean createHeader = null;

	private List<VertexEvaluator<? extends Expression>> headerEvaluators = null;

	@Override
	protected PCollection<Object> getResultDatastructure(
			InternalGreqlEvaluator evaluator) {
		if (createHeader == null) {
			if (vertex.getFirstIsTableHeaderOfIncidence(EdgeDirection.IN) != null) {
				headerEvaluators = new ArrayList<VertexEvaluator<? extends Expression>>();
				createHeader = true;
				for (IsTableHeaderOf tableInc : vertex
						.getIsTableHeaderOfIncidences(EdgeDirection.IN)) {
					VertexEvaluator<? extends Expression> headerEval = query
							.getVertexEvaluator(tableInc
									.getAlpha());
					headerEvaluators.add(headerEval);
				}
			} else {
				createHeader = false;
			}
		}
		if (createHeader) {
			PVector<String> headerTuple = JGraLab.<String> vector();
			for (VertexEvaluator<? extends Expression> headerEvaluator : headerEvaluators) {
				headerTuple = headerTuple.plus((String) headerEvaluator
						.getResult(evaluator));
			}
			Table<Object> table = Table.empty();
			return table.withTitles(headerTuple);
		}
		return JGraLab.vector();
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		ListComprehension listComp = getVertex();
		Declaration decl = listComp
				.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) query
				.getVertexEvaluator(decl);
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts();

		Expression resultDef = listComp
				.getFirstIsCompResultDefOfIncidence().getAlpha();
		VertexEvaluator<? extends Expression> resultDefEval = query
				.getVertexEvaluator(resultDef);
		long resultCosts = resultDefEval.getCurrentSubtreeEvaluationCosts();

		long ownCosts = declEval.getEstimatedCardinality() * addToListCosts;
		long iteratedCosts = ownCosts * getVariableCombinations();
		long subtreeCosts = iteratedCosts + resultCosts + declCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public long calculateEstimatedCardinality() {
		ListComprehension listComp = getVertex();
		Declaration decl = listComp
				.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) query
				.getVertexEvaluator(decl);
		return declEval.getEstimatedCardinality();
	}

}
