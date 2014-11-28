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

import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql.schema.TypeId;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;

/**
 * Creates a List of types out of the TypeId-Vertex.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class TypeIdEvaluator extends VertexEvaluator<TypeId> {
	private TypeCollection tc;

	public TypeIdEvaluator(TypeId vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	@Override
	public TypeCollection evaluate(InternalGreqlEvaluator evaluator) {
		if (tc == null) {
			tc = TypeCollection.empty().with(vertex.get_name(),
					vertex.is_type(), vertex.is_excluded());
		}
		try {
			tc = tc.bindToSchema(evaluator);
		} catch (UnknownTypeException e) {
			throw new UnknownTypeException(e.getTypeName(),
					createPossibleSourcePositions());
		}
		evaluator.progress(getOwnEvaluationCosts());
		return tc;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		OptimizerInfo optimizerInfo = query.getOptimizer().getOptimizerInfo();
		long costs = optimizerInfo.getEdgeClassCount()
				+ optimizerInfo.getVertexClassCount();
		return new VertexCosts(costs, costs, costs);
	}

	@Override
	public double calculateEstimatedSelectivity() {
		double selectivity;
		OptimizerInfo optimizerInfo = query.getOptimizer().getOptimizerInfo();
		if (tc != null) {
			selectivity = tc.getFrequency(optimizerInfo);
		} else {
			int typesInSchema = (int) Math
					.round((optimizerInfo.getEdgeClassCount() + optimizerInfo
							.getVertexClassCount()) / 2.0);
			selectivity = 1.0;
			TypeId id = getVertex();
			if (id.is_type()) {
				selectivity = 1.0 / typesInSchema;
			} else {
				double avgSubclasses = (optimizerInfo
						.getAverageEdgeSubclasses() + optimizerInfo
						.getAverageVertexSubclasses()) / 2.0;
				selectivity = avgSubclasses / typesInSchema;
			}
			if (id.is_excluded()) {
				selectivity = 1 - selectivity;
			}
		}
		logger.fine("TypeId estimated selectivity " + tc + ": " + selectivity);
		return selectivity;
	}

	@Override
	public String getLoggingName() {
		StringBuilder name = new StringBuilder();
		name.append(vertex.getAttributedElementClass().getQualifiedName());
		if (vertex.is_type()) {
			name.append("-type");
		}
		if (vertex.is_excluded()) {
			name.append("-excluded");
		}
		return name.toString();
	}

}
