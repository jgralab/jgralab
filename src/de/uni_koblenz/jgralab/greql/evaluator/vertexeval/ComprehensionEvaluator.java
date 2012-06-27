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
package de.uni_koblenz.jgralab.greql.evaluator.vertexeval;

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql.schema.Comprehension;
import de.uni_koblenz.jgralab.greql.schema.Declaration;
import de.uni_koblenz.jgralab.greql.schema.Expression;

public abstract class ComprehensionEvaluator<V extends Comprehension> extends
		VertexEvaluator<V> {

	private VariableDeclarationLayer varDeclLayer = null;
	private VertexEvaluator<? extends Expression> resultDefinitionEvaluator = null;
	protected long maxCount = Long.MAX_VALUE;

	public ComprehensionEvaluator(V vertex, QueryImpl query) {
		super(vertex, query);
	}

	protected abstract PCollection<Object> getResultDatastructure(
			InternalGreqlEvaluator evaluator);

	protected final VertexEvaluator<? extends Expression> getResultDefinitionEvaluator() {
		if (resultDefinitionEvaluator == null) {
			Expression resultDefinition = (Expression) getVertex()
					.getFirstIsCompResultDefOfIncidence(EdgeDirection.IN)
					.getAlpha();
			resultDefinitionEvaluator = query
					.getVertexEvaluator(resultDefinition);
		}
		return resultDefinitionEvaluator;
	}

	protected final VariableDeclarationLayer getVariableDeclationLayer(
			InternalGreqlEvaluator evaluator) {
		if (varDeclLayer == null) {
			Declaration d = (Declaration) getVertex()
					.getFirstIsCompDeclOfIncidence(EdgeDirection.IN).getAlpha();
			DeclarationEvaluator declEval = (DeclarationEvaluator) query
					.getVertexEvaluator(d);
			varDeclLayer = (VariableDeclarationLayer) declEval
					.getResult(evaluator);
		}
		return varDeclLayer;
	}

	protected final void initializeMaxCount(InternalGreqlEvaluator evaluator) {
		if (getVertex().get_maxCount() != null) {
			VertexEvaluator<? extends Expression> maxCountEval = query
					.getVertexEvaluator(getVertex().get_maxCount());
			maxCount = ((Number) maxCountEval.getResult(evaluator)).longValue();
		}
	}

	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		initializeMaxCount(evaluator);
		VariableDeclarationLayer declLayer = getVariableDeclationLayer(evaluator);
		VertexEvaluator<?> resultDefEval = getResultDefinitionEvaluator();
		PCollection<Object> resultCollection = getResultDatastructure(evaluator);
		declLayer.reset();
		while (declLayer.iterate(evaluator)
				&& (resultCollection.size() < maxCount)) {
			Object localResult = resultDefEval.getResult(evaluator);
			resultCollection = resultCollection.plus(localResult);
		}
		return resultCollection;
	}
}
